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

package com.openexchange.session.management.json.actions;

import java.util.Collection;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.clientinfo.ClientInfo;
import com.openexchange.clientinfo.ClientInfoService;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.management.ManagedSession;
import com.openexchange.session.management.SessionManagementService;
import com.openexchange.session.management.SessionManagementStrings;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AllAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class AllAction implements AJAXActionService {

    private final ServiceLookup services;

    public AllAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        SessionManagementService service = services.getService(SessionManagementService.class);
        if (null == service) {
            throw ServiceExceptionCode.absentService(SessionManagementService.class);
        }
        Locale locale = session.getUser().getLocale();
        String unknownLocation = SessionManagementStrings.UNKNOWN_LOCATION;
        StringHelper helper = StringHelper.valueOf(locale);
        if (null != helper) {
            unknownLocation = helper.getString(SessionManagementStrings.UNKNOWN_LOCATION);
        }
        Collection<ManagedSession> sessions = service.getSessionsForUser(session);
        JSONArray browsers = new JSONArray();
        JSONArray oxapps = new JSONArray();
        JSONArray syncapps = new JSONArray();
        JSONArray others = new JSONArray();
        JSONArray result = new JSONArray();
        try {
            for (ManagedSession s : sessions) {
                JSONObject json = new JSONObject(7);
                json.put("sessionId", s.getSessionId());
                json.put("ipAddress", s.getIpAddress());
                json.put("client", s.getClient());
                json.put("userAgent", s.getUserAgent());
                String location = s.getLocation();
                if (Strings.isNotEmpty(location) && !unknownLocation.equals(location)) {
                    json.put("location", s.getLocation());
                }
                long loginTime = s.getLoginTime();
                if (0 < loginTime) {
                    json.put("loginTime", loginTime);
                }
                long lastActive = s.getLastActive();
                if (0 < lastActive) {
                    if (lastActive > loginTime) {
                        json.put("lastActive", lastActive);
                    } else {
                        json.put("lastActive", loginTime);
                    }
                }
                JSONObject deviceInfo = getDeviceInfo(s, locale);
                if (null != deviceInfo) {
                    json.put("device", deviceInfo);
                    String type = deviceInfo.getJSONObject("client").getString("type");
                    switch (type) {
                        case "browser":
                            browsers.add(0, json);
                            break;
                        case "oxapp":
                            oxapps.add(0, json);
                            break;
                        case "sync":
                            syncapps.add(0, json);
                            break;
                        default:
                            others.add(0, json);
                            break;
                    }
                }
            }
            for (int i = 0; i < browsers.length(); i++) {
                result.add(result.length(), browsers.get(i));
            }
            for (int i = 0; i < oxapps.length(); i++) {
                result.add(result.length(), oxapps.get(i));
            }
            for (int i = 0; i < syncapps.length(); i++) {
                result.add(result.length(), syncapps.get(i));
            }
            for (int i = 0; i < others.length(); i++) {
                result.add(result.length(), others.get(i));
            }
        } catch (JSONException e) {
            // should not happen
        }
        return new AJAXRequestResult(result, "json");
    }

    private JSONObject getDeviceInfo(ManagedSession session, Locale locale) {
        JSONObject deviceInfo = new JSONObject(3);
        try {
            ClientInfoService service = services.getService(ClientInfoService.class);
            if (null != service) {
                ClientInfo info = service.getClientInfo(session.getSession());
                if (null != info) {
                    deviceInfo.put("displayName", info.getDisplayName(locale));
                    JSONObject os = new JSONObject(2);
                    os.put("name", info.getOSFamily());
                    os.put("version", info.getOSVersion());
                    deviceInfo.put("os", os);
                    JSONObject client = new JSONObject(3);
                    client.put("name", info.getClientName());
                    client.put("version", info.getClientVersion());
                    client.put("type", info.getType().getName());
                    deviceInfo.put("client", client);
                    return deviceInfo;
                }
            }
            StringHelper helper = StringHelper.valueOf(locale);
            deviceInfo.put("displayName", helper.getString(SessionManagementStrings.UNKNOWN_DEVICE));
            JSONObject client = new JSONObject(1);
            client.put("type", "other");
            deviceInfo.put("client", client);
            return deviceInfo;
        } catch (JSONException e) {
            // will not happen
        }
        return deviceInfo;
    }

}
