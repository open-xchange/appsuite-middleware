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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.file.storage.copycom;

import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONObject;
import com.copy.api.Folder;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.TypeAware;

/**
 * {@link CopyComFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CopyComFolder extends DefaultFileStorageFolder implements TypeAware {

    private static final String TYPE_FOLDER = CopyComConstants.TYPE_FOLDER;

    private FileStorageFolderType type;

    /**
     * Initializes a new {@link CopyComFolder}.
     */
    public CopyComFolder(final int userId) {
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
    public CopyComFolder setType(FileStorageFolderType type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return id == null ? super.toString() : id;
    }

    /**
     * Parses specified Copy.com directory.
     *
     * @param dir The Copy.com directory
     * @param rootFolderId The identifier of the root folder
     * @param accountDisplayName The account's display name
     * @throws OXException If parsing Copy.com directory fails
     */
    public CopyComFolder parseDirEntry(Folder dir, String rootFolderId, String accountDisplayName) throws OXException {
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
                    String parentId;
                    {
                        int pos = id.lastIndexOf('/');
                        parentId = pos > 0 ? id.substring(0, pos) : null;
                    }
                    setParentId(isRootFolder(parentId, rootFolderId) ? FileStorageFolder.ROOT_FULLNAME : parentId);
                    setName(dir.getName());
                }

                boolean hasSubfolders = false;
                for (Object child : dir.getChildren()) {
                    if (child instanceof Folder) {
                        hasSubfolders = true;
                        break;
                    }
                }

                setSubfolders(hasSubfolders);
                setSubscribedSubfolders(hasSubfolders);
            } catch (final RuntimeException e) {
                throw CopyComExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }

    /**
     * Parses specified Copy.com directory.
     *
     * @param dir The Copy.com directory
     * @param rootFolderId The identifier of the root folder
     * @param accountDisplayName The account's display name
     * @throws OXException If parsing Copy.com directory fails
     */
    public CopyComFolder parseDirEntry(JSONObject dir, String rootFolderId, String accountDisplayName) throws OXException {
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
                    String parentId;
                    {
                        int pos = id.lastIndexOf('/');
                        parentId = pos > 0 ? id.substring(0, pos) : null;
                    }
                    setParentId(isRootFolder(parentId, rootFolderId) ? FileStorageFolder.ROOT_FULLNAME : parentId);
                    setName(dir.optString("name", null));
                }

                boolean hasSubfolders = false;
                JSONArray children = dir.optJSONArray("children");
                if (null != children && !children.isEmpty()) {
                    int length = children.length();
                    for (int i = 0; i < length; i++) {
                        JSONObject child = children.optJSONObject(i);
                        String type = (String) child.opt("type");
                        if ("dir".equals(type)) {
                            hasSubfolders = true;
                            break;
                        }
                    }
                }

                setSubfolders(hasSubfolders);
                setSubscribedSubfolders(hasSubfolders);
            } catch (final RuntimeException e) {
                throw CopyComExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return this;
    }

    private static boolean isRootFolder(String id, String rootFolderId) {
        return rootFolderId.equals(id);
    }

}
