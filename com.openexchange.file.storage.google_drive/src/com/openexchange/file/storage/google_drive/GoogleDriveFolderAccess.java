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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.file.storage.google_drive;

import static com.openexchange.java.Strings.isEmpty;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.google.api.client.http.HttpResponseException;
import com.google.api.services.drive.Drive;
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
import com.openexchange.file.storage.google_drive.access.GoogleDriveAccess;
import com.openexchange.session.Session;

/**
 * {@link GoogleDriveFolderAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GoogleDriveFolderAccess extends AbstractGoogleDriveAccess implements FileStorageFolderAccess {

    private static final String QUERY_STRING_DIRECTORIES_ONLY = GoogleDriveConstants.QUERY_STRING_DIRECTORIES_ONLY;

    // ---------------------------------------------------------------------------------------------------------------------- //

    private final GoogleDriveAccountAccess accountAccess;
    private final int userId;
    private final String accountDisplayName;

    /**
     * Initializes a new {@link GoogleDriveFolderAccess}.
     */
    public GoogleDriveFolderAccess(final GoogleDriveAccess googleDriveAccess, final FileStorageAccount account, final Session session, final GoogleDriveAccountAccess accountAccess) throws OXException {
        super(googleDriveAccess, account, session);
        this.accountAccess = accountAccess;
        userId = session.getUserId();
        accountDisplayName = account.getDisplayName();
    }

    private void checkDirValidity(com.google.api.services.drive.model.File file) throws OXException {
        if (!isDir(file)) {
            throw GoogleDriveExceptionCodes.NOT_A_FOLDER.create(file.getId());
        }
        checkIfTrashed(file);
    }

    private GoogleDriveFolder parseGoogleDriveFolder(com.google.api.services.drive.model.File dir, Drive drive) throws OXException, IOException {
        return new GoogleDriveFolder(userId).parseDirEntry(dir, rootFolderId, accountDisplayName, drive);
    }

    @Override
    public boolean exists(String folderId) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();
            com.google.api.services.drive.model.File file = drive.files().get(toGoogleDriveFolderId(folderId)).execute();
            Boolean explicitlyTrashed = file.getExplicitlyTrashed();
            return isDir(file) && (null == explicitlyTrashed || !explicitlyTrashed.booleanValue());
        } catch (final HttpResponseException e) {
            if (404 == e.getStatusCode()) {
                return false;
            }
            throw handleHttpResponseError(null, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();
            com.google.api.services.drive.model.File dir = drive.files().get(toGoogleDriveFolderId(folderId)).execute();
            checkDirValidity(dir);
            return parseGoogleDriveFolder(dir, drive);
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(folderId, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
    public FileStorageFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();

            Drive.Children.List list = drive.children().list(toGoogleDriveFolderId(parentIdentifier));
            list.setQ(QUERY_STRING_DIRECTORIES_ONLY);
            ChildList childList = list.execute();

            if (childList.getItems().isEmpty()) {
                return new FileStorageFolder[0];
            }

            List<FileStorageFolder> folders = new LinkedList<FileStorageFolder>();
            for (ChildReference childReference : childList.getItems()) {
                folders.add(parseGoogleDriveFolder(drive.files().get(childReference.getId()).execute(), drive));
            }

            String nextPageToken = list.getPageToken();
            while (!isEmpty(nextPageToken)) {
                list.setPageToken(nextPageToken);
                childList = list.execute();
                if (!childList.getItems().isEmpty()) {
                    for (ChildReference childReference : childList.getItems()) {
                        folders.add(parseGoogleDriveFolder(drive.files().get(childReference.getId()).execute(), drive));
                    }
                }

                nextPageToken = list.getPageToken();
            }

            return folders.toArray(new FileStorageFolder[0]);
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(parentIdentifier, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        return getFolder(FileStorageFolder.ROOT_FULLNAME);
    }

    @Override
    public String createFolder(final FileStorageFolder toCreate) throws OXException {
        String parentId = toGoogleDriveFolderId(toCreate.getParentId());
        try {
            Drive drive = googleDriveAccess.getDrive();

            Drive.Children.List list = drive.children().list(parentId);
            list.setQ("title='"+toCreate.getName()+"' and " + GoogleDriveConstants.QUERY_STRING_DIRECTORIES_ONLY_EXCLUDING_TRASH);
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
            throw handleHttpResponseError(toCreate.getParentId(), e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
        String fid = toGoogleDriveFolderId(folderId);
        String nfid = toGoogleDriveFolderId(newParentId);
        try {
            Drive drive = googleDriveAccess.getDrive();

            String title = null == newName ? drive.files().get(fid).execute().getTitle() : newName;

            Drive.Children.List list = drive.children().list(nfid);
            list.setQ("title='"+title+"' and " + GoogleDriveConstants.QUERY_STRING_DIRECTORIES_ONLY_EXCLUDING_TRASH);
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
            throw handleHttpResponseError(folderId, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        String fid = toGoogleDriveFolderId(folderId);
        try {
            Drive drive = googleDriveAccess.getDrive();

            Drive.Children.List list = drive.children().list(fid);
            list.setQ("title='"+newName+"' and " + GoogleDriveConstants.QUERY_STRING_DIRECTORIES_ONLY_EXCLUDING_TRASH);
            if (!list.execute().getItems().isEmpty()) {
                // Already such a folder
                throw FileStorageExceptionCodes.DUPLICATE_FOLDER.create(newName, drive.files().get(fid).execute().getTitle());
            }

            File driveDir = new File();
            driveDir.setId(fid);
            driveDir.setTitle(newName);
            driveDir.setMimeType(GoogleDriveConstants.MIME_TYPE_DIRECTORY);

            File renamedDir = drive.files().patch(fid, driveDir).execute();
            return toFileStorageFolderId(renamedDir.getId());
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(folderId, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        return deleteFolder(folderId, false);
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();

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
            throw handleHttpResponseError(folderId, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        clearFolder(folderId, false);
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();

            String fid = toGoogleDriveFolderId(folderId);
            if (hardDelete || isTrashed(fid, drive)) {
                // Delete permanently
                Drive.Children.List list = drive.children().list(fid);
                ChildList childList = list.execute();
                if (!childList.getItems().isEmpty()) {
                    for (ChildReference child : childList.getItems()) {
                        drive.files().delete(child.getId()).execute();
                    }

                    String nextPageToken = list.getPageToken();
                    while (!isEmpty(nextPageToken)) {
                        list.setPageToken(nextPageToken);
                        childList = list.execute();
                        if (!childList.getItems().isEmpty()) {
                            for (ChildReference child : childList.getItems()) {
                                drive.files().delete(child.getId()).execute();
                            }
                        }
                        nextPageToken = list.getPageToken();
                    }
                }
            } else {
                // Move to trash
                Drive.Children.List list = drive.children().list(fid);
                ChildList childList = list.execute();
                if (!childList.getItems().isEmpty()) {
                    for (ChildReference child : childList.getItems()) {
                        drive.files().trash(child.getId()).execute();
                    }

                    String nextPageToken = list.getPageToken();
                    while (!isEmpty(nextPageToken)) {
                        list.setPageToken(nextPageToken);
                        childList = list.execute();
                        if (!childList.getItems().isEmpty()) {
                            for (ChildReference child : childList.getItems()) {
                                drive.files().trash(child.getId()).execute();
                            }
                        }
                        nextPageToken = list.getPageToken();
                    }
                }
            }
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(folderId, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();

            List<FileStorageFolder> list = new LinkedList<FileStorageFolder>();

            String fid = toGoogleDriveFolderId(folderId);
            File dir = drive.files().get(fid).execute();
            FileStorageFolder f = parseGoogleDriveFolder(dir, drive);
            list.add(f);

            while (!rootFolderId.equals(fid)) {
                fid = dir.getParents().get(0).getId();
                dir = drive.files().get(fid).execute();
                f = parseGoogleDriveFolder(dir, drive);
                list.add(f);
            }

            return list.toArray(new FileStorageFolder[list.size()]);
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(folderId, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public Quota getStorageQuota(final String folderId) throws OXException {
        return Type.STORAGE.getUnlimited();
    }

    @Override
    public Quota getFileQuota(final String folderId) throws OXException {
        return Type.FILE.getUnlimited();
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        final Quota[] ret = new Quota[types.length];
        for (int i = 0; i < types.length; i++) {
            ret[i] = types[i].getUnlimited();
        }
        return ret;
    }

}
