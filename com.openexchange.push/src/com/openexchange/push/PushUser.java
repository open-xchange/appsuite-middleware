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

package com.openexchange.push;

import java.util.Optional;

/**
 * {@link PushUser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class PushUser implements Comparable<PushUser> {

    private final int userId;
    private final int contextId;
    private final int hash;
    private final String optSessionId;

    /**
     * Initializes a new {@link PushUser}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public PushUser(int userId, int contextId) {
        this(userId, contextId, Optional.empty());
    }

    /**
     * Initializes a new {@link PushUser}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param idOfIssuingSession The optional identifier of the session that issues an operation for this push user
     */
    public PushUser(int userId, int contextId, Optional<String> idOfIssuingSession) {
        super();
        this.userId = userId;
        this.contextId = contextId;
        this.optSessionId = idOfIssuingSession.orElse(null);

        int prime = 31;
        int result = prime * 1 + contextId;
        result = prime * result + userId;
        hash = result;
    }

    @Override
    public int compareTo(PushUser other) {
        int thisInt = this.contextId;
        int otherInt = other.contextId;
        if (thisInt == otherInt) {
            thisInt = this.userId;
            otherInt = other.userId;
        }
        return (thisInt < otherInt) ? -1 : (thisInt == otherInt ? 0 : 1);
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
     * Gets the optional identifier of the session that issues an operation for this push user.
     *
     * @return The session identifier or empty
     */
    public Optional<String> getIdOfIssuingSession() {
        return Optional.ofNullable(optSessionId);
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
        if (!(obj instanceof PushUser)) {
            return false;
        }
        PushUser other = (PushUser) obj;
        if (contextId != other.contextId) {
            return false;
        }
        if (userId != other.userId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append("[userId=").append(userId).append(", contextId=").append(contextId).append(']').toString();
    }

}
