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

package com.openexchange.saml.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.saml.oauth.service.OAuthAccessToken;
import com.openexchange.saml.oauth.service.OAuthAccessTokenService;
import com.openexchange.server.ServiceLookup;

/**
 * {@link HttpClientOAuthAccessTokenService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class HttpClientOAuthAccessTokenService implements OAuthAccessTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthAccessTokenService.class);

    // -----------------------------------------------------------------------------------------------------------------

    private final ServiceLookup services;
    
    private final OAuthAccessTokenRequest accessTokenRequest;
    private final OAuthRefreshTokenRequest refreshTokenRequest;

    /**
     * Initializes a new {@link HttpClientOAuthAccessTokenService}.
     * 
     * @param services The service lookup to get the {@link ConfigViewFactory} and the {@link HttpClientService} from
     */
    public HttpClientOAuthAccessTokenService(ServiceLookup services) {
        super();
        this.services = services;

        // Initialize request instances
        accessTokenRequest = new OAuthAccessTokenRequest(services, "saml-oauth");
        refreshTokenRequest = new OAuthRefreshTokenRequest(services, "saml-oauth");
    }

    @Override
    public OAuthAccessToken getAccessToken(OAuthGrantType type, String data, int userId, int contextId, String scope) throws OXException {
        if (null == type) {
            throw new OXException(new IllegalArgumentException("Missing grant type"));
        }

        switch (type) {
            case SAML:
                OAuthAccessToken result = accessTokenRequest.requestAccessToken(data, userId, contextId, scope);
                LOG.debug("Successfully handled a SAML assertion for an access token.");
                return result;
            case REFRESH_TOKEN:
                return refreshTokenRequest.requestAccessToken(data, userId, contextId, scope);
        }

        // Should never occur
        throw OXException.general("Unknown grant type: " + type);
    }

    @Override
    public boolean isConfigured(int userId, int contextId) throws OXException {
        return SAMLOAuthConfig.isConfigured(userId, contextId, services.getServiceSafe(ConfigViewFactory.class));
    }

}
