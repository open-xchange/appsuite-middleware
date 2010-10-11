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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.file.storage.webdav;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import org.apache.commons.httpclient.HttpClient;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageException;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.session.Session;
import com.openexchange.tx.TransactionException;

/**
 * {@link WebDAVFileStorageFileAccess}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class WebDAVFileStorageFileAccess extends AbstractWebDAVAccess implements FileStorageFileAccess {

    private final String rootUri;

    /**
     * Initializes a new {@link WebDAVFileStorageFileAccess}.
     */
    public WebDAVFileStorageFileAccess(final HttpClient client, final FileStorageAccount account, final Session session) {
        super(client, account, session);
        rootUri = (String) account.getConfiguration().get(WebDAVConstants.WEBDAV_URL);
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#exists(java.lang.String, java.lang.String, int)
     */
    public boolean exists(String folderId, String id, int version) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDelta(java.lang.String, long, java.util.List, boolean)
     */
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> columns, boolean ignoreDeleted) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDelta(java.lang.String, long, java.util.List, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection, boolean)
     */
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> columns, Field sort, SortDirection order, boolean ignoreDeleted) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocument(java.lang.String, java.lang.String, int)
     */
    public InputStream getDocument(String folderId, String id, int version) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.lang.String)
     */
    public TimedResult<File> getDocuments(String folderId) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.lang.String, java.util.List)
     */
    public TimedResult<File> getDocuments(String folderId, List<Field> columns) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.lang.String, java.util.List, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection)
     */
    public TimedResult<File> getDocuments(String folderId, List<Field> columns, Field sort, SortDirection order) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getDocuments(java.lang.String[][], java.util.List)
     */
    public TimedResult<File> getDocuments(String[][] ids, List<Field> columns) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getFileMetadata(java.lang.String, java.lang.String, int)
     */
    public File getFileMetadata(String folderId, String id, int version) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getVersions(java.lang.String, java.lang.String)
     */
    public TimedResult<File> getVersions(String folderId, String id) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getVersions(java.lang.String, java.lang.String, java.util.List)
     */
    public TimedResult<File> getVersions(String folderId, String id, List<Field> columns) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#getVersions(java.lang.String, java.lang.String, java.util.List, com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection)
     */
    public TimedResult<File> getVersions(String folderId, String id, List<Field> columns, Field sort, SortDirection order) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#lock(java.lang.String, java.lang.String, long)
     */
    public void lock(String folderId, String id, long diff) throws FileStorageException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeDocument(java.lang.String, long)
     */
    public void removeDocument(String folderId, long sequenceNumber) throws FileStorageException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeDocument(java.lang.String[][], long)
     */
    public String[] removeDocument(String[][] ids, long sequenceNumber) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeVersion(java.lang.String, java.lang.String, int[])
     */
    public int[] removeVersion(String folderId, String id, int[] versions) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveDocument(com.openexchange.file.storage.File, java.io.InputStream, long)
     */
    public void saveDocument(File document, InputStream data, long sequenceNumber) throws FileStorageException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveDocument(com.openexchange.file.storage.File, java.io.InputStream, long, java.util.List)
     */
    public void saveDocument(File document, InputStream data, long sequenceNumber, List<Field> modifiedColumns) throws FileStorageException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveFileMetadata(com.openexchange.file.storage.File, long)
     */
    public void saveFileMetadata(File document, long sequenceNumber) throws FileStorageException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveFileMetadata(com.openexchange.file.storage.File, long, java.util.List)
     */
    public void saveFileMetadata(File document, long sequenceNumber, List<Field> modifiedColumns) throws FileStorageException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#touch(java.lang.String, java.lang.String)
     */
    public void touch(String folderId, String id) throws FileStorageException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#unlock(java.lang.String, java.lang.String)
     */
    public void unlock(String folderId, String id) throws FileStorageException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.tx.TransactionAware#commit()
     */
    public void commit() throws TransactionException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.tx.TransactionAware#finish()
     */
    public void finish() throws TransactionException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.tx.TransactionAware#rollback()
     */
    public void rollback() throws TransactionException {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.tx.TransactionAware#setCommitsTransaction(boolean)
     */
    public void setCommitsTransaction(boolean commits) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.tx.TransactionAware#setRequestTransactional(boolean)
     */
    public void setRequestTransactional(boolean transactional) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.tx.TransactionAware#setTransactional(boolean)
     */
    public void setTransactional(boolean transactional) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.tx.TransactionAware#startTransaction()
     */
    public void startTransaction() throws TransactionException {
        // TODO Auto-generated method stub
        
    }

  

}
