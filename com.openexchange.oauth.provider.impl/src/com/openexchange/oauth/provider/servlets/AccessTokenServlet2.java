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
import java.io.OutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.v2.BaseErrorCode;
import net.oauth.v2.BaseGrantType;
import net.oauth.v2.OAuth2;
import net.oauth.v2.OAuth2Accessor;
import net.oauth.v2.OAuth2Client;
import net.oauth.v2.OAuth2Message;
import net.oauth.v2.OAuth2ProblemException;
import net.oauth.v2.server.OAuth2Servlet;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.internal.DatabaseOAuth2ProviderService;
import com.openexchange.oauth.provider.internal.OAuthProviderServiceLookup;
import com.openexchange.oauth.provider.v2.OAuth2ProviderService;

/**
 * {@link AccessTokenServlet2} - Access Token request handler for OAuth2.0
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AccessTokenServlet2 extends HttpServlet {

    private static final long serialVersionUID = 4401443514189021492L;

    private static final String PASSWORD = BaseGrantType.PASSWORD;

    private static final String CLIENT_CREDENTIALS = BaseGrantType.CLIENT_CREDENTIALS;

    private static final String REFRESH_TOKEN = BaseGrantType.REFRESH_TOKEN;

    private static final String AUTHORIZATION_CODE = BaseGrantType.AUTHORIZATION_CODE;

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        processRequest(request, response);
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        processRequest(request, response);
    }

    public void processRequest(final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {
        try {
            /*
             * Parse OAuth message
             */
            final OAuth2Message requestMessage = OAuth2Servlet.getMessage(request, null);
            final String grantType = requestMessage.getParameter(OAuth2.GRANT_TYPE);
            /*
             * Retrieve accessor by grant type
             */
            final OAuth2Accessor accessor;
            final OAuth2ProviderService providerService = getProviderService();
            if (grantType.equals(AUTHORIZATION_CODE)) {
                accessor = providerService.getAccessorByCode(requestMessage);
            } else if (grantType.equals(REFRESH_TOKEN)) {
                accessor = providerService.getAccessorByRefreshToken(requestMessage);
            } else if (grantType.equals(PASSWORD)) {
                final OAuth2Client client = providerService.getClientFromAuthHeader(requestMessage);
                accessor = new OAuth2Accessor(client);
            } else if (grantType.equals(CLIENT_CREDENTIALS)) {
                final OAuth2Client client = providerService.getClientFromAuthHeader(requestMessage);
                accessor = new OAuth2Accessor(client);
            } else {
                throw new OAuth2ProblemException(BaseErrorCode.UNSUPPORTED_GRANT_TYPE);
            }
            /*
             * Validate OAuth request with accessor
             */
            providerService.getValidator().validateRequestMessageForAccessToken(requestMessage, accessor);
            /*
             * Generate access token and secret
             */
            if (grantType.equals(AUTHORIZATION_CODE)) {
                // make sure code is authorized
                if (!Boolean.TRUE.equals(accessor.getProperty("authorized"))) {
                    throw new OAuth2ProblemException(BaseErrorCode.INVALID_GRANT);
                }
                if (accessor.accessToken == null) {
                    final int userId = accessor.<Integer> getProperty(OAuthProviderConstants.PROP_USER).intValue();
                    final int contextId = accessor.<Integer> getProperty(OAuthProviderConstants.PROP_CONTEXT).intValue();
                    providerService.generateAccessAndRefreshToken(accessor, userId, contextId);
                }
            } else if (grantType.equals(REFRESH_TOKEN)) {
                // make sure code is authorized
                if (!Boolean.TRUE.equals(accessor.getProperty("authorized"))) {
                    throw new OAuth2ProblemException(BaseErrorCode.INVALID_GRANT);
                }
                final int userId = accessor.<Integer> getProperty(OAuthProviderConstants.PROP_USER).intValue();
                final int contextId = accessor.<Integer> getProperty(OAuthProviderConstants.PROP_CONTEXT).intValue();
                providerService.generateAccessAndRefreshToken(accessor, userId, contextId);
            } else if (grantType.equals(PASSWORD)) {
                // check if username and password are valid.
                final String username = requestMessage.getParameter(OAuth2.USERNAME);
                final String password = requestMessage.getParameter(OAuth2.PASSWORD);
                if (username == null || password == null) {
                    throw new OAuth2ProblemException(BaseErrorCode.INVALID_GRANT);
                }
                // do Authentication here
                if (username.equals("invalid")) {
                    throw new OAuth2ProblemException(BaseErrorCode.INVALID_GRANT);
                }
                final int userId = accessor.<Integer> getProperty(OAuthProviderConstants.PROP_USER).intValue();
                final int contextId = accessor.<Integer> getProperty(OAuthProviderConstants.PROP_CONTEXT).intValue();
                providerService.generateAccessAndRefreshToken(accessor, userId, contextId);
            } else if (grantType.equals(CLIENT_CREDENTIALS)) {
                final int userId = accessor.<Integer> getProperty(OAuthProviderConstants.PROP_USER).intValue();
                final int contextId = accessor.<Integer> getProperty(OAuthProviderConstants.PROP_CONTEXT).intValue();
                providerService.generateAccessAndRefreshToken(accessor, userId, contextId);
                // In client credential grant type flow, a refresh token should not be included into response.
                accessor.refreshToken = null;
            }

            response.setContentType("application/json");
            final OutputStream out = response.getOutputStream();
            // OAuth.formEncode(OAuth.newList("oauth_token", accessor.accessToken,
            // "oauth_token_secret", accessor.tokenSecret),
            // out);
            if (accessor.refreshToken != null) {
                OAuth2.formEncodeInJson(OAuth2.newList(
                    OAuth2.ACCESS_TOKEN,
                    accessor.accessToken,
                    OAuth2.TOKEN_TYPE,
                    accessor.tokenType,
                    OAuth2.EXPIRES_IN,
                    "3600",
                    OAuth2.REFRESH_TOKEN,
                    accessor.refreshToken), out);
            } else {
                OAuth2.formEncodeInJson(OAuth2.newList(
                    OAuth2.ACCESS_TOKEN,
                    accessor.accessToken,
                    OAuth2.TOKEN_TYPE,
                    accessor.tokenType,
                    OAuth2.EXPIRES_IN,
                    "3600"), out);
            }
            // send response back with JSON
            out.flush();
        } catch (final Exception e) {
            // sendback without Authorization Header
            // sendback json
            final boolean sendBodyInJson = true;
            // TODO If it is the failure of client authentication, "withAuthHeader" will be true.
            final boolean withAuthHeader = false;
            DatabaseOAuth2ProviderService.handleException(e, request, response, sendBodyInJson, withAuthHeader);
        }
    }

    private OAuth2ProviderService getProviderService() {
        return OAuthProviderServiceLookup.getService(OAuth2ProviderService.class);
    }

}
