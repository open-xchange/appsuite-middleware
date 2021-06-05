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

package com.openexchange.database.internal;

/**
 * {@link LockMech} - Specifies how a locking for a certain database pool (and possibly a given schema) is supposed to be performed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum LockMech {

    /**
     * Performs row locking through a <code>"SELECT ... FOR UPDATE"</code> statement using pool identifier (and possibly schema name) for fine-grained lock scope.
     */
    ROW_LOCK("row"),
    /**
     * Performs global locking through attempting to increment a counter in a "semaphore" table:<br><code>"UPDATE ctx_per_schema_sem SET id=id+1"</code>
     */
    GLOBAL_LOCK("global");

    private final String id;

    private LockMech(String id) {
        this.id = id;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the lock mechanism for specified identifier
     *
     * @param id The identifier
     * @return The looked-up lock mechanism or {@link #ROW_LOCK} as fall-back
     */
    public static LockMech lockMechFor(String id) {
        if (null != id) {
            for (LockMech lm : values()) {
                if (lm.id.equals(id)) {
                    return lm;
                }
            }
        }
        return LockMech.ROW_LOCK;
    }

}
