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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

    private final Map<Integer, Integer> users;

    private final Map<Integer, Integer> groups;

    public Entities() {
        super();
        users = new LinkedHashMap<>();
        groups = new LinkedHashMap<>();
    }

    /**
     * Adds a user entity.
     *
     * @param userId The user ID
     * @param permissionBits The permission bits as folder permission bit mask
     */
    public void addUser(int userId, int permissionBits) {
        users.put(userId, permissionBits);
    }

    /**
     * Adds a group entity.
     *
     * @param groupId The group ID
     * @param permissionBits The permission bits as folder permission bit mask
     */
    public void addGroupt(int groupId, int permissionBits) {
        groups.put(groupId, permissionBits);
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
    public int getUserPermissionBits(int userId) {
        return users.get(userId);
    }

    /**
     * Gets the permission bits of a contained group entity.
     *
     * @param groupId The group ID
     * @return The permission bits as folder permission bit mask
     */
    public int getGroupPermissionBits(int groupId) {
        return groups.get(groupId);
    }

    public int size() {
        return users.size() + groups.size();
    }

}
