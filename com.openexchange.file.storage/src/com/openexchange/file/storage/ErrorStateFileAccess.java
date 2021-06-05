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

import java.io.InputStream;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.Results;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link ErrorStateFileAccess}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
/**
 * {@link ErrorStateFileAccess} - A {@link FileStorageFileAccess} implementation which can be used in case of an account error.
 * <p>
 * If the real files storage is known to be in an error state, this implementation will, at least, return empty search results
 * This is useful to serve them to a client but tag them as defective.
 * </p>
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class ErrorStateFileAccess implements FileStorageFileAccess {

    private final OXException error;
    private final FileStorageAccountAccess accountAccess;

    /**
     * Initializes a new {@link ErrorStateFileAccess}.
     *
     * @param error The current problem preventing to query the remote files
     * @param accountAccess the corresponding {@link FileStorageAccountAccess}
     */
    public ErrorStateFileAccess(OXException error, FileStorageAccountAccess accountAccess) {
        this.error = error;
        this.accountAccess = accountAccess;
    }

    @Override
    public void startTransaction() throws OXException {
        //no-op
    }

    @Override
    public void commit() throws OXException {
        //no-op
    }

    @Override
    public void rollback() throws OXException {
        //no-op
    }

    @Override
    public void finish() throws OXException {
        //no-op
    }

    @Override
    public void setTransactional(boolean transactional) {
        //no-op
    }

    @Override
    public void setRequestTransactional(boolean transactional) {
        //no-op
    }

    @Override
    public void setCommitsTransaction(boolean commits) {
        //no-op
    }

    @Override
    public boolean exists(String folderId, String id, String version) throws OXException {
        return false;
    }

    @Override
    public File getFileMetadata(String folderId, String id, String version) throws OXException {
        return new DefaultFile();
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber) throws OXException {
        throw error;
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        throw error;

    }

    @Override
    public IDTuple copy(IDTuple source, String version, String destFolder, File update, InputStream newFile, List<Field> modifiedFields) throws OXException {
        throw error;
    }

    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<Field> modifiedFields) throws OXException {
        throw error;
    }

    @Override
    public InputStream getDocument(String folderId, String id, String version) throws OXException {
        throw error;
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        throw error;
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        throw error;
    }

    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        throw error;
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        throw error;
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber, boolean hardDelete) throws OXException {
        throw error;
    }

    @Override
    public void touch(String folderId, String id) throws OXException {
        throw error;
    }

    @Override
    public TimedResult<File> getDocuments(String folderId) throws OXException {
        return Results.emptyTimedResult();
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields) throws OXException {
        return Results.emptyTimedResult();
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {
        return Results.emptyTimedResult();
    }

    @Override
    public TimedResult<File> getDocuments(List<IDTuple> ids, List<Field> fields) throws OXException {
        return Results.emptyTimedResult();
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        return Results.emptyDelta();
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        return Results.emptyDelta();
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
        return Results.emptyIterator();
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
        return Results.emptyIterator();
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }
}
