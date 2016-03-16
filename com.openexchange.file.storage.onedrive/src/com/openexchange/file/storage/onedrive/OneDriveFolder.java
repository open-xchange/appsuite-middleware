/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.file.storage.onedrive;

import java.text.ParseException;
import java.util.Collections;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.TypeAware;
import com.openexchange.file.storage.onedrive.rest.folder.RestFolder;
import com.openexchange.file.storage.onedrive.utils.ISO8601DateParser;

/**
 * {@link OneDriveFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OneDriveFolder extends DefaultFileStorageFolder implements TypeAware {

    private static final String TYPE_FOLDER = OneDriveConstants.TYPE_FOLDER;

    private FileStorageFolderType type;

    /**
     * Initializes a new {@link OneDriveFolder}.
     */
    public OneDriveFolder(final int userId) {
        super();
        type = FileStorageFolderType.NONE;
        holdsFiles = true;
        b_holdsFiles = true;
        holdsFolders = true;
        b_holdsFolders = true;
        exists = true;
        subscribed = true;
        b_subscribed = true;
        final DefaultFileStoragePermission permission = DefaultFileStoragePermission.newInstance();
        permission.setEntity(userId);
        permissions = Collections.<FileStoragePermission> singletonList(permission);
        ownPermission = permission;
        createdBy = userId;
        modifiedBy = userId;
    }

    @Override
    public FileStorageFolderType getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type The type to set
     * @return This folder with type applied
     */
    public OneDriveFolder setType(FileStorageFolderType type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return id == null ? super.toString() : id;
    }

    /**
     * Parses specified Microsoft OneDrive directory.
     *
     * @param dir The Microsoft OneDrive directory
     * @param rootFolderId The identifier of the root folder
     * @param accountDisplayName The account's display name
     * @param hasSubfolders Whether this folder has subfolders;<br>
     *            e.g. <tt>"GET https://apis.live.net/v5.0/&lt;folder-id&gt;/files?access_token=&lt;access-token&gt;"</tt>
     * @throws OXException If parsing Microsoft OneDrive directory fails
     */
    public OneDriveFolder parseDirEntry(RestFolder dir, String rootFolderId, String accountDisplayName, boolean hasSubfolders) throws OXException {
        if (null != dir) {
            try {
                id = dir.getId();
                rootFolder = isRootFolder(dir.getId(), rootFolderId);
                b_rootFolder = true;

                if (rootFolder) {
                    id = FileStorageFolder.ROOT_FULLNAME;
                    setParentId(null);
                    setName(null == accountDisplayName ? dir.getName() : accountDisplayName);
                } else {
                    String parentId = dir.getParentId();
                    setParentId(isRootFolder(parentId, rootFolderId) ? FileStorageFolder.ROOT_FULLNAME : parentId);
                    setName(dir.getName());
                }

                {
                    String createdAt = dir.getCreatedTime();
                    if (null != createdAt) {
                        try {
                            creationDate = (ISO8601DateParser.parse(createdAt));
                        } catch (ParseException e) {
                            Logger logger = org.slf4j.LoggerFactory.getLogger(OneDriveFile.class);
                            logger.warn("Could not parse date from: {}", createdAt, e);
                        }
                    }
                }
                {
                    String modifiedAt = dir.getUpdatedTime();
                    if (null != modifiedAt) {
                        try {
                            lastModifiedDate = (ISO8601DateParser.parse(modifiedAt));
                        } catch (ParseException e) {
                            Logger logger = org.slf4j.LoggerFactory.getLogger(OneDriveFile.class);
                            logger.warn("Could not parse date from: {}", modifiedAt, e);
                        }
                    }
                }

                setSubfolders(hasSubfolders);
                setSubscribedSubfolders(hasSubfolders);
            } catch (final RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }

    /**
     * Parses specified Microsoft OneDrive directory.
     *
     * @param dir The Microsoft OneDrive directory
     * @param rootFolderId The identifier of the root folder
     * @param accountDisplayName The account's display name
     * @param hasSubfolders Whether this folder has subfolders;<br>
     *            e.g. <tt>"GET https://apis.live.net/v5.0/&lt;folder-id&gt;/files?access_token=&lt;access-token&gt;"</tt>
     * @throws OXException If parsing Microsoft OneDrive directory fails
     */
    public OneDriveFolder parseDirEntry(JSONObject dir, String rootFolderId, String accountDisplayName, boolean hasSubfolders) throws OXException {
        if (null != dir) {
            try {
                id = dir.optString("id", null);
                rootFolder = isRootFolder(id, rootFolderId);
                b_rootFolder = true;

                if (rootFolder) {
                    id = FileStorageFolder.ROOT_FULLNAME;
                    setParentId(null);
                    setName(null == accountDisplayName ? dir.optString("name", null) : accountDisplayName);
                } else {
                    String parentId = dir.optString("parent_id", null);
                    setParentId(isRootFolder(parentId, rootFolderId) ? FileStorageFolder.ROOT_FULLNAME : parentId);
                    setName(dir.optString("name", null));
                }

                {
                    String createdAt = dir.optString("created_time", null);
                    if (null != createdAt) {
                        try {
                            creationDate = (ISO8601DateParser.parse(createdAt));
                        } catch (ParseException e) {
                            Logger logger = org.slf4j.LoggerFactory.getLogger(OneDriveFile.class);
                            logger.warn("Could not parse date from: {}", createdAt, e);
                        }
                    }
                }
                {
                    String modifiedAt = dir.optString("updated_time", null);
                    if (null != modifiedAt) {
                        try {
                            lastModifiedDate = (ISO8601DateParser.parse(modifiedAt));
                        } catch (ParseException e) {
                            Logger logger = org.slf4j.LoggerFactory.getLogger(OneDriveFile.class);
                            logger.warn("Could not parse date from: {}", modifiedAt, e);
                        }
                    }
                }

                setSubfolders(hasSubfolders);
                setSubscribedSubfolders(hasSubfolders);
            } catch (final RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }

    private static boolean isRootFolder(String id, String rootFolderId) {
        return "0".equals(id) || rootFolderId.equals(id);
    }

}
