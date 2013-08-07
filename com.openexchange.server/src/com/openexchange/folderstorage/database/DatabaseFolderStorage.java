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

package com.openexchange.folderstorage.database;

import static com.openexchange.folderstorage.database.DatabaseFolderStorageUtility.getUnsignedInteger;
import static com.openexchange.groupware.container.FolderObject.SYSTEM_MODULE;
import static com.openexchange.server.impl.OCLPermission.ADMIN_PERMISSION;
import static com.openexchange.server.impl.OCLPermission.DELETE_ALL_OBJECTS;
import static com.openexchange.server.impl.OCLPermission.READ_ALL_OBJECTS;
import static com.openexchange.server.impl.OCLPermission.READ_FOLDER;
import static com.openexchange.server.impl.OCLPermission.WRITE_ALL_OBJECTS;
import gnu.trove.ConcurrentTIntObjectHashMap;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
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
import com.openexchange.folderstorage.filestorage.FileStorageFolderIdentifier;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.folderstorage.type.SystemType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.infostore.InfostoreFacades;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderBatchLoader;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.oxfolder.OXFolderLoader;
import com.openexchange.tools.oxfolder.OXFolderLoader.IdAndName;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderSQL;
import com.openexchange.tools.oxfolder.OXFolderUtility;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link DatabaseFolderStorage} - The database folder storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DatabaseFolderStorage implements FolderStorage {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(DatabaseFolderStorage.class));

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

        protected NonClosingConnectionProvider(final ConnectionMode connection/*, final DatabaseService databaseService, final int contextId*/) {
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
            if (connection.readWrite) {
                databaseService.backWritable(contextId, connection.connection);
            } else {
                databaseService.backReadOnly(contextId, connection.connection);
            }
        }
    }

    /**
     * Initializes a new {@link DatabaseFolderStorage}.
     */
    public DatabaseFolderStorage() {
        super();
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
        final DatabaseService databaseService = DatabaseServiceRegistry.getService(DatabaseService.class, true);
        Connection con = null;
        boolean close = true;
        boolean readOnly = true;
        try {
            {
                final ConnectionMode conMode = optParameter(ConnectionMode.class, DatabaseParameterConstants.PARAM_CONNECTION, storageParameters);
                if (null != conMode) {
                    con = conMode.connection;
                    readOnly = !conMode.readWrite;
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
            if (readOnly) {
                if (close) {
                    databaseService.backReadOnly(contextId, con);
                }
                con = databaseService.getWritable(contextId);
                readOnly = false;
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
                if (readOnly) {
                    databaseService.backReadOnly(contextId, con);
                } else {
                    databaseService.backWritable(contextId, con);
                }
            }
            STAMPS.put(contextId, Long.valueOf(now));
        }
    }

    @Override
    public ContentType[] getSupportedContentTypes() {
        return new ContentType[] {
            TaskContentType.getInstance(), CalendarContentType.getInstance(), ContactContentType.getInstance(),
            InfostoreContentType.getInstance(), UnboundContentType.getInstance(), SystemContentType.getInstance() };
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
            /*
             * Already committed
             */
            if (LOG.isWarnEnabled()) {
                LOG.warn("Storage already committed:\n" + params.getCommittedTrace(), e);
            }
            return;
        }
        if (null == con) {
            return;
        }
        if (con.readWrite) {
            try {
                con.connection.commit();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.autocommit(con.connection);
                final DatabaseService databaseService = DatabaseServiceRegistry.getServiceRegistry().getService(DatabaseService.class);
                if (null != databaseService) {
                    databaseService.backWritable(params.getContext(), con.connection);
                }
                final FolderType folderType = getFolderType();
                params.putParameter(folderType, PARAM_CONNECTION, null);
                params.markCommitted();
            }
        } else {
            final DatabaseService databaseService = DatabaseServiceRegistry.getServiceRegistry().getService(DatabaseService.class);
            if (null != databaseService) {
                databaseService.backReadOnly(params.getContext(), con.connection);
            }
            final FolderType folderType = getFolderType();
            params.putParameter(folderType, PARAM_CONNECTION, null);
            params.markCommitted();
        }
    }

    @Override
    public void restore(final String treeId, final String folderIdentifier, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(true, storageParameters);
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

    private static final TIntSet SPECIALS = new TIntHashSet(new int[] { FolderObject.SYSTEM_PRIVATE_FOLDER_ID, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID });

    @Override
    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(true, storageParameters);
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
                    createMe.setType(getFolderType(createMe.getParentFolderID(), storageParameters.getContext(), con));
                } else {
                    createMe.setType(getTypeByFolderType(t));
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
                    oclPerm.setAllPermission(
                        p.getFolderPermission(),
                        p.getReadPermission(),
                        p.getWritePermission(),
                        p.getDeletePermission());
                    oclPerm.setSystem(p.getSystem());
                    oclPermissions[i] = oclPerm;
                }
                createMe.setPermissionsAsArray(oclPermissions);
            } else {
                final int parentFolderID = createMe.getParentFolderID();
                /*
                 * Prepare
                 */
                final FolderObject parent = getFolderObject(parentFolderID, storageParameters.getContext(), con);
                final int userId = storageParameters.getUserId();
                final boolean isShared = parent.isShared(userId);
                final boolean isSystem = (SYSTEM_MODULE == parent.getModule());
                final List<OCLPermission> parentPermissions = parent.getPermissions();
                /*
                 * Create permission list
                 */
                final List<OCLPermission> permissions = new ArrayList<OCLPermission>((isSystem ? 0 : parentPermissions.size()) + 1);
                if (isShared) {
                    permissions.add(newMaxPermissionFor(parent.getCreatedBy()));
                    permissions.add(newStandardPermissionFor(userId));
                } else {
                    permissions.add(newMaxPermissionFor(userId));
                }
                if (!isSystem) {
                    final TIntSet ignore = new TIntHashSet(2); ignore.add(userId);
                    if (isShared) {
                        ignore.add(parent.getCreatedBy());
                    }
                    for (final OCLPermission permission : parentPermissions) {
                        if (permission.getSystem() <= 0 && (permission.isGroupPermission() || !ignore.contains(permission.getEntity()))) {
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

    private static final int[] PUBLIC_FOLDER_IDS =
        {
            FolderObject.SYSTEM_PUBLIC_FOLDER_ID, FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID,
            FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID };

    private static int getFolderType(final int parentId, final Context ctx, final Connection con) throws OXException, OXException {
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
        } else if (pid == FolderObject.SYSTEM_OX_PROJECT_FOLDER_ID) {
            type = FolderObject.PROJECT;
        } else {
            type = getFolderAccess(ctx, con).getFolderType(pid);
        }
        return type;
    }

    @Override
    public void clearFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(true, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final FolderObject fo = getFolderObject(Integer.parseInt(folderId), storageParameters.getContext(), con);
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            final OXFolderManager folderManager = OXFolderManager.getInstance(session, con, con);
            folderManager.clearFolder(fo, true, System.currentTimeMillis());
        } finally {
            provider.close();
        }
    }

    @Override
    public void deleteFolder(final String treeId, final String folderIdentifier, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(true, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final FolderObject fo = new FolderObject();
            final int folderId = Integer.parseInt(folderIdentifier);
            fo.setObjectID(folderId);
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            final OXFolderManager folderManager = OXFolderManager.getInstance(session, con, con);
            /*-
             * TODO: Perform last-modified check?
            {
                final Date clientLastModified = storageParameters.getTimeStamp();
                if (null != clientLastModified && getFolderAccess(storageParameters, getFolderType()).getFolderLastModified(folderId).after(
                    clientLastModified)) {
                    throw FolderExceptionErrorMessage.CONCURRENT_MODIFICATION.create();
                }
            }
             *
             */
            folderManager.deleteFolder(fo, true, System.currentTimeMillis());
        } finally {
            provider.close();
        }
    }

    @Override
    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final Type type, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(false, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final Session session = storageParameters.getSession();
            if (null == session) {
                throw FolderExceptionErrorMessage.MISSING_SESSION.create(new Object[0]);
            }
            final Context context = storageParameters.getContext();
            final int folderId;
            if (TaskContentType.getInstance().equals(contentType)) {
                folderId = OXFolderSQL.getUserDefaultFolder(session.getUserId(), FolderObject.TASK, con, context);
            } else if (CalendarContentType.getInstance().equals(contentType)) {
                folderId = OXFolderSQL.getUserDefaultFolder(session.getUserId(), FolderObject.CALENDAR, con, context);
            } else if (ContactContentType.getInstance().equals(contentType)) {
                folderId = OXFolderSQL.getUserDefaultFolder(session.getUserId(), FolderObject.CONTACT, con, context);
            } else if (InfostoreContentType.getInstance().equals(contentType)) {
                folderId = OXFolderSQL.getUserDefaultFolder(session.getUserId(), FolderObject.INFOSTORE, con, context);
            } else {
                return null;
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
        } else if (pid == FolderObject.SYSTEM_OX_PROJECT_FOLDER_ID) {
            return SystemType.getInstance();
        } else {
            final ConnectionProvider provider = getConnection(false, storageParameters);
            try {
                final FolderObject p = getFolderAccess(storageParameters.getContext(), provider.getConnection()).getFolderObject(pid);
                final int parentType = p.getType();
                if (FolderObject.PRIVATE == parentType) {
                    return p.getCreatedBy() == user.getId() ? PrivateType.getInstance() : SharedType.getInstance();
                } else if (FolderObject.PUBLIC == parentType) {
                    return PublicType.getInstance();
                }
            } finally {
                provider.close();
            }
        }
        return SystemType.getInstance();
    }

    @Override
    public boolean containsForeignObjects(final User user, final String treeId, final String folderIdentifier, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(false, storageParameters);
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
                return folderAccess.containsForeignObjects(getFolderObject(folderId, ctx, con), storageParameters.getSession(), ctx);
            }
        } finally {
            provider.close();
        }
    }

    @Override
    public boolean isEmpty(final String treeId, final String folderIdentifier, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(false, storageParameters);
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
                return folderAccess.isEmpty(getFolderObject(folderId, ctx, con), storageParameters.getSession(), ctx);
            }
        } finally {
            provider.close();
        }
    }

    @Override
    public void updateLastModified(final long lastModified, final String treeId, final String folderIdentifier, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(true, storageParameters);
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

    private static final int[] VIRTUAL_IDS = {
        FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID, FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID,
        FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID, FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID };

    @Override
    public Folder getFolder(final String treeId, final String folderIdentifier, final StorageType storageType, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(false, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final User user = storageParameters.getUser();
            final Context ctx = storageParameters.getContext();
            final UserConfiguration userConfiguration;
            {
                final Session s = storageParameters.getSession();
                if (s instanceof ServerSession) {
                    userConfiguration = ((ServerSession) s).getUserConfiguration();
                } else {
                    userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx);
                }
            }

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
                        final FolderObject fo = getFolderObject(folderId, ctx, con);
                        final boolean altNames = StorageParametersUtility.getBoolParameter("altNames", storageParameters);
                        retval = DatabaseFolderConverter.convert(fo, user, userConfiguration, ctx, storageParameters.getSession(), altNames, con);
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

                final FolderObject fo =
                    FolderObject.loadFolderObjectFromDB(folderId, ctx, con, true, false, "del_oxfolder_tree", "del_oxfolder_permissions");
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
            final UserConfiguration userConfiguration;
            {
                final Session s = storageParameters.getSession();
                if (s instanceof ServerSession) {
                    userConfiguration = ((ServerSession) s).getUserConfiguration();
                } else {
                    userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx);
                }
            }
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
                provider = getConnection(false, storageParameters);
                final Connection con = provider.getConnection();
                if (!map.isEmpty()) {
                    final Session session = storageParameters.getSession();
                    for (final FolderObject folderObject : getFolderObjects(map.keys(), ctx, con)) {
                        if (null != folderObject) {
                            final int index = map.get(folderObject.getObjectID());
                            ret[index] = DatabaseFolderConverter.convert(folderObject, user, userConfiguration, ctx, session, altNames, con);
                        }
                    }
                }
                provider.close();
                provider = null;
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
                        storageParameters.addWarning(FolderExceptionErrorMessage.NOT_FOUND.create(
                            Integer.valueOf(folderIdentifiers.get(i)),
                            treeId));
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
            provider = getConnection(false, storageParameters);
            final Connection con = provider.getConnection();
            final List<FolderObject> folders =
                OXFolderBatchLoader.loadFolderObjectsFromDB(
                    list.toArray(),
                    ctx,
                    con,
                    true,
                    false,
                    "del_oxfolder_tree",
                    "del_oxfolder_permissions");
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
        final ConnectionProvider provider = getConnection(false, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final User user = storageParameters.getUser();
            final int userId = user.getId();
            final Context ctx = storageParameters.getContext();
            final UserConfiguration userConfiguration;
            {
                final Session s = storageParameters.getSession();
                if (s instanceof ServerSession) {
                    userConfiguration = ((ServerSession) s).getUserConfiguration();
                } else {
                    userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(userId, ctx);
                }
            }
            final int iType = getTypeByFolderTypeWithShared(type);
            final int iModule = getModuleByContentType(contentType);
            final List<FolderObject> list =
                ((FolderObjectIterator) OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfType(
                    userId,
                    user.getGroups(),
                    userConfiguration.getAccessibleModules(),
                    iType,
                    new int[] { iModule },
                    ctx,
                    con)).asList();
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
                    final FolderObject gab = getFolderObject(FolderObject.SYSTEM_LDAP_FOLDER_ID, ctx, con);
                    if (gab.isVisible(userId, userConfiguration)) {
                        gab.setFolderName(StringHelper.valueOf(user.getLocale()).getString(FolderStrings.SYSTEM_LDAP_FOLDER_NAME));
                        list.add(gab);
                    }
                } catch (final RuntimeException e) {
                    throw OXFolderExceptionCode.RUNTIME_ERROR.create(e, Integer.valueOf(ctx.getContextId()));
                }
            }
            /*
             * Localize folder names
             */
            {
                StringHelper stringHelper = null;
                for (final FolderObject folderObject : list) {
                    /*
                     * Check if folder is user's default folder and set locale-sensitive name
                     */
                    if (folderObject.isDefaultFolder()) {
                        final int module = folderObject.getModule();
                        if (FolderObject.CALENDAR == module) {
                            {
                                if (null == stringHelper) {
                                    stringHelper = StringHelper.valueOf(user.getLocale());
                                }
                                folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_CALENDAR_FOLDER_NAME));
                            }
                        } else if (FolderObject.CONTACT == module) {
                            {
                                if (null == stringHelper) {
                                    stringHelper = StringHelper.valueOf(user.getLocale());
                                }
                                folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_CONTACT_FOLDER_NAME));
                            }
                        } else if (FolderObject.TASK == module) {
                            {
                                if (null == stringHelper) {
                                    stringHelper = StringHelper.valueOf(user.getLocale());
                                }
                                folderObject.setFolderName(stringHelper.getString(FolderStrings.DEFAULT_TASK_FOLDER_NAME));
                            }
                        }
                    }
                }
            }
            if (FolderObject.PRIVATE == iType) {
                /*
                 * Sort them by default-flag and name: <user's default folder>, <aaa>, <bbb>, ... <zzz>
                 */
                Collections.sort(list, new FolderObjectComparator(user.getLocale(), ctx));
            } else {
                /*
                 * Sort them by name only
                 */
                Collections.sort(list, new FolderNameComparator(user.getLocale(), storageParameters.getContext()));
            }
            /*
             * Extract IDs
             */
            final SortableId[] ret = new SortableId[list.size()];
            for (int i = 0; i < ret.length; i++) {
                final FolderObject folderObject = list.get(i);
                final String id = String.valueOf(folderObject.getObjectID());
                ret[i] = new DatabaseId(id, i, folderObject.getFolderName());
            }
            return ret;
        } finally {
            provider.close();
        }
    }

    @Override
    public SortableId[] getSubfolders(final String treeId, final String parentIdentifier, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(false, storageParameters);
        try {
            final Connection con = provider.getConnection();

            if (DatabaseFolderStorageUtility.hasSharedPrefix(parentIdentifier)) {
                final User user = storageParameters.getUser();
                final Context ctx = storageParameters.getContext();
                final UserConfiguration userConfiguration;
                {
                    final Session s = storageParameters.getSession();
                    if (s instanceof ServerSession) {
                        userConfiguration = ((ServerSession) s).getUserConfiguration();
                    } else {
                        userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx);
                    }
                }
                final List<FolderIdNamePair> subfolderIds =
                    SharedPrefixFolder.getSharedPrefixFolderSubfolders(parentIdentifier, user, userConfiguration, ctx, con);
                final List<SortableId> list = new ArrayList<SortableId>(subfolderIds.size());
                int i = 0;
                for (final FolderIdNamePair props : subfolderIds) {
                    list.add(new DatabaseId(props.fuid, i++, props.name));
                }
                return list.toArray(new SortableId[list.size()]);
            }

            final int parentId = Integer.parseInt(parentIdentifier);

            if (FolderObject.SYSTEM_ROOT_FOLDER_ID == parentId) {
                final List<String[]> subfolderIds = SystemRootFolder.getSystemRootFolderSubfolder(storageParameters.getUser().getLocale());
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
                final User user = storageParameters.getUser();
                final Context ctx = storageParameters.getContext();
                final UserConfiguration userConfiguration;
                {
                    final Session s = storageParameters.getSession();
                    if (s instanceof ServerSession) {
                        userConfiguration = ((ServerSession) s).getUserConfiguration();
                    } else {
                        userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx);
                    }
                }
                final List<String[]> subfolderIds =
                    VirtualListFolder.getVirtualListFolderSubfolders(parentId, user, userConfiguration, ctx, con);
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
                final User user = storageParameters.getUser();
                final Context ctx = storageParameters.getContext();
                final UserConfiguration userConfiguration;
                {
                    final Session s = storageParameters.getSession();
                    if (s instanceof ServerSession) {
                        userConfiguration = ((ServerSession) s).getUserConfiguration();
                    } else {
                        userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx);
                    }
                }
                final List<String[]> subfolderIds = SystemPrivateFolder.getSystemPrivateFolderSubfolders(user, userConfiguration, ctx, con);
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
                final User user = storageParameters.getUser();
                final Context ctx = storageParameters.getContext();
                final UserConfiguration userConfiguration;
                {
                    final Session s = storageParameters.getSession();
                    if (s instanceof ServerSession) {
                        userConfiguration = ((ServerSession) s).getUserConfiguration();
                    } else {
                        userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx);
                    }
                }
                final List<String[]> subfolderIds = SystemSharedFolder.getSystemSharedFolderSubfolder(user, userConfiguration, ctx, con);
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
                final User user = storageParameters.getUser();
                final Context ctx = storageParameters.getContext();
                final UserConfiguration userConfiguration;
                {
                    final Session s = storageParameters.getSession();
                    if (s instanceof ServerSession) {
                        userConfiguration = ((ServerSession) s).getUserConfiguration();
                    } else {
                        userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx);
                    }
                }
                final List<String[]> subfolderIds = SystemPublicFolder.getSystemPublicFolderSubfolders(user, userConfiguration, ctx, con);
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
                final User user = storageParameters.getUser();
                final Context ctx = storageParameters.getContext();
                final UserConfiguration userConfiguration;
                {
                    final Session s = storageParameters.getSession();
                    if (s instanceof ServerSession) {
                        userConfiguration = ((ServerSession) s).getUserConfiguration();
                    } else {
                        userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx);
                    }
                }
                final boolean altNames = StorageParametersUtility.getBoolParameter("altNames", storageParameters);
                final List<String[]> subfolderIds = SystemInfostoreFolder.getSystemInfostoreFolderSubfolders(user, userConfiguration, ctx, altNames, con);
                final int size = subfolderIds.size();
                final List<SortableId> list = new ArrayList<SortableId>(size);
                for (int i = 0; i < size; i++) {
                    final String[] sa = subfolderIds.get(i);
                    list.add(new DatabaseId(sa[0], i, sa[1]));
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
                            final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(
                                fileStorageService.getId(),
                                defaultAccount.getId(),
                                personalFolder.getId());
                            return new SortableId[] { new DatabaseId(fsfi.toString(), 0, personalFolder.getName()) };
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
                                final FileStorageFolderIdentifier fsfi = new FileStorageFolderIdentifier(serviceId, accountId, folder.getId());
                                ret[i] = new DatabaseId(fsfi.toString(), i, folder.getName());
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
                subfolders = getFolderObjects(arr, storageParameters.getContext(), con);
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
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
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
            LOG.error(e.getMessage(), e);
            return;
        }
        if (null == con) {
            return;
        }
        if (con.readWrite) {
            try {
                DBUtils.rollback(con.connection);
            } finally {
                DBUtils.autocommit(con.connection);
                final DatabaseService databaseService = DatabaseServiceRegistry.getServiceRegistry().getService(DatabaseService.class);
                if (null != databaseService) {
                    databaseService.backWritable(params.getContext(), con.connection);
                }
                params.putParameter(getFolderType(), PARAM_CONNECTION, null);
            }
        } else {
            final DatabaseService databaseService = DatabaseServiceRegistry.getServiceRegistry().getService(DatabaseService.class);
            if (null != databaseService) {
                databaseService.backReadOnly(params.getContext(), con.connection);
            }
        }
    }

    @Override
    public boolean startTransaction(final StorageParameters parameters, final boolean modify) throws OXException {
        final FolderType folderType = getFolderType();
        try {
            final DatabaseService databaseService = DatabaseServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
            final Context context = parameters.getContext();
            ConnectionMode con = parameters.getParameter(folderType, PARAM_CONNECTION);
            if (null != con) {
                if (con.readWrite || modify == con.readWrite) {
                    // Connection already present in proper access mode
                    return false;
                }
                /*-
                 * Connection in wrong access mode:
                 *
                 * commit, restore auto-commit & push to pool
                 */
                parameters.putParameter(folderType, PARAM_CONNECTION, null);
                if (con.readWrite) {
                    try {
                        con.connection.commit();
                    } catch (final Exception e) {
                        // Ignore
                        DBUtils.rollback(con.connection);
                    }
                    DBUtils.autocommit(con.connection);
                    databaseService.backWritable(context, con.connection);
                } else {
                    databaseService.backReadOnly(context, con.connection);
                }
            }
            if (modify) {
                con = new ConnectionMode(databaseService.getWritable(context), true);
                con.connection.setAutoCommit(false);
            } else {
                con = new ConnectionMode(databaseService.getReadOnly(context), false);
            }
            // Put to parameters
            if (parameters.putParameterIfAbsent(folderType, PARAM_CONNECTION, con)) {
                // Success
            } else {
                // Fail
                if (modify) {
                    con.connection.setAutoCommit(true);
                    databaseService.backWritable(context, con.connection);
                } else {
                    databaseService.backReadOnly(context, con.connection);
                }
            }
            return true;
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(true, storageParameters);
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
                throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(
                    OXFolderUtility.getUserName(session.getUserId(), context),
                    UserStorage.getStorageUser(owner, context).getDisplayName(),
                    Integer.valueOf(context.getContextId()));
            }

            final int folderId = Integer.parseInt(id);

            /*
             * Check for concurrent modification
             */
            {
                final Date clientLastModified = storageParameters.getTimeStamp();
                if (null != clientLastModified && getFolderAccess(context, con).getFolderLastModified(folderId).after(
                    clientLastModified)) {
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
                    updateMe.setParentFolderID(getFolderObject(folderId, context, con).getParentFolderID());
                } else {
                    if (DatabaseFolderStorageUtility.hasSharedPrefix(parentId)) {
                        updateMe.setParentFolderID(getFolderObject(folderId, context, con).getParentFolderID());
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
                    oclPerm.setAllPermission(
                        p.getFolderPermission(),
                        p.getReadPermission(),
                        p.getWritePermission(),
                        p.getDeletePermission());
                    oclPerm.setSystem(p.getSystem());
                    oclPermissions[i] = oclPerm;
                }
                updateMe.setPermissionsAsArray(oclPermissions);
            }
            final OXFolderManager folderManager = OXFolderManager.getInstance(session, con, con);
            folderManager.updateFolder(updateMe, true, StorageParametersUtility.isHandDownPermissions(storageParameters), millis.getTime());
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
        final ConnectionProvider provider = getConnection(false, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final User user = storageParameters.getUser();
            final Context ctx = storageParameters.getContext();
            final UserConfiguration userConfiguration;
            {
                final Session s = storageParameters.getSession();
                if (s instanceof ServerSession) {
                    userConfiguration = ((ServerSession) s).getUserConfiguration();
                } else {
                    userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx);
                }
            }

            final boolean retval;

            if (StorageType.WORKING.equals(storageType)) {
                if (DatabaseFolderStorageUtility.hasSharedPrefix(folderIdentifier)) {

                    retval = SharedPrefixFolder.existsSharedPrefixFolder(folderIdentifier, user, userConfiguration, ctx, con);
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
                            retval = VirtualListFolder.existsVirtualListFolder(folderId, user, userConfiguration, ctx, con);
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
    } // End of containsFolder()

    @Override
    public String[] getModifiedFolderIDs(final String treeId, final Date timeStamp, final ContentType[] includeContentTypes, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(false, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final Context ctx = storageParameters.getContext();

            final Queue<FolderObject> q =
                ((FolderObjectIterator) OXFolderIteratorSQL.getAllModifiedFoldersSince(
                    timeStamp == null ? new Date(0) : timeStamp,
                    ctx,
                    con)).asQueue();
            final int size = q.size();
            final Iterator<FolderObject> iterator = q.iterator();
            final String[] ret = new String[size];
            for (int i = 0; i < size; i++) {
                ret[i] = String.valueOf(iterator.next().getObjectID());
            }

            return ret;
        } finally {
            provider.close();
        }
    } // End of getModifiedFolderIDs()

    @Override
    public String[] getDeletedFolderIDs(final String treeId, final Date timeStamp, final StorageParameters storageParameters) throws OXException {
        final ConnectionProvider provider = getConnection(false, storageParameters);
        try {
            final Connection con = provider.getConnection();
            final User user = storageParameters.getUser();
            final Context ctx = storageParameters.getContext();
            final UserConfiguration userConfiguration;
            {
                final Session s = storageParameters.getSession();
                if (s instanceof ServerSession) {
                    userConfiguration = ((ServerSession) s).getUserConfiguration();
                } else {
                    userConfiguration = UserConfigurationStorage.getInstance().getUserConfiguration(user.getId(), ctx);
                }
            }

            final Queue<FolderObject> q =
                ((FolderObjectIterator) OXFolderIteratorSQL.getDeletedFoldersSince(
                    timeStamp,
                    user.getId(),
                    user.getGroups(),
                    userConfiguration.getAccessibleModules(),
                    ctx,
                    con)).asQueue();
            final int size = q.size();
            final Iterator<FolderObject> iterator = q.iterator();
            final String[] ret = new String[size];
            for (int i = 0; i < size; i++) {
                ret[i] = String.valueOf(iterator.next().getObjectID());
            }

            return ret;
        } finally {
            provider.close();
        }

    } // End of getDeletedFolderIDs()

    /*-
     * ############################# HELPER METHODS #############################
     */

    private static FolderObject getFolderObject(final int folderId, final Context ctx, final Connection con) throws OXException {
        if (!FolderCacheManager.isEnabled()) {
            return FolderObject.loadFolderObjectFromDB(folderId, ctx, con, true, true);
        }
        final FolderCacheManager cacheManager = FolderCacheManager.getInstance();
        FolderObject fo = cacheManager.getFolderObject(folderId, ctx);
        if (null == fo) {
            fo = FolderObject.loadFolderObjectFromDB(folderId, ctx, con, true, true);
            cacheManager.putFolderObject(fo, ctx, false, null);
        }
        return fo;
    }

    private static List<FolderObject> getFolderObjects(final int[] folderIds, final Context ctx, final Connection con) throws OXException {
        if (!FolderCacheManager.isEnabled()) {
            /*
             * OX folder cache not enabled
             */
            return OXFolderBatchLoader.loadFolderObjectsFromDB(folderIds, ctx, con, true, true);
        }
        /*
         * Load them either from cache or from database
         */
        final int length = folderIds.length;
        final FolderObject[] ret = new FolderObject[length];
        final TIntIntMap toLoad = new TIntIntHashMap(length);
        final FolderCacheManager cacheManager = FolderCacheManager.getInstance();
        for (int index = 0; index < length; index++) {
            final int folderId = folderIds[index];
            final FolderObject fo = cacheManager.getFolderObject(folderId, ctx);
            if (null == fo) { // Cache miss
                toLoad.put(folderId, index);
            } else { // Cache hit
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

    private static ConnectionProvider getConnection(final boolean modify, final StorageParameters storageParameters) throws OXException {
        ConnectionMode connection = optParameter(ConnectionMode.class, PARAM_CONNECTION, storageParameters);
        if (null != connection) {
            return new NonClosingConnectionProvider(connection/*, databaseService, context.getContextId()*/);
        }
        final Context context = storageParameters.getContext();
        final DatabaseService databaseService = DatabaseServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        connection = modify ? new ConnectionMode(databaseService.getWritable(context), true) : new ConnectionMode(databaseService.getReadOnly(context), false);
        return new ClosingConnectionProvider(connection, databaseService, context.getContextId());
    }

    private static <T> T getParameter(final Class<T> clazz, final String name, final StorageParameters parameters) throws OXException {
        final T parameter = optParameter(clazz, name, parameters);
        if (null == parameter) {
            throw OXFolderExceptionCode.MISSING_PARAMETER.create(name);
        }
        return parameter;
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

    private static int getTypeByFolderType(final Type type) {
        if (PrivateType.getInstance().equals(type)) {
            return FolderObject.PRIVATE;
        }
        if (PublicType.getInstance().equals(type)) {
            return FolderObject.PUBLIC;
        }
        return FolderObject.SYSTEM_TYPE;
    }

    private static int getTypeByFolderTypeWithShared(final Type type) {
        if (PrivateType.getInstance().equals(type)) {
            return FolderObject.PRIVATE;
        }
        if (PublicType.getInstance().equals(type)) {
            return FolderObject.PUBLIC;
        }
        if (SharedType.getInstance().equals(type)) {
            return FolderObject.SHARED;
        }
        return FolderObject.SYSTEM_TYPE;
    }

    private static final class FolderObjectComparator implements Comparator<FolderObject> {

        private final Collator collator;

        private final Context context;

        public FolderObjectComparator(final Locale locale, final Context context) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
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
                        if (owner1 > 0 && owner2 > 0) {
                            return collator.compare(
                                UserStorage.getStorageUser(owner1, context).getDisplayName(),
                                UserStorage.getStorageUser(owner2, context).getDisplayName());
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

    } // End of FolderObjectComparator

    private static final class FolderNameComparator implements Comparator<FolderObject> {

        private final Collator collator;

        private final Context context;

        public FolderNameComparator(final Locale locale, final Context context) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
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
                if (owner1 > 0 && owner2 > 0) {
                    return collator.compare(
                        UserStorage.getStorageUser(owner1, context).getDisplayName(),
                        UserStorage.getStorageUser(owner2, context).getDisplayName());
                }
            }
            return collator.compare(folderName1, folderName2);
        }

    } // End of FolderNameComparator

    public static final class ConnectionMode {

        /**
         * The connection.
         */
        public final Connection connection;

        /**
         * Whether connection is read-write or read-only.
         */
        public final boolean readWrite;

        /**
         * Initializes a new {@link ConnectionMode}.
         *
         * @param connection
         * @param readWrite
         */
        public ConnectionMode(final Connection connection, final boolean readWrite) {
            super();
            this.connection = connection;
            this.readWrite = readWrite;
        }
    }

}
