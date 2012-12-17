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
import java.util.List;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.dropbox.session.DropboxOAuthAccess;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link DropboxFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DropboxFileAccess extends AbstractDropboxAccess implements FileStorageFileAccess {

    /**
     * Initializes a new {@link DropboxFileAccess}.
     */
    public DropboxFileAccess(DropboxOAuthAccess dropboxOAuthAccess, FileStorageAccount account, Session session) {
        super(dropboxOAuthAccess, account, session);
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
    public void setTransactional(boolean transactional) {
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
    public boolean exists(String folderId, String id, int version) throws OXException {
        try {
            dropboxAPI.metadata(folderId, 1, null, false, null);
            return true;
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
    public File getFileMetadata(String folderId, String id, int version) throws OXException {
        try {
            final Entry entry = dropboxAPI.metadata(folderId, 1, null, false, null);
            if (entry.isDir) {
                throw DropboxExceptionCodes.NOT_A_FILE.create(id);
            }
            return new DropboxFile(entry.parentPath(), entry.path, CURRENT_VERSION);
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
    public void saveFileMetadata(File file, long sequenceNumber) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveFileMetadata(com.openexchange.file.storage.File, long, java.util.List)
     */
    @Override
    public void saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#copy(com.openexchange.file.storage.FileStorageFileAccess.IDTuple, java.lang.String, com.openexchange.file.storage.File, java.io.InputStream, java.util.List)
     */
    @Override
    public IDTuple copy(IDTuple source, String destFolder, File update, InputStream newFil, List<Field> modifiedFields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocument(java.lang.String, java.lang.String, int)
     */
    @Override
    public InputStream getDocument(String folderId, String id, int version) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveDocument(com.openexchange.file.storage.File, java.io.InputStream, long)
     */
    @Override
    public void saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveDocument(com.openexchange.file.storage.File, java.io.InputStream, long, java.util.List)
     */
    @Override
    public void saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeDocument(java.lang.String, long)
     */
    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeDocument(java.util.List, long)
     */
    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeVersion(java.lang.String, java.lang.String, int[])
     */
    @Override
    public int[] removeVersion(String folderId, String id, int[] versions) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#unlock(java.lang.String, java.lang.String)
     */
    @Override
    public void unlock(String folderId, String id) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#lock(java.lang.String, java.lang.String, long)
     */
    @Override
    public void lock(String folderId, String id, long diff) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#touch(java.lang.String, java.lang.String)
     */
    @Override
    public void touch(String folderId, String id) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.lang.String)
     */
    @Override
    public TimedResult<File> getDocuments(String folderId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.lang.String, java.util.List)
     */
    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.lang.String, java.util.List, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection)
     */
    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getVersions(java.lang.String, java.lang.String)
     */
    @Override
    public TimedResult<File> getVersions(String folderId, String id) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getVersions(java.lang.String, java.lang.String, java.util.List)
     */
    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getVersions(java.lang.String, java.lang.String, java.util.List, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection)
     */
    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.util.List, java.util.List)
     */
    @Override
    public TimedResult<File> getDocuments(List<IDTuple> ids, List<Field> fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDelta(java.lang.String, long, java.util.List, boolean)
     */
    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDelta(java.lang.String, long, java.util.List, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection, boolean)
     */
    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#search(java.lang.String, java.util.List, java.lang.String, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection, int, int)
     */
    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getAccountAccess()
     */
    @Override
    public FileStorageAccountAccess getAccountAccess() {
        // TODO Auto-generated method stub
        return null;
    }

}
