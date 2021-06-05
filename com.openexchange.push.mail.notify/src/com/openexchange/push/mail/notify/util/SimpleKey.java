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

package com.openexchange.push.mail.notify.util;

import com.openexchange.session.Session;

/**
 * {@link SimpleKey}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public class SimpleKey {

    /**
     * Gets a simple key instance for given user.
     *
     * @param session The session
     * @return The simple key
     */
    public static SimpleKey valueOf(Session session) {
        return null == session ? null : new SimpleKey(session.getUserId(), session.getContextId());
    }

    /**
     * Gets a simple key instance for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The simple key
     */
    public static SimpleKey valueOf(final int userId, final int contextId) {
        return new SimpleKey(userId, contextId);
    }

    // -------------------------------------------------------------------------------------------- //

    final int contextId;
    final int userId;
    private final int hash;

    private SimpleKey(final int userId, final int contextId) {
        super();
        this.contextId = contextId;
        this.userId = userId;
        // hash code
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        result = prime * result + userId;
        hash = result;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SimpleKey)) {
            return false;
        }
        final SimpleKey other = (SimpleKey) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        return true;
    }

}
