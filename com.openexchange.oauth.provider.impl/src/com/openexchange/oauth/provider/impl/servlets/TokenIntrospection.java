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

package com.openexchange.oauth.provider.impl.servlets;

import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import com.openexchange.mail.service.MailService;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.authorizationserver.grant.Grant;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantManagement;
import com.openexchange.oauth.provider.authorizationserver.spi.OAuthAuthorizationService;
import com.openexchange.oauth.provider.impl.OAuthProviderConstants;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.User;
import com.openexchange.user.UserService;


/**
 * {@link TokenIntrospection} - End-point for validating a token.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class TokenIntrospection extends OAuthTokenValidationEndpoint {

    private static final long serialVersionUID = 1337205004658187201L;

    // ------------------------------------------------------------------------------------------------------------------

    public TokenIntrospection(OAuthAuthorizationService oauthAuthService, ClientManagement clientManagement, GrantManagement grantManagement, ServiceLookup services) {
        super(oauthAuthService, clientManagement, grantManagement, services);
    }

    private void respondWithError(HttpServletResponse resp) throws Exception {
        respondWithError("invalid_token", resp);
    }

    private void respondWithError(String errorToken, HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        PrintWriter writer = resp.getWriter();
        writer.write(new StringBuilder("{\"error\":\"").append(errorToken).append("\"}").toString());
        writer.flush();
    }

    @Override
    protected Set<String> requiredQueryParameters() {
        return Collections.singleton(OAuthProviderConstants.PARAM_CLIENT_ID);
    }

    @Override
    protected void handleExpiredToken(String accessToken, HttpServletRequest request, HttpServletResponse resp) throws Exception {
        respondWithError(resp);
    }

    @Override
    protected void handleMalformedToken(String accessToken, HttpServletRequest request, HttpServletResponse resp) throws Exception {
        respondWithError(resp);
    }

    @Override
    protected void handleUnknownToken(String accessToken, HttpServletRequest request, HttpServletResponse resp) throws Exception {
        respondWithError(resp);
    }

    @Override
    protected void handleValidToken(String accessToken, Map<String, String> parameters, HttpServletRequest request, HttpServletResponse resp) throws Exception {
        Grant grant = grantManagement.getGrantByAccessToken(accessToken);
        if (null == grant) {
            respondWithError(resp);
            return;
        }

        {
            String givenClientId = parameters.get(OAuthProviderConstants.PARAM_CLIENT_ID);
            if (false == givenClientId.equals(grant.getClientId())) {
                respondWithError("invalid_client_id", resp);
                return;
            }
        }

        UserService userService = services.getService(UserService.class);
        if (null == userService) {
            throw ServiceExceptionCode.absentService(UserService.class);
        }

        MailService mailService = services.getService(MailService.class);
        if (null == mailService) {
            throw ServiceExceptionCode.absentService(MailService.class);
        }

        User user = userService.getUser(grant.getUserId(), grant.getContextId());
        String mailLogin = mailService.getMailLoginFor(grant.getUserId(), grant.getContextId(), 0);

        JSONObject jResponse = new JSONObject(4);
        jResponse.put("mail_login", mailLogin);
        jResponse.put("enabled", user.isMailEnabled());
        jResponse.put("login_info", user.getLoginInfo());

        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        PrintWriter writer = resp.getWriter();
        writer.write(jResponse.toString());
        writer.flush();
    }

}
