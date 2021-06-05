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

package com.openexchange.file.storage.rdb.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AdministrativeFileStorageAccount;
import com.openexchange.file.storage.AdministrativeFileStorageAccountStorage;
import com.openexchange.file.storage.DefaultAdministrativeFileStorageAccount;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RdbAdministrativeFileStorageAccountStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class RdbAdministrativeFileStorageAccountStorage implements AdministrativeFileStorageAccountStorage {

    private static final String SELECT_FOR_CONTEXT = "SELECT cid,user,serviceId,account FROM filestorageAccount WHERE cid=?;";
    private static final String SELECT_FOR_USER = "SELECT cid,user,serviceId,account FROM filestorageAccount WHERE cid=? AND user=?;";
    private static final String SELECT_FOR_CONTEXT_PROVIDER = "SELECT cid,user,serviceId,account FROM filestorageAccount WHERE cid=? AND serviceId=?;";
    private static final String SELECT_ACCOUNT = "SELECT cid,user,serviceId,account FROM filestorageAccount WHERE cid=? AND user=? AND accountId=?;";
    private static final String SELECT_FOR_USER_PROVIDER = "SELECT cid,user,serviceId,account FROM filestorageAccount WHERE cid=? AND user=? AND serviceId=?;";
    private static final String DELETE_ACCOUNT = "DELETE FROM filestorageAccount WHERE cid=? AND user=? AND account=?;";

    private final ServiceLookup services;

    /**
     * Initializes a new {@link RdbAdministrativeFileStorageAccountStorage}.
     *
     * @param services The {@link ServiceLookup}
     */
    public RdbAdministrativeFileStorageAccountStorage(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public List<AdministrativeFileStorageAccount> getAccounts(int contextId) throws OXException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(contextId);
        try {
            statement = connection.prepareStatement(SELECT_FOR_CONTEXT);
            statement.setInt(1, contextId);
            resultSet = statement.executeQuery();
            return getAccounts(resultSet);
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(statement, resultSet);
            databaseService.backReadOnly(contextId, connection);
        }
    }

    @Override
    public List<AdministrativeFileStorageAccount> getAccounts(int contextId, String providerId) throws OXException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(contextId);
        try {
            statement = connection.prepareStatement(SELECT_FOR_CONTEXT_PROVIDER);
            statement.setInt(1, contextId);
            statement.setString(2, providerId);
            resultSet = statement.executeQuery();
            return getAccounts(resultSet);
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(statement, resultSet);
            databaseService.backReadOnly(contextId, connection);
        }
    }

    @Override
    public List<AdministrativeFileStorageAccount> getAccounts(int contextId, int userId) throws OXException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(contextId);
        try {
            statement = connection.prepareStatement(SELECT_FOR_USER);
            statement.setInt(1, contextId);
            statement.setInt(2, userId);
            resultSet = statement.executeQuery();
            return getAccounts(resultSet);
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(statement, resultSet);
            databaseService.backReadOnly(contextId, connection);
        }
    }

    @Override
    public List<AdministrativeFileStorageAccount> getAccounts(int contextId, int userId, String providerId) throws OXException {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(contextId);
        try {
            statement = connection.prepareStatement(SELECT_FOR_USER_PROVIDER);
            statement.setInt(1, contextId);
            statement.setInt(2, userId);
            statement.setString(3, providerId);
            resultSet = statement.executeQuery();
            return getAccounts(resultSet);
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(statement, resultSet);
            databaseService.backReadOnly(contextId, connection);
        }
    }

    @Override
    public boolean deleteAccount(int contextId, int userId, int accountId) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getWritable(contextId);
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;

            boolean deleted = deleteAccount(contextId, userId, accountId, connection);

            connection.commit();
            rollback = 2;
            return deleted;
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
            databaseService.backWritable(contextId, connection);
        }
    }

    @Override
    public boolean deleteAccount(int contextId, int userId, int accountId, Connection connection) throws OXException {
        PreparedStatement statement = null;
        try {
            AdministrativeFileStorageAccount account = getAccount(contextId, userId, accountId, connection);
            if (account == null) {
                return false;
            }

            statement = connection.prepareStatement(DELETE_ACCOUNT);
            statement.setInt(1, contextId);
            statement.setInt(2, userId);
            statement.setInt(3, accountId);
            boolean deleted = statement.executeUpdate() > 0;

            invalidate(account);
            return deleted;
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(statement);
        }
    }

    //////////////////////////// HELPERS //////////////////////////////

    /**
     * Invalidates the specified account
     *
     * @param account The account to invalidate
     * @throws OXException if invalidation fails
     */
    private void invalidate(AdministrativeFileStorageAccount account) throws OXException {
        CachingFileStorageAccountStorage cache = CachingFileStorageAccountStorage.getInstance();
        cache.invalidate(account.getServiceId(), account.getId(), account.getUserId(), account.getContextId());
    }

    /**
     * Retrieves a single account
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @param connection The connection
     * @return The account or <code>null</code> if no account found
     * @throws OXException if an error is occurred
     */
    private AdministrativeFileStorageAccount getAccount(int contextId, int userId, int accountId, Connection connection) throws OXException {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement(SELECT_ACCOUNT);
            statement.setInt(1, contextId);
            statement.setInt(2, userId);
            statement.setInt(3, accountId);
            rs = statement.executeQuery();
            if (false == rs.next()) {
                return null;
            }
            return getAccount(rs);
        } catch (SQLException e) {
            throw FileStorageExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(statement, rs);
        }
    }

    /**
     * Parses the accounts from the specified {@link ResultSet}
     *
     * @param resultSet The {@link ResultSet}
     * @return The accounts
     * @throws SQLException if an SQL error is occurred
     */
    private List<AdministrativeFileStorageAccount> getAccounts(ResultSet resultSet) throws SQLException {
        if (!resultSet.next()) {
            return Collections.emptyList();
        }

        List<AdministrativeFileStorageAccount> accounts = new LinkedList<>();
        do {
            accounts.add(getAccount(resultSet));
        } while (resultSet.next());
        return accounts;
    }

    /**
     * Parses the specified {@link ResultSet} as an account
     *
     * @param resultSet The {@link ResultSet} to parse
     * @return The account
     * @throws SQLException if an SQL error is occurred
     */
    private AdministrativeFileStorageAccount getAccount(ResultSet resultSet) throws SQLException {
        return new DefaultAdministrativeFileStorageAccount(resultSet.getInt(1), resultSet.getInt(2), resultSet.getInt(4), resultSet.getString(3));
    }

    /**
     * Returns the {@link DatabaseService}
     *
     * @return the {@link DatabaseService}
     * @throws OXException if the service is absent
     */
    private DatabaseService getDatabaseService() throws OXException {
        return services.getServiceSafe(DatabaseService.class);
    }
}
