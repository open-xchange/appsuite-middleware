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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.file.storage.smartDrive;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.smartdrive.client.SmartDriveException;
import com.openexchange.smartdrive.client.SmartDriveFile;
import com.openexchange.smartdrive.client.SmartDriveResource;
import com.openexchange.smartdrive.client.SmartDriveResponse;
import com.openexchange.smartdrive.client.SmartDriveStatefulAccess;
import com.openexchange.smartdrive.client.SmartDriveStatelessAccess;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tx.TransactionException;
import static com.openexchange.file.storage.smartDrive.Helpers.*;

/**
 * {@link UISDFileAccess}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UISDFileAccess implements FileStorageFileAccess {

    private UISDAccountAccess accountAccess;

    public UISDFileAccess(UISDAccountAccess accountAccess) {
        super();
        this.accountAccess = accountAccess;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#copy(com.openexchange.file.storage.FileStorageFileAccess.IDTuple,
     * java.lang.String, com.openexchange.file.storage.File, java.io.InputStream, java.util.List)
     */
    public IDTuple copy(IDTuple source, String destFolder, File update, InputStream newFil, List<Field> modifiedFields) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean exists(String folderId, String id, int version) throws FileStorageException {
        try {
            getFileMetadata(folderId, id, version);
        } catch (FileStorageException x) {
            return false;
        }
        return true;
    }

    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws FileStorageException {
        try {
            SearchIterator<File> i1 = getDocuments(folderId).results();
            SearchIterator<File> i2 = SearchIteratorAdapter.emptyIterator();
            SearchIterator<File> i3 = SearchIteratorAdapter.emptyIterator();

            return new FileDelta(i1, i2, i3, 0);
        } catch (FileStorageException x) {
            throw x;
        } catch (AbstractOXException x) {
            throw new FileStorageException(x);
        }
    }

    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws FileStorageException {
        return getDelta(folderId, updateSince, fields, ignoreDeleted);
    }

    public InputStream getDocument(String folderId, String id, int version) throws FileStorageException {
        String path = folderId + "/" + id;
        SmartDriveStatelessAccess statelessAccess = accountAccess.getStatelessAccess();
        try {
            return statelessAccess.downloadFile(path);
        } catch (SmartDriveException x) {
            throw new FileStorageException(x);
        }
    }

    public TimedResult<File> getDocuments(String folderId) throws FileStorageException {
        return getDocuments(folderId, Arrays.asList(File.Field.values()));
    }

    public TimedResult<File> getDocuments(String folderId, List<Field> fields) throws FileStorageException {
        return getDocuments(folderId, fields, null, null);
    }

    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws FileStorageException {
        SmartDriveStatefulAccess statefulAccess = accountAccess.getStatefulAccess();
        try {
            SmartDriveResponse<List<SmartDriveResource>> response = statefulAccess.list(folderId);
            checkResponse(response);

            List<SmartDriveResource> resources = response.getResponse();

            List<File> asFiles = toFiles(folderId, resources);

            if (sort != null) {
                Collections.sort(asFiles, order.comparatorBy(sort));
            }

            return new FileTimedResult(new SearchIteratorAdapter<File>(asFiles.iterator()));
        } catch (SmartDriveException x) {
            throw new FileStorageException(x);
        }

    }

    private List<File> toFiles(String parentFolder, List<SmartDriveResource> resources) throws FileStorageException {
        List<File> files = new ArrayList<File>(resources.size());
        try {
            for (SmartDriveResource resource : resources) {
                if (!resource.isDirectory()) {
                    SmartDriveFile file = resource.toFile();

                    DefaultFile f = new DefaultFile();

                    f.setCreated(file.getCreationDate());
                    f.setLastModified(file.getLastModified());
                    f.setFileSize(file.getFileSize());

                    String name = file.getName();
                    f.setTitle(name);
                    f.setFileName(name);
                    f.setId(name);
                    f.setFolderId(parentFolder);

                    files.add(f);

                }
            }
        } catch (SmartDriveException e) {
            throw new FileStorageException(e);
        }

        return files;
    }

    public TimedResult<File> getDocuments(List<IDTuple> ids, List<Field> fields) throws FileStorageException {
        List<File> files = new ArrayList<File>(ids.size());

        for (IDTuple address : ids) {
            files.add(getFileMetadata(address.getFolder(), address.getId(), CURRENT_VERSION));
        }

        return new FileTimedResult(new SearchIteratorAdapter<File>(files.iterator()));
    }

    public File getFileMetadata(String folderId, String id, int version) throws FileStorageException {
        String path = folderId + "/" + id;
        SmartDriveStatefulAccess statefulAccess = accountAccess.getStatefulAccess();
        try {

            SmartDriveResponse<List<SmartDriveResource>> response = statefulAccess.propget(path, new int[0]);
            checkResponse(response);

            List<File> asFiles = toFiles(folderId, response.getResponse());

            if (!asFiles.isEmpty()) {
                return asFiles.get(0);
            }

        } catch (SmartDriveException e) {
            throw new FileStorageException(e);
        }

        return null;
    }

    public TimedResult<File> getVersions(String folderId, String id) throws FileStorageException {
        File fileMetadata = getFileMetadata(folderId, id, CURRENT_VERSION);
        return new FileTimedResult(SearchIteratorAdapter.createArrayIterator(new File[] { fileMetadata }));
    }

    public TimedResult<File> getVersions(String folder, String id, List<Field> fields) throws FileStorageException {
        return getVersions(folder, id);
    }

    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields, Field sort, SortDirection order) throws FileStorageException {
        return getVersions(folderId, id);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#lock(java.lang.String, java.lang.String, long)
     */
    public void lock(String folderId, String id, long diff) throws FileStorageException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeDocument(java.lang.String, long)
     */
    public void removeDocument(String folderId, long sequenceNumber) throws FileStorageException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeDocument(java.util.List, long)
     */
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#removeVersion(java.lang.String, java.lang.String, int[])
     */
    public int[] removeVersion(String folderId, String id, int[] versions) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    public void saveDocument(File file, InputStream data, long sequenceNumber) throws FileStorageException {
        String id = file.getId();
        if (id == NEW) {
            id = file.getFileName();
            file.setId(id);
        }
        if (id == null) {
            throw FileStorageExceptionCodes.INVALID_PARAMETER.create("File", "File must contain filename or id");
        }

        if (file.getFolderId() == null) {
            throw FileStorageExceptionCodes.INVALID_PARAMETER.create("File", "File must contain a folder id");
        }

        try {
            SmartDriveStatelessAccess access = accountAccess.getStatelessAccess();

            access.uploadFile(file.getFolderId(), id, data);
        } catch (SmartDriveException x) {
            throw new FileStorageException(x);
        }

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveDocument(com.openexchange.file.storage.File, java.io.InputStream, long,
     * java.util.List)
     */
    public void saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws FileStorageException {
        saveDocument(file, data, sequenceNumber);
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveFileMetadata(com.openexchange.file.storage.File, long)
     */
    public void saveFileMetadata(File file, long sequenceNumber) throws FileStorageException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#saveFileMetadata(com.openexchange.file.storage.File, long, java.util.List)
     */
    public void saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws FileStorageException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#search(java.lang.String, java.util.List, java.lang.String,
     * com.openexchange.file.storage.File.Field, com.openexchange.file.storage.FileStorageFileAccess.SortDirection, int, int)
     */
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws FileStorageException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#touch(java.lang.String, java.lang.String)
     */
    public void touch(String folderId, String id) throws FileStorageException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.file.storage.FileStorageFileAccess#unlock(java.lang.String, java.lang.String)
     */
    public void unlock(String folderId, String id) throws FileStorageException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tx.TransactionAware#commit()
     */
    public void commit() throws TransactionException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tx.TransactionAware#finish()
     */
    public void finish() throws TransactionException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tx.TransactionAware#rollback()
     */
    public void rollback() throws TransactionException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tx.TransactionAware#setCommitsTransaction(boolean)
     */
    public void setCommitsTransaction(boolean commits) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tx.TransactionAware#setRequestTransactional(boolean)
     */
    public void setRequestTransactional(boolean transactional) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tx.TransactionAware#setTransactional(boolean)
     */
    public void setTransactional(boolean transactional) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.tx.TransactionAware#startTransaction()
     */
    public void startTransaction() throws TransactionException {
        // TODO Auto-generated method stub

    }

}
