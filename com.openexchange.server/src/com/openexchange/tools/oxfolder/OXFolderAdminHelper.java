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

package com.openexchange.tools.oxfolder;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.server.impl.OCLPermission.ADMIN_PERMISSION;
import static com.openexchange.server.impl.OCLPermission.ALL_GROUPS_AND_USERS;
import static com.openexchange.server.impl.OCLPermission.ALL_GUESTS;
import static com.openexchange.server.impl.OCLPermission.CREATE_SUB_FOLDERS;
import static com.openexchange.server.impl.OCLPermission.NO_PERMISSIONS;
import static com.openexchange.server.impl.OCLPermission.READ_ALL_OBJECTS;
import static com.openexchange.server.impl.OCLPermission.READ_FOLDER;
import static com.openexchange.server.impl.OCLPermission.WRITE_OWN_OBJECTS;
import static com.openexchange.tools.sql.DBUtils.closeResources;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.cache.impl.FolderQueryCacheManager;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.NameBuilder;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.cache.CacheFolderStorage;
import com.openexchange.folderstorage.outlook.OutlookFolderStorage;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;

/**
 * {@link OXFolderAdminHelper} - Helper class for admin.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXFolderAdminHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXFolderAdminHelper.class);

    private static final boolean ADMIN_EDITABLE = false;

    /**
     * Initializes a new {@link OXFolderAdminHelper}.
     */
    public OXFolderAdminHelper() {
        super();
    }

    private static final int[] CHANGEABLE_PUBLIC_FOLDERS =
        { FolderObject.SYSTEM_PUBLIC_FOLDER_ID, FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID };

    /**
     * Checks if context's admin has administer permission on public folder(s).
     * <p>
     * <b>Note</b>: Succeeds only of specified user identifier denotes context's admin
     *
     * @param contextId The context identifier
     * @param userId The identifier of the user for whom the setting shall be checked
     * @param readCon A readable connection
     * @return <code>true</code> if context's admin has administer permission on public folder(s); otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    public boolean isPublicFolderEditable(final int contextId, final int userId, final Connection readCon) throws OXException {
        final int admin;
        try {
            admin = getContextAdminID(contextId, readCon);
        } catch (OXException e) {
            LOG.error("", e);
            return false;
        }
        if (admin != userId) {
            return false;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt =
                readCon.prepareStatement("SELECT admin_flag FROM oxfolder_permissions WHERE cid = ? AND permission_id = ? AND fuid IN " + StringCollection.getSqlInString(CHANGEABLE_PUBLIC_FOLDERS));
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            boolean editable = true;
            boolean foundPermissions = false;
            while (editable && rs.next()) {
                editable &= (rs.getInt(1) > 0);
                foundPermissions = true;
            }
            return editable && foundPermissions;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Sets if context's admin has administer permission on public folder(s).
     * <p>
     * <b>Note</b>: Succeeds only of specified user identifier denotes context's admin
     *
     * @param editable <code>true</code> if context's admin has administer permission on public folder(s); otherwise <code>false</code>
     * @param contextId The context identifier
     * @param userId The identifier of the user for whom the option shall be set
     * @param writeCon A writable connection
     * @throws OXException If an error occurs
     */
    public void setPublicFolderEditable(final boolean editable, final int contextId, final int userId, final Connection writeCon) throws OXException {
        final int admin;
        try {
            admin = getContextAdminID(contextId, writeCon);
        } catch (OXException e) {
            LOG.error("", e);
            return;
        }
        for (final int id : CHANGEABLE_PUBLIC_FOLDERS) {
            try {
                /*
                 * Check if folder has already been created for given context
                 */
                if (!checkFolderExistence(contextId, id, writeCon)) {
                    createPublicFolder(id, contextId, admin, writeCon, System.currentTimeMillis());
                }
            } catch (SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt =
                    writeCon.prepareStatement("SELECT permission_id, admin_flag, system FROM oxfolder_permissions WHERE cid = ? AND fuid = ? AND permission_id = ?");
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, id);
                stmt.setInt(pos++, userId);
                rs = stmt.executeQuery();
                final boolean update = rs.next();
                final boolean prevEditable = update && rs.getInt(2) > 0;
                final int system;
                if (update) {
                    system = rs.getInt(3);
                } else {
                    system = -1;
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                /*
                 * Check user
                 */
                if (admin != userId) {
                    if (prevEditable == editable || !editable) {
                        // No-op
                        return;
                    }
                    throw OXFolderExceptionCode.ADMIN_OP_ONLY.create();
                }
                /*
                 * Insert/Update
                 */
                if (update) {
                    stmt =
                        writeCon.prepareStatement("UPDATE oxfolder_permissions SET fp = ?, orp = ?, owp = ?, admin_flag = ?, odp = ? WHERE cid = ? AND fuid = ? AND permission_id = ? AND system = ?");
                    pos = 1;
                    stmt.setInt(pos++, CREATE_SUB_FOLDERS); // fp
                    stmt.setInt(pos++, NO_PERMISSIONS); // orp
                    stmt.setInt(pos++, NO_PERMISSIONS); // owp
                    stmt.setInt(pos++, editable ? 1 : 0); // admin_flag
                    stmt.setInt(pos++, NO_PERMISSIONS); // odp
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, id);
                    stmt.setInt(pos++, admin);
                    stmt.setInt(pos++, system);
                    stmt.executeUpdate();
                } else {
                    stmt =
                        writeCon.prepareStatement("INSERT INTO oxfolder_permissions (cid, fuid, permission_id, fp, orp, owp, odp, admin_flag, group_flag, system) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    pos = 1;
                    stmt.setInt(pos++, contextId); // cid
                    stmt.setInt(pos++, id); // fuid
                    stmt.setInt(pos++, admin); // permission_id
                    stmt.setInt(pos++, CREATE_SUB_FOLDERS); // fp
                    stmt.setInt(pos++, NO_PERMISSIONS); // orp
                    stmt.setInt(pos++, NO_PERMISSIONS); // owp
                    stmt.setInt(pos++, NO_PERMISSIONS); // odp
                    stmt.setInt(pos++, editable ? 1 : 0); // admin_flag
                    stmt.setInt(pos++, 0); // group_flag
                    stmt.setInt(pos++, 0); // system
                    stmt.executeUpdate();
                }
                Databases.closeSQLStuff(stmt);
                stmt = null;
                /*
                 * Update last-modified of folder
                 */
                final ContextImpl ctx = new ContextImpl(contextId);
                ctx.setMailadmin(admin);
                OXFolderSQL.updateLastModified(id, System.currentTimeMillis(), admin, writeCon, ctx);
                /*
                 * Update caches
                 */
                ConditionTreeMapManagement.dropFor(ctx.getContextId());
                try {
                    if (FolderCacheManager.isEnabled()) {
                        FolderCacheManager.getInstance().removeFolderObject(id, ctx);
                    }
                    if (FolderQueryCacheManager.isInitialized()) {
                        FolderQueryCacheManager.getInstance().invalidateContextQueries(contextId);
                    }
                } catch (OXException e) {
                    LOG.error("", e);
                }
            } catch (SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
        }
    }

    /**
     * Restores default permissions on global address book folder in given context.
     *
     * @param contextId The context identifier
     * @param gabMode The mode to use or <code>null</code>
     * @param enable Whether to enable or disable global address book access
     * @throws OXException If an error occurs
     */
    public void restoreDefaultGlobalAddressBookPermissions(final int contextId, final GABMode gabMode, final boolean enable) throws OXException {
        final Connection writeCon = Database.get(contextId, true);
        int rollback = 0;
        try {
            boolean isGlobalGABPermission = checkGlobalGABPermissionExistence(contextId, writeCon);
            GABMode mode = gabMode;
            if (null == mode) {
                mode = isGlobalGABPermission ? GABMode.GLOBAL : GABMode.INDIVIDUAL;
            }

            Databases.startTransaction(writeCon);
            rollback = 1;

            switch (mode) {
                case GLOBAL:
                    if (isGlobalGABPermission) {
                        /*
                         * Ensure correct permissions for default user group
                         */
                        restoreGABPermissionsGlobal(writeCon, contextId, enable);
                    } else {
                        /*
                         * Mode is switched, delete individual entries and add default user group
                         */
                        switchToGlobalAccess(writeCon, contextId, enable);
                    }
                    break;

                case INDIVIDUAL:
                    if (isGlobalGABPermission) {
                        /*
                         * Mode is switched, delete group permission and add all user individual
                         */
                        switchToIndividualAccess(writeCon, contextId, enable);
                    } else {
                        /*
                         * Ensure correct permissions for all users
                         */
                        restoreGABPermissionsIndividual(writeCon, contextId, enable);
                    }
                    break;

                default:
                    throw OXFolderExceptionCode.RUNTIME_ERROR.create(new RuntimeException("Unkown mode for global adressbook"));
            }

            writeCon.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(writeCon);
                }
                Databases.autocommit(writeCon);
            }
            Database.back(contextId, true, writeCon);
        }
    }

    /**
     * Restores the "all users and groups" permissions for the global address book.
     *
     * @param writeCon The write connection to use
     * @param contextId The context identifier
     * @param enable Whether to enable or disable global address book access
     */
    private void restoreGABPermissionsGlobal(Connection writeCon, int contextId, boolean enable) throws SQLException, OXException {
        int admin = getContextAdminID(contextId, writeCon);
        for (OCLPermission permission : ImmutableList.of(getGABAdmin(writeCon, contextId, Optional.of(Integer.valueOf(admin))), getAllUserGABGroup(enable))) {
            PreparedStatement stmt = null;
            try {
                stmt = writeCon.prepareStatement("UPDATE oxfolder_permissions SET fp = ?, orp = ?, owp = ?, admin_flag = ?, group_flag = ?, odp = ? WHERE cid = ? AND fuid = ? AND permission_id = ?");
                int pos = 1;
                stmt.setInt(pos++, permission.getFolderPermission());
                stmt.setInt(pos++, permission.getReadPermission());
                stmt.setInt(pos++, permission.getWritePermission());
                stmt.setInt(pos++, permission.isFolderAdmin() ? 1 : 0);
                stmt.setInt(pos++, permission.isGroupPermission() ? 1 : 0);
                stmt.setInt(pos++, NO_PERMISSIONS);
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, FolderObject.SYSTEM_LDAP_FOLDER_ID);
                stmt.setInt(pos++, permission.getEntity());
                stmt.executeUpdate();
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }
        // Propagate change
        {
            ContextImpl ctx = new ContextImpl(contextId);
            ctx.setMailadmin(admin);
            OXFolderSQL.updateLastModified(FolderObject.SYSTEM_LDAP_FOLDER_ID, System.currentTimeMillis(), admin, writeCon, ctx);
            /*
             * Update caches
             */
            ConditionTreeMapManagement.dropFor(ctx.getContextId());
            try {
                if (FolderCacheManager.isEnabled()) {
                    FolderCacheManager.getInstance().removeFolderObject(FolderObject.SYSTEM_LDAP_FOLDER_ID, ctx);
                }
                CacheFolderStorage.getInstance().clearCache(-1, ctx.getContextId());
                if (FolderQueryCacheManager.isInitialized()) {
                    FolderQueryCacheManager.getInstance().invalidateContextQueries(contextId);
                }
                final CacheService service = ServerServiceRegistry.getInstance().getService(CacheService.class);
                if (null != service) {
                    Cache cache = service.getCache("GlobalFolderCache");
                    String sContextId = Integer.toString(contextId);
                    String sGlobalAddressBookId = Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID);
                    for (String treeId : new String[] { FolderStorage.REAL_TREE_ID, OutlookFolderStorage.OUTLOOK_TREE_ID, "20" }) {
                        CacheKey cacheKey = service.newCacheKey(1, treeId, sGlobalAddressBookId);
                        cache.removeFromGroup(cacheKey, sContextId);
                    }
                }
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Switches the modus of the global address book from a per user permission to a permission for the all user group
     *
     * @param writeCon The write connection to use
     * @param contextId The context identifier
     * @param enable Whether to enable or disable global address book access
     * @throws SQLException In case of a DB error
     * @throws OXException In case of an error
     */
    private void switchToGlobalAccess(Connection writeCon, int contextId, boolean enable) throws SQLException, OXException {
        int admin = getContextAdminID(contextId, writeCon);
        deleteAllGABEntries(writeCon, contextId);
        insertPermissionsForSystemFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID, ImmutableList.of(getGABAdmin(writeCon, contextId, Optional.of(Integer.valueOf(admin))), getAllUserGABGroup(enable)), contextId, writeCon);
        {
            ContextImpl ctx = new ContextImpl(contextId);
            ctx.setMailadmin(admin);
            OXFolderSQL.updateLastModified(FolderObject.SYSTEM_LDAP_FOLDER_ID, System.currentTimeMillis(), admin, writeCon, ctx);
            /*
             * Update caches
             */
            ConditionTreeMapManagement.dropFor(ctx.getContextId());
            try {
                if (FolderCacheManager.isEnabled()) {
                    FolderCacheManager.getInstance().removeFolderObject(FolderObject.SYSTEM_LDAP_FOLDER_ID, ctx);
                }
                CacheFolderStorage.getInstance().clearCache(-1, ctx.getContextId());
                if (FolderQueryCacheManager.isInitialized()) {
                    FolderQueryCacheManager.getInstance().invalidateContextQueries(contextId);
                }
                final CacheService service = ServerServiceRegistry.getInstance().getService(CacheService.class);
                if (null != service) {
                    Cache cache = service.getCache("GlobalFolderCache");
                    String sContextId = Integer.toString(contextId);
                    String sGlobalAddressBookId = Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID);
                    for (String treeId : new String[] { FolderStorage.REAL_TREE_ID, OutlookFolderStorage.OUTLOOK_TREE_ID, "20" }) {
                        CacheKey cacheKey = service.newCacheKey(1, treeId, sGlobalAddressBookId);
                        cache.removeFromGroup(cacheKey, sContextId);
                    }
                }
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
    }

    /**
     * Restores the per user permissions for the global address book
     *
     * @param writeCon The write connection to use
     * @param contextId The context identifier
     * @param enable Whether to enable or disable global address book access
     */
    private void restoreGABPermissionsIndividual(Connection writeCon, int contextId, boolean enable) throws OXException {
        /*
         * Get context's users, excluding guest users. Afterwards iterate over them
         */
        final TIntList users = getUsers(writeCon, contextId);
        if (!users.isEmpty()) {
            final Integer admin = Integer.valueOf(getContextAdminID(contextId, writeCon));
            for (int i = 1; i < users.size(); i++) {
                setGlobalAddressBookDisabled(contextId, users.get(i), !enable, GABMode.INDIVIDUAL, writeCon, admin, false);
            }
            /*
             * Propagate with last update
             */
            setGlobalAddressBookDisabled(contextId, users.get(0), !enable, GABMode.INDIVIDUAL, writeCon, admin, true);
        }
    }

    /**
     * Switches the modus of the global address book from a global access permission to user individual permissions in the DB
     *
     * @param writeCon The write connection to use
     * @param contextId The context identifier
     * @param enable Whether to enable or disable global address book access for each user
     * @throws OXException In case of error
     * @throws SQLException In case of DB error
     */
    private void switchToIndividualAccess(Connection writeCon, int contextId, boolean enable) throws OXException, SQLException {
        deleteAllGABEntries(writeCon, contextId);
        restoreGABPermissionsIndividual(writeCon, contextId, enable);
    }

    /**
     * Delete all permission entires on the global address book
     *
     * @param writeCon The connection to use
     * @param contextId The context identifier
     * @throws SQLException in case of an error
     */
    private void deleteAllGABEntries(Connection writeCon, int contextId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement("DELETE FROM oxfolder_permissions WHERE cid = ? AND fuid = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, FolderObject.SYSTEM_LDAP_FOLDER_ID);
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Get the users of a specific context, excluding guest users
     *
     * @param connection The connection to query the users from
     * @param contextId The context identifier
     * @return The users
     * @throws OXException In case of an error
     */
    private TIntList getUsers(Connection connection, int contextId) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT id FROM user WHERE cid = ? AND guestCreatedBy=0");
            final int pos = 1;
            stmt.setInt(pos, contextId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return new TIntArrayList(0);
            }

            TIntList users = new TIntLinkedList();
            do {
                users.add(rs.getInt(pos));
            } while (rs.next());
            return users;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Get the permissions for the context administrator regarding the global address book
     *
     * @param connection The connection to query the administrator information from
     * @param contextId The context identifier
     * @param optionalAdminId The optional administrator identifier
     * @return The permissions
     * @throws OXException in case of an error
     */
    private OCLPermission getGABAdmin(Connection connection, int contextId, Optional<Integer> optionalAdminId) throws OXException {
        OCLPermission adminPermission = new OCLPermission();
        adminPermission.setEntity(optionalAdminId.isPresent() ? optionalAdminId.get().intValue() : getContextAdminID(contextId, connection));
        adminPermission.setGroupPermission(false);
        setGABPermissions(adminPermission);
        adminPermission.setFolderAdmin(true);
        return adminPermission;
    }

    /**
     * Get permissions for the all user group
     *
     * @param enable Whether to enable or disable global address book access
     * @return The permissions
     */
    private OCLPermission getAllUserGABGroup(boolean enable) {
        OCLPermission allUserGroup = new OCLPermission(OCLPermission.ALL_GROUPS_AND_USERS, FolderObject.SYSTEM_LDAP_FOLDER_ID);
        allUserGroup.setGroupPermission(true);
        allUserGroup.setFolderAdmin(false);
        setGABPermissions(allUserGroup, enable);
        return allUserGroup;
    }

    /**
     * Checks whether global address book is enabled for specified user.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param readCon A readable connection
     * @return <code>true</code> if global address book is disabled; otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    public boolean isGlobalAddressBookDisabled(final int contextId, final int userId, final Connection readCon) throws OXException {
        /*
         * Check if global permission is enabled for global address book folder
         */
        final int globalAddressBookId = FolderObject.SYSTEM_LDAP_FOLDER_ID;
        try {
            final int[] perms = getPermissionValue(contextId, globalAddressBookId, ALL_GROUPS_AND_USERS, readCon);
            if (null != perms) {
                LOG.warn("Cannot look-up individual user permission: Global permission is active on global address book folder.\nReturning global permission instead. user={}, context={}", I(userId), I(contextId));
                return (perms[0] == NO_PERMISSIONS);
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = readCon.prepareStatement("SELECT fp, orp FROM oxfolder_permissions WHERE cid = ? AND fuid = ? AND permission_id = ?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, globalAddressBookId);
            stmt.setInt(pos++, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == NO_PERMISSIONS; // && rs.getInt(2) >= READ_FOLDER;
            }
            return true;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Enables/disables specified user's global address book permission.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param disable <code>true</code> to disable user's global address book permission; otherwise <code>false</code>
     * @param gabMode The mode to use
     * @param writeCon A writable connection
     * @throws OXException If an error occurs
     */
    public void setGlobalAddressBookDisabled(final int contextId, final int userId, final boolean disable, final GABMode gabMode, final Connection writeCon) throws OXException {
        setGlobalAddressBookDisabled(contextId, userId, disable, gabMode, writeCon, null, true);
        LOG.debug("{} global address book for user {} in context {}", disable ? "Disabled" : "Enabled", Integer.valueOf(userId), Integer.valueOf(contextId));
    }

    /**
     * Enables/disables specified user's individual global address book permission.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param disable <code>true</code> to disable user's global address book permission; otherwise <code>false</code>
     * @param gabMode The mode to use
     * @param writeCon A writable connection
     * @param adminId The optional administrator identifier or <code>null</code>
     * @param propagate <code>true</code> to propagate; otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    private void setGlobalAddressBookDisabled(final int contextId, final int userId, final boolean disable, final GABMode gabMode, final Connection writeCon, final Integer adminId, final boolean propagate) throws OXException {
        int admin = adminId == null ? getContextAdminID(contextId, writeCon) : adminId.intValue();
        boolean isAdmin = (admin == userId);
        int globalAddressBookId = FolderObject.SYSTEM_LDAP_FOLDER_ID;
        /*
         * Check if folder has already been created for given context in case context administrator is given
         */
        if (isAdmin) {
            try {
                if (!checkFolderExistence(contextId, globalAddressBookId, writeCon)) {
                    createGlobalAddressBook(contextId, admin, gabMode, writeCon, System.currentTimeMillis());
                }
            } catch (SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        /*
         * Check if global permission is enabled for global address book folder
         */
        try {
            if (checkGlobalGABPermissionExistence(contextId, writeCon)) {
                /*
                 * Global permission enabled for global address book folder
                 */
                if (GABMode.INDIVIDUAL.equals(gabMode)) {
                    LOG.debug("Cannot update individual permission on global address book folder since global permission is active. user={}, context={}", I(userId), I(contextId));
                }
                return;
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * Enable/disable user's individual global address book permission
         */
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = writeCon.prepareStatement("SELECT permission_id,system FROM oxfolder_permissions WHERE cid = ? AND fuid = ? AND permission_id = ?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, globalAddressBookId);
            stmt.setInt(pos++, userId);
            rs = stmt.executeQuery();
            final boolean update = rs.next();
            final int system = (update) ? rs.getInt(2) : -1;

            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            /*
             * Insert/Update
             */
            if (update) {
                stmt = writeCon.prepareStatement("UPDATE oxfolder_permissions SET fp = ?, orp = ?, owp = ?, admin_flag = ?, odp = ? WHERE cid = ? AND fuid = ? AND permission_id = ? AND system = ?");
                pos = 1;
                if (disable) {
                    stmt.setInt(pos++, NO_PERMISSIONS);
                    stmt.setInt(pos++, NO_PERMISSIONS);
                    stmt.setInt(pos++, NO_PERMISSIONS);
                } else {
                    stmt.setInt(pos++, READ_FOLDER);
                    stmt.setInt(pos++, READ_ALL_OBJECTS);
                    stmt.setInt(pos++, OXFolderProperties.isEnableInternalUsersEdit() ? WRITE_OWN_OBJECTS : NO_PERMISSIONS);
                }
                stmt.setInt(pos++, isAdmin ? 1 : 0);
                stmt.setInt(pos++, NO_PERMISSIONS);
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, globalAddressBookId);
                stmt.setInt(pos++, userId);
                stmt.setInt(pos++, system);
                stmt.executeUpdate();
            } else {
                stmt = writeCon.prepareStatement("INSERT INTO oxfolder_permissions (cid, fuid, permission_id, fp, orp, owp, odp, admin_flag, group_flag, system) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                pos = 1;
                stmt.setInt(pos++, contextId); // cid
                stmt.setInt(pos++, globalAddressBookId); // fuid
                stmt.setInt(pos++, userId); // permission_id
                if (disable) {
                    stmt.setInt(pos++, NO_PERMISSIONS); // fp
                    stmt.setInt(pos++, NO_PERMISSIONS); // orp
                    stmt.setInt(pos++, NO_PERMISSIONS); // owp
                } else {
                    stmt.setInt(pos++, READ_FOLDER); // fp
                    stmt.setInt(pos++, READ_ALL_OBJECTS); // orp
                    stmt.setInt(pos++, OXFolderProperties.isEnableInternalUsersEdit() ? WRITE_OWN_OBJECTS : NO_PERMISSIONS); // owp
                }
                stmt.setInt(pos++, NO_PERMISSIONS); // odp
                stmt.setInt(pos++, isAdmin ? 1 : 0); // admin_flag
                stmt.setInt(pos++, 0); // group_flag
                stmt.setInt(pos++, 0); // system
                stmt.executeUpdate();
            }
            Databases.closeSQLStuff(stmt);
            stmt = null;
            /*
             * Update last-modified of folder
             */
            if (propagate) {
                ContextImpl ctx = new ContextImpl(contextId);
                ctx.setMailadmin(admin);
                OXFolderSQL.updateLastModified(globalAddressBookId, System.currentTimeMillis(), admin, writeCon, ctx);
                /*
                 * Update caches
                 */
                ConditionTreeMapManagement.dropFor(ctx.getContextId());
                try {
                    if (FolderCacheManager.isEnabled()) {
                        FolderCacheManager.getInstance().removeFolderObject(globalAddressBookId, ctx);
                    }
                    CacheFolderStorage.getInstance().clearCache(-1, ctx.getContextId());
                    if (FolderQueryCacheManager.isInitialized()) {
                        FolderQueryCacheManager.getInstance().invalidateContextQueries(contextId);
                    }
                    final CacheService service = ServerServiceRegistry.getInstance().getService(CacheService.class);
                    if (null != service) {
                        Cache cache = service.getCache("GlobalFolderCache");
                        String sContextId = Integer.toString(contextId);
                        String sGlobalAddressBookId = Integer.toString(globalAddressBookId);
                        for (String treeId : new String[] { FolderStorage.REAL_TREE_ID, OutlookFolderStorage.OUTLOOK_TREE_ID, "20" }) {
                            CacheKey cacheKey = service.newCacheKey(1, treeId, sGlobalAddressBookId);
                            cache.removeFromGroup(cacheKey, sContextId);
                        }
                    }
                } catch (OXException e) {
                    LOG.error("", e);
                }
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private static final String STR_TABLE = "#TABLE#";

    /**
     * Creates the standard system folders located in each context for given context in database and creates the default folders for
     * context's admin user by invoking <code>{@link #addUserToOXFolders(int, String, String, int, Connection)}</code>
     *
     * @param contextId The context identifier
     * @param gabMode The modus the global address book shall operate on
     * @param mailAdminDisplayName The display name of context's admin user
     * @param language The language conforming to syntax rules of locales
     * @param con A writeable connection
     * @throws OXException If system folder could not be created successfully or default folders for context's admin user could not be
     *             created
     */
    public void addContextSystemFolders(final int contextId, final GABMode gabMode, final String mailAdminDisplayName, final String language, final Connection con) throws OXException {
        try {
            final int contextMalAdmin = getContextAdminID(contextId, con);
            addContextSystemFolders(contextId, contextMalAdmin, gabMode, mailAdminDisplayName, language, con);
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private static int getContextAdminID(final int contextId, final Connection readCon) throws OXException {
        try {
            final int retval = OXFolderSQL.getContextAdminID(new ContextImpl(contextId), readCon);
            if (retval == -1) {
                throw OXFolderExceptionCode.NO_ADMIN_USER_FOUND_IN_CONTEXT.create(Integer.valueOf(contextId));
            }
            return retval;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private void addContextSystemFolders(final int contextId, final int mailAdmin, final GABMode gabMode, final String mailAdminDisplayName, final String language, final Connection writeCon) throws SQLException, OXException {
        final long creatingTime = System.currentTimeMillis();
        final OCLPermission systemPermission = new OCLPermission();
        systemPermission.setEntity(ALL_GROUPS_AND_USERS);
        systemPermission.setGroupPermission(true);
        /*
         * Insert system private folder
         */
        systemPermission.setAllPermission(CREATE_SUB_FOLDERS, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        systemPermission.setFolderAdmin(false);
        createSystemFolder(
            FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
            FolderObject.SYSTEM_PRIVATE_FOLDER_NAME,
            systemPermission,
            FolderObject.SYSTEM_ROOT_FOLDER_ID,
            FolderObject.SYSTEM_MODULE,
            true,
            creatingTime,
            mailAdmin,
            true,
            contextId,
            writeCon);
        final OCLPermission guestPermission = new OCLPermission();
        guestPermission.setEntity(ALL_GUESTS);
        guestPermission.setGroupPermission(true);
        guestPermission.setFolderAdmin(false);
        guestPermission.setAllPermission(READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        createSinglePermission(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, guestPermission, contextId, writeCon);
        /*
         * Insert system public folder
         */
        if (!checkFolderExistence(contextId, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, writeCon)) {
            createSystemPublicFolder(contextId, mailAdmin, writeCon, creatingTime);
        }
        /*
         * Insert system shared folder
         */
        systemPermission.setAllPermission(READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        systemPermission.setFolderAdmin(false);
        createSystemFolder(
            FolderObject.SYSTEM_SHARED_FOLDER_ID,
            FolderObject.SYSTEM_SHARED_FOLDER_NAME,
            systemPermission,
            FolderObject.SYSTEM_ROOT_FOLDER_ID,
            FolderObject.SYSTEM_MODULE,
            true,
            creatingTime,
            mailAdmin,
            true,
            contextId,
            writeCon);
        guestPermission.setAllPermission(READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        createSinglePermission(FolderObject.SYSTEM_SHARED_FOLDER_ID, guestPermission, contextId, writeCon);
        /*
         * Insert system system folder
         */
        systemPermission.setAllPermission(READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        systemPermission.setFolderAdmin(false);
        createSystemFolder(
            FolderObject.SYSTEM_FOLDER_ID,
            FolderObject.SYSTEM_FOLDER_NAME,
            systemPermission,
            FolderObject.SYSTEM_ROOT_FOLDER_ID,
            FolderObject.SYSTEM_MODULE,
            true,
            creatingTime,
            mailAdmin,
            true,
            contextId,
            writeCon);
        /*
         * Insert system infostore folder
         */
        if (!checkFolderExistence(contextId, FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, writeCon)) {
            createSystemInfostoreFolder(contextId, mailAdmin, writeCon, creatingTime);
        }
        /*-
         * Insert system system_global folder aka 'Shared Address Book'
         *
        systemPermission.setAllPermission(CREATE_SUB_FOLDERS, ADMIN_PERMISSION, ADMIN_PERMISSION, ADMIN_PERMISSION);
        systemPermission.setFolderAdmin(false);
        createSystemFolder(
            FolderObject.SYSTEM_GLOBAL_FOLDER_ID,
            FolderObject.SYSTEM_GLOBAL_FOLDER_NAME,
            systemPermission,
            FolderObject.SYSTEM_FOLDER_ID,
            FolderObject.CONTACT,
            true,
            creatingTime,
            mailAdmin,
            true,
            contextId,
            writeCon);
        */
        /*
         * Insert system internal users folder aka 'Global Address Book'
         */
        // TODO: Whether to enable/disable internal-user-edit should be set by
        // caller (admin) as a parameter
        final int globalAddressBookId = FolderObject.SYSTEM_LDAP_FOLDER_ID;
        if (checkFolderExistence(contextId, globalAddressBookId, writeCon)) {
            final ContextImpl ctx = new ContextImpl(contextId);
            ctx.setMailadmin(mailAdmin);
            try {
                OXFolderSQL.updateLastModified(globalAddressBookId, creatingTime, mailAdmin, writeCon, ctx);
            } catch (OXException e) {
                // Cannot occur
                LOG.error("", e);
            }
        } else {
            createGlobalAddressBook(contextId, mailAdmin, gabMode, writeCon, creatingTime);
        }
        /*
         * Insert system user folder
         */
        systemPermission.setAllPermission(READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        systemPermission.setFolderAdmin(false);
        createSystemFolder(
            FolderObject.SYSTEM_OX_FOLDER_ID,
            FolderObject.SYSTEM_OX_FOLDER_NAME,
            systemPermission,
            FolderObject.SYSTEM_ROOT_FOLDER_ID,
            FolderObject.SYSTEM_MODULE,
            true,
            creatingTime,
            mailAdmin,
            true,
            contextId,
            writeCon);
        /*
         * Insert system userstore infostore folder
         */
        systemPermission.setAllPermission(READ_FOLDER, READ_ALL_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
        systemPermission.setFolderAdmin(false);
        createSystemFolder(
            FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID,
            FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_NAME,
            systemPermission,
            FolderObject.SYSTEM_INFOSTORE_FOLDER_ID,
            FolderObject.INFOSTORE,
            true,
            creatingTime,
            mailAdmin,
            true,
            contextId,
            writeCon);
        guestPermission.setAllPermission(READ_FOLDER, READ_ALL_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
        createSinglePermission(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, guestPermission, contextId, writeCon);
        /*
         * Insert system public infostore folder
         */
        if (!checkFolderExistence(contextId, FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID, writeCon)) {
            createSystemPublicInfostoreFolder(contextId, mailAdmin, writeCon, creatingTime);
        }
        LOG.debug("All System folders successfully created for context {}", I(contextId));
        /*
         * Add mailadmin's folder rights to context's system folders and create his standard folders
         */
        /*-
         * Don't grant full access for admin on top level infostore folder
         *
        createSingleUserPermission(
            FolderObject.SYSTEM_INFOSTORE_FOLDER_ID,
            mailAdmin,
            new int[] {
                ADMIN_PERMISSION, ADMIN_PERMISSION, ADMIN_PERMISSION,
                ADMIN_PERMISSION },
            true,
            contextId,
            writeCon);
         */
        /*-
         * Grant full access for 'Shared Address Book' to admin
         *
        createSingleUserPermission(
            FolderObject.SYSTEM_GLOBAL_FOLDER_ID,
            mailAdmin,
            new int[] { ADMIN_PERMISSION, ADMIN_PERMISSION, ADMIN_PERMISSION, ADMIN_PERMISSION },
            true,
            contextId,
            writeCon);
        */
        if (ADMIN_EDITABLE) {
            /*
             * Grant admin access for public infostore folder to admin
             */
            createSingleUserPermission(
                FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID,
                mailAdmin,
                new int[] { CREATE_SUB_FOLDERS, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS },
                true,
                contextId,
                writeCon);
            /*
             * Grant admin access for user infostore folder to admin
             */
            createSingleUserPermission(
                FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID,
                mailAdmin,
                new int[] { READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS },
                true,
                contextId,
                writeCon);
        }
        addUserToOXFolders(mailAdmin, mailAdminDisplayName, language, contextId, writeCon, OXFolderDefaultMode.DEFAULT);
        LOG.debug("Folder rights for mail admin successfully added for context {}", I(contextId));
    }

    /**
     * Creates a public system folder with default permission for all-groups-and-users entity.
     *
     * @param contextId The context identifier
     * @param mailAdmin The context admin
     * @param writeCon A writable connection
     * @param creatingTime The creation date
     * @throws SQLException If a SQL error occurs
     */
    private static void createPublicFolder(final int folderId, final int contextId, final int mailAdmin, final Connection writeCon, final long creatingTime) throws SQLException {
        if (FolderObject.SYSTEM_PUBLIC_FOLDER_ID == folderId) {
            createSystemPublicFolder(contextId, mailAdmin, writeCon, creatingTime);
        } else if (FolderObject.SYSTEM_INFOSTORE_FOLDER_ID == folderId) {
            createSystemInfostoreFolder(contextId, mailAdmin, writeCon, creatingTime);
        } else if (FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID == folderId) {
            createSystemPublicInfostoreFolder(contextId, mailAdmin, writeCon, creatingTime);
        } else {
            throw new IllegalArgumentException("Specified folder identifier is not a public folder identifier: " + folderId);
        }
    }

    /**
     * Creates the public folder with default permission for all-groups-and-users entity.
     *
     * @param contextId The context identifier
     * @param mailAdmin The context admin
     * @param writeCon A writable connection
     * @param creatingTime The creation date
     * @throws SQLException If a SQL error occurs
     */
    private static void createSystemPublicFolder(final int contextId, final int mailAdmin, final Connection writeCon, final long creatingTime) throws SQLException {
        final OCLPermission systemPermission = new OCLPermission();
        systemPermission.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
        systemPermission.setGroupPermission(true);
        systemPermission.setAllPermission(CREATE_SUB_FOLDERS, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        systemPermission.setFolderAdmin(false);
        createSystemFolder(
            FolderObject.SYSTEM_PUBLIC_FOLDER_ID,
            FolderObject.SYSTEM_PUBLIC_FOLDER_NAME,
            systemPermission,
            FolderObject.SYSTEM_ROOT_FOLDER_ID,
            FolderObject.SYSTEM_MODULE,
            true,
            creatingTime,
            mailAdmin,
            true,
            contextId,
            writeCon);
        final OCLPermission guestPermission = new OCLPermission();
        guestPermission.setEntity(OCLPermission.ALL_GUESTS);
        guestPermission.setGroupPermission(true);
        guestPermission.setFolderAdmin(false);
        guestPermission.setAllPermission(READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        createSinglePermission(FolderObject.SYSTEM_PUBLIC_FOLDER_ID, guestPermission, contextId, writeCon);

    }

    /**
     * Creates the infostore folder with default permission for all-groups-and-users entity.
     *
     * @param contextId The context identifier
     * @param mailAdmin The context admin
     * @param writeCon A writable connection
     * @param creatingTime The creation date
     * @throws SQLException If a SQL error occurs
     */
    private static void createSystemInfostoreFolder(final int contextId, final int mailAdmin, final Connection writeCon, final long creatingTime) throws SQLException {
        final OCLPermission systemPermission = new OCLPermission();
        systemPermission.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
        systemPermission.setGroupPermission(true);
        systemPermission.setAllPermission(READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        systemPermission.setFolderAdmin(false);
        createSystemFolder(
            FolderObject.SYSTEM_INFOSTORE_FOLDER_ID,
            FolderObject.SYSTEM_INFOSTORE_FOLDER_NAME,
            systemPermission,
            FolderObject.SYSTEM_ROOT_FOLDER_ID,
            FolderObject.SYSTEM_MODULE,
            true,
            creatingTime,
            mailAdmin,
            true,
            contextId,
            writeCon);
        final OCLPermission guestPermission = new OCLPermission();
        guestPermission.setEntity(OCLPermission.ALL_GUESTS);
        guestPermission.setGroupPermission(true);
        guestPermission.setFolderAdmin(false);
        guestPermission.setAllPermission(READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        createSinglePermission(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, guestPermission, contextId, writeCon);
    }

    /**
     * Creates the public infostore folder with default permission for all-groups-and-users entity.
     *
     * @param contextId The context identifier
     * @param mailAdmin The context admin
     * @param writeCon A writable connection
     * @param creatingTime The creation date
     * @throws SQLException If a SQL error occurs
     */
    private static void createSystemPublicInfostoreFolder(final int contextId, final int mailAdmin, final Connection writeCon, final long creatingTime) throws SQLException {
        final OCLPermission systemPermission = new OCLPermission();
        systemPermission.setEntity(ALL_GROUPS_AND_USERS);
        systemPermission.setGroupPermission(true);
        systemPermission.setAllPermission(CREATE_SUB_FOLDERS, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        systemPermission.setFolderAdmin(false);
        createSystemFolder(
            FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID,
            FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_NAME,
            systemPermission,
            FolderObject.SYSTEM_INFOSTORE_FOLDER_ID,
            FolderObject.INFOSTORE,
            true,
            creatingTime,
            mailAdmin,
            true,
            contextId,
            writeCon);
        final OCLPermission guestPermission = new OCLPermission();
        guestPermission.setEntity(ALL_GUESTS);
        guestPermission.setGroupPermission(true);
        guestPermission.setFolderAdmin(false);
        guestPermission.setAllPermission(READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        createSinglePermission(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID, guestPermission, contextId, writeCon);
    }

    /**
     * Checks permission existence for all-groups-and-users on GAB folder in given context.
     *
     * @param contextId The context identifier
     * @param con A connection
     * @return <code>true</code> if a permission exists; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    private static boolean checkGlobalGABPermissionExistence(final int contextId, final Connection con) throws SQLException {
        return checkPermissionExistence(contextId, FolderObject.SYSTEM_LDAP_FOLDER_ID, OCLPermission.ALL_GROUPS_AND_USERS, con);
    }

    /**
     * Checks permission existence in given folder in given context for given user.
     *
     * @param contextId The context identifier
     * @param folderId The folder identifier
     * @param userId The user identifier
     * @param con A connection
     * @return <code>true</code> if a permission exists; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    private static boolean checkPermissionExistence(final int contextId, final int folderId, final int userId, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT permission_id FROM oxfolder_permissions WHERE cid = ? AND fuid = ? AND permission_id = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);
            return (rs = stmt.executeQuery()).next();
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Checks permission values for folder, object-read, object-write and object-delete permission.
     *
     * @param contextId The context identifier
     * @param folderId The folder identifier
     * @param entityId The entity identifier
     * @param con A connection
     * @return The permissions as an array or <code>null</code>
     * @throws SQLException If a SQL error occurs
     */
    private static int[] getPermissionValue(final int contextId, final int folderId, final int entityId, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt =
                con.prepareStatement("SELECT fp, orp, owp, odp FROM oxfolder_permissions WHERE cid = ? AND fuid = ? AND permission_id = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.setInt(3, entityId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return new int[] { rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4) };
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Checks existence of given folder in given context.
     *
     * @param contextId The context identifier
     * @param folderId The folder identifier
     * @param con A connection
     * @return <code>true</code> if exists; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    private static boolean checkFolderExistence(final int contextId, final int folderId, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("SELECT fuid FROM oxfolder_tree WHERE cid = ? AND fuid = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            return stmt.executeQuery().next();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Creates the global address book folder.
     *
     * @param contextId The context identifier
     * @param mailAdmin The identifier of context's administrator
     * @param gabMode The modus to operate on
     * @param writeCon A writable connection
     * @param creatingTime The creating time
     * @throws SQLException If a SQL error occurs
     * @throws OXException If context administrator can't be found
     */
    private void createGlobalAddressBook(final int contextId, final int mailAdmin, final GABMode gabMode, final Connection writeCon, final long creatingTime) throws SQLException, OXException {
        createSystemFolder(
            FolderObject.SYSTEM_LDAP_FOLDER_ID,
            FolderObject.SYSTEM_LDAP_FOLDER_NAME,
            GABMode.GLOBAL.equals(gabMode) ? ImmutableList.of(getGABAdmin(writeCon, contextId, Optional.of(Integer.valueOf(mailAdmin))), getAllUserGABGroup(OXFolderProperties.isEnableInternalUsersEdit())) : ImmutableList.of(getGABAdmin(writeCon, contextId, Optional.of(Integer.valueOf(mailAdmin)))),
            FolderObject.SYSTEM_FOLDER_ID,
            FolderObject.CONTACT,
            true,
            creatingTime,
            mailAdmin,
            false,
            contextId,
            writeCon);
    }

    private final static String SQL_INSERT_SYSTEM_FOLDER =
        "INSERT INTO oxfolder_tree (fuid, cid, parent, fname, module, type, creating_date, created_from, changing_date, changed_from, permission_flag, subfolder_flag) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String SQL_INSERT_SYSTEM_PERMISSION =
        "INSERT INTO oxfolder_permissions (cid, fuid, permission_id, fp, orp, owp, odp, admin_flag, group_flag) VALUES (?,?,?,?,?,?,?,?,?)";

    private static final String SQL_INSERT_SPECIAL_FOLDER = "INSERT INTO oxfolder_specialfolders " + "(tag, cid, fuid) VALUES (?,?,?)";

    private static void createSystemFolder(final int systemFolderId, final String systemFolderName, final OCLPermission systemPermission, final int parentId, final int module, final boolean insertIntoSpecialFolders, final long creatingTime, final int mailAdminId, final boolean isPublic, final int contextId, final Connection writeCon) throws SQLException {
        createSystemFolder(systemFolderId, systemFolderName, Collections.singleton(systemPermission), parentId, module, insertIntoSpecialFolders, creatingTime, mailAdminId, isPublic, contextId, writeCon);
    }

    private static void createSystemFolder(final int systemFolderId, final String systemFolderName, final Collection<OCLPermission> systemPermissions, final int parentId, final int module, final boolean insertIntoSpecialFolders, final long creatingTime, final int mailAdminId, final boolean isPublic, final int contextId, final Connection writeCon) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement(SQL_INSERT_SYSTEM_FOLDER);
            stmt.setInt(1, systemFolderId);
            stmt.setInt(2, contextId);
            stmt.setInt(3, parentId);
            stmt.setString(4, systemFolderName);
            stmt.setInt(5, module);
            stmt.setInt(6, FolderObject.SYSTEM_TYPE);
            stmt.setLong(7, creatingTime);
            stmt.setInt(8, mailAdminId); // created_from
            stmt.setLong(9, creatingTime); // changing_date
            stmt.setInt(10, mailAdminId); // changed_from
            stmt.setInt(11, isPublic ? FolderObject.PUBLIC_PERMISSION : FolderObject.CUSTOM_PERMISSION); // permission_flag
            stmt.setInt(12, 1); // subfolder_flag
            stmt.executeUpdate();
            stmt.close();
            stmt = null;
            insertPermissionsForSystemFolder(systemFolderId, systemPermissions, contextId, writeCon);
            if (insertIntoSpecialFolders) {
                stmt = writeCon.prepareStatement(SQL_INSERT_SPECIAL_FOLDER);
                stmt.setString(1, systemFolderName); // tag
                stmt.setInt(2, contextId); // cid
                stmt.setInt(3, systemFolderId); // fuid
                stmt.executeUpdate();
                stmt.close();
                stmt = null;
            }
        } finally {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        }
    }

    private static void insertPermissionsForSystemFolder(final int systemFolderId, final Collection<OCLPermission> systemPermissions, final int contextId, final Connection writeCon) throws SQLException {
        if (systemPermissions.isEmpty()) {
            return;
        }

        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement(SQL_INSERT_SYSTEM_PERMISSION);
            for (OCLPermission systemPermission : systemPermissions) {
                stmt.setInt(1, contextId); // cid
                stmt.setInt(2, systemFolderId); // fuid
                stmt.setInt(3, systemPermission.getEntity()); // entity
                stmt.setInt(4, systemPermission.getFolderPermission()); // folder permission
                stmt.setInt(5, systemPermission.getReadPermission()); // read permission
                stmt.setInt(6, systemPermission.getWritePermission()); // write permission
                stmt.setInt(7, systemPermission.getDeletePermission()); // delete permission
                stmt.setInt(8, systemPermission.isFolderAdmin() ? 1 : 0); // admin_flag
                stmt.setInt(9, systemPermission.isGroupPermission() ? 1 : 0); // group_flag
                stmt.addBatch();
            }
            stmt.executeBatch();
        } finally {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        }
    }

    private void createSingleUserPermission(final int fuid, final int userId, final int[] allPerms, final boolean isFolderAdmin, final int contextId, final Connection writeCon) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement(SQL_INSERT_SYSTEM_PERMISSION);
            stmt.setInt(1, contextId);
            stmt.setInt(2, fuid);
            stmt.setInt(3, userId);
            stmt.setInt(4, allPerms[0]);
            stmt.setInt(5, allPerms[1]);
            stmt.setInt(6, allPerms[2]);
            stmt.setInt(7, allPerms[3]);
            stmt.setInt(8, isFolderAdmin ? 1 : 0);
            stmt.setInt(9, 0);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;
        } finally {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        }
    }

    private static void createSinglePermission(final int fuid, final OCLPermission addMe, final int contextId, final Connection writeCon) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement(SQL_INSERT_SYSTEM_PERMISSION);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, fuid);
            stmt.setInt(pos++, addMe.getEntity());
            stmt.setInt(pos++, addMe.getFolderPermission());
            stmt.setInt(pos++, addMe.getReadPermission());
            stmt.setInt(pos++, addMe.getWritePermission());
            stmt.setInt(pos++, addMe.getDeletePermission());
            stmt.setInt(pos++, addMe.isFolderAdmin() ? 1 : 0);
            stmt.setInt(pos++, addMe.isGroupPermission() ? 1 : 0);
            stmt.executeUpdate();
            stmt.close();
            stmt = null;
        } finally {
            if (stmt != null) {
                stmt.close();
                stmt = null;
            }
        }
    }

    private static final String STR_OXFOLDERTREE = "oxfolder_tree";

    private static final String STR_DELOXFOLDERTREE = "del_oxfolder_tree";

    private static final String STR_OXFOLDERPERMS = "oxfolder_permissions";

    private static final String SQL_DELETE_TABLE = "DELETE FROM #TABLE# WHERE cid = ?";

    /**
     * Deletes all context associated folder in both working and backup tables from database
     *
     * @param contextId The context identifier
     * @param readCon A readable connection
     * @param writeCon A writeable connection
     */
    public void deleteAllContextFolders(final int contextId, final Connection readCon, final Connection writeCon) {
        try {
            final Set<String> oxfolderTables = new HashSet<String>();
            final Set<String> delOxfolderTables = new HashSet<String>();
            final DatabaseMetaData databaseMetaData = readCon.getMetaData();
            ResultSet rs = null;
            try {
                rs = databaseMetaData.getTables(null, null, "oxfolder_%", null);
                while (rs.next() && rs.getString(4).equals("TABLE")) {
                    oxfolderTables.add(rs.getString(3));
                }
            } finally {
                Databases.closeSQLStuff(rs);
            }
            try {
                rs = databaseMetaData.getTables(null, null, "del_oxfolder_%", null);
                while (rs.next() && rs.getString(4).equals("TABLE")) {
                    delOxfolderTables.add(rs.getString(3));
                }
            } finally {
                Databases.closeSQLStuff(rs);
            }
            /*
             * Remove root tables
             */
            final String rootTable = STR_OXFOLDERTREE;
            final String delRootTable = STR_DELOXFOLDERTREE;
            oxfolderTables.remove(rootTable);
            delOxfolderTables.remove(delRootTable);
            /*
             * Delete tables with constraints to root tables
             */
            final boolean performCommit = writeCon.getAutoCommit();
            if (performCommit) {
                writeCon.setAutoCommit(false);
            }
            final String tableReplaceLabel = STR_TABLE;
            PreparedStatement stmt = null;
            try {
                int size = oxfolderTables.size();
                Iterator<String> iter = oxfolderTables.iterator();
                for (int i = 0; i < size; i++) {
                    final String tblName = iter.next();
                    stmt = writeCon.prepareStatement(Strings.replaceSequenceWith(SQL_DELETE_TABLE, tableReplaceLabel, tblName));
                    stmt.setInt(1, contextId);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt = null;
                }
                size = delOxfolderTables.size();
                iter = delOxfolderTables.iterator();
                for (int i = 0; i < size; i++) {
                    final String tblName = iter.next();
                    stmt = writeCon.prepareStatement(Strings.replaceSequenceWith(SQL_DELETE_TABLE, tableReplaceLabel, tblName));
                    stmt.setInt(1, contextId);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt = null;
                }
                stmt = writeCon.prepareStatement(Strings.replaceSequenceWith(SQL_DELETE_TABLE, tableReplaceLabel, rootTable));
                stmt.setInt(1, contextId);
                stmt.executeUpdate();
                stmt.close();
                stmt = null;
                stmt = writeCon.prepareStatement(Strings.replaceSequenceWith(SQL_DELETE_TABLE, tableReplaceLabel, delRootTable));
                stmt.setInt(1, contextId);
                stmt.executeUpdate();
                stmt.close();
                stmt = null;
                if (performCommit) {
                    writeCon.commit();
                    writeCon.setAutoCommit(true);
                }
            } finally {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                if (performCommit) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
            }
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    private static final String SQL_UPDATE_FOLDER_TIMESTAMP = "UPDATE #FT# AS ot SET ot.changing_date = ? WHERE ot.cid = ? AND ot.fuid = ?";

    // GROUP BY CLAUSE: ensure ONLY_FULL_GROUP_BY compatibility
    private static final String SQL_SELECT_FOLDER_IN_PERMISSIONS =
        "SELECT ot.fuid FROM #FT# AS ot JOIN #PT# as op ON ot.fuid = op.fuid AND ot.cid = ? AND op.cid = ? WHERE op.permission_id = ? GROUP BY ot.fuid";

    /**
     * Propagates that a group has been modified throughout affected folders by touching folder's last-modified timestamp.
     *
     * @param group The affected group identifier
     * @param readCon A readable connection to database
     * @param writeCon A writable connection to database
     * @param contextId The context identifier
     * @return true in case the write connection has been used, otherwise false
     * @throws SQLException If a SQL related error occurs
     */
    public static boolean propagateGroupModification(final int group, final Connection readCon, final Connection writeCon, final int contextId) throws SQLException {
        try {
            final int[] members = ServerServiceRegistry.getServize(GroupService.class, true).getGroup(ContextStorage.getStorageContext(contextId), group).getMember();
            for (final int member : members) {
                CacheFolderStorage.getInstance().clearCache(member, contextId);
            }
        } catch (Exception e) {
            // Ignore
            LOG.error("", e);
        }
        /*
         * Update last-modified time stamp
         */
        final long lastModified = System.currentTimeMillis();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            /*
             * Touch all folder timestamps in whose permissions the group's entity identifier occurs
             */
            stmt =
                readCon.prepareStatement(Strings.replaceSequenceWith(Strings.replaceSequenceWith(SQL_SELECT_FOLDER_IN_PERMISSIONS, "#FT#", STR_OXFOLDERTREE), "#PT#", STR_OXFOLDERPERMS));
            stmt.setInt(1, contextId);
            stmt.setInt(2, contextId);
            stmt.setInt(3, group);
            rs = stmt.executeQuery();
            final TIntList list = new TIntArrayList();
            while (rs.next()) {
                list.add(rs.getInt(1));
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
            if (!list.isEmpty()) {
                stmt = writeCon.prepareStatement(Strings.replaceSequenceWith(SQL_UPDATE_FOLDER_TIMESTAMP, "#FT#", STR_OXFOLDERTREE));
                do {
                    final int fuid = list.removeAt(0);
                    stmt.setLong(1, lastModified);
                    stmt.setInt(2, contextId);
                    stmt.setInt(3, fuid);
                    stmt.addBatch();
                    if (FolderCacheManager.isInitialized()) {
                        /*
                         * Remove from cache
                         */
                        try {
                            FolderCacheManager.getInstance().removeFolderObject(fuid, new ContextImpl(contextId));
                        } catch (OXException e) {
                            LOG.error("Folder could not be removed from cache", e);
                        }
                    }
                } while (!list.isEmpty());
                stmt.executeBatch();
                return true;
            }
            return false;
        } finally {
            closeResources(rs, stmt, null, true, contextId);
        }
    }

    private static final String SQL_SELECT_DISPLAY_NAME = "SELECT field01 FROM prg_contacts WHERE cid = ? AND userid = ?;";

    /**
     * Load specified user's display name with given connection (which is possibly in non-auto-commit mode). Thus we obtain most up-to-date
     * data and do not read old value.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param con The connection
     * @return The user's display name or <code>null</code> if no such user exists
     * @throws OXException If user's display name cannot be loaded
     */
    static String getUserDisplayName(final int userId, final int contextId, final Connection con) throws OXException {
        final PreparedStatement stmt;
        try {
            stmt = con.prepareStatement(SQL_SELECT_DISPLAY_NAME);
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        ResultSet rs = null;
        try {
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private static final String DEFAULT_CAL_NAME = "My Calendar";

    private static final String DEFAULT_CON_NAME = "My Contacts";

    private static final String DEFAULT_TASK_NAME = "My Tasks";

    private static final String DEFAULT_INFOSTORE_TRASH_NAME = "Deleted files";

    /**
     * Creates default folders for modules task, calendar, contact, and infostore for given user identifier
     *
     * @param userId The user identifier
     * @param displayName The display name which is taken as folder name for user's default infostore folder
     * @param language User's language which determines the translation of default folder names
     * @param contextId The context identifier
     * @param writeCon A writable connection to (master) database
     * @throws OXException If user's default folders could not be created successfully
     */
    public void addUserToOXFolders(final int userId, final String displayName, final String language, final int contextId, final Connection writeCon, OXFolderDefaultMode folderDefaultMode) throws OXException {
        try {
            Context ctx = new ContextImpl(contextId);
            StringHelper strHelper = StringHelper.valueOf(LocaleTools.getLocale(language));
            /*
             * Check infostore sibling
             */
            if (false == isUniqueInfostoreFolderName(ctx, displayName, writeCon)) {
                /*
                 * Check if folder uniqueness is enforced
                 */
                ConfigViewFactory configViewFactory = ServerServiceRegistry.getInstance().getService(ConfigViewFactory.class);
                ConfigView view = null;
                if (null == configViewFactory || null == (view = configViewFactory.getView(-1, contextId)) || view.opt("com.openexchange.user.enforceUniqueDisplayName", Boolean.class, Boolean.TRUE).booleanValue()) {
                    throw OXFolderExceptionCode.NO_DEFAULT_INFOSTORE_CREATE.create(displayName, FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_NAME, Integer.valueOf(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID), Integer.valueOf(contextId));
                }
            }
            /*
             * Add user to global address book permissions if not present
             */
            final int globalAddressBookId = FolderObject.SYSTEM_LDAP_FOLDER_ID;
            final boolean globalPermEnabled = checkGlobalGABPermissionExistence(contextId, writeCon);
            if (globalPermEnabled) {
                LOG.warn("Individual user permission not added to global address book folder since global permission is active. user={}, context={}", I(userId), I(contextId));
            } else {
                if (!checkPermissionExistence(contextId, globalAddressBookId, userId, writeCon)) {
                    final OCLPermission p = new OCLPermission();
                    p.setEntity(userId);
                    p.setGroupPermission(false);
                    setGABPermissions(p);
                    p.setFolderAdmin(false);
                    createSinglePermission(globalAddressBookId, p, contextId, writeCon);
                }
            }
            /*
             * Proceed
             */
            String defaultCalName = strHelper.getString(FolderStrings.DEFAULT_CALENDAR_FOLDER_NAME);
            if (defaultCalName == null || defaultCalName.length() == 0) {
                defaultCalName = DEFAULT_CAL_NAME;
            }
            String defaultConName = strHelper.getString(FolderStrings.DEFAULT_CONTACT_FOLDER_NAME);
            if (defaultConName == null || defaultCalName.length() == 0) {
                defaultConName = DEFAULT_CON_NAME;
            }
            String defaultTaskName = strHelper.getString(FolderStrings.DEFAULT_TASK_FOLDER_NAME);
            if (defaultTaskName == null || defaultTaskName.length() == 0) {
                defaultTaskName = DEFAULT_TASK_NAME;
            }
            String defaultInfostoreTrashName = strHelper.getString(FolderStrings.SYSTEM_TRASH_FILES_FOLDER_NAME);
            if (defaultInfostoreTrashName == null || defaultInfostoreTrashName.length() == 0) {
                defaultInfostoreTrashName = DEFAULT_INFOSTORE_TRASH_NAME;
            }

            LOG.debug("Folder names determined for default folders:\n\tCalendar={}\tContact={}\tTask={}\tInfostore Trash={}",
                defaultCalName, defaultConName, defaultTaskName, defaultInfostoreTrashName);
            /*
             * Insert default calendar folder
             */
            final long creatingTime = System.currentTimeMillis();
            final OCLPermission defaultPerm = new OCLPermission();
            defaultPerm.setEntity(userId);
            defaultPerm.setGroupPermission(false);
            defaultPerm.setAllPermission(ADMIN_PERMISSION, ADMIN_PERMISSION, ADMIN_PERMISSION, ADMIN_PERMISSION);
            defaultPerm.setFolderAdmin(true);
            final FolderObject fo = new FolderObject();
            fo.setPermissionsAsArray(new OCLPermission[] { defaultPerm });
            fo.setDefaultFolder(true);
            fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
            fo.setType(FolderObject.PRIVATE);
            fo.setFolderName(defaultCalName);
            fo.setModule(FolderObject.CALENDAR);
            int newFolderId = OXFolderSQL.getNextSerialForAdmin(ctx, writeCon);
            OXFolderSQL.insertDefaultFolderSQL(newFolderId, userId, fo, creatingTime, true, ctx, writeCon);
            LOG.debug("User's default CALENDAR folder successfully created");
            /*
             * Insert default contact folder
             */
            fo.setFolderName(defaultConName);
            fo.setModule(FolderObject.CONTACT);
            newFolderId = OXFolderSQL.getNextSerialForAdmin(ctx, writeCon);
            OXFolderSQL.insertDefaultFolderSQL(newFolderId, userId, fo, creatingTime, true, ctx, writeCon);
            LOG.debug("User's default CONTACT folder successfully created");
            /*
             * Insert default contact folder
             */
            fo.setFolderName(defaultTaskName);
            fo.setModule(FolderObject.TASK);
            newFolderId = OXFolderSQL.getNextSerialForAdmin(ctx, writeCon);
            OXFolderSQL.insertDefaultFolderSQL(newFolderId, userId, fo, creatingTime, true, ctx, writeCon);
            LOG.debug("User's default TASK folder successfully created");
            /*
             * Insert default infostore folders
             */
            switch (folderDefaultMode) {
                case NORMAL:
                    InfoStoreFolderAdminHelper.addDefaultFoldersDeletable(writeCon, contextId, userId, LocaleTools.getLocale(language), Optional.of(displayName));
                    break;
                case NONE:
                    InfoStoreFolderAdminHelper.addDefaultFoldersNone(writeCon, contextId, userId, Optional.of(displayName));
                    break;
                default:
                    InfoStoreFolderAdminHelper.addDefaultFolders(writeCon, contextId, userId, Optional.of(displayName));
            }

            LOG.debug("All user default folders were successfully created");
            /*
             * TODO: Set standard special folders (projects, ...) located beneath system user folder
             */
            LOG.info("User {} successfully created in context {}", I(userId), I(contextId));
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Sets the global address book permission dependent on {@link OXFolderProperties#isEnableInternalUsersEdit()} option.
     *
     * @param p The permission instance whose permissions shall be set
     */
    private static void setGABPermissions(final OCLPermission p) {
        setGABPermissions(p, OXFolderProperties.isEnableInternalUsersEdit());
    }

    /**
     * Sets the global address book permission
     *
     * @param p The permission instance whose permissions shall be set
     * @param enable Whether to enable or disable global address book access
     */
    private static void setGABPermissions(final OCLPermission p, boolean enable) {
        p.setAllPermission(READ_FOLDER, READ_ALL_OBJECTS, enable ? WRITE_OWN_OBJECTS : NO_PERMISSIONS, NO_PERMISSIONS);
    }

    /**
     * Get the unique folder name for a user's default folder
     *
     * @param context The {@link Context}
     * @param displayName The display name of the user to user as default folder name
     * @param connection A read connection
     * @return The folder name
     * @throws OXException In case {@link FolderObject#SYSTEM_USER_INFOSTORE_FOLDER_ID} or subfolder can't be loaded
     */
    public static String determineUserstoreFolderName(Context context, String displayName, Connection connection) throws OXException {
        try {
            boolean isUnique = isUniqueInfostoreFolderName(context, displayName, connection);
            if (isUnique) {
                return displayName;

            }
            /*
             * Enhance name until it is unique
             */
            NameBuilder builder = NameBuilder.nameBuilderFor(displayName);
            do {
                isUnique = isUniqueInfostoreFolderName(context, builder.advance().toString(), connection);
            } while (false == isUnique);

            /*
             * Return enhanced name
             */
            return builder.toString();
        } catch (SQLException e) {
            LOG.debug("Unable to check the folder name for uniqueness.", e);
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private static boolean isUniqueInfostoreFolderName(Context context, String displayName, Connection connection) throws OXException, SQLException {
        return -1 == OXFolderSQL.lookUpFolder(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, displayName, FolderObject.INFOSTORE, connection, context);
    }
}
