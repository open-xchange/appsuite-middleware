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

package com.openexchange.session;

/**
 * {@link UserAndContext} - An immutable pair of user and context identifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class UserAndContext {

    /**
     * Creates a new instance
     *
     * @param session The session providing user data
     * @return The new instance
     */
    public static UserAndContext newInstance(Session session) {
        return newInstance(session.getUserId(), session.getContextId());
    }

    /**
     * Creates a new instance
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The new instance
     */
    public static UserAndContext newInstance(int userId, int contextId) {
        return new UserAndContext(userId, contextId);
    }

    // ---------------------------------------------------------------

    private final int contextId;
    private final int userId;
    private final int hash;

    /**
     * Initializes a new {@link UserAndContext}.
     */
    private UserAndContext(int userId, int contextId) {
        super();
        this.contextId = contextId;
        this.userId = userId;
        int prime = 31;
        int result = prime * 1 + contextId;
        result = prime * result + userId;
        this.hash = result;
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

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UserAndContext)) {
            return false;
        }
        UserAndContext other = (UserAndContext) obj;
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
