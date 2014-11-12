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

package com.openexchange.file.storage.dropbox;

import static com.openexchange.file.storage.dropbox.Utils.handle;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.ThumbFormat;
import com.dropbox.client2.DropboxAPI.ThumbSize;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
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
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.file.storage.dropbox.access.DropboxOAuthAccess;
import com.openexchange.file.storage.search.AndTerm;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.OrTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link DropboxFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DropboxFileAccess extends AbstractDropboxAccess implements ThumbnailAware, FileStorageSequenceNumberProvider {

    private final DropboxAccountAccess accountAccess;
    private final int userId;

    /**
     * Initializes a new {@link DropboxFileAccess}.
     */
    public DropboxFileAccess(final DropboxOAuthAccess dropboxOAuthAccess, final FileStorageAccount account, final Session session, final DropboxAccountAccess accountAccess) {
        super(dropboxOAuthAccess, account, session);
        this.accountAccess = accountAccess;
        userId = session.getUserId();
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
        String path = toPath(folderId, id);
        try {
            final Entry entry = dropboxAPI.metadata(path, 1, null, false, version);
            return !entry.isDir && !entry.isDeleted;
        } catch (Exception e) {
            OXException x = handle(e, path);
            if (DropboxExceptionCodes.NOT_FOUND.equals(x)) {
                return false;
            }
            throw x;
        }
    }

    @Override
    public File getFileMetadata(final String folderId, final String id, final String version) throws OXException {
        String path = toPath(folderId, id);
        try {
            final Entry entry = dropboxAPI.metadata(path, 1, null, false, version);
            if (entry.isDir) {
                throw DropboxExceptionCodes.NOT_A_FILE.create(path);
            }
            if (entry.isDeleted) {
                throw DropboxExceptionCodes.NOT_FOUND.create(path);
            }
            return new DropboxFile(entry, userId);
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public IDTuple saveFileMetadata(final File file, final long sequenceNumber) throws OXException {
        return saveFileMetadata(file, sequenceNumber, null);
    }

    @Override
    public IDTuple saveFileMetadata(final File file, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        if (FileStorageFileAccess.NEW == file.getId()) {
            /*
             * create new, empty file ("touch")
             */
            String path = toPath(file.getFolderId(), file.getFileName());
            try {
                Entry entry = dropboxAPI.putFile(path, Streams.EMPTY_INPUT_STREAM, 0, null, null);
                file.setId(entry.fileName());
                file.setVersion(entry.rev);
                return new IDTuple(toId(entry.path), entry.fileName());
            } catch (Exception e) {
                throw handle(e, path);
            }
        } else {
            /*
             * only rename possible
             */
            if (null == modifiedFields || modifiedFields.contains(Field.FILENAME)) {
                String path = toPath(file.getFolderId(), file.getId());
                String toPath = toPath(file.getFolderId(), file.getFileName());
                if (false == path.equals(toPath)) {
                    try {
                        if (Strings.equalsNormalizedIgnoreCase(path, toPath)) {
                            Entry temp = dropboxAPI.move(
                                path, toPath(file.getFolderId(), UUID.randomUUID().toString() + ' ' + file.getFileName()));
                            path = temp.path;
                        }
                        Entry entry = dropboxAPI.move(path, toPath);
                        file.setId(entry.fileName());
                        file.setVersion(entry.rev);
                        return new IDTuple(toId(entry.path), entry.fileName());
                    } catch (Exception e) {
                        throw handle(e, path);
                    }
                }
            }
            return new IDTuple(file.getFolderId(), file.getId());
        }
    }

    @Override
    public IDTuple copy(final IDTuple source, String version, final String destFolder, final File update, final InputStream newFil, final List<Field> modifiedFields) throws OXException {
        if (version != CURRENT_VERSION) {
            // can only copy the current revision
            throw DropboxExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
        }
        String path = toPath(source.getFolder(), source.getId());
        String destName = null != update && null != modifiedFields && modifiedFields.contains(Field.FILENAME) ? update.getFileName() : source.getId();
        try {
            /*
             * ensure filename uniqueness in target folder
             */
            for (int i = 1; exists(destFolder, destName, CURRENT_VERSION); i++) {
                destName = FileStorageUtility.enhance(destName, i);
            }
            /*
             * perform copy
             */
            Entry entry = dropboxAPI.copy(path, toPath(destFolder, destName));
            return new IDTuple(entry.parentPath(), entry.fileName());
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<File.Field> modifiedFields) throws OXException {
        String path = toPath(source.getFolder(), source.getId());
        String destName = null != update && null != modifiedFields && modifiedFields.contains(Field.FILENAME) ? update.getFileName() : source.getId();
        String destPath = toPath(destFolder, destName);
        try {
            Entry entry = dropboxAPI.move(path, destPath);
            return new IDTuple(entry.parentPath(), entry.fileName());
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public InputStream getDocument(final String folderId, final String id, final String version) throws OXException {
        String path = toPath(folderId, id);
        try {
            return dropboxAPI.getFileStream(path, version);
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public InputStream getThumbnailStream(String folderId, String id, String version) throws OXException {
        String path = toPath(folderId, id);
        try {
            return dropboxAPI.getThumbnailStream(path, ThumbSize.ICON_128x128, ThumbFormat.JPEG);
        } catch (Exception e) {
            OXException x = handle(e, path);
            if (DropboxExceptionCodes.NOT_FOUND.equals(x)) {
                return null;
            }
            throw x;
        }
    }

    @Override
    public IDTuple saveDocument(final File file, final InputStream data, final long sequenceNumber) throws OXException {
        return saveDocument(file, data, sequenceNumber, null);
    }

    @Override
    public IDTuple saveDocument(final File file, final InputStream data, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        String path = FileStorageFileAccess.NEW == file.getId() ? null : toPath(file.getFolderId(), file.getId());
        try {
            final long fileSize = file.getFileSize();
            final long length = fileSize > 0 ? fileSize : -1L;
            final Entry entry;
            if (Strings.isEmpty(path) || !exists(file.getFolderId(), file.getId(), CURRENT_VERSION)) {
                // Create
                entry = dropboxAPI.putFile(
                    new StringBuilder(file.getFolderId()).append('/').append(file.getFileName()).toString(),
                    data,
                    length,
                    null,
                    null);
                file.setId(entry.fileName());
                file.setVersion(entry.rev);
                return new IDTuple(toId(entry.path), entry.fileName());
            } else {
                // Update, adjust metadata as needed
                entry = dropboxAPI.putFileOverwrite(path, data, length, null);
                file.setId(entry.fileName());
                file.setVersion(entry.rev);
                return saveFileMetadata(file, sequenceNumber);
            }
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public void removeDocument(final String folderId, final long sequenceNumber) throws OXException {
        String path = toPath(folderId);
        try {
            final Entry directoryEntry = dropboxAPI.metadata(path, 0, null, true, null);
            if (!directoryEntry.isDir) {
                throw DropboxExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            for (final Entry childEntry : directoryEntry.contents) {
                if (!childEntry.isDir && !childEntry.isDeleted) {
                    dropboxAPI.delete(childEntry.path);
                }
            }
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber, boolean hardDelete) throws OXException {
        try {
            final List<IDTuple> ret = new ArrayList<IDTuple>(ids.size());
            for (final IDTuple id : ids) {
                String path = toPath(id.getFolder(), id.getId());
                try {
                    dropboxAPI.delete(path);
                } catch (final DropboxServerException e) {
                    if (404 != e.error) {
                        ret.add(id);
                    }
                }
            }
            return ret;
        } catch (Exception e) {
            throw handle(e, null);
        }
    }

    @Override
    public String[] removeVersion(final String folderId, final String id, final String[] versions) throws OXException {
        /*
         * Dropbox API does not support removing revisions of a file
         */
        for (final String version : versions) {
            if (version != CURRENT_VERSION) {
                throw DropboxExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
            }
        }
        String path = toPath(folderId, id);
        try {
            dropboxAPI.delete(path);
            return new String[0];
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                return new String[0];
            }
            throw handleServerError(path, e);
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
        String path = toPath(folderId);
        try {
            final Entry directoryEntry = dropboxAPI.metadata(path, 0, null, true, null);
            if (!directoryEntry.isDir) {
                throw DropboxExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final List<Entry> contents = directoryEntry.contents;
            final List<File> files = new ArrayList<File>(contents.size());
            for (final Entry childEntry : contents) {
                if (!childEntry.isDir && !childEntry.isDeleted) {
                    files.add(new DropboxFile(childEntry, userId));
                }
            }
            return new FileTimedResult(files);
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields) throws OXException {
        return getDocuments(folderId);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        String path = toPath(folderId);
        try {
            final Entry directoryEntry = dropboxAPI.metadata(path, 0, null, true, null);
            if (!directoryEntry.isDir) {
                throw DropboxExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final List<Entry> contents = directoryEntry.contents;
            final List<File> files = new ArrayList<File>(contents.size());
            for (final Entry childEntry : contents) {
                if (!childEntry.isDir && !childEntry.isDeleted) {
                    files.add(new DropboxFile(childEntry, userId));
                }
            }
            // Sort collection if needed
            sort(files, sort, order);
            return new FileTimedResult(files);
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id) throws OXException {
        String path = toPath(folderId, id);
        try {
            final List<Entry> revisions = dropboxAPI.revisions(path, 0);
            final List<File> files = new ArrayList<File>(revisions.size());
            for (final Entry revisionEntry : revisions) {
                files.add(new DropboxFile(revisionEntry, userId));
            }
            return new FileTimedResult(files);
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id, final List<Field> fields) throws OXException {
        return getVersions(folderId, id);
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id, final List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        String path = toPath(folderId, id);
        try {
            final List<Entry> revisions = dropboxAPI.revisions(path, 0);
            final List<File> files = new ArrayList<File>(revisions.size());
            for (final Entry revisionEntry : revisions) {
                files.add(new DropboxFile(revisionEntry, userId));
            }
            // Sort collection
            sort(files, sort, order);
            return new FileTimedResult(files);
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    @Override
    public TimedResult<File> getDocuments(final List<IDTuple> ids, final List<Field> fields) throws OXException {
        try {
            List<File> files = new ArrayList<File>(ids.size());
            Map<String, List<String>> filesPerFolder = getFilesPerFolder(ids);
            if (1 == filesPerFolder.size() && 2 < filesPerFolder.values().iterator().next().size()) {
                /*
                 * seems like a "list" request for multiple items from one folder, get metadata via common folder
                 */
                String folderID  = filesPerFolder.keySet().iterator().next();
                String path = toPath(folderID);
                Entry directoryEntry = dropboxAPI.metadata(path, 0, null, true, null);
                if (false == directoryEntry.isDir) {
                    throw DropboxExceptionCodes.NOT_A_FOLDER.create(folderID);
                }
                for (IDTuple id : ids) {
                    for (Entry entry : directoryEntry.contents) {
                        if (id.getId().equals(entry.fileName()) && false == entry.isDeleted && false == entry.isDir) {
                            files.add(new DropboxFile(entry, userId));
                            break;
                        }
                    }
                }
                return new FileTimedResult(files);
            } else {
                /*
                 * load metadata one-by-one
                 */
                for (IDTuple id : ids) {
                    String path = toPath(id.getFolder(), id.getId());
                    try {
                        Entry entry = dropboxAPI.metadata(path, 1, null, false, null);
                        if (!entry.isDeleted && !entry.isDir) {
                            files.add(new DropboxFile(entry, userId));
                        }
                    } catch (Exception e) {
                        // skip non-existing file in result
                        OXException x = handle(e, path);
                        if (false == DropboxExceptionCodes.NOT_FOUND.equals(x)) {
                            throw x;
                        }
                    }
                }
            }
            return new FileTimedResult(files);
        } catch (Exception e) {
            throw handle(e, null);
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
        /*
         * search in one or all folders only
         */
        if (null != folderIds && 1 != folderIds.size()) {
            throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create("Can only search in one or all folders");
        }
        String folderID = null == folderIds ? null : folderIds.get(0);
        /*
         * search by one or more filename patterns
         */
        List<String> patterns = extractPatterns(searchTerm);
        List<File> files = new LinkedList<File>();
        for (String pattern : patterns) {
            files.addAll(searchInFolder(folderID, pattern));
        }
        return getSearchIterator(files, sort, order, start, end);
    }

    private static List<String> extractPatterns(SearchTerm<?> searchTerm) throws OXException {
        if (FileNameTerm.class.isInstance(searchTerm)) {
            /*
             * single filename pattern
             */
            return Collections.singletonList(((FileNameTerm) searchTerm).getPattern());
        } else if (OrTerm.class.isInstance(searchTerm)) {
            /*
             * try multiple filename patterns
             */
            List<SearchTerm<?>> nestedTerms = ((OrTerm) searchTerm).getPattern();
            List<String> patterns = new ArrayList<String>(nestedTerms.size());
            for (SearchTerm<?> nestedTerm : nestedTerms) {
                if (FileNameTerm.class.isInstance(nestedTerm)) {
                    patterns.add(((FileNameTerm) nestedTerm).getPattern());
                } else {
                    throw FileStorageExceptionCodes.SEARCH_TERM_NOT_SUPPORTED.create(searchTerm.getClass().getSimpleName());
                }
            }
            return patterns;
        } else if (AndTerm.class.isInstance(searchTerm)) {
            /*
             * construct single filename pattern
             */
            List<SearchTerm<?>> nestedTerms = ((AndTerm) searchTerm).getPattern();
            StringBuilder patternBuilder = new StringBuilder();
            for (SearchTerm<?> nestedTerm : nestedTerms) {
                if (FileNameTerm.class.isInstance(nestedTerm)) {
                    patternBuilder.append(((FileNameTerm) nestedTerm).getPattern()).append(' ');
                } else {
                    throw FileStorageExceptionCodes.SEARCH_TERM_NOT_SUPPORTED.create(searchTerm.getClass().getSimpleName());
                }
            }
            return Collections.singletonList(patternBuilder.toString().trim());
        }
        throw FileStorageExceptionCodes.SEARCH_TERM_NOT_SUPPORTED.create(searchTerm.getClass().getSimpleName());
    }

    @Override
    public SearchIterator<File> search(final String pattern, final List<Field> fields, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        return getSearchIterator(searchInFolder(folderId, pattern), sort, order, start, end);
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
                    files.add(new DropboxFile(childEntry, userId));
                }
            }
        }
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

    @Override
    public Map<String, Long> getSequenceNumbers(List<String> folderIds) throws OXException {
        Map<String, Long> sequenceNumbers = new HashMap<String, Long>(folderIds.size());
        for (String folderId : folderIds) {
            String path = toPath(folderId);
            try {
                Entry entry = dropboxAPI.metadata(toPath(folderId), 0, null, true, null);
                if (false == entry.isDir || entry.isDeleted) {
                    throw DropboxExceptionCodes.NOT_FOUND.create(folderId);
                }
                sequenceNumbers.put(folderId, getSequenceNumber(entry));
            } catch (Exception e) {
                handle(e, path);
            }
        }
        return sequenceNumbers;
    }

    /**
     * Searches files matching the supplied pattern in a folder.
     *
     * @param folderId The ID of the folder to search in, or <code>null</code> to search in all folders
     * @param pattern The pattern
     * @return The found files
     * @throws OXException
     * @throws DropboxException
     */
    private List<File> searchInFolder(String folderId, String pattern) throws OXException {
        String path = toPath(folderId);
        try {
            if (null == path) {
                /*
                 * all folders (dropbox search is recursive by default)
                 */
                return searchInPath("/", pattern, true);
            } else {
                /*
                 * specific folder
                 */
                return searchInPath(path, pattern, false);
            }
        } catch (Exception e) {
            throw handle(e, path);
        }
    }

    /**
     * Searches files matching the supplied pattern in a dropbox folder path.
     *
     * @param folderPath The dropbox folder path
     * @param pattern The pattern
     * @param recursive <code>true</code> to search in the supplied folder and all subfolders recursively, <code>false</code>, to include
     *                  matches in the supplied folder only
     * @return The found files
     * @throws OXException
     * @throws DropboxException
     */
    private List<File> searchInPath(String folderPath, String pattern, boolean recursive) throws OXException, DropboxException {
        if (Strings.isEmpty(pattern)) {
            List<File> files = new LinkedList<File>();
            gatherAllFiles(folderPath, files);
            return files;
        }
        // Dropbox API only supports searching by file name
        List<Entry> results = dropboxAPI.search(folderPath, pattern, 0, false);
        List<File> files = new ArrayList<File>(results.size());
        for (Entry entry : results) {
            if (false == entry.isDir && (recursive ||
                Utils.normalizeFolderId(folderPath).equals(Utils.normalizeFolderId(entry.parentPath())))) {
                files.add(new DropboxFile(entry, userId));
            }
        }
        return files;
    }

    /**
     * Wraps the supplied files into a search iterator, respecting the given sort order and ranges.
     *
     * @param files The files
     * @param sort The sort field
     * @param order The sort direction
     * @param start The start index
     * @param end The end index
     * @return The search iterator
     */
    private static SearchIterator<File> getSearchIterator(List<File> files, Field sort, final SortDirection order, int start, int end) {
        if (files.isEmpty()) {
            return SearchIteratorAdapter.emptyIterator();
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
     * Generates a mostly unique sequence number for the supplied folder entry, based on the contained {@link Entry#hash} member.
     *
     * @param entry The entry
     * @return The sequence number
     */
    private static long getSequenceNumber(Entry entry) {
        if (null == entry.hash) {
            return 0;
        }
        long hash = 1125899906842597L;
        for (int i = 0; i < entry.hash.length(); i++) {
            hash = 31 * hash + entry.hash.charAt(i);
        }
        return hash;
    }

    /**
     * Maps the file identifiers of the supplied ID tuples to their parent folder identifiers.
     *
     * @param ids The ID tuples to map
     * @return The mapped identifiers
     */
    private static Map<String, List<String>> getFilesPerFolder(List<IDTuple> ids) {
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

}

