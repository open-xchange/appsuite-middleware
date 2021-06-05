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

package com.openexchange.drive.json.action;

import static com.openexchange.configuration.ServerConfig.Property.FORCE_HTTPS;
import static com.openexchange.login.ConfigurationProperty.HTTP_AUTH_CLIENT;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginRequestImpl;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.authentication.Cookie;
import com.openexchange.authentication.application.AppPasswordUtils;
import com.openexchange.config.ConfigurationService;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.login.Interface;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.AuthCookie;

/**
 * {@link JumpAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class JumpAction extends AbstractDriveAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JumpAction.class);

    public JumpAction() {
        super();
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException {
        DriveService driveService = getDriveService();
        try {
            /*
             * get parameters
             */
            String authId = requestData.getParameter("authId");
            if (Strings.isEmpty(authId)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("authId");
            }
            String clientToken = requestData.getParameter("clientToken");
            if (Strings.isEmpty(clientToken)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("clientToken");
            }
            String path = requestData.getParameter("path");
            if (Strings.isEmpty(path)) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create("path");
            }
            String name = requestData.getParameter("name");
            String method = requestData.getParameter("method");
            if (Strings.isEmpty(method)) {
                method = "preview";
            }
            // TODO Check for possibilities that the client can determine the User-Agent from the browser and transfer it to create a more
            // secure session.
            String userAgent = requestData.getUserAgent();
            /*
             * obtain redirect URL from drive service, implicitly checking the existence of the target
             */
            String link = driveService.getJumpRedirectUrl(session, path, name, method);
            /*
             * attempt to perform token based login & obtain corresponding server token if applicable
             */
            String serverToken = null;
            if (attemptTokenLogin(session)) {
                HttpServletRequest request = requestData.optHttpServletRequest();
                if (null == request) {
                    throw DriveExceptionCodes.IO_ERROR.create("Request must not be null");
                }
                Cookie[] cookies = getCookies(request);
                Map<String, List<String>> headers = getHeaders(request);
                String client = getClient();
                String hash = HashCalculator.getInstance().getHash(request, requestData.getUserAgent(), client);
                boolean forceHTTPS = com.openexchange.tools.servlet.http.Tools.considerSecure(request, forceHTTPS());
                LoginRequestImpl req = new LoginRequestImpl(session.getServerSession().getLogin(), session.getServerSession().getPassword(), session.getServerSession().getLocalIp(), userAgent, authId, client, "Drive Jump", hash, Interface.HTTP_JSON, headers, request.getParameterMap(), cookies, forceHTTPS, request.getServerName(), request.getServerPort(), request.getSession(false));
                req.setClientToken(clientToken);
                try {
                    serverToken = LoginPerformer.getInstance().doLogin(req).getServerToken();
                } catch (OXException e) {
                    LOG.debug("Unable to spawn jump session for {}, falling back to plain direct link.", session, e);
                }
            }
            /*
             * return jump response holding the redirect URL or just the link as JSON
             */
            JSONObject jsonObject = new JSONObject();
            if (Strings.isEmpty(serverToken)) {
                jsonObject.put("redirectUrl", link);
                jsonObject.put("appendClientToken", false);
            } else {
                jsonObject.put("redirectUrl", new StringBuilder(link).append("&serverToken=").append(serverToken).toString());
                jsonObject.put("appendClientToken", true);
            }
            return new AJAXRequestResult(jsonObject, "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static boolean attemptTokenLogin(DriveSession session) throws OXException {
        if (Strings.isEmpty(session.getServerSession().getPassword()) || AppPasswordUtils.isRestricted(session.getServerSession())) {
            return false;
        }
        ServerConfigService serverConfigService = Services.getService(ServerConfigService.class);
        if (null != serverConfigService) {
            ServerConfig serverConfig = serverConfigService.getServerConfig(session.getHostData().getHost(), session.getServerSession());
            Map<String, Object> clientConfig = serverConfig.forClient();
            if (Boolean.TRUE.equals(clientConfig.get("oidcLogin")) || Boolean.TRUE.equals(clientConfig.get("samlLogin"))) {
                return false;
            }
        }
        return true;
    }

    private static Cookie[] getCookies(HttpServletRequest req) {
        final List<Cookie> cookies;
        if (null == req) {
            cookies = Collections.emptyList();
        } else {
            cookies = new ArrayList<Cookie>();
            for (final javax.servlet.http.Cookie c : req.getCookies()) {
                cookies.add(new AuthCookie(c));
            }
        }
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    private static Map<String, List<String>> getHeaders(HttpServletRequest req) {
        final Map<String, List<String>> headers;
        if (null == req) {
            headers = Collections.emptyMap();
        } else {
            headers = new HashMap<String, List<String>>();
            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                List<String> header = new ArrayList<String>();
                if (headers.containsKey(name)) {
                    header = headers.get(name);
                }
                header.add(req.getHeader(name));
                headers.put(name, header);
            }
        }
        return headers;
    }

    private String getClient() throws OXException {
        ConfigurationService configService = getConfigService();
        return configService.getProperty(HTTP_AUTH_CLIENT.getPropertyName(), HTTP_AUTH_CLIENT.getDefaultValue());
    }

    private boolean forceHTTPS() throws OXException {
        ConfigurationService configService = getConfigService();
        return Boolean.parseBoolean(configService.getProperty(FORCE_HTTPS.getPropertyName(), FORCE_HTTPS.getDefaultValue()));
    }
}
