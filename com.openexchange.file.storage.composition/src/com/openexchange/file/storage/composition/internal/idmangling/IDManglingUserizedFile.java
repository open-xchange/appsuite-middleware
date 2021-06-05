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

package com.openexchange.file.storage.composition.internal.idmangling;

import com.openexchange.file.storage.File;
import com.openexchange.file.storage.MediaStatus;
import com.openexchange.file.storage.UserizedFile;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.session.Session;


/**
 * {@link IDManglingUserizedFile}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class IDManglingUserizedFile extends IDManglingFile implements UserizedFile {

    private final String originalId;
    private final String originalFolder;

    /**
     * Initializes a new {@link IDManglingUserizedFile}.
     *
     * @param file The delegate file
     * @param service The service identifier
     * @param account The account identifier
     */
    IDManglingUserizedFile(UserizedFile file, String service, String account) {
        super(file, service, account);
        originalId = new FileID(service, account, file.getOriginalFolderId(), file.getOriginalId()).toUniqueID();
        originalFolder = new FolderID(service, account, file.getOriginalFolderId()).toUniqueID();
    }

    @Override
    public String getOriginalId() {
        return originalId;
    }

    @Override
    public void setOriginalId(String id) {
        throw new IllegalStateException("IDs are only read-only with this class");
    }

    @Override
    public String getOriginalFolderId() {
        return originalFolder;
    }

    @Override
    public void setOriginalFolderId(String id) {
        throw new IllegalStateException("IDs are only read-only with this class");
    }

    @Override
    public MediaStatus getMediaStatusForClient(Session session) {
        File delegate = getDelegate();
        return delegate instanceof UserizedFile ? ((UserizedFile) delegate).getMediaStatusForClient(session) : getMediaStatus();
    }

}
