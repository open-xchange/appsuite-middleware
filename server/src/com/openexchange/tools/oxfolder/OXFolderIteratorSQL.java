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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import static com.openexchange.tools.oxfolder.OXFolderUtility.getUserName;
import static com.openexchange.tools.sql.DBUtils.closeResources;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.api2.OXException;
import com.openexchange.database.DBPoolingException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.FolderObjectIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * This class provides SQL related methods to fill instances of <code>com.openexchange.tools.iterator.FolderObjectIterator</code>
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXFolderIteratorSQL {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(OXFolderIteratorSQL.class);

    private static final String OXFOLDER_PERMISSIONS = "oxfolder_permissions";

    private static final String OXFOLDER_TREE = "oxfolder_tree";

    private static final String STR_EMPTY = "";

    private static final String STR_SELECT = "SELECT ";

    private OXFolderIteratorSQL() {
        super();
    }

    /**
     * Generates the core SQL statement to query user-visible folders.
     * <p>
     * Returned {@link String} is supposed to be used within a {@link PreparedStatement}.<br>
     * The following fields have to be set via {@link PreparedStatement#setInt(int, int)} method:
     * <ol>
     * <li>Context ID</li>
     * <li>User ID</li>
     * <li>Context ID</li>
     * <li>Context ID</li>
     * <li>User ID</li>
     * <li>Context ID</li>
     * <li>Context ID</li>
     * <ol>
     * 
     * @param fields The fields to select
     * @param permissionIds The user's permission identifiers
     * @param accessibleModules The user's accessible modules
     * @param additionalCondition The optional additional condition; pass <code>null</code> to ignore
     * @param orderBy The optional <code>ORDER BY</code> clause; pass <code>null</code> to ignore
     * @return The core SQL statement to query user-visible folders
     */
    private static String getSQLUserVisibleFolders(final String fields, final String permissionIds, final String accessibleModules, final String additionalCondition, final String orderBy) {
        return getSQLUserVisibleFolders(
            OXFOLDER_TREE,
            OXFOLDER_PERMISSIONS,
            fields,
            permissionIds,
            accessibleModules,
            additionalCondition,
            orderBy);
    }

    /**
     * Generates the core SQL statement to query user-visible folders.
     * <p>
     * Returned {@link String} is supposed to be used within a {@link PreparedStatement}.<br>
     * The following fields have to be set via {@link PreparedStatement#setInt(int, int)} method:
     * <ol>
     * <li>Context ID</li>
     * <li>User ID</li>
     * <li>Context ID</li>
     * <li>Context ID</li>
     * <li>User ID</li>
     * <li>Context ID</li>
     * <li>Context ID</li>
     * <ol>
     * 
     * @param folderTable The folder table name
     * @param permissionTable The permission table name
     * @param fields The fields to select
     * @param permissionIds The user's permission identifiers
     * @param accessibleModules The user's accessible modules
     * @param additionalCondition The optional additional condition; pass <code>null</code> to ignore
     * @param orderBy The optional <code>ORDER BY</code> clause; pass <code>null</code> to ignore
     * @return The core SQL statement to query user-visible folders
     */
    private static String getSQLUserVisibleFolders(final String folderTable, final String permissionTable, final String fields, final String permissionIds, final String accessibleModules, final String additionalCondition, final String orderBy) {
        final StringBuilder sb = new StringBuilder(256);
        /*
         * Compose SELECT string prepended to each UNION statement
         */
        final String preparedOrderBy;
        sb.append(STR_SELECT).append(fields);
        if (null == orderBy) {
            preparedOrderBy = null;
        } else {
            /*
             * Ensure each field contained in ORDER BY clause is contained in selected fields
             */
            final String[] orderFields = parseFieldsFromOrderBy(orderBy);
            if (0 < orderFields.length) {
                for (final String orderField : orderFields) {
                    if (-1 == fields.indexOf(orderField)) {
                        sb.append(", ").append(orderField);
                    }
                }
            }
            /*
             * Prepare the ORDER BY clause by removing table alias
             */
            preparedOrderBy = prepareOrderBy(orderBy);
        }
        sb.append(" FROM ").append(folderTable).append(" AS ot ");
        final String select = sb.toString();
        sb.setLength(0);
        /*
         * Compose WHERE clauses
         */
        final String[] whereClauses = new String[3];
        /*-
         * WHERE ot.cid = ? AND (ot.permission_flag = 1 AND ot.created_from = ?)
         * 
         * 1. cid
         * 2. user
         */
        sb.append(" WHERE ot.cid = ? AND (ot.permission_flag = ").append(FolderObject.PRIVATE_PERMISSION).append(
            " AND ot.created_from = ?)");
        appendix(sb, accessibleModules, additionalCondition);
        whereClauses[0] = sb.toString();
        sb.setLength(0);
        /*-
         * JOIN oxfolder_permissions AS op ON ot.fuid = op.fuid AND ot.cid = ? AND op.cid = ?
         * WHERE (op.admin_flag = 1 AND op.permission_id = ?)
         * 
         * 3. cid
         * 4. cid
         * 5. user
         */
        sb.append(" JOIN ").append(permissionTable).append(
            " AS op ON ot.fuid = op.fuid AND ot.cid = ? AND op.cid = ? WHERE (op.admin_flag = 1 AND op.permission_id = ?)");
        appendix(sb, accessibleModules, additionalCondition);
        whereClauses[1] = sb.toString();
        sb.setLength(0);
        /*-
         * JOIN oxfolder_permissions AS op ON ot.fuid = op.fuid AND ot.cid = ? AND op.cid = ?
         * WHERE (op.fp > 0 AND op.permission_id IN (17,0,1))
         * 
         * 6. cid
         * 7. cid
         */
        sb.append(" JOIN ").append(permissionTable).append(" AS op ON ot.fuid = op.fuid AND ot.cid = ? AND op.cid = ? WHERE (op.fp > ").append(
            OCLPermission.NO_PERMISSIONS).append(" AND op.permission_id IN ").append(permissionIds).append(')');
        appendix(sb, accessibleModules, additionalCondition);
        whereClauses[2] = sb.toString();
        sb.setLength(0);
        /*
         * Finally, compose UNION statement
         */
        {
            sb.append(select);
            sb.append(whereClauses[0]);
        }
        for (int i = 1; i < whereClauses.length; i++) {
            sb.append(" UNION ");
            sb.append(select);
            sb.append(whereClauses[i]);
        }
        if (null != preparedOrderBy) {
            sb.append(' ').append(preparedOrderBy);
        }
        return sb.toString();
    }

    private static final void appendix(final StringBuilder sb, final String accessibleModules, final String additionalCondition) {
        if (OXFolderProperties.isIgnoreSharedAddressbook()) {
            sb.append(" AND (ot.fuid != ").append(FolderObject.SYSTEM_GLOBAL_FOLDER_ID).append(')');
        }
        if (accessibleModules != null) {
            sb.append(" AND (ot.module IN ").append(accessibleModules).append(')');
        }
        if (additionalCondition != null) {
            sb.append(' ').append(additionalCondition);
        }
    }

    private static final Pattern PAT_ORDER_BY = Pattern.compile(
        "( *ORDER +BY +)(?:([a-zA-Z0-9_.]+)(?: *ASC *| *DESC *)?)(?:, *[a-zA-Z0-9_.]+(?: *ASC *| *DESC *)?)?",
        Pattern.CASE_INSENSITIVE);

    private static final Pattern PAT_FIELD = Pattern.compile("(?:([a-zA-Z0-9_.]+)(?: *ASC| *DESC)?)", Pattern.CASE_INSENSITIVE);

    /**
     * Parses denoted fields out of specified <code>ORDER BY</code> statement; <code>" ORDER BY co.field01 DESC "</code>
     * 
     * @param orderBy The <code>ORDER BY</code> statement
     * @return The parsed fields
     */
    private static String[] parseFieldsFromOrderBy(final String orderBy) {
        // ORDER BY oc.field01 DESC
        Matcher m = PAT_ORDER_BY.matcher(orderBy);
        if (m.matches()) {
            m = PAT_FIELD.matcher(orderBy.substring(m.end(1)));
            final List<String> l = new ArrayList<String>(2);
            while (m.find()) {
                l.add(m.group(1));
            }
            return l.toArray(new String[l.size()]);
        }
        return new String[0];
    }

    private static final Pattern PAT_PREP = Pattern.compile("[a-zA-Z0-9_]+\\.([a-zA-Z0-9_])");

    /**
     * Prepares given <code>ORDER BY</code> statement to be used within a <code>UNION</code> statement.
     * 
     * <pre>
     * ORDER BY co.field01 DESC -&gt; ORDER BY field01 DESC
     * </pre>
     * 
     * @param orderBy The <code>ORDER BY</code> statement
     * @return The prepared <code>ORDER BY</code> statement
     */
    private static final String prepareOrderBy(final String orderBy) {
        if (null == orderBy) {
            return null;
        }
        return PAT_PREP.matcher(orderBy).replaceAll("$1");
    }

    private final static String STR_OT = "ot";

    private final static String STR_ORDER_BY = "ORDER BY ";

    private final static String STR_FUID = "fuid";

    private final static String STR_DEFAULTFLAG_DESC = "default_flag DESC";

    private final static String STR_FNAME = "fname";

    private static String getRootOrderBy(final String tableAlias) {
        return getOrderBy(tableAlias, STR_FUID);
    }

    private static String getSubfolderOrderBy(final String tableAlias) {
        return getOrderBy(tableAlias, STR_DEFAULTFLAG_DESC, STR_FNAME);
    }

    private static String getOrderBy(final String tableAlias, final String... strings) {
        if (strings == null || strings.length == 0) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(32);
        final String alias;
        if (tableAlias == null) {
            alias = STR_EMPTY;
        } else {
            alias = sb.append(tableAlias).append('.').toString();
            sb.setLength(0);
        }
        sb.append(STR_ORDER_BY);
        sb.append(alias).append(strings[0]);
        for (int i = 1; i < strings.length; i++) {
            sb.append(", ");
            sb.append(alias).append(strings[i]);
        }
        return sb.toString();
    }

    private final static String STR_GROUP_BY = "GROUP BY ";

    private static String getGroupBy(final String tableAlias) {
        final String alias;
        if (tableAlias == null) {
            alias = STR_EMPTY;
        } else {
            alias = new StringBuilder(tableAlias.length() + 1).append(tableAlias).append('.').toString();
        }
        return new StringBuilder(STR_GROUP_BY).append(alias).append(STR_FUID).toString();
    }

    /**
     * Gets the user-visible root folders.
     * 
     * @param userId The user identifier
     * @param memberInGroups The user's group identifiers
     * @param userConfig The user's configuration
     * @param ctx The context
     * @return The user-visible root folders
     * @throws OXException If a folder error occurs
     * @throws SearchIteratorException If a search iterator error occurs
     */
    public static SearchIterator<FolderObject> getUserRootFoldersIterator(final int userId, final int[] memberInGroups, final UserConfiguration userConfig, final Context ctx) throws OXException, SearchIteratorException {
        StringBuilder condBuilder = new StringBuilder(32).append("AND (ot.type = ").append(FolderObject.SYSTEM_TYPE).append(
            ") AND (ot.parent = 0)");
        if (!userConfig.hasFullSharedFolderAccess()) {
            condBuilder.append(" AND (ot.fuid != ").append(FolderObject.SYSTEM_SHARED_FOLDER_ID).append(')');
        }
        if (!userConfig.hasInfostore()) {
            condBuilder.append(" AND (ot.fuid != ").append(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID).append(')');
        }
        final String sqlSelectStr = getSQLUserVisibleFolders(
            FolderObjectIterator.getFieldsForSQL(STR_OT),
            StringCollection.getSqlInString(userId, memberInGroups),
            StringCollection.getSqlInString(userConfig.getAccessibleModules()),
            condBuilder.toString(),
            getRootOrderBy(STR_OT));
        condBuilder = null;
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final int contextId = ctx.getContextId();
        try {
            readCon = DBPool.pickup(ctx);
            stmt = readCon.prepareStatement(sqlSelectStr);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);

            if (LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new StringBuilder().append("\nFolderSQL Query: ").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }

            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(contextId));
        } catch (final DBPoolingException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(contextId));
        } catch (final Throwable t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw new OXFolderException(FolderCode.RUNTIME_ERROR, t, Integer.valueOf(contextId));
        }
        return new FolderObjectIterator(rs, stmt, true, ctx, readCon, true);
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances, which represent all user-visible sub folders of a
     * certain parent folder.
     */
    public static SearchIterator<FolderObject> getVisibleSubfoldersIterator(final int parentFolderId, final int userId, final int[] groups, final Context ctx, final UserConfiguration userConfig, final Timestamp since) throws SQLException, DBPoolingException, OXException, SearchIteratorException {
        return getVisibleSubfoldersIterator(parentFolderId, userId, groups, ctx, userConfig, since, null);
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances, which represent all user-visible sub folders of a
     * certain parent folder.
     */
    public static SearchIterator<FolderObject> getVisibleSubfoldersIterator(final int parentFolderId, final int userId, final int[] groups, final Context ctx, final UserConfiguration userConfig, final Timestamp since, final Connection con) throws SQLException, DBPoolingException, OXException, SearchIteratorException {
        if (parentFolderId == FolderObject.SYSTEM_PRIVATE_FOLDER_ID) {
            return getVisiblePrivateFolders(userId, groups, userConfig.getAccessibleModules(), ctx, since, con);
        } else if (parentFolderId == FolderObject.SYSTEM_PUBLIC_FOLDER_ID) {
            return getVisiblePublicFolders(userId, groups, userConfig.getAccessibleModules(), ctx, since, con);
        } else if (parentFolderId == FolderObject.SYSTEM_SHARED_FOLDER_ID) {
            return getVisibleSharedFolders(userId, groups, userConfig.getAccessibleModules(), ctx, since, con);
        } else {
            /*
             * Check user's effective permission on subfolder's parent
             */
            final FolderObject parentFolder = new OXFolderAccess(con, ctx).getFolderObject(parentFolderId);
            final OCLPermission effectivePerm = parentFolder.getEffectiveUserPermission(userId, userConfig);
            if (effectivePerm.getFolderPermission() < OCLPermission.READ_FOLDER) {
                return FolderObjectIterator.EMPTY_FOLDER_ITERATOR;
            }
            return getVisibleSubfoldersIterator(parentFolder, userId, groups, userConfig.getAccessibleModules(), ctx, since, con);
        }
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances which are located beneath system's private folder.
     */
    private static SearchIterator<FolderObject> getVisiblePrivateFolders(final int userId, final int[] groups, final int[] accessibleModules, final Context ctx, final Timestamp since, final Connection con) throws OXException {
        final StringBuilder condBuilder = new StringBuilder(32).append("AND (ot.type = ").append(FolderObject.PRIVATE).append(
            " AND ot.created_from = ").append(userId).append(") AND (ot.parent = ").append(FolderObject.SYSTEM_PRIVATE_FOLDER_ID).append(
            ')');
        if (since != null) {
            condBuilder.append(" AND (changing_date > ").append(since.getTime()).append(')');
        }
        final String sqlSelectStr = getSQLUserVisibleFolders(FolderObjectIterator.getFieldsForSQL(STR_OT), StringCollection.getSqlInString(
            userId,
            groups), StringCollection.getSqlInString(accessibleModules), condBuilder.toString(), getSubfolderOrderBy(STR_OT));
        Connection readCon = con;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final int contextId = ctx.getContextId();
        try {
            if (null == readCon) {
                readCon = DBPool.pickup(ctx);
                closeCon = true;
            }
            stmt = readCon.prepareStatement(sqlSelectStr);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);

            if (LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new StringBuilder().append("\nFolderSQL Query: ").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }

            rs = stmt.executeQuery();
            /*
             * Ensure ordering of private default folder follows: calendar, contacts, tasks
             */
            final List<FolderObject> list = new FolderObjectIterator(rs, stmt, false, ctx, readCon, closeCon).asList();
            if (list.size() >= 3 && (list.get(0).getModule() != FolderObject.CALENDAR || list.get(1).getModule() != FolderObject.CONTACT || list.get(
                2).getModule() != FolderObject.TASK)) {
                final FolderObject[] defaultFolders = new FolderObject[] { list.remove(0), list.remove(0), list.remove(0) };
                /*
                 * Restore order
                 */
                switchElements(defaultFolders);
                for (int i = 0; i < defaultFolders.length; i++) {
                    list.add(i, defaultFolders[i]);
                }
            }
            return new FolderObjectIterator(list, false);
        } catch (final SQLException e) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(contextId));
        } catch (final DBPoolingException e) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(contextId));
        } catch (final Throwable t) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.RUNTIME_ERROR, t, Integer.valueOf(contextId));
        }
        // return new FolderObjectIterator(rs, stmt, false, ctx, readCon, true);
    }

    private static final int[] DEF_MODULES = { FolderObject.CALENDAR, FolderObject.CONTACT, FolderObject.TASK };

    private static void switchElements(final FolderObject[] folders) {
        for (int i = 0; i < folders.length; i++) {
            boolean switched = false;
            for (int j = 0; j < DEF_MODULES.length && !switched; j++) {
                if (folders[i].getModule() == DEF_MODULES[j] && i != j) {
                    /*
                     * Switch elements
                     */
                    final FolderObject tmp = folders[j];
                    folders[j] = folders[i];
                    folders[i] = tmp;
                    switched = true;
                }
            }
        }
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances which are located beneath system's public folder.
     */
    private static SearchIterator<FolderObject> getVisiblePublicFolders(final int userId, final int[] groups, final int[] accessibleModules, final Context ctx, final Timestamp since, final Connection con) throws OXException, SearchIteratorException {
        final StringBuilder condBuilder = new StringBuilder(32).append("AND (ot.type = ").append(FolderObject.PUBLIC).append(
            ") AND (ot.parent = ").append(FolderObject.SYSTEM_PUBLIC_FOLDER_ID).append(')');
        if (null != since) {
            condBuilder.append(" AND (changing_date > ").append(since.getTime()).append(')');
        }
        final String sqlSelectStr = getSQLUserVisibleFolders(FolderObjectIterator.getFieldsForSQL(STR_OT), StringCollection.getSqlInString(
            userId,
            groups), StringCollection.getSqlInString(accessibleModules), condBuilder.toString(), getSubfolderOrderBy(STR_OT));
        Connection readCon = con;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final int contextId = ctx.getContextId();
        try {
            if (null == readCon) {
                readCon = DBPool.pickup(ctx);
                closeCon = true;
            }
            stmt = readCon.prepareStatement(sqlSelectStr);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);

            if (LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new StringBuilder().append("\nFolderSQL Query: ").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }

            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(contextId));
        } catch (final DBPoolingException e) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(contextId));
        } catch (final Throwable t) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.RUNTIME_ERROR, t, Integer.valueOf(contextId));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, closeCon);
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances which offer a share right for given user and therefore
     * should appear right beneath system's shared folder in displayed folder tree.
     */
    private static SearchIterator<FolderObject> getVisibleSharedFolders(final int userId, final int[] groups, final int[] accessibleModules, final Context ctx, final Timestamp since, final Connection con) throws OXException, SearchIteratorException {
        return getVisibleSharedFolders(userId, groups, accessibleModules, -1, ctx, since, con);
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances which are located beneath given parent folder.
     */
    private static SearchIterator<FolderObject> getVisibleSubfoldersIterator(final FolderObject parentFolder, final int userId, final int[] memberInGroups, final int[] accessibleModules, final Context ctx, final Timestamp since, final Connection con) throws OXException, SearchIteratorException {
        final boolean shared = parentFolder.isShared(userId);
        final StringBuilder condBuilder = new StringBuilder(32);
        if (shared) {
            condBuilder.append("AND (ot.type = ").append(FolderObject.PRIVATE).append(" AND ot.created_from != ").append(userId).append(
                ") ");
        }
        condBuilder.append("AND (ot.parent = ").append(parentFolder.getObjectID()).append(')');
        if (null != since) {
            condBuilder.append(" AND (changing_date > ").append(since.getTime()).append(')');
        }
        final String sqlSelectStr = getSQLUserVisibleFolders(FolderObjectIterator.getFieldsForSQL(STR_OT), StringCollection.getSqlInString(
            userId,
            memberInGroups), StringCollection.getSqlInString(accessibleModules), condBuilder.toString(), getSubfolderOrderBy(STR_OT));
        Connection readCon = con;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final int contextId = ctx.getContextId();
        try {
            if (null == readCon) {
                readCon = DBPool.pickup(ctx);
                closeCon = true;
            }
            stmt = readCon.prepareStatement(sqlSelectStr);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);

            if (LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new StringBuilder().append("\nFolderSQL Query: ").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }

            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(contextId));
        } catch (final DBPoolingException e) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(contextId));
        } catch (final Throwable t) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.RUNTIME_ERROR, t, Integer.valueOf(contextId));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, closeCon);
    }

    /**
     * Returns an <code>SearchIterator</code> of <code>FolderObject</code> instances of user-visible shared folders.
     */
    public static SearchIterator<FolderObject> getVisibleSharedFolders(final int userId, final int[] memberInGroups, final int[] accessibleModules, final int owner, final Context ctx, final Timestamp since, final Connection con) throws OXException, SearchIteratorException {
        final StringBuilder condBuilder = new StringBuilder(32).append("AND (ot.type = ").append(FolderObject.PRIVATE).append(
            " AND ot.created_from != ").append(userId).append(')');
        if (owner > -1) {
            condBuilder.append(" AND (ot.created_from = ").append(owner).append(')');
        }
        if (since != null) {
            condBuilder.append(" AND (changing_date > ").append(since.getTime()).append(')');
        }
        final String sqlSelectStr = getSQLUserVisibleFolders(FolderObjectIterator.getFieldsForSQL(STR_OT), StringCollection.getSqlInString(
            userId,
            memberInGroups), StringCollection.getSqlInString(accessibleModules), condBuilder.toString(), getSubfolderOrderBy(STR_OT));
        Connection readCon = con;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final int contextId = ctx.getContextId();
        try {
            if (null == readCon) {
                readCon = DBPool.pickup(ctx);
                closeCon = true;
            }
            stmt = readCon.prepareStatement(sqlSelectStr);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);

            if (LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new StringBuilder().append("\nFolderSQL Query: ").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }

            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(contextId));
        } catch (final DBPoolingException e) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(contextId));
        } catch (final Throwable t) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.RUNTIME_ERROR, t, Integer.valueOf(contextId));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, closeCon);
    }

    /**
     * Gets all visible public folders that are not visible in hierarchic tree-view (because any ancestor folder is not visible)
     * 
     * @param userId The user ID
     * @param groups The user's group IDs
     * @param userConfig The user configuration
     * @param ctx The context
     * @return An iterator for all visible public folders that are not visible in hierarchic tree-view (because any ancestor folder is not
     *         visible)
     * @throws OXException If all visible public folders that are not visible in hierarchic tree-view cannot be determined
     */
    public static SearchIterator<FolderObject> getAllVisibleFoldersNotSeenInTreeView(final int userId, final int[] groups, final UserConfiguration userConfig, final Context ctx) throws OXException {
        return getVisibleFoldersNotSeenInTreeView(null, userId, groups, userConfig, ctx, null);
    }

    /**
     * Gets all visible public folders that are not visible in hierarchic tree-view (because any ancestor folder is not visible)
     * 
     * @param userId The user ID
     * @param groups The user's group IDs
     * @param userConfig The user configuration
     * @param ctx The context
     * @param readCon An readable connection (optional: may be <code>null</code>)
     * @return An iterator for all visible public folders that are not visible in hierarchic tree-view (because any ancestor folder is not
     *         visible)
     * @throws OXException If all visible public folders that are not visible in hierarchic tree-view cannot be determined
     */
    public static SearchIterator<FolderObject> getAllVisibleFoldersNotSeenInTreeView(final int userId, final int[] groups, final UserConfiguration userConfig, final Context ctx, final Connection readCon) throws OXException {
        return getVisibleFoldersNotSeenInTreeView(null, userId, groups, userConfig, ctx, readCon);
    }

    /**
     * Gets specified module's visible public folders that are not visible in hierarchic tree-view (because any ancestor folder is not
     * visible)
     * 
     * @param module The module whose non-hierarchic-visible folders should be determined
     * @param userId The user ID
     * @param groups The user's group IDs
     * @param userConfig The user configuration
     * @param ctx The context
     * @param readCon An readable connection (optional: may be <code>null</code>)
     * @return An iterator for specified module's visible public folders that are not visible in hierarchic tree-view
     * @throws OXException If module's visible public folders that are not visible in hierarchic tree-view cannot be determined
     */
    public static SearchIterator<FolderObject> getVisibleFoldersNotSeenInTreeView(final int module, final int userId, final int[] groups, final UserConfiguration userConfig, final Context ctx, final Connection readCon) throws OXException {
        return getVisibleFoldersNotSeenInTreeView(Integer.valueOf(module), userId, groups, userConfig, ctx, readCon);
    }

    private static final String SQL_SEL_ALL_PUB = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND type = ? ORDER BY fuid";

    private static final String SQL_SEL_ALL_PUB_MODULE = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND type = ? AND module = ? ORDER BY fuid";

    private static SearchIterator<FolderObject> getVisibleFoldersNotSeenInTreeView(final Integer module, final int userId, final int[] groups, final UserConfiguration userConfig, final Context ctx, final Connection readCon) throws OXException {
        Connection rc = readCon;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final int contextId = ctx.getContextId();
        try {
            if (readCon == null) {
                rc = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            int pos = 1;
            /*
             * 1.) Select all user-visible public folders
             */
            {
                final StringBuilder condBuilder = new StringBuilder(32).append("AND ot.type = ").append(FolderObject.PUBLIC);
                if (null != module) {
                    condBuilder.append(" AND ot.module = ").append(module.intValue());
                }
                final String sqlSelectStr = getSQLUserVisibleFolders(
                    FolderObjectIterator.getFieldsForSQL(STR_OT),
                    StringCollection.getSqlInString(userId, groups),
                    StringCollection.getSqlInString(userConfig.getAccessibleModules()),
                    condBuilder.toString(),
                    getOrderBy(STR_OT, "module", "fname"));
                stmt = rc.prepareStatement(sqlSelectStr);
            }
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);

            if (LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new StringBuilder().append("\nFolderSQL Query: ").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }

            rs = stmt.executeQuery();
            /*
             * asQueue() already closes all resources
             */
            final Queue<FolderObject> q = new FolderObjectIterator(rs, stmt, false, ctx, rc, closeReadCon).asQueue();
            final int size = q.size();
            if (size == 0) {
                /*
                 * Set resources to null since they were already closed by asQueue() method
                 */
                rs = null;
                stmt = null;
                rc = null;
                return FolderObjectIterator.EMPTY_FOLDER_ITERATOR;
            }
            /*
             * 2.) All non-user-visible public folders
             */
            if (readCon == null) {
                rc = DBPool.pickup(ctx);
            }
            stmt = rc.prepareStatement(null == module ? SQL_SEL_ALL_PUB : SQL_SEL_ALL_PUB_MODULE);
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, FolderObject.PUBLIC);
            if (null != module) {
                stmt.setInt(pos, module.intValue());
            }
            rs = stmt.executeQuery();
            final Set<Integer> nonVisibleSet = new HashSet<Integer>(1024);
            while (rs.next()) {
                nonVisibleSet.add(Integer.valueOf(rs.getInt(1)));
            }
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            nonVisibleSet.removeAll(queue2IDSet(q, size));
            /*
             * 3.) Filter all visible public folders with a non-visible parent
             */
            for (final Iterator<FolderObject> iter = q.iterator(); iter.hasNext();) {
                if (!nonVisibleSet.contains(Integer.valueOf(iter.next().getParentFolderID()))) {
                    iter.remove();
                }
            }
            return new FolderObjectIterator(q, false);
        } catch (final SQLException e) {
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(contextId));
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(contextId));
        } catch (final Throwable t) {
            throw new OXFolderException(FolderCode.RUNTIME_ERROR, t, Integer.valueOf(contextId));
        } finally {
            closeResources(rs, stmt, closeReadCon ? rc : null, true, ctx);
        }
    }

    private static Set<Integer> queue2IDSet(final Queue<FolderObject> q, final int size) {
        final Set<Integer> retval = new HashSet<Integer>(size);
        final Iterator<FolderObject> iter = q.iterator();
        for (int i = 0; i < size; i++) {
            retval.add(Integer.valueOf(iter.next().getObjectID()));
        }
        return retval;
    }

    /**
     * Returns a <code>SearchIterator</code> of <code>FolderObject</code> which represent all visible folders lying on path from given
     * folder to root folder.
     */
    public static SearchIterator<FolderObject> getFoldersOnPathToRoot(final int folderId, final int userId, final UserConfiguration userConfig, final Locale locale, final Context ctx) throws OXException, SearchIteratorException {
        final List<FolderObject> folderList = new ArrayList<FolderObject>();
        fillAncestor(folderList, folderId, userId, userConfig, locale, null, ctx);
        return new FolderObjectIterator(folderList, false);
    }

    private static void fillAncestor(final List<FolderObject> folderList, final int folderId, final int userId, final UserConfiguration userConfig, final Locale locale, final UserStorage userStoreArg, final Context ctx) throws OXException {
        final OXFolderAccess access = new OXFolderAccess(ctx);
        if (checkForSpecialFolder(folderList, folderId, locale, access)) {
            return;
        }
        UserStorage userStore = userStoreArg;
        FolderObject fo = access.getFolderObject(folderId);
        final int contextId = ctx.getContextId();
        try {
            if (!fo.getEffectiveUserPermission(userId, userConfig).isFolderVisible()) {
                if (folderList.isEmpty()) {
                    /*
                     * Starting folder is not visible to user
                     */
                    throw new OXFolderException(
                        FolderCode.NOT_VISIBLE,
                        Integer.valueOf(folderId),
                        getUserName(userId, ctx),
                        Integer.valueOf(contextId));
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
                } catch (final LdapException e) {
                    if (fo.getCreatedBy() != OCLPermission.ALL_GROUPS_AND_USERS) {
                        throw e;
                    }
                    final StringHelper strHelper = new StringHelper(locale);
                    creatorDisplayName = strHelper.getString(Groups.ALL_USERS);
                }
                final FolderObject virtualOwnerFolder = FolderObject.createVirtualFolderObject(
                    FolderObject.SHARED_PREFIX + fo.getCreatedBy(),
                    creatorDisplayName,
                    FolderObject.SYSTEM_MODULE,
                    true,
                    FolderObject.SYSTEM_TYPE);
                folderList.add(virtualOwnerFolder);
                /*
                 * Set folder to system shared folder
                 */
                fo = access.getFolderObject(FolderObject.SYSTEM_SHARED_FOLDER_ID);
                fo.setFolderName(FolderObject.getFolderString(FolderObject.SYSTEM_SHARED_FOLDER_ID, locale));
                folderList.add(fo);
                return;
            } else if (fo.getType() == FolderObject.PUBLIC && hasNonVisibleParent(fo, userId, userConfig, access)) {
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
                    throw new OXFolderException(
                        FolderCode.UNKNOWN_MODULE,
                        STR_EMPTY,
                        folderModule2String(fo.getModule()),
                        Integer.valueOf(contextId));
                }
                checkForSpecialFolder(folderList, virtualParent, locale, access);
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
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(contextId));
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(contextId));
        } catch (final LdapException e) {
            throw new OXFolderException(FolderCode.LDAP_ERROR, e, Integer.valueOf(contextId));
        } catch (final OXException e) {
            throw e;
        } catch (final Throwable t) {
            throw new OXFolderException(FolderCode.RUNTIME_ERROR, t, Integer.valueOf(contextId));
        }
    }

    private static boolean checkForSpecialFolder(final List<FolderObject> folderList, final int folderId, final Locale locale, final OXFolderAccess access) throws OXException {
        final boolean publicParent;
        final FolderObject specialFolder;
        switch (folderId) {
        case FolderObject.SYSTEM_LDAP_FOLDER_ID:
            specialFolder = access.getFolderObject(folderId);
            specialFolder.setFolderName(FolderObject.getFolderString(FolderObject.SYSTEM_LDAP_FOLDER_ID, locale));
            publicParent = true;
            break;
        case FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID:
            specialFolder = FolderObject.createVirtualFolderObject(FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID, FolderObject.getFolderString(
                FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID,
                locale), FolderObject.SYSTEM_MODULE, true, FolderObject.SYSTEM_TYPE);
            publicParent = true;
            break;
        case FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID:
            specialFolder = FolderObject.createVirtualFolderObject(
                FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID,
                FolderObject.getFolderString(FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID, locale),
                FolderObject.SYSTEM_MODULE,
                true,
                FolderObject.SYSTEM_TYPE);
            publicParent = true;
            break;
        case FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID:
            specialFolder = FolderObject.createVirtualFolderObject(
                FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID,
                FolderObject.getFolderString(FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID, locale),
                FolderObject.SYSTEM_MODULE,
                true,
                FolderObject.SYSTEM_TYPE);
            publicParent = true;
            break;
        case FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID:
            specialFolder = FolderObject.createVirtualFolderObject(
                FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID,
                FolderObject.getFolderString(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, locale),
                FolderObject.SYSTEM_MODULE,
                true,
                FolderObject.SYSTEM_TYPE);
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
        final FolderObject parent = access.getFolderObject(parentId);
        parent.setFolderName(FolderObject.getFolderString(parentId, locale));
        folderList.add(parent);
        return true;
    }

    private static boolean hasNonVisibleParent(final FolderObject fo, final int userId, final UserConfiguration userConf, final OXFolderAccess access) throws OXException, DBPoolingException, SQLException {
        if (fo.getParentFolderID() == FolderObject.SYSTEM_ROOT_FOLDER_ID) {
            return false;
        }
        return !access.getFolderObject(fo.getParentFolderID()).getEffectiveUserPermission(userId, userConf).isFolderVisible();
    }

    /**
     * Returns a <code>SearchIterator</code> of <code>FolderObject</code> instances, which represent all user-visible folders of a certain
     * type regardless of their parent folder.
     */
    public static SearchIterator<FolderObject> getAllVisibleFoldersIteratorOfType(final int userId, final int[] memberInGroups, final int[] accessibleModules, final int type, final int[] modules, final Context ctx) throws OXException, SearchIteratorException {
        return getAllVisibleFoldersIteratorOfType(userId, memberInGroups, accessibleModules, type, modules, null, ctx);
    }

    /**
     * Returns a <code>SearchIterator</code> of <code>FolderObject</code> instances, which represent all user-visible folders of a certain
     * type and a certain parent folder.
     */
    public static SearchIterator<FolderObject> getAllVisibleFoldersIteratorOfType(final int userId, final int[] memberInGroups, final int[] accessibleModules, final int type, final int[] modules, final int parent, final Context ctx) throws OXException, SearchIteratorException {
        return getAllVisibleFoldersIteratorOfType(userId, memberInGroups, accessibleModules, type, modules, Integer.valueOf(parent), ctx);
    }

    /**
     * Returns a <code>SearchIterator</code> of <code>FolderObject</code> instances, which represent all user-visible folders of a certain
     * type and a certain parent folder.
     */
    private static SearchIterator<FolderObject> getAllVisibleFoldersIteratorOfType(final int userId, final int[] memberInGroups, final int[] accessibleModules, final int type, final int[] modules, final Integer parent, final Context ctx) throws OXException, SearchIteratorException {
        final StringBuilder condBuilder = new StringBuilder(32).append("AND (ot.module IN (");
        condBuilder.append(modules[0]);
        for (int i = 1; i < modules.length; i++) {
            condBuilder.append(", ").append(modules[i]);
        }
        if (type == FolderObject.SHARED) {
            condBuilder.append(")) AND (ot.type = ").append(FolderObject.PRIVATE);
            condBuilder.append(" AND ot.created_from != ").append(userId);
        } else {
            condBuilder.append(")) AND (ot.type = ").append(type);
        }
        condBuilder.append(')');
        if (parent != null) {
            condBuilder.append(" AND (ot.parent = ").append(parent.intValue()).append(')');
        }
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final int contextId = ctx.getContextId();
        try {
            readCon = DBPool.pickup(ctx);
            stmt = readCon.prepareStatement(getSQLUserVisibleFolders(
                FolderObjectIterator.getFieldsForSQL(STR_OT),
                StringCollection.getSqlInString(userId, memberInGroups),
                StringCollection.getSqlInString(accessibleModules),
                condBuilder.toString(),
                getSubfolderOrderBy(STR_OT)));
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);

            if (LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new StringBuilder().append("\nFolderSQL Query: ").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }

            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(contextId));
        } catch (final DBPoolingException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(contextId));
        } catch (final Throwable t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw new OXFolderException(FolderCode.RUNTIME_ERROR, t, Integer.valueOf(contextId));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, true);
    }

    /**
     * Returns a <code>SearchIterator</code> of <code>FolderObject</code> instances of a certain module
     */
    public static SearchIterator<FolderObject> getAllVisibleFoldersIteratorOfModule(final int userId, final int[] memberInGroups, final int[] accessibleModules, final int module, final Context ctx) throws OXException, SearchIteratorException {
        return getAllVisibleFoldersIteratorOfModule(userId, memberInGroups, accessibleModules, module, ctx, null);
    }

    /**
     * Returns a <code>SearchIterator</code> of <code>FolderObject</code> instances of a certain module
     */
    public static SearchIterator<FolderObject> getAllVisibleFoldersIteratorOfModule(final int userId, final int[] memberInGroups, final int[] accessibleModules, final int module, final Context ctx, final Connection readConArg) throws OXException, SearchIteratorException {
        final String sqlSelectStr = getSQLUserVisibleFolders(
            FolderObjectIterator.getFieldsForSQL(STR_OT),
            StringCollection.getSqlInString(userId, memberInGroups),
            StringCollection.getSqlInString(accessibleModules),
            new StringBuilder("AND (ot.module = ").append(module).append(')').toString(),
            getSubfolderOrderBy(STR_OT));
        final Connection readCon;
        final boolean closeReadCon = (readConArg == null);
        final int contextId = ctx.getContextId();
        try {
            if (closeReadCon) {
                readCon = DBPool.pickup(ctx);
            } else {
                readCon = readConArg;
            }
        } catch (final DBPoolingException e) {
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(contextId));
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = readCon.prepareStatement(sqlSelectStr);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);

            if (LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new StringBuilder().append("\nFolderSQL Query: ").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }

            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(contextId));
        } catch (final Throwable t) {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.RUNTIME_ERROR, t, Integer.valueOf(contextId));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, closeReadCon);
    }

    /**
     * Gets formerly user-visible folders which were deleted since specified time stamp.
     * 
     * @param since The time stamp
     * @param userId The user identifier
     * @param memberInGroups The user's group identifiers
     * @param accessibleModules The user's accessible modules
     * @param ctx The context
     * @return The formerly user-visible folders which were deleted since specified time stamp
     * @throws OXException If a folder error occurs
     * @throws SearchIteratorException If a search iterator error occurs
     */
    public static SearchIterator<FolderObject> getDeletedFoldersSince(final Date since, final int userId, final int[] memberInGroups, final int[] accessibleModules, final Context ctx) throws OXException, SearchIteratorException {
        return getDeletedFoldersSince(since, userId, memberInGroups, accessibleModules, ctx, null);
    }

    /**
     * Gets formerly user-visible folders which were deleted since specified time stamp.
     * 
     * @param since The time stamp
     * @param userId The user identifier
     * @param memberInGroups The user's group identifiers
     * @param accessibleModules The user's accessible modules
     * @param ctx The context
     * @param con The connection to use
     * @return The formerly user-visible folders which were deleted since specified time stamp
     * @throws OXException If a folder error occurs
     * @throws SearchIteratorException If a search iterator error occurs
     */
    public static SearchIterator<FolderObject> getDeletedFoldersSince(final Date since, final int userId, final int[] memberInGroups, final int[] accessibleModules, final Context ctx, final Connection con) throws OXException, SearchIteratorException {
        final String fields = FolderObjectIterator.getFieldsForSQL(STR_OT);
        final String condition = since == null ? null : new StringBuilder(" AND (ot.changing_date > ").append(since.getTime()).append(')').toString();
        final String sqlSelectStr = getSQLUserVisibleFolders(
            "del_oxfolder_tree",
            "del_oxfolder_permissions",
            fields,
            StringCollection.getSqlInString(userId, memberInGroups),
            StringCollection.getSqlInString(accessibleModules),
            condition,
            "ORDER by ot.fuid");
        Connection readCon = con;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final int contextId = ctx.getContextId();
        try {
            if (null == readCon) {
                readCon = DBPool.pickup(ctx);
                closeCon = true;
            }
            stmt = readCon.prepareStatement(sqlSelectStr);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);

            if (LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new StringBuilder().append("\nFolderSQL Query: ").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }

            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(contextId));
        } catch (final DBPoolingException e) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(contextId));
        } catch (final Throwable t) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.RUNTIME_ERROR, t, Integer.valueOf(contextId));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, closeCon);
    }

    /**
     * Gets user-visible folders which were modified since specified time stamp.
     * 
     * @param since The time stamp
     * @param userId The user identifier
     * @param memberInGroups The user's group identifiers
     * @param accessibleModules The user's accessible modules
     * @param userFoldersOnly <code>true</code> to consider only folder which were created by given user; otherwise <code>false</code>
     * @param ctx The context
     * @return The user-visible folders which were modified since specified time stamp
     * @throws OXException If a folder error occurs
     * @throws SearchIteratorException If a search iterator error occurs
     */
    public static SearchIterator<FolderObject> getModifiedFoldersSince(final Date since, final int userId, final int[] memberInGroups, final int[] accessibleModules, final boolean userFoldersOnly, final Context ctx) throws OXException, SearchIteratorException {
        final StringBuilder condBuilder = new StringBuilder(32).append("AND (changing_date > ").append(since.getTime()).append(
            ") AND (module IN ").append(FolderObject.SQL_IN_STR_STANDARD_MODULES).append(')');
        if (userFoldersOnly) {
            condBuilder.append(" AND (ot.created_from = ").append(userId).append(") ");
        }
        final String sqlSelectStr = getSQLUserVisibleFolders(FolderObjectIterator.getFieldsForSQL(STR_OT), StringCollection.getSqlInString(
            userId,
            memberInGroups), StringCollection.getSqlInString(accessibleModules), condBuilder.toString(), getSubfolderOrderBy(STR_OT));
        Connection readCon = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final int contextId = ctx.getContextId();
        try {
            readCon = DBPool.pickup(ctx);
            stmt = readCon.prepareStatement(sqlSelectStr);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, contextId);

            if (LOG.isDebugEnabled()) {
                final String sql = stmt.toString();
                LOG.debug(new StringBuilder().append("\nFolderSQL Query: ").append(sql.substring(sql.indexOf(": ") + 2)).toString());
            }

            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(contextId));
        } catch (final DBPoolingException e) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(contextId));
        } catch (final Throwable t) {
            closeResources(rs, stmt, readCon, true, ctx);
            throw new OXFolderException(FolderCode.RUNTIME_ERROR, t, Integer.valueOf(contextId));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, true);
    }

    private static final String SQL_SELECT_FOLDERS_START = new StringBuilder(200).append(STR_SELECT).append(
        FolderObjectIterator.getFieldsForSQL(STR_OT)).append(" FROM oxfolder_tree AS ot").append(" WHERE (cid = ?) ").toString();

    /**
     * Gets <b>all</b> modified folders since given time stamp.
     * <p>
     * Quote from <a href= "http://www.open-xchange.com/wiki/index.php?title=HTTP_API#Updates">HTTP API Updates</a>: <code>
     * ...
     * When requesting updates to a previously retrieved set of objects,
     * the client sends the last timestamp which belongs to that set of objects.
     * The response contains all updates with timestamps greater than the one
     * specified by the client. The field timestamp of the response contains the
     * new maximum timestamp value.
     * ...
     * </code>
     * 
     * @param since The time stamp
     * @param ctx The context
     * @return <b>All</b> modified folders since given time stamp
     * @throws OXException If a folder error occurs
     * @throws SearchIteratorException If a search iterator error occurs
     */
    public static SearchIterator<FolderObject> getAllModifiedFoldersSince(final Date since, final Context ctx) throws OXException, SearchIteratorException {
        return getAllModifiedFoldersSince(since, ctx, null);
    }

    /**
     * Gets <b>all</b> modified folders since given time stamp.
     * <p>
     * Quote from <a href= "http://www.open-xchange.com/wiki/index.php?title=HTTP_API#Updates">HTTP API Updates</a>: <code>
     * ...
     * When requesting updates to a previously retrieved set of objects,
     * the client sends the last timestamp which belongs to that set of objects.
     * The response contains all updates with timestamps greater than the one
     * specified by the client. The field timestamp of the response contains the
     * new maximum timestamp value.
     * ...
     * </code>
     * 
     * @param since The time stamp
     * @param ctx The context
     * @param con The connection to use
     * @return <b>All</b> modified folders since given time stamp
     * @throws OXException If a folder error occurs
     * @throws SearchIteratorException If a search iterator error occurs
     */
    public static SearchIterator<FolderObject> getAllModifiedFoldersSince(final Date since, final Context ctx, final Connection con) throws OXException, SearchIteratorException {
        final String sqlSelectStr = new StringBuilder(256).append(SQL_SELECT_FOLDERS_START).append(
            "AND (changing_date > ?) AND (module IN ").append(FolderObject.SQL_IN_STR_STANDARD_MODULES_ALL).append(") ").append(
            OXFolderProperties.isEnableDBGrouping() ? getGroupBy(STR_OT) : null).append(" ORDER by ot.fuid").toString();
        Connection readCon = con;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final int contextId = ctx.getContextId();
        try {
            if (null == readCon) {
                readCon = DBPool.pickup(ctx);
                closeCon = true;
            }
            stmt = readCon.prepareStatement(sqlSelectStr);
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setLong(pos, since.getTime());
            rs = stmt.executeQuery();
        } catch (final SQLException e) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(contextId));
        } catch (final DBPoolingException e) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.DBPOOLING_ERROR, e, Integer.valueOf(contextId));
        } catch (final Throwable t) {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            throw new OXFolderException(FolderCode.RUNTIME_ERROR, t, Integer.valueOf(contextId));
        }
        return new FolderObjectIterator(rs, stmt, false, ctx, readCon, closeCon);
    }

}
