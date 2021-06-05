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
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;
import com.openexchange.microsoft.graph.onedrive.MicrosoftGraphDriveService;
import com.openexchange.microsoft.graph.onedrive.OneDriveFolder;
import com.openexchange.microsoft.graph.onedrive.exception.MicrosoftGraphDriveServiceExceptionCodes;
import com.openexchange.session.Session;

/**
 * {@link OneDriveFolderAccess} - Just a light-weighted proxy that bridges the Infostore and
 * the real service that handles the actual requests, {@link MicrosoftGraphDriveService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class OneDriveFolderAccess extends AbstractOneDriveResourceAccess implements FileStorageFolderAccess, FileStorageCaseInsensitiveAccess, FileStorageAutoRenameFoldersAccess, UserCreatedFileStorageFolderAccess {

    private final String accountDisplayName;

    /**
     * Initializes a new {@link OneDriveFolderAccess}.
     * 
     * @throws OXException if the {@link MicrosoftGraphDriveService} is absent
     */
    public OneDriveFolderAccess(OneDriveOAuthAccess oneDriveAccess, FileStorageAccount account, Session session) throws OXException {
        super(oneDriveAccess, session);
        accountDisplayName = account.getDisplayName();
    }

    @Override
    public boolean exists(final String folderId) throws OXException {
        return perform(new OneDriveClosure<Boolean>() {

            @Override
            protected Boolean doPerform() throws OXException {
                return Autoboxing.valueOf(driveService.existsFolder(getAccessToken(), toOneDriveFolderId(folderId)));
            }
        }).booleanValue();
    }

    @Override
    public FileStorageFolder getFolder(final String folderId) throws OXException {
        return perform(new OneDriveClosure<FileStorageFolder>() {

            @Override
            protected FileStorageFolder doPerform() throws OXException {
                return driveService.getFolder(session.getUserId(), getAccessToken(), toOneDriveFolderId(folderId));
            }
        });
    }

    @Override
    public FileStorageFolder getPersonalFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder getTrashFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder[] getPublicFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    @Override
    public FileStorageFolder[] getUserSharedFolders() throws OXException {
        return new FileStorageFolder[0];
    }

    @Override
    public FileStorageFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        return perform(new OneDriveClosure<FileStorageFolder[]>() {

            @Override
            protected FileStorageFolder[] doPerform() throws OXException {
                return driveService.getSubFolders(session.getUserId(), getAccessToken(), toOneDriveFolderId(parentIdentifier)).toArray(new FileStorageFolder[0]);
            }
        });
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        OneDriveFolder root = driveService.getRootFolder(session.getUserId(), getAccessToken());
        root.setName(accountDisplayName);
        return root;
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        return createFolder(toCreate, true);
    }

    @Override
    public String createFolder(final FileStorageFolder toCreate, final boolean autoRename) throws OXException {
        return perform(new OneDriveClosure<String>() {

            @Override
            protected String doPerform() throws OXException {
                try {
                    // No need to use a {@link NameBuilder} in this case. The Microsoft Graph API already provides
                    // the autorename functionality.
                    return driveService.createFolder(session.getUserId(), getAccessToken(), toCreate.getName(), toOneDriveFolderId(toCreate.getParentId()), autoRename).getId();
                } catch (OXException e) {
                    if (MicrosoftGraphDriveServiceExceptionCodes.FOLDER_ALREADY_EXISTS.equals(e)) {
                        throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(e, toCreate.getName(), toCreate.getParentId());
                    }
                    throw e;
                }
            }
        });
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        // Support for neither subscription nor permissions.
        return identifier;
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        return moveFolder(folderId, newParentId, null);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        return moveFolder(folderId, newParentId, newName, true);
    }

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
                    folderName = driveService.getFolder(session.getUserId(), getAccessToken(), oneDriveFolderId).getName();
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

    @Override
    public String deleteFolder(String folderId) throws OXException {
        return deleteFolder(folderId, false);
    }

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

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(final String folderId) throws OXException {
        return perform(new OneDriveClosure<FileStorageFolder[]>() {

            @Override
            protected FileStorageFolder[] doPerform() throws OXException {
                List<FileStorageFolder> list = new LinkedList<>();

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

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        return perform(new OneDriveClosure<Quota>() {

            @Override
            protected Quota doPerform() throws OXException {
                return driveService.getQuota(getAccessToken());
            }
        });
    }

    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        return Type.FILE.getUnlimited();
    }

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
