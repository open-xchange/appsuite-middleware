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

package com.openexchange.file.storage.onedrive;

import static com.openexchange.java.Strings.isEmpty;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.file.storage.onedrive.access.OneDriveAccess;
import com.openexchange.file.storage.onedrive.rest.file.RestFile;
import com.openexchange.file.storage.onedrive.rest.file.RestFileResponse;
import com.openexchange.file.storage.search.FileNameTerm;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.Charsets;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link OneDriveFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OneDriveFileAccess extends AbstractOneDriveResourceAccess implements ThumbnailAware {

    private final OneDriveAccountAccess accountAccess;
    final int userId;

    /**
     * Initializes a new {@link OneDriveFileAccess}.
     */
    public OneDriveFileAccess(OneDriveAccess boxAccess, FileStorageAccount account, Session session, OneDriveAccountAccess accountAccess) throws OXException {
        super(boxAccess, account, session);
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
        return perform(new OneDriveClosure<Boolean>() {

            @Override
            protected Boolean doPerform(DefaultHttpClient httpClient) throws OXException, IOException {
                try {
                    HttpGet method = new HttpGet(buildUri(id, initiateQueryString()));

                    HttpResponse response = httpClient.execute(method);
                    return Boolean.valueOf(200 == response.getStatusLine().getStatusCode());
                } catch (HttpResponseException e) {
                    if (404 == e.getStatusCode()) {
                        return Boolean.FALSE;
                    }
                    throw e;
                }

            }
        }).booleanValue();
    }

    @Override
    public File getFileMetadata(final String folderId, final String id, final String version) throws OXException {
        return perform(new OneDriveClosure<File>() {

            @Override
            protected File doPerform(DefaultHttpClient httpClient) throws OXException, IOException {
                HttpGet method = new HttpGet(buildUri(id, initiateQueryString()));

                RestFileResponse restResponse = handleHttpResponse(httpClient.execute(method), RestFileResponse.class);
                RestFile restFile = restResponse.getData().get(0);
                return new OneDriveFile(folderId, id, userId, rootFolderId).parseBoxFile(restFile);
            }
        });
    }

    @Override
    public void saveFileMetadata(final File file, final long sequenceNumber) throws OXException {
        saveFileMetadata(file, sequenceNumber, null);
    }

    @Override
    public void saveFileMetadata(final File file, long sequenceNumber, List<Field> modifiedFields) throws OXException {
        if (null == modifiedFields || modifiedFields.contains(Field.FILENAME)) {
            perform(new OneDriveClosure<Void>() {

                @Override
                protected Void doPerform(DefaultHttpClient httpClient) throws OXException, JSONException, IOException {
                    try {
                        HttpPost method = new HttpPost(buildUri(file.getId(), null));
                        method.setHeader("Authorization", "Bearer " + oneDriveAccess.getAccessToken());
                        method.setHeader("Content-Type", "application/json");
                        method.setEntity(asHttpEntity(new JSONObject(2).put("name", file.getFileName())));

                        handleHttpResponse(httpClient.execute(method), Void.class);
                        return null;
                    } catch (HttpResponseException e) {
                        throw handleHttpResponseError(file.getId(), e);
                    }
                }
            });
        }
    }

    @Override
    public IDTuple copy(final IDTuple source, String version, final String destFolder, File update, InputStream newFil, List<Field> modifiedFields) throws OXException {
        if (version != CURRENT_VERSION) {
            // can only copy the current revision
            throw OneDriveExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
        }

        return perform(new OneDriveClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform(DefaultHttpClient httpClient) throws throws OXException, IOException {
                try {
                    OneDriveFile boxfile = boxClient.getFilesManager().getFile(source.getId(), null);
                    checkFileValidity(boxfile);

                    String boxFolderId = toBoxFolderId(destFolder);
                    OneDriveFolder boxfolder = boxClient.getFoldersManager().getFolder(boxFolderId, null);

                    // Check destination folder
                    String title = boxfile.getName();
                    {
                        String baseName;
                        String ext;
                        {
                            int dotPos = title.lastIndexOf('.');
                            if (dotPos > 0) {
                                baseName = title.substring(0, dotPos);
                                ext = title.substring(dotPos);
                            } else {
                                baseName = title;
                                ext = "";
                            }
                        }
                        int count = 1;
                        boolean keepOn = true;
                        while (keepOn) {
                            keepOn = false;
                            for (BoxTypedObject child : boxfolder.getItemCollection().getEntries()) {
                                if (isFile(child) && title.equals(((OneDriveFile) child).getName())) {
                                    keepOn = true;
                                    title = new StringBuilder(baseName).append(" (").append(count++).append(')').append(ext).toString();
                                    break;
                                }
                            }
                        }
                    }

                    BoxItemCopyRequestObject reqObj = BoxItemCopyRequestObject.copyItemRequestObject(boxFolderId);
                    reqObj.setName(title);
                    OneDriveFile copiedFile = boxClient.getFilesManager().copyFile(source.getId(), reqObj);

                    return new IDTuple(destFolder, copiedFile.getId());
                } catch (HttpResponseException e) {
                    throw handleHttpResponseError(source.getId(), e);
                }
            }
        });
    }

    @Override
    public IDTuple move(final IDTuple source, final String destFolder, long sequenceNumber, File update, List<File.Field> modifiedFields) throws OXException {
        return perform(new OneDriveClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform(DefaultHttpClient httpClient) throws OXException, IOException {
                try {
                    OneDriveFile boxfile = boxClient.getFilesManager().getFile(source.getId(), null);
                    checkFileValidity(boxfile);

                    String boxFolderId = toBoxFolderId(destFolder);
                    OneDriveFolder boxfolder = boxClient.getFoldersManager().getFolder(boxFolderId, null);

                    // Check destination folder
                    String title = boxfile.getName();
                    {
                        String baseName;
                        String ext;
                        {
                            int dotPos = title.lastIndexOf('.');
                            if (dotPos > 0) {
                                baseName = title.substring(0, dotPos);
                                ext = title.substring(dotPos);
                            } else {
                                baseName = title;
                                ext = "";
                            }
                        }
                        int count = 1;
                        boolean keepOn = true;
                        while (keepOn) {
                            keepOn = false;
                            for (BoxTypedObject child : boxfolder.getItemCollection().getEntries()) {
                                if (isFile(child) && title.equals(((OneDriveFile) child).getName())) {
                                    keepOn = true;
                                    title = new StringBuilder(baseName).append(" (").append(count++).append(')').append(ext).toString();
                                    break;
                                }
                            }
                        }
                    }

                    BoxFileRequestObject reqObj = BoxFileRequestObject.getRequestObject();
                    reqObj.setName(title);
                    reqObj.setParent(boxFolderId);
                    OneDriveFile movedFile = boxClient.getFilesManager().updateFileInfo(source.getId(), reqObj);

                    return new IDTuple(destFolder, movedFile.getId());
                } catch (HttpResponseException e) {
                    throw handleHttpResponseError(source.getId(), e);
                }
            }
        });
    }

    @Override
    public InputStream getDocument(final String folderId, final String id, final String version) throws OXException {
        return perform(new OneDriveClosure<InputStream>() {

            @Override
            protected InputStream doPerform(DefaultHttpClient httpClient) throws OXException, IOException {
                try {
                    OneDriveFile boxfile = boxClient.getFilesManager().getFile(id, null);
                    checkFileValidity(boxfile);

                    return boxClient.getFilesManager().downloadFile(id, null);
                } catch (HttpResponseException e) {
                    throw handleHttpResponseError(id, e);
                }
            }

        });
    }

    @Override
    public InputStream getThumbnailStream(String folderId, final String id, String version) throws OXException {
        return perform(new OneDriveClosure<InputStream>() {

            @Override
            protected InputStream doPerform(DefaultHttpClient httpClient) throws OXException, IOException {
                try {
                    OneDriveFile boxfile = boxClient.getFilesManager().getFile(id, null);
                    checkFileValidity(boxfile);

                    BoxImageRequestObject reqObj = BoxImageRequestObject.pagePreviewRequestObject(1, 64, 128, 64, 128);
                    BoxThumbnail thumbnail = boxClient.getFilesManager().getThumbnail(id, null, reqObj);

                    return thumbnail.getContent();
                } catch (HttpResponseException e) {
                    throw handleHttpResponseError(id, e);
                }
            }

        });
    }

    @Override
    public void saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        saveDocument(file, data, sequenceNumber, null);
    }

    @Override
    public void saveDocument(final File file, final InputStream data, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        final String id = file.getId();
        final String boxFolderId = toBoxFolderId(file.getFolderId());
        perform(new OneDriveClosure<Void>() {

            @Override
            protected Void doPerform(DefaultHttpClient httpClient) throws OXException, IOException {
                try {

                    if (isEmpty(id) || !exists(null, id, CURRENT_VERSION)) {
                        BoxFileUploadRequestObject reqObj = BoxFileUploadRequestObject.uploadFileRequestObject(boxFolderId, file.getFileName(), data);
                        boxClient.getFilesManager().uploadFile(reqObj);
                    } else {
                        OneDriveFile boxfile = boxClient.getFilesManager().getFile(id, null);
                        checkFileValidity(boxfile);

                        String prevVersion = boxfile.getVersionNumber();

                        BoxFileUploadRequestObject reqObj = BoxFileUploadRequestObject.uploadFileRequestObject(boxFolderId, id, data);
                        boxClient.getFilesManager().uploadNewVersion(id, reqObj);
                    }

                    return null;
                } catch (BoxJSONException e) {
                    throw OneDriveExceptionCodes.ONE_DRIVE_ERROR.create(e, e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw OneDriveExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        });
    }

    @Override
    public void removeDocument(final String folderId, long sequenceNumber) throws OXException {
        BoxClient boxClient = oneDriveAccess.getBoxClient();
        perform(new OneDriveClosure<Void>() {

            @Override
            protected Void doPerform(DefaultHttpClient httpClient) throws OXException, IOException {
                OneDriveFolder folder = boxClient.getFoldersManager().getFolder(toBoxFolderId(folderId), null);

                List<String> toDelete = new LinkedList<String>();
                for (BoxTypedObject child : folder.getItemCollection().getEntries()) {
                    if (isFile(child)) {
                        toDelete.add(child.getId());
                    }
                }

                IBoxFilesManager filesManager = boxClient.getFilesManager();
                for (String id : toDelete) {
                    filesManager.deleteFile(id, null);
                }

                return null;
            }

        });
    }

    @Override
    public List<IDTuple> removeDocument(List<IDTuple> ids, long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, long sequenceNumber, final boolean hardDelete) throws OXException {
        return perform(new OneDriveClosure<List<IDTuple>>() {

            @Override
            protected List<IDTuple> doPerform(DefaultHttpClient httpClient) throws OXException, IOException {

                for (IDTuple idTuple : ids) {
                    try {
                        OneDriveFile file = boxClient.getFilesManager().getFile(idTuple.getId(), null);
                        boxClient.getFilesManager().deleteFile(idTuple.getId(), null);
                    } catch (HttpResponseException e) {
                        if (404 != e.getStatusCode()) {
                            throw e;
                        }
                    }
                }

                return Collections.emptyList();
            }
        });
    }

    @Override
    public String[] removeVersion(String folderId, final String id, String[] versions) throws OXException {
        /*
         * No versioning support
         */
        for (final String version : versions) {
            if (version != CURRENT_VERSION) {
                throw OneDriveExceptionCodes.VERSIONING_NOT_SUPPORTED.create();
            }
        }
        return perform(new OneDriveClosure<String[]>() {

            @Override
            protected String[] doPerform(DefaultHttpClient httpClient) throws OXException, IOException {

                try {
                    OneDriveFile file = boxClient.getFilesManager().getFile(id, null);
                    boxClient.getFilesManager().deleteFile(id, null);
                } catch (HttpResponseException e) {
                    if (404 != e.getStatusCode()) {
                        throw e;
                    }
                }

                return new String[0];
            }

        });
    }

    @Override
    public void unlock(String folderId, String id) throws OXException {
        // Nope
    }

    @Override
    public void lock(String folderId, String id, long diff) throws OXException {
        // Nope
    }

    @Override
    public void touch(String folderId, String id) throws OXException {
        exists(folderId, id, CURRENT_VERSION);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId) throws OXException {
        return perform(new OneDriveClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(DefaultHttpClient httpClient) throws OXException, IOException {

                OneDriveFolder boxfolder = boxClient.getFoldersManager().getFolder(toBoxFolderId(folderId), null);
                IBoxFilesManager filesManager = boxClient.getFilesManager();

                List<File> files = new LinkedList<File>();
                BoxCollection itemCollection = boxfolder.getItemCollection();
                if (itemCollection.getTotalCount().intValue() <= itemCollection.getEntries().size()) {
                    for (BoxTypedObject child : itemCollection.getEntries()) {
                        if (isFile(child)) {
                            files.add(new com.openexchange.file.storage.onedrive.OneDriveFile(folderId, child.getId(), userId, rootFolderId).parseBoxFile(filesManager.getFile(child.getId(), null)));
                        }
                    }
                } else {
                    int offset = 0;
                    final int limit = 100;

                    int resultsFound;
                    do {
                        BoxPagingRequestObject reqObj = BoxPagingRequestObject.pagingRequestObject(limit, offset);
                        BoxCollection collection = boxClient.getFoldersManager().getFolderItems(toBoxFolderId(folderId), reqObj);

                        List<BoxTypedObject> entries = collection.getEntries();
                        resultsFound = entries.size();
                        for (BoxTypedObject typedObject : entries) {
                            if (isFile(typedObject)) {
                                files.add(new com.openexchange.file.storage.onedrive.OneDriveFile(folderId, typedObject.getId(), userId, rootFolderId).parseBoxFile(filesManager.getFile(typedObject.getId(), null)));
                            }
                        }

                        offset += limit;
                    } while (resultsFound == limit);
                }


                return new FileTimedResult(files);
            }
        });
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields) throws OXException {
        return getDocuments(folderId);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        return perform(new OneDriveClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(DefaultHttpClient httpClient) throws OXException, IOException {

                OneDriveFolder boxfolder = boxClient.getFoldersManager().getFolder(toBoxFolderId(folderId), null);
                IBoxFilesManager filesManager = boxClient.getFilesManager();

                List<File> files = new LinkedList<File>();

                BoxCollection itemCollection = boxfolder.getItemCollection();
                if (itemCollection.getTotalCount().intValue() <= itemCollection.getEntries().size()) {
                    for (BoxTypedObject child : itemCollection.getEntries()) {
                        if (isFile(child)) {
                            files.add(new com.openexchange.file.storage.onedrive.OneDriveFile(folderId, child.getId(), userId, rootFolderId).parseBoxFile(filesManager.getFile(child.getId(), null)));
                        }
                    }
                } else {
                    int offset = 0;
                    final int limit = 100;

                    int resultsFound;
                    do {
                        BoxPagingRequestObject reqObj = BoxPagingRequestObject.pagingRequestObject(limit, offset);
                        BoxCollection collection = boxClient.getFoldersManager().getFolderItems(toBoxFolderId(folderId), reqObj);

                        List<BoxTypedObject> entries = collection.getEntries();
                        resultsFound = entries.size();
                        for (BoxTypedObject typedObject : entries) {
                            if (isFile(typedObject)) {
                                files.add(new com.openexchange.file.storage.onedrive.OneDriveFile(folderId, typedObject.getId(), userId, rootFolderId).parseBoxFile(filesManager.getFile(typedObject.getId(), null)));
                            }
                        }

                        offset += limit;
                    } while (resultsFound == limit);
                }

                // Sort collection if needed
                sort(files, sort, order);

                return new FileTimedResult(files);
            }
        });
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id) throws OXException {
        return perform(new OneDriveClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(DefaultHttpClient httpClient) throws OXException, IOException {

                OneDriveFile boxfile = boxClient.getFilesManager().getFile(id, null);
                List<File> files = Collections.<File> singletonList(new com.openexchange.file.storage.onedrive.OneDriveFile(folderId, id, userId, rootFolderId).parseBoxFile(boxfile));

                return new FileTimedResult(files);
            }
        });
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields) throws OXException {
        return getVersions(folderId, id);
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        BoxClient boxClient = oneDriveAccess.getBoxClient();
        return perform(new OneDriveClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(DefaultHttpClient httpClient) throws OXException, IOException {

                OneDriveFile boxfile = boxClient.getFilesManager().getFile(id, null);
                List<File> files = Collections.<File> singletonList(new com.openexchange.file.storage.onedrive.OneDriveFile(folderId, id, userId, rootFolderId).parseBoxFile(boxfile));

                return new FileTimedResult(files);
            }
        });
    }

    @Override
    public TimedResult<File> getDocuments(final List<IDTuple> ids, List<Field> fields) throws OXException {
        return perform(new OneDriveClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(DefaultHttpClient httpClient) throws OXException, IOException {

                List<File> files = new LinkedList<File>();
                for (IDTuple id : ids) {
                    OneDriveFile boxfile = boxClient.getFilesManager().getFile(id.getId(), null);
                    files.add(new com.openexchange.file.storage.onedrive.OneDriveFile(id.getFolder(), id.getId(), userId, rootFolderId).parseBoxFile(boxfile));
                }

                return new FileTimedResult(files);
            }
        });
    }

    private static final SearchIterator<File> EMPTY_ITER = SearchIteratorAdapter.emptyIterator();

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    @Override
    public Delta<File> getDelta(String folderId, long updateSince, List<Field> fields, Field sort, SortDirection order, boolean ignoreDeleted) throws OXException {
        return new FileDelta(EMPTY_ITER, EMPTY_ITER, EMPTY_ITER, 0L);
    }

    @Override
    public SearchIterator<File> search(List<String> folderIds, SearchTerm<?> searchTerm, List<Field> fields, Field sort, final SortDirection order, int start, int end) throws OXException {
        if (FileNameTerm.class.isInstance(searchTerm) && (null == folderIds || 1 == folderIds.size())) {
            String pattern = ((FileNameTerm) searchTerm).getPattern();
            return search(pattern, fields, null != folderIds && 1 == folderIds.size() ? folderIds.get(0) : null, sort, order, start, end);
        }
        throw FileStorageExceptionCodes.SEARCH_TERM_NOT_SUPPORTED.create(searchTerm.getClass().getSimpleName());
    }

    @Override
    public SearchIterator<File> search(final String pattern, List<Field> fields, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        return perform(new OneDriveClosure<SearchIterator<File>>() {

            @Override
            protected SearchIterator<File> doPerform(DefaultHttpClient httpClient) throws OXException, IOException {

                List<File> files = new LinkedList<File>();

                int offset = 0;
                final int limit = 100;

                int resultsFound;
                do {
                    BoxDefaultRequestObject reqObj = new BoxDefaultRequestObject();
                    reqObj.put("type", "file");
                    if (null != folderId) {
                        reqObj.put("ancestor_folder_ids", toBoxFolderId(folderId));
                    }
                    reqObj.setPage(limit, offset);
                    BoxCollection collection = boxClient.getSearchManager().search(null == pattern ? "*" : pattern, reqObj);

                    List<BoxTypedObject> entries = collection.getEntries();
                    resultsFound = entries.size();
                    for (BoxTypedObject typedObject : entries) {
                        OneDriveFile boxfile = (OneDriveFile) typedObject;
                        files.add(new com.openexchange.file.storage.onedrive.OneDriveFile(toFileStorageFolderId(boxfile.getParent().getId()), boxfile.getId(), userId, rootFolderId).parseBoxFile(boxfile));
                    }

                    offset += limit;
                } while (resultsFound == limit);

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

        });
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
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

}
