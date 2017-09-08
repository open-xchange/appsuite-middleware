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

package com.openexchange.oidc.tools;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.tools.servlet.http.Cookies;

public class OIDCTools {
    
    private static final Logger LOG = LoggerFactory.getLogger(OIDCTools.class);

    public static final String SESSION_TOKEN = "sessionToken";
    
    public static final String OIDC_LOGIN = "oidcLogin";

    public static final String IDTOKEN = "__session.oidc.idToken";

    public static final String TYPE = "type";
    
    public static final String END = "end";

    public static final String STATE = "state";

    public static final String AUTOLOGIN_COOKIE_PREFIX = "open-xchange-oidc-";

    public static final String SESSION_COOKIE = "com.openexchange.oidc.SessionCookie";

    public static final String ACCESS_TOKEN = "access_token";

    public static final String REFRESH_TOKEN = "refresh_token";

    public static final String OIDC_LOGOUT = "oidcLogout";

    public static final String ACCESS_TOKEN_EXPIRY = "access_token_expiry";
    
    public static final String BACKEND_PATH = "__session.oidc.backend.path";
    
    public static String getPathString(String path) {
        if (Strings.isEmpty(path)) {
            return "";
        }
        return path;
    }
    
    /**
     * Generates the relative redirect location to enter the web frontend directly with a session.
     *
     * @param session The session
     * @param uiWebPath The path to use
     */
    public static String buildFrontendRedirectLocation(Session session, String uiWebPath) {
        String retval = uiWebPath;

        // Prevent HTTP response splitting.
        retval = retval.replaceAll("[\n\r]", "");
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_SESSION, session.getSessionID());
        return retval;
    }
    
    public static URI getURIFromPath(String path) throws OXException{
        try {
            return new URI(path);
        } catch (URISyntaxException e) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_URI.create(e, path);
        }
    }
    
    public static String getDomainName(HttpServletRequest request, HostnameService hostnameService) {
        if (hostnameService == null) {
            return request.getServerName();
        }

        String hostname = hostnameService.getHostname(-1, -1);
        if (hostname == null) {
            return request.getServerName();
        }

        return hostname;
    }
    
    public static boolean considerSecure(final HttpServletRequest request) {
        final ConfigurationService configurationService = Services.getService(ConfigurationService.class);
        if (configurationService != null && configurationService.getBoolProperty(ServerConfig.Property.FORCE_HTTPS.getPropertyName(), false) && !Cookies.isLocalLan(request)) {
            // HTTPS is enforced by configuration
            return true;
        }
        return request.isSecure();
    }
    
    public static void validateSession(Session session, HttpServletRequest request) throws OXException {
        SessionUtility.checkIP(session, request.getRemoteAddr());
        Map<String, Cookie> cookies = Cookies.cookieMapFor(request);
        Cookie secretCookie = cookies.get(LoginServlet.SECRET_PREFIX + session.getHash());
        if (secretCookie == null || !session.getSecret().equals(secretCookie.getValue())) {
            LOG.debug("No secret cookie found for session: " + session.getSessionID());
            throw SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
        }
    }
    
    public static void buildRedirectResponse(HttpServletResponse response, String redirectURI, String respondWithRedirect) throws IOException {
        if (respondWithRedirect != null && Boolean.parseBoolean(respondWithRedirect)) {
            response.sendRedirect(redirectURI);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setCharacterEncoding(Charsets.UTF_8_NAME);
            response.setContentType("application/json");
            PrintWriter writer = response.getWriter();
            writer.write("{\"redirect\":\"" + redirectURI + "\"}");
            writer.flush();
        }
    }
    
    public static Cookie loadAutologinCookie(HttpServletRequest request, LoginConfiguration loginConfiguration) throws OXException {
        String hash = HashCalculator.getInstance().getHash(request, LoginTools.parseUserAgent(request), LoginTools.parseClient(request, false, loginConfiguration.getDefaultClient()));
        Map<String, Cookie> cookies = Cookies.cookieMapFor(request);
        return cookies.get(OIDCTools.AUTOLOGIN_COOKIE_PREFIX + hash);
    }
    
    public static void addParameterToSession(Session session, Map<String, String> map, String entryLoad, String entrySet) {
        String parameter = map.get(entryLoad);
        if (!Strings.isEmpty(parameter)) {
            session.setParameter(entrySet, parameter);
        }
    }

    public static String getUIWebPath(LoginConfiguration loginConfiguration, OIDCBackendConfig backendConfig) {
        String uiWebPath = backendConfig.getUIWebpath();
        if (Strings.isEmpty(uiWebPath)) {
            uiWebPath = loginConfiguration.getUiWebPath();
        }
        return uiWebPath;
    }
}
