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

package com.openexchange.file.storage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link InMemoryFileStorageFileAccess}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InMemoryFileStorageFileAccess implements FileStorageFileAccess {
    
    private final Map<String, Map<String, VersionContainer>> storage = new HashMap<String, Map<String, VersionContainer>>();

    private final String accountId;

    private final String serviceId;
    
    
    public InMemoryFileStorageFileAccess(String serviceId, String accountId) {
        super();
        this.serviceId = serviceId;
        this.accountId = accountId;
    }

    @Override
    public boolean exists(String folderId, String id, int version) throws OXException {
        Map<String, VersionContainer> map = storage.get(folderId);
        if (map == null) {
            return false;
        }
        
        return map.containsKey(id) ? map.get(id).containsVersion(version) : false;
    }

    @Override
    public File getFileMetadata(String folderId, String id, int version) throws OXException {
        Map<String, VersionContainer> map = storage.get(folderId);
        if (map == null) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
        }
        
        VersionContainer versionContainer = map.get(id);
        if (versionContainer == null) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
        }
        
        if (version == FileStorageFileAccess.CURRENT_VERSION) {
            version = versionContainer.getCurrentVersionNumber();
        }
        if (versionContainer.containsVersion(version)) {
            return versionContainer.getVersion(version).getFile();
        }
        
        throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
    }

    @Override
    public void saveFileMetadata(File file, long sequenceNumber) throws OXException {
        save(file, null);
    }

    @Override
    public void saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        save(file, null);
    }

    @Override
    public void saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        save(file, data);
    }

    @Override
    public void saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        save(file, data);
    }
    
    private void save(File file, InputStream data) throws OXException {
        String folderId = file.getFolderId();
        Map<String, VersionContainer> map = storage.get(folderId);
        if (map == null) {
            map = new HashMap<String, VersionContainer>();
            storage.put(folderId, map);
        }
        
        String id = file.getId();
        if (id == FileStorageFileAccess.NEW) {
            id = UUID.randomUUID().toString();
            file.setId(id);
            FileHolder holder;
            if (data == null) {
                holder = new FileHolder(file);
            } else {
                holder = new FileHolder(file, data);
            }
            VersionContainer versionContainer = new VersionContainer();
            versionContainer.addVersion(holder);
            map.put(id, versionContainer);
        } else {
            VersionContainer versionContainer = map.get(id);
            if (versionContainer == null) {
                throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
            }
            
            FileHolder holder;
            if (data == null) {
                holder = new FileHolder(file);
            } else {
                holder = new FileHolder(file, data);
            }
            versionContainer.addVersion(holder);            
        }
    }

    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        // Not implemented
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        List<IDTuple> notRemoved = new ArrayList<IDTuple>();
        for (IDTuple tuple : ids) {
            String folderId = tuple.getFolder();
            String id = tuple.getId();
            
            Map<String, VersionContainer> map = storage.get(folderId);
            if (map == null) {
                notRemoved.add(tuple);
                continue;
            }
            
            VersionContainer versionContainer = map.remove(id);
            if (versionContainer == null) {
                notRemoved.add(tuple);
                continue;
            }
        }
        
        return notRemoved;
    }

    @Override
    public int[] removeVersion(String folderId, String id, int[] versions) throws OXException {
        List<Integer> notRemovedList = new ArrayList<Integer>();
        Map<String, VersionContainer> map = storage.get(folderId);
        if (map == null) {
            return versions;
        }
        
        VersionContainer versionContainer = map.get(id);
        if (versionContainer == null) {
            return versions;
        }
        
        for (int version : versions) {
            FileHolder holder;
            if (version == FileStorageFileAccess.CURRENT_VERSION) {
                holder = versionContainer.removeVersion(versionContainer.getCurrentVersionNumber());
            } else {
                holder = versionContainer.removeVersion(version);
            }
            
            if (holder == null) {
                notRemovedList.add(version);
            }
        }

        int[] notRemoved = new int[notRemovedList.size()];
        for (int i = 0; i < notRemoved.length; i++) {
            notRemoved[i] = notRemovedList.get(i);
        }
        
        return notRemoved;
    }    

    @Override
    public InputStream getDocument(String folderId, String id, int version) throws OXException {
        Map<String, VersionContainer> map = storage.get(folderId);
        if (map == null) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
        }
        
        VersionContainer versionContainer = map.get(id);
        if (versionContainer == null) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
        }
        
        FileHolder holder;
        if (version == FileStorageFileAccess.CURRENT_VERSION) {
            holder = versionContainer.getCurrentVersion();
        } else {
            holder = versionContainer.getVersion(version);
        }
        
        if (holder == null) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
        }
        
        return holder.getContent();
    }
    
    @Override
    public IDTuple copy(IDTuple source, String destFolder, File update, InputStream newFile, List<Field> modifiedFields) throws OXException {
        final File orig = getFileMetadata(source.getFolder(), source.getId(), CURRENT_VERSION);
        if(newFile == null && orig.getFileName() != null) {
            newFile = getDocument(source.getFolder(), source.getId(), CURRENT_VERSION);
        }
        if(update != null) {
            orig.copyFrom(update, modifiedFields.toArray(new File.Field[modifiedFields.size()]));
        }
        orig.setId(NEW);
        orig.setFolderId(destFolder);

        if(newFile == null) {
            saveFileMetadata(orig, UNDEFINED_SEQUENCE_NUMBER);
        } else {
            saveDocument(orig, newFile, UNDEFINED_SEQUENCE_NUMBER);
        }

        return new IDTuple(destFolder, orig.getId());
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
    public void touch(String folderId, String id) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public TimedResult<File> getDocuments(String folderId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        // TODO Auto-generated method stub
        return null;
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
    public FileStorageAccountAccess getAccountAccess() {
        return new FileStorageAccountAccess() {
            
            @Override
            public boolean ping() throws OXException {
                return true;
            }
            
            @Override
            public boolean isConnected() {
                return true;
            }
            
            @Override
            public void connect() throws OXException {

            }
            
            @Override
            public void close() {

            }
            
            @Override
            public boolean cacheable() {
                return true;
            }
            
            @Override
            public FileStorageService getService() {
                return new FileStorageService() {
                    
                    @Override
                    public Set<String> getSecretProperties() {
                        return null;
                    }
                    
                    @Override
                    public String getId() {
                        return serviceId;
                    }
                    
                    @Override
                    public DynamicFormDescription getFormDescription() {
                        return null;
                    }
                    
                    @Override
                    public String getDisplayName() {
                        return null;
                    }
                    
                    @Override
                    public FileStorageAccountManager getAccountManager() throws OXException {
                        return null;
                    }
                    
                    @Override
                    public FileStorageAccountAccess getAccountAccess(String accountId, Session session) throws OXException {
                        return null;
                    }
                };
            }
            
            @Override
            public FileStorageFolder getRootFolder() throws OXException {
                return null;
            }
            
            @Override
            public FileStorageFolderAccess getFolderAccess() throws OXException {
                return null;
            }
            
            @Override
            public FileStorageFileAccess getFileAccess() throws OXException {
                return null;
            }
            
            @Override
            public String getAccountId() {
                return accountId;
            }
        };
    }
    
    /*
     * Ignore transactional behavior
     */
    @Override
    public void startTransaction() throws OXException {

    }

    @Override
    public void commit() throws OXException {

    }

    @Override
    public void rollback() throws OXException {

    }

    @Override
    public void finish() throws OXException {

    }

    @Override
    public void setTransactional(boolean transactional) {

    }

    @Override
    public void setRequestTransactional(boolean transactional) {

    }

    @Override
    public void setCommitsTransaction(boolean commits) {

    }

}
