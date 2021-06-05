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

package com.openexchange.contact.storage.rdb.internal.account;

import static com.openexchange.database.Databases.getPlaceholders;
import static com.openexchange.database.Databases.isPrimaryKeyConflictInMySQL;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONObject;
import com.openexchange.caching.CacheService;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.DefaultContactsAccount;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.contact.storage.ContactsAccountStorage;
import com.openexchange.contact.storage.rdb.internal.RdbStorage;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RdbContactsAccountStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class RdbContactsAccountStorage extends RdbStorage implements ContactsAccountStorage {

    private static final int ID_GENERATOR_TYPE = Types.SUBSCRIPTION;

    /**
     * Initialises a new contacts account storage for the specified context
     *
     * @param services A service lookup reference
     * @param context The context
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     * @return The initialized account storage
     */
    public static ContactsAccountStorage init(ServiceLookup services, Context context, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        RdbContactsAccountStorage accountStorage = new RdbContactsAccountStorage(context, dbProvider, txPolicy);
        CacheService cacheService = services.getOptionalService(CacheService.class);
        if (null != cacheService) {
            try {
                return new CachingContactsAccountStorage(accountStorage, context.getContextId(), cacheService);
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(RdbContactsAccountStorage.class).warn("Error initiliazing contacts account cache", e);
            }
        }
        return accountStorage;
    }

    /**
     * Initializes a new {@link RdbContactsAccountStorage}.
     *
     * @param context The context
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    private RdbContactsAccountStorage(Context context, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
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
    public void insertAccount(ContactsAccount account) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = insertAccount(connection, context.getContextId(), account);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            if (isPrimaryKeyConflictInMySQL(e)) {
                throw ContactsProviderExceptionCodes.ACCOUNT_NOT_WRITTEN.create(e);
            }
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void updateAccount(ContactsAccount account, long clientTimestamp) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = updateAccount(connection, context.getContextId(), account, clientTimestamp);
            if (0 == updated) {
                checkAccount(account.getUserId(), account.getAccountId(), clientTimestamp, connection);
            }
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAccount(int userId, int accountId, long clientTimestamp) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAccount(connection, context.getContextId(), accountId, userId, clientTimestamp);
            if (0 != updated) {
                txPolicy.commit(connection);
                return;
            }
            checkAccount(userId, accountId, clientTimestamp, connection);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public ContactsAccount loadAccount(int userId, int accountId) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            ContactsAccount account = selectAccount(connection, context.getContextId(), accountId, userId);
            if (null == account) {
                throw ContactsProviderExceptionCodes.ACCOUNT_NOT_FOUND.create(I(accountId));
            }
            return account;
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public ContactsAccount[] loadAccounts(int userId, int[] accountIds) throws OXException {
        if (null == accountIds) {
            return null;
        }
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            ContactsAccount[] accounts = new ContactsAccount[accountIds.length];
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
    public List<ContactsAccount> loadAccounts(int userId) throws OXException {
        return loadAccounts(userId, (String[]) null);
    }

    @Override
    public List<ContactsAccount> loadAccounts(int userId, String... providerIds) throws OXException {
        return loadAccounts(new int[] { userId }, providerIds);
    }

    @Override
    public void invalidateAccount(int userId, int accountId) throws OXException {
        // no
    }

    //////////////////////////////////////// HELPERS //////////////////////////////////

    /**
     * Gets the underlying transaction policy.
     *
     * @return The transaction policy
     */
    public DBTransactionPolicy getTransactionPolicy() {
        return txPolicy;
    }

    /**
     * Checks whether the specified account exists and if is modified concurrently
     *
     * @param userId The user identifier
     * @param accountId The account identifier
     * @param clientTimestamp The last known client timestamp
     * @param connection The write-able connection
     * @throws SQLException if an SQL error is occurred
     * @throws OXException if the account is either not found or is concurrently modified
     */
    private void checkAccount(int userId, int accountId, long clientTimestamp, Connection connection) throws SQLException, OXException {
        ContactsAccount storedAccount = selectAccount(connection, context.getContextId(), accountId, userId);
        if (null == storedAccount) {
            throw ContactsProviderExceptionCodes.ACCOUNT_NOT_FOUND.create(I(accountId));
        }
        if (storedAccount.getLastModified().getTime() > clientTimestamp) {
            throw ContactsProviderExceptionCodes.CONCURRENT_MODIFICATION.create(String.valueOf(storedAccount.getAccountId()), L(clientTimestamp), L(storedAccount.getLastModified().getTime()));
        }
    }

    /**
     * Generates a unique account identifier
     *
     * @param connection The connection
     * @param context the context
     * @return the unique identifier
     * @throws SQLException
     */
    private int nextAccountId(Connection connection, Context context) throws SQLException {
        if (isAutoCommitSafe(connection)) {
            throw new SQLException("Generating unique identifier is threadsafe if and only if it is executed in a transaction.");
        }
        return IDGenerator.getId(context, ID_GENERATOR_TYPE, connection);
    }

    /**
     * Inserts the specified contacts account to the database
     *
     * @param connection the writeable connection
     * @param cid The context identifier
     * @param account The account to insert
     * @return The amount of updated rows
     * @throws SQLException if an SQL error is occurred
     */
    private int insertAccount(Connection connection, int cid, ContactsAccount account) throws SQLException {
        String sql = "INSERT INTO contacts_account (cid,id,provider,user,modified,internalConfig,userConfig) VALUES (?,?,?,?,?,?,?);";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            InputStream internalConfigStream = null;
            InputStream userConfigStream = null;
            try {
                int index = 1;
                internalConfigStream = JSONUtil.serialise(account.getInternalConfiguration());
                userConfigStream = JSONUtil.serialise(account.getUserConfiguration());
                stmt.setInt(index++, cid);
                stmt.setInt(index++, account.getAccountId());
                stmt.setString(index++, account.getProviderId());
                stmt.setInt(index++, account.getUserId());
                stmt.setLong(index++, System.currentTimeMillis());
                stmt.setBinaryStream(index++, internalConfigStream);
                stmt.setBinaryStream(index++, userConfigStream);
                return logExecuteUpdate(stmt);
            } finally {
                Streams.close(internalConfigStream, userConfigStream);
            }
        }
    }

    /**
     * Updates the specified contacts account
     *
     * @param connection The write-able connection
     * @param cid The context identifier
     * @param account The contacts account
     * @param clientTimestamp The client timestamp
     * @return The amount of updated rows
     * @throws SQLException if an SQL error is occurred
     */
    private int updateAccount(Connection connection, int cid, ContactsAccount account, long clientTimestamp) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder("UPDATE contacts_account SET modified=?");
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
                internalConfigStream = null != internalConfig ? JSONUtil.serialise(internalConfig) : null;
                userConfigStream = null != userConfig ? JSONUtil.serialise(userConfig) : null;
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

    /**
     * Loads the accounts of the specified users that match the specified providers
     *
     * @param userIds The user identifiers
     * @param providerIds The provider identifiers
     * @return A List with all found contacts accounts
     * @throws OXException if an error is occurred
     */
    private List<ContactsAccount> loadAccounts(int[] userIds, String[] providerIds) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAccounts(connection, context.getContextId(), userIds, providerIds);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    /**
     * Selects the account with the specified identifier for the specified user
     * in the specified context.
     *
     * @param connection The connection
     * @param cid the context identifier
     * @param id The account identifier
     * @param user The user identifier
     * @return The contacts account (if found) or <code>null</code>
     * @throws SQLException if an error is occurred
     */
    private ContactsAccount selectAccount(Connection connection, int cid, int id, int user) throws SQLException {
        String sql = "SELECT id,user,provider,modified,internalConfig,userConfig FROM contacts_account WHERE cid=? AND id=? AND user=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int index = 1;
            stmt.setInt(index++, cid);
            stmt.setInt(index++, id);
            stmt.setInt(index++, user);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                return resultSet.next() ? readAccount(resultSet) : null;
            }
        }
    }

    /**
     * Reads the specified {@link ResultSet} into a {@link ContactsAccount}
     *
     * @param resultSet The result set to read
     * @return The {@link ContactsAccount}
     * @throws SQLException if an SQL error is occurred
     */
    private ContactsAccount readAccount(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        int user = resultSet.getInt("user");
        String provider = resultSet.getString("provider");
        long lastModified = resultSet.getLong("modified");
        JSONObject internalConfig = JSONUtil.readJSON("internalConfig", resultSet);
        JSONObject userConfig = JSONUtil.readJSON("userConfig", resultSet);
        return new DefaultContactsAccount(provider, id, user, internalConfig, userConfig, new Date(lastModified));
    }

    /**
     * Deletes the specified account of the specified user
     *
     * @param connection The write-able connection
     * @param cid the context identifier
     * @param id The account identifier
     * @param user The user identifier
     * @param clientTimestamp The client timestamp
     * @return The amount of affected rows
     * @throws SQLException if an SQL error is occurred
     */
    private int deleteAccount(Connection connection, int cid, int id, int user, long clientTimestamp) throws SQLException {
        String sql = "DELETE FROM contacts_account WHERE cid=? AND id=? AND user=? AND modified<=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cid);
            stmt.setInt(2, id);
            stmt.setInt(3, user);
            stmt.setLong(4, clientTimestamp);
            return logExecuteUpdate(stmt);
        }
    }

    /**
     * Selects all accounts that match the specified provider ids for the specified users
     *
     * @param connection The connection identifier
     * @param cid The context identifier
     * @param userIds The user identifiers
     * @param providerIds The provider identifiers
     * @return A List with all found contacts accounts
     * @throws SQLException if an SQL error is occurred
     */
    private List<ContactsAccount> selectAccounts(Connection connection, int cid, int[] userIds, String[] providerIds) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder().append("SELECT id,user,provider,modified,internalConfig,userConfig FROM contacts_account WHERE cid=?");
        if (null != userIds && 0 < userIds.length) {
            stringBuilder.append(" AND user").append(getPlaceholders(userIds.length));
        }
        if (null != providerIds && 0 < providerIds.length) {
            stringBuilder.append(" AND provider").append(getPlaceholders(providerIds.length));
        }
        String sql = stringBuilder.append(';').toString();
        List<ContactsAccount> accounts = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            if (null != userIds) {
                for (int userId : userIds) {
                    stmt.setInt(parameterIndex++, userId);
                }
            }
            if (null != providerIds) {
                for (String providerId : providerIds) {
                    stmt.setString(parameterIndex++, providerId);
                }
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    accounts.add(readAccount(resultSet));
                }
            }
        }
        return accounts;
    }

}
