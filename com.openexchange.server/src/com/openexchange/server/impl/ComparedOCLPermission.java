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

package com.openexchange.server.impl;

import static com.openexchange.java.Autoboxing.I;
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
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link ComparedOCLPermission}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public abstract class ComparedOCLPermission<P> {

    private Collection<P> newPermissions;
    private Collection<P> originalPermissions;
    private Map<Integer, P> addedUsers;
    private List<P> addedGroups;
    private List<P> removedUsers;
    private List<P> modifiedUsers;
    private boolean hasChanges;

    /**
     * Initializes a new {@link ComparedOCLPermission}. You must call {@link #calc()} to initialize the instance completely!
     *
     * @param newPermissions The new permissions or <code>null</code>
     * @param originalPermissions The original permissions or <code>null</code>
     */
    protected ComparedOCLPermission(P[] newPermissions, P[] originalPermissions) {
        this(newPermissions == null ? null : Arrays.asList(newPermissions), originalPermissions == null ? null : Arrays.asList(originalPermissions));
    }

    /**
     * Initializes a new {@link ComparedOCLPermission}. You must call {@link #calc()} to initialize the instance completely!
     *
     * @param newPermissions The new permissions or <code>null</code>
     * @param originalPermissions The original permissions or <code>null</code>
     */
    protected ComparedOCLPermission(Collection<P> newPermissions, Collection<P> originalPermissions) {
        super();
        this.newPermissions = newPermissions;
        this.originalPermissions = originalPermissions;
    }

    protected abstract boolean isSystemPermission(P p);

    protected abstract boolean isGroupPermission(P p);

    protected abstract int getEntityId(P p);

    protected abstract boolean areEqual(P p1, P p2);

    protected void calc() {
        if (newPermissions == null) {
            addedUsers = Collections.emptyMap();
            addedGroups = Collections.emptyList();
            removedUsers = Collections.emptyList();
            modifiedUsers = Collections.emptyList();
            hasChanges = false;
            return;
        }

        addedUsers = new LinkedHashMap<>();
        addedGroups = new LinkedList<>();
        removedUsers = new LinkedList<>();
        modifiedUsers = new LinkedList<>();

        /*
         * Calculate added permissions
         */
        final Map<Integer, P> newUsers = new HashMap<>();
        final Map<Integer, P> newGroups = new HashMap<>();
        for (P permission : newPermissions) {
            if (isSystemPermission(permission)) {
                continue;
            }
            if (isGroupPermission(permission)) {
                newGroups.put(I(getEntityId(permission)), permission);
            } else {
                newUsers.put(I(getEntityId(permission)), permission);
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
                    oldGroups.put(I(getEntityId(permission)), permission);
                } else {
                    oldUsers.put(I(getEntityId(permission)), permission);
                }
            }
        }

        boolean permissionsChanged = false;
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
         * Calculate new user permissions
         */
        for (P newPermission : newUsers.values()) {
            int entityId = getEntityId(newPermission);
            P oldPermission = oldUsers.get(I(entityId));
            if (!areEqual(newPermission, oldPermission)) {
                permissionsChanged = true;
                if (oldPermission == null) {
                    addedUsers.put(I(entityId), newPermission);
                }
            }
        }

        for (P newPermission : newGroups.values()) {
            P oldPermission = oldGroups.get(I(getEntityId(newPermission)));
            if (!areEqual(newPermission, oldPermission)) {
                permissionsChanged = true;
                addedGroups.add(newPermission);
            }
        }

        /*
         * Calculate removed permissions
         */
        for (Integer removed : removedUserIds) {
            removedUsers.add(oldUsers.get(removed));
        }

        for (Entry<Integer, P> entry : newUsers.entrySet()) {
            if (oldUsers.containsKey(entry.getKey())) {
                modifiedUsers.add(entry.getValue());
            }
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
     * @return <code>true</code> if permissions for users have been added
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
     * @return <code>true</code> if user permissions have been removed
     */
    public boolean hasRemovedUsers() {
        return !removedUsers.isEmpty();
    }

    /**
     * @return <code>true</code> if user permissions have been modified
     */
    public boolean hasModifiedUsers() {
        return !modifiedUsers.isEmpty();
    }

    /**
     * @return A list of added permissions for users; never <code>null</code>
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
     * @return A list of added permissions for all entities
     */
    public List<P> getAddedPermissions() {
        List<P> permissions = new ArrayList<>(addedUsers.size() + addedGroups.size());
        permissions.addAll(addedUsers.values());
        permissions.addAll(addedGroups);
        return permissions;
    }

    /**
     * @return A list of removed user permissions; never <code>null</code>
     */
    public List<P> getRemovedUserPermissions() {
        return removedUsers;
    }

    /**
     * @return A list of modified user permissions; never <code>null</code>
     */
    public List<P> getModifiedUserPermissions() {
        return modifiedUsers;
    }

    /**
     * @return A list of user IDs for whom permissions have been added
     */
    public List<Integer> getAddedUsers() {
        return new ArrayList<>(addedUsers.keySet());
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
