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

package com.openexchange.file.storage.infostore.folder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.TypeAware;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.DocumentsType;
import com.openexchange.folderstorage.type.MusicType;
import com.openexchange.folderstorage.type.PicturesType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.TemplatesType;
import com.openexchange.folderstorage.type.TrashType;
import com.openexchange.folderstorage.type.VideosType;
import com.openexchange.i18n.LocaleTools;

/**
 * {@link UserizedFileStorageFolder}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class UserizedFileStorageFolder extends DefaultFileStorageFolder implements TypeAware {

    private final UserizedFolder folder;
    private FileStorageFolderType type;

    /**
     * Initializes a new {@link UserizedFileStorageFolder} from the supplied userized folder
     *
     * @param folder The userized folder to construct the file storage folder from
     */
    public UserizedFileStorageFolder(UserizedFolder folder) throws OXException {
        super();
        this.folder = folder;
        setCreationDate(folder.getCreationDateUTC());
        setDefaultFolder(folder.isDefault());
        setExists(true);
        setId(folder.getID());
        setLastModifiedDate(folder.getLastModifiedUTC());
        String defaultName = folder.getLocalizedName(LocaleTools.DEFAULT_LOCALE, folder.isAltNames());
        setName(null != defaultName ? defaultName : folder.getName());
        setParentId(folder.getParentID());
        setPermissions(parsePermission(folder.getPermissions()));
        setOwnPermission(parsePermission(folder.getOwnPermission()));
        setRootFolder(folder.getParentID() == null);
        setSubscribed(folder.isSubscribed());
        String[] subfolderIDs = folder.getSubfolderIDs();
        setSubfolders(subfolderIDs != null && subfolderIDs.length > 0);
        FolderID folderID = new FolderID(folder.getID());
        setType(getType(folder.getType()));
        setCreatedBy(folder.getCreatedBy());
        setModifiedBy(folder.getModifiedBy());
        /*
         * only assume all infostore capabilities if it's really an infostore folder
         */
        if (FileID.INFOSTORE_SERVICE_ID.equals(folderID.getService()) && FileID.INFOSTORE_ACCOUNT_ID.equals(folderID.getAccountId())) {
            setCapabilities(FileStorageFolder.ALL_CAPABILITIES);
        }
    }

    @Override
    public FileStorageFolderType getType() {
        return type;
    }

    /**
     * Sets the folder type.
     *
     * @param type The type to set
     */
    public void setType(FileStorageFolderType type) {
        this.type = type;
    }

    @Override
    public String getLocalizedName(Locale locale) {
        return folder.getLocalizedName(locale, folder.isAltNames());
    }

    @Override
    public String toString() {
        return "UserizedFileStorageFolder [id=" + id + ", name=" + name + "]";
    }

    /**
     * Parses given permission.
     *
     * @param permission The permission to parse
     * @return The parsed permission
     * @throws OXException If parsing fails
     */
    private static FileStoragePermission parsePermission(final Permission permission) throws OXException {
        if (null == permission) {
            return null;
        }
        try {
            final int entity = permission.getEntity();
            final DefaultFileStoragePermission oclPerm = DefaultFileStoragePermission.newInstance();
            oclPerm.setEntity(entity);
            oclPerm.setGroup(permission.isGroup());
            oclPerm.setAdmin(permission.isAdmin());
            oclPerm.setAllPermissions(
                permission.getFolderPermission(),
                permission.getReadPermission(),
                permission.getWritePermission(),
                permission.getDeletePermission());
                return (oclPerm);
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses given permissions.
     *
     * @param permissions The permissions to parse
     * @return The parsed permissions
     * @throws OXException If parsing fails
     */
    private static List<FileStoragePermission> parsePermission(Permission[] permissions) throws OXException {
        if (null == permissions || 0 == permissions.length) {
            return Collections.emptyList();
        }
        List<FileStoragePermission> perms = new ArrayList<FileStoragePermission>(permissions.length);
        for (Permission permission : permissions) {
            perms.add(parsePermission(permission));
        }
        return perms;
    }

    /**
     * Determines the file storage folder type matching the supplied folderstorage type.
     *
     * @param type The folder storage type to get the file storage folder type for
     * @return The file storage folder type, or {@link FileStorageFolderType#NONE} if not matching folder type was detected
     */
    private static FileStorageFolderType getType(com.openexchange.folderstorage.Type type) {
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

}
