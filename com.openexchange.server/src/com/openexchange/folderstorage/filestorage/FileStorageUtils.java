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

package com.openexchange.folderstorage.filestorage;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageFolderPermissionType;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStorageGuestPermission;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.folderstorage.BasicGuestPermission;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.FolderPermissionType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.type.DocumentsType;
import com.openexchange.folderstorage.type.MusicType;
import com.openexchange.folderstorage.type.PicturesType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.TemplatesType;
import com.openexchange.folderstorage.type.TrashType;
import com.openexchange.folderstorage.type.VideosType;

/**
 * {@link FileStorageUtils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class FileStorageUtils {

    /**
     * Gets a list of {@link FileStoragePermission}s representing the supplied folder storage permissions.
     *
     * @param permissions The permissions to convert
     * @return The corresponding file storage permissions
     */
    public static List<FileStoragePermission> getFileStoragePermissions(Permission[] permissions) {
        if (null == permissions) {
            return null;
        }
        List<FileStoragePermission> fileStoragePermissions = new ArrayList<FileStoragePermission>(permissions.length);
        for (Permission permission : permissions) {
            fileStoragePermissions.add(getFileStoragePermission(permission));
        }
        return fileStoragePermissions;
    }

    /**
     * Gets a {@link FileStoragePermission} representing the supplied folder storage permission.
     *
     * @param permission The permission to convert
     * @return The corresponding file storage permission
     */
    public static FileStoragePermission getFileStoragePermission(Permission permission) {
        if (null == permission) {
            return null;
        }
        DefaultFileStoragePermission fileStoragePermission = DefaultFileStoragePermission.newInstance();
        fileStoragePermission.setEntity(permission.getEntity());
        fileStoragePermission.setIdentifier(permission.getIdentifier());
        fileStoragePermission.setEntityInfo(permission.getEntityInfo());
        fileStoragePermission.setGroup(permission.isGroup());
        fileStoragePermission.setSystem(permission.getSystem());
        fileStoragePermission.setAdmin(permission.isAdmin());
        fileStoragePermission.setAllPermissions(permission.getFolderPermission(), permission.getReadPermission(), permission.getWritePermission(), permission.getDeletePermission());
        fileStoragePermission.setType(getFileStorageFolderPermissionType(permission.getType()));
        fileStoragePermission.setPermissionLegator(permission.getPermissionLegator());
        return (fileStoragePermission);
    }

    private static FileStorageFolderPermissionType getFileStorageFolderPermissionType(FolderPermissionType type) {
        if (null == type) {
            return null;
        }
        switch (type) {
            case INHERITED:
                return FileStorageFolderPermissionType.INHERITED;
            case LEGATOR:
                return FileStorageFolderPermissionType.LEGATOR;
            default:
                return FileStorageFolderPermissionType.NORMAL;
        }
    }

    /**
     * Determines the file storage folder type matching the supplied folderstorage type.
     *
     * @param type The folder storage type to get the file storage folder type for
     * @return The file storage folder type, or {@link FileStorageFolderType#NONE} if not matching folder type was detected
     */
    public static FileStorageFolderType getFileStorageFolderType(com.openexchange.folderstorage.Type type) {
        if (TrashType.getInstance().equals(type)) {
            return FileStorageFolderType.TRASH_FOLDER;
        }
        if (PublicType.getInstance().equals(type)) {
            return FileStorageFolderType.PUBLIC_FOLDER;
        }
        if (PicturesType.getInstance().equals(type)) {
            return FileStorageFolderType.PICTURES_FOLDER;
        }
        if (DocumentsType.getInstance().equals(type)) {
            return FileStorageFolderType.DOCUMENTS_FOLDER;
        }
        if (MusicType.getInstance().equals(type)) {
            return FileStorageFolderType.MUSIC_FOLDER;
        }
        if (VideosType.getInstance().equals(type)) {
            return FileStorageFolderType.VIDEOS_FOLDER;
        }
        if (TemplatesType.getInstance().equals(type)) {
            return FileStorageFolderType.TEMPLATES_FOLDER;
        }
        return FileStorageFolderType.NONE;
    }

    /**
     * Gets an array of {@link Permission}s representing the supplied file storage permissions.
     *
     * @param permissions The permissions to convert
     * @return The corresponding folder storage permissions
     */
    public static Permission[] getPermissions(List<FileStoragePermission> fileStoragePermissions) {
        if (null == fileStoragePermissions) {
            return null;
        }
        Permission[] permissions = new Permission[fileStoragePermissions.size()];
        for (int i = 0; i < fileStoragePermissions.size(); i++) {
            permissions[i] = getPermission(fileStoragePermissions.get(i));
        }
        return permissions;
    }

    /**
     * Gets a {@link Permission} representing the supplied file storage permission.
     *
     * @param permission The permission to convert
     * @return The corresponding folder storage permission
     */
    public static Permission getPermission(FileStoragePermission fileStoragePermission) {
        if (null == fileStoragePermission) {
            return null;
        }
        Permission permission;
        if (FileStorageGuestPermission.class.isInstance(fileStoragePermission)) {
            BasicGuestPermission guestPermission = new BasicGuestPermission();
            guestPermission.setRecipient(((FileStorageGuestPermission) fileStoragePermission).getRecipient());
            permission = guestPermission;
        } else {
            permission = new BasicPermission();
        }
        permission.setIdentifier(fileStoragePermission.getIdentifier());
        permission.setEntity(fileStoragePermission.getEntity());
        permission.setEntityInfo(fileStoragePermission.getEntityInfo());
        permission.setGroup(fileStoragePermission.isGroup());
        permission.setAdmin(fileStoragePermission.isAdmin());
        permission.setAllPermissions(fileStoragePermission.getFolderPermission(), fileStoragePermission.getReadPermission(),
            fileStoragePermission.getWritePermission(), fileStoragePermission.getDeletePermission());
        return permission;
    }

}
