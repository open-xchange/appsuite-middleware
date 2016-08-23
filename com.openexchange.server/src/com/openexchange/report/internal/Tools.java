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

package com.openexchange.report.internal;

import static com.openexchange.tools.sql.DBUtils.IN_LIMIT;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getIN;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.tools.sql.DBUtils;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class Tools {

    public static List<Integer> getContextInSameSchema(int contextId) throws OXException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        int[] contextsInSameSchema = dbService.getContextsInSameSchema(contextId);
        return java.util.Arrays.asList(ArrayUtils.toObject(contextsInSameSchema));
    }

    public static List<Integer> getAllContextIds() throws OXException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);

        final List<Integer> retval = new ArrayList<Integer>();
        PreparedStatement stmt = null;
        ResultSet result = null;
        Connection readConnection = null;
        try {
            readConnection = dbService.getReadOnly();
            stmt = readConnection.prepareStatement("SELECT cid FROM context");
            result = stmt.executeQuery();
            while (result.next()) {
                retval.add(Integer.valueOf(result.getInt(1)));
            }
        } catch (final SQLException e) {
            throw ContextExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            dbService.backReadOnly(readConnection);
        }
        return retval;
    }

    public static User[] getUser(int contextId) throws OXException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Connection readOnly = null;

        final List<User> users = new ArrayList<User>();
        try {
            readOnly = dbService.getReadOnly(contextId);
            int[] userIds = listAllUser(contextId, readOnly);

            final int length = userIds.length;
            if (0 == length) {
                return new User[0];
            }
            for (int i = 0; i < length; i += IN_LIMIT) {
                PreparedStatement stmt = null;
                ResultSet result = null;
                try {
                    final int[] currentUserIds = Arrays.extract(userIds, i, IN_LIMIT);
                    stmt = readOnly.prepareStatement(getIN("SELECT id,mailEnabled,mail,guestCreatedBy FROM user WHERE user.cid=? AND id IN (", currentUserIds.length));
                    int pos = 1;
                    stmt.setInt(pos++, contextId);
                    for (final int userId : currentUserIds) {
                        stmt.setInt(pos++, userId);
                    }
                    result = stmt.executeQuery();
                    while (result.next()) {
                        final UserImpl user = new UserImpl();
                        pos = 1;
                        user.setId(result.getInt(pos++));
                        user.setMailEnabled(result.getBoolean(pos++));
                        user.setMail(result.getString(pos++));
                        // 'guestCreatedBy'
                        user.setCreatedBy(result.getInt(pos++));

                        users.add(user);
                    }
                } finally {
                    closeSQLStuff(result, stmt);
                }
            }
        } catch (final SQLException e) {
            throw UserExceptionCode.LOAD_FAILED.create(e, e.getMessage());
        } finally {
            dbService.backReadOnly(contextId, readOnly);
        }
        return users.toArray(new User[users.size()]);
    }

    public static int getNumberOfUsers(List<Integer> contextIds) throws OXException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Connection readOnly = null;
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            readOnly = dbService.getReadOnly(contextIds.get(0).intValue());

            stmt = readOnly.prepareStatement(getIN("SELECT COUNT(id) AS total FROM user WHERE user.guestCreatedBy = 0 AND user.cid IN (", contextIds.size()));
            int pos = 1;
            for (final int contextId : contextIds) {
                stmt.setInt(pos++, contextId);
            }
            result = stmt.executeQuery();
            if (result.next()) {
                return result.getInt("total");
            }
        } catch (final SQLException e) {
            throw UserExceptionCode.LOAD_FAILED.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            dbService.backReadOnly(contextIds.get(0).intValue(), readOnly);
        }
        return 0;
    }

    public static int getNumberOfGuests(List<Integer> contextIds) throws OXException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Connection readOnly = null;
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            readOnly = dbService.getReadOnly(contextIds.get(0).intValue());

            stmt = readOnly.prepareStatement(getIN("SELECT COUNT(id) AS total FROM user WHERE user.guestCreatedBy > 0 AND user.mail <> '' AND user.cid IN (", contextIds.size()));
            int pos = 1;
            for (final int contextId : contextIds) {
                stmt.setInt(pos++, contextId);
            }
            result = stmt.executeQuery();

            if (result.next()) {
                return result.getInt("total");
            }
        } catch (final SQLException e) {
            throw UserExceptionCode.LOAD_FAILED.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            dbService.backReadOnly(contextIds.get(0).intValue(), readOnly);
        }
        return 0;
    }

    public static int getNumberOfLinks(List<Integer> contextIds) throws OXException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Connection readOnly = null;
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            readOnly = dbService.getReadOnly(contextIds.get(0).intValue());

            stmt = readOnly.prepareStatement(getIN("SELECT COUNT(id) AS total FROM user WHERE user.guestCreatedBy > 0 AND user.mail = '' AND user.cid IN (", contextIds.size()));
            int pos = 1;
            for (final int contextId : contextIds) {
                stmt.setInt(pos++, contextId);
            }
            result = stmt.executeQuery();

            if (result.next()) {
                return result.getInt("total");
            }
        } catch (final SQLException e) {
            throw UserExceptionCode.LOAD_FAILED.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            dbService.backReadOnly(contextIds.get(0).intValue(), readOnly);
        }
        return 0;
    }

    private static int[] listAllUser(int contextID, Connection con) throws OXException {
        StringBuilder stringBuilder = new StringBuilder("SELECT id FROM user WHERE cid=?");
        String sql = stringBuilder.toString();
        final int[] users;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, contextID);
            result = stmt.executeQuery();
            final TIntList tmp = new TIntArrayList();
            while (result.next()) {
                tmp.add(result.getInt(1));
            }
            users = tmp.toArray();
        } catch (final SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return users;
    }

    public static final Map<String, Integer> getAllSchemata(final org.slf4j.Logger logger) throws SQLException, OXException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        final Map<String, Integer> schemaMap = new LinkedHashMap<String, Integer>(50); // Keep insertion order
        {
            final Connection readcon;
            try {
                readcon = dbService.getReadOnly();
            } catch (final OXException e) {
                logger.error("", e);
                throw e;
            }
            /*
             * Get all schemas and put them into a map.
             */
            Statement statement = null;
            ResultSet rs = null;
            try {
                statement = readcon.createStatement();
                rs = statement.executeQuery("SELECT read_db_pool_id, db_schema FROM context_server2db_pool GROUP BY db_schema");
                while (rs.next()) {
                    schemaMap.put(rs.getString(2), Integer.valueOf(rs.getInt(1)));
                }
            } catch (final SQLException e) {
                logger.error("", e);
                throw e;
            } finally {
                DBUtils.closeSQLStuff(rs, statement);
                dbService.backReadOnly(readcon);
            }
        }
        return schemaMap;
    }
}
