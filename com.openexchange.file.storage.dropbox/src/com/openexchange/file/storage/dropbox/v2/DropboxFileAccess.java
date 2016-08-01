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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.ListRevisionsErrorException;
import com.dropbox.core.v2.files.ListRevisionsResult;
import com.dropbox.core.v2.files.LookupError;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.RelocationErrorException;
import com.dropbox.core.v2.files.RestoreErrorException;
import com.dropbox.core.v2.files.SearchErrorException;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.v2.files.SearchResult;
import com.dropbox.core.v2.files.ThumbnailErrorException;
import com.dropbox.core.v2.files.ThumbnailFormat;
import com.dropbox.core.v2.files.ThumbnailSize;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.UploadSessionFinishUploader;
import com.dropbox.core.v2.files.UploadSessionStartResult;
import com.dropbox.core.v2.files.UploadSessionStartUploader;
import com.dropbox.core.v2.files.UploadUploader;
import com.dropbox.core.v2.files.WriteMode;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageSequenceNumberProvider;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.FileStorageVersionedFileAccess;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.file.storage.dropbox.DropboxConstants;
import com.openexchange.file.storage.dropbox.access.DropboxOAuthAccess;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.SizeKnowingInputStream;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link DropboxFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DropboxFileAccess extends AbstractDropboxAccess implements ThumbnailAware, FileStorageSequenceNumberProvider, FileStorageVersionedFileAccess {

    private final DropboxAccountAccess accountAccess;
    private final int userId;

    private static final int CHUNK_SIZE = 512000 * 1;

    /**
     * Initializes a new {@link DropboxFileAccess}.
     */
    public DropboxFileAccess(final DropboxOAuthAccess dropboxOAuthAccess, final FileStorageAccount account, final Session session, final DropboxAccountAccess accountAccess) {
        super(dropboxOAuthAccess, account, session);
        this.accountAccess = accountAccess;
        userId = session.getUserId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#exists(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean exists(String folderId, String id, String version) throws OXException {
        //TODO: double check whether the 'version' parameter has to be used
        try {
            Metadata metadata = getMetadata(toPath(folderId, id));
            return metadata instanceof FileMetadata;
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
     * @see com.openexchange.file.storage.FileStorageFileAccess#getFileMetadata(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public File getFileMetadata(String folderId, String id, String version) throws OXException {
        //TODO: double check whether the 'version' parameter has to be used
        try {
            String path = toPath(folderId, id);
            Metadata metadata = getMetadata(path);
            if (!(metadata instanceof FileMetadata)) {
                throw FileStorageExceptionCodes.NOT_A_FILE.create(DropboxConstants.ID, folderId);
            }
            DropboxFile dropboxFile = new DropboxFile((FileMetadata) metadata, userId);
            //TODO: fetching all revisions just to get the number of versions is quite expensive;
            //      maybe we can introduce something like "-1" for "unknown number of versions"
            ListRevisionsResult revisions = client.files().listRevisions(path, 100);
            if (revisions != null) {
                dropboxFile.setNumberOfVersions(revisions.getEntries().size());
            }
            return dropboxFile;
        } catch (GetMetadataErrorException e) {
            // TODO: Maybe introduce new exception codes?
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveFileMetadata(com.openexchange.file.storage.File, long)
     */
    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber) throws OXException {
        return saveFileMetadata(file, sequenceNumber, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveFileMetadata(com.openexchange.file.storage.File, long, java.util.List)
     */
    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        if (file.getId() == FileStorageFileAccess.NEW) {
            // Create new, empty file ("touch")
            try {
                String path = toPath(file.getFolderId(), file.getFileName());
                UploadUploader upload = client.files().upload(path);
                FileMetadata metadata = upload.finish();
                DropboxFile dbxFile = new DropboxFile(metadata, userId);
                file.copyFrom(dbxFile, Field.ID, Field.FOLDER_ID, Field.VERSION, Field.FILE_SIZE, Field.FILENAME, Field.LAST_MODIFIED, Field.CREATED);
                return dbxFile.getIDTuple();
            } catch (DbxException e) {
                throw DropboxExceptionHandler.handle(e);
            }
        } else {
            String path = toPath(file.getFolderId(), file.getId());

            // Rename
            if (modifiedFields == null || modifiedFields.contains(Field.FILENAME)) {
                String toPath = toPath(file.getFolderId(), file.getFileName());
                if (!path.equals(toPath)) {
                    try {
                        if (Strings.equalsNormalizedIgnoreCase(path, toPath)) {
                            String filePath = toPath(file.getFolderId(), UUID.randomUUID().toString() + ' ' + file.getFileName());
                            Metadata metadata = client.files().move(path, filePath);
                            path = metadata.getPathDisplay();
                        }

                        Metadata metadata = client.files().move(path, toPath);
                        DropboxFile dbxFile = new DropboxFile((FileMetadata) metadata, userId);
                        file.copyFrom(dbxFile, Field.ID, Field.FOLDER_ID, Field.VERSION, Field.FILE_SIZE, Field.FILENAME, Field.LAST_MODIFIED, Field.CREATED);
                        return dbxFile.getIDTuple();
                    } catch (RelocationErrorException e) {
                        // TODO: Maybe introduce new exception codes?
                        throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                    } catch (DbxException e) {
                        throw DropboxExceptionHandler.handle(e);
                    }
                }
            }

            // Restore version
            if (modifiedFields == null || modifiedFields.contains(Field.VERSION)) {
                if (file.getVersion() != null) {
                    try {
                        FileMetadata metadata = client.files().restore(path, file.getVersion());
                        DropboxFile dbxFile = new DropboxFile(metadata, userId);
                        file.copyFrom(dbxFile, Field.ID, Field.FOLDER_ID, Field.VERSION, Field.FILE_SIZE, Field.FILENAME, Field.LAST_MODIFIED, Field.CREATED);
                        return dbxFile.getIDTuple();
                    } catch (RestoreErrorException e) {
                        // TODO: Maybe introduce new exception codes?
                        throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                    } catch (DbxException e) {
                        throw DropboxExceptionHandler.handle(e);
                    }
                }
            }

            return new IDTuple(file.getFolderId(), file.getId());
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#copy(com.openexchange.file.storage.FileStorageFileAccess.IDTuple, java.lang.String, java.lang.String, com.openexchange.file.storage.File, java.io.InputStream, java.util.List)
     */
    @Override
    public IDTuple copy(IDTuple source, String version, String destFolder, File update, InputStream newFile, List<Field> modifiedFields) throws OXException {
        if (version != CURRENT_VERSION) {
            // can only copy the current revision
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(DropboxConstants.ID);
        }

        String path = toPath(source.getFolder(), source.getId());
        String destName = null != update && null != modifiedFields && modifiedFields.contains(Field.FILENAME) ? update.getFileName() : source.getId();

        // Ensure filename uniqueness in target folder
        for (int i = 1; exists(destFolder, destName, CURRENT_VERSION); i++) {
            destName = FileStorageUtility.enhance(destName, i);
        }

        try {
            String destPath = toPath(destFolder, destName);

            // Copy
            Metadata metadata = client.files().copy(path, destPath);
            if (!(metadata instanceof FileMetadata)) {
                throw FileStorageExceptionCodes.NOT_A_FILE.create(DropboxConstants.ID, destPath);
            }
            DropboxFile dbxFile = new DropboxFile((FileMetadata) metadata, userId);
            if (update != null) {
                update.copyFrom(dbxFile, Field.ID, Field.FOLDER_ID, Field.VERSION, Field.FILE_SIZE, Field.FILENAME, Field.LAST_MODIFIED, Field.CREATED);
            }
            return dbxFile.getIDTuple();
        } catch (RelocationErrorException e) {
            // TODO Auto-generated catch block
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#move(com.openexchange.file.storage.FileStorageFileAccess.IDTuple, java.lang.String, long, com.openexchange.file.storage.File, java.util.List)
     */
    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<Field> modifiedFields) throws OXException {
        String path = toPath(source.getFolder(), source.getId());
        String destName = null != update && null != modifiedFields && modifiedFields.contains(Field.FILENAME) ? update.getFileName() : source.getId();
        String destPath = toPath(destFolder, destName);

        try {
            Metadata metadata = client.files().move(path, destPath);
            if (!(metadata instanceof FileMetadata)) {
                throw FileStorageExceptionCodes.NOT_A_FILE.create(DropboxConstants.ID, destPath);
            }
            DropboxFile dbxFile = new DropboxFile((FileMetadata) metadata, userId);
            if (update != null) {
                update.copyFrom(dbxFile, Field.ID, Field.FOLDER_ID, Field.VERSION, Field.FILE_SIZE, Field.FILENAME, Field.LAST_MODIFIED, Field.CREATED);
            }
            return dbxFile.getIDTuple();
        } catch (RelocationErrorException e) {
            // TODO Auto-generated catch block
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocument(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public InputStream getDocument(String folderId, String id, String version) throws OXException {
        try {
            DbxDownloader<FileMetadata> download = client.files().download(toPath(folderId, id), version);
            return new SizeKnowingInputStream(download.getInputStream(), download.getResult().getSize());
        } catch (DownloadErrorException e) {
            // TODO Auto-generated catch block
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveDocument(com.openexchange.file.storage.File, java.io.InputStream, long)
     */
    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        return saveDocument(file, data, sequenceNumber, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveDocument(com.openexchange.file.storage.File, java.io.InputStream, long, java.util.List)
     */
    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        String path = FileStorageFileAccess.NEW == file.getId() ? null : toPath(file.getFolderId(), file.getId());
        long fileSize = file.getFileSize();

        if (Strings.isEmpty(path) || !exists(file.getFolderId(), file.getId(), CURRENT_VERSION)) {
            // Create file
            if (fileSize > CHUNK_SIZE) {
                return sessionUpload(file, data);
            } else {
                return singleUpload(file, data);
            }
        } else {
            // Update, adjust metadata as needed
            try {
                UploadBuilder uploadBuilder = client.files().uploadBuilder(path);
                //TODO: use session if the file size is greater than 150MB
                FileMetadata fileMetadata = uploadBuilder.withMode(WriteMode.OVERWRITE).start().uploadAndFinish(data);
                file.setId(fileMetadata.getId());
                file.setVersion(fileMetadata.getRev());
                return saveFileMetadata(file, sequenceNumber);
            } catch (UploadErrorException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (DbxException e) {
                throw DropboxExceptionHandler.handle(e);
            } catch (IOException e) {
                throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeDocument(java.lang.String, long)
     */
    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        try {
            Metadata metadata = getMetadata(folderId);
            // Ensure that we are emptying a folder
            if (metadata instanceof FileMetadata) {
                throw FileStorageExceptionCodes.NOT_A_FOLDER.create(DropboxConstants.ID, folderId);
            }

            // Empty the folder
            ListFolderResult listFolder = client.files().listFolder(folderId);
            for (Metadata entry : listFolder.getEntries()) {
                if (entry instanceof FileMetadata) {
                    client.files().delete(entry.getPathDisplay());
                }
            }
        } catch (DeleteErrorException e) {
            // TODO Auto-generated catch block
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeDocument(java.util.List, long)
     */
    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeDocument(java.util.List, long, boolean)
     */
    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber, boolean hardDelete) throws OXException {
        try {
            final List<IDTuple> ret = new ArrayList<IDTuple>(ids.size());
            for (IDTuple id : ids) {
                try {
                    client.files().delete(toPath(id.getFolder(), id.getId()));
                } catch (DeleteErrorException e) {
                    //TODO: maybe log this exception for further analysis
                    //LOG.error("{}", e.getMessage(), e);
                    ret.add(id);
                }
            }
            return ret;
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#touch(java.lang.String, java.lang.String)
     */
    @Override
    public void touch(String folderId, String id) throws OXException {
        exists(folderId, id, CURRENT_VERSION);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.lang.String)
     */
    @Override
    public TimedResult<File> getDocuments(String folderId) throws OXException {
        try {
            ListFolderResult listFolder = client.files().listFolder(folderId);

            int numberOfFiles = listFolder.getEntries().size();
            List<File> files = new ArrayList<>(numberOfFiles);

            for (Metadata metadata : listFolder.getEntries()) {
                if (metadata instanceof FileMetadata) {
                    files.add(new DropboxFile((FileMetadata) metadata, userId));
                }
            }

            return new FileTimedResult(files);
        } catch (GetMetadataErrorException e) {
            // TODO: Maybe introduce new exception codes?
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.lang.String, java.util.List)
     */
    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields) throws OXException {
        return getDocuments(folderId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.lang.String, java.util.List, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection)
     */
    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {
        try {
            ListFolderResult listFolder = client.files().listFolder(folderId);
            Iterator<Metadata> iterator = listFolder.getEntries().iterator();
            final List<File> files = new ArrayList<File>(listFolder.getEntries().size());
            while (iterator.hasNext()) {
                Metadata next = iterator.next();
                if (next instanceof FileMetadata) {
                    files.add(new DropboxFile((FileMetadata) next, userId));
                }
            }
            sort(files, sort, order);
            return new FileTimedResult(files);
        } catch (Exception e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.util.List, java.util.List)
     */
    @Override
    public TimedResult<File> getDocuments(List<IDTuple> ids, List<Field> fields) throws OXException {
        List<File> files = new ArrayList<>(ids.size());
        Map<String, List<String>> filesPerFolder = getFilesPerFolder(ids);
        if (filesPerFolder.size() == 1 && filesPerFolder.values().iterator().next().size() > 2) {
            // Seems like a 'list' request for multiple items from one folder
            String folderId = filesPerFolder.keySet().iterator().next();
            String path = toPath(folderId);
            try {
                ListFolderResult listFolder = client.files().listFolder(path);
                for (IDTuple id : ids) {
                    for (Metadata metadata : listFolder.getEntries()) {
                        if (id.getId().equals(metadata.getName()) && metadata instanceof FileMetadata) {
                            files.add(new DropboxFile((FileMetadata) metadata, userId));
                            break;
                        }
                    }
                }
            } catch (ListFolderErrorException e) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (DbxException e) {
                throw DropboxExceptionHandler.handle(e);
            }
        } else {
            // Load metadata one-by-one
            for (IDTuple id : ids) {
                try {
                    FileMetadata metadata = getFileMetadata(id.getFolder(), id.getId());
                    files.add(new DropboxFile(metadata, userId));
                } catch (OXException e) {
                    // Skip non-existing files
                    if (!FileStorageExceptionCodes.NOT_A_FILE.equals(e)) {
                        throw e;
                    }
                } catch (GetMetadataErrorException e) {
                    // Skip non-existing files
                    if (!LookupError.NOT_FOUND.equals(e.errorValue.getPathValue())) {
                        throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                    }
                } catch (DbxException e) {
                    throw DropboxExceptionHandler.handle(e);
                }
            }
        }
        return new FileTimedResult(files);
    }

    private static final SearchIterator<File> EMPTY_ITER = SearchIteratorAdapter.emptyIterator();

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDelta(java.lang.String, long, java.util.List, boolean)
     */
    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDelta(java.lang.String, long, java.util.List, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection, boolean)
     */
    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#search(java.lang.String, java.util.List, java.lang.String, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection, int, int)
     */
    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
        return search(pattern, fields, folderId, false, sort, order, start, end);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#search(java.lang.String, java.util.List, java.lang.String, boolean, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection, int, int)
     */
    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
        if (folderId == null) {
            // Fall-back to root folder
            folderId = "";
        }

        // Search
        List<File> results = search(pattern, folderId, includeSubfolders);

        // Sort results
        sort(results, sort, order);

        // Range (if needed)
        results = range(results, start, end);
        return new SearchIteratorAdapter<File>(results.iterator(), results.size());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageFileAccess#getAccountAccess()
     */
    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.tx.TransactionAware#startTransaction()
     */
    @Override
    public void startTransaction() throws OXException {
        // no op
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.tx.TransactionAware#commit()
     */
    @Override
    public void commit() throws OXException {
        // no op
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.tx.TransactionAware#rollback()
     */
    @Override
    public void rollback() throws OXException {
        // no op
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.tx.TransactionAware#finish()
     */
    @Override
    public void finish() throws OXException {
        // no op
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.tx.TransactionAware#setTransactional(boolean)
     */
    @Override
    public void setTransactional(boolean transactional) {
        // no op
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.tx.TransactionAware#setRequestTransactional(boolean)
     */
    @Override
    public void setRequestTransactional(boolean transactional) {
        // no op
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.tx.TransactionAware#setCommitsTransaction(boolean)
     */
    @Override
    public void setCommitsTransaction(boolean commits) {
        // no op
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageVersionedFileAccess#removeVersion(java.lang.String, java.lang.String, java.lang.String[])
     */
    @Override
    public String[] removeVersion(String folderId, String id, String[] versions) throws OXException {
        // The Dropbox API does not support removing revisions of a file
        for (final String version : versions) {
            if (version != CURRENT_VERSION) {
                throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(DropboxConstants.ID);
            }
        }
        try {
            client.files().delete(toPath(folderId, id));
            return new String[0];
        } catch (DeleteErrorException e) {
            // TODO: Maybe introduce new exception codes?
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageVersionedFileAccess#getVersions(java.lang.String, java.lang.String)
     */
    @Override
    public TimedResult<File> getVersions(String folderId, String id) throws OXException {
        return getVersions(folderId, id, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageVersionedFileAccess#getVersions(java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields) throws OXException {
        return getVersions(folderId, id, fields, null, SortDirection.DEFAULT);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageVersionedFileAccess#getVersions(java.lang.String, java.lang.String, java.util.List, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection)
     */
    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        try {
            // Fetch all revisions
            ListRevisionsResult revisions = client.files().listRevisions(toPath(folderId, id), 100);
            int numberOfVersions = revisions.getEntries().size();
            List<File> files = new ArrayList<File>(numberOfVersions);

            // Convert to DropboxFiles
            int i = 0;
            for (FileMetadata fileMetadata : revisions.getEntries()) {
                DropboxFile dbxFile = new DropboxFile(fileMetadata, userId);
                dbxFile.setNumberOfVersions(numberOfVersions);
                dbxFile.setIsCurrentVersion(0 == i++);
                files.add(dbxFile);
            }

            // Sort & return
            sort(files, sort, order);
            return new FileTimedResult(files);
        } catch (ListRevisionsErrorException e) {
            // TODO: Maybe introduce new exception codes?
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.FileStorageSequenceNumberProvider#getSequenceNumbers(java.util.List)
     */
    @Override
    public Map<String, Long> getSequenceNumbers(List<String> folderIds) throws OXException {
        Map<String, Long> sequenceNumbers = new HashMap<>(folderIds.size());
        for (String folderId : folderIds) {
            try {
                FolderMetadata metadata = getFolderMetadata(folderId);
                sequenceNumbers.put(folderId, getSequenceNumber(metadata));
            } catch (GetMetadataErrorException e) {
                // TODO: Maybe introduce new exception codes?
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (DbxException e) {
                throw DropboxExceptionHandler.handle(e);
            }
        }
        return sequenceNumbers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.file.storage.ThumbnailAware#getThumbnailStream(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public InputStream getThumbnailStream(String folderId, String id, String version) throws OXException {
        try {
            DbxDownloader<FileMetadata> dbxDownloader = client.files().getThumbnailBuilder(toPath(folderId, id)).withFormat(ThumbnailFormat.JPEG).withSize(ThumbnailSize.W128H128).start();
            return dbxDownloader.getInputStream();
        } catch (ThumbnailErrorException e) {
            // TODO: Maybe introduce new exception codes?
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /**
     * Sorts the supplied list of files if needed.
     *
     * @param files The files to sort
     * @param sort The sort order, or <code>null</code> if not specified
     * @param order The sort direction
     */
    private void sort(List<File> files, Field sort, SortDirection order) {
        if (null != sort && 1 < files.size()) {
            Collections.sort(files, order.comparatorBy(sort));
        }
    }

    /**
     * Generates a mostly unique sequence number for the supplied folder entry, based on the contained {@link FolderMetadata#hashCode()}.
     *
     * @param FolderMetadata The {@link FolderMetadata}
     * @return The sequence number
     */
    private long getSequenceNumber(FolderMetadata metadata) {
        long hashCode = metadata.hashCode();
        return Math.abs(hashCode);
    }

    /**
     * Uploads the specified file in chunks
     * 
     * @param file The {@link File} to upload
     * @param data The {@link InputStream} containing the actual data
     * @return The {@link IDTuple} of the uploaded file
     * @throws OXException If an error is occurred
     */
    private IDTuple sessionUpload(File file, InputStream data) throws OXException {
        ThresholdFileHolder sink = null;
        try {
            sink = new ThresholdFileHolder();
            sink.write(data);

            InputStream stream = sink.getStream();

            // Start an upload session and get the session id
            UploadSessionStartUploader uploadSession = client.files().uploadSessionStart();
            UploadSessionStartResult result = uploadSession.uploadAndFinish(stream, CHUNK_SIZE);
            String sessionId = result.getSessionId();
            long offset = CHUNK_SIZE;

            UploadSessionCursor cursor = new UploadSessionCursor(sessionId, offset);
            while (sink.getCount() - offset > CHUNK_SIZE) {
                client.files().uploadSessionAppendV2(cursor).uploadAndFinish(stream, CHUNK_SIZE);
                offset += CHUNK_SIZE;
                cursor = new UploadSessionCursor(sessionId, offset);
            }

            long remaining = sink.getCount() - offset;
            CommitInfo commitInfo = new CommitInfo(toPath(file.getFolderId(), file.getFileName()));
            UploadSessionFinishUploader sessionFinish = client.files().uploadSessionFinish(cursor, commitInfo);
            FileMetadata metadata = sessionFinish.uploadAndFinish(stream, remaining);

            DropboxFile dbxFile = new DropboxFile(metadata, userId);
            file.copyFrom(dbxFile, Field.ID, Field.FOLDER_ID, Field.VERSION, Field.FILE_SIZE, Field.FILENAME, Field.LAST_MODIFIED, Field.CREATED);
            return dbxFile.getIDTuple();
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(sink);
        }
    }

    /**
     * Uploads the specified file in a single request
     * 
     * @param file The {@link File} to upload
     * @param data The {@link InputStream} containing the actual data
     * @return The {@link IDTuple} of the uploaded file
     * @throws OXException if an error is occurred
     */
    private IDTuple singleUpload(File file, InputStream data) throws OXException {
        String name = file.getFileName();
        String fileName = name;
        try {
            FileMetadata metadata = client.files().upload(new StringBuilder(file.getFolderId()).append('/').append(fileName).toString()).uploadAndFinish(data);
            DropboxFile dbxFile = new DropboxFile(metadata, userId);
            file.copyFrom(dbxFile, Field.ID, Field.FOLDER_ID, Field.VERSION, Field.FILE_SIZE, Field.FILENAME, Field.LAST_MODIFIED, Field.CREATED);
            return dbxFile.getIDTuple();
        } catch (UploadErrorException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        } catch (IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Maps the file identifiers of the supplied ID tuples to their parent folder identifiers.
     *
     * @param ids The ID tuples to map
     * @return The mapped identifiers
     */
    private Map<String, List<String>> getFilesPerFolder(List<IDTuple> ids) {
        Map<String, List<String>> filesPerFolder = new HashMap<String, List<String>>();
        for (IDTuple id : ids) {
            List<String> files = filesPerFolder.get(id.getFolder());
            if (null == files) {
                files = new ArrayList<String>();
                filesPerFolder.put(id.getFolder(), files);
            }
            files.add(id.getId());
        }
        return filesPerFolder;
    }

    /**
     * Search under the specified folder for the specified pattern
     * 
     * @param pattern The pattern to search for
     * @param folderId The folder identifier (full path)
     * @param includeSubfolders If the sub-folders will be included in the search
     * @return A list with {@link File}s matching the specified pattern
     * @throws OXException if an error is occurred
     */
    private List<File> search(String pattern, String folderId, boolean includeSubfolders) throws OXException {
        try {
            if (Strings.isEmpty(pattern) || pattern.equals("*")) {
                // Return everything
                try {
                    return getAllFiles(folderId, true);
                } catch (ListFolderErrorException e) {
                    throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            } else {
                // Search
                try {
                    return fireSearch(folderId, pattern, includeSubfolders);
                } catch (SearchErrorException e) {
                    throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        } catch (DbxException e) {
            throw DropboxExceptionHandler.handle(e);
        }
    }

    /**
     * Retrieves all files from the specified folder.
     * 
     * @param folderId The folder path
     * @param recursive If set to true then it retrieves all files recursively from all folders under the specified folder
     * @return A list with {@link File}s
     * @throws ListFolderErrorException if a list error is occurred
     * @throws DbxException if a generic Dropbox error is occurred
     */
    private List<File> getAllFiles(String folderId, boolean recursive) throws ListFolderErrorException, DbxException {
        List<File> results = new ArrayList<File>();
        ListFolderResult listFolderResult = client.files().listFolderBuilder(folderId).withRecursive(recursive).start();
        boolean hasMore = listFolderResult.getHasMore();
        do {
            List<Metadata> entries = listFolderResult.getEntries();

            for (Metadata metadata : entries) {
                if (metadata instanceof FileMetadata) {
                    results.add(new DropboxFile((FileMetadata) metadata, userId));
                }
            }
            if (hasMore) {
                String cursor = listFolderResult.getCursor();
                listFolderResult = client.files().listFolderContinue(cursor);
            }
        } while (hasMore);

        return results;
    }

    /**
     * Searches in the specified folder for the specified pattern (fires the actual search request to Dropbox.
     * 
     * @param folderId The folder identifier (full path)
     * @param pattern The pattern to search for
     * @param includeSubfolders If the sub-folders will be included in the search
     * @return A list with {@link File}s matching the specified pattern
     * @throws SearchErrorException if a search error is occurred
     * @throws DbxException if a generic Dropbox error is occurred
     */
    private List<File> fireSearch(String folderId, String pattern, boolean includeSubfolders) throws SearchErrorException, DbxException {
        // Search
        SearchResult searchResult = client.files().searchBuilder(folderId, pattern).withMaxResults(1000L).start();
        List<SearchMatch> matches = searchResult.getMatches();

        List<File> results = new ArrayList<File>(matches.size());

        for (SearchMatch match : matches) {
            Metadata metadata = match.getMetadata();
            String parent = getParent(metadata.getPathDisplay());
            if (metadata instanceof FileMetadata && (includeSubfolders || folderId.equals(parent))) {
                results.add(new DropboxFile((FileMetadata) metadata, userId));
            }
        }
        return results;
    }

    /**
     * Returns a sub-list starting from the specified index and ending to the specified index
     * @param files The {@link List} of {@link File}s 
     * @param startIndex The start index
     * @param endIndex The end index
     * @return The sub-list
     */
    private List<File> range(List<File> files, int startIndex, int endIndex) {
        if (startIndex == NOT_SET && endIndex == NOT_SET) {
            return files;
        }
        if (startIndex > files.size()) {
            return Collections.emptyList();
        }
        if (endIndex > files.size()) {
            endIndex = files.size();
        }
        return files.subList(startIndex, endIndex);
    }
}
