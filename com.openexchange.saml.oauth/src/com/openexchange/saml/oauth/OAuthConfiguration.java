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

/**
 * {@link OAuthConfiguration} - An OAuth configuration for a certain user.
 * <ul>
 * <li>Token end-point (HTTP)
 * <li>Client identifier
 * <li>Client secret
 * </ul>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class OAuthConfiguration {

    private final String tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final String scope;

    /**
     * Initializes a new {@link OAuthConfiguration}.
     *
     * @param tokenEndpoint The token end-point
     * @param clientId The client identifier
     * @param clientSecret The client secret
     */
    public OAuthConfiguration(String tokenEndpoint, String clientId, String clientSecret, String scope) {
        super();
        this.tokenEndpoint = tokenEndpoint;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
    }

    /**
     * Gets the token end-point
     *
     * @return The token end-point
     */
    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    /**
     * Gets the client identifier
     *
     * @return The client identifier
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the client secret
     *
     * @return The client secret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Gets the scope
     *
     * @return The scope
     */
    public String getScope() {
        return scope;
    }

}
