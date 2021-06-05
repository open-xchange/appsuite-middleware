/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.report.internal;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.IN_LIMIT;
import static com.openexchange.tools.sql.DBUtils.getIN;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.ArrayUtils;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.arrays.Arrays;
import com.openexchange.user.User;
import com.openexchange.user.UserExceptionCode;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class Tools {

    private static final String TOTAL = "total";

    public static Map<PoolAndSchema, List<Integer>> getSchemaAssociations() throws OXException {
        return ContextStorage.getInstance().getSchemaAssociations();
    }

    public static List<Integer> getContextInSameSchema(int contextId) throws OXException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        int[] contextsInSameSchema = dbService.getContextsInSameSchema(contextId);
        return java.util.Arrays.asList(ArrayUtils.toObject(contextsInSameSchema));
    }

    public static List<Integer> getAllContextIds() throws OXException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);

        final List<Integer> retval = new ArrayList<>();
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
        } catch (SQLException e) {
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

        final List<User> users = new ArrayList<>();
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
        } catch (SQLException e) {
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
                return result.getInt(TOTAL);
            }
        } catch (SQLException e) {
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
                return result.getInt(TOTAL);
            }
        } catch (SQLException e) {
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
                return result.getInt(TOTAL);
            }
        } catch (SQLException e) {
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
        } catch (SQLException e) {
            throw UserExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return users;
    }

    public static final Map<String, Integer> getAllSchemata(final org.slf4j.Logger logger) throws SQLException, OXException {
        DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        Connection readcon = dbService.getReadOnly();
        try {
            return dbService.getAllSchemata(readcon);
        } catch (OXException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SQLException) {
                SQLException sqle = (SQLException) cause;
                logger.error("", sqle);
                throw sqle;
            }
            logger.error("", e);
            throw e;
        } finally {
            dbService.backReadOnly(readcon);
        }
    }
}
