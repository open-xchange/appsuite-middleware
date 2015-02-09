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
import static com.openexchange.tools.servlet.http.Tools.sendErrorPage;
import static com.openexchange.tools.servlet.http.Tools.sendErrorResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import com.google.common.net.HttpHeaders;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.service.Authentication;
import com.openexchange.authorization.AuthorizationService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.oauth.provider.Client;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderExceptionCodes;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.internal.OAuthProviderProperties;
import com.openexchange.oauth.provider.internal.URLHelper;
import com.openexchange.server.ServiceLookup;
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if (!Tools.considerSecure(request)) {
                response.setHeader(HttpHeaders.LOCATION, URLHelper.getSecureLocation(request));
                response.sendError(HttpServletResponse.SC_MOVED_PERMANENTLY);
                return;
            }

            // Check for CSRF
            // Respond with JSON errors w/o redirect
            if (isInvalidReferer(request)) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request contained no or invalid referer header");
                return;
            }

            // Set JSESSIONID cookie and generate CSRF token
            if (isInvalidCSRFToken(request)) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request contained no or invalid CSRF token. Ensure that cookies are allowed.");
                return;
            }

            // Check & validate client, redirect URI and state
            // Respond with error pages
            String clientId = request.getParameter(OAuthProviderConstants.PARAM_CLIENT_ID);
            if (Strings.isEmpty(clientId)) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_CLIENT_ID);
                return;
            }

            Client client = oAuthProvider.getClientById(clientId);
            if (client == null || !client.isEnabled()) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request contained an invalid value for parameter: " + OAuthProviderConstants.PARAM_CLIENT_ID);
                return;
            }

            String redirectURI = request.getParameter(OAuthProviderConstants.PARAM_REDIRECT_URI);
            if (Strings.isEmpty(redirectURI)) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_REDIRECT_URI);
                return;
            }

            if (!client.hasRedirectURI(redirectURI)) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request contained an invalid value for parameter: " + OAuthProviderConstants.PARAM_REDIRECT_URI);
                return;
            }

            String state = request.getParameter(OAuthProviderConstants.PARAM_STATE);
            if (Strings.isEmpty(state)) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_STATE);
                return;
            }

            // Client identifier, redirect URI and state are valid
            // Now respond with error redirects
            // If successful: YOUR_REDIRECT_URI/?code=AUTHORIZATION_CODE&state=STATE
            // If not: error page or YOUR_REDIRECT_URI/?error=<error-code>&error_description=<error-desc>&state=<state>

            // Check if user aborted process
            String accessDenied = request.getParameter(OAuthProviderConstants.PARAM_ACCESS_DENIED);
            if (Boolean.parseBoolean(accessDenied)) {
                // YOUR_REDIRECT_URI/?error=access_denied&error_description=the+user+denied+your+request&state=STATE
                response.sendRedirect(URLHelper.getErrorRedirectLocation(redirectURI, "access_denied", "the user denied your request", OAuthProviderConstants.PARAM_STATE, state));
                return;
            }

            // Check user credentials
            String login = request.getParameter(OAuthProviderConstants.PARAM_USER_LOGIN);
            if (Strings.isEmpty(login)) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_USER_LOGIN);
                return;
            }

            String password = request.getParameter(OAuthProviderConstants.PARAM_USER_PASSWORD);
            if (Strings.isEmpty(password)) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_USER_PASSWORD);
                return;
            }

            // Check response type
            String responseType = request.getParameter(OAuthProviderConstants.PARAM_RESPONSE_TYPE);
            if (Strings.isEmpty(responseType)) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_RESPONSE_TYPE);
                return;
            }
            if (!"code".equals(responseType)) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request contained an invalid value for parameter: " + OAuthProviderConstants.PARAM_RESPONSE_TYPE);
                return;
            }

            // Check scope
            String scope = request.getParameter(OAuthProviderConstants.PARAM_SCOPE);
            if (Strings.isEmpty(scope)) {
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "Request was missing a required parameter: " + OAuthProviderConstants.PARAM_SCOPE);
                return;
            }

            // Validate scope
            if (!oAuthProvider.isValidScopeString(scope)) {
                response.sendRedirect(URLHelper.getErrorRedirectLocation(redirectURI, "invalid_scope", "invalid parameter value:" + OAuthProviderConstants.PARAM_SCOPE, OAuthProviderConstants.PARAM_STATE, state));
                return;
            }

            try {
                // Authenticate
                Authenticated authed = null;
                try {
                    authed = Authentication.login(login, password, new HashMap<String, Object>(0));
                } catch (OXException e) {
                    if (!INVALID_CREDENTIALS.equals(e)) {
                        throw e;
                    }
                }

                if (null == authed) {
                    // TODO: redirect to login form again and set error message?
                    response.sendRedirect(URLHelper.getErrorRedirectLocation(redirectURI, "invalid_grant", "invalid resource owner credentials", OAuthProviderConstants.PARAM_STATE, state));
                    return;
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
                    sendErrorPage(response, HttpServletResponse.SC_FORBIDDEN, "You are not allowed to grant access via OAuth to 3rd party applications.");
                }

                // Everything OK, do the redirect with authorization code & state
                // YOUR_REDIRECT_URI/?code=AUTHORIZATION_CODE&state=STATE
                String code = oAuthProvider.generateAuthorizationCodeFor(clientId, redirectURI, scope, user.getId(), ctx.getContextId());
                response.sendRedirect(URLHelper.getRedirectLocation(redirectURI, OAuthProviderConstants.PARAM_CODE, code, OAuthProviderConstants.PARAM_STATE, state));
            } catch (OXException e) {
                if (OAuthProviderExceptionCodes.GRANTS_EXCEEDED.equals(e)) {
                    // TODO: nicer error page and maybe localization based on optional uri params
                    sendErrorPage(response, HttpServletResponse.SC_FORBIDDEN, e.getDisplayMessage(Locale.US));
                } else {
                    // Special handling for OXException after client identifier and redirect URI have been validated
                    LOG.error("Authorization request failed", e);
                    response.sendRedirect(URLHelper.getErrorRedirectLocation(redirectURI, "server_error", "internal error", OAuthProviderConstants.PARAM_STATE, state));
                    return;
                }
            }

        } catch (OXException e) {
            LOG.error("Authorization request failed", e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error_description\":\"internal error\",\"error\":\"server_error\"}");
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            if (!Tools.considerSecure(request)) {
                response.setHeader(HttpHeaders.LOCATION, URLHelper.getSecureLocation(request));
                response.sendError(HttpServletResponse.SC_MOVED_PERMANENTLY);
                return;
            }

            String responseType = request.getParameter(OAuthProviderConstants.PARAM_RESPONSE_TYPE);
            if (Strings.isEmpty(responseType)) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "missing required parameter: "+OAuthProviderConstants.PARAM_RESPONSE_TYPE);
                return;
            }
            if (!"code".equals(responseType)) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "response type not supported: " + responseType);
                return;
            }

            String clientId = request.getParameter(OAuthProviderConstants.PARAM_CLIENT_ID);
            if (Strings.isEmpty(clientId)) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "missing required parameter: "+OAuthProviderConstants.PARAM_CLIENT_ID);
                return;
            }

            Client client = oAuthProvider.getClientById(clientId);
            if (client == null) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "invalid parameter value: "+OAuthProviderConstants.PARAM_CLIENT_ID);
                return;
            }

            if (!client.isEnabled()) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "invalid parameter value: "+OAuthProviderConstants.PARAM_CLIENT_ID);
                return;
            }

            String state = request.getParameter(OAuthProviderConstants.PARAM_STATE);
            if (Strings.isEmpty(state)) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "missing required parameter: "+OAuthProviderConstants.PARAM_STATE);
                return;
            }

            String redirectURI = request.getParameter(OAuthProviderConstants.PARAM_REDIRECT_URI);
            if (Strings.isEmpty(redirectURI)) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "missing required parameter: "+OAuthProviderConstants.PARAM_REDIRECT_URI);
                return;
            }

            String scope = request.getParameter(OAuthProviderConstants.PARAM_SCOPE);
            if (Strings.isEmpty(scope)) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "missing required parameter: "+OAuthProviderConstants.PARAM_SCOPE);
                return;
            }

            if (!oAuthProvider.isValidScopeString(scope)) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "invalid parameter value: "+OAuthProviderConstants.PARAM_SCOPE);
                return;
            }

            if (!client.hasRedirectURI(redirectURI)) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "invalid parameter value: "+OAuthProviderConstants.PARAM_REDIRECT_URI);
                return;
            }

            // Set JSESSIONID cookie and generate CSRF token
            HttpSession session = request.getSession(true);
            String csrfToken = UUIDs.getUnformattedStringFromRandom();
            session.setAttribute(ATTR_OAUTH_CSRF_TOKEN, csrfToken);

            // Redirect to login page

            // Hidden: Client identifier, Redirect URI, Scope, State, Response Type, CSRF Token
            // Visible: Login, Password
            StringBuilder sb = new StringBuilder(1024);
            String lineSep = System.getProperty("line.separator");
            sb.append("<!DOCTYPE html>").append(lineSep);
            sb.append("<html><head>").append(lineSep);
            {
                sb.append("<title>Login</title>").append(lineSep);
            }
            sb.append("</head><body>").append(lineSep);
            sb.append("<form action=\"").append(getAuthorizationEndpointURL(request)).append("\" method=\"POST\" enctype=\"application/x-www-form-urlencoded\">").append(lineSep);
            sb.append("<p><label for=\"").append(OAuthProviderConstants.PARAM_USER_LOGIN).append("\">Username: </label><input name=\"").append(OAuthProviderConstants.PARAM_USER_LOGIN).append("\" type=\"text\"><br>").append(lineSep);
            sb.append("<p><label for=\"").append(OAuthProviderConstants.PARAM_USER_PASSWORD).append("\">Password:  </label><input name=\"").append(OAuthProviderConstants.PARAM_USER_PASSWORD).append("\" type=\"password\">").append(lineSep);
            sb.append("<input name=\"").append(OAuthProviderConstants.PARAM_CLIENT_ID).append("\" type=\"hidden\" value=\"").append(clientId).append("\">").append(lineSep);
            sb.append("<input name=\"").append(OAuthProviderConstants.PARAM_REDIRECT_URI).append("\" type=\"hidden\" value=\"").append(redirectURI).append("\">").append(lineSep);
            sb.append("<input name=\"").append(OAuthProviderConstants.PARAM_SCOPE).append("\" type=\"hidden\" value=\"").append(scope).append("\">").append(lineSep);
            sb.append("<input name=\"").append(OAuthProviderConstants.PARAM_STATE).append("\" type=\"hidden\" value=\"").append(state).append("\">").append(lineSep);
            sb.append("<input name=\"").append(OAuthProviderConstants.PARAM_CSRF_TOKEN).append("\" type=\"hidden\" value=\"").append(csrfToken).append("\">").append(lineSep);
            sb.append("<input name=\"").append(OAuthProviderConstants.PARAM_RESPONSE_TYPE).append("\" type=\"hidden\" value=\"").append(responseType).append("\">").append(lineSep);
            sb.append("<p><button name=\"").append(OAuthProviderConstants.PARAM_ACCESS_DENIED).append("\" type=\"submit\" value=\"false\">Allow</button>").append(lineSep);
            sb.append("<button name=\"").append( OAuthProviderConstants.PARAM_ACCESS_DENIED).append("\" type=\"submit\" value=\"true\">Deny</button>").append(lineSep);
            sb.append("</form>").append(lineSep);
            sb.append("</body></html>").append(lineSep);


            response.setContentType("text/html; charset=UTF-8");
            response.setHeader("Content-Disposition", "inline");
            response.setStatus(200);
            PrintWriter writer = response.getWriter();
            writer.write(sb.toString());
            writer.flush();
        } catch (OXException e) {
            LOG.error("Login request failed", e);
            sendErrorPage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal error");
        }
    }

    private final String getAuthorizationEndpointURL(HttpServletRequest request) throws OXException {
        String hostName = determineHostName(request, -1, -1);
        DispatcherPrefixService dispatcherPrefixService = com.openexchange.osgi.Tools.requireService(DispatcherPrefixService.class, services);
        String servletPrefix = dispatcherPrefixService.getPrefix();
        return (Tools.considerSecure(request) ? "https://" : "http://") + hostName + servletPrefix + OAuthProviderConstants.AUTHORIZATION_SERVLET_ALIAS;
    }

    private static boolean isInvalidCSRFToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }

        String csrfToken = (String) session.getAttribute(ATTR_OAUTH_CSRF_TOKEN);
        session.removeAttribute(ATTR_OAUTH_CSRF_TOKEN); // not necessary anymore
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

    private static boolean isInvalidReferer(HttpServletRequest request) throws OXException {
        String referer = request.getHeader(HttpHeaders.REFERER);
        if (Strings.isEmpty(referer)) {
            return true;
        }

        try {
            URI expectedReferer = new URI(URLHelper.getSecureLocation(request));
            URI actualReferer = new URI(referer);
            if (!expectedReferer.getScheme().equals(actualReferer.getScheme())) {
                return true;
            }

            if (!expectedReferer.getHost().equals(actualReferer.getHost())) {
                return true;
            }

            if (expectedReferer.getPort() != actualReferer.getPort()) {
                return true;
            }

            if (!expectedReferer.getPath().equals(actualReferer.getPath())) {
                return true;
            }
        } catch (URISyntaxException e) {
            return true;
        }

        return false;
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

    private String determineHostName(HttpServletRequest req, int userId, int contextId) {
        HostnameService hostnameService = services.getService(HostnameService.class);
        if (null == hostnameService) {
            return req.getServerName();
        }
        String hn = hostnameService.getHostname(userId, contextId);
        return null == hn ? req.getServerName() : hn;
    }

}
