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
import java.util.Collection;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link OIDCTools}
 *
 * Provides multiple static methods and attributes, that are used throughout the OpenID feature.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.10.0
 */
public class OIDCTools {

    private static final Logger LOG = LoggerFactory.getLogger(OIDCTools.class);

    public static final String SESSION_TOKEN = "sessionToken";

    public static final String OIDC_LOGIN = "oidcLogin";

    public static final String IDTOKEN = "__session.oidc.idToken";

    public static final String TYPE = "type";

    public static final String END = "end";

    public static final String RESUME = "resume";

    public static final String STATE = "state";

    public static final String AUTOLOGIN_COOKIE_PREFIX = "open-xchange-oidc-";

    public static final String SESSION_COOKIE = "com.openexchange.oidc.SessionCookie";

    public static final String ACCESS_TOKEN = "access_token";

    public static final String REFRESH_TOKEN = "refresh_token";

    public static final String OIDC_LOGOUT = "oidcLogout";

    public static final String ACCESS_TOKEN_EXPIRY = "access_token_expiry";

    public static final String BACKEND_PATH = "__session.oidc.backend.path";

    public static final String DEFAULT_BACKEND_PATH = "oidc";

    public static final String PARAM_DEEP_LINK = "hash";

    public static String getPathString(String path) {
        if (Strings.isEmpty(path)) {
            return "";
        }
        return path;
    }

