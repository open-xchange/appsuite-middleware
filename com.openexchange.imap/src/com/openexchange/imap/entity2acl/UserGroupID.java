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

package com.openexchange.imap.entity2acl;

/**
 * {@link UserGroupID} - A user/group identifier.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UserGroupID {

    /**
     * The constant representing a failed mapping of a ACL entity name to a system user/group.
     */
    public static final UserGroupID NULL = new NullUserGroupID();

    private final int id;

    private final boolean group;

    /**
     * Initializes a new {@link UserGroupID}.
     *
     * @param id The user identifier
     * @throws IllegalArgumentException If identifier is less than zero
     */
    UserGroupID(int id) {
        this(id, false);
    }

    /**
     * Initializes a new {@link UserGroupID}.
     *
     * @param id The user/group identifier
     * @param group <code>true</code> if identifier denotes a group; otherwise <code>false</code>
     * @throws IllegalArgumentException If identifier is less than zero
     */
    UserGroupID(int id, boolean group) {
        this(id, group, false);
    }

    /**
     * Initializes a new {@link UserGroupID}.
     *
     * @param id The user/group identifier
     * @param group <code>true</code> if identifier denotes a group; otherwise <code>false</code>
     * @param allowNegative <code>true</code> if a negative identifier is allowed; otherwise <code>false</code>
     * @throws IllegalArgumentException If identifier is less than zero
     */
    protected UserGroupID(int id, boolean group, boolean allowNegative) {
        super();
        if (!allowNegative && id < 0) {
            throw new IllegalArgumentException("Identifier is less than zero.");
        }
        this.id = id;
        this.group = group;
    }

    /**
     * Gets the user/group identifier.
     *
     * @return The user/group identifier
     */
    public int getId() {
        return id;
    }

    /**
     * Checks if identifier denotes a group.
     *
     * @return <code>true</code> if identifier denotes a group; otherwise <code>false</code>
     */
    public boolean isGroup() {
        return group;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (group ? 1231 : 1237);
        result = prime * result + id;
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
        if (!(obj instanceof UserGroupID)) {
            return false;
        }
        final UserGroupID other = (UserGroupID) obj;
        if (group != other.group) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        return true;
    }

    private static final class NullUserGroupID extends UserGroupID {

        public NullUserGroupID() {
            super(-1, false, true);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (isGroup() ? 1231 : 1237);
            result = prime * result + getId();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return true;
            }
            return false;
        }

    }

}
