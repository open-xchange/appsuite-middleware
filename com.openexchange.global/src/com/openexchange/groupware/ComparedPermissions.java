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

package com.openexchange.groupware;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;

/**
 * {@link ComparedPermissions}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class ComparedPermissions<P, GP extends P> {

    private Collection<P> newPermissions;
    private Collection<P> originalPermissions;
    private List<GP> newGuests;
    private Map<Integer, P> addedGuests;
    private Map<Integer, P> addedUsers;
    private List<P> addedGroups;
    private List<P> removedGuests;
    private List<P> removedUsers;
    private Map<Integer, P> modifiedGuests;
    private boolean hasChanges;

    /**
     * Initializes a new {@link ComparedPermissions}. You must call {@link #calc()} to initialize the instance completely!
     *
     * @param newPermissions The new permissions or <code>null</code>
     * @param originalPermissions The original permissions or <code>null</code>
     * @throws OXException
     */
    protected ComparedPermissions(P[] newPermissions, P[] originalPermissions) throws OXException {
        this(newPermissions == null ? null : Arrays.asList(newPermissions), originalPermissions == null ? null : Arrays.asList(originalPermissions));
    }

    /**
     * Initializes a new {@link ComparedPermissions}. You must call {@link #calc()} to initialize the instance completely!
     *
     * @param newPermissions The new permissions or <code>null</code>
     * @param originalPermissions The original permissions or <code>null</code>
     * @throws OXException
     */
    protected ComparedPermissions(Collection<P> newPermissions, Collection<P> originalPermissions) throws OXException {
        super();
        this.newPermissions = newPermissions;
        this.originalPermissions = originalPermissions;
    }

    protected abstract boolean isSystemPermission(P p);

    protected abstract boolean isUnresolvedGuestPermission(P p);

    protected abstract boolean isGuestUser(int userId) throws OXException;

    protected abstract boolean isGroupPermission(P p);

    protected abstract int getEntityId(P p);

    protected abstract boolean areEqual(P p1, P p2);

    protected void calc() throws OXException {
        if (newPermissions == null) {
            newGuests = Collections.emptyList();
            addedGuests = Collections.emptyMap();
            addedUsers = Collections.emptyMap();
            addedGroups = Collections.emptyList();
            removedGuests = Collections.emptyList();
            removedUsers = Collections.emptyList();
            modifiedGuests = Collections.emptyMap();
            hasChanges = false;
            return;
        } else {
            newGuests = new LinkedList<>();
            addedGuests = new LinkedHashMap<>();
            addedUsers = new LinkedHashMap<>();
            addedGroups = new LinkedList<>();
            removedGuests = new LinkedList<>();
            removedUsers = new LinkedList<>();
            modifiedGuests = new LinkedHashMap<>();
        }

        /*
         * Calculate added permissions
         */
        final Map<Integer, P> newUsers = new HashMap<>();
        final Map<Integer, P> newGroups = new HashMap<>();
        for (P permission : newPermissions) {
            if (isSystemPermission(permission)) {
                continue;
            }

            if (isUnresolvedGuestPermission(permission)) {
                // Check for guests among the new permissions
                newGuests.add((GP) permission);
            } else {
                if (isGroupPermission(permission)) {
                    newGroups.put(getEntityId(permission), permission);
                } else {
                    newUsers.put(getEntityId(permission), permission);
                }
            }
        }

        /*
         * Calculate removed permissions
         */
        final Map<Integer, P> oldUsers = new HashMap<>();
        final Map<Integer, P> oldGroups = new HashMap<>();
        if (null != originalPermissions) {
            for (P permission : originalPermissions) {
                if (isSystemPermission(permission)) {
                    continue;
                }

                if (isGroupPermission(permission)) {
                    oldGroups.put(getEntityId(permission), permission);
                } else {
                    oldUsers.put(getEntityId(permission), permission);
                }
            }
        }

        boolean permissionsChanged = newGuests.size() > 0;
        Set<Integer> addedUserIds = new HashSet<>(newUsers.keySet());
        addedUserIds.removeAll(oldUsers.keySet());
        permissionsChanged |= addedUserIds.size() > 0;

        Set<Integer> addedGroupIds = new HashSet<>(newGroups.keySet());
        addedGroupIds.removeAll(oldGroups.keySet());
        permissionsChanged |= addedGroupIds.size() > 0;

        Set<Integer> removedUserIds = new HashSet<>(oldUsers.keySet());
        removedUserIds.removeAll(newUsers.keySet());
        permissionsChanged |= removedUserIds.size() > 0;

        Set<Integer> removedGroupIds = new HashSet<>(oldGroups.keySet());
        removedGroupIds.removeAll(newGroups.keySet());
        permissionsChanged |= removedGroupIds.size() > 0;

        /*
         * Calculate new user and modified guest permissions
         */
        for (P newPermission : newUsers.values()) {
            int entityId = getEntityId(newPermission);
            P oldPermission = oldUsers.get(entityId);
            if (!areEqual(newPermission, oldPermission)) {
                permissionsChanged = true;
                boolean isGuest = isGuestUser(entityId);
                if (oldPermission == null) {
                    if (isGuest) {
                        addedGuests.put(entityId, newPermission);
                    } else {
                        addedUsers.put(entityId, newPermission);
                    }
                } else if (isGuest) {
                    modifiedGuests.put(entityId, newPermission);
                }
            }
        }

        for (P newPermission : newGroups.values()) {
            P oldPermission = oldGroups.get(getEntityId(newPermission));
            if (!areEqual(newPermission, oldPermission)) {
                permissionsChanged = true;
                addedGroups.add(newPermission);
            }
        }

        /*
         * Calculate removed guest permissions
         */
        for (Integer removed : removedUserIds) {
            if (isGuestUser(removed)) {
                removedGuests.add(oldUsers.get(removed));
            }
            removedUsers.add(oldUsers.get(removed));
        }

        hasChanges = permissionsChanged;
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
     * @return <code>true</code> if guest permissions have been removed
     */
    public boolean hasRemovedUsers() {
        return !removedUsers.isEmpty();
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
    public List<GP> getNewGuestPermissions() {
        return newGuests;
    }

    /**
     * @return A list of added guest permissions for already existing guest users; never <code>null</code>
     */
    public List<P> getAddedGuestPermissions() {
        return new ArrayList<>(addedGuests.values());
    }

    /**
     * @return A list of added permissions for non-guest guest users; never <code>null</code>
     */
    public List<P> getAddedUserPermissions() {
        return new ArrayList<>(addedUsers.values());
    }

    /**
     * @return A list of added permissions for groups; never <code>null</code>
     */
    public List<P> getAddedGroupPermissions() {
        return addedGroups;
    }

    /**
     * @return A list of added permissions for all entities except new guests, i.e. no {@link GuestPermission}s
     */
    public List<P> getAddedPermissions() {
        List<P> permissions = new ArrayList<>(addedGuests.size() + addedUsers.size() + addedGroups.size());
        permissions.addAll(addedGuests.values());
        permissions.addAll(addedUsers.values());
        permissions.addAll(addedGroups);
        return permissions;
    }

    /**
     * @return A list of removed guest permissions; never <code>null</code>
     */
    public List<P> getRemovedGuestPermissions() {
        return removedGuests;
    }

    /**
     * @return A list of removed user permissions; never <code>null</code>
     */
    public List<P> getRemovedUserPermissions() {
        return removedUsers;
    }

    /**
     * @return A list of modified guest permissions; never <code>null</code>
     */
    public List<P> getModifiedGuestPermissions() {
        return new ArrayList<>(modifiedGuests.values());
    }

    /**
     * @return A list of already existing guest user IDs for whom permissions have been added
     */
    public List<Integer> getAddedGuests() {
        return new ArrayList<>(addedGuests.keySet());
    }

    /**
     * @return A list of non-guest user IDs for whom permissions have been added
     */
    public List<Integer> getAddedUsers() {
        return new ArrayList<>(addedUsers.keySet());
    }

    /**
     * @return A list of already existing guest user IDs for whom permissions have been modified
     */
    public List<Integer> getModifiedGuests() {
        return new ArrayList<>(modifiedGuests.keySet());
    }

    /**
     * @param guest The added guests ID
     * @return Gets the permission of the according added and already existing guest user
     */
    public P getAddedGuestPermission(Integer guestId) {
        return addedGuests.get(guestId);
    }

    /**
     * @param guest The modified guests ID
     * @return Gets the permission of the according modified guest user
     */
    public P getModifiedGuestPermission(Integer guestId) {
        return modifiedGuests.get(guestId);
    }

    /**
     * Gets the collection of original (storage) permissions that were used to calculate the changes.
     *
     * @return The original permissions
     */
    public Collection<P> getOriginalPermissions() {
        return originalPermissions;
    }

    /**
     * Gets the collection of the new (updated) permissions that were used to calculate the changes.
     *
     * @return The new permissions
     */
    public Collection<P> getNewPermissions() {
        return newPermissions;
    }

}
