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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.file.storage.google_drive.access.GoogleDriveAccess;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link GoogleDriveFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GoogleDriveFileAccess extends AbstractGoogleDriveAccess implements ThumbnailAware {

    private final GoogleDriveAccountAccess accountAccess;
    private final int userId;

    /**
     * Initializes a new {@link GoogleDriveFileAccess}.
     */
    public GoogleDriveFileAccess(GoogleDriveAccess googleDriveAccess, FileStorageAccount account, Session session, GoogleDriveAccountAccess accountAccess) {
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

    private void checkIfTrashed(com.google.api.services.drive.model.File file) throws OXException {
        Boolean explicitlyTrashed = file.getExplicitlyTrashed();
        if (null != explicitlyTrashed && explicitlyTrashed.booleanValue()) {
            throw GoogleDriveExceptionCodes.NOT_FOUND.create(file.getId());
        }
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
            return new GoogleDriveFile(folderId, id, userId).parseGoogleDriveFile(file);
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
    public void saveFileMetadata(final File file, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        saveDocument(file, Streams.newByteArrayInputStream(new byte[0]), sequenceNumber, modifiedFields);
    }

    @Override
    public IDTuple copy(final IDTuple source, String version, final String destFolder, final File update, final InputStream newFil, final List<Field> modifiedFields) throws OXException {
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
                    com.google.api.services.drive.Drive.Files.List list = drive.files().list();
                    list.setQ(new StringBuilder().append("title = '").append(title).append("' and mimeType != 'application/vnd.google-apps.folder'").toString());

                    FileList fileList = list.execute();
                    if (!fileList.getItems().isEmpty()) {
                        title = new StringBuilder(baseName).append(" (").append(count++).append(')').append(ext).toString();
                    } else {
                        keepOn = false;
                    }
                }
            }

            // Create a file at destination directory
            com.google.api.services.drive.model.File copy = new com.google.api.services.drive.model.File();
            copy.setTitle(title);
            copy.setParents(Collections.<ParentReference> singletonList(new ParentReference().setId(destFolder)));

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

            // Check destination folder
            {
                for (ParentReference parentReference : srcFile.getParents()) {
                    if (parentReference.getId().equals(destFolder)) {
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
                fileMetadata.setParents(Collections.<ParentReference> singletonList(new ParentReference().setId(file.getFolderId())));

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
                fileMetadata.setParents(Collections.<ParentReference> singletonList(new ParentReference().setId(file.getFolderId())));

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
    public void removeDocument(final String folderId, final long sequenceNumber) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();

            // Query all files
            com.google.api.services.drive.Drive.Files.List list = drive.files().list();
            list.setQ("mimeType != 'application/vnd.google-apps.folder'");

            FileList fileList = list.execute();
            if (!fileList.isEmpty()) {
                for (com.google.api.services.drive.model.File child : fileList.getItems()) {
                    drive.children().delete(folderId, child.getId()).execute();
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
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber, boolean hardDelete) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive();

            List<IDTuple> ret = new ArrayList<IDTuple>(ids.size());
            for (IDTuple id : ids) {
                try {
                    drive.children().delete(id.getFolder(), id.getId()).execute();
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
    public String[] removeVersion(final String folderId, final String id, final String[] versions) throws OXException {
        /*
         * Dropbox API does not support removing revisions of a file
         */
        for (final String version : versions) {
            if (version != CURRENT_VERSION) {
                throw GoogleDriveExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
            }
        }
        try {
            Drive drive = googleDriveAccess.getDrive();
            drive.children().delete(folderId, id).execute();
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
    public void unlock(final String folderId, final String id) throws OXException {
        // Nope
    }

    @Override
    public void lock(final String folderId, final String id, final long diff) throws OXException {
        // Nope
    }

    @Override
    public void touch(final String folderId, final String id) throws OXException {
        exists(folderId, id, CURRENT_VERSION);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId) throws OXException {
        try {
            final Entry directoryEntry = dropboxAPI.metadata(toPath(folderId), 0, null, true, null);
            if (!directoryEntry.isDir) {
                throw GoogleDriveExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final List<Entry> contents = directoryEntry.contents;
            final List<File> files = new ArrayList<File>(contents.size());
            for (final Entry childEntry : contents) {
                if (!childEntry.isDir && !childEntry.isDeleted) {
                    files.add(new GoogleDriveFile(folderId, childEntry.path, userId).parseGoogleDriveFile(childEntry));
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
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        try {
            final Entry directoryEntry = dropboxAPI.metadata(toPath(folderId), 0, null, true, null);
            if (!directoryEntry.isDir) {
                throw GoogleDriveExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final List<Entry> contents = directoryEntry.contents;
            final List<File> files = new ArrayList<File>(contents.size());
            for (final Entry childEntry : contents) {
                if (!childEntry.isDir && !childEntry.isDeleted) {
                    files.add(new GoogleDriveFile(folderId, childEntry.path, userId).parseGoogleDriveFile(childEntry));
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
    public TimedResult<File> getVersions(final String folderId, final String id) throws OXException {
        try {
            final List<Entry> revisions = dropboxAPI.revisions(id, 0);
            final List<File> files = new ArrayList<File>(revisions.size());
            for (final Entry revisionEntry : revisions) {
                files.add(new GoogleDriveFile(folderId, id, userId).parseGoogleDriveFile(revisionEntry));
            }
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
    public TimedResult<File> getVersions(final String folderId, final String id, final List<Field> fields) throws OXException {
        return getVersions(folderId, id);
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id, final List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        try {
            final List<Entry> revisions = dropboxAPI.revisions(id, 0);
            final List<File> files = new ArrayList<File>(revisions.size());
            for (final Entry revisionEntry : revisions) {
                files.add(new GoogleDriveFile(folderId, id, userId).parseGoogleDriveFile(revisionEntry));
            }
            // Sort collection
            sort(files, sort, order);
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
    public TimedResult<File> getDocuments(final List<IDTuple> ids, final List<Field> fields) throws OXException {
        try {
            final List<File> files = new ArrayList<File>(ids.size());
            for (final IDTuple id : ids) {
                final Entry entry = dropboxAPI.metadata(id.getId(), 1, null, false, null);
                if (!entry.isDeleted && !entry.isDir) {
                    files.add(new GoogleDriveFile(id.getFolder(), id.getId(), userId).parseGoogleDriveFile(entry));
                }
            }
            return new FileTimedResult(files);
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                throw GoogleDriveExceptionCodes.NOT_FOUND.create(e, e.reason);
            }
            throw handleServerError(null, e);
        } catch (final DropboxException e) {
            throw GoogleDriveExceptionCodes.GOOGLE_DRIVE_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw GoogleDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final SearchIterator<File> EMPTY_ITER = SearchIteratorAdapter.emptyIterator();

    @Override
    public Delta<File> getDelta(final String folderId, final long updateSince, final List<Field> fields, final boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    @Override
    public Delta<File> getDelta(final String folderId, final long updateSince, final List<Field> fields, final Field sort, final SortDirection order, final boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    @Override
    public SearchIterator<File> search(final List<String> folderIds, final SearchTerm<?> searchTerm, List<Field> fields, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        if (FileNameTerm.class.isInstance(searchTerm) && null == folderIds || 1 == folderIds.size()) {
            String pattern = ((FileNameTerm) searchTerm).getPattern();
            return search(pattern, fields, null != folderIds && 1 == folderIds.size() ? folderIds.get(0) : null, sort, order, start, end);
        }
        throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create("Search term not supported: " + searchTerm);
    }

    @Override
    public SearchIterator<File> search(final String pattern, final List<Field> fields, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        try {
            if (isEmpty(pattern)) {
                List<File> files = new LinkedList<File>();
                gatherAllFiles("/", files);
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
            final List<Entry> results;
            if (null == folderId) {
                // All folders...
                final Set<String> folderPaths = new LinkedHashSet<String>(16);
                folderPaths.add("/");
                gatherAllFolders("/", folderPaths);
                // Search in them
                results = new LinkedList<Entry>();
                for (final String folderPath : folderPaths) {
                    results.addAll(searchBy(pattern, folderPath));
                }
            } else {
                results = searchBy(pattern, toPath(folderId));
                if (results.isEmpty()) {
                    return SearchIteratorAdapter.emptyIterator();
                }
            }
            // Convert entries to Files
            List<File> files = new ArrayList<File>(results.size());
            for (final Entry resultsEntry : results) {
                if (!resultsEntry.isDir) {
                    files.add(new GoogleDriveFile(folderId, resultsEntry.path, userId).parseGoogleDriveFile(resultsEntry));
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

    private List<Entry> searchBy(final String pattern, final String folderPath) throws DropboxException {
        // Dropbox API only supports searching by file name
        return dropboxAPI.search(folderPath, pattern, 0, false);
    }

    private void gatherAllFolders(final String path, final Set<String> folderPaths) throws DropboxException {
        final Entry metadata = dropboxAPI.metadata(path, 0, null, true, null);
        final List<Entry> contents = metadata.contents;
        final List<String> collectedPaths = new ArrayList<String>(contents.size());
        for (final Entry childEntry : contents) {
            final String childPath = childEntry.path;
            if (childEntry.isDir && !childEntry.isDeleted && folderPaths.add(childPath)) {
                // No direct recursive invocation to maintain hierarchical order in linked set
                collectedPaths.add(childPath);
            }
        }
        for (final String childPath : collectedPaths) {
            gatherAllFolders(childPath, folderPaths);
        }
    }

    private void gatherAllFiles(final String path, final List<File> files) throws DropboxException, OXException {
        final Entry metadata = dropboxAPI.metadata(path, 0, null, true, null);
        final List<Entry> contents = metadata.contents;
        for (final Entry childEntry : contents) {
            final String childPath = childEntry.path;
            if (!childEntry.isDeleted) {
                if (childEntry.isDir) {
                    gatherAllFiles(childPath, files);
                } else {
                    files.add(new GoogleDriveFile(toId(path), childPath, userId).parseGoogleDriveFile(childEntry));
                }
            }
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

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = com.openexchange.java.Strings.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
