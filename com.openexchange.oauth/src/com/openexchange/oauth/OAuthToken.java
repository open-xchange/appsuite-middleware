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

package com.openexchange.oauth;

/**
 * {@link OAuthToken} - Represents an OAuth token.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface OAuthToken {

    /**
     * The constant for an empty token. Provides the empty string for both {@link #getToken()} and {@link #getSecret()}
     * and <code>-1</code> for the TTL.
     */
    public static final OAuthToken EMPTY_TOKEN = new OAuthToken() {

        @Override
        public String getToken() {
            return "";
        }

        @Override
        public String getSecret() {
            return "";
        }

        @Override
        public long getExpiration() {
            return -1;
        }
    };

    /**
     * Gets the token.
     *
     * @return The token
     */
    String getToken();

    /**
     * Gets the secret.
     *
     * @return The secret
     */
    String getSecret();

    /**
     * Returns the expiration time stamp of the token, which is the number of milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @return The expiration time stamp or a value equal to/less than <code>0</code> (zero) to signal no expiration time stamp
     */
    long getExpiration();
}
