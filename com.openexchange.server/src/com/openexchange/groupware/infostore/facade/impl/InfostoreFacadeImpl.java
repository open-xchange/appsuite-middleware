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

package com.openexchange.groupware.infostore.facade.impl;

import static com.openexchange.java.Autoboxing.*;
import static com.openexchange.tools.arrays.Arrays.contains;
import java.io.IOException;
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
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.ajax.fileholder.IFileHolder.InputStreamClosure;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.filestore.QuotaFileStorageService;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.EffectiveObjectPermission;
import com.openexchange.groupware.container.EffectiveObjectPermissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.infostore.DocumentAndMetadata;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EffectiveInfostoreFolderPermission;
import com.openexchange.groupware.infostore.EffectiveInfostorePermission;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.groupware.infostore.InfostoreTimedResult;
import com.openexchange.groupware.infostore.database.FilenameReservation;
import com.openexchange.groupware.infostore.database.FilenameReserver;
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
import com.openexchange.groupware.infostore.database.impl.FilenameReserverImpl;
import com.openexchange.groupware.infostore.database.impl.InfostoreIterator;
import com.openexchange.groupware.infostore.database.impl.InfostoreQueryCatalog;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurity;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurityImpl;
import com.openexchange.groupware.infostore.database.impl.ReplaceDocumentIntoDelTableAction;
import com.openexchange.groupware.infostore.database.impl.Tools;
import com.openexchange.groupware.infostore.database.impl.UpdateDocumentAction;
import com.openexchange.groupware.infostore.database.impl.UpdateObjectPermissionAction;
import com.openexchange.groupware.infostore.database.impl.UpdateVersionAction;
import com.openexchange.groupware.infostore.database.impl.versioncontrol.VersionControlUtil;
import com.openexchange.groupware.infostore.search.SearchTerm;
import com.openexchange.groupware.infostore.search.impl.SearchEngineImpl;
import com.openexchange.groupware.infostore.utils.FileDelta;
import com.openexchange.groupware.infostore.utils.GetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.SetSwitch;
import com.openexchange.groupware.infostore.validation.FilenamesMayNotContainSlashesValidator;
import com.openexchange.groupware.infostore.validation.InvalidCharactersValidator;
import com.openexchange.groupware.infostore.validation.ObjectPermissionValidator;
import com.openexchange.groupware.infostore.validation.ValidationChain;
import com.openexchange.groupware.infostore.webdav.EntityLockManager;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.Lock;
import com.openexchange.groupware.infostore.webdav.LockManager.Scope;
import com.openexchange.groupware.infostore.webdav.LockManager.Type;
import com.openexchange.groupware.infostore.webdav.TouchInfoitemsWithExpiredLocksListener;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.results.CustomizableTimedResult;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.SizeKnowingInputStream;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.quota.QuotaExceptionCodes;
import com.openexchange.quota.groupware.AmountQuotas;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.share.ShareService;
import com.openexchange.tools.file.AppendFileAction;
import com.openexchange.tools.file.SaveFileAction;
import com.openexchange.tools.iterator.Customizer;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorDelegator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.tx.UndoableAction;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserPermissionService;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;

