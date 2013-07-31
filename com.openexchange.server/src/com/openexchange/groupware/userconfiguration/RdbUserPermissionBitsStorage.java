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

package com.openexchange.groupware.userconfiguration;

import static com.openexchange.tools.sql.DBUtils.closeResources;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.impl.DBPool;


/**
 * {@link RdbUserPermissionBitsStorage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RdbUserPermissionBitsStorage extends UserPermissionBitsStorage {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(RdbUserPermissionBitsStorage.class);

    /**
     * Initializes a new {@link RdbUserPermissionBitsStorage}.
     */
    public RdbUserPermissionBitsStorage() {
        super();
    }

    @Override
    public UserPermissionBits getUserPermissionBits(int userId, Context ctx) throws OXException {
        try {
            return loadUserPermissionBits(userId, ctx);
        } catch (SQLException e) {
            throw UserConfigurationCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }


    @Override
    public UserPermissionBits[] getUserPermissionBits(final Context ctx, final User[] users) throws OXException {
        try {
            int[] userIds = new int[users.length];
            for(int i = 0; i < users.length; i++) {
                userIds[i] = users[i].getId();
            }
            return loadUserPermissionBits(ctx, null, userIds);
        } catch (final SQLException e) {
            throw UserConfigurationCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public UserPermissionBits[] getUserPermissionBits(Context ctx, int[] userIds) throws OXException {
        try {
            return loadUserPermissionBits(ctx, null, userIds);
        } catch (final SQLException e) {
            throw UserConfigurationCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }


    @Override
    public void clearStorage() {
    }


    @Override
    public void removeUserPermissionBits(final int userId, final Context ctx) {

    }


    @Override
    public void saveUserPermissionBits(final int permissionBits, final int userId, final Context ctx) throws OXException {
        saveUserPermissionBits0(permissionBits, userId, ctx);
    }

    /*-
     * ------------- Methods for saving -------------
     */

    /**
     * Saves given user permission bits to database. If <code>insert</code> is <code>true</code> an INSERT command is performed, otherwise an
     * UPDATE command.
     *
     * @param perms - the user permission bits to save
     * @param insert - <code>true</code> for an INSERT; otherwise UPDATE
     * @param writeCon - the writable connection; may be <code>null</code>
     * @throws SQLException - if saving fails due to a SQL error
     * @throws OXException - if a writable connection could not be obtained from database
     */
    public static void saveUserPermissionBits(final UserPermissionBits perms, final boolean insert, final Connection writeCon) throws SQLException, OXException {
        saveUserPermissionBits(perms.getPermissionBits(), perms.getUserId(), insert, perms.getContextId(), writeCon);
    }

    private static Context getContext(UserPermissionBits perms) throws OXException {
        return ContextStorage.getInstance().getContext(perms.getContextId());
    }

    private static final String SQL_SELECT = "SELECT user FROM user_configuration WHERE cid = ? AND user = ?";

    /**
     * Saves given user permissions to database by self-determining if an INSERT or UPDATE is going to be performed.
     *
     * @param permissionBits The permission bits.
     * @param userId The user ID.
     * @param ctx The context the user belongs to.
     * @throws OXException - if saving fails
     */
    private static void saveUserPermissionBits0(final int permissionBits, final int userId, final Context ctx) throws OXException {
        boolean insert = false;
        try {
            Connection readCon = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                readCon = DBPool.pickup(ctx);
                stmt = readCon.prepareStatement(SQL_SELECT);
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, userId);
                rs = stmt.executeQuery();
                insert = !rs.next();
            } finally {
                closeResources(rs, stmt, readCon, true, ctx);
            }
            saveUserPermissionBits(permissionBits, userId, insert, ctx.getContextId(), null);
        } catch (final SQLException e) {
            throw UserConfigurationCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private static final String INSERT_USER_CONFIGURATION = "INSERT INTO user_configuration (cid, user, permissions) VALUES (?, ?, ?)";

    private static final String UPDATE_USER_CONFIGURATION = "UPDATE user_configuration SET permissions = ? WHERE cid = ? AND user = ?";

    /**
     * Saves given user permissions to database. If <code>insert</code> is <code>true</code> an INSERT command is performed, otherwise an
     * UPDATE command.
     *
     * @param permissionBits The permission bits.
     * @param userId The user ID.
     * @param insert - <code>true</code> for an INSERT; otherwise UPDATE
     * @param ctx - the context
     * @param writeConArg - the writable connection; may be <code>null</code>
     * @throws SQLException If saving fails due to a SQL error
     * @throws OXException If a writable connection could not be obtained from database
     */
    public static void saveUserPermissionBits(final int permissionBits, final int userId, final boolean insert, final int ctxId, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        boolean closeConnection = false;
        PreparedStatement stmt = null;
        ContextImpl ctx = new ContextImpl(ctxId);
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeConnection = true;
            }
            if (insert) {
                stmt = writeCon.prepareStatement(INSERT_USER_CONFIGURATION);
                stmt.setInt(1, ctxId);
                stmt.setInt(2, userId);
                stmt.setInt(3, permissionBits);
            } else {
                stmt = writeCon.prepareStatement(UPDATE_USER_CONFIGURATION);
                stmt.setInt(1, permissionBits);
                stmt.setInt(2, ctxId);
                stmt.setInt(3, userId);
            }
            stmt.executeUpdate();
            if (!insert) {
                try {
                    UserConfigurationStorage.getInstance().invalidateCache(userId, ctx);
                } catch (final OXException e) {
                    LOG.warn("User Configuration could not be removed from cache", e);
                }
            }
        } finally {
            closeResources(null, stmt, closeConnection ? writeCon : null, false, ctx);
        }
    }

    /*-
     * ------------- Methods for loading -------------
     */

    /**
     * Loads the user permissions from database specified through user ID and context
     *
     * @param userId - the user ID
     * @param ctx - the context
     * @return the instance of <code>{@link UserPermissionBits}</code>
     * @throws SQLException - if user configuration could not be loaded from database
     * @throws OXException - if user's groups are <code>null</code> and could not be determined by <code>{@link UserStorage}</code>
     *             implementation
     */
    public static UserPermissionBits loadUserPermissionBits(final int userId, final Context ctx) throws SQLException, OXException {
        return loadUserPermissionBits(userId, ctx, null);
    }

    /**
     * Special method invoked by admin to load user permissions since no exception is thrown if no matching config could be found. In this
     * case an instance of {@link UserPermissionBits} is returned that does not hold any permissions.
     *
     * @param userId - the user ID
     * @param groups - the group IDs the user belongs to; may be <code>null</code>
     * @param cid - the context ID
     * @param readConArg - the readable context; may be <code>null</code>
     * @return the instance of <code>{@link UserPermissionBits}</code>
     * @throws SQLException - if user configuration could not be loaded from database
     * @throws OXException - if a readable connection could not be obtained from connection pool
     */
    public static UserPermissionBits adminLoadUserPermissionBits(final int userId, final int[] groups, final int cid, final Connection readConArg) throws SQLException, OXException {
        final Context ctx = new ContextImpl(cid);
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(LOAD_USER_CONFIGURATION);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            return rs.next() ? new UserPermissionBits(rs.getInt(1) , userId, groups, ctx.getContextId()) : new UserPermissionBits(0, userId, groups, ctx.getContextId());
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
    }

    /**
     * Counts all users with the permission as set in {@link UserPermissionBits} object
     *
     * @param cid - the context id
     * @param permissions {@link UserPermissionBits} object containing set of permissions to count
     * @param readConArg - the readable context; may be <code>null</code>
     * @return number of users with permission as set in {@link UserPermissionBits}
     * @throws SQLException
     * @throws OXException
     */
    public static int adminCountUsersByPermission(final int cid, final UserPermissionBits permissions, final Connection readConArg) throws SQLException, OXException {
        final Context ctx = new ContextImpl(cid);
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(COUNT_USERS_BY_PERMISSION);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, permissions.getPermissionBits());
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
    }

    private static final String LOAD_USER_CONFIGURATION = "SELECT permissions FROM user_configuration WHERE cid = ? AND user = ?";

    private static final String LOAD_SOME_USER_CONFIGURATIONS = "SELECT user,permissions FROM user_configuration WHERE cid=? AND user IN (";

    private static final String COUNT_USERS_BY_PERMISSION =
        "SELECT COUNT(permissions) FROM user_configuration WHERE cid = ? AND permissions = ?";

    /**
     * Loads the user permissions from database specified through user ID and context
     *
     * @param userId - the user ID
     * @param groupsArg - the group IDs the user belongs to; may be <code>null</code>
     * @param ctx - the context
     * @param readConArg - the readable context; may be <code>null</code>
     * @return the instance of <code>{@link UserPermissionBits}</code>
     * @throws SQLException - if user configuration could not be loaded from database
     * @throws OXException - if user's groups are <code>null</code> and could not be determined by <code>{@link UserStorage}</code>
     *             implementation
     * @throws OXException - if a readable connection could not be obtained from connection pool
     * @throws OXException - if no matching user configuration is kept in database
     */
    public static UserPermissionBits loadUserPermissionBits(final int userId, final Context ctx, final Connection readConArg) throws SQLException, OXException {
        Connection readCon = readConArg;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeCon = true;
            }
            stmt = readCon.prepareStatement(LOAD_USER_CONFIGURATION);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw UserConfigurationCodes.NOT_FOUND.create(Integer.valueOf(userId), Integer.valueOf(ctx.getContextId()));
            }
            return new UserPermissionBits(rs.getInt(1), userId, ctx.getContextId());
        } finally {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
        }
    }

    private static final int LIMIT = 1000;

    public static UserPermissionBits[] loadUserPermissionBits(final Context ctx, final Connection conArg, final int[] userIds) throws OXException, SQLException {
        final int length = userIds.length;
        if (0 == length) {
            return new UserPermissionBits[0];
        }
        final Connection con;
        final boolean closeCon;
        if (null == conArg) {
            con = DBPool.pickup(ctx);
            closeCon = true;
        } else {
            con = conArg;
            closeCon = false;
        }
        PreparedStatement stmt = null;
        ResultSet result = null;
        final UserPermissionBits[] retval = new UserPermissionBits[length];
        try {
            final TIntIntMap userMap;
            if (length <= LIMIT) {
                final StringBuilder sb = new StringBuilder(512);
                sb.append("SELECT u.user, u.permissions FROM user_configuration AS u");
                if (1 == length) {
                    sb.append(" WHERE u.user = ? AND u.cid = ?");
                } else {
                    sb.append(" INNER JOIN (");
                    sb.append("SELECT ? AS user");
                    for (int i = 1; i < length; i++) {
                        sb.append(" UNION ALL SELECT ?");
                    }
                    sb.append(") AS x ON u.user = x.user WHERE u.cid = ?");
                }
                stmt = con.prepareStatement(sb.toString());
                int pos = 1;
                userMap = new TIntIntHashMap(length, 1);
                for (int index = 0; index < length; index++) {
                    final int userId = userIds[index];
                    stmt.setInt(pos++,userId);
                    userMap.put(userId, index);
                }
                stmt.setInt(pos++, ctx.getContextId());
            } else {
                stmt = con.prepareStatement("SELECT u.user, u.permissions FROM user_configuration AS u WHERE u.cid = ?");
                userMap = new TIntIntHashMap(length, 1);
                for (int index = 0; index < length; index++) {
                    userMap.put(userIds[index], index);
                }
                stmt.setInt(1, ctx.getContextId());
            }
            result = stmt.executeQuery();

            while (result.next()) {
                final int userId = result.getInt(1);
                if (userMap.containsKey(userId)) {
                    final int index = userMap.get(userId);
                    retval[index] = new UserPermissionBits(result.getInt(2), userId, ctx.getContextId());
                }
            }
        } finally {
            closeResources(result, stmt, closeCon ? con : null, true, ctx);
        }
        return retval;
    }

    /*-
     * ------------- Methods for deleting -------------
     */

    /**
     * Deletes the user configuration from database specified through ID and context. This is a convenience method that delegates invokation
     * to <code>{@link #deleteUserPermissionBits(int, Connection, Context)}</code>. whereby connection is set to <code>null</code>, thus a
     * new writeable connection is going to be obtained from connection pool.
     *
     * @param userId - the user ID
     * @param ctx - the context
     * @throws SQLException - if user configuration cannot be removed from database
     * @throws OXException If no writeable connection could be obtained
     */
    public static void deleteUserPermissionBits(final int userId, final Context ctx) throws SQLException, OXException {
        RdbUserPermissionBitsStorage.deleteUserPermissionBits(userId, null, ctx);
    }

    private static final String DELETE_USER_CONFIGURATION = "DELETE FROM user_configuration WHERE cid = ? AND user = ?";

    /**
     * Deletes the user configuration from database specified through ID and context.
     *
     * @param userId - the user ID
     * @param writeConArg - the writeable connection
     * @param ctx - the context
     * @throws SQLException - if user configuration cannot be removed from database
     * @throws OXException - if no writeable connection could be obtained
     */
    public static void deleteUserPermissionBits(final int userId, final Connection writeConArg, final Context ctx) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            stmt = writeCon.prepareStatement(DELETE_USER_CONFIGURATION);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            try {
                UserConfigurationStorage.getInstance().invalidateCache(userId, ctx);
            } catch (final OXException e) {
                LOG.warn("User Configuration could not be removed from cache", e);
            }
        } finally {
            closeResources(null, stmt, closeWriteCon ? writeCon : null, false, ctx);
        }
    }
}
