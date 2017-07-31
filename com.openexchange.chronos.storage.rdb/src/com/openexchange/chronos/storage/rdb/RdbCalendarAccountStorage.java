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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.storage.rdb;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.DefaultCalendarAccount;
import com.openexchange.chronos.storage.CalendarAccountStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbCalendarAccountStorage extends RdbStorage implements CalendarAccountStorage {

    private static final int ID_GENERATOR_TYPE = Types.SUBSCRIPTION; //TODO own type

    /**
     * Initializes a new {@link RdbCalendarStorage}.
     *
     * @param context The context
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    public RdbCalendarAccountStorage(Context context, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, dbProvider, txPolicy);
    }

    @Override
    public int insertAccount(String providerId, int userId, Map<String, Object> data) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            int id = nextAccountId(connection, context);
            updated = insertAccount(connection, context.getContextId(), id, providerId, userId, System.currentTimeMillis(), data);
            txPolicy.commit(connection);
            return id;
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public CalendarAccount loadAccount(int id) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAccount(connection, context.getContextId(), id);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public void updateAccount(int id, Map<String, Object> data) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = updateAccount(connection, context.getContextId(), id, System.currentTimeMillis(), data);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAccount(int id) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            updated = deleteAccount(connection, context.getContextId(), id);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public List<CalendarAccount> loadAccounts(int userId) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAccounts(connection, context.getContextId(), userId);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    private static int insertAccount(Connection connection, int cid, int id, String provider, int user, long modified, Map<String, Object> data) throws SQLException, OXException {
        String sql = "INSERT INTO calendar_account (cid,id,provider,user,modified,data) VALUES (?,?,?,?,?,?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            InputStream inputStream = null;
            try {
                inputStream = serializeMap(data);
                stmt.setInt(1, cid);
                stmt.setInt(2, id);
                stmt.setString(3, provider);
                stmt.setInt(4, user);
                stmt.setLong(5, modified);
                stmt.setBinaryStream(6, inputStream);
                return logExecuteUpdate(stmt);
            } finally {
                Streams.close(inputStream);
            }
        }
    }

    private static int updateAccount(Connection connection, int cid, int id, long modified, Map<String, Object> data) throws SQLException, OXException {
        String sql = "UPDATE calendar_account SET modified=?,data=? WHERE cid=? AND id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            InputStream inputStream = null;
            try {
                inputStream = serializeMap(data);
                stmt.setLong(1, modified);
                stmt.setBinaryStream(2, inputStream);
                stmt.setInt(3, cid);
                stmt.setInt(4, id);
                return logExecuteUpdate(stmt);
            } finally {
                Streams.close(inputStream);
            }
        }
    }

    private static int deleteAccount(Connection connection, int cid, int id) throws SQLException, OXException {
        String sql = "DELETE FROM calendar_account WHERE cid=? AND id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cid);
            stmt.setInt(2, id);
            return logExecuteUpdate(stmt);
        }
    }

    private static List<CalendarAccount> selectAccounts(Connection connection, int cid, int user) throws SQLException, OXException {
        List<CalendarAccount> accounts = new ArrayList<CalendarAccount>();
        String sql = "SELECT id,provider,modified,data FROM calendar_account WHERE cid=? AND user=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    int id = resultSet.getInt(1);
                    String providerId = resultSet.getString(2);
                    long lastModified = resultSet.getLong(3);
                    Map<String, Object> data;
                    InputStream inputStream = null;
                    try {
                        inputStream = resultSet.getBinaryStream(4);
                        data = deserializeMap(inputStream);
                    } finally {
                        Streams.close(inputStream);
                    }
                    accounts.add(new DefaultCalendarAccount(providerId, id, user, data, new Date(lastModified)));
                }
            }
        }
        return accounts;
    }

    private static CalendarAccount selectAccount(Connection connection, int cid, int id) throws SQLException, OXException {
        String sql = "SELECT provider,user,modified,data FROM calendar_account WHERE cid=? AND id=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cid);
            stmt.setInt(2, id);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                if (resultSet.next()) {
                    String providerId = resultSet.getString(1);
                    int userId = resultSet.getInt(2);
                    long lastModified = resultSet.getLong(3);
                    Map<String, Object> data;
                    InputStream inputStream = null;
                    try {
                        inputStream = resultSet.getBinaryStream(4);
                        data = deserializeMap(inputStream);
                    } finally {
                        Streams.close(inputStream);
                    }
                    return new DefaultCalendarAccount(providerId, id, userId, data, new Date(lastModified));
                }
            }
        }
        return null;
    }

    private static int nextAccountId(Connection connection, Context context) throws SQLException {
        if (connection.getAutoCommit()) {
            throw new SQLException("Generating unique identifier is threadsafe if and only if it is executed in a transaction.");
        }
        return IDGenerator.getId(context, ID_GENERATOR_TYPE, connection);
    }

    /**
     * Deserializes a an arbitrary map (as used in an account's configuration field) from the supplied input stream.
     *
     * @param inputStream The input stream to deserialize
     * @return The deserialized map
     */
    private static Map<String, Object> deserializeMap(InputStream inputStream) throws OXException {
        if (null == inputStream) {
            return Collections.emptyMap();
        }
        try {
            return new JSONObject(new AsciiReader(inputStream)).asMap();
        } catch (JSONException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Serializes an arbitrary meta map (as used in an account's configuration field) to an input stream.
     *
     * @param data The map to serialize, or <code>null</code>
     * @return The serialized map data, or <code>null</code> if the map is empty
     */
    private static InputStream serializeMap(Map<String, Object> data) throws OXException {
        if (null == data || data.isEmpty()) {
            return null;
        }
        Object coerced = null;
        try {
            coerced = JSONCoercion.coerceToJSON(data);
        } catch (JSONException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
        if (null == coerced || JSONObject.NULL.equals(coerced)) {
            return null;
        }
        String json = ((JSONValue) coerced).toString();
        return Streams.newByteArrayInputStream(json.getBytes(Charsets.US_ASCII));
        //TODO: return new JSONInputStream((JSONValue) coerced, "US-ASCII");
    }

}
