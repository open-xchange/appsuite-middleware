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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import static com.openexchange.file.storage.composition.internal.IDManglingFileCustomizer.fixIDs;
import static com.openexchange.java.Autoboxing.I;
import java.io.InputStream;
import java.sql.Connection;
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
import org.osgi.service.event.EventAdmin;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.DefaultFileStorageObjectPermission;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageEfficientRetrieval;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.FileStorageEventHelper;
import com.openexchange.file.storage.FileStorageEventHelper.EventProperty;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageIgnorableVersionFileAccess;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.file.storage.FileStorageRandomFileAccess;
import com.openexchange.file.storage.FileStorageSequenceNumberProvider;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.ThumbnailAware;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FileStreamHandler;
import com.openexchange.file.storage.composition.FileStreamHandlerRegistry;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedRandomFileAccess;
import com.openexchange.file.storage.composition.IDBasedSequenceNumberProvider;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.file.storage.search.SearchTerm;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.CallerRunsCompletionService;
import com.openexchange.log.LogProperties;
import com.openexchange.session.Session;
import com.openexchange.share.Share;
import com.openexchange.share.ShareService;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.iterator.FilteringSearchIterator;
import com.openexchange.tools.iterator.MergingSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tx.AbstractService;
import com.openexchange.tx.TransactionAwares;
import com.openexchange.tx.TransactionException;
import com.openexchange.tx.TransactionState;

