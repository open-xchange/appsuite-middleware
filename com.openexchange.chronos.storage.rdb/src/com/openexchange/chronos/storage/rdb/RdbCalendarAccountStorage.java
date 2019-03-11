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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.IOException;
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
import com.google.json.JsonSanitizer;
import com.openexchange.caching.CacheService;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.DefaultCalendarAccount;
import com.openexchange.chronos.storage.CalendarAccountStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.rdb.osgi.Services;
import com.openexchange.database.Databases;
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

    private static final int ID_GENERATOR_TYPE = Types.SUBSCRIPTION; // TODO own type

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

    /**
     * Gets the underlying transaction policy.
     *
     * @return The transaction policy
     */
    public DBTransactionPolicy getTransactionPolicy() {
        return txPolicy;
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
    public void insertAccount(CalendarAccount account, int maxAccounts) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = insertAccount(connection, context.getContextId(), account, maxAccounts);
            if (0 == updated) {
                throw CalendarExceptionCodes.ACCOUNT_NOT_WRITTEN.create();
            }
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void updateAccount(CalendarAccount account, long clientTimestamp) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = updateAccount(connection, context.getContextId(), account, clientTimestamp);
            if (0 == updated) {
                CalendarAccount storedAccount = selectAccount(connection, context.getContextId(), account.getAccountId(), account.getUserId());
                if (null == storedAccount) {
                    throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(I(account.getAccountId()));
                }
                if (storedAccount.getLastModified().getTime() > clientTimestamp) {
                    throw CalendarExceptionCodes.CONCURRENT_MODIFICATION.create(String.valueOf(storedAccount.getAccountId()), L(clientTimestamp), L(storedAccount.getLastModified().getTime()));
                }
            }
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
    public CalendarAccount[] loadAccounts(int userId, int[] accountIds) throws OXException {
        if (null == accountIds) {
            return null;
        }
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            CalendarAccount[] accounts = new CalendarAccount[accountIds.length];
            for (int i = 0; i < accountIds.length; i++) {
                accounts[i] = selectAccount(connection, context.getContextId(), accountIds[i], userId);
            }
            return accounts;
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public void deleteAccount(int userId, int accountId, long clientTimestamp) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            updated = deleteAccount(connection, context.getContextId(), accountId, userId, clientTimestamp);
            if (0 == updated) {
                CalendarAccount storedAccount = selectAccount(connection, context.getContextId(), accountId, userId);
                if (null == storedAccount) {
                    throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(I(accountId));
                }
                if (storedAccount.getLastModified().getTime() > clientTimestamp) {
                    throw CalendarExceptionCodes.CONCURRENT_MODIFICATION.create(String.valueOf(storedAccount.getAccountId()), L(clientTimestamp), L(storedAccount.getLastModified().getTime()));
                }
            }
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

    private static int insertAccount(Connection connection, int cid, CalendarAccount account) throws SQLException {
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
                stmt.setLong(5, System.currentTimeMillis());
                stmt.setBinaryStream(6, internalConfigStream);
                stmt.setBinaryStream(7, userConfigStream);
                return logExecuteUpdate(stmt);
            } finally {
                Streams.close(internalConfigStream, userConfigStream);
            }
        }
    }

    private static int insertAccount(Connection connection, int cid, CalendarAccount account, int maxAccounts) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("INSERT INTO calendar_account (cid,id,provider,user,modified,internalConfig,userConfig) ")
        ;
        if (0 < maxAccounts) {
            stringBuilder.append("SELECT ?,?,?,?,?,?,? FROM DUAL ")
                .append("WHERE ?>(SELECT COUNT(*) FROM calendar_account WHERE cid=? AND user=? AND provider=?);");
        } else {
            stringBuilder.append("VALUES (?,?,?,?,?,?,?);");
        }
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            InputStream internalConfigStream = null;
            InputStream userConfigStream = null;
            try {
                internalConfigStream = serialize(account.getInternalConfiguration());
                userConfigStream = serialize(account.getUserConfiguration());
                stmt.setInt(1, cid);
                stmt.setInt(2, account.getAccountId());
                stmt.setString(3, account.getProviderId());
                stmt.setInt(4, account.getUserId());
                stmt.setLong(5, System.currentTimeMillis());
                stmt.setBinaryStream(6, internalConfigStream);
                stmt.setBinaryStream(7, userConfigStream);
                if (0 < maxAccounts) {
                    stmt.setInt(8, maxAccounts);
                    stmt.setInt(9, cid);
                    stmt.setInt(10, account.getUserId());
                    stmt.setString(11, account.getProviderId());
                }
                return logExecuteUpdate(stmt);
            } finally {
                Streams.close(internalConfigStream, userConfigStream);
            }
        }
    }

    private static int updateAccount(Connection connection, int cid, CalendarAccount account, long clientTimestamp) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder("UPDATE calendar_account SET modified=?");
        JSONObject internalConfig = account.getInternalConfiguration();
        JSONObject userConfig = account.getUserConfiguration();
        if (null != internalConfig) {
            stringBuilder.append(",internalConfig=?");
        }
        if (null != userConfig) {
            stringBuilder.append(",userConfig=?");
        }
        stringBuilder.append(" WHERE cid=? AND id=? AND user=? AND modified<=?;");
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            InputStream internalConfigStream = null;
            InputStream userConfigStream = null;
            try {
                internalConfigStream = null != internalConfig ? serialize(internalConfig) : null;
                userConfigStream = null != userConfig ? serialize(userConfig) : null;
                stmt.setLong(parameterIndex++, System.currentTimeMillis());
                if (null != internalConfigStream) {
                    stmt.setBinaryStream(parameterIndex++, internalConfigStream);
                }
                if (null != userConfigStream) {
                    stmt.setBinaryStream(parameterIndex++, userConfigStream);
                }
                stmt.setInt(parameterIndex++, cid);
                stmt.setInt(parameterIndex++, account.getAccountId());
                stmt.setInt(parameterIndex++, account.getUserId());
                stmt.setLong(parameterIndex++, clientTimestamp);
                return logExecuteUpdate(stmt);
            } finally {
                Streams.close(internalConfigStream, userConfigStream);
            }
        }
    }

    private static int deleteAccount(Connection connection, int cid, int id, int user, long clientTimestamp) throws SQLException {
        String sql = "DELETE FROM calendar_account WHERE cid=? AND id=? AND user=? AND modified<=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cid);
            stmt.setInt(2, id);
            stmt.setInt(3, user);
            stmt.setLong(4, clientTimestamp);
            return logExecuteUpdate(stmt);
        }
    }

    private static List<CalendarAccount> selectAccounts(Connection connection, int cid, int user) throws SQLException {
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

    private static List<CalendarAccount> selectAccounts(Connection connection, int cid, String provider, int[] userIds) throws SQLException {
        String sql = new StringBuilder()
            .append("SELECT id,user,provider,modified,internalConfig,userConfig FROM calendar_account WHERE cid=? AND provider=? AND user")
            .append(Databases.getPlaceholders(userIds.length)).append(';')
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

    private static CalendarAccount selectAccount(Connection connection, int cid, int id, int user) throws SQLException {
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

    private static CalendarAccount selectAccount(Connection connection, int cid, int user, String provider) throws SQLException {
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
        JSONObject internalConfig = readJSON("internalConfig", resultSet);
        JSONObject userConfig = readJSON("userConfig", resultSet);
        return new DefaultCalendarAccount(provider, id, user, internalConfig, userConfig, new Date(lastModified));
    }

    /**
     * Reads the JSON content of the designated column in the current row of specified <code>ResultSet</code> object.
     *
     * @param columnName The column name
     * @param resultSet The <code>ResultSet</code> object
     * @return The JSON content or <code>null</code>
     * @throws SQLException If JSON content cannot be read
     */
    private static JSONObject readJSON(String columnName, ResultSet resultSet) throws SQLException {
        JSONObject retval;
        InputStream inputStream = null;
        try {
            inputStream = resultSet.getBinaryStream(columnName);
            retval = deserialize(inputStream);
        } catch (SQLException e) {
            if (false == JSONException.isParseException(e)) {
                throw e;
            }

            // Try to sanitize corrupt input
            Streams.close(inputStream);
            inputStream = resultSet.getBinaryStream(columnName);
            retval = deserialize(inputStream, true);
        } finally {
            Streams.close(inputStream);
        }
        return retval;
    }

    /**
     * Deserializes a JSON object (as used in an account's configuration) from the supplied input stream.
     *
     * @param inputStream The input stream to deserialize
     * @return The deserialized JSON object
     */
    private static JSONObject deserialize(InputStream inputStream) throws SQLException {
        return deserialize(inputStream, false);
    }

    /**
     * Deserializes a JSON object (as used in an account's configuration) from the supplied input stream.
     *
     * @param inputStream The input stream to deserialize
     * @param withSanitize <code>true</code> if JSON content provided by input stream is supposed to be sanitized; otherwise <code>false</code> to read as-is
     * @return The deserialized JSON object
     */
    private static JSONObject deserialize(InputStream inputStream, boolean withSanitize) throws SQLException {
        if (null == inputStream) {
            return null;
        }

        try {
            if (withSanitize) {
                String jsonish = JsonSanitizer.sanitize(Streams.reader2string(new AsciiReader(inputStream)));
                return new JSONObject(jsonish);
            }

            return new JSONObject(new AsciiReader(inputStream));
        } catch (JSONException e) {
            throw new SQLException(e);
        } catch (IOException e) {
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
