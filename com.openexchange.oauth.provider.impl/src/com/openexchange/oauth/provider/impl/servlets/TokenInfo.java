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
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import com.openexchange.java.ISO8601Utils;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.authorizationserver.grant.Grant;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantManagement;
import com.openexchange.oauth.provider.authorizationserver.spi.OAuthAuthorizationService;
import com.openexchange.server.ServiceLookup;


/**
 * {@link TokenInfo} - End-point for validating a token.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class TokenInfo extends OAuthTokenValidationEndpoint {

    private static final long serialVersionUID = 1337205004658187201L;

    private static final TimeZone TIME_ZONE_UTC = TimeZone.getTimeZone("UTC");

    // ------------------------------------------------------------------------------------------------------------------

    public TokenInfo(OAuthAuthorizationService oauthAuthService, ClientManagement clientManagement, GrantManagement grantManagement, ServiceLookup services) {
        super(oauthAuthService, clientManagement, grantManagement, services);
    }

    private void respondWithError(HttpServletResponse resp) throws Exception {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        PrintWriter writer = resp.getWriter();
        writer.write("{\"error\":\"invalid_token\"}");
        writer.flush();
    }

    @Override
    protected Set<String> requiredQueryParameters() {
        // No additional query parameters required
        return null;
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
    protected void handleValidToken(String accessToken,  Map<String, String> parameters, HttpServletRequest request, HttpServletResponse resp) throws Exception {
        Grant grant = grantManagement.getGrantByAccessToken(accessToken);
        if (null == grant) {
            respondWithError(resp);
            return;
        }

        JSONObject jResponse = new JSONObject(6);
        jResponse.put("audience", grant.getClientId());
        jResponse.put("context_id", grant.getContextId());
        jResponse.put("user_id", grant.getUserId());
        Date expirationDate = grant.getExpirationDate();
        jResponse.put("expiration_date", ISO8601Utils.format(null != expirationDate ? expirationDate : new Date(Long.MAX_VALUE), false, TIME_ZONE_UTC));
        jResponse.put("scope", grant.getScope().toString());

        resp.setContentType("application/json;charset=UTF-8");
        resp.setStatus(HttpServletResponse.SC_OK);
        PrintWriter writer = resp.getWriter();
        writer.write(jResponse.toString());
        writer.flush();
    }

}