/**
 * {@link AbstractCompositingIDBasedFileAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractCompositingIDBasedFileAccess extends AbstractService<Transaction> implements IDBasedRandomFileAccess, IDBasedSequenceNumberProvider {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractCompositingIDBasedFileAccess.class);

    /** The empty {@link TimedResult} */
    private static final TimedResult<File> EMPTY_TIMED_RESULT = new TimedResult<File>() {

        @Override
        public SearchIterator<File> results() throws OXException {
            return SearchIteratorAdapter.emptyIterator();
        }

        @Override
        public long sequenceNumber() throws OXException {
            return 0;
        }

    };

    /** The handler registry */
    private static final AtomicReference<FileStreamHandlerRegistry> HANDLER_REGISTRY = new AtomicReference<FileStreamHandlerRegistry>();

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

    /**
     * The service identifier for InfoStore.
     */
    protected static final String INFOSTORE_SERVICE_ID = "com.openexchange.infostore";

    private static final AtomicReference<FileStorageService> INFOSTORE_SERVICE_REF = new AtomicReference<FileStorageService>();

    // ------------------------------------------------------------------------------------------------- //

    /** The associated session */
    protected Session session;

    private final ThreadLocal<Map<String, FileStorageAccountAccess>> connectedAccounts = new ThreadLocal<Map<String, FileStorageAccountAccess>>();
    private final ThreadLocal<List<FileStorageAccountAccess>> accessesToClose = new ThreadLocal<List<FileStorageAccountAccess>>();
    private final Comparator<FileStorageFileAccess> infostoreFirstFileAccessComparator;

    /**
     * Initializes a new {@link AbstractCompositingIDBasedFileAccess}.
     *
     * @param session The associated session
     */
    protected AbstractCompositingIDBasedFileAccess(final Session session) {
        super();
        this.session = session;
        connectedAccounts.set(new HashMap<String, FileStorageAccountAccess>());
        accessesToClose.set(new LinkedList<FileStorageAccountAccess>());
        infostoreFirstFileAccessComparator = new Comparator<FileStorageFileAccess>() {

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
            if (FileStorageSequenceNumberProvider.class.isInstance(fileAccess)) {
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
    public boolean supportsSequenceNumbers(String folderId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        FileStorageFileAccess fileAccess = getFileAccess(folderID.getService(), folderID.getAccountId());
        return FileStorageSequenceNumberProvider.class.isInstance(fileAccess);
    }

    @Override
    public Document getDocumentAndMetadata(String id, final String version) throws OXException {
        final FileID fileID = new FileID(id);
        final FileStreamHandlerRegistry registry = getStreamHandlerRegistry();
        FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
        if (!(fileAccess instanceof FileStorageEfficientRetrieval)) {
            return null;
        }
        FileStorageEfficientRetrieval retrieval = (FileStorageEfficientRetrieval) fileAccess;
        // Post event
        {
            File metaData = fileAccess.getFileMetadata(fileID.getFolderId(), fileID.getFileId(), version);
            if (null != metaData) {
                postEvent(FileStorageEventHelper.buildAccessEvent(
                    session,
                    fileID.getService(),
                    fileID.getAccountId(),
                    metaData.getFolderId(),
                    fileID.toUniqueID(),
                    metaData.getFileName(),
                    extractRemoteAddress()));
            }
        }
        // Proceed...
        final Document document = retrieval.getDocumentAndMetadata(fileID.getFolderId(), fileID.getFileId(), version);
        if (null == document) {
            return null;
        }
        if (null == registry) {
            return document;
        }
        final Collection<FileStreamHandler> handlers = registry.getHandlers();
        if (null == handlers || handlers.isEmpty()) {
            return document;
        }
        // Handle stream
        Document clone = new Document(document) {

            @Override
            public InputStream getData() throws OXException {
                InputStream inputStream = document.getData();
                for (final FileStreamHandler streamHandler : handlers) {
                    inputStream = streamHandler.handleDocumentStream(inputStream, fileID, version, session.getContextId());
                }
                return inputStream;
            }

        };

        return clone;
    }

    @Override
    public Document getDocumentAndMetadata(String id, final String version, String clientETag) throws OXException {
        final FileID fileID = new FileID(id);
        final FileStreamHandlerRegistry registry = getStreamHandlerRegistry();
        FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
        if (!(fileAccess instanceof FileStorageEfficientRetrieval)) {
            return null;
        }
        FileStorageEfficientRetrieval retrieval = (FileStorageEfficientRetrieval) fileAccess;
        // Post event
        {
            File metaData = fileAccess.getFileMetadata(fileID.getFolderId(), fileID.getFileId(), version);
            if (null != metaData) {
                postEvent(FileStorageEventHelper.buildAccessEvent(
                    session,
                    fileID.getService(),
                    fileID.getAccountId(),
                    metaData.getFolderId(),
                    fileID.toUniqueID(),
                    metaData.getFileName(),
                    extractRemoteAddress()));
            }
        }
        // Proceed...
        final Document document = retrieval.getDocumentAndMetadata(fileID.getFolderId(), fileID.getFileId(), version, clientETag);
        if (null == document) {
            return null;
        }
        if (null == registry) {
            return document;
        }
        final Collection<FileStreamHandler> handlers = registry.getHandlers();
        if (null == handlers || handlers.isEmpty()) {
            return document;
        }
        // Handle stream
        Document clone = new Document(document) {

            @Override
            public InputStream getData() throws OXException {
                InputStream inputStream = document.getData();
                for (final FileStreamHandler streamHandler : handlers) {
                    inputStream = streamHandler.handleDocumentStream(inputStream, fileID, version, session.getContextId());
                }
                return inputStream;
            }

        };

        return clone;
    }

    @Override
    public InputStream getDocument(final String id, final String version) throws OXException {
        final FileID fileID = new FileID(id);
        final FileStreamHandlerRegistry registry = getStreamHandlerRegistry();
        FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
        // Post event
        {
            File metaData = fileAccess.getFileMetadata(fileID.getFolderId(), fileID.getFileId(), version);
            if (null != metaData) {
                postEvent(FileStorageEventHelper.buildAccessEvent(
                    session,
                    fileID.getService(),
                    fileID.getAccountId(),
                    metaData.getFolderId(),
                    fileID.toUniqueID(),
                    metaData.getFileName(),
                    extractRemoteAddress()));
            }
        }
        // Proceed...
        if (null == registry) {
            return fileAccess.getDocument(fileID.getFolderId(), fileID.getFileId(), version);
        }
        final Collection<FileStreamHandler> handlers = registry.getHandlers();
        if (null == handlers || handlers.isEmpty()) {
            return fileAccess.getDocument(fileID.getFolderId(), fileID.getFileId(), version);
        }
        // Handle stream
        InputStream inputStream = fileAccess.getDocument(fileID.getFolderId(), fileID.getFileId(), version);
        for (final FileStreamHandler streamHandler : handlers) {
            inputStream = streamHandler.handleDocumentStream(inputStream, fileID, version, session.getContextId());
        }
        return inputStream;
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
        TimedResult<File> result;
        try {
            result = getFileAccess(service, accountId).getDocuments(folderID.getFolderId(), addIDColumns(columns), sort, order);
            return fixIDs(result, service, accountId);
        } catch (final OXException e) {
            if (!FileStorageExceptionCodes.UNKNOWN_FILE_STORAGE_SERVICE.equals(e) || !INFOSTORE_SERVICE_ID.equals(service)) {
                throw e;
            }
            result = EMPTY_TIMED_RESULT;
        }
        return fixIDs(result, service, accountId);
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
    public File getFileMetadata(final String id, final String version) throws OXException {
        final FileID fileID = new FileID(id);
        final File fileMetadata = getFileAccess(fileID.getService(), fileID.getAccountId()).getFileMetadata(
            fileID.getFolderId(),
            fileID.getFileId(),
            version);
        return fixIDs(fileMetadata, fileID.getService(), fileID.getAccountId());
    }

    @Override
    public TimedResult<File> getVersions(final String id) throws OXException {
        final FileID fileID = new FileID(id);
        final TimedResult<File> result = getFileAccess(fileID.getService(), fileID.getAccountId()).getVersions(
            fileID.getFolderId(),
            fileID.getFileId());
        return fixIDs(result, fileID.getService(), fileID.getAccountId());
    }

    @Override
    public TimedResult<File> getVersions(final String id, final List<Field> columns) throws OXException {
        final FileID fileID = new FileID(id);
        final TimedResult<File> result = getFileAccess(fileID.getService(), fileID.getAccountId()).getVersions(
            fileID.getFolderId(),
            fileID.getFileId(),
            addIDColumns(columns));
        return fixIDs(result, fileID.getService(), fileID.getAccountId());
    }

    @Override
    public TimedResult<File> getVersions(final String id, final List<Field> columns, final Field sort, final SortDirection order) throws OXException {
        final FileID fileID = new FileID(id);
        final TimedResult<File> result = getFileAccess(fileID.getService(), fileID.getAccountId()).getVersions(
            fileID.getFolderId(),
            fileID.getFileId(),
            addIDColumns(columns),
            sort,
            order);
        return fixIDs(result, fileID.getService(), fileID.getAccountId());
    }

    @Override
    public void lock(final String id, final long diff) throws OXException {
        final FileID fileID = new FileID(id);
        final TransactionAwareFileAccessDelegation<Void> lockDelegation = new TransactionAwareFileAccessDelegation<Void>() {

            @Override
            protected Void callInTransaction(FileStorageFileAccess access) throws OXException {
                access.lock(fileID.getFolderId(), fileID.getFileId(), diff);
                return null;
            }
        };
        lockDelegation.call(getFileAccess(fileID.getService(), fileID.getAccountId()));
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
        final List<String> notDeleted = new ArrayList<String>(ids.size());
        for (final Map.Entry<FileStorageFileAccess, List<IDTuple>> deleteOp : deleteOperations.entrySet()) {
            final FileStorageFileAccess access = deleteOp.getKey();
            final List<IDTuple> toDelete = ensureFolderIDs(access, deleteOp.getValue());
            /*
             * delete
             */
            final TransactionAwareFileAccessDelegation<List<IDTuple>> removeDocumentDelegation = new TransactionAwareFileAccessDelegation<List<IDTuple>>() {

                @Override
                protected List<IDTuple> callInTransaction(FileStorageFileAccess access) throws OXException {
                    return access.removeDocument(toDelete, sequenceNumber, hardDelete);
                }
            };
            final List<IDTuple> conflicted = removeDocumentDelegation.call(access);
            for (final IDTuple tuple : conflicted) {
                final FileStorageAccountAccess accountAccess = access.getAccountAccess();
                notDeleted.add(new FileID(
                    accountAccess.getService().getId(),
                    accountAccess.getAccountId(),
                    tuple.getFolder(),
                    tuple.getId()).toUniqueID());
            }

            /*
             * Send event
             */
            String serviceId = access.getAccountAccess().getService().getId();
            String accountId = access.getAccountAccess().getAccountId();
            toDelete.removeAll(conflicted);
            for (IDTuple tuple : toDelete) {
                String folderId = new FolderID(serviceId, accountId, tuple.getFolder()).toUniqueID();
                String fileId = new FileID(serviceId, accountId, tuple.getFolder(), tuple.getId()).toUniqueID();
                EventProperty property = new EventProperty(FileStorageEventConstants.HARD_DELETE, Boolean.valueOf(hardDelete));
                postEvent(FileStorageEventHelper.buildDeleteEvent(session, serviceId, accountId, folderId, fileId, null, null, property));
            }
        }
        return notDeleted;
    }

    @Override
    public String[] removeVersion(final String id, final String[] versions) throws OXException {
        final FileID fileID = new FileID(id);
        final FileStorageFileAccess access = getFileAccess(fileID.getService(), fileID.getAccountId());
        final TransactionAwareFileAccessDelegation<String[]> removeVersionDelegation = new TransactionAwareFileAccessDelegation<String[]>() {

            @Override
            protected String[] callInTransaction(FileStorageFileAccess access) throws OXException {
                return access.removeVersion(fileID.getFolderId(), fileID.getFileId(), versions);
            }
        };
        final String[] notRemoved = removeVersionDelegation.call(access);

        String serviceId = access.getAccountAccess().getService().getId();
        String accountId = access.getAccountAccess().getAccountId();
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
            boolean tsOwner = !TransactionState.isInitialized();
            if (tsOwner) {
                TransactionState.init();
            }
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
                if (tsOwner) {
                    TransactionState.cleanUp();
                }
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

    private List<FileStorageGuestObjectPermission> stripGuestPermissions(File document) {
        List<FileStorageObjectPermission> objectPermissions = document.getObjectPermissions();
        if (objectPermissions != null && !objectPermissions.isEmpty()) {
            List<FileStorageGuestObjectPermission> guestPermissions = new ArrayList<FileStorageGuestObjectPermission>(objectPermissions.size());
            List<FileStorageObjectPermission> userPermissions = new ArrayList<FileStorageObjectPermission>(objectPermissions.size());
            for (FileStorageObjectPermission fsop : objectPermissions) {
                if (FileStorageGuestObjectPermission.class.isAssignableFrom(fsop.getClass())) {
                    guestPermissions.add((FileStorageGuestObjectPermission) fsop);
                } else {
                    userPermissions.add(fsop);
                }
            }

            document.setObjectPermissions(userPermissions);
            return guestPermissions;
        }

        return Collections.emptyList();
    }

    private boolean handleNewGuestPermissions(File document, List<FileStorageGuestObjectPermission> guestPermissions) throws OXException {
        if (guestPermissions != null && !guestPermissions.isEmpty()) {
            List<ShareRecipient> shareRecipients = new ArrayList<ShareRecipient>(guestPermissions.size());
            for (FileStorageGuestObjectPermission guestPermission : guestPermissions) {
                shareRecipients.add(guestPermission.toShareRecipient());
            }

            Connection con = null;
            if (TransactionState.isInitialized()) {
                Object object = TransactionState.get(TransactionState.WRITE_CON);
                if (object != null && object instanceof Connection) {
                    con = (Connection) object;
                }
            }

            List<FileStorageObjectPermission> allPermissions = new ArrayList<FileStorageObjectPermission>(shareRecipients.size());
            try {
                session.setParameter(Connection.class.getName(), con);
                ShareService shareService = Services.getService(ShareService.class);
                ShareTarget shareTarget = new ShareTarget(8, document.getFolderId(), document.getId()); // TODO: no module constant accessible
                List<Share> shares = shareService.createShares(session, shareTarget, shareRecipients);
                for (int i = 0; i < guestPermissions.size(); i++) {
                    FileStorageGuestObjectPermission guestPermission = guestPermissions.get(0);
                    Share share = shares.get(i);
                    allPermissions.add(new DefaultFileStorageObjectPermission(share.getGuest(), false, guestPermission.getPermissions()));
                }

                List<FileStorageObjectPermission> objectPermissions = document.getObjectPermissions();
                if (objectPermissions != null) {
                    for (FileStorageObjectPermission objectPermission : objectPermissions) {
                        allPermissions.add(objectPermission);
                    }
                }

                document.setObjectPermissions(allPermissions);
                return true;
            } finally {
                session.setParameter(Connection.class.getName(), null);
            }
        }

        return false;
    }

    protected void save(final File document, final InputStream data, final long sequenceNumber, final List<Field> modifiedColumns, final FileAccessDelegation<Void> saveDelegation) throws OXException {
        if (FileStorageFileAccess.NEW == document.getId()) {
            /*
             * create new file
             */
            FolderID targetFolderID;
            {
                String folderId = document.getFolderId();
                if (null == folderId) {
                    throw FileStorageExceptionCodes.INVALID_FOLDER_IDENTIFIER.create("null");
                }
                targetFolderID = new FolderID(folderId);
            }

            document.setFolderId(targetFolderID.getFolderId());
            FileStorageFileAccess fileAccess = getFileAccess(targetFolderID.getService(), targetFolderID.getAccountId());
            saveDelegation.call(fileAccess);
            FileID newID = new FileID(
                targetFolderID.getService(),
                targetFolderID.getAccountId(),
                targetFolderID.getFolderId(),
                document.getId());
            document.setId(newID.toUniqueID());
            document.setFolderId(targetFolderID.toUniqueID());

            postEvent(FileStorageEventHelper.buildCreateEvent(
                session,
                newID.getService(),
                newID.getAccountId(),
                targetFolderID.toUniqueID(),
                newID.toUniqueID(),
                document.getFileName()));
        } else {
            /*
             * update existing file
             */
            FileID sourceFileID = new FileID(document.getId());
            if (null == sourceFileID.getFolderId()) {
                // preserve folder information also for infostore items
                sourceFileID.setFolderId(document.getFolderId());
            }
            final FolderID targetFolderID;
            if (null == document.getFolderId()) {
                targetFolderID = new FolderID(sourceFileID.getService(), sourceFileID.getAccountId(), sourceFileID.getFolderId());
            } else {
                targetFolderID = new FolderID(document.getFolderId());
                if (false == sourceFileID.getService().equals(targetFolderID.getService()) || false == sourceFileID.getAccountId().equals(
                    targetFolderID.getAccountId())) {
                    /*
                     * special handling for move between storages
                     */
                    move(document, data, sequenceNumber, modifiedColumns);
                    return;
                }
            }
            FileStorageFileAccess fileAccess = getFileAccess(sourceFileID.getService(), sourceFileID.getAccountId());
            final IDTuple sourceIDTuple = new IDTuple(sourceFileID.getFolderId(), sourceFileID.getFileId());
            ensureFolderIDs(fileAccess, Collections.singletonList(sourceIDTuple));
            if (null != document.getFolderId() && false == sourceIDTuple.getFolder().equals(targetFolderID.getFolderId())) {
                /*
                 * special handling for move to different folder
                 */
                FolderID sourceFolderID = new FolderID(sourceFileID.getService(), sourceFileID.getAccountId(), sourceIDTuple.getFolder());
                document.setFolderId(sourceIDTuple.getFolder());
                document.setId(sourceIDTuple.getId());
                final TransactionAwareFileAccessDelegation<IDTuple> moveDelegation = new TransactionAwareFileAccessDelegation<IDTuple>() {

                    @Override
                    protected IDTuple callInTransaction(final FileStorageFileAccess access) throws OXException {
                        return access.move(sourceIDTuple, targetFolderID.getFolderId(), sequenceNumber, document, modifiedColumns);
                    }
                };
                IDTuple newID = moveDelegation.call(fileAccess);

                FileID newFileID = new FileID(sourceFileID.getService(), sourceFileID.getAccountId(), newID.getFolder(), newID.getId());
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
                    targetFolderID.toUniqueID(),
                    newFileID.toUniqueID(),
                    document.getFileName()));
            } else {
                /*
                 * update without move
                 */
                document.setFolderId(targetFolderID.getFolderId());
                document.setId(sourceFileID.getFileId());
                saveDelegation.call(getFileAccess(targetFolderID.getService(), targetFolderID.getAccountId()));
                FileID newID = new FileID(
                    targetFolderID.getService(),
                    targetFolderID.getAccountId(),
                    targetFolderID.getFolderId(),
                    document.getId());
                document.setId(newID.toUniqueID());
                document.setFolderId(targetFolderID.toUniqueID());
                postEvent(FileStorageEventHelper.buildUpdateEvent(
                    session,
                    newID.getService(),
                    newID.getAccountId(),
                    targetFolderID.toUniqueID(),
                    newID.toUniqueID(),
                    document.getFileName()));
            }
        }
    }

    protected void move(final File document, InputStream data, final long sequenceNumber, final List<Field> modifiedColumns) throws OXException {
        final FileID id = new FileID(document.getId()); // signifies the source
        final FolderID folderId = new FolderID(document.getFolderId()); // signifies the destination

        final boolean partialUpdate = modifiedColumns != null && !modifiedColumns.isEmpty();
        final FileStorageFileAccess destAccess = getFileAccess(folderId.getService(), folderId.getAccountId());
        final FileStorageFileAccess sourceAccess = getFileAccess(id.getService(), id.getAccountId());

        document.setId(FileStorageFileAccess.NEW);
        document.setFolderId(folderId.getFolderId());

        if (data == null) {
            data = sourceAccess.getDocument(id.getFolderId(), id.getFileId(), FileStorageFileAccess.CURRENT_VERSION);
        }

        if (partialUpdate) {
            final File original = sourceAccess.getFileMetadata(id.getFolderId(), id.getFileId(), FileStorageFileAccess.CURRENT_VERSION);
            final Set<Field> fieldsToSkip = new HashSet<Field>(modifiedColumns);
            fieldsToSkip.add(Field.FOLDER_ID);
            fieldsToSkip.add(Field.ID);
            fieldsToSkip.add(Field.LAST_MODIFIED);
            fieldsToSkip.add(Field.CREATED);
            fieldsToSkip.add(Field.VERSION);
            fieldsToSkip.add(Field.VERSION_COMMENT);

            final Set<Field> toCopy = EnumSet.complementOf(EnumSet.copyOf(fieldsToSkip));

            document.copyFrom(original, toCopy.toArray(new File.Field[toCopy.size()]));

        }

        {
            final InputStream in = data;
            final TransactionAwareFileAccessDelegation<Void> saveDocumentDelegation = new TransactionAwareFileAccessDelegation<Void>() {

                @Override
                protected Void callInTransaction(final FileStorageFileAccess access) throws OXException {
                    access.saveDocument(document, in, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
                    return null;
                }

            };
            saveDocumentDelegation.call(destAccess);
        }

        FileID newId = new FileID(folderId.getService(), folderId.getAccountId(), document.getFolderId(), document.getId());
        document.setId(newId.toUniqueID());
        document.setFolderId(new FolderID(folderId.getService(), folderId.getAccountId(), document.getFolderId()).toUniqueID());

        {
            final TransactionAwareFileAccessDelegation<Void> removeDocumentDelegation = new TransactionAwareFileAccessDelegation<Void>() {

                @Override
                protected Void callInTransaction(FileStorageFileAccess access) throws OXException {
                    access.removeDocument(Arrays.asList(new FileStorageFileAccess.IDTuple(id.getFolderId(), id.getFileId())), sequenceNumber, true);
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
            folderId.getService(),
            folderId.getAccountId(),
            folderId.toUniqueID(),
            newId.toUniqueID(),
            document.getFileName());
        postEvent(deleteEvent);
        postEvent(createEvent);
    }

    @Override
    public void saveDocument(final File document, final InputStream data, final long sequenceNumber) throws OXException {
        save(document, data, sequenceNumber, null, new TransactionAwareFileAccessDelegation<Void>() {

            @Override
            protected Void callInTransaction(final FileStorageFileAccess access) throws OXException {
                List<FileStorageGuestObjectPermission> guestPermissions = stripGuestPermissions(document);
                access.saveDocument(document, data, sequenceNumber);
                if (handleNewGuestPermissions(document, guestPermissions)) {
                    access.saveFileMetadata(document, document.getSequenceNumber(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
                }
                return null;
            }

        });
    }

    @Override
    public void saveDocument(final File document, final InputStream data, final long sequenceNumber, final List<Field> modifiedColumns) throws OXException {
        save(document, data, sequenceNumber, modifiedColumns, new TransactionAwareFileAccessDelegation<Void>() {

            @Override
            protected Void callInTransaction(final FileStorageFileAccess access) throws OXException {
                List<FileStorageGuestObjectPermission> guestPermissions = stripGuestPermissions(document);
                access.saveDocument(document, data, sequenceNumber, modifiedColumns);
                if (handleNewGuestPermissions(document, guestPermissions)) {
                    access.saveFileMetadata(document, document.getSequenceNumber(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
                }
                return null;
            }

        });
    }

    @Override
    public void saveDocument(final File document, final InputStream data, final long sequenceNumber, final List<Field> modifiedColumns, final boolean ignoreVersion) throws OXException {
        save(document, data, sequenceNumber, modifiedColumns, new TransactionAwareFileAccessDelegation<Void>() {

            @Override
            protected Void callInTransaction(final FileStorageFileAccess access) throws OXException {
                List<FileStorageGuestObjectPermission> guestPermissions = stripGuestPermissions(document);
                if (access instanceof FileStorageIgnorableVersionFileAccess) {
                    ((FileStorageIgnorableVersionFileAccess) access).saveDocument(
                        document,
                        data,
                        sequenceNumber,
                        modifiedColumns,
                        ignoreVersion);
                } else {
                    access.saveDocument(document, data, sequenceNumber, modifiedColumns);
                }
                if (handleNewGuestPermissions(document, guestPermissions)) {
                    access.saveFileMetadata(document, document.getSequenceNumber(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
                }
                return null;
            }

        });
    }

    @Override
    public InputStream getDocument(String id, String version, long offset, long length) throws OXException {
        FileID fileID = new FileID(id);
        FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
        if (false == FileStorageRandomFileAccess.class.isInstance(fileAccess)) {
            throw new UnsupportedOperationException("FileStorageRandomFileAccess required");
        }
        return ((FileStorageRandomFileAccess) fileAccess).getDocument(fileID.getFolderId(), fileID.getFileId(), version, offset, length);
    }

    @Override
    public void saveDocument(final File document, final InputStream data, final long sequenceNumber, final List<Field> modifiedColumns, final long offset) throws OXException {
        save(document, data, sequenceNumber, modifiedColumns, new TransactionAwareFileAccessDelegation<Void>() {

            @Override
            protected Void callInTransaction(FileStorageFileAccess fileAccess) throws OXException {
                List<FileStorageGuestObjectPermission> guestPermissions = stripGuestPermissions(document);
                if (false == FileStorageRandomFileAccess.class.isInstance(fileAccess)) {
                    throw new UnsupportedOperationException("FileStorageRandomFileAccess required");
                }
                ((FileStorageRandomFileAccess) fileAccess).saveDocument(document, data, sequenceNumber, modifiedColumns, offset);
                if (handleNewGuestPermissions(document, guestPermissions)) {
                    fileAccess.saveFileMetadata(document, document.getSequenceNumber(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
                }
                return null;
            }

        });
    }

    @Override
    public boolean supportsIgnorableVersion(final String serviceId, final String accountId) throws OXException {
        return (getFileAccess(serviceId, accountId) instanceof FileStorageIgnorableVersionFileAccess);
    }

    @Override
    public boolean supportsRandomFileAccess(final String serviceId, final String accountId) throws OXException {
        return FileStorageRandomFileAccess.class.isInstance(getFileAccess(serviceId, accountId));
    }

    @Override
    public void saveFileMetadata(final File document, final long sequenceNumber) throws OXException {
        save(document, null, sequenceNumber, null, new TransactionAwareFileAccessDelegation<Void>() {

            @Override
            protected Void callInTransaction(final FileStorageFileAccess access) throws OXException {
                List<FileStorageGuestObjectPermission> guestPermissions = stripGuestPermissions(document);
                access.saveFileMetadata(document, sequenceNumber);
                if (handleNewGuestPermissions(document, guestPermissions)) {
                    access.saveFileMetadata(document, document.getSequenceNumber(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
                }
                return null;
            }

        });
    }

    @Override
    public void saveFileMetadata(final File document, final long sequenceNumber, final List<Field> modifiedColumns) throws OXException {
        save(document, null, sequenceNumber, modifiedColumns, new TransactionAwareFileAccessDelegation<Void>() {

            @Override
            protected Void callInTransaction(final FileStorageFileAccess access) throws OXException {
                List<FileStorageGuestObjectPermission> guestPermissions = stripGuestPermissions(document);
                access.saveFileMetadata(document, sequenceNumber, modifiedColumns);
                if (handleNewGuestPermissions(document, guestPermissions)) {
                    access.saveFileMetadata(document, document.getSequenceNumber(), Collections.singletonList(Field.OBJECT_PERMISSIONS));
                }
                return null;
            }

        });
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
        File fileMetadata = new DefaultFile(getFileMetadata(sourceId, version));
        if (update != null) {
            fileMetadata.copyFrom(update, fields.toArray(new File.Field[fields.size()]));
        }

        if (newData == null) {
            newData = getDocument(sourceId, version);
        }

        fileMetadata.setId(FileStorageFileAccess.NEW);
        fileMetadata.setFolderId(destFolderId);

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
            SearchIterator<File> searchIterator = entry.getKey().search(entry.getValue(), searchTerm, fields, sort, order, start, end);
            if (null == searchIterator) {
                return SearchIteratorAdapter.emptyIterator();
            }
            FileStorageAccountAccess accountAccess = entry.getKey().getAccountAccess();
            return fixIDs(searchIterator, accountAccess.getService().getId(), accountAccess.getAccountId());
        } else {
            /*
             * schedule tasks to perform search in multiple storages concurrently
             */
            ThreadPoolService threadPool = ThreadPools.getThreadPool();
            CompletionService<SearchIterator<File>> completionService = null != threadPool ?
                new ThreadPoolCompletionService<SearchIterator<File>>(threadPool) : new CallerRunsCompletionService<SearchIterator<File>>();
            for (final Entry<FileStorageFileAccess, List<String>> entry : foldersByFileAccess.entrySet()) {
                completionService.submit(getSearchCallable(entry.getKey(), entry.getValue(), searchTerm, fields, sort, order));
            }
            /*
             * collect & filter results
             */
            return new FilteringSearchIterator<File>(collectSearchResults(completionService, foldersByFileAccess.size(), sort, order)) {

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
    public SearchIterator<File> search(final String query, final List<Field> columns, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
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
            final SearchIterator<File> result = files.search(query, cols, folderId, sort, order, start, end);
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
        Collections.sort(all, infostoreFirstFileAccessComparator);
        LinkedList<Future<SearchIterator<File>>> tasks = new LinkedList<Future<SearchIterator<File>>>();
        ThreadPoolService threadPool = ThreadPools.getThreadPool();
        for (int i = 0; i < numOfStorages; i++) {
            final FileStorageFileAccess files = all.get(i);
            tasks.add(threadPool.submit(new AbstractTask<SearchIterator<File>>() {

                @Override
                public SearchIterator<File> call() {
                    try {
                        final SearchIterator<File> result = files.search(query, cols, folderId, sort, order, start, end);
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

        return new MergingSearchIterator<File>(order.comparatorBy(sort), new LinkedList<SearchIterator<File>>(results));
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

    @Override
    public void unlock(final String id) throws OXException {
        final FileID fileID = new FileID(id);
        final TransactionAwareFileAccessDelegation<Void> unlockDelegation = new TransactionAwareFileAccessDelegation<Void>() {

            @Override
            protected Void callInTransaction(FileStorageFileAccess access) throws OXException {
                access.unlock(fileID.getFolderId(), fileID.getFileId());
                return null;
            }
        };
        unlockDelegation.call(getFileAccess(fileID.getService(), fileID.getAccountId()));
    }

    protected List<File.Field> addIDColumns(List<File.Field> columns) {
        final boolean hasID = columns.contains(File.Field.ID);
        final boolean hasFolder = columns.contains(File.Field.FOLDER_ID);
        final boolean hasLastModified = columns.contains(File.Field.LAST_MODIFIED);

        if (hasID && hasFolder && hasLastModified) {
            return columns;
        }

        columns = new ArrayList<File.Field>(columns);

        if (!hasID) {
            columns.add(File.Field.ID);
        }

        if (!hasFolder) {
            columns.add(File.Field.FOLDER_ID);
        }

        if (!hasLastModified) {
            columns.add(File.Field.LAST_MODIFIED);
        }

        return columns;

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
        for (String id : ids) {
            FileID fileID = new FileID(id);
            FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
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
     * Gets the file access.
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @return The file access
     * @throws OXException If an error occurs
     */
    protected FileStorageFileAccess getFileAccess(final String serviceId, final String accountId) throws OXException {
        // Special handling for InfoStore
        if (INFOSTORE_SERVICE_ID.equals(serviceId)) {
            return getInfoStoreService().getAccountAccess(accountId, session).getFileAccess();
        }

        // Others...
        Map<String, FileStorageAccountAccess> connectedAccounts = this.connectedAccounts.get();
        if (null == connectedAccounts) {
            connectedAccounts = new HashMap<String, FileStorageAccountAccess>();
            this.connectedAccounts.set(connectedAccounts);
        }
        final FileStorageAccountAccess cached = connectedAccounts.get(new StringBuilder(serviceId).append('/').append(accountId).toString());
        if (cached != null) {
            return cached.getFileAccess();
        }
        final FileStorageService fileStorage = getFileStorageService(serviceId);

        final FileStorageAccountAccess accountAccess = fileStorage.getAccountAccess(accountId, session);
        connect(accountAccess);
        return accountAccess.getFileAccess();
    }

    /**
     * Gets the folder access.
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @return The folder access
     * @throws OXException If an error occurs
     */
    protected FileStorageFolderAccess getFolderAccess(final String serviceId, final String accountId) throws OXException {
        // Special handling for InfoStore
        if (INFOSTORE_SERVICE_ID.equals(serviceId)) {
            return getInfoStoreService().getAccountAccess(accountId, session).getFolderAccess();
        }

        // Others...
        final FileStorageAccountAccess cached = connectedAccounts.get().get(serviceId + "/" + accountId);
        if (cached != null) {
            return cached.getFolderAccess();
        }
        final FileStorageService fileStorage = getFileStorageService(serviceId);

        final FileStorageAccountAccess accountAccess = fileStorage.getAccountAccess(accountId, session);
        connect(accountAccess);
        return accountAccess.getFolderAccess();
    }

    private void connect(final FileStorageAccountAccess accountAccess) throws OXException {
        final String id = accountAccess.getService().getId() + "/" + accountAccess.getAccountId();

        Map<String, FileStorageAccountAccess> connectedAccounts = this.connectedAccounts.get();
        if (!connectedAccounts.containsKey(id)) {
            connectedAccounts.put(id, accountAccess);
            accountAccess.connect();
            List<FileStorageAccountAccess> accessesToClose = this.accessesToClose.get();
            if (null == accessesToClose) {
                accessesToClose = new LinkedList<FileStorageAccountAccess>();
                this.accessesToClose.set(accessesToClose);
            }
            accessesToClose.add(accountAccess);
        }
    }

    protected List<FileStorageFileAccess> getAllFileStorageAccesses() throws OXException {
        final List<FileStorageService> allFileStorageServices = getAllFileStorageServices();
        final List<FileStorageFileAccess> retval = new ArrayList<FileStorageFileAccess>(allFileStorageServices.size());
        for (final FileStorageService fsService : allFileStorageServices) {
            List<FileStorageAccount> accounts = null;
            if (fsService instanceof AccountAware) {
                accounts = ((AccountAware) fsService).getAccounts(session);
            }
            if (null == accounts) {
                accounts = fsService.getAccountManager().getAccounts(session);
            }
            for (final FileStorageAccount fileStorageAccount : accounts) {
                final FileStorageAccountAccess accountAccess = fsService.getAccountAccess(fileStorageAccount.getId(), session);
                connect(accountAccess);
                retval.add(accountAccess.getFileAccess());
            }
        }
        return retval;
    }

    /**
     * Gets the special InfoStore service.
     *
     * @return The special InfoStore service
     * @throws OXException If special InfoStore cannot be returned
     */
    protected FileStorageService getInfoStoreService() throws OXException {
        FileStorageService infstoreService = INFOSTORE_SERVICE_REF.get();
        if (null == infstoreService) {
            infstoreService = Services.getService(FileStorageServiceRegistry.class).getFileStorageService(INFOSTORE_SERVICE_ID);
            if (null == infstoreService) {
                throw FileStorageExceptionCodes.UNKNOWN_FILE_STORAGE_SERVICE.create(INFOSTORE_SERVICE_ID);
            }
            INFOSTORE_SERVICE_REF.set(infstoreService);
        }
        return infstoreService;
    }

    protected abstract FileStorageService getFileStorageService(String serviceId) throws OXException;

    protected abstract List<FileStorageService> getAllFileStorageServices() throws OXException;

    protected abstract EventAdmin getEventAdmin();

    // Transaction Handling

    @Override
    protected void commit(final Transaction transaction) throws TransactionException {
        // Nothing
    }

    @Override
    protected Transaction createTransaction() throws TransactionException {
        return null;
    }

    @Override
    protected void rollback(final Transaction transaction) throws TransactionException {
        // Nothing
    }

    @Override
    public void setCommitsTransaction(final boolean commits) {
        // Nothing
    }

    @Override
    public void setRequestTransactional(final boolean transactional) {
        // Nothing
    }

    @Override
    public void setTransactional(final boolean transactional) {
        // Nothing
    }

    @Override
    public void startTransaction() throws TransactionException {
        super.startTransaction();
        Map<String, FileStorageAccountAccess> connectedAccounts = this.connectedAccounts.get();
        if (null != connectedAccounts) {
            connectedAccounts.clear();
        }
        List<FileStorageAccountAccess> accessesToClose = this.accessesToClose.get();
        if (null != accessesToClose) {
            accessesToClose.clear();
        }
    }

    @Override
    public void finish() throws TransactionException {
        Map<String, FileStorageAccountAccess> connectedAccounts = this.connectedAccounts.get();
        if (null != connectedAccounts) {
            connectedAccounts.clear();
        }
        List<FileStorageAccountAccess> accessesToClose = this.accessesToClose.get();
        if (null != accessesToClose) {
            for (final FileStorageAccountAccess acc : accessesToClose) {
                acc.close();
            }
            accessesToClose.clear();
        }
        super.finish();
    }

    private static void checkPatternLength(final String pattern) throws OXException {
        final ConfigurationService configurationService = Services.optService(ConfigurationService.class);
        final int minimumSearchCharacters = null == configurationService ? 0 : configurationService.getIntProperty("com.openexchange.MinimumSearchCharacters", 0);
        if (minimumSearchCharacters <= 0) {
            return;
        }
        if (null != pattern && 0 != pattern.length() && com.openexchange.java.SearchStrings.lengthWithoutWildcards(pattern) < minimumSearchCharacters) {
            throw FileStorageExceptionCodes.PATTERN_NEEDS_MORE_CHARACTERS.create(I(minimumSearchCharacters));
        }
    }

    /**
     * Processes the list of supplied ID tuples to ensure that each entry has an assigned folder ID.
     *
     * @param access The file access to query if folder IDs are missing
     * @param idTuples The ID tuples to process
     * @return The ID tuples, with each entry holding its full file- and folder-ID information
     * @throws OXException
     */
    //TODO: This is weird. The client already sends fileID:folderID pairs, though they get stripped for the infostore currently
    //      when generating the corresponding com.openexchange.file.storage.composition.FileID.
    private static List<IDTuple> ensureFolderIDs(FileStorageFileAccess access, List<IDTuple> idTuples) throws OXException {
        if (null == idTuples || 0 == idTuples.size()) {
            return idTuples;
        }
        List<IDTuple> incompleteTuples = new ArrayList<FileStorageFileAccess.IDTuple>();
        for (IDTuple tuple : idTuples) {
            if (null == tuple.getFolder()) {
                incompleteTuples.add(tuple);
            }
        }
        if (0 < incompleteTuples.size()) {
            SearchIterator<File> searchIterator = null;
            try {
                searchIterator = access.getDocuments(
                    incompleteTuples, Arrays.asList(new Field[] { Field.ID, Field.FOLDER_ID })).results();
                for (int i = 0; i < incompleteTuples.size() && searchIterator.hasNext(); i++) {
                    File file = searchIterator.next();
                    incompleteTuples.get(i).setFolder(file.getFolderId());
                }
            } finally {
                if (null != searchIterator) {
                    searchIterator.close();
                }
            }
        }
        return idTuples;
    }

    private EventProperty extractRemoteAddress() {
        Object serverName = LogProperties.get(LogProperties.Name.GRIZZLY_REMOTE_ADDRESS);
        if (null == serverName) {
            serverName = LogProperties.get(LogProperties.Name.AJP_REMOTE_ADDRESS);
        }
        if (null != serverName) {
            return new EventProperty("remoteAddress", serverName.toString());
        }
        return null;
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
     * Collects and merges multiple search results from the supplied completion service.
     *
     * @param completionService The completion service to take the results from
     * @param count The number of results to take
     * @param sort The field to sort
     * @param order The sort orde
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
        return new MergingSearchIterator<File>(order.comparatorBy(sort), searchIterators);
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
    private static Callable<SearchIterator<File>> getSearchCallable(final FileStorageFileAccess fileAccess, final List<String> folderIds, final SearchTerm<?> searchTerm, final List<Field> fields, final Field sort, final SortDirection order) {
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

}
