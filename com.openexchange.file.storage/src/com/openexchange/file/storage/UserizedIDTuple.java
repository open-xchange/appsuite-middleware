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

package com.openexchange.file.storage;

import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;

/**
 * A {@link UserizedIDTuple} to handle drive update events for shared folders
 *
 * @author <a href="mailto:jan-oliver.huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.2
 */
public class UserizedIDTuple extends IDTuple {

    private String originalFolderId;

    public UserizedIDTuple(String folderId, String fileId, String originalFolderId) {
        super(folderId, fileId);
        this.originalFolderId = originalFolderId;
    }

    public String getOriginalFolderId() {
        return originalFolderId;
    }

    public void setOriginalFolderId(String originalFolderId) {
        this.originalFolderId = originalFolderId;
    }

}
