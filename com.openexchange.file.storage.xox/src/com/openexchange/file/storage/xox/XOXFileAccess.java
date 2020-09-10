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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.file.storage.xox;

import static com.openexchange.java.Autoboxing.L;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.openexchange.api.client.common.calls.infostore.DocumentResponse;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAutoRenameFoldersAccess;
import com.openexchange.file.storage.FileStorageCaseInsensitiveAccess;
import com.openexchange.file.storage.FileStorageEfficientRetrieval;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageIgnorableVersionFileAccess;
import com.openexchange.file.storage.FileStorageLockedFileAccess;
import com.openexchange.file.storage.FileStorageRangeFileAccess;
import com.openexchange.file.storage.FileStorageSequenceNumberProvider;
import com.openexchange.file.storage.FileStorageVersionedFileAccess;
import com.openexchange.file.storage.FileStorageZippableFolderFileAccess;
import com.openexchange.file.storage.Range;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link XOXFileAccess}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class XOXFileAccess implements /*@formatter:off*/
                                          ThumbnailAware,
                                          FileStorageVersionedFileAccess,
                                          FileStorageIgnorableVersionFileAccess,
                                          FileStorageSequenceNumberProvider,
                                          FileStorageEfficientRetrieval,
                                          FileStorageLockedFileAccess,
                                          FileStorageZippableFolderFileAccess,
                                          FileStorageCaseInsensitiveAccess,
                                          FileStorageAutoRenameFoldersAccess,
                                          FileStorageRangeFileAccess {
                                          /*@formatter:on*/

    private final XOXAccountAccess accountAccess;
    private final ShareClient client;

    /**
     * Initializes a new {@link XOXFileAccess}.
     *
     * @param accountAccess The {@link XOXAccountAccess}
     * @param client The {@link ShareClient} for accessing the remote OX
     */
    public XOXFileAccess(XOXAccountAccess accountAccess, ShareClient client) {
        this.accountAccess = Objects.requireNonNull(accountAccess, "accountAccess must not be null");
        this.client = Objects.requireNonNull(client, "client must not be null");
    }

    /**
     * Gets a file's meta data
     *
     * @param folderId The ID of the folder the file is part of
     * @param id The ID of the file
     * @param version the version to get the meta data for; may be CURRENT_VERSION
     * @return The file meta data
     * @throws OXException
     */
    private XOXFile getMetadata(String folderId, String id, String version) throws OXException {
        return client.getMetaData(folderId, id, version);
    }

    /**
     * Internal method to update a document meta- and binary data
     *
     * @param file The file metadata
     * @param data The binary data
     * @param sequenceNumber the sequence number
     * @param modifiedFields The fields to update
     * @param tryAddVersion True to add a new version if the filename already exists
     * @return The {@link IDTuple} of the updated file
     * @throws OXException
     */
    private IDTuple saveDocumentInternal(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields, boolean tryAddVersion) throws OXException {
        IDTuple ret = null;
        if (file.getId() == NEW) { /* upload a new file */
            if (file.getFileName() == null) {
                throw FileStorageExceptionCodes.MISSING_FILE_NAME.create(file.getFileName());
            }
            if (data == null) {
                throw FileStorageExceptionCodes.NO_CONTENT.create(file.getFileName());
            }
            ret = client.saveNewDocument(file, data, tryAddVersion);
        } else { /* update an existing file */
            List<Field> fieldsToUpdate = new ArrayList<Field>(null == modifiedFields ? Arrays.asList(Field.values()) : modifiedFields);
            fieldsToUpdate = Field.reduceBy(fieldsToUpdate, Field.FOLDER_ID, Field.FILE_MIMETYPE, Field.FILE_SIZE, Field.FILENAME);
            int[] modifiedColumns = fieldsToUpdate.stream().mapToInt(f -> f.getNumber()).toArray();
            ret = client.updateDocument(file, data, sequenceNumber, modifiedColumns);
        }
        return ret;
    }

    @Override
    public boolean exists(String folderId, String id, String version) throws OXException {
        try {
            getMetadata(folderId, id, version);
            return true;
        } catch (OXException e) {
            if (e.similarTo(FileStorageExceptionCodes.FILE_NOT_FOUND)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public XOXFile getFileMetadata(String folderId, String id, String version) throws OXException {
        return getMetadata(folderId, id, version);
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber) throws OXException {
        return saveFileMetadata(file, sequenceNumber, ALL_FIELDS);
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        if (file.getId() == NEW) {
            return saveDocument(file, null, sequenceNumber, modifiedFields);
        }
        List<Field> fieldsToUpdate = new ArrayList<Field>(null == modifiedFields ? Arrays.asList(Field.values()) : modifiedFields);
        int[] modifiedColumns = fieldsToUpdate.stream().mapToInt(f -> f.getNumber()).toArray();
        return client.updateDocument(file, sequenceNumber, modifiedColumns);
    }

    @Override
    public IDTuple copy(IDTuple source, String version, String destFolder, File update, InputStream newFile, List<Field> modifiedFields) throws OXException {
        DefaultFile fileToUpdate = new DefaultFile(update);
        fileToUpdate.setFolderId(destFolder);
        List<Field> fieldsToUpdate = new ArrayList<Field>(null == modifiedFields ? Arrays.asList(Field.values()) : modifiedFields);
        fieldsToUpdate = Field.reduceBy(fieldsToUpdate, Field.ID); //The field ID must not be present in the meta of the destination object
        int[] modifiedColumns = fieldsToUpdate.stream().mapToInt(f -> f.getNumber()).toArray();

        return client.copyDocument(source.getId(), fileToUpdate, modifiedColumns, newFile);
    }

    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<Field> modifiedFields) throws OXException {
        //IDTuple newId = client.moveDocument(source.getId(), destFolder, sequenceNumber);
        //XOXFile movedFile = getMetadata(newId.getFolder(), newId.getId(), CURRENT_VERSION);
        //return update != null ? saveFileMetadata(movedFile, movedFile.getSequenceNumber(), modifiedFields) : newId;

        IDTuple newId = client.moveDocument(source.getId(), destFolder, sequenceNumber);
        if(modifiedFields != null) {
            XOXFile movedFile = new XOXFile(update);
            movedFile.setSequenceNumber(getMetadata(newId.getFolder(), newId.getId(), CURRENT_VERSION).getSequenceNumber());
            return update != null ? saveFileMetadata(movedFile, movedFile.getSequenceNumber(), modifiedFields) : newId;
        }
        return newId;
    }

    @Override
    public InputStream getDocument(String folderId, String id, String version) throws OXException {
        return client.getDocument(folderId, id, version).getInputStream();
    }

    @Override
    public Document getDocumentAndMetadata(String folderId, String fileId, String version) throws OXException {
        return getDocumentAndMetadata(folderId, fileId, version, null);
    }

    @Override
    public Document getDocumentAndMetadata(String folderId, String fileId, String version, String clientETag) throws OXException {
        XOXFile fileMetaData = getMetadata(folderId, fileId, version);
        DocumentResponse response = client.getDocument(folderId, fileId, version, clientETag);
        return new XOXDocument(fileMetaData, () -> response.getInputStream(), response.getETag());
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        return saveDocumentInternal(file, data, sequenceNumber, null, false);
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        return saveDocumentInternal(file, data, sequenceNumber, modifiedFields, false);
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields, boolean ignoreVersion) throws OXException {
        //@formatter:off
        return ignoreVersion ?
            saveDocumentInternal(file, data, sequenceNumber, modifiedFields, false) :
            saveDocumentTryAddVersion(file, data, sequenceNumber, modifiedFields);
        //@formatter:on
    }

    @Override
    public IDTuple saveDocumentTryAddVersion(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        return saveDocumentInternal(file, data, sequenceNumber, modifiedFields, false);
    }

    /**
     * Removes all documents from the given folder
     *
     * @param folderId The ID of the folder to remove
     * @param sequenceNumber The sequencenumber
     * @param hardDelete The hardDelete flag
     * @throws OXException In case of an error
     */
    public void removeDocument(String folderId, long sequenceNumber, boolean hardDelete) throws OXException {
        List<IDTuple> ids = new ArrayList<IDTuple>();
        try (SearchIterator<File> iterator = getDocuments(folderId, Arrays.asList(Field.ID, Field.FOLDER_ID)).results()) {
            while (iterator.hasNext()) {
                File document = iterator.next();
                ids.add(new IDTuple(document.getFolderId(), document.getId()));
            }
        }
        removeDocument(ids, sequenceNumber, hardDelete);

    }

    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        removeDocument(folderId, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber, boolean hardDelete) throws OXException {
        client.removeDocuments(ids, sequenceNumber, hardDelete);
        return Collections.emptyList();
    }

    @Override
    public void touch(String folderId, String id) throws OXException {
        exists(folderId, folderId, CURRENT_VERSION);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId) throws OXException {
        return getDocuments(folderId, ALL_FIELDS);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields) throws OXException {
        return getDocuments(folderId, fields, null, SortDirection.DEFAULT);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {
        return client.getDocuments(folderId, fields, sort, order);
    }

    @Override
    public TimedResult<File> getDocuments(List<IDTuple> ids, List<Field> fields) throws OXException {
        return client.getDocuments(ids, fields);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order, Range range) throws OXException {
        return client.getDocuments(folderId, fields, sort, order, range);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        return getDelta(folderId, updateSince, fields, null, SortDirection.DEFAULT, ignoreDeleted);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        return client.getDelta(folderId, updateSince, fields, sort, order, ignoreDeleted);
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
        return search(pattern, fields, folderId, false, sort, order, start, end);
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
        return client.search(pattern, fields, folderId, includeSubfolders, sort, order, start, end);
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

    @Override
    public InputStream getThumbnailStream(String folderId, String id, String version) throws OXException {
        return getDocument(folderId, id, version);
    }

    @Override
    public void startTransaction() throws OXException {
        /* no-op */
    }

    @Override
    public void commit() throws OXException {
        /* no-op */
    }

    @Override
    public void rollback() throws OXException {
        /* no-op */
    }

    @Override
    public void finish() throws OXException {
        /* no-op */
    }

    @Override
    public void setTransactional(boolean transactional) {
        /* no-op */
    }

    @Override
    public void setRequestTransactional(boolean transactional) {
        /* no-op */
    }

    @Override
    public void setCommitsTransaction(boolean commits) {
        /* no-op */
    }

    @Override
    public String[] removeVersion(String folderId, String id, String[] versions) throws OXException {
        return client.deleteVersions(id, folderId, DISTANT_FUTURE, versions);
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id) throws OXException {
        return getVersions(folderId, id, ALL_FIELDS, null, SortDirection.DEFAULT);
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields) throws OXException {
        return getVersions(folderId, id, fields, null, SortDirection.DEFAULT);
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        return client.getVersions(id, fields, sort, order);

    }

    @Override
    public Map<String, Long> getSequenceNumbers(List<String> folderIds) throws OXException {
        HashMap<String, Long> ret = new HashMap<String, Long>(folderIds.size());
        for (String folderId : folderIds) {
            TimedResult<File> document = getDocuments(folderId, Arrays.asList(Field.SEQUENCE_NUMBER));
            ret.put(folderId, L(document.sequenceNumber()));
        }
        return ret;
    }

    @Override
    public void unlock(String folderId, String id) throws OXException {
        client.Unlock(id);
    }

    @Override
    public void lock(String folderId, String id, long diff) throws OXException {
        client.lock(id, diff);
    }

    @Override
    public String createFolder(FileStorageFolder toCreate, boolean autoRename) throws OXException {
        return client.createaFolder(toCreate, autoRename);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName, boolean autoRename) throws OXException {
        return client.moveFolder(folderId, newParentId, newName, DISTANT_FUTURE, autoRename);
    }

}
