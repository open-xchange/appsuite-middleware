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

package com.openexchange.file.storage.composition.internal;

import static com.openexchange.file.storage.composition.internal.FileStorageTools.addIDColumns;
import static com.openexchange.file.storage.composition.internal.FileStorageTools.checkPatternLength;
import static com.openexchange.file.storage.composition.internal.FileStorageTools.ensureFolderIDs;
import static com.openexchange.file.storage.composition.internal.FileStorageTools.extractRemoteAddress;
import static com.openexchange.file.storage.composition.internal.FileStorageTools.getAccountName;
import static com.openexchange.file.storage.composition.internal.FileStorageTools.getPathString;
import static com.openexchange.file.storage.composition.internal.idmangling.IDManglingFileCustomizer.fixIDs;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.service.event.Event;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAdvancedSearchFileAccess;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageETagProvider;
import com.openexchange.file.storage.FileStorageEfficientRetrieval;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.FileStorageEventHelper;
import com.openexchange.file.storage.FileStorageEventHelper.EventProperty;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageIgnorableVersionFileAccess;
import com.openexchange.file.storage.FileStorageLockedFileAccess;
import com.openexchange.file.storage.FileStorageMultiMove;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FileStorageRandomFileAccess;
import com.openexchange.file.storage.FileStorageRangeFileAccess;
import com.openexchange.file.storage.FileStorageSequenceNumberProvider;
import com.openexchange.file.storage.FileStorageVersionedFileAccess;
import com.openexchange.file.storage.ObjectPermissionAware;
import com.openexchange.file.storage.Range;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FileStreamHandler;
import com.openexchange.file.storage.composition.FileStreamHandlerRegistry;
import com.openexchange.file.storage.composition.FilenameValidationUtils;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.Results;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.CallerRunsCompletionService;
import com.openexchange.java.Strings;
import com.openexchange.objectusecount.IncrementArguments;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.iterator.FilteringSearchIterator;
import com.openexchange.tools.iterator.MergingSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorDelegator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tx.TransactionAwares;

