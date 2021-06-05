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

import static com.openexchange.tools.servlet.http.Tools.sendErrorResponse;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.authorizationserver.grant.GrantManagement;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.http.Tools;


/**
 * {@link RevokeEndpoint}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class RevokeEndpoint extends OAuthEndpoint {

    private static final long serialVersionUID = 1621367181615030938L;

    private static final Logger LOG = LoggerFactory.getLogger(RevokeEndpoint.class);

    /**
     * Initializes a new {@link RevokeEndpoint}.
     * @param oAuthProvider
     */
    public RevokeEndpoint(ClientManagement clientManagement, GrantManagement grantManagement, ServiceLookup services) {
        super(clientManagement, grantManagement, services);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Tools.disableCaching(response);
            String accessToken = request.getParameter("access_token");
            if (Strings.isEmpty(accessToken)) {
                String refreshToken = request.getParameter("refresh_token");
                if (Strings.isEmpty(refreshToken)) {
                    failWithMissingParameter(response, "refresh_token");
                    return;
                }
                if (!grantManagement.revokeByRefreshToken(refreshToken)) {
                    failWithInvalidParameter(response, "refresh_token");
                    return;
                }
            } else {
                if (!grantManagement.revokeByAccessToken(accessToken)) {
                    failWithInvalidParameter(response, "access_token");
                    return;
                }
            }

            response.setStatus(HttpServletResponse.SC_OK);
        } catch (OXException e) {
            LOG.error("Revoke request failed", e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "{\"error_description\":\"internal error\",\"error\":\"server_error\"}");
        }
    }
}
