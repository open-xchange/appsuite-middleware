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

package com.openexchange.oauth.impl.internal;

import org.scribe.model.Token;
import com.openexchange.oauth.OAuthToken;

/**
 * {@link ScribeOAuthToken} - An {@link OAuthToken} backed by a Scribe {@link Token} instance. If passed instance is <code>null</code>, an
 * empty token is assumed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ScribeOAuthToken implements OAuthToken {

    private static final String EMPTY = "";
    private final Token token;

    /**
     * Initializes a new {@link ScribeOAuthToken}.
     *
     * @param token The Scribe token
     */
    public ScribeOAuthToken(final Token token) {
        super();
        this.token = token;
    }

    @Override
    public String getSecret() {
        return token == null ? EMPTY : token.getSecret();
    }

    @Override
    public String getToken() {
        return token == null ? EMPTY : token.getToken();
    }

    @Override
    public String toString() {
        return token == null ? "<empty-token>" : token.toString();
    }

    @Override
    public long getExpiration() {
        return token == null ? 0L : (token.getExpiry() == null ? 0L : token.getExpiry().getTime());
    }
}
