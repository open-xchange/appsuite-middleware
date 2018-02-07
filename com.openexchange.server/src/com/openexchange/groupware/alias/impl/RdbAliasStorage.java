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

package com.openexchange.groupware.alias.impl;

import static com.openexchange.database.Databases.IN_LIMIT;
import static com.openexchange.database.Databases.getIN;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.groupware.alias.UserAliasStorageExceptionCodes;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.arrays.Arrays;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link RdbAliasStorage}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.0
 */
public class RdbAliasStorage implements UserAliasStorage {

    /**
     * Initialises a new {@link RdbAliasStorage}.
     */
    public RdbAliasStorage() {
        super();
    }

    @Override
    public void invalidateAliases(int contextId, int userId) throws OXException {
        // Nothing
    }

    @Override
    public ImmutableSet<String> getAliases(int contextId) throws OXException {
        Connection con = Database.get(contextId, false);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT alias FROM user_alias WHERE cid=?");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return ImmutableSet.<String> builder().build();
            }

            ImmutableSet.Builder<String> aliases = ImmutableSet.builder();
            do {
                aliases.add(rs.getString(1));
            } while (rs.next());
            return aliases.build();
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            Database.back(contextId, false, con);
        }
    }

    @Override
    public ImmutableSet<String> getAliases(int contextId, int userId) throws OXException {
        Connection con = Database.get(contextId, false);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            int index = 0;
            stmt = con.prepareStatement("SELECT alias FROM user_alias WHERE cid=? AND user=?");
            stmt.setInt(++index, contextId);
            stmt.setInt(++index, userId);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return ImmutableSet.<String> builder().build();
            }

            ImmutableSet.Builder<String> aliases = ImmutableSet.builder();
            do {
                aliases.add(rs.getString(1));
            } while (rs.next());
            return aliases.build();
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            Database.back(contextId, false, con);
        }
    }

    @Override
    public List<Set<String>> getAliases(int contextId, int... userIds) throws OXException {
        if (null == userIds) {
            return Collections.emptyList();
        }

        int length = userIds.length;
        if (length == 0) {
            return Collections.emptyList();
        }

        if (1 == length) {
            return Collections.<Set<String>> singletonList(getAliases(contextId, userIds[0]));
        }

        TIntObjectMap<ImmutableSet<String>> map = getAliasesMapping(contextId, userIds);
        List<Set<String>> list = new ArrayList<>(length);
        for (int userId : userIds) {
            list.add(map.get(userId));
        }
        return list;
    }

    /**
     * Gets the aliases mapping for specified user identifiers
     *
     * @param contextId The context identifier
     * @param userIds The user identifiers
     * @return The alias mapping
     * @throws OXException If alias mapping cannot be returned
     */
    TIntObjectMap<ImmutableSet<String>> getAliasesMapping(int contextId, int[] userIds) throws OXException {
        Connection con = Database.get(contextId, false);
        try {
            int length = userIds.length;
            TIntObjectMap<ImmutableSet.Builder<String>> map = new TIntObjectHashMap<>(length);

            for (int i = 0; i < length; i += IN_LIMIT) {
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    int clen = Arrays.determineRealSize(length, i, IN_LIMIT);
                    stmt = con.prepareStatement(getIN("SELECT user, alias FROM user_alias WHERE cid=? AND user IN (", clen));
                    int pos = 1;
                    stmt.setInt(pos++, contextId);
                    for (int j = 0; j < clen; j++) {
                        int userId = userIds[i + j];
                        stmt.setInt(pos++, userId);
                        map.put(userId, ImmutableSet.<String> builder());
                    }
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        int userId = rs.getInt(1);
                        Builder<String> aliases = map.get(userId);
                        aliases.add(rs.getString(2));
                    }
                } catch (SQLException e) {
                    throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
                } finally {
                    Databases.closeSQLStuff(rs, stmt);
                }
            }

            TIntObjectMap<ImmutableSet<String>> retval = new TIntObjectHashMap<>(length);
            TIntObjectIterator<Builder<String>> iterator = map.iterator();
            for (int i = map.size(); i-- > 0;) {
                iterator.advance();
                retval.put(iterator.key(), iterator.value().build());
            }
            return retval;
        } finally {
            Database.back(contextId, false, con);
        }
    }

    @Override
    public int getUserId(int contextId, String alias) throws OXException {
        Connection con = Database.get(contextId, false);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            int index = 0;
            /*
             * Use utf8*_bin to match umlauts. But that also makes it case sensitive, so use LOWER to be case insensitive.
             */
            StringBuilder stringBuilder = new StringBuilder("SELECT user FROM user_alias WHERE cid=? AND LOWER(alias) LIKE LOWER(?) COLLATE ")
                .append(Databases.getCharacterSet(con).contains("utf8mb4") ? "utf8mb4_bin" : "utf8_bin");
            stmt = con.prepareStatement(stringBuilder.toString());
            stmt.setInt(++index, contextId);
            stmt.setString(++index, alias);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            Database.back(contextId, false, con);
        }
    }

    private void setAliases(int contextId, int userId, Set<String> aliases) throws OXException {
        Connection con = Database.get(contextId, true);
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            setAliases(con, contextId, userId, aliases);

            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    @Override
    public void setAliases(Connection con, int contextId, int userId, Set<String> aliases) throws OXException {
        if (con == null) {
            setAliases(contextId, userId, aliases);
            return;
        }

        PreparedStatement stmt = null;
        try {
            int index = 0;
            stmt = con.prepareStatement("DELETE FROM user_alias WHERE cid=? AND user=?");
            stmt.setInt(++index, contextId);
            stmt.setInt(++index, userId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            if (null != aliases && !aliases.isEmpty()) {
                stmt = con.prepareStatement("INSERT INTO user_alias (cid, user, alias, uuid) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE alias=?");
                for (String alias : aliases) {
                    index = 0;
                    stmt.setInt(++index, contextId);
                    stmt.setInt(++index, userId);
                    stmt.setString(++index, alias);
                    stmt.setBytes(++index, UUIDs.toByteArray(UUID.randomUUID()));
                    stmt.setString(++index, alias);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private boolean createAlias(int contextId, int userId, String alias) throws OXException {
        Connection con = Database.get(contextId, true);
        try {
            return createAlias(con, contextId, userId, alias);
        } finally {
            Database.back(contextId, true, con);
        }
    }

    @Override
    public boolean createAlias(Connection con, int contextId, int userId, String alias) throws OXException {
        if (con == null) {
            return createAlias(contextId, userId, alias);
        }

        PreparedStatement stmt = null;
        try {
            int index = 0;
            stmt = con.prepareStatement("INSERT INTO user_alias (cid, user, alias, uuid) VALUES(?,?,?,?)");
            stmt.setInt(++index, contextId);
            stmt.setInt(++index, userId);
            stmt.setString(++index, alias);
            stmt.setBytes(++index, UUIDs.toByteArray(UUID.randomUUID()));

            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            if (Databases.isPrimaryKeyConflictInMySQL(e)) {
                // Hide original exception, don't add to stack trace. See bug 50225
                throw UserAliasStorageExceptionCodes.DUPLICATE_ALIAS.create(alias);
            }
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private boolean updateAlias(int contextId, int userId, String oldAlias, String newAlias) throws OXException {
        Connection con = Database.get(contextId, true);
        try {
            return updateAlias(con, contextId, userId, oldAlias, newAlias);
        } finally {
            Database.back(contextId, true, con);
        }
    }

    @Override
    public boolean updateAlias(Connection con, int contextId, int userId, String oldAlias, String newAlias) throws OXException {
        if (con == null) {
            return updateAlias(contextId, userId, oldAlias, newAlias);
        }

        PreparedStatement stmt = null;
        try {
            int index = 0;
            stmt = con.prepareStatement("UPDATE user_alias SET alias=? WHERE cid=? AND user=? AND alias=?");
            stmt.setString(++index, newAlias);
            stmt.setInt(++index, contextId);
            stmt.setInt(++index, userId);
            stmt.setString(++index, oldAlias);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private boolean deleteAlias(int contextId, int userId, String alias) throws OXException {
        Connection con = Database.get(contextId, true);
        try {
            return deleteAlias(con, contextId, userId, alias);
        } finally {
            Database.back(contextId, true, con);
        }
    }

    @Override
    public boolean deleteAlias(Connection con, int contextId, int userId, String alias) throws OXException {
        if (con == null) {
            return deleteAlias(contextId, userId, alias);
        }

        PreparedStatement stmt = null;
        try {
            int index = 0;
            stmt = con.prepareStatement("DELETE FROM user_alias WHERE cid=? AND user=? AND alias=?");
            stmt.setInt(++index, contextId);
            stmt.setInt(++index, userId);
            stmt.setString(++index, alias);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private boolean deleteAliases(int contextId, int userId) throws OXException {
        Connection con = Database.get(contextId, true);
        try {
            return deleteAliases(con, contextId, userId);
        } finally {
            Database.back(contextId, true, con);
        }
    }

    @Override
    public boolean deleteAliases(Connection con, int contextId, int userId) throws OXException {
        if (con == null) {
            return deleteAliases(contextId, userId);
        }

        PreparedStatement stmt = null;
        try {
            int index = 0;
            stmt = con.prepareStatement("DELETE FROM user_alias WHERE cid=? AND user=?");
            stmt.setInt(++index, contextId);
            stmt.setInt(++index, userId);
            return stmt.executeUpdate() != 0;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public List<Integer> getUserIdsByAliasDomain(int contextId, String aliasDomain) throws OXException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con = Database.get(contextId, false);
            stmt = con.prepareStatement("SELECT us.id FROM user us LEFT JOIN user_alias la ON us.id = la.user AND us.cid = la.cid WHERE us.cid = ? and la.alias LIKE ?");
            stmt.setInt(1, contextId);
            stmt.setString(2, "%" + aliasDomain);
            rs = stmt.executeQuery();

            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<Integer> retval = new ArrayList<Integer>();
            do {
                retval.add(Integer.valueOf(rs.getInt(1)));
            } while (rs.next());
            return retval;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            Database.back(contextId, false, con);
        }
    }
}
