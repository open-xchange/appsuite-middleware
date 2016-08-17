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

import static com.openexchange.tools.servlet.http.Tools.sendErrorResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.net.HttpHeaders;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.authorizationserver.client.Client;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException;
import com.openexchange.oauth.provider.authorizationserver.grant.Grant;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantManagement;
import com.openexchange.oauth.provider.impl.OAuthProviderConstants;
import com.openexchange.oauth.provider.impl.tools.URLHelper;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.http.Tools;


/**
 * {@link TokenEndpoint}
 * <p>
 * <img src="./webflow.png" alt="OAuth Web Flow">
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class TokenEndpoint extends OAuthEndpoint {

    private static final long serialVersionUID = 7597205004658187201L;

    private static final Logger LOG = LoggerFactory.getLogger(TokenEndpoint.class);


    public TokenEndpoint(ClientManagement clientManagement, GrantManagement grantManagement, ServiceLookup services) {
        super(clientManagement, grantManagement, services);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Tools.disableCaching(response);
            if (!Tools.considerSecure(request)) {
                response.setHeader(HttpHeaders.LOCATION, URLHelper.getSecureLocation(request));
                response.sendError(HttpServletResponse.SC_MOVED_PERMANENTLY);
                return;
            }

            String clientId = request.getParameter(OAuthProviderConstants.PARAM_CLIENT_ID);
            if (clientId == null) {
                failWithMissingParameter(response, OAuthProviderConstants.PARAM_CLIENT_ID);
                return;
            }

            Client client = clientManagement.getClientById(clientId);
            if (client == null) {
                failWithInvalidParameter(response, OAuthProviderConstants.PARAM_CLIENT_ID);
                return;
            }

            if (!client.isEnabled()) {
                failWithInvalidParameter(response, OAuthProviderConstants.PARAM_CLIENT_ID);
                return;
            }

            String clientSecret = request.getParameter(OAuthProviderConstants.PARAM_CLIENT_SECRET);
            if (clientSecret == null) {
                failWithMissingParameter(response, OAuthProviderConstants.PARAM_CLIENT_SECRET);
                return;
            }

            if (!client.getSecret().equals(clientSecret)) {
                fail(response, HttpServletResponse.SC_UNAUTHORIZED, "unauthorized_client", "invalid client secret");
                return;
            }

            String grantType = request.getParameter(OAuthProviderConstants.PARAM_GRANT_TYPE);
            if (grantType == null) {
                failWithMissingParameter(response, OAuthProviderConstants.PARAM_GRANT_TYPE);
                return;
            }

            if (OAuthProviderConstants.GRANT_TYPE_AUTH_CODE.equals(grantType)) {
                handleAuthorizationCode(client, request, response);
            } else if (OAuthProviderConstants.GRANT_TYPE_REFRESH_TOKEN.equals(grantType)) {
                handleRefreshToken(client, request, response);
            } else {
                failWithInvalidParameter(response, OAuthProviderConstants.PARAM_GRANT_TYPE);
                return;
            }
        } catch (OXException | JSONException | ClientManagementException e) {
            LOG.error("Token request failed", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "{\"error_description\":\"internal error\",\"error\":\"server_error\"}");
        }
    }

    private void handleAuthorizationCode(Client client, HttpServletRequest req, HttpServletResponse resp) throws IOException, JSONException, OXException {
        String redirectUri = req.getParameter(OAuthProviderConstants.PARAM_REDIRECT_URI);
        if (redirectUri == null) {
            failWithMissingParameter(resp, OAuthProviderConstants.PARAM_REDIRECT_URI);
            return;
        }

        if (!client.hasRedirectURI(redirectUri)) {
            failWithInvalidParameter(resp, OAuthProviderConstants.PARAM_REDIRECT_URI);
            return;
        }

        String authCode = req.getParameter(OAuthProviderConstants.PARAM_CODE);
        if (authCode == null) {
            failWithMissingParameter(resp, OAuthProviderConstants.PARAM_CODE);
            return;
        }

        Grant token = grantManagement.redeemAuthCode(client, redirectUri, authCode);
        if (token == null) {
            failWithInvalidParameter(resp, OAuthProviderConstants.PARAM_CODE);
            return;
        }

        respondWithToken(token, resp);
    }

    private void handleRefreshToken(Client client, HttpServletRequest req, HttpServletResponse resp) throws IOException, JSONException, OXException {
        String refreshToken = req.getParameter(OAuthProviderConstants.PARAM_REFRESH_TOKEN);
        if (refreshToken == null) {
            failWithMissingParameter(resp, OAuthProviderConstants.PARAM_REFRESH_TOKEN);
            return;
        }

        Grant token = grantManagement.redeemRefreshToken(client, refreshToken);
        if (token == null) {
            failWithInvalidParameter(resp, OAuthProviderConstants.PARAM_REFRESH_TOKEN);
            return;
        }

        respondWithToken(token, resp);
    }

    private static void respondWithToken(Grant grant, HttpServletResponse resp) throws IOException, JSONException {
        JSONObject result = new JSONObject();
        result.put("access_token", grant.getAccessToken());
        result.put("refresh_token", grant.getRefreshToken());
        result.put("token_type", "Bearer");
        result.put("expires_in", TimeUnit.SECONDS.convert(grant.getExpirationDate().getTime(), TimeUnit.MILLISECONDS) - TimeUnit.SECONDS.convert(System.currentTimeMillis(), TimeUnit.MILLISECONDS));
        result.put("scope", grant.getScope().toString());

        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        PrintWriter writer = resp.getWriter();
        writer.write(result.toString());
        writer.flush();
    }

}
