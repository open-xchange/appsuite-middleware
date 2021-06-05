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

package com.openexchange.file.storage.boxcom;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import com.box.sdk.BoxFile.Info;
import com.box.sdk.BoxLock;
import com.box.sdk.BoxSharedLink;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.java.Strings;
import com.openexchange.mime.MimeTypeMap;

/**
 * {@link BoxFile}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class BoxFile extends DefaultFile {

    /**
     * Initializes a new {@link BoxFile}.
     *
     * @param folderId The folder identifier
     * @param id The file identifier
     * @param userId The user identifier
     */
    public BoxFile(String folderId, String id, int userId, String rootFolderId) {
        super();
        setFolderId(isRootFolder(folderId, rootFolderId) ? FileStorageFolder.ROOT_FULLNAME : folderId);
        setCreatedBy(userId);
        setModifiedBy(userId);
        setId(id);
        setFileName(id);
        setVersion(FileStorageFileAccess.CURRENT_VERSION);
        setNumberOfVersions(1);
        setIsCurrentVersion(true);
        setUniqueId(id);
    }

    private static boolean isRootFolder(String id, String rootFolderId) {
        return "0".equals(id) || rootFolderId.equals(id);
    }

    @Override
    public String toString() {
        final String url = getURL();
        return url == null ? super.toString() : url;
    }

    /**
     * Parses specified Box file.
     *
     * @param file The Box file
     * @throws OXException If parsing Box file fails
     * @return This Box file
     */
    public BoxFile parseBoxFile(Info info) throws OXException {
        return parseBoxFile(info, null);
    }

    /**
     * Parses specified Box file.
     *
     * @param info The Box file
     * @param fields The fields to consider
     * @throws OXException If parsing Box file fails
     * @return This Box file with property set applied
     */
    public BoxFile parseBoxFile(Info info, List<Field> fields) throws OXException {
        if (null != info) {
            try {
                final String name = info.getName();
                setTitle(name);
                setFileName(name);
                final Set<Field> set = null == fields || fields.isEmpty() ? EnumSet.allOf(Field.class) : EnumSet.copyOf(fields);

                if (set.contains(Field.CREATED)) {
                    Date parsed = info.getCreatedAt();
                    if (parsed != null) {
                        setCreated(parsed);
                    }
                }
                if (set.contains(Field.LAST_MODIFIED) || set.contains(Field.LAST_MODIFIED_UTC)) {
                    Date parsed = info.getModifiedAt();
                    if (parsed != null) {
                        setLastModified(parsed);
                    }
                }
                if (set.contains(Field.FILE_MIMETYPE)) {
                    MimeTypeMap map = Services.getService(MimeTypeMap.class);
                    String contentType = null == map ? "application/octet-stream" : map.getContentType(name);
                    setFileMIMEType(contentType);
                }
                if (set.contains(Field.FILE_SIZE)) {
                    long size = info.getSize();
                    setFileSize(size);
                }
                if (set.contains(Field.URL)) {
                    BoxSharedLink sharedLink = info.getSharedLink();
                    if (null != sharedLink) {
                        setURL(sharedLink.getDownloadURL());
                    }
                }
                if (set.contains(Field.COLOR_LABEL)) {
                    setColorLabel(0);
                }
                if (set.contains(Field.CATEGORIES)) {
                    List<String> tags = info.getTags();
                    if (null != tags && !tags.isEmpty()) {
                        setCategories(Strings.concat(", ", tags.toArray(new String[tags.size()])));
                    }
                }
                if (set.contains(Field.DESCRIPTION)) {
                    setDescription(info.getDescription());
                }
                if (set.contains(Field.VERSION_COMMENT)) {
                    setVersionComment(null);
                }
                if (set.contains(Field.LOCKED_UNTIL)) {
                    BoxLock boxLock = info.getLock();
                    if (boxLock != null) {
                        Date parsed = boxLock.getExpiresAt();
                        if (parsed != null) {
                            setLockedUntil(parsed);
                        } else {
                            setLockedUntil(new Date(Long.MAX_VALUE)); //indefinite
                        }
                    }
                }
            } catch (RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }
}
