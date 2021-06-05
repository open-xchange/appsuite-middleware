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

import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.infostore.internal.Utils;
import com.openexchange.file.storage.infostore.internal.VirtualFolderInfostoreFacade;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.java.Strings;


/**
 * Encapsulates common methods needed by classes that delegate calls to {@link InfostoreFacade}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class InfostoreAccess {

    protected static final InfostoreFacade VIRTUAL_INFOSTORE = new VirtualFolderInfostoreFacade();
    protected static final Set<Long> VIRTUAL_FOLDERS = ImmutableSet.of(
        Long.valueOf(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID),
        Long.valueOf(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID),
        Long.valueOf(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID));

    protected final InfostoreFacade infostore;

    protected InfostoreAccess(InfostoreFacade infostore) {
        super();
        this.infostore = infostore;
    }

    protected InfostoreFacade getInfostore(final String folderId) throws OXException {
        if (Strings.isNotEmpty(folderId)) {
            try {
                if (VIRTUAL_FOLDERS.contains(Long.valueOf(Utils.parseUnsignedLong(folderId)))) {
                    return VIRTUAL_INFOSTORE;
                }
            } catch (NumberFormatException e) {
                throw InfostoreExceptionCodes.NOT_INFOSTORE_FOLDER.create(e, folderId);
            }
        }
        return infostore;
    }

    protected static int ID(final String id) {
        return Utils.parseUnsignedInt(id);
    }

    protected static long FOLDERID(final String folderId) {
        return Utils.parseUnsignedLong(folderId);
    }

    protected static int VERSION(final String version) {
        int iVersion = InfostoreFacade.CURRENT_VERSION;
        if (version != FileStorageFileAccess.CURRENT_VERSION) {
            iVersion = Utils.parseUnsignedInt(version);
        }

        return iVersion;
    }

}
