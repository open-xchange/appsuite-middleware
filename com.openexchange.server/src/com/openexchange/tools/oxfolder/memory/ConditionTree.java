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

package com.openexchange.tools.oxfolder.memory;

import java.util.LinkedList;
import java.util.List;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * {@link ConditionTree} - A condition tree for a certain user/group.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConditionTree {

    /*private static final int SYSTEM_GLOBAL_FOLDER_ID = FolderObject.SYSTEM_GLOBAL_FOLDER_ID;*/ // finally dropped

    static final Condition CONDITION_ADMIN = new Condition() {

        @Override
        public boolean fulfilled(Permission p) {
            return p.admin;
        }
    };

    static final Condition CONDITION_READ_FOLDER = new Condition() {

        @Override
        public boolean fulfilled(Permission p) {
            return p.readFolder;
        }
    };

    private static final Condition CONDITION_FOLDER_VISIBLE = new Condition() {

        @Override
        public boolean fulfilled(Permission p) {
            return p.admin || p.readFolder;
        }
    };

    // ------------------------------------------------------------------------------------------- //

    private final TIntSet folderIds;
    private final List<Permission> permissions;
    /*private final boolean ignoreSharedAddressbook;*/ // finally dropped
    private final long stamp;

    /**
     * Initializes a new {@link ConditionTree}.
     */
    public ConditionTree() {
        super();
        folderIds = new TIntHashSet();
        permissions = new LinkedList<Permission>();
        /*ignoreSharedAddressbook = OXFolderProperties.isIgnoreSharedAddressbook();*/ // finally dropped
        stamp = System.currentTimeMillis();
    }

    /**
     * Checks if this user-associated condition tree is elapsed according to given time stamp.
     *
     * @param stamp The time stamp to compare with
     * @return <code>true</code> if elapsed; otherwise <code>false</code>
     */
    public boolean isElapsed(long stamp) {
        return this.stamp < stamp;
    }

    /**
     * Inserts specified permission.
     *
     * @param p The permission associated with a certain entity
     */
    public void insert(Permission p) {
        /*-
         *
        if (ignoreSharedAddressbook && (SYSTEM_GLOBAL_FOLDER_ID == p.fuid)) {
            return;
        }
        */
        if (CONDITION_FOLDER_VISIBLE.fulfilled(p)) {
            folderIds.add(p.fuid);
            permissions.add(p);
        }
    }

    /**
     * Gets the identifiers of visible folders.
     *
     * @return The visible folders' identifiers
     */
    public TIntSet getVisibleFolderIds() {
        return folderIds;
    }

    /**
     * Gets the identifiers of visible folders filtered by given condition.
     *
     * @param condition The condition to apply to returned identifiers
     * @return The visible folders' identifiers
     */
    public TIntSet getVisibleFolderIds(Condition condition) {
        if (null == condition) {
            return new TIntHashSet(folderIds);
        }
        TIntSet retval = new TIntHashSet(folderIds.size());
        for (Permission p : permissions) {
            if (condition.fulfilled(p)) {
                retval.add(p.fuid);
            }
        }
        return retval;
    }

}
