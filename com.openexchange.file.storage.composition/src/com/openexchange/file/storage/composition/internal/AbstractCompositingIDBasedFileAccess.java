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

package com.openexchange.file.storage.composition.internal;

import static com.openexchange.file.storage.composition.internal.IDManglingFileCustomizer.fixIDs;
import gnu.trove.ConcurrentTIntObjectHashMap;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageEventHelper;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageIgnorableVersionFileAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FileStreamHandler;
import com.openexchange.file.storage.composition.FileStreamHandlerRegistry;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedIgnorableVersionFileAccess;
import com.openexchange.groupware.results.AbstractTimedResult;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.java.CallerRunsCompletionService;
import com.openexchange.java.StringAllocator;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.iterator.MergingSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tx.AbstractService;
import com.openexchange.tx.TransactionException;

/**
 * {@link AbstractCompositingIDBasedFileAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractCompositingIDBasedFileAccess extends AbstractService<Transaction> implements IDBasedIgnorableVersionFileAccess {

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

    protected Session session;

    private final ThreadLocal<Map<String, FileStorageAccountAccess>> connectedAccounts = new ThreadLocal<Map<String, FileStorageAccountAccess>>();
    private final ThreadLocal<List<FileStorageAccountAccess>> accessesToClose = new ThreadLocal<List<FileStorageAccountAccess>>();

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
    public InputStream getDocument(final String id, final String version) throws OXException {
        final FileID fileID = new FileID(id);
        final FileStreamHandlerRegistry registry = getStreamHandlerRegistry();
        if (null == registry) {
            return getFileAccess(fileID.getService(), fileID.getAccountId()).getDocument(fileID.getFolderId(), fileID.getFileId(), version);
        }
        final Collection<FileStreamHandler> handlers = registry.getHandlers();
        if (null == handlers || handlers.isEmpty()) {
            return getFileAccess(fileID.getService(), fileID.getAccountId()).getDocument(fileID.getFolderId(), fileID.getFileId(), version);
        }
        // Handle stream
        InputStream inputStream = getFileAccess(fileID.getService(), fileID.getAccountId()).getDocument(fileID.getFolderId(), fileID.getFileId(), version);
        for (final FileStreamHandler streamHandler : handlers) {
            inputStream = streamHandler.handleDocumentStream(inputStream, fileID, version, session.getContextId());
        }
        return inputStream;
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId) throws OXException {
        final FolderID folderID = new FolderID(folderId);
        final TimedResult<File> result = getFileAccess(folderID.getService(), folderID.getAccountId()).getDocuments(folderID.getFolderId());
        return fixIDs(result, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> columns) throws OXException {
        final FolderID folderID = new FolderID(folderId);
        return getFileAccess(folderID.getService(), folderID.getAccountId()).getDocuments(folderID.getFolderId(), addIDColumns(columns));
    }

    @Override
    public TimedResult<File> getDocuments(final String folderId, final List<Field> columns, final Field sort, final SortDirection order) throws OXException {
        final FolderID folderID = new FolderID(folderId);
        final TimedResult<File> result = getFileAccess(folderID.getService(), folderID.getAccountId()).getDocuments(
            folderID.getFolderId(),
            addIDColumns(columns),
            sort,
            order);
        return fixIDs(result, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public TimedResult<File> getDocuments(final List<String> ids, final List<Field> columns) throws OXException {
        final List<File> files = new ArrayList<File>(ids.size());
        for (final String id : ids) {
            if(exists(id, FileStorageFileAccess.CURRENT_VERSION)) {
                final File fileMetadata = getFileMetadata(id, FileStorageFileAccess.CURRENT_VERSION);
                files.add(fileMetadata);
            }
        }

        return new AbstractTimedResult<File>(new SearchIteratorAdapter<File>(files.iterator())) {

            @Override
            protected long extractTimestamp(final File object) {
                return object.getSequenceNumber();
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
        getFileAccess(fileID.getService(), fileID.getAccountId()).lock(fileID.getFolderId(), fileID.getFileId(), diff);
    }

    @Override
    public void removeDocument(final String folderId, final long sequenceNumber) throws OXException {
        final FolderID id = new FolderID(folderId);

        getFileAccess(id.getService(), id.getAccountId()).removeDocument(id.getFolderId(), sequenceNumber);
        // TODO: Does this method really make sense? Skipping possible delete event.
    }

    @Override
    public List<String> removeDocument(final List<String> ids, final long sequenceNumber) throws OXException {
        final Map<FileStorageFileAccess, List<IDTuple>> deleteOperations = new HashMap<FileStorageFileAccess, List<IDTuple>>();
        final List<File> reloaded = new ArrayList<File>();
        for (final String id : ids) {
            final FileID fileID = new FileID(id);
            final FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
            List<IDTuple> deletes = deleteOperations.get(fileAccess);
            if (deletes == null) {
                deletes = new ArrayList<IDTuple>();
                deleteOperations.put(fileAccess, deletes);
            }

            deletes.add(new FileStorageFileAccess.IDTuple(fileID.getFolderId(), fileID.getFileId()));
            TimedResult<File> documents = fileAccess.getDocuments(deletes, Arrays.asList(new Field[] { Field.ID, Field.FOLDER_ID }));
            if (documents != null) {
                SearchIterator<File> it = documents.results();
                while (it.hasNext()) {
                    File file = it.next();
                    reloaded.add(file);
                }
            }
        }


        final List<String> notDeleted = new ArrayList<String>(ids.size());
        for (final Map.Entry<FileStorageFileAccess, List<IDTuple>> deleteOp : deleteOperations.entrySet()) {
            final FileStorageFileAccess access = deleteOp.getKey();
            final List<IDTuple> toDelete = deleteOp.getValue();
            final List<IDTuple> conflicted = access.removeDocument(toDelete, sequenceNumber);
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
                String fileFolder = tuple.getFolder();
                String id = tuple.getId();
                if (fileFolder == null) {
                    /*
                     * Reload the document to get it's folder id.
                     */
                    for (File file : reloaded) {
                        if (file.getId().equals(id)) {
                            fileFolder = file.getFolderId();
                        }
                    }
                } else {

                }
                String folderId = new FolderID(serviceId, accountId, fileFolder).toUniqueID();
                String objectId = new FileID(serviceId, accountId, fileFolder, id).toUniqueID();
                postEvent(FileStorageEventHelper.buildDeleteEvent(session, serviceId, accountId, folderId, objectId, null));
            }
        }
        return notDeleted;
    }

    @Override
    public String[] removeVersion(final String id, final String[] versions) throws OXException {
        FileID fileID = new FileID(id);
        FileStorageFileAccess access = getFileAccess(fileID.getService(), fileID.getAccountId());
        String[] notRemoved = access.removeVersion(fileID.getFolderId(), fileID.getFileId(), versions);

        String serviceId = access.getAccountAccess().getService().getId();
        String accountId = access.getAccountAccess().getAccountId();
        Set<String> removed = new HashSet<String>(versions.length);
        for (String i : versions) {
            removed.add(i);
        }

        for (String i : notRemoved) {
            removed.remove(i);
        }

        String objectId = fileID.getFileId();
        FolderID folderID;
        String fileFolder = fileID.getFolderId();
        if (fileFolder == null) {
            /*
             * Reload the document to get it's folder id.
             */
            File fileMetadata = access.getFileMetadata(fileFolder, objectId, FileStorageFileAccess.CURRENT_VERSION);
            folderID = new FolderID(serviceId, accountId, fileMetadata.getFolderId());
        } else {
            folderID = new FolderID(serviceId, accountId, fileFolder);
        }

        postEvent(FileStorageEventHelper.buildDeleteEvent(session, serviceId, accountId, folderID.toUniqueID(), fileID.toUniqueID(), removed));
        return notRemoved;
    }

    private static interface FileAccessDelegation {

        public void call(FileStorageFileAccess access) throws OXException;

    }

    protected void save(final File document, final InputStream data, final long sequenceNumber, final List<Field> modifiedColumns, final FileAccessDelegation delegation) throws OXException {
        String id = document.getId();
        FolderID folderID = null;
        if(document.getFolderId() != null) {
            folderID = new FolderID(document.getFolderId());
        }

        Event event = null;
        if (id != FileStorageFileAccess.NEW) {
            FileID fileID = new FileID(id);
            if(folderID == null) {
                folderID = new FolderID(fileID.getService(), fileID.getAccountId(), fileID.getFolderId());
            }
            if(!(fileID.getService().equals(folderID.getService())) || !(fileID.getAccountId().equals(folderID.getAccountId()))) {
                move(document, data, sequenceNumber, modifiedColumns);
                return;
            }

            document.setId(fileID.getFileId());
            event = FileStorageEventHelper.buildUpdateEvent(session, fileID.getService(), fileID.getAccountId(), folderID.toUniqueID(), fileID.toUniqueID());
        }

        document.setFolderId(folderID.getFolderId());
        FileStorageFileAccess fileAccess = getFileAccess(folderID.getService(), folderID.getAccountId());
        FolderID srcFolder = null;
        if (id != null) {
            File fileMetadata = fileAccess.getFileMetadata(folderID.getFolderId(), new FileID(id).getFileId(), FileStorageFileAccess.CURRENT_VERSION);
            srcFolder = new FolderID(folderID.getService(), folderID.getAccountId(), fileMetadata.getFolderId());
        }
        delegation.call(fileAccess);

        FileID fileID = new FileID(folderID.getService(), folderID.getAccountId(), document.getFolderId(), document.getId());
        document.setId(fileID.toUniqueID());
        document.setFolderId(new FolderID(folderID.getService(), folderID.getAccountId(), document.getFolderId()).toUniqueID());
        if (event == null) {
            event = FileStorageEventHelper.buildCreateEvent(session, fileID.getService(), fileID.getAccountId(), folderID.toUniqueID(), fileID.toUniqueID());
        } else if (modifiedColumns != null && modifiedColumns.contains(Field.FOLDER_ID)) {
            event = FileStorageEventHelper.buildCreateEvent(session, fileID.getService(), fileID.getAccountId(), folderID.toUniqueID(), fileID.toUniqueID());
            postEvent(FileStorageEventHelper.buildDeleteEvent(session, fileID.getService(), fileID.getAccountId(), srcFolder.toUniqueID(), fileID.toUniqueID(), null));
        }

        postEvent(event);
    }

    protected void move(final File document, InputStream data, final long sequenceNumber, final List<Field> modifiedColumns) throws OXException {
        final FileID id = new FileID(document.getId()); // signifies the source
        final FolderID folderId = new FolderID(document.getFolderId()); // signifies the destination

        final boolean partialUpdate = modifiedColumns != null && !modifiedColumns.isEmpty();
        final boolean hasUpload = data != null;

        final FileStorageFileAccess destAccess = getFileAccess(folderId.getService(), folderId.getAccountId());
        final FileStorageFileAccess sourceAccess = getFileAccess(id.getService(), id.getAccountId());

        document.setId(FileStorageFileAccess.NEW);
        document.setFolderId(folderId.getFolderId());

        if (!hasUpload) {
            data = sourceAccess.getDocument(id.getFolderId(), id.getFileId(), FileStorageFileAccess.CURRENT_VERSION);
        }

        if (partialUpdate) {
            final File original = sourceAccess.getFileMetadata(id.getFolderId(), id.getFileId(), FileStorageFileAccess.CURRENT_VERSION);
            final Set<Field> fieldsToSkip = new HashSet<Field>(modifiedColumns);
            fieldsToSkip.add(Field.FOLDER_ID);
            fieldsToSkip.add(Field.ID);
            fieldsToSkip.add(Field.LAST_MODIFIED);
            fieldsToSkip.add(Field.CREATED);

            final Set<Field> toCopy = EnumSet.complementOf(EnumSet.copyOf(fieldsToSkip));

            document.copyFrom(original, toCopy.toArray(new File.Field[toCopy.size()]));

        }

        destAccess.saveDocument(document, data, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
        FileID newId = new FileID(folderId.getService(), folderId.getAccountId(), document.getFolderId(), document.getId());
        document.setId(newId.toUniqueID());
        document.setFolderId(new FolderID(folderId.getService(), folderId.getAccountId(), document.getFolderId()).toUniqueID());

        sourceAccess.removeDocument(Arrays.asList(new FileStorageFileAccess.IDTuple(id.getFolderId(), id.getFileId())), sequenceNumber);

        Event deleteEvent = FileStorageEventHelper.buildDeleteEvent(session, id.getService(), id.getAccountId(), new FolderID(id.getService(), id.getAccountId(), id.getFolderId()).toUniqueID(), id.toUniqueID(), null);
        Event createEvent = FileStorageEventHelper.buildCreateEvent(session, folderId.getService(), folderId.getAccountId(), folderId.toUniqueID(), newId.toUniqueID());
        postEvent(deleteEvent);
        postEvent(createEvent);
    }

    @Override
    public void saveDocument(final File document, final InputStream data, final long sequenceNumber) throws OXException {
        save(document, data, sequenceNumber, null, new FileAccessDelegation() {

            @Override
            public void call(final FileStorageFileAccess access) throws OXException {
                access.saveDocument(document, data, sequenceNumber);
            }

        });
    }

    @Override
    public void saveDocument(final File document, final InputStream data, final long sequenceNumber, final List<Field> modifiedColumns) throws OXException {
        save(document, data, sequenceNumber, modifiedColumns, new FileAccessDelegation() {

            @Override
            public void call(final FileStorageFileAccess access) throws OXException {
                access.saveDocument(document, data, sequenceNumber, modifiedColumns);
            }

        });
    }

    @Override
    public void saveDocument(final File document, final InputStream data, final long sequenceNumber, final List<Field> modifiedColumns, final boolean ignoreVersion) throws OXException {
        save(document, data, sequenceNumber, modifiedColumns, new FileAccessDelegation() {

            @Override
            public void call(final FileStorageFileAccess access) throws OXException {
                if (access instanceof FileStorageIgnorableVersionFileAccess) {
                    ((FileStorageIgnorableVersionFileAccess) access).saveDocument(document, data, sequenceNumber, modifiedColumns, ignoreVersion);
                } else {
                    access.saveDocument(document, data, sequenceNumber, modifiedColumns);
                }
            }

        });
    }

    @Override
    public boolean supportsIgnorableVersion(final String serviceId, final String accountId) throws OXException {
        return (getFileAccess(serviceId, accountId) instanceof FileStorageIgnorableVersionFileAccess);
    }

    @Override
    public void saveFileMetadata(final File document, final long sequenceNumber) throws OXException {
        save(document, null, sequenceNumber, null, new FileAccessDelegation() {

            @Override
            public void call(final FileStorageFileAccess access) throws OXException {
                access.saveFileMetadata(document, sequenceNumber);
            }

        });
    }

    @Override
    public void saveFileMetadata(final File document, final long sequenceNumber, final List<Field> modifiedColumns) throws OXException {
        save(document, null, sequenceNumber, modifiedColumns, new FileAccessDelegation() {

            @Override
            public void call(final FileStorageFileAccess access) throws OXException {
                access.saveFileMetadata(document, sequenceNumber, modifiedColumns);
            }

        });
    }

    @Override
    public String copy(final String sourceId, final String destFolderId, final File update, InputStream newData, final List<File.Field> fields) throws OXException {
        final FileID source = new FileID(sourceId);
        FolderID dest = null;

        File fileMetadata = null;
        if(destFolderId != null) {
            dest = new FolderID(destFolderId);
        } else {
            fileMetadata = getFileMetadata(sourceId, FileStorageFileAccess.CURRENT_VERSION);
            dest = new FolderID(fileMetadata.getFolderId());
        }

        if(source.getService().equals(dest.getService()) && source.getAccountId().equals(dest.getAccountId())) {
            final FileStorageFileAccess fileAccess = getFileAccess(source.getService(), source.getAccountId());
            final IDTuple destAddress = fileAccess.copy(new IDTuple(source.getFolderId(), source.getFileId()), dest.getFolderId(), update, newData, fields);
            return new FileID(source.getService(), source.getAccountId(), destAddress.getFolder(), destAddress.getId()).toUniqueID();
        }

        if(fileMetadata == null) {
            fileMetadata = getFileMetadata(sourceId, FileStorageFileAccess.CURRENT_VERSION);
        }

        if(update != null) {
            fileMetadata.copyFrom(update, fields.toArray(new File.Field[fields.size()]));
        }

        if(newData == null) {
            newData = getDocument(sourceId, FileStorageFileAccess.CURRENT_VERSION);
        }

        fileMetadata.setId(FileStorageFileAccess.NEW);
        fileMetadata.setFolderId(destFolderId);

        if(newData == null) {
            saveFileMetadata(fileMetadata, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
        } else {
            saveDocument(fileMetadata, newData, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER);
        }

        return fileMetadata.getId();
    }

    @Override
    public SearchIterator<File> search(final String query, final List<Field> columns, final String folderId, final Field sort, final SortDirection order, final int start, final int end) throws OXException {
        final List<Field> cols = addIDColumns(columns);
        if (FileStorageFileAccess.ALL_FOLDERS != folderId) {
            final FolderID id = new FolderID(folderId);
            return getFileAccess(id.getService(), id.getAccountId()).search(query, cols, id.getFolderId(), sort, order, start, end);
        }
        /*
         * Search in all available folders
         */
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
        final ConcurrentTIntObjectHashMap<SearchIterator<File>> resultMap = new ConcurrentTIntObjectHashMap<SearchIterator<File>>(numOfStorages);
        final CompletionService<Void> completionService;
        {
            final ThreadPoolService threadPool = ThreadPools.getThreadPool();
            if (null == threadPool) {
                completionService = new CallerRunsCompletionService<Void>();
                for (int i = 0; i < numOfStorages; i++) {
                    final FileStorageFileAccess files = all.get(i);
                    final int index = i;
                    completionService.submit(new Callable<Void>() {

                        @Override
                        public Void call() throws Exception {
                            try {
                                final SearchIterator<File> result = files.search(query, cols, folderId, sort, order, start, end);
                                if(result != null) {
                                    final FileStorageAccountAccess accountAccess = files.getAccountAccess();
                                    resultMap.put(index, fixIDs(result, accountAccess.getService().getId(), accountAccess.getAccountId()));
                                }
                            } catch (final Exception e) {
                                // Ignore failed one in composite search results
                            }
                            return null;
                        }
                    });
                }
            } else {
                final ThreadPoolCompletionService<Void> tcompletionService = new ThreadPoolCompletionService<Void>(threadPool);
                for (int i = 0; i < numOfStorages; i++) {
                    final FileStorageFileAccess files = all.get(i);
                    final int index = i;
                    tcompletionService.submit(new Callable<Void>() {

                        @Override
                        public Void call() throws OXException {
                            try {
                                final SearchIterator<File> result = files.search(query, cols, folderId, sort, order, start, end);
                                if(result != null) {
                                    final FileStorageAccountAccess accountAccess = files.getAccountAccess();
                                    resultMap.put(index, fixIDs(result, accountAccess.getService().getId(), accountAccess.getAccountId()));
                                }
                            } catch (final Exception e) {
                                // Ignore failed one in composite search results
                            }
                            return null;
                        }
                    });
                }
                completionService = tcompletionService;
            }
        }
        /*
         * Take from completion service
         */
        for (int i = 0; i < numOfStorages; i++) {
            try {
                completionService.take();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw FileStorageExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        final List<SearchIterator<File>> results = new ArrayList<SearchIterator<File>>(numOfStorages);
        for (int i = 0; i < numOfStorages; i++) {
            final SearchIterator<File> result = resultMap.get(i);
            if (null != result) {
                results.add(result);
            }
        }
        return new MergingSearchIterator<File>(order.comparatorBy(sort), results);
    }

    @Override
    public void touch(final String id) throws OXException {
        final FileID fileID = new FileID(id);
        FileStorageFileAccess fileAccess = getFileAccess(fileID.getService(), fileID.getAccountId());
        fileAccess.touch(fileID.getFolderId(), fileID.getFileId());

        /*
         * Post event
         */
        String fileFolder = fileID.getFolderId();
        FolderID folderID;
        if (fileFolder == null) {
            File metadata = fileAccess.getFileMetadata(null, id, FileStorageFileAccess.CURRENT_VERSION);
            folderID = new FolderID(metadata.getFolderId());
        } else {
            folderID = new FolderID(fileID.getService(), fileID.getAccountId(), fileID.getFolderId());
        }

        Event event = FileStorageEventHelper.buildUpdateEvent(session, fileID.getService(), fileID.getAccountId(), folderID.toUniqueID(), fileID.toUniqueID());
        postEvent(event);
    }

    protected void postEvent(Event event) {
        EventAdmin eventAdmin = getEventAdmin();
        eventAdmin.postEvent(event);
    }

    @Override
    public void unlock(final String id) throws OXException {
        final FileID fileID = new FileID(id);
        getFileAccess(fileID.getService(), fileID.getAccountId()).unlock(fileID.getFolderId(), fileID.getFileId());
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
     * Gets the file access.
     *
     * @param serviceId The service identifier
     * @param accountId The account identifier
     * @return The file access
     * @throws OXException If an error occurs
     */
    protected FileStorageFileAccess getFileAccess(final String serviceId, final String accountId) throws OXException {
        final Map<String, FileStorageAccountAccess> connectedAccounts = this.connectedAccounts.get();
        final FileStorageAccountAccess cached = connectedAccounts.get(new StringAllocator(serviceId).append('/').append(accountId).toString());
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
        final String id = accountAccess.getService().getId()+"/"+accountAccess.getAccountId();


        if(!connectedAccounts.get().containsKey(id)) {
            connectedAccounts.get().put(id, accountAccess);
            accountAccess.connect();
            accessesToClose.get().add(accountAccess);
        }
    }

    protected List<FileStorageFileAccess> getAllFileStorageAccesses() throws OXException {
        final List<FileStorageService> allFileStorageServices = getAllFileStorageServices();
        final List<FileStorageFileAccess> retval = new ArrayList<FileStorageFileAccess>(allFileStorageServices.size());
        for (final FileStorageService fsService : allFileStorageServices) {
            final List<FileStorageAccount> accounts;
            if (fsService instanceof AccountAware) {
                accounts = ((AccountAware) fsService).getAccounts(session);
            } else {
                accounts = fsService.getAccountManager().getAccounts(session);
            }
            for (final FileStorageAccount fileStorageAccount : accounts) {
                final FileStorageAccountAccess accountAccess = fsService.getAccountAccess(fileStorageAccount.getId(), session);
                connect( accountAccess );
                retval.add(accountAccess.getFileAccess());
            }
        }
        return retval;
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
        connectedAccounts.get().clear();
        accessesToClose.get().clear();
    }

    @Override
    public void finish() throws TransactionException {
        connectedAccounts.get().clear();
        for(final FileStorageAccountAccess acc : accessesToClose.get()) {
            acc.close();
        }
        accessesToClose.get().clear();
        super.finish();
    }
}
