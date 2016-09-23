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

package com.openexchange.folderstorage.database;

import static com.openexchange.folderstorage.database.DatabaseFolderStorageUtility.extractIDs;
import static com.openexchange.folderstorage.database.DatabaseFolderStorageUtility.getUserPermissionBits;
import static com.openexchange.folderstorage.database.DatabaseFolderStorageUtility.localizeFolderNames;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.util.Tools.getUnsignedInteger;
import static com.openexchange.server.impl.OCLPermission.ADMIN_PERMISSION;
import static com.openexchange.server.impl.OCLPermission.DELETE_ALL_OBJECTS;
import static com.openexchange.server.impl.OCLPermission.READ_ALL_OBJECTS;
import static com.openexchange.server.impl.OCLPermission.READ_FOLDER;
import static com.openexchange.server.impl.OCLPermission.WRITE_ALL_OBJECTS;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.folderstorage.AfterReadAwareFolderStorage;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.LockCleaningFolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StorageParametersUtility;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.SystemContentType;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.folderstorage.database.contentType.UnboundContentType;
import com.openexchange.folderstorage.database.getfolder.SharedPrefixFolder;
import com.openexchange.folderstorage.database.getfolder.SystemInfostoreFolder;
import com.openexchange.folderstorage.database.getfolder.SystemPrivateFolder;
import com.openexchange.folderstorage.database.getfolder.SystemPublicFolder;
import com.openexchange.folderstorage.database.getfolder.SystemRootFolder;
import com.openexchange.folderstorage.database.getfolder.SystemSharedFolder;
import com.openexchange.folderstorage.database.getfolder.VirtualListFolder;
import com.openexchange.folderstorage.outlook.OutlookFolderStorage;
import com.openexchange.folderstorage.outlook.osgi.Services;
import com.openexchange.folderstorage.outlook.sql.Delete;
import com.openexchange.folderstorage.tx.TransactionManager;
import com.openexchange.folderstorage.type.DocumentsType;
import com.openexchange.folderstorage.type.MusicType;
import com.openexchange.folderstorage.type.PicturesType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.folderstorage.type.SystemType;
import com.openexchange.folderstorage.type.TemplatesType;
import com.openexchange.folderstorage.type.TrashType;
import com.openexchange.folderstorage.type.VideosType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.infostore.InfostoreFacades;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.CallerRunsCompletionService;
import com.openexchange.java.Collators;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareService;
import com.openexchange.share.recipient.RecipientType;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderBatchLoader;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.oxfolder.OXFolderLoader;
import com.openexchange.tools.oxfolder.OXFolderLoader.IdAndName;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderSQL;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.sql.DBUtils;
import gnu.trove.ConcurrentTIntObjectHashMap;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * {@link DatabaseFolderStorage} - The database folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DatabaseFolderStorage implements AfterReadAwareFolderStorage, LockCleaningFolderStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DatabaseFolderStorage.class);

    private static final String PARAM_CONNECTION = DatabaseParameterConstants.PARAM_CONNECTION;

    /**
     * Simple interface for providing and closing a connection.
     */
    private static interface ConnectionProvider {

        /**
         * Gets the (active) connection.
         *
         * @return The connection
         */
        Connection getConnection();

        /**
         * Closes underlying connection.
         */
        void close();
    }

    private static final class NonClosingConnectionProvider implements ConnectionProvider {

        private final ConnectionMode connection;

        //private final DatabaseService databaseService;

        //private final int contextId;

        protected NonClosingConnectionProvider(final ConnectionMode connection/* , final DatabaseService databaseService, final int contextId */) {
            super();
            this.connection = connection;
            //this.databaseService = databaseService;
            //this.contextId = contextId;
        }

        @Override
        public Connection getConnection() {
            return connection.connection;
        }

        @Override
        public void close() {
            // Nothing to do
        }
    }

    private static final class ClosingConnectionProvider implements ConnectionProvider {

        private final DatabaseService databaseService;

        private final ConnectionMode connection;

        private final int contextId;

        protected ClosingConnectionProvider(final ConnectionMode connection, final DatabaseService databaseService, final int contextId) {
            super();
            this.connection = connection;
            this.databaseService = databaseService;
            this.contextId = contextId;
        }

        @Override
        public Connection getConnection() {
            return connection.connection;
        }

        @Override
        public void close() {
            connection.close(databaseService, contextId);
        }
    }

    static final EnumSet<Mode> WRITEES = EnumSet.of(Mode.WRITE, Mode.WRITE_AFTER_READ);

    // -------------------------------------------------------------------------------------------------------------------------- //

    private final ServiceLookup services;

    /**
     * Initializes a new {@link DatabaseFolderStorage}.
     */
    public DatabaseFolderStorage(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void clearCache(final int userId, final int contextId) {
        /*
         * Nothing to do...
         */
    }

    private static final ConcurrentTIntObjectHashMap<Long> STAMPS = new ConcurrentTIntObjectHashMap<Long>(128);
    private static final long DELAY = 60 * 60 * 1000;
    private static final int MAX = 3;

    @Override
    public void checkConsistency(final String treeId, final StorageParameters storageParameters) throws OXException {
        final int contextId = storageParameters.getContextId();
        final long now = System.currentTimeMillis();
        final Long stamp = STAMPS.get(contextId);
        if ((null != stamp) && ((stamp.longValue() + DELAY) > now)) {
            return;
        }
        // Delay exceeded
        STAMPS.remove(contextId);
        final DatabaseService databaseService = services.getService(DatabaseService.class);
        Connection con = null;
        boolean close = true;
        Mode mode = Mode.READ;
        boolean modified = false;
        try {
            {
                final ConnectionMode conMode = optParameter(ConnectionMode.class, DatabaseParameterConstants.PARAM_CONNECTION, storageParameters);
                if (null != conMode) {
                    con = conMode.connection;
                    mode = conMode.readWrite;
                    close = false;
                } else {
                    con = databaseService.getReadOnly(contextId);
                }
            }
            final ServerSession session = ServerSessionAdapter.valueOf(storageParameters.getSession());
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            /*
             * Determine folder with non-existing parents
             */
            final Context context = session.getContext();
            int[] nonExistingParents = OXFolderSQL.getNonExistingParents(context, con);
            if (null == nonExistingParents || 0 == nonExistingParents.length) {
                return;
            }
            /*
             * Upgrade to read-write connection & repeat if check was performed with read-only connection
             */
            if (Mode.READ == mode) {
                if (close) {
                    databaseService.backReadOnly(contextId, con);
                }
                con = databaseService.getWritable(contextId);
                mode = Mode.WRITE_AFTER_READ;
                close = true;
                // Query again...
                nonExistingParents = OXFolderSQL.getNonExistingParents(context, con);
                if (null == nonExistingParents || 0 == nonExistingParents.length) {
                    return;
                }
            }
            /*
             * Some variables
             */
            final TIntSet shared = new TIntHashSet();
            final OXFolderManager manager = OXFolderManager.getInstance(session, con, con);
            final OXFolderAccess folderAccess = getFolderAccess(context, con);
            final int userId = session.getUserId();
            /*
             * Iterate folders
             */
            int runCount = 0;
            final TIntSet tmp = new TIntHashSet();
            do {
                for (final int folderId : nonExistingParents) {
                    if (folderId >= FolderObject.MIN_FOLDER_ID) {
                        if (FolderObject.SHARED == folderAccess.getFolderType(folderId, userId)) {
                            shared.add(folderId);
                        } else {
                            manager.deleteValidatedFolder(folderId, now, -1, true);
                            modified = true;
                        }
                    }
                }
                tmp.clear();
                tmp.addAll(OXFolderSQL.getNonExistingParents(context, con));
                if (tmp.isEmpty()) {
                    nonExistingParents = null;
                } else {
                    tmp.removeAll(shared.toArray());
                    for (int i = 0; i < FolderObject.MIN_FOLDER_ID; i++) {
                        tmp.remove(i);
                    }
                    nonExistingParents = tmp.toArray();
                }
            } while (++runCount <= MAX && null != nonExistingParents && nonExistingParents.length > 0);
        } finally {
            if (null != con && close) {
                if (Mode.READ == mode) {
                    databaseService.backReadOnly(contextId, con);
                } else {
                    if (modified) {
                        databaseService.backWritable(contextId, con);
                    } else {
                        databaseService.backWritableAfterReading(contextId, con);
                    }
                }
            }
            STAMPS.put(contextId, Long.valueOf(now));
        }
    }

    @Override
    public ContentType[] getSupportedContentTypes() {
        return new ContentType[] { TaskContentType.getInstance(), CalendarContentType.getInstance(), ContactContentType.getInstance(), InfostoreContentType.getInstance(), UnboundContentType.getInstance(), SystemContentType.getInstance() };
    }

    @Override
    public ContentType getDefaultContentType() {
        return ContactContentType.getInstance();
    }

    @Override
    public void commitTransaction(final StorageParameters params) throws OXException {
        final ConnectionMode con;
        try {
            con = optParameter(ConnectionMode.class, PARAM_CONNECTION, params);
        } catch (final OXException e) {
            LOG.warn("Storage already committed:\n{}", params.getCommittedTrace(), e);
            return;
        }
        if (null == con) {
            return;
        }

        if (!con.controlledExternally) {
            if (WRITEES.contains(con.readWrite)) {
                try {
                    con.connection.commit();
                } catch (final SQLException e) {
                    throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
                } finally {
                    DBUtils.autocommit(con.connection);
                    final DatabaseService databaseService = services.getService(DatabaseService.class);
                    if (null != databaseService) {
                        con.close(databaseService, params.getContext().getContextId());
                    }
                }
            } else {
                final DatabaseService databaseService = services.getService(DatabaseService.class);
                if (null != databaseService) {
                    databaseService.backReadOnly(params.getContext(), con.connection);
                }
            }
        }

        final FolderType folderType = getFolderType();
        params.putParameter(folderType, PARAM_CONNECTION, null);
        params.markCommitted();
    }

    @Override
    public void restore(final String treeId, final String folderIdentifier, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(Mode.WRITE, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            final int folderId = Integer.parseInt(folderIdentifier);
            final Context context = storageParameters.getContext();
            FolderObject.loadFolderObjectFromDB(folderId, context, con, false, false, "del_oxfolder_tree", "del_oxfolder_permissions");
            /*
             * From backup to working table
             */
            OXFolderSQL.restore(folderId, context, null);
        } catch (final NumberFormatException e) {
            throw FolderExceptionErrorMessage.INVALID_FOLDER_ID.create(folderIdentifier);
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    // private static final TIntSet SPECIALS = new TIntHashSet(new int[] { FolderObject.SYSTEM_PRIVATE_FOLDER_ID, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID });

    @Override
    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(Mode.WRITE, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            final long millis = System.currentTimeMillis();

            final FolderObject createMe = new FolderObject();
            createMe.setCreatedBy(session.getUserId());
            createMe.setCreationDate(new Date(millis));
            createMe.setCreator(session.getUserId());
            createMe.setDefaultFolder(false);
            {
                final String name = folder.getName();
                if (null != name) {
                    createMe.setFolderName(name);
                }
            }
            createMe.setLastModified(new Date(millis));
            createMe.setModifiedBy(session.getUserId());
            {
                final ContentType ct = folder.getContentType();
                if (null != ct) {
                    createMe.setModule(getModuleByContentType(ct));
                }
            }
            {
                final String parentId = folder.getParentID();
                if (null != parentId) {
                    createMe.setParentFolderID(Integer.parseInt(parentId));
                }
            }
            {
                final Type t = folder.getType();
                if (null == t) {
                    /*
                     * Determine folder type by examining parent folder
                     */
                    createMe.setType(getFolderType(createMe.getModule(), createMe.getParentFolderID(), storageParameters.getContext(), con));
                } else {
                    createMe.setType(getTypeByFolderType(t));
                }
            }
            // Meta
            {
                final Map<String, Object> meta = folder.getMeta();
                if (null != meta) {
                    createMe.setMeta(meta);
                }
            }
            // Permissions
            final Permission[] perms = folder.getPermissions();
            if (null != perms) {
                final OCLPermission[] oclPermissions = new OCLPermission[perms.length];
                for (int i = 0; i < perms.length; i++) {
                    final Permission p = perms[i];
                    final OCLPermission oclPerm = new OCLPermission();
                    oclPerm.setEntity(p.getEntity());
                    oclPerm.setGroupPermission(p.isGroup());
                    oclPerm.setFolderAdmin(p.isAdmin());
                    oclPerm.setAllPermission(p.getFolderPermission(), p.getReadPermission(), p.getWritePermission(), p.getDeletePermission());
                    oclPerm.setSystem(p.getSystem());
                    oclPermissions[i] = oclPerm;
                }
                createMe.setPermissionsAsArray(oclPermissions);
            } else {
                final int parentFolderID = createMe.getParentFolderID();
                /*
                 * Prepare
                 */
                final FolderObject parent = getFolderObject(parentFolderID, storageParameters.getContext(), con, storageParameters);
                final int userId = storageParameters.getUserId();
                final boolean isShared = parent.isShared(userId);
                final boolean isSystem = FolderObject.SYSTEM_TYPE == parent.getType();
                final List<OCLPermission> parentPermissions = parent.getPermissions();
                /*
                 * Create permission list
                 */
                final List<OCLPermission> permissions = new ArrayList<OCLPermission>((isSystem ? 1 : parentPermissions.size()) + 1);
                if (isShared) {
                    permissions.add(newMaxPermissionFor(parent.getCreatedBy()));
                    permissions.add(newStandardPermissionFor(userId));
                } else {
                    permissions.add(newMaxPermissionFor(userId));
                }
                if (!isSystem) {
                    final TIntSet ignore = new TIntHashSet(4);
                    ignore.add(userId);
                    ignore.add(OCLPermission.ALL_GUESTS);
                    if (isShared) {
                        ignore.add(parent.getCreatedBy());
                    }
                    for (final OCLPermission permission : parentPermissions) {
                        if ((permission.getSystem() <= 0) && !ignore.contains(permission.getEntity())) {
                            if (false == permission.isGroupPermission()) {
                                GuestInfo guestInfo = ServerServiceRegistry.getServize(ShareService.class).getGuestInfo(session, permission.getEntity());
                                if (null != guestInfo && RecipientType.ANONYMOUS.equals(guestInfo.getRecipientType())) {
                                    continue; // don't inherit anonymous share links
                                }
                            }
                            permissions.add(permission);
                        }
                    }
                }
                createMe.setPermissions(permissions);
            }
            // Create
            final OXFolderManager folderManager = OXFolderManager.getInstance(session, con, con);
            folderManager.createFolder(createMe, true, millis);
            final int fuid = createMe.getObjectID();
            if (fuid <= 0) {
                throw OXFolderExceptionCode.CREATE_FAILED.create(new Object[0]);
            }
            folder.setID(String.valueOf(fuid));
            // Handle warnings
            final List<OXException> warnings = folderManager.getWarnings();
            if (null != warnings) {
                for (final OXException warning : warnings) {
                    storageParameters.addWarning(warning);
                }
            }
        } finally {
            provider.close();
        }
    }

    private static OCLPermission newMaxPermissionFor(final int entity) {
        final OCLPermission oclPerm = new OCLPermission();
        oclPerm.setEntity(entity);
        oclPerm.setGroupPermission(false);
        oclPerm.setFolderAdmin(true);
        oclPerm.setAllPermission(ADMIN_PERMISSION, ADMIN_PERMISSION, ADMIN_PERMISSION, ADMIN_PERMISSION);
        oclPerm.setSystem(0);
        return oclPerm;
    }

    private static OCLPermission newStandardPermissionFor(final int entity) {
        final OCLPermission oclPerm = new OCLPermission();
        oclPerm.setEntity(entity);
        oclPerm.setGroupPermission(false);
        oclPerm.setFolderAdmin(false);
        oclPerm.setAllPermission(READ_FOLDER, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, DELETE_ALL_OBJECTS);
        oclPerm.setSystem(0);
        return oclPerm;
    }

    private static final int[] PUBLIC_FOLDER_IDS = { FolderObject.SYSTEM_PUBLIC_FOLDER_ID, FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID };

    /**
     * Determines the target folder type for new folders below a parent folder.
     *
     * @param module The module identifier of the new folder
     * @param parentId The ID of the parent folder
     * @param ctx The context
     * @param con A readable database connection
     * @return The folder type
     * @throws OXException
     * @throws OXException
     */
    private static int getFolderType(int module, final int parentId, final Context ctx, final Connection con) throws OXException, OXException {
        int type = -1;
        int pid = parentId;
        /*
         * Special treatment for system folders
         */
        if (pid == FolderObject.SYSTEM_SHARED_FOLDER_ID) {
            pid = FolderObject.SYSTEM_PRIVATE_FOLDER_ID;
            type = FolderObject.SHARED;
        } else if (pid == FolderObject.SYSTEM_PRIVATE_FOLDER_ID) {
            type = FolderObject.PRIVATE;
        } else if (Arrays.binarySearch(PUBLIC_FOLDER_IDS, pid) >= 0) {
            type = FolderObject.PUBLIC;
        } else {
            type = getFolderAccess(ctx, con).getFolderType(pid);
        }
        return type;
    }

    @Override
    public void clearFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(Mode.WRITE, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final FolderObject fo = getFolderObject(Integer.parseInt(folderId), storageParameters.getContext(), con, storageParameters);
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            final OXFolderManager folderManager = OXFolderManager.getInstance(session, con, con);
            folderManager.clearFolder(fo, true, System.currentTimeMillis());

            final List<OXException> warnings = folderManager.getWarnings();
            if (null != warnings) {
                for (final OXException warning : warnings) {
                    storageParameters.addWarning(warning);
                }
            }
        } finally {
            provider.close();
        }
    }

    @Override
    public void deleteFolder(final String treeId, final String folderIdentifier, final StorageParameters storageParameters) throws OXException {
        int folderId = Integer.parseInt(folderIdentifier);

        ConnectionProvider provider = getConnection(Mode.WRITE, storageParameters);
        try {
            Connection con = provider.getConnection();
            FolderObject fo = new FolderObject();
            fo.setObjectID(folderId);
            Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            FolderServiceDecorator decorator = storageParameters.getDecorator();
            boolean hardDelete = null != decorator && (Boolean.TRUE.equals(decorator.getProperty("hardDelete")) || decorator.getBoolProperty("hardDelete"));
            OXFolderManager folderManager = OXFolderManager.getInstance(session, con, con);
            folderManager.deleteFolder(fo, true, System.currentTimeMillis(), hardDelete);

            // Cleanse from other folder storage, too
            if (hardDelete) {
                Delete.hardDeleteFolder(session.getContextId(), getUnsignedInteger(OutlookFolderStorage.OUTLOOK_TREE_ID), session.getUserId(), folderIdentifier, true, true, con);
            } else {
                Delete.deleteFolder(session.getContextId(), getUnsignedInteger(OutlookFolderStorage.OUTLOOK_TREE_ID), session.getUserId(), folderIdentifier, true, true, con);
            }

            List<OXException> warnings = folderManager.getWarnings();
            if (null != warnings) {
                for (final OXException warning : warnings) {
                    storageParameters.addWarning(warning);
                }
            }
        } finally {
            provider.close();
        }
    }

    @Override
    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(Mode.READ, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            final Context context = storageParameters.getContext();
            int folderId = -1;
            if (TaskContentType.getInstance().equals(contentType)) {
                folderId = OXFolderSQL.getUserDefaultFolder(session.getUserId(), FolderObject.TASK, con, context);
            } else if (CalendarContentType.getInstance().equals(contentType)) {
                folderId = OXFolderSQL.getUserDefaultFolder(session.getUserId(), FolderObject.CALENDAR, con, context);
            } else if (ContactContentType.getInstance().equals(contentType)) {
                folderId = OXFolderSQL.getUserDefaultFolder(session.getUserId(), FolderObject.CONTACT, con, context);
            } else if (InfostoreContentType.getInstance().equals(contentType)) {
                if (TrashType.getInstance().equals(type)) {
                    folderId = OXFolderSQL.getUserDefaultFolder(user.getId(), FolderObject.INFOSTORE, FolderObject.TRASH, con, context);
                } else if (DocumentsType.getInstance().equals(type)) {
                    folderId = OXFolderSQL.getUserDefaultFolder(user.getId(), FolderObject.INFOSTORE, FolderObject.DOCUMENTS, con, context);
                } else if (TemplatesType.getInstance().equals(type)) {
                    folderId = OXFolderSQL.getUserDefaultFolder(user.getId(), FolderObject.INFOSTORE, FolderObject.TEMPLATES, con, context);
                } else if (VideosType.getInstance().equals(type)) {
                    folderId = OXFolderSQL.getUserDefaultFolder(user.getId(), FolderObject.INFOSTORE, FolderObject.VIDEOS, con, context);
                } else if (MusicType.getInstance().equals(type)) {
                    folderId = OXFolderSQL.getUserDefaultFolder(user.getId(), FolderObject.INFOSTORE, FolderObject.MUSIC, con, context);
                } else if (PicturesType.getInstance().equals(type)) {
                    folderId = OXFolderSQL.getUserDefaultFolder(user.getId(), FolderObject.INFOSTORE, FolderObject.PICTURES, con, context);
                } else {
                    folderId = OXFolderSQL.getUserDefaultFolder(user.getId(), FolderObject.INFOSTORE, con, context);
                }
            }
            if (-1 == folderId) {
                throw FolderExceptionErrorMessage.NO_DEFAULT_FOLDER.create(contentType, treeId);
            }
            return String.valueOf(folderId);
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public Type getTypeByParent(final User user, final String treeId, final String parentId, final StorageParameters storageParameters) throws OXException {
        /*
         * Special treatment for system folders
         */
        final int pid = Integer.parseInt(parentId);
        if (pid == FolderObject.SYSTEM_SHARED_FOLDER_ID) {
            return SharedType.getInstance();
        } else if (pid == FolderObject.SYSTEM_PRIVATE_FOLDER_ID) {
            return PrivateType.getInstance();
        } else if (Arrays.binarySearch(PUBLIC_FOLDER_IDS, pid) >= 0) {
            return PublicType.getInstance();
        } else {
            final ConnectionProvider provider = getConnection(Mode.READ, storageParameters);
            try {
                final FolderObject p = getFolderAccess(storageParameters.getContext(), provider.getConnection()).getFolderObject(pid);
                final int parentType = p.getType();
                if (FolderObject.PRIVATE == parentType) {
                    return p.getCreatedBy() == user.getId() ? PrivateType.getInstance() : SharedType.getInstance();
                } else if (FolderObject.PUBLIC == parentType) {
                    return PublicType.getInstance();
                } else if (FolderObject.TRASH == parentType) {
                    return TrashType.getInstance();
                }
            } finally {
                provider.close();
            }
        }
        return SystemType.getInstance();
    }

    @Override
    public boolean containsForeignObjects(final User user, final String treeId, final String folderIdentifier, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(Mode.READ, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final Context ctx = storageParameters.getContext();
            /*
             * A numeric folder identifier
             */
            final int folderId = getUnsignedInteger(folderIdentifier);
            if (folderId < 0) {
                throw OXFolderExceptionCode.NOT_EXISTS.create(folderIdentifier, Integer.valueOf(ctx.getContextId()));
            }
            if (FolderObject.SYSTEM_ROOT_FOLDER_ID == folderId) {
                return false;
            } else if (FolderObject.SYSTEM_SHARED_FOLDER_ID == folderId) {
                /*
                 * The system shared folder
                 */
                return false;
            } else if (FolderObject.SYSTEM_PUBLIC_FOLDER_ID == folderId) {
                /*
                 * The system public folder
                 */
                return false;
            } else if (FolderObject.SYSTEM_INFOSTORE_FOLDER_ID == folderId) {
                /*
                 * The system infostore folder
                 */
                return false;
            } else if (FolderObject.SYSTEM_PRIVATE_FOLDER_ID == folderId) {
                /*
                 * The system private folder
                 */
                return false;
            } else if (Arrays.binarySearch(VIRTUAL_IDS, folderId) >= 0) {
                /*
                 * A virtual database folder
                 */
                return true;
            } else {
                /*
                 * A non-virtual database folder
                 */
                final OXFolderAccess folderAccess = getFolderAccess(ctx, con);
                return folderAccess.containsForeignObjects(getFolderObject(folderId, ctx, con, storageParameters), storageParameters.getSession(), ctx);
            }
        } finally {
            provider.close();
        }
    }

    @Override
    public boolean isEmpty(final String treeId, final String folderIdentifier, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(Mode.READ, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final Context ctx = storageParameters.getContext();
            /*
             * A numeric folder identifier
             */
            final int folderId = getUnsignedInteger(folderIdentifier);
            if (folderId < 0) {
                throw OXFolderExceptionCode.NOT_EXISTS.create(folderIdentifier, Integer.valueOf(ctx.getContextId()));
            }
            if (FolderObject.SYSTEM_ROOT_FOLDER_ID == folderId) {
                return true;
            } else if (FolderObject.SYSTEM_SHARED_FOLDER_ID == folderId) {
                /*
                 * The system shared folder
                 */
                return true;
            } else if (FolderObject.SYSTEM_PUBLIC_FOLDER_ID == folderId) {
                /*
                 * The system public folder
                 */
                return true;
            } else if (FolderObject.SYSTEM_INFOSTORE_FOLDER_ID == folderId) {
                /*
                 * The system infostore folder
                 */
                return true;
            } else if (FolderObject.SYSTEM_PRIVATE_FOLDER_ID == folderId) {
                /*
                 * The system private folder
                 */
                return true;
            } else if (Arrays.binarySearch(VIRTUAL_IDS, folderId) >= 0) {
                /*
                 * A virtual database folder
                 */
                return false;
            } else {
                /*
                 * A non-virtual database folder
                 */
                final OXFolderAccess folderAccess = getFolderAccess(ctx, con);
                return folderAccess.isEmpty(getFolderObject(folderId, ctx, con, storageParameters), storageParameters.getSession(), ctx);
            }
        } finally {
            provider.close();
        }
    }

    @Override
    public void updateLastModified(final long lastModified, final String treeId, final String folderIdentifier, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(Mode.WRITE, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final Context ctx = storageParameters.getContext();
            final int folderId = getUnsignedInteger(folderIdentifier);
            if (getFolderAccess(ctx, con).getFolderLastModified(folderId).after(new Date(lastModified))) {
                throw FolderExceptionErrorMessage.CONCURRENT_MODIFICATION.create();
            }
            OXFolderSQL.updateLastModified(folderId, lastModified, storageParameters.getUserId(), con, ctx);
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public Folder getFolder(final String treeId, final String folderIdentifier, final StorageParameters storageParameters) throws OXException {
        return getFolder(treeId, folderIdentifier, StorageType.WORKING, storageParameters);
    }

    private static final int[] VIRTUAL_IDS = { FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID, FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID, FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID };

    @Override
    public Folder getFolder(final String treeId, final String folderIdentifier, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(Mode.READ, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final User user = storageParameters.getUser();
            final Context ctx = storageParameters.getContext();
            final UserPermissionBits userPermissionBits = getUserPermissionBits(con, storageParameters);

            final DatabaseFolder retval;

            if (StorageType.WORKING.equals(storageType)) {
                if (DatabaseFolderStorageUtility.hasSharedPrefix(folderIdentifier)) {
                    retval = SharedPrefixFolder.getSharedPrefixFolder(folderIdentifier, user, ctx);
                } else {
                    /*
                     * A numeric folder identifier
                     */
                    final int folderId = getUnsignedInteger(folderIdentifier);

                    if (folderId < 0) {
                        throw OXFolderExceptionCode.NOT_EXISTS.create(folderIdentifier, Integer.valueOf(ctx.getContextId()));
                    }

                    if (FolderObject.SYSTEM_ROOT_FOLDER_ID == folderId) {
                        retval = SystemRootFolder.getSystemRootFolder();
                    } else if (Arrays.binarySearch(VIRTUAL_IDS, folderId) >= 0) {
                        /*
                         * A virtual database folder
                         */
                        final boolean altNames = StorageParametersUtility.getBoolParameter("altNames", storageParameters);
                        retval = VirtualListFolder.getVirtualListFolder(folderId, altNames);
                    } else {
                        /*
                         * A non-virtual database folder
                         */
                        final FolderObject fo = getFolderObject(folderId, ctx, con, storageParameters);
                        final boolean altNames = StorageParametersUtility.getBoolParameter("altNames", storageParameters);
                        retval = DatabaseFolderConverter.convert(fo, user, userPermissionBits, ctx, storageParameters.getSession(), altNames, con);
                    }
                }
            } else {
                /*
                 * Get from backup tables
                 */
                final int folderId = getUnsignedInteger(folderIdentifier);

                if (folderId < 0) {
                    throw OXFolderExceptionCode.NOT_EXISTS.create(folderIdentifier, Integer.valueOf(ctx.getContextId()));
                }

                final FolderObject fo = FolderObject.loadFolderObjectFromDB(folderId, ctx, con, true, false, "del_oxfolder_tree", "del_oxfolder_permissions");
                retval = new DatabaseFolder(fo);
            }
            retval.setTreeID(treeId);
            // TODO: Subscribed?

            return retval;
        } finally {
            provider.close();
        }
    }

    @Override
    public Folder prepareFolder(final String treeId, final Folder folder, final StorageParameters storageParameters) throws OXException {
        /*
         * Owner
         */
        final int owner = folder.getCreatedBy();
        if (owner < 0) {
            return folder;
        }
        /*
         * Check shared...
         */
        if (owner != storageParameters.getUserId() && PrivateType.getInstance().equals(folder.getType())) {
            try {
                return getFolder(treeId, folder.getID(), StorageType.WORKING, storageParameters);
            } catch (final OXException e) {
                if (OXFolderExceptionCode.NOT_EXISTS.equals(e) || FolderExceptionErrorMessage.NOT_FOUND.equals(e)) {
                    return getFolder(treeId, folder.getID(), StorageType.BACKUP, storageParameters);
                }
                throw e;
            }
        }
        return folder;
    }

    @Override
    public List<Folder> getFolders(final String treeId, final List<String> folderIdentifiers, final StorageParameters storageParameters) throws OXException {
        return getFolders(treeId, folderIdentifiers, StorageType.WORKING, storageParameters);
    }

    @Override
    public List<Folder> getFolders(final String treeId, final List<String> folderIdentifiers, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        ConnectionProvider provider = null;
        try {
            final User user = storageParameters.getUser();
            final Context ctx = storageParameters.getContext();
            final boolean altNames = StorageParametersUtility.getBoolParameter("altNames", storageParameters);
            /*
             * Either from working or from backup storage type
             */
            if (StorageType.WORKING.equals(storageType)) {
                final int size = folderIdentifiers.size();
                final Folder[] ret = new Folder[size];
                final TIntIntMap map = new TIntIntHashMap(size);
                /*
                 * Check for special folder identifier
                 */
                for (int index = 0; index < size; index++) {
                    final String folderIdentifier = folderIdentifiers.get(index);
                    if (DatabaseFolderStorageUtility.hasSharedPrefix(folderIdentifier)) {
                        ret[index] = SharedPrefixFolder.getSharedPrefixFolder(folderIdentifier, user, ctx);
                    } else {
                        /*
                         * A numeric folder identifier
                         */
                        final int folderId = getUnsignedInteger(folderIdentifier);
                        if (FolderObject.SYSTEM_ROOT_FOLDER_ID == folderId) {
                            ret[index] = SystemRootFolder.getSystemRootFolder();
                        } else if (Arrays.binarySearch(VIRTUAL_IDS, folderId) >= 0) {
                            ret[index] = VirtualListFolder.getVirtualListFolder(folderId, altNames);
                        } else {
                            map.put(folderId, index);
                        }
                    }
                }
                /*
                 * Batch load
                 */
                if (0 < map.size()) {
                    provider = getConnection(Mode.READ, storageParameters);
                    try {
                        Connection con = provider.getConnection();
                        Session session = storageParameters.getSession();
                        UserPermissionBits userPermissionBits = getUserPermissionBits(con, storageParameters);
                        for (FolderObject folder : getFolderObjects(map.keys(), ctx, con, storageParameters)) {
                            if (null != folder) {
                                int index = map.get(folder.getObjectID());
                                ret[index] = DatabaseFolderConverter.convert(folder, user, userPermissionBits, ctx, session, altNames, con);
                            }
                        }
                    } finally {
                        if (null != provider) {
                            provider.close();
                            provider = null;
                        }
                    }
                }
                /*
                 * Set proper tree identifier
                 */
                for (final Folder folder : ret) {
                    if (null != folder) {
                        folder.setTreeID(treeId);
                    }
                }
                /*
                 * Return
                 */
                final int length = ret.length;
                final List<Folder> l = new ArrayList<Folder>(length);
                for (int i = 0; i < length; i++) {
                    final Folder folder = ret[i];
                    if (null != folder) {
                        l.add(folder);
                    } else {
                        storageParameters.addWarning(FolderExceptionErrorMessage.NOT_FOUND.create(Integer.valueOf(folderIdentifiers.get(i)), treeId));
                    }
                }
                return l;
            }
            /*
             * Get from backup tables
             */
            final TIntList list = new TIntArrayList(folderIdentifiers.size());
            for (final String folderIdentifier : folderIdentifiers) {
                list.add(getUnsignedInteger(folderIdentifier));
            }
            provider = getConnection(Mode.READ, storageParameters);
            final Connection con = provider.getConnection();
            final List<FolderObject> folders = OXFolderBatchLoader.loadFolderObjectsFromDB(list.toArray(), ctx, con, true, false, "del_oxfolder_tree", "del_oxfolder_permissions");
            provider.close();
            provider = null;
            final int size = folders.size();
            final List<Folder> ret = new ArrayList<Folder>(size);
            for (int i = 0; i < size; i++) {
                final FolderObject fo = folders.get(i);
                if (null == fo) {
                    storageParameters.addWarning(FolderExceptionErrorMessage.NOT_FOUND.create(Integer.valueOf(list.get(i)), treeId));
                } else {
                    final DatabaseFolder df = new DatabaseFolder(fo);
                    df.setTreeID(treeId);
                    ret.add(df);
                }
            }
            return ret;
        } catch (final OXException e) {
            throw e;
        } finally {
            if (null != provider) {
                provider.close();
            }
        }
    }

    @Override
    public FolderType getFolderType() {
        return DatabaseFolderType.getInstance();
    }

    @Override
    public SortableId[] getVisibleFolders(final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws OXException {
        final User user = storageParameters.getUser();
        final ConnectionProvider provider = getConnection(Mode.READ, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final int userId = user.getId();
            final Context ctx = storageParameters.getContext();
            final UserPermissionBits userPermissionBits = getUserPermissionBits(con, storageParameters);
            final int iType = getTypeByFolderTypeWithShared(type);
            final int iModule = getModuleByContentType(contentType);
            final List<FolderObject> list = ((FolderObjectIterator) OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfType(userId, user.getGroups(), userPermissionBits.getAccessibleModules(), iType, new int[] { iModule }, ctx, con)).asList();
            if (FolderObject.PRIVATE == iType) {
                /*
                 * Remove shared ones manually
                 */
                for (final Iterator<FolderObject> iterator = list.iterator(); iterator.hasNext();) {
                    if (iterator.next().getCreatedBy() != userId) {
                        iterator.remove();
                    }
                }
            } else if (FolderObject.PUBLIC == iType && FolderObject.CONTACT == iModule) {
                try {
                    /*
                     * Add global address book manually
                     */
                    final CapabilityService capsService = ServerServiceRegistry.getInstance().getService(CapabilityService.class);
                    if (null == capsService || capsService.getCapabilities(user.getId(), ctx.getContextId()).contains("gab")) {
                        final FolderObject gab = getFolderObject(FolderObject.SYSTEM_LDAP_FOLDER_ID, ctx, con, storageParameters);
                        if (gab.isVisible(userId, userPermissionBits)) {
                            gab.setFolderName(StringHelper.valueOf(user.getLocale()).getString(FolderStrings.SYSTEM_LDAP_FOLDER_NAME));
                            list.add(gab);
                        }
                    }
                } catch (final RuntimeException e) {
                    throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, Integer.valueOf(ctx.getContextId()));
                }
            }
            /*
             * localize & sort folders
             */
            localizeFolderNames(list, user.getLocale());
            if (FolderObject.PRIVATE == iType) {
                /*
                 * Sort them by default-flag and name: <user's default folder>, <aaa>, <bbb>, ... <zzz>
                 */
                Collections.sort(list, new FolderObjectComparator(user.getLocale(), ctx));
            } else {
                /*
                 * Sort them by name only
                 */
                if (FolderObject.SHARED == iType) {
                    /*
                     * Pre-load users with current connection
                     */
                    Map<String, Integer> tracker = new HashMap<String, Integer>(list.size());
                    for (FolderObject folder : list) {
                        Integer pre = tracker.get(folder.getFolderName());
                        if (null == pre) {
                            int owner = folder.getCreatedBy();
                            if (owner > 0) {
                                tracker.put(folder.getFolderName(), Integer.valueOf(owner));
                            }
                        } else {
                            int owner = folder.getCreatedBy();
                            if (owner > 0) {
                                int otherOwner = pre.intValue();
                                if (otherOwner != owner) {
                                    UserStorage.getInstance().loadIfAbsent(otherOwner, ctx, con);
                                    UserStorage.getInstance().loadIfAbsent(owner, ctx, con);
                                }
                            }
                        }
                    }
                }
                Collections.sort(list, new FolderNameComparator(user.getLocale(), storageParameters.getContext()));
            }
            /*
             * return sortable identifiers
             */
            return extractIDs(list);
        } finally {
            provider.close();
        }
    }

    @Override
    public SortableId[] getUserSharedFolders(String treeId, ContentType contentType, StorageParameters storageParameters) throws OXException {
        ConnectionProvider provider = getConnection(Mode.READ, storageParameters);
        SearchIterator<FolderObject> searchIterator = null;
        try {
            /*
             * load & filter user shared folders of content type
             */
            Connection connection = provider.getConnection();
            User user = storageParameters.getUser();
            UserPermissionBits userPermissionBits = getUserPermissionBits(connection, storageParameters);
            searchIterator = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(user.getId(), user.getGroups(), userPermissionBits.getAccessibleModules(), getModuleByContentType(contentType), storageParameters.getContext(), connection);
            List<FolderObject> folders = getUserSharedFolders(searchIterator, connection, storageParameters);
            /*
             * localize / sort folders & return their sortable identifiers
             */
            if (0 == folders.size()) {
                return new SortableId[0];
            }
            localizeFolderNames(folders, user.getLocale());
            if (1 < folders.size()) {
                Collections.sort(folders, new FolderObjectComparator(user.getLocale(), storageParameters.getContext()));
            }
            return extractIDs(folders);
        } finally {
            SearchIterators.close(searchIterator);
            provider.close();
        }
    }

    @Override
    public SortableId[] getSubfolders(final String treeId, final String parentIdentifier, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(Mode.READ, storageParameters);
        try {
            final User user = storageParameters.getUser();
            final Context ctx = storageParameters.getContext();
            final Connection con = provider.getConnection();
            final UserPermissionBits userPermissionBits = getUserPermissionBits(con, storageParameters);
            if (DatabaseFolderStorageUtility.hasSharedPrefix(parentIdentifier)) {
                final List<FolderIdNamePair> subfolderIds = SharedPrefixFolder.getSharedPrefixFolderSubfolders(parentIdentifier, user, userPermissionBits, ctx, con);
                final List<SortableId> list = new ArrayList<SortableId>(subfolderIds.size());
                int i = 0;
                for (final FolderIdNamePair props : subfolderIds) {
                    list.add(new DatabaseId(props.fuid, i++, props.name));
                }
                return list.toArray(new SortableId[list.size()]);
            }

            final int parentId = Integer.parseInt(parentIdentifier);

            if (FolderObject.SYSTEM_ROOT_FOLDER_ID == parentId) {
                final List<String[]> subfolderIds = SystemRootFolder.getSystemRootFolderSubfolder(user, userPermissionBits, ctx, con);
                final List<SortableId> list = new ArrayList<SortableId>(subfolderIds.size());
                int i = 0;
                for (final String[] sa : subfolderIds) {
                    list.add(new DatabaseId(sa[0], i++, sa[1]));
                }
                return list.toArray(new SortableId[list.size()]);
            }

            if (Arrays.binarySearch(VIRTUAL_IDS, parentId) >= 0) {
                /*
                 * A virtual database folder
                 */
                final List<String[]> subfolderIds = VirtualListFolder.getVirtualListFolderSubfolders(parentId, user, userPermissionBits, ctx, con);
                final int size = subfolderIds.size();
                final List<SortableId> list = new ArrayList<SortableId>(size);
                for (int i = 0; i < size; i++) {
                    final String[] sa = subfolderIds.get(i);
                    list.add(new DatabaseId(sa[0], i, sa[1]));
                }
                return list.toArray(new SortableId[list.size()]);
            }

            if (FolderObject.SYSTEM_PRIVATE_FOLDER_ID == parentId) {
                /*
                 * The system private folder
                 */
                final List<String[]> subfolderIds = SystemPrivateFolder.getSystemPrivateFolderSubfolders(user, userPermissionBits, ctx, con);
                final int size = subfolderIds.size();
                final List<SortableId> list = new ArrayList<SortableId>(size);
                for (int i = 0; i < size; i++) {
                    final String[] sa = subfolderIds.get(i);
                    list.add(new DatabaseId(sa[0], i, sa[1]));
                }
                return list.toArray(new SortableId[list.size()]);
            }

            if (FolderObject.SYSTEM_SHARED_FOLDER_ID == parentId) {
                /*
                 * The system shared folder
                 */
                final List<String[]> subfolderIds = SystemSharedFolder.getSystemSharedFolderSubfolder(user, userPermissionBits, ctx, con);
                final int size = subfolderIds.size();
                final List<SortableId> list = new ArrayList<SortableId>(size);
                for (int i = 0; i < size; i++) {
                    final String[] sa = subfolderIds.get(i);
                    list.add(new DatabaseId(sa[0], i, sa[1]));
                }
                return list.toArray(new SortableId[list.size()]);
            }

            if (FolderObject.SYSTEM_PUBLIC_FOLDER_ID == parentId) {
                /*
                 * The system public folder
                 */
                final List<String[]> subfolderIds = SystemPublicFolder.getSystemPublicFolderSubfolders(user, userPermissionBits, ctx, con);
                final int size = subfolderIds.size();
                final List<SortableId> list = new ArrayList<SortableId>(size);
                for (int i = 0; i < size; i++) {
                    final String[] sa = subfolderIds.get(i);
                    list.add(new DatabaseId(sa[0], i, sa[1]));
                }
                return list.toArray(new SortableId[list.size()]);
            }

            if (FolderObject.SYSTEM_INFOSTORE_FOLDER_ID == parentId) {
                /*
                 * The system infostore folder
                 */
                final Session s = storageParameters.getSession();

                List<String[]> l = null;
                if (REAL_TREE_ID.equals(treeId)) {
                    /*
                     * File storage accounts
                     */
                    final Queue<FileStorageAccount> fsAccounts = new ConcurrentLinkedQueue<FileStorageAccount>();
                    final FileStorageServiceRegistry fsr = Services.getService(FileStorageServiceRegistry.class);
                    if (null == fsr) {
                        // Do nothing
                    } else {
                        CompletionService<Void> completionService = new CallerRunsCompletionService<Void>();
                        int taskCount = 0;
                        try {
                            final List<FileStorageService> allServices = fsr.getAllServices();
                            for (final FileStorageService fsService : allServices) {
                                Callable<Void> task = new Callable<Void>() {

                                    @Override
                                    public Void call() throws Exception {
                                        /*
                                         * Check if file storage service provides a root folder
                                         */
                                        List<FileStorageAccount> userAccounts = null;
                                        if (fsService instanceof AccountAware) {
                                            userAccounts = ((AccountAware) fsService).getAccounts(s);
                                        }
                                        if (null == userAccounts) {
                                            userAccounts = fsService.getAccountManager().getAccounts(s);
                                        }
                                        for (final FileStorageAccount userAccount : userAccounts) {
                                            if ("infostore".equals(userAccount.getId()) || FileStorageAccount.DEFAULT_ID.equals(userAccount.getId())) {
                                                // Ignore infostore file storage and default account
                                                continue;
                                            }
                                            fsAccounts.add(userAccount);
                                        }
                                        return null;
                                    }
                                };
                                completionService.submit(task);
                                taskCount++;
                            }
                        } catch (final OXException e) {
                            LOG.error("", e);
                        }
                        for (int i = taskCount; i-- > 0;) {
                            completionService.take();
                        }
                        if (fsAccounts.isEmpty()) {
                            // Do nothing
                        } else {
                            l = new LinkedList<String[]>();
                            List<FileStorageAccount> accountList = new ArrayList<FileStorageAccount>(fsAccounts);
                            Collections.sort(accountList, new FileStorageAccountComparator(user.getLocale()));
                            final int sz = accountList.size();
                            final String fid = FileStorageFolder.ROOT_FULLNAME;
                            for (int i = 0; i < sz; i++) {
                                final FileStorageAccount fsa = accountList.get(i);
                                final String serviceId;
                                if (fsa instanceof com.openexchange.file.storage.ServiceAware) {
                                    serviceId = ((com.openexchange.file.storage.ServiceAware) fsa).getServiceId();
                                } else {
                                    final FileStorageService tmp = fsa.getFileStorageService();
                                    serviceId = null == tmp ? null : tmp.getId();
                                }
                                FolderID folderID = new FolderID(serviceId, fsa.getId(), fid);
                                l.add(new String[] { folderID.toUniqueID(), fsa.getDisplayName() });
                            }
                        }
                    }
                }

                boolean altNames = StorageParametersUtility.getBoolParameter("altNames", storageParameters);
                List<String[]> subfolderIds = SystemInfostoreFolder.getSystemInfostoreFolderSubfolders(user, userPermissionBits, ctx, altNames, storageParameters.getSession(), con);

                List<SortableId> list = new ArrayList<SortableId>(subfolderIds.size() + (null == l ? 0 : l.size()));
                int in = 0;
                for (String[] sa : subfolderIds) {
                    list.add(new DatabaseId(sa[0], in++, sa[1]));
                }

                if (null != l) {
                    for (String[] sa : l) {
                        list.add(new DatabaseId(sa[0], in++, sa[1]));
                    }
                }

                return list.toArray(new SortableId[list.size()]);
            }

            if (FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID == parentId) {
                if (!InfostoreFacades.isInfoStoreAvailable()) {
                    final Session session = storageParameters.getSession();
                    final FileStorageAccount defaultAccount = DatabaseFolderConverter.getDefaultFileStorageAccess(session);
                    if (null != defaultAccount) {
                        final FileStorageService fileStorageService = defaultAccount.getFileStorageService();
                        final String defaultId = FileStorageAccount.DEFAULT_ID;
                        final FileStorageAccountAccess defaultFileStorageAccess = fileStorageService.getAccountAccess(defaultId, session);
                        defaultFileStorageAccess.connect();
                        try {
                            final FileStorageFolder personalFolder = defaultFileStorageAccess.getFolderAccess().getPersonalFolder();
                            FolderID folderID = new FolderID(fileStorageService.getId(), defaultAccount.getId(), personalFolder.getId());
                            return new SortableId[] { new DatabaseId(folderID.toUniqueID(), 0, personalFolder.getName()) };
                            // TODO: Shared?
                        } finally {
                            defaultFileStorageAccess.close();
                        }
                    }
                }
            }

            if (FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID == parentId) {
                if (!InfostoreFacades.isInfoStoreAvailable()) {
                    final Session session = storageParameters.getSession();
                    final FileStorageAccount defaultAccount = DatabaseFolderConverter.getDefaultFileStorageAccess(session);
                    if (null != defaultAccount) {
                        final FileStorageService fileStorageService = defaultAccount.getFileStorageService();
                        final String defaultId = FileStorageAccount.DEFAULT_ID;
                        final FileStorageAccountAccess defaultFileStorageAccess = fileStorageService.getAccountAccess(defaultId, session);
                        defaultFileStorageAccess.connect();
                        try {
                            final FileStorageFolder[] publicFolders = defaultFileStorageAccess.getFolderAccess().getPublicFolders();
                            final SortableId[] ret = new SortableId[publicFolders.length];
                            final String serviceId = fileStorageService.getId();
                            final String accountId = defaultAccount.getId();
                            for (int i = 0; i < publicFolders.length; i++) {
                                final FileStorageFolder folder = publicFolders[i];
                                FolderID folderID = new FolderID(serviceId, accountId, folder.getId());
                                ret[i] = new DatabaseId(folderID.toUniqueID(), i, folder.getName());
                            }
                            return ret;
                        } finally {
                            defaultFileStorageAccess.close();
                        }
                    }
                }
            }

            /*-
             * IDs already sorted by default_flag DESC, fname
             *
             * TODO: Ensure locale-specific ordering is maintained
             */
            final boolean doDBSorting = true;
            if (doDBSorting) {
                final List<IdAndName> idAndNames = OXFolderLoader.getSubfolderIdAndNames(parentId, storageParameters.getContext(), con);
                final int size = idAndNames.size();
                final List<SortableId> list = new ArrayList<SortableId>(size);
                for (int i = 0; i < size; i++) {
                    final IdAndName idAndName = idAndNames.get(i);
                    list.add(new DatabaseId(idAndName.getFolderId(), i, idAndName.getName()));
                }
                return list.toArray(new SortableId[size]);
            }
            /*
             * Ensure locale-specific ordering is maintained
             */
            final List<FolderObject> subfolders;
            {
                final List<Integer> subfolderIds = FolderObject.getSubfolderIds(parentId, storageParameters.getContext(), con);
                final int[] arr = new int[subfolderIds.size()];
                for (int i = 0; i < arr.length; i++) {
                    arr[i] = subfolderIds.get(i).intValue();
                }
                subfolders = getFolderObjects(arr, storageParameters.getContext(), con, storageParameters);
            }
            final ServerSession session;
            {
                final Session s = storageParameters.getSession();
                if (null == s) {
                    throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
                }
                if (s instanceof ServerSession) {
                    session = (ServerSession) s;
                } else {
                    session = ServerSessionAdapter.valueOf(s);
                }
            }
            Collections.sort(subfolders, new FolderObjectComparator(session.getUser().getLocale(), storageParameters.getContext()));
            final int size = subfolders.size();
            final List<SortableId> list = new ArrayList<SortableId>(size);
            for (int i = 0; i < size; i++) {
                final FolderObject folderObject = subfolders.get(i);
                list.add(new DatabaseId(folderObject.getObjectID(), i, folderObject.getFolderName()));
            }
            return list.toArray(new SortableId[size]);
        } catch (SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (InterruptedException e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }

    @Override
    public void rollback(final StorageParameters params) {
        final ConnectionMode con;
        try {
            con = optParameter(ConnectionMode.class, PARAM_CONNECTION, params);
        } catch (final OXException e) {
            LOG.error("", e);
            return;
        }
        if (null == con) {
            return;
        }

        if (!con.controlledExternally) {
            params.putParameter(getFolderType(), PARAM_CONNECTION, null);
            if (con.isWritable()) {
                try {
                    DBUtils.rollback(con.connection);
                } finally {
                    DBUtils.autocommit(con.connection);
                    final DatabaseService databaseService = services.getService(DatabaseService.class);
                    if (null != databaseService) {
                        con.close(databaseService, params.getContext().getContextId());
                    }
                }
            } else {
                final DatabaseService databaseService = services.getService(DatabaseService.class);
                if (null != databaseService) {
                    databaseService.backReadOnly(params.getContext(), con.connection);
                }
            }
        }

        params.putParameter(getFolderType(), PARAM_CONNECTION, null);
    }

    @Override
    public boolean startTransaction(final StorageParameters parameters, final boolean modify) throws OXException {
        return startTransaction(parameters, modify ? Mode.WRITE : Mode.READ);
    }

    @Override
    public boolean startTransaction(final StorageParameters parameters, final Mode mode) throws OXException {
        final FolderType folderType = getFolderType();
        try {
            final DatabaseService databaseService = services.getService(DatabaseService.class);
            final Context context = parameters.getContext();
            ConnectionMode connectionMode = parameters.getParameter(folderType, PARAM_CONNECTION);
            if (null != connectionMode) {
                if (connectionMode.supports(mode)) {
                    // Connection already present in proper access mode
                    return false;
                }
                /*-
                 * Connection in wrong access mode:
                 *
                 * commit, restore auto-commit & push to pool
                 */
                if (!connectionMode.controlledExternally) {
                    parameters.putParameter(folderType, PARAM_CONNECTION, null);
                    if (connectionMode.isWritable()) {
                        try {
                            connectionMode.connection.commit();
                        } catch (final Exception e) {
                            // Ignore
                            DBUtils.rollback(connectionMode.connection);
                        }
                        DBUtils.autocommit(connectionMode.connection);
                    }
                    connectionMode.close(databaseService, context.getContextId());
                }
            }

            Connection connection = null;
            FolderServiceDecorator decorator = parameters.getDecorator();
            if (decorator != null) {
                Object obj = decorator.getProperty(Connection.class.getName());
                if (obj != null && obj instanceof Connection) {
                    connection = (Connection) obj;
                }
            }

            if (WRITEES.contains(mode)) {
                if (connection == null || connection.isReadOnly()) {
                    connectionMode = new ConnectionMode(databaseService.getWritable(context), mode);
                    connectionMode.connection.setAutoCommit(false);
                } else {
                    connectionMode = new ConnectionMode(connection, mode, true);
                }
            } else {
                if (connection == null) {
                    connectionMode = new ConnectionMode(databaseService.getReadOnly(context), mode);
                } else {
                    connectionMode = new ConnectionMode(connection, mode, true);
                }
            }
            // Put to parameters
            if (parameters.putParameterIfAbsent(folderType, PARAM_CONNECTION, connectionMode)) {
                // Success
            } else {
                // Fail
                if (!connectionMode.controlledExternally) {
                    if (connectionMode.isWritable()) {
                        connectionMode.connection.setAutoCommit(true);
                    }
                    connectionMode.close(databaseService, context.getContextId());
                }
            }

            if (TransactionManager.isManagedTransaction(parameters)) {
                TransactionManager.getTransactionManager(parameters).transactionStarted(this);
                return false;
            }

            return true;
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(Mode.WRITE, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }

            final String id = folder.getID();
            final Context context = storageParameters.getContext();

            if (DatabaseFolderStorageUtility.hasSharedPrefix(id)) {
                final int owner = Integer.parseInt(id.substring(FolderObject.SHARED_PREFIX.length()));
                throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(Integer.valueOf(session.getUserId()), UserStorage.getInstance().getUser(owner, context).getDisplayName(), Integer.valueOf(context.getContextId()));
            }

            final int folderId = Integer.parseInt(id);

            /*
             * Check for concurrent modification
             */
            {
                final Date clientLastModified = storageParameters.getTimeStamp();
                if (null != clientLastModified && getFolderAccess(context, con).getFolderLastModified(folderId).after(clientLastModified)) {
                    throw FolderExceptionErrorMessage.CONCURRENT_MODIFICATION.create();
                }
            }

            final Date millis = new Date();

            final FolderObject updateMe = new FolderObject();
            updateMe.setObjectID(folderId);
            updateMe.setDefaultFolder(false);
            {
                final String name = folder.getName();
                if (null != name) {
                    updateMe.setFolderName(name);
                }
            }
            updateMe.setLastModified(millis);
            folder.setLastModified(millis);
            updateMe.setModifiedBy(session.getUserId());
            {
                final ContentType ct = folder.getContentType();
                if (null != ct) {
                    updateMe.setModule(getModuleByContentType(ct));
                }
            }
            {
                final String parentId = folder.getParentID();
                if (null == parentId) {
                    updateMe.setParentFolderID(getFolderObject(folderId, context, con, storageParameters).getParentFolderID());
                } else {
                    if (DatabaseFolderStorageUtility.hasSharedPrefix(parentId)) {
                        updateMe.setParentFolderID(getFolderObject(folderId, context, con, storageParameters).getParentFolderID());
                    } else {
                        updateMe.setParentFolderID(Integer.parseInt(parentId));
                    }
                }
            }
            {
                final Type t = folder.getType();
                if (null != t) {
                    updateMe.setType(getTypeByFolderType(t));
                }
            }
            // Meta
            {
                final Map<String, Object> meta = folder.getMeta();
                if (null != meta) {
                    updateMe.setMeta(meta);
                }
            }
            // Permissions
            final Permission[] perms = folder.getPermissions();
            if (null != perms) {
                final OCLPermission[] oclPermissions = new OCLPermission[perms.length];
                for (int i = 0; i < perms.length; i++) {
                    final Permission p = perms[i];
                    final OCLPermission oclPerm = new OCLPermission();
                    oclPerm.setEntity(p.getEntity());
                    oclPerm.setGroupPermission(p.isGroup());
                    oclPerm.setFolderAdmin(p.isAdmin());
                    oclPerm.setAllPermission(p.getFolderPermission(), p.getReadPermission(), p.getWritePermission(), p.getDeletePermission());
                    oclPerm.setSystem(p.getSystem());
                    oclPermissions[i] = oclPerm;
                }
                updateMe.setPermissionsAsArray(oclPermissions);
            }
            // Do update
            final OXFolderManager folderManager = OXFolderManager.getInstance(session, con, con);
            folderManager.updateFolder(updateMe, true, StorageParametersUtility.isHandDownPermissions(storageParameters), millis.getTime());
            // Handle warnings
            final List<OXException> warnings = folderManager.getWarnings();
            if (null != warnings) {
                for (final OXException warning : warnings) {
                    storageParameters.addWarning(warning);
                }
            }
        } finally {
            provider.close();
        }
    }

    @Override
    public StoragePriority getStoragePriority() {
        return StoragePriority.NORMAL;
    }

    @Override
    public boolean containsFolder(final String treeId, final String folderIdentifier, final StorageParameters storageParameters) throws OXException {
        return containsFolder(treeId, folderIdentifier, StorageType.WORKING, storageParameters);
    }

    @Override
    public boolean containsFolder(final String treeId, final String folderIdentifier, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(Mode.READ, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final User user = storageParameters.getUser();
            final Context ctx = storageParameters.getContext();
            final UserPermissionBits userPermissionBits = getUserPermissionBits(con, storageParameters);
            final boolean retval;

            if (StorageType.WORKING.equals(storageType)) {
                if (DatabaseFolderStorageUtility.hasSharedPrefix(folderIdentifier)) {

                    retval = SharedPrefixFolder.existsSharedPrefixFolder(folderIdentifier, user, userPermissionBits, ctx, con);
                } else {
                    /*
                     * A numeric folder identifier
                     */
                    final int folderId = getUnsignedInteger(folderIdentifier);

                    if (folderId < 0) {
                        retval = false;
                    } else {
                        if (FolderObject.SYSTEM_ROOT_FOLDER_ID == folderId) {
                            retval = true;
                        } else if (Arrays.binarySearch(VIRTUAL_IDS, folderId) >= 0) {
                            /*
                             * A virtual database folder
                             */
                            retval = VirtualListFolder.existsVirtualListFolder(folderId, user, userPermissionBits, ctx, con);
                        } else {
                            /*
                             * A non-virtual database folder
                             */

                            if (FolderObject.SYSTEM_SHARED_FOLDER_ID == folderId) {
                                /*
                                 * The system shared folder
                                 */
                                retval = true;
                            } else if (FolderObject.SYSTEM_PUBLIC_FOLDER_ID == folderId) {
                                /*
                                 * The system public folder
                                 */
                                retval = true;
                            } else if (FolderObject.SYSTEM_INFOSTORE_FOLDER_ID == folderId) {
                                /*
                                 * The system infostore folder
                                 */
                                retval = true;
                            } else if (FolderObject.SYSTEM_PRIVATE_FOLDER_ID == folderId) {
                                /*
                                 * The system private folder
                                 */
                                retval = true;
                            } else {
                                /*
                                 * Check for shared folder, that is folder is of type private and requesting user is different from folder's
                                 * owner
                                 */
                                retval = OXFolderSQL.exists(folderId, con, ctx);
                            }
                        }
                    }
                }
            } else {
                final int folderId = getUnsignedInteger(folderIdentifier);

                if (folderId < 0) {
                    retval = false;
                } else {
                    retval = OXFolderSQL.exists(folderId, con, ctx, "del_oxfolder_tree");
                }
            }
            return retval;
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            provider.close();
        }
    }// End of containsFolder()

    @Override
    public String[] getModifiedFolderIDs(final String treeId, final Date timeStamp, final ContentType[] includeContentTypes, final StorageParameters storageParameters) throws OXException {
        Date since = null != timeStamp ? timeStamp : new Date(0L);
        ConnectionProvider provider = getConnection(Mode.READ, storageParameters);
        SearchIterator<FolderObject> searchIterator = null;
        try {
            if (null == includeContentTypes) {
                searchIterator = OXFolderIteratorSQL.getAllModifiedFoldersSince(since, storageParameters.getContext(), provider.getConnection());
            } else {
                searchIterator = OXFolderIteratorSQL.getModifiedFoldersSince(since, getModulesByContentType(includeContentTypes), storageParameters.getContext(), provider.getConnection());
            }
            return filterByContentType(searchIterator, includeContentTypes);
        } finally {
            SearchIterators.close(searchIterator);
            provider.close();
        }
    }// End of getModifiedFolderIDs()

    @Override
    public String[] getDeletedFolderIDs(final String treeId, final Date timeStamp, final StorageParameters storageParameters) throws OXException {
        return getDeletedFolderIDs(treeId, timeStamp, null, storageParameters);
    }

    public String[] getDeletedFolderIDs(final String treeId, final Date timeStamp, ContentType[] includeContentTypes, final StorageParameters storageParameters) throws OXException {
        Date since = null != timeStamp ? timeStamp : new Date(0L);
        ConnectionProvider provider = getConnection(Mode.READ, storageParameters);
        SearchIterator<FolderObject> searchIterator = null;
        try {
            Connection connection = provider.getConnection();
            User user = storageParameters.getUser();
            UserPermissionBits userPermissionBits = getUserPermissionBits(connection, storageParameters);
            searchIterator = OXFolderIteratorSQL.getDeletedFoldersSince(
                since, user.getId(), user.getGroups(), userPermissionBits.getAccessibleModules(), storageParameters.getContext(), connection);
            return filterByContentType(searchIterator, includeContentTypes);
        } finally {
            SearchIterators.close(searchIterator);
            provider.close();
        }
    }// End of getDeletedFolderIDs()

    /*-
     * ############################# HELPER METHODS #############################
     */

    private static FolderObject getFolderObject(final int folderId, final Context ctx, final Connection con, StorageParameters storageParameters) throws OXException {
        Boolean ignoreCache = storageParameters.getIgnoreCache();
        if (!FolderCacheManager.isEnabled()) {
            return FolderObject.loadFolderObjectFromDB(folderId, ctx, con, true, true);
        }

        FolderCacheManager cacheManager = FolderCacheManager.getInstance();
        if (Boolean.TRUE.equals(ignoreCache)) {
            FolderObject fo = FolderObject.loadFolderObjectFromDB(folderId, ctx, con, true, true);
            Boolean do_not_cache = storageParameters.getParameter(FolderType.GLOBAL, "DO_NOT_CACHE");
            if (null == do_not_cache || !do_not_cache) {
                cacheManager.putFolderObject(fo, ctx, true, null);
            }
            return fo;
        }

        FolderObject fo = cacheManager.getFolderObject(folderId, ctx);
        if (null == fo) {
            fo = FolderObject.loadFolderObjectFromDB(folderId, ctx, con, true, true);
            cacheManager.putFolderObject(fo, ctx, false, null);
        }
        return fo;
    }

    private static List<FolderObject> getFolderObjects(final int[] folderIds, final Context ctx, final Connection con, StorageParameters storageParameters) throws OXException {
        Boolean ignoreCache = storageParameters.getIgnoreCache();
        if (!FolderCacheManager.isEnabled()) {
            /*
             * OX folder cache not enabled
             */
            return OXFolderBatchLoader.loadFolderObjectsFromDB(folderIds, ctx, con, true, true);
        }

        FolderCacheManager cacheManager = FolderCacheManager.getInstance();
        if (Boolean.TRUE.equals(ignoreCache)) {
            List<FolderObject> folders = OXFolderBatchLoader.loadFolderObjectsFromDB(folderIds, ctx, con, true, true);
            for (FolderObject fo : folders) {
                cacheManager.putFolderObject(fo, ctx, true, null);
            }
            return folders;
        }

        // Load them either from cache or from database
        final int length = folderIds.length;
        final FolderObject[] ret = new FolderObject[length];
        final TIntIntMap toLoad = new TIntIntHashMap(length);
        for (int index = 0; index < length; index++) {
            final int folderId = folderIds[index];
            final FolderObject fo = cacheManager.getFolderObject(folderId, ctx);
            if (null == fo) {// Cache miss
                toLoad.put(folderId, index);
            } else {// Cache hit
                ret[index] = fo;
            }
        }
        if (!toLoad.isEmpty()) {
            final List<FolderObject> list = OXFolderBatchLoader.loadFolderObjectsFromDB(toLoad.keys(), ctx, con, true, true);
            for (final FolderObject folderObject : list) {
                if (null != folderObject) {
                    final int index = toLoad.get(folderObject.getObjectID());
                    ret[index] = folderObject;
                    cacheManager.putFolderObject(folderObject, ctx, false, null);
                }
            }
        }
        return Arrays.asList(ret);
    }

    private static OXFolderAccess getFolderAccess(final Context ctx, final Connection con) {
        return new OXFolderAccess(con, ctx);
    }

    private ConnectionProvider getConnection(final Mode mode, final StorageParameters storageParameters) throws OXException {
        ConnectionMode connection = optParameter(ConnectionMode.class, PARAM_CONNECTION, storageParameters);
        if (null != connection) {
            return new NonClosingConnectionProvider(connection/* , databaseService, context.getContextId() */);
        }
        final Context context = storageParameters.getContext();
        final DatabaseService databaseService = services.getService(DatabaseService.class);
        connection = WRITEES.contains(mode) ? new ConnectionMode(databaseService.getWritable(context), mode) : new ConnectionMode(databaseService.getReadOnly(context), mode);
        return new ClosingConnectionProvider(connection, databaseService, context.getContextId());
    }

    private static <T> T optParameter(final Class<T> clazz, final String name, final StorageParameters parameters) throws OXException {
        final Object obj = parameters.getParameter(DatabaseFolderType.getInstance(), name);
        if (null == obj) {
            return null;
        }
        try {
            return clazz.cast(obj);
        } catch (final ClassCastException e) {
            throw OXFolderExceptionCode.MISSING_PARAMETER.create(e, name);
        }
    }

    private static String[] filterByContentType(SearchIterator<FolderObject> searchIterator, ContentType[] allowedContentTypes) throws OXException {
        List<String> folderIDs = new ArrayList<String>();
        while (searchIterator.hasNext()) {
            FolderObject folder = searchIterator.next();
            if (matches(folder, allowedContentTypes)) {
                folderIDs.add(String.valueOf(folder.getObjectID()));
            }
        }
        return folderIDs.toArray(new String[folderIDs.size()]);
    }

    private static boolean matches(FolderObject folder, ContentType[] allowedContentTypes) {
        if (null == allowedContentTypes) {
            return true;
        }
        for (ContentType allowedContentType : allowedContentTypes) {
            if (folder.getModule() == getModuleByContentType(allowedContentType)) {
                return true;
            }
        }
        return false;
    }

    private static int getModuleByContentType(final ContentType contentType) {
        final String cts = contentType.toString();
        if (TaskContentType.getInstance().toString().equals(cts)) {
            return FolderObject.TASK;
        }
        if (CalendarContentType.getInstance().toString().equals(cts)) {
            return FolderObject.CALENDAR;
        }
        if (ContactContentType.getInstance().toString().equals(cts)) {
            return FolderObject.CONTACT;
        }
        if (InfostoreContentType.getInstance().toString().equals(cts)) {
            return FolderObject.INFOSTORE;
        }
        return FolderObject.UNBOUND;
    }

    private static int[] getModulesByContentType(ContentType[] contentTypes) {
        if (null == contentTypes) {
            return null;
        }
        int[] modules = new int[contentTypes.length];
        for (int i = 0; i < contentTypes.length; i++) {
            modules[i] = getModuleByContentType(contentTypes[i]);
        }
        return modules;
    }

    private static int getTypeByFolderType(final Type type) {
        if (PrivateType.getInstance().equals(type)) {
            return FolderObject.PRIVATE;
        }
        if (PublicType.getInstance().equals(type)) {
            return FolderObject.PUBLIC;
        }
        if (TrashType.getInstance().equals(type)) {
            return FolderObject.TRASH;
        }
        if (DocumentsType.getInstance().equals(type)) {
            return FolderObject.DOCUMENTS;
        }
        if (TemplatesType.getInstance().equals(type)) {
            return FolderObject.TEMPLATES;
        }
        if (MusicType.getInstance().equals(type)) {
            return FolderObject.MUSIC;
        }
        if (PicturesType.getInstance().equals(type)) {
            return FolderObject.PICTURES;
        }
        if (VideosType.getInstance().equals(type)) {
            return FolderObject.VIDEOS;
        }
        return FolderObject.SYSTEM_TYPE;
    }

    private static int getTypeByFolderTypeWithShared(final Type type) {
        if (SharedType.getInstance().equals(type)) {
            return FolderObject.SHARED;
        }
        return getTypeByFolderType(type);
    }

    /**
     * Reads the supplied search iterator and filters those folders that are considered as "shared" by the user, i.e. non-public folders
     * of the user that have been shared to at least one other entity.
     *
     * @param searchIterator The search iterator to process
     * @param connection An opened database connection to use
     * @param storageParameters The storage parameters as passed from the client
     * @return The shared folders of the user
     */
    private static List<FolderObject> getUserSharedFolders(SearchIterator<FolderObject> searchIterator, Connection connection, StorageParameters storageParameters) throws OXException {
        List<FolderObject> folders = new ArrayList<FolderObject>();
        Set<Integer> knownPrivateFolders = new HashSet<Integer>();
        while (searchIterator.hasNext()) {
            /*
             * only include shared, non-public folders created by the user
             */
            FolderObject folder = searchIterator.next();
            if (folder.getCreatedBy() == storageParameters.getUserId()) {
                OCLPermission[] permissions = folder.getNonSystemPermissionsAsArray();
                if (null != permissions && 1 < permissions.length && considerPrivate(folder, knownPrivateFolders, connection, storageParameters)) {
                    folders.add(folder);
                }
            }
        }
        return folders;
    }

    /**
     * Gets a value indicating whether the supplied folder can be considered as a "private" one, i.e. it is marked directly as such, or is
     * an infostore folder somewhere in the "private" subtree below the user's personal default infostore folder.
     *
     * @param folder The folder to check
     * @param knownPrivateFolders A set of already known private folders (from previous checks)
     * @param connection An opened database connection to use
     * @param storageParameters The storage parameters as passed from the client
     * @return <code>true</code> if the folder can be considered as "public", <code>false</code>, otherwise
     */
    private static boolean considerPrivate(FolderObject folder, Set<Integer> knownPrivateFolders, Connection connection, StorageParameters storageParameters) throws OXException {
        if (FolderObject.INFOSTORE == folder.getModule()) {
            /*
             * infostore folders are always of "public" type, so check if they're somewhere below the personal subtree
             */
            List<Integer> seenFolders = new ArrayList<Integer>();
            FolderObject currentFolder = folder;
            do {
                Integer id = I(currentFolder.getObjectID());
                seenFolders.add(id);
                if (knownPrivateFolders.contains(id) || knownPrivateFolders.contains(I(currentFolder.getParentFolderID())) || currentFolder.isDefaultFolder() && FolderObject.PUBLIC == currentFolder.getType()) {
                    /*
                     * folder or its parent is a previously recognized "private" folder, or the user's personal infostore folder is reached
                     */
                    knownPrivateFolders.addAll(seenFolders);
                    return true;
                } else {
                    /*
                     * check parent folder if there are more parent folders available
                     */
                    if (FolderObject.MIN_FOLDER_ID < currentFolder.getParentFolderID()) {
                        currentFolder = getFolderObject(currentFolder.getParentFolderID(), storageParameters.getContext(), connection, storageParameters);
                    } else {
                        currentFolder = null;
                        return false;
                    }
                }
            } while (null != currentFolder);
            return false;
        } else {
            /*
             * always consider non-infostore "private" folders as private
             */
            return FolderObject.PRIVATE == folder.getType(storageParameters.getUserId());
        }
    }

    private static final class FolderObjectComparator implements Comparator<FolderObject> {

        private final Collator collator;
        private final Context context;

        FolderObjectComparator(Locale locale, Context context) {
            super();
            collator = Collators.getSecondaryInstance(locale);
            this.context = context;
        }

        @Override
        public int compare(final FolderObject o1, final FolderObject o2) {
            if (o1.isDefaultFolder()) {
                if (o2.isDefaultFolder()) {
                    if (o1.getFolderName().equals(o2.getFolderName())) {
                        /*
                         * Sort by owner's display name
                         */
                        final int owner1 = o1.getCreatedBy();
                        final int owner2 = o2.getCreatedBy();
                        if (owner1 > 0 && owner2 > 0 && owner1 != owner2) {
                            String d1;
                            try {
                                d1 = UserStorage.getInstance().getUser(owner1, context).getDisplayName();
                            } catch (OXException e) {
                                d1 = null;
                            }
                            String d2;
                            try {
                                d2 = UserStorage.getInstance().getUser(owner2, context).getDisplayName();
                            } catch (OXException e) {
                                d2 = null;
                            }
                            ;
                            return collator.compare(d1, d2);
                        }
                    }
                    return compareById(o1.getObjectID(), o2.getObjectID());
                }
                return -1;
            } else if (o2.isDefaultFolder()) {
                return 1;
            }
            // Compare by name
            return collator.compare(o1.getFolderName(), o2.getFolderName());
        }

        private static int compareById(final int id1, final int id2) {
            return (id1 < id2 ? -1 : (id1 == id2 ? 0 : 1));
        }

    }// End of FolderObjectComparator

    private static final class FolderNameComparator implements Comparator<FolderObject> {

        private final Collator collator;
        private final Context context;

        FolderNameComparator(Locale locale, Context context) {
            super();
            collator = Collators.getSecondaryInstance(locale);
            this.context = context;
        }

        @Override
        public int compare(final FolderObject o1, final FolderObject o2) {
            /*
             * Compare by name
             */
            final String folderName1 = o1.getFolderName();
            final String folderName2 = o2.getFolderName();
            if (folderName1.equals(folderName2)) {
                /*
                 * Sort by owner's display name
                 */
                final int owner1 = o1.getCreatedBy();
                final int owner2 = o2.getCreatedBy();
                if (owner1 > 0 && owner2 > 0 && owner1 != owner2) {
                    String d1;
                    try {
                        d1 = UserStorage.getInstance().getUser(owner1, context).getDisplayName();
                    } catch (OXException e) {
                        d1 = null;
                    }
                    String d2;
                    try {
                        d2 = UserStorage.getInstance().getUser(owner2, context).getDisplayName();
                    } catch (OXException e) {
                        d2 = null;
                    }
                    ;
                    return collator.compare(d1, d2);
                }
            }
            return collator.compare(folderName1, folderName2);
        }

    }// End of FolderNameComparator

    private static final class FileStorageAccountComparator implements Comparator<FileStorageAccount> {

        private final Collator collator;

        public FileStorageAccountComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale);
        }

        @Override
        public int compare(final FileStorageAccount o1, final FileStorageAccount o2) {
            return collator.compare(o1.getDisplayName(), o2.getDisplayName());
        }

    }// End of FileStorageAccountComparator

    public static final class ConnectionMode {

        /**
         * The connection.
         */
        public final Connection connection;

        /**
         * Whether connection is read-write or read-only.
         */
        public Mode readWrite;

        /**
         * Whether this connection is in a transactional state controlled by an outer scope.
         */
        public boolean controlledExternally;

        /**
         * Initializes a new {@link ConnectionMode}.
         *
         * @param connection
         * @param readWrite
         */
        public ConnectionMode(final Connection connection, final Mode readWrite) {
            this(connection, readWrite, false);
        }

        /**
         * Initializes a new {@link ConnectionMode}.
         *
         * @param connection
         * @param readWrite
         * @param controlledExternally
         */
        public ConnectionMode(final Connection connection, final Mode readWrite, final boolean controlledExternally) {
            super();
            this.connection = connection;
            this.readWrite = readWrite;
            this.controlledExternally = controlledExternally;
        }

        public boolean isWritable() {
            return WRITEES.contains(readWrite);
        }

        public boolean supports(Mode mode) {
            if (isWritable()) {
                if (WRITEES.contains(mode)) {
                    readWrite = mode;
                }
                return true;
            }
            return readWrite == mode;
        }

        /**
         * Closes the connection
         *
         * @param databaseService The database service
         * @param contextId The context identifier
         */
        public void close(final DatabaseService databaseService, final int contextId) {
            if (!controlledExternally) {
                if (Mode.WRITE == readWrite) {
                    databaseService.backWritable(contextId, connection);
                } else if (Mode.WRITE_AFTER_READ == readWrite) {
                    databaseService.backWritableAfterReading(contextId, connection);
                } else {
                    databaseService.backReadOnly(contextId, connection);
                }
            }
        }
    }

    @Override
    public void cleanLocksFor(Folder folder, int[] userIds, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(Mode.WRITE, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final OXFolderManager folderManager = OXFolderManager.getInstance(storageParameters.getSession(), con, con);

            FolderObject fo = new FolderObject(Integer.valueOf(folder.getID()));
            folderManager.cleanLocksForFolder(fo, userIds);
        } finally {
            provider.close();
        }

    }

}
