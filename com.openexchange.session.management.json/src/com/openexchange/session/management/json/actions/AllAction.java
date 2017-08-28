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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.management.ManagedSession;
import com.openexchange.session.management.SessionManagementService;
import com.openexchange.session.management.SessionManagementStrings;
import com.openexchange.session.management.json.osgi.Services;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.uadetector.UserAgentParser;
import net.sf.uadetector.OperatingSystem;
import net.sf.uadetector.ReadableUserAgent;

/**
 * {@link AllAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class AllAction implements AJAXActionService {

    //private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AllAction.class);

    public AllAction() {
        super();
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        SessionManagementService service = Services.getService(SessionManagementService.class);
        if (null == service) {
            throw ServiceExceptionCode.absentService(SessionManagementService.class);
        }
        Collection<ManagedSession> sessions = service.getSessionsForUser(session);
        JSONArray result = new JSONArray(sessions.size());
        try {
            for (ManagedSession s : sessions) {
                JSONObject json = new JSONObject(7);
                json.put("sessionId", s.getSessionId());
                json.put("ipAddress", s.getIpAddress());
                json.put("client", s.getClient());
                json.put("userAgent", s.getUserAgent());
                String location = s.getLocation();
                if (Strings.isNotEmpty(location) && !SessionManagementStrings.UNKNOWN_LOCATION.equals(location)) {
                    json.put("location", s.getLocation());
                }
                long loginTime = s.getLoginTime();
                if (0 < loginTime) {
                    json.put("loginTime", loginTime);
                }
                JSONObject deviceInfo = getDeviceInfo(s.getUserAgent());
                if (null != deviceInfo) {
                    json.put("deviceInfo", deviceInfo);
                }
                result.add(0, json);
            }
        } catch (JSONException e) {
            // should not happen
        }
        return new AJAXRequestResult(result, "json");
    }

    private JSONObject getDeviceInfo(String userAgent) {
        UserAgentParser parser = Services.getService(UserAgentParser.class);
        if (null == parser) {
            return null;
        }

        ReadableUserAgent info = parser.parse(userAgent);
        OperatingSystem operatingSystem = info.getOperatingSystem();
        String os = null;
        String osVersion = null;
        if (null != operatingSystem) {
            os = operatingSystem.getName();
            osVersion = operatingSystem.getVersionNumber().getMajor();
        }
        String browser = info.getName();
        String browserVersion = info.getVersionNumber().getMajor();
        if (Strings.isNotEmpty(os) && Strings.isNotEmpty(osVersion) && Strings.isNotEmpty(browser) || Strings.isNotEmpty(browserVersion)) {
            JSONObject deviceInfo = new JSONObject(5);
            try {
                if (Strings.isNotEmpty(os)) {
                    deviceInfo.put("os", os);
                }
                if (Strings.isNotEmpty(osVersion)) {
                    deviceInfo.put("osVersion", osVersion);
                }
                if (Strings.isNotEmpty(browser)) {
                    deviceInfo.put("browser", browser);
                }
                if (Strings.isNotEmpty(browserVersion)) {
                    deviceInfo.put("browserVersion", browserVersion);
                }
            } catch (JSONException e) {
                // will not happen
            }
            return deviceInfo;
        }
        return null;
    }

}
