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

package com.openexchange.drive.json.action;

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
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.login.ConfigurationProperty;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.AuthCookie;

/**
 * {@link JumpAction}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class JumpAction extends AbstractDriveAction {

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
             * perform token based login & obtain corresponding server token
             */
            HttpServletRequest request = requestData.optHttpServletRequest();
            if (null == request) {
                throw DriveExceptionCodes.IO_ERROR.create("Request must not be null");
            }
            Cookie[] cookies = getCookies(request);
            Map<String, List<String>> headers = getHeaders(request);
            String client = getClient();
            String hash = HashCalculator.getInstance().getHash(request, requestData.getUserAgent(), client);
            boolean forceHTTPS = com.openexchange.tools.servlet.http.Tools.considerSecure(request, forceHTTPS());
            LoginRequestImpl req = new LoginRequestImpl(session.getServerSession().getLogin(), session.getServerSession().getPassword(), session.getServerSession().getLocalIp(), userAgent, authId, client, "Drive Jump", hash, Interface.HTTP_JSON, headers, cookies, forceHTTPS, request.getServerName(), request.getServerPort(), requestData.getRoute());
            req.setClientToken(clientToken);
            LoginResult res = LoginPerformer.getInstance().doLogin(req);
            String serverToken = res.getServerToken();
            /*
             * return jump response holding the redirect URL as JSON
             */
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("redirectUrl", new StringBuilder(link).append("&serverToken=").append(serverToken).toString());
            return new AJAXRequestResult(jsonObject, "json");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
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
        return configService.getProperty(ConfigurationProperty.HTTP_AUTH_CLIENT.getPropertyName(), ConfigurationProperty.HTTP_AUTH_CLIENT.getDefaultValue());
    }

    private boolean forceHTTPS() throws OXException {
        ConfigurationService configService = getConfigService();
        return Boolean.parseBoolean(configService.getProperty(Property.FORCE_HTTPS.getPropertyName(), Property.FORCE_HTTPS.getDefaultValue()));
    }
}
