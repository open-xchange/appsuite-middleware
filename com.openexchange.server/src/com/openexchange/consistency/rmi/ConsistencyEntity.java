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

package com.openexchange.consistency.rmi;

import java.io.Serializable;
import java.rmi.Remote;
import com.openexchange.consistency.Entity.EntityType;

/**
 * {@link ConsistencyEntity}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ConsistencyEntity implements Remote, Serializable {

    private static final long serialVersionUID = -405561203602708107L;
    private final int contextId;
    private final int userId;
    private final EntityType type;

    /**
     * Initialises a new {@link ConsistencyEntity}.
     * 
     * @param contextId The context identifier
     */
    public ConsistencyEntity(int contextId) {
        this(contextId, -1);
    }

    /**
     * Initialises a new {@link ConsistencyEntity}.
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     */
    public ConsistencyEntity(int contextId, int userId) {
        if (contextId <= 0) {
            throw new IllegalArgumentException("The context identifier cannot be less than or equal to 0");
        }
        type = (userId <= 0) ? EntityType.Context : EntityType.User;
        this.contextId = contextId;
        this.userId = userId;
    }

    /**
     * Gets the contextId
     *
     * @return The contextId
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Gets the userId
     *
     * @return The userId
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the type
     *
     * @return The type
     */
    public EntityType getType() {
        return type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getType()).append(": { ContextID: '").append(getContextId()).append("'");
        if (getType().equals(EntityType.User)) {
            builder.append(", UserID: '").append(getUserId()).append("'");
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + userId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConsistencyEntity other = (ConsistencyEntity) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (type != other.type) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        return true;
    }
}