    /**
     * Generates the relative redirect location of the web frontend.
     *
     * @param session The session to add to the location
     * @param uiWebPath The path to use
     * @param deeplink The deeplink, can be <code>null</code>
     */
    public static String buildFrontendRedirectLocation(Session session, String uiWebPath, String deeplink) {
        LOG.trace("buildFrontendRedirectLocation(Session session: {}, String uiWebPath: {})", session.getSessionID(), uiWebPath);
        String retval = uiWebPath;

        // Prevent HTTP response splitting.
        retval = retval.replaceAll("[\n\r]", "");
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_SESSION, session.getSessionID());
        if (!Strings.isEmpty(deeplink)) {
            retval += "&" + AJAXUtility.encodeUrl(deeplink.substring(1), true);
        }
        return retval;
    }

    /**
     * Create an {@link URI} object from the given path.
     *
     * @param path The path
     * @return The {@link URI}
     * @throws OXException If the path can not be parsed
     */
    public static URI getURIFromPath(String path) throws OXException{
        LOG.trace("getURIFromPath({})", path);
        try {
            return new URI(path);
        } catch (URISyntaxException e) {
            throw OIDCExceptionCode.UNABLE_TO_PARSE_URI.create(e, path);
        }
    }

    /**
     * Load the domain name from {@link HostnameService} or get the given
     * {@link HttpServletRequest}s server name if {@link HostnameService} is null
     *
     * @param request The {@link HttpServletRequest}
     * @param hostnameService The {@link HostnameService}, nullable
     * @return The domain name
     */
    public static String getDomainName(HttpServletRequest request, HostnameService hostnameService) {
        LOG.trace("getDomainName(HttpServletRequest request: {}, HostnameService hostnameService: {})", request.getRequestURI(), (hostnameService != null ? "HostnameService instance" : "null"));
        if (hostnameService == null) {
            return request.getServerName();
        }

        String hostname = hostnameService.getHostname(-1, -1);
        if (hostname == null) {
            return request.getServerName();
        }

        return hostname;
    }

    /**
     * Validate the given {@link Session} by checking its IP, cookies and secret informations.
     *
     * @param session The {@link Session} to validate
     * @param request The {@link HttpServletRequest} with additional information
     * @throws OXException If the session is expired
     */
    public static void validateSession(Session session, HttpServletRequest request) throws OXException {
        LOG.trace("validateSession(Session session: {}, HttpServletRequest request: {})",session.getSessionID(), request.getRequestURI());
        SessionUtility.checkIP(session, request.getRemoteAddr());
        Map<String, Cookie> cookies = Cookies.cookieMapFor(request);
        Cookie secretCookie = cookies.get(LoginServlet.SECRET_PREFIX + session.getHash());
        if (secretCookie == null || !session.getSecret().equals(secretCookie.getValue())) {
            LOG.debug("No secret cookie found for session: {}", session);
            throw SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
        }
    }

    /**
     * Add the given redirect URI to the given {@link HttpServletResponse}. Either in a JSON object with
     * the attribute name 'redirect', if respondWithRedirect is 'true' or by a direct redirect.
     *
     * @param response The {@link HttpServletResponse} to use
     * @param redirectURI The URI where the response should redirect to
     * @param respondWithRedirect 'true', 'false' or <code>null</code>. Triggers the addition of a JSON body, where an
     *  attribute is added with the name 'redirect' and the value of the redirect URI.
     * @throws IOException If the redirect URI can not be added
     */
    public static void buildRedirectResponse(HttpServletResponse response, String redirectURI, String respondWithRedirect) throws IOException {
        buildRedirectResponse(response, redirectURI, null == respondWithRedirect ? null : Boolean.valueOf(respondWithRedirect.trim()));
    }

    /**
     * Add the given redirect URI to the given {@link HttpServletResponse}. Either in a JSON object with
     * the attribute name 'redirect', if respondWithRedirect is 'true' or by a direct redirect.
     *
     * @param response The {@link HttpServletResponse} to use
     * @param redirectURI The URI where the response should redirect to
     * @param respondWithRedirect <code>Boolean.TRUE</code>, <code>Boolean.FALSE</code> or <code>null</code>. Triggers the addition of a JSON body, where an
     *  attribute is added with the name 'redirect' and the value of the redirect URI.
     * @throws IOException If the redirect URI can not be added
     */
    public static void buildRedirectResponse(HttpServletResponse response, String redirectURI, Boolean respondWithRedirect) throws IOException {
        LOG.trace("buildRedirectResponse(HttpServletResponse response, String redirectURI: {}, Boolean respondWithRedirect: {})", redirectURI, (respondWithRedirect != null ? respondWithRedirect : "null"));
        if (respondWithRedirect != null && respondWithRedirect.booleanValue()) {
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

    /**
     * Load the OIDC auto-login cookie with the given {@link HttpServletRequest} informations.
     *
     * @param request The {@link HttpServletRequest} with needed information
     * @param loginConfiguration The {@link LoginConfiguration} to get additional information from
     * @return The auto-login cookie or null
     * @throws OXException If something fails
     */
    public static Cookie loadAutologinCookie(HttpServletRequest request, LoginConfiguration loginConfiguration) throws OXException {
        LOG.trace("loadAutologinCookie(HttpServletRequest request: {}, LoginConfiguration loginConfiguration)", request.getRequestURI());
        String hash = calculateHash(request, loginConfiguration);
        Map<String, Cookie> cookies = Cookies.cookieMapFor(request);
        return cookies.get(OIDCTools.AUTOLOGIN_COOKIE_PREFIX + hash);
    }
    
    public static String calculateHash(HttpServletRequest request, LoginConfiguration loginConfiguration) throws OXException {
        return HashCalculator.getInstance().getHash(request, LoginTools.parseUserAgent(request), LoginTools.parseClient(request, false, loginConfiguration.getDefaultClient()));
    }

    /**
     * Adds a parameter from a given map to a session.
     *
     * @param session The session, where a parameter should be added
     * @param map The map, which contains the values
     * @param entryLoad The key, which find the value in the map
     * @param entrySet The key, that should be used in the session for the value, loaded from the map
     */
    public static void addParameterToSession(Session session, Map<String, String> map, String entryLoad, String entrySet) {
        LOG.trace("void addParameterToSession(Session session: {}, Map<String, String> map size: {}, String entryLoad: {}, String entrySet: {})", session.getSessionID(), map.size(), entryLoad, entrySet);
        String parameter = map.get(entryLoad);
        if (!Strings.isEmpty(parameter)) {
            session.setParameter(entrySet, parameter);
        }
    }

    /**
     * Load the UI path from the given {@link OIDCBackendConfig} or the {@link LoginConfiguration} instead.
     *
     * @param loginConfiguration The {@link LoginConfiguration} to use as fallback
     * @param backendConfig The {@link OIDCBackendConfig} to load the path from
     * @return The UI web path
     */
    public static String getUIWebPath(LoginConfiguration loginConfiguration, OIDCBackendConfig backendConfig) {
        LOG.trace("getUIWebPath(LoginConfiguration loginConfiguration,  OIDCBackendConfig backendConfig: {})", backendConfig.getClientID());
        String uiWebPath = backendConfig.getUIWebpath();
        if (Strings.isEmpty(uiWebPath)) {
            uiWebPath = loginConfiguration.getUiWebPath();
        }
        return uiWebPath;
    }

    /**
     * Helper method that validates the path to only contain allowed characters
     * @param path The path to be checked.
     * @return
     */
    public static void validatePath(String path) throws OXException{
        if (path.matches(".*[^a-zA-Z0-9].*")) {
            throw OIDCExceptionCode.INVALID_BACKEND_PATH.create(path);
        }
    }


    /**
     * Get the prefix for the given path or set it to "oidc/" if not configured.
     *
     * @param oidcBackend The backend which prefix should be determined
     * @return the backends path or "oidc/"
     */
    public static String getPrefix(final OIDCBackend oidcBackend) {
        StringBuilder prefixBuilder = new StringBuilder();
        prefixBuilder.append(getRedirectPathPrefix());
        prefixBuilder.append(DEFAULT_BACKEND_PATH);
        prefixBuilder.append("/");
        String path = oidcBackend.getPath();
        if (!Strings.isEmpty(path)) {
            prefixBuilder.append(path).append("/");
        }
        return prefixBuilder.toString();
    }

    /**
     * Load the UI Client name from the given request. If not present,
     * load the default client information from {@link LoginConfiguration}.
     *
     * @param request The request for which the UI Client should be determined
     * @return The UI client name
     */
    public static String getUiClient(HttpServletRequest request) {
        String uiClientID = request.getParameter("client");

        if (uiClientID == null || Strings.isEmpty(uiClientID)) {

            LoginConfiguration loginConfiguration =  LoginServlet.getLoginConfiguration();
            uiClientID = loginConfiguration.getDefaultClient();
        }

        return uiClientID;
    }

    /**
     * Determine whether the given request is secure or not.
     *
     * @param request The request
     * @return "https" if request is considered secure, "http" otherwise
     */
    public static String getRedirectScheme(HttpServletRequest request) {
        boolean secure = Tools.considerSecure(request);
        return secure ? "https" : "http";
    }

    /**
     * Load prefix from {@link DispatcherPrefixService}
     * @return The prefix
     */
    public static String getRedirectPathPrefix() {
        DispatcherPrefixService prefixService = Services.getService(DispatcherPrefixService.class);
        return prefixService.getPrefix();
    }
    
    /**
     * Load a session from the given {@link Cookie}.
     * 
     * @param oidcAtologinCookie The cookie where the session is stored.
     * @param request Used to validate the found session.
     * @return The loaded session or null, if no session could be found or the validation failed.
     * @throws OXException If an error occurs while filtering, when finding sessions
     */
    public static Session getSessionFromAutologinCookie(Cookie oidcAtologinCookie, HttpServletRequest request) throws OXException {
        LOG.trace("getSessionFromAutologinCookie(Cookie oidcAtologinCookie: {})", oidcAtologinCookie.getValue());
        Session session = null;
        SessiondService sessiondService = Services.getService(SessiondService.class);
        Collection<String> sessions = sessiondService.findSessions(SessionFilter.create("(" + OIDCTools.SESSION_COOKIE + "=" + oidcAtologinCookie.getValue() + ")"));
        if (!sessions.isEmpty()) {
            session = sessiondService.getSession(sessions.iterator().next());
        }
        
        if (session == null) {
            return null;
        }
        
        try {
            OIDCTools.validateSession(session, request);
        } catch (OXException e) {
            LOG.debug("Session validation failed for {}", session.getSessionID());
            session = null;
        }
        
        return session;
    }
}
