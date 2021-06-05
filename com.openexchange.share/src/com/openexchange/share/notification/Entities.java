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

package com.openexchange.share.notification;

import static com.openexchange.java.Autoboxing.I;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the IDs of different entities to notify about shares.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class Entities {

    public static enum PermissionType {
        FOLDER, OBJECT;
    }

    private final Map<Integer, Permission> users;

    private final Map<Integer, Permission> groups;

    public Entities() {
        super();
        users = new LinkedHashMap<>();
        groups = new LinkedHashMap<>();
    }

    /**
     * Adds a user entity.
     *
     * @param userId The user ID
     * @param type The permission type
     * @param permissions The permissions according to the passed type
     */
    public void addUser(int userId, PermissionType type, int permissions) {
        users.put(I(userId), new Permission(type, permissions));
    }

    /**
     * Adds a group entity.
     *
     * @param groupId The group ID
     * @param type The permission type
     * @param permissions The permissions according to the passed type
     */
    public void addGroup(int groupId, PermissionType type, int permissions) {
        groups.put(I(groupId), new Permission(type, permissions));
    }

    public Set<Integer> getUsers() {
        return users.keySet();
    }

    public Set<Integer> getGroups() {
        return groups.keySet();
    }

    /**
     * Gets the permission bits of a contained user entity.
     *
     * @param userId The user ID
     * @return The permission bits as folder permission bit mask
     */
    public Permission getUserPermissionBits(int userId) {
        return users.get(I(userId));
    }

    /**
     * Gets the permission bits of a contained group entity.
     *
     * @param groupId The group ID
     * @return The permission bits as folder permission bit mask
     */
    public Permission getGroupPermissionBits(int groupId) {
        return groups.get(I(groupId));
    }

    public int size() {
        return users.size() + groups.size();
    }

    public static final class Permission {

        private final PermissionType type;

        private final int permissions;

        public Permission(PermissionType type, int permissions) {
            super();
            this.type = type;
            this.permissions = permissions;
        }

        /**
         * Gets the type
         *
         * @return The type
         */
        public PermissionType getType() {
            return type;
        }

        /**
         * Gets the permissions
         *
         * @return The permissions
         */
        public int getPermissions() {
            return permissions;
        }
    }

}
