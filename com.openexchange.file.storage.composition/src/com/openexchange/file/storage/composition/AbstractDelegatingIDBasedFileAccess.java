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

package com.openexchange.file.storage.composition;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.Range;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link AbstractDelegatingIDBasedFileAccess} - An abstract implementation for <code>DelegatingIDBasedFileAccess</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public abstract class AbstractDelegatingIDBasedFileAccess implements DelegatingIDBasedFileAccess {

    /** The backing file access */
    protected final IDBasedFileAccess fileAccess;

    /**
     * Initializes a new {@link AbstractDelegatingIDBasedFileAccess}.
     *
     * @param fileAccess The backing file access
     */
    protected AbstractDelegatingIDBasedFileAccess(IDBasedFileAccess fileAccess) {
        super();
        this.fileAccess = fileAccess;
    }

    @Override
    public IDBasedFileAccess getDelegate() {
        return fileAccess;
    }

    @Override
    public void startTransaction() throws OXException {
        fileAccess.startTransaction();
    }

    @Override
    public void commit() throws OXException {
        fileAccess.commit();
    }

    @Override
    public List<OXException> getWarnings() {
        return fileAccess.getWarnings();
    }

    @Override
    public void rollback() throws OXException {
        fileAccess.rollback();
    }

    @Override
    public List<OXException> getAndFlushWarnings() {
        return fileAccess.getAndFlushWarnings();
    }

    @Override
    public void finish() throws OXException {
        fileAccess.finish();
    }

    @Override
    public void addWarning(OXException warning) {
        fileAccess.addWarning(warning);
    }

    @Override
    public void setTransactional(boolean transactional) {
        fileAccess.setTransactional(transactional);
    }

    @Override
    public void setRequestTransactional(boolean transactional) {
        fileAccess.setRequestTransactional(transactional);
    }

    @Override
    public void setCommitsTransaction(boolean commits) {
        fileAccess.setCommitsTransaction(commits);
    }

    @Override
    public void removeWarning(OXException warning) {
        fileAccess.removeWarning(warning);
    }

    @Override
    public boolean supports(String serviceID, String accountID, FileStorageCapability... capabilities) throws OXException {
        return fileAccess.supports(serviceID, accountID, capabilities);
    }

    @Override
    public boolean exists(String id, String version) throws OXException {
        return fileAccess.exists(id, version);
    }

    @Override
    public File getFileMetadata(String id, String version) throws OXException {
        return fileAccess.getFileMetadata(id, version);
    }

    @Override
    public String saveFileMetadata(File document, long sequenceNumber) throws OXException {
        return fileAccess.saveFileMetadata(document, sequenceNumber);
    }

    @Override
    public String saveFileMetadata(File document, long sequenceNumber, List<Field> modifiedColumns) throws OXException {
        return fileAccess.saveFileMetadata(document, sequenceNumber, modifiedColumns);
    }

    @Override
    public String saveFileMetadata(File document, long sequenceNumber, List<Field> modifiedColumns, boolean ignoreWarnings, boolean tryAddVersion) throws OXException {
        return fileAccess.saveFileMetadata(document, sequenceNumber, modifiedColumns, ignoreWarnings, tryAddVersion);
    }

    @Override
    public String copy(String sourceId, String version, String destFolderId, File update, InputStream newData, List<Field> modifiedFields) throws OXException {
        return fileAccess.copy(sourceId, version, destFolderId, update, newData, modifiedFields);
    }

    @Override
    public List<String> move(List<String> sourceIds, long sequenceNumber, String destFolderId, boolean adjustFilenamesAsNeeded) throws OXException {
        return fileAccess.move(sourceIds, sequenceNumber, destFolderId, adjustFilenamesAsNeeded);
    }

    @Override
    public InputStream getDocument(String id, String version) throws OXException {
        return fileAccess.getDocument(id, version);
    }

    @Override
    public InputStream getDocument(String id, String version, long offset, long length) throws OXException {
        return fileAccess.getDocument(id, version, offset, length);
    }

    @Override
    public InputStream optThumbnailStream(String id, String version) throws OXException {
        return fileAccess.optThumbnailStream(id, version);
    }

    @Override
    public Document getDocumentAndMetadata(String id, String version) throws OXException {
        return fileAccess.getDocumentAndMetadata(id, version);
    }

    @Override
    public Document getDocumentAndMetadata(String id, String version, String clientEtag) throws OXException {
        return fileAccess.getDocumentAndMetadata(id, version, clientEtag);
    }

    @Override
    public String saveDocument(File document, InputStream data, long sequenceNumber) throws OXException {
        return fileAccess.saveDocument(document, data, sequenceNumber);
    }

    @Override
    public String saveDocument(File document, InputStream data, long sequenceNumber, List<Field> modifiedColumns) throws OXException {
        return fileAccess.saveDocument(document, data, sequenceNumber, modifiedColumns);
    }

    @Override
    public String saveDocument(File document, InputStream data, long sequenceNumber, List<Field> modifiedColumns, boolean ignoreVersion) throws OXException {
        return fileAccess.saveDocument(document, data, sequenceNumber, modifiedColumns, ignoreVersion);
    }

    @Override
    public String saveDocument(File document, InputStream data, long sequenceNumber, List<Field> modifiedColumns, boolean ignoreVersion, boolean ignoreWarnings, boolean tryAddVersion) throws OXException {
        return fileAccess.saveDocument(document, data, sequenceNumber, modifiedColumns, ignoreVersion, ignoreWarnings, tryAddVersion);
    }

    @Override
    public String saveDocument(File document, InputStream data, long sequenceNumber, List<Field> modifiedColumns, long offset) throws OXException {
        return fileAccess.saveDocument(document, data, sequenceNumber, modifiedColumns, offset);
    }

    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        fileAccess.removeDocument(folderId, sequenceNumber);
    }

    @Override
    public List<String> removeDocument(List<String> ids, long sequenceNumber) throws OXException {
        return fileAccess.removeDocument(ids, sequenceNumber);
    }

    @Override
    public List<String> removeDocument(List<String> ids, long sequenceNumber, boolean hardDelete) throws OXException {
        return fileAccess.removeDocument(ids, sequenceNumber, hardDelete);
    }

    @Override
    public String[] removeVersion(String id, String[] versions) throws OXException {
        return fileAccess.removeVersion(id, versions);
    }

    @Override
    public void unlock(String id) throws OXException {
        fileAccess.unlock(id);
    }

    @Override
    public void lock(String id, long diff) throws OXException {
        fileAccess.lock(id, diff);
    }

    @Override
    public void touch(String id) throws OXException {
        fileAccess.touch(id);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId) throws OXException {
        return fileAccess.getDocuments(folderId);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> columns) throws OXException {
        return fileAccess.getDocuments(folderId, columns);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> columns, Field sort, SortDirection order) throws OXException {
        return fileAccess.getDocuments(folderId, columns, sort, order);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order, Range range) throws OXException {
        return fileAccess.getDocuments(folderId, fields, sort, order, range);
    }

    @Override
    public SearchIterator<File> getUserSharedDocuments(List<Field> fields, Field sort, SortDirection order) throws OXException {
        return fileAccess.getUserSharedDocuments(fields, sort, order);
    }

    @Override
    public TimedResult<File> getVersions(String id) throws OXException {
        return fileAccess.getVersions(id);
    }

    @Override
    public TimedResult<File> getVersions(String id, List<Field> columns) throws OXException {
        return fileAccess.getVersions(id, columns);
    }

    @Override
    public TimedResult<File> getVersions(String id, List<Field> columns, Field sort, SortDirection order) throws OXException {
        return fileAccess.getVersions(id, columns, sort, order);
    }

    @Override
    public TimedResult<File> getDocuments(List<String> ids, List<Field> columns) throws OXException {
        return fileAccess.getDocuments(ids, columns);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> columns, boolean ignoreDeleted) throws OXException {
        return fileAccess.getDelta(folderId, updateSince, columns, ignoreDeleted);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> columns, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        return fileAccess.getDelta(folderId, updateSince, columns, sort, order, ignoreDeleted);
    }

    @Override
    public SearchIterator<File> search(String query, List<Field> cols, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
        return fileAccess.search(query, cols, folderId, sort, order, start, end);
    }

    @Override
    public SearchIterator<File> search(String query, List<Field> cols, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
        return fileAccess.search(query, cols, folderId, includeSubfolders, sort, order, start, end);
    }

    @Override
    public SearchIterator<File> search(List<String> folderIds, SearchTerm<?> searchTerm, List<Field> fields, Field sort, SortDirection order, int start, int end) throws OXException {
        return fileAccess.search(folderIds, searchTerm, fields, sort, order, start, end);
    }

    @Override
    public SearchIterator<File> search(String folderId, boolean includeSubfolders, SearchTerm<?> searchTerm, List<Field> fields, Field sort, SortDirection order, int start, int end) throws OXException {
        return fileAccess.search(folderId, includeSubfolders, searchTerm, fields, sort, order, start, end);
    }

    @Override
    public Map<String, Long> getSequenceNumbers(List<String> folderIds) throws OXException {
        return fileAccess.getSequenceNumbers(folderIds);
    }

    @Override
    public Map<String, String> getETags(List<String> folderIds) throws OXException {
        return fileAccess.getETags(folderIds);
    }

}
