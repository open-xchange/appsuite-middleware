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
import java.util.ArrayList;
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

    static final Logger LOGGER = LoggerFactory.getLogger(BoxFileAccess.class);

    // --------------------------------------------------------------------------------------------

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
            protected Boolean doPerform() throws BoxAPIException, OXException {
                try {
                    BoxAPIConnection boxClient = boxAccess.<BoxAPIConnection> getClient().client;
                    com.box.sdk.BoxFile file = new com.box.sdk.BoxFile(boxClient, id);
                    checkFileValidity(file.getInfo("trashed_at"));
                    return Boolean.TRUE;
                } catch (final BoxAPIException e) {
                    if (SC_NOT_FOUND == e.getResponseCode()) {
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
            protected File doPerform() throws BoxAPIException, OXException {
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
                protected IDTuple doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException {
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
    public IDTuple copy(final IDTuple source, final String version, final String destFolder, File update, InputStream newFil, List<Field> modifiedFields) throws OXException {
        if (version != CURRENT_VERSION) {
            // can only copy the current revision
            throw FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(BoxConstants.ID);
        }

        return perform(new BoxClosure<IDTuple>() {

            @Override
            protected IDTuple doPerform() throws BoxAPIException, OXException {
                try {
                    BoxAPIConnection boxClient = boxAccess.<BoxAPIConnection> getClient().client;
                    com.box.sdk.BoxFile boxFile = new com.box.sdk.BoxFile(boxClient, source.getId());
                    com.box.sdk.BoxFile.Info fileInfo = boxFile.getInfo();
                    checkFileValidity(fileInfo);

                    String boxFolderId = toBoxFolderId(destFolder);
                    com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(boxClient, boxFolderId);

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

                    // Copy the file
                    Info copiedFile = boxFile.copy(boxFolder, title);

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
            protected IDTuple doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException {
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
            protected InputStream doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException {
                try {
                    BoxAPIConnection apiConnection = getAPIConnection();
                    com.box.sdk.BoxFile boxFile = new com.box.sdk.BoxFile(apiConnection, id);
                    Info fileInfo = boxFile.getInfo("trashed_at", "name", "size");
                    checkFileValidity(fileInfo);

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
            protected InputStream doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException {
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
            protected IDTuple doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException {
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
                        if (e.getResponseCode() != SC_NOT_FOUND) {
                            throw handleHttpResponseError(file.getId(), account.getId(), e);
                        }

                        LOGGER.debug("Pre-flight check: File does not exist.");
                    }

                    // Upload new
                    Info fileInfo = null;
                    if (isEmpty(fileId) || !exists(null, fileId, CURRENT_VERSION)) {
                        ThresholdFileHolder sink = null;
                        try {
                            sink = new ThresholdFileHolder();
                            sink.write(data); // Implicitly closes 'data' input stream

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
                                }
                            }
                        } finally {
                            Streams.close(sink);
                        }
                    } else {
                        try {
                            boxFile.uploadVersion(data, file.getLastModified(), file.getFileSize(), new UploadProgressListener());
                            fileInfo = boxFile.getInfo();
                            checkFileValidity(fileInfo);
                        } finally {
                            Streams.close(data);
                        }
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
            protected Void doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException {
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
            protected List<IDTuple> doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();

                for (IDTuple idTuple : ids) {
                    try {
                        com.box.sdk.BoxFile boxFile = new com.box.sdk.BoxFile(apiConnection, idTuple.getId());
                        boxFile.delete();
                    } catch (BoxAPIException e) {
                        if (SC_NOT_FOUND != e.getResponseCode()) {
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
            protected TimedResult<File> doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException {
                return new FileTimedResult(getFiles(folderId, null));
            }
        });
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields) throws OXException {
        return getDocuments(folderId);
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> fields, final Field sort, final SortDirection order) throws OXException {
        return perform(new BoxClosure<TimedResult<File>>() {

            @Override
            protected TimedResult<File> doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException {
                List<File> files = getFiles(folderId, fields);
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
            protected TimedResult<File> doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException {
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
            protected SearchIterator<File> doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException {
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

    @Override
    public void unlock(String folderId, final String id) throws OXException {
        perform(new BoxClosure<Void>() {

            @Override
            protected Void doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException {
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
            protected Void doPerform() throws OXException, BoxAPIException, UnsupportedEncodingException {
                BoxAPIConnection apiConnection = getAPIConnection();
                com.box.sdk.BoxFile boxFile = new com.box.sdk.BoxFile(apiConnection, id);
                boxFile.lock(new Date());
                return null;
            }
        });
    }

    private static final class UploadProgressListener implements ProgressListener {

        /**
         * Initializes a new {@link BoxFileAccess.UploadProgressListener}.
         */
        UploadProgressListener() {
            super();
        }

        @Override
        public void onProgressChanged(long numBytes, long totalBytes) {
            if (totalBytes > 0) {
                LOGGER.debug("Uploaded {} of {} bytes in total", numBytes, totalBytes);
            }
        }
    }

    /**
     * Returns a {@link List} of {@link File}s contained in the specified folder
     * 
     * @param folderId The folder identifier
     * @param fields The optional fields to fetch for each file
     * @return a {@link List} of {@link File}s contained in the specified folder
     * @throws OXException if an error is occurred
     */
    private List<File> getFiles(String folderId, List<Field> fields) throws OXException {
        BoxAPIConnection apiConnection = getAPIConnection();
        com.box.sdk.BoxFolder boxFolder = new com.box.sdk.BoxFolder(apiConnection, toBoxFolderId(folderId));

        String[] bxFields = (fields == null || fields.isEmpty()) ? BoxFileField.getAllFields() : BoxFileField.parseFields(fields);
        List<File> files = new LinkedList<File>();
        Iterable<com.box.sdk.BoxItem.Info> children = boxFolder.getChildren(bxFields);
        for (com.box.sdk.BoxItem.Info info : children) {
            if (info instanceof Info) {
                Info i = (Info) info;
                files.add(new BoxFile(folderId, i.getID(), userId, rootFolderId).parseBoxFile(i));
            }
        }
        return files;
    }

    /**
     * {@link BoxFileField} to {@link Field} mapper
     *
     * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
     */
    private enum BoxFileField {
        //type,
        ID(Field.ID, "id"),
        //file_version,
        SEQUENCE_NUMBER(Field.SEQUENCE_NUMBER, "sequence_id"),
        //etag,
        FILE_MD5SUM(Field.FILE_MD5SUM, "sha1"),
        FILENAME(Field.FILENAME, "name"),
        DESCRIPTION(Field.DESCRIPTION, "description"),
        FILE_SIZE(Field.FILE_SIZE, "size"),
        //path_collection,
        CREATED(Field.CREATED, "created_at"),
        LAST_MODIFIED(Field.LAST_MODIFIED, "modified_at"),
        //trashed_at,
        //purged_at,
        //content_created_at,
        //content_modified_at,
        CREATED_BY(Field.CREATED_BY, "created_by"),
        MODIFIED_BY(Field.MODIFIED_BY, "modified_by"),
        //owned_by,
        URL(Field.URL, "shared_link"),
        FOLDER_ID(Field.FOLDER_ID, "parent"),
        //item_status,
        //VERSION(Field.VERSION, "version_number"),
        //comment_count,
        OBJECT_PERMISSIONS(Field.OBJECT_PERMISSIONS, "permissions"),
        //tags,
        LOCKED_UNTIL(Field.LOCKED_UNTIL, "lock"),
        //extension,
        //is_package,
        //expiring_embeded_link,
        //watermark_info;
        ;

        // Commented out BoxFileFields are not mappable with OX File Fields

        private final Field field;
        private final String boxField;

        private static String[] allFields;

        static {
            BoxFileField[] boxFileFields = BoxFileField.values();
            String[] allFields = new String[boxFileFields.length];
            int index = 0;
            for (BoxFileField bxField : boxFileFields) {
                allFields[index++] = bxField.getBoxField();
            }
        }

        /**
         * Initialises a new {@link BoxFileField}.
         * 
         * @param field The mapped {@link Field}
         */
        private BoxFileField(Field field, String boxField) {
            this.field = field;
            this.boxField = boxField;
        }

        /**
         * Gets the field
         *
         * @return The field
         */
        public Field getField() {
            return field;
        }

        /**
         * Gets the boxField
         *
         * @return The boxField
         */
        public String getBoxField() {
            return boxField;
        }

        /**
         * Return an array of strings of the {@link BoxFileField}s
         * 
         * @return an array of string of the {@link BoxFileField}s
         */
        public static String[] getAllFields() {
            return allFields;
        }

        /**
         * Parses the specified {@link Field}s to {@link BoxFileField}s and returns those as a string array
         * 
         * @param fields The OX File {@link Field}s
         * @return a string array with all parsed {@link BoxFileField}s
         */
        public static String[] parseFields(List<Field> fields) {
            //String[] parsedFields = new String[fields.size()];
            List<String> parsedFields = new ArrayList<String>(fields.size());
            for (Field f : fields) {
                try {
                    BoxFileField bff = BoxFileField.valueOf(f.name());
                    parsedFields.add(bff.boxField);
                } catch (IllegalArgumentException e) {
                    LOGGER.debug("OX File Field '{}' is not mappable to a BoxField. Skipping.", f);
                }
            }
            return parsedFields.toArray(new String[parsedFields.size()]);
        }
    }
}
