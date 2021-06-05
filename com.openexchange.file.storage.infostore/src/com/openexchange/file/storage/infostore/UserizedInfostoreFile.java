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

package com.openexchange.file.storage.infostore;

import com.openexchange.file.storage.UserizedFile;
import com.openexchange.groupware.infostore.DocumentMetadata;

/**
 * {@link UserizedInfostoreFile}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class UserizedInfostoreFile extends InfostoreFile implements UserizedFile {

    private String originalId;
    private String originalFolderId;

    /**
     * Initializes a new {@link UserizedInfostoreFile}.
     *
     * @param documentMetadata
     */
    public UserizedInfostoreFile(DocumentMetadata documentMetadata) {
        super(documentMetadata);
    }

    @Override
    public String getOriginalId() {
        if (originalId == null) {
            return getId();
        }

        return originalId;
    }

    @Override
    public String getOriginalFolderId() {
        if (originalFolderId == null) {
            return getFolderId();
        }

        return originalFolderId;
    }

    @Override
    public void setOriginalId(String originalId) {
        this.originalId = originalId;
    }

    @Override
    public void setOriginalFolderId(String originalFolderId) {
        this.originalFolderId = originalFolderId;
    }

}
