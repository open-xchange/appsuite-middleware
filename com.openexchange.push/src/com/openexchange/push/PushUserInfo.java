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

/**
 * {@link PushUserInfo} - The push user information.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class PushUserInfo implements Comparable<PushUserInfo> {

    private final PushUser pushUser;
    private final boolean permanent;
    private final int hash;

    /**
     * Initializes a new {@link PushUserInfo}.
     *
     * @param pushUser The associated push user
     * @param permanent Whether listener is permanent or not
     */
    public PushUserInfo(PushUser pushUser, boolean permanent) {
        super();
        this.pushUser = pushUser;
        this.permanent = permanent;

        int prime = 31;
        int result = prime * 1 + (permanent ? 1231 : 1237);
        result = prime * result + ((pushUser == null) ? 0 : pushUser.hashCode());
        hash = result;
    }

    /**
     * Gets the push user
     *
     * @return The push user
     */
    public PushUser getPushUser() {
        return pushUser;
    }

    /**
     * Gets the user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return pushUser.getUserId();
    }

    /**
     * Gets the context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return pushUser.getContextId();
    }

    /**
     * Gets the permanent flag.
     *
     * @return The permanent flag
     */
    public boolean isPermanent() {
        return permanent;
    }

    @Override
    public int compareTo(PushUserInfo o) {
        return pushUser.compareTo(o.pushUser);
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
        if (!(obj instanceof PushUserInfo)) {
            return false;
        }
        PushUserInfo other = (PushUserInfo) obj;
        if (permanent != other.permanent) {
            return false;
        }
        if (pushUser == null) {
            if (other.pushUser != null) {
                return false;
            }
        } else if (!pushUser.equals(other.pushUser)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder(48).append("[userId=").append(pushUser.getUserId()).append(", contextId=").append(pushUser.getContextId()).append(", permanent=").append(permanent).append(']').toString();
    }

}
