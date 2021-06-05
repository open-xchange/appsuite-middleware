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

package com.openexchange.file.storage.webdav;

import static com.openexchange.file.storage.webdav.WebDAVUtils.extractLockToken;
import static com.openexchange.file.storage.webdav.WebDAVUtils.extractLockedUntil;
import static com.openexchange.file.storage.webdav.WebDAVUtils.find;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.l;
import static com.openexchange.webdav.client.PropertyName.DAV_CREATIONDATE;
import static com.openexchange.webdav.client.PropertyName.DAV_DISPLAYNAME;
import static com.openexchange.webdav.client.PropertyName.DAV_GETCONTENTLENGTH;
import static com.openexchange.webdav.client.PropertyName.DAV_GETCONTENTTYPE;
import static com.openexchange.webdav.client.PropertyName.DAV_GETETAG;
import static com.openexchange.webdav.client.PropertyName.DAV_GETLASTMODIFIED;
import static com.openexchange.webdav.client.PropertyName.DAV_LOCKDISCOVERY;
import static com.openexchange.webdav.client.PropertyName.DAV_RESOURCETYPE;
import static com.openexchange.webdav.client.WebDAVClient.DEPTH_0;
import static com.openexchange.webdav.client.WebDAVClient.DEPTH_1;
import static com.openexchange.webdav.client.WebDAVClient.DEPTH_INFINITY;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.namespace.QName;
import org.apache.http.HttpStatus;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAutoRenameFoldersAccess;
import com.openexchange.file.storage.FileStorageEfficientRetrieval;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageLockedFileAccess;
import com.openexchange.file.storage.FileStorageSequenceNumberProvider;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.NameBuilder;
import com.openexchange.file.storage.webdav.exception.WebdavExceptionCodes;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.FileKnowingInputStream;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.webdav.client.PropertyName;
import com.openexchange.webdav.client.WebDAVClient;
import com.openexchange.webdav.client.WebDAVClientException;
import com.openexchange.webdav.client.WebDAVResource;

/**
 * {@link AbstractWebDAVFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.10.4
 */
public abstract class AbstractWebDAVFileAccess extends AbstractWebDAVAccess implements FileStorageLockedFileAccess, FileStorageSequenceNumberProvider, FileStorageAutoRenameFoldersAccess, FileStorageEfficientRetrieval {

    /** An empty search iterator */
    protected static final SearchIterator<File> EMPTY_ITER = SearchIteratorAdapter.emptyIterator();

