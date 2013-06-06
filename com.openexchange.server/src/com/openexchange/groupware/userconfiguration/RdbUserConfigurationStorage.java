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
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getIN;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import com.openexchange.capabilities.Capability;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link RdbUserConfigurationStorage} - The database storage implementation of a user configuration storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RdbUserConfigurationStorage extends UserConfigurationStorage {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(RdbUserConfigurationStorage.class);

    /**
     * Initializes a new {@link RdbUserConfigurationStorage}
     */
    public RdbUserConfigurationStorage() {
        super();
    }

    @Override
    protected void startInternal() {
        /*
         * Nothing to start
         */
    }

    @Override
    protected void stopInternal() {
        /*
         * Nothing to stop
         */
    }
    
    @Override
    public UserConfiguration getUserConfiguration(int userId, int[] groups, Context ctx) throws OXException {
        try {
            return loadUserConfiguration(userId, groups, ctx);
        } catch (SQLException e) {
            throw UserConfigurationCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public UserConfiguration[] getUserConfiguration(final Context ctx, final User[] users) throws OXException {
        try {
            return loadUserConfiguration(ctx, null, users);
        } catch (final SQLException e) {
            throw UserConfigurationCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }
    

    @Override
    public void clearStorage() {
        /*
         * Since this storage implementation directly fetches data from database this method has no effect
         */
    }

    @Override
    public void invalidateCache(final int userId, final Context ctx) {
        /*
         * Since this storage implementation directly fetches data from database this method has no effect
         */
    }
    
    /*-
     * ------------- Methods for loading -------------
     */

    /**
     * Loads the user configuration from database specified through user ID and context
     *
     * @param userId - the user ID
     * @param ctx - the context
     * @return the instance of <code>{@link UserConfiguration}</code>
     * @throws SQLException - if user configuration could not be loaded from database
     * @throws OXException - if user's groups are <code>null</code> and could not be determined by <code>{@link UserStorage}</code>
     *             implementation
     */
    public static UserConfiguration loadUserConfiguration(final int userId, final Context ctx) throws SQLException, OXException {
        return loadUserConfiguration(userId, null, ctx, true, null);
    }

    /**
     * Loads the user configuration from database specified through user ID and context
     *
     * @param userId - the user ID
     * @param groups - the group IDs the user belongs to; may be <code>null</code>
     * @param ctx - the context
     * @return the instance of <code>{@link UserConfiguration}</code>
     * @throws SQLException - if user configuration could not be loaded from database
     * @throws OXException - if user's groups are <code>null</code> and could not be determined by <code>{@link UserStorage}</code>
     *             implementation
     */
    public static UserConfiguration loadUserConfiguration(final int userId, final int[] groups, final Context ctx) throws SQLException, OXException {
        return loadUserConfiguration(userId, groups, ctx, true, null);
    }

    private static Set<String> getCapabilities(final int userId, final int cid) throws OXException {
        CapabilityService capabilities = ServerServiceRegistry.getInstance().getService(CapabilityService.class);
        if (capabilities == null) {
            return new HashSet<String>();
        }
        return stringify(capabilities.getCapabilities(userId, cid));
    }

    private static Set<String> stringify(Set<Capability> capabilities) {
        Set<String> set = new HashSet<String>(capabilities.size());
        for (Capability capability : capabilities) {
            set.add(capability.getId().toLowerCase());
        }
        
        return set;
    }

    private static final String LOAD_USER_CONFIGURATION = "SELECT permissions FROM user_configuration WHERE cid = ? AND user = ?";

    /**
     * Loads the user configuration from database specified through user ID and context
     *
     * @param userId - the user ID
     * @param groupsArg - the group IDs the user belongs to; may be <code>null</code>
     * @param ctx - the context
     * @param readConArg - the readable context; may be <code>null</code>
     * @return the instance of <code>{@link UserConfiguration}</code>
     * @throws SQLException - if user configuration could not be loaded from database
     * @throws OXException - if user's groups are <code>null</code> and could not be determined by <code>{@link UserStorage}</code>
     *             implementation
     * @throws OXException - if a readable connection could not be obtained from connection pool
     * @throws OXException - if no matching user configuration is kept in database
     */
    public static UserConfiguration loadUserConfiguration(final int userId, final int[] groupsArg, final Context ctx, final boolean calcPerms, final Connection readConArg) throws SQLException, OXException {
        final int[] groups = groupsArg == null ? UserStorage.getInstance().getUser(userId, ctx).getGroups() : groupsArg;
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
            final UserConfiguration userConfiguration = new UserConfiguration(getCapabilities(userId, ctx.getContextId()), userId, groups, ctx);
            
            return userConfiguration;
        } finally {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
        }
    }

    private static final int LIMIT = 1000;

    public static UserConfiguration[] loadUserConfiguration(final Context ctx, final Connection conArg, final User[] users) throws OXException, SQLException {
        final int length = users.length;
        if (0 == length) {
            return new UserConfiguration[0];
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
        final UserConfiguration[] retval = new UserConfiguration[length];
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
                    final User user = users[index];
                    stmt.setInt(pos++, user.getId());
                    userMap.put(user.getId(), index);
                }
                stmt.setInt(pos++, ctx.getContextId());
            } else {
                stmt = con.prepareStatement("SELECT u.user, u.permissions FROM user_configuration AS u WHERE u.cid = ?");
                userMap = new TIntIntHashMap(length, 1);
                for (int index = 0; index < length; index++) {
                    userMap.put(users[index].getId(), index);
                }
                stmt.setInt(1, ctx.getContextId());
            }
            result = stmt.executeQuery();
            
            while (result.next()) {
                final int userId = result.getInt(1);
                if (userMap.containsKey(userId)) {
                    final int index = userMap.get(userId);
                    final User user = users[index];
                    final UserConfiguration userConfiguration = new UserConfiguration(getCapabilities(user.getId(), ctx.getContextId()), user.getId(), user.getGroups(), ctx);
                    
                    retval[index] = userConfiguration;
                }
            }
        } finally {
            closeResources(result, stmt, closeCon ? con : null, true, ctx);
        }
        return retval;
    }
    
    @Override
    public UserConfiguration[] getUserConfigurations(Context ctx, int[] userIds, int[][] groups) throws OXException {
        if (0 == userIds.length) {
            return new UserConfiguration[0];
        }
        Connection con = Database.get(ctx, false);
        try {
            return loadUserConfigurations(ctx, con, userIds, groups);
        } catch (SQLException e) {
            throw UserConfigurationCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Database.back(ctx, false, con);
        }
    }

    private static UserConfiguration[] loadUserConfigurations(Context ctx, Connection con, int[] userIds, int[][] groupsArg) throws OXException, SQLException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            final int length = userIds.length;
            final TIntIntMap userMap;
            if (length <= LIMIT) {
                stmt = con.prepareStatement(getIN("SELECT user,permissions FROM user_configuration WHERE cid=? AND user IN (", length));
                int pos = 1;
                stmt.setInt(pos++, ctx.getContextId());
                userMap = new TIntIntHashMap(length, 1);
                for (int i = 0; i < length; i++) {
                    stmt.setInt(pos++, userIds[i]);
                    userMap.put(userIds[i], i);
                }
            } else {
                stmt = con.prepareStatement("SELECT user,permissions FROM user_configuration WHERE cid=?");
                stmt.setInt(1, ctx.getContextId());
                userMap = new TIntIntHashMap(length, 1);
                for (int i = 0; i < length; i++) {
                    userMap.put(userIds[i], i);
                }
            }
            result = stmt.executeQuery();
            final List<UserConfiguration> list = new ArrayList<UserConfiguration>(length);
            while (result.next()) {
                final int userId = result.getInt(1);
                if (userMap.containsKey(userId)) {
                    final int pos = userMap.get(userId);
                    final int[] groups = groupsArg[pos] == null ? UserStorage.getInstance().getUser(userId, ctx).getGroups() : groupsArg[pos];
                    list.add(new UserConfiguration(getCapabilities(userId, ctx.getContextId()), userId, groups, ctx));
                }
            }
            return list.toArray(new UserConfiguration[0]);
        } finally {
            closeSQLStuff(result, stmt);
        }
    }
}
