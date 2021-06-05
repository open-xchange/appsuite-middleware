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

package com.openexchange.file.storage.googledrive;

import java.io.IOException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.openexchange.annotation.NonNull;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.googledrive.access.GoogleDriveOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link AbstractGoogleDriveAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractGoogleDriveAccess {

    private static final String MIME_TYPE_DIRECTORY = GoogleDriveConstants.MIME_TYPE_DIRECTORY;
    // private static final String QUERY_STRING_DIRECTORIES_ONLY = GoogleDriveConstants.QUERY_STRING_DIRECTORIES_ONLY;

    protected final @NonNull GoogleDriveOAuthAccess googleDriveAccess;

    protected final @NonNull Session session;

    protected final @NonNull FileStorageAccount account;

    protected String rootFolderIdentifier;

    /**
     * Initializes a new {@link AbstractGoogleDriveAccess}.
     *
     * @param googleDriveAccess The {@link GoogleDriveAccountAccess} to use
     * @param account The {@link FileStorageAccount}
     * @param session The {@link Session}
     */
    protected AbstractGoogleDriveAccess(final @NonNull GoogleDriveOAuthAccess googleDriveAccess, final @NonNull FileStorageAccount account, final @NonNull Session session) {
        super();
        this.googleDriveAccess = googleDriveAccess;
        this.account = account;
        this.session = session;
    }

    /**
     * Gets the root folder identifier
     *
     * @return The root folder identifier
     * @throws OXException If root folder cannot be returned
     */
    protected String getRootFolderId() throws OXException {
        String rootFolderId = rootFolderIdentifier;
        if (null == rootFolderId) {
            String key = "com.openexchange.file.storage.googledrive.rootFolderId/" + account.getId();
            rootFolderId = (String) session.getParameter(key);
            if (null == rootFolderId) {
                try {
                    Drive drive = googleDriveAccess.<Drive> getClient().client;
                    rootFolderId = drive.files().get("root").execute().getId();
                    session.setParameter(key, rootFolderId);
                } catch (IOException e) {
                    throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }
            }
            rootFolderIdentifier = rootFolderId;
        }
        return rootFolderId;
    }

    /**
     * Checks if given Google Drive resource is trashed.
     *
     * @param id The Google Drive identifier
     * @param drive The drive reference
     * @return <code>true</code> if trashed; otherwise <code>false</code>
     * @throws IOException If check fails
     */
    protected static boolean isTrashed(String id, Drive drive) throws IOException {
        File file = drive.files().get(id).setFields("trashed").execute();
        if (null == file.getTrashed()) {
            return false;
        }
        return file.getTrashed().booleanValue();
    }

    /**
     * Checks if given file/directory is trashed
     *
     * @param file The file/directory to check
     * @throws OXException If file/directory is trashed
     */
    protected void checkIfTrashed(com.google.api.services.drive.model.File file) throws OXException {
        Boolean explicitlyTrashed = file.getExplicitlyTrashed();
        if (null != explicitlyTrashed && explicitlyTrashed.booleanValue()) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(file.getId(), "");
        }
    }

    /**
     * Moves the given file to the trash
     *
     * @param drive The client API
     * @param fileId The identifier of the file to trash
     * @throws IOException On error
     */
    protected void trashFile(Drive drive, String fileId) throws IOException {
        drive.files().update(fileId, new com.google.api.services.drive.model.File().setTrashed(Boolean.TRUE)).execute();
    }

    /**
     * Checks if given file is a directory.
     *
     * @param file The file to check
     * @return <code>true</code> if file is a directory, otherwise <code>false</code>
     */
    protected static boolean isDir(File file) {
        return MIME_TYPE_DIRECTORY.equals(file.getMimeType());
    }

    /**
     * Gets the Google Drive folder identifier from given file storage folder identifier
     *
     * @param folderId The file storage folder identifier
     * @return The appropriate Google Drive folder identifier
     * @throws OXException If operation fails
     */
    protected String toGoogleDriveFolderId(String folderId) throws OXException {
        return FileStorageFolder.ROOT_FULLNAME.equals(folderId) ? getRootFolderId() : folderId;
    }

    /**
     * Gets the file storage folder identifier from given Google Drive folder identifier
     *
     * @param googleId The Google Drive folder identifier
     * @return The appropriate file storage folder identifier
     * @throws OXException If operation fails
     */
    protected String toFileStorageFolderId(String googleId) throws OXException {
        return getRootFolderId().equals(googleId) || "root".equals(googleId) ? FileStorageFolder.ROOT_FULLNAME : googleId;
    }

}
