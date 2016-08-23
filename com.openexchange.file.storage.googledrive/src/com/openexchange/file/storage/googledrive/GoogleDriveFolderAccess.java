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

package com.openexchange.file.storage.googledrive;

import static com.openexchange.java.Strings.isEmpty;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.googledrive.access.GoogleDriveOAuthAccess;
import com.openexchange.session.Session;

/**
 * {@link GoogleDriveFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GoogleDriveFolderAccess extends AbstractGoogleDriveAccess implements FileStorageFolderAccess {

    private static final String QUERY_STRING_DIRECTORIES_ONLY = GoogleDriveConstants.QUERY_STRING_DIRECTORIES_ONLY;

    // ---------------------------------------------------------------------------------------------------------------------- //

    private final int userId;
    private final String accountDisplayName;

    /**
     * Initializes a new {@link GoogleDriveFolderAccess}.
     */
    public GoogleDriveFolderAccess(final GoogleDriveOAuthAccess googleDriveAccess, final FileStorageAccount account, final Session session) throws OXException {
        super(googleDriveAccess, account, session);
        userId = session.getUserId();
        accountDisplayName = account.getDisplayName();
    }

    private void checkDirValidity(com.google.api.services.drive.model.File file) throws OXException {
        if (!isDir(file)) {
            throw FileStorageExceptionCodes.NOT_A_FOLDER.create(GoogleDriveConstants.ID, file.getId());
        }
        checkIfTrashed(file);
    }

    private GoogleDriveFolder parseGoogleDriveFolder(com.google.api.services.drive.model.File dir, Drive drive) throws OXException, IOException {
        return new GoogleDriveFolder(userId).parseDirEntry(dir, getRootFolderId(), accountDisplayName, drive);
    }

    @Override
    public boolean exists(String folderId) throws OXException {
        return exists(folderId, 0);
    }

    private boolean exists(String folderId, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.<Drive>getClient().client;
            com.google.api.services.drive.model.File file = drive.files().get(toGoogleDriveFolderId(folderId)).execute();
            Boolean explicitlyTrashed = file.getExplicitlyTrashed();
            return isDir(file) && (null == explicitlyTrashed || !explicitlyTrashed.booleanValue());
        } catch (final HttpResponseException e) {
            if (404 == e.getStatusCode()) {
                return false;
            }
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(null, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(null, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return exists(folderId, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        return getFolder(folderId, 0);
    }

    private FileStorageFolder getFolder(String folderId, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.<Drive>getClient().client;
            com.google.api.services.drive.model.File dir = drive.files().get(toGoogleDriveFolderId(folderId)).execute();
            checkDirValidity(dir);
            return parseGoogleDriveFolder(dir, drive);
        } catch (final HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(folderId, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(folderId, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return getFolder(folderId, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
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
        return getSubfolders(parentIdentifier, all, 0);
    }

    private FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.<Drive>getClient().client;

            Drive.Children.List list = drive.children().list(toGoogleDriveFolderId(parentIdentifier));
            list.setQ(GoogleDriveConstants.QUERY_STRING_DIRECTORIES_ONLY_EXCLUDING_TRASH);
            ChildList childList = list.execute();

            if (childList.getItems().isEmpty()) {
                return new FileStorageFolder[0];
            }

            List<FileStorageFolder> folders = new LinkedList<FileStorageFolder>();
            for (ChildReference childReference : childList.getItems()) {
                folders.add(parseGoogleDriveFolder(drive.files().get(childReference.getId()).execute(), drive));
            }

            String nextPageToken = childList.getNextPageToken();
            while (!isEmpty(nextPageToken)) {
                list.setPageToken(nextPageToken);
                childList = list.execute();
                if (!childList.getItems().isEmpty()) {
                    for (ChildReference childReference : childList.getItems()) {
                        folders.add(parseGoogleDriveFolder(drive.files().get(childReference.getId()).execute(), drive));
                    }
                }

                nextPageToken = childList.getNextPageToken();
            }

            return folders.toArray(new FileStorageFolder[0]);
        } catch (final HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(parentIdentifier, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(parentIdentifier, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return getSubfolders(parentIdentifier, all, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        GoogleDriveFolder rootFolder = new GoogleDriveFolder(userId);
        rootFolder.setRootFolder(true);
        rootFolder.setId(FileStorageFolder.ROOT_FULLNAME);
        rootFolder.setParentId(null);
        rootFolder.setName(accountDisplayName);
        rootFolder.setSubfolders(true);
        rootFolder.setSubscribedSubfolders(true);
        return rootFolder;
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        return createFolder(toCreate, 0);
    }

    private String createFolder(FileStorageFolder toCreate, int retryCount) throws OXException {
        String parentId = toGoogleDriveFolderId(toCreate.getParentId());
        try {
            Drive drive = googleDriveAccess.<Drive> getClient().client;

            Drive.Children.List list = drive.children().list(parentId);
            list.setQ("title='" + toCreate.getName() + "' and " + GoogleDriveConstants.QUERY_STRING_DIRECTORIES_ONLY_EXCLUDING_TRASH);
            if (!list.execute().getItems().isEmpty()) {
                // Already such a folder
                throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(toCreate.getName(), drive.files().get(parentId).execute().getTitle());
            }

            File driveDir = new File();
            driveDir.setParents(Collections.singletonList(new ParentReference().setId(parentId)));
            driveDir.setTitle(toCreate.getName());
            driveDir.setMimeType(GoogleDriveConstants.MIME_TYPE_DIRECTORY);

            File newDir = drive.files().insert(driveDir).execute();

            return toFileStorageFolderId(newDir.getId());
        } catch (final HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(toCreate.getParentId(), e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(toCreate.getParentId(), e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return createFolder(toCreate, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        // Neither support for subscription nor permissions
        return identifier;
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        return moveFolder(folderId, newParentId, null);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        return moveFolder(folderId, newParentId, newName, 0);
    }

    private String moveFolder(String folderId, String newParentId, String newName, int retryCount) throws OXException {
        String fid = toGoogleDriveFolderId(folderId);
        String nfid = toGoogleDriveFolderId(newParentId);
        try {
            Drive drive = googleDriveAccess.<Drive>getClient().client;

            String title = null == newName ? drive.files().get(fid).execute().getTitle() : newName;

            Drive.Children.List list = drive.children().list(nfid);
            list.setQ("title='" + title + "' and " + GoogleDriveConstants.QUERY_STRING_DIRECTORIES_ONLY_EXCLUDING_TRASH);
            if (!list.execute().getItems().isEmpty()) {
                // Already such a folder
                throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(title, drive.files().get(nfid).execute().getTitle());
            }

            File driveDir = new File();
            driveDir.setId(fid);
            driveDir.setParents(Collections.singletonList(new ParentReference().setId(nfid)));
            if (null != newName) {
                driveDir.setTitle(newName);
            }
            driveDir.setMimeType(GoogleDriveConstants.MIME_TYPE_DIRECTORY);

            File movedDir = drive.files().patch(fid, driveDir).execute();
            return toFileStorageFolderId(movedDir.getId());
        } catch (final HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(folderId, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(folderId, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return moveFolder(folderId, newParentId, newName, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        return renameFolder(folderId, newName, 0);
    }

    private String renameFolder(String folderId, String newName, int retryCount) throws OXException {
        String fid = toGoogleDriveFolderId(folderId);
        try {
            Drive drive = googleDriveAccess.<Drive>getClient().client;
            /*
             * get folder to rename
             */
            File folder = drive.files().get(fid).setFields("parents/id,title").execute();
            if (newName.equals(folder.getTitle())) {
                return folderId;
            }
            /*
             * check for name conflict below parent folder
             */
            List<ParentReference> parentReferences = folder.getParents();
            for (ParentReference parentReference : parentReferences) {
                Drive.Children.List list = drive.children().list(parentReference.getId());
                list.setQ("title='" + newName + "' and " + GoogleDriveConstants.QUERY_STRING_DIRECTORIES_ONLY_EXCLUDING_TRASH);
                if (false == list.execute().getItems().isEmpty()) {
                    throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(newName, drive.files().get(parentReference.getId()).execute().getTitle());
                }
            }
            /*
             * perform rename
             */
            File driveDir = new File().setId(fid).setTitle(newName).setMimeType(GoogleDriveConstants.MIME_TYPE_DIRECTORY);
            File renamedDir = drive.files().patch(fid, driveDir).execute();
            return toFileStorageFolderId(renamedDir.getId());
        } catch (final HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(folderId, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(folderId, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return renameFolder(folderId, newName, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        return deleteFolder(folderId, false);
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        return deleteFolder(folderId, hardDelete, 0);
    }

    private String deleteFolder(String folderId, boolean hardDelete, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.<Drive> getClient().client;

            String fid = toGoogleDriveFolderId(folderId);
            if (hardDelete || isTrashed(fid, drive)) {
                drive.files().delete(fid).execute();
            } else {
                drive.files().trash(fid).execute();
            }

            return folderId;
        } catch (final HttpResponseException e) {
            if (404 == e.getStatusCode()) {
                return folderId;
            }

            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(folderId, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(folderId, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return deleteFolder(folderId, hardDelete, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        clearFolder(folderId, false);
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        clearFolder(folderId, hardDelete, 0);
    }

    private void clearFolder(String folderId, boolean hardDelete, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.<Drive> getClient().client;
            /*
             * build request to list all files in a folder
             */
            String fid = toGoogleDriveFolderId(folderId);
            boolean deletePermanently = hardDelete || isTrashed(fid, drive);
            com.google.api.services.drive.Drive.Children.List listRequest = drive.children().list(fid).setQ(GoogleDriveConstants.QUERY_STRING_FILES_ONLY).setFields("nextPageToken,items(id)");
            /*
             * execute as often as needed & delete files
             */
            ChildList childList;
            do {
                childList = listRequest.execute();
                for (ChildReference child : childList.getItems()) {
                    if (deletePermanently) {
                        drive.files().delete(child.getId()).execute();
                    } else {
                        drive.files().trash(child.getId()).execute();
                    }
                }
                listRequest.setPageToken(childList.getNextPageToken());
            } while (null != childList.getNextPageToken());
        } catch (final HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(folderId, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(folderId, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            clearFolder(folderId, hardDelete, retry);
            return;
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        return getPath2DefaultFolder(folderId, 0);
    }

    private FileStorageFolder[] getPath2DefaultFolder(String folderId, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.<Drive>getClient().client;

            List<FileStorageFolder> list = new LinkedList<FileStorageFolder>();

            String fid = toGoogleDriveFolderId(folderId);
            File dir = drive.files().get(fid).execute();
            FileStorageFolder f = parseGoogleDriveFolder(dir, drive);
            list.add(f);

            String rootFolderId = getRootFolderId();
            while (!rootFolderId.equals(fid)) {
                fid = dir.getParents().get(0).getId();
                dir = drive.files().get(fid).execute();
                f = parseGoogleDriveFolder(dir, drive);
                list.add(f);
            }

            return list.toArray(new FileStorageFolder[list.size()]);
        } catch (final HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(folderId, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(folderId, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return getPath2DefaultFolder(folderId, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        return getStorageQuota(folderId, 0);
    }

    private Quota getStorageQuota(String folderId, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.<Drive>getClient().client;
            About about = drive.about().get().setFields("quotaType,quotaBytesUsed,quotaBytesTotal").execute();
            if ("UNLIMITED".equals(about.getQuotaType())) {
                return Type.STORAGE.getUnlimited();
            }
            return new Quota(about.getQuotaBytesTotal(), about.getQuotaBytesUsed(), Type.STORAGE);
        } catch (HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(folderId, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(folderId, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return getStorageQuota(folderId, retry);
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
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
