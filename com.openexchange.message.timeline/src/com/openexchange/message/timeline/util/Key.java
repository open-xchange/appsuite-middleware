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

package com.openexchange.message.timeline.util;

import com.openexchange.session.Session;

/**
 * {@link Key} - A user-bound key to his queues.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Key {

    public static Key valueOf(final Session session) {
        return valueOf(session.getUserId(), session.getContextId());
    }

    public static Key valueOf(final int userId, final int cid) {
        return new Key(userId, cid);
    }

    // -------------------------------------------------------------- //

    private final int userId;
    private final int cid;
    private final int hash;

    /**
     * Initializes a new {@link Key}.
     */
    private Key(final int userId, final int cid) {
        super();
        this.userId = userId;
        this.cid = cid;
        final int prime = 31;
        int result = 1;
        result = prime * result + cid;
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
        if (!(obj instanceof Key)) {
            return false;
        }
        final Key other = (Key) obj;
        if (cid != other.cid) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        return true;
    }

}