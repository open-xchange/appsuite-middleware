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

package com.openexchange.groupware.container;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;

/**
 * DataObject
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */

public abstract class FolderChildObject extends DataObject {

    private static final long serialVersionUID = 9177795092994324456L;

    /**
     * The minimum identifier for a folder kept in database.
     */
    public static final int FOLDER_ID = 20;

    protected int parentFolderId;

    protected boolean b_parent_folder_id;

    // GET METHODS
    public int getParentFolderID() {
        return parentFolderId;
    }

    // SET METHODS
    public void setParentFolderID(final int parentFolderId) {
        this.parentFolderId = parentFolderId;
        b_parent_folder_id = true;
    }

    // REMOVE METHODS
    public void removeParentFolderID() {
        parentFolderId = 0;
        b_parent_folder_id = false;
    }

    // CONTAINS METHODS
    public boolean containsParentFolderID() {
        return b_parent_folder_id;
    }

    @Override
    public void reset() {
        super.reset();
        parentFolderId = 0;
        b_parent_folder_id = false;
    }

    @Override
    public void set(int field, Object value) {
        switch (field) {
        case FOLDER_ID:
            if (value instanceof Integer) {
                setParentFolderID(i((Integer) value));
            }
            if (value instanceof String) {
                setParentFolderID(Integer.parseInt((String) value));
            }
            break;
        default:
            super.set(field, value);
        }
    }

    @Override
    public Object get(int field) {
        switch (field) {
        case FOLDER_ID:
            return I(getParentFolderID());
        default:
            return super.get(field);
        }
    }

    @Override
    public boolean contains(int field) {
        switch (field) {
        case FOLDER_ID:
            return containsParentFolderID();
        default:
            return super.contains(field);
        }
    }

    @Override
    public void remove(int field) {
        switch (field) {
        case FOLDER_ID:
            removeParentFolderID();
            break;
        default:
            super.remove(field);
        }
    }
}
