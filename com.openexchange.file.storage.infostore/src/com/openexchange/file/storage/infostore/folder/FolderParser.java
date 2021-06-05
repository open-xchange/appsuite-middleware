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

package com.openexchange.file.storage.infostore.folder;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.SetterAwareFileStorageFolder;
import com.openexchange.file.storage.infostore.osgi.Services;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.filestorage.FileStorageUtils;

/**
 * {@link FolderParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderParser {

    private static final String REAL_TREE_ID = FolderStorage.REAL_TREE_ID;

    private static final String INFOSTORE = "infostore";

    private static volatile ContentType contentType;

    /**
     * Initializes a new {@link FolderParser}.
     */
    private FolderParser() {
        super();
    }

    /**
     * Parses a folder from given file storage folder.
     *
     * @param fsFolder The file storage folder
     * @return The parsed folder
     * @throws OXException If parsing folder fails
     */
    public static Folder parseFolder(final FileStorageFolder fsFolder) throws OXException {
        if (null == fsFolder) {
            return null;
        }
        try {
            final ParsedFolder folder = new ParsedFolder();
            folder.setTreeID(REAL_TREE_ID);
            {
                final String str = fsFolder.getId();
                if (!com.openexchange.java.Strings.isEmpty(str)) {
                    folder.setID(str);
                }
            }
            {
                final String str = fsFolder.getParentId();
                if (!com.openexchange.java.Strings.isEmpty(str)) {
                    folder.setParentID(str);
                }
            }
            {
                final String str = fsFolder.getName();
                if (!com.openexchange.java.Strings.isEmpty(str)) {
                    folder.setName(str);
                }
            }
            {
                folder.setContentType(getContentType());
            }

            if (false == SetterAwareFileStorageFolder.class.isInstance(fsFolder) || ((SetterAwareFileStorageFolder) fsFolder).containsSubscribed()) {
                folder.setSubscribed(fsFolder.isSubscribed());
            }

            {
                final List<FileStoragePermission> permissions = fsFolder.getPermissions();
                if (null != permissions && !permissions.isEmpty()) {
                    folder.setPermissions(FileStorageUtils.getPermissions(permissions));
                }
            }

            folder.setMeta(fsFolder.getMeta());

            return folder;
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public static ContentType getContentType() throws OXException {
        ContentType ct = contentType;
        if (null == ct) {
            synchronized (FolderParser.class) {
                ct = contentType;
                if (null == ct) {
                    try {
                        final ContentTypeDiscoveryService discoveryService = Services.getService(ContentTypeDiscoveryService.class);
                        ct = discoveryService.getByString(INFOSTORE);
                        if (null == ct) {
                            throw FolderExceptionErrorMessage.UNKNOWN_CONTENT_TYPE.create(INFOSTORE);
                        }
                        contentType = ct;
                    } catch (OXException e) {
                        throw e;
                    }
                }
            }
        }
        return ct;
    }

}
