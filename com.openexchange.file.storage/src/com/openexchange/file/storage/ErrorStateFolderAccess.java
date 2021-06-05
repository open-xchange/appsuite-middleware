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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Quota.Type;

/**
 * {@link ErrorStateFolderAccess} - A {@link FileStorageFolderAccess} implementation which can be used in case of an account error.
 * <p>
 * If the real folder storage is known to be in an error state, this implementation will, at least, return the last known folders.
 * This is useful to serve them to a client but tag them as defective.
 * </p>
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public abstract class ErrorStateFolderAccess implements FileStorageFolderAccess {

    private final OXException error;
    private final FileStorageAccount account;

    /**
     * {@link FileStorageFolderStub} represents a folder which is defective and will not be cached
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v7.10.5
     */
    public static class FileStorageFolderStub extends DefaultFileStorageFolder implements CacheAware {

        private OXException accountError;

        @Override
        public boolean cacheable() {
            //Do not cache corrupt folders
            return false;
        }

        @Override
        public OXException getAccountError() {
            return accountError;
        }

        /**
         * Sets the account error as {@link OXException}
         *
         * @param accountError The account error as {@link OXException}
         */
        public void setAccountError(OXException accountError) {
            this.accountError = accountError;
        }
    }

    /**
     * Initializes a new {@link ErrorStateFolderAccess}.
     *
     * @param account The {@link FileStorageAccount}
     * @param error The current problem preventing to query the remote folders
     * @param getFolderFunction A function which will be used to retrieve the last known folder, when loaded
     */
    //@formatter:off
    public ErrorStateFolderAccess(FileStorageAccount account, OXException error) {
        this.account = Objects.requireNonNull(account, "account must not be null");
        this.error = Objects.requireNonNull(error, "error must not be null");
    }
    //@formatter:on

    /**
     * Gets the last known folder with the given folder ID
     *
     * @param folderId The ID of the folder to get
     * @return The folder with the given ID, or null if no such folder was found
     * @throws OXException
     */
    public abstract FileStorageFolderStub getLastKnownFolder(String folderId) throws OXException;

    /**
     * Gets the last known sub-folders for the given parent folder ID
     *
     * @param folderId The ID of the folder to get the last sub-folders for
     * @return The last known subfolders for the given ID
     * @throws OXException
     */
    public abstract FileStorageFolderStub[] getLastKnownSubFolders(String folderId) throws OXException;

    /**
     * Updates a last known folder
     *
     * @param folderId The folder to update
     * @param ignoreWarnings <code>true</code> in order to ignore all warnings, <code>false</code> to raise warnings
     * @param toUpdate The file storage folder to update containing only the modified fields
     * @return The ID of the updated folder, or null in case the folder could not be updated
     * @throws OXException
     */
    public abstract FileStorageResult<String> updateLastKnownFolder(FileStorageFolder folder, boolean ignoreWarnings, FileStorageFolder toUpdate) throws OXException;

    /**
     * Returns a list of visible/subscribed root-subfolders
     *
     * @return A list of subscribed root subfolders
     * @throws OXException
     */
    protected List<FileStorageFolder> getVisibleRootFolders() throws OXException {
        List<FileStorageFolder> ret = new ArrayList<>();
        FileStorageFolder[] f1 = getSubfolders("10", false);
        if (f1 != null) {
            java.util.Collections.addAll(ret, f1);
        }
        FileStorageFolder[] f2 = getSubfolders("15", false);
        if (f2 != null) {
            java.util.Collections.addAll(ret, f2);
        }
        return ret;
    }

    @Override
    public boolean exists(String folderId) throws OXException {
        return getFolder(folderId) != null;
    }

    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        FileStorageFolderStub lastKnownFolder = getLastKnownFolder(folderId);
        if (lastKnownFolder == null) {
            throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
        }

        //Set the last known error as "account error" when returning the folder
        lastKnownFolder.setAccountError(error);
        return lastKnownFolder;
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
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        FileStorageFolderStub[] subfolders = getLastKnownSubFolders(parentIdentifier);
        return all ? subfolders : Arrays.asList(subfolders).stream().filter(f -> f.subscribed).toArray(FileStorageFolder[]::new);
    }

    @Override
    public FileStorageFolder[] getUserSharedFolders() throws OXException {
        throw error;
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        throw error;
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        throw error;
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        FileStorageResult<String> updateFolder = updateFolder(identifier, true, toUpdate);
        if(updateFolder != null) {
            return updateFolder.getResponse();
        }
        return null;
    }

    @Override
    public FileStorageResult<String> updateFolder(String identifier, boolean ignoreWarnings, FileStorageFolder toUpdate) throws OXException {
        /*
         * We are unable to perform the update in case of an error; except it is an persistent known folder
         */
        //Subscription update
        FileStorageFolderStub lastKnownFolder = getLastKnownFolder(identifier);
        if(lastKnownFolder != null) {
            //Updating a known folder;
             FileStorageResult<String> updateLastKnownFolder = updateLastKnownFolder(lastKnownFolder, ignoreWarnings, toUpdate);
            //The actual implementation decides whether or not the data can be updated
            //in the persistent "last known" version of a folder
            if(updateLastKnownFolder != null) {
                return updateLastKnownFolder;
            }
        }
        //..throw an exception otherwise
        throw error;
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        throw error;
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        throw error;
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        throw error;
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        throw error;
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        throw error;
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        throw error;
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        throw error;
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        //In case of an error state, we can only serve the path if the folder is part of the "last known folders".
        FileStorageFolderStub lastKnownFolder = getLastKnownFolder(folderId);
        if(lastKnownFolder != null) {
            DefaultFileStorageFolder defaultFolder = new DefaultFileStorageFolder();
            defaultFolder.setId("10");
            FileStorageFolder[] ret = new FileStorageFolder[]{lastKnownFolder, defaultFolder};
            return ret;
        }
        //..throw an exception otherwise
        throw error;
    }

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        throw error;
    }

    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        throw error;
    }

    @Override
    public Quota[] getQuotas(String folder, Type[] types) throws OXException {
        throw error;
    }
}
