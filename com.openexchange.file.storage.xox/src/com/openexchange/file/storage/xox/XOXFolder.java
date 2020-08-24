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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.xox;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.api.client.common.calls.folders.RemoteFolder;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.TypeAware;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;

/**
 * {@link XOXFolder} - A folder shared from another OX instance
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class XOXFolder extends DefaultFileStorageFolder implements TypeAware {

    private FileStorageFolderType type;

    /**
     * Initializes a new {@link XOXFolder}.
     *
     * @param userId The ID of the owner
     */
    public XOXFolder(final int userId) {
        this(userId, null);
    }

    /**
     * Initializes a new {@link XOXFolder}.
     *
     * @param userId The ID of the owner
     * @param other The {@link RemoteFolder} to copy the values from
     */
    public XOXFolder(final int userId, final RemoteFolder other) {

        id = other == null || other.getID() == null ? FileStorageFolder.ROOT_FULLNAME : other.getID();
        name = other != null ? other.getName() : null;
        type = FileStorageFolderType.NONE;
        exists = true;

        holdsFiles = true;
        b_holdsFiles = true;

        holdsFolders = true;
        b_holdsFolders = true;

        subscribed = true; //TODO
        b_subscribed = true; //TODO

        creationDate = other != null ? other.getCreationDate() : null;
        lastModifiedDate = other != null ? other.getLastModified() : null;

        if (other != null && other.getParentID() != null) {
            setParentId(other.getParentID());
        }
        if (other != null) {
            setSubfolders(other.hasSubfolders());
        } else {
            setSubfolders(true);
        }
        setCapabilities(FileStorageFolder.ALL_CAPABILITIES);
        setSubscribedSubfolders(true);

        //own permissions: Adopt the permissions the user has as guest
        final DefaultFileStoragePermission ownFolderPermission = DefaultFileStoragePermission.newInstance();
        ownFolderPermission.setEntity(userId);
        if (other != null && other.containsOwnRights()) {
            int[] permissionBits = Permissions.parsePermissionBits(other.getOwnRights());
            ownFolderPermission.setFolderPermission(permissionBits[0]);
            ownFolderPermission.setReadPermission(permissionBits[1]);
            ownFolderPermission.setWritePermission(permissionBits[2]);
            ownFolderPermission.setDeletePermission(permissionBits[3]);
            ownFolderPermission.setAdmin(permissionBits[4] > 0 ? true : false);
        }
        ownPermission = ownFolderPermission;

        //Set permissions for other entities
        if (other != null && other.getPermissions() != null && other.getPermissions().length > 0) {
            List<FileStoragePermission> remotePermissions = new ArrayList<>(other.getPermissions().length);
            for (Permission p : other.getPermissions()) {
                DefaultFileStoragePermission permission = DefaultFileStoragePermission.newInstance();
                //TODO MW-1380 MW-1401 Resolve remote entity
                permission.setEntity(p.getEntity());
                permission.setFolderPermission(p.getFolderPermission());
                permission.setGroup(permission.isGroup());
                remotePermissions.add(permission);
            }
            //TODO MW-1380 MW-1401 Set as extended permission
        }

        //ownPermission might get recalculated by the caller based on the existing permissions so we add the own permissions to the list
        addPermission(ownPermission);

        createdBy = 0;
        //TODO MW1380 MW-1401 set extendedCreateBy

        modifiedBy = 0;
        //TODO MW1380 MW-1401 set extendedModifiedBy
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
    public XOXFolder setType(FileStorageFolderType type) {
        this.type = type;
        return this;
    }
}
