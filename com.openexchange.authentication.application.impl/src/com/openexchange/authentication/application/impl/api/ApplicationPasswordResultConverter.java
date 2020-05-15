/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.authentication.application.impl.api;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.authentication.application.ApplicationPassword;
import com.openexchange.authentication.application.impl.AppPasswordServiceImpl;
import com.openexchange.authentication.application.storage.history.AppPasswordLogin;
import com.openexchange.exception.OXException;
import com.openexchange.geolocation.GeoInformation;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ApplicationPasswordResultConverter}
 * Convert a LimitingPassword Result to JSON for output
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class ApplicationPasswordResultConverter implements ResultConverter {

    public static final String INPUT_FORMAT = "ApplicationPassword";
    private static final String OUTPUT_FORMAT = "json";

    private static final Logger LOG = LoggerFactory.getLogger(AppPasswordServiceImpl.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ApplicationPasswordResultConverter}.
     * 
     * @param services A service lookup reference
     */
    public ApplicationPasswordResultConverter(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getInputFormat() {
        return INPUT_FORMAT;
    }

    @Override
    public String getOutputFormat() {
        return OUTPUT_FORMAT;
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @SuppressWarnings("unused")
    @Override
    public void convert(AJAXRequestData requestData, AJAXRequestResult result, ServerSession session, Converter converter) throws OXException {
        Object resultObject = result.getResultObject();
        if (false == (resultObject instanceof Collection)) {
            throw new UnsupportedOperationException();
        }
        @SuppressWarnings("unchecked") Collection<Entry<ApplicationPassword, AppPasswordLogin>> passwordInfos = (Collection<Entry<ApplicationPassword, AppPasswordLogin>>) resultObject;
        try {
            JSONArray json = new JSONArray(passwordInfos.size());
            for (Entry<ApplicationPassword, AppPasswordLogin> p : passwordInfos) {
                json.put(toJSON(session, p.getKey(), p.getValue()));
            }
            result.setResultObject(json);
        } catch (JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }

    /**
     * Simple mapping of only UUID and permission list from passwords
     * Rest would be sensitive information
     *
     * @param session The session
     * @param applicationPassword The application password
     * @param lastLogin The last login, or <code>null</code> if there was none
     * @return JSON Object representing the password
     * @throws JSONException if a JSON error is occurred
     */
    private JSONObject toJSON(Session session, ApplicationPassword applicationPassword, AppPasswordLogin lastLogin) throws JSONException {
        JSONObject result = new JSONObject();
        result.put("UUID", applicationPassword.getGUID().toString());
        result.putSafe("Scope", applicationPassword.getAppType());
        result.putSafe("Name", applicationPassword.getName());
        if (null != lastLogin) {
            result.putSafe("LastDevice", lastLogin.getClient());
            result.put("LastLogin", lastLogin.getTimestamp());
            result.putSafe("IP", lastLogin.getIpAddress());
            result.putSafe("GeoData", getGeolocation(session.getContextId(), lastLogin.getIpAddress()));
        }
        return result;
    }

    /**
     * Check if geoLocation service installed and if has record for the IP address.
     * Returns "City, Country" if found
     * getGeolocation
     *
     * @param contextId The contextId of the user
     * @param ipAddress String representation of the IP address
     * @return String of city,country for the location of the IP address
     */
    private String getGeolocation(int contextId, String ipAddress) {
        if (ipAddress != null) {
            GeoLocationService geoService = services.getService(GeoLocationService.class);
            if (geoService != null) {
                try {
                    InetAddress addr = InetAddress.getByName(ipAddress);
                    if (addr.isSiteLocalAddress() || addr.isLinkLocalAddress() || addr.isLoopbackAddress()) {
                        return null;
                    }
                    GeoInformation location = geoService.getGeoInformation(contextId, addr);
                    if (location.hasCountry()) {  // Minimum required data for is country
                        StringBuilder sb = new StringBuilder();
                        if (location.hasCity()) {
                            sb.append(location.getCity());
                            sb.append(", ");
                        }
                        sb.append(location.getCountry());
                        return sb.toString();
                    }
                } catch (UnknownHostException | OXException ex) {
                    LOG.error("Error utilizing geolocation service for application password history", ex);
                }
            }
        }
        return null;
    }

}
