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

package com.openexchange.drive.events.internal;

import java.util.Collections;
import java.util.Map;
import com.openexchange.drive.Action;
import com.openexchange.drive.DirectoryVersion;
import com.openexchange.drive.DriveAction;
import com.openexchange.java.Strings;

/**
 * {@link SyncDirectoriesAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class SyncDirectoriesAction implements DriveAction<DirectoryVersion> {

    private final String rootFolderID;

    /**
     * Initializes a new {@link SyncDirectoriesAction}.
     *
     * @param rootFolderID The corresponding session's root folder identifier
     */
    public SyncDirectoriesAction(String rootFolderID) {
        super();
        this.rootFolderID = rootFolderID;
    }

    @Override
    public int compareTo(DriveAction<DirectoryVersion> o) {
        return 0;
    }

    @Override
    public Action getAction() {
        return Action.SYNC;
    }

    @Override
    public DirectoryVersion getVersion() {
        return null;
    }

    @Override
    public DirectoryVersion getNewVersion() {
        return null;
    }

    @Override
    public Map<String, Object> getParameters() {
        if (Strings.isNotEmpty(rootFolderID)) {
            return Collections.<String, Object>singletonMap(DriveAction.PARAMETER_ROOT, rootFolderID);
        }
        return Collections.emptyMap();
    }

}
