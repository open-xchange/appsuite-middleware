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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.file.storage.cifs;

import static com.openexchange.file.storage.cifs.Utils.checkFolderId;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageIgnorableVersionFileAccess;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.cifs.cache.SmbFileMapManagement;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Charsets;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tx.TransactionException;

/**
 * {@link CIFSFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CIFSFileAccess extends AbstractCIFSAccess implements FileStorageIgnorableVersionFileAccess {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(CIFSFileAccess.class);
    private static final boolean DEBUG = LOG.isDebugEnabled();

    private final FileStorageAccountAccess accountAccess;

    /**
     * Initializes a new {@link CIFSFileAccess}.
     */
    public CIFSFileAccess(final String rootUrl, final NtlmPasswordAuthentication auth, final FileStorageAccount account, final Session session, final CIFSAccountAccess accountAccess) {
        super(rootUrl, auth, account, session, accountAccess);
        this.accountAccess = accountAccess;
    }

    @Override
    public void startTransaction() throws TransactionException {
        // Nothing to do
    }

    @Override
    public void commit() throws TransactionException {
        // Nothing to do
    }

    @Override
    public void rollback() throws TransactionException {
        // Nothing to do
    }

    @Override
    public void finish() throws TransactionException {
        // Nothing to do
    }

    @Override
    public void setTransactional(final boolean transactional) {
        // Nothing to do
    }

    @Override
    public void setRequestTransactional(final boolean transactional) {
        // Nothing to do
    }

    @Override
    public void setCommitsTransaction(final boolean commits) {
        // Nothing to do
    }

    @Override
    public boolean exists(final String folderId, final String id, final String version) throws OXException {
        try {
            /*
             * Check
             */
            final String fid = checkFolderId(folderId, rootUrl);
            final SmbFile smbFile = getSmbFile(fid + id);
            if (!exists(smbFile)) {
                return false;
            }
            if (!smbFile.isFile()) {
                /*
                 * Not a directory
                 */
                throw CIFSExceptionCodes.NOT_A_FILE.create(folderId);
            }
            return true;
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public File getFileMetadata(final String folderId, final String id, final String version) throws OXException {
        if (version != CURRENT_VERSION) {
            throw CIFSExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
        }
        try {
            /*
             * Check
             */
            final String fid = checkFolderId(folderId, rootUrl);
            final String url = fid + id;
            //final URI uri = new URI(fid + id, false);
            final SmbFile smbFile = getSmbFile(url);
            if (!exists(smbFile)) {
                throw CIFSExceptionCodes.NOT_FOUND.create(url);
            }
            if (!smbFile.isFile()) {
                /*
                 * Not a directory
                 */
                throw CIFSExceptionCodes.NOT_A_FILE.create(url);
            }
            /*
             * Start conversion
             */
            return new CIFSFile(fid, id, session.getUserId()).parseSmbFile(smbFile);
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void saveFileMetadata(final File file, final long sequenceNumber) throws OXException {
        createSmbFile(file, null);
    }

    @Override
    public void saveFileMetadata(final File file, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        createSmbFile(file, modifiedFields);
    }

    private SmbFile createSmbFile(final File file, final List<Field> modifiedFields) throws OXException {
        try {
            final Set<Field> set =
                null == modifiedFields || modifiedFields.isEmpty() ? EnumSet.allOf(Field.class) : EnumSet.copyOf(modifiedFields);
            /*
             * Check
             */
            final String folderId = checkFolderId(file.getFolderId(), rootUrl);
            final String id;
            {
                final String fid = file.getId();
                if (null == fid) {
                    String name = file.getFileName();
                    if (isEmpty(name)) {
                        name = file.getTitle();
                        if (isEmpty(name)) {
                            throw CIFSExceptionCodes.MISSING_FILE_NAME.create();
                        }
                    }
                    id = name;
                    file.setId(id);
                } else {
                    id = fid;
                }
            }
            /*
             * Convert file to SMB representation
             */
            final SmbFile smbFile = getSmbFile(folderId + id);
            /*
             * Create if non-existent
             */
            if (!exists(smbFile)) {
                smbFile.createNewFile();
            }
            final long now = System.currentTimeMillis();
            if (set.contains(Field.CREATED)) {
                smbFile.setCreateTime(now);
            }
            if (set.contains(Field.LAST_MODIFIED)) {
                smbFile.setLastModified(now);
            }
            smbFile.setReadWrite();
            /*
             * Check for comment
             */
            final String description = file.getDescription();
            if (!isEmpty(description)) {
                final SmbFileOutputStream outputStream = new SmbFileOutputStream(smbFile, false);
                try {
                    outputStream.write(description.getBytes(Charsets.ISO_8859_1));
                    outputStream.flush();
                } finally {
                    outputStream.close();
                }
            }
            /*
             * Invalidate
             */
            SmbFileMapManagement.getInstance().dropFor(session);
            return smbFile;
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public IDTuple copy(final IDTuple source, final String destFolder, final File update, final InputStream newFil, final List<Field> modifiedFields) throws OXException {
        try {
            final String fid = checkFolderId(source.getFolder(), rootUrl);
            final String url = (fid + source.getId());
            /*
             * Check validity
             */
            final SmbFile copyMe = getSmbFile(url);
            if (!copyMe.exists()) {
                throw CIFSExceptionCodes.NOT_FOUND.create(url);
            }
            if (!copyMe.isFile()) {
                throw CIFSExceptionCodes.NOT_A_FILE.create(url);
            }
            final SmbFile dest = getSmbFile(checkFolderId(destFolder, rootUrl) + source.getId());
            /*
             * Perform COPY
             */
            copyMe.copyTo(dest);
            /*
             * Save
             */
            saveDocument0(update, newFil, modifiedFields);
            /*
             * Invalidate
             */
            SmbFileMapManagement.getInstance().dropFor(session);
            /*
             * Return
             */
            return new IDTuple(destFolder, source.getId());
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public InputStream getDocument(final String folderId, final String id, final String version) throws OXException {
        try {
            final String fid = checkFolderId(folderId, rootUrl);
            final String url = (fid + id);
            final SmbFile smbFile = getSmbFile(url);
            if (!exists(smbFile)) {
                throw CIFSExceptionCodes.NOT_FOUND.create(url);
            }
            if (!smbFile.isFile()) {
                /*
                 * Not a directory
                 */
                throw CIFSExceptionCodes.NOT_A_FILE.create(url);
            }
            /*
             * Get SMB file's input stream
             */
            return new SmbFileInputStream(smbFile);
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void saveDocument(final File file, final InputStream data, final long sequenceNumber) throws OXException {
        saveDocument0(file, data, null);
    }

    @Override
    public void saveDocument(final File file, final InputStream data, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        saveDocument0(file, data, modifiedFields);
    }

    @Override
    public void saveDocument(final File file, final InputStream data, final long sequenceNumber, final List<Field> modifiedFields, final boolean ignoreVersion) throws OXException {
        // CIFS/SMB does not support versioning
        saveDocument0(file, data, modifiedFields);
    }

    private void saveDocument0(final File file, final InputStream data, final List<Field> modifiedFields) throws OXException {
        try {
            /*
             * Save metadata
             */
            final SmbFile newSmbFile = createSmbFile(file, modifiedFields);
            /*
             * Upload data
             */
            final SmbFileOutputStream outputStream = new SmbFileOutputStream(newSmbFile, false);
            try {
                final byte[] buf = new byte[8192];
                int read;
                while ((read = data.read(buf)) > 0) {
                    outputStream.write(buf, 0, read);
                }
                outputStream.flush();
            } finally {
                outputStream.close();
            }
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            try {
                data.close();
            } catch (final IOException e) {
                com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CIFSFileAccess.class)).error(e.getMessage(), e);
            }
        }
    }

    private static final SmbFileFilter FILE_FILTER = new SmbFileFilter() {

        @Override
        public boolean accept(final SmbFile file) throws SmbException {
            return file.isFile();
        }
    };

    @Override
    public void removeDocument(final String folderId, final long sequenceNumber) throws OXException {
        try {
            /*
             * Get & check folder
             */
            final String fid = checkFolderId(folderId, rootUrl);
            final SmbFile smbFolder = getSmbFile(fid);
            if (!smbFolder.exists()) {
                throw CIFSExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!smbFolder.isDirectory()) {
                throw CIFSExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            /*
             * List its sub-resources
             */
            final SmbFile[] subFiles = smbFolder.listFiles(FILE_FILTER);
            for (final SmbFile subFile : subFiles) {
                subFile.delete();
            }
            /*
             * Invalidate
             */
            SmbFileMapManagement.getInstance().dropFor(session);
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber) throws OXException {
        try {
            final List<IDTuple> ret = new ArrayList<IDTuple>();
            for (final IDTuple id : ids) {
                final String fid = checkFolderId(id.getFolder(), rootUrl);
                final String url = (fid + id.getId());
                /*
                 * Check validity
                 */
                final SmbFile smbFile = getSmbFile(url);
                if (exists(smbFile)) {
                    /*
                     * Check for file
                     */
                    if (!smbFile.isFile()) {
                        throw CIFSExceptionCodes.NOT_A_FILE.create(url);
                    }
                    /*
                     * Delete
                     */
                    try {
                        smbFile.delete();
                    } catch (final SmbException e) {
                        /*
                         * Delete failed
                         */
                        ret.add(id);
                    }
                }
            }
            /*
             * Invalidate
             */
            SmbFileMapManagement.getInstance().dropFor(session);
            /*
             * Return
             */
            return ret;
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public String[] removeVersion(final String folderId, final String id, final String[] versions) throws OXException {
        for (final String version : versions) {
            if (version != CURRENT_VERSION) {
                throw CIFSExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
            }
        }
        try {
            final String fid = checkFolderId(folderId, rootUrl);
            final String url = (fid + id);
            /*
             * Check validity
             */
            final SmbFile smbFile = getSmbFile(url);
            if (!exists(smbFile)) {
                /*
                 * NO-OP for us
                 */
                return new String[0];
            }
            if (!smbFile.isFile()) {
                throw CIFSExceptionCodes.NOT_A_FILE.create(url);
            }
            /*
             * Delete
             */
            smbFile.delete();
            /*
             * Invalidate
             */
            SmbFileMapManagement.getInstance().dropFor(session);
            /*
             * Return empty array
             */
            return new String[0];
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void unlock(final String folderId, final String id) throws OXException {
        // Nothing to do

    }

    @Override
    public void lock(final String folderId, final String id, final long diff) throws OXException {
        // Nothing to do

    }

    @Override
    public void touch(final String folderId, final String id) throws OXException {
        try {
            /*
             * Check
             */
            final String fid = checkFolderId(folderId, rootUrl);
            final String url = (fid + id);
            /*
             * Check validity
             */
            final SmbFile smbFile = getSmbFile(url);
            if (!exists(smbFile)) {
                throw CIFSExceptionCodes.NOT_FOUND.create(url);
            }
            if (!smbFile.isFile()) {
                throw CIFSExceptionCodes.NOT_A_FILE.create(url);
            }
            /*
             * Update
             */
            smbFile.setLastModified(System.currentTimeMillis());
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId) throws OXException {
        return new FileTimedResult(getFileList(folderId, null));
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields) throws OXException {
        return new FileTimedResult(getFileList(folderId, fields));
    }

    private List<File> getFileList(final String folderId, final List<Field> fields) throws OXException {
        try {
            /*
             * Get & check folder
             */
            final String fid = checkFolderId(folderId, rootUrl);
            final SmbFile smbFolder = getSmbFile(fid);
            if (!smbFolder.exists()) {
                throw CIFSExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!smbFolder.isDirectory()) {
                throw CIFSExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            /*
             * List its sub-resources
             */
            SmbFile[] subFiles;
            try {
                if (DEBUG) {
                    final long st = System.currentTimeMillis();
                    subFiles = smbFolder.canRead() ? smbFolder.listFiles(FILE_FILTER) : new SmbFile[0];
                    final long dur = System.currentTimeMillis() - st;
                    LOG.debug("CIFSFileAccess.getFileList() - SmbFile.listFiles() took " + dur + "msec.");
                } else {
                    subFiles = smbFolder.canRead() ? smbFolder.listFiles(FILE_FILTER) : new SmbFile[0];
                }
            } catch (final SmbException e) {
                if (!indicatesNotReadable(e)) {
                    throw e;
                }
                subFiles = new SmbFile[0];
            }
            final List<File> files = new ArrayList<File>(subFiles.length);
            for (final SmbFile subFile : subFiles) {
                files.add(new CIFSFile(fid, subFile.getName(), session.getUserId()).parseSmbFile(subFile, fields));
            }
            /*
             * Return list
             */
            return files;
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        final List<File> files = getFileList(folderId, fields);
        /*
         * Sort list
         */
        Collections.sort(files, order.comparatorBy(sort));
        /*
         * Return sorted result
         */
        return new FileTimedResult(files);
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id) throws OXException {
        return new FileTimedResult(Collections.singletonList(getFileMetadata(folderId, id, CURRENT_VERSION)));
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id, final List<Field> fields) throws OXException {
        return new FileTimedResult(Collections.singletonList(getFileMetadata(folderId, id, CURRENT_VERSION)));
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id, final List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        return new FileTimedResult(Collections.singletonList(getFileMetadata(folderId, id, CURRENT_VERSION)));
    }

    @Override
    public TimedResult<File> getDocuments(final List<IDTuple> ids, final List<Field> fields) throws OXException {
        try {
            /*
             * Iterate identifiers
             */
            final List<File> files = new ArrayList<File>(ids.size());
            for (final IDTuple id : ids) {
                final String fid = checkFolderId(id.getFolder(), rootUrl);
                final String uri = (fid + id.getId());
                final SmbFile smbFile = getSmbFile(uri);
                if (!exists(smbFile)) {
                    throw CIFSExceptionCodes.NOT_FOUND.create(uri);
                }
                if (!smbFile.isFile()) {
                    /*
                     * Not a directory
                     */
                    throw CIFSExceptionCodes.NOT_A_FILE.create(uri);
                }
                files.add(new CIFSFile(fid, id.getId(), session.getUserId()).parseSmbFile(smbFile, fields));
            }
            /*
             * Return
             */
            return new FileTimedResult(files);
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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

    private static final String ALL = "*";

    @Override
    public SearchIterator<File> search(final String pattern, final List<Field> fields, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        final String pat = isEmpty(pattern) ? ALL : pattern;
        final List<File> results;
        if (ALL_FOLDERS == folderId) {
            /*
             * Recursively search files in directories
             */
            results = new ArrayList<File>();
            recursiveSearchFile(pat, rootUrl, fields, results);
        } else {
            /*
             * Get files from folder
             */
            results = getFileList(folderId, fields);
            /*
             * Filter by search pattern
             */
            for (final Iterator<File> iterator = results.iterator(); iterator.hasNext();) {
                final File file = iterator.next();
                if (!file.matches(pat)) {
                    iterator.remove();
                }
            }
        }
        /*
         * Empty?
         */
        if (results.isEmpty()) {
            return SearchIteratorAdapter.emptyIterator();
        }
        /*
         * Sort
         */
        Collections.sort(results, order.comparatorBy(sort));
        /*
         * Consider start/end index
         */
        if (start != NOT_SET && end != NOT_SET && end > start) {
            final int fromIndex = start;
            int toIndex = end;
            if ((fromIndex) > results.size()) {
                /*
                 * Return empty iterator if start is out of range
                 */
                return SearchIteratorAdapter.emptyIterator();
            }
            /*
             * Reset end index if out of range
             */
            if (toIndex >= results.size()) {
                toIndex = results.size();
            }
            /*
             * Return
             */
            final List<File> subList = results.subList(fromIndex, toIndex);
            return new SearchIteratorAdapter<File>(subList.iterator(), subList.size());
        }
        /*
         * Return sorted result
         */
        return new SearchIteratorAdapter<File>(results.iterator(), results.size());
    }

    private void recursiveSearchFile(final String pattern, final String folderId, final List<Field> fields, final List<File> results) throws OXException {
        try {
            /*
             * Check
             */
            final String fid = checkFolderId(folderId, rootUrl);
            final SmbFile smbFolder = getSmbFile(fid);
            if (!smbFolder.exists()) {
                throw CIFSExceptionCodes.NOT_FOUND.create(folderId);
            }
            if (!smbFolder.isDirectory()) {
                throw CIFSExceptionCodes.NOT_A_FOLDER.create(folderId);
            }
            SmbFile[] subFiles;
            try {
                subFiles = smbFolder.canRead() ? smbFolder.listFiles() : new SmbFile[0];
            } catch (final SmbException e) {
                if (!indicatesNotReadable(e)) {
                    throw e;
                }
                subFiles = new SmbFile[0];
            }
            for (final SmbFile subFile : subFiles) {
                if (subFile.isDirectory()) {
                    recursiveSearchFile(pattern, subFile.getPath(), fields, results);
                } else {
                    final CIFSFile file = new CIFSFile(folderId, subFile.getName(), session.getUserId()).parseSmbFile(subFile, fields);
                    if (file.matches(pattern)) {
                        results.add(file);
                    }
                }
            }
        } catch (final SmbException e) {
            throw CIFSExceptionCodes.forSmbException(e);
        } catch (final IOException e) {
            throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
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
