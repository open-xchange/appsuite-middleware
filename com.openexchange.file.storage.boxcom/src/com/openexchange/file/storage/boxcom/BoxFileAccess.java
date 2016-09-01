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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxFile.Info;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxSearch;
import com.box.sdk.BoxSearchParameters;
import com.box.sdk.PartialCollection;
import com.box.sdk.ProgressListener;
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
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.SizeKnowingInputStream;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(BoxFileAccess.class);

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
            protected Boolean doPerform(BoxOAuthAccess boxAccess) throws BoxAPIException, OXException {
                try {
                    BoxAPIConnection boxClient = boxAccess.<BoxAPIConnection> getClient().client;
                    com.box.sdk.BoxFile file = new com.box.sdk.BoxFile(boxClient, id);
                    checkFileValidity(file.getInfo("trashed_at"));
                    return Boolean.TRUE;
                } catch (final BoxAPIException e) {
                    if (404 == e.getResponseCode()) {
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
            protected File doPerform(BoxOAuthAccess boxAccess) throws BoxAPIException, OXException {
                try {
                    BoxAPIConnection boxClient = boxAccess.<BoxAPIConnection> getClient().client;

                    // Versions are only tracked for Box users with premium accounts- Hence we do not support it (anymore)
                    /* final int versions = boxAccess.getBoxClient().getFilesManager().getFileVersions(id, customRequestObject(Arrays.asList(BoxFile.FIELD_NAME))).size(); */

                    com.box.sdk.BoxFile file = new com.box.sdk.BoxFile(boxClient, id);
                    Info fileInfo = file.getInfo();
                    checkFileValidity(fileInfo);

                    com.openexchange.file.storage.boxcom.BoxFile boxFile = new com.openexchange.file.storage.boxcom.BoxFile(folderId, id, userId, rootFolderId).parseBoxFile(fileInfo);
                    return boxFile;
                } catch (final BoxAPIException e) {
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
                protected IDTuple doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                    try {
                        BoxAPIConnection boxClient = boxAccess.<BoxAPIConnection> getClient().client;
                        com.box.sdk.BoxFile boxFile = new com.box.sdk.BoxFile(boxClient, file.getId());
                        Info fileInfo = boxFile.getInfo();
                        checkFileValidity(fileInfo);

                        fileInfo.setName(file.getFileName());
                        fileInfo.setDescription(file.getDescription());
                        boxFile.updateInfo(fileInfo);

                        return new IDTuple(file.getFolderId(), boxFile.getID());
                    } catch (final BoxAPIException e) {
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
            protected IDTuple doPerform(BoxOAuthAccess boxAccess) throws BoxAPIException, OXException {
                try {
                    BoxAPIConnection boxClient = boxAccess.<BoxAPIConnection> getClient().client;
                    com.box.sdk.BoxFile boxFile = new com.box.sdk.BoxFile(boxClient, source.getId());
                    com.box.sdk.BoxFile.Info fileInfo = boxFile.getInfo();
                    checkFileValidity(fileInfo);

                    String boxFolderId = toBoxFolderId(destFolder);
                    com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(boxClient, boxFolderId);
                    com.box.sdk.BoxFolder.Info folderInfo = boxFolder.getInfo();

                    // Check destination folder
                    String title = fileInfo.getName();
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
                            // TODO: Expensive as it needs to iterate through all the files in a folder... 
                            //       Consider the possibility of checking if the file exists in the folder with the exists method
                            for (com.box.sdk.BoxItem.Info info : boxFolder) {
                                // Check for filename clashes and append a number at the end of the filename
                                if (info instanceof Info && title.equals(info.getName())) {
                                    keepOn = true;
                                    title = new StringBuilder(baseName).append(" (").append(count++).append(')').append(ext).toString();
                                    break;
                                }
                            }
                        }
                    }

                    fileInfo.setName(title);
                    Info copiedFile = boxFile.copy(boxFolder);

                    return new IDTuple(destFolder, copiedFile.getID());
                } catch (final BoxAPIException e) {
                    throw handleHttpResponseError(source.getId(), account.getId(), e);
                }
            }
        });
    }

    @Override
    public IDTuple move(final IDTuple source, final String destFolder, long sequenceNumber, File update, List<File.Field> modifiedFields) throws OXException {
        return perform(new BoxClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                try {
                    BoxAPIConnection apiConnection = getAPIConnection();
                    com.box.sdk.BoxFile boxFile = new com.box.sdk.BoxFile(apiConnection, source.getId());
                    Info fileInfo = boxFile.getInfo();
                    checkFileValidity(fileInfo);

                    String boxFolderId = toBoxFolderId(destFolder);
                    com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(apiConnection, boxFolderId);

                    // Check destination folder
                    String title = fileInfo.getName();
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
                            // TODO: Expensive as it needs to iterate through all the files in a folder... 
                            //       Consider the possibility of checking if the file exists in the folder with the exists method
                            for (com.box.sdk.BoxItem.Info info : boxFolder) {
                                // Check for filename clashes and append a number at the end of the filename
                                if (info instanceof Info && title.equals(info.getName())) {
                                    keepOn = true;
                                    title = new StringBuilder(baseName).append(" (").append(count++).append(')').append(ext).toString();
                                    break;
                                }
                            }
                        }
                    }

                    com.box.sdk.BoxItem.Info movedFile = boxFile.move(boxFolder, title);

                    return new IDTuple(destFolder, movedFile.getID());
                } catch (final BoxAPIException e) {
                    throw handleHttpResponseError(source.getId(), account.getId(), e);
                }
            }
        });
    }

    @Override
    public InputStream getDocument(final String folderId, final String id, final String version) throws OXException {
        return perform(new BoxClosure<InputStream>() {

            @Override
            protected InputStream doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                try {
                    BoxAPIConnection apiConnection = getAPIConnection();
                    com.box.sdk.BoxFile boxFile = new com.box.sdk.BoxFile(apiConnection, id);
                    //TODO: Check if the user has premium features and fetch the asked 'version'. If not fall back to latest version
                    Info fileInfo = boxFile.getInfo("trashed_at", "name", "size");
                    checkFileValidity(fileInfo);

                    //BoxDefaultRequestObject versionRequest = new BoxDefaultRequestObject();
                    //versionRequest.getRequestExtras().addQueryParam("version", version);

                    // FIXME: Memory intensive?
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    boxFile.download(outputStream);
                    outputStream.close();

                    return new SizeKnowingInputStream(new ByteArrayInputStream(outputStream.toByteArray()), fileInfo.getSize());
                } catch (IOException e) {
                    throw FileStorageExceptionCodes.IO_ERROR.create(e, e.getMessage());
                } catch (final BoxAPIException e) {
                    throw handleHttpResponseError(id, account.getId(), e);
                }
            }
        });
    }

    @Override
    public InputStream getThumbnailStream(String folderId, final String id, String version) throws OXException {
        return perform(new BoxClosure<InputStream>() {

            @Override
            protected InputStream doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                try {
                    BoxAPIConnection apiConnection = getAPIConnection();
                    com.box.sdk.BoxFile boxFile = new com.box.sdk.BoxFile(apiConnection, id);
                    byte[] thumbnail = boxFile.getThumbnail(com.box.sdk.BoxFile.ThumbnailFileType.PNG, 64, 64, 128, 128);

                    return new ByteArrayInputStream(thumbnail);
                } catch (final BoxAPIException e) {
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
            protected IDTuple doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                try {
                    //TODO: rework upload logic
                    BoxAPIConnection apiConnection = getAPIConnection();

                    String fileId = file.getId();
                    String boxFolderId = toBoxFolderId(file.getFolderId());

                    // Pre-flight Check
                    com.box.sdk.BoxFile boxFile = new com.box.sdk.BoxFile(apiConnection, file.getId());
                    try {
                        boxFile.canUploadVersion(file.getFileName(), file.getFileSize(), boxFolderId);
                    } catch (BoxAPIException e) {
                        if (e.getResponseCode() == 404) {
                            LOGGER.debug("Pre-flight check: File does not exist.");
                        } else {
                            throw handleHttpResponseError(file.getId(), account.getId(), e);
                        }
                    }

                    // Upload new
                    Info fileInfo = null;
                    if (isEmpty(fileId) || !exists(null, fileId, CURRENT_VERSION)) {
                        ThresholdFileHolder sink = null;
                        try {
                            sink = new ThresholdFileHolder();
                            sink.write(data);

                            int count = 0;
                            String name = file.getFileName();
                            String fileName = name;

                            // TODO: Should we retry a max number of tries or for ever?
                            boolean retry = true;
                            while (retry) {
                                try {
                                    com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(apiConnection, boxFolderId);
                                    fileInfo = boxFolder.uploadFile(sink.getStream(), fileName, sink.getLength(), new UploadProgressListener());
                                    retry = false;
                                } catch (BoxAPIException e) {
                                    if (SC_CONFLICT != e.getResponseCode()) {
                                        throw e;
                                    }
                                    fileName = FileStorageUtility.enhance(name, ++count);
                                } finally {
                                    Streams.close(sink);
                                }
                            }
                        } finally {
                            Streams.close(sink);
                        }
                    } else {
                        boxFile.uploadVersion(data, file.getLastModified(), file.getFileSize(), new UploadProgressListener());
                        fileInfo = boxFile.getInfo();
                        checkFileValidity(fileInfo);
                    }

                    if (null == fileInfo) {
                        IllegalStateException x = new IllegalStateException("box.com upload failed");
                        throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(x, x.getMessage());
                    }

                    if (!Strings.isEmpty(file.getDescription())) {
                        fileInfo.setDescription(file.getDescription());
                        boxFile.updateInfo(fileInfo);
                    }

                    return new IDTuple(file.getFolderId(), fileInfo.getID());
                } catch (BoxAPIException e) {
                    throw handleHttpResponseError(file.getId(), account.getId(), e);
                }
            }
        });
    }

    @Override
    public void removeDocument(final String folderId, long sequenceNumber) throws OXException {
        perform(new BoxClosure<Void>() {

            @Override
            protected Void doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();
                com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(apiConnection, folderId);

                for (com.box.sdk.BoxItem.Info info : boxFolder) {
                    if (info instanceof Info) {
                        Info i = (Info) info;
                        new com.box.sdk.BoxFile(apiConnection, i.getID()).delete();
                    }
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
            protected List<IDTuple> doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();

                for (IDTuple idTuple : ids) {
                    try {
                        com.box.sdk.BoxFile boxFile = new com.box.sdk.BoxFile(apiConnection, idTuple.getId());
                        boxFile.delete();
                    } catch (BoxAPIException e) {
                        if (404 != e.getResponseCode()) {
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
            protected TimedResult<File> doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();
                com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(apiConnection, folderId);

                List<File> files = new LinkedList<File>();
                for (com.box.sdk.BoxItem.Info info : boxFolder) {
                    if (info instanceof Info) {
                        Info i = (Info) info;
                        files.add(new BoxFile(folderId, i.getID(), userId, rootFolderId).parseBoxFile(i));
                    }
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
            protected TimedResult<File> doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();
                com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(apiConnection, toBoxFolderId(folderId));

                List<File> files = new LinkedList<File>();
                for (com.box.sdk.BoxItem.Info info : boxFolder) {
                    if (info instanceof Info) {
                        Info i = (Info) info;
                        files.add(new BoxFile(folderId, i.getID(), userId, rootFolderId).parseBoxFile(i));
                    }
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
            protected TimedResult<File> doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();

                List<File> files = new LinkedList<File>();
                for (IDTuple id : ids) {
                    com.box.sdk.BoxFile boxFile = new com.box.sdk.BoxFile(apiConnection, id.getId());
                    Info info = boxFile.getInfo();
                    files.add(new com.openexchange.file.storage.boxcom.BoxFile(id.getFolder(), id.getId(), userId, rootFolderId).parseBoxFile(info));
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
            protected SearchIterator<File> doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();
                List<File> files = new LinkedList<File>();

                BoxSearchParameters bsp = new BoxSearchParameters(pattern == null ? "*" : pattern);
                bsp.setType("file");
                if (folderId != null) {
                    bsp.setAncestorFolderIds(Collections.singletonList(toBoxFolderId(folderId)));
                }

                // TODO: play with start and end parameters
                final int limit = 30; //Default Box limit
                int s = start;
                int e = end;
                if (s < 0) {
                    s = 0;
                }
                if (e < 0 || e < s) {
                    e = limit;
                }

                BoxSearch boxSearch = new BoxSearch(apiConnection);
                PartialCollection<com.box.sdk.BoxItem.Info> searchRange = boxSearch.searchRange(s, limit, bsp);
                for (BoxItem.Info info : searchRange) {
                    if (info instanceof com.box.sdk.BoxFile.Info) {
                        com.box.sdk.BoxFile.Info i = (Info) info;
                        String parentFolderId = i.getParent().getID();
                        if (null != folderId && false == includeSubfolders && false == folderId.equals(parentFolderId)) {
                            continue;
                        }
                        files.add(new BoxFile(parentFolderId, i.getID(), userId, rootFolderId).parseBoxFile(i));
                    }
                }

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
            protected String[] doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                List<String> undeletable = new ArrayList<String>();
                Logger logger = org.slf4j.LoggerFactory.getLogger(BoxFile.class);
                for (String version : versions) {
                    try {
                        boxAccess.getExtendedBoxClient().getFilesManager().deleteFileVersion(id, version);
                    } catch (BoxAPIException e) {
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
            protected TimedResult<File> doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
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
            protected Void doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();
                com.box.sdk.BoxFile boxFile = new com.box.sdk.BoxFile(apiConnection, id);
                boxFile.unlock();
                return null;
            }
        });
    }

    @Override
    public void lock(String folderId, final String id, long diff) throws OXException {
        perform(new BoxClosure<Void>() {

            @Override
            protected Void doPerform(BoxOAuthAccess boxAccess) throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();
                com.box.sdk.BoxFile boxFile = new com.box.sdk.BoxFile(apiConnection, id);
                boxFile.lock(new Date());
                return null;
            }
        });
    }

    private static final class UploadProgressListener implements ProgressListener {

        /**
         * Initialises a new {@link BoxFileAccess.UploadProgressListener}.
         */
        public UploadProgressListener() {
            super();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.box.sdk.ProgressListener#onProgressChanged(long, long)
         */
        @Override
        public void onProgressChanged(long numBytes, long totalBytes) {
            long l = numBytes / totalBytes;
            LOGGER.debug("Uploading :{}%", l);
        }

    }

    //    /**
    //     * Create a default request
    //     *
    //     * @return The default request
    //     */
    //    static BoxDefaultRequestObject defaultBoxRequest() {
    //        List<String> fields = new ArrayList<String>();
    //        fields.add(BoxFile.FIELD_MODIFIED_AT);
    //        fields.add(BoxFile.FIELD_MODIFIED_BY);
    //        fields.add(BoxFile.FIELD_PARENT);
    //        fields.add(BoxFile.FIELD_NAME);
    //        fields.add(BoxFile.FIELD_VERSION_NUMBER);
    //        //fields.add(BoxFile.FIELD_); content?
    //        fields.add(BoxFile.FIELD_ID);
    //        fields.add(BoxFile.FIELD_SIZE);
    //        fields.add(BoxFile.FIELD_DESCRIPTION);
    //        fields.add(BoxFile.FIELD_SHARED_LINK);
    //        fields.add(BoxFile.FIELD_CREATED_BY);
    //        fields.add(BoxFile.FIELD_NAME);//filename
    //        fields.add(BoxFile.FIELD_TYPE); //mimetype
    //        fields.add(BoxFile.FIELD_SEQUENCE_ID);
    //        //fields.add(BoxFile.FIELD_); category?
    //        //fields.add(BoxFile.FIELD_LOCK); locked until?
    //        //fields.add(BoxFile.FIELD_); comment?
    //        fields.add(BoxFile.FIELD_ETAG); //version?
    //        //fields.add(BoxFile.FIELD_); color?
    //        fields.add(BoxFile.FIELD_MODIFIED_AT); //convert to utc
    //        //fields.add(BoxFile.FIELD_VERSION_NUMBER);
    //        fields.add(BoxFile.FIELD_LOCK);
    //
    //        return customRequestObject(fields);
    //    }
    //
    //    /**
    //     * Create a custom request object with the specified fields
    //     *
    //     * @param fields The fields to request
    //     * @return A request object with the specified fields
    //     */
    //    static BoxDefaultRequestObject customRequestObject(List<String> fields) {
    //        BoxDefaultRequestObject customRequestObject = new BoxDefaultRequestObject();
    //        for (String field : fields) {
    //            customRequestObject.getRequestExtras().addField(field);
    //        }
    //        return customRequestObject;
    //    }
}
