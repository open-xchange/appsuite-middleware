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

package com.openexchange.secret.impl;

import com.openexchange.session.Session;

/**
 * Reserved tokens.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum ReservedToken implements Token {
    PASSWORD("password", new Token() {

        @Override
        public String getFrom(final Session session) {
            return session.getPassword();
        }
    }),
    USER_ID("user-id", new Token() {

        @Override
        public String getFrom(final Session session) {
            return Integer.toString(session.getUserId(), 10);
        }
    }),
    CONTEXT_ID("context-id", new Token() {

        @Override
        public String getFrom(final Session session) {
            return Integer.toString(session.getContextId(), 10);
        }
    }),
    RANDOM("random", new Token() {

        @Override
        public String getFrom(final Session session) {
            return TokenBasedSecretService.RANDOM.get();
        }
    }),

    ;

    private final Token impl;

    private final String id;

    private ReservedToken(final String id, final Token impl) {
        this.id = id;
        this.impl = impl;
    }

    @Override
    public String getFrom(final Session session) {
        return impl.getFrom(session);
    }

    @Override
    public String toString() {
        return new StringBuilder(12).append('<').append(id).append('>').toString();
    }

    /**
     * Gets the reserved token for specified token name.
     *
     * @param token The token name
     * @return The reserved token or <code>null</code>
     */
    public static ReservedToken reservedTokenFor(final String token) {
        if (token == null) {
            return null;
        }
        for (final ReservedToken rt : values()) {
            if (rt.id.equals(token)) {
                return rt;
            }
        }
        return null;
    }

}
