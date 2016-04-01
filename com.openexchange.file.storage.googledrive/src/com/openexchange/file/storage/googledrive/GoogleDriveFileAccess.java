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

import static com.openexchange.file.storage.googledrive.GoogleDriveConstants.FIELDS_DEFAULT;
import static com.openexchange.file.storage.googledrive.GoogleDriveConstants.QUERY_STRING_FILES_ONLY;
import static com.openexchange.file.storage.googledrive.GoogleDriveConstants.QUERY_STRING_FILES_ONLY_EXCLUDING_TRASH;
import static com.openexchange.java.Strings.isEmpty;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.ChangeList;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Revision;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStoragePersistentIDs;
import com.openexchange.file.storage.FileStorageSequenceNumberProvider;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.FileStorageVersionedFileAccess;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.file.storage.googledrive.access.GoogleDriveAccess;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.SizeKnowingInputStream;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link GoogleDriveFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GoogleDriveFileAccess extends AbstractGoogleDriveAccess implements ThumbnailAware, FileStorageSequenceNumberProvider, FileStorageVersionedFileAccess, FileStoragePersistentIDs {

    private final GoogleDriveAccountAccess accountAccess;
    private final int userId;

    /**
     * Initializes a new {@link GoogleDriveFileAccess}.
     *
     * @param googleDriveAccess The underlying Google Drive access
     * @param account The used file storage account
     * @param session The session
     * @param accountAccess A Google Drive account access reference
     */
    public GoogleDriveFileAccess(GoogleDriveAccess googleDriveAccess, FileStorageAccount account, Session session, GoogleDriveAccountAccess accountAccess) throws OXException {
        super(googleDriveAccess, account, session);
        this.accountAccess = accountAccess;
        this.userId = session.getUserId();
    }

    private List<Revision> optRevisions(Drive drive, String id) throws IOException, HttpResponseException {
        List<Revision> revisions;
        try {
            revisions = drive.revisions().list(id).setFields("items/id").execute().getItems();
        } catch (HttpResponseException e) {
            if (e.getStatusCode() != SC_BAD_REQUEST) {
                throw e;
            }
            revisions = null;
        }
        return revisions;
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
    public void setRequestTransactional(boolean transactional) {
        // Nope
    }

    @Override
    public void setCommitsTransaction(boolean commits) {
        // Nope
    }

    @Override
    public boolean exists(String folderId, String id, String version) throws OXException {
        try {
            return null != getMetadata(folderId, id, version, Collections.singletonList(Field.ID), 0);
        } catch (OXException e) {
            if (FileStorageExceptionCodes.FILE_NOT_FOUND.equals(e) || FileStorageExceptionCodes.FOLDER_NOT_FOUND.equals(e) ||
                FileStorageExceptionCodes.FILE_VERSION_NOT_FOUND.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public File getFileMetadata(String folderId, String id, String version) throws OXException {
        return getMetadata(folderId, id, version, ALL_FIELDS, 0);
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber) throws OXException {
        return saveFileMetadata(file, sequenceNumber, ALL_FIELDS);
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        return saveFileMetadata(file, sequenceNumber, modifiedFields, 0);
    }

    private IDTuple saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields, int retryCount) throws OXException {
        if (null == modifiedFields || modifiedFields.contains(Field.FILENAME) || modifiedFields.contains(Field.VERSION)) {
            try {
                Drive drive = googleDriveAccess.getDrive(session);
                com.google.api.services.drive.model.File savedFile = new com.google.api.services.drive.model.File();
                if (FileStorageFileAccess.NEW != file.getId()) {
                    savedFile.setId(file.getId());
                    if ((null == modifiedFields || modifiedFields.contains(Field.FILENAME)) && false == Strings.isEmpty(file.getFileName())) {
                        savedFile.setTitle(file.getFileName());
                        savedFile = drive.files().patch(file.getId(), savedFile).execute();
                    }
                    if ((null == modifiedFields || modifiedFields.contains(Field.VERSION)) && false == Strings.isEmpty(file.getVersion())) {
                        List<Revision> revisions = drive.revisions().list(file.getId()).execute().getItems();
                        Revision referencedRevision = null;
                        for (Revision revision : revisions) {
                            if (revision.getId().equals(file.getVersion())) {
                                referencedRevision = revision;
                                break;
                            }
                        }
                        if (null == referencedRevision) {
                            throw FileStorageExceptionCodes.FILE_VERSION_NOT_FOUND.create(file.getVersion(), file.getId(), file.getFolderId());
                        }
                        if (revisions.size() - 1 != revisions.indexOf(referencedRevision)) {
                            HttpResponse resp = drive.getRequestFactory().buildGetRequest(new GenericUrl(referencedRevision.getDownloadUrl())).execute();
                            InputStream content = null;
                            try {
                                content = resp.getContent();
                                Drive.Files.Update update = drive.files().update(file.getId(), savedFile,
                                    new InputStreamContent(referencedRevision.getMimeType(), content));
                                MediaHttpUploader uploader = update.getMediaHttpUploader();
                                uploader.setDirectUploadEnabled(true);
                                savedFile = update.execute();
                            } finally {
                                Streams.close(content);
                            }
                        }
                    }
                } else {
                    savedFile.setTitle(file.getFileName());
                    savedFile = drive.files().insert(savedFile).execute();
                }
                return new IDTuple(file.getFolderId(), savedFile.getId());
            } catch (final HttpResponseException e) {
                if (!isUserRateLimitExceeded(e)) {
                    // Otherwise throw exception
                    throw handleHttpResponseError(file.getId(), e);
                }

                // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
                int retry = retryCount + 1;
                if (retry > 5) {
                    // Exceeded max. retry count
                    throw handleHttpResponseError(file.getId(), e);
                }

                long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
                LockSupport.parkNanos(nanosToWait);
                return saveFileMetadata(file, sequenceNumber, modifiedFields, retry);
            } catch (final IOException e) {
                throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } catch (final RuntimeException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return new IDTuple(file.getFolderId(), file.getId());
    }

    @Override
    public IDTuple copy(IDTuple source, String version, String destFolder, File update, InputStream newFil, List<Field> modifiedFields) throws OXException {
        return copy(source, version, destFolder, update, newFil, modifiedFields, 0);
    }

    private IDTuple copy(IDTuple source, String version, String destFolder, File update, InputStream newFil, List<Field> modifiedFields, int retryCount) throws OXException {
        if (version != CURRENT_VERSION) {
            // can only copy the current revision
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(GoogleDriveConstants.ID);
        }
        String id = source.getId();
        try {
            Drive drive = googleDriveAccess.getDrive(session);

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
            if (null != update) {
                if (false == Strings.isEmpty(update.getTitle()) && (null == modifiedFields || modifiedFields.contains(File.Field.FILENAME)) &&
                    false == update.getTitle().equals(srcFile.getTitle())) {
                    copy.setTitle(update.getTitle());
                }
            }

            // Copy file
            com.google.api.services.drive.model.File copiedFile = drive.files().copy(id, copy).execute();

            return new IDTuple(destFolder, copiedFile.getId());
        } catch (final HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(id, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(id, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return copy(source, version, destFolder, update, newFil, modifiedFields, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<File.Field> modifiedFields) throws OXException {
        return move(source, destFolder, sequenceNumber, update, modifiedFields, 0);
    }

    private IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<File.Field> modifiedFields, int retryCount) throws OXException {
        String id = source.getId();
        try {
            Drive drive = googleDriveAccess.getDrive(session);

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
            if (null != update) {
                if (false == Strings.isEmpty(update.getTitle()) && (null == modifiedFields || modifiedFields.contains(File.Field.FILENAME)) &&
                    false == update.getTitle().equals(srcFile.getTitle())) {
                    patch.setTitle(update.getTitle());
                }
            }

            // Patch the file
            com.google.api.services.drive.model.File patchedFile = drive.files().patch(id, patch).execute();

            return new IDTuple(destFolder, patchedFile.getId());
        } catch (final HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(id, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(id, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return move(source, destFolder, sequenceNumber, update, modifiedFields, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public InputStream getDocument(String folderId, String id, String version) throws OXException {
        return getDocument(folderId, id, version, 0);
    }

    private InputStream getDocument(String folderId, String id, String version, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive(session);
            /*
             * get download URL from file or revision
             */
            com.google.api.services.drive.model.File file = drive.files().get(id).setFields(FIELDS_DEFAULT + ",downloadUrl,fileSize").execute();
            checkFileValidity(file);
            String downloadUrl;
            if (CURRENT_VERSION == version) {
                downloadUrl = file.getDownloadUrl();
            } else {
                Revision revision = drive.revisions().get(id, version).setFields("downloadUrl").execute();
                downloadUrl = revision.getDownloadUrl();
            }
            if (Strings.isEmpty(downloadUrl)) {
                // The file doesn't have any content stored on Drive.
                throw FileStorageExceptionCodes.NO_CONTENT.create(id);
            }
            /*
             * get content stream
             */
            HttpResponse resp = drive.getRequestFactory().buildGetRequest(new GenericUrl(downloadUrl)).execute();
            if (null != file.getFileSize()) {
                return new SizeKnowingInputStream(resp.getContent(), file.getFileSize().longValue());
            } else {
                return resp.getContent();
            }
        } catch (final HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(id, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(id, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return getDocument(folderId, id, version, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public InputStream getThumbnailStream(String folderId, String id, String version) throws OXException {
        return getThumbnailStream(folderId, id, version, 0);
    }

    private InputStream getThumbnailStream(String folderId, String id, String version, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive(session);
            /*
             * get thumbnail link from file
             */
            com.google.api.services.drive.model.File file = drive.files().get(id).setFields(FIELDS_DEFAULT + ",thumbnailLink").execute();
            checkFileValidity(file);
            String thumbnailLink = file.getThumbnailLink();
            if (Strings.isEmpty(thumbnailLink)) {
                // The file doesn't have a thumbnail
                return null;
            }
            /*
             * thumbnail link is valid for latest revision only
             */
            if (CURRENT_VERSION != version) {
                List<Revision> revisions = optRevisions(drive, id);
                if (null == revisions || 0 == revisions.size() || false == version.equals(revisions.get(revisions.size() - 1).getId())) {
                    return null;
                }
            }
            /*
             * get thumbnail stream
             */
            HttpResponse resp = drive.getRequestFactory().buildGetRequest(new GenericUrl(thumbnailLink)).execute();
            return resp.getContent();
        } catch (final HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(id, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(id, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return getThumbnailStream(folderId, id, version, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        return saveDocument(file, data, sequenceNumber, null);
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        return saveDocument(file, data, sequenceNumber, modifiedFields, 0);
    }

    private IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields, int retryCount) throws OXException {
        /*
         * prepare Google Drive file
         */
        com.google.api.services.drive.model.File fileMetadata = new com.google.api.services.drive.model.File();
        fileMetadata.setParents(Collections.<ParentReference> singletonList(new ParentReference().setId(toGoogleDriveFolderId(file.getFolderId()))));
        try {
            Drive drive = googleDriveAccess.getDrive(session);
            if (FileStorageFileAccess.NEW == file.getId()) {
                /*
                 * insert new file
                 */
                ThresholdFileHolder sink = null;
                try {
                    sink = new ThresholdFileHolder();
                    sink.write(data);

                    String name = file.getFileName();
                    String fileName = name;
                    int count = 0;

                    while (true) {
                        try {
                            fileMetadata.setTitle(fileName);
                            Drive.Files.Insert insert = drive.files().insert(fileMetadata, new InputStreamContent(file.getFileMIMEType(), sink.getStream()));
                            insert.getMediaHttpUploader().setDirectUploadEnabled(true);
                            com.google.api.services.drive.model.File gDriveFile = insert.execute();
                            String newId = gDriveFile.getId();
                            file.setId(newId);
                            return new IDTuple(file.getFolderId(), newId);
                        } catch (com.google.api.client.http.HttpResponseException e) {
                            if (SC_CONFLICT != e.getStatusCode()) {
                                throw e;
                            }

                            fileName = FileStorageUtility.enhance(name, ++count);
                        }
                    }

                } finally {
                    Streams.close(sink);
                }

            } else {
                /*
                 * upload new version of existing file, adjusting metadata as requested
                 */
                if (null != file.getFileName() && (null == modifiedFields || modifiedFields.contains(Field.FILENAME))) {
                    fileMetadata.setTitle(file.getFileName());
                }
                Drive.Files.Update update = drive.files().update(file.getId(), fileMetadata, new InputStreamContent(file.getFileMIMEType(), data));
                update.getMediaHttpUploader().setDirectUploadEnabled(true);
                fileMetadata = update.execute();
                return new IDTuple(file.getFolderId(), fileMetadata.getId());
            }
        } catch (final HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(file.getId(), e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(file.getId(), e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return saveDocument(file, data, sequenceNumber, modifiedFields, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        removeDocument(folderId, sequenceNumber, 0);
    }

    private void removeDocument(String folderId, long sequenceNumber, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive(session);

            // Determine folder identifier
            String fid = toGoogleDriveFolderId(folderId);

            // Query all files
            Drive.Children.List list = drive.children().list(fid);
            list.setQ(QUERY_STRING_FILES_ONLY_EXCLUDING_TRASH);
            list.setFields("kind,nextPageToken,items(id)");

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

                String nextPageToken = childList.getNextPageToken();
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

                    nextPageToken = childList.getNextPageToken();
                }
            }
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

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            removeDocument(folderId, sequenceNumber, retry);
            return;
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber, boolean hardDelete) throws OXException {
        return removeDocument(ids, sequenceNumber, hardDelete, 0);
    }

    private List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber, boolean hardDelete, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive(session);
            Map<String, Boolean> knownTrashFolders = new HashMap<String, Boolean>();
            List<IDTuple> ret = new ArrayList<IDTuple>(ids.size());
            for (IDTuple id : ids) {
                try {
                    if (hardDelete) {
                        drive.files().delete(id.getId()).execute();
                    } else {
                        Boolean isTrashed = knownTrashFolders.get(id.getFolder());
                        if (null == isTrashed) {
                            isTrashed = Boolean.valueOf(isTrashed(toGoogleDriveFolderId(id.getFolder()), drive));
                            knownTrashFolders.put(id.getFolder(), isTrashed);
                        }
                        if (isTrashed.booleanValue()) {
                            drive.files().delete(id.getId()).execute();
                        } else {
                            drive.files().trash(id.getId()).execute();
                        }
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

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return removeDocument(ids, sequenceNumber, hardDelete, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void touch(String folderId, String id) throws OXException {
        exists(folderId, id, CURRENT_VERSION);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId) throws OXException {
        return getDocuments(folderId, ALL_FIELDS);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields) throws OXException {
        return getDocuments(folderId, fields, null, SortDirection.DEFAULT);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {
        return getDocuments(folderId, fields, sort, order, 0);
    }

    private TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive(session);
            List<File> files = new LinkedList<File>();
            /*
             * build request to list all files in a folder
             */
            com.google.api.services.drive.Drive.Files.List listRequest = drive.files().list()
                .setQ(QUERY_STRING_FILES_ONLY_EXCLUDING_TRASH + " and '" + toGoogleDriveFolderId(folderId) + "' in parents")
                .setFields("kind,nextPageToken,items(" + getFields(fields) + ')');
            /*
             * execute as often as needed & parse files
             */
            FileList fileList;
            do {
                fileList = listRequest.execute();
                for (com.google.api.services.drive.model.File file : fileList.getItems()) {
                    GoogleDriveFile metadata = createFile(folderId, file.getId(), file, fields);
                    if (null == fields || fields.contains(Field.VERSION) || fields.contains(Field.NUMBER_OF_VERSIONS)) {
                        List<Revision> revisions = optRevisions(drive, metadata.getId());
                        if (null != revisions && 0 < revisions.size()) {
                            metadata.setNumberOfVersions(revisions.size());
                            metadata.setVersion(revisions.get(revisions.size() - 1).getId());
                        } else {
                            metadata.setVersion(FileStorageFileAccess.CURRENT_VERSION);
                            metadata.setNumberOfVersions(1);
                        }
                    }
                    files.add(metadata);
                }
                listRequest.setPageToken(fileList.getNextPageToken());
            } while (null != fileList.getNextPageToken());
            /*
             * return sorted timed result
             */
            sort(files, sort, order);
            return new FileTimedResult(files);
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

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return getDocuments(folderId, fields, sort, order, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public TimedResult<File> getDocuments(List<IDTuple> ids, List<Field> fields) throws OXException {
        List<File> files = new LinkedList<File>();
        for (IDTuple idTuple : ids) {
            files.add(getMetadata(idTuple.getFolder(), idTuple.getId(), CURRENT_VERSION, fields, 0));
        }
        return new FileTimedResult(files);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        return getDelta(folderId, updateSince, fields, null, SortDirection.DEFAULT, ignoreDeleted);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        return getDelta(folderId, updateSince, fields, sort, order, ignoreDeleted, 0);
    }

    private Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive(session);
            List<File> updatedFiles = new LinkedList<File>();
            List<File> deletedFiles = new LinkedList<File>();
            List<File> newFiles = new LinkedList<File>();
            long sequenceNumber = updateSince;
            /*
             * build request to list all files in a folder, changed since the supplied timestamp
             */
            StringBuilder stringBuilder = new StringBuilder(QUERY_STRING_FILES_ONLY);
            stringBuilder.append(" and '").append(toGoogleDriveFolderId(folderId)).append("' in parents");
            if (Long.MIN_VALUE != updateSince) {
                stringBuilder.append(" and modifiedDate > '").append(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date(updateSince))).append('\'');
            }
            com.google.api.services.drive.Drive.Files.List listRequest =
                drive.files().list().setQ(stringBuilder.toString()).setFields("kind,nextPageToken,items(" + getFields(fields, Field.CREATED) + ')');
            /*
             * execute as often as needed & parse files
             */
            FileList fileList;
            do {
                fileList = listRequest.execute();
                for (com.google.api.services.drive.model.File file : fileList.getItems()) {
                    GoogleDriveFile metadata = createFile(folderId, file.getId(), file, fields);
                    /*
                     * determine maximum sequence number & add file to appropriate delta collection
                     */
                    sequenceNumber = Math.max(sequenceNumber, metadata.getSequenceNumber());
                    if (null != file.getLabels() && Boolean.TRUE.equals(file.getLabels().getTrashed())) {
                        deletedFiles.add(metadata);
                    } else {
                        if (null == fields || fields.contains(Field.VERSION) || fields.contains(Field.NUMBER_OF_VERSIONS)) {
                            List<Revision> revisions = optRevisions(drive, metadata.getId());
                            if (null != revisions && 0 < revisions.size()) {
                                metadata.setNumberOfVersions(revisions.size());
                                metadata.setVersion(revisions.get(revisions.size() - 1).getId());
                            } else {
                                metadata.setVersion(FileStorageFileAccess.CURRENT_VERSION);
                                metadata.setNumberOfVersions(1);
                            }
                        }
                        if (Long.MIN_VALUE == updateSince || null != metadata.getCreated() && metadata.getCreated().getTime() > updateSince) {
                            newFiles.add(metadata);
                        } else {
                            updatedFiles.add(metadata);
                        }
                    }
                }
                listRequest.setPageToken(fileList.getNextPageToken());
            } while (null != fileList.getNextPageToken());
            /*
             * return sorted timed result
             */
            sort(updatedFiles, sort, order);
            sort(deletedFiles, sort, order);
            sort(newFiles, sort, order);
            return new FileDelta(newFiles, updatedFiles, deletedFiles, sequenceNumber);
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

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return getDelta(folderId, updateSince, fields, sort, order, ignoreDeleted, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
        return search(pattern, fields, folderId, false, sort, order, start, end);
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
        try {
            // Search by pattern
            List<File> files = searchByFileNamePattern(pattern, folderId, includeSubfolders, fields, sort, order, 0);

            // Start, end...
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
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public Map<String, Long> getSequenceNumbers(List<String> folderIds) throws OXException {
        return getSequenceNumbers(folderIds, 0);
    }

    private Map<String, Long> getSequenceNumbers(List<String> folderIds, int retryCount) throws OXException {
        Long largestChangeId;
        try {
            Drive drive = googleDriveAccess.getDrive(session);
            ChangeList changeList = drive.changes().list().setFields("largestChangeId").execute();
            largestChangeId = changeList.getLargestChangeId();
        } catch (HttpResponseException e) {
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

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return getSequenceNumbers(folderIds, retry);
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        Map<String, Long> sequenceNumbers = new HashMap<String, Long>(folderIds.size());
        for (String folderId : folderIds) {
            sequenceNumbers.put(folderId, largestChangeId);
        }
        return sequenceNumbers;
    }

    /**
     * Searches for files whose filename matches the supplied pattern.
     *
     * @param pattern The pattern to search for
     * @param folderId The parent folder identifier to restrict the search to, or <code>null</code> to search all folders
     * @param includeSubfolders <code>true</code> to include subfolderes, <code>false</code>, otherwise
     * @param fields The fields to retrieve
     * @param sort The field to use to sort the results
     * @param order The sort order to apply
     * @return The found files
     */
    private List<File> searchByFileNamePattern(String pattern, String folderId, boolean includeSubfolders, List<Field> fields, Field sort, SortDirection order, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive(session);
            List<File> files = new ArrayList<File>();
            /*
             * build search query
             */
            StringBuilder stringBuilder = new StringBuilder(QUERY_STRING_FILES_ONLY_EXCLUDING_TRASH);
            Map<String, Boolean> allowedFolders;
            if (null != folderId) {
                allowedFolders = new HashMap<String, Boolean>();
                allowedFolders.put(folderId, Boolean.TRUE);
                if (false == includeSubfolders) {
                    stringBuilder.append(" and '").append(toGoogleDriveFolderId(folderId)).append("' in parents");
                }
            } else {
                allowedFolders = null;
            }
            if (null != pattern) {
                stringBuilder.append(" and title contains '").append(escape(pattern)).append('\'');
            }
            /*
             * build request based on query
             */
            com.google.api.services.drive.Drive.Files.List listRequest = drive.files().list().setQ(stringBuilder.toString())
                .setFields("kind,nextPageToken,items(" + getFields(fields, sort) + ')');
            /*
             * execute as often as needed & parse files
             */
            FileList fileList;
            do {
                fileList = listRequest.execute();
                for (com.google.api.services.drive.model.File file : fileList.getItems()) {
                    GoogleDriveFile metadata = createFile(null, file.getId(), file, fields);
                    if (null != allowedFolders) {
                        Boolean allowed = allowedFolders.get(metadata.getFolderId());
                        if (null == allowed) {
                            allowed = Boolean.valueOf(includeSubfolders && isSubfolderOf(drive, metadata.getFolderId(), folderId));
                            allowedFolders.put(metadata.getFolderId(), allowed);
                        }
                        if (false == allowed.booleanValue()) {
                            continue; // skip this file
                        }
                    }
                    if (null == fields || fields.contains(Field.VERSION) || fields.contains(Field.NUMBER_OF_VERSIONS)) {
                        List<Revision> revisions = optRevisions(drive, metadata.getId());
                        if (null != revisions && 0 < revisions.size()) {
                            metadata.setNumberOfVersions(revisions.size());
                            metadata.setVersion(revisions.get(revisions.size() - 1).getId());
                        } else {
                            metadata.setVersion(FileStorageFileAccess.CURRENT_VERSION);
                            metadata.setNumberOfVersions(1);
                        }
                    }
                    files.add(metadata);
                }
                listRequest.setPageToken(fileList.getNextPageToken());
            } while (null != fileList.getNextPageToken());
            /*
             * return sorted timed result
             */
            sort(files, sort, order);
            return files;
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

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return searchByFileNamePattern(pattern, folderId, includeSubfolders, fields, sort, order, retry);
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets a value indicating whether a folder is a subfolder (at any level) of a parent folder.
     *
     * @param drive A reference to the drive service
     * @param folderId The identifier of the folder to check
     * @param parentFolderId The identifier of the parent folder, or <code>null</code> to fall back to the root folder
     * @return <code>true</code> if the folder  is a subfolder (at any level) of the parent folder, <code>false</code>, otherwise
     */
    private boolean isSubfolderOf(Drive drive, String folderId, String parentFolderId) throws OXException, IOException {
        String driveId = toGoogleDriveFolderId(folderId);
        String rootDriveId = getRootFolderId();
        String parentDriveId = null != parentFolderId ? toGoogleDriveFolderId(parentFolderId) : rootDriveId;
        if (parentDriveId.equals(rootDriveId)) {
            return true;
        }
        if (driveId.equals(rootDriveId) || driveId.equals(parentDriveId)) {
            return false;
        }
        do {
            com.google.api.services.drive.model.File dir = drive.files().get(driveId).execute();
            driveId = dir.getParents().get(0).getId();
        } while (false == driveId.equals(parentDriveId) && false == driveId.equals(rootDriveId));
        return driveId.equals(parentDriveId);
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

    @Override
    public String[] removeVersion(String folderId, String id, String[] versions) throws OXException {
        return removeVersion(folderId, id, versions, 0);
    }

    private String[] removeVersion(String folderId, String id, String[] versions, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive(session);
            for (String version : versions) {
                drive.revisions().delete(id, version).execute();
            }
            return new String[0];
        } catch (HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(id, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(id, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return removeVersion(folderId, id, versions, retry);
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id) throws OXException {
        return getVersions(folderId, id, null);
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields) throws OXException {
        return getVersions(folderId, id, fields, null, SortDirection.DEFAULT);
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        return getVersions(folderId, id, fields, sort, order, 0);
    }

    private TimedResult<File> getVersions(String folderId, String id, List<Field> fields, Field sort, SortDirection order, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive(session);
            /*
             * get parent file & apply revisions
             */
            com.google.api.services.drive.model.File file = drive.files().get(id).setFields(getFields(fields)).execute();
            checkFileValidity(file);
            List<Revision> revisions = drive.revisions().list(file.getId()).execute().getItems();
            List<File> versions = new ArrayList<File>(revisions.size());
            for (int i = 0; i < revisions.size(); i++) {
                Revision revision = revisions.get(i);
                GoogleDriveFile version = applyRevision(createFile(folderId, id, file, fields), revision, fields, i == revisions.size() - 1);
                version.setNumberOfVersions(revisions.size());
                versions.add(version);
            }
            /*
             * sort & return results
             */
            sort(versions, sort, order);
            return new FileTimedResult(versions);
        } catch (HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(id, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(id, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return getVersions(folderId, id, fields, sort, order, retry);
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets metadata of a single file version in a folder.
     *
     * @param folderId The identifier of the parent folder
     * @param id The file identifier
     * @param version The version to get, or {@link FileStorageFileAccess#CURRENT_VERSION} to get the current one
     * @param fields The fields to include
     * @return The file
     */
    private GoogleDriveFile getMetadata(String folderId, String id, String version, List<Field> fields, int retryCount) throws OXException {
        try {
            Drive drive = googleDriveAccess.getDrive(session);
            /*
             * get single file
             */
            com.google.api.services.drive.model.File file = drive.files().get(id).setFields(getFields(fields)).execute();
            checkFileValidity(file);
            String parentID = file.getParents().get(0).getId();
            GoogleDriveFile metadata = createFile(parentID, id, file, fields);
            if (null != folderId && false == folderId.equals(metadata.getFolderId())) {
                throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
            }
            /*
             * add version information as needed
             */
            if (FileStorageFileAccess.CURRENT_VERSION != version || null == fields || fields.contains(Field.NUMBER_OF_VERSIONS) || fields.contains(Field.VERSION)) {
                if (FileStorageFileAccess.CURRENT_VERSION == version) {
                    List<Revision> revisions = optRevisions(drive, id);
                    if (revisions!= null && !revisions.isEmpty()) {
                        metadata.setNumberOfVersions(revisions.size());
                        metadata.setVersion(revisions.get(revisions.size() - 1).getId());
                    } else {
                        metadata.setNumberOfVersions(1);
                        metadata.setVersion(FileStorageFileAccess.CURRENT_VERSION);
                    }
                    return metadata;
                } else {
                    List<Revision> revisions = drive.revisions().list(id).execute().getItems();
                    metadata.setNumberOfVersions(revisions.size());
                    for (int i = 0; i < revisions.size(); i++) {
                        Revision revision = revisions.get(i);
                        if (revision.getId().equals(version)) {
                            return applyRevision(metadata, revision, fields, i == revisions.size() - 1);
                        }
                    }
                    throw FileStorageExceptionCodes.FILE_VERSION_NOT_FOUND.create(id, version, folderId);
                }
            }
            return metadata;
        } catch (final HttpResponseException e) {
            if (!isUserRateLimitExceeded(e)) {
                // Otherwise throw exception
                throw handleHttpResponseError(id, e);
            }

            // Handle user rate limit error following using exponential backoff (https://developers.google.com/analytics/devguides/reporting/core/v3/coreErrors#backoff)
            int retry = retryCount + 1;
            if (retry > 5) {
                // Exceeded max. retry count
                throw handleHttpResponseError(id, e);
            }

            long nanosToWait = TimeUnit.NANOSECONDS.convert((retry * 1000) + ((long)(Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
            return getMetadata(folderId, id, version, fields, retry);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Creates a {@link GoogleDriveFile} based on a {@link com.google.api.services.drive.model.File}.
     *
     * @param folderId The folder identifier to apply, or <code>null</code> to get the parent folder from the file
     * @param fileId The file identifier to apply
     * @param file The file
     * @param fields The fields to assign, or <code>null</code> to set all fields
     * @return The file
     */
    private GoogleDriveFile createFile(String folderId, String fileId, com.google.api.services.drive.model.File file, List<Field> fields) throws OXException {
        if (null == folderId && null != file && null != file.getParents() && 0 < file.getParents().size()) {
            folderId = file.getParents().get(0).getId();
        }
        return new GoogleDriveFile(folderId, fileId, userId, getRootFolderId()).parseGoogleDriveFile(file, fields);
    }

    /**
     * Applies a {@link Revision} to a parent a {@link GoogleDriveFile}.
     *
     * @param file The parent file
     * @param revision The revision to apply
     * @param fields The fields to assign, or <code>null</code> to set all fields
     * @param current <code>true</code> if this is the current version, <code>false</code>, otherwise
     * @return The file
     */
    private GoogleDriveFile applyRevision(GoogleDriveFile file, Revision revision, List<Field> fields, boolean current) throws OXException {
        file.parseRevision(revision, fields);
        file.setIsCurrentVersion(current);
        return file;
    }

    private void checkFileValidity(com.google.api.services.drive.model.File file) throws OXException {
        if (isDir(file)) {
            throw FileStorageExceptionCodes.NOT_A_FILE.create(GoogleDriveConstants.ID, file.getId());
        }
        checkIfTrashed(file);
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

    /**
     * Gets the Google Drive fields to query from the service corresponding to the supplied {@link Field} collection. The mandatory {@link GoogleDriveConstants#FIELDS_DEFAULT} are always included.
     *
     * @param requestedFields The fields as requested by the client, or {@link FileStorageFileAccess#ALL_FIELDS} to query all known fields
     * @param additionalFields Additional fields to include
     * @return The Google Drive fields as comma-separated string
     */
    private static String getFields(List<Field> requestedFields, Field... additionalFields) {
        StringBuilder stringBuilder = new StringBuilder(FIELDS_DEFAULT);
        for (Field field : getUniqueFields(requestedFields, additionalFields)) {
            switch (field) {
                case CREATED:
                    stringBuilder.append(",createdDate");
                    break;
                case TITLE:
                    /* fall-through */
                case FILENAME:
                    stringBuilder.append(",title");
                    break;
                case FILE_SIZE:
                    stringBuilder.append(",fileSize");
                    break;
                case URL:
                    stringBuilder.append(",webContentLink");
                    break;
                case DESCRIPTION:
                    stringBuilder.append(",description");
                    break;
                case FILE_MD5SUM:
                    stringBuilder.append(",md5Checksum");
                default:
                    break;
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Gets a unique collection containing the fields based on the ones that were requested by the client, as well as additionally needed
     * fields.
     *
     * @param requestedFields The fields as requested by the client, or {@link FileStorageFileAccess#ALL_FIELDS} to query all known fields
     * @param additionalFields Additional fields to include
     * @return The unique fields
     */
    private static Collection<Field> getUniqueFields(List<Field> requestedFields, Field... additionalFields) {
        if (ALL_FIELDS == requestedFields) {
            return Arrays.asList(Field.values());
        } else {
            Set<Field> fieldSet = new HashSet<Field>(requestedFields);
            if (null != additionalFields && 0 < additionalFields.length) {
                for (Field additionalField : additionalFields) {
                    if (null != additionalField) {
                        fieldSet.add(additionalField);
                    }
                }
            }
            return fieldSet;
        }
    }

    /**
     * Escapes a pattern string to be used in Google Drive queries.
     *
     * @param pattern The pattern to escape
     * @return The escaped pattern
     */
    private static String escape(String pattern) {
        if (null == pattern) {
            return pattern;
        }
        return pattern.replace("'", "\\'");
    }

}
