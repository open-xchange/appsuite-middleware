/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
