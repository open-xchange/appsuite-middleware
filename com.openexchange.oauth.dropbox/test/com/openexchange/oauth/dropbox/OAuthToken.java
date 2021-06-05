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

package com.openexchange.oauth.dropbox;

/**
 * {@link OAuthToken}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OAuthToken {

    private final String key;
    private final String secret;

    /**
     * Initialises a new {@link OAuthToken}.
     */
    public OAuthToken(String key, String secret) {
        super();
        this.key = key;
        this.secret = secret;
    }

    /**
     * Gets the key
     *
     * @return The key
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the secret
     *
     * @return The secret
     */
    public String getSecret() {
        return secret;
    }
}
