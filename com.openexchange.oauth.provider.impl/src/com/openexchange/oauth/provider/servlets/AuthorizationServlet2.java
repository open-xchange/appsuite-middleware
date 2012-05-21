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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.v2.BaseResponseType;
import net.oauth.v2.OAuth2;
import net.oauth.v2.OAuth2.Parameter;
import net.oauth.v2.OAuth2Accessor;
import net.oauth.v2.OAuth2Client;
import net.oauth.v2.OAuth2Message;
import net.oauth.v2.OAuth2ProblemException;
import net.oauth.v2.server.OAuth2Servlet;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.internal.DatabaseOAuth2ProviderService;
import com.openexchange.oauth.provider.internal.OAuthProviderServiceLookup;
import com.openexchange.oauth.provider.v2.OAuth2ProviderService;

/**
 * {@link AuthorizationServlet2} - Autherization request handler for OAuth2.0.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AuthorizationServlet2 extends AbstractAuthorizationServlet {

    private static final long serialVersionUID = 6393806486708501254L;

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        OAuth2Message requestMessage = null;
        try {
            /*
             * Parse OAuth message
             */
            requestMessage = OAuth2Servlet.getMessage(request, null);
            /*
             * Get provider service
             */
            final OAuth2ProviderService providerService = getProviderService();
            final OAuth2Client client = providerService.getClient(requestMessage);
            /*
             * Validate
             */
            providerService.getValidator().validateRequestMessageForAuthorization(requestMessage, client);
            /*
             * Redirect
             */
            sendToAuthorizePage(request, response, client);
        } catch (final Exception e) {
            final boolean sendBodyInJson = false;
            final boolean withAuthHeader = false;
            if ((e instanceof OAuth2ProblemException) && null != requestMessage) {
                final OAuth2ProblemException problem = (OAuth2ProblemException) e;
                problem.setParameter(OAuth2.REDIRECT_URI, OAuth2.decodePercent(requestMessage.getParameter(OAuth2.REDIRECT_URI)));
                problem.setParameter(OAuth2ProblemException.HTTP_STATUS_CODE, new Integer(302));
                /* it can be removed at here */
                if (requestMessage.getParameter(OAuth2.STATE) != null) {
                    problem.setParameter(OAuth2.STATE, requestMessage.getParameter(OAuth2.STATE));
                }
            }
            DatabaseOAuth2ProviderService.handleException(e, request, response, sendBodyInJson, withAuthHeader);
        }

    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        try {
            /*
             * Parse OAuth message
             */
            final OAuth2Message requestMessage = OAuth2Servlet.getMessage(request, null);
            /*
             * Get provider service
             */
            final OAuth2ProviderService providerService = getProviderService();
            final OAuth2Client client = providerService.getClient(requestMessage);
            /*
             * Get user/context identifier
             */
            final String login = request.getParameter("login");
            final String password = request.getParameter("password");
            if (isEmpty(login) || isEmpty(password)) {
                providerService.getValidator().validateRequestMessageForAuthorization(requestMessage, client);
                sendToAuthorizePage(request, response, client);
                return;
            }
            /*
             * Resolve login
             */
            final Map<String, Object> map = resolveLogin(login, password);
            /*
             * Set userId in accessor and mark it as authorized
             */
            final OAuth2Accessor accessor = new OAuth2Accessor(client);
            final int userId = ((User) map.get("user")).getId();
            final int contextId = ((Context) map.get("context")).getContextId();
            accessor.setProperty(OAuthProviderService.PROP_LOGIN, login);
            accessor.setProperty(OAuthProviderService.PROP_PASSWORD, password);
            providerService.markAsAuthorized(accessor, userId, contextId);
            /*
             * Process by response type
             */
            final String requested = requestMessage.getParameter(OAuth2.RESPONSE_TYPE);
            if (requested.equals(BaseResponseType.CODE)) {
                providerService.generateCode(accessor, userId, contextId);
                returnToConsumer(request, response, accessor);
            } else if (requested.equals(BaseResponseType.TOKEN)) {
                /*
                 * Generate refresh token here but do not send back that
                 */
                providerService.generateAccessAndRefreshToken(accessor, userId, contextId);
                String redirectUri = request.getParameter(OAuth2.REDIRECT_URI);
                final String state = request.getParameter(OAuth2.STATE);

                final List<Parameter> list = new ArrayList<Parameter>(5);
                list.add(new Parameter(OAuth2.ACCESS_TOKEN, accessor.accessToken));
                list.add(new Parameter(OAuth2.TOKEN_TYPE, accessor.tokenType));
                list.add(new Parameter(OAuth2.EXPIRES_IN, "3600"));
                if (accessor.scope != null) {
                    list.add(new Parameter(OAuth2.SCOPE, accessor.scope));
                }
                if (state != null) {
                    list.add(new Parameter(OAuth2.STATE, state));
                }

                redirectUri = OAuth2.addParametersAsFragment(redirectUri, list);
                response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                response.setHeader("Location", OAuth2.decodePercent(redirectUri));
            } else if (requested.equals(BaseResponseType.CODE_AND_TOKEN)) {
                // TODO
            } else {
                // TODO
            }
        } catch (final Exception e) {
            final boolean sendBodyInJson = false;
            final boolean withAuthHeader = false;
            if (e instanceof OAuth2ProblemException) {
                final OAuth2ProblemException problem = (OAuth2ProblemException) e;
                problem.setParameter(OAuth2ProblemException.HTTP_STATUS_CODE, Integer.valueOf(HttpServletResponse.SC_MOVED_TEMPORARILY));
                // problem.setParameters(OAuth2ProblemException.HTTP_LOCATION,)
            }
            DatabaseOAuth2ProviderService.handleException(e, request, response, sendBodyInJson, withAuthHeader);
        }
    }

    private void sendToAuthorizePage(final HttpServletRequest request, final HttpServletResponse response, final OAuth2Client client) throws IOException, ServletException {
        final String redirect_uri = request.getParameter(OAuth2.REDIRECT_URI);
        final String response_type = request.getParameter(OAuth2.RESPONSE_TYPE);

        // maybe redirect_uri check shold be done OAuth2Validator
        // if(redirect_uri == null || redirect_uri.length() <=0) {
        // throw exception
        // }
        final String client_description = (String) client.getProperty("description");
        request.setAttribute("CLIE_DESC", client_description);
        request.setAttribute("REDIRECT_URI", redirect_uri);
        request.setAttribute("RESPONSE_TYPE", response_type);
        request.setAttribute("CLIE_ID", client.clientId);
        request.getRequestDispatcher //
        ("/authorize2.jsp").forward(request, response);

    }

    private void returnToConsumer(final HttpServletRequest request, final HttpServletResponse response, final OAuth2Accessor accessor) throws IOException, ServletException {
        /*
         * Send the user back to site's callBackUrl
         */
        final String redirectUri = request.getParameter(OAuth2.REDIRECT_URI);
        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        response.setHeader("Location", OAuth2.decodePercent(redirectUri));
    }

    private OAuth2ProviderService getProviderService() {
        return OAuthProviderServiceLookup.getService(OAuth2ProviderService.class);
    }

}
