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

package com.openexchange.saml.spi;

/**
 * Encapsulates information about whose sessions are to be terminated.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class LogoutInfo {

    private int contextId = -1;

    private int userId = -1;

    public LogoutInfo() {
        super();
    }

    /**
     * Gets the context ID.
     *
     * @return The context ID or <code>-1</code> if the context could not be determined
     */
    public int getContextId() {
        return contextId;
    }

    /**
     * Sets the context ID.
     *
     * @param contextId The context ID
     */
    public void setContextId(int contextId) {
        this.contextId = contextId;
    }

    /**
     * Gets the user ID.
     *
     * @return The user ID or <code>-1</code> if the user could not be determined
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the user ID.
     *
     * @param userId The user ID
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + contextId;
        result = prime * result + userId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LogoutInfo other = (LogoutInfo) obj;
        if (contextId != other.contextId)
            return false;
        if (userId != other.userId)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "LogoutInfo [contextId=" + contextId + ", userId=" + userId + "]";
    }

}
