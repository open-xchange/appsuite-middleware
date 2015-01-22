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

import static com.openexchange.ajax.AJAXUtility.encodeUrl;
import static com.openexchange.authentication.LoginExceptionCodes.INVALID_CREDENTIALS;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.servlet.http.Tools.sendErrorPage;
import static com.openexchange.tools.servlet.http.Tools.sendErrorResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.service.Authentication;
import com.openexchange.authorization.Authorization;
import com.openexchange.authorization.AuthorizationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.Scope;
import com.openexchange.oauth.provider.osgi.Services;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link AuthorizationEndpoint} - Authorization request handler for OAuth2.0.
 * <p>
 * <img src="./webflow.png" alt="OAuth Web Flow">
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AuthorizationEndpoint extends HttpServlet {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AuthorizationEndpoint.class);

    private static final long serialVersionUID = 6393806486708501254L;

    private final OAuthProviderService oAuthProvider;

    /**
     * Initializes a new {@link AuthorizationEndpoint}.
     */
    public AuthorizationEndpoint(OAuthProviderService oAuthProvider) {
        super();
        this.oAuthProvider = oAuthProvider;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // First, check & validate client identifier, redirect URI and state
            // Respond with JSON errors w/o redirect
            String clientId = request.getParameter(OAuthProviderConstants.PARAM_CLIENT_ID);
            if (Strings.isEmpty(clientId)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error_description\":\"missing required parameter: "+OAuthProviderConstants.PARAM_CLIENT_ID+"\",\"error\":\"invalid_request\"}");
                return;
            }

            String redirectUri = request.getParameter(OAuthProviderConstants.PARAM_REDIRECT_URI);
            if (Strings.isEmpty(redirectUri)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error_description\":\"missing required parameter: "+OAuthProviderConstants.PARAM_REDIRECT_URI+"\",\"error\":\"invalid_request\"}");
                return;
            }

            if (false == oAuthProvider.validateClientId(clientId)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, new JSONObject(2).put("error_description", "invalid parameter value: " + OAuthProviderConstants.PARAM_CLIENT_ID).put("error", "invalid_request").toString());
                return;
            }

            if (false == oAuthProvider.validateRedirectUri(clientId, redirectUri)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, new JSONObject(2).put("error_description", "invalid parameter value: " + OAuthProviderConstants.PARAM_REDIRECT_URI).put("error", "invalid_request").toString());
                return;
            }

            String state = request.getParameter(OAuthProviderConstants.PARAM_STATE);
            if (Strings.isEmpty(state)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error_description\":\"missing required parameter: "+OAuthProviderConstants.PARAM_STATE+"\",\"error\":\"invalid_request\"}");
                return;
            }

            // Client identifier, redirect URI and state are valid

            // Now respond with error redirects
            // If successful: YOUR_REDIRECT_URI/?code=AUTHORIZATION_CODE&state=STATE
            // If not: YOUR_REDIRECT_URI/?error=<error-code>&error_description=<error-desc>&state=<state>

            // Start composing redirect
            StringBuilder builder = new StringBuilder(encodeUrl(redirectUri, true, false));
            char concat = '?';
            if (redirectUri.indexOf('?') >= 0) {
                concat = '&';
            }

            // Check if user aborted process
            {
                String accessDenied = request.getParameter(OAuthProviderConstants.PARAM_ACCESS_DENIED);
                if (Boolean.parseBoolean(accessDenied)) {
                    // YOUR_REDIRECT_URI/?error=access_denied&error_description=the+user+denied+your+request&state=STATE
                    builder.append(concat);
                    concat = '&';
                    builder.append(OAuthProviderConstants.PARAM_ERROR).append('=').append("access_denied");

                    builder.append(concat);
                    builder.append(OAuthProviderConstants.PARAM_ERROR_DESCRIPTION).append('=').append(encodeUrl("the user denied your request", true, true));

                    builder.append(concat);
                    builder.append(OAuthProviderConstants.PARAM_STATE).append('=').append(encodeUrl(state, true, true));

                    response.sendRedirect(builder.toString());
                    return;
                }
            }

            // Check user credentials
            String login = request.getParameter(OAuthProviderConstants.PARAM_USER_LOGIN);
            if (Strings.isEmpty(login)) {
                builder.append(concat);
                concat = '&';
                builder.append(OAuthProviderConstants.PARAM_ERROR).append('=').append("invalid_request");

                builder.append(concat);
                builder.append(OAuthProviderConstants.PARAM_ERROR_DESCRIPTION).append('=').append(encodeUrl("missing required parameter: "+OAuthProviderConstants.PARAM_USER_LOGIN, true, true));

                builder.append(concat);
                builder.append(OAuthProviderConstants.PARAM_STATE).append('=').append(encodeUrl(state, true, true));

                response.sendRedirect(builder.toString());
                return;
            }

            String password = request.getParameter(OAuthProviderConstants.PARAM_USER_PASSWORD);
            if (Strings.isEmpty(password)) {
                builder.append(concat);
                concat = '&';
                builder.append(OAuthProviderConstants.PARAM_ERROR).append('=').append("invalid_request");

                builder.append(concat);
                builder.append(OAuthProviderConstants.PARAM_ERROR_DESCRIPTION).append('=').append(encodeUrl("missing required parameter: "+OAuthProviderConstants.PARAM_USER_PASSWORD, true, true));

                builder.append(concat);
                builder.append(OAuthProviderConstants.PARAM_STATE).append('=').append(encodeUrl(state, true, true));

                response.sendRedirect(builder.toString());
                return;
            }

            // Check response type
            {
                String responseType = request.getParameter(OAuthProviderConstants.PARAM_RESPONSE_TYPE);
                if (Strings.isEmpty(responseType)) {
                    builder.append(concat);
                    concat = '&';
                    builder.append(OAuthProviderConstants.PARAM_ERROR).append('=').append("invalid_request");

                    builder.append(concat);
                    builder.append(OAuthProviderConstants.PARAM_ERROR_DESCRIPTION).append('=').append(encodeUrl("missing required parameter: "+OAuthProviderConstants.PARAM_RESPONSE_TYPE, true, true));

                    builder.append(concat);
                    builder.append(OAuthProviderConstants.PARAM_STATE).append('=').append(encodeUrl(state, true, true));

                    response.sendRedirect(builder.toString());
                    return;
                }
                if (!"code".equals(responseType)) {
                    builder.append(concat);
                    concat = '&';
                    builder.append(OAuthProviderConstants.PARAM_ERROR).append('=').append("unsupported_response_type");

                    builder.append(concat);
                    builder.append(OAuthProviderConstants.PARAM_ERROR_DESCRIPTION).append('=').append(encodeUrl("response type not supported: "+responseType, true, true));

                    builder.append(concat);
                    builder.append(OAuthProviderConstants.PARAM_STATE).append('=').append(encodeUrl(state, true, true));

                    response.sendRedirect(builder.toString());
                    return;
                }
            }

            // Check scope
            String sScope = request.getParameter(OAuthProviderConstants.PARAM_SCOPE);
            if (Strings.isEmpty(sScope)) {
                builder.append(concat);
                concat = '&';
                builder.append(OAuthProviderConstants.PARAM_ERROR).append('=').append("invalid_request");

                builder.append(concat);
                builder.append(OAuthProviderConstants.PARAM_ERROR_DESCRIPTION).append('=').append(encodeUrl("missing required parameter: " + OAuthProviderConstants.PARAM_SCOPE, true, true));

                builder.append(concat);
                builder.append(OAuthProviderConstants.PARAM_STATE).append('=').append(encodeUrl(state, true, true));

                response.sendRedirect(builder.toString());
                return;
            }

            try {
                // Validate scope
                Scope scope = oAuthProvider.validateScope(sScope);
                if (null == scope) {
                    builder.append(concat);
                    concat = '&';
                    builder.append(OAuthProviderConstants.PARAM_ERROR).append('=').append("invalid_scope");

                    builder.append(concat);
                    builder.append(OAuthProviderConstants.PARAM_ERROR_DESCRIPTION).append('=').append(encodeUrl("invalid parameter value:" + OAuthProviderConstants.PARAM_SCOPE, true, true));

                    builder.append(concat);
                    builder.append(OAuthProviderConstants.PARAM_STATE).append('=').append(encodeUrl(state, true, true));

                    response.sendRedirect(builder.toString());
                    return;
                }

                // Authenticate
                Authenticated authed = Authentication.login(login, password, new HashMap<String, Object>(0));
                if (null == authed) {
                    throw INVALID_CREDENTIALS.create();
                }

                // Perform user / context lookup
                Context ctx = findContext(authed.getContextInfo());
                User user = findUser(ctx, authed.getUserInfo());

                // Checks if something is deactivated.
                final AuthorizationService authService = Authorization.getService();
                if (null == authService) {
                    throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AuthorizationService.class.getName());
                }
                // Authorize
                authService.authorizeUser(ctx, user);

                // Everything OK, do the redirect with authorization code & state
                // YOUR_REDIRECT_URI/?code=AUTHORIZATION_CODE&state=STATE

                String code = oAuthProvider.generateAuthorizationCodeFor(clientId, scope, user.getId(), ctx.getContextId());

                {
                    builder.append(concat);
                    concat = '&';
                    builder.append(OAuthProviderConstants.PARAM_CODE).append('=').append(encodeUrl(code, true, true));
                }

                {
                    builder.append(concat);
                    builder.append(OAuthProviderConstants.PARAM_STATE).append('=').append(encodeUrl(state, true, true));
                }

                response.sendRedirect(builder.toString());
            } catch (OXException e) {
                // Special handling for OXException after client identifier and redirect URI have been validated
                LOGGER.error("Authorization request failed", e);
                builder.append(concat);
                concat = '&';
                builder.append(OAuthProviderConstants.PARAM_ERROR).append('=').append("server_error");

                builder.append(concat);
                builder.append(OAuthProviderConstants.PARAM_ERROR_DESCRIPTION).append('=').append(encodeUrl("internal error", true, true));

                builder.append(concat);
                builder.append(OAuthProviderConstants.PARAM_STATE).append('=').append(encodeUrl(state, true, true));

                response.sendRedirect(builder.toString());
                return;
            }

        } catch (OXException e) {
            LOGGER.error("Authorization request failed", e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error_description\":\"internal error\",\"error\":\"server_error\"}");
        } catch (JSONException e) {
            LOGGER.error("Authorization request failed", e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error_description\":\"internal error\",\"error\":\"server_error\"}");
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
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

            String state = request.getParameter(OAuthProviderConstants.PARAM_STATE);
            if (Strings.isEmpty(state)) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "missing required parameter: "+OAuthProviderConstants.PARAM_STATE);
                return;
            }

            String redirectUri = request.getParameter(OAuthProviderConstants.PARAM_REDIRECT_URI);
            if (Strings.isEmpty(redirectUri)) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "missing required parameter: "+OAuthProviderConstants.PARAM_REDIRECT_URI);
                return;
            }

            String sScope = request.getParameter(OAuthProviderConstants.PARAM_SCOPE);
            if (Strings.isEmpty(sScope)) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "missing required parameter: "+OAuthProviderConstants.PARAM_SCOPE);
                return;
            }

            Scope scope = oAuthProvider.validateScope(sScope);
            if (null == scope) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "invalid parameter value: "+OAuthProviderConstants.PARAM_SCOPE);
                return;
            }

            if (false == oAuthProvider.validateClientId(clientId)) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "invalid parameter value: "+OAuthProviderConstants.PARAM_CLIENT_ID);
                return;
            }

            if (false == oAuthProvider.validateRedirectUri(clientId, redirectUri)) {
                // Send error page
                sendErrorPage(response, HttpServletResponse.SC_BAD_REQUEST, "invalid parameter value: "+OAuthProviderConstants.PARAM_REDIRECT_URI);
                return;
            }

            // Redirect to login page

            // Hidden: Client identifier, Redirect URI, Scope, State, Response Type
            // Visible: Login, Password

            StringBuilder sb = new StringBuilder(1024);
            String lineSep = System.getProperty("line.separator");
            sb.append("<!DOCTYPE html>").append(lineSep);
            sb.append("<html><head>").append(lineSep);
            {
                sb.append("<title>Login</title>").append(lineSep);
            }
            sb.append("</head><body>").append(lineSep);
            sb.append("<form action=\"").append(Tools.considerSecure(request) ? "https://" : "http://").append(determineHostName(request, -1, -1)).append(OAuthProviderConstants.AUTHORIZATION_SERVLET_ALIAS).append("\" method=\"POST\" enctype=\"application/x-www-form-urlencoded\">");
            sb.append("<input name=\"").append(OAuthProviderConstants.PARAM_USER_LOGIN).append("\" type=\"text\">");
            sb.append("<input name=\"").append(OAuthProviderConstants.PARAM_USER_PASSWORD).append("\" type=\"password\">");
            sb.append("<input name=\"").append(OAuthProviderConstants.PARAM_CLIENT_ID).append("\" type=\"hidden\" value=\"").append(clientId).append("\">");
            sb.append("<input name=\"").append(OAuthProviderConstants.PARAM_REDIRECT_URI).append("\" type=\"hidden\" value=\"").append(redirectUri).append("\">");
            sb.append("<input name=\"").append(OAuthProviderConstants.PARAM_SCOPE).append("\" type=\"hidden\" value=\"").append((null == scope ? "" : scope.scopeString())).append("\">");
            sb.append("<input name=\"").append(OAuthProviderConstants.PARAM_STATE).append("\" type=\"hidden\" value=\"").append(state).append("\">");
            sb.append("<input name=\"").append(OAuthProviderConstants.PARAM_RESPONSE_TYPE).append("\" type=\"hidden\" value=\"").append(responseType).append("\">");
            sb.append("<button name=\"").append(OAuthProviderConstants.PARAM_ACCESS_DENIED).append("\" type=\"submit\" value=\"false\">Allow</button>");
            sb.append("<button name=\"").append( OAuthProviderConstants.PARAM_ACCESS_DENIED).append("\" type=\"submit\" value=\"true\">Deny</button>");
            sb.append("</form>");
            sb.append("</body></html>").append(lineSep);

            response.setContentType("text/html; charset=UTF-8");
            response.setHeader("Content-Disposition", "inline");
            response.setStatus(200);
            PrintWriter writer = response.getWriter();
            writer.write(sb.toString());
            writer.flush();
        } catch (OXException e) {
            LOGGER.error("Login request failed", e);
            sendErrorPage(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "internal error");
        }
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
        HostnameService hostnameService = Services.getService(HostnameService.class);
        if (null == hostnameService) {
            return req.getServerName();
        }
        String hn = hostnameService.getHostname(userId, contextId);
        return null == hn ? req.getServerName() : hn;
    }

}
