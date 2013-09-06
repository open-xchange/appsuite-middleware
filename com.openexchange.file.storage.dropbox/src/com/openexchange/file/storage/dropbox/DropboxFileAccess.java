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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.dropbox.session.DropboxOAuthAccess;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAllocator;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link DropboxFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DropboxFileAccess extends AbstractDropboxAccess implements FileStorageFileAccess {

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
        try {
            final Entry entry = dropboxAPI.metadata(id, 1, null, false, version);
            return !entry.isDir && !entry.isDeleted;
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                return false;
            }
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public File getFileMetadata(final String folderId, final String id, final String version) throws OXException {
        try {
            final Entry entry = dropboxAPI.metadata(id, 1, null, false, version);
            if (entry.isDir) {
                throw DropboxExceptionCodes.NOT_A_FILE.create(id);
            }
            if (entry.isDeleted) {
                throw DropboxExceptionCodes.NOT_FOUND.create(id);
            }
            return new DropboxFile(entry.parentPath(), entry.path, userId).parseDropboxFile(entry);
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                throw DropboxExceptionCodes.NOT_FOUND.create(e, id);
            }
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
    public IDTuple copy(final IDTuple source, final String destFolder, final File update, final InputStream newFil, final List<Field> modifiedFields) throws OXException {
        final String id = source.getId();
        try {
            final String name = id.substring(id.lastIndexOf('/') + 1);
            final String destPath = toPath(destFolder);
            final int pos = destPath.lastIndexOf('/');
            final Entry entry = dropboxAPI.copy(id, pos > 0 ? new StringAllocator(destPath).append('/').append(name).toString() : name);
            return new IDTuple(entry.parentPath(), entry.path);
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                throw DropboxExceptionCodes.NOT_FOUND.create(e, id);
            }
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<File.Field> modifiedFields) throws OXException {
        final String id = source.getId();
        try {
            final String name = null != update && null != modifiedFields && modifiedFields.contains(Field.FILENAME) ?
                update.getFileName() : id.substring(id.lastIndexOf('/') + 1);
            final String destPath = toPath(destFolder);
            final int pos = destPath.lastIndexOf('/');
            final Entry entry = dropboxAPI.move(id, pos > 0 ? new StringAllocator(destPath).append('/').append(name).toString() : name);
            return new IDTuple(entry.parentPath(), entry.path);
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                throw DropboxExceptionCodes.NOT_FOUND.create(e, id);
            }
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public InputStream getDocument(final String folderId, final String id, final String version) throws OXException {
        try {
            return dropboxAPI.getFileStream(id, version);
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                throw DropboxExceptionCodes.NOT_FOUND.create(e, id);
            }
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void saveDocument(final File file, final InputStream data, final long sequenceNumber) throws OXException {
        saveDocument(file, data, sequenceNumber, null);
    }

    @Override
    public void saveDocument(final File file, final InputStream data, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        final String id = file.getId();
        try {
            final long fileSize = file.getFileSize();
            final long length = fileSize > 0 ? fileSize : -1L;
            final Entry entry;
            if (isEmpty(id) || !exists(null, id, CURRENT_VERSION)) {
                // Create
                entry = dropboxAPI.putFile(
                    new StringAllocator(file.getFolderId()).append('/').append(file.getFileName()).toString(),
                    data,
                    length,
                    null,
                    null);
            } else {
                // Update
                entry = dropboxAPI.putFileOverwrite(id, data, length, null);
            }
            file.setId(entry.path);
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                throw DropboxExceptionCodes.NOT_FOUND.create(e, id);
            }
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void removeDocument(final String folderId, final long sequenceNumber) throws OXException {
        try {
            final Entry directoryEntry = dropboxAPI.metadata(toPath(folderId), 0, null, true, null);
            if (!directoryEntry.isDir) {
                throw DropboxExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            for (final Entry childEntry : directoryEntry.contents) {
                if (!childEntry.isDir && !childEntry.isDeleted) {
                    dropboxAPI.delete(childEntry.path);
                }
            }
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                throw DropboxExceptionCodes.NOT_FOUND.create(e, folderId);
            }
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber) throws OXException {
        try {
            final List<IDTuple> ret = new ArrayList<IDTuple>(ids.size());
            for (final IDTuple id : ids) {
                try {
                    dropboxAPI.delete(id.getId());
                } catch (final DropboxServerException e) {
                    if (404 != e.error) {
                        ret.add(id);
                    }
                }
            }
            return ret;
        } catch (final DropboxServerException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
        try {
            dropboxAPI.delete(id);
            return new String[0];
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                return new String[0];
            }
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
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
        try {
            final Entry directoryEntry = dropboxAPI.metadata(toPath(folderId), 0, null, true, null);
            if (!directoryEntry.isDir) {
                throw DropboxExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final List<Entry> contents = directoryEntry.contents;
            final List<File> files = new ArrayList<File>(contents.size());
            for (final Entry childEntry : contents) {
                if (!childEntry.isDir && !childEntry.isDeleted) {
                    files.add(new DropboxFile(folderId, childEntry.path, userId).parseDropboxFile(childEntry));
                }
            }
            return new FileTimedResult(files);
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                throw DropboxExceptionCodes.NOT_FOUND.create(e, folderId);
            }
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
                throw DropboxExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            final List<Entry> contents = directoryEntry.contents;
            final List<File> files = new ArrayList<File>(contents.size());
            for (final Entry childEntry : contents) {
                if (!childEntry.isDir && !childEntry.isDeleted) {
                    files.add(new DropboxFile(folderId, childEntry.path, userId).parseDropboxFile(childEntry));
                }
            }
            // Sort collection
            Collections.sort(files, order.comparatorBy(sort));
            return new FileTimedResult(files);
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                throw DropboxExceptionCodes.NOT_FOUND.create(e, folderId);
            }
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id) throws OXException {
        try {
            final List<Entry> revisions = dropboxAPI.revisions(id, 0);
            final List<File> files = new ArrayList<File>(revisions.size());
            for (final Entry revisionEntry : revisions) {
                files.add(new DropboxFile(folderId, id, userId).parseDropboxFile(revisionEntry));
            }
            return new FileTimedResult(files);
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                throw DropboxExceptionCodes.NOT_FOUND.create(e, id);
            }
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
                files.add(new DropboxFile(folderId, id, userId).parseDropboxFile(revisionEntry));
            }
            // Sort collection
            Collections.sort(files, order.comparatorBy(sort));
            return new FileTimedResult(files);
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                throw DropboxExceptionCodes.NOT_FOUND.create(e, id);
            }
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public TimedResult<File> getDocuments(final List<IDTuple> ids, final List<Field> fields) throws OXException {
        try {
            final List<File> files = new ArrayList<File>(ids.size());
            for (final IDTuple id : ids) {
                final Entry entry = dropboxAPI.metadata(id.getId(), 1, null, false, null);
                if (!entry.isDeleted && !entry.isDir) {
                    files.add(new DropboxFile(id.getFolder(), id.getId(), userId).parseDropboxFile(entry));
                }
            }
            return new FileTimedResult(files);
        } catch (final DropboxServerException e) {
            if (404 == e.error) {
                throw DropboxExceptionCodes.NOT_FOUND.create(e, e.reason);
            }
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
    public SearchIterator<File> search(final String pattern, final List<Field> fields, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        try {
            if (isEmpty(pattern)) {
                List<File> files = new LinkedList<File>();
                gatherAllFiles("/", files);
                // Sort collection
                Collections.sort(files, order.comparatorBy(sort));
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
                    files.add(new DropboxFile(folderId, resultsEntry.path, userId).parseDropboxFile(resultsEntry));
                }
            }
            // Sort collection
            Collections.sort(files, order.comparatorBy(sort));
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
        } catch (final DropboxServerException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final DropboxException e) {
            throw DropboxExceptionCodes.DROPBOX_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw DropboxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
                    files.add(new DropboxFile(toId(path), childPath, userId).parseDropboxFile(childEntry));
                }
            }
        }
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
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
