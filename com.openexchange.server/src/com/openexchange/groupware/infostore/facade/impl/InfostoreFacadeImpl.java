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

package com.openexchange.groupware.infostore.facade.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.arrays.Arrays.contains;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.ReuseReadConProvider;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.index.Attachment;
import com.openexchange.groupware.attach.index.AttachmentUUID;
import com.openexchange.groupware.container.EffectiveObjectPermission;
import com.openexchange.groupware.container.EffectiveObjectPermissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EffectiveInfostoreFolderPermission;
import com.openexchange.groupware.infostore.EffectiveInfostorePermission;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreTimedResult;
import com.openexchange.groupware.infostore.database.BatchFilenameReserver;
import com.openexchange.groupware.infostore.database.FilenameReservation;
import com.openexchange.groupware.infostore.database.InfostoreFilenameReservation;
import com.openexchange.groupware.infostore.database.InfostoreFilenameReserver;
import com.openexchange.groupware.infostore.database.impl.BatchFilenameReserverImpl;
import com.openexchange.groupware.infostore.database.impl.CheckSizeSwitch;
import com.openexchange.groupware.infostore.database.impl.CreateDocumentAction;
import com.openexchange.groupware.infostore.database.impl.CreateObjectPermissionAction;
import com.openexchange.groupware.infostore.database.impl.CreateVersionAction;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.infostore.database.impl.DeleteDocumentAction;
import com.openexchange.groupware.infostore.database.impl.DeleteObjectPermissionAction;
import com.openexchange.groupware.infostore.database.impl.DeleteVersionAction;
import com.openexchange.groupware.infostore.database.impl.DocumentCustomizer;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.database.impl.InfostoreIterator;
import com.openexchange.groupware.infostore.database.impl.InfostoreQueryCatalog;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurity;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurityImpl;
import com.openexchange.groupware.infostore.database.impl.ReplaceDocumentIntoDelTableAction;
import com.openexchange.groupware.infostore.database.impl.SelectForUpdateFilenameReserver;
import com.openexchange.groupware.infostore.database.impl.Tools;
import com.openexchange.groupware.infostore.database.impl.UpdateDocumentAction;
import com.openexchange.groupware.infostore.database.impl.UpdateObjectPermissionAction;
import com.openexchange.groupware.infostore.database.impl.UpdateVersionAction;
import com.openexchange.groupware.infostore.index.InfostoreUUID;
import com.openexchange.groupware.infostore.utils.GetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.SetSwitch;
import com.openexchange.groupware.infostore.validation.FilenamesMayNotContainSlashesValidator;
import com.openexchange.groupware.infostore.validation.InvalidCharactersValidator;
import com.openexchange.groupware.infostore.validation.ValidationChain;
import com.openexchange.groupware.infostore.webdav.EntityLockManager;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.Lock;
import com.openexchange.groupware.infostore.webdav.LockManager;
import com.openexchange.groupware.infostore.webdav.LockManager.Scope;
import com.openexchange.groupware.infostore.webdav.LockManager.Type;
import com.openexchange.groupware.infostore.webdav.TouchInfoitemsWithExpiredLocksListener;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.DeltaImpl;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.file.AppendFileAction;
import com.openexchange.tools.file.SaveFileAction;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.tx.UndoableAction;

