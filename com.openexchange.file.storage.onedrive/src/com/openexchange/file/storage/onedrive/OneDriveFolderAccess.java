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

import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAutoRenameFoldersAccess;
import com.openexchange.file.storage.FileStorageCaseInsensitiveAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.NameBuilder;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.UserCreatedFileStorageFolderAccess;
import com.openexchange.file.storage.onedrive.access.OneDriveOAuthAccess;
import com.openexchange.file.storage.onedrive.osgi.Services;
import com.openexchange.java.Strings;
import com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService;
import com.openexchange.microsoft.graph.onedrive.OneDriveFolder;
import com.openexchange.microsoft.graph.onedrive.exception.MicrosoftGraphDriveServiceExceptionCodes;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;

/**
 * {@link OneDriveFolderAccess} - Just a light-weighted proxy that bridges the Infostore and the real
 * the real service that handles the actual requests, {@link MicrosoftGraphDriveService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class OneDriveFolderAccess extends AbstractOneDriveResourceAccess implements FileStorageFolderAccess, FileStorageCaseInsensitiveAccess, FileStorageAutoRenameFoldersAccess, UserCreatedFileStorageFolderAccess {

    private final int userId;
    private final String accountDisplayName;
    private final MicrosoftGraphDriveService driveService;

    /**
     * Initializes a new {@link OneDriveFolderAccess}.
     */
    public OneDriveFolderAccess(final OneDriveOAuthAccess oneDriveAccess, final FileStorageAccount account, final Session session, final OneDriveAccountAccess accountAccess) throws OXException {
        super(oneDriveAccess, account, session);
        userId = session.getUserId();
        accountDisplayName = account.getDisplayName();
        driveService = Services.getOptionalService(MicrosoftGraphDriveService.class);
        if (driveService == null) {
            throw ServiceExceptionCode.absentService(MicrosoftGraphDriveService.class);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#exists(java.lang.String)
     */
    @Override
    public boolean exists(final String folderId) throws OXException {
        return perform(new OneDriveClosure<Boolean>() {

            @Override
            protected Boolean doPerform() throws OXException {
                return driveService.existsFolder(getAccessToken(), toOneDriveFolderId(folderId));
            }
        }).booleanValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getFolder(java.lang.String)
     */
    @Override
    public FileStorageFolder getFolder(final String folderId) throws OXException {
        return perform(new OneDriveClosure<FileStorageFolder>() {

            @Override
            protected FileStorageFolder doPerform() throws OXException {
                return driveService.getFolder(userId, getAccessToken(), toOneDriveFolderId(folderId));
            }
        });
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
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getUserSharedFolders()
     */
    @Override
    public FileStorageFolder[] getUserSharedFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getSubfolders(java.lang.String, boolean)
     */
    @Override
    public FileStorageFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        return perform(new OneDriveClosure<FileStorageFolder[]>() {

            @Override
            protected FileStorageFolder[] doPerform() throws OXException {
                return driveService.getSubFolders(userId, getAccessToken(), toOneDriveFolderId(parentIdentifier)).toArray(new FileStorageFolder[0]);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getRootFolder()
     */
    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        OneDriveFolder root = driveService.getRootFolder(userId, getAccessToken());
        root.setName(accountDisplayName);
        return root;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#createFolder(com.openexchange.file.storage.FileStorageFolder)
     */
    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        return createFolder(toCreate, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageAutoRenameFoldersAccess#createFolder(com.openexchange.file.storage.FileStorageFolder, boolean)
     */
    @Override
    public String createFolder(final FileStorageFolder toCreate, final boolean autoRename) throws OXException {
        return perform(new OneDriveClosure<String>() {

            @Override
            protected String doPerform() throws OXException {
                try {
                    // No need to use a {@link NameBuilder} in this case. The Microsoft Graph API already provides
                    // the autorename functionality.
                    return driveService.createFolder(userId, getAccessToken(), toCreate.getName(), toOneDriveFolderId(toCreate.getParentId()), autoRename).getId();
                } catch (OXException e) {
                    if (MicrosoftGraphDriveServiceExceptionCodes.FOLDER_ALREADY_EXISTS.equals(e)) {
                        throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(e, toCreate.getName(), toCreate.getParentId());
                    }
                    throw e;
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#updateFolder(java.lang.String, com.openexchange.file.storage.FileStorageFolder)
     */
    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        // Support for neither subscription nor permissions.
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
        return moveFolder(folderId, newParentId, newName, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageAutoRenameFoldersAccess#moveFolder(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public String moveFolder(String folderId, String newParentId, String newName, boolean autoRename) throws OXException {
        return perform(new OneDriveClosure<String>() {

            @Override
            protected String doPerform() throws OXException {
                String oneDriveFolderId = toOneDriveFolderId(folderId);
                String oneDriveParentId = toOneDriveFolderId(newParentId);
                if (!autoRename) {
                    if (Strings.isEmpty(newName)) {
                        return driveService.moveFolder(getAccessToken(), oneDriveFolderId, oneDriveParentId);
                    }
                    return driveService.moveFolder(getAccessToken(), oneDriveFolderId, oneDriveParentId, newName);
                }
                String folderName = newName;
                if (Strings.isEmpty(folderName)) {
                    folderName = driveService.getFolder(userId, getAccessToken(), oneDriveFolderId).getName();
                }
                NameBuilder name = new NameBuilder(folderName);
                while (true) {
                    try {
                        return driveService.moveFolder(getAccessToken(), oneDriveFolderId, oneDriveParentId, name.toString());
                    } catch (OXException e) {
                        if (MicrosoftGraphDriveServiceExceptionCodes.FOLDER_ALREADY_EXISTS.equals(e)) {
                            name.advance();
                            continue;
                        }
                        throw e;
                    }
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#renameFolder(java.lang.String, java.lang.String)
     */
    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        return perform(new OneDriveClosure<String>() {

            @Override
            protected String doPerform() throws OXException {
                try {
                    driveService.renameFolder(getAccessToken(), toOneDriveFolderId(folderId), newName);
                    return folderId;
                } catch (OXException e) {
                    if (false == MicrosoftGraphDriveServiceExceptionCodes.FOLDER_ALREADY_EXISTS.equals(e)) {
                        throw e;
                    }
                    FileStorageFolder folder = getFolder(folderId);
                    FileStorageFolder parent = getFolder(folder.getParentId());
                    throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(e, newName, parent.getName());
                }
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#deleteFolder(java.lang.String)
     */
    @Override
    public String deleteFolder(String folderId) throws OXException {
        return deleteFolder(folderId, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#deleteFolder(java.lang.String, boolean)
     */
    @Override
    public String deleteFolder(final String folderId, boolean hardDelete) throws OXException {
        return perform(new OneDriveClosure<String>() {

            @Override
            protected String doPerform() throws OXException {
                driveService.deleteFolder(getAccessToken(), toOneDriveFolderId(folderId));
                return folderId;
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#clearFolder(java.lang.String)
     */
    @Override
    public void clearFolder(String folderId) throws OXException {
        clearFolder(folderId, false);
    }

    @Override
    public void clearFolder(final String folderId, boolean hardDelete) throws OXException {
        perform(new OneDriveClosure<Void>() {

            @Override
            protected Void doPerform() throws OXException {
                driveService.clearFolder(getAccessToken(), toOneDriveFolderId(folderId));
                return null;
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getPath2DefaultFolder(java.lang.String)
     */
    @Override
    public FileStorageFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        return perform(new OneDriveClosure<FileStorageFolder[]>() {

            @Override
            protected FileStorageFolder[] doPerform() throws OXException {
                List<FileStorageFolder> list = new LinkedList<FileStorageFolder>();

                String fid = folderId;
                FileStorageFolder f = getFolder(fid);
                list.add(f);

                while (!FileStorageFolder.ROOT_FULLNAME.equals(fid)) {
                    fid = f.getParentId();
                    f = getFolder(fid);
                    list.add(f);
                }
                return list.toArray(new FileStorageFolder[list.size()]);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFolderAccess#getStorageQuota(java.lang.String)
     */
    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        return perform(new OneDriveClosure<Quota>() {

            @Override
            protected Quota doPerform() throws OXException {
                return driveService.getQuota(getAccessToken());
            }
        });
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
}
