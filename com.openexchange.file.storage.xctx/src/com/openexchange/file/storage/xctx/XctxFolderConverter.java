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

package com.openexchange.file.storage.xctx;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.infostore.folder.FolderConverter;
import com.openexchange.file.storage.infostore.folder.UserizedFileStorageFolder;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.EntityInfo;
import com.openexchange.session.Session;

/**
 * {@link XctxFolderConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class XctxFolderConverter extends FolderConverter {
    
    private final Session guestSession;
    private final Session localSession;
    private final EntityHelper entityHelper;

    /**
     * Initializes a new {@link XctxFolderConverter}.
     * 
     * @param accountAccess The parent account access
     * @param localSession The user's <i>local</i> session associated with the file storage account
     * @param guestSession The <i>remote</i> session of the guest user used to access the contents of the foreign context
     */
    public XctxFolderConverter(XctxAccountAccess accountAccess, Session localSession, Session guestSession) {
        super();
        this.guestSession = guestSession;
        this.localSession = localSession;
        this.entityHelper = new EntityHelper(accountAccess);
    }

    @Override
    public UserizedFileStorageFolder getStorageFolder(UserizedFolder folder) throws OXException {
        /*
         * get storage folder with entities under perspective of remote guest session in foreign context
         */
        UserizedFileStorageFolder storageFolder = super.getStorageFolder(folder);
        storageFolder.setCacheable(Boolean.FALSE);
        /*
         * enhance & qualify remote entities for usage in local session in storage account's context & erase ambiguous numerical identifiers
         */
        storageFolder.setCreatedFrom(entityHelper.mangleRemoteEntity(null == folder.getCreatedFrom() && 0 < folder.getCreatedBy() ? 
            entityHelper.optEntityInfo(guestSession, folder.getCreatedBy(), false) : folder.getCreatedFrom()));
        storageFolder.setCreatedBy(0);        
        storageFolder.setModifiedFrom(entityHelper.mangleRemoteEntity(null == folder.getModifiedFrom() && 0 < folder.getModifiedBy() ? 
            entityHelper.optEntityInfo(guestSession, folder.getModifiedBy(), false) : folder.getModifiedFrom()));
        storageFolder.setModifiedBy(0);
        /*
         * exchange remote guest user id with local session user's id in own permissions
         */
        FileStoragePermission ownStoragePermission = DefaultFileStoragePermission.newInstance(storageFolder.getOwnPermission());
        ownStoragePermission.setEntity(localSession.getUserId());
        ownStoragePermission = entityHelper.addPermissionEntityInfo(localSession, ownStoragePermission);
        storageFolder.setOwnPermission(ownStoragePermission);
        /*
         * enhance & qualify remote entities in folder permissions for usage in local session in storage account's context
         */
        List<FileStoragePermission> permissions = entityHelper.addPermissionEntityInfos(guestSession, storageFolder.getPermissions());
        storageFolder.setPermissions(entityHelper.mangleRemotePermissions(permissions));
        /*
         * insert user's own permission as system permission to ensure folder is considered as visible for the local session user throughout the stack
         */
        DefaultFileStoragePermission systemPermission = DefaultFileStoragePermission.newInstance(ownStoragePermission);
        systemPermission.setSystem(1);
        storageFolder.addPermission(systemPermission);
        /*
         * adjust capabilities from remote folder & clear type to disable certain functionality
         */
        storageFolder.setCapabilities(getStorageCapabilities(storageFolder.getCapabilities()));
        storageFolder.setType(FileStorageFolderType.NONE); // if not: parent folder may get exchanged to 15 or 10 at c.o.folderstorage.filestorage.FileStorageFolderImpl.FileStorageFolderImpl(FileStorageFolder, String, boolean, IDBasedFolderAccess)  
        return storageFolder;
    }

    @Override
    public Folder getFolder(FileStorageFolder storageFolder) throws OXException {
        /*
         * get folder with entities under perspective of local session in storage account's context
         */
        Folder folder = super.getFolder(storageFolder);
        /*
         * restore previously mangled entities for context of guest session
         */
        EntityInfo remoteCreatedFrom = entityHelper.unmangleLocalEntity(folder.getCreatedFrom());
        folder.setCreatedFrom(remoteCreatedFrom);
        folder.setCreatedBy(null != remoteCreatedFrom ? remoteCreatedFrom.getEntity() : 0);
        EntityInfo remoteModifiedFrom = entityHelper.unmangleLocalEntity(folder.getModifiedFrom());
        folder.setModifiedFrom(remoteModifiedFrom);
        folder.setModifiedBy(null != remoteModifiedFrom ? remoteModifiedFrom.getEntity() : 0);
        /*
         * restore previously adjusted entities in permissions for context of guest session
         */
        folder.setPermissions(entityHelper.unmangleLocalPermissions(folder.getPermissions()));
        return folder;
    }

    private static Set<String> getStorageCapabilities(Set<String> capabilities) {
        if (null == capabilities) {
            return null;
        }
        HashSet<String> storageCapabilities = new HashSet<String>(capabilities);
        storageCapabilities.remove(FileStorageFolder.CAPABILITY_SUBSCRIPTION);
        storageCapabilities.remove(FileStorageFolder.CAPABILITY_PERMISSIONS);
        return storageCapabilities;
    }

}
