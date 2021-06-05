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
import com.openexchange.drive.FileVersion;
import com.openexchange.drive.impl.comparison.ThreeWayComparison;

/**
 * {@link RemoveFileAction}
 *
 * Removes an existing file. The <code>file</code> parameter is set to the client- or server-file to delete. Deleting a file implicitly
 * removes any stored metadata of the file. If the file- or metadata to delete no longer exists, no error should occur, and the action
 * should be treated as no-op.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RemoveFileAction extends AbstractFileAction {

    /**
     * Initializes a new {@link RemoveFileAction}.
     *
     * @param file The file to delete
     * @param path The path to the parent directory
     */
    public RemoveFileAction(FileVersion file, ThreeWayComparison<FileVersion> comparison, String path) {
        super(file, null, comparison);
        parameters.put(PARAMETER_PATH, path);
    }

    @Override
    public Action getAction() {
        return Action.REMOVE;
    }

}