/**
 * {@link InfostoreFacadeImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreFacadeImpl extends DBService implements InfostoreFacade, InfostoreSearchEngine {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreFacadeImpl.class);
    private static final InfostoreQueryCatalog QUERIES = InfostoreQueryCatalog.getInstance();
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
    private final ThreadLocal<Map<Integer, Set<Integer>>> guestCleanupList = new ThreadLocal<Map<Integer, Set<Integer>>>();

    private final TouchInfoitemsWithExpiredLocksListener expiredLocksListener;

    private final ObjectPermissionLoader objectPermissionLoader;
    private final NumberOfVersionsLoader numberOfVersionsLoader;
    private final LockedUntilLoader lockedUntilLoader;
    private final SearchEngineImpl searchEngine;

    /**
     * Initializes a new {@link InfostoreFacadeImpl}.
     */
    public InfostoreFacadeImpl() {
        super();
        expiredLocksListener = new TouchInfoitemsWithExpiredLocksListener(null, this);
        lockManager.addExpiryListener(expiredLocksListener);
        this.searchEngine = new SearchEngineImpl(this);
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
    public boolean exists(int id, int version, ServerSession session) throws OXException {
        try {
            return security.getInfostorePermission(session, id).canReadObject();
        } catch (OXException e) {
            if (InfostoreExceptionCodes.NOT_EXIST.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public boolean exists(int id, int version, Context context) throws OXException {
        Metadata[] metadata = new Metadata[] { Metadata.FOLDER_ID_LITERAL, Metadata.ID_LITERAL, Metadata.VERSION_LITERAL };
        InfostoreIterator searchIterator = null;
        try {
            searchIterator = InfostoreIterator.versions(id, metadata, Metadata.VERSION_LITERAL, InfostoreFacade.ASC, this, context);
            if (version == InfostoreFacade.CURRENT_VERSION) {
                return searchIterator.hasNext();
            }
            boolean found = false;
            while (searchIterator.hasNext()) {
                DocumentMetadata document = searchIterator.next();
                if (version == document.getVersion()) {
                    found = true;
                    break;
                }
            }
            return found;
        } finally {
            SearchIterators.close(searchIterator);
        }
    }

    @Override
    public boolean hasDocumentAccess(int id, AccessPermission permission, User user, Context context) throws OXException {
        UserPermissionService userPermissionService = ServerServiceRegistry.getServize(UserPermissionService.class, true);
        UserPermissionBits permissionBits = userPermissionService.getUserPermissionBits(user.getId(), context);
        EffectiveInfostorePermission effectivePermission = security.getInfostorePermission(context, user, permissionBits, id);
        return permission.appliesTo(effectivePermission);
    }

    @Override
    public DocumentMetadata getDocumentMetadata(int id, int version, ServerSession session) throws OXException {
        return getDocumentMetadata(-1, id, version, session);
    }

    @Override
    public DocumentMetadata getDocumentMetadata(long folderId, int id, int version, ServerSession session) throws OXException {
        Context context = session.getContext();
        /*
         * load document metadata (including object permissions)
         */
        DocumentMetadata document = objectPermissionLoader.add(load(id, version, context), context, null);
        /*
         * check permissions
         */
        EffectiveInfostorePermission permission = security.getInfostorePermission(session, document);
        if (false == permission.canReadObject()) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        }
        /*
         * adjust parent folder if required
         */
        if (getSharedFilesFolderID(session) == folderId || false == permission.canReadObjectInFolder()) {
            document.setOriginalFolderId(document.getFolderId());
            document.setFolderId(getSharedFilesFolderID(session));
            /*
             * Re-sharing of files is not allowed.
             */
            document.setShareable(false);
        } else {
            document.setShareable(permission.canShareObject());
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
        /*
         * get needed metadata & check read permissions
         */
        DocumentMetadata metadata = load(id, version, session.getContext());
        EffectiveInfostorePermission permission = security.getInfostorePermission(session, metadata);
        if (false == permission.canReadObject()) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        }
        /*
         * get & return file from storage
         */
        if (null == metadata.getFilestoreLocation()) {
            return Streams.EMPTY_INPUT_STREAM;
        }
        FileStorage fileStorage = getFileStorage(permission.getFolderOwner(), session.getContextId());
        if (0 == offset && -1 == length) {
            return new SizeKnowingInputStream(fileStorage.getFile(metadata.getFilestoreLocation()), metadata.getFileSize());
        }
        return new SizeKnowingInputStream(fileStorage.getFile(metadata.getFilestoreLocation(), offset, length), length);
    }

    /**
     * Generates an E-Tag based on the supplied document metadata.
     *
     * @param metadata The metadata
     * @return The E-Tag
     */
    private static String getETag(DocumentMetadata metadata) {
        FileID fileID = new FileID(String.valueOf(metadata.getId()));
        fileID.setFolderId(String.valueOf(metadata.getFolderId()));
        return FileStorageUtility.getETagFor(fileID.toUniqueID(), String.valueOf(metadata.getVersion()), metadata.getLastModified());
    }

    @Override
    public DocumentAndMetadata getDocumentAndMetadata(int id, int version, String clientETag, ServerSession session) throws OXException {
        return getDocumentAndMetadata(-1, id, version, clientETag, session);
    }

    @Override
    public DocumentAndMetadata getDocumentAndMetadata(long folderId, int id, int version, String clientETag, ServerSession session) throws OXException {
        Context context = session.getContext();
        /*
         * get needed metadata (including object permissions) & check read permissions
         */
        DocumentMetadata metadata = objectPermissionLoader.add(load(id, version, context), context, null);
        EffectiveInfostorePermission permission = security.getInfostorePermission(session, metadata);
        if (false == permission.canReadObject()) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        }
        /*
         * adjust parent folder if required, add further metadata
         */
        if (getSharedFilesFolderID(session) == folderId || false == permission.canReadObjectInFolder()) {
            metadata.setOriginalFolderId(metadata.getFolderId());
            metadata.setFolderId(getSharedFilesFolderID(session));
            /*
             * Re-sharing of files is not allowed.
             */
            metadata.setShareable(false);
        } else {
            metadata.setShareable(permission.canShareObject());
        }
        metadata = numberOfVersionsLoader.add(lockedUntilLoader.add(metadata, context, null), context, null);
        /*
         * check client E-Tag if supplied
         */
        String eTag = getETag(metadata);
        if (false == Strings.isEmpty(clientETag) && clientETag.equals(eTag)) {
            return new DocumentAndMetadataImpl(metadata, null, eTag);
        }
        /*
         * add file to result, otherwise
         */
        final FileStorage fileStorage = getFileStorage(permission.getFolderOwner(), session.getContextId());
        final String filestoreLocation = metadata.getFilestoreLocation();
        InputStreamClosure isClosure = new InputStreamClosure() {

            @Override
            public InputStream newStream() throws OXException, IOException {
                return null == filestoreLocation ? Streams.EMPTY_INPUT_STREAM : fileStorage.getFile(filestoreLocation);
            }
        };
        return new DocumentAndMetadataImpl(metadata, isClosure, eTag);
    }

    @Override
    public void lock(final int id, final long diff, final ServerSession session) throws OXException {
        Context context = session.getContext();
        User user = session.getUser();
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(session, id);
        if (!infoPerm.canWriteObject()) {
            throw InfostoreExceptionCodes.WRITE_PERMS_FOR_LOCK_MISSING.create();
        }
        final DocumentMetadata document = checkWriteLock(id, session);
        if (lockManager.isLocked(document.getId(), session.getContext(), user)) {
            // Already locked by this user
            return;
        }

        long timeout = diff;
        lockManager.lock(id, timeout, Scope.EXCLUSIVE, Type.WRITE, session.getUserlogin(), context, user);
        touch(id, session);
    }

    @Override
    public void unlock(int id, ServerSession session) throws OXException {
        EffectiveInfostorePermission infoPerm = security.getInfostorePermission(session, id);
        if (false == infoPerm.canWriteObject()) {
            throw InfostoreExceptionCodes.WRITE_PERMS_FOR_UNLOCK_MISSING.create();
        }
        checkMayUnlock(id, session);
        lockManager.removeAll(id, session);
        touch(id, session);
    }

    @Override
    public void touch(final int id, final ServerSession session) throws OXException {
        final Context context = session.getContext();
        final DocumentMetadata oldDocument = load(id, CURRENT_VERSION, context);
        final DocumentMetadata document = new DocumentMetadataImpl(oldDocument);
        Metadata[] modifiedColums = new Metadata[] { Metadata.LAST_MODIFIED_LITERAL, Metadata.MODIFIED_BY_LITERAL };
        long sequenceNumber = oldDocument.getSequenceNumber();

        document.setLastModified(new Date());
        document.setModifiedBy(session.getUserId());
        perform(new UpdateDocumentAction(this, QUERIES, context, document, oldDocument, modifiedColums, sequenceNumber, session), true);
        perform(new UpdateVersionAction(this, QUERIES, context, document, oldDocument, modifiedColums, sequenceNumber, session), true);
    }

    @Override
    public void touch(int id, Context context) throws OXException {
        DocumentMetadata oldDocument = load(id, CURRENT_VERSION, context);
        DocumentMetadata document = new DocumentMetadataImpl(oldDocument);
        document.setLastModified(new Date());
        document.setModifiedBy(context.getMailadmin());
        HashSet<Metadata> modifiedColums = new HashSet<Metadata>(Arrays.asList(Metadata.LAST_MODIFIED_LITERAL, Metadata.MODIFIED_BY_LITERAL));
        long sequenceNumber = oldDocument.getSequenceNumber();
        int folderOwner = security.getFolderOwner(oldDocument, context);
        saveModifiedDocument(new SaveParameters(context, null, document, oldDocument, sequenceNumber, modifiedColums, folderOwner));
    }

    @Override
    public com.openexchange.file.storage.Quota getFileQuota(ServerSession session) throws OXException {
        long limit = com.openexchange.file.storage.Quota.UNLIMITED;
        long usage = com.openexchange.file.storage.Quota.UNLIMITED;
        limit = AmountQuotas.getLimit(session, "infostore", ServerServiceRegistry.getServize(ConfigViewFactory.class, true), ServerServiceRegistry.getServize(DatabaseService.class, true));
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
            QuotaFileStorage fileStorage = getFileStorage(session.getUserId(), session.getContextId());
            limit = fileStorage.getQuota();
            if (com.openexchange.file.storage.Quota.UNLIMITED != limit) {
                usage = fileStorage.getUsage();
            }
        } catch (OXException e) {
            LOG.warn("Error getting file storage quota for user {} in context {}", session.getUserId(), session.getContextId(), e);
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
            iterator = InfostoreIterator.loadDocumentIterator(id, version, this, ctx);
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
            throw InfostoreExceptionCodes.CURRENTLY_LOCKED.create();
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
    public IDTuple saveDocumentTryAddVersion(DocumentMetadata document, InputStream data, long sequenceNumber, Metadata[] modifiedColumns, ServerSession session) throws OXException {
        try {
            return saveDocument(document, data, sequenceNumber, true, session);
        } catch (OXException e) {
            if (e.getCode() == InfostoreExceptionCodes.FILENAME_NOT_UNIQUE.getNumber() && e.getPrefix().equals(EnumComponent.INFOSTORE.getAbbreviation())) {
                long folderId = (long) e.getDisplayArgs()[1];
                int id = (int) e.getDisplayArgs()[2];
                if (id == 0) {
                    throw InfostoreExceptionCodes.MODIFIED_CONCURRENTLY.create();
                }
                try {
                    DocumentMetadata existing = load(id, CURRENT_VERSION, session.getContext());
                    DocumentMetadata update = new DocumentMetadataImpl(document);
                    update.setFolderId(folderId);
                    update.setId(id);
                    update.setLastModified(new Date());
                    Metadata[] columns = null == modifiedColumns ? new Metadata[] { Metadata.LAST_MODIFIED_LITERAL } : addLastModifiedIfNeeded(modifiedColumns);
                    return saveDocument(update, data, existing.getSequenceNumber(), columns, session);
                } catch (OXException x) {
                    if (x.getCode() == InfostoreExceptionCodes.DOCUMENT_NOT_EXIST.getNumber() && x.getPrefix().equals(EnumComponent.INFOSTORE.getAbbreviation())) {
                        return saveDocument(document, data, sequenceNumber, false, session);
                    }
                    throw x;
                }
            }
            throw e;
        }
    }

    @Override
    public IDTuple saveDocument(final DocumentMetadata document, final InputStream data, final long sequenceNumber, final ServerSession session) throws OXException {
        return saveDocument(document, data, sequenceNumber, false, session);
    }

    private IDTuple saveDocument(final DocumentMetadata document, final InputStream data, final long sequenceNumber, final boolean tryAddVersion, final ServerSession session) throws OXException {
        if (document.getId() != InfostoreFacade.NEW) {
            return saveDocument(document, data, sequenceNumber, nonNull(document), session);
        }

        // Insert NEW document
        final Context context = session.getContext();
        EffectiveInfostoreFolderPermission targetFolderPermission = security.getFolderPermission(session, document.getFolderId());
        if (FolderObject.INFOSTORE != targetFolderPermission.getPermission().getFolderModule()) {
            throw InfostoreExceptionCodes.NOT_INFOSTORE_FOLDER.create(L(document.getFolderId()));
        }
        if (false == targetFolderPermission.canCreateObjects()) {
            throw InfostoreExceptionCodes.NO_CREATE_PERMISSION.create();
        }
        if (null != document.getObjectPermissions() && false == targetFolderPermission.canShareOwnObjects()) {
            throw InfostoreExceptionCodes.NO_WRITE_PERMISSION.create();
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
        getValidationChain().validate(session, document, null, null);
        CheckSizeSwitch.checkSizes(document, this, context);

        FilenameReserver filenameReserver = null;
        try {
            filenameReserver = new FilenameReserverImpl(context, this);
            FilenameReservation reservation = filenameReserver.reserve(document, !tryAddVersion);
            if (reservation.wasAdjusted()) {
                document.setFileName(reservation.getFilename());
                if (reservation.wasSameTitle()) {
                    document.setTitle(reservation.getFilename());
                }
            }
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

            if (null != data) {
                document.setVersion(1);
            } else {
                document.setVersion(0);
            }

            perform(new CreateDocumentAction(this, QUERIES, context, Collections.singletonList(document), session), true);
            perform(new CreateObjectPermissionAction(this, context, document), true);

            final DocumentMetadata version0 = new DocumentMetadataImpl(document);
            version0.setFileName(null);
            version0.setFileSize(0);
            version0.setFileMD5Sum(null);
            version0.setFileMIMEType(null);
            version0.setVersion(0);
            version0.setFilestoreLocation(null);

            perform(new CreateVersionAction(this, QUERIES, context, Collections.singletonList(version0), session), true);

            if (data != null) {
                SaveFileAction saveFile = new SaveFileAction(getFileStorage(targetFolderPermission.getFolderOwner(), session.getContextId()), data, document.getFileSize());
                perform(saveFile, false);
                document.setVersion(1);
                document.setFilestoreLocation(saveFile.getFileStorageID());
                document.setFileMD5Sum(saveFile.getChecksum());
                document.setFileSize(saveFile.getByteCount());

                perform(new CreateVersionAction(this, QUERIES, context, Collections.singletonList(document), session), true);
            }

            return new IDTuple(String.valueOf(document.getFolderId()), String.valueOf(document.getId()));
        } finally {
            if (null != filenameReserver) {
                filenameReserver.cleanUp();
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

    protected QuotaFileStorage getFileStorage(int folderOwner, int contextId) throws OXException {
        QuotaFileStorageService storageService = QFS_REF.get();
        if (null == storageService) {
            throw ServiceExceptionCode.absentService(QuotaFileStorageService.class);
        }

        return storageService.getQuotaFileStorage(folderOwner, contextId);
    }

    private Metadata[] nonNull(final DocumentMetadata document) {
        final List<Metadata> nonNull = new ArrayList<>();
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
            return saveDocument(document, data, sequenceNumber, session);
        }

        // Check permissions
        Context context = session.getContext();
        int sharedFilesFolderID = getSharedFilesFolderID(session);
        EffectiveInfostorePermission infoPerm = security.getInfostorePermission(session, document.getId());
        if (false == infoPerm.canWriteObject() || contains(modifiedColumns, Metadata.OBJECT_PERMISSIONS_LITERAL) && (document.getFolderId() == sharedFilesFolderID || false == infoPerm.canShareObject())) {
            throw InfostoreExceptionCodes.NO_WRITE_PERMISSION.create();
        }

        // Check and adjust folder id
        List<Metadata> sanitizedColumns = new ArrayList<Metadata>(modifiedColumns.length);
        Collections.addAll(sanitizedColumns, modifiedColumns);
        if (sanitizedColumns.contains(Metadata.FOLDER_ID_LITERAL)) {
            long folderId = document.getFolderId();
            if (folderId == sharedFilesFolderID) {
                document.setFolderId(infoPerm.getObject().getFolderId());
                sanitizedColumns.remove(Metadata.FOLDER_ID_LITERAL);
                modifiedColumns = sanitizedColumns.toArray(new Metadata[sanitizedColumns.size()]);
            } else if (document.getFolderId() != -1 && infoPerm.getObject().getFolderId() != document.getFolderId()) {
                EffectiveInfostoreFolderPermission targetFolderPermission = security.getFolderPermission(session, document.getFolderId());
                if (FolderObject.INFOSTORE != targetFolderPermission.getPermission().getFolderModule()) {
                    throw InfostoreExceptionCodes.NOT_INFOSTORE_FOLDER.create(L(folderId));
                }
                if (false == targetFolderPermission.canCreateObjects()) {
                    throw InfostoreExceptionCodes.NO_CREATE_PERMISSION.create();
                }
                if (false == infoPerm.canDeleteObject()) {
                    throw InfostoreExceptionCodes.NO_SOURCE_DELETE_PERMISSION.create();
                }
            }
        }

        // Set modified information
        Set<Metadata> updatedCols = new HashSet<Metadata>(Arrays.asList(modifiedColumns));
        updatedCols.removeAll(Arrays.asList(Metadata.CREATED_BY_LITERAL, Metadata.CREATION_DATE_LITERAL, Metadata.ID_LITERAL));
        if (!updatedCols.contains(Metadata.LAST_MODIFIED_LITERAL) || null == document.getLastModified()) {
            document.setLastModified(new Date());
        }
        document.setModifiedBy(session.getUserId());
        updatedCols.add(Metadata.LAST_MODIFIED_LITERAL);
        updatedCols.add(Metadata.MODIFIED_BY_LITERAL);

        CheckSizeSwitch.checkSizes(document, this, context);

        DocumentMetadata oldDocument = objectPermissionLoader.add(checkWriteLock(document.getId(), session), session.getContext(), null);
        getValidationChain().validate(session, document, oldDocument, updatedCols);

        SaveParameters saveParameters = new SaveParameters(context, session, document, oldDocument, sequenceNumber, updatedCols, infoPerm.getFolderOwner());
        saveParameters.setData(data, offset, session.getUserId(), ignoreVersion);
        saveModifiedDocument(saveParameters);

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

        CheckSizeSwitch.checkSizes(document, this, context);

        DocumentMetadata oldDocument = objectPermissionLoader.add(load(document.getId(), context), context, null);
        return saveModifiedDocument(new SaveParameters(context, null, document, oldDocument, sequenceNumber, updatedCols, security.getFolderOwner(folderId, context)));
    }

    private IDTuple saveModifiedDocument(SaveParameters parameters) throws OXException {
        FilenameReserver filenameReserver = null;
        try {
            Set<Metadata> updatedCols = parameters.getUpdatedCols();
            DocumentMetadata document = parameters.getDocument();
            DocumentMetadata oldDocument = parameters.getOldDocument();
            Context context = parameters.getContext();
            ServerSession session = parameters.getSession();
            int checkedVersion = -1;

            if (updatedCols.contains(Metadata.VERSION_LITERAL)) {
                String fname = load(document.getId(), document.getVersion(), context).getFileName();
                checkedVersion = document.getVersion();
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
                DocumentMetadata placeHolder = new DocumentMetadataImpl(oldDocument.getId());
                placeHolder.setFolderId(document.getFolderId());
                placeHolder.setFileName(newFileName);
                if (null == filenameReserver) {
                    filenameReserver = new FilenameReserverImpl(context, db);
                }
                FilenameReservation reservation = filenameReserver.reserve(placeHolder, true);
                document.setFileName(reservation.getFilename());
                updatedCols.add(Metadata.FILENAME_LITERAL);

                // insert tombstone row to del_infostore table in case of move operations to aid folder based synchronizations
                DocumentMetadataImpl tombstoneDocument = new DocumentMetadataImpl(oldDocument);
                tombstoneDocument.setLastModified(document.getLastModified());
                tombstoneDocument.setModifiedBy(document.getModifiedBy());
                perform(new ReplaceDocumentIntoDelTableAction(this, QUERIES, context, tombstoneDocument, session), true);

                // remove any object permissions upon move
                document.setObjectPermissions(null);
                updatedCols.add(Metadata.OBJECT_PERMISSIONS_LITERAL);
            } else if (isRename) {
                // this is a rename - reserve in current folder
                DocumentMetadata placeHolder = new DocumentMetadataImpl(oldDocument.getId());
                placeHolder.setFolderId(oldDocument.getFolderId());
                placeHolder.setFileName(document.getFileName());
                if (null == filenameReserver) {
                    filenameReserver = new FilenameReserverImpl(context, db);
                }
                FilenameReservation reservation = filenameReserver.reserve(placeHolder, true);
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

            if (isMove) {
                VersionControlUtil.doVersionControl(this, Collections.singletonList(document), Collections.singletonList(oldDocument), document.getFolderId(), context);
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

                    // Ensure version existence
                    if (checkedVersion > 0 && checkedVersion != document.getVersion()) {
                        load(document.getId(), document.getVersion(), context);
                    }

                    // Perform the version-related updates
                    perform(new UpdateVersionAction(this, QUERIES, context, document, oldDocument, modifiedCols, parameters.getSequenceNumber(), session), true);
                }
            }

            if (QUERIES.updateDocument(modifiedCols)) {
                perform(new UpdateDocumentAction(this, QUERIES, context, document, oldDocument, modifiedCols, Long.MAX_VALUE, session), true);
            }

            // Update object permissions as needed
            if (updatedCols.contains(Metadata.OBJECT_PERMISSIONS_LITERAL)) {
                rememberForGuestCleanup(context.getContextId(), Collections.singletonList(oldDocument));
                perform(new UpdateObjectPermissionAction(this, context, document, oldDocument), true);
            }
            return new IDTuple(String.valueOf(document.getFolderId()), String.valueOf(document.getId()));
        } finally {
            if (null != filenameReserver) {
                filenameReserver.cleanUp();
            }
        }
    }

    private void storeNewData(SaveParameters parameters) throws OXException {
        QuotaFileStorage qfs = getFileStorage(parameters.getOptFolderAdmin(), parameters.getContext().getContextId());
        if (0 < parameters.getOffset()) {
            AppendFileAction appendFile = new AppendFileAction(qfs, parameters.getData(), parameters.getOldDocument().getFilestoreLocation(), parameters.getDocument().getFileSize(), parameters.getOffset());
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
            parameters.getUpdatedCols().addAll(Arrays.asList(Metadata.FILE_MD5SUM_LITERAL, Metadata.FILE_SIZE_LITERAL, Metadata.FILESTORE_LOCATION_LITERAL));
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
        Session session = parameters.getSession();
        final UndoableAction action;
        if (parameters.isIgnoreVersion()) {
            parameters.getDocument().setVersion(parameters.getOldDocument().getVersion());
            parameters.getUpdatedCols().add(Metadata.VERSION_LITERAL);
            parameters.getUpdatedCols().add(Metadata.FILESTORE_LOCATION_LITERAL);
            action = new UpdateVersionAction(this, QUERIES, parameters.getContext(), parameters.getDocument(), parameters.getOldDocument(), parameters.getUpdatedCols().toArray(new Metadata[parameters.getUpdatedCols().size()]), parameters.getSequenceNumber(), session);

            // Remove old file "version" if not appended
            if (0 >= parameters.getOffset()) {
                removeFile(parameters.getContext(), parameters.getOldDocument().getFilestoreLocation(), security.getFolderOwner(parameters.getOldDocument(), parameters.getContext()));
            }
        } else {
            Connection con = null;
            try {
                con = getReadConnection(parameters.getContext());
                parameters.getDocument().setVersion(getNextVersionNumberForInfostoreObject(parameters.getContext().getContextId(), parameters.getDocument().getId(), con));
                parameters.getUpdatedCols().add(Metadata.VERSION_LITERAL);
            } catch (final SQLException e) {
                LOG.error("SQLException: ", e);
            } finally {
                releaseReadConnection(parameters.getContext(), con);
            }

            action = new CreateVersionAction(this, QUERIES, parameters.getContext(), Collections.singletonList(parameters.getDocument()), session);
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
        String whereClause = "infostore.folder_id = " + folderId;
        List<DocumentMetadata> allDocuments = InfostoreIterator.allDocumentsWhere(whereClause, Metadata.VALUES_ARRAY, this, context).asList();
        if (0 < allDocuments.size()) {
            List<DocumentMetadata> allVersions = InfostoreIterator.allVersionsWhere(whereClause, Metadata.VALUES_ARRAY, this, context).asList();
            objectPermissionLoader.add(allDocuments, context, objectPermissionLoader.load(folderId, context));
            removeDocuments(allDocuments, allVersions, date, session, null);
        }
    }

    protected void removeDocuments(final List<DocumentMetadata> allDocuments, final List<DocumentMetadata> allVersions, final long date, final ServerSession session, final List<DocumentMetadata> rejected) throws OXException {
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
                checkWriteLock(m, session);
                m.setLastModified(now);
                delDocs.add(m);
            }
        }

        final Context context = session.getContext();

        /*
         * Move records into del_* tables
         */
        perform(new ReplaceDocumentIntoDelTableAction(this, QUERIES, context, delDocs, session), true);
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
         * Delete documents, all versions and object permissions from database
         */
        perform(new DeleteVersionAction(this, QUERIES, context, delVers, session), true);
        perform(new DeleteDocumentAction(this, QUERIES, context, delDocs, session), true);
        perform(new DeleteObjectPermissionAction(this, context, delDocs), true);
        rememberForGuestCleanup(context.getContextId(), delDocs);
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
     * Remembers the permission entities of the supplied documents for subsequent guest cleanup tasks.
     *
     * @param contextID The context identifier
     * @param removedDocuments The documents being removed
     */
    private void rememberForGuestCleanup(int contextID, List<DocumentMetadata> removedDocuments) {
        if (null != removedDocuments && 0 < removedDocuments.size()) {
            for (DocumentMetadata document : removedDocuments) {
                List<ObjectPermission> objectPermissions = document.getObjectPermissions();
                if (null != objectPermissions && 0 < objectPermissions.size()) {
                    Map<Integer, Set<Integer>> cleanupList = guestCleanupList.get();
                    Set<Integer> entities = cleanupList.get(I(contextID));
                    if (null == entities) {
                        entities = new HashSet<Integer>(objectPermissions.size());
                        cleanupList.put(I(contextID), entities);
                    }
                    for (ObjectPermission permission : objectPermissions) {
                        if (false == permission.isGroup()) {
                            entities.add(I(permission.getEntity()));
                        }
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
    protected List<DocumentMetadata> moveDocuments(ServerSession session, List<DocumentMetadata> documents, long destinationFolderID, long sequenceNumber, boolean adjustFilenamesAsNeeded) throws OXException {
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
        List<DocumentMetadata> rejectedDocuments = new LinkedList<DocumentMetadata>();
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

        int numberOfDocuments = sourceDocuments.size();
        if (0 < numberOfDocuments) {
            /*
             * prepare move
             */
            Date now = new Date();
            Connection readConnection = null;
            FilenameReserver filenameReserver = new FilenameReserverImpl(session.getContext(), this);
            try {
                readConnection = getReadConnection(context);
                List<DocumentMetadata> tombstoneDocuments = new ArrayList<DocumentMetadata>(numberOfDocuments);
                List<DocumentMetadata> documentsToUpdate = new ArrayList<DocumentMetadata>(numberOfDocuments);
                List<DocumentMetadata> versionsToUpdate = new ArrayList<DocumentMetadata>();
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
                            if (reservation.wasSameTitle()) {
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
                perform(new ReplaceDocumentIntoDelTableAction(this, QUERIES, session.getContext(), tombstoneDocuments, session), true);
                /*
                 * Do the version control
                 */
                VersionControlUtil.doVersionControl(this, documentsToUpdate, sourceDocuments, destinationFolderID, context);
                /*
                 * perform document move
                 */
                perform(new UpdateDocumentAction(this, QUERIES, session.getContext(), documentsToUpdate, sourceDocuments, new Metadata[] { Metadata.LAST_MODIFIED_LITERAL, Metadata.MODIFIED_BY_LITERAL, Metadata.FOLDER_ID_LITERAL }, sequenceNumber, session), true);
                /*
                 * perform object permission inserts / removals
                 */
                if (0 < objectPermissionsToDelete.size()) {
                    perform(new DeleteObjectPermissionAction(this, context, objectPermissionsToDelete), true);
                    rememberForGuestCleanup(context.getContextId(), objectPermissionsToDelete);
                }
                /*
                 * perform version update (only required in case of adjusted filenames)
                 */
                if (0 < versionsToUpdate.size()) {
                    perform(new UpdateVersionAction(this, QUERIES, session.getContext(), versionsToUpdate, sourceDocuments, new Metadata[] { Metadata.FILENAME_LITERAL, Metadata.TITLE_LITERAL }, sequenceNumber, session), true);
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
        int[] objectIDs = Tools.getObjectIDArray(ids);
        List<DocumentMetadata> allDocuments = getAllDocuments(this, session.getContext(), objectIDs, Metadata.VALUES_ARRAY);
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
        List<DocumentMetadata> rejectedDocuments = moveDocuments(session, allDocuments, destinationFolderID, sequenceNumber, adjustFilenamesAsNeeded);
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

        Set<Integer> objectIDs = idsToFolders.keySet();
        String whereClause;
        if (1 == objectIDs.size()) {
            whereClause = "infostore.id=" + objectIDs.iterator().next();
        } else {
            StringBuilder stringBuilder = new StringBuilder("infostore.id IN (");
            Strings.join(objectIDs, ",", stringBuilder);
            whereClause = stringBuilder.append(')').toString();
        }
        List<DocumentMetadata> allDocuments = InfostoreIterator.allDocumentsWhere(whereClause, Metadata.VALUES_ARRAY, this, context).asList();
        List<DocumentMetadata> allVersions = InfostoreIterator.allVersionsWhere(whereClause, Metadata.VALUES_ARRAY, this, context).asList();
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
        Set<Integer> objectIDs = idsToFolders.keySet();
        String whereClause;
        if (1 == objectIDs.size()) {
            whereClause = "infostore.id=" + objectIDs.iterator().next();
        } else {
            StringBuilder stringBuilder = new StringBuilder("infostore.id IN (");
            Strings.join(objectIDs, ",", stringBuilder);
            whereClause = stringBuilder.append(')').toString();
        }
        List<DocumentMetadata> allDocuments = InfostoreIterator.allDocumentsWhere(whereClause, Metadata.VALUES_ARRAY, this, context).asList();
        List<DocumentMetadata> allVersions = InfostoreIterator.allVersionsWhere(whereClause, Metadata.VALUES_ARRAY, this, context).asList();

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
        perform(new ReplaceDocumentIntoDelTableAction(this, QUERIES, context, allDocuments, null), true);
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
         * Delete documents, all versions and object permissions from database
         */
        perform(new DeleteVersionAction(this, QUERIES, context, allVersions, null), true);
        perform(new DeleteDocumentAction(this, QUERIES, context, allDocuments, null), true);
        perform(new DeleteObjectPermissionAction(this, context, allDocuments), true);
        rememberForGuestCleanup(context.getContextId(), allDocuments);
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
        EffectiveInfostorePermission permission = security.getInfostorePermission(session, metadata);
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

        List<DocumentMetadata> allVersions = InfostoreIterator.allVersionsWhere("infostore_document.infostore_id = " + id + " AND infostore_document.version_number IN " + versions.toString() + " and infostore_document.version_number != 0 ", Metadata.VALUES_ARRAY, this, context).asList();

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

            perform(new UpdateVersionAction(this, QUERIES, context, version0, oldVersion0, new Metadata[] { Metadata.DESCRIPTION_LITERAL, Metadata.TITLE_LITERAL, Metadata.URL_LITERAL, Metadata.LAST_MODIFIED_LITERAL, Metadata.MODIFIED_BY_LITERAL, Metadata.FILE_MIMETYPE_LITERAL }, Long.MAX_VALUE, session), true);

            // Set new Version Number
            update.setVersion(db.getMaxActiveVersion(metadata.getId(), context, allVersions));
            updatedFields.add(Metadata.VERSION_LITERAL);
        }

        FilenameReserver filenameReserver = null;
        try {
            if (removeCurrent) {
                filenameReserver = new FilenameReserverImpl(context, db);
                metadata = load(metadata.getId(), update.getVersion(), context);
                FilenameReservation reservation = filenameReserver.reserve(metadata, true);
                if (reservation.wasAdjusted()) {
                    update.setFileName(reservation.getFilename());
                    updatedFields.add(Metadata.FILENAME_LITERAL);
                }
                if (reservation.wasSameTitle()) {
                    update.setTitle(reservation.getFilename());
                    updatedFields.add(Metadata.TITLE_LITERAL);
                }
            }
            perform(new UpdateDocumentAction(this, QUERIES, context, update, metadata, updatedFields.toArray(new Metadata[updatedFields.size()]), Long.MAX_VALUE, session), true);

            // Remove Versions
            perform(new DeleteVersionAction(this, QUERIES, context, allVersions, session), true);

            final int[] retval = new int[versionSet.size()];
            int i = 0;
            for (final Integer integer : versionSet) {
                retval[i++] = integer.intValue();
            }

            return retval;
        } finally {
            if (null != filenameReserver) {
                filenameReserver.cleanUp();
            }
        }
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
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, ServerSession session) throws OXException {
        return getDocuments(folderId, columns, sort, order, -1, -1, session);
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(long folderId, Metadata[] columns, Metadata sort, int order, int start, int end, Context context, User user, UserPermissionBits permissionBits) throws OXException {
        return getDocuments(context, user, permissionBits, folderId, columns, sort, order, start, end);
    }

    @Override
    public TimedResult<DocumentMetadata> getDocuments(final long folderId, Metadata[] columns, Metadata sort, int order, int start, int end, ServerSession session) throws OXException {
        return getDocuments(session.getContext(), session.getUser(), session.getUserPermissionBits(), folderId, columns, sort, order, start, end);
    }

    @Override
    public TimedResult<DocumentMetadata> getUserSharedDocuments(Metadata[] columns, Metadata sort, int order, int start, int end, ServerSession session) throws OXException {
        Metadata[] fields = Tools.getFieldsToQuery(columns, Metadata.LAST_MODIFIED_LITERAL, Metadata.ID_LITERAL, Metadata.FOLDER_ID_LITERAL);
        Context context = session.getContext();
        /*
         * search documents shared by user
         */
        List<DocumentMetadata> documents;
        InfostoreIterator iterator = null;
        try {
            iterator = InfostoreIterator.sharedDocumentsByUser(context, session.getUser(), fields, sort, order, start, end, db);
            documents = Tools.removeNonPrivate(iterator, session, db);
        } finally {
            SearchIterators.close(iterator);
        }
        if (contains(columns, Metadata.SHAREABLE_LITERAL)) {
            for (DocumentMetadata document : documents) {
                /*
                 * assume document still shareable if loaded via "shared documents" query
                 */
                document.setShareable(true);
            }
        }
        if (contains(columns, Metadata.LOCKED_UNTIL_LITERAL)) {
            documents = lockedUntilLoader.add(documents, context, (Map<Integer, List<Lock>>) null);
        }
        if (contains(columns, Metadata.OBJECT_PERMISSIONS_LITERAL)) {
            documents = objectPermissionLoader.add(documents, context, (Map<Integer, List<ObjectPermission>>) null);
        }
        return new InfostoreTimedResult(new SearchIteratorAdapter<DocumentMetadata>(documents.iterator(), documents.size()));
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
        final EffectiveInfostorePermission infoPerm = security.getInfostorePermission(session, id);
        if (false == infoPerm.canReadObject()) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        }
        Metadata[] cols = addLastModifiedIfNeeded(columns);
        InfostoreIterator iter = InfostoreIterator.versions(id, cols, sort, order, this, context);
        iter.setCustomizer(new DocumentCustomizer() {

            @Override
            public DocumentMetadata handle(DocumentMetadata document) {
                if (false == infoPerm.canReadObjectInFolder()) {
                    document.setOriginalFolderId(document.getFolderId());
                    document.setFolderId(getSharedFilesFolderID(session));
                    /*
                     * Re-sharing of files is not allowed.
                     */
                    document.setShareable(false);
                } else {
                    document.setShareable(infoPerm.canShareObject());
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
        final Map<Integer, List<ObjectPermission>> knownObjectPermissions = addObjectPermissions ? objectPermissionLoader.load(objectIDs, context) : null;
        /*
         * get items, checking permissions as lazy as possible
         */
        final Map<Long, EffectiveInfostoreFolderPermission> knownFolderPermissions = new HashMap<Long, EffectiveInfostoreFolderPermission>();
        InfostoreIterator iterator = InfostoreIterator.list(Autoboxing.I2i(objectIDs), cols, this, session.getContext());
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
                    List<ObjectPermission> objectPermissions = null != knownObjectPermissions ? knownObjectPermissions.get(I(document.getId())) : objectPermissionLoader.load(document.getId(), context);
                    if (null != objectPermissions) {
                        ObjectPermission matchingPermission = EffectiveObjectPermissions.find(user, objectPermissions);
                        if (null != matchingPermission) {
                            EffectiveObjectPermission objectPermission = EffectiveObjectPermissions.convert(FolderObject.INFOSTORE, (int) document.getFolderId(), document.getId(), matchingPermission, session.getUserPermissionBits());
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
                        document.setOriginalFolderId(document.getFolderId());
                        document.setFolderId(getSharedFilesFolderID(session));
                        /*
                         * Re-sharing of files is not allowed.
                         */
                        document.setShareable(false);
                    }
                } else {
                    /*
                     * adjust parent folder id to match requested identifier
                     */
                    Long requestedFolderID = idsToFolders.get(I(document.getId()));
                    if (getSharedFilesFolderID(session) == requestedFolderID.intValue()) {
                        document.setOriginalFolderId(document.getFolderId());
                        document.setFolderId(requestedFolderID.longValue());
                        /*
                         * Re-sharing of files is not allowed.
                         */
                        document.setShareable(false);
                    } else {
                        document.setShareable(folderPermission.canShareAllObjects() || folderPermission.canShareOwnObjects() && document.getCreatedBy() == user.getId());
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
        final Map<Integer, List<Lock>> locks = loadLocksInFolderAndExpireOldLocks(folderId, session);

        InfostoreIterator newIter = null;
        InfostoreIterator modIter = null;
        InfostoreIterator delIter = null;
        Metadata[] cols = addLastModifiedIfNeeded(columns);
        final int sharedFilesFolderID = getSharedFilesFolderID(session);
        if (folderId == sharedFilesFolderID) {
            DocumentCustomizer customizer = new DocumentCustomizer() {

                @Override
                public DocumentMetadata handle(DocumentMetadata document) {
                    document.setOriginalFolderId(document.getFolderId());
                    document.setFolderId(sharedFilesFolderID);
                    return document;
                }
            };
            newIter = InfostoreIterator.newSharedDocumentsForUser(context, user, columns, sort, order, updateSince, this);
            newIter.setCustomizer(customizer);
            modIter = InfostoreIterator.modifiedSharedDocumentsForUser(context, user, columns, sort, order, updateSince, this);
            modIter.setCustomizer(customizer);
            if (!ignoreDeleted) {
                delIter = InfostoreIterator.deletedSharedDocumentsForUser(context, user, columns, sort, order, updateSince, this);
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
                newIter = InfostoreIterator.newDocumentsByCreator(folderId, user.getId(), cols, sort, order, updateSince, this, context);
                modIter = InfostoreIterator.modifiedDocumentsByCreator(folderId, user.getId(), cols, sort, order, updateSince, this, context);
                if (!ignoreDeleted) {
                    delIter = InfostoreIterator.deletedDocumentsByCreator(folderId, user.getId(), sort, order, updateSince, this, context);
                }
            } else {
                newIter = InfostoreIterator.newDocuments(folderId, cols, sort, order, updateSince, this, context);
                modIter = InfostoreIterator.modifiedDocuments(folderId, cols, sort, order, updateSince, this, context);
                if (!ignoreDeleted) {
                    delIter = InfostoreIterator.deletedDocuments(folderId, sort, order, updateSince, this, context);
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

        Delta<DocumentMetadata> delta = new FileDelta(newIter, modIter, it, System.currentTimeMillis());
        if (addLocked) {
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
                performQuery(context, QUERIES.getSharedDocumentsSequenceNumbersQuery(versionsOnly, true, contextId, user.getId(), user.getGroups()), new ResultProcessor<Void>() {

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
                performQuery(context, QUERIES.getSharedDocumentsSequenceNumbersQuery(versionsOnly, false, contextId, user.getId(), user.getGroups()), new ResultProcessor<Void>() {

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

            performQuery(context, QUERIES.getFolderSequenceNumbersQuery(folderIds, versionsOnly, true, contextId), new ResultProcessor<Void>() {

                @Override
                public Void process(ResultSet rs) throws SQLException {
                    while (rs.next()) {
                        sequenceNumbers.put(Long.valueOf(rs.getLong(1)), Long.valueOf(rs.getLong(2)));
                    }
                    return null;
                }
            });
            performQuery(context, QUERIES.getFolderSequenceNumbersQuery(folderIds, versionsOnly, false, contextId), new ResultProcessor<Void>() {

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
        final InfostoreIterator documents = InfostoreIterator.documents(folderId, new Metadata[] { Metadata.ID_LITERAL }, null, -1, -1, -1, this, session.getContext());
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
        if (folderId == getSharedFilesFolderID(session)) {
            InfostoreIterator it = InfostoreIterator.sharedDocumentsForUser(session.getContext(), session.getUser(), ObjectPermission.READ, new Metadata[] { Metadata.ID_LITERAL }, null, 0, -1, -1, this);
            return it.asList().size();
        }

        boolean onlyOwn = false;
        User user = session.getUser();
        final EffectiveInfostoreFolderPermission isperm = security.getFolderPermission(folderId, session.getContext(), user, session.getUserPermissionBits());
        if (!(isperm.canReadAllObjects()) && !(isperm.canReadOwnObjects())) {
            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
        } else if (!isperm.canReadAllObjects() && isperm.canReadOwnObjects()) {
            onlyOwn = true;
        }
        return db.countDocuments(folderId, onlyOwn, session.getContext(), user);
    }

    @Override
    public long getTotalSize(long folderId, ServerSession session) throws OXException {
        return db.getTotalSize(session.getContext(), folderId);
    }

    @Override
    public boolean hasFolderForeignObjects(final long folderId, final ServerSession session) throws OXException {
        if (folderId == getSharedFilesFolderID(session)) {
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
    public void removeUser(final int userId, final Context ctx, Integer destUser, final ServerSession session) throws OXException {
        db.removeUser(userId, ctx, destUser, session, lockManager);
    }

    @Override
    public SearchIterator<DocumentMetadata> search(ServerSession session, String query, int folderId, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException {
        return search(session, query, folderId, false, cols, sortedBy, dir, start, end);
    }

    @Override
    public SearchIterator<DocumentMetadata> search(ServerSession session, String query, int folderId, boolean includeSubfolders, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException {
        /*
         * get folders for search and corresponding permissions
         */
        List<Integer> all = new ArrayList<Integer>();
        List<Integer> own = new ArrayList<Integer>();
        Map<Integer, EffectiveInfostoreFolderPermission> permissionsByFolderID;
        if (NOT_SET == folderId || NO_FOLDER == folderId) {
            permissionsByFolderID = Tools.gatherVisibleFolders(session, security, db, null, all, own);
        } else if (includeSubfolders) {
            permissionsByFolderID = Tools.gatherVisibleFolders(session, security, db, folderId, all, own);
        } else {
            permissionsByFolderID = Tools.gatherVisibleFolders(session, security, db, new int[] { folderId }, all, own);
        }
        if (all.isEmpty() && own.isEmpty()) {
            return SearchIteratorAdapter.emptyIterator();
        }
        /*
         * perform search & enhance results with additional metadata as needed
         */
        Metadata[] fields = Tools.getFieldsToQuery(cols, Metadata.ID_LITERAL, Metadata.FOLDER_ID_LITERAL);
        SearchIterator<DocumentMetadata> searchIterator = searchEngine.search(session, query, all, own, fields, sortedBy, dir, start, end);
        return postProcessSearch(session, searchIterator, fields, permissionsByFolderID);
    }

    @Override
    public SearchIterator<DocumentMetadata> search(ServerSession session, SearchTerm<?> searchTerm, int[] folderIds, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException {
        /*
         * get folders for search and corresponding permissions
         */
        List<Integer> all = new ArrayList<Integer>();
        List<Integer> own = new ArrayList<Integer>();
        int[] requestedFolderIDs = null == folderIds || 0 == folderIds.length ? null : folderIds;
        Map<Integer, EffectiveInfostoreFolderPermission> permissionsByFolderID = Tools.gatherVisibleFolders(session, security, db, requestedFolderIDs, all, own);
        if (all.isEmpty() && own.isEmpty()) {
            return SearchIteratorAdapter.emptyIterator();
        }
        /*
         * perform search & enhance results with additional metadata as needed
         */
        Metadata[] fields = Tools.getFieldsToQuery(cols, Metadata.ID_LITERAL, Metadata.FOLDER_ID_LITERAL);
        SearchIterator<DocumentMetadata> searchIterator = searchEngine.search(session, searchTerm, all, own, fields, sortedBy, dir, start, end);
        return postProcessSearch(session, searchIterator, fields, permissionsByFolderID);
    }

    @Override
    public SearchIterator<DocumentMetadata> search(ServerSession session, SearchTerm<?> searchTerm, int folderId, boolean includeSubfolders, Metadata[] cols, Metadata sortedBy, int dir, int start, int end) throws OXException {
        /*
         * get folders for search and corresponding permissions
         */
        List<Integer> all = new ArrayList<Integer>();
        List<Integer> own = new ArrayList<Integer>();
        Map<Integer, EffectiveInfostoreFolderPermission> permissionsByFolderID;
        if (includeSubfolders) {
            permissionsByFolderID = Tools.gatherVisibleFolders(session, security, db, folderId, all, own);
        } else {
            permissionsByFolderID = Tools.gatherVisibleFolders(session, security, db, new int[] { folderId }, all, own);
        }
        if (all.isEmpty() && own.isEmpty()) {
            return SearchIteratorAdapter.emptyIterator();
        }
        /*
         * perform search & enhance results with additional metadata as needed
         */
        Metadata[] fields = Tools.getFieldsToQuery(cols, Metadata.ID_LITERAL, Metadata.FOLDER_ID_LITERAL);
        SearchIterator<DocumentMetadata> searchIterator = searchEngine.search(session, searchTerm, all, own, fields, sortedBy, dir, start, end);
        return postProcessSearch(session, searchIterator, fields, permissionsByFolderID);
    }

    /**
     * Adds additional metadata based on the requested columns to a search iterator result.
     *
     * @param session The session
     * @param searchIterator The search iterator as fetched from the search engine
     * @param fields The requested fields
     * @param permissionsByFolderID A map holding the effective permissions of all used folders during the search, or <code>null</code> to
     *            assume all documents being readable & shareable by the current user
     * @return The enhanced search results
     */
    private SearchIterator<DocumentMetadata> postProcessSearch(ServerSession session, SearchIterator<DocumentMetadata> searchIterator, Metadata[] fields, final Map<Integer, EffectiveInfostoreFolderPermission> permissionsByFolderID) throws OXException {
        /*
         * check requested metadata
         */
        int sharedFilesFolderID = FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID;
        boolean containsSharedFilesResults = null != permissionsByFolderID && permissionsByFolderID.containsKey(I(sharedFilesFolderID));
        boolean addLocked = contains(fields, Metadata.LOCKED_UNTIL_LITERAL);
        boolean addNumberOfVersions = contains(fields, Metadata.NUMBER_OF_VERSIONS_LITERAL);
        boolean addObjectPermissions = contains(fields, Metadata.OBJECT_PERMISSIONS_LITERAL);
        boolean addShareable = contains(fields, Metadata.SHAREABLE_LITERAL);
        if (false == addLocked && false == addNumberOfVersions && false == addObjectPermissions && false == addShareable && false == containsSharedFilesResults) {
            /*
             * stick to plain search iterator result if no further metadata is needed
             */
            return searchIterator;
        }
        /*
         * prepare customizable search iterator to add additional metadata as requested
         */
        List<DocumentMetadata> documents;
        try {
            documents = SearchIterators.asList(searchIterator);
        } finally {
            SearchIterators.close(searchIterator);
        }
        if (null == documents || 0 == documents.size()) {
            return SearchIteratorAdapter.emptyIterator();
        }
        List<Integer> objectIDs = Tools.getIDs(documents);
        /*
         * add object permissions if requested or needed to evaluate "shareable" flag
         */
        if (addObjectPermissions || addShareable || containsSharedFilesResults) {
            documents = objectPermissionLoader.add(documents, session.getContext(), objectIDs);
        }
        if (addLocked) {
            documents = lockedUntilLoader.add(documents, session.getContext(), objectIDs);
        }
        if (addNumberOfVersions) {
            documents = numberOfVersionsLoader.add(documents, session.getContext(), objectIDs);
        }
        if (addShareable || containsSharedFilesResults) {
            boolean hasSharedFolderAccess = session.getUserConfiguration().hasFullSharedFolderAccess();
            for (DocumentMetadata document : documents) {
                int physicalFolderID = (int) document.getFolderId();
                if (null == permissionsByFolderID) {
                    /*
                     * assume document shareable & readable at physical location
                     */
                    document.setShareable(hasSharedFolderAccess);
                    continue;
                }
                EffectiveInfostoreFolderPermission folderPermission = permissionsByFolderID.get(I(physicalFolderID));
                if (null != folderPermission && (folderPermission.canReadAllObjects() || folderPermission.canReadOwnObjects() && document.getCreatedBy() == session.getUserId())) {
                    /*
                     * document is readable at physical location
                     */
                    document.setShareable(folderPermission.canShareAllObjects() || folderPermission.canShareOwnObjects() && document.getCreatedBy() == session.getUserId());
                } else {
                    /*
                     * set 'shareable' flag and parent folder based on object permissions
                     */
                    List<ObjectPermission> objectPermissions = document.getObjectPermissions();
                    if (null != objectPermissions) {
                        ObjectPermission matchingPermission = EffectiveObjectPermissions.find(session.getUser(), objectPermissions);
                        if (null != matchingPermission && matchingPermission.canRead()) {
                            document.setOriginalFolderId(document.getFolderId());
                            document.setFolderId(sharedFilesFolderID);
                            /*
                             * Re-sharing of files is not allowed.
                             */
                            document.setShareable(false);
                        } else {
                            throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
                        }
                    } else {
                        throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
                    }
                }
            }
        }
        return new SearchIteratorDelegator<DocumentMetadata>(documents);
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
            if (1 == filesToRemove.size()) {
                FileRemoveInfo removeInfo = filesToRemove.get(0);
                getFileStorage(removeInfo.folderAdmin, removeInfo.contextId).deleteFile(removeInfo.fileId);
            } else {
                Map<QuotaFileStorage, List<String>> removalsPerStorage = new HashMap<QuotaFileStorage, List<String>>();
                for (FileRemoveInfo removeInfo : filesToRemove) {
                    QuotaFileStorage fileStorage = getFileStorage(removeInfo.folderAdmin, removeInfo.contextId);
                    List<String> removals = removalsPerStorage.get(fileStorage);
                    if (null == removals) {
                        removals = new ArrayList<String>();
                        removalsPerStorage.put(fileStorage, removals);
                    }
                    removals.add(removeInfo.fileId);
                }
                for (Map.Entry<QuotaFileStorage, List<String>> entry : removalsPerStorage.entrySet()) {
                    entry.getKey().deleteFiles(entry.getValue().toArray(new String[entry.getValue().size()]));
                }
            }
        }
        /*
         * schedule guest cleanup tasks as needed
         */
        Map<Integer, Set<Integer>> guestsToCleanup = guestCleanupList.get();
        if (null != guestsToCleanup && 0 < guestsToCleanup.size()) {
            for (Entry<Integer, Set<Integer>> entry : guestsToCleanup.entrySet()) {
                int contextID = i(entry.getKey());
                Set<Integer> guestIDs = filterGuests(contextID, entry.getValue());
                if (null != guestIDs && 0 < guestIDs.size()) {
                    ShareService shareService = ServerServiceRegistry.getServize(ShareService.class);
                    if (null != shareService) {
                        shareService.scheduleGuestCleanup(contextID, I2i(guestIDs));
                    }
                }
            }
        }
        super.commit();
    }

    private Set<Integer> filterGuests(int contextID, Set<Integer> entityIDs) throws OXException {
        if (null == entityIDs || 0 == entityIDs.size()) {
            return Collections.emptySet();
        }
        UserService userService = ServerServiceRegistry.getServize(UserService.class);
        Set<Integer> guestIDs = new HashSet<Integer>(entityIDs.size());
        for (Integer id : entityIDs) {
            try {
                if (userService.isGuest(id.intValue(), contextID)) {
                    guestIDs.add(id);
                }
            } catch (OXException e) {
                if (UserExceptionCode.USER_NOT_FOUND.equals(e)) {
                    continue;
                }
                throw e;
            }
        }
        return guestIDs;
    }

    @Override
    public void finish() throws OXException {
        fileIdRemoveList.set(null);
        guestCleanupList.set(null);
        db.finish();
        ServiceMethod.FINISH.callUnsafe(security);
        lockManager.finish();
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
        guestCleanupList.set(new HashMap<Integer, Set<Integer>>());
        db.startTransaction();
        ServiceMethod.START_TRANSACTION.callUnsafe(security);
        lockManager.startTransaction();
        super.startTransaction();
    }

    @Override
    public void setProvider(final DBProvider provider) {
        super.setProvider(provider);
        db.setProvider(this);
        ServiceMethod.SET_PROVIDER.call(security, this);
        ServiceMethod.SET_PROVIDER.call(lockManager, this);
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
                iterator = InfostoreIterator.list(Autoboxing.I2i(incompleteTuples.keySet()), new Metadata[] { Metadata.ID_LITERAL, Metadata.FOLDER_ID_LITERAL }, this, context);
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

    /**
     * Gets an chain of validation checks to use before saving documents.
     *
     * @return The validation chain
     */
    private ValidationChain getValidationChain() {
        return new ValidationChain(new InvalidCharactersValidator(), new FilenamesMayNotContainSlashesValidator(), new ObjectPermissionValidator(this));
    }

    private TimedResult<DocumentMetadata> getDocuments(Context context, final User user, UserPermissionBits permissionBits, final long folderId, Metadata[] columns, Metadata sort, int order, int start, int end) throws OXException {
        /*
         * get appropriate infostore iterator
         */
        Metadata[] cols = addLastModifiedIfNeeded(columns);
        final long sharedFilesFolderID = getSharedFilesFolderID(context, user);
        final EffectiveInfostoreFolderPermission folderPermission;
        InfostoreIterator iterator;
        if (sharedFilesFolderID == folderId) {
            /*
             * load readable documents from virtual shared files folder
             */
            folderPermission = null;
            iterator = InfostoreIterator.sharedDocumentsForUser(context, user, ObjectPermission.READ, cols, sort, order, start, end, db);
            iterator.setCustomizer(new DocumentCustomizer() {

                @Override
                public DocumentMetadata handle(DocumentMetadata document) {
                    document.setOriginalFolderId(document.getFolderId());
                    document.setFolderId(sharedFilesFolderID);
                    return document;
                }
            });
        } else {

            if (null == permissionBits) {
                throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
            }
            /*
             * load all / own objects from physical folder
             */
            folderPermission = security.getFolderPermission(folderId, context, user, permissionBits);
            if (folderPermission.canReadAllObjects()) {
                iterator = InfostoreIterator.documents(folderId, cols, sort, order, start, end, this, context);
            } else if (folderPermission.canReadOwnObjects()) {
                iterator = InfostoreIterator.documentsByCreator(folderId, user.getId(), cols, sort, order, start, end, this, context);
            } else {
                throw InfostoreExceptionCodes.NO_READ_PERMISSION.create();
            }
        }
        /*
         * check requested metadata
         */
        boolean addLocked = contains(columns, Metadata.LOCKED_UNTIL_LITERAL);
        boolean addNumberOfVersions = contains(columns, Metadata.NUMBER_OF_VERSIONS_LITERAL);
        boolean addObjectPermissions = contains(columns, Metadata.OBJECT_PERMISSIONS_LITERAL);
        boolean addShareable = contains(columns, Metadata.SHAREABLE_LITERAL);
        if (false == addLocked && false == addNumberOfVersions && false == addObjectPermissions && false == addShareable) {
            /*
             * stick to plain infostore timed result if no further metadata is needed
             */
            return new InfostoreTimedResult(iterator);
        }
        /*
         * prepare customizable timed result to add additional metadata as requested
         */
        final List<DocumentMetadata> documents = iterator.asList();
        if (0 == documents.size()) {
            return com.openexchange.groupware.results.Results.emptyTimedResult();
        }
        long maxSequenceNumber = 0;
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
         * add object permissions if requested or needed to evaluate "shareable" flag
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
        if (addShareable) {
            final boolean hasSharedFolderAccess = permissionBits.hasFullSharedFolderAccess();
            timedResult = new CustomizableTimedResult<DocumentMetadata>(timedResult, new Customizer<DocumentMetadata>() {

                @Override
                public DocumentMetadata customize(DocumentMetadata document) throws OXException {
                    if (false == hasSharedFolderAccess || sharedFilesFolderID == folderId) {
                        /*
                         * no permissions to share or re-share
                         */
                        document.setShareable(false);
                    } else {
                        /*
                         * set "shareable" flag based on folder permissions
                         */
                        document.setShareable(folderPermission.canWriteAllObjects() || folderPermission.canWriteOwnObjects() && document.getCreatedBy() == user.getId());
                    }
                    return document;
                }
            });
        }
        return timedResult;
    }

    /**
     * Gets the identifier of the folder holding single documents shared to the session's user based on extended object permissions.
     *
     * @param session The session
     * @return The identifier of the shared documents folder
     */
    private int getSharedFilesFolderID(ServerSession session) {
        return getSharedFilesFolderID(session.getContext(), session.getUser());
    }

    /**
     * Gets the identifier of the folder holding single documents shared to the session's user based on extended object permissions.
     *
     * @param context The context
     * @param user The user
     * @return The identifier of the shared documents folder
     */
    private int getSharedFilesFolderID(Context context, User user) {
        return FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID;
    }

}
