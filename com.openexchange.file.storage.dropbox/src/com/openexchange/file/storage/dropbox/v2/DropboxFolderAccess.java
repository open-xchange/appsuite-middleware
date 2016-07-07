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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.LookupError;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.RelocationErrorException;
import com.dropbox.core.v2.users.SpaceUsage;
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
//TODO: Better exception handling
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
            // The Dropbox V2 API does not allow to fetch metadata for the root folder,
            // thus we assume that it always exists
            if (isRoot(folderId)) {
                return true;
            }
            Metadata metadata = getMetadata(folderId);
            return metadata instanceof FolderMetadata;
        } catch (GetMetadataErrorException e) {
            // TODO: Maybe introduce new exception codes?
            if (LookupError.NOT_FOUND.equals(e.errorValue.getPathValue())) {
                return false;
            }
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getFolder(java.lang.String)
     */
    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        try {
            FolderMetadata metadata = getFolderMetadata(folderId);
            // Check for sub folders
            boolean hasSubFolders = hasSubFolders(folderId);
            // Parse metadata
            return new DropboxFolder(metadata, userId, accountDisplayName, hasSubFolders);
        } catch (ListFolderErrorException e) {
            // TODO: Maybe introduce new exception codes?
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (GetMetadataErrorException e) {
            if (LookupError.NOT_FOUND.equals(e.errorValue.getPathValue())) {
                throw FileStorageExceptionCodes.NOT_FOUND.create(DropboxConstants.ID, folderId);
            }
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
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
            if (!exists(parentIdentifier)) {
                throw FileStorageExceptionCodes.NOT_FOUND.create(DropboxConstants.ID, parentIdentifier);
            }

            List<FileStorageFolder> folders = new LinkedList<FileStorageFolder>();
            ListFolderResult listFolder;
            String cursor = null;
            do {
                listFolder = (cursor != null) ? client.files().listFolderContinue(cursor) : client.files().listFolder(parentIdentifier);
                listFolder = client.files().listFolder(parentIdentifier);
                List<Metadata> entries = listFolder.getEntries();

                for (Metadata entry : entries) {
                    if (entry instanceof FolderMetadata) {
                        FolderMetadata folderMetadata = (FolderMetadata) entry;
                        folders.add(new DropboxFolder(folderMetadata, userId, accountDisplayName, hasSubFolders(folderMetadata.getPathDisplay())));
                    }
                }
                cursor = listFolder.getCursor();
            } while (listFolder.getHasMore());

            return folders.toArray(new FileStorageFolder[0]);
        } catch (ListFolderErrorException e) {
            // TODO: Maybe introduce new exception codes?
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
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
        try {
            String fullpath = constructPath(toCreate.getParentId(), toCreate.getName());
            FolderMetadata folderMetadata = client.files().createFolder(fullpath);
            return folderMetadata.getPathDisplay();
        } catch (CreateFolderErrorException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#updateFolder(java.lang.String, com.openexchange.file.storage.FileStorageFolder)
     */
    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        // TODO: Dropbox V2 API supports permissions for shared folders. Consider updating?
        //       More info: 
        //         - https://www.dropbox.com/developers/documentation/http/documentation#sharing-update_folder_member
        //         - https://www.dropbox.com/developers/documentation/http/documentation#sharing-update_folder_policy
        return identifier;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#moveFolder(java.lang.String, java.lang.String)
     */
    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        return moveFolder(folderId, newParentId, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#moveFolder(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        try {
            if (newName == null) {
                int lastIndex = folderId.lastIndexOf('/');
                newName = folderId.substring(lastIndex);
                newParentId += newName;
            }
            Metadata metadata = client.files().move(folderId, newParentId);
            return metadata.getPathDisplay();
        } catch (RelocationErrorException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#renameFolder(java.lang.String, java.lang.String)
     */
    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        try {
            int lastIndex = folderId.lastIndexOf('/');
            String newPath = folderId.substring(0, lastIndex + 1);
            newPath += newName;
            Metadata metadata = client.files().move(folderId, newPath);
            return metadata.getPathDisplay();
        } catch (RelocationErrorException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#deleteFolder(java.lang.String)
     */
    @Override
    public String deleteFolder(String folderId) throws OXException {
        try {
            Metadata metadata = client.files().delete(folderId);
            return metadata.getName();
        } catch (DeleteErrorException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#deleteFolder(java.lang.String, boolean)
     */
    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        return deleteFolder(folderId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#clearFolder(java.lang.String)
     */
    @Override
    public void clearFolder(String folderId) throws OXException {
        try {
            ListFolderResult listFolder = client.files().listFolder(folderId);
            for (Metadata entry : listFolder.getEntries()) {
                if (entry instanceof FolderMetadata) {
                    client.files().delete(entry.getPathDisplay());
                }
            }
        } catch (DeleteErrorException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (ListFolderErrorException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#clearFolder(java.lang.String, boolean)
     */
    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        clearFolder(folderId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getPath2DefaultFolder(java.lang.String)
     */
    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        try {
            FolderMetadata metadata = getFolderMetadata(folderId);
            List<FileStorageFolder> folders = new ArrayList<>();

            FileStorageFolder folder = new DropboxFolder(metadata, userId, accountDisplayName, hasSubFolders(folderId));
            folders.add(folder);
            String parentId;
            while ((parentId = folder.getParentId()) != null) {
                folder = getFolder(parentId);
                folders.add(folder);
            }
            return folders.toArray(new FileStorageFolder[folders.size()]);
        } catch (GetMetadataErrorException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getStorageQuota(java.lang.String)
     */
    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        try {
            SpaceUsage spaceUsage = client.users().getSpaceUsage();
            return new Quota(spaceUsage.getUsed(), spaceUsage.getAllocation().getIndividualValue().getAllocated() + spaceUsage.getAllocation().getTeamValue().getAllocated(), Type.STORAGE);
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getFileQuota(java.lang.String)
     */
    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        return Type.FILE.getUnlimited();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getQuotas(java.lang.String, com.openexchange.file.storage.Quota.Type[])
     */
    @Override
    public Quota[] getQuotas(String folder, Type[] types) throws OXException {
        if (null == types) {
            return null;
        }
        Quota[] quotas = new Quota[types.length];
        for (int i = 0; i < types.length; i++) {
            switch (types[i]) {
                case FILE:
                    quotas[i] = getFileQuota(folder);
                    break;
                case STORAGE:
                    quotas[i] = getStorageQuota(folder);
                    break;
                default:
                    throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create("Quota " + types[i]);
            }
        }
        return quotas;
    }

    /**
     * Check for sub folders
     * 
     * @param folderId
     * @return
     * @throws ListFolderErrorException
     * @throws DbxException
     */
    private boolean hasSubFolders(String folderId) throws ListFolderErrorException, DbxException {
        ListFolderResult listFolder = client.files().listFolder(folderId);
        List<Metadata> entries = listFolder.getEntries();
        boolean hasSubFolders = false;
        for (Metadata entry : entries) {
            hasSubFolders = entry instanceof FolderMetadata;
            if (hasSubFolders) {
                break;
            }
        }
        return hasSubFolders;
    }

    /**
     * Construct a full path from the specified parent and folder name.
     * It simply concatenates both strings by using the '/' path separator.
     * 
     * @param parent The parent folder
     * @param folder The folder name
     * @return The full path
     */
    private String constructPath(String parent, String folder) {
        if (isRoot(parent)) {
            parent = "/";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(parent);
        if (!parent.endsWith("/") || !folder.startsWith("/")) {
            builder.append("/");
        }
        builder.append(folder);
        return builder.toString();
    }

    /**
     * Gets the {@link FolderMetadata} of the specified folder.
     * 
     * @param folderId The folder identifier
     * @return The {@link FolderMetadata}
     * @throws GetMetadataErrorException If a metadata error is occurred
     * @throws DbxException If a generic Dropbox error is occurred
     * @throws OXException if the specified identifier does not denote a folder
     */
    private FolderMetadata getFolderMetadata(String folderId) throws GetMetadataErrorException, DbxException, OXException {
        // FIXME: How to handle the '/' folderId? 
        //        The Dropbox V2 API does not allow to fetch metadata for the root folder
        Metadata metadata = getMetadata(folderId);
        if (metadata instanceof FolderMetadata) {
            return (FolderMetadata) metadata;
        }
        throw FileStorageExceptionCodes.NOT_A_FOLDER.create(DropboxConstants.ID, folderId);
    }
}
