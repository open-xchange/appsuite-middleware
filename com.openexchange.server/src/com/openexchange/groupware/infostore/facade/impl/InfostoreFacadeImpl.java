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
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.ReuseReadConProvider;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.index.Attachment;
import com.openexchange.groupware.attach.index.AttachmentUUID;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.infostore.DocumentMetadata;
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
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.collections.Injector;
import com.openexchange.tools.file.AppendFileAction;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.file.SaveFileAction;
import com.openexchange.tools.iterator.CombinedSearchIterator;
import com.openexchange.tools.iterator.CustomizableSearchIterator;
import com.openexchange.tools.iterator.Customizer;
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

    public static final InfostoreQueryCatalog QUERIES = new InfostoreQueryCatalog();

    private final DatabaseImpl db = new DatabaseImpl();

    protected InfostoreSecurity security = new InfostoreSecurityImpl();

    private final EntityLockManager lockManager = new EntityLockManagerImpl("infostore_lock");

    private final ThreadLocal<List<String>> fileIdRemoveList = new ThreadLocal<List<String>>();

    private final ThreadLocal<Context> ctxHolder = new ThreadLocal<Context>();

    private final TouchInfoitemsWithExpiredLocksListener expiredLocksListener;

    public InfostoreFacadeImpl() {
        super();
        expiredLocksListener = new TouchInfoitemsWithExpiredLocksListener(null, this);
        lockManager.addExpiryListener(expiredLocksListener);
    }

    public InfostoreFacadeImpl(final DBProvider provider) {
        this();
        setProvider(provider);
    }

    public void setSecurity(final InfostoreSecurity security) {
        this.security = security;
        if (null != getProvider()) {
            setProvider(getProvider());
        }
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
        final User user = session.getUser();
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(id, session.getContext(), user, session.getUserPermissionBits());
        if (!infoPerm.canReadObject()) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        }

        final List<Lock> locks = lockManager.findLocks(id, session);
        final Map<Integer, List<Lock>> allLocks = new HashMap<Integer, List<Lock>>();
        allLocks.put(Integer.valueOf(id), locks);

        DocumentMetadata document = load(id, version, session.getContext());
        if (!infoPerm.canReadObjectInFolder() && infoPerm.canReadObject()) {
            document.setFolderId(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
        }
        return addObjectPermissions(addNumberOfVersions(addLocked(document, allLocks, session), session.getContext()), session.getContext(), null);
    }

    @Override
    public void saveDocumentMetadata(final DocumentMetadata document, final long sequenceNumber, final ServerSession session) throws OXException {
        saveDocument(document, null, sequenceNumber, session);
    }

    @Override
    public void saveDocumentMetadata(final DocumentMetadata document, final long sequenceNumber, final Metadata[] modifiedColumns, final ServerSession session) throws OXException {
        saveDocument(document, null, sequenceNumber, modifiedColumns, session);
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
        final FileStorage fs = getFileStorage(session.getContext());
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
        final Context context = session.getContext();
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(
            id,
            context,
            getUser(session),
            session.getUserPermissionBits());
        if (!infoPerm.canWriteObject()) {
            throw InfostoreExceptionCodes.WRITE_PERMS_FOR_LOCK_MISSING.create();
        }
        final DocumentMetadata document = checkWriteLock(id, session);
        if (lockManager.isLocked(document.getId(), session.getContext(), getUser(session))) {
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
            getUser(session));
        touch(id, session);
    }

    @Override
    public void unlock(final int id, final ServerSession session) throws OXException {
        final Context context = session.getContext();
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(
            id,
            context,
            getUser(session),
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
            usage = getFileStorage(session.getContext()).getUsage();
        }
        return new com.openexchange.file.storage.Quota(limit, usage, com.openexchange.file.storage.Quota.Type.FILE);
    }

    @Override
    public com.openexchange.file.storage.Quota getStorageQuota(ServerSession session) throws OXException {
        long limit = com.openexchange.file.storage.Quota.UNLIMITED;
        long usage = com.openexchange.file.storage.Quota.UNLIMITED;
        try {
            limit = getFileStorage(session.getContext()).getQuota();
        } catch (OXException e) {
            LOG.warn("Error getting file storage quota for context {}", session.getContextId(), e);
        }
        if (com.openexchange.file.storage.Quota.UNLIMITED != limit) {
            usage = getFileStorage(session.getContext()).getUsage();
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

    private Delta<DocumentMetadata> addLocked(final Delta<DocumentMetadata> delta, final Map<Integer, List<Lock>> locks, final ServerSession session) throws OXException {
        try {
            return new LockDelta(delta, locks, session);
        } catch (final OXException e) {
            throw InfostoreExceptionCodes.ITERATE_FAILED.create(e);
        }
    }

    private Delta<DocumentMetadata> addNumberOfVersions(final Delta<DocumentMetadata> delta, final Context ctx) throws OXException {
        try {
            return new NumberOfVersionsDelta(delta, ctx);
        } catch (final OXException e) {
            throw InfostoreExceptionCodes.ITERATE_FAILED.create(e);
        }
    }

    private TimedResult<DocumentMetadata> addNumberOfVersions(final TimedResult<DocumentMetadata> tr, final Context ctx) throws OXException {
        return new NumberOfVersionsTimedResult(tr, ctx);
    }

    private TimedResult<DocumentMetadata> addLocked(final TimedResult<DocumentMetadata> tr, final ServerSession session) throws OXException {
        try {
            return new LockTimedResult(tr, session);
        } catch (final OXException e) {
            throw InfostoreExceptionCodes.ITERATE_FAILED.create(e);
        }
    }

    private DocumentMetadata addNumberOfVersions(final DocumentMetadata document, final Context ctx) throws OXException {

        try {
            return performQuery(ctx, QUERIES.getNumberOfVersionsQueryForOneDocument(), new ResultProcessor<DocumentMetadata>() {

                @Override
                public DocumentMetadata process(final ResultSet rs) throws SQLException {
                    if (!rs.next()) {
                        LOG.error("Infoitem disappeared when trying to count versions");
                        return document;
                    }
                    int numberOfVersions = rs.getInt(1);
                    numberOfVersions -= 1; // ignore version 0 in count
                    document.setNumberOfVersions(numberOfVersions);
                    return document;
                }
            }, Integer.valueOf(document.getId()), Integer.valueOf(ctx.getContextId()));
        } catch (final SQLException e) {
            LOG.error("", e);
            throw InfostoreExceptionCodes.NUMBER_OF_VERSIONS_FAILED.create(
                e,
                I(document.getId()),
                I(ctx.getContextId()),
                QUERIES.getNumberOfVersionsQueryForOneDocument());
        }
    }

    private DocumentMetadata addLocked(final DocumentMetadata document, final Map<Integer, List<Lock>> allLocks, final ServerSession session) throws OXException {
        List<Lock> locks = null;
        if (allLocks != null) {
            locks = allLocks.get(Integer.valueOf(document.getId()));
        } else {
            locks = lockManager.findLocks(document.getId(), session);
        }
        if (locks == null) {
            locks = Collections.emptyList();
        }
        long max = 0;
        for (final Lock l : locks) {
            if (l.getTimeout() > max) {
                max = l.getTimeout();
            }
        }
        if (max > 0) {
            document.setLockedUntil(new Date(System.currentTimeMillis() + max));
        }
        return document;
    }

    /**
     * Adds object permissions to the supplied document.
     *
     * @param document The document to add the object permissions for
     * @param ctx The context
     * @param knownObjectPermissions A map of already known object permissions, or <code>null</code> if not available
     * @return The document, with added object permissions (in case they are set)
     * @throws OXException
     */
    private DocumentMetadata addObjectPermissions(final DocumentMetadata document, final Context ctx, Map<Integer, List<ObjectPermission>> knownObjectPermissions) throws OXException {
        Integer id = Integer.valueOf(document.getId());
        if (null != knownObjectPermissions && knownObjectPermissions.containsKey(id)) {
            document.setObjectPermissions(knownObjectPermissions.get(id));
            return document;
        }
        Map<Integer, List<ObjectPermission>> objectPermissions = loadObjectPermissions(Collections.singleton(id), ctx);
        document.setObjectPermissions(objectPermissions.get(id));
        return document;
    }

    /**
     * Adds object permissions to the supplied documents.
     *
     * @param document The documents to add the object permissions for
     * @param ctx The context
     * @return The document, with added object permissions (in case they are set)
     * @throws OXException
     */
    private List<DocumentMetadata> addObjectPermissions(List<DocumentMetadata> documents, Context ctx, Map<Integer, List<ObjectPermission>> knownObjectPermissions) throws OXException {
        List<Integer> idsToQuery = new ArrayList<Integer>();
        for (DocumentMetadata document : documents) {
            Integer id = Integer.valueOf(document.getId());
            if (null != knownObjectPermissions && knownObjectPermissions.containsKey(id)) {
                document.setObjectPermissions(knownObjectPermissions.get(id));
            } else {
                idsToQuery.add(id);
            }
        }
        if (0 < idsToQuery.size()) {
            Map<Integer, List<ObjectPermission>> objectPermissions = loadObjectPermissions(idsToQuery, ctx);
            for (DocumentMetadata document : documents) {
                Integer id = Integer.valueOf(document.getId());
                if (objectPermissions.containsKey(id)) {
                    document.setObjectPermissions(objectPermissions.get(id));
                }
            }
        }
        return documents;
    }

    SearchIterator<DocumentMetadata> numberOfVersionsIterator(final SearchIterator<?> iter, final Context ctx) throws OXException {
        final List<DocumentMetadata> list = new ArrayList<DocumentMetadata>();
        while (iter.hasNext()) {
            final DocumentMetadata m = (DocumentMetadata) iter.next();
            // addLocked(m, ctx, user, userConfig);
            list.add(m);
        }
        for (final DocumentMetadata m : list) {
            addNumberOfVersions(m, ctx);
        }
        return new SearchIteratorAdapter<DocumentMetadata>(list.iterator());
    }

    /**
     * Wraps the supplied iterator into a search iterator that dynamically injects object permissions to the documents.
     *
     * @param delegate The iterator to wrap
     * @param ctx The context
     * @param knownObjectPermissions A map of already known object permissions
     * @return The wrapped iterator
     * @throws OXException
     */
    SearchIterator<DocumentMetadata> objectPermissionsIterator(SearchIterator<DocumentMetadata> delegate, final Context ctx,
        final Map<Integer, List<ObjectPermission>> knownObjectPermissions) throws OXException {
        return new CustomizableSearchIterator<DocumentMetadata>(delegate, new Customizer<DocumentMetadata>() {

            @Override
            public DocumentMetadata customize(DocumentMetadata thing) throws OXException {
                return addObjectPermissions(thing, ctx, knownObjectPermissions);
            }
        });
    }

    SearchIterator<DocumentMetadata> lockedUntilIterator(final SearchIterator<?> iter, final Map<Integer, List<Lock>> locks, final ServerSession session) throws OXException {
        final List<DocumentMetadata> list = new ArrayList<DocumentMetadata>();
        while (iter.hasNext()) {
            final DocumentMetadata m = (DocumentMetadata) iter.next();
            // addLocked(m, ctx, user, userConfig);
            list.add(m);
        }
        /*
         * Moving addLock() outside of iterator's loop to avoid a duplicate connection fetch from DB pool since first invocation of
         * hasNext() already obtains a pooled connection which is only released on final call to next().
         */
        for (final DocumentMetadata m : list) {
            addLocked(m, locks, session);
        }
        return new SearchIteratorAdapter<DocumentMetadata>(list.iterator());
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

        if (lockManager.isLocked(document.getId(), session.getContext(), getUser(session))) {
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
    public void saveDocument(final DocumentMetadata document, final InputStream data, final long sequenceNumber, final ServerSession session) throws OXException {
        final Context context = session.getContext();
        security.checkFolderId(document.getFolderId(), context);

        boolean wasCreation = false;
        if (document.getId() == InfostoreFacade.NEW) {
            wasCreation = true;
            final EffectivePermission isperm = security.getFolderPermission(
                document.getFolderId(),
                context,
                getUser(session),
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
                    SaveFileAction saveFile = new SaveFileAction(getFileStorage(context), data, document.getFileSize());
                    perform(saveFile, false);
                    document.setVersion(1);
                    document.setFilestoreLocation(saveFile.getFileStorageID());
                    document.setFileMD5Sum(saveFile.getChecksum());
                    document.setFileSize(saveFile.getByteCount());

                    perform(new CreateVersionAction(this, QUERIES, context, Collections.singletonList(document)), true);
                }

                indexDocument(context, session.getUserId(), document.getId(), -1L, wasCreation);
            } finally {
                if (reservation != null) {
                    reservation.destroySilently();
                }
            }
        } else {
            saveDocument(document, data, sequenceNumber, nonNull(document), session);
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

    protected QuotaFileStorage getFileStorage(final Context ctx) throws OXException {
        return QuotaFileStorage.getInstance(FilestoreStorage.createURI(ctx), ctx);
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
    public void saveDocument(final DocumentMetadata document, final InputStream data, final long sequenceNumber, final Metadata[] modifiedColumns, final ServerSession session) throws OXException {
        saveDocument(document, data, sequenceNumber, modifiedColumns, false, session);
    }

    @Override
    public void saveDocument(final DocumentMetadata document, final InputStream data, final long sequenceNumber, final Metadata[] modifiedColumns, final boolean ignoreVersion, final ServerSession session) throws OXException {
        saveDocument(document, data, sequenceNumber, modifiedColumns, ignoreVersion, -1L, session);
    }

    @Override
    public void saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, long offset, final ServerSession session) throws OXException {
        saveDocument(document, data, sequenceNumber, modifiedColumns, true, offset, session);
    }

    protected void saveDocument(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, boolean ignoreVersion, long offset, final ServerSession session) throws OXException {
        if (0 < offset && (NEW == document.getId() || false == ignoreVersion)) {
            throw InfostoreExceptionCodes.NO_OFFSET_FOR_NEW_VERSIONS.create();
        }
        if (document.getId() == NEW) {
            saveDocument(document, data, sequenceNumber, session);
            indexDocument(session.getContext(), session.getUserId(), document.getId(), -1L, true);
            return;
        }
        final Context context = session.getContext();
        // Check permission
        {
            final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(
                document.getId(),
                context,
                getUser(session),
                session.getUserPermissionBits());
            if (!infoPerm.canWriteObject()) {
                throw InfostoreExceptionCodes.NO_WRITE_PERMISSION.create();
            }

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
                    final EffectivePermission isperm = security.getFolderPermission(
                        document.getFolderId(),
                        context,
                        getUser(session),
                        session.getUserPermissionBits());
                    if (!(isperm.canCreateObjects())) {
                        throw InfostoreExceptionCodes.NO_CREATE_PERMISSION.create();
                    }
                    if (!infoPerm.canDeleteObject()) {
                        throw InfostoreExceptionCodes.NO_SOURCE_DELETE_PERMISSION.create();
                    }
                }
            }
        }

        CheckSizeSwitch.checkSizes(document, getProvider(), context);

        final DocumentMetadata oldDocument = addObjectPermissions(checkWriteLock(document.getId(), session), session.getContext(), null);

        Metadata[] modifiedCols = modifiedColumns;
        final Set<Metadata> updatedCols = new HashSet<Metadata>(Arrays.asList(modifiedCols));
        if (!updatedCols.contains(Metadata.LAST_MODIFIED_LITERAL)) {
            document.setLastModified(new Date());
        }
        document.setModifiedBy(session.getUserId());

        VALIDATION.validate(document);

        // db.updateDocument(document, data, sequenceNumber,
        // modifiedColumns, sessionObj.getContext(),
        // sessionObj.getUserObject(), getUserConfiguration(sessionObj));

        // db.createDocument(document, data, sessionObj.getContext(),
        // sessionObj.getUserObject(), getUserConfiguration(sessionObj));

        updatedCols.add(Metadata.LAST_MODIFIED_LITERAL);
        updatedCols.add(Metadata.MODIFIED_BY_LITERAL);

        final List<InfostoreFilenameReservation> reservations = new ArrayList<InfostoreFilenameReservation>(2);
        try {
            if (updatedCols.contains(Metadata.VERSION_LITERAL)) {
                final String fname = load(document.getId(), document.getVersion(), context).getFileName();
                if (!updatedCols.contains(Metadata.FILENAME_LITERAL)) {
                    updatedCols.add(Metadata.FILENAME_LITERAL);
                    document.setFileName(fname);
                }
            }

            final String oldFileName = oldDocument.getFileName();
            if (updatedCols.contains(Metadata.FOLDER_ID_LITERAL) && oldDocument.getFolderId() != document.getFolderId()) {
                // this is a move - reserve in target folder
                String fileName = null != document.getFileName() ? document.getFileName() : oldFileName;
                final InfostoreFilenameReservation reservation = reserve(
                    fileName,
                    document.getFolderId(),
                    oldDocument.getId(),
                    context, true);
                reservations.add(reservation);
                document.setFileName(reservation.getFilename());
                updatedCols.add(Metadata.FILENAME_LITERAL);
            } else if (updatedCols.contains(Metadata.FILENAME_LITERAL) && null != document.getFileName() &&
                false == document.getFileName().equals(oldFileName)) {
                // this is a rename - reserve in current folder
                final InfostoreFilenameReservation reservation = reserve(
                    document.getFileName(),
                    oldDocument.getFolderId(),
                    oldDocument.getId(),
                    context, true);
                reservations.add(reservation);
                document.setFileName(reservation.getFilename());
                updatedCols.add(Metadata.FILENAME_LITERAL);
            }

            final String oldTitle = oldDocument.getTitle();
            if (!updatedCols.contains(Metadata.TITLE_LITERAL) && oldFileName != null && oldTitle != null && oldFileName.equals(oldTitle)) {
                final String fileName = document.getFileName();
                if (null == fileName) {
                    document.setTitle(oldFileName);
                    document.setFileName(oldFileName);
                    updatedCols.add(Metadata.FILENAME_LITERAL);
                } else {
                    document.setTitle(fileName);
                }
                updatedCols.add(Metadata.TITLE_LITERAL);
            }

            modifiedCols = updatedCols.toArray(new Metadata[updatedCols.size()]);

            if (data != null) {
                QuotaFileStorage qfs = getFileStorage(context);
                if (0 < offset) {
                    AppendFileAction appendFile = new AppendFileAction(
                        qfs, data, oldDocument.getFilestoreLocation(), document.getFileSize(), offset);
                    perform(appendFile, false);
                    document.setFilestoreLocation(oldDocument.getFilestoreLocation());
                    document.setFileSize(appendFile.getByteCount() + offset);
                    document.setFileMD5Sum(null); // invalidate due to append-operation
                    updatedCols.addAll(Arrays.asList(Metadata.FILE_MD5SUM_LITERAL, Metadata.FILE_SIZE_LITERAL));
                } else {
                    SaveFileAction saveFile = new SaveFileAction(qfs, data, document.getFileSize());
                    perform(saveFile, false);
                    document.setFilestoreLocation(saveFile.getFileStorageID());
                    document.setFileSize(saveFile.getByteCount());
                    document.setFileMD5Sum(saveFile.getChecksum());
                    updatedCols.addAll(Arrays.asList(
                        Metadata.FILE_MD5SUM_LITERAL, Metadata.FILE_SIZE_LITERAL, Metadata.FILESTORE_LOCATION_LITERAL));
                }

                final GetSwitch get = new GetSwitch(oldDocument);
                final SetSwitch set = new SetSwitch(document);
                final Set<Metadata> alreadySet = new HashSet<Metadata>(Arrays.asList(modifiedCols));
                for (final Metadata m : Arrays.asList(Metadata.DESCRIPTION_LITERAL, Metadata.TITLE_LITERAL, Metadata.FILENAME_LITERAL, Metadata.URL_LITERAL)) {
                    if (alreadySet.contains(m)) {
                        continue;
                    }
                    set.setValue(m.doSwitch(get));
                    m.doSwitch(set);
                }

                document.setCreatedBy(session.getUserId());
                if (!updatedCols.contains(Metadata.CREATION_DATE_LITERAL)) {
                    document.setCreationDate(new Date());
                }

                // Set version
                final UndoableAction action;
                if (ignoreVersion) {
                    document.setVersion(oldDocument.getVersion());
                    updatedCols.add(Metadata.VERSION_LITERAL);
                    updatedCols.add(Metadata.FILESTORE_LOCATION_LITERAL);
                    action = new UpdateVersionAction(this, QUERIES, context, document, oldDocument,
                        updatedCols.toArray(new Metadata[updatedCols.size()]), sequenceNumber);

                    // Remove old file "version" if not appended
                    if (0 >= offset) {
                        removeFile(context, oldDocument.getFilestoreLocation());
                    }
                } else {
                    Connection con = null;
                    try {
                        con = getReadConnection(context);
                        document.setVersion(getNextVersionNumberForInfostoreObject(
                            context.getContextId(),
                            document.getId(),
                            con));
                        updatedCols.add(Metadata.VERSION_LITERAL);
                    } catch (final SQLException e) {
                        LOG.error("SQLException: ", e);
                    } finally {
                        releaseReadConnection(context, con);
                    }

                    action = new CreateVersionAction(this, QUERIES, context, Collections.singletonList(document));
                }
                // Perform action
                perform(action, true);
            } else if (QUERIES.updateVersion(modifiedCols)) {
                if (!updatedCols.contains(Metadata.VERSION_LITERAL)) {
                    document.setVersion(oldDocument.getVersion());
                }
                perform(new UpdateVersionAction(this, QUERIES, context, document, oldDocument, modifiedCols, sequenceNumber), true);
            }

            modifiedCols = updatedCols.toArray(new Metadata[updatedCols.size()]);
            if (QUERIES.updateDocument(modifiedCols)) {
                perform(new UpdateDocumentAction(this, QUERIES, context, document, oldDocument, modifiedCols, Long.MAX_VALUE), true);
            }
            /*
             * update object permissions as needed
             */
            if (updatedCols.contains(Metadata.OBJECT_PERMISSIONS_LITERAL)) {
                perform(new UpdateObjectPermissionAction(this, context, document, oldDocument), true);
            }

            // insert tombstone row to del_infostore table in case of move operations to aid folder based synchronizations
            if (updatedCols.contains(Metadata.FOLDER_ID_LITERAL) && oldDocument.getFolderId() != document.getFolderId()) {
                DocumentMetadataImpl tombstoneDocument = new DocumentMetadataImpl(oldDocument);
                tombstoneDocument.setLastModified(document.getLastModified());
                tombstoneDocument.setModifiedBy(document.getModifiedBy());
                perform(new ReplaceDocumentIntoDelTableAction(this, QUERIES, session.getContext(), tombstoneDocument), true);
            }

            final long indexFolderId = document.getFolderId() == oldDocument.getFolderId() ? -1L : oldDocument.getFolderId();
            indexDocument(context, session.getUserId(), oldDocument.getId(), indexFolderId, false);
        } finally {
            for (final InfostoreFilenameReservation infostoreFilenameReservation : reservations) {
                infostoreFilenameReservation.destroySilently();
            }
        }
    }

    @Override
    public void removeDocument(final long folderId, final long date, final ServerSession session) throws OXException {
        if (folderId == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
            throw InfostoreExceptionCodes.NO_DELETE_PERMISSION.create();
        }

        final DBProvider reuseProvider = new ReuseReadConProvider(getProvider());
        final List<DocumentMetadata> allVersions = InfostoreIterator.allVersionsWhere(
            "infostore.folder_id = " + folderId,
            Metadata.VALUES_ARRAY,
            reuseProvider,
            session.getContext()).asList();
        final List<DocumentMetadata> allDocuments = InfostoreIterator.allDocumentsWhere(
            "infostore.folder_id = " + folderId,
            Metadata.VALUES_ARRAY,
            reuseProvider,
            session.getContext()).asList();
        addObjectPermissions(allDocuments, session.getContext(), null);
        removeDocuments(allDocuments, allVersions, date, session, null);
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
        for (final DocumentMetadata m : allVersions) {
            if (!rejectedIds.contains(Integer.valueOf(m.getId()))) {
                delVers.add(m);
                m.setLastModified(now);
                if (null != m.getFilestoreLocation()) {
                    filestoreLocations.add(m.getFilestoreLocation());
                }
            }
        }
        removeFiles(context, filestoreLocations);

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
     * @throws OXException
     */
    private void removeFile(final Context context, final String filestoreLocation) throws OXException {
        removeFiles(context, Collections.singletonList(filestoreLocation));
    }

    /**
     * Removes the supplied files from the underlying storage. If a transaction is active, the files are remembered to be deleted during
     * the {@link #commit()}-phase - otherwise they're deleted from the storage directly.
     *
     * @param context The context
     * @param filestoreLocations A list of locations referencing the files to be deleted in the storage
     * @throws OXException
     */
    private void removeFiles(Context context, List<String> filestoreLocations) throws OXException {
        if (null != filestoreLocations && 0 < filestoreLocations.size()) {
            List<String> removeList = fileIdRemoveList.get();
            if (null != removeList) {
                removeList.addAll(filestoreLocations);
                ctxHolder.set(context);
            } else {
                getFileStorage(context).deleteFiles(filestoreLocations.toArray(new String[filestoreLocations.size()]));
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
        EffectivePermission destinationFolderPermission = security.getFolderPermission(destinationFolderID, context, user, permissionBits);
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
            boolean moveToTrash = FolderObject.TRASH == new OXFolderAccess(getReadConnection(context), context)
                .getFolderObject((int) destinationFolderID).getType();
            Date now = new Date();
            BatchFilenameReserver filenameReserver = new BatchFilenameReserverImpl(session.getContext(), this);
            try {
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
            }
        }
        /*
         * return rejected documents
         */
        return rejectedDocuments;
    }

    @Override
    public int[] moveDocuments(ServerSession session, int[] ids, long sequenceNumber, String targetFolderID, boolean adjustFilenamesAsNeeded) throws OXException {
        if (null == ids || 0 == ids.length) {
            return new int[0];
        }
        long destinationFolderID;
        try {
            destinationFolderID = Long.valueOf(targetFolderID).longValue();
        } catch (NumberFormatException e) {
            throw InfostoreExceptionCodes.NOT_INFOSTORE_FOLDER.create(targetFolderID, e);
        }
        /*
         * get documents to move
         */
        DBProvider reuseProvider = new ReuseReadConProvider(getProvider());
        List<DocumentMetadata> allDocuments = getAllDocuments(reuseProvider, session.getContext(), ids, Metadata.VALUES_ARRAY);
        addObjectPermissions(allDocuments, session.getContext(), null);
        /*
         * perform move
         */
        List<DocumentMetadata> rejectedDocuments = moveDocuments(
            session, allDocuments, destinationFolderID, sequenceNumber, adjustFilenamesAsNeeded);
        if (null == rejectedDocuments || 0 == rejectedDocuments.size()) {
            return new int[0];
        }
        int[] rejectedIDs = new int[rejectedDocuments.size()];
        for (int i = 0; i < rejectedIDs.length; i++) {
            rejectedIDs[i] = rejectedDocuments.get(i).getId();
        }
        return rejectedIDs;
    }

    @Override
    public int[] removeDocument(final int[] ids, final long date, final ServerSession session) throws OXException {
        if (ids == null || ids.length == 0) {
            return new int[0];
        }

        final Set<Integer> idSet = new HashSet<Integer>();
        for (final int i : ids) {
            idSet.add(Integer.valueOf(i));
        }

        final Context context = session.getContext();
        final User user = session.getUser();
        final UserPermissionBits userPermissionBits = session.getUserPermissionBits();

        final DBProvider reuseProvider = new ReuseReadConProvider(getProvider());
        List<DocumentMetadata> allVersions = null;
        List<DocumentMetadata> allDocuments = null;
        try {
            final StringBuilder sIds = new StringBuilder().append('(');
            Strings.join(idSet, ", ", sIds);
            sIds.append(')');

            allVersions = InfostoreIterator.allVersionsWhere(
                "infostore.id IN " + sIds.toString(),
                Metadata.VALUES_ARRAY,
                reuseProvider,
                context).asList();
            allDocuments = InfostoreIterator.allDocumentsWhere(
                "infostore.id IN " + sIds.toString(),
                Metadata.VALUES_ARRAY,
                reuseProvider,
                context).asList();
            addObjectPermissions(allDocuments, context, null);
        } catch (final OXException x) {
            throw x;
        } catch (final Throwable t) {
            LOG.error("Unexpected Error:", t);
        }

        // Check Permissions
        List<EffectiveInfostorePermission> permissions = security.getInfostorePermissions(allDocuments, context, user, userPermissionBits);
        for (EffectiveInfostorePermission permission : permissions) {
            if (!permission.canDeleteObject()) {
                throw InfostoreExceptionCodes.NO_DELETE_PERMISSION.create();
            }
        }

        final Set<Integer> unknownDocuments = new HashSet<Integer>(idSet);
        for (DocumentMetadata document : allDocuments) {
            unknownDocuments.remove(document.getId());
        }

        final List<DocumentMetadata> rejected = new ArrayList<DocumentMetadata>();
        removeDocuments(allDocuments, allVersions, date, session, rejected);

        final int[] nd = new int[rejected.size() + unknownDocuments.size()];
        int i = 0;
        for (final DocumentMetadata rej : rejected) {
            nd[i++] = rej.getId();
        }
        for (final int notFound : unknownDocuments) {
            nd[i++] = notFound;
        }

        return nd;
    }

    /**
     * Loads all stored object permissions for one or more documents from the database.
     *
     * @param ids The identifiers of the documents to get the object permissions for
     * @param ctx The context
     * @return A map holding the object permissions (or <code>null</code>) to a document's id
     * @throws OXException
     */
    private Map<Integer, List<ObjectPermission>> loadObjectPermissions(Collection<Integer> ids, Context ctx) throws OXException {
        if (null == ids || 0 == ids.size()) {
            return Collections.emptyMap();
        }
        final Map<Integer, List<ObjectPermission>> objectPermissions = new HashMap<Integer, List<ObjectPermission>>(ids.size());
        List<Object> parameters = new ArrayList<Object>(ids.size() + 1);
        parameters.add(Integer.valueOf(ctx.getContextId()));
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT object_id,permission_id,group_flag,bits FROM object_permission WHERE cid=? AND object_id");
        if (1 == ids.size()) {
            stringBuilder.append("=?;");
            Integer id = ids.iterator().next();
            parameters.add(id);
            objectPermissions.put(id, null);
        } else {
            Iterator<Integer> iterator = ids.iterator();
            stringBuilder.append(" IN (?");
            Integer id = iterator.next();
            parameters.add(id);
            objectPermissions.put(id, null);
            do {
                stringBuilder.append(",?");
                id = iterator.next();
                parameters.add(id);
                objectPermissions.put(id, null);
            } while (iterator.hasNext());
            stringBuilder.append(");");
        }
        try {
            performQuery(ctx, stringBuilder.toString(), new ResultProcessor<Void>() {

                @Override
                public Void process(ResultSet rs) throws SQLException {
                    while (rs.next()) {
                        Integer id = Integer.valueOf(rs.getInt(1));
                        List<ObjectPermission> permissions = objectPermissions.get(id);
                        if (null == permissions) {
                            permissions = new ArrayList<ObjectPermission>();
                            objectPermissions.put(id, permissions);
                        }
                        permissions.add(new ObjectPermission(rs.getInt(2), rs.getBoolean(3), rs.getInt(4)));
                    }
                    return null;
                }
            }, parameters.toArray(new Object[parameters.size()]));
        } catch (SQLException e) {
            LOG.error("", e);
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
        return objectPermissions;
    }

    @Override
    public int[] removeVersion(final int id, final int[] versionIds, final ServerSession session) throws OXException {
        if (null == versionIds || 0 == versionIds.length) {
            return new int[0];
        }

        final Context context = session.getContext();

        DocumentMetadata metadata = load(id, InfostoreFacade.CURRENT_VERSION, context);
        try {
            checkWriteLock(metadata, session);
        } catch (final OXException x) {
            return versionIds;
        }
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(
            metadata,
            context,
            getUser(session),
            session.getUserPermissionBits());
        if (!infoPerm.canDeleteObject()) {
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
            removeFile(context, v.getFilestoreLocation());
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
        User user = session.getUser();
        final EffectivePermission isperm = security.getFolderPermission(folderId, session.getContext(), user, session.getUserPermissionBits());
        if (isperm.getReadPermission() == OCLPermission.NO_PERMISSIONS) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        } else if (isperm.getReadPermission() == OCLPermission.READ_OWN_OBJECTS) {
            onlyOwn = true;
        }
        boolean addLocked = false;
        boolean addNumberOfVersions = false;
        for (final Metadata m : cols) {
            if (m == Metadata.LOCKED_UNTIL_LITERAL) {
                addLocked = true;
                break;
            }
            if (m == Metadata.NUMBER_OF_VERSIONS_LITERAL) {
                addNumberOfVersions = true;
                break;
            }
        }

        InfostoreIterator iter = null;
        if (onlyOwn) {
            iter = InfostoreIterator.documentsByCreator(folderId, user.getId(), cols, sort, order, getProvider(), session.getContext());
        } else {
            iter = InfostoreIterator.documents(folderId, cols, sort, order, getProvider(), session.getContext());
        }
        TimedResult<DocumentMetadata> tr = new InfostoreTimedResult(iter);
        if (addLocked) {
            tr = addLocked(tr, session);
        }
        if (addNumberOfVersions) {
            tr = addNumberOfVersions(tr, session.getContext());
        }
        return tr;
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
        final User user = session.getUser();
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(id, session.getContext(), user, session.getUserPermissionBits());
        if (!infoPerm.canReadObject()) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        }
        boolean addLocked = false;
        for (final Metadata m : columns) {
            if (m == Metadata.LOCKED_UNTIL_LITERAL) {
                addLocked = true;
                break;
            }
        }
        Metadata[] cols = addLastModifiedIfNeeded(columns);
        final InfostoreIterator iter = InfostoreIterator.versions(id, cols, sort, order, getProvider(), session.getContext());
        iter.setCustomizer(new DocumentCustomizer() {
            @Override
            public DocumentMetadata handle(DocumentMetadata document) {
                if (!infoPerm.canReadObjectInFolder()) {
                    document.setFolderId(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
                }

                return document;
            }
        });
        final TimedResult<DocumentMetadata> tr = new InfostoreTimedResult(iter);

        if (addLocked) {
            return addLocked(tr, session);
        }
        return tr;

    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(final int[] ids, Metadata[] columns, final ServerSession session) throws OXException {
        final User user = session.getUser();
        final Map<Integer, EffectiveInfostorePermission> permissionsById;
        try {
            permissionsById = security.injectInfostorePermissions(ids, session.getContext(), user, session.getUserPermissionBits(), new HashMap<Integer, EffectiveInfostorePermission>(ids.length * 2), new Injector<Map<Integer, EffectiveInfostorePermission>, EffectiveInfostorePermission>() {

                @Override
                public Map<Integer, EffectiveInfostorePermission> inject(final Map<Integer, EffectiveInfostorePermission> permissions, final EffectiveInfostorePermission element) {
                    if (!element.canReadObject()) {
                        throw new NotAllowed(element.getObjectID());
                    }
                    permissions.put(element.getObjectID(), element);
                    return permissions;
                }

            });
        } catch (final NotAllowed na) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        }
        Metadata[] cols = addLastModifiedIfNeeded(columns);
        final InfostoreIterator iter = InfostoreIterator.list(ids, cols, getProvider(), session.getContext());
        iter.setCustomizer(new DocumentCustomizer() {
            @Override
            public DocumentMetadata handle(DocumentMetadata document) {
                EffectiveInfostorePermission infostorePermission = permissionsById.get(document.getId());
                if (infostorePermission != null) {
                    if (!infostorePermission.canReadObjectInFolder() && infostorePermission.canReadObject()) {
                        document.setFolderId(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
                    }
                }

                return document;
            }
        });
        TimedResult<DocumentMetadata> tr = new InfostoreTimedResult(iter);

        for (final Metadata m : cols) {
            if (m == Metadata.LOCKED_UNTIL_LITERAL) {
                tr = addLocked(tr, session);
            }
            if (m == Metadata.NUMBER_OF_VERSIONS_LITERAL) {
                tr = addNumberOfVersions(tr, session.getContext());
            }
        }
        return tr;

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
            final EffectivePermission isperm = security.getFolderPermission(folderId, context, user, session.getUserPermissionBits());
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

        final Map<Integer, List<Lock>> locks = loadLocksInFolderAndExpireOldLocks(folderId, session);
        Delta<DocumentMetadata> delta = new DeltaImpl<DocumentMetadata>(newIter, modIter, it, System.currentTimeMillis());
        if (addLocked) {
            delta = addLocked(delta, locks, session);
        }
        if (addNumberOfVersions) {
            delta = addNumberOfVersions(delta, context);
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
        final EffectivePermission isperm = security.getFolderPermission(folderId, session.getContext(), user, session.getUserPermissionBits());
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

    private static final class NotAllowed extends RuntimeException {

        private static final long serialVersionUID = 4872889537922290831L;

        private final int id;

        public NotAllowed(final int id) {
            this.id = id;
        }
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
        List<String> filesToRemove = fileIdRemoveList.get();
        if (null != filesToRemove && 0 < filesToRemove.size()) {
            getFileStorage(ctxHolder.get()).deleteFiles(filesToRemove.toArray(new String[filesToRemove.size()]));
        }
        super.commit();
    }

    @Override
    public void finish() throws OXException {
        fileIdRemoveList.set(null);
        ctxHolder.set(null);
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
        fileIdRemoveList.set(new ArrayList<String>());
        ctxHolder.set(null);
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

    private static final UserConfiguration getUserConfiguration(final ServerSession session) {
        return session.getUserConfiguration();
        //return UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), sessionObj.getContext());
    }

    private static final User getUser(final ServerSession session) {
        return session.getUser();
        //return UserStorage.getStorageUser(session.getUserId(), session.getContext());
    }

    private final class NumberOfVersionsTimedResult implements TimedResult<DocumentMetadata> {

        private final long sequenceNumber;

        private final SearchIterator<DocumentMetadata> results;

        public NumberOfVersionsTimedResult(final TimedResult<DocumentMetadata> delegate, final Context ctx) throws OXException {
            sequenceNumber = delegate.sequenceNumber();

            this.results = numberOfVersionsIterator(delegate.results(), ctx);
        }

        @Override
        public SearchIterator<DocumentMetadata> results() throws OXException {
            return results;
        }

        @Override
        public long sequenceNumber() throws OXException {
            return sequenceNumber;
        }

    }

    private final class ObjectPermissionsTimedResult implements TimedResult<DocumentMetadata> {

        private final long sequenceNumber;
        private final SearchIterator<DocumentMetadata> results;
        private final Map<Integer, List<ObjectPermission>> knownObjectPermissions;

        public ObjectPermissionsTimedResult(TimedResult<DocumentMetadata> delegate, Context ctx, final Map<Integer, List<ObjectPermission>> knownObjectPermissions) throws OXException {
            super();
            this.knownObjectPermissions = knownObjectPermissions;
            this.sequenceNumber = delegate.sequenceNumber();
            this.results = objectPermissionsIterator(delegate.results(), ctx, knownObjectPermissions);
        }

        @Override
        public SearchIterator<DocumentMetadata> results() throws OXException {
            return results;
        }

        @Override
        public long sequenceNumber() throws OXException {
            return sequenceNumber;
        }

    }

    private final class LockTimedResult implements TimedResult<DocumentMetadata> {

        private final long sequenceNumber;

        private final SearchIterator<DocumentMetadata> results;

        public LockTimedResult(final TimedResult<DocumentMetadata> delegate, final ServerSession session) throws OXException {
            sequenceNumber = delegate.sequenceNumber();

            this.results = lockedUntilIterator(delegate.results(), null, session);
        }

        @Override
        public SearchIterator<DocumentMetadata> results() throws OXException {
            return results;
        }

        @Override
        public long sequenceNumber() throws OXException {
            return sequenceNumber;
        }

    }

    private final class LockDelta implements Delta<DocumentMetadata> {

        private final long sequenceNumber;

        private final SearchIterator<DocumentMetadata> newIter;

        private final SearchIterator<DocumentMetadata> modified;

        private SearchIterator<DocumentMetadata> deleted;

        public LockDelta(final Delta<DocumentMetadata> delegate, final Map<Integer, List<Lock>> locks, final ServerSession session) throws OXException {
            final SearchIterator<DocumentMetadata> deleted = delegate.getDeleted();
            if (null != deleted) {
                this.deleted = lockedUntilIterator(deleted, locks, session);
            }
            this.modified = lockedUntilIterator(delegate.getModified(), locks, session);
            this.newIter = lockedUntilIterator(delegate.getNew(), locks, session);
            this.sequenceNumber = delegate.sequenceNumber();
        }

        @Override
        public SearchIterator<DocumentMetadata> getDeleted() {
            return deleted;
        }

        @Override
        public SearchIterator<DocumentMetadata> getModified() {
            return modified;
        }

        @Override
        public SearchIterator<DocumentMetadata> getNew() {
            return newIter;
        }

        @Override
        public SearchIterator<DocumentMetadata> results() throws OXException {
            return new CombinedSearchIterator<DocumentMetadata>(newIter, modified);
        }

        @Override
        public long sequenceNumber() throws OXException {
            return sequenceNumber;
        }

        public void close() throws OXException {
            newIter.close();
            modified.close();
            deleted.close();
        }

    }

    private final class NumberOfVersionsDelta implements Delta<DocumentMetadata> {

        private final long sequenceNumber;

        private final SearchIterator<DocumentMetadata> newIter;

        private final SearchIterator<DocumentMetadata> modified;

        private SearchIterator<DocumentMetadata> deleted;

        public NumberOfVersionsDelta(final Delta<DocumentMetadata> delegate, final Context ctx) throws OXException {
            final SearchIterator<DocumentMetadata> deleted = delegate.getDeleted();
            if (null != deleted) {
                this.deleted = numberOfVersionsIterator(deleted, ctx);
            }
            this.modified = numberOfVersionsIterator(delegate.getModified(), ctx);
            this.newIter = numberOfVersionsIterator(delegate.getNew(), ctx);
            this.sequenceNumber = delegate.sequenceNumber();
        }

        @Override
        public SearchIterator<DocumentMetadata> getDeleted() {
            return deleted;
        }

        @Override
        public SearchIterator<DocumentMetadata> getModified() {
            return modified;
        }

        @Override
        public SearchIterator<DocumentMetadata> getNew() {
            return newIter;
        }

        @Override
        public SearchIterator<DocumentMetadata> results() throws OXException {
            return new CombinedSearchIterator<DocumentMetadata>(newIter, modified);
        }

        @Override
        public long sequenceNumber() throws OXException {
            return sequenceNumber;
        }

        public void close() throws OXException {
            newIter.close();
            modified.close();
            deleted.close();
        }

    }

    private static interface ResultProcessor<T> {

        public T process(ResultSet rs) throws SQLException;
    }

    @Override
    public void setSessionHolder(final SessionHolder sessionHolder) {
        expiredLocksListener.setSessionHolder(sessionHolder);
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
                    FileStorage fileStorage = getFileStorage(context);
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
