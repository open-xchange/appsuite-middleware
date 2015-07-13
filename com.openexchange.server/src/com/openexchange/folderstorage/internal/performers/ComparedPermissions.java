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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.GuestPermission;
import com.openexchange.folderstorage.Permission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.user.UserService;

/**
 * Helper class to calculate a diff of the folder permissions on an update request.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ComparedPermissions {

    private final Context context;
    private final UserService userService;
    private final List<GuestPermission> newGuests;
    private final Map<User, Permission> addedGuests;
    private final Map<User, Permission> addedUsers;
    private final List<Permission> addedGroups;
    private final List<Permission> removedGuests;
    private final Map<User, Permission> modifiedGuests;
    private final boolean hasChanges;
    private final Permission[] newPermissions;
    private final Permission[] originalPermissions;
    private final Connection connection;

    /**
     * Initializes a new {@link ComparedPermissions}.
     *
     * @param context The context
     * @param newPermissions The new permissions
     * @param originalPermissions The original permissions
     * @param userService The user service
     * @param connection The database connection used to load users, or <code>null</code>
     * @throws OXException
     */
    public ComparedPermissions(Context context, Permission[] newPermissions, Permission[] originalPermissions, UserService userService, Connection connection) throws OXException {
        super();
        this.context = context;
        this.newPermissions = newPermissions;
        this.originalPermissions = originalPermissions;
        this.userService = userService;
        this.connection = connection;
        newGuests = new LinkedList<GuestPermission>();
        addedGuests = new LinkedHashMap<User, Permission>();
        addedUsers = new LinkedHashMap<User, Permission>();
        addedGroups = new LinkedList<Permission>();
        removedGuests = new LinkedList<Permission>();
        modifiedGuests = new LinkedHashMap<User, Permission>();
        hasChanges = calc();
    }

    /**
     * Initializes a new {@link ComparedPermissions}.
     *
     * @param context The context
     * @param newFolder The modified object sent by the client; not <code>null</code>
     * @param origFolder The original object loaded from the storage; not <code>null</code>
     * @param userService The user service; not <code>null</code>
     * @param connection The database connection used to load users, or <code>null</code>
     * @throws OXException If errors occur when loading additional data for the comparison
     */
    public ComparedPermissions(Context context, Folder newFolder, Folder origFolder, UserService userService, Connection connection) throws OXException {
        this(context, newFolder.getPermissions(), origFolder.getPermissions(), userService, connection);
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
            if (permission.getSystem() == 0 && permission.getEntity() >= 0) {
                if (permission.isGroup()) {
                    newGroups.put(permission.getEntity(), permission);
                } else {
                    newUsers.put(permission.getEntity(), permission);
                }
            } else if (GuestPermission.class.isAssignableFrom(permission.getClass())) {
                // Check for guests among the new permissions
                newGuests.add((GuestPermission) permission);
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

        boolean permissionsChanged = newGuests.size() > 0;
        Set<Integer> addedUserIds = new HashSet<Integer>(newUsers.keySet());
        addedUserIds.removeAll(oldUsers.keySet());
        permissionsChanged |= addedUserIds.size() > 0;

        Set<Integer> addedGroupIds = new HashSet<Integer>(newGroups.keySet());
        addedGroupIds.removeAll(oldGroups.keySet());
        permissionsChanged |= addedGroupIds.size() > 0;

        Set<Integer> removedUserIds = new HashSet<Integer>(oldUsers.keySet());
        removedUserIds.removeAll(newUsers.keySet());
        permissionsChanged |= removedUserIds.size() > 0;

        Set<Integer> removedGroupIds = new HashSet<Integer>(oldGroups.keySet());
        removedGroupIds.removeAll(newGroups.keySet());
        permissionsChanged |= removedGroupIds.size() > 0;

        /*
         * Calculate new user and modified guest permissions
         */
        for (Permission newPermission : newUsers.values()) {
            User user;
            if (connection == null) {
                user = userService.getUser(newPermission.getEntity(), context.getContextId());
            } else {
                user = userService.getUser(connection, newPermission.getEntity(), context);
            }

            Permission oldPermission = oldUsers.get(newPermission.getEntity());
            if (!newPermission.equals(oldPermission)) {
                permissionsChanged = true;
                if (oldPermission == null) {
                    if (user.isGuest()) {
                        addedGuests.put(user, newPermission);
                    } else {
                        addedUsers.put(user, newPermission);
                    }
                } else {
                    if (user.isGuest()) {
                        modifiedGuests.put(user, newPermission);
                    }
                }
            }
        }

        for (Permission newPermission : newGroups.values()) {
            Permission oldPermission = oldGroups.get(newPermission.getEntity());
            if (!newPermission.equals(oldPermission)) {
                permissionsChanged = true;
                addedGroups.add(newPermission);
            }
        }

        /*
         * Calculate removed guest permissions
         */
        for (Integer removed : removedUserIds) {
            User user;
            if (connection == null) {
                user = userService.getUser(removed, context.getContextId());
            } else {
                user = userService.getUser(connection, removed, context);
            }

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
     * @return <code>true</code> if new permissions of type {@link GuestPermission} have been added
     */
    public boolean hasNewGuests() {
        return !newGuests.isEmpty();
    }

    /**
     * @return <code>true</code> if permissions for already existing guest users have been added
     */
    public boolean hasAddedGuests() {
        return !addedGuests.isEmpty();
    }

    /**
     * @return <code>true</code> if permissions for non-guest users have been added
     */
    public boolean hasAddedUsers() {
        return !addedUsers.isEmpty();
    }

    /**
     * @return <code>true</code> if permissions for groups have been added
     */
    public boolean hasAddedGroups() {
        return !addedGroups.isEmpty();
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
     * @return A list of new {@link GuestPermission}s; never <code>null</code>
     */
    public List<GuestPermission> getNewGuestPermissions() {
        return newGuests;
    }

    /**
     * @return A list of added guest permissions for already existing guest users; never <code>null</code>
     */
    public List<Permission> getAddedGuestPermissions() {
        return new ArrayList<>(addedGuests.values());
    }

    /**
     * @return A list of added permissions for non-guest guest users; never <code>null</code>
     */
    public List<Permission> getAddedUserPermissions() {
        return new ArrayList<>(addedUsers.values());
    }

    /**
     * @return A list of added permissions for groups; never <code>null</code>
     */
    public List<Permission> getAddedGroupPermissions() {
        return addedGroups;
    }

    /**
     * @return A list of already existing guest users for whom permissions have been added
     */
    public List<User> getAddedGuests() {
        return new ArrayList<>(addedGuests.keySet());
    }

    /**
     * @return A list of non-guest users for whom permissions have been added
     */
    public List<User> getAddedUsers() {
        return new ArrayList<>(addedUsers.keySet());
    }

    /**
     * @return A list of added permissions for all entities except new guests, i.e. no {@link GuestPermission}s
     */
    public List<Permission> getAddedPermissions() {
        List<Permission> permissions = new ArrayList<>(addedGuests.size() + addedUsers.size() + addedGroups.size());
        permissions.addAll(addedGuests.values());
        permissions.addAll(addedUsers.values());
        permissions.addAll(addedGroups);
        return permissions;
    }

    /**
     * @return A list of removed guest permissions; never <code>null</code>
     */
    public List<Permission> getRemovedGuestPermissions() {
        return removedGuests;
    }

    /**
     * @return A list of modified guest permissions; never <code>null</code>
     */
    public List<Permission> getModifiedGuestPermissions() {
        return new ArrayList<>(modifiedGuests.values());
    }

    /**
     * @return A list of already existing guest users for whom permissions have been modified
     */
    public List<User> getModifiedGuests() {
        return new ArrayList<>(modifiedGuests.keySet());
    }

    /**
     * @param guest The added guest
     * @return Gets the permission of the according added and already existing guest user
     */
    public Permission getAddedGuestPermission(User guest) {
        return addedGuests.get(guest);
    }

    /**
     * @param guest The modified guest
     * @return Gets the permission of the according modified guest user
     */
    public Permission getModifiedGuestPermission(User guest) {
        return modifiedGuests.get(guest);
    }

}
