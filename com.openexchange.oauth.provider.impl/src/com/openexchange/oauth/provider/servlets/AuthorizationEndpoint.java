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

package com.openexchange.oauth.provider.servlets;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.osgi.Tools.requireService;
import static com.openexchange.tools.servlet.http.Tools.sendErrorPage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.google.common.net.HttpHeaders;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.service.Authentication;
import com.openexchange.authorization.AuthorizationService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.oauth.provider.DefaultScopes;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.OAuthScopeProvider;
import com.openexchange.oauth.provider.Scopes;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.client.ClientManagementException;
import com.openexchange.oauth.provider.internal.OAuthProviderProperties;
import com.openexchange.oauth.provider.internal.URLHelper;
import com.openexchange.oauth.provider.notification.OAuthMailNotificationService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.OXTemplateExceptionHandler;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.servlet.http.Tools;

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

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AuthorizationEndpoint}.
     */
    public AuthorizationEndpoint(OAuthProviderService oAuthProvider, ServiceLookup services) {
        super(oAuthProvider);
        this.services = services;
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

        try {
            // Set JSESSIONID cookie and generate CSRF token
            HttpSession session = request.getSession(true);
            String csrfToken = UUIDs.getUnformattedStringFromRandom();
            session.setAttribute(ATTR_OAUTH_CSRF_TOKEN, csrfToken);

            try {
                if (respondWithForm(request)) {
                    // Send login form
                    String loginPage = compileLoginPage(request, authRequest.getRedirectURI(), authRequest.getState(), csrfToken, authRequest.getClient(), DefaultScopes.parseScope(authRequest.getScope()), determineLocale(request));
                    response.setContentType("text/html; charset=UTF-8");
                    response.setHeader("Content-Disposition", "inline");
                    response.setStatus(200);
                    PrintWriter writer = response.getWriter();
                    writer.write(loginPage);
                    writer.flush();
                } else {
                    // Redirect to appsuite login screen
                    StringBuilder fragment = new StringBuilder()
                        .append("login_type=oauth").append('&')
                        .append("client_id=").append(authRequest.getClient().getId()).append('&')
                        .append("scope=").append(authRequest.getScope()).append('&')
                        .append("state=").append(authRequest.getState()).append('&')
                        .append("csrf_token=").append(csrfToken).append('&')
                        .append("redirect_uri=").append(authRequest.getRedirectURI()).append('&')
                        .append("language=").append(determineLocale(request).toString());

                    String uiWebPath = requireService(ConfigurationService.class, services).getProperty(ServerConfig.Property.UI_WEB_PATH.getPropertyName(), "/appsuite/");
                    URIBuilder redirectLocation = new URIBuilder()
                        .setScheme("https")
                        .setHost(URLHelper.getHostname(request))
                        .setPath(uiWebPath)
                        .setFragment(fragment.toString());
                    response.sendRedirect(redirectLocation.build().toString());
                }
            } catch (OXException e) {
                LOG.error("Authorization GET request failed", e);
                if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                    response.sendRedirect(URLHelper.getErrorRedirectLocation(authRequest.getRedirectURI(), "temporarily_unavailable", "The service is currently not available.", OAuthProviderConstants.PARAM_STATE, authRequest.getState()));
                }
                response.sendRedirect(URLHelper.getErrorRedirectLocation(authRequest.getRedirectURI(), "server_error", "An internal error occurred.", OAuthProviderConstants.PARAM_STATE, authRequest.getState()));
            } catch (URISyntaxException e) {
                LOG.error("Authorization GET request failed", e);
                response.sendRedirect(URLHelper.getErrorRedirectLocation(authRequest.getRedirectURI(), "server_error", "An internal error occurred.", OAuthProviderConstants.PARAM_STATE, authRequest.getState()));
            }
        } catch (OXException e) {
            /*
             * Responding with an error redirect failed. We can only display a proper message in the login popup now.
             */
            LOG.error("Could not send error redirect for authorization GET request", e);
            sendJSONError(request, response, e);
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
            // Check if user denied access
            String accessDenied = request.getParameter(OAuthProviderConstants.PARAM_ACCESS_DENIED);
            if (Boolean.parseBoolean(accessDenied)) {
                response.sendRedirect(URLHelper.getErrorRedirectLocation(authRequest.getRedirectURI(), "access_denied", "The user denied your request.", OAuthProviderConstants.PARAM_STATE, authRequest.getState()));
                return;
            }

            try {
                Authenticated authenticated = authenticate(request, response);
                if (authenticated == null) {
                    return;
                }

                // Perform user / context lookup
                Context ctx = findContext(authenticated.getContextInfo());
                User user = findUser(ctx, authenticated.getUserInfo());

                // Authorize
                AuthorizationService authService = requireService(AuthorizationService.class, services);
                authService.authorizeUser(ctx, user);

                // Check if OAuth is deactivated for this user
                ConfigView configView = requireService(ConfigViewFactory.class, services).getView(user.getId(), ctx.getContextId());
                if (!configView.opt(OAuthProviderProperties.ENABLED, Boolean.class, Boolean.TRUE).booleanValue() || user.isGuest()) {
                    response.sendRedirect(URLHelper.getErrorRedirectLocation(authRequest.getRedirectURI(), "access_denied", "The user is not allowed to grant OAuth access to 3rd party applications.", OAuthProviderConstants.PARAM_STATE, authRequest.getState()));
                    return;
                }

                // Everything OK, send notification mail and do the redirect with authorization code & state
                String code = oAuthProvider.generateAuthorizationCodeFor(authRequest.getClient().getId(), authRequest.getRedirectURI(), authRequest.getScope(), user.getId(), ctx.getContextId());
                try {
                    OAuthMailNotificationService notificationService = new OAuthMailNotificationService(oAuthProvider);
                    notificationService.sendNotification(user.getId(), ctx.getContextId(), authRequest.getClient().getId(), request);
                } catch (OXException e) {
                    LOG.error("Send oauth notification mail for {} to {} failed.", authRequest.getClient().getName(), user.getMail(), e);
                }

                String redirectLocation = URLHelper.getRedirectLocation(authRequest.getRedirectURI(), OAuthProviderConstants.PARAM_CODE, code, OAuthProviderConstants.PARAM_STATE, authRequest.getState());
                if (Boolean.parseBoolean(request.getParameter("redirect"))) {
                    response.sendRedirect(redirectLocation);
                } else {
                    sendJSONRedirect(request, response, redirectLocation);
                }
            } catch (OXException e) {
                if (LoginExceptionCodes.INVALID_CREDENTIALS.equals(e) || ContextExceptionCodes.UPDATE.equals(e) || OAuthProviderExceptionCodes.GRANTS_EXCEEDED.equals(e)) {
                    // Let the popup display this error so that the user can retry or does at least now why he can't grant access
                    sendJSONError(request, response, e);
                    return;
                }

                LOG.error("Authorization POST request failed", e);
                if (ServiceExceptionCode.SERVICE_UNAVAILABLE.equals(e)) {
                    response.sendRedirect(URLHelper.getErrorRedirectLocation(authRequest.getRedirectURI(), "temporarily_unavailable", "The service is currently not available.", OAuthProviderConstants.PARAM_STATE, authRequest.getState()));
                }

                response.sendRedirect(URLHelper.getErrorRedirectLocation(authRequest.getRedirectURI(), "server_error", "An internal error occurred.", OAuthProviderConstants.PARAM_STATE, authRequest.getState()));
            }
        } catch (OXException e) {
            /*
             * Responding with an error redirect failed. We can only display a proper message in the login popup now.
             */
            LOG.error("Could not send error redirect for authorization POST request", e);
            sendJSONError(request, response, e);
        }
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
            sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request contained no or invalid referer header");
            return true;
        }

        if (isInvalidCSRFToken(request)) {
            sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request contained no or invalid CSRF token. Ensure that cookies are allowed.");
            return true;
        }

        return false;
    }

    /**
     * @param request
     * @param response
     * @param e
     * @throws IOException
     */
    private void sendJSONError(HttpServletRequest request, HttpServletResponse response, OXException e) throws IOException {
        Response errorResponse = new Response();
        errorResponse.setLocale(determineLocale(request));
        errorResponse.setException(e);
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            ResponseWriter.write(errorResponse, response.getWriter());
        } catch (JSONException je) {
            LOG.error("Could not send error response", je);
            response.reset();
            Tools.disableCaching(response);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendJSONRedirect(HttpServletRequest request, HttpServletResponse response, String redirectLocation) throws IOException {
        try {
            JSONObject data = new JSONObject();
            data.put("redirect_uri", redirectLocation);

            Response redirectResponse = new Response();
            redirectResponse.setData(data);
            redirectResponse.setLocale(determineLocale(request));
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_OK);
            ResponseWriter.write(redirectResponse, response.getWriter());
        } catch (JSONException je) {
            LOG.error("Could not send redirect response", je);
            response.reset();
            Tools.disableCaching(response);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private boolean respondWithForm(HttpServletRequest request) {
        String respondWith = request.getParameter("respond_with");
        return "form".equals(respondWith);
    }

    private static final class AuthorizationRequest {

        private final Client client;

        private final String redirectURI;

        private final String state;

        private final String scope;

        protected AuthorizationRequest(Client client, String redirectURI, String state, String scope) {
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


        public String getScope() {
            return scope;
        }

    }

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

            String scope = checkScope(request, response, client, redirectURI, state);
            if (scope == null) {
                return null;
            }

            return new AuthorizationRequest(client, redirectURI, state, scope);
        } catch (OXException e) {
            /*
             * Unexpected error. We must not or cannot redirect but will respond with an error payload
             * that gets displayed by the popup.
             */
            sendJSONError(request, response, e);
            return null;
        }
    }

    private Authenticated authenticate(HttpServletRequest request, HttpServletResponse response) throws OXException, IOException {
        // Check user credentials
        String login = request.getParameter(OAuthProviderConstants.PARAM_USER_LOGIN);
        String password = request.getParameter(OAuthProviderConstants.PARAM_USER_PASSWORD);
        if (Strings.isEmpty(login) || Strings.isEmpty(password)) {
           throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
        }

        return Authentication.login(login, password, new HashMap<String, Object>(0));
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
            sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_CLIENT_ID);
            return null;
        }

        try {
            Client client = oAuthProvider.getClientManagement().getClientById(clientId);
            if (client == null || !client.isEnabled()) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Client not found: " + clientId);
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
            sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_REDIRECT_URI);
            return null;
        }

        if (!client.hasRedirectURI(redirectURI)) {
            sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid redirect URI: " + redirectURI);
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
    private String checkScope(HttpServletRequest request, HttpServletResponse response, Client client, String redirectURI, String state) throws IOException, OXException {
        String scope = request.getParameter(OAuthProviderConstants.PARAM_SCOPE);
        if (Strings.isEmpty(scope)) {
            scope = client.getDefaultScope().scopeString();
        } else {
            // Validate scope
            if (!oAuthProvider.isValidScopeString(scope)) {
                response.sendRedirect(URLHelper.getErrorRedirectLocation(redirectURI, "invalid_scope", "Invalid scope: " + scope, OAuthProviderConstants.PARAM_STATE, state));
                return null;
            }
        }

        return scope;
    }

    private String compileLoginPage(HttpServletRequest request, String redirectURI, String state, String csrfToken, Client client, Scopes scopes, Locale locale) throws OXException, IOException {
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        TemplateService templateService = requireService(TemplateService.class, services);
        HtmlService htmlService = requireService(HtmlService.class, services);
        OXTemplate loginPage = templateService.loadTemplate("oauth-provider-login.tmpl", OXTemplateExceptionHandler.RETHROW_HANDLER);

        // build replacement strings
        Translator translator = translatorFactory.translatorFor(locale);
        Map<String, Object> vars = new HashMap<>();
        vars.put("lang", locale.getLanguage());
        String title = translator.translate(OAuthProviderStrings.LOGIN);
        vars.put("title", title);
        vars.put("iconURL", icon2HTMLDataSource(client.getIcon()));
        String clientName = htmlService.htmlFormat(client.getName());
        vars.put("iconAlternative", clientName);
        vars.put("intro", translator.translate(String.format(OAuthProviderStrings.OAUTH_INTRO, clientName)));
        List<String> descriptions = new ArrayList<>(scopes.size());
        for (String scope : scopes.get()) {
            OAuthScopeProvider scopeProvider = oAuthProvider.getScopeProvider(scope);
            if (scopeProvider == null) {
                descriptions.add(scope);
            } else {
                descriptions.add(translator.translate(scopeProvider.getDescription()));
            }
        }
        vars.put("scopeDescriptions", descriptions);
        vars.put("target", getAuthorizationEndpointURL(request));
        vars.put("formHeading", title);
        vars.put("usernameLabel", translator.translate(OAuthProviderStrings.USERNAME));
        vars.put("passwordLabel", translator.translate(OAuthProviderStrings.PASSWORD));
        vars.put("allowLabel", translator.translate(OAuthProviderStrings.ALLOW));
        vars.put("denyLabel", translator.translate(OAuthProviderStrings.DENY));
        vars.put("clientId", client.getId());
        vars.put("redirectURI", redirectURI);
        vars.put("scopes", scopes.scopeString());
        vars.put("state", state);
        vars.put("csrfToken", csrfToken);

        StringWriter writer = new StringWriter();
        loginPage.process(vars, writer);
        return writer.toString();
    }

    private final String getAuthorizationEndpointURL(HttpServletRequest request) throws OXException {
        return URLHelper.getBaseLocation(request) + OAuthProviderConstants.AUTHORIZATION_SERVLET_ALIAS;
    }

    /**
     * Looks up the context for the supplied context info, throwing appropriate exceptions if not found.
     *
     * @param contextInfo The context info (as usually supplied in the login name)
     * @return The context
     * @throws OXException If context look-up fails
     */
    private Context findContext(String contextInfo) throws OXException {
        ContextStorage contextStor = ContextStorage.getInstance();
        int contextId = contextStor.getContextId(contextInfo);
        if (ContextStorage.NOT_FOUND == contextId) {
            throw ContextExceptionCodes.NO_MAPPING.create(contextInfo);
        }
        Context context = contextStor.getContext(contextId);
        if (null == context) {
            throw ContextExceptionCodes.NOT_FOUND.create(I(contextId));
        }
        return context;
    }

    /**
     * Looks up the user for the supplied user info in a context, throwing appropriate exceptions if not found.
     *
     * @param ctx The context
     * @param userInfo The user info (as usually supplied in the login name)
     * @return The user
     * @throws OXException If user look-up fails
     */
    private User findUser(Context ctx, String userInfo) throws OXException {
        final String proxyDelimiter = MailProperties.getInstance().getAuthProxyDelimiter();
        final UserStorage us = UserStorage.getInstance();
        int userId;
        if (null != proxyDelimiter && userInfo.contains(proxyDelimiter)) {
            userId = us.getUserId(userInfo.substring(userInfo.indexOf(proxyDelimiter) + proxyDelimiter.length(), userInfo.length()), ctx);
        } else {
            userId = us.getUserId(userInfo, ctx);
        }
        return us.getUser(userId, ctx);
    }

}
