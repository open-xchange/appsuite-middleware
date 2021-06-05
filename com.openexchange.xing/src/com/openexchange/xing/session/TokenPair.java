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


package com.openexchange.xing.session;

import java.io.Serializable;

/**
 * <p>
 * Just two strings -- a "key" and a "secret". Used by OAuth in several places (consumer key/secret, request token/secret, access
 * token/secret). Use specific subclasses instead of using this class directly.
 * </p>
 */
public abstract class TokenPair implements Serializable {

    private static final long serialVersionUID = -2045713563484764236L;

    /**
     * The "key" portion of the pair. For example, the "consumer key", "request token", or "access token". Will never contain the "|"
     * character.
     */
    public final String key;

    /**
     * The "secret" portion of the pair. For example, the "consumer secret", "request token secret", or "access token secret".
     */
    public final String secret;

    /**
     * @param key assigned to {@link #key}.
     * @param secret assigned to {@link #secret}.
     * @throws IllegalArgumentException if key or secret is null or invalid.
     */
    public TokenPair(String key, String secret) {
        super();
        if (key == null) {
            throw new IllegalArgumentException("'key' must be non-null");
        } else if (key.indexOf('|') >= 0) {
            throw new IllegalArgumentException("'key' must not contain a \"|\" character: \"" + key + "\"");
        }
        if (secret == null) {
            throw new IllegalArgumentException("'secret' must be non-null");
        }

        this.key = key;
        this.secret = secret;
    }

    @Override
    public int hashCode() {
        return key.hashCode() ^ secret.hashCode() << 1;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TokenPair && equals((TokenPair) o);
    }

    public boolean equals(TokenPair o) {
        return key.equals(o.key) && secret.equals(o.secret);
    }

    @Override
    public String toString() {
        return "{key=\"" + key + "\", secret=\"" + secret + "\"}";
    }
}
