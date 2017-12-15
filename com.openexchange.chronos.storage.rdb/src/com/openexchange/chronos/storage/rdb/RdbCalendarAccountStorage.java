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
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONInputStream;
import org.json.JSONObject;
import com.openexchange.caching.CacheService;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.DefaultCalendarAccount;
import com.openexchange.chronos.storage.CalendarAccountStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.rdb.osgi.Services;
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

    /**
     * Initializes a new calendar account storage.
     *
     * @param context The context
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     * @return The initialized account storage
     */
    public static CalendarAccountStorage init(Context context, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        RdbCalendarAccountStorage accountStorage = new RdbCalendarAccountStorage(context, dbProvider, txPolicy);
        CacheService cacheService = Services.getOptionalService(CacheService.class);
        if (null != cacheService) {
            try {
                return new CachingCalendarAccountStorage(accountStorage, context.getContextId(), cacheService);
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(RdbCalendarAccountStorage.class).warn("Error initiliazing calendar account cache", e);
            }
        }
        return accountStorage;
    }

    private static final int ID_GENERATOR_TYPE = Types.SUBSCRIPTION; //TODO own type

    /**
     * Initializes a new {@link RdbCalendarStorage}.
     *
     * @param context The context
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    private RdbCalendarAccountStorage(Context context, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, dbProvider, txPolicy);
    }

    @Override
    public int nextId() throws OXException {
        int value;
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            value = nextAccountId(connection, context);
            updated = 1;
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
        return value;
    }

    @Override
    public void insertAccount(CalendarAccount account) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = insertAccount(connection, context.getContextId(), account);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void updateAccount(CalendarAccount account) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = updateAccount(connection, context.getContextId(), account);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public CalendarAccount loadAccount(int userId, int accountId) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAccount(connection, context.getContextId(), accountId, userId);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public void deleteAccount(int userId, int accountId) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            updated = deleteAccount(connection, context.getContextId(), accountId, userId);
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

    @Override
    public List<CalendarAccount> loadAccounts(int[] userIds, String providerId) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAccounts(connection, context.getContextId(), providerId, userIds);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public CalendarAccount loadAccount(int userId, String providerId) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAccount(connection, context.getContextId(), userId, providerId);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public void invalidateAccount(int userId, int id) throws OXException {
        // no
    }

    private static int insertAccount(Connection connection, int cid, CalendarAccount account) throws SQLException, OXException {
        String sql = "INSERT INTO calendar_account (cid,id,provider,user,modified,internalConfig,userConfig) VALUES (?,?,?,?,?,?,?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            InputStream internalConfigStream = null;
            InputStream userConfigStream = null;
            try {
                internalConfigStream = serialize(account.getInternalConfiguration());
                userConfigStream = serialize(account.getUserConfiguration());
                stmt.setInt(1, cid);
                stmt.setInt(2, account.getAccountId());
                stmt.setString(3, account.getProviderId());
                stmt.setInt(4, account.getUserId());
                stmt.setLong(5, account.getLastModified().getTime());
                stmt.setBinaryStream(6, internalConfigStream);
                stmt.setBinaryStream(7, userConfigStream);
                return logExecuteUpdate(stmt);
            } finally {
                Streams.close(internalConfigStream, userConfigStream);
            }
        }
    }

    private static int updateAccount(Connection connection, int cid, CalendarAccount account) throws SQLException, OXException {
        StringBuilder stringBuilder = new StringBuilder("UPDATE calendar_account SET modified=?");
        JSONObject internalConfig = account.getInternalConfiguration();
        JSONObject userConfig = account.getUserConfiguration();
        if (null != internalConfig) {
            stringBuilder.append(",internalConfig=?");
        }
        if (null != userConfig) {
            stringBuilder.append(",userConfig=?");
        }
        stringBuilder.append(" WHERE cid=? AND id=? AND user=?;");
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            InputStream internalConfigStream = null;
            InputStream userConfigStream = null;
            try {
                internalConfigStream = null != internalConfig ? serialize(internalConfig) : null;
                userConfigStream = null != userConfig ? serialize(userConfig) : null;
                stmt.setLong(parameterIndex++, account.getLastModified().getTime());
                if (null != internalConfigStream) {
                    stmt.setBinaryStream(parameterIndex++, internalConfigStream);
                }
                if (null != userConfigStream) {
                    stmt.setBinaryStream(parameterIndex++, userConfigStream);
                }
                stmt.setInt(parameterIndex++, cid);
                stmt.setInt(parameterIndex++, account.getAccountId());
                stmt.setInt(parameterIndex++, account.getUserId());
                return logExecuteUpdate(stmt);
            } finally {
                Streams.close(internalConfigStream, userConfigStream);
            }
        }
    }

    private static int deleteAccount(Connection connection, int cid, int id, int user) throws SQLException, OXException {
        String sql = "DELETE FROM calendar_account WHERE cid=? AND id=? AND user=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cid);
            stmt.setInt(2, id);
            stmt.setInt(3, user);
            return logExecuteUpdate(stmt);
        }
    }

    private static List<CalendarAccount> selectAccounts(Connection connection, int cid, int user) throws SQLException, OXException {
        List<CalendarAccount> accounts = new ArrayList<CalendarAccount>();
        String sql = "SELECT id,user,provider,modified,internalConfig,userConfig FROM calendar_account WHERE cid=? AND user=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    accounts.add(readAccount(resultSet));
                }
            }
        }
        return accounts;
    }

    private static List<CalendarAccount> selectAccounts(Connection connection, int cid, String provider, int[] userIds) throws SQLException, OXException {
        String sql = new StringBuilder()
            .append("SELECT id,user,provider,modified,internalConfig,userConfig FROM calendar_account WHERE cid=? AND provider=? AND user")
            .append(getPlaceholders(userIds.length)).append(';')
        .toString();
        List<CalendarAccount> accounts = new ArrayList<CalendarAccount>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setString(parameterIndex++, provider);
            for (int userId : userIds) {
                stmt.setInt(parameterIndex++, userId);
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    accounts.add(readAccount(resultSet));
                }
            }
        }
        return accounts;
    }

    private static CalendarAccount selectAccount(Connection connection, int cid, int id, int user) throws SQLException, OXException {
        String sql = "SELECT id,user,provider,modified,internalConfig,userConfig FROM calendar_account WHERE cid=? AND id=? AND user=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cid);
            stmt.setInt(2, id);
            stmt.setInt(3, user);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                return resultSet.next() ? readAccount(resultSet) : null;
            }
        }
    }

    private static CalendarAccount selectAccount(Connection connection, int cid, int user, String provider) throws SQLException, OXException {
        String sql = "SELECT id,user,provider,modified,internalConfig,userConfig FROM calendar_account WHERE cid=? AND user=? AND provider=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cid);
            stmt.setInt(2, user);
            stmt.setString(3, provider);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                return resultSet.next() ? readAccount(resultSet) : null;
            }
        }
    }

    private static int nextAccountId(Connection connection, Context context) throws SQLException {
        if (connection.getAutoCommit()) {
            throw new SQLException("Generating unique identifier is threadsafe if and only if it is executed in a transaction.");
        }
        return IDGenerator.getId(context, ID_GENERATOR_TYPE, connection);
    }

    private static CalendarAccount readAccount(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        int user = resultSet.getInt("user");
        String provider = resultSet.getString("provider");
        long lastModified = resultSet.getLong("modified");
        JSONObject internalConfig;
        InputStream inputStream = null;
        try {
            inputStream = resultSet.getBinaryStream("internalConfig");
            internalConfig = deserialize(inputStream);
        } finally {
            Streams.close(inputStream);
        }
        JSONObject userConfig;
        try {
            inputStream = resultSet.getBinaryStream("userConfig");
            userConfig = deserialize(inputStream);
        } finally {
            Streams.close(inputStream);
        }
        return new DefaultCalendarAccount(provider, id, user, internalConfig, userConfig, new Date(lastModified));
    }

    /**
     * Deserializes a JSON object (as used in an account's configuration) from the supplied input stream.
     *
     * @param inputStream The input stream to deserialize
     * @return The deserialized JSON object
     */
    private static JSONObject deserialize(InputStream inputStream) throws SQLException {
        if (null == inputStream) {
            return null;
        }
        try {
            return new JSONObject(new AsciiReader(inputStream));
        } catch (JSONException e) {
            throw new SQLException(e);
        }
    }

    /**
     * Serializes a JSON object (as used in an account's configuration) to an input stream.
     *
     * @param data The JSON object serialize, or <code>null</code>
     * @return The serialized JSON object, or <code>null</code> if the passed object was <code>null</code>
     */
    private static InputStream serialize(JSONObject data) {
        if (null == data) {
            return null;
        }
        return new JSONInputStream(data, Charsets.US_ASCII.name());
    }

}
