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

import static com.openexchange.server.impl.OCLPermission.ADMIN_PERMISSION;
import static com.openexchange.server.impl.OCLPermission.ALL_GROUPS_AND_USERS;
import static com.openexchange.server.impl.OCLPermission.ALL_GUESTS;
import static com.openexchange.server.impl.OCLPermission.CREATE_SUB_FOLDERS;
import static com.openexchange.server.impl.OCLPermission.NO_PERMISSIONS;
import static com.openexchange.server.impl.OCLPermission.READ_ALL_OBJECTS;
import static com.openexchange.server.impl.OCLPermission.READ_FOLDER;
import static com.openexchange.server.impl.OCLPermission.WRITE_OWN_OBJECTS;
import static com.openexchange.tools.sql.DBUtils.closeResources;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.cache.impl.FolderQueryCacheManager;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.cache.CacheFolderStorage;
import com.openexchange.folderstorage.outlook.OutlookFolderStorage;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.calendar.CalendarCache;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;
import com.openexchange.tools.sql.DBUtils;

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
     * <b>Note</b>: Succeeds only of specified user ID denotes context's admin
     *
     * @param cid The context ID
     * @param userId The ID of the user for whom the setting shall be checked
     * @param readCon A readable connection
     * @return <code>true</code> if context's admin has administer permission on public folder(s); otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    public boolean isPublicFolderEditable(final int cid, final int userId, final Connection readCon) throws OXException {
        final int admin;
        try {
            admin = getContextAdminID(cid, readCon);
        } catch (final OXException e) {
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
            stmt.setInt(1, cid);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            boolean editable = true;
            boolean foundPermissions = false;
            while (editable && rs.next()) {
                editable &= (rs.getInt(1) > 0);
                foundPermissions = true;
            }
            return editable && foundPermissions;
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Sets if context's admin has administer permission on public folder(s).
     * <p>
     * <b>Note</b>: Succeeds only of specified user ID denotes context's admin
     *
     * @param editable <code>true</code> if context's admin has administer permission on public folder(s); otherwise <code>false</code>
     * @param cid The context ID
     * @param userId The ID of the user for whom the option shall be set
     * @param writeCon A writable connection
     * @throws OXException If an error occurs
     */
    public void setPublicFolderEditable(final boolean editable, final int cid, final int userId, final Connection writeCon) throws OXException {
        final int admin;
        try {
            admin = getContextAdminID(cid, writeCon);
        } catch (final OXException e) {
            LOG.error("", e);
            return;
        }
        for (final int id : CHANGEABLE_PUBLIC_FOLDERS) {
            try {
                /*
                 * Check if folder has already been created for given context
                 */
                if (!checkFolderExistence(cid, id, writeCon)) {
                    createPublicFolder(id, cid, admin, writeCon, System.currentTimeMillis());
                }
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt =
                    writeCon.prepareStatement("SELECT permission_id, admin_flag, system FROM oxfolder_permissions WHERE cid = ? AND fuid = ? AND permission_id = ?");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, id);
                stmt.setInt(pos++, userId);
                rs = stmt.executeQuery();
                final boolean update = rs.next();
                final boolean prevEditable = update && rs.getInt(2) > 0;
                final int system;
                if(update) {
                    system = rs.getInt(3);
                } else {
                    system = -1;
                }
                DBUtils.closeSQLStuff(rs, stmt);
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
                    stmt.setInt(pos++, cid);
                    stmt.setInt(pos++, id);
                    stmt.setInt(pos++, admin);
                    stmt.setInt(pos++, system);
                    stmt.executeUpdate();
                } else {
                    stmt =
                        writeCon.prepareStatement("INSERT INTO oxfolder_permissions (cid, fuid, permission_id, fp, orp, owp, odp, admin_flag, group_flag, system) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    pos = 1;
                    stmt.setInt(pos++, cid); // cid
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
                DBUtils.closeSQLStuff(stmt);
                stmt = null;
                /*
                 * Update last-modified of folder
                 */
                final ContextImpl ctx = new ContextImpl(cid);
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
                        FolderQueryCacheManager.getInstance().invalidateContextQueries(cid);
                    }
                    if (CalendarCache.isInitialized()) {
                        CalendarCache.getInstance().invalidateGroup(cid);
                    }
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
        }
    }

    /**
     * Restores default permissions on global address book folder in given context.
     *
     * @param cid The context ID
     * @param enable Whether to enable or disable global address book access for each user
     * @throws OXException If an error occurs
     */
    public void restoreDefaultGlobalAddressBookPermissions(final int cid, final boolean enable) throws OXException {
        final Connection writeCon = Database.get(cid, true);
        try {
            /*
             * Check if global permission is enabled for global address book folder in current context
             */
            try {
                if (checkGlobalGABPermissionExistence(cid, writeCon)) {
                    /*
                     * Global permission enabled for global address book folder; nothing to be restored for this context.
                     */
                    LOG.warn("Cannot restore individual global address book permissions since global permission is active.");
                    // updateGABWritePermission(cid, enable, writeCon);
                    return;
                }
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
            /*
             * Get context's users
             */
            final TIntList users;
            {
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = writeCon.prepareStatement("SELECT id FROM user WHERE cid = ?");
                    final int pos = 1;
                    stmt.setInt(pos, cid);
                    rs = stmt.executeQuery();
                    users = new TIntLinkedList();
                    while (rs.next()) {
                        users.add(rs.getInt(pos));
                    }
                } catch (final SQLException e) {
                    throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
                } finally {
                    DBUtils.closeSQLStuff(rs, stmt);
                }
            }
            /*
             * Iterate users
             */
            if (!users.isEmpty()) {
                final Integer admin = Integer.valueOf(getContextAdminID(cid, writeCon));
                final int size = users.size();
                for (int i = 1; i < size; i++) {
                    setGlobalAddressBookDisabled(cid, users.get(i), !enable, writeCon, admin, false);
                }
                /*
                 * Propagate with last update
                 */
                setGlobalAddressBookDisabled(cid, users.get(0), !enable, writeCon, admin, true);
            }
        } finally {
            Database.back(cid, true, writeCon);
        }
    }

    /**
     * Checks whether global address book is enabled for specified user.
     *
     * @param cid The context ID
     * @param userId The user ID
     * @param readCon A readable connection
     * @return <code>true</code> if global address book is disabled; otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    public boolean isGlobalAddressBookDisabled(final int cid, final int userId, final Connection readCon) throws OXException {
        /*
         * Check if global permission is enabled for global address book folder
         */
        final int globalAddressBookId = FolderObject.SYSTEM_LDAP_FOLDER_ID;
        try {
            final int[] perms = getPermissionValue(cid, globalAddressBookId, ALL_GROUPS_AND_USERS, readCon);
            if (null != perms) {
                LOG.warn("Cannot look-up individual user permission: Global permission is active on global address book folder.\nReturning global permission instead. user={}, context={}", userId, cid);
                return (perms[0] == NO_PERMISSIONS);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = readCon.prepareStatement("SELECT fp, orp FROM oxfolder_permissions WHERE cid = ? AND fuid = ? AND permission_id = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, globalAddressBookId);
            stmt.setInt(pos++, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == NO_PERMISSIONS; // && rs.getInt(2) >= READ_FOLDER;
            }
            return true;
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Enables/Disables specified user's global address book permission.
     *
     * @param cid The context ID
     * @param userId The user ID
     * @param disable <code>true</code> to enabled user's global address book permission; otherwise <code>false</code>
     * @param writeCon A writable connection
     * @throws OXException If an error occurs
     */
    public void setGlobalAddressBookDisabled(final int cid, final int userId, final boolean disable, final Connection writeCon) throws OXException {
        setGlobalAddressBookDisabled(cid, userId, disable, writeCon, null, true);
    }

    /**
     * Enables/Disables specified user's global address book permission.
     *
     * @param cid The context ID
     * @param userId The user ID
     * @param disable <code>true</code> to enabled user's global address book permission; otherwise <code>false</code>
     * @param writeCon A writable connection
     * @throws OXException If an error occurs
     */
    private void setGlobalAddressBookDisabled(final int cid, final int userId, final boolean disable, final Connection writeCon, final Integer adminId, final boolean propagate) throws OXException {
        final int admin = adminId == null ? getContextAdminID(cid, writeCon) : adminId.intValue();
        final boolean isAdmin = (admin == userId);
        final int globalAddressBookId = FolderObject.SYSTEM_LDAP_FOLDER_ID;
        if (isAdmin) {
            try {
                /*
                 * Check if folder has already been created for given context
                 */
                if (!checkFolderExistence(cid, globalAddressBookId, writeCon)) {
                    createGlobalAddressBook(cid, admin, writeCon, System.currentTimeMillis());
                }
            } catch (final SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        /*
         * Check if global permission is enabled for global address book folder
         */
        try {
            if (checkGlobalGABPermissionExistence(cid, writeCon)) {
                /*
                 * Global permission enabled for global address book folder
                 */
                LOG.warn("Cannot update individual permission on global address book folder since global permission is active. user={}, context={}", userId, cid);
                // updateGABWritePermission(cid, enable, writeCon);
                return;
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = writeCon.prepareStatement("SELECT permission_id,system FROM oxfolder_permissions WHERE cid = ? AND fuid = ? AND permission_id = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, globalAddressBookId);
            stmt.setInt(pos++, userId);
            rs = stmt.executeQuery();
            final boolean update = rs.next();
            final int system = (update) ? rs.getInt(2) : -1;

            DBUtils.closeSQLStuff(rs, stmt);
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
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, globalAddressBookId);
                stmt.setInt(pos++, userId);
                stmt.setInt(pos++, system);
                stmt.executeUpdate();
            } else {
                stmt = writeCon.prepareStatement("INSERT INTO oxfolder_permissions (cid, fuid, permission_id, fp, orp, owp, odp, admin_flag, group_flag, system) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                pos = 1;
                stmt.setInt(pos++, cid); // cid
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
            DBUtils.closeSQLStuff(stmt);
            stmt = null;
            /*
             * Update last-modified of folder
             */
            if (propagate) {
                final ContextImpl ctx = new ContextImpl(cid);
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
                        FolderQueryCacheManager.getInstance().invalidateContextQueries(cid);
                    }
                    if (CalendarCache.isInitialized()) {
                        CalendarCache.getInstance().invalidateGroup(cid);
                    }
                    final CacheService service = ServerServiceRegistry.getInstance().getService(CacheService.class);
                    if (null != service) {
                        final Cache cache = service.getCache("GlobalFolderCache");
                        final String sContextId = Integer.toString(cid);
                        final String[] trees = new String[] { FolderStorage.REAL_TREE_ID, OutlookFolderStorage.OUTLOOK_TREE_ID, "20" };
                        for (final String tid : trees) {
                            final CacheKey cacheKey = service.newCacheKey(1, tid, Integer.toString(globalAddressBookId));
                            cache.removeFromGroup(cacheKey, sContextId);
                        }
                    }
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private static final String STR_TABLE = "#TABLE#";

    /**
     * Creates the standard system folders located in each context for given context in database and creates the default folders for
     * context's admin user by invoking <code>{@link #addUserToOXFolders(int, String, String, int, Connection)}</code>
     *
     * @param cid The context ID
     * @param mailAdminDisplayName The display name of context's admin user
     * @param language The language conforming to syntax rules of locales
     * @param con A writeable connection
     * @throws OXException If system folder could not be created successfully or default folders for context's admin user could not be
     *             created
     */
    public void addContextSystemFolders(final int cid, final String mailAdminDisplayName, final String language, final Connection con) throws OXException {
        try {
            final int contextMalAdmin = getContextAdminID(cid, con);
            addContextSystemFolders(cid, contextMalAdmin, mailAdminDisplayName, language, con);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private static int getContextAdminID(final int cid, final Connection readCon) throws OXException {
        try {
            final int retval = OXFolderSQL.getContextAdminID(new ContextImpl(cid), readCon);
            if (retval == -1) {
                throw OXFolderExceptionCode.NO_ADMIN_USER_FOUND_IN_CONTEXT.create(Integer.valueOf(cid));
            }
            return retval;
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private void addContextSystemFolders(final int cid, final int mailAdmin, final String mailAdminDisplayName, final String language, final Connection writeCon) throws SQLException, OXException {
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
            cid,
            writeCon);
        final OCLPermission guestPermission = new OCLPermission();
        guestPermission.setEntity(ALL_GUESTS);
        guestPermission.setGroupPermission(true);
        guestPermission.setFolderAdmin(false);
        guestPermission.setAllPermission(READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        createSinglePermission(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, guestPermission, cid, writeCon);
        /*
         * Insert system public folder
         */
        if (!checkFolderExistence(cid, FolderObject.SYSTEM_PUBLIC_FOLDER_ID, writeCon)) {
            createSystemPublicFolder(cid, mailAdmin, writeCon, creatingTime);
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
            cid,
            writeCon);
        guestPermission.setAllPermission(READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        createSinglePermission(FolderObject.SYSTEM_SHARED_FOLDER_ID, guestPermission, cid, writeCon);
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
            cid,
            writeCon);
        /*
         * Insert system infostore folder
         */
        if (!checkFolderExistence(cid, FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, writeCon)) {
            createSystemInfostoreFolder(cid, mailAdmin, writeCon, creatingTime);
        }
        /*
         * Insert system system_global folder aka 'Shared Address Book'
         */
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
            cid,
            writeCon);
        /*
         * Insert system internal users folder aka 'Global Address Book'
         */
        // TODO: Whether to enable/disable internal-user-edit should be set by
        // caller (admin) as a parameter
        final int globalAddressBookId = FolderObject.SYSTEM_LDAP_FOLDER_ID;
        if (checkFolderExistence(cid, globalAddressBookId, writeCon)) {
            final ContextImpl ctx = new ContextImpl(cid);
            ctx.setMailadmin(mailAdmin);
            try {
                OXFolderSQL.updateLastModified(globalAddressBookId, creatingTime, mailAdmin, writeCon, ctx);
            } catch (final OXException e) {
                // Cannot occur
                LOG.error("", e);
            }
        } else {
            createGlobalAddressBook(cid, mailAdmin, writeCon, creatingTime);
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
            cid,
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
            cid,
            writeCon);
        guestPermission.setAllPermission(READ_FOLDER, READ_ALL_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
        createSinglePermission(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, guestPermission, cid, writeCon);
        /*
         * Insert system public infostore folder
         */
        if (!checkFolderExistence(cid, FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID, writeCon)) {
            createSystemPublicInfostoreFolder(cid, mailAdmin, writeCon, creatingTime);
        }
        LOG.info("All System folders successfully created for context {}", cid);
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
            cid,
            writeCon);
         */
        /*
         * Grant full access for 'Shared Address Book' to admin
         */
        createSingleUserPermission(
            FolderObject.SYSTEM_GLOBAL_FOLDER_ID,
            mailAdmin,
            new int[] { ADMIN_PERMISSION, ADMIN_PERMISSION, ADMIN_PERMISSION, ADMIN_PERMISSION },
            true,
            cid,
            writeCon);
        if (ADMIN_EDITABLE) {
            /*
             * Grant admin access for public infostore folder to admin
             */
            createSingleUserPermission(
                FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID,
                mailAdmin,
                new int[] { CREATE_SUB_FOLDERS, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS },
                true,
                cid,
                writeCon);
            /*
             * Grant admin access for user infostore folder to admin
             */
            createSingleUserPermission(
                FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID,
                mailAdmin,
                new int[] { READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS },
                true,
                cid,
                writeCon);
        }
        addUserToOXFolders(mailAdmin, mailAdminDisplayName, language, cid, writeCon);
        LOG.info("Folder rights for mail admin successfully added for context {}", cid);
    }

    /**
     * Creates a public system folder with default permission for all-groups-and-users entity.
     *
     * @param cid The context ID
     * @param mailAdmin The context admin
     * @param writeCon A writable connection
     * @param creatingTime The creation date
     * @throws SQLException If a SQL error occurs
     */
    private static void createPublicFolder(final int folderId, final int cid, final int mailAdmin, final Connection writeCon, final long creatingTime) throws SQLException {
        if (FolderObject.SYSTEM_PUBLIC_FOLDER_ID == folderId) {
            createSystemPublicFolder(cid, mailAdmin, writeCon, creatingTime);
        } else if (FolderObject.SYSTEM_INFOSTORE_FOLDER_ID == folderId) {
            createSystemInfostoreFolder(cid, mailAdmin, writeCon, creatingTime);
        } else if (FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID == folderId) {
            createSystemPublicInfostoreFolder(cid, mailAdmin, writeCon, creatingTime);
        } else {
            throw new IllegalArgumentException("Specified folder ID is not a public folder ID: " + folderId);
        }
    }

    /**
     * Creates the public folder with default permission for all-groups-and-users entity.
     *
     * @param cid The context ID
     * @param mailAdmin The context admin
     * @param writeCon A writable connection
     * @param creatingTime The creation date
     * @throws SQLException If a SQL error occurs
     */
    private static void createSystemPublicFolder(final int cid, final int mailAdmin, final Connection writeCon, final long creatingTime) throws SQLException {
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
            cid,
            writeCon);
        final OCLPermission guestPermission = new OCLPermission();
        guestPermission.setEntity(OCLPermission.ALL_GUESTS);
        guestPermission.setGroupPermission(true);
        guestPermission.setFolderAdmin(false);
        guestPermission.setAllPermission(READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        createSinglePermission(FolderObject.SYSTEM_PUBLIC_FOLDER_ID, guestPermission, cid, writeCon);

    }

    /**
     * Creates the infostore folder with default permission for all-groups-and-users entity.
     *
     * @param cid The context ID
     * @param mailAdmin The context admin
     * @param writeCon A writable connection
     * @param creatingTime The creation date
     * @throws SQLException If a SQL error occurs
     */
    private static void createSystemInfostoreFolder(final int cid, final int mailAdmin, final Connection writeCon, final long creatingTime) throws SQLException {
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
            cid,
            writeCon);
        final OCLPermission guestPermission = new OCLPermission();
        guestPermission.setEntity(OCLPermission.ALL_GUESTS);
        guestPermission.setGroupPermission(true);
        guestPermission.setFolderAdmin(false);
        guestPermission.setAllPermission(READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        createSinglePermission(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, guestPermission, cid, writeCon);
    }

    /**
     * Creates the public infostore folder with default permission for all-groups-and-users entity.
     *
     * @param cid The context ID
     * @param mailAdmin The context admin
     * @param writeCon A writable connection
     * @param creatingTime The creation date
     * @throws SQLException If a SQL error occurs
     */
    private static void createSystemPublicInfostoreFolder(final int cid, final int mailAdmin, final Connection writeCon, final long creatingTime) throws SQLException {
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
            cid,
            writeCon);
        final OCLPermission guestPermission = new OCLPermission();
        guestPermission.setEntity(ALL_GUESTS);
        guestPermission.setGroupPermission(true);
        guestPermission.setFolderAdmin(false);
        guestPermission.setAllPermission(READ_FOLDER, NO_PERMISSIONS, NO_PERMISSIONS, NO_PERMISSIONS);
        createSinglePermission(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID, guestPermission, cid, writeCon);
    }

    /**
     * Checks permission existence for all-groups-abd-users on GAB folder in given context.
     *
     * @param cid The context ID
     * @param con A connection
     * @return <code>true</code> if a permission exists; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    private static boolean checkGlobalGABPermissionExistence(final int cid, final Connection con) throws SQLException {
        return checkPermissionExistence(cid, FolderObject.SYSTEM_LDAP_FOLDER_ID, OCLPermission.ALL_GROUPS_AND_USERS, con);
    }

    /**
     * Checks permission existence in given folder in given context for given user.
     *
     * @param cid The context ID
     * @param folderId The folder ID
     * @param userId The user ID
     * @param con A connection
     * @return <code>true</code> if a permission exists; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    private static boolean checkPermissionExistence(final int cid, final int folderId, final int userId, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT permission_id FROM oxfolder_permissions WHERE cid = ? AND fuid = ? AND permission_id = ?");
            stmt.setInt(1, cid);
            stmt.setInt(2, folderId);
            stmt.setInt(3, userId);
            return (rs = stmt.executeQuery()).next();
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Checks permission values for folder, object-read, object-write and object-delete permission.
     *
     * @param cid The context ID
     * @param folderId The folder ID
     * @param entityId The entity ID
     * @param con A connection
     * @return The permissions as an array or <code>null</code>
     * @throws SQLException If a SQL error occurs
     */
    private static int[] getPermissionValue(final int cid, final int folderId, final int entityId, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt =
                con.prepareStatement("SELECT fp, orp, owp, odp FROM oxfolder_permissions WHERE cid = ? AND fuid = ? AND permission_id = ?");
            stmt.setInt(1, cid);
            stmt.setInt(2, folderId);
            stmt.setInt(3, entityId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return new int[] { rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(4) };
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Checks existence of given folder in given context.
     *
     * @param cid The context ID
     * @param folderId The folder ID
     * @param con A connection
     * @return <code>true</code> if exists; otherwise <code>false</code>
     * @throws SQLException If a SQL error occurs
     */
    private static boolean checkFolderExistence(final int cid, final int folderId, final Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("SELECT fuid FROM oxfolder_tree WHERE cid = ? AND fuid = ?");
            stmt.setInt(1, cid);
            stmt.setInt(2, folderId);
            return stmt.executeQuery().next();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * Creates the global address book folder.
     *
     * @param cid The context ID
     * @param mailAdmin The ID of context's admin
     * @param writeCon A writable connection
     * @param creatingTime The creating time
     * @throws SQLException If a SQL error occurs
     */
    private void createGlobalAddressBook(final int cid, final int mailAdmin, final Connection writeCon, final long creatingTime) throws SQLException {
        final OCLPermission adminPermission = new OCLPermission();
        adminPermission.setEntity(mailAdmin);
        adminPermission.setGroupPermission(false);
        setGABPermissions(adminPermission);
        adminPermission.setFolderAdmin(true);
        createSystemFolder(
            FolderObject.SYSTEM_LDAP_FOLDER_ID,
            FolderObject.SYSTEM_LDAP_FOLDER_NAME,
            adminPermission,
            FolderObject.SYSTEM_FOLDER_ID,
            FolderObject.CONTACT,
            true,
            creatingTime,
            mailAdmin,
            false,
            cid,
            writeCon);
    }

    private final static String SQL_INSERT_SYSTEM_FOLDER =
        "INSERT INTO oxfolder_tree (fuid, cid, parent, fname, module, type, creating_date, created_from, changing_date, changed_from, permission_flag, subfolder_flag) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String SQL_INSERT_SYSTEM_PERMISSION =
        "INSERT INTO oxfolder_permissions (cid, fuid, permission_id, fp, orp, owp, odp, admin_flag, group_flag) VALUES (?,?,?,?,?,?,?,?,?)";

    private static final String SQL_INSERT_SPECIAL_FOLDER = "INSERT INTO oxfolder_specialfolders " + "(tag, cid, fuid) VALUES (?,?,?)";

    private static void createSystemFolder(final int systemFolderId, final String systemFolderName, final OCLPermission systemPermission, final int parentId, final int module, final boolean insertIntoSpecialFolders, final long creatingTime, final int mailAdminId, final boolean isPublic, final int cid, final Connection writeCon) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement(SQL_INSERT_SYSTEM_FOLDER);
            stmt.setInt(1, systemFolderId);
            stmt.setInt(2, cid);
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
            stmt = writeCon.prepareStatement(SQL_INSERT_SYSTEM_PERMISSION);
            stmt.setInt(1, cid);
            stmt.setInt(2, systemFolderId); // fuid
            stmt.setInt(3, systemPermission.getEntity()); // entity
            stmt.setInt(4, systemPermission.getFolderPermission()); // folder
            // permission
            stmt.setInt(5, systemPermission.getReadPermission()); // read
            // permission
            stmt.setInt(6, systemPermission.getWritePermission()); // write
            // permission
            stmt.setInt(7, systemPermission.getDeletePermission()); // delete
            // permission
            stmt.setInt(8, systemPermission.isFolderAdmin() ? 1 : 0); // admin_flag
            stmt.setInt(9, systemPermission.isGroupPermission() ? 1 : 0); // group_flag
            stmt.executeUpdate();
            stmt.close();
            stmt = null;
            if (insertIntoSpecialFolders) {
                stmt = writeCon.prepareStatement(SQL_INSERT_SPECIAL_FOLDER);
                stmt.setString(1, systemFolderName); // tag
                stmt.setInt(2, cid); // cid
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

    private void createSingleUserPermission(final int fuid, final int userId, final int[] allPerms, final boolean isFolderAdmin, final int cid, final Connection writeCon) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement(SQL_INSERT_SYSTEM_PERMISSION);
            stmt.setInt(1, cid);
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

    private static void createSinglePermission(final int fuid, final OCLPermission addMe, final int cid, final Connection writeCon) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = writeCon.prepareStatement(SQL_INSERT_SYSTEM_PERMISSION);
            int pos = 1;
            stmt.setInt(pos++, cid);
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
     * @param cid The context ID
     * @param readCon A readable connection
     * @param writeCon A writeable connection
     */
    public void deleteAllContextFolders(final int cid, final Connection readCon, final Connection writeCon) {
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
                closeSQLStuff(rs);
            }
            try {
                rs = databaseMetaData.getTables(null, null, "del_oxfolder_%", null);
                while (rs.next() && rs.getString(4).equals("TABLE")) {
                    delOxfolderTables.add(rs.getString(3));
                }
            } finally {
                closeSQLStuff(rs);
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
                    stmt = writeCon.prepareStatement(SQL_DELETE_TABLE.replaceFirst(tableReplaceLabel, tblName));
                    stmt.setInt(1, cid);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt = null;
                }
                size = delOxfolderTables.size();
                iter = delOxfolderTables.iterator();
                for (int i = 0; i < size; i++) {
                    final String tblName = iter.next();
                    stmt = writeCon.prepareStatement(SQL_DELETE_TABLE.replaceFirst(tableReplaceLabel, tblName));
                    stmt.setInt(1, cid);
                    stmt.executeUpdate();
                    stmt.close();
                    stmt = null;
                }
                stmt = writeCon.prepareStatement(SQL_DELETE_TABLE.replaceFirst(tableReplaceLabel, rootTable));
                stmt.setInt(1, cid);
                stmt.executeUpdate();
                stmt.close();
                stmt = null;
                stmt = writeCon.prepareStatement(SQL_DELETE_TABLE.replaceFirst(tableReplaceLabel, delRootTable));
                stmt.setInt(1, cid);
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
        } catch (final Exception e) {
            LOG.error("", e);
        }
    }

    private static final String SQL_UPDATE_FOLDER_TIMESTAMP = "UPDATE #FT# AS ot SET ot.changing_date = ? WHERE ot.cid = ? AND ot.fuid = ?";

    private static final String SQL_SELECT_FOLDER_IN_PERMISSIONS =
        "SELECT ot.fuid FROM #FT# AS ot JOIN #PT# as op ON ot.fuid = op.fuid AND ot.cid = ? AND op.cid = ? WHERE op.permission_id = ? GROUP BY ot.fuid";

    /**
     * Propagates that a group has been modified throughout affected folders by touching folder's last-modified timestamp.
     *
     * @param group The affected group ID
     * @param readCon A readable connection to database
     * @param writeCon A writable connection to database
     * @param cid The context ID
     * @throws SQLException If a SQL related error occurs
     */
    public static void propagateGroupModification(final int group, final Connection readCon, final Connection writeCon, final int cid) throws SQLException {
        try {
            final int[] members = GroupStorage.getInstance().getGroup(group, ContextStorage.getStorageContext(cid)).getMember();
            for (final int member : members) {
                CacheFolderStorage.getInstance().clearCache(member, cid);
            }
        } catch (final Exception e) {
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
                readCon.prepareStatement(SQL_SELECT_FOLDER_IN_PERMISSIONS.replaceFirst("#FT#", STR_OXFOLDERTREE).replaceFirst(
                    "#PT#",
                    STR_OXFOLDERPERMS));
            stmt.setInt(1, cid);
            stmt.setInt(2, cid);
            stmt.setInt(3, group);
            rs = stmt.executeQuery();
            final TIntList list = new TIntArrayList();
            while (rs.next()) {
                list.add(rs.getInt(1));
            }
            closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
            if (!list.isEmpty()) {
                stmt = writeCon.prepareStatement(SQL_UPDATE_FOLDER_TIMESTAMP.replaceFirst("#FT#", STR_OXFOLDERTREE));
                do {
                    final int fuid = list.removeAt(0);
                    stmt.setLong(1, lastModified);
                    stmt.setInt(2, cid);
                    stmt.setInt(3, fuid);
                    stmt.addBatch();
                    if (FolderCacheManager.isInitialized()) {
                        /*
                         * Remove from cache
                         */
                        try {
                            FolderCacheManager.getInstance().removeFolderObject(fuid, new ContextImpl(cid));
                        } catch (final OXException e) {
                            LOG.error("Folder could not be removed from cache", e);
                        }
                    }
                } while (!list.isEmpty());
                stmt.executeBatch();
            }
        } finally {
            closeResources(rs, stmt, null, true, cid);
        }
    }

    /**
     * Propagates modifications <b>already</b> performed on an existing user throughout folder module.
     *
     * @param userId The ID of the user who has been modified
     * @param changedFields The changed fields of the user taken from constants defined in {@link Contact}; e.g.
     *            {@link Contact#DISPLAY_NAME}
     * @param lastModified The last modified timestamp that should be taken on folder modifications
     * @param readCon A readable connection if a writable connection should not be used for read access to database
     * @param writeCon A writable connection
     * @param cid The context ID
     * @throws OXException If user's modifications could not be successfully propagated
     */
    public static void propagateUserModification(final int userId, final int[] changedFields, final long lastModified, final Connection readCon, final Connection writeCon, final int cid) throws OXException {
        Arrays.sort(changedFields);
        final int adminID = getContextAdminID(cid, writeCon);
        if (Arrays.binarySearch(changedFields, Contact.DISPLAY_NAME) > -1) {
            propagateDisplayNameModification(userId, lastModified, adminID, readCon, writeCon, cid);
        }
    }

    private static void propagateDisplayNameModification(final int userId, final long lastModified, final int contextAdminID, final Connection readCon, final Connection writeCon, final int cid) throws OXException {
        final ContextImpl ctx = new ContextImpl(cid);
        ctx.setMailadmin(contextAdminID);
        /*
         * Update shared folder's last modified timestamp
         */
        try {
            OXFolderSQL.updateLastModified(FolderObject.SYSTEM_SHARED_FOLDER_ID, lastModified, contextAdminID, writeCon, ctx);
            /*
             * Reload cache entry
             */
            if (FolderCacheManager.isInitialized()) {
                /*
                 * Distribute remove among remote caches
                 */
                FolderCacheManager.getInstance().removeFolderObject(FolderObject.SYSTEM_SHARED_FOLDER_ID, ctx);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * Update user's default infostore folder name
         */
        try {
            final int defaultInfostoreFolderId = OXFolderSQL.getUserDefaultFolder(userId, FolderObject.INFOSTORE, readCon, ctx);
            final String newDisplayName = getUserDisplayName(userId, cid, readCon == null ? writeCon : readCon);
            if (newDisplayName == null) {
                throw LdapExceptionCode.USER_NOT_FOUND.create(LdapExceptionCode.USER_NOT_FOUND,
                    Integer.valueOf(userId),
                    Integer.valueOf(cid)).setPrefix(EnumComponent.USER.getAbbreviation());
            }
            OXFolderSQL.updateName(defaultInfostoreFolderId, newDisplayName, lastModified, contextAdminID, writeCon, ctx);
            /*
             * Reload cache entry
             */
            if (FolderCacheManager.isInitialized()) {
                /*
                 * Distribute remove among remote caches
                 */
                FolderCacheManager.getInstance().removeFolderObject(defaultInfostoreFolderId, ctx);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private static final String SQL_SELECT_DISPLAY_NAME = "SELECT field01 FROM prg_contacts WHERE cid = ? AND userid = ?;";

    /**
     * Load specified user's display name with given connection (which is possibly in non-auto-commit mode). Thus we obtain most up-to-date
     * data and do not read old value.
     *
     * @param userId The user ID
     * @param cid The context ID
     * @param con The connection
     * @return The user's display name or <code>null</code> if no such user exists
     * @throws OXException If user's display name cannot be loaded
     */
    static String getUserDisplayName(final int userId, final int cid, final Connection con) throws OXException {
        final PreparedStatement stmt;
        try {
            stmt = con.prepareStatement(SQL_SELECT_DISPLAY_NAME);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        ResultSet rs = null;
        try {
            stmt.setInt(1, cid);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString(1);
            }
            return null;
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    private static final String DEFAULT_CAL_NAME = "My Calendar";

    private static final String DEFAULT_CON_NAME = "My Contacts";

    private static final String DEFAULT_TASK_NAME = "My Tasks";

    private static final String DEFAULT_INFOSTORE_TRASH_NAME = "Deleted files";

    /**
     * Creates default folders for modules task, calendar, contact, and infostore for given user ID
     *
     * @param userId The user ID
     * @param displayName The display name which is taken as folder name for user's default infostore folder
     * @param language User's language which determines the translation of default folder names
     * @param cid The context ID
     * @param writeCon A writable connection to (master) database
     * @throws OXException If user's default folders could not be created successfully
     */
    public void addUserToOXFolders(final int userId, final String displayName, final String language, final int cid, final Connection writeCon) throws OXException {
        try {
            final Context ctx = new ContextImpl(cid);
            final StringHelper strHelper = StringHelper.valueOf(LocaleTools.getLocale(language));
            /*
             * Check infostore sibling
             */
            if (OXFolderSQL.lookUpFolder(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, displayName, FolderObject.INFOSTORE, writeCon, ctx) != -1) {
                throw OXFolderExceptionCode.NO_DEFAULT_INFOSTORE_CREATE.create(displayName,
                    FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_NAME,
                    Integer.valueOf(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID),
                    Integer.valueOf(ctx.getContextId()));
            }
            /*
             * Add user to global address book permissions if not present
             */
            final int globalAddressBookId = FolderObject.SYSTEM_LDAP_FOLDER_ID;
            final boolean globalPermEnabled = checkGlobalGABPermissionExistence(cid, writeCon);
            if (globalPermEnabled) {
                LOG.warn("Individual user permission not added to global address book folder since global permission is active. user={}, context={}", userId, cid);
            } else {
                if (!checkPermissionExistence(cid, globalAddressBookId, userId, writeCon)) {
                    final OCLPermission p = new OCLPermission();
                    p.setEntity(userId);
                    p.setGroupPermission(false);
                    setGABPermissions(p);
                    p.setFolderAdmin(false);
                    createSinglePermission(globalAddressBookId, p, cid, writeCon);
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
            LOG.info("Folder names determined for default folders:\n\tCalendar={}\tContact={}\tTask={}\tInfostore Trash={}",
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
            OXFolderSQL.insertDefaultFolderSQL(newFolderId, userId, fo, creatingTime, ctx, writeCon);
            LOG.info("User's default CALENDAR folder successfully created");
            /*
             * Insert default contact folder
             */
            fo.setFolderName(defaultConName);
            fo.setModule(FolderObject.CONTACT);
            newFolderId = OXFolderSQL.getNextSerialForAdmin(ctx, writeCon);
            OXFolderSQL.insertDefaultFolderSQL(newFolderId, userId, fo, creatingTime, ctx, writeCon);
            LOG.info("User's default CONTACT folder successfully created");
            /*
             * Insert default contact folder
             */
            fo.setFolderName(defaultTaskName);
            fo.setModule(FolderObject.TASK);
            newFolderId = OXFolderSQL.getNextSerialForAdmin(ctx, writeCon);
            OXFolderSQL.insertDefaultFolderSQL(newFolderId, userId, fo, creatingTime, ctx, writeCon);
            LOG.info("User's default TASK folder successfully created");
            /*
             * Insert default infostore folders
             */
            InfoStoreFolderAdminHelper.addDefaultFolders(writeCon, cid, userId);

            LOG.info("All user default folders were successfully created");
            /*
             * TODO: Set standard special folders (projects, ...) located beneath system user folder
             */
            LOG.info("User {} successfully created in context {}", userId, cid);
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Sets the global address book permission dependent on {@link OXFolderProperties#isEnableInternalUsersEdit()} option.
     *
     * @param p The permission instance whose permissions shall be set
     */
    private static void setGABPermissions(final OCLPermission p) {
        p.setAllPermission(READ_FOLDER, READ_ALL_OBJECTS, OXFolderProperties.isEnableInternalUsersEdit() ? WRITE_OWN_OBJECTS : NO_PERMISSIONS, NO_PERMISSIONS);
    }

}
