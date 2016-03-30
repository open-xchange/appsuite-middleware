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

package com.openexchange.file.storage;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File.Field;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.DeltaImpl;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link InMemoryFileStorageFileAccess}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InMemoryFileStorageFileAccess implements FileStorageFileAccess, FileStorageVersionedFileAccess {

    private final Map<String, Map<String, VersionContainer>> storage = new HashMap<String, Map<String, VersionContainer>>();

    private final String accountId;

    private final String serviceId;

    public InMemoryFileStorageFileAccess(String serviceId, String accountId) {
        super();
        this.serviceId = serviceId;
        this.accountId = accountId;
    }

    @Override
    public boolean exists(String folderId, String id, String version) throws OXException {
        Map<String, VersionContainer> map = storage.get(folderId);
        if (map == null) {
            return false;
        }

        return map.containsKey(id) ? map.get(id).containsVersion(Integer.parseInt(version)) : false;
    }

    @Override
    public File getFileMetadata(String folderId, String id, String version) throws OXException {
        VersionContainer versionContainer = getVersionContainer(folderId, id);

        if (version == FileStorageFileAccess.CURRENT_VERSION) {
            version = Integer.toString(versionContainer.getCurrentVersionNumber());
        }
        if (versionContainer.containsVersion(Integer.parseInt(version))) {
            return versionContainer.getVersion(Integer.parseInt(version)).getFile();
        }

        throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
    }

    private VersionContainer getVersionContainer(String folderId, String id) throws OXException {
        Map<String, VersionContainer> map = storage.get(folderId);
        if (map == null) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
        }

        VersionContainer versionContainer = map.get(id);
        if (versionContainer == null) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
        }
        return versionContainer;
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber) throws OXException {
        return save(file, null);
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        return save(file, null);
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        return save(file, data);
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        return save(file, data);
    }

    private IDTuple save(File file, InputStream data) throws OXException {
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
            int version = versionContainer.addVersion(holder);
            file.setVersion(Integer.toString(version));
            map.put(id, versionContainer);
            return new IDTuple(folderId, id);
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
            int version = versionContainer.addVersion(holder);
            file.setVersion(Integer.toString(version));

            // Swap IDs
            String oldId = id;
            id = UUID.randomUUID().toString();
            file.setId(id);
            map.put(id, map.remove(oldId));
            for(FileHolder fh: versionContainer.getAllVersions()) {
                fh.getInternalFile().setId(id);
            }
            return new IDTuple(folderId, id);
        }

    }

    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        // Not implemented
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long sequenceNumber, boolean hardDelete) throws OXException {
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
    public String[] removeVersion(String folderId, String id, String[] versions) throws OXException {
        List<String> notRemovedList = new ArrayList<String>();
        Map<String, VersionContainer> map = storage.get(folderId);
        if (map == null) {
            return versions;
        }

        VersionContainer versionContainer = map.get(id);
        if (versionContainer == null) {
            return versions;
        }

        for (String version : versions) {
            FileHolder holder;
            if (version == FileStorageFileAccess.CURRENT_VERSION) {
                holder = versionContainer.removeVersion(versionContainer.getCurrentVersionNumber());
            } else {
                holder = versionContainer.removeVersion(Integer.parseInt(version));
            }

            if (holder == null) {
                notRemovedList.add(version);
            }
        }

        String[] notRemoved = new String[notRemovedList.size()];
        for (int i = 0; i < notRemoved.length; i++) {
            notRemoved[i] = notRemovedList.get(i);
        }

        return notRemoved;
    }

    @Override
    public InputStream getDocument(String folderId, String id, String version) throws OXException {
        VersionContainer versionContainer = getVersionContainer(folderId, id);

        FileHolder holder;
        if (version == FileStorageFileAccess.CURRENT_VERSION) {
            holder = versionContainer.getCurrentVersion();
        } else {
            holder = versionContainer.getVersion(Integer.parseInt(version));
        }

        if (holder == null) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
        }

        return holder.getContent();
    }

    @Override
    public IDTuple copy(IDTuple source, String version, String destFolder, File update, InputStream newFile, List<Field> modifiedFields) throws OXException {
        final File orig = getFileMetadata(source.getFolder(), source.getId(), version);
        if (newFile == null && orig.getFileName() != null) {
            newFile = getDocument(source.getFolder(), source.getId(), version);
        }
        if (update != null) {
            orig.copyFrom(update, modifiedFields.toArray(new File.Field[modifiedFields.size()]));
        }
        orig.setId(NEW);
        orig.setFolderId(destFolder);

        if (newFile == null) {
            saveFileMetadata(orig, UNDEFINED_SEQUENCE_NUMBER);
        } else {
            saveDocument(orig, newFile, UNDEFINED_SEQUENCE_NUMBER);
        }

        return new IDTuple(destFolder, orig.getId());
    }

    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<File.Field> modifiedFields) throws OXException {
        final File orig = getFileMetadata(source.getFolder(), source.getId(), CURRENT_VERSION);
        if (update != null) {
            orig.copyFrom(update, modifiedFields.toArray(new File.Field[modifiedFields.size()]));
        }
        orig.setFolderId(destFolder);
        saveFileMetadata(orig, sequenceNumber, modifiedFields);
        return new IDTuple(destFolder, orig.getId());
    }

    @Override
    public void touch(String folderId, String id) throws OXException {
        // Nothing to do
    }

    @Override
    public TimedResult<File> getDocuments(String folderId) throws OXException {
        Map<String, VersionContainer> files = storage.get(folderId);

        return new InMemoryTimedResult(files);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields) throws OXException {
        return getDocuments(folderId);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {
        return new InMemoryTimedResult(storage.get(folderId), sort, order);
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id) throws OXException {
        VersionContainer versionContainer = getVersionContainer(folderId, id);

        return new AllVersionsTimedResult(versionContainer);
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields) throws OXException {
        VersionContainer versionContainer = getVersionContainer(folderId, id);

        return new AllVersionsTimedResult(versionContainer);
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        VersionContainer versionContainer = getVersionContainer(folderId, id);

        return new AllVersionsTimedResult(versionContainer, sort, order);
    }

    @Override
    public TimedResult<File> getDocuments(final List<IDTuple> ids, List<Field> fields) throws OXException {

        return new TimedResult<File>() {

            @Override
            public SearchIterator<File> results() throws OXException {
                List<File> files = new ArrayList<File>(ids.size());
                for (IDTuple idTuple : ids) {
                    files.add(getVersionContainer(idTuple.getFolder(), idTuple.getId()).getCurrentVersion().getFile());
                }

                return new SearchIteratorAdapter<File>(files.iterator(), files.size());
            }

            @Override
            public long sequenceNumber() throws OXException {
                return System.currentTimeMillis();
            }
        };
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        return new DeltaImpl<File>(getDocuments(folderId).results(), SearchIteratorAdapter.<File>emptyIterator(),  SearchIteratorAdapter.<File>emptyIterator(), System.currentTimeMillis());
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        return new DeltaImpl<File>(getDocuments(folderId, fields, sort, order).results(), SearchIteratorAdapter.<File>emptyIterator(),  SearchIteratorAdapter.<File>emptyIterator(), System.currentTimeMillis());
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
        return SearchIteratorAdapter. <File> emptyIterator();
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
        return SearchIteratorAdapter.emptyIterator();
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

    private final class InMemoryTimedResult implements TimedResult<File> {

        private final Map<String, VersionContainer> files;

        private final long sequenceNumber;

        private Field sort;

        private SortDirection order;

        public InMemoryTimedResult(Map<String, VersionContainer> files) {
            this.files = files == null ? new HashMap<String, VersionContainer>() : files;
            this.sequenceNumber = System.currentTimeMillis();
        }

        public InMemoryTimedResult(Map<String, VersionContainer> files, Field sort, SortDirection order) {
            this(files);
            this.sort = sort;
            this.order = order;
        }

        @Override
        public SearchIterator<File> results() throws OXException {
            List<File> fileList = new ArrayList<File>(files.size());
            for (VersionContainer container : files.values()) {
                File file = container.getCurrentVersion().getFile();
                fileList.add(file);
            }
            if (sort != null && order != null) {
                Collections.sort(fileList, order.comparatorBy(sort));
            }

            return new SearchIteratorAdapter<File>(fileList.iterator(), fileList.size());
        }

        @Override
        public long sequenceNumber() throws OXException {
            return sequenceNumber;
        }

    }

    /**
     * {@link AllVersionsTimedResult}
     *
     * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
     */
    public class AllVersionsTimedResult implements TimedResult<File> {

        private final VersionContainer versionContainer;

        private Field sort;

        private SortDirection order;

        private final long sequenceNumber = System.currentTimeMillis();

        public AllVersionsTimedResult(VersionContainer versionContainer) {
            super();
            this.versionContainer = versionContainer;
        }

        public AllVersionsTimedResult(VersionContainer versionContainer, Field sort, SortDirection order) {
            super();
            this.versionContainer = versionContainer;
            this.sort = sort;
            this.order = order;
        }

        @Override
        public SearchIterator<File> results() throws OXException {
            if (versionContainer == null) {
                return SearchIteratorAdapter.<File> emptyIterator();
            }
            Collection<FileHolder> allVersions = versionContainer.getAllVersions();
            List<File> versions = new ArrayList<File>(allVersions.size());

            for (FileHolder fileHolder : allVersions) {
                versions.add(fileHolder.getFile());
            }

            if (sort != null && order != null) {
                Collections.sort(versions, order.comparatorBy(sort));
            }

            return new SearchIteratorAdapter<File>(versions.iterator(), versions.size());
        }

        @Override
        public long sequenceNumber() throws OXException {
            return sequenceNumber;
        }

    }

}
