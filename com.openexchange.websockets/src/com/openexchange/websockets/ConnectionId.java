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

package com.openexchange.websockets;


/**
 * {@link ConnectionId} - An identifier for a certain session-bound Web Socket connection.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class ConnectionId implements Comparable<ConnectionId> {

    /**
     * Creates a new instance.
     *
     * @param id The identifier
     * @return The new instance
     */
    public static ConnectionId newInstance(String id) {
        return null == id ? null : new ConnectionId(id);
    }

    // ---------------------------------------------------------

    private final String id;
    private final int hash;

    /**
     * Initializes a new {@link ConnectionId}.
     */
    private ConnectionId(String id) {
        super();
        this.id = id;
        this.hash = id.hashCode();
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (null == obj) {
            return false;
        }
        if (obj.getClass() != ConnectionId.class) {
            return false;
        }
        ConnectionId other = (ConnectionId) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int compareTo(ConnectionId o) {
        return o == null ? -1 : id.compareTo(o.id);
    }

}
