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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.internal.performers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.GuestPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.groupware.ldap.User;
import com.openexchange.user.UserService;

/**
 * Helper class to calculate a diff of the folder permissions on an update request.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class ComparedPermissions {

    private final int contextId;
    private final UserService userService;
    private final List<GuestPermission> addedGuests;
    private final LinkedList<Permission> removedGuests;
    private final LinkedList<Permission> modifiedGuests;
    private final boolean hasChanges;
    private final Permission[] newPermissions;
    private final Permission[] originalPermissions;

    /**
     * Initializes a new {@link ComparedPermissions}.
     *
     * @param contextId The context ID
     * @param newPermissions The new permissions
     * @param originalPermissions The original permissions
     * @param userService The user service
     * @throws OXException
     */
    public ComparedPermissions(int contextId, Permission[] newPermissions, Permission[] originalPermissions, UserService userService) throws OXException {
        super();
        this.contextId = contextId;
        this.newPermissions = newPermissions;
        this.originalPermissions = originalPermissions;
        this.userService = userService;
        addedGuests = new LinkedList<GuestPermission>();
        removedGuests = new LinkedList<Permission>();
        modifiedGuests = new LinkedList<Permission>();
        hasChanges = calc();
    }

    /**
     * Initializes a new {@link ComparedPermissions}.
     *
     * @param contextId The context id
     * @param newFolder The modified object sent by the client; not <code>null</code>
     * @param origFolder The original object loaded from the storage; not <code>null</code>
     * @param userService The user service; not <code>null</code>
     * @throws OXException If errors occur when loading additional data for the comparison
     */
    public ComparedPermissions(int contextId, Folder newFolder, Folder origFolder, UserService userService) throws OXException {
        this(contextId, newFolder.getPermissions(), origFolder.getPermissions(), userService);
    }

    private boolean calc() throws OXException {
        if (newPermissions == null) {
            return false;
        }

        /*
         * Calculate added permissions
         */
        final Map<Integer, Permission> newUsers = new HashMap<Integer, Permission>();
        final Map<Integer, Permission> newGroups = new HashMap<Integer, Permission>();
        for (Permission permission : newPermissions) {
            if (permission.getSystem() == 0 && permission.getEntity() > 0) {
                if (permission.isGroup()) {
                    newGroups.put(permission.getEntity(), permission);
                } else {
                    newUsers.put(permission.getEntity(), permission);
                }
            }

            // Check for guests among the new permissions
            if (GuestPermission.class.isAssignableFrom(permission.getClass())) {
                addedGuests.add((GuestPermission) permission);
            }
        }

        /*
         * Calculate removed permissions
         */
        final Map<Integer, Permission> oldUsers = new HashMap<Integer, Permission>();
        final Map<Integer, Permission> oldGroups = new HashMap<Integer, Permission>();
        if (null != originalPermissions) {
            for (Permission permission : originalPermissions) {
                if (permission.getSystem() == 0) {
                    if (permission.isGroup()) {
                        oldGroups.put(permission.getEntity(), permission);
                    } else {
                        oldUsers.put(permission.getEntity(), permission);
                    }
                }
            }
        }

        boolean permissionsChanged = addedGuests.size() > 0;
        Set<Integer> addedUsers = new HashSet<Integer>(newUsers.keySet());
        addedUsers.removeAll(oldUsers.keySet());
        permissionsChanged |= addedUsers.size() > 0;

        Set<Integer> addedGroups = new HashSet<Integer>(newGroups.keySet());
        addedGroups.removeAll(oldGroups.keySet());
        permissionsChanged |= addedGroups.size() > 0;

        Set<Integer> removedUsers = new HashSet<Integer>(oldUsers.keySet());
        removedUsers.removeAll(newUsers.keySet());
        permissionsChanged |= removedUsers.size() > 0;

        Set<Integer> removedGroups = new HashSet<Integer>(oldGroups.keySet());
        removedGroups.removeAll(newGroups.keySet());
        permissionsChanged |= removedGroups.size() > 0;

        /*
         * Calculate modifications of guest permissions
         */
        for (Permission newPermission : newUsers.values()) {
            Permission oldPermission = oldUsers.get(newPermission.getEntity());
            if (!newPermission.equals(oldPermission)) {
                permissionsChanged = true;
                User user = userService.getUser(newPermission.getEntity(), contextId);
                if (user.isGuest()) {
                    modifiedGuests.add(newPermission);
                }
            }
        }

        for (Permission newPermission : newGroups.values()) {
            Permission oldPermission = oldGroups.get(newPermission.getEntity());
            if (!newPermission.equals(oldPermission)) {
                permissionsChanged = true;
                break;
            }
        }

        /*
         * Calculate removed guest permissions
         */
        for (Integer removed : removedUsers) {
            User user = userService.getUser(removed, contextId);
            if (user.isGuest()) {
                removedGuests.add(oldUsers.get(removed));
            }
        }

        return permissionsChanged;
    }

    /**
     * @return Whether the permissions of both folder objects differ from each other
     */
    public boolean hasChanges() {
        return hasChanges;
    }

    /**
     * @return <code>true</code> if guest permissions have been added
     */
    public boolean hasNewGuests() {
        return !addedGuests.isEmpty();
    }

    /**
     * @return <code>true</code> if guest permissions have been removed
     */
    public boolean hasRemovedGuests() {
        return !removedGuests.isEmpty();
    }

    /**
     * @return <code>true</code> if guest permissions have been modified
     */
    public boolean hasModifiedGuests() {
        return !modifiedGuests.isEmpty();
    }

    /**
     * @return A list of added guest permissions; never <code>null</code>
     */
    public List<GuestPermission> getAddedGuests() {
        return addedGuests;
    }

    /**
     * @return A list of removed guest permissions; never <code>null</code>
     */
    public LinkedList<Permission> getRemovedGuests() {
        return removedGuests;
    }

    /**
     * @return A list of modified guest permissions; never <code>null</code>
     */
    public LinkedList<Permission> getModifiedGuests() {
        return modifiedGuests;
    }

}
