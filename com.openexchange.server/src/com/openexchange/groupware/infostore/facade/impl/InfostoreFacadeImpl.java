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

package com.openexchange.groupware.infostore.facade.impl;

import static com.openexchange.java.Autoboxing.I;
import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.ReuseReadConProvider;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.index.Attachment;
import com.openexchange.groupware.attach.index.AttachmentUUID;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EffectiveInfostorePermission;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreTimedResult;
import com.openexchange.groupware.infostore.database.InfostoreFilenameReservation;
import com.openexchange.groupware.infostore.database.InfostoreFilenameReserver;
import com.openexchange.groupware.infostore.database.impl.CheckSizeSwitch;
import com.openexchange.groupware.infostore.database.impl.CreateDocumentAction;
import com.openexchange.groupware.infostore.database.impl.CreateVersionAction;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.infostore.database.impl.DeleteDocumentAction;
import com.openexchange.groupware.infostore.database.impl.DeleteVersionAction;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.database.impl.InfostoreIterator;
import com.openexchange.groupware.infostore.database.impl.InfostoreQueryCatalog;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurity;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurityImpl;
import com.openexchange.groupware.infostore.database.impl.InsertDocumentIntoDelTableAction;
import com.openexchange.groupware.infostore.database.impl.SelectForUpdateFilenameReserver;
import com.openexchange.groupware.infostore.database.impl.UpdateDocumentAction;
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
import com.openexchange.index.IndexAccess;
import com.openexchange.index.IndexConstants;
import com.openexchange.index.IndexDocument;
import com.openexchange.index.IndexExceptionCodes;
import com.openexchange.index.IndexFacadeService;
import com.openexchange.index.StandardIndexDocument;
import com.openexchange.java.Streams;
import com.openexchange.log.LogFactory;
import com.openexchange.quota.Quota;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.QuotaService;
import com.openexchange.quota.QuotaType;
import com.openexchange.quota.Resource;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.collections.Injector;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.file.SaveFileWithQuotaAction;
import com.openexchange.tools.iterator.CombinedSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
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

    static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(InfostoreFacadeImpl.class));

    public static final InfostoreQueryCatalog QUERIES = new InfostoreQueryCatalog();

    private final DatabaseImpl db = new DatabaseImpl();

    private InfostoreSecurity security = new InfostoreSecurityImpl();

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
    public boolean exists(final int id, final int version, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        try {
            return security.getInfostorePermission(id, ctx, user, userConfig).canReadObject();
        } catch (final OXException x) {
            if (InfostoreExceptionCodes.NOT_EXIST.equals(x)) {
                return false;
            }
            throw x;
        }
    }

    @Override
    public DocumentMetadata getDocumentMetadata(final int id, final int version, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(id, ctx, user, userConfig);

        if (!infoPerm.canReadObject()) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        }

        final List<Lock> locks = lockManager.findLocks(id, ctx, user, userConfig);
        final Map<Integer, List<Lock>> allLocks = new HashMap<Integer, List<Lock>>();
        allLocks.put(Integer.valueOf(id), locks);

        return addNumberOfVersions(addLocked(load(id, version, ctx), allLocks, ctx, user, userConfig), ctx);
    }

    DocumentMetadata load(final int id, final int version, final Context ctx) throws OXException {
        final InfostoreIterator iter = InfostoreIterator.loadDocumentIterator(id, version, getProvider(), ctx);
        if (!iter.hasNext()) {
            throw InfostoreExceptionCodes.DOCUMENT_NOT_EXIST.create();
        }
        DocumentMetadata dm;
        try {
            dm = iter.next();
            iter.close();
        } catch (final OXException e) {
            throw e;
        }
        return dm;
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
    public InputStream getDocument(final int id, final int version, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(id, ctx, user, userConfig);
        if (!infoPerm.canReadObject()) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        }
        final DocumentMetadata dm = load(id, version, ctx);
        final FileStorage fs = getFileStorage(ctx);
        try {
            if (dm.getFilestoreLocation() == null) {
                return Streams.newByteArrayInputStream(new byte[0]);
            }
            return fs.getFile(dm.getFilestoreLocation());
        } catch (final OXException e) {
            throw e;
        }
    }

    @Override
    public void lock(final int id, final long diff, final ServerSession session) throws OXException {
        final Context context = session.getContext();
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(
            id,
            context,
            getUser(session),
            getUserConfiguration(session));
        if (!infoPerm.canWriteObject()) {
            throw InfostoreExceptionCodes.WRITE_PERMS_FOR_LOCK_MISSING.create();
        }
        checkWriteLock(id, session);
        long timeout = 0;
        if (timeout == -1) {
            timeout = LockManager.INFINITE;
        } else {
            timeout = System.currentTimeMillis() + diff;
        }
        lockManager.lock(
            id,
            timeout,
            Scope.EXCLUSIVE,
            Type.WRITE,
            session.getUserlogin(),
            context,
            getUser(session),
            getUserConfiguration(session));
        touch(id, session);
    }

    @Override
    public void unlock(final int id, final ServerSession session) throws OXException {
        final Context context = session.getContext();
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(
            id,
            context,
            getUser(session),
            getUserConfiguration(session));
        if (!infoPerm.canWriteObject()) {
            throw InfostoreExceptionCodes.WRITE_PERMS_FOR_UNLOCK_MISSING.create();
        }
        checkMayUnlock(id, session);
        lockManager.removeAll(id, context, getUser(session), getUserConfiguration(session));
        touch(id, session);
    }

    @Override
    public void touch(final int id, final ServerSession sessionObj) throws OXException {
        try {
            final Context context = sessionObj.getContext();
            final DocumentMetadata oldDocument = load(id, CURRENT_VERSION, context);
            final DocumentMetadata document = new DocumentMetadataImpl(oldDocument);

            document.setLastModified(new Date());
            document.setModifiedBy(sessionObj.getUserId());

            final UpdateDocumentAction updateDocument = new UpdateDocumentAction();
            updateDocument.setContext(context);
            updateDocument.setDocuments(Arrays.asList(document));
            updateDocument.setModified(Metadata.LAST_MODIFIED_LITERAL, Metadata.MODIFIED_BY_LITERAL);
            updateDocument.setOldDocuments(Arrays.asList(oldDocument));
            updateDocument.setProvider(this);
            updateDocument.setQueryCatalog(QUERIES);
            updateDocument.setTimestamp(oldDocument.getSequenceNumber());

            perform(updateDocument, true);

            final UpdateVersionAction updateVersion = new UpdateVersionAction();
            updateVersion.setContext(context);
            updateVersion.setDocuments(Arrays.asList(document));
            updateVersion.setModified(Metadata.LAST_MODIFIED_LITERAL, Metadata.MODIFIED_BY_LITERAL);
            updateVersion.setOldDocuments(Arrays.asList(oldDocument));
            updateVersion.setProvider(this);
            updateVersion.setQueryCatalog(QUERIES);
            updateVersion.setTimestamp(oldDocument.getSequenceNumber());

            perform(updateVersion, true);
        } catch (final OXException x) {
            throw x;
        } catch (final Exception e) {
            // FIXME Client
            LOG.error("", e);
        }
    }

    private Delta<DocumentMetadata> addLocked(final Delta<DocumentMetadata> delta, final Map<Integer, List<Lock>> locks, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        try {
            return new LockDelta(delta, locks, ctx, user, userConfig);
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

    private TimedResult<DocumentMetadata> addLocked(final TimedResult<DocumentMetadata> tr, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        try {
            return new LockTimedResult(tr, ctx, user, userConfig);
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
            LOG.error(e.getMessage(), e);
            throw InfostoreExceptionCodes.NUMBER_OF_VERSIONS_FAILED.create(
                e,
                I(document.getId()),
                I(ctx.getContextId()),
                QUERIES.getNumberOfVersionsQueryForOneDocument());
        }
    }

    private DocumentMetadata addLocked(final DocumentMetadata document, final Map<Integer, List<Lock>> allLocks, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        List<Lock> locks = null;
        if (allLocks != null) {
            locks = allLocks.get(Integer.valueOf(document.getId()));
        } else {
            locks = lockManager.findLocks(document.getId(), ctx, user, userConfig);
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

    SearchIterator<DocumentMetadata> lockedUntilIterator(final SearchIterator<?> iter, final Map<Integer, List<Lock>> locks, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
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
            addLocked(m, locks, ctx, user, userConfig);
        }
        return new SearchIteratorAdapter<DocumentMetadata>(list.iterator());
    }

    private DocumentMetadata checkWriteLock(final int id, final ServerSession sessionObj) throws OXException {
        final DocumentMetadata document = load(id, CURRENT_VERSION, sessionObj.getContext());
        checkWriteLock(document, sessionObj);
        return document;
    }

    private void checkWriteLock(final DocumentMetadata document, final ServerSession sessionObj) throws OXException {
        if (document.getModifiedBy() == sessionObj.getUserId()) {
            return;
        }

        if (lockManager.isLocked(document.getId(), sessionObj.getContext(), getUser(sessionObj), getUserConfiguration(sessionObj))) {
            throw InfostoreExceptionCodes.ALREADY_LOCKED.create();
        }

    }

    private void checkMayUnlock(final int id, final ServerSession sessionObj) throws OXException {
        final DocumentMetadata document = load(id, CURRENT_VERSION, sessionObj.getContext());
        if (document.getCreatedBy() == sessionObj.getUserId() || document.getModifiedBy() == sessionObj.getUserId()) {
            return;
        }
        final List<Lock> locks = lockManager.findLocks(id, sessionObj.getContext(), getUser(sessionObj), getUserConfiguration(sessionObj));
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
                getUserConfiguration(session));
            if (!isperm.canCreateObjects()) {
                throw InfostoreExceptionCodes.NO_CREATE_PERMISSION.create();
            }

            // Check quota
            {
                final QuotaService quotaService = ServerServiceRegistry.getInstance().getService(QuotaService.class);
                if (null != quotaService) {
                    final Quota quota = quotaService.getQuotaFor(Resource.INFOSTORE_FILES, session);
                    final long quotaValue = quota.getQuota(QuotaType.AMOUNT);
                    if (quotaValue > 0) {
                        final long used = getUsedQuota(context);
                        if (used > 0 && used >= quotaValue) {
                            throw QuotaExceptionCodes.QUOTA_EXCEEDED.create();
                        }
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

                document.setCreationDate(new Date(System.currentTimeMillis()));
                document.setLastModified(document.getCreationDate());
                document.setCreatedBy(session.getUserId());
                document.setModifiedBy(session.getUserId());

                // db.createDocument(document, data, sessionObj.getContext(),
                // sessionObj.getUserObject(), getUserConfiguration(sessionObj));

                if (null != data) {
                    document.setVersion(1);
                } else {
                    document.setVersion(0);
                }

                final CreateDocumentAction createAction = new CreateDocumentAction();
                createAction.setContext(context);
                createAction.setDocuments(Arrays.asList(document));
                createAction.setProvider(this);
                createAction.setQueryCatalog(QUERIES);

                perform(createAction, true);

                final DocumentMetadata version0 = new DocumentMetadataImpl(document);
                version0.setFileName(null);
                version0.setFileSize(0);
                version0.setFileMD5Sum(null);
                version0.setFileMIMEType(null);
                version0.setVersion(0);
                version0.setFilestoreLocation(null);

                CreateVersionAction createVersionAction = new CreateVersionAction();
                createVersionAction.setContext(context);
                createVersionAction.setDocuments(Arrays.asList(version0));
                createVersionAction.setProvider(this);
                createVersionAction.setQueryCatalog(QUERIES);

                perform(createVersionAction, true);

                if (data != null) {
                    final SaveFileWithQuotaAction saveFile = new SaveFileWithQuotaAction();
                    final QuotaFileStorage qfs = getFileStorage(context);
                    saveFile.setStorage(qfs);
                    saveFile.setSizeHint(document.getFileSize());
                    saveFile.setIn(data);

                    perform(saveFile, false);

                    document.setVersion(1);
                    document.setFilestoreLocation(saveFile.getId());
                    if (document.getFileSize() == 0) {
                        document.setFileSize(qfs.getFileSize(saveFile.getId()));
                    }

                    createVersionAction = new CreateVersionAction();
                    createVersionAction.setContext(context);
                    createVersionAction.setDocuments(Arrays.asList(document));
                    createVersionAction.setProvider(this);
                    createVersionAction.setQueryCatalog(QUERIES);

                    perform(createVersionAction, true);

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
                InfostoreFilenameReservation r = reserve(enhance(filename, ++cnt), folderId, id, ctx, cnt);
                r.setWasAdjusted(true);
                return r;
            }
        } catch (final SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e, "");
        }
        return reservation;
    }

    private static final Pattern IS_NUMBERED_WITH_EXTENSION = Pattern.compile("\\(\\d+\\)\\.");

    private static final Pattern IS_NUMBERED = Pattern.compile("\\(\\d+\\)$");

    private String enhance(final String filename, final int c) {
        final StringBuilder stringBuilder = new StringBuilder(filename);

        Matcher matcher = IS_NUMBERED_WITH_EXTENSION.matcher(filename);
        if (matcher.find()) {
            final int start = matcher.start();
            final int end = matcher.end();
            stringBuilder.replace(start, end - 1, "(" + c + ")");
            return stringBuilder.toString();
        }

        matcher = IS_NUMBERED.matcher(filename);
        if (matcher.find()) {
            final int start = matcher.start();
            final int end = matcher.end();
            stringBuilder.replace(start, end, "(" + c + ")");
            return stringBuilder.toString();
        }

        int index = filename.lastIndexOf('.');
        if (index == -1) {
            index = filename.length();
        }

        stringBuilder.insert(index, "(" + c + ")");

        return stringBuilder.toString();
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
        if (document.getId() == NEW) {
            saveDocument(document, data, sequenceNumber, session);
            indexDocument(session.getContext(), session.getUserId(), document.getId(), -1L, true);
            return;
        }
        try {
            final Context context = session.getContext();
            // Check permission
            {
                final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(
                    document.getId(),
                    context,
                    getUser(session),
                    getUserConfiguration(session));
                if (!infoPerm.canWriteObject()) {
                    throw InfostoreExceptionCodes.NO_WRITE_PERMISSION.create();
                }
                if ((Arrays.asList(modifiedColumns).contains(Metadata.FOLDER_ID_LITERAL)) && (document.getFolderId() != -1) && infoPerm.getObject().getFolderId() != document.getFolderId()) {
                    security.checkFolderId(document.getFolderId(), context);
                    final EffectivePermission isperm = security.getFolderPermission(
                        document.getFolderId(),
                        context,
                        getUser(session),
                        getUserConfiguration(session));
                    if (!(isperm.canCreateObjects())) {
                        throw InfostoreExceptionCodes.NO_TARGET_CREATE_PERMISSION.create();
                    }
                    if (!infoPerm.canDeleteObject()) {
                        throw InfostoreExceptionCodes.NO_SOURCE_DELETE_PERMISSION.create();
                    }
                }
            }

            CheckSizeSwitch.checkSizes(document, getProvider(), context);

            final DocumentMetadata oldDocument = checkWriteLock(document.getId(), session);

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
                if (document.getFileName() != null && !document.getFileName().equals(oldFileName)) {
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

                    final SaveFileWithQuotaAction saveFile = new SaveFileWithQuotaAction();
                    final QuotaFileStorage qfs = getFileStorage(context);
                    saveFile.setStorage(qfs);
                    saveFile.setSizeHint(document.getFileSize());
                    saveFile.setIn(data);
                    perform(saveFile, false);
                    document.setFilestoreLocation(saveFile.getId());

                    if (document.getFileSize() == 0) {
                        document.setFileSize(qfs.getFileSize(saveFile.getId()));
                    }

                    final GetSwitch get = new GetSwitch(oldDocument);
                    final SetSwitch set = new SetSwitch(document);
                    final Set<Metadata> alreadySet = new HashSet<Metadata>(Arrays.asList(modifiedCols));
                    for (final Metadata m : Arrays.asList(Metadata.DESCRIPTION_LITERAL, Metadata.TITLE_LITERAL, Metadata.URL_LITERAL)) {
                        if (alreadySet.contains(m)) {
                            continue;
                        }
                        set.setValue(m.doSwitch(get));
                        m.doSwitch(set);
                    }

                    document.setCreatedBy(session.getUserId());
                    document.setCreationDate(new Date());
                    // Set version
                    final UndoableAction action;
                    if (ignoreVersion) {
                        document.setVersion(oldDocument.getVersion());
                        updatedCols.add(Metadata.VERSION_LITERAL);

                        final UpdateVersionAction updateVersionAction = new UpdateVersionAction();
                        updateVersionAction.setContext(context);
                        updateVersionAction.setDocuments(Arrays.asList(document));
                        updateVersionAction.setOldDocuments(Arrays.asList(oldDocument));
                        updateVersionAction.setProvider(this);
                        updateVersionAction.setQueryCatalog(QUERIES);
                        updateVersionAction.setModified(modifiedCols);
                        updateVersionAction.setTimestamp(sequenceNumber);

                        // Remove old file "version"
                        removeFile(context, oldDocument.getFilestoreLocation());

                        action = updateVersionAction;
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

                        final CreateVersionAction createVersionAction = new CreateVersionAction();
                        createVersionAction.setContext(context);
                        createVersionAction.setDocuments(Arrays.asList(document));
                        createVersionAction.setProvider(this);
                        createVersionAction.setQueryCatalog(QUERIES);

                        action = createVersionAction;
                    }
                    // Perform action
                    perform(action, true);
                } else if (QUERIES.updateVersion(modifiedCols)) {
                    if (!updatedCols.contains(Metadata.VERSION_LITERAL)) {
                        document.setVersion(oldDocument.getVersion());
                    }
                    final UpdateVersionAction updateVersionAction = new UpdateVersionAction();
                    updateVersionAction.setContext(context);
                    updateVersionAction.setDocuments(Arrays.asList(document));
                    updateVersionAction.setOldDocuments(Arrays.asList(oldDocument));
                    updateVersionAction.setProvider(this);
                    updateVersionAction.setQueryCatalog(QUERIES);
                    updateVersionAction.setModified(modifiedCols);
                    updateVersionAction.setTimestamp(sequenceNumber);
                    perform(updateVersionAction, true);
                }

                modifiedCols = updatedCols.toArray(new Metadata[updatedCols.size()]);
                if (QUERIES.updateDocument(modifiedCols)) {
                    final UpdateDocumentAction updateAction = new UpdateDocumentAction();
                    updateAction.setContext(context);
                    updateAction.setDocuments(Arrays.asList(document));
                    updateAction.setOldDocuments(Arrays.asList(oldDocument));
                    updateAction.setProvider(this);
                    updateAction.setQueryCatalog(QUERIES);
                    updateAction.setModified(modifiedCols);
                    updateAction.setTimestamp(Long.MAX_VALUE);
                    perform(updateAction, true);
                }

                final long indexFolderId = document.getFolderId() == oldDocument.getFolderId() ? -1L : oldDocument.getFolderId();
                indexDocument(context, session.getUserId(), oldDocument.getId(), indexFolderId, false);
            } finally {
                for (final InfostoreFilenameReservation infostoreFilenameReservation : reservations) {
                    infostoreFilenameReservation.destroySilently();
                }
            }
        } catch (final OXException x) {
            throw x;
        }
    }

    @Override
    public void removeDocument(final long folderId, final long date, final ServerSession session) throws OXException {
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
        removeDocuments(allDocuments, allVersions, date, session, null);
    }

    private void removeDocuments(final List<DocumentMetadata> allDocuments, final List<DocumentMetadata> allVersions, final long date, final ServerSession sessionObj, final List<DocumentMetadata> rejected) throws OXException {
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

        final InsertDocumentIntoDelTableAction insertIntoDel = new InsertDocumentIntoDelTableAction();
        insertIntoDel.setContext(context);
        insertIntoDel.setDocuments(delDocs);
        insertIntoDel.setProvider(this);
        insertIntoDel.setQueryCatalog(QUERIES);

        perform(insertIntoDel, true);

        for (final DocumentMetadata m : allVersions) {
            if (!rejectedIds.contains(Integer.valueOf(m.getId()))) {
                delVers.add(m);
                m.setLastModified(now);
                removeFile(context, m.getFilestoreLocation());
            }
        }

        // Set<Integer> notDeleted = db.removeDocuments(deleteMe,
        // timed.sequenceNumber(), sessionObj.getContext(),
        // sessionObj.getUserObject(), getUserConfiguration(sessionObj));

        final DeleteVersionAction deleteVersion = new DeleteVersionAction();
        deleteVersion.setContext(context);
        deleteVersion.setDocuments(delVers);
        deleteVersion.setProvider(this);
        deleteVersion.setQueryCatalog(QUERIES);

        {
            perform(deleteVersion, true);
        }

        final DeleteDocumentAction deleteDocument = new DeleteDocumentAction();
        deleteDocument.setContext(context);
        deleteDocument.setDocuments(delDocs);
        deleteDocument.setProvider(this);
        deleteDocument.setQueryCatalog(QUERIES);

        {
            perform(deleteDocument, true);
        }

        removeFromIndex(context, sessionObj.getUserId(), delDocs);
        // TODO: This triggers a full re-indexing and can be improved. We only have to re-index if the latest version is affected.
        removeFromIndex(context, sessionObj.getUserId(), delVers);
    }

    private void removeFile(final Context context, final String filestoreLocation) throws OXException {
        if (filestoreLocation == null) {
            return;
        }
        if (fileIdRemoveList.get() != null) {
            fileIdRemoveList.get().add(filestoreLocation);
            ctxHolder.set(context);
        } else {
            final QuotaFileStorage qfs = getFileStorage(context);
            qfs.deleteFile(filestoreLocation);
        }
    }

    @Override
    public int[] removeDocument(final int[] ids, final long date, final ServerSession session) throws OXException {
        final StringBuilder sIds = new StringBuilder().append('(');
        for (final int i : ids) {
            sIds.append(i).append(',');
        }
        sIds.setLength(sIds.length() - 1);
        sIds.append(')');

        List<DocumentMetadata> allVersions = null;
        List<DocumentMetadata> allDocuments = null;

        final Context context = session.getContext();

        final DBProvider reuseProvider = new ReuseReadConProvider(getProvider());
        try {
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
        } catch (final OXException x) {
            throw x;
        } catch (final Throwable t) {
            LOG.error("Unexpected Error:", t);
        }

        // Check Permissions

        final List<DocumentMetadata> rejected = new ArrayList<DocumentMetadata>();
        final Set<Integer> rejectedIds = new HashSet<Integer>();

        final Set<Integer> idSet = new HashSet<Integer>();
        for (final int i : ids) {
            idSet.add(Integer.valueOf(i));
        }

        final TLongObjectMap<EffectivePermission> perms = new TLongObjectHashMap<EffectivePermission>();

        final List<DocumentMetadata> toDeleteDocs = new ArrayList<DocumentMetadata>();
        final List<DocumentMetadata> toDeleteVersions = new ArrayList<DocumentMetadata>();

        if (allDocuments != null) {
            for (final DocumentMetadata m : allDocuments) {
                idSet.remove(Integer.valueOf(m.getId()));
                EffectivePermission p = perms.get(m.getFolderId());
                if (p == null) {
                    p = security.getFolderPermission(
                        m.getFolderId(),
                        context,
                        getUser(session),
                        getUserConfiguration(session));
                    perms.put(m.getFolderId(), p);
                }
                final EffectiveInfostorePermission infoPerm = new EffectiveInfostorePermission(p, m, getUser(session));
                if (!infoPerm.canDeleteObject()) {
                    throw InfostoreExceptionCodes.NO_DELETE_PERMISSION.create();
                }
                toDeleteDocs.add(m);
            }
        }

        if (allVersions != null) {
            for (final DocumentMetadata m : allVersions) {
                if (!rejectedIds.contains(Integer.valueOf(m.getId()))) {
                    toDeleteVersions.add(m);
                }
            }
        }

        removeDocuments(toDeleteDocs, toDeleteVersions, date, session, rejected);

        final int[] nd = new int[rejected.size() + idSet.size()];
        int i = 0;
        for (final DocumentMetadata rej : rejected) {
            nd[i++] = rej.getId();
        }
        for (final int notFound : idSet) {
            nd[i++] = notFound;
        }

        return nd;
    }

    @Override
    public int[] removeVersion(final int id, final int[] versionIds, final ServerSession session) throws OXException {
        if (versionIds.length <= 0) {
            return versionIds;
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
            getUserConfiguration(session));
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

        List<DocumentMetadata> allVersions = null;
        try {
            allVersions = InfostoreIterator.allVersionsWhere(
                "infostore_document.infostore_id = " + id + " AND infostore_document.version_number IN " + versions.toString() + " and infostore_document.version_number != 0 ",
                Metadata.VALUES_ARRAY,
                this,
                context).asList();
        } catch (final OXException x) {
            throw x;
        }

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

            final UpdateVersionAction updateVersion = new UpdateVersionAction();
            updateVersion.setContext(context);
            updateVersion.setDocuments(Arrays.asList(version0));
            updateVersion.setModified(
                Metadata.DESCRIPTION_LITERAL,
                Metadata.TITLE_LITERAL,
                Metadata.URL_LITERAL,
                Metadata.LAST_MODIFIED_LITERAL,
                Metadata.MODIFIED_BY_LITERAL,
                Metadata.FILE_MIMETYPE_LITERAL);
            updateVersion.setOldDocuments(Arrays.asList(oldVersion0));
            updateVersion.setProvider(this);
            updateVersion.setQueryCatalog(QUERIES);
            updateVersion.setTimestamp(Long.MAX_VALUE);
            try {
                perform(updateVersion, true);
            } catch (final OXException x) {
                throw x;
            }

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
        final UpdateDocumentAction updateDocument = new UpdateDocumentAction();
        updateDocument.setContext(context);
        updateDocument.setDocuments(Arrays.asList(update));
        updateDocument.setModified(updatedFields.toArray(new Metadata[updatedFields.size()]));
        updateDocument.setOldDocuments(Arrays.asList(metadata));
        updateDocument.setProvider(this);
        updateDocument.setQueryCatalog(QUERIES);
        updateDocument.setTimestamp(Long.MAX_VALUE);

        try {
            perform(updateDocument, true);
        } catch (final OXException x) {
            throw x;
        }

        // Remove Versions

        final DeleteVersionAction deleteVersion = new DeleteVersionAction();
        deleteVersion.setContext(context);
        deleteVersion.setDocuments(allVersions);
        deleteVersion.setProvider(this);
        deleteVersion.setQueryCatalog(QUERIES);

        try {
            perform(deleteVersion, true);
        } catch (final OXException x) {
            throw x;
        }

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
    public TimedResult<DocumentMetadata> getDocuments(final long folderId, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        return getDocuments(folderId, Metadata.HTTPAPI_VALUES_ARRAY, null, 0, ctx, user, userConfig);
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(final long folderId, final Metadata[] columns, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        return getDocuments(folderId, columns, null, 0, ctx, user, userConfig);
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(final long folderId, Metadata[] columns, final Metadata sort, final int order, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        Metadata[] cols = addLastModifiedIfNeeded(columns);
        boolean onlyOwn = false;
        final EffectivePermission isperm = security.getFolderPermission(folderId, ctx, user, userConfig);
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
            iter = InfostoreIterator.documentsByCreator(folderId, user.getId(), cols, sort, order, getProvider(), ctx);
        } else {
            iter = InfostoreIterator.documents(folderId, cols, sort, order, getProvider(), ctx);
        }
        TimedResult<DocumentMetadata> tr = new InfostoreTimedResult(iter);
        if (addLocked) {
            tr = addLocked(tr, ctx, user, userConfig);
        }
        if (addNumberOfVersions) {
            tr = addNumberOfVersions(tr, ctx);
        }
        return tr;
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(final int id, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        return getVersions(id, Metadata.HTTPAPI_VALUES_ARRAY, null, 0, ctx, user, userConfig);
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(final int id, final Metadata[] columns, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        return getVersions(id, columns, null, 0, ctx, user, userConfig);
    }

    @Override
    public TimedResult<DocumentMetadata> getVersions(final int id, Metadata[] columns, final Metadata sort, final int order, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(id, ctx, user, userConfig);
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
        final InfostoreIterator iter = InfostoreIterator.versions(id, cols, sort, order, getProvider(), ctx);
        final TimedResult<DocumentMetadata> tr = new InfostoreTimedResult(iter);

        if (addLocked) {
            return addLocked(tr, ctx, user, userConfig);
        }
        return tr;

    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(final int[] ids, Metadata[] columns, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {

        try {
            security.injectInfostorePermissions(ids, ctx, user, userConfig, null, new Injector<Object, EffectiveInfostorePermission>() {

                @Override
                public Object inject(final Object list, final EffectiveInfostorePermission element) {
                    if (!element.canReadObject()) {
                        throw new NotAllowed(element.getObjectID());
                    }
                    return list;
                }

            });
        } catch (final NotAllowed na) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        }
        Metadata[] cols = addLastModifiedIfNeeded(columns);
        final InfostoreIterator iter = InfostoreIterator.list(ids, cols, getProvider(), ctx);
        TimedResult<DocumentMetadata> tr = new InfostoreTimedResult(iter);

        for (final Metadata m : cols) {
            if (m == Metadata.LOCKED_UNTIL_LITERAL) {
                tr = addLocked(tr, ctx, user, userConfig);
            }
            if (m == Metadata.NUMBER_OF_VERSIONS_LITERAL) {
                tr = addNumberOfVersions(tr, ctx);
            }
        }
        return tr;

    }

    @Override
    public Delta<DocumentMetadata> getDelta(final long folderId, final long updateSince, final Metadata[] columns, final boolean ignoreDeleted, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        return getDelta(folderId, updateSince, columns, null, 0, ignoreDeleted, ctx, user, userConfig);
    }

    @Override
    public Delta<DocumentMetadata> getDelta(final long folderId, final long updateSince, Metadata[] columns, final Metadata sort, final int order, final boolean ignoreDeleted, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        boolean onlyOwn = false;
        final EffectivePermission isperm = security.getFolderPermission(folderId, ctx, user, userConfig);
        if (isperm.getReadPermission() == OCLPermission.NO_PERMISSIONS) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        } else if (isperm.getReadPermission() == OCLPermission.READ_OWN_OBJECTS) {
            onlyOwn = true;
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

        final Map<Integer, List<Lock>> locks = loadLocksInFolderAndExpireOldLocks(folderId, ctx, user, userConfig);

        final DBProvider reuse = new ReuseReadConProvider(getProvider());

        InfostoreIterator newIter = null;
        InfostoreIterator modIter = null;
        InfostoreIterator delIter = null;

        Metadata[] cols = addLastModifiedIfNeeded(columns);

        if (onlyOwn) {
            newIter = InfostoreIterator.newDocumentsByCreator(folderId, user.getId(), cols, sort, order, updateSince, reuse, ctx);
            modIter = InfostoreIterator.modifiedDocumentsByCreator(folderId, user.getId(), cols, sort, order, updateSince, reuse, ctx);
            if (!ignoreDeleted) {
                delIter = InfostoreIterator.deletedDocumentsByCreator(folderId, user.getId(), sort, order, updateSince, reuse, ctx);
            }
        } else {
            newIter = InfostoreIterator.newDocuments(folderId, cols, sort, order, updateSince, reuse, ctx);
            modIter = InfostoreIterator.modifiedDocuments(folderId, cols, sort, order, updateSince, reuse, ctx);
            if (!ignoreDeleted) {
                delIter = InfostoreIterator.deletedDocuments(folderId, sort, order, updateSince, reuse, ctx);
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
            delta = addLocked(delta, locks, ctx, user, userConfig);
        }
        if (addNumberOfVersions) {
            delta = addNumberOfVersions(delta, ctx);
        }
        return delta;
    }

    private Map<Integer, List<Lock>> loadLocksInFolderAndExpireOldLocks(final long folderId, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        final Map<Integer, List<Lock>> locks = new HashMap<Integer, List<Lock>>();
        final InfostoreIterator documents = InfostoreIterator.documents(
            folderId,
            new Metadata[] { Metadata.ID_LITERAL },
            null,
            -1,
            getProvider(),
            ctx);
        try {
            while (documents.hasNext()) {
                final DocumentMetadata document = documents.next();
                lockManager.findLocks(document.getId(), ctx, user, userConfig);
            }
        } finally {
            documents.close();
        }
        return locks;
    }

    @Override
    public int countDocuments(final long folderId, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        boolean onlyOwn = false;
        final EffectivePermission isperm = security.getFolderPermission(folderId, ctx, user, userConfig);
        if (!(isperm.canReadAllObjects()) && !(isperm.canReadOwnObjects())) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        } else if (isperm.canReadOwnObjects()) {
            onlyOwn = true;
        }
        return db.countDocuments(folderId, onlyOwn, ctx, user);
    }

    @Override
    public boolean hasFolderForeignObjects(final long folderId, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
        return db.hasFolderForeignObjects(folderId, ctx, user);
    }

    @Override
    public boolean isFolderEmpty(final long folderId, final Context ctx) throws OXException {
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
        if (null != fileIdRemoveList.get() && fileIdRemoveList.get().size() > 0) {
            final QuotaFileStorage qfs = getFileStorage(ctxHolder.get());
            for (final String id : fileIdRemoveList.get()) {
                try {
                    qfs.deleteFile(id);
                } catch (final OXException x) {
                    throw x;
                }
            }
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

    private final class LockTimedResult implements TimedResult<DocumentMetadata> {

        private final long sequenceNumber;

        private final SearchIterator<DocumentMetadata> results;

        public LockTimedResult(final TimedResult<DocumentMetadata> delegate, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
            sequenceNumber = delegate.sequenceNumber();

            this.results = lockedUntilIterator(delegate.results(), null, ctx, user, userConfig);
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

        public LockDelta(final Delta<DocumentMetadata> delegate, final Map<Integer, List<Lock>> locks, final Context ctx, final User user, final UserConfiguration userConfig) throws OXException {
            final SearchIterator<DocumentMetadata> deleted = delegate.getDeleted();
            if (null != deleted) {
                this.deleted = lockedUntilIterator(deleted, locks, ctx, user, userConfig);
            }
            this.modified = lockedUntilIterator(delegate.getModified(), locks, ctx, user, userConfig);
            this.newIter = lockedUntilIterator(delegate.getNew(), locks, ctx, user, userConfig);
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
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Deleting infostore document");
                        }

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
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Could not remove document from infostore index.");
                            }
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
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Indexing infostore document");
                        }

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
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Could index document to infostore index.");
                            }
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
