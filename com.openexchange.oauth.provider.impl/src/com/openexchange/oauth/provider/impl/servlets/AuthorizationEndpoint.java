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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.impl.servlets;

import static com.openexchange.osgi.Tools.requireService;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import com.google.common.net.HttpHeaders;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.login.AutoLoginTools;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestImpl;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginExceptionMessages;
import com.openexchange.config.cascade.ConfigProviderService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.contexts.impl.ContextExceptionMessage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogProperties;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.client.ClientManagementException;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionMessages;
import com.openexchange.oauth.provider.impl.OAuthProviderProperties;
import com.openexchange.oauth.provider.impl.notification.OAuthMailNotificationService;
import com.openexchange.oauth.provider.impl.tools.URLHelper;
import com.openexchange.oauth.provider.scope.Scope;
import com.openexchange.oauth.provider.scope.OAuthScopeProvider;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.OXTemplateExceptionHandler;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link AuthorizationEndpoint} - Authorization request handler for OAuth2.0.
 * <p>
 * <img src="./webflow.png" alt="OAuth Web Flow">
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AuthorizationEndpoint extends OAuthEndpoint {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AuthorizationEndpoint.class);

    private static final long serialVersionUID = 6393806486708501254L;

    /**
     * Initializes a new {@link AuthorizationEndpoint}.
     */
    public AuthorizationEndpoint(OAuthProviderService oAuthProvider, ServiceLookup services) {
        super(oAuthProvider, services);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Tools.disableCaching(response);
        if (!Tools.considerSecure(request)) {
            response.setHeader(HttpHeaders.LOCATION, URLHelper.getSecureLocation(request));
            response.sendError(HttpServletResponse.SC_MOVED_PERMANENTLY);
            return;
        }

        AuthorizationRequest authRequest = validate(request, response);
        if (authRequest == null) {
            return;
        }

        // Set JSESSIONID cookie and generate CSRF token
        HttpSession jSession = request.getSession(true);
        String csrfToken = UUIDs.getUnformattedStringFromRandom();
        jSession.setAttribute(ATTR_OAUTH_CSRF_TOKEN, csrfToken);

        try {
            try {
                String htmlResult;
                SessionResult session = checkSession(request, response, authRequest);
                if (session == null) {
                    LoginError error = optLoginError(request);
                    htmlResult = compileLoginPage(request, authRequest, csrfToken, error);
                } else {
                    htmlResult = compileAuthorizationPage(request, authRequest, csrfToken, session);
                }

                response.setContentType("text/html; charset=UTF-8");
                response.setHeader("Content-Disposition", "inline");
                response.setStatus(200);
                PrintWriter writer = response.getWriter();
                writer.write(htmlResult);
                writer.flush();
            } catch (OXException e) {
                LOG.error("Authorization GET request failed", e);
                String redirectLocation;
                if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                    redirectLocation = URLHelper.getErrorRedirectLocation(authRequest.getRedirectURI(), "temporarily_unavailable", "The service is currently not available.", OAuthProviderConstants.PARAM_STATE, authRequest.getState());
                } else {
                    redirectLocation = URLHelper.getErrorRedirectLocation(authRequest.getRedirectURI(), "server_error", "An internal error occurred.", OAuthProviderConstants.PARAM_STATE, authRequest.getState());
                }

                response.sendRedirect(redirectLocation);
            }
        } catch (OXException e) {
            /*
             * Responding with an error redirect failed. We can only display a proper message in the popup now.
             */
            LOG.error("Could not send error redirect for authorization GET request", e);
            respondWithErrorPage(request, response, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Tools.disableCaching(response);
        if (!Tools.considerSecure(request)) {
            response.setHeader(HttpHeaders.LOCATION, URLHelper.getSecureLocation(request));
            response.sendError(HttpServletResponse.SC_MOVED_PERMANENTLY);
            return;
        }

        if (isPotentialCSRF(request, response)) {
            return;
        }

        AuthorizationRequest authRequest = validate(request, response);
        if (authRequest == null) {
            return;
        }

        try {
            try {
                String redirectLocation;
                String sessionId = request.getParameter(OAuthProviderConstants.PARAM_SESSION);
                if (sessionId == null) {
                    redirectLocation = handleLogin(request, response, authRequest);
                } else {
                    SessionResult.Type sessionType = SessionResult.Type.forCode(request.getParameter("session_type"));
                    if (sessionType == null) {
                        // TODO: think about
                        redirectLocation = URLHelper.getErrorRedirectLocation(authRequest.getRedirectURI(), "server_error", "An internal error occurred.", OAuthProviderConstants.PARAM_STATE, authRequest.getState());
                    } else {
                        redirectLocation = handleAuthorization(request, response, authRequest, sessionId, sessionType);
                    }
                }

                response.sendRedirect(redirectLocation);
            } catch (OXException e) {
                String redirectLocation;
                // Let the popup display this error so that the user can retry or does at least now why he can't grant access
                if (LoginExceptionCodes.INVALID_CREDENTIALS.equals(e)) {
                    Map<String, String> redirectParams = prepareSelfRedirectParams(authRequest);
                    redirectParams.put("error", LoginError.INVALID_CREDENTIALS.getCode());
                    redirectLocation = URLHelper.getRedirectLocation(URLHelper.getSecureLocation(request), redirectParams);
                } else if (ContextExceptionCodes.UPDATE.equals(e)) {
                    Map<String, String> redirectParams = prepareSelfRedirectParams(authRequest);
                    redirectParams.put("error", LoginError.UPDATE_TASK.getCode());
                    redirectLocation = URLHelper.getRedirectLocation(URLHelper.getSecureLocation(request), redirectParams);
                } else if (OAuthProviderExceptionCodes.GRANTS_EXCEEDED.equals(e)) {
                    Map<String, String> redirectParams = prepareSelfRedirectParams(authRequest);
                    redirectParams.put("error", LoginError.GRANTS_EXCEEDED.getCode());
                    redirectLocation = URLHelper.getRedirectLocation(URLHelper.getSecureLocation(request), redirectParams);
                } else if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                    LOG.error("Authorization POST request failed", e);
                    redirectLocation = URLHelper.getErrorRedirectLocation(authRequest.getRedirectURI(), "temporarily_unavailable", "The service is currently not available.", OAuthProviderConstants.PARAM_STATE, authRequest.getState());
                } else {
                    LOG.error("Authorization POST request failed", e);
                    redirectLocation = URLHelper.getErrorRedirectLocation(authRequest.getRedirectURI(), "server_error", "An internal error occurred.", OAuthProviderConstants.PARAM_STATE, authRequest.getState());
                }

                response.sendRedirect(redirectLocation);
            }
        } catch (OXException e) {
            /*
             * Responding with an error redirect failed. We can only display a proper message in the popup now.
             */
            LOG.error("Could not send error redirect for authorization POST request", e);
            respondWithErrorPage(request, response, e);
        }
    }

    private String handleAuthorization(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authRequest, String sessionId, SessionResult.Type sessionType) throws OXException {
        try {
            ServerSession session = checkSession1(request, response, authRequest, sessionId, sessionType);
            if (session == null) {
                return URLHelper.getRedirectLocation(URLHelper.getSecureLocation(request), prepareSelfRedirectParams(authRequest));
            }

            Context context = session.getContext();
            User user = session.getUser();

            // Check if user denied access
            String accessDenied = request.getParameter(OAuthProviderConstants.PARAM_ACCESS_DENIED);
            if (Boolean.parseBoolean(accessDenied)) {
                return URLHelper.getErrorRedirectLocation(authRequest.getRedirectURI(), "access_denied", "The user denied your request.", OAuthProviderConstants.PARAM_STATE, authRequest.getState());
            }

            // Check if OAuth is deactivated for this user
            ConfigView configView = requireService(ConfigViewFactory.class, services).getView(user.getId(), context.getContextId());
            if (!configView.opt(OAuthProviderProperties.ENABLED, Boolean.class, Boolean.TRUE).booleanValue() || user.isGuest()) {
                return URLHelper.getErrorRedirectLocation(authRequest.getRedirectURI(), "access_denied", "The user is not allowed to grant OAuth access to 3rd party applications.", OAuthProviderConstants.PARAM_STATE, authRequest.getState());
            }

            // Everything OK, send notification mail and do the redirect with authorization code & state
            String code = oAuthProvider.generateAuthorizationCodeFor(authRequest.getClient().getId(), authRequest.getRedirectURI(), authRequest.getScope(), user.getId(), context.getContextId());
            String redirectLocation = URLHelper.getRedirectLocation(
                authRequest.getRedirectURI(),
                OAuthProviderConstants.PARAM_CODE,
                code,
                OAuthProviderConstants.PARAM_STATE,
                authRequest.getState());

            try {
                OAuthMailNotificationService notificationService = new OAuthMailNotificationService(oAuthProvider);
                notificationService.sendNotification(user.getId(), context.getContextId(), authRequest.getClient().getId(), request);
            } catch (OXException e) {
                LOG.error("Sending OAuth notification mail to {} failed for client {}.", user.getMail(), authRequest.getClient().getId(), e);
            }

            return redirectLocation;
        } catch (OXException e) {
            if (SessionExceptionCodes.SESSION_EXPIRED.equals(e) || SessionExceptionCodes.WRONG_SESSION_SECRET.equals(e)) {
                return URLHelper.getRedirectLocation(URLHelper.getSecureLocation(request), prepareSelfRedirectParams(authRequest));
            }

            throw e;
        }
    }

    private ServerSession checkSession1(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authRequest, String sessionId, SessionResult.Type sessionType) throws OXException {
        // Session must only be provided by a previous login POST. This is enforced via additional cookie hash parameters.
        Session session = requireService(SessiondService.class, services).getSession(sessionId);
        if (session != null) {
            Map<String, Cookie> cookies = Cookies.cookieMapFor(request);
            String cookieHash;
            if (sessionType == SessionResult.Type.AUTOLOGIN) {
                cookieHash = HashCalculator.getInstance().getHash(request);
            } else {
                cookieHash = cookieHash(request, authRequest);
            }
            Cookie secretCookie = cookies.get(LoginServlet.SECRET_PREFIX + cookieHash);
            if (secretCookie != null && session.getSecret().equals(secretCookie.getValue())) {
                String remoteAddress = request.getRemoteAddr();
                LoginConfiguration loginConfig = LoginServlet.getLoginConfiguration();
                if (loginConfig.isIpCheck()) {
                    try {
                        SessionUtility.checkIP(true, loginConfig.getRanges(), session, remoteAddress, loginConfig.getIpCheckWhitelist());
                        return ServerSessionAdapter.valueOf(session);
                    } catch (OXException e) {
                        if (SessionExceptionCodes.WRONG_CLIENT_IP.equals(e)) {
                            LOG.debug("Client IP check failed during OAuth flow.");
                        } else {
                            throw e;
                        }
                    }
                } else {
                    return ServerSessionAdapter.valueOf(session);
                }
            }
        }

        return null;
    }

    private SessionResult checkSession(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authRequest) throws OXException {
        String sessionId = request.getParameter(OAuthProviderConstants.PARAM_SESSION);
        if (sessionId == null) {
            LoginResult loginResult = AutoLoginTools.tryAutologin(LoginServlet.getLoginConfiguration(), request, response);
            if (loginResult != null) {
                return new SessionResult(loginResult.getSession(), SessionResult.Type.AUTOLOGIN);
            }
        } else {
            // Session must only be provided by a previous login POST. This is enforced via additional cookie hash parameters.
            Session session = requireService(SessiondService.class, services).getSession(sessionId);
            if (session != null) {
                Map<String, Cookie> cookies = Cookies.cookieMapFor(request);
                Cookie secretCookie = cookies.get(LoginServlet.SECRET_PREFIX + cookieHash(request, authRequest));
                if (secretCookie != null && session.getSecret().equals(secretCookie.getValue())) {
                    String remoteAddress = request.getRemoteAddr();
                    LoginConfiguration loginConfig = LoginServlet.getLoginConfiguration();
                    if (loginConfig.isIpCheck()) {
                        try {
                            SessionUtility.checkIP(true, loginConfig.getRanges(), session, remoteAddress, loginConfig.getIpCheckWhitelist());
                            return new SessionResult(session, SessionResult.Type.OAUTH_FORM_LOGIN);
                        } catch (OXException e) {
                            if (SessionExceptionCodes.WRONG_CLIENT_IP.equals(e)) {
                                LOG.debug("Client IP check failed during OAuth flow.");
                            } else {
                                throw e;
                            }
                        }
                    } else {
                        return new SessionResult(session, SessionResult.Type.OAUTH_FORM_LOGIN);
                    }
                }
            }
        }

        return null;
    }

    private static final class SessionResult {
        private static enum Type {
            AUTOLOGIN(1),
            OAUTH_FORM_LOGIN(2);

            private static final Map<Integer, Type> typesByCodes = new HashMap<>(4);
            static {
                for (Type type : Type.values()) {
                    typesByCodes.put(type.code, type);
                }
            }

            private final int code;
            private Type(int code) {
                this.code = code;
            }

            public String getCode() {
                return Integer.toString(code);
            }

            public static Type forCode(String code) {
                if (code == null) {
                    return null;
                }

                try {
                    int intCode = Integer.parseInt(code);
                    return typesByCodes.get(intCode);
                } catch (NumberFormatException e) {
                    return null;
                }
            }

        }

        private final Session session;
        private final Type type;

        public SessionResult(Session session, Type type) {
            super();
            this.session = session;
            this.type = type;
        }

        public Session getSession() {
            return session;
        }

        public String getSessionId() {
            return session.getSessionID();
        }

        public Type getType() {
            return type;
        }
    }

    /**
     * Handles the login. Finally the user agent is redirected back to the GET method,
     * either to respond with the authorization page or again with the login page and
     * an according error message.
     *
     * @param request The servlet request
     * @param response The servlet response
     * @param authRequest The authorization request
     */
    private String handleLogin(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authRequest) throws OXException, IOException {
        String login = request.getParameter(LoginFields.LOGIN_PARAM);
        Map<String, String> redirectParams = prepareSelfRedirectParams(authRequest);
        if (login == null) {
            redirectParams.put("error", LoginError.INVALID_CREDENTIALS.getCode());
        } else {
            // Authenticate
            LoginConfiguration loginConfig = LoginServlet.getLoginConfiguration();
            LoginRequestImpl loginRequest = LoginTools.parseLogin(
                request,
                LoginFields.LOGIN_PARAM,
                false,
                loginConfig.getDefaultClient(),
                loginConfig.isCookieForceHTTPS(),
                loginConfig.isDisableTrimLogin(),
                false);
            loginRequest.setTransient(true);
            LoginResult loginResult = LoginPerformer.getInstance().doLogin(loginRequest);
            Session session = loginResult.getSession();

            // Add session log properties
            LogProperties.putSessionProperties(session);

            // Add headers and cookies from login result
            LoginServlet.addHeadersAndCookies(loginResult, response);

            // Add secret and public cookie
            LoginServlet.writeSecretCookie(request, response, session, cookieHash(request, authRequest), true, request.getServerName(), loginConfig);

            redirectParams.put(OAuthProviderConstants.PARAM_SESSION, loginResult.getSession().getSessionID());
        }

        return URLHelper.getRedirectLocation(URLHelper.getSecureLocation(request), redirectParams);
    }

    private String cookieHash(HttpServletRequest request, AuthorizationRequest authRequest) {
        return HashCalculator.getInstance().getHash(request, HashCalculator.getUserAgent(request), loginClient(authRequest), authRequest.getRedirectURI(), authRequest.getState());
    }

    private String loginClient(AuthorizationRequest authRequest) {
        return "OAuth Client " + authRequest.getClient().getId();
    }

    private Map<String, String> prepareSelfRedirectParams( AuthorizationRequest authRequest) {
        Map<String, String> redirectParams = new LinkedHashMap<>();
        redirectParams.put(OAuthProviderConstants.PARAM_CLIENT_ID, authRequest.getClient().getId());
        redirectParams.put(OAuthProviderConstants.PARAM_REDIRECT_URI, authRequest.getRedirectURI());
        redirectParams.put(OAuthProviderConstants.PARAM_STATE, authRequest.getState());
        redirectParams.put(OAuthProviderConstants.PARAM_SCOPE, authRequest.getScope().toString());
        redirectParams.put(OAuthProviderConstants.PARAM_RESPONSE_TYPE, OAuthProviderConstants.RESPONSE_TYPE_AUTH_CODE);
        return redirectParams;
    }

    private LoginError optLoginError(HttpServletRequest request) {
        String errorCode = request.getParameter("error");
        if (errorCode == null) {
            return null;
        }

        return LoginError.forCode(errorCode);
    }

    /**
     * Checks if this request is a potential CSRF attack. The check is based on the referer header
     * and a special token that is set during the GET request and remembered in the HTTP session.
     * If a potential attack is detected, an error response is sent and <code>true</code> is
     * returned.
     *
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    private boolean isPotentialCSRF(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isInvalidReferer(request)) {
            respondWithErrorPage(request, response, "Missing or invalid referer.");
            return true;
        }

        if (isInvalidCSRFToken(request)) {
            respondWithErrorPage(request, response, "Missing or invalid CSRF token.");
            return true;
        }

        return false;
    }

    private static final class AuthorizationRequest {

        private final Client client;

        private final String redirectURI;

        private final String state;

        private final Scope scope;

        protected AuthorizationRequest(Client client, String redirectURI, String state, Scope scope) {
            super();
            this.client = client;
            this.redirectURI = redirectURI;
            this.state = state;
            this.scope = scope;
        }


        public Client getClient() {
            return client;
        }


        public String getRedirectURI() {
            return redirectURI;
        }


        public String getState() {
            return state;
        }


        public Scope getScope() {
            return scope;
        }

    }

    /**
     * Validates the authorization request and returns an instance of {@link AuthorizationRequest}
     * that encapsulates all the required request data.
     *
     * @param request The servlet request
     * @param response The servlet response
     * @return An {@link AuthorizationRequest} or <code>null</code> if the request is invalid. In that case
     * the response was already submitted after the call returns.
     * @throws IOException
     */
    private AuthorizationRequest validate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Client client = checkClient(request, response);
            if (client == null) {
                return null;
            }

            String redirectURI = checkRedirectURI(request, response, client);
            if (redirectURI == null) {
                return null;
            }

            String state = checkState(request, response, redirectURI);
            if (state == null) {
                return null;
            }

            if (!isValidResponseType(request, response, client, redirectURI, state)) {
                return null;
            }

            Scope scope = checkScope(request, response, client, redirectURI, state);
            if (scope == null) {
                return null;
            }

            return new AuthorizationRequest(client, redirectURI, state, scope);
        } catch (OXException e) {
            /*
             * Unexpected error. We must not or cannot redirect so we display an error page.
             */
            LOG.error("Error while validating OAuth authorization request", e);
            respondWithErrorPage(request, response, e);
            return null;
        }
    }

    /**
     * Checks if a proper client ID is provided as request parameter and returns an according instance.
     * If the request is invalid or an error occurs, the response is submitted and <code>null</code> is
     * returned.
     *
     * @param request The request
     * @param response The response
     * @return The client or <code>null</code>
     * @throws IOException
     * @throws OXException If loading the client fails
     */
    private Client checkClient(HttpServletRequest request, HttpServletResponse response) throws IOException, OXException {
        /*
         * We must not redirect to unverified clients.
         */
        String clientId = request.getParameter(OAuthProviderConstants.PARAM_CLIENT_ID);
        if (Strings.isEmpty(clientId)) {
            respondWithErrorPage(request, response, "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_CLIENT_ID);
            return null;
        }

        try {
            Client client = oAuthProvider.getClientManagement().getClientById(clientId);
            if (client == null || !client.isEnabled()) {
                respondWithErrorPage(request, response, "Invalid client ID: " + clientId);
                return null;
            }

            return client;
        } catch (ClientManagementException e) {
            LOG.error("Could not load OAuth client with ID {}", clientId, e);
            throw OAuthProviderExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Checks if the request contains the 'redirect_uri' parameter and that its value is valid.
     * If the request is invalid or an error occurs, the response is submitted and <code>null</code> is
     * returned.
     *
     * @param request The request
     * @param response The response
     * @param client The client
     * @return The URI
     * @throws IOException
     */
    private String checkRedirectURI(HttpServletRequest request, HttpServletResponse response, Client client) throws IOException {
        /*
         * We must not redirect to invalid URIs.
         */
        String redirectURI = request.getParameter(OAuthProviderConstants.PARAM_REDIRECT_URI);
        if (Strings.isEmpty(redirectURI)) {
            respondWithErrorPage(request, response, "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_REDIRECT_URI);
            return null;
        }

        if (!client.hasRedirectURI(redirectURI)) {
            respondWithErrorPage(request, response, "Invalid redirect URI: " + redirectURI);
            return null;
        }

        return redirectURI;
    }

    /**
     * Checks if a state value is provided as request parameter and returns an according instance.
     * If the request is invalid or an error occurs, the response is submitted and <code>null</code> is
     * returned.
     *
     * @param request The request
     * @param response The response
     * @param redirectURI The redirect URI
     * @return The state or <code>null</code>
     * @throws IOException
     * @throws OXException If constructing the error redirect fails
     */
    private String checkState(HttpServletRequest request, HttpServletResponse response, String redirectURI) throws IOException, OXException {
        String state = request.getParameter(OAuthProviderConstants.PARAM_STATE);
        if (Strings.isEmpty(state)) {
            response.sendRedirect(URLHelper.getErrorRedirectLocation(redirectURI, "invalid_request", "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_STATE));
            return null;
        }

        return state;
    }

    /**
     * Checks if the request contains parameter 'response_type' with status 'code'.
     * If the request is invalid or an error occurs, the response is submitted and <code>false</code> is
     * returned.
     *
     * @param request The request
     * @param response The response
     * @param client The client
     * @param redirectURI The redirect URI
     * @param state The client state
     * @return <code>true</code> if the response type is valid
     * @throws IOException
     * @throws OXException If constructing the error redirect fails
     */
    private boolean isValidResponseType(HttpServletRequest request, HttpServletResponse response, Client client, String redirectURI, String state) throws IOException, OXException {
        String responseType = request.getParameter(OAuthProviderConstants.PARAM_RESPONSE_TYPE);
        if (Strings.isEmpty(responseType)) {
            response.sendRedirect(URLHelper.getErrorRedirectLocation(redirectURI, "invalid_request", "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_RESPONSE_TYPE, OAuthProviderConstants.PARAM_STATE, state));
            return false;
        }

        if (!OAuthProviderConstants.RESPONSE_TYPE_AUTH_CODE.equals(responseType)) {
            response.sendRedirect(URLHelper.getErrorRedirectLocation(redirectURI, "unsupported_response_type", "Only response type '" + OAuthProviderConstants.RESPONSE_TYPE_AUTH_CODE + "' is supported.", OAuthProviderConstants.PARAM_STATE, state));
            return false;
        }

        return true;
    }

    /**
     * Checks if the request contains a valid 'scope' parameter. If the parameter is missing,
     * the clients default scope is returned. Otherwise the provided scope is returned unless
     * it is invalid. In that case an error redirect is sent and <code>null</code> is returned.
     *
     * @param request The request
     * @param response The response
     * @param client The client
     * @param redirectURI The redirect URI
     * @param state The client state
     * @return The scope or <code>null</code> if the requested one was invalid
     * @throws IOException
     * @throws OXException If constructing the error redirect fails
     */
    private Scope checkScope(HttpServletRequest request, HttpServletResponse response, Client client, String redirectURI, String state) throws IOException, OXException {
        Scope scope;
        String scopeStr = request.getParameter(OAuthProviderConstants.PARAM_SCOPE);
        if (Strings.isEmpty(scopeStr)) {
            scope = client.getDefaultScope();
        } else if (oAuthProvider.isValidScope(scopeStr)) {
            scope = Scope.parseScope(scopeStr);
        } else {
            response.sendRedirect(URLHelper.getErrorRedirectLocation(redirectURI, "invalid_scope", "Invalid scope: " + scopeStr, OAuthProviderConstants.PARAM_STATE, state));
            return null;
        }

        return scope;
    }

    private String compileLoginPage(HttpServletRequest request, AuthorizationRequest authRequest, String csrfToken, LoginError error) throws OXException, IOException {
        ServerConfigService serverConfigService = requireService(ServerConfigService.class, services);
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        TemplateService templateService = requireService(TemplateService.class, services);
        OXTemplate loginPage = templateService.loadTemplate("oauth-provider-login.tmpl", OXTemplateExceptionHandler.RETHROW_HANDLER);

        // build replacement strings
        Locale locale = determineLocale(request);
        Translator translator = translatorFactory.translatorFor(locale);
        Map<String, Object> vars = new HashMap<>();
        vars.put("lang", locale.getLanguage());
        String title = translator.translate(OAuthProviderStrings.LOGIN);
        vars.put("title", title);
        vars.put("productName", serverConfigService.getServerConfig(URLHelper.getHostname(request), ConfigProviderService.NO_USER, ConfigProviderService.NO_CONTEXT).getProductName());
        vars.put("target", getAuthorizationEndpointURL(request));
        vars.put("formHeading", title);
        vars.put("usernameLabel", translator.translate(OAuthProviderStrings.USERNAME));
        vars.put("passwordLabel", translator.translate(OAuthProviderStrings.PASSWORD));
        vars.put("allowLabel", translator.translate(OAuthProviderStrings.ALLOW));
        vars.put("denyLabel", translator.translate(OAuthProviderStrings.DENY));
        vars.put("clientId", authRequest.getClient().getId());
        vars.put("redirectURI", authRequest.getRedirectURI());
        vars.put("scopes", authRequest.getScope().toString());
        vars.put("state", authRequest.getState());
        vars.put("csrfToken", csrfToken);
        if (error != null) {
            vars.put("error", translator.translate(error.getMessage()));
        }

        StringWriter writer = new StringWriter();
        loginPage.process(vars, writer);
        return writer.toString();
    }

    private String compileAuthorizationPage(HttpServletRequest request, AuthorizationRequest authRequest, String csrfToken, SessionResult sessionResult) throws OXException, IOException {
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        TemplateService templateService = requireService(TemplateService.class, services);
        HtmlService htmlService = requireService(HtmlService.class, services);
        OXTemplate loginPage = templateService.loadTemplate("oauth-provider-authorization.tmpl", OXTemplateExceptionHandler.RETHROW_HANDLER);

        // build replacement strings
        Locale locale = determineLocale(request);
        Translator translator = translatorFactory.translatorFor(locale);
        Map<String, Object> vars = new HashMap<>();
        vars.put("lang", locale.getLanguage());
        String title = translator.translate(OAuthProviderStrings.LOGIN);
        vars.put("title", title);
        vars.put("iconURL", icon2HTMLDataSource(authRequest.getClient().getIcon()));
        String clientName = htmlService.htmlFormat(authRequest.getClient().getName());
        vars.put("iconAlternative", clientName);
        vars.put("intro", translator.translate(String.format(OAuthProviderStrings.OAUTH_INTRO, clientName)));
        List<String> descriptions = new ArrayList<>(authRequest.getScope().size());
        for (String token : authRequest.getScope().get()) {
            OAuthScopeProvider scopeProvider = oAuthProvider.getScopeProvider(token);
            if (scopeProvider == null) {
                LOG.warn("No scope provider available for token {}", token);
                descriptions.add(token);
            } else {
                descriptions.add(translator.translate(scopeProvider.getDescription()));
            }
        }
        vars.put("scopeDescriptions", descriptions);
        vars.put("target", getAuthorizationEndpointURL(request));
        vars.put("allowLabel", translator.translate(OAuthProviderStrings.ALLOW));
        vars.put("denyLabel", translator.translate(OAuthProviderStrings.DENY));
        vars.put("clientId", authRequest.getClient().getId());
        vars.put("redirectURI", authRequest.getRedirectURI());
        vars.put("scopes", authRequest.getScope().toString());
        vars.put("state", authRequest.getState());
        vars.put("csrfToken", csrfToken);
        vars.put("session", sessionResult.getSessionId());
        vars.put("sessionType", sessionResult.getType().getCode());

        StringWriter writer = new StringWriter();
        loginPage.process(vars, writer);
        return writer.toString();
    }

    private final String getAuthorizationEndpointURL(HttpServletRequest request) throws OXException {
        return URLHelper.getBaseLocation(request) + OAuthProviderConstants.AUTHORIZATION_SERVLET_ALIAS;
    }

    private static enum LoginError {
        INVALID_CREDENTIALS(1, LoginExceptionMessages.INVALID_CREDENTIALS_MSG),
        UPDATE_TASK(2, ContextExceptionMessage.UPDATE_MSG),
        GRANTS_EXCEEDED(3, OAuthProviderExceptionMessages.GRANTS_EXCEEDED_MSG);

        private static final Map<Integer, LoginError> errorsByCodes = new HashMap<>(3, 0.75f);
        static {
            for (LoginError error : LoginError.values()) {
                errorsByCodes.put(error.code, error);
            }
        }

        private final int code;
        private final String msg;

        private LoginError(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public String getCode() {
            return Integer.toString(code);
        }

        public String getMessage() {
            return msg;
        }

        public static LoginError forCode(String code) {
            if (code == null) {
                return null;
            }

            try {
                int intCode = Integer.parseInt(code);
                return errorsByCodes.get(intCode);
            } catch (NumberFormatException e) {
                return null;
            }
        }

    }

}