/**
 * {@link AbstractCompositingIDBasedFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractCompositingIDBasedFileAccess extends AbstractCompositingIDBasedAccess implements IDBasedFileAccess {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractCompositingIDBasedFileAccess.class);

    /** The empty {@link TimedResult} */
    private static final TimedResult<File> EMPTY_TIMED_RESULT = Results.emptyTimedResult();

    /** The handler registry */
    private static final AtomicReference<FileStreamHandlerRegistry> HANDLER_REGISTRY = new AtomicReference<FileStreamHandlerRegistry>();

    /** The service identifier for InfoStore. */
    protected static final String INFOSTORE_SERVICE_ID = "com.openexchange.infostore";

    /** A comparator for file accesses preferring the infostore */
    private static final Comparator<FileStorageFileAccess> INFOSTORE_FIRST_COMPARATOR = new Comparator<FileStorageFileAccess>() {

        @Override
        public int compare(FileStorageFileAccess fa1, FileStorageFileAccess fa2) {
            String id1 = fa1.getAccountAccess().getService().getId();
            String id2 = fa2.getAccountAccess().getService().getId();

            if (INFOSTORE_SERVICE_ID.equals(id1)) {
                return INFOSTORE_SERVICE_ID.equals(id2) ? 0 : -1;
            }
            if (INFOSTORE_SERVICE_ID.equals(id2)) {
                return INFOSTORE_SERVICE_ID.equals(id1) ? 0 : 1;
            }
            return 0;
        }
    };

    /**
     * Sets the registry reference.
     *
     * @param streamHandlerRegistry The registry or <code>null</code>
     */
    public static void setHandlerRegistry(final FileStreamHandlerRegistry streamHandlerRegistry) {
        HANDLER_REGISTRY.set(streamHandlerRegistry);
    }

    /**
     * Gets the registry reference.
     *
     * @return The registry or <code>null</code>
     */
    public static FileStreamHandlerRegistry getStreamHandlerRegistry() {
        return HANDLER_REGISTRY.get();
    }


    // ------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link AbstractCompositingIDBasedFileAccess}.
     *
     * @param session The associated session
     */
    protected AbstractCompositingIDBasedFileAccess(Session session) {
        super(session);
    }

    @Override
    public boolean supports(String serviceID, String accountID, FileStorageCapability...capabilities) throws OXException {
        if (null == capabilities || 0 == capabilities.length) {
            return true;
        }
        FileStorageFileAccess fileAccess = getFileAccess(serviceID, accountID);
        for (FileStorageCapability capability : capabilities) {
            if (false == FileStorageTools.supports(fileAccess, capability)) {
                return false;
            }
        }
        return true; // all supported
    }

    @Override
    public boolean exists(final String id, final String version) throws OXException {
        final FileID fileID = new FileID(id);
        try {
            return getFileAccess(fileID.getService(), fileID.getAccountId()).exists(fileID.getFolderId(), fileID.getFileId(), version);
        } catch (final OXException e) {
            if (FileStorageExceptionCodes.UNKNOWN_FILE_STORAGE_SERVICE.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public Delta<File> getDelta(final String folderId, final long updateSince, final List<Field> columns, final boolean ignoreDeleted) throws OXException {
        final FolderID folderID = new FolderID(folderId);
        final Delta<File> delta = getFileAccess(folderID.getService(), folderID.getAccountId()).getDelta(
            folderID.getFolderId(),
            updateSince,
            addIDColumns(columns),
            ignoreDeleted);
        return fixIDs(delta, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public Delta<File> getDelta(final String folderId, final long updateSince, final List<Field> columns, final Field sort, final SortDirection order, final boolean ignoreDeleted) throws OXException {
        final FolderID folderID = new FolderID(folderId);
        final Delta<File> delta = getFileAccess(folderID.getService(), folderID.getAccountId()).getDelta(
            folderID.getFolderId(),
            updateSince,
            addIDColumns(columns),
            sort,
            order,
            ignoreDeleted);
        return fixIDs(delta, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public Map<String, Long> getSequenceNumbers(List<String> folderIds) throws OXException {
        if (null == folderIds || 0 == folderIds.size()) {
            return Collections.emptyMap();
        }
        /*
         * determine the file accesses for queried folders
         */
        Map<FileStorageFileAccess, List<String>> foldersPerFileAccess = new HashMap<FileStorageFileAccess, List<String>>();
        for (String folderId : folderIds) {
            FolderID folderID = new FolderID(folderId);
            FileStorageFileAccess fileAccess = getFileAccess(folderID.getService(), folderID.getAccountId());
            List<String> folders = foldersPerFileAccess.get(fileAccess);
            if (null == folders) {
                folders = new ArrayList<String>();
                foldersPerFileAccess.put(fileAccess, folders);
            }
            folders.add(folderID.getFolderId());
        }
        /*
         * get folder sequence numbers from file accesses
         */
        Map<String, Long> sequenceNumbers = new HashMap<String, Long>(folderIds.size());
        for (Entry<FileStorageFileAccess, List<String>> entry : foldersPerFileAccess.entrySet()) {
            FileStorageFileAccess fileAccess = entry.getKey();
            String accountID = fileAccess.getAccountAccess().getAccountId();
            String serviceID = fileAccess.getAccountAccess().getService().getId();
            if (FileStorageTools.supports(fileAccess, FileStorageCapability.SEQUENCE_NUMBERS)) {
                /*
                 * use optimized sequence number access
                 */
                Map<String, Long> fsSequenceNumbers = ((FileStorageSequenceNumberProvider) fileAccess).getSequenceNumbers(entry.getValue());
                if (null != fsSequenceNumbers) {
                    for (Entry<String, Long> fssn : fsSequenceNumbers.entrySet()) {
                        sequenceNumbers.put(new FolderID(serviceID, accountID, fssn.getKey()).toUniqueID(), fssn.getValue());
                    }
                }
            }
        }
        return sequenceNumbers;
    }

    @Override
    public Map<String, String> getETags(List<String> folderIds) throws OXException {
        Map<String, String> eTags = new HashMap<String, String>();
        Map<FileStorageFileAccess, List<String>> foldersByFileAccess = getFoldersByFileAccess(folderIds);
        for (Map.Entry<FileStorageFileAccess, List<String>> entry : foldersByFileAccess.entrySet()) {
            FileStorageFileAccess fileAccess = entry.getKey();
            if (false == FileStorageTools.supports(fileAccess, FileStorageCapability.FOLDER_ETAGS)) {
                throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(fileAccess.getAccountAccess().getService());
            }
            eTags.putAll(((FileStorageETagProvider) fileAccess).getETags(entry.getValue()));
        }
        return eTags;
    }

    @Override
    public Document getDocumentAndMetadata(String id, final String version) throws OXException {
        return getDocumentAndMetadata(id, version, null);
    }

    @Override
    public Document getDocumentAndMetadata(String id, final String version, String clientETag) throws OXException {
        FileID fileID = new FileID(id);
        FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
        if (false == FileStorageEfficientRetrieval.class.isInstance(fileAccess)) {
            return null;
        }
        /*
         * get document & metadata
         */
        FileStorageEfficientRetrieval efficientRetrieval = (FileStorageEfficientRetrieval) fileAccess;
        Document document = null != clientETag ? efficientRetrieval.getDocumentAndMetadata(fileID.getFolderId(), fileID.getFileId(), version, clientETag) :
            efficientRetrieval.getDocumentAndMetadata(fileID.getFolderId(), fileID.getFileId(), version);
        if (null == document) {
            return null;
        }
        /*
         * post "access" event
         */
        postEvent(FileStorageEventHelper.buildAccessEvent(
            session, fileID.getService(), fileID.getAccountId(), fileID.getFolderId(), fileID.toUniqueID(), document.getName(), extractRemoteAddress()));
        /*
         * return handled document
         */
        return handleDocumentStream(fileID, version, fixIDs(document, fileID.getService(), fileID.getAccountId()));
    }

    @Override
    public InputStream getDocument(final String id, final String version) throws OXException {
        /*
         * get data from file access & post "access" event
         */
        FileID fileID = new FileID(id);
        FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
        InputStream data = fileAccess.getDocument(fileID.getFolderId(), fileID.getFileId(), version);
        postEvent(FileStorageEventHelper.buildAccessEvent(
            session, fileID.getService(), fileID.getAccountId(), fileID.getFolderId(), fileID.toUniqueID(), null, extractRemoteAddress()));
        /*
         * return handled stream
         */
        return handleInputStream(fileID, version, data);
    }

    @Override
    public InputStream getDocument(String id, String version, long offset, long length) throws OXException {
        /*
         * check random access capability
         */
        FileID fileID = new FileID(id);
        FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
        if (false == FileStorageRandomFileAccess.class.isInstance(fileAccess)) {
            throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(fileAccess.getAccountAccess().getService());
        }
        /*
         * get data from random file access & post "access" event
         */
        InputStream data = ((FileStorageRandomFileAccess) fileAccess).getDocument(fileID.getFolderId(), fileID.getFileId(), version, offset, length);
        postEvent(FileStorageEventHelper.buildAccessEvent(
            session, fileID.getService(), fileID.getAccountId(), fileID.getFolderId(), fileID.toUniqueID(), null, extractRemoteAddress()));
        /*
         * return handled stream
         */
        return handleInputStream(fileID, version, data);
    }

    @Override
    public InputStream optThumbnailStream(String id, String version) throws OXException {
        final FileID fileID = new FileID(id);
        FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());

        return fileAccess instanceof ThumbnailAware ? ((ThumbnailAware) fileAccess).getThumbnailStream(fileID.getFolderId(), fileID.getFileId(), version) : null;
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId) throws OXException {
        final FolderID folderID = new FolderID(folderId);
        final TimedResult<File> result = getFileAccess(folderID.getService(), folderID.getAccountId()).getDocuments(folderID.getFolderId());
        return fixIDs(result, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> columns) throws OXException {
        FolderID folderID = new FolderID(folderId);
        TimedResult<File> result = getFileAccess(folderID.getService(), folderID.getAccountId()).getDocuments(folderID.getFolderId(), addIDColumns(columns));
        return fixIDs(result, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> columns, final Field sort, final SortDirection order) throws OXException {
        FolderID folderID = new FolderID(folderId);
        String service = folderID.getService();
        String accountId = folderID.getAccountId();

        try {
            TimedResult<File> result = getFileAccess(service, accountId).getDocuments(folderID.getFolderId(), addIDColumns(columns), sort, order);
            return fixIDs(result, service, accountId);
        } catch (final OXException e) {
            if (!FileStorageExceptionCodes.UNKNOWN_FILE_STORAGE_SERVICE.equals(e) || !INFOSTORE_SERVICE_ID.equals(service)) {
                throw e;
            }
            return EMPTY_TIMED_RESULT;
        }
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, List<Field> columns, Field sort, SortDirection order, Range range) throws OXException {
        if (null != range && range.to <= range.from) {
            return EMPTY_TIMED_RESULT;
        }

        FolderID folderID = new FolderID(folderId);
        String service = folderID.getService();
        String accountId = folderID.getAccountId();

        try {
            TimedResult<File> result;

            FileStorageFileAccess fileAccess = getFileAccess(service, accountId);
            if (FileStorageTools.supports(fileAccess, FileStorageCapability.RANGES)) {
                result = ((FileStorageRangeFileAccess) fileAccess).getDocuments(folderID.getFolderId(), addIDColumns(columns), sort, order, range);
            } else {
                result = fileAccess.getDocuments(folderID.getFolderId(), addIDColumns(columns), sort, order);
                result = slice(result, range);
            }

            return fixIDs(result, service, accountId);
        } catch (final OXException e) {
            if (!FileStorageExceptionCodes.UNKNOWN_FILE_STORAGE_SERVICE.equals(e) || !INFOSTORE_SERVICE_ID.equals(service)) {
                throw e;
            }
            return EMPTY_TIMED_RESULT;
        }
    }

    @Override
    public TimedResult<File> getDocuments(final List<String> ids, final List<Field> columns) throws OXException {
        Map<FileStorageFileAccess, List<IDTuple>> getOperations = getFilesPerFileAccesses(ids);
        if (1 == getOperations.size()) {
            /*
             * get files from single storage
             */
            Entry<FileStorageFileAccess, List<IDTuple>> getOp = getOperations.entrySet().iterator().next();
            FileStorageFileAccess access = getOp.getKey();
            return fixIDs(access.getDocuments(getOp.getValue(), columns),
                access.getAccountAccess().getService().getId(), access.getAccountAccess().getAccountId());
        }
        /*
         * get files from multiple storages
         */
        List<File> documents = new ArrayList<File>(ids.size());
        for (Map.Entry<FileStorageFileAccess, List<IDTuple>> getOp : getOperations.entrySet()) {
            FileStorageFileAccess access = getOp.getKey();
            TimedResult<File> results = fixIDs(access.getDocuments(getOp.getValue(), columns),
                access.getAccountAccess().getService().getId(), access.getAccountAccess().getAccountId());
            documents.addAll(SearchIteratorAdapter.toList(results.results()));
        }
        /*
         * re-order according to requested order, extract sequence number
         */
        List<File> orderedDocuments = new ArrayList<File>(documents.size());
        long sequenceNumber = 0;
        for (String id : ids) {
            File file = find(documents, id);
            if (null != file) {
                orderedDocuments.add(file);
                if (file.getSequenceNumber() > sequenceNumber) {
                    sequenceNumber = file.getSequenceNumber();
                }
            } else {
                FileID fileID = new FileID(id);
                throw FileStorageExceptionCodes.FILE_NOT_FOUND.create(fileID.getFileId(), fileID.getFolderId());
            }
        }
        /*
         * wrap result in search iterator
         */
        final long finalSequenceNumber = sequenceNumber;
        final SearchIterator<File> searchIterator = new SearchIteratorAdapter<File>(orderedDocuments.iterator(), orderedDocuments.size());
        return new TimedResult<File>() {

            @Override
            public long sequenceNumber() throws OXException {
                return finalSequenceNumber;
            }

            @Override
            public SearchIterator<File> results() throws OXException {
                return searchIterator;
            }
        };
    }

    @Override
    public SearchIterator<File> getUserSharedDocuments(List<Field> fields, Field sort, SortDirection order) throws OXException {
        List<SearchIterator<File>> searchIterators = new ArrayList<SearchIterator<File>>();
        List<FileStorageFileAccess> fileStorageAccesses = getAllFileStorageAccesses();
        for (FileStorageFileAccess fileAccess : fileStorageAccesses) {
            if (ObjectPermissionAware.class.isInstance(fileAccess)) {
                SearchIterator<File> searchIterator = ((ObjectPermissionAware) fileAccess).getUserSharedDocuments(fields, sort, order);
                FileStorageAccountAccess accountAccess = fileAccess.getAccountAccess();
                searchIterators.add(fixIDs(searchIterator, accountAccess.getService().getId(), accountAccess.getAccountId()));
            }
        }
        return new MergingSearchIterator<File>(order.comparatorBy(sort), order.equals(SortDirection.ASC), searchIterators);
    }

    @Override
    public File getFileMetadata(final String id, final String version) throws OXException {
        final FileID fileID = new FileID(id);
        FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
        File metadata = fileAccess.getFileMetadata(fileID.getFolderId(), fileID.getFileId(), version);
        return fixIDs(metadata, fileID.getService(), fileID.getAccountId());
    }

    @Override
    public TimedResult<File> getVersions(final String id) throws OXException {
        FileID fileID = new FileID(id);
        FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
        TimedResult<File> result;
        if (false == FileStorageTools.supports(fileAccess, FileStorageCapability.FILE_VERSIONS)) {
            result = fileAccess.getDocuments(Collections.singletonList(new IDTuple(fileID.getFolderId(), fileID.getFileId())), null);
        } else {
            result = ((FileStorageVersionedFileAccess) fileAccess).getVersions(fileID.getFolderId(), fileID.getFileId());
        }
        return fixIDs(result, fileID.getService(), fileID.getAccountId());
    }

    @Override
    public TimedResult<File> getVersions(final String id, final List<Field> columns) throws OXException {
        FileID fileID = new FileID(id);
        FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
        List<Field> fields = addIDColumns(columns);
        TimedResult<File> result;
        if (false == FileStorageTools.supports(fileAccess, FileStorageCapability.FILE_VERSIONS)) {
            result = fileAccess.getDocuments(Collections.singletonList(new IDTuple(fileID.getFolderId(), fileID.getFileId())), fields);
        } else {
            result = ((FileStorageVersionedFileAccess) fileAccess).getVersions(fileID.getFolderId(), fileID.getFileId(), fields);
        }
        return fixIDs(result, fileID.getService(), fileID.getAccountId());
    }

    @Override
    public TimedResult<File> getVersions(final String id, final List<Field> columns, final Field sort, final SortDirection order) throws OXException {
        FileID fileID = new FileID(id);
        FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
        List<Field> fields = addIDColumns(columns);
        TimedResult<File> result;
        if (false == FileStorageTools.supports(fileAccess, FileStorageCapability.FILE_VERSIONS)) {
            result = fileAccess.getDocuments(Collections.singletonList(new IDTuple(fileID.getFolderId(), fileID.getFileId())), fields);
        } else {
            result = ((FileStorageVersionedFileAccess) fileAccess).getVersions(fileID.getFolderId(), fileID.getFileId(), fields, sort, order);
        }
        return fixIDs(result, fileID.getService(), fileID.getAccountId());
    }

    @Override
    public void lock(final String id, final long diff) throws OXException {
        final FileID fileID = new FileID(id);
        final FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
        if (FileStorageTools.supports(fileAccess, FileStorageCapability.LOCKS)) {
            new TransactionAwareFileAccessDelegation<Void>() {

                @Override
                protected Void callInTransaction(FileStorageFileAccess access) throws OXException {
                    ((FileStorageLockedFileAccess) fileAccess).lock(fileID.getFolderId(), fileID.getFileId(), diff);
                    return null;
                }
            }.call(fileAccess);
        }
    }

    @Override
    public void unlock(final String id) throws OXException {
        final FileID fileID = new FileID(id);
        final FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
        if (FileStorageTools.supports(fileAccess, FileStorageCapability.LOCKS)) {
            new TransactionAwareFileAccessDelegation<Void>() {

                @Override
                protected Void callInTransaction(FileStorageFileAccess access) throws OXException {
                    ((FileStorageLockedFileAccess) fileAccess).unlock(fileID.getFolderId(), fileID.getFileId());
                    return null;
                }
            }.call(fileAccess);
        }
    }

    @Override
    public void removeDocument(final String folderId, final long sequenceNumber) throws OXException {
        final FolderID id = new FolderID(folderId);
        final TransactionAwareFileAccessDelegation<Void> removeDocumentDelegation = new TransactionAwareFileAccessDelegation<Void>() {

            @Override
            protected Void callInTransaction(FileStorageFileAccess access) throws OXException {
                access.removeDocument(id.getFolderId(), sequenceNumber);
                return null;
            }
        };
        removeDocumentDelegation.call(getFileAccess(id.getService(), id.getAccountId()));
        // TODO: Does this method really make sense? Skipping possible delete event.
    }

    @Override
    public List<String> removeDocument(final List<String> ids, final long sequenceNumber) throws OXException {
        return removeDocument(ids, sequenceNumber, false);
    }

    @Override
    public List<String> removeDocument(final List<String> ids, final long sequenceNumber, final boolean hardDelete) throws OXException {
        /*
         * delete files per storage
         */
        Map<FileStorageFileAccess, List<IDTuple>> deleteOperations = getFilesPerFileAccesses(ids);
        List<String> notDeleted = new ArrayList<String>(ids.size());
        for (Map.Entry<FileStorageFileAccess, List<IDTuple>> deleteOp : deleteOperations.entrySet()) {
            FileStorageFileAccess access = deleteOp.getKey();
            final List<IDTuple> toDelete = ensureFolderIDs(access, deleteOp.getValue());
            /*
             * perform delete, collect any conflicting files
             */
            List<IDTuple> conflicted = new TransactionAwareFileAccessDelegation<List<IDTuple>>() {

                @Override
                protected List<IDTuple> callInTransaction(FileStorageFileAccess access) throws OXException {
                    List<IDTuple> conflicted = access.removeDocument(toDelete, sequenceNumber, hardDelete);
                    List<IDTuple> deleted = new ArrayList<FileStorageFileAccess.IDTuple>(toDelete);
                    deleted.removeAll(conflicted);
                    return conflicted;
                }
            }.call(access);
            String serviceId = access.getAccountAccess().getService().getId();
            String accountId = access.getAccountAccess().getAccountId();
            for (IDTuple tuple : conflicted) {
                notDeleted.add(new FileID(serviceId, accountId, tuple.getFolder(), tuple.getId()).toUniqueID());
            }
            /*
             * Send event
             */
            for (IDTuple tuple : toDelete) {
                String folderId = new FolderID(serviceId, accountId, tuple.getFolder()).toUniqueID();
                String fileId = new FileID(serviceId, accountId, tuple.getFolder(), tuple.getId()).toUniqueID();
                EventProperty hardDeleteProperty = new EventProperty(FileStorageEventConstants.HARD_DELETE, Boolean.valueOf(hardDelete));
                EventProperty shareCleanupDoneProperty = new EventProperty(FileStorageEventConstants.SHARE_CLEANUP_DONE, Boolean.TRUE);
                postEvent(FileStorageEventHelper.buildDeleteEvent(
                    session, serviceId, accountId, folderId, fileId, null, null, hardDeleteProperty, shareCleanupDoneProperty));
            }
        }
        return notDeleted;
    }

    @Override
    public String[] removeVersion(final String id, final String[] versions) throws OXException {
        final FileID fileID = new FileID(id);
        String serviceId = fileID.getService();
        String accountId = fileID.getAccountId();
        FileStorageFileAccess access = getFileAccess(serviceId, accountId);
        String[] notRemoved;
        if (FileStorageTools.supports(access, FileStorageCapability.FILE_VERSIONS)) {
            notRemoved = new TransactionAwareFileAccessDelegation<String[]>() {

                @Override
                protected String[] callInTransaction(FileStorageFileAccess access) throws OXException {
                    return ((FileStorageVersionedFileAccess) access).removeVersion(fileID.getFolderId(), fileID.getFileId(), versions);
                }
            }.call(access);
        } else if (1 == versions.length && FileStorageFileAccess.CURRENT_VERSION == versions[0]) {
            List<IDTuple> result = access.removeDocument(Collections.singletonList(
                new IDTuple(fileID.getFolderId(), fileID.getFileId())), FileStorageFileAccess.DISTANT_FUTURE);
            notRemoved = 0 < result.size() ? versions : new String[0];
        } else {
            notRemoved = versions;
        }

        Set<String> removed = new HashSet<String>(versions.length);
        for (String i : versions) {
            removed.add(i);
        }
        for (String i : notRemoved) {
            removed.remove(i);
        }
        /*
         * prepare event if needed
         */
        if (0 < removed.size()) {
            String objectId = fileID.getFileId();
            FolderID folderID;
            String fileFolder = fileID.getFolderId();
            String fileName = null;
            if (fileFolder == null) {
                /*
                 * Reload the document to get it's folder id.
                 */
                File fileMetadata = access.getFileMetadata(fileFolder, objectId, FileStorageFileAccess.CURRENT_VERSION);
                fileName = fileMetadata.getFileName();
                folderID = new FolderID(serviceId, accountId, fileMetadata.getFolderId());
            } else {
                folderID = new FolderID(serviceId, accountId, fileFolder);
            }

            postEvent(FileStorageEventHelper.buildDeleteEvent(
                session,
                serviceId,
                accountId,
                folderID.toUniqueID(),
                fileID.toUniqueID(),
                fileName,
                removed));
        }

        return notRemoved;
    }

    private static interface FileAccessDelegation<V> {

        /**
         * Invokes this delegation.
         *
         * @param access The file access
         * @return The resulting object
         * @throws OXException If operation fails
         */
        public V call(FileStorageFileAccess access) throws OXException;

    }

    private static abstract class TransactionAwareFileAccessDelegation<V> implements FileAccessDelegation<V> {

        public TransactionAwareFileAccessDelegation() {
            super();
        }

        @Override
        public final V call(final FileStorageFileAccess access) throws OXException {
            boolean rollback = false;
            try {
                // Start transaction
                access.startTransaction();
                rollback = true;
                // Invoke
                final V retval = callInTransaction(access);
                // Commit
                access.commit();
                rollback = false;
                // Return result
                return retval;
            } finally {
                if (rollback) {
                    TransactionAwares.rollbackSafe(access);
                }
                TransactionAwares.finishSafe(access);
            }
        }

        /**
         * Invokes this delegation while it is in transaction state
         *
         * @param access The file access
         * @return The resulting object
         * @throws OXException If operation fails
         */
        protected abstract V callInTransaction(FileStorageFileAccess access) throws OXException;

    }

    protected String save(final File document, final InputStream data, final long sequenceNumber, final List<Field> modifiedColumns, final FileAccessDelegation<SaveResult> saveDelegation) throws OXException {
        return save(document, data, sequenceNumber, modifiedColumns, false, false, saveDelegation);
    }

    private static final class SaveResult {

        private IDTuple idTuple;
        private List<FileStorageObjectPermission> addedPermissions;

        SaveResult() {
            super();
        }

        public IDTuple getIDTuple() {
            return idTuple;
        }

        public List<FileStorageObjectPermission> getAddedPermissions() {
            return addedPermissions;
        }

        public void setIDTuple(IDTuple idTuple) {
            this.idTuple = idTuple;
        }

        public void setAddedPermissions(List<FileStorageObjectPermission> addedPermissions) {
            this.addedPermissions = addedPermissions;
        }

    }

    protected String save(final File document, final InputStream data, final long sequenceNumber, final List<Field> modifiedColumns, boolean ignoreWarnings, boolean tryAddVersion, final FileAccessDelegation<SaveResult> saveDelegation) throws OXException {

        if (Strings.isNotEmpty(document.getFileName())) {
            FilenameValidationUtils.checkCharacters(document.getFileName());
            FilenameValidationUtils.checkName(document.getFileName());
        }

        if (FileStorageFileAccess.NEW == document.getId()) {
            /*
             * create new file, determine target file storage
             */
            if (null == document.getFolderId()) {
                throw FileStorageExceptionCodes.INVALID_FOLDER_IDENTIFIER.create("null");
            }
            FolderID targetFolderID = new FolderID(document.getFolderId());
            String serviceID = targetFolderID.getService();
            String accountID = targetFolderID.getAccountId();
            String sourceFolderId = document.getFolderId();
            document.setFolderId(targetFolderID.getFolderId());
            FileStorageFileAccess fileAccess = getFileAccess(serviceID, accountID);
            /*
             * collect warnings prior saving & abort the operation if not ignored
             */
            final List<OXException> warnings = collectWarningsBeforeSave(document, fileAccess, modifiedColumns);
            if (0 < warnings.size()) {
                addWarnings(warnings);
                if (false == ignoreWarnings) {
                    return null;
                }
            }
            if (tryAddVersion) {
                if (FileStorageCapabilityTools.supports(fileAccess, FileStorageCapability.FILE_VERSIONS)) {
                    SearchIterator<File> it = fileAccess.search(document.getFileName(), Arrays.asList(Field.FOLDER_ID, Field.ID), document.getFolderId(), null, null, FileStorageFileAccess.NOT_SET, FileStorageFileAccess.NOT_SET);
                    if (it.hasNext()) {
                        File existing = it.next();
                        final File metadata = fileAccess.getFileMetadata(existing.getFolderId(), existing.getId(), FileStorageFileAccess.CURRENT_VERSION);
                        metadata.setFolderId(sourceFolderId);
                        modifiedColumns.add(Field.ID);

                        return save(metadata, data, 2116800000000L, modifiedColumns, ignoreWarnings, tryAddVersion, new TransactionAwareFileAccessDelegation<SaveResult>() {

                            @Override
                            protected SaveResult callInTransaction(final FileStorageFileAccess access) throws OXException {
                                ComparedObjectPermissions comparedPermissions = ShareHelper.processGuestPermissions(session, access, metadata, modifiedColumns);
                                IDTuple result;
                                /*
                                 * perform normal save operation
                                 */
                                Map<String, Object> meta = metadata.getMeta();
                                meta.put("save_action", "new_version");
                                metadata.setMeta(meta);
                                result = access.saveDocument(metadata, data, 2116800000000L, modifiedColumns);
                                metadata.setFolderId(result.getFolder());
                                metadata.setId(result.getId());
                                IDTuple idTuple = ShareHelper.applyGuestPermissions(session, access, metadata, comparedPermissions);
                                SaveResult saveResult = new SaveResult();
                                saveResult.setIDTuple(idTuple);
                                saveResult.setAddedPermissions(ShareHelper.collectAddedObjectPermissions(comparedPermissions, session));
                                return saveResult;
                            }
                        });
                    }
                } else {
                    warnings.add(FileStorageExceptionCodes.VERSIONING_NOT_SUPPORTED.create(serviceID));
                }
            }

            /*
             * perform save operation & return resulting file identifier
             */
            SaveResult result = saveDelegation.call(fileAccess);
            IDTuple idTuple = result.getIDTuple();
            FileID newID = new FileID(serviceID, accountID, idTuple.getFolder(), idTuple.getId());
            FolderID newFolderID = new FolderID(serviceID, accountID, idTuple.getFolder());
            document.setId(newID.toUniqueID());
            document.setFolderId(newFolderID.toUniqueID());
            postEvent(FileStorageEventHelper.buildCreateEvent(
                session, serviceID, accountID, newFolderID.toUniqueID(), newID.toUniqueID(), document.getFileName()));
            return newID.toUniqueID();
        }
        /*
         * Update existing file
         */
        FileID sourceFileID = new FileID(document.getId());
        if (null == sourceFileID.getFolderId()) {
            // preserve folder information also for infostore items
            sourceFileID.setFolderId(document.getFolderId());
        }
        String serviceID = sourceFileID.getService();
        String accountID = sourceFileID.getAccountId();
        final FolderID targetFolderID;
        if (null == document.getFolderId()) {
            targetFolderID = new FolderID(serviceID, accountID, sourceFileID.getFolderId());
        } else {
            targetFolderID = new FolderID(document.getFolderId());
            if (false == serviceID.equals(targetFolderID.getService()) || false == accountID.equals(targetFolderID.getAccountId())) {
                /*
                 * Special handling for move between storages
                 */
                return move(document, data, sequenceNumber, modifiedColumns, ignoreWarnings);
            }
        }
        /*
         * update within file storage boundaries, collect warnings prior saving & abort the operation if not ignored
         */
        FileStorageFileAccess fileAccess = getFileAccess(serviceID, accountID);
        List<OXException> warnings = collectWarningsBeforeSave(document, fileAccess, modifiedColumns);
        if (0 < warnings.size()) {
            addWarnings(warnings);
            if (false == ignoreWarnings) {
                return null;
            }
        }
        final IDTuple sourceIDTuple = new IDTuple(sourceFileID.getFolderId(), sourceFileID.getFileId());
        ensureFolderIDs(fileAccess, Collections.singletonList(sourceIDTuple));
        if (null != document.getFolderId() && false == sourceIDTuple.getFolder().equals(targetFolderID.getFolderId())) {
            /*
             * Special handling for move to different folder
             */
            FolderID sourceFolderID = new FolderID(sourceFileID.getService(), sourceFileID.getAccountId(), sourceIDTuple.getFolder());
            document.setFolderId(sourceIDTuple.getFolder());
            document.setId(sourceIDTuple.getId());
            IDTuple result = new TransactionAwareFileAccessDelegation<IDTuple>() {

                @Override
                protected IDTuple callInTransaction(FileStorageFileAccess access) throws OXException {
                    return access.move(sourceIDTuple, targetFolderID.getFolderId(), sequenceNumber, document, modifiedColumns);
                }
            }.call(fileAccess);

            FileID newFileID = new FileID(targetFolderID.getService(), targetFolderID.getAccountId(), result.getFolder(), result.getId());
            FolderID newFolderID = new FolderID(targetFolderID.getService(), targetFolderID.getAccountId(), result.getFolder());
            postEvent(FileStorageEventHelper.buildDeleteEvent(
                session,
                sourceFileID.getService(),
                sourceFileID.getAccountId(),
                sourceFolderID.toUniqueID(),
                sourceFileID.toUniqueID(),
                document.getFileName(),
                null));
            postEvent(FileStorageEventHelper.buildCreateEvent(
                session,
                newFileID.getService(),
                newFileID.getAccountId(),
                newFolderID.toUniqueID(),
                newFileID.toUniqueID(),
                document.getFileName()));
            return newFileID.toUniqueID();
        }

        /*
         * Update without move
         */
        document.setFolderId(targetFolderID.getFolderId());
        document.setId(sourceFileID.getFileId());
        SaveResult result = saveDelegation.call(getFileAccess(serviceID, accountID));
        IDTuple idTuple = result.getIDTuple();
        FileID newFileID = new FileID(serviceID, accountID, idTuple.getFolder(), idTuple.getId());
        FolderID newFolderID = new FolderID(serviceID, accountID, idTuple.getFolder());
        DefaultFile file = new DefaultFile(document);
        file.setId(newFileID.toUniqueID());
        file.setFolderId(newFolderID.toUniqueID());
        postEvent(FileStorageEventHelper.buildUpdateEvent(
            session, serviceID, accountID, newFolderID.toUniqueID(), newFileID.toUniqueID(), file.getFileName()));
        return newFileID.toUniqueID();
    }


    /**
     * Moves a file to a folder located in a different file storage.
     *
     * @param document The document to move
     * @param data The document payload
     * @param sequenceNumber The sequence number as supplied by the client
     * @param modifiedColumns A list of modified columns to apply for the target file
     * @param ignoreWarnings <code>true</code> to force the file move even if warnings regarding potential data loss are detected, <code>false</code>, otherwise
     * @return The new identifier of the moved file
     */
    protected String move(final File document, InputStream data, final long sequenceNumber, final List<Field> modifiedColumns, boolean ignoreWarnings) throws OXException {
        final FileID id = new FileID(document.getId()); // signifies the source
        final FolderID folderId = new FolderID(document.getFolderId()); // signifies the destination

        final FileStorageFileAccess destAccess = getFileAccess(folderId.getService(), folderId.getAccountId());
        final FileStorageFileAccess sourceAccess = getFileAccess(id.getService(), id.getAccountId());
        File sourceFile = sourceAccess.getFileMetadata(id.getFolderId(), id.getFileId(), FileStorageFileAccess.CURRENT_VERSION);

        document.setId(FileStorageFileAccess.NEW);
        document.setFolderId(folderId.getFolderId());
        document.setObjectPermissions(null);

        if (null == data) {
            /*
             * full move, check for potential data loss
             */
            List<OXException> warnings = collectWarningsBeforeMove(id, sourceFile, folderId, modifiedColumns);
            if (0 < warnings.size()) {
                addWarnings(warnings);
                if (false == ignoreWarnings) {
                    return null;
                }
            }
            /*
             * copy data from source document
             */
            data = sourceAccess.getDocument(id.getFolderId(), id.getFileId(), FileStorageFileAccess.CURRENT_VERSION);
        }
        /*
         * take over metadata selectively
         */
        if (null != modifiedColumns && 0 < modifiedColumns.size()) {
            Set<Field> fieldsToSkip = new HashSet<Field>(modifiedColumns);
            fieldsToSkip.add(Field.FOLDER_ID);
            fieldsToSkip.add(Field.ID);
            fieldsToSkip.add(Field.LAST_MODIFIED);
            fieldsToSkip.add(Field.CREATED);
            fieldsToSkip.add(Field.VERSION);
            fieldsToSkip.add(Field.VERSION_COMMENT);
            Set<Field> toCopy = EnumSet.complementOf(EnumSet.copyOf(fieldsToSkip));
            document.copyFrom(sourceFile, toCopy.toArray(new File.Field[toCopy.size()]));
        }

        IDTuple result;
        {
            final InputStream in = data;
            final TransactionAwareFileAccessDelegation<IDTuple> saveDocumentDelegation = new TransactionAwareFileAccessDelegation<IDTuple>() {

                @Override
                protected IDTuple callInTransaction(final FileStorageFileAccess access) throws OXException {
                    return access.saveDocument(document, in, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
                }

            };
            result = saveDocumentDelegation.call(destAccess);
        }

        FileID newFileID = new FileID(folderId.getService(), folderId.getAccountId(), result.getFolder(), result.getId());
        FolderID newFolderID = new FolderID(folderId.getService(), folderId.getAccountId(), result.getFolder());
        document.setId(newFileID.toUniqueID());
        document.setFolderId(newFolderID.toUniqueID());

        {
            final TransactionAwareFileAccessDelegation<Void> removeDocumentDelegation = new TransactionAwareFileAccessDelegation<Void>() {

                @Override
                protected Void callInTransaction(FileStorageFileAccess access) throws OXException {
                    List<IDTuple> ids = Collections.singletonList(new IDTuple(id.getFolderId(), id.getFileId()));
                    access.removeDocument(ids, sequenceNumber, true);
                    return null;
                }
            };
            removeDocumentDelegation.call(sourceAccess);
        }

        Event deleteEvent = FileStorageEventHelper.buildDeleteEvent(
            session,
            id.getService(),
            id.getAccountId(),
            new FolderID(id.getService(), id.getAccountId(), id.getFolderId()).toUniqueID(),
            id.toUniqueID(),
            document.getFileName(),
            null);
        Event createEvent = FileStorageEventHelper.buildCreateEvent(
            session,
            newFileID.getService(),
            newFileID.getAccountId(),
            newFolderID.toUniqueID(),
            newFileID.toUniqueID(),
            document.getFileName());
        postEvent(deleteEvent);
        postEvent(createEvent);
        return newFileID.toUniqueID();
    }

    @Override
    public String saveDocument(final File document, final InputStream data, final long sequenceNumber) throws OXException {
        return save(document, data, sequenceNumber, null, new TransactionAwareFileAccessDelegation<SaveResult>() {

            @Override
            protected SaveResult callInTransaction(final FileStorageFileAccess access) throws OXException {
                ComparedObjectPermissions comparedPermissions = ShareHelper.processGuestPermissions(session, access, document, null);
                IDTuple result = access.saveDocument(document, data, sequenceNumber);
                document.setFolderId(result.getFolder());
                document.setId(result.getId());
                IDTuple idTuple = ShareHelper.applyGuestPermissions(session, access, document, comparedPermissions);
                SaveResult saveResult = new SaveResult();
                saveResult.setIDTuple(idTuple);
                saveResult.setAddedPermissions(ShareHelper.collectAddedObjectPermissions(comparedPermissions, session));
                return saveResult;
            }
        });
    }

    @Override
    public String saveDocument(File document, InputStream data, long sequenceNumber, List<Field> modifiedColumns) throws OXException {
        return saveDocument(document, data, sequenceNumber, modifiedColumns, false);
    }

    @Override
    public String saveDocument(File document, InputStream data, long sequenceNumber, List<Field> modifiedColumns, boolean ignoreVersion) throws OXException {
        return saveDocument(document, data, sequenceNumber, modifiedColumns, ignoreVersion, false, false);
    }

    @Override
    public String saveDocument(final File document, final InputStream data, final long sequenceNumber, final List<Field> modifiedColumns, final boolean ignoreVersion, boolean ignoreWarnings, boolean tryAddVersion) throws OXException {
        return save(document, data, sequenceNumber, modifiedColumns, ignoreWarnings, tryAddVersion, new TransactionAwareFileAccessDelegation<SaveResult>() {

            @Override
            protected SaveResult callInTransaction(final FileStorageFileAccess access) throws OXException {
                ComparedObjectPermissions comparedPermissions = ShareHelper.processGuestPermissions(session, access, document, modifiedColumns);
                IDTuple result;
                if (ignoreVersion && FileStorageFileAccess.NEW != document.getId() &&
                    FileStorageTools.supports(access, FileStorageCapability.IGNORABLE_VERSION)) {
                    /*
                     * save and don't increment the version number
                     */
                    result = ((FileStorageIgnorableVersionFileAccess) access).saveDocument(
                        document, data, sequenceNumber, modifiedColumns, ignoreVersion);
                } else {
                    /*
                     * perform normal save operation
                     */
                    result = access.saveDocument(document, data, sequenceNumber, modifiedColumns);
                }
                document.setFolderId(result.getFolder());
                document.setId(result.getId());
                IDTuple idTuple = ShareHelper.applyGuestPermissions(session, access, document, comparedPermissions);
                SaveResult saveResult = new SaveResult();
                saveResult.setIDTuple(idTuple);
                saveResult.setAddedPermissions(ShareHelper.collectAddedObjectPermissions(comparedPermissions, session));
                return saveResult;
            }
        });
    }

    @Override
    public String saveDocument(final File document, final InputStream data, final long sequenceNumber, final List<Field> modifiedColumns, final long offset) throws OXException {
        return save(document, data, sequenceNumber, modifiedColumns, new TransactionAwareFileAccessDelegation<SaveResult>() {

            @Override
            protected SaveResult callInTransaction(FileStorageFileAccess access) throws OXException {
                if (false == FileStorageTools.supports(access, FileStorageCapability.RANDOM_FILE_ACCESS)) {
                    throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(access.getAccountAccess().getService());
                }
                ComparedObjectPermissions comparedPermissions = ShareHelper.processGuestPermissions(session, access, document, modifiedColumns);
                IDTuple result = ((FileStorageRandomFileAccess) access).saveDocument(document, data, sequenceNumber, modifiedColumns, offset);
                document.setFolderId(result.getFolder());
                document.setId(result.getId());
                IDTuple idTuple = ShareHelper.applyGuestPermissions(session, access, document, comparedPermissions);
                SaveResult saveResult = new SaveResult();
                saveResult.setIDTuple(idTuple);
                saveResult.setAddedPermissions(ShareHelper.collectAddedObjectPermissions(comparedPermissions, session));
                return saveResult;
            }
        });
    }

    @Override
    public String saveFileMetadata(final File document, final long sequenceNumber) throws OXException {
        return save(document, null, sequenceNumber, null, new TransactionAwareFileAccessDelegation<SaveResult>() {

            @Override
            protected SaveResult callInTransaction(final FileStorageFileAccess access) throws OXException {
                ComparedObjectPermissions comparedPermissions = ShareHelper.processGuestPermissions(session, access, document, null);
                IDTuple result = access.saveFileMetadata(document, sequenceNumber);
                document.setFolderId(result.getFolder());
                document.setId(result.getId());
                IDTuple idTuple = ShareHelper.applyGuestPermissions(session, access, document, comparedPermissions);
                SaveResult saveResult = new SaveResult();
                saveResult.setIDTuple(idTuple);
                saveResult.setAddedPermissions(ShareHelper.collectAddedObjectPermissions(comparedPermissions, session));
                return saveResult;
            }
        });
    }

    @Override
    public String saveFileMetadata(final File document, final long sequenceNumber, final List<Field> modifiedColumns) throws OXException {
        return saveFileMetadata(document, sequenceNumber, modifiedColumns, false, false);
    }

    @Override
    public String saveFileMetadata(final File document, final long sequenceNumber, final List<Field> modifiedColumns, boolean ignoreWarnings, boolean tryAddVersion) throws OXException {
        return save(document, null, sequenceNumber, modifiedColumns, ignoreWarnings, tryAddVersion, new TransactionAwareFileAccessDelegation<SaveResult>() {

            @Override
            protected SaveResult callInTransaction(final FileStorageFileAccess access) throws OXException {
                ComparedObjectPermissions comparedPermissions = ShareHelper.processGuestPermissions(session, access, document, modifiedColumns);
                if (null != comparedPermissions && comparedPermissions.hasAddedUsers()) {
                    ObjectUseCountService useCountService = Services.optService(ObjectUseCountService.class);
                    if (null != useCountService) {
                        List<Integer> addedUsers = comparedPermissions.getAddedUsers();
                        for (Integer i : addedUsers) {
                            IncrementArguments arguments = new IncrementArguments.Builder(i.intValue()).build();
                            useCountService.incrementObjectUseCount(session, arguments);
                        }
                    }
                }
                IDTuple result = access.saveFileMetadata(document, sequenceNumber, modifiedColumns);
                document.setFolderId(result.getFolder());
                document.setId(result.getId());
                IDTuple idTuple = ShareHelper.applyGuestPermissions(session, access, document, comparedPermissions);
                SaveResult saveResult = new SaveResult();
                saveResult.setIDTuple(idTuple);
                saveResult.setAddedPermissions(ShareHelper.collectAddedObjectPermissions(comparedPermissions, session));
                return saveResult;
            }
        });
    }

    @Override
    public List<String> move(final List<String> sourceIds, final long sequenceNumber, String destFolderId, final boolean adjustFilenamesAsNeeded) throws OXException {
        final FolderID destinationID;
        if (null == destFolderId) {
            throw FileStorageExceptionCodes.INVALID_FOLDER_IDENTIFIER.create("null");
        }
        destinationID = new FolderID(destFolderId);

        boolean useMultiMove = false;
        final String destService = destinationID.getService();
        final String destAccountId = destinationID.getAccountId();
        FileStorageFileAccess fileAccess = getFileAccess(destService, destAccountId);
        if (FileStorageTools.supports(fileAccess, FileStorageCapability.MULTI_MOVE)) {
            useMultiMove = true;
            for (int i = sourceIds.size(); useMultiMove && i-- > 0;) {
                FileID sourceID = new FileID(sourceIds.get(i));
                useMultiMove = (sourceID.getService().equals(destService) && sourceID.getAccountId().equals(destAccountId));
            }

            if (useMultiMove) {
                TransactionAwareFileAccessDelegation<List<String>> callable = new TransactionAwareFileAccessDelegation<List<String>>() {

                    @Override
                    protected List<String> callInTransaction(FileStorageFileAccess access) throws OXException {
                        FileStorageMultiMove multiMove = (FileStorageMultiMove) access;

                        List<IDTuple> sources = new ArrayList<IDTuple>(sourceIds.size());
                        for (String sourceId : sourceIds) {
                            FileID sourceID = new FileID(sourceId);
                            sources.add(new IDTuple(sourceID.getFolderId(), sourceID.getFileId()));
                        }

                        List<IDTuple> failedOnes = multiMove.move(sources, destinationID.getFolderId(), sequenceNumber, adjustFilenamesAsNeeded);

                        List<String> ids = new ArrayList<String>(sourceIds.size());
                        for (IDTuple idTuple : failedOnes) {
                            FileID fid = new FileID(destService, destAccountId, idTuple.getId(), idTuple.getId());
                            ids.add(fid.toUniqueID());
                        }
                        return ids;
                    }
                };

                return callable.call(fileAccess);
            }
        }

        // One-by-one...
        for (String sourceId : sourceIds) {
            DefaultFile file = new DefaultFile();
            file.setId(sourceId);
            file.setFolderId(destFolderId);

            // Save file metadata without binary payload
            saveFileMetadata(file, FileStorageFileAccess.DISTANT_FUTURE);
        }
        return Collections.emptyList();
    }

    @Override
    public String copy(final String sourceId, String version, final String destFolderId, final File update, InputStream newData, final List<File.Field> fields) throws OXException {
        /*
         * check source and target account
         */
        FileID sourceID = new FileID(sourceId);
        FolderID destinationID;
        if (null != destFolderId) {
            destinationID = new FolderID(destFolderId);
        } else if (null != update && null != update.getFolderId()) {
            destinationID = new FolderID(update.getFolderId());
        } else {
            throw FileStorageExceptionCodes.INVALID_FOLDER_IDENTIFIER.create("null");
        }
        /*
         * check for copy operation within same account
         */
        if (sourceID.getService().equals(destinationID.getService()) && sourceID.getAccountId().equals(destinationID.getAccountId())) {
            File metadata;
            if (null != update) {
                metadata = new DefaultFile();
                if (null != fields) {
                    metadata.copyFrom(update, fields.toArray(new File.Field[fields.size()]));
                } else {
                    metadata.copyFrom(update);
                }
                metadata.setFolderId(null);
                metadata.setId(null);
            } else {
                metadata = null;
            }
            IDTuple result = getFileAccess(sourceID.getService(), sourceID.getAccountId()).copy(
                new IDTuple(sourceID.getFolderId(), sourceID.getFileId()), version, destinationID.getFolderId(), metadata, newData, fields);
            FileID newID = new FileID(sourceID.getService(), sourceID.getAccountId(), result.getFolder(), result.getId());
            FolderID newFolderID = new FolderID(sourceID.getService(), sourceID.getAccountId(), result.getFolder());
            postEvent(FileStorageEventHelper.buildCreateEvent(
                session, newID.getService(), newID.getAccountId(), newFolderID.toUniqueID(), newID.toUniqueID(),
                null != update ? update.getFileName() : null));
            return newID.toUniqueID();
        }
        /*
         * create copy in target storage
         */
        DefaultFile fileMetadata = new DefaultFile(getFileMetadata(sourceId, version));
        if (update != null) {
            fileMetadata.copyFrom(update, fields.toArray(new File.Field[fields.size()]));
        }

        if (newData == null) {
            newData = getDocument(sourceId, version);
        }

        fileMetadata.setId(FileStorageFileAccess.NEW);
        fileMetadata.setFolderId(destFolderId);
        fileMetadata.setVersion(null);

        if (newData == null) {
            saveFileMetadata(fileMetadata, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
        } else {
            saveDocument(fileMetadata, newData, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
        }

        return fileMetadata.getId();
    }

    @Override
    public SearchIterator<File> search(final List<String> folderIds, final SearchTerm<?> searchTerm, final List<Field> fields, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        /*
         * check which storages to consider
         */
        Map<FileStorageFileAccess, List<String>> foldersByFileAccess;
        if (null == folderIds || 0 == folderIds.size()) {
            List<FileStorageFileAccess> allFileStorageAccesses = getAllFileStorageAccesses();
            foldersByFileAccess = new HashMap<FileStorageFileAccess, List<String>>(allFileStorageAccesses.size());
            for (FileStorageFileAccess fileStorageFileAccess : allFileStorageAccesses) {
                foldersByFileAccess.put(fileStorageFileAccess, null);
            }
        } else {
            foldersByFileAccess = getFoldersByFileAccess(folderIds);
        }
        if (0 == foldersByFileAccess.size()) {
            /*
             * no suitable storages
             */
            return SearchIteratorAdapter.emptyIterator();
        } else if (1 == foldersByFileAccess.size()) {
            /*
             * perform search in single storage
             */
            Entry<FileStorageFileAccess, List<String>> entry = foldersByFileAccess.entrySet().iterator().next();
            FileStorageFileAccess fileAccess = entry.getKey();
            if (false == FileStorageTools.supports(fileAccess, FileStorageCapability.SEARCH_BY_TERM)) {
                return SearchIteratorAdapter.emptyIterator();
            }
            SearchIterator<File> searchIterator = ((FileStorageAdvancedSearchFileAccess) fileAccess).search(
                entry.getValue(), searchTerm, fields, sort, order, start, end);
            if (null == searchIterator) {
                return SearchIteratorAdapter.emptyIterator();
            }
            FileStorageAccountAccess accountAccess = fileAccess.getAccountAccess();
            return fixIDs(searchIterator, accountAccess.getService().getId(), accountAccess.getAccountId());
        } else {
            /*
             * schedule tasks to perform search in multiple storages concurrently
             */
            ThreadPoolService threadPool = ThreadPools.getThreadPool();
            CompletionService<SearchIterator<File>> completionService = null != threadPool ?
                new ThreadPoolCompletionService<SearchIterator<File>>(threadPool) : new CallerRunsCompletionService<SearchIterator<File>>();
                int count = 0;
                for (Entry<FileStorageFileAccess, List<String>> entry : foldersByFileAccess.entrySet()) {
                    if (FileStorageTools.supports(entry.getKey(), FileStorageCapability.SEARCH_BY_TERM)) {
                        FileStorageAdvancedSearchFileAccess fileAccess = (FileStorageAdvancedSearchFileAccess) entry.getKey();
                        completionService.submit(getSearchCallable(fileAccess, entry.getValue(), searchTerm, fields, sort, order));
                        count++;
                    }
                }
                /*
                 * collect & filter results
                 */
                return new FilteringSearchIterator<File>(collectSearchResults(completionService, count, sort, order)) {

                    int index = 0;

                    @Override
                    public boolean accept(File thing) throws OXException {
                        try {
                            if (0 < start && index < start || 0 < end && index >= end) {
                                return false;
                            }
                            return true;
                        } finally {
                            index++;
                        }
                    }
                };
        }
    }

    @Override
    public SearchIterator<File> search(String folderId, boolean includeSubfolders, final SearchTerm<?> searchTerm, final List<Field> fields, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        /*
         * perform search in single storage
         */
        FolderID folderID = new FolderID(folderId);
        FileStorageFileAccess fileAccess = getFileAccess(folderID.getService(), folderID.getAccountId());
        if (false == FileStorageTools.supports(fileAccess, FileStorageCapability.SEARCH_BY_TERM)) {
            throw FileStorageExceptionCodes.OPERATION_NOT_SUPPORTED.create(fileAccess.getAccountAccess().getService());
        }
        SearchIterator<File> searchIterator = ((FileStorageAdvancedSearchFileAccess) fileAccess).search(
            folderID.getFolderId(), includeSubfolders, searchTerm, fields, sort, order, start, end);
        if (null == searchIterator) {
            return SearchIteratorAdapter.emptyIterator();
        }
        FileStorageAccountAccess accountAccess = fileAccess.getAccountAccess();
        return fixIDs(searchIterator, accountAccess.getService().getId(), accountAccess.getAccountId());
    }

    @Override
    public SearchIterator<File> search(final String query, final List<Field> columns, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        return search(query, columns, folderId, false, sort, order, start, end);
    }

    @Override
    public SearchIterator<File> search(final String query, final List<Field> columns, final String folderId, boolean includeSubfolders, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        // Check pattern
        checkPatternLength(query);

        // Proceed
        final List<Field> cols = addIDColumns(columns);
        if (FileStorageFileAccess.ALL_FOLDERS != folderId) {
            final FolderID id = new FolderID(folderId);
            SearchIterator<File> iterator = getFileAccess(id.getService(), id.getAccountId()).search(
                query,
                cols,
                id.getFolderId(),
                includeSubfolders,
                sort,
                order,
                start,
                end);
            return fixIDs(iterator, id.getService(), id.getAccountId());
        }

        // Search in all available folders
        final List<FileStorageFileAccess> all = getAllFileStorageAccesses();
        final int numOfStorages = all.size();
        if (0 >= numOfStorages) {
            return SearchIteratorAdapter.emptyIterator();
        }
        if (1 == numOfStorages) {
            final FileStorageFileAccess files = all.get(0);
            final SearchIterator<File> result = files.search(query, cols, FileStorageFileAccess.ALL_FOLDERS, sort, order, start, end);
            if (result == null) {
                return SearchIteratorAdapter.emptyIterator();
            }
            final FileStorageAccountAccess accountAccess = files.getAccountAccess();
            return fixIDs(result, accountAccess.getService().getId(), accountAccess.getAccountId());
        }

        /*-
         * We have to consider multiple file storages
         *
         * Poll them concurrently...
         */
        Collections.sort(all, INFOSTORE_FIRST_COMPARATOR);
        LinkedList<Future<SearchIterator<File>>> tasks = new LinkedList<Future<SearchIterator<File>>>();
        ThreadPoolService threadPool = ThreadPools.getThreadPool();
        for (int i = 0; i < numOfStorages; i++) {
            final FileStorageFileAccess files = all.get(i);
            tasks.add(threadPool.submit(new AbstractTask<SearchIterator<File>>() {

                @Override
                public SearchIterator<File> call() {
                    try {
                        final SearchIterator<File> result = files.search(query, cols, FileStorageFileAccess.ALL_FOLDERS, sort, order, start, end);
                        if (result != null) {
                            final FileStorageAccountAccess accountAccess = files.getAccountAccess();
                            return fixIDs(result, accountAccess.getService().getId(), accountAccess.getAccountId());
                        }
                    } catch (final Exception e) {
                        // Ignore failed one in composite search results
                    }
                    return SearchIteratorAdapter.emptyIterator();
                }
            }));
        }

        /*-
         * Await completion
         *
         * Take first from InfoStore using this thread
         */
        final Queue<SearchIterator<File>> results = new ConcurrentLinkedQueue<SearchIterator<File>>();
        {
            SearchIterator<File> it = getFrom(tasks.removeFirst());
            if (null != it) {
                results.offer(it);
            }
        }

        // Take from rest concurrently
        final SearchIterator<File> fallback = SearchIteratorAdapter.<File> emptyIterator();
        final CountDownLatch latch = new CountDownLatch(numOfStorages - 1);
        for (int i = 1; i < numOfStorages; i++) {
            final Future<SearchIterator<File>> future = tasks.removeFirst();
            threadPool.submit(new AbstractTask<Void>() {

                @Override
                public Void call() throws Exception {
                    try {
                        SearchIterator<File> it = getFrom(future, 5, TimeUnit.SECONDS, fallback);
                        if (null != it) {
                            results.offer(it);
                        }
                        return null;
                    } finally {
                        latch.countDown();
                    }
                }
            });
        }

        // Await
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }

        return new MergingSearchIterator<File>(order.comparatorBy(sort), order.equals(SortDirection.ASC), new LinkedList<SearchIterator<File>>(results));
    }

    static <V> V getFrom(Future<V> future) throws OXException {
        try {
            return future.get();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        }
    }

    static <V> V getFrom(Future<V> future, long timeout, TimeUnit unit, V fallbackOnTimeout) throws OXException {
        try {
            return future.get(timeout, unit);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (ExecutionException e) {
            throw ThreadPools.launderThrowable(e, OXException.class);
        } catch (TimeoutException e) {
            return fallbackOnTimeout;
        }
    }

    @Override
    public void touch(final String id) throws OXException {
        final FileID fileID = new FileID(id);
        final FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());

        /*
         * Touch
         */
        final TransactionAwareFileAccessDelegation<Void> touchDelegation = new TransactionAwareFileAccessDelegation<Void>() {

            @Override
            protected Void callInTransaction(FileStorageFileAccess access) throws OXException {
                access.touch(fileID.getFolderId(), fileID.getFileId());
                return null;
            }
        };
        touchDelegation.call(fileAccess);

        /*
         * Post event
         */
        String fileFolder = fileID.getFolderId();
        FolderID folderID;
        String fileName = null;
        if (fileFolder == null) {
            File metadata = fileAccess.getFileMetadata(null, id, FileStorageFileAccess.CURRENT_VERSION);
            fileName = metadata.getFileName();
            folderID = new FolderID(metadata.getFolderId());
        } else {
            folderID = new FolderID(fileID.getService(), fileID.getAccountId(), fileID.getFolderId());
        }

        Event event = FileStorageEventHelper.buildUpdateEvent(
            session,
            fileID.getService(),
            fileID.getAccountId(),
            folderID.toUniqueID(),
            fileID.toUniqueID(),
            fileName);
        postEvent(event);
    }

    /**
     * Posts specified event
     *
     * @param event The event
     */
    protected void postEvent(final Event event) {
        getEventAdmin().postEvent(event);
    }

    /**
     * Gets all file accesses being responsible for the supplied list of unique identifiers.
     *
     * @param ids The unique file identifiers to get the file accesses for
     * @return The file accesses, each one mapped to the corresponding, file access relative, ID tuples
     * @throws OXException
     */
    protected Map<FileStorageFileAccess, List<IDTuple>> getFilesPerFileAccesses(List<String> ids) throws OXException {
        Map<FileStorageFileAccess, List<IDTuple>> fileAccesses = new HashMap<FileStorageFileAccess, List<IDTuple>>();
        Map<String, FileStorageFileAccess> identifiedFileAccesses = new HashMap<String, FileStorageFileAccess>();
        for (String id : ids) {
            FileID fileID = new FileID(id);
            String fileAccessID = fileID.getService() + '/' + fileID.getAccountId();
            FileStorageFileAccess fileAccess = identifiedFileAccesses.get(fileAccessID);
            if (null == fileAccess) {
                fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
                identifiedFileAccesses.put(fileAccessID, fileAccess);
            }
            List<IDTuple> gets = fileAccesses.get(fileAccess);
            if (null == gets) {
                gets = new ArrayList<IDTuple>();
                fileAccesses.put(fileAccess, gets);
            }
            gets.add(new IDTuple(fileID.getFolderId(), fileID.getFileId()));
        }
        return fileAccesses;
    }

    /**
     * Handles the supplied input stream if appropriate stream handlers are registered.
     *
     * @param fileID The file identifier
     * @param version The file version
     * @param inputStream The input stream
     * @return The possibly modified input stream, or the original stream if no handlers registered
     * @throws OXException
     */
    private InputStream handleInputStream(FileID fileID, String version, InputStream inputStream) throws OXException {
        if (null == inputStream) {
            return null;
        }
        FileStreamHandlerRegistry handlerRegistry = getStreamHandlerRegistry();
        if (null != handlerRegistry) {
            final Collection<FileStreamHandler> handlers = handlerRegistry.getHandlers();
            if (null != handlers && 0 < handlers.size()) {
                for (FileStreamHandler streamHandler : handlers) {
                    inputStream = streamHandler.handleDocumentStream(inputStream, fileID, version, session.getContextId());
                }
            }
        }
        return inputStream;
    }

    /**
     * Handles the supplied document stream if appropriate stream handlers are registered.
     *
     * @param fileID The file identifier
     * @param version The file version
     * @param document The document
     * @return The handled document, with a possibly modified stream, or the original document if no handlers registered
     */
    private Document handleDocumentStream(final FileID fileID, final String version, final Document document) {
        FileStreamHandlerRegistry handlerRegistry = getStreamHandlerRegistry();
        if (null != handlerRegistry) {
            final Collection<FileStreamHandler> handlers = handlerRegistry.getHandlers();
            if (null != handlers && 0 < handlers.size()) {
                return new Document(document) {

                    @Override
                    public InputStream getData() throws OXException {
                        InputStream inputStream = document.getData();
                        for (FileStreamHandler streamHandler : handlers) {
                            inputStream = streamHandler.handleDocumentStream(inputStream, fileID, version, session.getContextId());
                        }
                        return inputStream;
                    }
                };
            }
        }
        return document;
    }

    /**
     * Gets all file storage file accesses serving the supplied folders.
     *
     * @param folderIds The (unique) folder IDs to get the file accesses for
     * @return The file accesses, each one mapped to the matching list of (storage-relative) folder IDs
     * @throws OXException
     */
    private Map<FileStorageFileAccess, List<String>> getFoldersByFileAccess(List<String> folderIds) throws OXException {
        if (null == folderIds || 0 == folderIds.size()) {
            return Collections.emptyMap();
        } else if (1 == folderIds.size()) {
            FolderID folderID = new FolderID(folderIds.get(0));
            return Collections.singletonMap(getFileAccess(folderID.getService(), folderID.getAccountId()),
                Collections.singletonList(folderID.getFolderId()));
        } else {
            Map<String, FileStorageFileAccess> fileAccessesByAccount = new HashMap<String, FileStorageFileAccess>();
            Map<String, List<String>> foldersByAccount = new HashMap<String, List<String>>();
            for (String uniqueID : folderIds) {
                FolderID folderID = new FolderID(uniqueID);
                String key = folderID.getService() + '/' + folderID.getAccountId();
                List<String> folders = foldersByAccount.get(key);
                if (null == folders) {
                    folders = new ArrayList<String>();
                    foldersByAccount.put(key, folders);
                }
                folders.add(folderID.getFolderId());
                FileStorageFileAccess fileAccess = fileAccessesByAccount.get(key);
                if (null == fileAccess) {
                    fileAccess = getFileAccess(folderID.getService(), folderID.getAccountId());
                    fileAccessesByAccount.put(key, fileAccess);
                }
            }
            Map<FileStorageFileAccess, List<String>> foldersByFileAccess =
                new HashMap<FileStorageFileAccess, List<String>>(foldersByAccount.size());
            for (Entry<String, FileStorageFileAccess> entry : fileAccessesByAccount.entrySet()) {
                foldersByFileAccess.put(entry.getValue(), foldersByAccount.get(entry.getKey()));
            }
            return foldersByFileAccess;
        }
    }

    /**
     * Collects any possible warnings that might occur when copying a file from one file storage account into another.
     *
     * @param sourceFileID The identifier of the source file
     * @param sourceFile The source file
     * @param targetFolderID The identifier of the target folder
     * @param modifiedColumns A list of fields that are going to be modified during the copy operation, or <code>null</code> if all fields are copied
     * @return The warnings, or an empty list if there are none
     */
    private List<OXException> collectWarningsBeforeMove(FileID sourceFileID, File sourceFile, FolderID targetFolderID, List<Field> modifiedColumns) throws OXException {
        List<OXException> warnings = new ArrayList<OXException>(6);
        if (Strings.isNotEmpty(sourceFile.getDescription()) && (null == modifiedColumns || modifiedColumns.contains(Field.DESCRIPTION))) {
            FolderID sourceFolderID = new FolderID(sourceFileID.getService(), sourceFileID.getAccountId(), sourceFileID.getFolderId());
            FileStorageFolder[] sourcePath = getFolderAccess(sourceFolderID).getPath2DefaultFolder(sourceFolderID.getFolderId());
            warnings.add(FileStorageExceptionCodes.LOSS_OF_NOTES.create(sourceFile.getFileName(), getPathString(sourcePath),
                getAccountName(this, targetFolderID), sourceFileID.toUniqueID(), targetFolderID.toUniqueID()));
        }
        if (Strings.isNotEmpty(sourceFile.getCategories()) && (null == modifiedColumns || modifiedColumns.contains(Field.CATEGORIES))) {
            FolderID sourceFolderID = new FolderID(sourceFileID.getService(), sourceFileID.getAccountId(), sourceFileID.getFolderId());
            FileStorageFolder[] sourcePath = getFolderAccess(sourceFolderID).getPath2DefaultFolder(sourceFolderID.getFolderId());
            warnings.add(FileStorageExceptionCodes.LOSS_OF_CATEGORIES.create(sourceFile.getFileName(), getPathString(sourcePath),
                getAccountName(this, targetFolderID), sourceFileID.toUniqueID(), targetFolderID.toUniqueID()));
        }
        if (1 < sourceFile.getNumberOfVersions()) {
            FolderID sourceFolderID = new FolderID(sourceFileID.getService(), sourceFileID.getAccountId(), sourceFileID.getFolderId());
            FileStorageFolder[] sourcePath = getFolderAccess(sourceFolderID).getPath2DefaultFolder(sourceFolderID.getFolderId());
            warnings.add(FileStorageExceptionCodes.LOSS_OF_VERSIONS.create(sourceFile.getFileName(), getPathString(sourcePath),
                getAccountName(this, targetFolderID), sourceFileID.toUniqueID(), targetFolderID.toUniqueID()));
        }
        if (null != sourceFile.getObjectPermissions() && 0 < sourceFile.getObjectPermissions().size()) {
            FolderID sourceFolderID = new FolderID(sourceFileID.getService(), sourceFileID.getAccountId(), sourceFileID.getFolderId());
            FileStorageFolder[] sourcePath = getFolderAccess(sourceFolderID).getPath2DefaultFolder(sourceFolderID.getFolderId());
            warnings.add(FileStorageExceptionCodes.LOSS_OF_FILE_SHARES.create(sourceFile.getFileName(), getPathString(sourcePath),
                getAccountName(this, targetFolderID), sourceFileID.toUniqueID(), targetFolderID.toUniqueID()));
        }
        return warnings;
    }

    /**
     * Collects any possible warnings that might occur when saving a file in a specific file storage.
     *
     * @param file The file to save
     * @param fileAccess The file storage access where the file is being saved
     * @param modifiedColumns A list of fields that are going to be modified during the save operation, or <code>null</code> if all fields are saved
     * @return The warnings, or an empty list if there are none
     */
    private List<OXException> collectWarningsBeforeSave(File file, FileStorageFileAccess fileAccess, List<Field> modifiedColumns) throws OXException {
        List<OXException> warnings = new ArrayList<OXException>(4);
        String fileName = null;
        if (Strings.isNotEmpty(file.getDescription()) && (null == modifiedColumns || modifiedColumns.contains(Field.DESCRIPTION)) && !FileStorageTools.supports(fileAccess, Field.DESCRIPTION)) {
            fileName = getFileNameFrom(file, fileAccess);
            warnings.add(FileStorageExceptionCodes.NO_NOTES_SUPPORT.create(fileName, getAccountName(this, fileAccess), file.getId(), file.getFolderId()));
        }
        if (Strings.isNotEmpty(file.getCategories()) && (null == modifiedColumns || modifiedColumns.contains(Field.CATEGORIES)) && !FileStorageTools.supports(fileAccess, Field.CATEGORIES)) {
            if (null == fileName) {
                fileName = getFileNameFrom(file, fileAccess);
            }
            warnings.add(FileStorageExceptionCodes.NO_CATEGORIES_SUPPORT.create(fileName, getAccountName(this, fileAccess), file.getId(), file.getFolderId()));
        }
        if (null != file.getObjectPermissions() && 0 < file.getObjectPermissions().size() && !FileStorageTools.supports(fileAccess, FileStorageCapability.OBJECT_PERMISSIONS)) {
            warnings.add(FileStorageExceptionCodes.NO_PERMISSION_SUPPORT.create(getAccountName(this, fileAccess), file.getFolderId(), session.getContextId()));
        }
        return warnings;
    }

    private static String getFileNameFrom(File file, FileStorageFileAccess fileAccess) throws OXException {
        String fileName = file.getFileName();
        if (Strings.isEmpty(fileName)) {
            fileName = fileAccess.getFileMetadata(file.getFolderId(), file.getId(), FileStorageFileAccess.CURRENT_VERSION).getFileName();
        }
        return fileName;
    }

    /**
     * Collects and merges multiple search results from the supplied completion service.
     *
     * @param completionService The completion service to take the results from
     * @param count The number of results to take
     * @param sort The field to sort
     * @param order The sort order
     * @return A merged search iterator
     * @throws OXException
     */
    private static SearchIterator<File> collectSearchResults(CompletionService<SearchIterator<File>> completionService, int count, Field sort, SortDirection order) throws OXException {
        List<SearchIterator<File>> searchIterators = new ArrayList<SearchIterator<File>>(count);
        for (int i = 0; i < count; i++) {
            SearchIterator<File> searchIterator = null;
            try {
                searchIterator = completionService.take().get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (null != cause && OXException.class.isInstance(e.getCause())) {
                    throw (OXException)cause;
                }
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
            if (null != searchIterator) {
                searchIterators.add(searchIterator);
            }
        }
        return new MergingSearchIterator<File>(order.comparatorBy(sort), order.equals(SortDirection.ASC), searchIterators);
    }

    /**
     * Constructs a callable task to search in a specific file storage access.
     *
     * @param fileAccess The file access to query
     * @param folderIds The (relative) folder IDs to pass to the storage
     * @param searchTerm The search term to pass to the storage
     * @param fields The fields to pass to the storage
     * @param sort The sort field to pass to the storage
     * @param order The sort order to pass to the storage
     * @return The callable
     */
    private static Callable<SearchIterator<File>> getSearchCallable(final FileStorageAdvancedSearchFileAccess fileAccess, final List<String> folderIds, final SearchTerm<?> searchTerm, final List<Field> fields, final Field sort, final SortDirection order) {
        return new Callable<SearchIterator<File>>() {

            @Override
            public SearchIterator<File> call() throws Exception {
                SearchIterator<File> searchIterator = null;
                try {
                    searchIterator = fileAccess.search(
                        folderIds, searchTerm, fields, sort, order, FileStorageFileAccess.NOT_SET, FileStorageFileAccess.NOT_SET);
                } catch (Exception e) {
                    // Ignore failed one in composite search results
                    LOG.warn("Error searching in file storage {}", fileAccess, e);
                }
                if (null == searchIterator) {
                    return SearchIteratorAdapter.emptyIterator();
                }
                FileStorageAccountAccess accountAccess = fileAccess.getAccountAccess();
                return fixIDs(searchIterator, accountAccess.getService().getId(), accountAccess.getAccountId());
            }
        };
    }

    /**
     * Looks up a file in a collection based on its unique identifier.
     *
     * @param files The files to search
     * @param uniqueID The ID to find
     * @return The file, or <code>null</code> if not found
     */
    private static File find(Collection<File> files, String uniqueID) {
        for (File file : files) {
            if (uniqueID.equals(file.getId())) {
                return file;
            }
        }
        return null;
    }

    private TimedResult<File> slice(TimedResult<File> documents, Range range) throws OXException {
        if (null == range) {
            return documents;
        }

        int from = range.from;
        int to = range.to;
        if (from >= to) {
            return EMPTY_TIMED_RESULT;
        }

        SearchIterator<File> iter = documents.results();
        try {
            int index = 0;
            while (index < from) {
                if (false == iter.hasNext()) {
                    return EMPTY_TIMED_RESULT;
                }
                iter.next();
                index++;
            }

            List<File> files = new LinkedList<File>();
            while (index < to && iter.hasNext()) {
                files.add(iter.next());
                index++;
            }

            return new ListBasedTimedResult(files, documents.sequenceNumber());
        } finally {
            SearchIterators.close(iter);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private final class ListBasedTimedResult implements TimedResult<File> {

        private final long sequenceNumber;
        private final SearchIterator<File> results;

        /**
         * Initializes a new {@link TimedResultImplementation}.
         */
        ListBasedTimedResult(List<File> files, long sequenceNumber) {
            super();
            this.results = new SearchIteratorDelegator<>(files);
            this.sequenceNumber = sequenceNumber;
        }

        @Override
        public long sequenceNumber() throws OXException {
            return sequenceNumber;
        }

        @Override
        public SearchIterator<File> results() throws OXException {
            return results;
        }
    }

}
