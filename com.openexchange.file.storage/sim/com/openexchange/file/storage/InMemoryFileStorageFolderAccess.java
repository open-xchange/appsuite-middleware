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

package com.openexchange.file.storage;

import com.openexchange.exception.OXException;

/**
 * {@link InMemoryFileStorageFolderAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class InMemoryFileStorageFolderAccess extends AbstractFileStorageFolderAccess {

    @Override
    public boolean exists(String folderId) throws OXException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileStorageFolder getTrashFolder() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

}
