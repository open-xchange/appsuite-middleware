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

package com.openexchange.oauth.provider.impl.servlets;

import static com.openexchange.login.Interface.HTTP_JSON;
import static com.openexchange.osgi.Tools.requireService;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import com.google.common.net.HttpHeaders;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
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
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogProperties;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.oauth.provider.authorizationserver.client.Client;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException;
import com.openexchange.oauth.provider.authorizationserver.client.Icon;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantManagement;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.exceptions.OAuthProviderExceptionMessages;
import com.openexchange.oauth.provider.impl.OAuthProviderConstants;
import com.openexchange.oauth.provider.impl.OAuthProviderProperties;
import com.openexchange.oauth.provider.impl.ScopeProviderRegistry;
import com.openexchange.oauth.provider.impl.notification.OAuthMailNotificationService;
import com.openexchange.oauth.provider.impl.tools.URLHelper;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessionExceptionMessages;
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

    private static final String STRING_SPLITTER = "#split#";

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AuthorizationEndpoint.class);

    private static final long serialVersionUID = 6393806486708501254L;

    /**
     * Initializes a new {@link AuthorizationEndpoint}.
     */
    public AuthorizationEndpoint(ClientManagement clientManagement, GrantManagement grantManagement, ServiceLookup services) {
        super(clientManagement, grantManagement, services);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Tools.disableCaching(response);
        applyFrameOptions(response);
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
                Session session = checkSession(request, response, authRequest);
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
                    redirectLocation = serviceUnavailable(authRequest);
                } else {
                    redirectLocation = serverError(authRequest);
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Tools.disableCaching(response);
        applyFrameOptions(response);
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
            boolean accessDenied = Boolean.parseBoolean(request.getParameter(OAuthProviderConstants.PARAM_ACCESS_DENIED));
            Session session = checkSession(request, response, authRequest);

            // Note: The session was created only for the authorization purpose, so we should terminate it if possible.
            String redirectLocation;
            if (accessDenied) {
                if (session == null) {
                    redirectLocation = accessDenied(authRequest);
                } else {
                    terminateSession(request, response, authRequest, session);
                    redirectLocation = accessDenied(authRequest);
                }
            } else {
                if (session == null) {
                    redirectLocation = handleLogin(request, response, authRequest);
                } else {
                    redirectLocation = handleAuthorization(request, response, authRequest, session);
                    terminateSession(request, response, authRequest, session);
                }
            }

            response.sendRedirect(redirectLocation);
        } catch (OXException e) {
            /*
             * Responding with an error redirect failed. We can only display a proper message in the popup now.
             */
            LOG.error("Could not send error redirect for authorization POST request", e);
            respondWithErrorPage(request, response, e);
        }
    }

    /**
     * Checks if the servlet request contains a valid session ID paramter.
     *
     * @param request The servlet request
     * @param response The servlet response
     * @param authRequest The authorization request
     * @return The session or <code>null</code> if a session ID was not present, invalid or
     *         a security issue exists.
     * @throws OXException If a non-recoverable error occurs
     */
    private Session checkSession(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authRequest) throws OXException {
        String sessionId = request.getParameter(OAuthProviderConstants.PARAM_SESSION);
        if (sessionId != null) {
            // Session must only be provided by a previous login POST. This is enforced via additional cookie hash parameters.
            LoginConfiguration loginConfig = LoginServlet.getLoginConfiguration();
            Session session = requireService(SessiondService.class, services).getSession(sessionId);
            if (session != null) {
                Map<String, Cookie> cookies = Cookies.cookieMapFor(request);
                Cookie secretCookie = cookies.get(LoginServlet.SECRET_PREFIX + cookieHash(request, authRequest));
                if (secretCookie != null && session.getSecret().equals(secretCookie.getValue())) {
                    String remoteAddress = request.getRemoteAddr();
                    if (loginConfig.isIpCheck()) {
                        try {
                            SessionUtility.checkIP(true, loginConfig.getRanges(), session, remoteAddress, loginConfig.getIpCheckWhitelist());
                            return session;
                        } catch (OXException e) {
                            if (SessionExceptionCodes.WRONG_CLIENT_IP.equals(e)) {
                                LOG.debug("Client IP check failed during OAuth flow.");
                            } else {
                                throw e;
                            }
                        }
                    } else {
                        return session;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Performs a login based on the credentials contained as servlet request parameters.
     * On successful authentication the session cookies get set on the servlet response
     * and the session is returned.
     *
     * @param request The servlet request
     * @param response The servlet response
     * @param authRequest The authorization request
     * @return The created session
     * @throws OXException If login failed
     */
    private Session createSession(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authRequest) throws OXException {
        // Authenticate
        LoginConfiguration loginConfig = LoginServlet.getLoginConfiguration();
        String serverName = URLHelper.getHostname(request);
        String hash = cookieHash(request, authRequest);
        LoginRequestImpl loginRequest = new LoginRequestImpl.Builder()
            .login(request.getParameter("login"))
            .password(request.getParameter("password"))
            .clientIP(LoginTools.parseClientIP(request))
            .userAgent(LoginTools.parseUserAgent(request))
            .authId(UUIDs.getUnformattedStringFromRandom())
            .client(getLoginClient(authRequest))
            .hash(hash)
            .iface(HTTP_JSON)
            .headers(Tools.copyHeaders(request))
            .cookies(Tools.getCookieFromHeader(request))
            .secure(Tools.considerSecure(request, true))
            .serverName(serverName)
            .serverPort(request.getServerPort())
            .httpSessionID(request.getSession(true).getId())
            .tranzient(true)
            .build();

        LoginResult loginResult = LoginPerformer.getInstance().doLogin(loginRequest);
        Session session = loginResult.getSession();

        // Add session log properties
        LogProperties.putSessionProperties(session);

        // Add headers and cookies from login result
        LoginServlet.addHeadersAndCookies(loginResult, response);

        // Add secret and public cookie
        LoginServlet.writeSecretCookie(request, response, session, hash, true, serverName, loginConfig);

        return session;
    }

    /**
     * Terminates the passed session, i.e. a logout is performed and the session cookies are
     * removed via the servlet response.
     *
     * @param request The servlet request
     * @param response The servlet response
     * @param authRequest The authorization request
     * @param session The session
     */
    private void terminateSession(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authRequest, Session session) {
        try {
            SessionUtility.removeOXCookies(cookieHash(request, authRequest), request, response, session);
            LoginPerformer.getInstance().doLogout(session.getSessionID());
        } catch (OXException e) {
            LOG.warn("Error while terminating OAuth provider authorization session", e);
        }
    }

    /**
     * Handles the authorization request of the user and generates the according redirect location.
     *
     * @param request The servlet request
     * @param response The servlet response
     * @param authRequest The authorization request
     * @param session The session
     * @return The redirect location
     * @throws OXException If a non-recoverable error occurs
     */
    private String handleAuthorization(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authRequest, Session session) throws OXException {
        try {
            ServerSession serverSession = ServerSessionAdapter.valueOf(session);
            Context context = serverSession.getContext();
            User user = serverSession.getUser();

            // Check if OAuth is deactivated for this user
            ConfigView configView = requireService(ConfigViewFactory.class, services).getView(user.getId(), context.getContextId());
            if (!configView.opt(OAuthProviderProperties.ENABLED, Boolean.class, Boolean.TRUE).booleanValue() || user.isGuest()) {
                return URLHelper.getErrorRedirectLocation(
                    authRequest.getRedirectURI(),
                    "access_denied",
                    "The user is not allowed to grant OAuth access to 3rd party applications.",
                    OAuthProviderConstants.PARAM_STATE,
                    authRequest.getState());
            }

            // Everything OK, send notification mail and do the redirect with authorization code & state
            String code = grantManagement.generateAuthorizationCodeFor(authRequest.getClient().getId(), authRequest.getRedirectURI(), authRequest.getScope(), session);
            String redirectLocation = URLHelper.getRedirectLocation(
                authRequest.getRedirectURI(),
                OAuthProviderConstants.PARAM_CODE,
                code,
                OAuthProviderConstants.PARAM_STATE,
                authRequest.getState());

            try {
                OAuthMailNotificationService notificationService = new OAuthMailNotificationService();
                notificationService.sendNotification(serverSession, authRequest.getClient(), request);
            } catch (OXException e) {
                LOG.error("Sending OAuth notification mail to {} failed for client {}.", user.getMail(), authRequest.getClient().getId(), e);
            }

            return redirectLocation;
        } catch (OXException e) {
            if (SessionExceptionCodes.SESSION_EXPIRED.equals(e) || SessionExceptionCodes.WRONG_SESSION_SECRET.equals(e)) {
                Map<String, String> redirectParams = prepareSelfRedirectParams(request, authRequest);
                redirectParams.put(OAuthProviderConstants.PARAM_ERROR, LoginError.SESSION_EXPIRED.getCode());
                return URLHelper.getRedirectLocation(getAuthorizationEndpointURL(request), redirectParams);
            }

            throw e;
        }
    }

    /**
     * Compiles the login page and returns it as String.
     *
     * @param request
     * @param authRequest
     * @param csrfToken
     * @param error
     * @return
     * @throws OXException
     * @throws IOException
     */
    private String compileLoginPage(HttpServletRequest request, AuthorizationRequest authRequest, String csrfToken, LoginError error) throws OXException {
        ServerConfigService serverConfigService = requireService(ServerConfigService.class, services);
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        TemplateService templateService = requireService(TemplateService.class, services);
        OXTemplate loginPage = templateService.loadTemplate("oauth-provider-login.tmpl", OXTemplateExceptionHandler.RETHROW_HANDLER);

        // build replacement strings
        Locale locale = determineLocale(request);
        Translator translator = translatorFactory.translatorFor(locale);
        String productName = serverConfigService.getServerConfig(URLHelper.getHostname(request), ConfigProviderService.NO_USER, ConfigProviderService.NO_CONTEXT).getProductName();
        String title = String.format(translator.translate(OAuthProviderStrings.POPUP_TITLE), authRequest.getClient().getName());
        String headline = String.format(translator.translate(OAuthProviderStrings.LOGIN_FORM_HEADLINE), productName, authRequest.getClient().getName());

        Map<String, Object> vars = new HashMap<>();
        vars.put("lang", locale.getLanguage());
        vars.put("title", title);
        vars.put("headline", headline);
        if (error != null) {
            vars.put("error", translator.translate(error.getMessage()));
        }
        vars.put("target", getAuthorizationEndpointURL(request));
        vars.put("usernameLabel", translator.translate(OAuthProviderStrings.USERNAME));
        vars.put("passwordLabel", translator.translate(OAuthProviderStrings.PASSWORD));
        vars.put("cancelLabel", translator.translate(OAuthProviderStrings.CANCEL));
        vars.put("loginLabel", translator.translate(OAuthProviderStrings.LOGIN));
        vars.put("clientId", authRequest.getClient().getId());
        vars.put("redirectURI", authRequest.getRedirectURI());
        vars.put("scopes", authRequest.getScope().toString());
        vars.put("state", authRequest.getState());
        vars.put("csrfToken", csrfToken);
        vars.put("language", locale.toString());

        StringWriter writer = new StringWriter();
        loginPage.process(vars, writer);
        return writer.toString();
    }

    /**
     * Handles the users login request. As a result a redirect location is generated - either containing a
     * session ID or an error code.
     *
     * @param request The servlet request
     * @param response The servlet response
     * @param authRequest The authorization request
     * @return The redirect location
     * @throws OXException If a non-recoverable error occurs
     */
    private String handleLogin(HttpServletRequest request, HttpServletResponse response, AuthorizationRequest authRequest) throws OXException {
        Map<String, String> redirectParams = prepareSelfRedirectParams(request, authRequest);
        String login = request.getParameter("login");
        String password = request.getParameter("password");
        if (Strings.isEmpty(login) || Strings.isEmpty(password)) {
            redirectParams.put(OAuthProviderConstants.PARAM_ERROR, LoginError.INVALID_CREDENTIALS.getCode());
        } else {
            Session session;
            try {
                session = createSession(request, response, authRequest);
                redirectParams.put(OAuthProviderConstants.PARAM_SESSION, session.getSessionID());
            } catch (OXException e) {
                // Let the popup display this error so that the user can retry or does at least now why he can't grant access
                String redirectLocation;
                if (LoginExceptionCodes.INVALID_CREDENTIALS.equals(e)) {
                    redirectParams.put(OAuthProviderConstants.PARAM_ERROR, LoginError.INVALID_CREDENTIALS.getCode());
                    redirectLocation = URLHelper.getRedirectLocation(getAuthorizationEndpointURL(request), redirectParams);
                } else if (ContextExceptionCodes.UPDATE.equals(e)) {
                    redirectParams.put(OAuthProviderConstants.PARAM_ERROR, LoginError.UPDATE_TASK.getCode());
                    redirectLocation = URLHelper.getRedirectLocation(getAuthorizationEndpointURL(request), redirectParams);
                } else if (OAuthProviderExceptionCodes.GRANTS_EXCEEDED.equals(e)) {
                    redirectParams.put(OAuthProviderConstants.PARAM_ERROR, LoginError.GRANTS_EXCEEDED.getCode());
                    redirectLocation = URLHelper.getRedirectLocation(getAuthorizationEndpointURL(request), redirectParams);
                } else if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                    LOG.error("Login for OAuth authorization request failed", e);
                    redirectLocation = URLHelper.getErrorRedirectLocation(
                        authRequest.getRedirectURI(),
                        "temporarily_unavailable",
                        "The service is currently not available.",
                        OAuthProviderConstants.PARAM_STATE,
                        authRequest.getState());
                } else {
                    LOG.error("Login for OAuth authorization failed", e);
                    redirectLocation = URLHelper.getErrorRedirectLocation(
                        authRequest.getRedirectURI(),
                        "server_error",
                        "An internal error occurred.",
                        OAuthProviderConstants.PARAM_STATE,
                        authRequest.getState());
                }

                return redirectLocation;
            }
        }

        return URLHelper.getRedirectLocation(getAuthorizationEndpointURL(request), redirectParams);
    }

    /**
     * Compiles the authorization page and returns it as String.
     *
     * @param request
     * @param authRequest
     * @param csrfToken
     * @param session
     * @return
     * @throws OXException
     * @throws IOException
     */
    private String compileAuthorizationPage(HttpServletRequest request, AuthorizationRequest authRequest, String csrfToken, Session session) throws OXException {
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        TemplateService templateService = requireService(TemplateService.class, services);
        OXTemplate loginPage = templateService.loadTemplate("oauth-provider-authorization.tmpl", OXTemplateExceptionHandler.RETHROW_HANDLER);

        Locale locale = determineLocale(request);
        Translator translator = translatorFactory.translatorFor(locale);
        String title = String.format(translator.translate(OAuthProviderStrings.POPUP_TITLE), authRequest.getClient().getName());
        String intro = translator.translate(OAuthProviderStrings.AUTHORIZATION_INTRO);
        int splitIndex = intro.indexOf(STRING_SPLITTER);
        String introPre = intro;
        String introPost = "";
        if (splitIndex >= 0) {
            introPre = intro.substring(0, splitIndex);
            if (splitIndex + STRING_SPLITTER.length() <= intro.length()) {
                introPost = intro.substring(splitIndex + STRING_SPLITTER.length());
            }
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("lang", locale.getLanguage());
        vars.put("title", title);
        vars.put("headline", title);
        vars.put("clientDescription", authRequest.getClient().getDescription());
        vars.put("iconURL", icon2HTMLDataSource(authRequest.getClient().getIcon()));
        vars.put("iconAlternative", authRequest.getClient().getName());
        vars.put("introPre", introPre);
        vars.put("clientWebsite", authRequest.getClient().getWebsite());
        vars.put("clientName", authRequest.getClient().getName());
        vars.put("introPost", introPost);
        List<String> descriptions = new ArrayList<>(authRequest.getScope().size());
        for (String token : authRequest.getScope().get()) {
            OAuthScopeProvider scopeProvider = grantManagement.getScopeProvider(token);
            if (scopeProvider == null) {
                LOG.warn("No scope provider available for token {}", token);
                descriptions.add(token);
            } else {
                descriptions.add(translator.translate(scopeProvider.getDescription()));
            }
        }
        vars.put("scopeDescriptions", descriptions);
        vars.put("target", getAuthorizationEndpointURL(request));
        vars.put("clientId", authRequest.getClient().getId());
        vars.put("redirectURI", authRequest.getRedirectURI());
        vars.put("scopes", authRequest.getScope().toString());
        vars.put("state", authRequest.getState());
        vars.put("csrfToken", csrfToken);
        vars.put("session", session.getSessionID());
        vars.put("denyLabel", translator.translate(OAuthProviderStrings.DENY));
        vars.put("allowLabel", translator.translate(OAuthProviderStrings.ALLOW));
        vars.put("footer", translator.translate(OAuthProviderStrings.AUTHORIZATION_FOOTER));
        vars.put("language", locale.toString());

        StringWriter writer = new StringWriter();
        loginPage.process(vars, writer);
        return writer.toString();
    }

    /**
     * Compiles a HTML data-URI for the icon image.
     *
     * @param icon The icon
     * @return A URI in the form of <code>data:image/png;charset=UTF-8;base64,iVBORw0KGgoAAAANS...</code>
     * @throws IOException
     */
    private static String icon2HTMLDataSource(Icon icon) {
        return "data:" + icon.getMimeType() + ";charset=UTF-8;base64," + Base64.encodeBase64String(icon.getData());
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
            respondWithErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid referer.");
            return true;
        }

        if (isInvalidCSRFToken(request)) {
            respondWithErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid CSRF token.");
            return true;
        }

        return false;
    }

    /**
     * Checks if the current HTTP session already contains a CSRF token and the passed request contains
     * the same token as parameter.
     *
     * @param request
     * @return <code>true</code> if either the HTTP session or the request did NOT contain a valid CSRF token.
     */
    private static boolean isInvalidCSRFToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }

        String csrfToken = (String) session.getAttribute(ATTR_OAUTH_CSRF_TOKEN);
        if (csrfToken == null) {
            return true;
        }

        String actualToken = request.getParameter(OAuthProviderConstants.PARAM_CSRF_TOKEN);
        if (actualToken == null) {
            return true;
        }

        if (!csrfToken.equals(actualToken)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the referer header of the passed request matches the scheme, host, port and
     * path of this servlet.
     *
     * @param request
     * @return <code>true</code> if the referer header is not set or invalid
     */
    private static boolean isInvalidReferer(HttpServletRequest request) {
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (Strings.isEmpty(referer)) {
            return true;
        }

        try {
            URI expectedReferer = new URI(URLHelper.getSecureLocation(request));
            URI actualReferer = new URI(referer);
            if (!stringsEqual(expectedReferer.getScheme(), actualReferer.getScheme())) {
                return true;
            }

            if (!stringsEqual(expectedReferer.getHost(), actualReferer.getHost())) {
                return true;
            }

            if (expectedReferer.getPort() != actualReferer.getPort()) {
                return true;
            }

            if (!stringsEqual(normalizePath(expectedReferer.getPath()), normalizePath(actualReferer.getPath()))) {
                return true;
            }
        } catch (URISyntaxException e) {
            return true;
        }

        return false;
    }

    /**
     * Null-safe method for comparing two strings
     *
     * @param str1 string or <code>null</code>
     * @param str2 string or <code>null</code>
     * @return <code>true</code> if both strings are <code>null</code> or equal
     */
    private static boolean stringsEqual(String str1, String str2) {
        if (str1 != null && str2 != null) {
            return str1.equals(str2);
        }

        return str1 == null && str2 == null;
    }

    /**
     * Normalizes a servlet path for comparison by removing the dispatcher prefix if present.
     *
     * @param path The path or <code>null</code>
     * @return The normalized path or <code>null</code> if the input was also <code>null</code>
     */
    private static String normalizePath(String path) {
        if (path != null) {
            int index = path.indexOf(OAuthProviderConstants.AUTHORIZATION_SERVLET_ALIAS);
            if (index >= 0) {
                return path.substring(index);
            }
        }

        return null;
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
            respondWithErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_CLIENT_ID);
            return null;
        }

        try {
            Client client = clientManagement.getClientById(clientId);
            if (client == null || !client.isEnabled()) {
                respondWithErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Invalid client ID: " + clientId);
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
            respondWithErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_REDIRECT_URI);
            return null;
        }

        if (!client.hasRedirectURI(redirectURI)) {
            respondWithErrorPage(request, response, HttpServletResponse.SC_BAD_REQUEST, "Invalid redirect URI: " + redirectURI);
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
            response.sendRedirect(URLHelper.getErrorRedirectLocation(
                redirectURI,
                "invalid_request",
                "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_STATE));
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
            response.sendRedirect(URLHelper.getErrorRedirectLocation(
                redirectURI,
                "invalid_request",
                "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_RESPONSE_TYPE,
                OAuthProviderConstants.PARAM_STATE,
                state));
            return false;
        }

        if (!OAuthProviderConstants.RESPONSE_TYPE_AUTH_CODE.equals(responseType)) {
            response.sendRedirect(URLHelper.getErrorRedirectLocation(
                redirectURI,
                "unsupported_response_type",
                "Only response type '" + OAuthProviderConstants.RESPONSE_TYPE_AUTH_CODE + "' is supported.",
                OAuthProviderConstants.PARAM_STATE,
                state));
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
        } else if (isValidScope(scopeStr)) {
            scope = Scope.parseScope(scopeStr);
        } else {
            response.sendRedirect(URLHelper.getErrorRedirectLocation(redirectURI, "invalid_scope", "Invalid scope: " + scopeStr, OAuthProviderConstants.PARAM_STATE, state));
            return null;
        }

        return scope;
    }

    /**
     * Sets the response header <code>X-Frame-Options</code> to <code>SAMEORIGIN</code> to avoid
     * clickjacking attacks ({@link http://tools.ietf.org/html/draft-ietf-oauth-v2-23#section-10.13}).
     *
     * @param response The servlet response
     */
    private static void applyFrameOptions(HttpServletResponse response) {
        response.setHeader(HttpHeaders.X_FRAME_OPTIONS, "SAMEORIGIN");
    }

    /**
     * Calculates the session cookie hash.
     *
     * @param request The servlet request
     * @param authRequest The authorization request
     * @return The hash
     */
    private static String cookieHash(HttpServletRequest request, AuthorizationRequest authRequest) {
        return HashCalculator.getInstance().getHash(
            request,
            LoginTools.parseUserAgent(request),
            getLoginClient(authRequest),
            authRequest.getRedirectURI(),
            authRequest.getScope().toString(),
            authRequest.getState());
    }

    /**
     * Gets the client identifier for the login request.
     *
     * @param authRequest The authorization request
     * @return The client identifier
     */
    private static String getLoginClient(AuthorizationRequest authRequest) {
        return "OAuth Client " + authRequest.getClient().getId();
    }

    /**
     * Prepares the query parameters for a redirect towards this same servlet
     * based on the passed {@link AuthorizationRequest}.
     *
     * @param request The servlet request
     * @param authRequest The authorization request
     * @return A map of query parameters
     */
    private static Map<String, String> prepareSelfRedirectParams(HttpServletRequest request, AuthorizationRequest authRequest) {
        Map<String, String> redirectParams = new LinkedHashMap<>();
        redirectParams.put(OAuthProviderConstants.PARAM_CLIENT_ID, authRequest.getClient().getId());
        redirectParams.put(OAuthProviderConstants.PARAM_REDIRECT_URI, authRequest.getRedirectURI());
        redirectParams.put(OAuthProviderConstants.PARAM_STATE, authRequest.getState());
        redirectParams.put(OAuthProviderConstants.PARAM_SCOPE, authRequest.getScope().toString());
        redirectParams.put(OAuthProviderConstants.PARAM_RESPONSE_TYPE, OAuthProviderConstants.RESPONSE_TYPE_AUTH_CODE);
        redirectParams.put(OAuthProviderConstants.PARAM_LANGUAGE, determineLocale(request).toString());
        return redirectParams;
    }

    /**
     * Checks if an error code is present as query parameter on the servlet request.
     *
     * @param request The servlet request
     * @return The error or <code>null</code> if none is present or the parameter
     *         value is invalid.
     */
    private static LoginError optLoginError(HttpServletRequest request) {
        String errorCode = request.getParameter(OAuthProviderConstants.PARAM_ERROR);
        if (errorCode == null) {
            return null;
        }

        return LoginError.forCode(errorCode);
    }

    /**
     * Gets the URL of this endpoint.
     *
     * @param request The servlet request
     * @return The URL
     * @throws OXException If generating the URL fails
     */
    private static String getAuthorizationEndpointURL(HttpServletRequest request) throws OXException {
        return URLHelper.getBaseLocation(request) + OAuthProviderConstants.AUTHORIZATION_SERVLET_ALIAS;
    }

    /**
     * Generates the error redirect for type <code>temporarily_unavailable</code>.
     *
     * @param authRequest The authorization request
     * @return The redirect location
     * @throws OXException If building the URI fails
     */
    private static String serviceUnavailable(AuthorizationRequest authRequest) throws OXException {
        return URLHelper.getErrorRedirectLocation(
            authRequest.getRedirectURI(),
            "temporarily_unavailable",
            "The service is currently not available.",
            OAuthProviderConstants.PARAM_STATE,
            authRequest.getState());
    }

    /**
     * Generates the error redirect for type <code>server_error</code>.
     *
     * @param authRequest The authorization request
     * @return The redirect location
     * @throws OXException If building the URI fails
     */
    private static String serverError(AuthorizationRequest authRequest) throws OXException {
        return URLHelper.getErrorRedirectLocation(
            authRequest.getRedirectURI(),
            "server_error",
            "An internal error occurred.",
            OAuthProviderConstants.PARAM_STATE,
            authRequest.getState());
    }

    private static String accessDenied(AuthorizationRequest authRequest) throws OXException {
        return URLHelper.getErrorRedirectLocation(
            authRequest.getRedirectURI(),
            "access_denied",
            "The user denied your request.",
            OAuthProviderConstants.PARAM_STATE,
            authRequest.getState());
    }

    private static boolean isValidScope(String scopeString) {
        if (Scope.isValidScopeString(scopeString)) {
            Scope scope = Scope.parseScope(scopeString);
            if (scope.size() == 0) {
                return false;
            }

            for (String token : scope.get()) {
                if (!ScopeProviderRegistry.getInstance().hasScopeProvider(token)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Encapsulates the request data of a valid request towards the authorization endpoint.
     */
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

        /**
         * Gets the client APPs ID.
         * @return The id
         */
        public Client getClient() {
            return client;
        }

        /**
         * Gets the requested redirect URI.
         * @return The URI
         */
        public String getRedirectURI() {
            return redirectURI;
        }

        /**
         * Gets the requested state.
         * @return The state
         */
        public String getState() {
            return state;
        }

        /**
         * Gets the requested scope.
         * @return The scope
         */
        public Scope getScope() {
            return scope;
        }

    }

    /**
     * Enumeration of errors which shall be displayed on the login page on occurrence.
     */
    private static enum LoginError {
        /**
         * The provided credentials are invalid.
         */
        INVALID_CREDENTIALS(1, LoginExceptionMessages.INVALID_CREDENTIALS_MSG),
        /**
         * Login not possible, update tasks are running.
         */
        UPDATE_TASK(2, ContextExceptionMessage.UPDATE_MSG),
        /**
         * The user exceeded the max. number of possible grants.
         */
        GRANTS_EXCEEDED(3, OAuthProviderExceptionMessages.GRANTS_EXCEEDED_MSG),
        /**
         * The session expired.
         */
        SESSION_EXPIRED(4, SessionExceptionMessages.SESSION_EXPIRED_MSG);

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

        /**
         * Gets the error code that is to be passed around via an URL parameter.
         * @return The error code
         */
        public String getCode() {
            return Integer.toString(code);
        }

        /**
         * Gets the localizable error message.
         * @return The error message
         */
        public String getMessage() {
            return msg;
        }

        /**
         * Gets the error instance for the given code.
         * @param code The code
         * @return The error or <code>null</code> if the code is <code>null</code> or invalid
         */
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