    /**
     * Initializes a new {@link AbstractWebDAVFileAccess}.
     *
     * @param webdavClient The WebDAV client to use
     * @param accountAccess A WebDAV account access reference
     * @throws {@link OXException} in case the account is not properly configured
     */
    public AbstractWebDAVFileAccess(WebDAVClient webdavClient, AbstractWebDAVAccountAccess accountAccess) throws OXException {
        super(webdavClient, accountAccess);
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
    public void setRequestTransactional(boolean transactional) {
        // Nope
    }

    @Override
    public void setCommitsTransaction(boolean commits) {
        // Nope
    }

    @Override
    public boolean exists(String folderId, String id, String version) throws OXException {
        if (CURRENT_VERSION != version) {
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(account.getFileStorageService().getId());
        }
        WebDAVPath path = getWebDAVPath(folderId, id);
        try {
            return client.exists(path.toString(), null);
        } catch (WebDAVClientException e) {
            throw asOXException(e, folderId, id);
        }
    }

    @Override
    public WebDAVFile getFileMetadata(String folderId, String id, String version) throws OXException {
        return getMetadata(folderId, id, version, ALL_FIELDS);
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber) throws OXException {
        return saveFileMetadata(file, sequenceNumber, ALL_FIELDS);
    }

    @Override
    public IDTuple saveFileMetadata(File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        if (NEW == file.getId()) {
            return saveDocument(file, null, sequenceNumber, modifiedFields);
        }
        WebDAVPath path = getWebDAVPath(file.getFolderId(), file.getId());
        List<Field> fieldsToUpdate = new ArrayList<>(null == modifiedFields ? Arrays.asList(Field.values()) : modifiedFields);
        /*
         * check for rename through update, first
         */
        if ((null == modifiedFields || modifiedFields.contains(Field.FILENAME)) &&
            null != file.getFileName() && false == file.getFileName().equals(path.getName())) {
            WebDAVPath targetPath = path.getParent().append(file.getFileName(), false);
            try {
                final Map<String, String> headers = Collections.singletonMap("Overwrite", "F");
                client.move(path.toString(), targetPath.toString(), headers);
            } catch (WebDAVClientException e) {
                if (HttpStatus.SC_PRECONDITION_FAILED == e.getStatusCode()) {
                    throw FileStorageExceptionCodes.FILE_ALREADY_EXISTS.create(e);
                }
                throw asOXException(e, file.getFolderId(), file.getId());
            }
            fieldsToUpdate = Field.reduceBy(fieldsToUpdate, Field.FILENAME);
            path = targetPath;
        }
        IDTuple newFileId = getFileId(path);
        Map<QName, Object> props = getPropertiesToSet(file, fieldsToUpdate);
        if (props.isEmpty()) {
            /*
             * nothing else to do
             */
            return newFileId;
        }
        Map<QName, Object> propsToSet = new HashMap<>();
        Set<QName> propsToRemove = new HashSet<>();
        for (Entry<QName, Object> entry : props.entrySet()) {
            if (null == entry.getValue()) {
                propsToRemove.add(entry.getKey());
            } else {
                propsToSet.put(entry.getKey(), entry.getValue());
            }
        }
        try {
            client.propPatch(path.toString(), propsToSet, propsToRemove, null);
        } catch (WebDAVClientException e) {
            throw asOXException(e, newFileId.getFolder(), newFileId.getId());
        }
        return newFileId;
    }

    @Override
    public IDTuple copy(IDTuple source, String version, String destFolder, File update, InputStream newFil, List<Field> modifiedFields) throws OXException {
        if (CURRENT_VERSION != version) {
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(account.getFileStorageService().getId());
        }
        WebDAVPath path = getWebDAVPath(source.getFolder(), source.getId());
        WebDAVPath targetPath;
        if (null == update || Strings.isEmpty(update.getFileName()) || null != modifiedFields && false == modifiedFields.contains(Field.FILENAME)) {
            /*
             * take over target filename from source file
             */
            targetPath = getWebDAVPath(destFolder).append(path.getName(), false);
        } else {
            /*
             * use filename from supplied metadata
             */
            if (Strings.isEmpty(update.getFileName())) {
                throw FileStorageExceptionCodes.MISSING_FILE_NAME.create();
            }
            targetPath = getWebDAVPath(destFolder).append(update.getFileName(), false);
        }
        try {
            Map<String, String> header = Collections.singletonMap("Overwrite", "F");
            client.copy(path.toString(), targetPath.toString(), header);
        } catch (WebDAVClientException e) {
            if (HttpStatus.SC_PRECONDITION_FAILED == e.getStatusCode()) {
                throw FileStorageExceptionCodes.FILE_ALREADY_EXISTS.create(e);
            }
            throw asOXException(e, source.getFolder(), source.getId());
        }
        IDTuple id = getFileId(targetPath);
        if (null != update) {
            File toUpdate = new DefaultFile(update);
            toUpdate.setFolderId(id.getFolder());
            toUpdate.setId(id.getId());
            saveFileMetadata(toUpdate, DISTANT_FUTURE, modifiedFields);
        }
        return id;
    }

    @Override
    public IDTuple move(IDTuple source, String destFolder, long sequenceNumber, File update, List<File.Field> modifiedFields) throws OXException {
        WebDAVPath path = getWebDAVPath(source.getFolder(), source.getId());
        WebDAVPath targetPath;
        if (null == update || Strings.isEmpty(update.getFileName()) || null != modifiedFields && false == modifiedFields.contains(Field.FILENAME)) {
            /*
             * take over target filename from source file
             */
            targetPath = getWebDAVPath(destFolder).append(path.getName(), false);
        } else {
            /*
             * use filename from supplied metadata
             */
            targetPath = getWebDAVPath(destFolder).append(update.getFileName(), false);
        }
        try {
            final Map<String, String> headers = Collections.singletonMap("Overwrite", "F");
            client.move(path.toString(), targetPath.toString(), headers);
        } catch (WebDAVClientException e) {
            if (HttpStatus.SC_PRECONDITION_FAILED == e.getStatusCode()) {
                throw FileStorageExceptionCodes.FILE_ALREADY_EXISTS.create(e);
            }
            throw asOXException(e, source.getFolder(), source.getId());
        }
        IDTuple id = getFileId(targetPath);
        if (null != update) {
            File toUpdate = new DefaultFile(update);
            toUpdate.setFolderId(id.getFolder());
            toUpdate.setId(id.getId());
            saveFileMetadata(toUpdate, DISTANT_FUTURE, modifiedFields);
        }
        return id;
    }

    @Override
    public InputStream getDocument(String folderId, String id, String version) throws OXException {
        if (CURRENT_VERSION != version) {
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(account.getFileStorageService().getId());
        }
        WebDAVPath path = getWebDAVPath(folderId, id);
        try {
            return client.get(path.toString(), null);
        } catch (WebDAVClientException e) {
            throw asOXException(e, folderId, id);
        }
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        return saveDocument(file, data, sequenceNumber, null);
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        List<Field> fieldsToUpdate = new ArrayList<>(null == modifiedFields ? Arrays.asList(Field.values()) : modifiedFields);
        WebDAVPath path = null;
        if (NEW == file.getId()) {
            /*
             * upload new file
             */
            if (null == file.getFileName()) {
                throw FileStorageExceptionCodes.MISSING_FILE_NAME.create(file.getFileName());
            }
            if (null == data) {
                throw FileStorageExceptionCodes.NO_CONTENT.create(file.getFileName());
            }

            NameBuilder name = NameBuilder.nameBuilderFor(file.getFileName());
            boolean retry = true;
            if (data instanceof FileKnowingInputStream) {
                java.io.File backingFile = ((FileKnowingInputStream) data).getFile();
                InputStream in = data;

                while (retry) {
                    path = getWebDAVPath(file.getFolderId()).append(name.toString(), false);
                    Map<String, String> headers = Collections.singletonMap("If-None-Match", "*");
                    try {
                        client.put(path.toString(), in, file.getFileMIMEType(), file.getFileSize(), headers);
                        retry = false;
                    } catch (WebDAVClientException e) {
                        if (HttpStatus.SC_PRECONDITION_FAILED == e.getStatusCode()) {
                            name.advance();
                            try {
                                in = new FileInputStream(backingFile);
                            } catch (FileNotFoundException fnfe) {
                                throw FileStorageExceptionCodes.IO_ERROR.create(fnfe, fnfe.getMessage());
                            }
                            continue;
                        }
                        throw asOXException(e, file.getFolderId(), NEW);
                    }
                }

            } else {
                ThresholdFileHolder sink = null;
                try {
                    sink = new ThresholdFileHolder();
                    sink.write(data); // Implicitly closes 'data' input stream

                    while (retry) {
                        path = getWebDAVPath(file.getFolderId()).append(name.toString(), false);
                        Map<String, String> headers = Collections.singletonMap("If-None-Match", "*");
                        try {
                            InputStream in = sink.getStream();
                            client.put(path.toString(), in, file.getFileMIMEType(), file.getFileSize(), headers);
                            retry = false;
                        } catch (WebDAVClientException e) {
                            if (HttpStatus.SC_PRECONDITION_FAILED == e.getStatusCode()) {
                                name.advance();
                                continue;
                            }
                            throw asOXException(e, file.getFolderId(), NEW);
                        }
                    }
                } finally {
                    Streams.close(sink);
                }
            }
            file.setId(getFileId(path).getId());
        } else {
            /*
             * overwrite existing file
             */
            //Check for a conflict by comparing the sequence number (last_modified time stamp) with the sequence number given
            final WebDAVFile lastModifiedData = getMetadata(file.getFolderId(), file.getId(), null, Collections.singletonList(Field.LAST_MODIFIED));
            if (sequenceNumber == FileStorageFileAccess.DISTANT_FUTURE || lastModifiedData.getSequenceNumber() <= sequenceNumber) {
                path = getWebDAVPath(file.getFolderId(), file.getId());
                final Map<String, String> headers = Collections.singletonMap("If-Match", lastModifiedData.getEtag());
                try {
                    client.put(path.toString(), data, file.getFileMIMEType(), file.getFileSize(), headers);
                } catch (WebDAVClientException e) {
                    if (HttpStatus.SC_PRECONDITION_FAILED == e.getStatusCode()) {
                        //Conflict
                        throw WebdavExceptionCodes.MODIFIED_CONCURRENTLY.create();
                    }
                    throw asOXException(e, file.getFolderId(), file.getId());
                }
            } else {
                //Conflict
                throw WebdavExceptionCodes.MODIFIED_CONCURRENTLY.create();
            }
        }
        /*
         * update metadata as needed
         */
        fieldsToUpdate = Field.reduceBy(fieldsToUpdate, Field.FOLDER_ID, Field.FILE_MIMETYPE, Field.FILE_SIZE, Field.FILENAME, Field.UNIQUE_ID);
        return saveFileMetadata(file, sequenceNumber, fieldsToUpdate);
    }

    @Override
    public void removeDocument(String folderId, long sequenceNumber) throws OXException {
        removeDocument(folderId, sequenceNumber, false);
    }

    public void removeDocument(String folderId, long sequenceNumber, boolean hardDelete) throws OXException {
        SearchIterator<File> iterator = getDocuments(folderId).results();
        List<IDTuple> ids = new ArrayList<>();
        try {
            while (iterator.hasNext()) {
                File document = iterator.next();
                ids.add(new IDTuple(document.getFolderId(), document.getId()));
            }
        } finally {
            SearchIterators.close(iterator);
        }
        removeDocument(ids, sequenceNumber, hardDelete);
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber, boolean hardDelete) throws OXException {
        List<IDTuple> notDeleted = new ArrayList<>();
        for (IDTuple id : ids) {
            try {
                //Check for a conflict by comparing the sequence number (last_modified time stamp) with the sequence number given
                final WebDAVFile lastModifiedData = getMetadata(id.getFolder(), id.getId(), null, Collections.singletonList(Field.LAST_MODIFIED));
                if (sequenceNumber == FileStorageFileAccess.DISTANT_FUTURE || lastModifiedData.getSequenceNumber() <= sequenceNumber) {
                    final Map<String, String> headers = Collections.singletonMap("If-Match", lastModifiedData.getEtag());
                    client.delete(getWebDAVPath(id.getFolder(), id.getId()).toString(), headers);
                }
                else {
                    //Conflict
                    notDeleted.add(id);
                }
            } catch (@SuppressWarnings("unused") OXException e) {
                notDeleted.add(id);
            }
        }
        return notDeleted;
    }

    @Override
    public void touch(String folderId, String id) throws OXException {
        exists(folderId, id, CURRENT_VERSION);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId) throws OXException {
        return getDocuments(folderId, ALL_FIELDS);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields) throws OXException {
        return getDocuments(folderId, fields, null, SortDirection.DEFAULT);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> fields, Field sort, SortDirection order) throws OXException {
        WebDAVPath path = getWebDAVPath(folderId);
        List<WebDAVResource> resources;
        try {
            resources = client.propFind(path.toString(), DEPTH_1, getPropertiesToQuery(fields, sort), null);
        } catch (WebDAVClientException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode()) {
                throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                    folderId, account.getId(), account.getFileStorageService().getId(), I(session.getUserId()), I(session.getContextId()));
            }
            throw asOXException(e);
        }
        List<File> files = new ArrayList<>();
        for (WebDAVResource resource : resources) {
            if (false == resource.isCollection()) {
                files.add(getWebDAVFile(resource));
            }
        }
        sort(files, sort, order);
        return new FileTimedResult(files);
    }

    /**
     * Constructs a file storage file from the supplied WebDAV resource.
     * <p/>
     * By default, all common properties are taken over if set. Override if applicable.
     *
     * @param resource The WebDAV resource to create the file storage file from
     * @return The file storage file
     * @throws UnsupportedOperationException if the specified resource is a collection
     * @throws OXException if any other error is occurred
     */
    @SuppressWarnings("unused")
    protected WebDAVFile getWebDAVFile(WebDAVResource resource) throws OXException {
        if (resource.isCollection()) {
            throw new UnsupportedOperationException();
        }
        WebDAVPath path = new WebDAVPath(resource.getHref());
        IDTuple idTuple = getFileId(path);
        WebDAVFile file = new WebDAVFile();
        file.setId(idTuple.getId());
        file.setFolderId(idTuple.getFolder());
        file.setFileName(path.getName());
        file.setNumberOfVersions(-1);
        file.setIsCurrentVersion(true);
        file.setLastModified(resource.getModifiedDate());
        file.setModifiedBy(session.getUserId()); //TODO otherwise client continously looks up user 0
        file.setCreated(resource.getCreationDate());
        file.setCreatedBy(session.getUserId()); //TODO otherwise client continously looks up user 0
        file.setFileSize(null != resource.getContentLength() ? l(resource.getContentLength()) : -1L);
        file.setLockedUntil(extractLockedUntil(resource.getProperty(DAV_LOCKDISCOVERY)));
        file.setEtag(resource.getEtag());
        return file;
    }

    @Override
    public TimedResult<File> getDocuments(List<IDTuple> ids, List<Field> fields) throws OXException {
        List<File> files = new LinkedList<>();
        for (IDTuple idTuple : ids) {
            files.add(getMetadata(idTuple.getFolder(), idTuple.getId(), CURRENT_VERSION, fields));
        }
        return new FileTimedResult(files);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        return getDelta(folderId, updateSince, fields, null, SortDirection.DEFAULT, ignoreDeleted);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, Field sort, SortDirection order, int start, int end) throws OXException {
        return search(pattern, fields, folderId, false, sort, order, start, end);
    }

    @Override
    public SearchIterator<File> search(String pattern, List<Field> fields, String folderId, boolean includeSubfolders, Field sort, SortDirection order, int start, int end) throws OXException {
        try {
            List<Field> fieldsToUse = Field.addDateFieldsIfNeeded(fields, sort);
            // Search by pattern
            List<File> files = searchByFileNamePattern(pattern, folderId, includeSubfolders, fieldsToUse, sort, order);

            // Start, end...
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

            return new SearchIteratorAdapter<>(files.iterator(), files.size());
        } catch (RuntimeException e) {
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public Map<String, Long> getSequenceNumbers(List<String> folderIds) throws OXException {
        Map<String, Long> sequenceNumbers = new HashMap<>(folderIds.size());
        Set<QName> props = Collections.singleton(PropertyName.DAV_GETETAG);
        for (String folderId : folderIds) {
            WebDAVPath path = getWebDAVPath(folderId);
            List<WebDAVResource> resources = client.propFind(path.toString(), DEPTH_0, props, null);
            WebDAVResource resource = find(resources, path);
            if (null == resource) {
                throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                    folderId, account.getId(), account.getFileStorageService().getId(), I(session.getUserId()), I(session.getContextId()));
            }
            String etag = resource.getEtag();
            if (null == etag) {
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create("got no etag for " + path);
            }
            sequenceNumbers.put(folderId, L(etag.hashCode()));
        }
        return sequenceNumbers;
    }

    /**
     * Searches for files whose filename matches the supplied pattern.
     *
     * @param pattern The pattern to search for
     * @param folderId The parent folder identifier to restrict the search to, or <code>null</code> to search all folders
     * @param includeSubfolders <code>true</code> to include subfolderes, <code>false</code>, otherwise
     * @param fields The fields to retrieve
     * @param sort The field to use to sort the results
     * @param order The sort order to apply
     * @return The found files
     */
    protected List<File> searchByFileNamePattern(String pattern, String folderId, boolean includeSubfolders, List<Field> fields, Field sort, SortDirection order) throws OXException {
        WebDAVPath path = getWebDAVPath(folderId);
        List<WebDAVResource> resources;
        try {
            resources = client.propFind(path.toString(), includeSubfolders ? DEPTH_INFINITY : DEPTH_1, getPropertiesToQuery(fields, sort), null);
        } catch (WebDAVClientException e) {
            if (HttpStatus.SC_NOT_FOUND == e.getStatusCode()) {
                throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                    folderId, account.getId(), account.getFileStorageService().getId(), I(session.getUserId()), I(session.getContextId()));
            }
            else if (HttpStatus.SC_FORBIDDEN == e.getStatusCode()) {
                //Infinity DEPTH not allowed by the server
                throw FileStorageExceptionCodes.SEARCH_TERM_NOT_SUPPORTED.create(e, "WebDAV infinity depth");
            }
            throw asOXException(e);
        }

        List<File> files = new ArrayList<>();
        for (WebDAVResource resource : resources) {
            if (false == resource.isCollection()) {
                files.add(getWebDAVFile(resource));
            }
        }

        List<File> result = new ArrayList<>();
        String filename = pattern.toLowerCase();
        for(File candidate : files) {
            if (candidate.getFileName().toLowerCase().contains(filename)) {
                result.add(candidate);
            }
        }
        sort(result, sort, order);
        return result;
    }

    /**
     * Sorts the supplied list of files if needed.
     *
     * @param files The files to sort
     * @param sort The sort order, or <code>null</code> if not specified
     * @param order The sort direction
     */
    protected static void sort(List<File> files, Field sort, SortDirection order) {
        if (null != sort && 1 < files.size()) {
            Collections.sort(files, order.comparatorBy(sort));
        }
    }

    /**
     * Escapes a pattern string to be used in Google Drive queries.
     *
     * @param pattern The pattern to escape
     * @return The escaped pattern
     */
    protected String escape(String pattern) {
        if (null == pattern) {
            return pattern;
        }

        StringBuilder opt = null;
        int length = pattern.length();
        for (int i = 0; i < length; i++) {
            char c = pattern.charAt(i);
            if ('\'' == c) {
                if (null == opt) {
                    opt = new StringBuilder(length);
                    if (i > 0) {
                        opt.append(pattern, 0, i);
                    }
                }
                opt.append("\\'");
            } else {
                if (null != opt) {
                    opt.append(c);
                }
            }
        }
        return null == opt ? pattern : opt.toString();
    }

    @Override
    public void unlock(String folderId, String id) throws OXException {
        WebDAVPath path = getWebDAVPath(folderId, id);
        Set<QName> props = Collections.singleton(DAV_LOCKDISCOVERY);
        WebDAVResource resource;
        try {
            resource = find(client.propFind(path.toString(), DEPTH_0, props, null), path);
        } catch (WebDAVClientException e) {
            throw asOXException(e, folderId, id);
        }
        if (null == resource) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
        }
        String lockToken = extractLockToken(resource.getProperty(DAV_LOCKDISCOVERY));
        if (null != lockToken) {
            try {
                client.unlock(path.toString(), lockToken, null);
            } catch (WebDAVClientException e) {
                throw asOXException(e, folderId, id);
            }
        }
    }

    @Override
    public void lock(String folderId, String id, long diff) throws OXException {
        WebDAVPath path = getWebDAVPath(folderId, id);
        try {
            client.lock(path.toString(), diff, null);
        } catch (WebDAVClientException e) {
            throw asOXException(e, folderId, id);
        }
    }

    @Override
    public String createFolder(FileStorageFolder toCreate, boolean autoRename) throws OXException {
        return accountAccess.getFolderAccess().createFolder(toCreate, autoRename);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName, boolean autoRename) throws OXException {
        return accountAccess.getFolderAccess().moveFolder(folderId, newParentId, newName, autoRename);
    }

    @Override
    public Document getDocumentAndMetadata(String folderId, String fileId, String version) throws OXException {
       return getDocumentAndMetadata(folderId, fileId, version, null);
    }

    @Override
    public Document getDocumentAndMetadata(String folderId, String fileId, String version, String clientETag) throws OXException  {

        //Getting the documents metadata
        final WebDAVFile fileMetadata = getFileMetadata(folderId, fileId, version);

        final String etag = fileMetadata.getEtag();
        //@formatter:off
        return Strings.isNotEmpty(clientETag) && clientETag.equals(etag) ?
                new WebDAVDocument(fileMetadata, null, etag) :
                new WebDAVDocument(fileMetadata, () -> getDocument(folderId, fileId, version), etag);
        //@formatter:on
    }

    protected WebDAVFile getMetadata(String folderId, String id, String version, List<Field> fields) throws OXException {
        if (CURRENT_VERSION != version) {
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(account.getFileStorageService().getId());
        }
        WebDAVPath path = getWebDAVPath(folderId, id);
        WebDAVResource resource = null;
        try {
            resource = find(client.propFind(path.toString(), DEPTH_0, getPropertiesToQuery(fields), null), path);
        } catch (WebDAVClientException e) {
            throw asOXException(e, folderId, id);
        }
        if (null == resource) {
            throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(id, folderId);
        }
        return getWebDAVFile(resource);
    }

    /**
     * Gets the qualified property names to retrieve when doing PROPFIND queries for resources from the WebDAV server.
     * <p/>
     * Defaults to a generic mapping to the standard WebDAV property names, override if applicable.
     *
     * @param requestedFields The fields requested by the client, or <code>null</code> if not specified
     * @return The property names to query
     */
    protected Set<QName> getPropertiesToQuery(Collection<Field> requestedFields) {
        return getPropertiesToQuery(requestedFields, null);
    }

    /**
     * Gets the qualified property names to retrieve when doing PROPFIND queries for resources from the WebDAV server.
     * <p/>
     * Defaults to a generic mapping to the standard WebDAV property names, override if applicable.
     *
     * @param requestedFields The fields requested by the client, or <code>null</code> if not specified
     * @param requestedSortBy The "sort by" field requested by the client, or <code>null</code> if not specified
     * @return The property names to query
     */
    protected Set<QName> getPropertiesToQuery(Collection<Field> requestedFields, Field requestedSortBy) {
        Set<Field> fields = null == requestedFields ? EnumSet.allOf(Field.class) : requestedFields.isEmpty() ? EnumSet.noneOf(Field.class) : EnumSet.copyOf(requestedFields);
        if (null != requestedSortBy) {
            fields.add(requestedSortBy);
        }
        HashSet<QName> props = new HashSet<>(fields.size() + 1);
        props.add(DAV_GETETAG);
        props.add(DAV_RESOURCETYPE);
        for (Field field : fields) {
            switch (field) {
                case LAST_MODIFIED:
                case LAST_MODIFIED_UTC:
                    props.add(DAV_GETLASTMODIFIED);
                    break;
                case CREATED:
                    props.add(DAV_CREATIONDATE);
                    break;
                case TITLE:
                    props.add(DAV_DISPLAYNAME);
                    break;
                case FILE_SIZE:
                    props.add(DAV_GETCONTENTLENGTH);
                    break;
                case FILE_MIMETYPE:
                    props.add(DAV_GETCONTENTTYPE);
                    break;
                case LOCKED_UNTIL:
                    props.add(DAV_LOCKDISCOVERY);
                    break;
                default:
                    break;
            }
        }
        return props;
    }

    protected Map<QName, Object> getPropertiesToSet(File file, Collection<Field> indicatedFields) {
        Map<QName, Object> props = new HashMap<>();
        Set<Field> fields = null == indicatedFields ? EnumSet.allOf(Field.class) : indicatedFields.isEmpty() ? EnumSet.noneOf(Field.class) : EnumSet.copyOf(indicatedFields);
        for (Field field : fields) {
            switch (field) {
                case TITLE:
                    props.put(DAV_DISPLAYNAME, file.getTitle());
                    break;
                case FILE_MIMETYPE:
                    props.put(DAV_GETCONTENTTYPE, file.getFileMIMEType());
                    break;
                case CREATED:
                    props.put(DAV_CREATIONDATE, file.getCreated());
                    break;
                case LAST_MODIFIED:
                case LAST_MODIFIED_UTC:
                    props.put(DAV_GETLASTMODIFIED, file.getLastModified());
                    break;
                default:
                    break;
            }
        }
        return props;
    }

    /**
     * Gets an appropriate file storage exception for the supplied WebDAV client exception that occurred during communication with the
     * remote WebDAV server when accessing a specific file.
     *
     * @param e The {@link WebDAVClientException} to get the {@link OXException} for
     * @param folderId The actual folder identifier
     * @param fileId The actual file identifier
     * @return The exception to re-throw
     */
    protected OXException asOXException(WebDAVClientException e, String folderId, String fileId) {
        switch (e.getStatusCode()) {
            case HttpStatus.SC_NOT_FOUND:
                return FileStorageExceptionCodes.FILE_NOT_FOUND.create(e, fileId, folderId);
            case HttpStatus.SC_LOCKED:
                return FileStorageExceptionCodes.UPDATE_DENIED.create(optName(folderId, fileId), e.getMessage());
            default:
                return super.asOXException(e);
        }
    }

    private String optName(String folderId, String fileId) {
        try {
            return getWebDAVPath(folderId, fileId).getName();
        } catch (@SuppressWarnings("unused") Exception e) {
            return fileId;
        }
    }

}