/**
 * {@link InfostoreFacadeImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreFacadeImpl extends DBService implements InfostoreFacade {

    private static final ValidationChain VALIDATION = new ValidationChain();
    static {
        VALIDATION.add(new InvalidCharactersValidator());
        VALIDATION.add(new FilenamesMayNotContainSlashesValidator());
        // Add more infostore validators here, as needed
    }

    private static final InfostoreFilenameReserver filenameReserver = new SelectForUpdateFilenameReserver();

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreFacadeImpl.class);

    private static final boolean INDEXING_ENABLED = false; //TODO: remove switch once we index infoitems

    public static final InfostoreQueryCatalog QUERIES = InfostoreQueryCatalog.getInstance();

    private static final AtomicReference<QuotaFileStorageService> QFS_REF = new AtomicReference<QuotaFileStorageService>();

    /**
     * Applies the given <code>QuotaFileStorageService</code> instance
     *
     * @param service The instance or <code>null</code>
     */
    public static void setQuotaFileStorageService(QuotaFileStorageService service) {
        QFS_REF.set(service);
    }

    private static class FileRemoveInfo {

        final String fileId;
        final int folderAdmin;
        final int contextId;

        FileRemoveInfo(String fileId, int folderAdmin, int contextId) {
            super();
            this.fileId = fileId;
            this.folderAdmin = folderAdmin;
            this.contextId = contextId;
        }
    }

    // -------------------------------------------------------------------------------------------------------

    /** The infostore security instance */
    protected final InfostoreSecurity security = new InfostoreSecurityImpl();

    private final DatabaseImpl db = new DatabaseImpl();

    private final EntityLockManager lockManager = new EntityLockManagerImpl("infostore_lock");

    private final ThreadLocal<List<FileRemoveInfo>> fileIdRemoveList = new ThreadLocal<List<FileRemoveInfo>>();

    private final TouchInfoitemsWithExpiredLocksListener expiredLocksListener;

    private final ObjectPermissionLoader objectPermissionLoader;
    private final NumberOfVersionsLoader numberOfVersionsLoader;
    private final LockedUntilLoader lockedUntilLoader;

    /**
     * Initializes a new {@link InfostoreFacadeImpl}.
     */
    public InfostoreFacadeImpl() {
        super();
        expiredLocksListener = new TouchInfoitemsWithExpiredLocksListener(null, this);
        lockManager.addExpiryListener(expiredLocksListener);
        this.objectPermissionLoader = new ObjectPermissionLoader(this);
        this.numberOfVersionsLoader = new NumberOfVersionsLoader(this);
        this.lockedUntilLoader = new LockedUntilLoader(lockManager);
    }

    /**
     * Initializes a new {@link InfostoreFacadeImpl}.
     *
     * @param provider The database provider to use
     */
    public InfostoreFacadeImpl(final DBProvider provider) {
        this();
        setProvider(provider);
    }

    @Override
    public boolean exists(final int id, final int version, final ServerSession session) throws OXException {
        try {
            return security.getInfostorePermission(id, session.getContext(), session.getUser(), session.getUserPermissionBits()).canReadObject();
        } catch (final OXException x) {
            if (InfostoreExceptionCodes.NOT_EXIST.equals(x)) {
                return false;
            }
            throw x;
        }
    }

    @Override
    public DocumentMetadata getDocumentMetadata(final int id, final int version, final ServerSession session) throws OXException {
        Context context = session.getContext();
        /*
         * load document metadata (including object permissions)
         */
        DocumentMetadata document = objectPermissionLoader.add(load(id, version, context), context, null);
        /*
         * check permissions
         */
        EffectiveInfostorePermission permission = security.getInfostorePermission(
            document, context, session.getUser(), session.getUserPermissionBits());
        if (false == permission.canReadObject()) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        }
        /*
         * adjust parent folder if required
         */
        if (false == permission.canReadObjectInFolder()) {
            document.setFolderId(getSharedFilesFolderID(session));
        }
        /*
         * add further metadata and return
         */
        return numberOfVersionsLoader.add(lockedUntilLoader.add(document, context, null), context, null);
    }

    @Override
    public DocumentMetadata getDocumentMetadata(int id, int version, Context context) throws OXException {
        /*
         * load document metadata (including object permissions)
         */
        DocumentMetadata document = objectPermissionLoader.add(load(id, version, context), context, null);
        /*
         * add further metadata and return
         */
        return numberOfVersionsLoader.add(lockedUntilLoader.add(document, context, null), context, null);
    }

    @Override
    public IDTuple saveDocumentMetadata(final DocumentMetadata document, final long sequenceNumber, final ServerSession session) throws OXException {
        return saveDocument(document, null, sequenceNumber, session);
    }

    @Override
    public IDTuple saveDocumentMetadata(final DocumentMetadata document, final long sequenceNumber, final Metadata[] modifiedColumns, final ServerSession session) throws OXException {
        return saveDocument(document, null, sequenceNumber, modifiedColumns, session);
    }

    @Override
    public InputStream getDocument(final int id, final int version, final ServerSession session) throws OXException {
        return getDocument(id, version, 0L, -1L, session);
    }

    @Override
    public InputStream getDocument(int id, int version, long offset, long length, final ServerSession session) throws OXException {
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(id, session.getContext(), session.getUser(), session.getUserPermissionBits());
        if (!infoPerm.canReadObject()) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        }
        final DocumentMetadata dm = load(id, version, session.getContext());
        final FileStorage fs = getFileStorage(infoPerm.getFolderOwner(), session.getContextId());
        if (null == dm.getFilestoreLocation()) {
            return Streams.EMPTY_INPUT_STREAM;
        }
        if (0 == offset && -1 == length) {
            return fs.getFile(dm.getFilestoreLocation());
        } else {
            return fs.getFile(dm.getFilestoreLocation(), offset, length);
        }
    }

    @Override
    public void lock(final int id, final long diff, final ServerSession session) throws OXException {
        Context context = session.getContext();
        User user = session.getUser();
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(
            id,
            context,
            user,
            session.getUserPermissionBits());
        if (!infoPerm.canWriteObject()) {
            throw InfostoreExceptionCodes.WRITE_PERMS_FOR_LOCK_MISSING.create();
        }
        final DocumentMetadata document = checkWriteLock(id, session);
        if (lockManager.isLocked(document.getId(), session.getContext(), user)) {
            // Already locked by this user
            return;
        }

        long timeout = 0;
        if (timeout == -1) {
            timeout = LockManager.INFINITE;
        } else {
            timeout = diff;
        }
        lockManager.lock(
            id,
            timeout,
            Scope.EXCLUSIVE,
            Type.WRITE,
            session.getUserlogin(),
            context,
            user);
        touch(id, session);
    }

    @Override
    public void unlock(final int id, final ServerSession session) throws OXException {
        final Context context = session.getContext();
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(
            id,
            context,
            session.getUser(),
            session.getUserPermissionBits());
        if (!infoPerm.canWriteObject()) {
            throw InfostoreExceptionCodes.WRITE_PERMS_FOR_UNLOCK_MISSING.create();
        }
        checkMayUnlock(id, session);
        lockManager.removeAll(id, session);
        touch(id, session);
    }

    @Override
    public void touch(final int id, final ServerSession sessionObj) throws OXException {
        try {
            final Context context = sessionObj.getContext();
            final DocumentMetadata oldDocument = load(id, CURRENT_VERSION, context);
            final DocumentMetadata document = new DocumentMetadataImpl(oldDocument);
            Metadata[] modifiedColums = new Metadata[] { Metadata.LAST_MODIFIED_LITERAL, Metadata.MODIFIED_BY_LITERAL };
            long sequenceNumber = oldDocument.getSequenceNumber();

            document.setLastModified(new Date());
            document.setModifiedBy(sessionObj.getUserId());
            perform(new UpdateDocumentAction(this, QUERIES, context, document, oldDocument, modifiedColums, sequenceNumber), true);
            perform(new UpdateVersionAction(this, QUERIES, context, document, oldDocument, modifiedColums, sequenceNumber), true);
        } catch (final OXException x) {
            throw x;
        } catch (final Exception e) {
            // FIXME Client
            LOG.error("", e);
        }
    }

    @Override
    public com.openexchange.file.storage.Quota getFileQuota(ServerSession session) throws OXException {
        long limit = com.openexchange.file.storage.Quota.UNLIMITED;
        long usage = com.openexchange.file.storage.Quota.UNLIMITED;
        limit = AmountQuotas.getLimit(
            session,
            "infostore",
            ServerServiceRegistry.getServize(ConfigViewFactory.class, true),
            ServerServiceRegistry.getServize(DatabaseService.class, true));
        if (com.openexchange.file.storage.Quota.UNLIMITED != limit) {
            usage = getUsedQuota(session.getContext());
        }
        return new com.openexchange.file.storage.Quota(limit, usage, com.openexchange.file.storage.Quota.Type.FILE);
    }

    @Override
    public com.openexchange.file.storage.Quota getStorageQuota(ServerSession session) throws OXException {
        long limit = com.openexchange.file.storage.Quota.UNLIMITED;
        long usage = com.openexchange.file.storage.Quota.UNLIMITED;
        try {
            limit = getFileStorage(session.getUserId(), session.getContextId()).getQuota();
        } catch (OXException e) {
            LOG.warn("Error getting file storage quota for context {}", session.getContextId(), e);
        }
        if (com.openexchange.file.storage.Quota.UNLIMITED != limit) {
            usage = getFileStorage(session.getUserId(), session.getContextId()).getUsage();
        }
        return new com.openexchange.file.storage.Quota(limit, usage, com.openexchange.file.storage.Quota.Type.STORAGE);
    }

    /**
     * Loads the current version of a document with all available metadata from the database.
     *
     * @param id The ID of the document to load
     * @param ctx The context
     * @return The loaded document
     * @throws OXException
     */
    protected DocumentMetadata load(int id, Context ctx) throws OXException {
        return load(id, CURRENT_VERSION, ctx);
    }

    /**
     * Loads a document in a specific version with all available metadata from the database.
     *
     * @param id The ID of the document to load
     * @param version The version to load
     * @param ctx The context
     * @return The loaded document
     * @throws OXException
     */
    protected DocumentMetadata load(final int id, final int version, final Context ctx) throws OXException {
        InfostoreIterator iterator = null;
        try {
            iterator = InfostoreIterator.loadDocumentIterator(id, version, getProvider(), ctx);
            if (false == iterator.hasNext()) {
                throw InfostoreExceptionCodes.DOCUMENT_NOT_EXIST.create();
            }
            return iterator.next();
        } finally {
            SearchIterators.close(iterator);
        }
    }

    private DocumentMetadata checkWriteLock(final int id, final ServerSession session) throws OXException {
        final DocumentMetadata document = load(id, CURRENT_VERSION, session.getContext());
        checkWriteLock(document, session);
        return document;
    }

    private void checkWriteLock(final DocumentMetadata document, final ServerSession session) throws OXException {
        if (document.getModifiedBy() == session.getUserId()) {
            return;
        }

        if (lockManager.isLocked(document.getId(), session.getContext(), session.getUser())) {
            throw InfostoreExceptionCodes.ALREADY_LOCKED.create();
        }
    }

    private void checkMayUnlock(final int id, final ServerSession session) throws OXException {
        final DocumentMetadata document = load(id, CURRENT_VERSION, session.getContext());
        if (document.getCreatedBy() == session.getUserId() || document.getModifiedBy() == session.getUserId()) {
            return;
        }
        final List<Lock> locks = lockManager.findLocks(id, session);
        if (locks.size() > 0) {
            throw InfostoreExceptionCodes.LOCKED_BY_ANOTHER.create();
        }
    }

    @Override
    public IDTuple saveDocument(final DocumentMetadata document, final InputStream data, final long sequenceNumber, final ServerSession session) throws OXException {
        final Context context = session.getContext();
        security.checkFolderId(document.getFolderId(), context);

        boolean wasCreation = false;
        if (document.getId() != InfostoreFacade.NEW) {
            return saveDocument(document, data, sequenceNumber, nonNull(document), session);
        }

        // Insert NEW document
        wasCreation = true;
        final EffectiveInfostoreFolderPermission isperm = security.getFolderPermission(
            document.getFolderId(),
            context,
            session.getUser(),
            session.getUserPermissionBits());
        if (!isperm.canCreateObjects()) {
            throw InfostoreExceptionCodes.NO_CREATE_PERMISSION.create();
        }

        {
            com.openexchange.file.storage.Quota storageQuota = getFileQuota(session);
            long limit = storageQuota.getLimit();
            if (limit > 0) {
                long usage = storageQuota.getUsage();
                if (usage >= limit) {
                    throw QuotaExceptionCodes.QUOTA_EXCEEDED_FILES.create(Long.valueOf(usage), Long.valueOf(limit));
                }
            }
        }

        setDefaults(document);

        VALIDATION.validate(document);
        CheckSizeSwitch.checkSizes(document, getProvider(), context);

        final boolean titleAlso = document.getFileName() != null && document.getTitle() != null && document.getFileName().equals(document.getTitle());

        final InfostoreFilenameReservation reservation = reserve(
            document.getFileName(),
            document.getFolderId(),
            document.getId(),
            context, true);

        document.setFileName(reservation.getFilename());
        if(titleAlso) {
            document.setTitle(reservation.getFilename());
        }

        try {
            Connection writeCon = null;
            try {
                startDBTransaction();
                writeCon = getWriteConnection(context);
                document.setId(getId(context, writeCon));
                commitDBTransaction();
            } catch (final SQLException e) {
                throw InfostoreExceptionCodes.NEW_ID_FAILED.create(e);
            } finally {
                releaseWriteConnection(context, writeCon);
                finishDBTransaction();
            }

            Date now = new Date();
            if (null == document.getLastModified()) {
                document.setLastModified(now);
            }
            if (null == document.getCreationDate()) {
                document.setCreationDate(now);
            }
            document.setCreatedBy(session.getUserId());
            document.setModifiedBy(session.getUserId());

            // db.createDocument(document, data, sessionObj.getContext(),
            // sessionObj.getUserObject(), getUserConfiguration(sessionObj));

            if (null != data) {
                document.setVersion(1);
            } else {
                document.setVersion(0);
            }

            perform(new CreateDocumentAction(this, QUERIES, context, Collections.singletonList(document)), true);
            perform(new CreateObjectPermissionAction(this, context, document), true);

            final DocumentMetadata version0 = new DocumentMetadataImpl(document);
            version0.setFileName(null);
            version0.setFileSize(0);
            version0.setFileMD5Sum(null);
            version0.setFileMIMEType(null);
            version0.setVersion(0);
            version0.setFilestoreLocation(null);

            perform(new CreateVersionAction(this, QUERIES, context, Collections.singletonList(version0)), true);

            if (data != null) {
                SaveFileAction saveFile = new SaveFileAction(getFileStorage(isperm.getFolderOwner(), session.getContextId()), data, document.getFileSize());
                perform(saveFile, false);
                document.setVersion(1);
                document.setFilestoreLocation(saveFile.getFileStorageID());
                document.setFileMD5Sum(saveFile.getChecksum());
                document.setFileSize(saveFile.getByteCount());

                perform(new CreateVersionAction(this, QUERIES, context, Collections.singletonList(document)), true);
            }

            indexDocument(context, session.getUserId(), document.getId(), -1L, wasCreation);

            return new IDTuple(String.valueOf(document.getFolderId()), String.valueOf(document.getId()));
        } finally {
            if (reservation != null) {
                reservation.destroySilently();
            }
        }
    }

    private long getUsedQuota(final Context context) throws OXException {
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = getReadConnection(context);
            stmt = readCon.prepareStatement("SELECT COUNT(id) from infostore where cid=?");
            stmt.setLong(1, context.getContextId());
            rs = stmt.executeQuery();
            return rs.next() ? rs.getLong(1) : -1L;
        } catch (final SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            if (null != readCon) {
                releaseReadConnection(context, readCon);
            }
        }
    }

    private void setDefaults(final DocumentMetadata document) {
        if (document.getTitle() == null || "".equals(document.getTitle())) {
            document.setTitle(document.getFileName());
        }
    }

    protected <T> T performQuery(final Context ctx, final String query, final ResultProcessor<T> rsp, final Object... args) throws SQLException, OXException {
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            readCon = getReadConnection(ctx);
            stmt = readCon.prepareStatement(query);
            for (int i = 0; i < args.length; i++) {
                stmt.setObject(i + 1, args[i]);
            }

            rs = stmt.executeQuery();

            return rsp.process(rs);
        } finally {
            close(stmt, rs);
            if (readCon != null) {
                releaseReadConnection(ctx, readCon);
            }
        }
    }

    // FIXME Move 2 query builder
    private int getNextVersionNumberForInfostoreObject(final int cid, final int infostore_id, final Connection con) throws SQLException {
        int retval = 0;

        PreparedStatement stmt = con.prepareStatement("SELECT MAX(version_number) FROM infostore_document WHERE cid=? AND infostore_id=?");
        stmt.setInt(1, cid);
        stmt.setInt(2, infostore_id);
        ResultSet result = stmt.executeQuery();
        if (result.next()) {
            retval = result.getInt(1);
        }
        result.close();
        stmt.close();

        stmt = con.prepareStatement("SELECT MAX(version_number) FROM del_infostore_document WHERE cid=? AND infostore_id=?");
        stmt.setInt(1, cid);
        stmt.setInt(2, infostore_id);
        result = stmt.executeQuery();
        if (result.next()) {
            final int delVersion = result.getInt(1);
            if (delVersion > retval) {
                retval = delVersion;
            }
        }
        result.close();
        stmt.close();

        return retval + 1;
    }

    private InfostoreFilenameReservation reserve(final String filename, final long folderId, final int id, final Context ctx, final boolean adjust) throws OXException {
        return reserve(filename, folderId, id, ctx, adjust ? 0 : -1);
    }

    private InfostoreFilenameReservation reserve(final String filename, final long folderId, final int id, final Context ctx, final int count) throws OXException {
        InfostoreFilenameReservation reservation = null;
        try {
            reservation = filenameReserver.reserveFilename(filename, folderId, id, ctx, this);
            if (reservation == null) {
                if (count == -1) {
                    throw InfostoreExceptionCodes.FILENAME_NOT_UNIQUE.create(filename, "");
                }
                int cnt = count;
                InfostoreFilenameReservation r = reserve(FileStorageUtility.enhance(filename, ++cnt), folderId, id, ctx, cnt);
                r.setWasAdjusted(true);
                return r;
            }
        } catch (final SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, "");
        }
        return reservation;
    }

    protected QuotaFileStorage getFileStorage(int folderOwner, int contextId) throws OXException {
        QuotaFileStorageService storageService = QFS_REF.get();
        if (null == storageService) {
            throw ServiceExceptionCode.absentService(QuotaFileStorageService.class);
        }

        return storageService.getQuotaFileStorage(folderOwner, contextId);
    }

    private Metadata[] nonNull(final DocumentMetadata document) {
        final List<Metadata> nonNull = new ArrayList<Metadata>();
        final GetSwitch get = new GetSwitch(document);
        for (final Metadata metadata : Metadata.HTTPAPI_VALUES) {
            if (null != metadata.doSwitch(get)) {
                nonNull.add(metadata);
            }
        }
        return nonNull.toArray(new Metadata[nonNull.size()]);
    }

    @Override
    public IDTuple saveDocument(final DocumentMetadata document, final InputStream data, final long sequenceNumber, final Metadata[] modifiedColumns, final ServerSession session) throws OXException {
        return saveDocument(document, data, sequenceNumber, modifiedColumns, false, session);
    }

    @Override
    public IDTuple saveDocument(final DocumentMetadata document, final InputStream data, final long sequenceNumber, final Metadata[] modifiedColumns, final boolean ignoreVersion, final ServerSession session) throws OXException {
        return saveDocument(document, data, sequenceNumber, modifiedColumns, ignoreVersion, -1L, session);
    }

    @Override
    public IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, long offset, final ServerSession session) throws OXException {
        return saveDocument(document, data, sequenceNumber, modifiedColumns, true, offset, session);
    }

    protected IDTuple saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, boolean ignoreVersion, long offset, final ServerSession session) throws OXException {
        if (0 < offset && (NEW == document.getId() || false == ignoreVersion)) {
            throw InfostoreExceptionCodes.NO_OFFSET_FOR_NEW_VERSIONS.create();
        }

        if (document.getId() == NEW) {
            IDTuple result = saveDocument(document, data, sequenceNumber, session);
            indexDocument(session.getContext(), session.getUserId(), document.getId(), -1L, true);
            return result;
        }

        // Check permission
        Context context = session.getContext();
        EffectiveInfostorePermission infoPerm = security.getInfostorePermission(
            document.getId(),
            context,
            session.getUser(),
            session.getUserPermissionBits());
        if (!infoPerm.canWriteObject()) {
            throw InfostoreExceptionCodes.NO_WRITE_PERMISSION.create();
        }

        // Check and adjust folder id
        List<Metadata> sanitizedColumns = new ArrayList<Metadata>(modifiedColumns.length);
        Collections.addAll(sanitizedColumns, modifiedColumns);
        if (sanitizedColumns.contains(Metadata.FOLDER_ID_LITERAL)) {
            long folderId = document.getFolderId();
            if (folderId == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
                document.setFolderId(infoPerm.getObject().getFolderId());
                sanitizedColumns.remove(Metadata.FOLDER_ID_LITERAL);
                modifiedColumns = sanitizedColumns.toArray(new Metadata[sanitizedColumns.size()]);
            } else if (document.getFolderId() != -1 && infoPerm.getObject().getFolderId() != document.getFolderId()) {
                security.checkFolderId(document.getFolderId(), context);
                final EffectiveInfostoreFolderPermission isperm = security.getFolderPermission(
                    document.getFolderId(),
                    context,
                    session.getUser(),
                    session.getUserPermissionBits());
                if (!(isperm.canCreateObjects())) {
                    throw InfostoreExceptionCodes.NO_CREATE_PERMISSION.create();
                }
                if (!infoPerm.canDeleteObject()) {
                    throw InfostoreExceptionCodes.NO_SOURCE_DELETE_PERMISSION.create();
                }
            }
        }

        // Set modified information
        Set<Metadata> updatedCols = new HashSet<Metadata>(Arrays.asList(modifiedColumns));
        updatedCols.removeAll(Arrays.asList(Metadata.CREATED_BY_LITERAL, Metadata.CREATION_DATE_LITERAL, Metadata.ID_LITERAL));
        if (!updatedCols.contains(Metadata.LAST_MODIFIED_LITERAL)) {
            document.setLastModified(new Date());
        }
        document.setModifiedBy(session.getUserId());
        updatedCols.add(Metadata.LAST_MODIFIED_LITERAL);
        updatedCols.add(Metadata.MODIFIED_BY_LITERAL);

        CheckSizeSwitch.checkSizes(document, getProvider(), context);
        VALIDATION.validate(document);

        DocumentMetadata oldDocument = objectPermissionLoader.add(checkWriteLock(document.getId(), session), session.getContext(), null);
        SaveParameters saveParameters = new SaveParameters(context, document, oldDocument, sequenceNumber, updatedCols, infoPerm.getFolderOwner());
        saveParameters.setData(data, offset, session.getUserId(), ignoreVersion);
        saveModifiedDocument(saveParameters);

        long indexFolderId = document.getFolderId() == oldDocument.getFolderId() ? -1L : oldDocument.getFolderId();
        indexDocument(context, session.getUserId(), oldDocument.getId(), indexFolderId, false);

        return new IDTuple(String.valueOf(document.getFolderId()), String.valueOf(document.getId()));
    }

    @Override
    public IDTuple saveDocumentMetadata(DocumentMetadata document, long sequenceNumber, Metadata[] modifiedColumns, Context context) throws OXException {
        if (document.getId() == NEW) {
            throw InfostoreExceptionCodes.DOCUMENT_NOT_EXIST.create();
        }

        long folderId = document.getFolderId();
        if (folderId < 0) {
            throw InfostoreExceptionCodes.NOT_INFOSTORE_FOLDER.create(folderId);
        }
        if (folderId < FolderObject.MIN_FOLDER_ID) {
            throw InfostoreExceptionCodes.NO_DOCUMENTS_IN_VIRTUAL_FOLDER.create();
        }

        Set<Metadata> updatedCols = new HashSet<Metadata>();
        Collections.addAll(updatedCols, modifiedColumns == null ? Metadata.VALUES_ARRAY : modifiedColumns);
        if (!updatedCols.contains(Metadata.LAST_MODIFIED_LITERAL)) {
            document.setLastModified(new Date());
            updatedCols.add(Metadata.LAST_MODIFIED_LITERAL);
        }

        if (!updatedCols.contains(Metadata.MODIFIED_BY_LITERAL)) {
            document.setModifiedBy(context.getMailadmin());
            updatedCols.add(Metadata.MODIFIED_BY_LITERAL);
        }

        CheckSizeSwitch.checkSizes(document, getProvider(), context);
        VALIDATION.validate(document);

        DocumentMetadata oldDocument = objectPermissionLoader.add(load(document.getId(), context), context, null);
        return saveModifiedDocument(new SaveParameters(context, document, oldDocument, sequenceNumber, updatedCols, security.getFolderOwner(folderId, context)));
    }

    private IDTuple saveModifiedDocument(SaveParameters parameters) throws OXException {
        InfostoreFilenameReservation reservation = null;
        try {
            Set<Metadata> updatedCols = parameters.getUpdatedCols();
            DocumentMetadata document = parameters.getDocument();
            DocumentMetadata oldDocument = parameters.getOldDocument();

            if (updatedCols.contains(Metadata.VERSION_LITERAL)) {
                final String fname = load(document.getId(), document.getVersion(), parameters.getContext()).getFileName();
                if (!updatedCols.contains(Metadata.FILENAME_LITERAL)) {
                    updatedCols.add(Metadata.FILENAME_LITERAL);
                    document.setFileName(fname);
                }
            }

            boolean isMove = updatedCols.contains(Metadata.FOLDER_ID_LITERAL) && oldDocument.getFolderId() != document.getFolderId();
            boolean isRename = updatedCols.contains(Metadata.FILENAME_LITERAL) && null != document.getFileName() && false == document.getFileName().equals(oldDocument.getFileName());
            if (isMove) {
                // this is a move - reserve in target folder
                String newFileName = null != document.getFileName() ? document.getFileName() : oldDocument.getFileName();
                reservation = reserve(
                    newFileName,
                    document.getFolderId(),
                    oldDocument.getId(),
                    parameters.getContext(), true);
                document.setFileName(reservation.getFilename());
                updatedCols.add(Metadata.FILENAME_LITERAL);

                // insert tombstone row to del_infostore table in case of move operations to aid folder based synchronizations
                DocumentMetadataImpl tombstoneDocument = new DocumentMetadataImpl(oldDocument);
                tombstoneDocument.setLastModified(document.getLastModified());
                tombstoneDocument.setModifiedBy(document.getModifiedBy());
                perform(new ReplaceDocumentIntoDelTableAction(this, QUERIES, parameters.getContext(), tombstoneDocument), true);
            } else if (isRename) {
                // this is a rename - reserve in current folder
                reservation = reserve(
                    document.getFileName(),
                    oldDocument.getFolderId(),
                    oldDocument.getId(),
                    parameters.getContext(), true);
                document.setFileName(reservation.getFilename());
                updatedCols.add(Metadata.FILENAME_LITERAL);
            }

            String oldTitle = oldDocument.getTitle();
            if (!updatedCols.contains(Metadata.TITLE_LITERAL) && oldDocument.getFileName() != null && oldTitle != null && oldDocument.getFileName().equals(oldTitle)) {
                if (null == document.getFileName()) {
                    document.setTitle(oldDocument.getFileName());
                    document.setFileName(oldDocument.getFileName());
                    updatedCols.add(Metadata.FILENAME_LITERAL);
                } else {
                    document.setTitle(document.getFileName());
                }
                updatedCols.add(Metadata.TITLE_LITERAL);
            }

            Metadata[] modifiedCols;
            if (parameters.hasData()) {
                storeNewData(parameters);
                modifiedCols = updatedCols.toArray(new Metadata[updatedCols.size()]);
            } else {
                modifiedCols = updatedCols.toArray(new Metadata[updatedCols.size()]);
                if (QUERIES.updateVersion(modifiedCols)) {
                    if (!updatedCols.contains(Metadata.VERSION_LITERAL)) {
                        document.setVersion(oldDocument.getVersion());
                    }

                    perform(new UpdateVersionAction(this, QUERIES, parameters.getContext(), document, oldDocument, modifiedCols, parameters.getSequenceNumber()), true);
                }
            }

            if (QUERIES.updateDocument(modifiedCols)) {
                perform(new UpdateDocumentAction(this, QUERIES, parameters.getContext(), document, oldDocument, modifiedCols, Long.MAX_VALUE), true);
            }
            /*
             * update object permissions as needed
             */
            if (updatedCols.contains(Metadata.OBJECT_PERMISSIONS_LITERAL)) {
                perform(new UpdateObjectPermissionAction(this, parameters.getContext(), document, oldDocument), true);
            }
            return new IDTuple(String.valueOf(document.getFolderId()), String.valueOf(document.getId()));
        } finally {
            if (reservation != null) {
                reservation.destroySilently();
            }
        }
    }

    private void storeNewData(SaveParameters parameters) throws OXException {
        QuotaFileStorage qfs = getFileStorage(parameters.getOptFolderAdmin(), parameters.getContext().getContextId());
        if (0 < parameters.getOffset()) {
            AppendFileAction appendFile = new AppendFileAction(
                qfs, parameters.getData(), parameters.getOldDocument().getFilestoreLocation(), parameters.getDocument().getFileSize(), parameters.getOffset());
            perform(appendFile, false);
            parameters.getDocument().setFilestoreLocation(parameters.getOldDocument().getFilestoreLocation());
            parameters.getDocument().setFileSize(appendFile.getByteCount() + parameters.getOffset());
            parameters.getDocument().setFileMD5Sum(null); // invalidate due to append-operation
            parameters.getUpdatedCols().addAll(Arrays.asList(Metadata.FILE_MD5SUM_LITERAL, Metadata.FILE_SIZE_LITERAL));
        } else {
            SaveFileAction saveFile = new SaveFileAction(qfs, parameters.getData(), parameters.getDocument().getFileSize());
            perform(saveFile, false);
            parameters.getDocument().setFilestoreLocation(saveFile.getFileStorageID());
            parameters.getDocument().setFileSize(saveFile.getByteCount());
            parameters.getDocument().setFileMD5Sum(saveFile.getChecksum());
            parameters.getUpdatedCols().addAll(Arrays.asList(
                Metadata.FILE_MD5SUM_LITERAL, Metadata.FILE_SIZE_LITERAL, Metadata.FILESTORE_LOCATION_LITERAL));
        }

        final GetSwitch get = new GetSwitch(parameters.getOldDocument());
        final SetSwitch set = new SetSwitch(parameters.getDocument());
        for (Metadata m : new Metadata[] { Metadata.DESCRIPTION_LITERAL, Metadata.TITLE_LITERAL, Metadata.FILENAME_LITERAL, Metadata.URL_LITERAL }) {
            if (parameters.getUpdatedCols().contains(m)) {
                continue;
            }
            set.setValue(m.doSwitch(get));
            m.doSwitch(set);
        }

        parameters.getDocument().setCreatedBy(parameters.getFileCreatedBy());
        if (!parameters.getUpdatedCols().contains(Metadata.CREATION_DATE_LITERAL)) {
            parameters.getDocument().setCreationDate(new Date());
        }

        // Set version
        final UndoableAction action;
        if (parameters.isIgnoreVersion()) {
            parameters.getDocument().setVersion(parameters.getOldDocument().getVersion());
            parameters.getUpdatedCols().add(Metadata.VERSION_LITERAL);
            parameters.getUpdatedCols().add(Metadata.FILESTORE_LOCATION_LITERAL);
            action = new UpdateVersionAction(this, QUERIES, parameters.getContext(), parameters.getDocument(), parameters.getOldDocument(),
                parameters.getUpdatedCols().toArray(new Metadata[parameters.getUpdatedCols().size()]), parameters.getSequenceNumber());

            // Remove old file "version" if not appended
            if (0 >= parameters.getOffset()) {
                removeFile(parameters.getContext(), parameters.getOldDocument().getFilestoreLocation(), security.getFolderOwner(parameters.getOldDocument(), parameters.getContext()));
            }
        } else {
            Connection con = null;
            try {
                con = getReadConnection(parameters.getContext());
                parameters.getDocument().setVersion(getNextVersionNumberForInfostoreObject(
                    parameters.getContext().getContextId(),
                    parameters.getDocument().getId(),
                    con));
                parameters.getUpdatedCols().add(Metadata.VERSION_LITERAL);
            } catch (final SQLException e) {
                LOG.error("SQLException: ", e);
            } finally {
                releaseReadConnection(parameters.getContext(), con);
            }

            action = new CreateVersionAction(this, QUERIES, parameters.getContext(), Collections.singletonList(parameters.getDocument()));
        }
        // Perform action
        perform(action, true);
    }

    @Override
    public void removeDocument(final long folderId, final long date, final ServerSession session) throws OXException {
        if (folderId == getSharedFilesFolderID(session)) {
            throw InfostoreExceptionCodes.NO_DELETE_PERMISSION.create();
        }
        Context context = session.getContext();
        DBProvider provider = new ReuseReadConProvider(getProvider());
        String whereClause = "infostore.folder_id = " + folderId;
        List<DocumentMetadata> allDocuments = InfostoreIterator.allDocumentsWhere(
            whereClause, Metadata.VALUES_ARRAY, provider, context).asList();
        if (0 < allDocuments.size()) {
            List<DocumentMetadata> allVersions = InfostoreIterator.allVersionsWhere(
                whereClause, Metadata.VALUES_ARRAY, provider, context).asList();
            objectPermissionLoader.add(allDocuments, context, objectPermissionLoader.load(folderId, context));
            removeDocuments(allDocuments, allVersions, date, session, null);
        }
    }

    protected void removeDocuments(final List<DocumentMetadata> allDocuments, final List<DocumentMetadata> allVersions, final long date, final ServerSession sessionObj, final List<DocumentMetadata> rejected) throws OXException {
        final List<DocumentMetadata> delDocs = new ArrayList<DocumentMetadata>();
        final List<DocumentMetadata> delVers = new ArrayList<DocumentMetadata>();
        final Set<Integer> rejectedIds = new HashSet<Integer>();

        final Date now = new Date(); // FIXME: Recovery will change lastModified;

        for (final DocumentMetadata m : allDocuments) {
            if (m.getSequenceNumber() > date) {
                if (rejected == null) {
                    throw InfostoreExceptionCodes.NOT_ALL_DELETED.create();
                }
                rejected.add(m);
                rejectedIds.add(Integer.valueOf(m.getId()));
            } else {
                checkWriteLock(m, sessionObj);
                m.setLastModified(now);
                delDocs.add(m);
            }
        }

        final Context context = sessionObj.getContext();

        /*
         * Move records into del_* tables
         */
        perform(new ReplaceDocumentIntoDelTableAction(this, QUERIES, context, delDocs), true);
        /*
         * Remove referenced files from underlying storage
         */
        List<String> filestoreLocations = new ArrayList<String>(allVersions.size());
        TIntList folderAdmins = new TIntLinkedList();
        for (final DocumentMetadata m : allVersions) {
            if (!rejectedIds.contains(Integer.valueOf(m.getId()))) {
                delVers.add(m);
                m.setLastModified(now);
                if (null != m.getFilestoreLocation()) {
                    filestoreLocations.add(m.getFilestoreLocation());
                    folderAdmins.add(security.getFolderOwners(Collections.singletonList(m), context));
                }
            }
        }
        removeFiles(context, filestoreLocations, folderAdmins.toArray());

        /*
         * Delete documents and all versions from database
         */
        perform(new DeleteDocumentAction(this, QUERIES, context, delDocs), true);
        /*
         * delete object permissions
         */
        perform(new DeleteObjectPermissionAction(this, context, delDocs), true);

        /*
         * Remove from index
         */
        removeFromIndex(context, sessionObj.getUserId(), delDocs);
        // TODO: This triggers a full re-indexing and can be improved. We only have to re-index if the latest version is affected.
        removeFromIndex(context, sessionObj.getUserId(), delVers);
    }

    /**
     * Removes the supplied file from the underlying storage. If a transaction is active, the file is remembered to be deleted during
     * the {@link #commit()}-phase - otherwise it's deleted from the storage directly.
     *
     * @param context The context
     * @param filestoreLocation The location referencing the file to be deleted in the storage
     * @param folderAdmin The folder administrator
     * @throws OXException
     */
    private void removeFile(final Context context, final String filestoreLocation, int folderAdmin) throws OXException {
        removeFiles(context, Collections.singletonList(filestoreLocation), new int[] { folderAdmin });
    }

    /**
     * Removes the supplied files from the underlying storage. If a transaction is active, the files are remembered to be deleted during
     * the {@link #commit()}-phase - otherwise they're deleted from the storage directly.
     *
     * @param context The context
     * @param filestoreLocations A list of locations referencing the files to be deleted in the storage
     * @param folderAdmins The associated folder administrators
     * @throws OXException
     */
    private void removeFiles(Context context, List<String> filestoreLocations, int[] folderAdmins) throws OXException {
        if (null != filestoreLocations) {
            int size = filestoreLocations.size();
            if (0 < size) {
                int contextId = context.getContextId();
                List<FileRemoveInfo> removeList = fileIdRemoveList.get();
                if (null != removeList) {
                    for (int i = 0; i < size; i++) {
                        removeList.add(new FileRemoveInfo(filestoreLocations.get(i), folderAdmins[i], contextId));
                    }
                } else {
                    for (int i = 0; i < size; i++) {
                        getFileStorage(folderAdmins[i], contextId).deleteFile(filestoreLocations.get(i));
                    }
                }
            }
        }
    }

    /**
     * Gets a list of infostore documents.
     *
     * @param provider The DB provider
     * @param context The context
     * @param ids The object IDs of the documents to get
     * @param metadata The metadata to retrieve
     * @return The documents
     * @throws OXException
     */
    private static List<DocumentMetadata> getAllDocuments(DBProvider provider, Context context, int[] ids, Metadata[] metadata) throws OXException {
        if (null == ids || 0 == ids.length) {
            return Collections.emptyList();
        }
        if (1 == ids.length) {
            return InfostoreIterator.allDocumentsWhere("infostore.id = " + ids[0], metadata, provider, context).asList();
        }
        StringBuilder StringBuilder = new StringBuilder("infostore.id IN (");
        StringBuilder.append(String.valueOf(ids[0]));
        for (int i = 1; i < ids.length; i++) {
            StringBuilder.append(',').append(String.valueOf(ids[i]));
        }
        StringBuilder.append(')');
        return InfostoreIterator.allDocumentsWhere(StringBuilder.toString(), metadata, provider, context).asList();
    }

    /**
     * Moves the supplied documents to another folder.
     *
     * @param session The server session
     * @param documents The source documents to move
     * @param destinationFolderID The destination folder ID
     * @param sequenceNumber The client timestamp to catch concurrent modifications
     * @param adjustFilenamesAsNeeded <code>true</code> to adjust filenames in target folder automatically, <code>false</code>, otherwise
     * @return A list of documents that could not be moved due to concurrent modifications
     * @throws OXException
     */
    protected List<DocumentMetadata> moveDocuments(ServerSession session, List<DocumentMetadata> documents, long destinationFolderID,
        long sequenceNumber, boolean adjustFilenamesAsNeeded) throws OXException {
        Context context = session.getContext();
        User user = session.getUser();
        UserPermissionBits permissionBits = session.getUserPermissionBits();
        /*
         * check destination folder permissions
         */
        EffectiveInfostoreFolderPermission destinationFolderPermission = security.getFolderPermission(destinationFolderID, context, user, permissionBits);
        if (false == destinationFolderPermission.canCreateObjects()) {
            throw InfostoreExceptionCodes.NO_CREATE_PERMISSION.create();
        }

        /*
         * check source folder permissions, write locks and client timestamp
         */
        List<DocumentMetadata> rejectedDocuments = new ArrayList<DocumentMetadata>();
        List<DocumentMetadata> sourceDocuments = new ArrayList<DocumentMetadata>(documents.size());
        List<EffectiveInfostorePermission> permissions = security.getInfostorePermissions(documents, context, user, permissionBits);
        for (EffectiveInfostorePermission permission : permissions) {
            if (!permission.canDeleteObject()) {
                throw InfostoreExceptionCodes.NO_DELETE_PERMISSION.create();
            }
        }

        for (DocumentMetadata document : documents) {
            checkWriteLock(document, session);
            if (document.getSequenceNumber() <= sequenceNumber) {
                sourceDocuments.add(document);
            } else {
                rejectedDocuments.add(document);
            }
        }

        if (0 < sourceDocuments.size()) {
            /*
             * prepare move
             */
            Date now = new Date();
            Connection readConnection = null;
            BatchFilenameReserver filenameReserver = new BatchFilenameReserverImpl(session.getContext(), this);
            try {
                readConnection = getReadConnection(context);
                boolean moveToTrash = FolderObject.TRASH == new OXFolderAccess(readConnection, context)
                    .getFolderObject((int) destinationFolderID).getType();
                List<DocumentMetadata> tombstoneDocuments = new ArrayList<DocumentMetadata>(sourceDocuments.size());
                List<DocumentMetadata> documentsToUpdate = new ArrayList<DocumentMetadata>(sourceDocuments.size());
                List<DocumentMetadata> versionsToUpdate = new ArrayList<DocumentMetadata>();
                List<DocumentMetadata> objectPermissionsToCreate = new ArrayList<DocumentMetadata>();
                List<DocumentMetadata> objectPermissionsToDelete = new ArrayList<DocumentMetadata>();
                for (DocumentMetadata document : sourceDocuments) {
                    /*
                     * prepare updated document
                     */
                    DocumentMetadataImpl documentToUpdate = new DocumentMetadataImpl(document);
                    documentToUpdate.setLastModified(now);
                    documentToUpdate.setModifiedBy(session.getUserId());
                    documentToUpdate.setFolderId(destinationFolderID);
                    documentsToUpdate.add(documentToUpdate);
                    /*
                     * prepare tombstone entry in del_infostore table for source document
                     */
                    DocumentMetadataImpl tombstoneDocument = new DocumentMetadataImpl(document);
                    tombstoneDocument.setLastModified(now);
                    tombstoneDocument.setModifiedBy(session.getUserId());
                    tombstoneDocuments.add(tombstoneDocument);
                    /*
                     * prepare object permission update / removal
                     */
                    if (null != document.getObjectPermissions() && 0 < document.getObjectPermissions().size()) {
                        objectPermissionsToDelete.add(document);
                        if (false == moveToTrash) {
                            objectPermissionsToCreate.add(documentToUpdate);
                        }
                    }
                }
                /*
                 * reserve filenames
                 */
                Map<DocumentMetadata, FilenameReservation> reservations = filenameReserver.reserve(documentsToUpdate, adjustFilenamesAsNeeded);
                if (adjustFilenamesAsNeeded) {
                    /*
                     * take over adjusted filenames; remember to update document version, too
                     */
                    for (Entry<DocumentMetadata, FilenameReservation> entry : reservations.entrySet()) {
                        FilenameReservation reservation = entry.getValue();
                        if (reservation.wasAdjusted()) {
                            DocumentMetadata document = entry.getKey();
                            if (document.getFileName().equals(document.getTitle())) {
                                document.setTitle(reservation.getFilename());
                            }
                            document.setFileName(reservation.getFilename());
                            versionsToUpdate.add(document);
                        }
                    }
                }
                /*
                 * perform tombstone creations
                 */
                perform(new ReplaceDocumentIntoDelTableAction(this, QUERIES, session.getContext(), tombstoneDocuments), true);
                /*
                 * perform document move
                 */
                perform(new UpdateDocumentAction(this, QUERIES, session.getContext(), documentsToUpdate, sourceDocuments, new Metadata[] {
                    Metadata.LAST_MODIFIED_LITERAL, Metadata.MODIFIED_BY_LITERAL, Metadata.FOLDER_ID_LITERAL }, sequenceNumber), true);
                /*
                 * perform object permission inserts / removals
                 */
                if (0 < objectPermissionsToDelete.size()) {
                    perform(new DeleteObjectPermissionAction(this, context, objectPermissionsToDelete), true);
                }
                if (0 < objectPermissionsToCreate.size()) {
                    for (DocumentMetadata document : objectPermissionsToCreate) {
                        perform(new CreateObjectPermissionAction(this, context, document), true);
                    }
                }
                /*
                 * perform version update (only required in case of adjusted filenames)
                 */
                if (0 < versionsToUpdate.size()) {
                    perform(new UpdateVersionAction(this, QUERIES, session.getContext(), versionsToUpdate, sourceDocuments,
                        new Metadata[] { Metadata.FILENAME_LITERAL, Metadata.TITLE_LITERAL }, sequenceNumber), true);
                }
                /*
                 * re-index moved documents
                 */
                for (DocumentMetadata sourceDocument : sourceDocuments) {
                    indexDocument(session.getContext(), session.getUserId(), sourceDocument.getId(), sourceDocument.getFolderId(), false);
                }
            } finally {
                filenameReserver.cleanUp();
                if (null != readConnection) {
                    releaseReadConnection(context, readConnection);
                }
            }
        }
        /*
         * return rejected documents
         */
        return rejectedDocuments;
    }

    @Override
    public List<IDTuple> moveDocuments(ServerSession session, List<IDTuple> ids, long sequenceNumber, String targetFolderID, boolean adjustFilenamesAsNeeded) throws OXException {
        if (null == ids || 0 == ids.size()) {
            return Collections.emptyList();
        }
        long destinationFolderID;
        try {
            destinationFolderID = Long.parseLong(targetFolderID);
        } catch (NumberFormatException e) {
            throw InfostoreExceptionCodes.NOT_INFOSTORE_FOLDER.create(targetFolderID, e);
        }
        /*
         * get documents to move
         */
        DBProvider reuseProvider = new ReuseReadConProvider(getProvider());
        int[] objectIDs = Tools.getObjectIDArray(ids);
        List<DocumentMetadata> allDocuments = getAllDocuments(reuseProvider, session.getContext(), objectIDs, Metadata.VALUES_ARRAY);
        objectPermissionLoader.add(allDocuments, session.getContext(), Tools.getIDs(allDocuments));
        /*
         * Ensure folder ids are consistent between request and existing documents
         */
        Map<Integer, Long> idsToFolders = Tools.getIDsToFolders(ids);
        for (DocumentMetadata document : allDocuments) {
            Long requestedFolder = idsToFolders.get(document.getId());
            long expectedFolder = document.getFolderId();
            if (requestedFolder == null || requestedFolder.longValue() != expectedFolder) {
                throw InfostoreExceptionCodes.NOT_EXIST.create();
            }
        }
        /*
         * perform move
         */
        List<DocumentMetadata> rejectedDocuments = moveDocuments(
            session, allDocuments, destinationFolderID, sequenceNumber, adjustFilenamesAsNeeded);
        if (null == rejectedDocuments || 0 == rejectedDocuments.size()) {
            return Collections.emptyList();
        }
        List<IDTuple> rejectedIDs = new ArrayList<IDTuple>();
        for (DocumentMetadata rejected : rejectedDocuments) {
            rejectedIDs.add(new IDTuple(Long.toString(rejected.getFolderId()), Integer.toString(rejected.getId())));
        }
        return rejectedIDs;
    }

    @Override
    public List<IDTuple> removeDocument(final List<IDTuple> ids, final long date, final ServerSession session) throws OXException {
        if (null == ids || 0 == ids.size()) {
            return Collections.emptyList();
        }

        final Map<Integer, Long> idsToFolders = Tools.getIDsToFolders(ids);
        final Context context = session.getContext();
        final User user = session.getUser();
        final UserPermissionBits userPermissionBits = session.getUserPermissionBits();
        final DBProvider reuseProvider = new ReuseReadConProvider(getProvider());

        Set<Integer> objectIDs = idsToFolders.keySet();
        String whereClause;
        if (1 == objectIDs.size()) {
            whereClause = "infostore.id=" + objectIDs.iterator().next();
        } else {
            StringBuilder stringBuilder = new StringBuilder("infostore.id IN (");
            Strings.join(objectIDs, ",", stringBuilder);
            whereClause = stringBuilder.append(')').toString();
        }
        List<DocumentMetadata> allDocuments = InfostoreIterator.allDocumentsWhere(
            whereClause, Metadata.VALUES_ARRAY, reuseProvider, context).asList();
        List<DocumentMetadata> allVersions = InfostoreIterator.allVersionsWhere(
            whereClause, Metadata.VALUES_ARRAY, reuseProvider, context).asList();
        objectPermissionLoader.add(allDocuments, context, idsToFolders.keySet());

        // Ensure folder ids are consistent between request and existing documents
        for (DocumentMetadata document : allDocuments) {
            Long requestedFolder = idsToFolders.get(document.getId());
            long expectedFolder = document.getFolderId();
            if (requestedFolder == null || requestedFolder.longValue() != expectedFolder) {
                throw InfostoreExceptionCodes.NOT_EXIST.create();
            }
        }

        // Check Permissions
        List<EffectiveInfostorePermission> permissions = security.getInfostorePermissions(allDocuments, context, user, userPermissionBits);
        for (EffectiveInfostorePermission permission : permissions) {
            if (!permission.canDeleteObject()) {
                throw InfostoreExceptionCodes.NO_DELETE_PERMISSION.create();
            }
        }

        final Set<Integer> unknownDocuments = new HashSet<Integer>(idsToFolders.keySet());
        for (DocumentMetadata document : allDocuments) {
            unknownDocuments.remove(document.getId());
        }

        final List<DocumentMetadata> rejectedDocuments = new ArrayList<DocumentMetadata>();
        removeDocuments(allDocuments, allVersions, date, session, rejectedDocuments);

        List<IDTuple> rejectedIDs = new ArrayList<IDTuple>(rejectedDocuments.size() + unknownDocuments.size());
        for (final DocumentMetadata rejected : rejectedDocuments) {
            rejectedIDs.add(new IDTuple(Long.toString(rejected.getFolderId()), Integer.toString(rejected.getId())));
        }

        for (Integer notFound : unknownDocuments) {
            rejectedIDs.add(new IDTuple(idsToFolders.get(notFound).toString(), Integer.toString(notFound)));
        }

        return rejectedIDs;
    }

    @Override
    public void removeDocuments(List<IDTuple> ids, Context context) throws OXException {
        if (null == ids || 0 == ids.size()) {
            return;
        }

        final Map<Integer, Long> idsToFolders = Tools.getIDsToFolders(ids);
        final DBProvider reuseProvider = new ReuseReadConProvider(getProvider());
        Set<Integer> objectIDs = idsToFolders.keySet();
        String whereClause;
        if (1 == objectIDs.size()) {
            whereClause = "infostore.id=" + objectIDs.iterator().next();
        } else {
            StringBuilder stringBuilder = new StringBuilder("infostore.id IN (");
            Strings.join(objectIDs, ",", stringBuilder);
            whereClause = stringBuilder.append(')').toString();
        }
        List<DocumentMetadata> allDocuments = InfostoreIterator.allDocumentsWhere(
            whereClause, Metadata.VALUES_ARRAY, reuseProvider, context).asList();
        List<DocumentMetadata> allVersions = InfostoreIterator.allVersionsWhere(
            whereClause, Metadata.VALUES_ARRAY, reuseProvider, context).asList();

        // Ensure folder ids are consistent between request and existing documents
        for (DocumentMetadata document : allDocuments) {
            Long requestedFolder = idsToFolders.get(document.getId());
            long expectedFolder = document.getFolderId();
            if (requestedFolder == null || requestedFolder.longValue() != expectedFolder) {
                throw InfostoreExceptionCodes.NOT_EXIST.create();
            }
        }

        final Set<Integer> unknownDocuments = new HashSet<Integer>(idsToFolders.keySet());
        for (DocumentMetadata document : allDocuments) {
            unknownDocuments.remove(document.getId());
        }

        if (!unknownDocuments.isEmpty()) {
            throw InfostoreExceptionCodes.NOT_EXIST.create();
        }

        /*
         * Move records into del_* tables
         */
        perform(new ReplaceDocumentIntoDelTableAction(this, QUERIES, context, allDocuments), true);
        /*
         * Remove referenced files from underlying storage
         */
        List<String> filestoreLocations = new ArrayList<String>(allVersions.size());
        TIntList folderAdmins = new TIntLinkedList();
        for (final DocumentMetadata m : allVersions) {
            if (null != m.getFilestoreLocation()) {
                filestoreLocations.add(m.getFilestoreLocation());
                folderAdmins.add(security.getFolderOwners(Collections.singletonList(m), context));
            }
        }
        removeFiles(context, filestoreLocations, folderAdmins.toArray());

        /*
         * Delete documents and all versions from database
         */
        perform(new DeleteDocumentAction(this, QUERIES, context, allDocuments), true);
        /*
         * delete object permissions
         */
        perform(new DeleteObjectPermissionAction(this, context, allDocuments), true);
    }

    /**
     * Gets the identifier of the folder holding single documents shared to the session's user based on extended object permissions.
     *
     * @param session The session
     * @return The identifier of the shared documents folder
     */
    private int getSharedFilesFolderID(ServerSession session) {
        return FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID;
    }

    @Override
    public int[] removeVersion(final int id, final int[] versionIds, final ServerSession session) throws OXException {
        if (null == versionIds || 0 == versionIds.length) {
            return new int[0];
        }
        Context context = session.getContext();
        /*
         * load document metadata (including object permissions)
         */
        DocumentMetadata metadata = objectPermissionLoader.add(load(id, CURRENT_VERSION, context), context, null);
        /*
         * check write lock & permissions
         */
        try {
            checkWriteLock(metadata, session);
        } catch (OXException x) {
            return versionIds;
        }
        EffectiveInfostorePermission permission = security.getInfostorePermission(
            metadata, context, session.getUser(), session.getUserPermissionBits());
        if (false == permission.canDeleteObject()) {
            throw InfostoreExceptionCodes.NO_DELETE_PERMISSION_FOR_VERSION.create();
        }

        final StringBuilder versions = new StringBuilder().append('(');
        final Set<Integer> versionSet = new HashSet<Integer>();

        for (final int v : versionIds) {
            versions.append(v).append(',');
            versionSet.add(Integer.valueOf(v));
        }
        versions.setLength(versions.length() - 1);
        versions.append(')');

        List<DocumentMetadata> allVersions = InfostoreIterator.allVersionsWhere(
            "infostore_document.infostore_id = " + id + " AND infostore_document.version_number IN " + versions.toString() + " and infostore_document.version_number != 0 ",
            Metadata.VALUES_ARRAY,
            this,
            context).asList();

        final Date now = new Date();

        boolean removeCurrent = false;
        for (final DocumentMetadata v : allVersions) {
            if (v.getVersion() == metadata.getVersion()) {
                removeCurrent = true;
            }
            versionSet.remove(Integer.valueOf(v.getVersion()));
            v.setLastModified(now);
            removeFile(context, v.getFilestoreLocation(), security.getFolderOwner(v, context));
        }

        // update version number if needed

        final DocumentMetadata update = new DocumentMetadataImpl(metadata);

        update.setLastModified(now);
        update.setModifiedBy(session.getUserId());

        final Set<Metadata> updatedFields = new HashSet<Metadata>();
        updatedFields.add(Metadata.LAST_MODIFIED_LITERAL);
        updatedFields.add(Metadata.MODIFIED_BY_LITERAL);

        if (removeCurrent) {

            // Update Version 0
            final DocumentMetadata oldVersion0 = load(id, 0, context);

            final DocumentMetadata version0 = new DocumentMetadataImpl(metadata);
            version0.setVersion(0);
            version0.setFileMIMEType("");

            perform(new UpdateVersionAction(this, QUERIES, context, version0, oldVersion0, new Metadata[] {
                Metadata.DESCRIPTION_LITERAL, Metadata.TITLE_LITERAL, Metadata.URL_LITERAL, Metadata.LAST_MODIFIED_LITERAL,
                Metadata.MODIFIED_BY_LITERAL, Metadata.FILE_MIMETYPE_LITERAL }, Long.MAX_VALUE), true);

            // Set new Version Number
            update.setVersion(db.getMaxActiveVersion(metadata.getId(), context, allVersions));
            updatedFields.add(Metadata.VERSION_LITERAL);
        }


        if (removeCurrent) {
            metadata = load(metadata.getId(), update.getVersion(), context);
            InfostoreFilenameReservation reservation = reserve(metadata.getFileName(), metadata.getFolderId(), metadata.getId(), context, true);
            if (reservation.wasAdjusted()) {
                update.setFileName(reservation.getFilename());
                updatedFields.add(Metadata.FILENAME_LITERAL);
            }
            if (metadata.getTitle().equals(metadata.getFileName())) {
                update.setTitle(update.getFileName());
                updatedFields.add(Metadata.TITLE_LITERAL);
            }
        }
        perform(new UpdateDocumentAction(this, QUERIES, context, update, metadata,
            updatedFields.toArray(new Metadata[updatedFields.size()]), Long.MAX_VALUE), true);

        // Remove Versions
        perform(new DeleteVersionAction(this, QUERIES, context, allVersions), true);

        final int[] retval = new int[versionSet.size()];
        int i = 0;
        for (final Integer integer : versionSet) {
            retval[i++] = integer.intValue();
        }

        if (removeCurrent) {
            removeFromIndex(context, session.getUserId(), Collections.singletonList(metadata));
            indexDocument(context, session.getUserId(), id, -1L, true);
        }

        return retval;
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(final long folderId, final ServerSession session) throws OXException {
        return getDocuments(folderId, Metadata.HTTPAPI_VALUES_ARRAY, null, 0, session);
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(final long folderId, final Metadata[] columns, final ServerSession session) throws OXException {
        return getDocuments(folderId, columns, null, 0, session);
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(final long folderId, Metadata[] columns, final Metadata sort, final int order, final ServerSession session) throws OXException {
        if (folderId == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
            return getReadableSharedDocuments(columns, sort, order, session);
        }

        Metadata[] cols = addLastModifiedIfNeeded(columns);
        boolean onlyOwn = false;
        Context context = session.getContext();
        User user = session.getUser();

        EffectiveInfostoreFolderPermission isperm = security.getFolderPermission(folderId, context, user, session.getUserPermissionBits());
        if (isperm.getReadPermission() == OCLPermission.NO_PERMISSIONS) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        } else if (isperm.getReadPermission() == OCLPermission.READ_OWN_OBJECTS) {
            onlyOwn = true;
        }

        InfostoreIterator iter;
        if (onlyOwn) {
            iter = InfostoreIterator.documentsByCreator(folderId, user.getId(), cols, sort, order, getProvider(), context);
        } else {
            iter = InfostoreIterator.documents(folderId, cols, sort, order, getProvider(), context);
        }
        /*
         * fast-forward results to get the object IDs and sequence number in case additional metadata is required
         * (#lockedUntilIterator() would do it anyway)
         */
        boolean addLocked = contains(columns, Metadata.LOCKED_UNTIL_LITERAL);
        boolean addNumberOfVersions = contains(columns, Metadata.NUMBER_OF_VERSIONS_LITERAL);
        boolean addObjectPermissions = contains(columns, Metadata.OBJECT_PERMISSIONS_LITERAL);
        if (addLocked || addNumberOfVersions || addObjectPermissions) {
            long maxSequenceNumber = 0;
            final List<DocumentMetadata> documents = iter.asList();
            if (0 == documents.size()) {
                return com.openexchange.groupware.results.Results.emptyTimedResult();
            }
            List<Integer> objectIDs = new ArrayList<Integer>(documents.size());
            for (DocumentMetadata document : documents) {
                maxSequenceNumber = Math.max(maxSequenceNumber, document.getSequenceNumber());
                objectIDs.add(Integer.valueOf(document.getId()));
            }
            final long sequenceNumber = maxSequenceNumber;
            TimedResult<DocumentMetadata> timedResult = new TimedResult<DocumentMetadata>() {

                @Override
                public SearchIterator<DocumentMetadata> results() throws OXException {
                    return new SearchIteratorAdapter<DocumentMetadata>(documents.iterator());
                }

                @Override
                public long sequenceNumber() throws OXException {
                    return sequenceNumber;
                }
            };
            /*
             * enhance metadata with pre-loaded data as needed
             */
            if (addObjectPermissions) {
                timedResult = objectPermissionLoader.add(timedResult, context, objectIDs);
            }
            if (addLocked) {
                timedResult = lockedUntilLoader.add(timedResult, context, objectIDs);
            }
            if (addNumberOfVersions) {
                timedResult = numberOfVersionsLoader.add(timedResult, context, objectIDs);
            }
            return timedResult;
        }
        /*
         * stick to plain infostore timed result, otherwise
         */
        return new InfostoreTimedResult(iter);
    }

    private TimedResult<DocumentMetadata> getReadableSharedDocuments(Metadata[] columns, final Metadata sort, final int order, final ServerSession session) throws OXException {
        InfostoreIterator iterator = InfostoreIterator.sharedDocumentsForUser(session.getContext(), session.getUser(), ObjectPermission.READ, columns, db);
        iterator.setCustomizer(new DocumentCustomizer() {
            @Override
            public DocumentMetadata handle(DocumentMetadata document) {
                document.setFolderId(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
                return document;
            }
        });
        return new InfostoreTimedResult(iterator);
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(final int id, final ServerSession session) throws OXException {
        return getVersions(id, Metadata.HTTPAPI_VALUES_ARRAY, null, 0, session);
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(final int id, final Metadata[] columns, final ServerSession session) throws OXException {
        return getVersions(id, columns, null, 0, session);
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(final int id, Metadata[] columns, final Metadata sort, final int order, final ServerSession session) throws OXException {
        Context context = session.getContext();
        final User user = session.getUser();
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(id, context, user, session.getUserPermissionBits());
        if (!infoPerm.canReadObject()) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        }
        Metadata[] cols = addLastModifiedIfNeeded(columns);
        final InfostoreIterator iter = InfostoreIterator.versions(id, cols, sort, order, getProvider(), context);
        iter.setCustomizer(new DocumentCustomizer() {
            @Override
            public DocumentMetadata handle(DocumentMetadata document) {
                if (!infoPerm.canReadObjectInFolder()) {
                    document.setFolderId(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
                }

                return document;
            }
        });
        TimedResult<DocumentMetadata> timedResult = new InfostoreTimedResult(iter);
        if (contains(columns, Metadata.LOCKED_UNTIL_LITERAL)) {
            timedResult = lockedUntilLoader.add(timedResult, context, Collections.singleton(I(id)));
        }
        if (contains(columns, Metadata.OBJECT_PERMISSIONS_LITERAL)) {
            timedResult = objectPermissionLoader.add(timedResult, context, Collections.singleton(I(id)));
        }
        return timedResult;
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(List<IDTuple> ids, Metadata[] columns, final ServerSession session) throws OXException {
        final Context context = session.getContext();
        final User user = session.getUser();
        final Map<Integer, Long> idsToFolders = Tools.getIDsToFolders(ensureFolderIDs(context, ids));
        List<Integer> objectIDs = Tools.getObjectIDs(ids);
        Metadata[] cols = addLastModifiedIfNeeded(columns);
        /*
         * pre-fetch object permissions if needed for result anyway
         */
        boolean addObjectPermissions = contains(cols, Metadata.OBJECT_PERMISSIONS_LITERAL);
        final Map<Integer, List<ObjectPermission>> knownObjectPermissions = addObjectPermissions ?
            objectPermissionLoader.load(objectIDs, context) : null;
        /*
         * get items, checking permissions as lazy as possible
         */
        final Map<Long, EffectiveInfostoreFolderPermission> knownFolderPermissions = new HashMap<Long, EffectiveInfostoreFolderPermission>();
        InfostoreIterator iterator = InfostoreIterator.list(Autoboxing.I2i(objectIDs), cols, getProvider(), session.getContext());
        iterator.setCustomizer(new DocumentCustomizer() {

            @Override
            public DocumentMetadata handle(DocumentMetadata document) throws OXException {
                /*
                 * get & remember permissions for parent folder
                 */
                Long folderID = Long.valueOf(document.getFolderId());
                EffectiveInfostoreFolderPermission folderPermission = knownFolderPermissions.get(folderID);
                if (null == folderPermission) {
                    folderPermission = security.getFolderPermission(folderID.longValue(), context, user, session.getUserPermissionBits());
                    knownFolderPermissions.put(folderID, folderPermission);
                }
                /*
                 * check read permissions, trying the folder permissions first
                 */
                if (false == new EffectiveInfostorePermission(folderPermission.getPermission(), document, user, -1).canReadObject()) {
                    /*
                     * check object permissions, too
                     */
                    EffectiveInfostorePermission infostorePermission = null;
                    List<ObjectPermission> objectPermissions = null != knownObjectPermissions ?
                        knownObjectPermissions.get(I(document.getId())) : objectPermissionLoader.load(document.getId(), context);
                    if (null != objectPermissions) {
                        ObjectPermission matchingPermission = EffectiveObjectPermissions.find(user, objectPermissions);
                        if (null != matchingPermission) {
                            EffectiveObjectPermission objectPermission = EffectiveObjectPermissions.convert(FolderObject.INFOSTORE,
                                (int) document.getFolderId(), document.getId(), matchingPermission, session.getUserPermissionBits());
                            infostorePermission = new EffectiveInfostorePermission(folderPermission.getPermission(), objectPermission, document, user, -1);
                        }
                    }
                    if (null == infostorePermission || false == infostorePermission.canReadObject()) {
                        throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
                    }
                    /*
                     * in case of available object permissions, check requested folder
                     */
                    if (false == infostorePermission.canReadObjectInFolder()) {
                        Long requestedFolderID = idsToFolders.get(I(document.getId()));
                        if (null == requestedFolderID || getSharedFilesFolderID(session) != requestedFolderID.intValue()) {
                            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
                        }
                        /*
                         * adjust parent folder id to match requested identifier
                         */
                        document.setFolderId(getSharedFilesFolderID(session));
                    }
                }
                return document;
            }
        });
        /*
         * wrap iterator into timed result, adding additional metadata as needed
         */
        TimedResult<DocumentMetadata> timedResult = new InfostoreTimedResult(iterator);
        if (addObjectPermissions) {
            timedResult = objectPermissionLoader.add(timedResult, context, knownObjectPermissions);
        }
        if (contains(cols, Metadata.LOCKED_UNTIL_LITERAL)) {
            timedResult = lockedUntilLoader.add(timedResult, context, objectIDs);
        }
        if (contains(cols, Metadata.NUMBER_OF_VERSIONS_LITERAL)) {
            timedResult = numberOfVersionsLoader.add(timedResult, context, objectIDs);
        }
        return timedResult;
    }

    @Override
    public Delta<DocumentMetadata> getDelta(final long folderId, final long updateSince, final Metadata[] columns, final boolean ignoreDeleted, final ServerSession session) throws OXException {
        return getDelta(folderId, updateSince, columns, null, 0, ignoreDeleted, session);
    }

    @Override
    public Delta<DocumentMetadata> getDelta(final long folderId, final long updateSince, Metadata[] columns, final Metadata sort, final int order, final boolean ignoreDeleted, final ServerSession session) throws OXException {
        final Context context = session.getContext();
        final User user = session.getUser();
        final DBProvider reuse = new ReuseReadConProvider(getProvider());

        InfostoreIterator newIter = null;
        InfostoreIterator modIter = null;
        InfostoreIterator delIter = null;
        Metadata[] cols = addLastModifiedIfNeeded(columns);
        if (folderId == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
            DocumentCustomizer customizer = new DocumentCustomizer() {
                @Override
                public DocumentMetadata handle(DocumentMetadata document) {
                    document.setFolderId(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
                    return document;
                }
            };
            newIter = InfostoreIterator.newSharedDocumentsForUser(context, user, columns, sort, order, updateSince, reuse);
            newIter.setCustomizer(customizer);
            modIter = InfostoreIterator.modifiedSharedDocumentsForUser(context, user, columns, sort, order, updateSince, reuse);
            modIter.setCustomizer(customizer);
            if (!ignoreDeleted) {
                delIter = InfostoreIterator.deletedSharedDocumentsForUser(context, user, columns, sort, order, updateSince, reuse);
                delIter.setCustomizer(customizer);
            }
        } else {
            boolean onlyOwn = false;
            final EffectiveInfostoreFolderPermission isperm = security.getFolderPermission(folderId, context, user, session.getUserPermissionBits());
            if (isperm.getReadPermission() == OCLPermission.NO_PERMISSIONS) {
                throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
            } else if (isperm.getReadPermission() == OCLPermission.READ_OWN_OBJECTS) {
                onlyOwn = true;
            }

            if (onlyOwn) {
                newIter = InfostoreIterator.newDocumentsByCreator(folderId, user.getId(), cols, sort, order, updateSince, reuse, context);
                modIter = InfostoreIterator.modifiedDocumentsByCreator(folderId, user.getId(), cols, sort, order, updateSince, reuse, context);
                if (!ignoreDeleted) {
                    delIter = InfostoreIterator.deletedDocumentsByCreator(folderId, user.getId(), sort, order, updateSince, reuse, context);
                }
            } else {
                newIter = InfostoreIterator.newDocuments(folderId, cols, sort, order, updateSince, reuse, context);
                modIter = InfostoreIterator.modifiedDocuments(folderId, cols, sort, order, updateSince, reuse, context);
                if (!ignoreDeleted) {
                    delIter = InfostoreIterator.deletedDocuments(folderId, sort, order, updateSince, reuse, context);
                }
            }
        }

        boolean addLocked = false;
        boolean addNumberOfVersions = false;
        for (final Metadata m : columns) {
            if (m == Metadata.LOCKED_UNTIL_LITERAL) {
                addLocked = true;
                break;
            }
            if (m == Metadata.NUMBER_OF_VERSIONS_LITERAL) {
                addNumberOfVersions = true;
                break;
            }
        }

        final SearchIterator<DocumentMetadata> it;
        if (ignoreDeleted) {
            it = SearchIteratorAdapter.emptyIterator();
        } else {
            it = delIter;
        }

        Delta<DocumentMetadata> delta = new DeltaImpl<DocumentMetadata>(newIter, modIter, it, System.currentTimeMillis());
        if (addLocked) {
            final Map<Integer, List<Lock>> locks = loadLocksInFolderAndExpireOldLocks(folderId, session);
            delta = lockedUntilLoader.add(delta, context, locks);
        }
        if (addNumberOfVersions) {
            delta = numberOfVersionsLoader.add(delta, context, (Map<Integer, Integer>) null);
        }
        return delta;
    }

    @Override
    public Map<Long, Long> getSequenceNumbers(List<Long> folderIds, boolean versionsOnly, final ServerSession session) throws OXException {
        if (0 == folderIds.size()) {
            return Collections.emptyMap();
        }

        final Map<Long, Long> sequenceNumbers = new HashMap<Long, Long>(folderIds.size());
        try {
            User user = session.getUser();
            int contextId = session.getContextId();
            Context context = session.getContext();
            final Long userInfostoreId = new Long(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
            if (folderIds.remove(userInfostoreId)) {
                performQuery(context,
                    QUERIES.getSharedDocumentsSequenceNumbersQuery(versionsOnly, true, contextId, user.getId(), user.getGroups()), new ResultProcessor<Void>() {
                    @Override
                    public Void process(ResultSet rs) throws SQLException {
                        while (rs.next()) {
                            long newSequence = rs.getLong(1);
                            Long oldSequence = sequenceNumbers.get(userInfostoreId);
                            if (oldSequence == null || oldSequence.longValue() < newSequence) {
                                sequenceNumbers.put(userInfostoreId, Long.valueOf(newSequence));
                            }
                        }
                        return null;
                    }
                });
                performQuery(context,
                    QUERIES.getSharedDocumentsSequenceNumbersQuery(versionsOnly, false, contextId,  user.getId(), user.getGroups()), new ResultProcessor<Void>() {
                    @Override
                    public Void process(ResultSet rs) throws SQLException {
                        while (rs.next()) {
                            long newSequence = rs.getLong(1);
                            Long oldSequence = sequenceNumbers.get(userInfostoreId);
                            if (oldSequence == null || oldSequence.longValue() < newSequence) {
                                sequenceNumbers.put(userInfostoreId, Long.valueOf(newSequence));
                            }
                        }
                        return null;
                    }
                });
            }

            performQuery(context,
                QUERIES.getFolderSequenceNumbersQuery(folderIds, versionsOnly, true, contextId), new ResultProcessor<Void>() {
                @Override
                public Void process(ResultSet rs) throws SQLException {
                    while (rs.next()) {
                        sequenceNumbers.put(Long.valueOf(rs.getLong(1)), Long.valueOf(rs.getLong(2)));
                    }
                    return null;
                }
            });
            performQuery(context,
                QUERIES.getFolderSequenceNumbersQuery(folderIds, versionsOnly, false, contextId), new ResultProcessor<Void>() {
                @Override
                public Void process(ResultSet rs) throws SQLException {
                    while (rs.next()) {
                        Long folderID = Long.valueOf(rs.getLong(1));
                        long newSequence = rs.getLong(2);
                        Long oldSequence = sequenceNumbers.get(folderID);
                        if (oldSequence == null || oldSequence.longValue() < newSequence) {
                            sequenceNumbers.put(folderID, Long.valueOf(newSequence));
                        }
                    }
                    return null;
                }
            });
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
        return sequenceNumbers;
    }

    private Map<Integer, List<Lock>> loadLocksInFolderAndExpireOldLocks(final long folderId, final ServerSession session) throws OXException {
        final Map<Integer, List<Lock>> locks = new HashMap<Integer, List<Lock>>();
        final InfostoreIterator documents = InfostoreIterator.documents(
            folderId,
            new Metadata[] { Metadata.ID_LITERAL },
            null,
            -1,
            getProvider(),
            session.getContext());
        try {
            while (documents.hasNext()) {
                final DocumentMetadata document = documents.next();
                lockManager.findLocks(document.getId(), session);
            }
        } finally {
            documents.close();
        }
        return locks;
    }

    @Override
    public int countDocuments(final long folderId, final ServerSession session) throws OXException {
        if (folderId == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
            InfostoreIterator it = InfostoreIterator.sharedDocumentsForUser(session.getContext(), session.getUser(), ObjectPermission.READ, new Metadata[] { Metadata.ID_LITERAL }, getProvider());
            return it.asList().size();
        }

        boolean onlyOwn = false;
        User user = session.getUser();
        final EffectiveInfostoreFolderPermission isperm = security.getFolderPermission(folderId, session.getContext(), user, session.getUserPermissionBits());
        if (!(isperm.canReadAllObjects()) && !(isperm.canReadOwnObjects())) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        } else if (isperm.canReadOwnObjects()) {
            onlyOwn = true;
        }
        return db.countDocuments(folderId, onlyOwn, session.getContext(), user);
    }

    @Override
    public boolean hasFolderForeignObjects(final long folderId, final ServerSession session) throws OXException {
        if (folderId == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
            return true;
        }

        return db.hasFolderForeignObjects(folderId, session.getContext(), session.getUser());
    }

    @Override
    public boolean isFolderEmpty(final long folderId, final Context ctx) throws OXException {
        if (folderId == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
            return true; // We can't determine this without a user id...
        }

        return db.isFolderEmpty(folderId, ctx);
    }

    @Override
    public void removeUser(final int userId, final Context ctx, final ServerSession session) throws OXException {
        db.removeUser(userId, ctx, session, lockManager);
    }

    private int getId(final Context context, final Connection writeCon) throws SQLException {
        final boolean autoCommit = writeCon.getAutoCommit();
        if (autoCommit) {
            writeCon.setAutoCommit(false);
        }
        try {
            return IDGenerator.getId(context, Types.INFOSTORE, writeCon);
        } finally {
            if (autoCommit) {
                writeCon.commit();
                writeCon.setAutoCommit(true);
            }
        }
    }

    private Metadata[] addLastModifiedIfNeeded(final Metadata[] columns) {
        for (final Metadata metadata : columns) {
            if (metadata == Metadata.LAST_MODIFIED_LITERAL || metadata == Metadata.LAST_MODIFIED_UTC_LITERAL) {
                return columns;
            }
        }
        final Metadata[] copy = new Metadata[columns.length + 1];
        int i = 0;
        for (final Metadata metadata : columns) {
            copy[i++] = metadata;
        }
        copy[i] = Metadata.LAST_MODIFIED_UTC_LITERAL;
        return copy;
    }

    public InfostoreSecurity getSecurity() {
        return security;
    }

    private static enum ServiceMethod {
        COMMIT, FINISH, ROLLBACK, SET_REQUEST_TRANSACTIONAL, START_TRANSACTION, SET_PROVIDER;

        public void call(final Object o, final Object... args) {
            if (!(o instanceof DBService)) {
                return;
            }
            final DBService service = (DBService) o;
            switch (this) {
            default:
                return;
            case SET_REQUEST_TRANSACTIONAL:
                service.setRequestTransactional(((Boolean) args[0]).booleanValue());
                break;
            case SET_PROVIDER:
                service.setProvider((DBProvider) args[0]);
                break;
            }
        }

        public void callUnsafe(final Object o, final Object... args) throws OXException {
            if (!(o instanceof DBService)) {
                return;
            }
            final DBService service = (DBService) o;
            switch (this) {
            default:
                call(o, args);
                break;
            case COMMIT:
                service.commit();
                break;
            case FINISH:
                service.finish();
                break;
            case ROLLBACK:
                service.rollback();
                break;
            case START_TRANSACTION:
                service.startTransaction();
                break;
            }
        }

    }

    @Override
    public void commit() throws OXException {
        db.commit();
        ServiceMethod.COMMIT.callUnsafe(security);
        lockManager.commit();
        List<FileRemoveInfo> filesToRemove = fileIdRemoveList.get();
        if (null != filesToRemove && !filesToRemove.isEmpty()) {
            for (FileRemoveInfo rmInfo : filesToRemove) {
                getFileStorage(rmInfo.folderAdmin, rmInfo.contextId).deleteFile(rmInfo.fileId);
            }
        }
        super.commit();
    }

    @Override
    public void finish() throws OXException {
        fileIdRemoveList.set(null);
        db.finish();
        ServiceMethod.FINISH.callUnsafe(security);
        super.finish();
    }

    @Override
    public void rollback() throws OXException {
        db.rollback();
        ServiceMethod.ROLLBACK.callUnsafe(security);
        lockManager.rollback();
        super.rollback();
    }

    @Override
    public void setRequestTransactional(final boolean transactional) {
        db.setRequestTransactional(transactional);
        ServiceMethod.SET_REQUEST_TRANSACTIONAL.call(security, Boolean.valueOf(transactional));
        lockManager.setRequestTransactional(transactional);
        super.setRequestTransactional(transactional);
    }

    @Override
    public void setTransactional(final boolean transactional) {
        lockManager.setTransactional(transactional);
    }

    @Override
    public void startTransaction() throws OXException {
        fileIdRemoveList.set(new LinkedList<InfostoreFacadeImpl.FileRemoveInfo>());
        db.startTransaction();
        ServiceMethod.START_TRANSACTION.callUnsafe(security);
        lockManager.startTransaction();
        super.startTransaction();
    }

    @Override
    public void setProvider(final DBProvider provider) {
        super.setProvider(provider);
        db.setProvider(provider);
        ServiceMethod.SET_PROVIDER.call(security, provider);
        ServiceMethod.SET_PROVIDER.call(lockManager, provider);
    }


    private static interface ResultProcessor<T> {

        public T process(ResultSet rs) throws SQLException;
    }

    @Override
    public void setSessionHolder(final SessionHolder sessionHolder) {
        expiredLocksListener.setSessionHolder(sessionHolder);
    }

    /**
     * Processes the list of supplied ID tuples to ensure that each entry has an assigned folder ID.
     *
     * @param context The context
     * @param tuples The ID tuples to process
     * @return The ID tuples, with each entry holding its full file- and folder-ID information
     * @throws OXException
     */
    private List<IDTuple> ensureFolderIDs(Context context, List<IDTuple> tuples) throws OXException {
        if (null == tuples || 0 == tuples.size()) {
            return tuples;
        }
        Map<Integer, IDTuple> incompleteTuples = new HashMap<Integer, IDTuple>();
        for (IDTuple tuple : tuples) {
            if (null == tuple.getFolder()) {
                try {
                    incompleteTuples.put(Integer.valueOf(tuple.getId()), tuple);
                } catch (NumberFormatException e) {
                    throw InfostoreExceptionCodes.NOT_EXIST.create();
                }
            }
        }
        if (0 < incompleteTuples.size()) {
            InfostoreIterator iterator = null;
            try {
                iterator = InfostoreIterator.list(Autoboxing.I2i(incompleteTuples.keySet()),
                    new Metadata[] { Metadata.ID_LITERAL, Metadata.FOLDER_ID_LITERAL }, this, context);
                while (iterator.hasNext()) {
                    DocumentMetadata document = iterator.next();
                    IDTuple tuple = incompleteTuples.get(Integer.valueOf(document.getId()));
                    if (null != tuple) {
                        tuple.setFolder(String.valueOf(document.getFolderId()));
                    }
                }
            } finally {
                SearchIterators.close(iterator);
            }
        }
        return tuples;
    }

    private void removeFromIndex(final Context context, final int userId, final List<DocumentMetadata> documents) {
        if (false == INDEXING_ENABLED) {
            return;
        }
        if (documents == null || documents.isEmpty()) {
            return;
        }
        ThreadPoolService threadPoolService = ServerServiceRegistry.getInstance().getService(ThreadPoolService.class);
        ExecutorService executorService = threadPoolService.getExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                IndexFacadeService indexFacade = ServerServiceRegistry.getInstance().getService(IndexFacadeService.class);
                if (indexFacade != null) {
                    IndexAccess<DocumentMetadata> infostoreIndex = null;
                    IndexAccess<Attachment> attachmentIndex = null;
                    try {
                        LOG.debug("Deleting infostore document");

                        infostoreIndex = indexFacade.acquireIndexAccess(Types.INFOSTORE, userId, context.getContextId());
                        attachmentIndex = indexFacade.acquireIndexAccess(Types.ATTACHMENT, userId, context.getContextId());
                        for (DocumentMetadata document : documents) {
                            infostoreIndex.deleteById(InfostoreUUID.newUUID(
                                context.getContextId(),
                                userId,
                                document.getFolderId(),
                                document.getId()).toString());
                            attachmentIndex.deleteById(AttachmentUUID.newUUID(
                                context.getContextId(),
                                userId,
                                Types.INFOSTORE,
                                IndexConstants.DEFAULT_ACCOUNT,
                                String.valueOf(document.getFolderId()),
                                String.valueOf(document.getId()),
                                IndexConstants.DEFAULT_ATTACHMENT).toString());
                        }
                    } catch (Exception e) {
                        if ((e instanceof OXException) && (IndexExceptionCodes.INDEX_LOCKED.equals((OXException) e) || IndexExceptionCodes.INDEXING_NOT_ENABLED.equals((OXException) e))) {
                            LOG.debug("Could not remove document from infostore index.");
                        } else {
                            LOG.error("Error while deleting documents from index.", e);
                        }
                    } finally {
                        if (infostoreIndex != null) {
                            try {
                                indexFacade.releaseIndexAccess(infostoreIndex);
                            } catch (OXException e) {
                                // Ignore
                            }
                        }

                        if (attachmentIndex != null) {
                            try {
                                indexFacade.releaseIndexAccess(attachmentIndex);
                            } catch (OXException e) {
                                // Ignore
                            }
                        }
                    }
                }
            }
        });
    }

    private void indexDocument(final Context context, final int userId, final int id, final long origFolderId, final boolean isCreation) {
        if (false == INDEXING_ENABLED) {
            return;
        }
        ThreadPoolService threadPoolService = ServerServiceRegistry.getInstance().getService(ThreadPoolService.class);
        ExecutorService executorService = threadPoolService.getExecutor();
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                IndexFacadeService indexFacade = ServerServiceRegistry.getInstance().getService(IndexFacadeService.class);
                if (indexFacade != null) {
                    IndexAccess<DocumentMetadata> infostoreIndex = null;
                    IndexAccess<Attachment> attachmentIndex = null;
                    try {
                        LOG.debug("Indexing infostore document");

                        infostoreIndex = indexFacade.acquireIndexAccess(Types.INFOSTORE, userId, context.getContextId());
                        attachmentIndex = indexFacade.acquireIndexAccess(Types.ATTACHMENT, userId, context.getContextId());
                        DocumentMetadata document = load(id, CURRENT_VERSION, context);
                        if (isCreation) {
                            addToIndex(document, infostoreIndex, attachmentIndex);
                        } else {
                            if (origFolderId > 0) {
                                // folder move
                                infostoreIndex.deleteById(InfostoreUUID.newUUID(context.getContextId(), userId, origFolderId, id).toString());
                                attachmentIndex.deleteById(AttachmentUUID.newUUID(
                                    context.getContextId(),
                                    userId,
                                    Types.INFOSTORE,
                                    IndexConstants.DEFAULT_ACCOUNT,
                                    String.valueOf(origFolderId),
                                    String.valueOf(id),
                                    IndexConstants.DEFAULT_ATTACHMENT).toString());

                                addToIndex(document, infostoreIndex, attachmentIndex);
                            } else {
                                infostoreIndex.deleteById(InfostoreUUID.newUUID(context.getContextId(), userId, document.getFolderId(), id).toString());
                                attachmentIndex.deleteById(AttachmentUUID.newUUID(
                                    context.getContextId(),
                                    userId,
                                    Types.INFOSTORE,
                                    IndexConstants.DEFAULT_ACCOUNT,
                                    String.valueOf(document.getFolderId()),
                                    String.valueOf(id),
                                    IndexConstants.DEFAULT_ATTACHMENT).toString());

                                addToIndex(document, infostoreIndex, attachmentIndex);
                            }
                        }
                    } catch (Exception e) {
                        if ((e instanceof OXException) && (IndexExceptionCodes.INDEX_LOCKED.equals((OXException) e) || IndexExceptionCodes.INDEXING_NOT_ENABLED.equals((OXException) e))) {
                            LOG.debug("Could index document to infostore index.");
                        } else {
                            LOG.error("Error while indexing document.", e);
                        }
                    } finally {
                        if (infostoreIndex != null) {
                            try {
                                indexFacade.releaseIndexAccess(infostoreIndex);
                            } catch (OXException e) {
                            }
                        }

                        if (attachmentIndex != null) {
                            try {
                                indexFacade.releaseIndexAccess(attachmentIndex);
                            } catch (OXException e) {
                            }
                        }
                    }
                }
            }

            private void addToIndex(DocumentMetadata document, IndexAccess<DocumentMetadata> infostoreIndex, IndexAccess<Attachment> attachmentIndex) throws OXException {
                IndexDocument<DocumentMetadata> indexDocument = new StandardIndexDocument<DocumentMetadata>(document);
                infostoreIndex.addDocument(indexDocument);

                String filestoreLocation = document.getFilestoreLocation();
                if (filestoreLocation != null) {
                    FileStorage fileStorage = getFileStorage(security.getFolderOwner(document, context), context.getContextId());
                    InputStream file = fileStorage.getFile(filestoreLocation);
                    Attachment attachment = new Attachment();
                    attachment.setModule(Types.INFOSTORE);
                    attachment.setAccount(IndexConstants.DEFAULT_ACCOUNT);
                    attachment.setFolder(String.valueOf(document.getFolderId()));
                    attachment.setObjectId(String.valueOf(id));
                    attachment.setAttachmentId(IndexConstants.DEFAULT_ATTACHMENT);
                    attachment.setFileName(document.getFileName());
                    attachment.setFileSize(document.getFileSize());
                    attachment.setMimeType(document.getFileMIMEType());
                    attachment.setMd5Sum(document.getFileMD5Sum());
                    attachment.setContent(file);

                    attachmentIndex.addDocument(new StandardIndexDocument<Attachment>(attachment));
                }
            }
        });
    }

}
