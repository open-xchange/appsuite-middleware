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

package com.openexchange.oauth.provider.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.authorizationserver.client.Client;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException;
import com.openexchange.oauth.provider.authorizationserver.spi.AuthorizationException;
import com.openexchange.oauth.provider.authorizationserver.spi.DefaultValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.OAuthAuthorizationService;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse;
import com.openexchange.oauth.provider.authorizationserver.spi.ValidationResponse.TokenStatus;
import com.openexchange.oauth.provider.impl.grant.OAuthGrantStorage;
import com.openexchange.oauth.provider.impl.grant.StoredGrant;
import com.openexchange.oauth.provider.impl.tools.UserizedToken;

/**
 * {@link DefaultAuthorizationService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.1
 */
public class DefaultAuthorizationService implements OAuthAuthorizationService {

    /*
     * From https://tools.ietf.org/html/rfc6750#section-2.1:
     * The syntax for Bearer credentials is as follows:
     * b64token = 1*( ALPHA / DIGIT / "-" / "." / "_" / "~" / "+" / "/" ) *"="
     * credentials = "Bearer" 1*SP b64token
     */
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\x41-\\x5a\\x61-\\x7a\\x30-\\x39-._~+/]+=*");

    private final ClientManagement clientManagement;

    private final OAuthGrantStorage grantStorage;

    public DefaultAuthorizationService(ClientManagement clientManagement, OAuthGrantStorage grantStorage) {
        super();
        this.clientManagement = clientManagement;
        this.grantStorage = grantStorage;
    }

    @Override
    public ValidationResponse validateAccessToken(String accessToken) throws AuthorizationException {
        DefaultValidationResponse response = new DefaultValidationResponse();
        if (!TOKEN_PATTERN.matcher(accessToken).matches() || !UserizedToken.isValid(accessToken)) {
            response.setTokenStatus(TokenStatus.MALFORMED);
        }

        StoredGrant grant;
        try {
            grant = grantStorage.getGrantByAccessToken(UserizedToken.parse(accessToken));
        } catch (OXException e) {
            throw new AuthorizationException("Could not get OAuth grant from storage", e);
        }

        if (grant == null) {
            response.setTokenStatus(TokenStatus.UNKNOWN);
        } else {
            response.setContextId(grant.getContextId());
            response.setUserId(grant.getUserId());

            if (grant.getExpirationDate().before(new Date())) {
                response.setTokenStatus(TokenStatus.EXPIRED);
            }

            response.setScope(new ArrayList<>(grant.getScope().get()));

            Client client = getClient(grant.getClientId());
            if (client == null) {
                response.setTokenStatus(TokenStatus.UNKNOWN);
            } else {
                response.setTokenStatus(TokenStatus.VALID);
                response.setClientName(client.getName());
            }
        }

        return response;
    }

    private Client getClient(String clientId) throws AuthorizationException {
        try {
            return clientManagement.getClientById(clientId);
        } catch (ClientManagementException e) {
            if (e.getReason() == com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException.Reason.INVALID_CLIENT_ID) {
                return null;
            }

            throw new AuthorizationException("Could not get client information for ID " + clientId, e);
        }
    }

}
