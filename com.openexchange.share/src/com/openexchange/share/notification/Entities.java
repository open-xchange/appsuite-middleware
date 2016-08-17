/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.share.notification;

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
        users.put(userId, new Permission(type, permissions));
    }

    /**
     * Adds a group entity.
     *
     * @param groupId The group ID
     * @param type The permission type
     * @param permissions The permissions according to the passed type
     */
    public void addGroup(int groupId, PermissionType type, int permissions) {
        groups.put(groupId, new Permission(type, permissions));
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
        return users.get(userId);
    }

    /**
     * Gets the permission bits of a contained group entity.
     *
     * @param groupId The group ID
     * @return The permission bits as folder permission bit mask
     */
    public Permission getGroupPermissionBits(int groupId) {
        return groups.get(groupId);
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
