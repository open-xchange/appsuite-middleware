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

package com.openexchange.websockets.grizzly.remote;

/**
 * {@link Removal} - Represents a remove operation from Hazelcast multi-map.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class Removal {

    private final int userId;
    private final int contextId;
    private final String memberUuid;
    private final String connectionId;
    private volatile Integer hash;

    /**
     * Initializes a new {@link Removal}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param memberUuid The member UUID
     * @param connectionId The identifier of the Web Socket connection
     */
    public Removal(int userId, int contextId, String memberUuid, String connectionId) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.memberUuid = memberUuid;
        this.connectionId = connectionId;
    }

    @Override
    public int hashCode() {
        Integer tmp = hash;
        if (null == tmp) {
            int prime = 31;
            int result = 1;
            result = prime * result + contextId;
            result = prime * result + userId;
            result = prime * result + ((memberUuid == null) ? 0 : memberUuid.hashCode());
            result = prime * result + ((connectionId == null) ? 0 : connectionId.hashCode());
            tmp = Integer.valueOf(result);
            hash = tmp;
        }
        return tmp.intValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Removal)) {
            return false;
        }
        Removal other = (Removal) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        if (memberUuid == null) {
            if (other.memberUuid != null) {
                return false;
            }
        } else if (!memberUuid.equals(other.memberUuid)) {
            return false;
        }
        if (connectionId == null) {
            if (other.connectionId != null) {
                return false;
            }
        } else if (!connectionId.equals(other.connectionId)) {
            return false;
        }
        return true;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the member UUID
     *
     * @return The member UUID
     */
    public String getMemberUuid() {
        return memberUuid;
    }

    /**
     * Gets the identifier of the Web Socket connection
     *
     * @return The connection identifier
     */
    public String getConnectionId() {
        return connectionId;
    }

}
