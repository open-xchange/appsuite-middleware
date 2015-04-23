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

import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.osgi.Tools.requireService;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import com.google.common.net.HttpHeaders;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.service.Authentication;
import com.openexchange.authorization.AuthorizationService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.oauth.provider.DefaultScopes;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.OAuthProviderExceptionMessages;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.OAuthScopeProvider;
import com.openexchange.oauth.provider.Scopes;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.client.ClientManagementException;
import com.openexchange.oauth.provider.internal.OAuthProviderProperties;
import com.openexchange.oauth.provider.notification.OAuthMailNotificationService;
import com.openexchange.oauth.provider.utils.OAuthRedirectUtils;
import com.openexchange.server.ServiceLookup;
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

    private static final String ATTR_OAUTH_CSRF_TOKEN = "oauth-csrf-token";

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Tools.disableCaching(response);

            checkSecure(request, response);
            Locale locale = getLocale(request);

            // Check for CSRF
            if (!isValidReferer(request)) {
                OAuthRedirectUtils.setParameterErrorRedirectUrl(request, response, locale, "invalid_request", OAuthProviderExceptionMessages.INVALID_REFERER_HEADER_MSG, HttpHeaders.REFERER);
                return;
            }

            if (!isValidCSRFToken(request)) {
                OAuthRedirectUtils.setParameterErrorRedirectUrl(request, response, locale, "invalid_request", OAuthProviderExceptionMessages.INVALID_CSRF_TOKEN_MSG, OAuthProviderConstants.PARAM_CSRF_TOKEN);
                return;
            }

            String clientId = getClientId(request, response, locale);
            Client client = getClient(request, response, locale, clientId);
            String redirectURI = getRedirectURI(request, response, locale);
            checkClientHasRedirectURI(request, response, locale, client, redirectURI);
            String state = getState(request, response, locale);

            // Client identifier, redirect URI and state are valid
            // Now respond with error redirects
            // If successful: YOUR_REDIRECT_URI/?code=AUTHORIZATION_CODE&state=STATE
            // If not: error page or YOUR_REDIRECT_URI/?error=<error-code>&error_description=<error-desc>&state=<state>

            checkAccessDenied(request, response, locale);

            // Check user credentials
            String login = request.getParameter(OAuthProviderConstants.PARAM_USER_LOGIN);
            if (Strings.isEmpty(login)) {
                OAuthRedirectUtils.setParameterErrorRedirectUrl(request, response, locale, "invalid_request", OAuthProviderExceptionMessages.PARAMETER_MISSING_MSG, OAuthProviderConstants.PARAM_USER_LOGIN);
                return;
            }

            String password = request.getParameter(OAuthProviderConstants.PARAM_USER_PASSWORD);
            if (Strings.isEmpty(password)) {
                OAuthRedirectUtils.setParameterErrorRedirectUrl(request, response, locale, "invalid_request", OAuthProviderExceptionMessages.PARAMETER_MISSING_MSG, OAuthProviderConstants.PARAM_USER_PASSWORD);
                return;
            }

            checkResponseType(request, response, locale);
            String scope = getScope(request, response, locale, client);

            try {
                // Authenticate
                Authenticated authed = null;
                try {
                    authed = Authentication.login(login, password, new HashMap<String, Object>(0));
                } catch (OXException e) {
                    if (INVALID_CREDENTIALS.equals(e)) {
                        OAuthRedirectUtils.setInvalidCredentialsErrorRedirectUrl(request, response, locale, e);
                        return;
                    }
                    throw e;
                }

                // Perform user / context lookup
                Context ctx = findContext(authed.getContextInfo());
                User user = findUser(ctx, authed.getUserInfo());

                // Authorize
                AuthorizationService authService = requireService(AuthorizationService.class, services);
                authService.authorizeUser(ctx, user);

                // Checks if something is deactivated.
                ConfigView configView = requireService(ConfigViewFactory.class, services).getView(user.getId(), ctx.getContextId());
                if (!configView.opt(OAuthProviderProperties.ENABLED, Boolean.class, Boolean.TRUE).booleanValue() || user.isGuest()) {
                    OAuthRedirectUtils.setPermissionErrorRedirectUrl(request, response, locale, "not_allowed", OAuthProviderExceptionMessages.NOT_ALLOWED_MSG);
                }

                // Everything OK, send notification mail and do the redirect with authorization code & state
                // YOUR_REDIRECT_URI/?code=AUTHORIZATION_CODE&state=STATE
                String code = oAuthProvider.generateAuthorizationCodeFor(clientId, redirectURI, scope, user.getId(), ctx.getContextId());
                try {
                    OAuthMailNotificationService notificationService = new OAuthMailNotificationService(oAuthProvider);
                    notificationService.sendNotification(user.getId(), ctx.getContextId(), clientId, request);
                } catch (OXException e) {
                    LOG.error("Send oauth notification mail for {} to {} failed.", client.getName(), user.getMail(), e);
                }

                if (services.getOptionalService(ConfigurationService.class).getBoolProperty("com.openexchange.oauth.provider.loginPage.enabled", false)) {
                    response.sendRedirect(OAuthRedirectUtils.getRedirectLocationWithParameter(redirectURI, OAuthProviderConstants.PARAM_CODE, code, OAuthProviderConstants.PARAM_STATE, state));
                    return;
                }
                OAuthRedirectUtils.setSuccessfullyAuthenticatedRedirect(response, OAuthRedirectUtils.getRedirectLocationWithParameter(redirectURI, OAuthProviderConstants.PARAM_CODE, code, OAuthProviderConstants.PARAM_STATE, state));
            } catch (OXException e) {
                if (OAuthProviderExceptionCodes.GRANTS_EXCEEDED.equals(e)) {
                    OAuthRedirectUtils.setPermissionErrorRedirectUrl(request, response, locale, "not_allowed", OAuthProviderExceptionMessages.NOT_ALLOWED_MSG);
                } else {
                    // Special handling for OXException after client identifier and redirect URI have been validated
                    LOG.error("Authorization request failed", e);
                    OAuthRedirectUtils.setUnkownErrorRedirectUrl(request, response, OXExceptionStrings.MESSAGE, e.getMessage());
                    return;
                }
            }
        } catch (OXException oxException) {
            if (isKnownError(oxException)) {
                return;
            }
            LOG.error("Login request failed", oxException);
            response.reset();
            OAuthRedirectUtils.setUnkownErrorRedirectUrl(request, response, OXExceptionStrings.MESSAGE, oxException.getMessage());
        } catch (ClientManagementException e) {
            LOG.error("Login request failed", e);
            response.reset();
            OAuthRedirectUtils.setUnkownErrorRedirectUrl(request, response, OXExceptionStrings.MESSAGE, e.getMessage());
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Tools.disableCaching(response);

            checkSecure(request, response);
            Locale locale = getLocale(request);
            checkResponseType(request, response, locale);

            String clientId = getClientId(request, response, locale);
            Client client = getClient(request, response, locale, clientId);
            String redirectURI = getRedirectURI(request, response, locale);
            checkClientHasRedirectURI(request, response, locale, client, redirectURI);
            String state = getState(request, response, locale);
            String scope = getScope(request, response, locale, client);

            // Set JSESSIONID cookie and generate CSRF token
            HttpSession session = request.getSession(true);
            String csrfToken = UUIDs.getUnformattedStringFromRandom();
            session.setAttribute(ATTR_OAUTH_CSRF_TOKEN, csrfToken);

            if (services.getOptionalService(ConfigurationService.class).getBoolProperty("com.openexchange.oauth.provider.loginPage.enabled", false)) {
                handleWithLoginPage(request, response, client, redirectURI, state, scope, csrfToken);
                return;
            }
            generateAndSetLoginResponse(response, request, redirectURI, state, csrfToken, client, DefaultScopes.parseScope(scope), locale);
        } catch (OXException oxException) {
            if (isKnownError(oxException)) {
                return;
            }
            LOG.error("Login request failed", oxException);
            response.reset();
            OAuthRedirectUtils.setUnkownErrorRedirectUrl(request, response, OXExceptionStrings.MESSAGE, oxException.getMessage());
        } catch (ClientManagementException | URISyntaxException e) {
            LOG.error("Login request failed", e);
            response.reset();
            OAuthRedirectUtils.setUnkownErrorRedirectUrl(request, response, OXExceptionStrings.MESSAGE, e.getMessage());
        }
    }

    private boolean isKnownError(OXException e) {
        if (OAuthProviderExceptionCodes.PARAMETER_MISSING.equals(e)
            || OAuthProviderExceptionCodes.PARAMETER_INVALID.equals(e)
            || OAuthProviderExceptionCodes.NO_SECURE_CONNECTION.equals(e)
            || OAuthProviderExceptionCodes.WRONG_RESPONSE_TYPE.equals(e)
            || OAuthProviderExceptionCodes.REQUEST_DENIED.equals(e)
            || OAuthProviderExceptionCodes.NO_SECURE_CONNECTION.equals(e)) {
            return true;
        }
        return false;
    }

    /**
     * @param request
     * @param response
     * @param client
     * @param redirectURI
     * @param state
     * @param scope
     * @param csrfToken
     * @throws OXException
     * @throws IOException
     */
    private void handleWithLoginPage(HttpServletRequest request, HttpServletResponse response, Client client, String redirectURI, String state, String scope, String csrfToken) throws OXException, IOException {
        // Redirect to login page
        String loginPage = compileLoginPage(request, redirectURI, state, csrfToken, client, DefaultScopes.parseScope(scope), LocaleTools.DEFAULT_LOCALE);
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Content-Disposition", "inline");
        response.setStatus(200);
        PrintWriter writer = response.getWriter();
        writer.write(loginPage);
        writer.flush();
    }

    private void generateAndSetLoginResponse(HttpServletResponse response, HttpServletRequest request, String redirectURI, String state, String csrfToken, Client client, Scopes scopes, Locale locale) throws OXException, URISyntaxException, IOException {
        TranslatorFactory translatorFactory = requireService(TranslatorFactory.class, services);
        Translator translator = translatorFactory.translatorFor(locale);
        HtmlService htmlService = requireService(HtmlService.class, services);

        Map<String, String> vars = new HashMap<>();
        vars.put(OAuthProviderConstants.PARAM_LANGUAGE, locale.getLanguage());
        String title = translator.translate(OAuthProviderStrings.LOGIN);
        vars.put("title", title);
        vars.put("iconURL", OAuthRedirectUtils.getBaseLocation(request) + OAuthProviderConstants.CLIENT_ICON_SERVLET_ALIAS + '/' + client.getId());
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
        vars.put("scopeDescriptions", descriptions.toString());
        vars.put("target", getAuthorizationEndpointURL(request));
        vars.put("formHeading", title);
        vars.put("usernameLabel", translator.translate(OAuthProviderStrings.USERNAME));
        vars.put("passwordLabel", translator.translate(OAuthProviderStrings.PASSWORD));
        vars.put("allowLabel", translator.translate(OAuthProviderStrings.ALLOW));
        vars.put("denyLabel", translator.translate(OAuthProviderStrings.DENY));
        vars.put(OAuthProviderConstants.PARAM_CLIENT_ID, client.getId());
        vars.put(OAuthProviderConstants.PARAM_REDIRECT_URI, redirectURI);
        vars.put(OAuthProviderConstants.PARAM_SCOPE, scopes.scopeString());
        vars.put(OAuthProviderConstants.PARAM_STATE, state);
        vars.put(OAuthProviderConstants.PARAM_CSRF_TOKEN, csrfToken);
        vars.put(OAuthProviderConstants.PARAM_RESPONSE_TYPE, "code");

        StringBuilder fragmentBuilder = new StringBuilder();
        for (Entry<String, String> s : vars.entrySet()) {
            fragmentBuilder.append("&" + s.getKey() + "=" + URLEncoder.encode(s.getValue(), "UTF-8"));
        }
        String fragment = fragmentBuilder.toString();
        if (fragment.startsWith("&")) {
            fragment = fragment.substring(1);
        }

        URIBuilder builder = new URIBuilder(OAuthRedirectUtils.getLoginPageUrl(request)).setFragment(fragment);

        response.setStatus(HttpServletResponse.SC_FOUND);
        response.sendRedirect(builder.build().toString());
    }

    /**
     * @param request
     * @param response
     * @param locale
     * @throws OXException
     * @throws IOException
     */
    private boolean checkAccessDenied(HttpServletRequest request, HttpServletResponse response, Locale locale) throws OXException, IOException {
        // Check if user aborted process
        String accessDenied = request.getParameter(OAuthProviderConstants.PARAM_ACCESS_DENIED);
        if (Boolean.parseBoolean(accessDenied)) {
            // YOUR_REDIRECT_URI/?error=access_denied&error_description=the+user+denied+your+request&state=STATE
            OAuthRedirectUtils.setParameterErrorRedirectUrl(request, response, locale, "access_denied", OAuthProviderExceptionMessages.REQUEST_DENIED_MSG, OAuthProviderConstants.PARAM_ACCESS_DENIED);
            throw OAuthProviderExceptionCodes.REQUEST_DENIED.create();
        }
        return false;
    }

    /**
     * @param response
     * @param locale
     * @param client
     * @param redirectURI
     * @return
     * @throws OXException
     * @throws IOException
     */
    private boolean checkClientHasRedirectURI(HttpServletRequest request, HttpServletResponse response, Locale locale, Client client, String redirectURI) throws OXException, IOException {
        if (!client.hasRedirectURI(redirectURI)) {
            OAuthRedirectUtils.setParameterErrorRedirectUrl(request, response, locale, "invalid_request", OAuthProviderExceptionMessages.PARAMETER_INVALID_MSG, OAuthProviderConstants.PARAM_REDIRECT_URI);
            throw OAuthProviderExceptionCodes.PARAMETER_INVALID.create(OAuthProviderConstants.PARAM_REDIRECT_URI);
        }
        return true;
    }

    /**
     * @param request
     * @return
     */
    private Locale getLocale(HttpServletRequest request) {
        String language = request.getParameter(OAuthProviderConstants.PARAM_LANGUAGE);
        if (Strings.isEmpty(language)) {
            language = Locale.US.getDisplayLanguage();
        }
        return new Locale(language);
    }

    /**
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws OXException
     */
    private boolean checkSecure(HttpServletRequest request, HttpServletResponse response) throws IOException, OXException {
        if (!Tools.considerSecure(request)) {
            response.setHeader(HttpHeaders.LOCATION, OAuthRedirectUtils.getSecureLocation(request));
            response.sendError(HttpServletResponse.SC_MOVED_PERMANENTLY);
            throw OAuthProviderExceptionCodes.NO_SECURE_CONNECTION.create();
        }
        return true;
    }

    /**
     * @param request
     * @param response
     * @param locale
     * @return
     * @throws OXException
     * @throws IOException
     */
    private boolean checkResponseType(HttpServletRequest request, HttpServletResponse response, Locale locale) throws OXException, IOException {
        String responseType = request.getParameter(OAuthProviderConstants.PARAM_RESPONSE_TYPE);
        if (Strings.isEmpty(responseType)) {
            OAuthRedirectUtils.setParameterErrorRedirectUrl(request, response, locale, "invalid_request", OAuthProviderExceptionMessages.PARAMETER_MISSING_MSG, OAuthProviderConstants.PARAM_RESPONSE_TYPE);
            throw OAuthProviderExceptionCodes.PARAMETER_MISSING.create(OAuthProviderConstants.PARAM_RESPONSE_TYPE);
        }
        if (!"code".equals(responseType)) {
            OAuthRedirectUtils.setParameterErrorRedirectUrl(request, response, locale, "invalid_request", OAuthProviderExceptionMessages.WRONG_RESPONSE_TYPE_MSG, responseType);
            throw OAuthProviderExceptionCodes.WRONG_RESPONSE_TYPE.create();
        }
        return true;
    }

    /**
     * @param request
     * @param response
     * @param locale
     * @param client
     * @return
     * @throws OXException
     * @throws IOException
     */
    private String getScope(HttpServletRequest request, HttpServletResponse response, Locale locale, Client client) throws OXException, IOException {
        String scope = request.getParameter(OAuthProviderConstants.PARAM_SCOPE);
        if (Strings.isEmpty(scope)) {
            scope = client.getDefaultScope().scopeString();
        } else {
            // Validate scope
            if (!oAuthProvider.isValidScopeString(scope)) {
                OAuthRedirectUtils.setParameterErrorRedirectUrl(request, response, locale, "invalid_scope", OAuthProviderExceptionMessages.PARAMETER_INVALID_MSG, OAuthProviderConstants.PARAM_SCOPE, OAuthProviderConstants.PARAM_STATE, request.getParameter(OAuthProviderConstants.PARAM_STATE));
                throw OAuthProviderExceptionCodes.PARAMETER_INVALID.create(OAuthProviderConstants.PARAM_SCOPE);
            }
        }
        return scope;
    }

    /**
     * @param request
     * @param response
     * @param locale
     * @return
     * @throws OXException
     * @throws IOException
     */
    private String getState(HttpServletRequest request, HttpServletResponse response, Locale locale) throws OXException, IOException {
        String state = request.getParameter(OAuthProviderConstants.PARAM_STATE);
        if (Strings.isEmpty(state)) {
            OAuthRedirectUtils.setParameterErrorRedirectUrl(request, response, locale, "invalid_request", OAuthProviderExceptionMessages.PARAMETER_MISSING_MSG, OAuthProviderConstants.PARAM_STATE, OAuthProviderConstants.PARAM_STATE, state);
            throw OAuthProviderExceptionCodes.PARAMETER_MISSING.create(OAuthProviderConstants.PARAM_STATE);
        }
        return state;
    }

    /**
     * @param request
     * @param response
     * @param locale
     * @return
     * @throws OXException
     * @throws IOException
     */
    private String getRedirectURI(HttpServletRequest request, HttpServletResponse response, Locale locale) throws OXException, IOException {
        String redirectURI = request.getParameter(OAuthProviderConstants.PARAM_REDIRECT_URI);
        if (Strings.isEmpty(redirectURI)) {
            OAuthRedirectUtils.setParameterErrorRedirectUrl(request, response, locale, "invalid_request", OAuthProviderExceptionMessages.PARAMETER_MISSING_MSG, OAuthProviderConstants.PARAM_REDIRECT_URI);
            throw OAuthProviderExceptionCodes.PARAMETER_MISSING.create(OAuthProviderConstants.PARAM_REDIRECT_URI);
        }
        return redirectURI;
    }

    /**
     * @param response
     * @param locale
     * @param clientId
     * @return
     * @throws OXException
     * @throws IOException
     * @throws ClientManagementException
     */
    private Client getClient(HttpServletRequest request, HttpServletResponse response, Locale locale, String clientId) throws OXException, IOException, ClientManagementException {
        Client client = oAuthProvider.getClientManagement().getClientById(clientId);
        if ((client == null) || (!client.isEnabled())) {
            OAuthRedirectUtils.setParameterErrorRedirectUrl(request, response, locale, "invalid_request", OAuthProviderExceptionMessages.PARAMETER_INVALID_MSG, OAuthProviderConstants.PARAM_CLIENT_ID);
            throw OAuthProviderExceptionCodes.PARAMETER_INVALID.create(OAuthProviderConstants.PARAM_CLIENT_ID);
        }
        return client;
    }

    /**
     * @param request
     * @param response
     * @param locale
     * @return
     * @throws OXException
     * @throws IOException
     */
    private String getClientId(HttpServletRequest request, HttpServletResponse response, Locale locale) throws OXException, IOException {
        String clientId = request.getParameter(OAuthProviderConstants.PARAM_CLIENT_ID);
        if (Strings.isEmpty(clientId)) {
            OAuthRedirectUtils.setParameterErrorRedirectUrl(request, response, locale, "invalid_request", OAuthProviderExceptionMessages.PARAMETER_MISSING_MSG, OAuthProviderConstants.PARAM_CLIENT_ID);
            throw OAuthProviderExceptionCodes.PARAMETER_MISSING.create(OAuthProviderConstants.PARAM_CLIENT_ID);
        }
        return clientId;
    }

    private boolean isValidCSRFToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        String csrfToken = (String) session.getAttribute(ATTR_OAUTH_CSRF_TOKEN);
        session.removeAttribute(ATTR_OAUTH_CSRF_TOKEN); // not necessary anymore
        if (csrfToken == null) {
            return false;
        }

        String actualToken = request.getParameter(OAuthProviderConstants.PARAM_CSRF_TOKEN);
        if (actualToken == null) {
            return false;
        }

        if (!csrfToken.equals(actualToken)) {
            return false;
        }

        return true;
    }

    private boolean isValidReferer(HttpServletRequest request) {
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (Strings.isEmpty(referer)) {
            return false;
        }

        try {
            URI expectedReferer = new URI(OAuthRedirectUtils.getSecureLocation(request));
            URI actualReferer = new URI(referer);
            if (!expectedReferer.getScheme().equals(actualReferer.getScheme())) {
                return false;
            }

            if (!expectedReferer.getHost().equals(actualReferer.getHost())) {
                return false;
            }

            if (expectedReferer.getPort() != actualReferer.getPort()) {
                return false;
            }

            if (!expectedReferer.getPath().equals(actualReferer.getPath())) {
                return false;
            }
        } catch (URISyntaxException e) {
            return false;
        }

        return true;
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

    private final String getAuthorizationEndpointURL(HttpServletRequest request) throws OXException {
        String hostName = OAuthRedirectUtils.getHostname(request);
        DispatcherPrefixService dispatcherPrefixService = requireService(DispatcherPrefixService.class, services);
        String servletPrefix = dispatcherPrefixService.getPrefix();
        return (Tools.considerSecure(request) ? "https://" : "http://") + hostName + servletPrefix + OAuthProviderConstants.AUTHORIZATION_SERVLET_ALIAS;
    }

    private String compileLoginPage(HttpServletRequest request, String redirectURI, String state, String csrfToken, Client client, Scopes scopes, Locale locale) throws OXException {
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
        vars.put("iconURL", OAuthRedirectUtils.getBaseLocation(request) + OAuthProviderConstants.CLIENT_ICON_SERVLET_ALIAS + '/' + client.getId());
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

}
