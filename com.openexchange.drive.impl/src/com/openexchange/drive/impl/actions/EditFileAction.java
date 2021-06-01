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

package com.openexchange.drive.impl.actions;

import com.openexchange.drive.Action;
import com.openexchange.drive.DriveAction;
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.comparison.ThreeWayComparison;

/**
 * {@link EditFileAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class EditFileAction extends AbstractFileAction {

    private int sortKey;

    /**
     * Initializes a new {@link EditFileAction}.
     *
     * @param file The (old) file version
     * @param newFile The new file version
     * @param comparison The underlying synchronization comparison this action resulted from
     * @param path The path of the parent directory where the action should be executed
     */
    public EditFileAction(FileVersion file, FileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path) {
        this(file, newFile, comparison, path, 0);
    }

    /**
     * Initializes a new {@link EditFileAction}.
     *
     * @param file The (old) file version
     * @param newFile The new file version
     * @param comparison The underlying synchronization comparison this action resulted from
     * @param path The path of the parent directory where the action should be executed
     * @param sortKey The sort key, or <code>0</code> if not applicable
     */
    public EditFileAction(FileVersion file, FileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path, int sortKey) {
        this(file, newFile, comparison, path, sortKey, true);
    }

    /**
     * Initializes a new {@link EditFileAction}.
     *
     * @param file The (old) file version
     * @param newFile The new file version
     * @param comparison The underlying synchronization comparison this action resulted from
     * @param path The path of the parent directory where the action should be executed
     * @param acknowledge <code>false</code> to not perform an implicit acknowledgment, or <code>true</code> for the default behavior
     */
    public EditFileAction(FileVersion file, FileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path, boolean acknowledge) {
        this(file, newFile, comparison, path, 0, acknowledge);
    }

    /**
     * Initializes a new {@link EditFileAction}.
     *
     * @param file The (old) file version
     * @param newFile The new file version
     * @param comparison The underlying synchronization comparison this action resulted from
     * @param path The path of the parent directory where the action should be executed
     * @param sortKey The sort key, or <code>0</code> if not applicable
     * @param acknowledge <code>false</code> to not perform an implicit acknowledgment, or <code>true</code> for the default behavior
     */
    public EditFileAction(FileVersion file, FileVersion newFile, ThreeWayComparison<FileVersion> comparison, String path, int sortKey, boolean acknowledge) {
        super(file, newFile, comparison);
        this.sortKey = sortKey;
        parameters.put(PARAMETER_PATH, path);
        if (false == acknowledge) {
            parameters.put(PARAMETER_ACKNOWLEDGE, Boolean.valueOf(acknowledge));
        }
    }

    @Override
    public Action getAction() {
        return Action.EDIT;
    }

    /**
     * Gets the sortKey
     *
     * @return The sortKey
     */
    public int getSortKey() {
        return sortKey;
    }

    /**
     * Sets the sortKey
     *
     * @param sortKey The sortKey to set
     */
    public void setSortKey(int sortKey) {
        this.sortKey = sortKey;
    }

    @Override
    public int compareTo(DriveAction<FileVersion> other) {
        int result = super.compareTo(other);
        if (0 != result) {
            return result;
        }
        if (EditFileAction.class.isInstance(other)) {
            EditFileAction otherEditFileAction = (EditFileAction)other;
            /*
             * compare sort keys if available
             */
            result = this.getSortKey() - otherEditFileAction.getSortKey();
            if (0 != result) {
                return result;
            }
            /*
             * compare new version if available
             */
            if (null != this.getNewVersion() && null != otherEditFileAction.getNewVersion()) {
                result = -1 * this.getNewVersion().getName().compareTo(otherEditFileAction.getNewVersion().getName());
            }
            if (0 != result) {
                return result;
            }
        }
        return result;
    }

}

