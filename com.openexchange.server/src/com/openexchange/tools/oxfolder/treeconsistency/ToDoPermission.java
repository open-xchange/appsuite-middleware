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

package com.openexchange.tools.oxfolder.treeconsistency;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * {@link ToDoPermission} - Simple helper class for tree-consistency checks
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class ToDoPermission {

    private final int folderId;

    private final Set<Integer> users;

    private final Set<Integer> groups;

    /**
     * Initializes a new {@link ToDoPermission}
     *
     * @param folderId The folder ID
     */
    ToDoPermission(final int folderId) {
        super();
        this.folderId = folderId;
        users = new HashSet<Integer>(4);
        groups = new HashSet<Integer>(4);
    }

    void addEntity(final int entity, final boolean isGroup) {
        if (isGroup) {
            addGroup(entity);
        } else {
            addUser(entity);
        }
    }

    void addUser(final int user) {
        users.add(Integer.valueOf(user));
    }

    void addGroup(final int group) {
        groups.add(Integer.valueOf(group));
    }

    int getFolderId() {
        return folderId;
    }

    int[] getUsers() {
        final int size = users.size();
        final int[] retval = new int[size];
        if (size == 0) {
            return retval;
        }
        final Iterator<Integer> i = users.iterator();
        for (int j = 0; j < size; j++) {
            retval[j] = i.next().intValue();
        }
        return retval;
    }

    int[] getGroups() {
        final int size = groups.size();
        final int[] retval = new int[size];
        if (size == 0) {
            return retval;
        }
        final Iterator<Integer> i = groups.iterator();
        for (int j = 0; j < size; j++) {
            retval[j] = i.next().intValue();
        }
        return retval;
    }

}
