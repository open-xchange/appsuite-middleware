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

import static com.openexchange.tools.oxfolder.OXFolderUtility.folderModule2String;
import static com.openexchange.tools.sql.DBUtils.closeResources;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.contact.ContactService;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Tasks;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * OXFolderTools
 *
 * @deprecated - use routines available in <code>OXFolderAccess</code>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Deprecated
public class OXFolderTools {

    private OXFolderTools() {
        super();
    }

    private static final String STR_EMPTY = "";

    /**
     * Determines folder type explicitly from underlying database storage. The returned value is either <code>FolderObject.PRIVATE</code>,
     * <code>FolderObject.PUBLIC</code> or <code>FolderObject.SHARED</code>
     */
    public static int getFolderTypeFromDB(final int folderId, final int userId, final UserConfiguration userConfig, final Context ctx) throws OXException {
        return getFolderTypeFromDB(folderId, userId, userConfig, ctx, null);
    }

    /**
     * Determines folder type explicitly from underlying database storage. The returned value is either <code>FolderObject.PRIVATE</code>,
     * <code>FolderObject.PUBLIC</code> or <code>FolderObject.SHARED</code>
     */
    public static int getFolderTypeFromDB(final int folderId, final int userId, final UserConfiguration userConfig, final Context ctx, final Connection readCon) throws OXException {
        try {
            final FolderObject fo;
            if (FolderCacheManager.isEnabled()) {
                fo = FolderCacheManager.getInstance().getFolderObject(folderId, false, ctx, readCon);
            } else {
                fo = FolderObject.loadFolderObjectFromDB(folderId, ctx, readCon);
            }
            if (!fo.getEffectiveUserPermission(userId, userConfig).isFolderVisible()) {
                throw OXFolderExceptionCode.NOT_VISIBLE.create(Integer.valueOf(folderId), Integer.valueOf(userId), Integer.valueOf(ctx.getContextId()));
            }
            return fo.getType(userId);
        } catch (final RuntimeException e) {
            throw OXFolderExceptionCode.FOLDER_COULD_NOT_BE_LOADED.create(Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), e);
        } catch (final OXException e) {
            throw OXFolderExceptionCode.FOLDER_COULD_NOT_BE_LOADED.create(Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), e);
        }
    }

    /**
     * Returns user's effective permission (including user configuration) on given folder
     */
    public static EffectivePermission getEffectiveFolderOCL(final int folderId, final int userId, final int[] groups, final Context ctx, final UserConfiguration userConfig, final Connection con) throws OXException {
        return getEffectiveFolderOCL(folderId, userId, groups, ctx, userConfig, con, true);
    }

    /**
     * Returns user's effective permission (including user configuration) on given folder
     */
    public static EffectivePermission getEffectiveFolderOCL(final int folderId, final int userId, final int[] groups, final Context ctx, final UserConfiguration userConfig) throws OXException {
        return getEffectiveFolderOCL(folderId, userId, groups, ctx, userConfig, null);
    }

    /**
     * Returns user's effective permission (including user configuration) on given folder
     */
    public static EffectivePermission getEffectiveFolderOCL(final int folderId, final int userId, final int[] groups, final Context ctx, final UserConfiguration userConfig, final Connection con, final boolean fromCache) throws OXException {
        /*
         * Look up in cache
         */
        if (fromCache && FolderCacheManager.isEnabled()) {
            final FolderObject folderObj = new OXFolderAccess(con, ctx).getFolderObject(folderId);
            if (folderObj != null) {
                try {
                    return folderObj.getEffectiveUserPermission(userId, userConfig);
                } catch (final RuntimeException e) {
                    throw OXFolderExceptionCode.FOLDER_COULD_NOT_BE_LOADED.create(e, Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), e);
                } catch (final OXException e) {
                    throw OXFolderExceptionCode.FOLDER_COULD_NOT_BE_LOADED.create(e, Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()), e);
                }
            }
        }
        /*
         * Fetch from storage
         */
        final EffectivePermission retval = new EffectivePermission(userId, folderId, getFolderTypeFromDB(folderId, userId, userConfig, ctx, con), getFolderModule(folderId, ctx, con), getFolderOwner(folderId, ctx, con), userConfig);
        retval.setEntity(userId);
        retval.setAllPermission(OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
        Connection readCon = con;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            try {
                final String permissionIds = StringCollection.getSqlInString(userId, groups);
                final String sqlSelectStr = "SELECT fp, orp, owp, odp, admin_flag, group_flag FROM oxfolder_permissions WHERE (cid = ?) AND (fuid = ?) " + "AND permission_id IN " + permissionIds;
                if (readCon == null) {
                    readCon = DBPool.pickup(ctx);
                    closeCon = true;
                }
                stmt = readCon.prepareStatement(sqlSelectStr);
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                rs = stmt.executeQuery();

                int fp_highest = 0;
                int orp_highest = 0;
                int owp_highest = 0;
                int odp_highest = 0;
                boolean folderAdmin = false;
                while (rs.next()) {
                    /*
                     * Determine Folder Permission
                     */
                    final int fp = rs.getInt(1);
                    if (!rs.wasNull()) {
                        fp_highest = Math.max(fp_highest, fp);
                    }
                    /*
                     * Determine Object Read Permission
                     */
                    final int orp = rs.getInt(2);
                    if (!rs.wasNull()) {
                        orp_highest = Math.max(orp_highest, orp);
                    }
                    /*
                     * Determine Object Write Permission
                     */
                    final int owp = rs.getInt(3);
                    if (!rs.wasNull()) {
                        owp_highest = Math.max(owp_highest, owp);
                    }
                    /*
                     * Determine Object Delete Permission
                     */
                    final int odp = rs.getInt(4);
                    if (!rs.wasNull()) {
                        odp_highest = Math.max(odp_highest, odp);
                    }
                    /*
                     * Set FolderAdmin if not set to true before
                     */
                    if (!folderAdmin) {
                        folderAdmin = rs.getInt(5) > 0 ? true : false;
                    }
                }
                if (!retval.setAllPermission(fp_highest, orp_highest, owp_highest, odp_highest)) {
                    throw OXFolderExceptionCode.NO_EFFECTIVE_PERMISSION.create(Integer.valueOf(folderId), Integer.valueOf(userId), Integer.valueOf(ctx.getContextId()));
                }
                retval.setFolderAdmin(folderAdmin);
            } finally {
                closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException t) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        return retval;
    }

    /**
     * Returns the folder id of user's default folder in given module
     */
    public static int getDefaultFolder(final int user, final int module, final Context ctx) throws OXException {
        return getDefaultFolder(user, module, ctx, null);
    }

    /**
     * Returns the folder id of user's default folder in given module
     */
    public static int getDefaultFolder(final int user, final int module, final Context ctx, final Connection con) throws OXException {
        Connection readCon = con;
        boolean closeCon = false;
        int retval = 0;
        try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                if (readCon == null) {
                    readCon = DBPool.pickup(ctx);
                    closeCon = true;
                }
                if (module == FolderObject.CALENDAR || module == FolderObject.TASK || module == FolderObject.CONTACT || module == FolderObject.INFOSTORE) {
                    final String sqlSelectStr = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND created_from = ? AND default_flag = ? AND module = ?";
                    stmt = readCon.prepareStatement(sqlSelectStr);
                    stmt.setInt(1, ctx.getContextId());
                    stmt.setInt(2, user);
                    stmt.setInt(3, 1);
                    stmt.setInt(4, module);
                } else {
                    final String sqlSelectStr = "SELECT fuid FROM oxfolder_userfolders_standardfolders WHERE cid = ? AND created_from = ? AND module = ?";
                    stmt = readCon.prepareStatement(sqlSelectStr);
                    stmt.setInt(1, ctx.getContextId());
                    stmt.setInt(2, user);
                    stmt.setInt(3, module);
                }
                rs = stmt.executeQuery();
                if (rs.next()) {
                    retval = rs.getInt(1);
                }
            } finally {
                closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException t) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        if (retval != 0) {
            return retval;
        }
        throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_FOUND.create(folderModule2String(module), user, Integer.valueOf(ctx.getContextId()));
    }

    /**
     * Returns user's contact default folder id
     */
    public static int getContactDefaultFolder(final int user, final Context ctx) throws OXException {
        return getDefaultFolder(user, ctx, null, FolderObject.CONTACT);
    }

    /**
     * Returns user's contact default folder id
     */
    public static int getContactDefaultFolder(final int user, final Context ctx, final Connection con) throws OXException {
        return getDefaultFolder(user, ctx, con, FolderObject.CONTACT);
    }

    /**
     * Returns user's task default folder id
     */
    public static int getTaskDefaultFolder(final int user, final Context ctx) throws OXException {
        return getDefaultFolder(user, ctx, null, FolderObject.TASK);
    }

    /**
     * Returns user's task default folder id
     */
    public static int getTaskDefaultFolder(final int user, final Context ctx, final Connection con) throws OXException {
        return getDefaultFolder(user, ctx, con, FolderObject.TASK);
    }

    /**
     * Returns user's calendar default folder id
     */
    public static int getCalendarDefaultFolder(final int user, final Context ctx) throws OXException {
        return getDefaultFolder(user, ctx, null, FolderObject.CALENDAR);
    }

    /**
     * Returns user's calendar default folder id
     */
    public static int getCalendarDefaultFolder(final int user, final Context ctx, final Connection con) throws OXException {
        return getDefaultFolder(user, ctx, con, FolderObject.CALENDAR);
    }

    /**
     * Returns user's infostore default folder id
     */
    public static int getInfostoreDefaultFolder(final int user, final Context ctx) throws OXException {
        return getDefaultFolder(user, ctx, null, FolderObject.INFOSTORE);
    }

    /**
     * Returns user's infostore default folder id
     */
    public static int getInfostoreDefaultFolder(final int user, final Context ctx, final Connection con) throws OXException {
        return getDefaultFolder(user, ctx, con, FolderObject.INFOSTORE);
    }

    private static final String SQL_SEL_DEFFLD = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND created_from = ? AND default_flag = ? AND module = ?";

    /**
     * Returns user's default folder id for given standard module (either task, calendar or contact)
     */
    private static final int getDefaultFolder(final int user, final Context ctx, final Connection readConArg, final int module) throws OXException {
        int retval = 0;
        Connection readCon = readConArg;
        boolean closeCon = false;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeCon = true;
            }
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = readCon.prepareStatement(SQL_SEL_DEFFLD);
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, user);
                stmt.setInt(3, 1);
                stmt.setInt(4, module);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    retval = rs.getInt(1);
                    if (rs.wasNull()) {
                        throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_FOUND.create(folderModule2String(module), user, Integer.valueOf(ctx.getContextId()));
                    }
                } else {
                    throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_FOUND.create(folderModule2String(module), user, ctx, Integer.valueOf(ctx.getContextId()));
                }
            } finally {
                closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException t) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        return retval;
    }

    /**
     * Determines if folder is of module calendar
     */
    public static boolean isFolderCalendar(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        final FolderObject folderObj = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
        return (folderObj != null && folderObj.getModule() == FolderObject.CALENDAR);
    }

    /**
     * Determines if folder is of module contact
     */
    public static boolean isFolderContact(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        final FolderObject folderObj = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
        return (folderObj != null && folderObj.getModule() == FolderObject.CONTACT);
    }

    /**
     * Determines if folder is of module task
     */
    public static boolean isFolderTask(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        final FolderObject folderObj = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
        return (folderObj != null && folderObj.getModule() == FolderObject.TASK);
    }

    /**
     * Determines if folder is private
     */
    public static boolean isFolderPrivate(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        final FolderObject folderObj = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
        return (folderObj != null && folderObj.getType() == FolderObject.PRIVATE);
    }

    /**
     * Determines if folder is public
     */
    public static boolean isFolderPublic(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        final FolderObject folderObj = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
        return (folderObj != null && folderObj.getType() == FolderObject.PUBLIC);
    }

    /**
     * Determines if folder is a shared folder for given user. <b>NOTE:</b> This method assumes that given user has read access!
     */
    public static boolean isFolderShared(final int folderId, final String user, final Context ctx, final Connection readCon) throws OXException {
        return isFolderShared(folderId, Integer.parseInt(user), ctx, readCon);
    }

    /**
     * Determines if folder is a shared folder for given user. <b>NOTE:</b> This method assumes that given user has read access!
     */
    public static boolean isFolderShared(final int folderId, final int user, final Context ctx, final Connection readCon) throws OXException {
        final FolderObject folderObj = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
        return (folderObj != null && (folderObj.getType() == FolderObject.PRIVATE && folderObj.getCreator() != user));
    }

    /**
     * Returns the folder module
     */
    public static int getFolderModule(final int folderId, final Context ctx) throws OXException {
        return getFolderModule(folderId, ctx, null);
    }

    /**
     * Returns the folder module
     */
    public static int getFolderModule(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        final FolderObject folderObj = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
        return folderObj.getModule();
    }

    /**
     * Returns the folder type: either private, public or shared
     */
    public static int getFolderType(final int folderId, final int user, final Context ctx) throws OXException {
        return getFolderType(folderId, user, ctx, null);
    }

    /**
     * Returns the folder type: either private, public or shared
     */
    public static int getFolderType(final int folderId, final int user, final Context ctx, final Connection readCon) throws OXException {
        final FolderObject folderObj = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
        return folderObj.isShared(user) ? FolderObject.SHARED : folderObj.getType();
    }

    /**
     * Determines if folder type is <code>FoldeObject.PRIVATE</code> or <code>FoldeObject.PUBLIC</code>. <b>NOTE:</b> This method does NOT
     * examine if folder is shared.
     */
    public static int getFolderType(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        final FolderObject folderObj = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
        return folderObj.getType();
    }

    /**
     * Returns folder's owner id (or similary its creator id)
     */
    public static int getFolderOwner(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        final FolderObject folderObj = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
        return folderObj.getCreator();
    }

    /**
     * Returns folder's default flag
     */
    public static boolean getFolderDefaultFlag(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        final FolderObject folderObj = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
        return folderObj.isDefaultFolder();
    }

    /**
     * Returns folder's default flag
     */
    public static String getFolderName(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        final FolderObject folderObj = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
        return folderObj.getFolderName();
    }

    /**
     * Returns folder's parent id
     */
    public static int getFolderParent(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        final FolderObject folderObj = new OXFolderAccess(readCon, ctx).getFolderObject(folderId);
        return folderObj.getParentFolderID();
    }

    /**
     * Returns the core sql statement to query user-visible folders. This query can be further parametrized by additional conditions (e.g.
     * only folders of a certain type or module)
     */
    private final static String getSQLUserVisibleFolders(final String fields, final String permissionIds, final String accessibleModules, final String additionalCondition, final String groupBy, final String orderBy) {
        final StringBuilder retValBuilder = new StringBuilder("SELECT ").append(fields).append(" FROM oxfolder_tree AS ot ").append("JOIN oxfolder_permissions AS op ON ot.fuid = op.fuid AND ot.cid = ? AND op.cid = ? ").append("WHERE (((ot.permission_flag = ").append(FolderObject.PRIVATE_PERMISSION).append(" AND ot.created_from = ?)) OR ").append("((op.admin_flag = 1 AND op.permission_id = ?) OR (op.fp > ").append(OCLPermission.NO_PERMISSIONS).append(" AND op.permission_id IN ").append(permissionIds).append(")))");
        if (OXFolderProperties.isIgnoreSharedAddressbook()) {
            retValBuilder.append(" AND (ot.fuid !=").append(FolderObject.SYSTEM_GLOBAL_FOLDER_ID).append(')');
        }
        if (accessibleModules != null) {
            retValBuilder.append(" AND (ot.module IN ").append(accessibleModules).append(')');
        }
        if (additionalCondition != null) {
            retValBuilder.append(' ').append(additionalCondition);
        }
        if (groupBy != null) {
            retValBuilder.append(' ').append(groupBy);
        }
        if (orderBy != null) {
            retValBuilder.append(' ').append(orderBy);
        }
        return retValBuilder.toString();
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances, which represent all user-visible root folders
     */
    public static SearchIterator getUserRootFoldersIterator(final int userId, final int[] memberInGroups, final int[] accessibleModules, final Context ctx) throws OXException, SearchIteratorException {
        final String sqlSelectStr = getSQLUserVisibleFolders(FolderObjectIterator.getFieldsForSQL("ot"), StringCollection.getSqlInString(userId, memberInGroups), StringCollection.getSqlInString(accessibleModules), "AND (ot.type = ?) AND (ot.parent = ?)", OXFolderProperties.isEnableDBGrouping() ? "GROUP BY ot.fuid" : null, "ORDER by ot.fuid");
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = DBPool.pickup(ctx);
            stmt = readCon.prepareStatement(sqlSelectStr);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, FolderObject.SYSTEM_TYPE);
            stmt.setInt(6, 0);
            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw e;
        } catch (final RuntimeException t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        return new FolderObjectIterator(rs, stmt, true, ctx, readCon, true);
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances, which represent all user-visible sub folders of a
     * certain parent folder.
     */
    public static SearchIterator getVisibleSubfoldersIterator(final int parentFolderId, final int userId, final int[] groups, final Context ctx, final UserConfiguration userConfig, final Timestamp since) throws SQLException, OXException, OXException, SearchIteratorException {
        if (parentFolderId == FolderObject.SYSTEM_PRIVATE_FOLDER_ID) {
            return getVisiblePrivateFolders(userId, groups, userConfig.getAccessibleModules(), ctx, since);
        } else if (parentFolderId == FolderObject.SYSTEM_PUBLIC_FOLDER_ID) {
            return getVisiblePublicFolders(userId, groups, userConfig.getAccessibleModules(), ctx, since);
        } else if (parentFolderId == FolderObject.SYSTEM_SHARED_FOLDER_ID) {
            return getVisibleSharedFolders(userId, groups, userConfig.getAccessibleModules(), ctx, since);
        } else {
            /*
             * Check user's effective permission on subfolder's parent
             */
            final FolderObject parentFolder = new OXFolderAccess(ctx).getFolderObject(parentFolderId);
            final OCLPermission effectivePerm = parentFolder.getEffectiveUserPermission(userId, userConfig);
            if (effectivePerm.getFolderPermission() < OCLPermission.READ_FOLDER) {
                return SearchIteratorAdapter.emptyIterator();
            }
            return getVisibleSubfoldersIterator(parentFolder, userId, groups, userConfig.getAccessibleModules(), ctx, since);
        }
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances which are located beneath system's private folder.
     */
    private static SearchIterator getVisiblePrivateFolders(final int userId, final int[] groups, final int[] accessibleModules, final Context ctx, final Timestamp since) throws OXException, SearchIteratorException {
        final StringBuilder condBuilder = new StringBuilder("AND (ot.type = ").append(FolderObject.PRIVATE).append(" AND ot.created_from = ").append(userId).append(") AND (ot.parent = ?)").append((since == null ? STR_EMPTY : " AND (changing_date > ?)"));
        final String sqlSelectStr = getSQLUserVisibleFolders(FolderObjectIterator.getFieldsForSQL("ot"), StringCollection.getSqlInString(userId, groups), StringCollection.getSqlInString(accessibleModules), condBuilder.toString(), OXFolderProperties.isEnableDBGrouping() ? "GROUP BY ot.fuid" : null, "ORDER by ot.fuid");
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = DBPool.pickup(ctx);
            stmt = readCon.prepareStatement(sqlSelectStr);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
            if (since != null) {
                stmt.setLong(6, since.getTime());
            }
            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw e;
        } catch (final RuntimeException t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, true);
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances which are located beneath system's public folder.
     */
    private static SearchIterator getVisiblePublicFolders(final int userId, final int[] groups, final int[] accessibleModules, final Context ctx, final Timestamp since) throws OXException, SearchIteratorException {
        final StringBuilder condBuilder = new StringBuilder("AND (ot.type = ").append(FolderObject.PUBLIC).append(") AND (ot.parent = ?)").append((since == null ? STR_EMPTY : " AND (changing_date > ?)"));
        final String sqlSelectStr = getSQLUserVisibleFolders(FolderObjectIterator.getFieldsForSQL("ot"), StringCollection.getSqlInString(userId, groups), StringCollection.getSqlInString(accessibleModules), condBuilder.toString(), OXFolderProperties.isEnableDBGrouping() ? "GROUP BY ot.fuid" : null, "ORDER by ot.fuid");
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = DBPool.pickup(ctx);
            stmt = readCon.prepareStatement(sqlSelectStr);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
            if (since != null) {
                stmt.setLong(6, since.getTime());
            }
            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw e;
        } catch (final RuntimeException t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, true);
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances which offer a share right for given user and therefore
     * should appear right beneath system's shared folder in displayed folder tree.
     */
    private static SearchIterator getVisibleSharedFolders(final int userId, final int[] groups, final int[] accessibleModules, final Context ctx, final Timestamp since) throws OXException, SearchIteratorException {
        return getVisibleSharedFolders(userId, groups, accessibleModules, -1, ctx, since);
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances which are located beneath given parent folder.
     */
    private static SearchIterator getVisibleSubfoldersIterator(final FolderObject parentFolder, final int userId, final int[] memberInGroups, final int[] accessibleModules, final Context ctx, final Timestamp since) throws OXException, SearchIteratorException {
        final boolean shared = parentFolder.isShared(userId);
        final StringBuilder condBuilder = new StringBuilder();
        if (shared) {
            condBuilder.append("AND (ot.type = ").append(FolderObject.PRIVATE).append(" AND ot.created_from != ").append(userId).append(") ");
        }
        condBuilder.append("AND (ot.parent = ?)").append((since == null ? STR_EMPTY : " AND (changing_date > ?)"));
        final String sqlSelectStr = getSQLUserVisibleFolders(FolderObjectIterator.getFieldsForSQL("ot"), StringCollection.getSqlInString(userId, memberInGroups), StringCollection.getSqlInString(accessibleModules), condBuilder.toString(), OXFolderProperties.isEnableDBGrouping() ? "GROUP BY ot.fuid" : null, "ORDER by ot.fuid");
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = DBPool.pickup(ctx);
            stmt = readCon.prepareStatement(sqlSelectStr);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, parentFolder.getObjectID());
            if (since != null) {
                stmt.setLong(6, since.getTime());
            }
            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw e;
        } catch (final RuntimeException t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, true);
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances of user-visible shared folders.
     */
    public static SearchIterator getVisibleSharedFolders(final int userId, final int[] memberInGroups, final int[] accessibleModules, final int owner, final Context ctx, final Timestamp since) throws OXException, SearchIteratorException {
        final StringBuilder condBuilder = new StringBuilder("AND (ot.type = ").append(FolderObject.PRIVATE).append(" AND ot.created_from != ").append(userId).append(')');
        if (owner > -1) {
            condBuilder.append(" AND (ot.created_from = ").append(owner).append(')');
        }
        condBuilder.append(since == null ? STR_EMPTY : " AND (changing_date > ?)");
        final String sqlSelectStr = getSQLUserVisibleFolders(FolderObjectIterator.getFieldsForSQL("ot"), StringCollection.getSqlInString(userId, memberInGroups), StringCollection.getSqlInString(accessibleModules), condBuilder.toString(), OXFolderProperties.isEnableDBGrouping() ? "GROUP BY ot.fuid" : null, "ORDER BY ot.fuid");
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = DBPool.pickup(ctx);
            stmt = readCon.prepareStatement(sqlSelectStr);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            if (since != null) {
                stmt.setLong(5, since.getTime());
            }
            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw e;
        } catch (final RuntimeException t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, true);
    }

    public static SearchIterator getAllVisibleFoldersNotSeenInTreeView(final int userId, final int[] groups, final UserConfiguration userConfig, final Context ctx) throws OXException, SearchIteratorException {
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = DBPool.pickup(ctx);
            /*
             * Following statement is not very performant, but at least it works as it should. I didn't find a working one using joins.
             */
            final StringBuilder sql = new StringBuilder(1000);
            sql.append("SELECT ").append(FolderObjectIterator.getFieldsForSQL("ot"));
            sql.append(" FROM oxfolder_tree AS ot JOIN oxfolder_permissions AS op ON ot.fuid = op.fuid AND ot.cid = ? AND op.cid = ?");
            sql.append(" WHERE ((ot.permission_flag = ").append(FolderObject.PUBLIC_PERMISSION);
            sql.append(") OR (ot.permission_flag = ").append(FolderObject.PRIVATE_PERMISSION).append(" AND ot.created_from = ?)");
            sql.append(" OR (op.admin_flag = 1 AND op.permission_id = ?) OR (op.fp > 0 AND op.permission_id IN ");
            sql.append(StringCollection.getSqlInString(userId, groups)).append(")) AND ot.parent IN (");
            sql.append("SELECT res.fuid FROM oxfolder_tree AS res WHERE res.cid = ? AND res.fuid NOT IN (");
            sql.append("SELECT ot2.fuid FROM oxfolder_tree AS ot2 JOIN oxfolder_permissions AS op2 ON ot2.fuid = op2.fuid AND ot2.cid = ? AND op2.cid = ?");
            sql.append(" WHERE (ot2.permission_flag = ").append(FolderObject.PUBLIC_PERMISSION);
            sql.append(") OR (ot2.permission_flag = ").append(FolderObject.PRIVATE_PERMISSION).append(" AND ot2.created_from = ?)");
            sql.append(" OR (op2.admin_flag = 1 AND op2.permission_id = ?) OR (op2.fp > 0 AND op2.permission_id IN ");
            sql.append(StringCollection.getSqlInString(userId, groups)).append("))) AND ot.type = ");
            sql.append(FolderObject.PUBLIC).append(" AND ot.module IN ");
            sql.append(StringCollection.getSqlInString(userConfig.getAccessibleModules()));
            sql.append(" GROUP BY ot.fuid ORDER BY ot.module, ot.fuid");
            stmt = readCon.prepareStatement(sql.toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, ctx.getContextId());
            stmt.setInt(6, ctx.getContextId());
            stmt.setInt(7, ctx.getContextId());
            stmt.setInt(8, userId);
            stmt.setInt(9, userId);
            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw e;
        } catch (final RuntimeException t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, true);
    }

    /**
     * Returns visible non-shared folders of given module that are not visible in hierarchic tree-view (because any ancestor folder is not
     * visible)
     */
    public static SearchIterator getVisibleFoldersNotSeenInTreeView(final int userId, final int[] groups, final int module, final UserConfiguration userConfig, final Context ctx) throws OXException, SearchIteratorException {
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = DBPool.pickup(ctx);
            final StringBuilder sb = new StringBuilder();
            sb.append("SELECT ").append(FolderObjectIterator.getFieldsForSQL("ot")).append(" FROM oxfolder_tree AS ot ").append("LEFT JOIN oxfolder_permissions AS op ON ot.fuid = op.fuid ").append(" AND ot.cid = ? AND op.cid = ? ").append("WHERE ((ot.permission_flag = ").append(FolderObject.PUBLIC_PERMISSION).append(") OR (ot.permission_flag = ").append(FolderObject.PRIVATE_PERMISSION).append(" AND ot.created_from = ?) OR (op.admin_flag = 1 AND op.permission_id = ?) ").append(" OR (op.fp > 0 AND op.permission_id IN ").append(StringCollection.getSqlInString(userId, groups)).append("))").append(" AND ot.parent NOT IN (SELECT ot2.fuid FROM oxfolder_tree AS ot2 LEFT JOIN oxfolder_permissions AS op2 ON ot2.fuid = op2.fuid ").append(" AND ot2.cid = ? AND op2.cid = ? ").append(" WHERE ((ot2.permission_flag = ").append(FolderObject.PUBLIC_PERMISSION).append(") OR (ot2.permission_flag = ").append(FolderObject.PRIVATE_PERMISSION).append(" AND ot2.created_from = ?)").append(" OR (op2.admin_flag = 1 AND op2.permission_id = ?) ").append(" OR (op2.fp > 0 AND op2.permission_id IN ").append(StringCollection.getSqlInString(userId, groups)).append(")) AND ot2.type != ").append(FolderObject.PRIVATE).append(") AND ot.type != ").append(FolderObject.PRIVATE).append(" AND ot.module = ").append(module).append(" AND ot.module IN ").append(StringCollection.getSqlInString(userConfig.getAccessibleModules())).append(OXFolderProperties.isEnableDBGrouping() ? " GROUP BY ot.fuid" : STR_EMPTY);
            stmt = readCon.prepareStatement(sb.toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, ctx.getContextId());
            stmt.setInt(6, ctx.getContextId());
            stmt.setInt(7, userId);
            stmt.setInt(8, userId);
            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw e;
        } catch (final RuntimeException t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, true);
    }

    /**
     * Returns a <code>SearchIterator</code> of <code>FolderObject</code> which represent all visible folders lying on path from given
     * folder to root folder.
     */
    public static SearchIterator getFoldersOnPathToRoot(final int folderId, final int userId, final UserConfiguration userConfig, final Locale locale, final Context ctx) throws OXException, SearchIteratorException {
        final List<FolderObject> folderList = new ArrayList<FolderObject>();
        fillAncestor(folderList, folderId, userId, userConfig, locale, null, ctx);
        return new FolderObjectIterator(folderList, false);
    }

    private static void fillAncestor(final List<FolderObject> folderList, final int folderId, final int userId, final UserConfiguration userConfig, final Locale locale, final UserStorage userStoreArg, final Context ctx) throws OXException {
        if (checkForSpecialFolder(folderList, folderId, locale, ctx)) {
            return;
        }
        UserStorage userStore = userStoreArg;
        FolderObject fo = new OXFolderAccess(ctx).getFolderObject(folderId);
        try {
            if (!fo.getEffectiveUserPermission(userId, userConfig).isFolderVisible()) {
                if (folderList.isEmpty()) {
                    /*
                     * Starting folder is not visible to user
                     */
                    throw OXFolderExceptionCode.NOT_VISIBLE.create(Integer.valueOf(folderId), Integer.valueOf(userId), Integer.valueOf(ctx.getContextId()));
                }
                return;
            }
            if (fo.isShared(userId)) {
                folderList.add(fo);
                /*
                 * Shared: Create virtual folder named to folder owner
                 */
                if (userStore == null) {
                    userStore = UserStorage.getInstance();
                }
                String creatorDisplayName;
                try {
                    creatorDisplayName = userStore.getUser(fo.getCreatedBy(), ctx).getDisplayName();
                } catch (final OXException e) {
                    if (fo.getCreatedBy() != OCLPermission.ALL_GROUPS_AND_USERS) {
                        throw e;
                    }
                    final StringHelper strHelper = StringHelper.valueOf(locale);
                    creatorDisplayName = strHelper.getString(Groups.ALL_USERS);
                }
                final FolderObject virtualOwnerFolder = FolderObject.createVirtualFolderObject("u:" + fo.getCreatedBy(), creatorDisplayName, FolderObject.SYSTEM_MODULE, true, FolderObject.SYSTEM_TYPE);
                folderList.add(virtualOwnerFolder);
                /*
                 * Set folder to system shared folder
                 */
                fo = new OXFolderAccess(ctx).getFolderObject(FolderObject.SYSTEM_SHARED_FOLDER_ID);
                fo.setFolderName(FolderObject.getFolderString(FolderObject.SYSTEM_SHARED_FOLDER_ID, locale));
                folderList.add(fo);
                return;
            } else if (fo.getType() == FolderObject.PUBLIC && hasNonVisibleParent(fo, userId, userConfig, ctx)) {
                /*
                 * Insert current folder
                 */
                folderList.add(fo);
                final int virtualParent;
                switch (fo.getModule()) {
                    case FolderObject.TASK:
                        virtualParent = FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID;
                        break;
                    case FolderObject.CALENDAR:
                        virtualParent = FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID;
                        break;
                    case FolderObject.CONTACT:
                        virtualParent = FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID;
                        break;
                    case FolderObject.INFOSTORE:
                        virtualParent = FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID;
                        break;
                    default:
                        throw OXFolderExceptionCode.UNKNOWN_MODULE.create(folderModule2String(fo.getModule()), Integer.valueOf(ctx.getContextId()));
                }
                checkForSpecialFolder(folderList, virtualParent, locale, ctx);
                return;
            }
            /*
             * Add folder to path
             */
            folderList.add(fo);
            /*
             * Follow ancestors to root
             */
            if (fo.getParentFolderID() != FolderObject.SYSTEM_ROOT_FOLDER_ID) {
                fillAncestor(folderList, fo.getParentFolderID(), userId, userConfig, locale, userStore, ctx);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            throw e;
        } catch (final RuntimeException t) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
    }

    private static final boolean checkForSpecialFolder(final List<FolderObject> folderList, final int folderId, final Locale locale, final Context ctx) throws OXException {
        final boolean publicParent;
        final FolderObject specialFolder;
        switch (folderId) {
            case FolderObject.SYSTEM_LDAP_FOLDER_ID:
                specialFolder = new OXFolderAccess(ctx).getFolderObject(folderId);
                specialFolder.setFolderName(FolderObject.getFolderString(FolderObject.SYSTEM_LDAP_FOLDER_ID, locale));
                publicParent = true;
                break;
            case FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID:
                specialFolder = FolderObject.createVirtualFolderObject(FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID, FolderObject.getFolderString(FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID, locale), FolderObject.SYSTEM_MODULE, true, FolderObject.SYSTEM_TYPE);
                publicParent = true;
                break;
            case FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID:
                specialFolder = FolderObject.createVirtualFolderObject(FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID, FolderObject.getFolderString(FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID, locale), FolderObject.SYSTEM_MODULE, true, FolderObject.SYSTEM_TYPE);
                publicParent = true;
                break;
            case FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID:
                specialFolder = FolderObject.createVirtualFolderObject(FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID, FolderObject.getFolderString(FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID, locale), FolderObject.SYSTEM_MODULE, true, FolderObject.SYSTEM_TYPE);
                publicParent = true;
                break;
            case FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID:
                specialFolder = FolderObject.createVirtualFolderObject(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, FolderObject.getFolderString(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, locale), FolderObject.SYSTEM_MODULE, true, FolderObject.SYSTEM_TYPE);
                publicParent = false;
                break;
            default:
                return false;
        }
        folderList.add(specialFolder);
        final int parentId = publicParent ? FolderObject.SYSTEM_PUBLIC_FOLDER_ID : FolderObject.SYSTEM_INFOSTORE_FOLDER_ID;
        /*
         * Parent
         */
        final FolderObject parent = new OXFolderAccess(ctx).getFolderObject(parentId);
        parent.setFolderName(FolderObject.getFolderString(parentId, locale));
        folderList.add(parent);
        return true;
    }

    private static final boolean hasNonVisibleParent(final FolderObject fo, final int userId, final UserConfiguration userConf, final Context ctx) throws OXException, OXException, SQLException {
        if (fo.getParentFolderID() == FolderObject.SYSTEM_ROOT_FOLDER_ID) {
            return false;
        }
        final FolderObject parent = new OXFolderAccess(ctx).getFolderObject(fo.getParentFolderID());
        return !parent.getEffectiveUserPermission(userId, userConf).isFolderVisible();
    }

    /**
     * Returns a <code>SearchIterator</code> of <code>FolderObject</code> instances, which represent all user-visible folders of a certain
     * type regardless of their parent folder.
     */
    public static SearchIterator getAllVisibleFoldersIteratorOfType(final int userId, final int[] memberInGroups, final int[] accessibleModules, final int type, final int[] modules, final Context ctx) throws OXException, SearchIteratorException {
        return getAllVisibleFoldersIteratorOfType(userId, memberInGroups, accessibleModules, type, modules, null, ctx);
    }

    /**
     * Returns a <code>SearchIterator</code> of <code>FolderObject</code> instances, which represent all user-visible folders of a certain
     * type and a certain parent folder.
     */
    public static SearchIterator getAllVisibleFoldersIteratorOfType(final int userId, final int[] memberInGroups, final int[] accessibleModules, final int type, final int[] modules, final int parent, final Context ctx) throws OXException, SearchIteratorException {
        return getAllVisibleFoldersIteratorOfType(userId, memberInGroups, accessibleModules, type, modules, Integer.valueOf(parent), ctx);
    }

    /**
     * Returns a <code>SearchIterator</code> of <code>FolderObject</code> instances, which represent all user-visible folders of a certain
     * type and a certain parent folder.
     */
    private static SearchIterator getAllVisibleFoldersIteratorOfType(final int userId, final int[] memberInGroups, final int[] accessibleModules, final int type, final int[] modules, final Integer parent, final Context ctx) throws OXException, SearchIteratorException {
        final StringBuilder condBuilder = new StringBuilder("AND (ot.module IN (");
        condBuilder.append(modules[0]);
        for (int i = 1; i < modules.length; i++) {
            condBuilder.append(", ").append(modules[i]);
        }
        condBuilder.append(")) AND (ot.type = ?");
        if (type == FolderObject.SHARED) {
            condBuilder.append(" AND ot.created_from != ").append(userId);
        }
        condBuilder.append(')');
        if (parent != null) {
            condBuilder.append(" AND (ot.parent = ").append(parent.intValue()).append(')');
        }
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = DBPool.pickup(ctx);
            stmt = readCon.prepareStatement(getSQLUserVisibleFolders(FolderObjectIterator.getFieldsForSQL("ot"), StringCollection.getSqlInString(userId, memberInGroups), StringCollection.getSqlInString(accessibleModules), condBuilder.toString(), OXFolderProperties.isEnableDBGrouping() ? "GROUP BY ot.fuid" : null, "ORDER BY ot.fuid"));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, type == FolderObject.SHARED ? FolderObject.PRIVATE : type);
            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw e;
        } catch (final RuntimeException t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, true);
    }

    public static SearchIterator getAllVisibleFoldersIteratorOfModule(final int userId, final int[] memberInGroups, final int[] accessibleModules, final int module, final Context ctx) throws OXException, SearchIteratorException {
        return getAllVisibleFoldersIteratorOfModule(userId, memberInGroups, accessibleModules, module, null, ctx);
    }

    /**
     * Returns a <code>SearchIterator</code> of <code>FolderObject</code> instances of a certain module
     */
    public static SearchIterator getAllVisibleFoldersIteratorOfModule(final int userId, final int[] memberInGroups, final int[] accessibleModules, final int module, final Connection readConArg, final Context ctx) throws OXException, SearchIteratorException {
        final String sqlSelectStr = getSQLUserVisibleFolders(FolderObjectIterator.getFieldsForSQL("ot"), StringCollection.getSqlInString(userId, memberInGroups), StringCollection.getSqlInString(accessibleModules), "AND (ot.module = ?)", OXFolderProperties.isEnableDBGrouping() ? "GROUP BY ot.fuid" : null, "ORDER BY ot.fuid");
        final Connection readCon;
        final boolean closeReadCon = (readConArg == null);
        if (closeReadCon) {
            readCon = DBPool.pickup(ctx);
        } else {
            readCon = readConArg;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = readCon.prepareStatement(sqlSelectStr);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, module);
            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, closeReadCon);
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances which represent user-visible deleted folders since a
     * given date.
     */
    public static SearchIterator getDeletedFoldersSince(final Date since, final int userId, final int[] memberInGroups, final int[] accessibleModules, final Context ctx) throws OXException, SearchIteratorException {
        final String fields = FolderObjectIterator.getFieldsForSQL("ot");
        final StringBuilder sqlBuilder = new StringBuilder("SELECT ").append(fields).append(" FROM del_oxfolder_tree AS ot JOIN del_oxfolder_permissions AS op ON ot.fuid = op.fuid AND ot.cid = ? AND op.cid = ? ").append("WHERE ((ot.permission_flag = ").append(FolderObject.PUBLIC_PERMISSION).append(" OR (ot.permission_flag = ").append(FolderObject.PRIVATE_PERMISSION).append(" AND ot.created_from = ?)) OR ").append("((op.admin_flag = 1 AND op.permission_id = ?) OR (op.fp > ? AND op.permission_id IN ").append(StringCollection.getSqlInString(userId, memberInGroups)).append("))) AND (changing_date > ?)").append(" AND (ot.module IN ").append(StringCollection.getSqlInString(accessibleModules)).append(')').append(OXFolderProperties.isEnableDBGrouping() ? " GROUP BY ot.fuid" : STR_EMPTY).append(" ORDER by ot.fuid");
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = DBPool.pickup(ctx);
            stmt = readCon.prepareStatement(sqlBuilder.toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, OCLPermission.NO_PERMISSIONS);
            stmt.setLong(6, since.getTime());
            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw e;
        } catch (final RuntimeException t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, true);
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances which represent <b>user-visible</b> modified folders
     * since a given date.
     */
    public static SearchIterator getModifiedFoldersSince(final Date since, final int userId, final int[] memberInGroups, final int[] accessibleModules, final boolean userFoldersOnly, final Context ctx) throws OXException, SearchIteratorException {
        final StringBuilder condBuilder = new StringBuilder("AND (changing_date > ?) AND (module IN ").append(FolderObject.SQL_IN_STR_STANDARD_MODULES).append(')');
        if (userFoldersOnly) {
            condBuilder.append(" AND (ot.created_from = ").append(userId).append(") ");
        }
        final String sqlSelectStr = getSQLUserVisibleFolders(FolderObjectIterator.getFieldsForSQL("ot"), StringCollection.getSqlInString(userId, memberInGroups), StringCollection.getSqlInString(accessibleModules), condBuilder.toString(), OXFolderProperties.isEnableDBGrouping() ? "GROUP BY ot.fuid" : null, "ORDER by ot.fuid");
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = DBPool.pickup(ctx);
            stmt = readCon.prepareStatement(sqlSelectStr);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setLong(5, since.getTime());
            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw e;
        } catch (final RuntimeException t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, true);
    }

    private static final String SQL_SELECT_FOLDERS_START = new StringBuilder(200).append("SELECT ").append(FolderObjectIterator.getFieldsForSQL("ot")).append(" FROM oxfolder_tree AS ot").append(" WHERE (cid = ?) ").toString();

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances which represent <b>all</b> modified folders since a
     * given date.
     */
    public static SearchIterator getAllModifiedFoldersSince(final Date since, final Context ctx) throws OXException, SearchIteratorException {
        final String sqlSelectStr = new StringBuilder(300).append(SQL_SELECT_FOLDERS_START).append("AND (changing_date > ?) AND (module IN ").append(FolderObject.SQL_IN_STR_STANDARD_MODULES).append(") ").append(OXFolderProperties.isEnableDBGrouping() ? "GROUP BY ot.fuid" : null).append(" ORDER by ot.fuid").toString();
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            readCon = DBPool.pickup(ctx);
            stmt = readCon.prepareStatement(sqlSelectStr);
            stmt.setInt(1, ctx.getContextId());
            stmt.setLong(2, since.getTime());
            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw e;
        } catch (final RuntimeException t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, true);
    }

    /**
     * Returns user's infostore folder
     */
    public static FolderObject getUsersInfostoreFolder(final int userId, final Context ctx) throws OXException {
        return getUsersInfostoreFolder(userId, ctx, null);
    }

    /**
     * Returns user's infostore folder
     */
    public static FolderObject getUsersInfostoreFolder(final int userId, final Context ctx, final Connection readConArg) throws OXException {
        Connection readCon = readConArg;
        boolean createCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            try {
                if (readCon == null) {
                    readCon = DBPool.pickup(ctx);
                    createCon = true;
                }
                stmt = readCon.prepareStatement("SELECT fuid FROM oxfolder_tree WHERE cid = ? AND created_from = ? AND module = ? AND default_flag = ?");
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, userId);
                stmt.setInt(3, FolderObject.INFOSTORE);
                stmt.setInt(4, 1);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    FolderObject fo;
                    if (FolderCacheManager.isEnabled()) {
                        fo = FolderCacheManager.getInstance().getFolderObject(rs.getInt(1), true, ctx, readCon);
                    } else {
                        fo = FolderObject.loadFolderObjectFromDB(rs.getInt(1), ctx, readCon);
                    }
                    return fo;
                }
                throw OXFolderExceptionCode.NO_DEFAULT_FOLDER_FOUND.create(folderModule2String(FolderObject.INFOSTORE), Integer.valueOf(userId), Integer.valueOf(ctx.getContextId()));
            } finally {
                closeResources(rs, stmt, createCon ? readCon : null, true, ctx);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException t) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
    }

    /**
     * Returns folder's parent id explicitly queried from underlying database storage.
     */
    public static int getFolderParentIdFromDB(final int folderId, final Context ctx, final Connection readConArg) throws OXException {
        Connection readCon = readConArg;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            try {
                if (readCon == null) {
                    readCon = DBPool.pickup(ctx);
                    closeCon = true;
                }
                stmt = readCon.prepareStatement("SELECT parent FROM oxfolder_tree WHERE cid = ? AND fuid = ?");
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                rs = stmt.executeQuery();
                int retval;
                if (rs.next()) {
                    retval = rs.getInt(1);
                } else {
                    throw OXFolderExceptionCode.NOT_EXISTS.create(Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()));
                }
                return retval;
            } finally {
                closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException t) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
    }

    /**
     * Returns folder's name explicitly queried from underlying database storage.
     */
    public static String getFolderNameFromDB(final int folderId, final Context ctx, final Connection readConArg) throws OXException {
        Connection readCon = readConArg;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            try {
                if (readCon == null) {
                    readCon = DBPool.pickup(ctx);
                    closeCon = true;
                }
                stmt = readCon.prepareStatement("SELECT fname FROM oxfolder_tree WHERE cid = ? AND fuid = ?");
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                rs = stmt.executeQuery();
                String retval;
                if (rs.next()) {
                    retval = rs.getString(1);
                } else {
                    throw OXFolderExceptionCode.NOT_EXISTS.create(Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()));
                }
                return retval;
            } finally {
                closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException t) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
    }

    /**
     * Returns folder's last modified timestamp.
     */
    public static Date getFolderLastModifed(final int folderId, final Context ctx) throws OXException {
        if (FolderCacheManager.isEnabled()) {
            return FolderCacheManager.getInstance().getFolderObject(folderId, true, ctx, null).getLastModified();
        }
        return getFolderLastModifedFromDB(folderId, ctx);
    }

    /**
     * Returns folder's last modified timestamp explicitly queried from underlying database storage.
     */
    public static Date getFolderLastModifedFromDB(final int folderId, final Context ctx) throws OXException {
        return getFolderLastModifedFromDB(folderId, ctx, null);
    }

    /**
     * Returns folder's last modified timestamp explicitly queried from underlying database storage.
     */
    /**
     * @param folderId
     * @param ctx
     * @param readConArg
     * @return
     * @throws OXException
     */
    public static Date getFolderLastModifedFromDB(final int folderId, final Context ctx, final Connection readConArg) throws OXException {
        Connection readCon = readConArg;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            try {
                if (readCon == null) {
                    readCon = DBPool.pickup(ctx);
                    closeCon = true;
                }
                stmt = readCon.prepareStatement("SELECT changing_date FROM oxfolder_tree WHERE cid = ? AND fuid = ?");
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                rs = stmt.executeQuery();
                Date retval;
                if (rs.next()) {
                    retval = new Date(rs.getLong(1));
                } else {
                    throw OXFolderExceptionCode.NOT_EXISTS.create(Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()));
                }
                return retval;
            } finally {
                closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException t) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
    }

    /**
     * Determines if session's user is allowed to delete all objects located in given folder
     */
    public static boolean canDeleteAllObjectsInFolder(final FolderObject fo, final Session session, final Connection readCon) throws OXException {
        final int userId = session.getUserId();
        final Context ctx = ContextStorage.getStorageContext(session);
        final UserPermissionBits permissionBits = UserPermissionBitsStorage.getInstance().getUserPermissionBits(session.getUserId(), ctx);
        try {
            /*
             * Check user permission on folder
             */
            final OCLPermission oclPerm = fo.getEffectiveUserPermission(userId, permissionBits, readCon);
            if (!oclPerm.isFolderVisible()) {
                /*
                 * Folder is not visible to user
                 */
                return false;
            } else if (oclPerm.canDeleteAllObjects()) {
                /*
                 * Can delete all objects
                 */
                return true;
            } else if (oclPerm.canDeleteOwnObjects()) {
                // TODO: Additional parameter for readable connection
                /*
                 * User may only delete own objects. Check if folder contains foreign objects which must not be deleted.
                 */
                switch (fo.getModule()) {
                    case FolderObject.TASK:
                        final Tasks tasks = Tasks.getInstance();
                        return !tasks.containsNotSelfCreatedTasks(session, fo.getObjectID());
                    case FolderObject.CALENDAR:
                        final AppointmentSQLInterface calSql = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
                        return !calSql.checkIfFolderContainsForeignObjects(userId, fo.getObjectID());
                    case FolderObject.CONTACT:
                        ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class, true);
                        return false == contactService.containsForeignObjectInFolder(session, String.valueOf(fo.getObjectID()));
                    case FolderObject.INFOSTORE:
                        final InfostoreFacade db = new InfostoreFacadeImpl(new DBPoolProvider());
                        return !db.hasFolderForeignObjects(fo.getObjectID(), ServerSessionAdapter.valueOf(session, ctx));
                    default:
                        throw OXFolderExceptionCode.UNKNOWN_MODULE.create(folderModule2String(fo.getModule()), Integer.valueOf(ctx.getContextId()));
                }
            } else {
                /*
                 * No delete permission: Return true if folder is empty
                 */
                switch (fo.getModule()) {
                    case FolderObject.TASK:
                        final Tasks tasks = Tasks.getInstance();
                        return tasks.isFolderEmpty(ctx, fo.getObjectID());
                    case FolderObject.CALENDAR:
                        final AppointmentSQLInterface calSql = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
                        return calSql.isFolderEmpty(userId, fo.getObjectID());
                    case FolderObject.CONTACT:
                        ContactService contactService = ServerServiceRegistry.getInstance().getService(ContactService.class, true);
                        return contactService.isFolderEmpty(session, String.valueOf(fo.getObjectID()));
                    case FolderObject.INFOSTORE:
                        final InfostoreFacade db = new InfostoreFacadeImpl(new DBPoolProvider());
                        return db.isFolderEmpty(fo.getObjectID(), ctx);
                    default:
                        throw OXFolderExceptionCode.UNKNOWN_MODULE.create(folderModule2String(fo.getModule()), Integer.valueOf(ctx.getContextId()));
                }
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException t) {
            throw OXFolderExceptionCode.RUNTIME_ERROR.create(t, Integer.valueOf(ctx.getContextId()));
        }
    }
}
