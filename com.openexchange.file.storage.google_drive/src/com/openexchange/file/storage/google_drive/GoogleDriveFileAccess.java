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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.file.storage.google_drive.access.GoogleDriveAccess;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link GoogleDriveFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GoogleDriveFileAccess extends AbstractGoogleDriveAccess implements ThumbnailAware {

    private static final String QUERY_STRING_FILES_ONLY = GoogleDriveConstants.QUERY_STRING_FILES_ONLY;
    private static final String QUERY_STRING_FILES_ONLY_EXCLUDING_TRASH = GoogleDriveConstants.QUERY_STRING_FILES_ONLY_EXCLUDING_TRASH;

    // ----------------------------------------------------------------------------------------------------------------------- //

    private final GoogleDriveAccountAccess accountAccess;
    private final int userId;

    /**
     * Initializes a new {@link GoogleDriveFileAccess}.
     */
    public GoogleDriveFileAccess(GoogleDriveAccess googleDriveAccess, FileStorageAccount account, Session session, GoogleDriveAccountAccess accountAccess) throws OXException {
        super(googleDriveAccess, account, session);
        this.accountAccess = accountAccess;
        userId = session.getUserId();
    }

    private void checkFileValidity(com.google.api.services.drive.model.File file) throws OXException {
        if (isDir(file)) {
            throw GoogleDriveExceptionCodes.NOT_A_FILE.create(file.getId());
        }
        checkIfTrashed(file);
    }

    @Override
    public void startTransaction() throws OXException {
        // Nope
    }

    @Override
    public void commit() throws OXException {
        // Nope
    }

    @Override
    public void rollback() throws OXException {
        // Nope
    }

    @Override
    public void finish() throws OXException {
        // Nope
    }

    @Override
    public void setTransactional(final boolean transactional) {
        // Nope
    }

    @Override
    public void setRequestTransactional(final boolean transactional) {
        // Nope
    }

    @Override
    public void setCommitsTransaction(final boolean commits) {
        // Nope
    }

    @Override
    public boolean exists(final String folderId, final String id, final String version) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();
            com.google.api.services.drive.model.File file = drive.files().get(id).execute();
            Boolean explicitlyTrashed = file.getExplicitlyTrashed();
            return !isDir(file) && (null == explicitlyTrashed || !explicitlyTrashed.booleanValue());
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
    public File getFileMetadata(final String folderId, final String id, final String version) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();
            com.google.api.services.drive.model.File file = drive.files().get(id).execute();
            checkFileValidity(file);
            return new GoogleDriveFile(folderId, id, userId, rootFolderId).parseGoogleDriveFile(file);
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(id, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void saveFileMetadata(final File file, final long sequenceNumber) throws OXException {
        saveFileMetadata(file, sequenceNumber, null);
    }

    @Override
    public void saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        if (null == modifiedFields || modifiedFields.contains(Field.FILENAME)) {
            try {
                Drive drive = googleDriveAccess.getDrive();

                com.google.api.services.drive.model.File modFile = new com.google.api.services.drive.model.File();
                modFile.setId(file.getId());
                modFile.setTitle(file.getFileName());

                drive.files().patch(file.getId(), modFile).execute();
            } catch (final HttpResponseException e) {
                throw handleHttpResponseError(file.getId(), e);
            } catch (final IOException e) {
                throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } catch (final RuntimeException e) {
                throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }

    @Override
    public IDTuple copy(IDTuple source, String version, String destFolder, File update, InputStream newFil, List<Field> modifiedFields) throws OXException {
        if (version != CURRENT_VERSION) {
            // can only copy the current revision
            throw GoogleDriveExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
        }
        String id = source.getId();
        try {
            Drive drive = googleDriveAccess.getDrive();

            // Get source file
            com.google.api.services.drive.model.File srcFile = drive.files().get(id).execute();
            checkFileValidity(srcFile);

            // Determine destination identifier
            String destId = toGoogleDriveFolderId(destFolder);

            // Check destination folder
            String title = srcFile.getTitle();
            {
                String baseName;
                String ext;
                {
                    int dotPos = title.lastIndexOf('.');
                    if (dotPos > 0) {
                        baseName = title.substring(0, dotPos);
                        ext = title.substring(dotPos);
                    } else {
                        baseName = title;
                        ext = "";
                    }
                }
                int count = 1;
                boolean keepOn = true;
                while (keepOn) {
                    Drive.Children.List list = drive.children().list(destId);
                    list.setQ(new StringBuilder().append("title = '").append(title).append("' and ").append(QUERY_STRING_FILES_ONLY_EXCLUDING_TRASH).toString());

                    ChildList childList = list.execute();
                    if (!childList.getItems().isEmpty()) {
                        title = new StringBuilder(baseName).append(" (").append(count++).append(')').append(ext).toString();
                    } else {
                        keepOn = false;
                    }
                }
            }

            // Create a file at destination directory
            com.google.api.services.drive.model.File copy = new com.google.api.services.drive.model.File();
            copy.setTitle(title);
            copy.setParents(Collections.<ParentReference> singletonList(new ParentReference().setId(destId)));

            // Copy file
            com.google.api.services.drive.model.File copiedFile = drive.files().copy(id, copy).execute();

            return new IDTuple(destFolder, copiedFile.getId());
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(id, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<File.Field> modifiedFields) throws OXException {
        String id = source.getId();
        try {
            Drive drive = googleDriveAccess.getDrive();

            // Get source file
            com.google.api.services.drive.model.File srcFile = drive.files().get(id).execute();
            checkFileValidity(srcFile);

            // Determine destination identifier
            String destId = toGoogleDriveFolderId(destFolder);

            // Check destination folder
            {
                for (ParentReference parentReference : srcFile.getParents()) {
                    if (parentReference.getId().equals(destId)) {
                        return source;
                    }
                }
            }

            // Create patch file
            com.google.api.services.drive.model.File patch = new com.google.api.services.drive.model.File();
            patch.setParents(Collections.<ParentReference> singletonList(new ParentReference().setId(destFolder)));

            // Patch the file
            com.google.api.services.drive.model.File patchedFile = drive.files().patch(id, patch).execute();

            return new IDTuple(destFolder, patchedFile.getId());
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(id, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public InputStream getDocument(final String folderId, final String id, final String version) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();

            // Get file
            com.google.api.services.drive.model.File file = drive.files().get(id).execute();
            checkFileValidity(file);

            String downloadUrl = file.getDownloadUrl();
            if (downloadUrl == null || downloadUrl.length() <= 0) {
                // The file doesn't have any content stored on Drive.
                throw GoogleDriveExceptionCodes.NO_CONTENT.create(id);
            }

            HttpResponse resp = drive.getRequestFactory().buildGetRequest(new GenericUrl(downloadUrl)).execute();
            return resp.getContent();
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(id, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public InputStream getThumbnailStream(String folderId, String id, String version) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();

            // Get file
            com.google.api.services.drive.model.File file = drive.files().get(id).execute();
            checkFileValidity(file);

            String thumbnailLink = file.getThumbnailLink();
            if (thumbnailLink == null || thumbnailLink.length() <= 0) {
                // The file doesn't have a thumbnail
                return null;
            }

            HttpResponse resp = drive.getRequestFactory().buildGetRequest(new GenericUrl(thumbnailLink)).execute();
            return resp.getContent();
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(id, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        saveDocument(file, data, sequenceNumber, null);
    }

    @Override
    public void saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        String id = file.getId();
        try {
            Drive drive = googleDriveAccess.getDrive();

            if (isEmpty(id) || !exists(null, id, CURRENT_VERSION)) {
                // Insert
                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setTitle(file.getFileName());
                // Determine folder identifier
                String folderId = FileStorageFolder.ROOT_FULLNAME.equals(file.getFolderId()) ? rootFolderId : file.getFolderId();
                fileMetadata.setParents(Collections.<ParentReference> singletonList(new ParentReference().setId(folderId)));

                Drive.Files.Insert insert = drive.files().insert(fileMetadata, new InputStreamContent(file.getFileMIMEType(), data));
                MediaHttpUploader uploader = insert.getMediaHttpUploader();
                uploader.setDirectUploadEnabled(true);
                String newId = insert.execute().getId();
                file.setId(newId);
            } else {
                // Update
                com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
                fileMetadata.setId(id);
                fileMetadata.setTitle(file.getFileName());
                // Determine folder identifier
                String folderId = FileStorageFolder.ROOT_FULLNAME.equals(file.getFolderId()) ? rootFolderId : file.getFolderId();
                fileMetadata.setParents(Collections.<ParentReference> singletonList(new ParentReference().setId(folderId)));

                Drive.Files.Update update = drive.files().update(id, fileMetadata, new InputStreamContent(file.getFileMIMEType(), data));
                MediaHttpUploader uploader = update.getMediaHttpUploader();
                uploader.setDirectUploadEnabled(true);
                update.execute();
            }
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(id, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();

            // Determine folder identifier
            String fid = toGoogleDriveFolderId(folderId);

            // Query all files
            Drive.Children.List list = drive.children().list(fid);
            list.setQ(QUERY_STRING_FILES_ONLY_EXCLUDING_TRASH);

            boolean hardDelete = isTrashed(fid, drive);

            ChildList childList = list.execute();
            if (!childList.getItems().isEmpty()) {
                for (ChildReference child : childList.getItems()) {
                    if (hardDelete) {
                        drive.files().delete(child.getId()).execute();
                    } else {
                        drive.files().trash(child.getId()).execute();
                    }
                }

                String nextPageToken = list.getPageToken();
                while (!isEmpty(nextPageToken)) {
                    list.setPageToken(nextPageToken);
                    childList = list.execute();
                    if (!childList.getItems().isEmpty()) {
                        for (ChildReference child : childList.getItems()) {
                            if (hardDelete) {
                                drive.files().delete(child.getId()).execute();
                            } else {
                                drive.files().trash(child.getId()).execute();
                            }
                        }
                    }

                    nextPageToken = list.getPageToken();
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
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber, boolean hardDelete) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();

            List<IDTuple> ret = new ArrayList<IDTuple>(ids.size());
            for (IDTuple id : ids) {
                try {
                    boolean delete = hardDelete || isTrashed(toGoogleDriveFolderId(id.getFolder()), drive);
                    if (delete) {
                        drive.files().delete(id.getId()).execute();
                    } else {
                        drive.files().trash(id.getId()).execute();
                    }
                } catch (final HttpResponseException e) {
                    if (404 != e.getStatusCode()) {
                        ret.add(id);
                    } else {
                        throw e;
                    }
                }
            }
            return ret;
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(null, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String[] removeVersion(String folderId, String id, String[] versions) throws OXException {
        /*
         * No versioning support
         */
        for (final String version : versions) {
            if (version != CURRENT_VERSION) {
                throw GoogleDriveExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
            }
        }
        try {
            Drive drive = googleDriveAccess.getDrive();
            // Determine folder identifier
            String fid = toGoogleDriveFolderId(folderId);
            drive.children().delete(fid, id).execute();
            return new String[0];
        } catch (final HttpResponseException e) {
            if (404 == e.getStatusCode()) {
                return new String[0];
            }
            throw handleHttpResponseError(null, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void unlock(String folderId, String id) throws OXException {
        // Nope
    }

    @Override
    public void lock(String folderId, String id, long diff) throws OXException {
        // Nope
    }

    @Override
    public void touch(String folderId, String id) throws OXException {
        exists(folderId, id, CURRENT_VERSION);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();

            // Determine folder identifier
            String fid = toGoogleDriveFolderId(folderId);

            Drive.Children.List list = drive.children().list(fid);
            list.setQ(QUERY_STRING_FILES_ONLY_EXCLUDING_TRASH);
            ChildList childList = list.execute();

            List<File> files = new LinkedList<File>();
            if (!childList.getItems().isEmpty()) {
                for (ChildReference child : childList.getItems()) {
                    String fileId = child.getId();
                    files.add(new GoogleDriveFile(folderId, fileId, userId, rootFolderId).parseGoogleDriveFile(drive.files().get(fileId).execute()));
                }

                String nextPageToken = list.getPageToken();
                while (!isEmpty(nextPageToken)) {
                    list.setPageToken(nextPageToken);
                    childList = list.execute();
                    if (!childList.getItems().isEmpty()) {
                        for (ChildReference child : childList.getItems()) {
                            String fileId = child.getId();
                            files.add(new GoogleDriveFile(folderId, fileId, userId, rootFolderId).parseGoogleDriveFile(drive.files().get(fileId).execute()));
                        }
                    }

                    nextPageToken = list.getPageToken();
                }
            }

            return new FileTimedResult(files);
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(folderId, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields) throws OXException {
        return getDocuments(folderId);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();

            // Determine folder identifier
            String fid = toGoogleDriveFolderId(folderId);

            Drive.Children.List list = drive.children().list(fid);
            list.setQ(QUERY_STRING_FILES_ONLY_EXCLUDING_TRASH);
            ChildList childList = list.execute();

            List<File> files = new LinkedList<File>();
            if (!childList.getItems().isEmpty()) {
                for (ChildReference child : childList.getItems()) {
                    String fileId = child.getId();
                    files.add(new GoogleDriveFile(folderId, fileId, userId, rootFolderId).parseGoogleDriveFile(drive.files().get(fileId).execute()));
                }

                String nextPageToken = list.getPageToken();
                while (!isEmpty(nextPageToken)) {
                    list.setPageToken(nextPageToken);
                    childList = list.execute();
                    if (!childList.getItems().isEmpty()) {
                        for (ChildReference child : childList.getItems()) {
                            String fileId = child.getId();
                            files.add(new GoogleDriveFile(folderId, fileId, userId, rootFolderId).parseGoogleDriveFile(drive.files().get(fileId).execute()));
                        }
                    }

                    nextPageToken = list.getPageToken();
                }
            }

            // Sort collection if needed
            sort(files, sort, order);
            return new FileTimedResult(files);
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(folderId, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();
            List<File> files = Collections.<File> singletonList(new GoogleDriveFile(folderId, id, userId, rootFolderId).parseGoogleDriveFile(drive.files().get(id).execute()));
            return new FileTimedResult(files);
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(id, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields) throws OXException {
        return getVersions(folderId, id);
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();
            List<File> files = Collections.<File> singletonList(new GoogleDriveFile(folderId, id, userId, rootFolderId).parseGoogleDriveFile(drive.files().get(id).execute()));
            return new FileTimedResult(files);
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(id, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public TimedResult<File> getDocuments(List<IDTuple> ids, List<Field> fields) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();

            List<File> files = new LinkedList<File>();
            for (IDTuple idTuple : ids) {
                String fileId = idTuple.getId();
                try {
                    files.add(new GoogleDriveFile(idTuple.getFolder(), fileId, userId, rootFolderId).parseGoogleDriveFile(drive.files().get(fileId).execute()));
                } catch (HttpResponseException e) {
                    if (404 == e.getStatusCode()) {
                        throw GoogleDriveExceptionCodes.NOT_FOUND.create(e, fileId);
                    }
                    throw e;
                }
            }

            return new FileTimedResult(files);
        } catch (HttpResponseException e) {
            throw handleHttpResponseError(null, e);
        } catch (IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final SearchIterator<File> EMPTY_ITER = SearchIteratorAdapter.emptyIterator();

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    @Override
    public SearchIterator<File> search(List<String> folderIds, SearchTerm<?> searchTerm, List<Field> fields, Field sort, final SortDirection order, int start, int end) throws OXException {
        if (FileNameTerm.class.isInstance(searchTerm) && null == folderIds || 1 == folderIds.size()) {
            String pattern = ((FileNameTerm) searchTerm).getPattern();
            return search(pattern, fields, null != folderIds && 1 == folderIds.size() ? folderIds.get(0) : null, sort, order, start, end);
        }
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create("Search term not supported: " + searchTerm);
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();

            if (isEmpty(pattern)) {
                // Get all files
                List<File> files = new LinkedList<File>();
                {
                    Drive.Files.List list = drive.files().list();
                    list.setQ(QUERY_STRING_FILES_ONLY_EXCLUDING_TRASH);

                    FileList fileList = list.execute();
                    if (!fileList.getItems().isEmpty()) {
                        for (com.google.api.services.drive.model.File file : fileList.getItems()) {
                            files.add(new GoogleDriveFile(folderId, file.getId(), userId, rootFolderId).parseGoogleDriveFile(file));
                        }

                        String nextPageToken = list.getPageToken();
                        while (!isEmpty(nextPageToken)) {
                            list.setPageToken(nextPageToken);
                            fileList = list.execute();
                            if (!fileList.getItems().isEmpty()) {
                                for (com.google.api.services.drive.model.File file : fileList.getItems()) {
                                    files.add(new GoogleDriveFile(folderId, file.getId(), userId, rootFolderId).parseGoogleDriveFile(file));
                                }
                            }

                            nextPageToken = list.getPageToken();
                        }
                    }
                }

                // Sort collection
                sort(files, sort, order);
                if ((start != NOT_SET) && (end != NOT_SET)) {
                    final int size = files.size();
                    if ((start) > size) {
                        /*
                         * Return empty iterator if start is out of range
                         */
                        return SearchIteratorAdapter.emptyIterator();
                    }
                    /*
                     * Reset end index if out of range
                     */
                    int toIndex = end;
                    if (toIndex >= size) {
                        toIndex = size;
                    }
                    files = files.subList(start, toIndex);
                }

                return new SearchIteratorAdapter<File>(files.iterator(), files.size());
            }

            // Search by pattern
            List<File> files = new LinkedList<File>();
            {
                Drive.Children.List list = drive.children().list(toGoogleDriveFolderId(folderId));
                list.setQ(new StringBuilder().append("title contains '").append(pattern).append("' and ").append(QUERY_STRING_FILES_ONLY).toString());

                ChildList fileList = list.execute();
                if (!fileList.getItems().isEmpty()) {
                    for (ChildReference childRef : fileList.getItems()) {
                        String fileId = childRef.getId();
                        files.add(new GoogleDriveFile(folderId, fileId, userId, rootFolderId).parseGoogleDriveFile(drive.files().get(fileId).execute()));
                    }

                    String nextPageToken = list.getPageToken();
                    while (!isEmpty(nextPageToken)) {
                        list.setPageToken(nextPageToken);
                        fileList = list.execute();
                        if (!fileList.getItems().isEmpty()) {
                            for (ChildReference childRef : fileList.getItems()) {
                                String fileId = childRef.getId();
                                files.add(new GoogleDriveFile(folderId, fileId, userId, rootFolderId).parseGoogleDriveFile(drive.files().get(fileId).execute()));
                            }
                        }

                        nextPageToken = list.getPageToken();
                    }
                }
            }

            // Sort collection
            sort(files, sort, order);
            if ((start != NOT_SET) && (end != NOT_SET)) {
                final int size = files.size();
                if ((start) > size) {
                    /*
                     * Return empty iterator if start is out of range
                     */
                    return SearchIteratorAdapter.emptyIterator();
                }
                /*
                 * Reset end index if out of range
                 */
                int toIndex = end;
                if (toIndex >= size) {
                    toIndex = size;
                }
                files = files.subList(start, toIndex);
            }
            return new SearchIteratorAdapter<File>(files.iterator(), files.size());
        } catch (final HttpResponseException e) {
            throw handleHttpResponseError(null, e);
        } catch (final IOException e) {
            throw GoogleDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

    /**
     * Sorts the supplied list of files if needed.
     *
     * @param files The files to sort
     * @param sort The sort order, or <code>null</code> if not specified
     * @param order The sort direction
     */
    private static void sort(List<File> files, Field sort, SortDirection order) {
        if (null != sort && 1 < files.size()) {
            Collections.sort(files, order.comparatorBy(sort));
        }
    }

}
