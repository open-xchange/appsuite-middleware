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

package com.openexchange.oauth.access;

import com.openexchange.java.Strings;

/**
 * {@link OAuthClient} - A combination of the actual client instance and its associated token.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OAuthClient<T> {

    /** The client instance */
    public final T client;

    /** The client's token */
    public final String token;

    /**
     * Initializes a new {@link OAuthClient}.
     *
     * @param client The OAuth client
     * @param token The OAuth token
     * @throws IllegalArgumentException if the client is '<code>null</code>' or if the token is either '<code>null</code>' or empty.
     */
    public OAuthClient(T client, String token) {
        super();
        if (client == null) {
            throw new IllegalArgumentException("The client can not be 'null'");
        }
        if (Strings.isEmpty(token)) {
            throw new IllegalArgumentException("The OAuth token can neither be 'null' nor empty.");
        }
        this.token = token;
        this.client = client;
    }

    // For the sake of order...

    /**
     * Gets the client
     *
     * @return The client
     */
    public T getClient() {
        return client;
    }

    /**
     * Gets the token
     *
     * @return The token
     */
    public String getToken() {
        return token;
    }

}
