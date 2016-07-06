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

package com.openexchange.file.storage.dropbox.v2;

import java.util.LinkedList;
import java.util.List;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.dropbox.DropboxConstants;
import com.openexchange.file.storage.dropbox.access.DropboxOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link DropboxFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropboxFolderAccess extends AbstractDropboxAccess implements FileStorageFolderAccess {

    private final int userId;
    private final String accountDisplayName;

    /**
     * Initialises a new {@link DropboxFolderAccess}.
     */
    public DropboxFolderAccess(DropboxOAuthAccess dropboxOAuthAccess, FileStorageAccount account, Session session) {
        super(dropboxOAuthAccess, account, session);
        userId = session.getUserId();
        accountDisplayName = account.getDisplayName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#exists(java.lang.String)
     */
    @Override
    public boolean exists(String folderId) throws OXException {
        try {
            if ("/".equals(folderId)) {
                return true;
            }
            Metadata metadata = client.files().getMetadata(folderId);
            return metadata instanceof FolderMetadata;
        } catch (DbxException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getFolder(java.lang.String)
     */
    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        try {
            boolean exists = exists(folderId);
            if (!exists) {
                throw FileStorageExceptionCodes.NOT_FOUND.create(DropboxConstants.ID, folderId);
            }

            ListFolderResult listFolder = client.files().listFolder(folderId);
            List<Metadata> entries = listFolder.getEntries();

            Metadata metadata = client.files().getMetadata(folderId);
            return new DropboxFolder((FolderMetadata) metadata, userId, accountDisplayName);
        } catch (ListFolderErrorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GetMetadataErrorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DbxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getPersonalFolder()
     */
    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getTrashFolder()
     */
    @Override
    public FileStorageFolder getTrashFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getPublicFolders()
     */
    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getSubfolders(java.lang.String, boolean)
     */
    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        try {
            boolean exists = exists(parentIdentifier);
            if (!exists) {
                throw FileStorageExceptionCodes.NOT_FOUND.create(DropboxConstants.ID, parentIdentifier);
            }
            ListFolderResult listFolder = client.files().listFolder(parentIdentifier);
            List<Metadata> entries = listFolder.getEntries();
            List<FileStorageFolder> folders = new LinkedList<FileStorageFolder>();
            for (Metadata entry : entries) {
                if (entry instanceof FolderMetadata) {
                    folders.add(new DropboxFolder((FolderMetadata) entry, userId, accountDisplayName));
                }
            }
            return folders.toArray(new FileStorageFolder[0]);
        } catch (DbxException e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getUserSharedFolders()
     */
    @Override
    public FileStorageFolder[] getUserSharedFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getRootFolder()
     */
    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        DropboxFolder rootFolder = new DropboxFolder(userId);
        rootFolder.setRootFolder(true);
        rootFolder.setId(FileStorageFolder.ROOT_FULLNAME);
        rootFolder.setParentId(null);
        rootFolder.setName(accountDisplayName);
        rootFolder.setSubfolders(true);
        rootFolder.setSubscribedSubfolders(true);
        return rootFolder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#createFolder(com.openexchange.file.storage.FileStorageFolder)
     */
    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#updateFolder(java.lang.String, com.openexchange.file.storage.FileStorageFolder)
     */
    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#moveFolder(java.lang.String, java.lang.String)
     */
    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#moveFolder(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#renameFolder(java.lang.String, java.lang.String)
     */
    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#deleteFolder(java.lang.String)
     */
    @Override
    public String deleteFolder(String folderId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#deleteFolder(java.lang.String, boolean)
     */
    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#clearFolder(java.lang.String)
     */
    @Override
    public void clearFolder(String folderId) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#clearFolder(java.lang.String, boolean)
     */
    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getPath2DefaultFolder(java.lang.String)
     */
    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getStorageQuota(java.lang.String)
     */
    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getFileQuota(java.lang.String)
     */
    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getQuotas(java.lang.String, com.openexchange.file.storage.Quota.Type[])
     */
    @Override
    public Quota[] getQuotas(String folder, Type[] types) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

}
