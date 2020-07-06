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

package com.openexchange.file.storage.appsuite;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import com.openexchange.exception.OXException;
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
import com.openexchange.file.storage.FileStorageSequenceNumberProvider;
import com.openexchange.file.storage.FileStorageVersionedFileAccess;
import com.openexchange.file.storage.FileStorageZippableFolderFileAccess;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link AppsuiteFileAccess}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class AppsuiteFileAccess implements ThumbnailAware, FileStorageVersionedFileAccess, FileStorageIgnorableVersionFileAccess, FileStorageSequenceNumberProvider, FileStorageEfficientRetrieval, FileStorageLockedFileAccess, FileStorageZippableFolderFileAccess, FileStorageCaseInsensitiveAccess, FileStorageAutoRenameFoldersAccess {

    private final AppsuiteAccountAccess accountAccess;
    private final ShareClient client;

    /**
     * Initializes a new {@link AppsuiteFileAccess}.
     *
     * @param accountAccess The {@link AccountAccess}
     * @param ShareClient The {@link ShareClient} for accessing the remote OX
     */
    public AppsuiteFileAccess(AppsuiteAccountAccess accountAccess, ShareClient client) {
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
    private AppsuiteFile getMetadata(String folderId, String id, String version) throws OXException {
        return client.getMetaData(folderId, id, version);
    }

    @Override
    public boolean exists(String folderId, String id, String version) throws OXException {
        try {
            getMetadata(folderId, id, version);
            return true;
        }
        catch(OXException e) {
            if(e.similarTo(FileStorageExceptionCodes.FILE_NOT_FOUND)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public AppsuiteFile getFileMetadata(String folderId, String id, String version) throws OXException {
        return getMetadata(folderId, id, version);
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDTuple copy(IDTuple source, String version, String destFolder, File update, InputStream newFile, List<Field> modifiedFields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<Field> modifiedFields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getDocument(String folderId, String id, String version) throws OXException {
        return client.getDocument(folderId, id, version);
    }

    @Override
    public Document getDocumentAndMetadata(String folderId, String fileId, String version) throws OXException {
        return getDocumentAndMetadata(folderId, fileId, version, null);
    }

    @Override
    public Document getDocumentAndMetadata(String folderId, String fileId, String version, String clientETag) throws OXException {
        //TODO: use e-tag
        AppsuiteFile fileMetaData = getMetadata(folderId, fileId, version);
        return new AppsuiteDocument(fileMetaData, () -> getDocument(folderId, fileId, version));
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        return saveDocument(file, data, sequenceNumber, null);
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        return null;
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields, boolean ignoreVersion) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IDTuple saveDocumentTryAddVersion(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        if(file.getId() == NEW) {
            AppsuiteFile newFile = client.saveDocument(file, data);
            return new IDTuple(newFile.getFolderId(), newFile.getId());
        }
        else {
            //TODO: update meta data only
            return null;
        }
    }

    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        // TODO Auto-generated method stub
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber, boolean hardDelete) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void touch(String folderId, String id) throws OXException {
        // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

    @Override
    public InputStream getThumbnailStream(String folderId, String id, String version) throws OXException {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void unlock(String folderId, String id) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void lock(String folderId, String id, long diff) throws OXException {
        // TODO Auto-generated method stub
    }

    @Override
    public String createFolder(FileStorageFolder toCreate, boolean autoRename) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName, boolean autoRename) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

}
