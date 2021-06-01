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

package com.openexchange.oidc.tools;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;
import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import com.nimbusds.oauth2.sdk.token.Tokens;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.DefaultAuthenticated;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.oidc.AuthenticationInfo;
import com.openexchange.oidc.OIDCBackend;
import com.openexchange.oidc.OIDCBackendConfig;
import com.openexchange.oidc.OIDCExceptionCode;
import com.openexchange.oidc.osgi.Services;
import com.openexchange.session.Session;
import com.openexchange.session.oauth.OAuthTokens;
import com.openexchange.session.oauth.TokenRefreshConfig;
import com.openexchange.sessiond.ExpirationReason;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.user.User;

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

    public static final String SESSION_COOKIE = "com.openexchange.oidc.SessionCookie";

    public static final String ACCESS_TOKEN = "access_token";

    public static final String REFRESH_TOKEN = "refresh_token";

    public static final String OIDC_LOGOUT = "oidcLogout";

    public static final String ACCESS_TOKEN_EXPIRY = "access_token_expiry";

    public static final String BACKEND_PATH = "__session.oidc.backend.path";

    public static final String DEFAULT_BACKEND_PATH = "oidc";

    public static final String PARAM_DEEP_LINK = "uriFragment";

    @Deprecated
    public static final String PARAM_DEEP_LINK_ALT = "hash";

    public static final String PARAM_SHARD = "shard";

    /**
     * Map key to preserve user login info
     */
    public static final String USER_LOGIN_INFO = "user_login_info";

    /**
     * Map key to preserve context login info
     */
    public static final String CONTEXT_LOGIN_INFO = "context_login_info";

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
        LOG.trace("buildFrontendRedirectLocation(Session session: {}, String uiWebPath: {}, String deeplink: {})", session.getSessionID(), uiWebPath, deeplink);
        URIBuilder location = new URIBuilder();
        setRedirectLocation(location, session, uiWebPath, deeplink);
        return location.toString();
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
        if (secretCookie == null) {
            LOG.debug("No secret cookie found for session: {}", session);
            OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
            oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, ExpirationReason.NO_EXPECTED_SECRET_COOKIE.getIdentifier());
            throw oxe;
        } else if (!session.getSecret().equals(secretCookie.getValue())) {
            LOG.debug("No secret cookie found for session: {}", session);
            OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
            oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, ExpirationReason.SECRET_MISMATCH.getIdentifier());
            throw oxe;
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
     * Load the session cookie with the given {@link HttpServletRequest} informations.
     *
     * @param request The {@link HttpServletRequest} with needed information
     * @param loginConfiguration The {@link LoginConfiguration} to get additional information from
     * @return The session cookie or null
     * @throws OXException If something fails
     */
    public static Cookie loadSessionCookie(HttpServletRequest request, LoginConfiguration loginConfiguration) throws OXException {
        LOG.trace("loadSessionCookie(HttpServletRequest request: {}, LoginConfiguration loginConfiguration)", request.getRequestURI());
        String hash = calculateHash(request, loginConfiguration);
        Map<String, Cookie> cookies = Cookies.cookieMapFor(request);
        return cookies.get(LoginServlet.SESSION_PREFIX + hash);
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
        LOG.trace("void addParameterToSession(Session session: {}, Map<String, String> map size: {}, String entryLoad: {}, String entrySet: {})", session.getSessionID(), I(map.size()), entryLoad, entrySet);
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
     * Gets a deep link (URI fragment hash to be appended to final UI redirect) from
     * the client request.
     *
     * @param request The request
     * @return The value to preserve or <code>null</code>
     */
    public static String getDeepLink(HttpServletRequest request) {
        String value = request.getParameter(PARAM_DEEP_LINK);
        if (value == null) {
            value = request.getParameter(PARAM_DEEP_LINK_ALT);
        }
        return value;
    }

    /**
     * Load a session from the given {@link Cookie}.
     *
     * @param sessionCookie The cookie where the session is stored.
     * @param request Used to validate the found session.
     * @return The loaded session or null, if no session could be found or the validation failed.
     */
    public static Session getSessionFromSessionCookie(Cookie sessionCookie, HttpServletRequest request) {
        LOG.trace("getSessionFromSessionCookie(Cookie sessionCookie: {})", sessionCookie.getValue());
        SessiondService sessiondService = Services.getService(SessiondService.class);
        Session session = sessiondService.getSession(sessionCookie.getValue());
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

    /**
     * Creates an {@link Authenticated} instance based on the given context, user and {@link AuthenticationInfo}
     * properties.
     */
    public static Authenticated getDefaultAuthenticated(final Context context, final User user, Map<String, String> state) {
        LOG.trace("getDefaultAuthenticated(final Context context: {}, final User user: {})", I(context.getContextId()), I(user.getId()));
        String contextInfo = state.get(OIDCTools.CONTEXT_LOGIN_INFO);
        if (Strings.isEmpty(contextInfo)) {
            contextInfo = context.getLoginInfo()[0];
        }

        String userInfo = state.get(OIDCTools.USER_LOGIN_INFO);
        if (Strings.isEmpty(userInfo)) {
            userInfo = user.getLoginInfo();
        }

        return new DefaultAuthenticated(contextInfo, userInfo);
    }

    /**
     * Adds OIDC specific parameters to a session instance. Any parameter must be contained in the provided
     * map. The following parameters and keys are allowed/expected:
     * <p>
     * <ul>
     *   <li>ID token: {@link OIDCTools#IDTOKEN}</li>
     *   <li>access token: {@link OIDCTools#ACCESS_TOKEN}</li>
     *   <li>refresh token: {@link OIDCTools#REFRESH_TOKEN}</li>
     *   <li>access token expiry: {@link OIDCTools#ACCESS_TOKEN_EXPIRY}</li>
     *   <li>backend path: {@link OIDCTools#BACKEND_PATH}</li>
     *   <li>last token refresh timestamp: {@link OIDCTools#LAST_REFRESH_TIMESTAMP}</li>
     * </ul>
     * <p>
     */
    public static void setSessionParameters(Session session, Map<String, String> params) {
        OIDCTools.addParameterToSession(session, params, OIDCTools.IDTOKEN, OIDCTools.IDTOKEN);
        OIDCTools.addParameterToSession(session, params, OIDCTools.ACCESS_TOKEN, Session.PARAM_OAUTH_ACCESS_TOKEN);
        OIDCTools.addParameterToSession(session, params, OIDCTools.REFRESH_TOKEN, Session.PARAM_OAUTH_REFRESH_TOKEN);
        OIDCTools.addParameterToSession(session, params, OIDCTools.ACCESS_TOKEN_EXPIRY, Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE);

        // preserve empty string which typically indicates the default backend
        String backendPath = params.get(OIDCTools.BACKEND_PATH);
        if (backendPath != null) {
            session.setParameter(OIDCTools.BACKEND_PATH, backendPath);
        }
    }

    /**
     * Converts a token map into an {@link OAuthTokens} instance. The following parameters and keys are allowed/expected:
     * <p>
     * <ul>
     *   <li>access token: {@link OIDCTools#ACCESS_TOKEN}</li>
     *   <li>refresh token: {@link OIDCTools#REFRESH_TOKEN}</li>
     *   <li>access token expiry: {@link OIDCTools#ACCESS_TOKEN_EXPIRY} - milliseconds since January 1, 1970, 00:00:00 GMT</li>
     * </ul>
     *
     * @param params
     */
    public static Optional<OAuthTokens> convertTokenMap(Map<String, String> params) {
        String accessToken = params.get(OIDCTools.ACCESS_TOKEN);
        String expiryString = params.get(OIDCTools.ACCESS_TOKEN_EXPIRY);
        String refreshToken = params.get(OIDCTools.REFRESH_TOKEN);

        if (accessToken == null) {
            return Optional.empty();
        }

        Date expiryDate = null;
        if (expiryString != null) {
            try {
                long expiryMillis = Long.parseLong(expiryString);
                expiryDate = new Date(expiryMillis);
            } catch (NumberFormatException e) {
                LOG.warn("Invalid entry in OAuth parameter map: {} => \"{}\"", OIDCTools.ACCESS_TOKEN_EXPIRY, expiryString);
            }
        }

        return Optional.of(new OAuthTokens(accessToken, expiryDate, refreshToken));
    }


    /**
     * Gets OIDC specific parameters from a session instance. The following parameters and keys are extracted if available:
     * <p>
     * <ul>
     *   <li>ID token: {@link OIDCTools#IDTOKEN}</li>
     *   <li>access token: {@link OIDCTools#ACCESS_TOKEN}</li>
     *   <li>refresh token: {@link OIDCTools#REFRESH_TOKEN}</li>
     *   <li>access token expiry: {@link OIDCTools#ACCESS_TOKEN_EXPIRY}</li>
     *   <li>backend path: {@link OIDCTools#BACKEND_PATH}</li>
     *   <li>last token refresh timestamp: {@link OIDCTools#LAST_REFRESH_TIMESTAMP}</li>
     * </ul>
     * <p>
     */
    public static Map<String, String> getSessionParameters(Session session) {
        Map<String, String> params = new HashMap<String, String>(6, 1.0f);
        Object parameter = session.getParameter(OIDCTools.IDTOKEN);
        if (parameter instanceof String) {
            params.put(IDTOKEN, (String) parameter);
        }
        parameter = session.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN);
        if (parameter instanceof String) {
            params.put(ACCESS_TOKEN, (String) parameter);
        }
        parameter = session.getParameter(Session.PARAM_OAUTH_REFRESH_TOKEN);
        if (parameter instanceof String) {
            params.put(REFRESH_TOKEN, (String) parameter);
        }
        parameter = session.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN_EXPIRY_DATE);
        if (parameter instanceof String) {
            params.put(ACCESS_TOKEN_EXPIRY, (String) parameter);
        }
        parameter = session.getParameter(BACKEND_PATH);
        if (parameter instanceof String) {
            params.put(BACKEND_PATH, (String) parameter);
        }
        return params;
    }

    /**
     * Converts a {@link Tokens} instance to an {@link OAuthTokens} instance.
     *
     * @param tokens
     */
    public static OAuthTokens convertNimbusTokens(Tokens tokens) {
        AccessToken accessToken = tokens.getAccessToken();
        long lifetime = accessToken.getLifetime();
        Date expiryDate = null;
        if (lifetime > 0) {
            long expiryDateMillis = System.currentTimeMillis();
            expiryDateMillis += lifetime * 1000;
            expiryDate = new Date(expiryDateMillis);
        }

        RefreshToken refreshToken = tokens.getRefreshToken();
        String refreshTokenValue = null;
        if (refreshToken != null) {
            refreshTokenValue = refreshToken.getValue();
        }

        return new OAuthTokens(tokens.getAccessToken().getValue(), expiryDate, refreshTokenValue);
    }

    /**
     * Gets a {@link TokenRefreshConfig} instance based on backend config
     *
     * @param config
     */
    public static TokenRefreshConfig getTokenRefreshConfig(OIDCBackendConfig config) {
        return TokenRefreshConfig.newBuilder()
            .setLockTimeout(config.getTokenLockTimeoutSeconds(), TimeUnit.SECONDS)
            .setRefreshThreshold(config.getOauthRefreshTime(), TimeUnit.MILLISECONDS)
            .setTryRecoverStoredTokens(config.tryRecoverStoredTokens())
            .build();
    }

    /**
     * Converts an OAuth access tokens <code>expires_in</code> property to a {@link Date}.
     *
     * @param expiresIn The <code>expires_in</code> value in seconds
     * @return The date
     */
    public static Date expiresInToDate(long expiresIn) {
        return new Date(System.currentTimeMillis() + (expiresIn * 1000));
    }

    /**
     * Sets path, query and fragment at the passed {@link URIBuilder} instance to create
     * an URI that can be used to bootstrap a frontend session with optional deep link.
     *
     * Path and deep link are sanitized before being appended. They are expected to be raw
     * (non-URI-encoded) Strings.
     *
     * @param uri The URI builder
     * @param session The session to jump into
     * @param path The URI path
     * @param deepLink The deep link argument which is appended to the URI fragment as <code>&[deepLink]</code>
     */
    private static void setRedirectLocation(URIBuilder uri, Session session, String path, String deepLink) {
        uri.setPath(path.replaceAll("[\n\r]", ""));

        StringBuilder fragment = new StringBuilder(PARAMETER_SESSION).append('=').append(session.getSessionID());
        fragment.append(sanitizeDeepLinkFragment(deepLink));
        uri.setFragment(fragment.toString());
    }

    private static final String sanitizeDeepLinkFragment(String uriFragment) {
        if (uriFragment == null) {
            return "";
        }

        uriFragment = uriFragment.replaceAll("[\n\r]", "");
        while (uriFragment.length() > 0 && (uriFragment.charAt(0) == '#' || uriFragment.charAt(0) == '&')) {
            uriFragment = uriFragment.substring(1);
        }

        if (uriFragment.length() > 0) {
            uriFragment = "&" + uriFragment;
        }

        return uriFragment;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Create a <code>toString()</code> representation for given token request.
     *
     * @param tokenReq The token request
     * @return The object providing proper <code>toString()</code> method
     */
    public static Object tokeRequestInfo(TokenRequest tokenReq) {
        if (tokenReq == null) {
            return "null";
        }

        return new TokenRequestToString(tokenReq);
    }

    private static final class TokenRequestToString {

        private final TokenRequest tokenReq;

        TokenRequestToString(TokenRequest tokenReq) {
            this.tokenReq = tokenReq;
        }

        @Override
        public String toString() {
            return new StringBuilder(512).append('{')
                .append("uri=").append(tokenReq.getEndpointURI())
                .append(", clientAuth=").append(toString(tokenReq.getClientAuthentication()))
                .append(", authzGrant=").append(toString(tokenReq.getAuthorizationGrant()))
                .append(", scope=").append(tokenReq.getScope())
                .append(", customParams=").append(tokenReq.getCustomParameters())
                .append('}').toString();
        }

        private static String toString(ClientAuthentication clientAuthentication) {
            if (clientAuthentication == null) {
                return "null";
            }

            return new StringBuilder(128).append('{')
                .append("method=").append(clientAuthentication.getMethod())
                .append(", clientID=").append(clientAuthentication.getClientID())
                .append('}').toString();
        }

        private static String toString(AuthorizationGrant authorizationGrant) {
            if (authorizationGrant == null) {
                return "null";
            }

            return new StringBuilder(64).append('{')
                .append("type=").append(authorizationGrant.getType())
                .append('}').toString();
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

}
