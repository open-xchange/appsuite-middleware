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

package com.openexchange.file.storage.boxcom;

import static com.openexchange.java.Strings.isEmpty;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.dao.BoxCollection;
import com.box.boxjavalibv2.dao.BoxFile;
import com.box.boxjavalibv2.dao.BoxFolder;
import com.box.boxjavalibv2.dao.BoxThumbnail;
import com.box.boxjavalibv2.dao.BoxTypedObject;
import com.box.boxjavalibv2.exceptions.AuthFatalFailureException;
import com.box.boxjavalibv2.exceptions.BoxJSONException;
import com.box.boxjavalibv2.exceptions.BoxServerException;
import com.box.boxjavalibv2.requests.requestobjects.BoxFileRequestObject;
import com.box.boxjavalibv2.requests.requestobjects.BoxImageRequestObject;
import com.box.boxjavalibv2.requests.requestobjects.BoxItemCopyRequestObject;
import com.box.boxjavalibv2.requests.requestobjects.BoxPagingRequestObject;
import com.box.boxjavalibv2.resourcemanagers.IBoxFilesManager;
import com.box.restclientv2.exceptions.BoxRestException;
import com.box.restclientv2.requestsbase.BoxDefaultRequestObject;
import com.box.restclientv2.requestsbase.BoxFileUploadRequestObject;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileDelta;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageLockedFileAccess;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.FileTimedResult;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.file.storage.boxcom.access.BoxOAuthAccess;
import com.openexchange.file.storage.boxcom.access.extended.ExtendedNonRefreshingBoxClient;
import com.openexchange.file.storage.boxcom.access.extended.requests.requestobjects.PreflightCheckRequestObject;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.SizeKnowingInputStream;
import com.openexchange.java.Streams;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link BoxFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class BoxFileAccess extends AbstractBoxResourceAccess implements ThumbnailAware, FileStorageLockedFileAccess {

    private static final String THUMBNAIL_EXTENSION = "png";

    private final BoxAccountAccess accountAccess;
    final int userId;

    /**
     * Initializes a new {@link BoxFileAccess}.
     */
    public BoxFileAccess(BoxOAuthAccess boxAccess, FileStorageAccount account, Session session, BoxAccountAccess accountAccess) throws OXException {
        super(boxAccess, account, session);
        this.accountAccess = accountAccess;
        userId = session.getUserId();
    }

    protected void checkFileValidity(BoxTypedObject typedObject) throws OXException {
        if (isFolder(typedObject) || null != ((BoxFile) typedObject).getTrashedAt()) {
            throw FileStorageExceptionCodes.NOT_A_FILE.create(BoxConstants.ID, typedObject.getId());
        }
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
        return perform(new BoxClosure<Boolean>() {

            @Override
            protected Boolean doPerform(BoxOAuthAccess boxAccess) throws BoxRestException, BoxServerException, AuthFatalFailureException, OXException {
                try {
                    BoxClient boxClient = boxAccess.<BoxClient>getClient().client;
                    BoxFile file = boxClient.getFilesManager().getFile(id, customRequestObject(Arrays.asList(BoxFile.FIELD_ID)));
                    checkFileValidity(file);
                    return Boolean.TRUE;
                } catch (final BoxRestException e) {
                    if (404 == e.getStatusCode()) {
                        return Boolean.FALSE;
                    }
                    throw e;
                } catch (final BoxServerException e) {
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
        return perform(new BoxClosure<File>() {

            @Override
            protected File doPerform(BoxOAuthAccess boxAccess) throws BoxRestException, BoxServerException, AuthFatalFailureException, OXException {
                try {
                    BoxClient boxClient = boxAccess.<BoxClient>getClient().client;

                    // Versions are only tracked for Box users with premium accounts- Hence we do not support it (anymore)
                    /* final int versions = boxAccess.getBoxClient().getFilesManager().getFileVersions(id, customRequestObject(Arrays.asList(BoxFile.FIELD_NAME))).size(); */

                    BoxFile file = boxClient.getFilesManager().getFile(id, defaultBoxRequest());
                    checkFileValidity(file);

                    com.openexchange.file.storage.boxcom.BoxFile boxFile = new com.openexchange.file.storage.boxcom.BoxFile(folderId, id, userId, rootFolderId).parseBoxFile(file);
                    return boxFile;
                } catch (final BoxServerException e) {
                    throw handleHttpResponseError(id, account.getId(), e);
                }
            }
        });
    }

    @Override
    public IDTuple saveFileMetadata(final File file, final long sequenceNumber) throws OXException {
        return saveFileMetadata(file, sequenceNumber, null);
    }

    @Override
    public IDTuple saveFileMetadata(final File file, long sequenceNumber, final List<Field> modifiedFields) throws OXException {
        if (null == modifiedFields || modifiedFields.contains(Field.FILENAME) || modifiedFields.contains(Field.DESCRIPTION)) {
            return perform(new BoxClosure<IDTuple>() {

                @Override
                protected IDTuple doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                    try {
                        BoxClient boxClient = boxAccess.<BoxClient>getClient().client;
                        BoxFile boxfile = boxClient.getFilesManager().getFile(file.getId(), customRequestObject(Arrays.asList(BoxFile.FIELD_TYPE)));
                        checkFileValidity(boxfile);

                        BoxFileRequestObject requestObject = BoxFileRequestObject.getRequestObject();

                        requestObject.setName(file.getFileName());
                        requestObject.setDescription(file.getDescription());
                        boxfile = boxClient.getFilesManager().updateFileInfo(file.getId(), requestObject);

                        return new IDTuple(file.getFolderId(), boxfile.getId());
                    } catch (final BoxServerException e) {
                        throw handleHttpResponseError(file.getId(), account.getId(), e);
                    }
                }
            });
        }
        return new IDTuple(file.getFolderId(), file.getId());
    }

    @Override
    public IDTuple copy(final IDTuple source, String version, final String destFolder, File update, InputStream newFil, List<Field> modifiedFields) throws OXException {
        if (version != CURRENT_VERSION) {
            // can only copy the current revision
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(BoxConstants.ID);
        }

        return perform(new BoxClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform(BoxOAuthAccess boxAccess) throws BoxRestException, BoxServerException, AuthFatalFailureException, OXException {
                try {
                    BoxClient boxClient = boxAccess.<BoxClient>getClient().client;
                    BoxFile boxfile = boxClient.getFilesManager().getFile(source.getId(), null);
                    checkFileValidity(boxfile);

                    String boxFolderId = toBoxFolderId(destFolder);
                    BoxFolder boxfolder = boxClient.getFoldersManager().getFolder(boxFolderId, null);

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
                                if (isFile(child) && title.equals(((BoxFile) child).getName())) {
                                    keepOn = true;
                                    title = new StringBuilder(baseName).append(" (").append(count++).append(')').append(ext).toString();
                                    break;
                                }
                            }
                        }
                    }

                    BoxItemCopyRequestObject reqObj = BoxItemCopyRequestObject.copyItemRequestObject(boxFolderId);
                    reqObj.setName(title);
                    BoxFile copiedFile = boxClient.getFilesManager().copyFile(source.getId(), reqObj);

                    return new IDTuple(destFolder, copiedFile.getId());
                } catch (final BoxServerException e) {
                    throw handleHttpResponseError(source.getId(), account.getId(), e);
                }
            }
        });
    }

    @Override
    public IDTuple move(final IDTuple source, final String destFolder, long sequenceNumber, File update, List<File.Field> modifiedFields) throws OXException {
        return perform(new BoxClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                try {
                    BoxClient boxClient = boxAccess.<BoxClient>getClient().client;
                    BoxFile boxfile = boxClient.getFilesManager().getFile(source.getId(), null);
                    checkFileValidity(boxfile);

                    String boxFolderId = toBoxFolderId(destFolder);
                    BoxFolder boxfolder = boxClient.getFoldersManager().getFolder(boxFolderId, null);

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
                                if (isFile(child) && title.equals(((BoxFile) child).getName())) {
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
                    BoxFile movedFile = boxClient.getFilesManager().updateFileInfo(source.getId(), reqObj);

                    return new IDTuple(destFolder, movedFile.getId());
                } catch (final BoxServerException e) {
                    throw handleHttpResponseError(source.getId(), account.getId(), e);
                }
            }
        });
    }

    @Override
    public InputStream getDocument(final String folderId, final String id, final String version) throws OXException {
        return perform(new BoxClosure<InputStream>() {

            @Override
            protected InputStream doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                try {
                    BoxClient boxClient = boxAccess.<BoxClient>getClient().client;

                    BoxFile boxfile = boxClient.getFilesManager().getFile(id, defaultBoxRequest());
                    checkFileValidity(boxfile);

                    BoxDefaultRequestObject versionRequest = new BoxDefaultRequestObject();
                    versionRequest.getRequestExtras().addQueryParam("version", version);
                    return new SizeKnowingInputStream(boxClient.getFilesManager().downloadFile(id, versionRequest), boxfile.getSize().longValue());
                } catch (final BoxServerException e) {
                    throw handleHttpResponseError(id, account.getId(), e);
                }
            }
        });
    }

    @Override
    public InputStream getThumbnailStream(String folderId, final String id, String version) throws OXException {
        return perform(new BoxClosure<InputStream>() {

            @Override
            protected InputStream doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                try {
                    BoxClient boxClient = boxAccess.<BoxClient>getClient().client;

                    BoxImageRequestObject reqObj = BoxImageRequestObject.pagePreviewRequestObject(1, 64, 128, 64, 128);
                    BoxThumbnail thumbnail = boxClient.getFilesManager().getThumbnail(id, THUMBNAIL_EXTENSION, reqObj);

                    return thumbnail.getContent();
                } catch (final BoxServerException e) {
                    throw handleHttpResponseError(id, account.getId(), e);
                }
            }

        });
    }

    @Override
    public IDTuple saveDocument(File file, InputStream data, long sequenceNumber) throws OXException {
        return saveDocument(file, data, sequenceNumber, null);
    }

    @Override
    public IDTuple saveDocument(final File file, final InputStream data, final long sequenceNumber, final List<Field> modifiedFields) throws OXException {

        return perform(new BoxClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                try {
                    BoxClient boxClient = boxAccess.<BoxClient>getClient().client;

                    String id = file.getId();
                    String boxFolderId = toBoxFolderId(file.getFolderId());

                    {
                        PreflightCheckRequestObject reqObj = new PreflightCheckRequestObject(file.getFileName(), boxFolderId, file.getFileSize());
                        ExtendedNonRefreshingBoxClient extendedClient = (ExtendedNonRefreshingBoxClient) boxAccess.getExtendedClient().client;
                        if (isEmpty(id) || !exists(null, id, CURRENT_VERSION)) {
                            extendedClient.getFilesManager().preflightCheck(reqObj);
                        } else {
                            extendedClient.getFilesManager().preflightCheck(id, reqObj);
                        }
                    }
                    BoxFile boxFile = null;
                    if (isEmpty(id) || !exists(null, id, CURRENT_VERSION)) {
                        ThresholdFileHolder sink = null;
                        try {
                            sink = new ThresholdFileHolder();
                            sink.write(data);

                            int count = 0;
                            String name = file.getFileName();
                            String fileName = name;

                            boolean retry = true;
                            while (retry) {
                                try {
                                    BoxFileUploadRequestObject reqObj = BoxFileUploadRequestObject.uploadFileRequestObject(boxFolderId, fileName, sink.getStream());
                                    boxFile = boxClient.getFilesManager().uploadFile(reqObj);
                                    retry = false;
                                } catch (BoxServerException e) {
                                    if (SC_CONFLICT != e.getStatusCode()) {
                                        throw e;
                                    }
                                    fileName = FileStorageUtility.enhance(name, ++count);
                                }
                            }
                        } finally {
                            Streams.close(sink);
                        }
                    } else {
                        BoxFile boxfile = boxClient.getFilesManager().getFile(id, null);
                        checkFileValidity(boxfile);
                        BoxFileUploadRequestObject reqObj = BoxFileUploadRequestObject.uploadFileRequestObject(boxFolderId, id, data);
                        boxFile = boxClient.getFilesManager().uploadNewVersion(id, reqObj);
                    }

                    if (null == boxFile) {
                        IllegalStateException x = new IllegalStateException("box.com upload failed");
                        throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(x, x.getMessage());
                    }

                    BoxFileRequestObject req = new BoxFileRequestObject();
                    req.setDescription(file.getDescription());
                    boxClient.getFilesManager().updateFileInfo(boxFile.getId(), req);

                    return new IDTuple(file.getFolderId(), boxFile.getId());
                } catch (BoxJSONException e) {
                    throw FileStorageExceptionCodes.JSON_ERROR.create(e, e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        });
    }

    @Override
    public void removeDocument(final String folderId, long sequenceNumber) throws OXException {
        perform(new BoxClosure<Void>() {

            @Override
            protected Void doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                BoxClient boxClient = boxAccess.<BoxClient>getClient().client;
                BoxFolder folder = boxClient.getFoldersManager().getFolder(toBoxFolderId(folderId), null);

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
        return perform(new BoxClosure<List<IDTuple>>() {

            @Override
            protected List<IDTuple> doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                BoxClient boxClient = boxAccess.<BoxClient>getClient().client;

                for (IDTuple idTuple : ids) {
                    try {
                        boxClient.getFilesManager().deleteFile(idTuple.getId(), null);
                    } catch (BoxServerException e) {
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
    public void touch(String folderId, String id) throws OXException {
        exists(folderId, id, CURRENT_VERSION);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId) throws OXException {
        return perform(new BoxClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                BoxClient boxClient = boxAccess.<BoxClient>getClient().client;

                BoxFolder boxfolder = boxClient.getFoldersManager().getFolder(toBoxFolderId(folderId), null);
                IBoxFilesManager filesManager = boxClient.getFilesManager();

                List<File> files = new LinkedList<File>();
                BoxCollection itemCollection = boxfolder.getItemCollection();
                if (itemCollection.getTotalCount().intValue() <= itemCollection.getEntries().size()) {
                    for (BoxTypedObject child : itemCollection.getEntries()) {
                        if (isFile(child)) {
                            files.add(new com.openexchange.file.storage.boxcom.BoxFile(folderId, child.getId(), userId, rootFolderId).parseBoxFile(filesManager.getFile(child.getId(), defaultBoxRequest())));
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
                                files.add(new com.openexchange.file.storage.boxcom.BoxFile(folderId, typedObject.getId(), userId, rootFolderId).parseBoxFile(filesManager.getFile(typedObject.getId(), defaultBoxRequest())));
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
        return perform(new BoxClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                BoxClient boxClient = boxAccess.<BoxClient>getClient().client;

                BoxFolder boxfolder = boxClient.getFoldersManager().getFolder(toBoxFolderId(folderId), null);
                IBoxFilesManager filesManager = boxClient.getFilesManager();

                List<File> files = new LinkedList<File>();

                BoxCollection itemCollection = boxfolder.getItemCollection();
                if (itemCollection.getTotalCount().intValue() <= itemCollection.getEntries().size()) {
                    for (BoxTypedObject child : itemCollection.getEntries()) {
                        if (isFile(child)) {
                            files.add(new com.openexchange.file.storage.boxcom.BoxFile(folderId, child.getId(), userId, rootFolderId).parseBoxFile(filesManager.getFile(child.getId(), defaultBoxRequest())));
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
                                files.add(new com.openexchange.file.storage.boxcom.BoxFile(folderId, typedObject.getId(), userId, rootFolderId).parseBoxFile(filesManager.getFile(typedObject.getId(), null)));
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
    public TimedResult<File> getDocuments(final List<IDTuple> ids, List<Field> fields) throws OXException {
        return perform(new BoxClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                BoxClient boxClient = boxAccess.<BoxClient>getClient().client;

                List<File> files = new LinkedList<File>();
                for (IDTuple id : ids) {
                    BoxFile boxfile = boxClient.getFilesManager().getFile(id.getId(), defaultBoxRequest());
                    files.add(new com.openexchange.file.storage.boxcom.BoxFile(id.getFolder(), id.getId(), userId, rootFolderId).parseBoxFile(boxfile));
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
    public SearchIterator<File> search(final String pattern, List<Field> fields, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        return search(pattern, fields, folderId, false, sort, order, start, end);
    }

    @Override
    public SearchIterator<File> search(final String pattern, List<Field> fields, final String folderId, final boolean includeSubfolders, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        return perform(new BoxClosure<SearchIterator<File>>() {

            @Override
            protected SearchIterator<File> doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                BoxClient boxClient = boxAccess.<BoxClient>getClient().client;
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
                        BoxFile boxfile = (BoxFile) typedObject;
                        String parentFolderId = toFileStorageFolderId(boxfile.getParent().getId());
                        if (null != folderId && false == includeSubfolders && false == folderId.equals(parentFolderId)) {
                            continue;
                        }
                        files.add(new com.openexchange.file.storage.boxcom.BoxFile(parentFolderId, boxfile.getId(), userId, rootFolderId).parseBoxFile(boxfile));
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

    /*-
     * Deprecated versions-related methods:
     *

    @Override
    public String[] removeVersion(String folderId, final String id, final String[] versions) throws OXException {
        return perform(new BoxClosure<String[]>() {

            @Override
            protected String[] doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                List<String> undeletable = new ArrayList<String>();
                Logger logger = org.slf4j.LoggerFactory.getLogger(BoxFile.class);
                for (String version : versions) {
                    try {
                        boxAccess.getExtendedBoxClient().getFilesManager().deleteFileVersion(id, version);
                    } catch (BoxServerException e) {
                        undeletable.add(version);
                        logger.warn("Could not delete version: {}", version, e);
                    }
                }
                return undeletable.toArray(new String[undeletable.size()]);
            }

        });
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id) throws OXException {
        return getVersions(folderId, id, null);
    }

    @Override
    public TimedResult<File> getVersions(String folderId, String id, List<Field> fields) throws OXException {
        return getVersions(folderId, id, fields, null, SortDirection.DEFAULT);
    }

    @Override
    public TimedResult<File> getVersions(final String folderId, final String id, List<Field> fields, Field sort, SortDirection order) throws OXException {
        return perform(new BoxClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                List<BoxFileVersion> versions = boxAccess.getBoxClient().getFilesManager().getFileVersions(id, null);
                MimeTypeMap map = Services.getService(MimeTypeMap.class);

                List<File> files = new LinkedList<File>();
                for (BoxFileVersion version : versions) {
                    com.openexchange.file.storage.boxcom.BoxFile file = new com.openexchange.file.storage.boxcom.BoxFile(folderId, id, userId, rootFolderId);
                    file.setTitle(version.getName());
                    file.setFileName(version.getName());
                    file.setFileSize((int) version.getExtraData("size"));
                    file.setFileMD5Sum((String) version.getValue("sha1"));
                    file.setFileMIMEType(map.getContentType(version.getName()));
                    file.setVersion(version.getId());
                    file.setNumberOfVersions(versions.size());
                    try {
                        file.setLastModified(ISO8601DateParser.parse(version.getModifiedAt()));
                    } catch (ParseException e) {
                        Logger logger = org.slf4j.LoggerFactory.getLogger(BoxFile.class);
                        logger.warn("Could not parse date from: {}", version.getModifiedAt(), e);
                    }
                    files.add(file);
                }
                return new FileTimedResult(files);
            }

        });
    }
     *
     */

    @Override
    public void unlock(String folderId, final String id) throws OXException {
        perform(new BoxClosure<Void>() {

            @Override
            protected Void doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                ExtendedNonRefreshingBoxClient extendedClient = (ExtendedNonRefreshingBoxClient) boxAccess.getExtendedClient().client;
                extendedClient.getFilesManager().unlockFile(id);
                return null;
            }

        });
    }

    @Override
    public void lock(String folderId, final String id, long diff) throws OXException {
        perform(new BoxClosure<Void>() {

            @Override
            protected Void doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxRestException, BoxServerException, AuthFatalFailureException, UnsupportedEncodingException {
                ExtendedNonRefreshingBoxClient extendedClient = (ExtendedNonRefreshingBoxClient) boxAccess.getExtendedClient().client;
                extendedClient.getFilesManager().lockFile(id);
                return null;
            }

        });
    }

    /**
     * Create a default request
     *
     * @return The default request
     */
    static BoxDefaultRequestObject defaultBoxRequest() {
        List<String> fields = new ArrayList<String>();
        fields.add(BoxFile.FIELD_MODIFIED_AT);
        fields.add(BoxFile.FIELD_MODIFIED_BY);
        fields.add(BoxFile.FIELD_PARENT);
        fields.add(BoxFile.FIELD_NAME);
        fields.add(BoxFile.FIELD_VERSION_NUMBER);
        //fields.add(BoxFile.FIELD_); content?
        fields.add(BoxFile.FIELD_ID);
        fields.add(BoxFile.FIELD_SIZE);
        fields.add(BoxFile.FIELD_DESCRIPTION);
        fields.add(BoxFile.FIELD_SHARED_LINK);
        fields.add(BoxFile.FIELD_CREATED_BY);
        fields.add(BoxFile.FIELD_NAME);//filename
        fields.add(BoxFile.FIELD_TYPE); //mimetype
        fields.add(BoxFile.FIELD_SEQUENCE_ID);
        //fields.add(BoxFile.FIELD_); category?
        //fields.add(BoxFile.FIELD_LOCK); locked until?
        //fields.add(BoxFile.FIELD_); comment?
        fields.add(BoxFile.FIELD_ETAG); //version?
        //fields.add(BoxFile.FIELD_); color?
        fields.add(BoxFile.FIELD_MODIFIED_AT); //convert to utc
        //fields.add(BoxFile.FIELD_VERSION_NUMBER);
        fields.add(BoxFile.FIELD_LOCK);

        return customRequestObject(fields);
    }

    /**
     * Create a custom request object with the specified fields
     *
     * @param fields The fields to request
     * @return A request object with the specified fields
     */
    static BoxDefaultRequestObject customRequestObject(List<String> fields) {
        BoxDefaultRequestObject customRequestObject = new BoxDefaultRequestObject();
        for (String field : fields) {
            customRequestObject.getRequestExtras().addField(field);
        }
        return customRequestObject;
    }
}
