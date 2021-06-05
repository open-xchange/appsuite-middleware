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

import java.util.List;
import com.openexchange.drive.events.DriveContentChange;
import com.openexchange.file.storage.IdAndName;

/**
 * {@link DriveContentChangeImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.3
 */
public class DriveContentChangeImpl implements DriveContentChange {

    private final String folderId;
    private final List<IdAndName> pathToRoot;

    /**
     * Initializes a new {@link DriveContentChangeImpl}.
     * 
     * @param folderId The affected folder id
     * @param pathToRoot The path to the root folder
     */
    public DriveContentChangeImpl(String folderId, List<IdAndName> pathToRoot) {
        super();
        this.folderId = folderId;
        this.pathToRoot = pathToRoot;
    }

    @Override
    public String getFolderId() {
        return folderId;
    }

    @Override
    public List<IdAndName> getPathToRoot() {
        return pathToRoot;
    }

    @Override
    public boolean isSubfolderOf(String rootFolderId) {
        for (IdAndName idAndName : pathToRoot) {
            if (idAndName.getId().equals(rootFolderId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getPath(String rootFolderId) {
        String path = "";
        for (IdAndName idAndName : pathToRoot) {
            if (idAndName.getId().equals(rootFolderId)) {
                path = '/' + path;
                break;
            }
            path = path.isEmpty() ? idAndName.getName() : idAndName.getName() + '/' + path;
        }
        return path;
    }

}
