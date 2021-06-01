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

package com.openexchange.authentication.application.storage.rdb.passwords;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import com.openexchange.authentication.application.AppLoginRequest;
import com.openexchange.authentication.application.ApplicationPassword;
import com.openexchange.authentication.application.exceptions.AppPasswordExceptionCodes;
import com.openexchange.authentication.application.storage.AppPasswordStorage;
import com.openexchange.authentication.application.storage.AuthenticatedApplicationPassword;
import com.openexchange.authentication.application.storage.history.AppPasswordLoginHistoryStorage;
import com.openexchange.authentication.application.storage.rdb.AbstractAppPasswordStorage;
import com.openexchange.authentication.application.storage.rdb.AppPasswordStorageProperty;
import com.openexchange.authentication.application.storage.rdb.history.AppPasswordLoginHistoryImpl;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.password.mechanism.PasswordDetails;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link AppPasswordStorageRDB} implements the MySQL database storage interface for application
 * specific passwords
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class AppPasswordStorageRDB extends AbstractAppPasswordStorage implements AppPasswordStorage {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(AppPasswordStorageRDB.class);

    private final AppPasswordLoginHistoryImpl loginHistoryStorage;

    /**
     * Initializes a new {@link AppPasswordStorageRDB}.
     * 
     * @param serviceLookup The service lookup
     */
    public AppPasswordStorageRDB(ServiceLookup serviceLookup) {
        super(serviceLookup);
        this.loginHistoryStorage = new AppPasswordLoginHistoryImpl(serviceLookup);
    }

    @Override
    public boolean handles(AppLoginRequest loginRequest) {
        return null != loginRequest && passwordGenerator.isInExpectedFormat(loginRequest.getPassword());
    }

    @Override
    public boolean handles(Session session, String appType) throws OXException {
        return true; // can handle everything for now 
    }

    // Database functionality

    /**
     * Remove the password from the database.
     *
     * @param contextId The context id
     * @param userId The user id
     * @param uuid The id of the password to be removed
     * @throws OXException if an SQL error is occurred
     * @return The number of affected rows
     */
    private int doRemovePassword(int contextId, int userId, String uuid) throws OXException {
        final DatabaseService databaseService = getDatabase();
        final Connection connection = databaseService.getWritable(contextId);
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(AppPasswordSQL.REMOVE_AUTH);
            int index = 1;
            statement.setInt(index++, contextId);
            statement.setInt(index++, userId);
            statement.setBytes(index++, UUIDs.toByteArray(UUID.fromString(uuid)));
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw AppPasswordExceptionCodes.DATABASE_ERROR.create(e);
        } finally {
            Databases.closeSQLStuff(statement);
            if (connection != null) {
                databaseService.backWritable(contextId, connection);
            }
        }
    }

    /**
     * Perform the delete all for user.
     *
     * @param contextId The context identifier
     * @param userId the user identifier
     * @param connection Database connection to use
     * @throws OXException if an SQL error is occurred
     */
    private void doDeleteAllForUser(int contextId, int userId, Connection connection) throws OXException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(AppPasswordSQL.DELETE_FOR_USER);
            int index = 1;
            statement.setInt(index++, contextId);
            statement.setInt(index++, userId);
            statement.execute();
        } catch (SQLException e) {
            throw AppPasswordExceptionCodes.DATABASE_ERROR.create(e);
        } finally {
            Databases.closeSQLStuff(statement);
        }
    }

    /**
     * Perform the delete all for context
     *
     * @param contextId The context identifier
     * @param connection The connection to use
     * @throws OXException if an SQL error is occurred
     */
    private void doDeleteAllForContext(int contextId, Connection connection) throws OXException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(AppPasswordSQL.DELETE_FOR_CONTEXT);
            int index = 1;
            statement.setInt(index++, contextId);
            statement.execute();
        } catch (SQLException e) {
            throw AppPasswordExceptionCodes.DATABASE_ERROR.create(e);
        } finally {
            Databases.closeSQLStuff(statement);
        }
    }

    @Override
    public boolean removePassword(Session session, String passwordId) throws OXException {
        if (0 < doRemovePassword(session.getContextId(), session.getUserId(), passwordId)) {
            getLoginHistoryStorage().deleteHistory(session, passwordId);
            return true;
        }
        return false;
    }

    @Override
    public ApplicationPassword addPassword(Session session, String appName, String appType) throws OXException {
        ApplicationPassword password = createAppPass(session, appName, appType);
        final DatabaseService databaseService = getDatabase();
        final Connection connection = databaseService.getWritable(session.getContextId());
        PreparedStatement statement = null;
        try {
            PasswordDetails passwd = getPasswordHash(password.getAppPassword());
            statement = connection.prepareStatement(AppPasswordSQL.INSERT_AUTH);
            int index = 1;
            statement.setInt(index++, session.getUserId());
            statement.setInt(index++, session.getContextId());
            statement.setBytes(index++, UUIDs.toByteArray(UUID.fromString(password.getGUID())));
            statement.setString(index++, password.getLogin());
            statement.setString(index++, password.getAppType());
            statement.setString(index++, password.getName());
            statement.setString(index++, passwd.getPasswordMech());
            statement.setString(index++, passwd.getEncodedPassword());
            statement.setBytes(index++, passwd.getSalt());
            statement.setString(index++, encryptPassword(password.getFullPassword(), password.getAppPassword()));
            statement.setString(index++, encryptPassword(password.getAppPassword(), password.getFullPassword()));
            statement.execute();
            return password;
        } catch (SQLException e) {
            throw AppPasswordExceptionCodes.DATABASE_ERROR.create(e);
        } finally {
            Databases.closeSQLStuff(statement);
            if (connection != null) {
                databaseService.backWritable(session.getContextId(), connection);
            }
        }
    }

    @Override
    public AuthenticatedApplicationPassword doAuth(String login, String password) throws OXException {
        final DatabaseService databaseService = getDatabase();
        final Connection connection = databaseService.getReadOnly(getContextId(login));
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement(AppPasswordSQL.GET_AUTH);
            int index = 1;
            statement.setString(index++, login);
            rs = statement.executeQuery();
            while (rs.next()) {
                if (isMatch(password, rs.getString("mech"), rs.getString("passHash"), rs.getBytes("salt"))) {
                    int contextId = rs.getInt("cid");
                    int userId = rs.getInt("user");
                    ApplicationPassword applicationPassword = ApplicationPassword.builder()
                        .setLogin(rs.getString("login"))
                        .setUUID(String.valueOf(UUIDs.toUUID(rs.getBytes("uuid"))))
                        .setAppType(rs.getString("appType"))
                        .setName(rs.getString("name"))
                        .setFullPassword(decryptPassword(rs.getString("encrPass"), password))
                    .build();
                    return new AuthenticatedApplicationPassword() {

                        @Override
                        public int getUserId() {
                            return userId;
                        }

                        @Override
                        public int getContextId() {
                            return contextId;
                        }

                        @Override
                        public ApplicationPassword getApplicationPassword() {
                            return applicationPassword;
                        }
                    };
                }
            }
            return null;
        } catch (SQLException e) {
            throw AppPasswordExceptionCodes.DATABASE_ERROR.create(e);
        } finally {
            Databases.closeSQLStuff(rs, statement);
            if (connection != null) {
                databaseService.backReadOnly(connection);
            }
        }
    }

    @Override
    public List<ApplicationPassword> getList(Session session) throws OXException {
        final DatabaseService databaseService = getDatabase();
        final Connection connection = databaseService.getReadOnly(session.getContextId());
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.prepareStatement(AppPasswordSQL.LIST_AUTH);
            int index = 1;
            statement.setInt(index++, session.getContextId());
            statement.setInt(index++, session.getUserId());
            rs = statement.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }

            List<ApplicationPassword> results = new ArrayList<ApplicationPassword>();
            do {
                UUID uuid = UUIDs.toUUID(rs.getBytes("uuid"));
                ApplicationPassword lPass = ApplicationPassword.builder() //@formatter:off
                    .setUUID(String.valueOf(uuid))
                    .setAppType(rs.getString("appType"))
                    .setName(rs.getString("name"))
                    .setLogin(rs.getString("login"))
                .build(); //@formatter:on
                results.add(lPass);
            } while (rs.next());
            return results;
        } catch (SQLException e) {
            throw AppPasswordExceptionCodes.DATABASE_ERROR.create(e);
        } finally {
            Databases.closeSQLStuff(rs, statement);
            if (connection != null) {
                databaseService.backReadOnly(connection);
            }
        }
    }

    @Override
    public AppPasswordLoginHistoryStorage getLoginHistoryStorage() {
        return loginHistoryStorage;
    }

    public void deleteAllForUser(int contextId, int userId, Connection connection) throws OXException {
        if (connection == null) {
            throw AppPasswordExceptionCodes.MISSING_DATABASE_CONNECTION.create();
        }
        // Cleanup history
        loginHistoryStorage.deleteForUser(userId, contextId, connection);
        // Do delete
        doDeleteAllForUser(contextId, userId, connection);
    }

    public void deleteAllForContext(int contextId, Connection connection) throws OXException {
        if (connection == null) {
            throw AppPasswordExceptionCodes.MISSING_DATABASE_CONNECTION.create();
        }
        loginHistoryStorage.deleteForContext(contextId, connection);
        doDeleteAllForContext(contextId, connection);
    }

    public void changePassword(int contextId, int userId, String newPassword, String oldPassword) throws OXException {
        if (false == getConfigService().getBooleanProperty(userId, contextId, AppPasswordStorageProperty.STORE_USER_PASSWORD)) {
            return;
        }

        DatabaseService databaseService = getDatabase();
        Connection connection = databaseService.getWritable(contextId);
        PreparedStatement statement = null;
        ResultSet rs = null;
        ArrayList<PasswordChange> passwords = new ArrayList<PasswordChange>();
        boolean failure = false;
        try {
            statement = connection.prepareStatement(AppPasswordSQL.GET_PASSWORDS_FOR_UPDATE);
            int index = 1;
            statement.setInt(index++, contextId);
            statement.setInt(index++, userId);
            rs = statement.executeQuery();
            while (rs.next()) {
                passwords.add(new PasswordChange(UUIDs.toUUID(rs.getBytes("uuid")), rs.getString("encrLogin")));
            }
        } catch (SQLException e) {
            failure = true;
            throw AppPasswordExceptionCodes.DATABASE_ERROR.create(e);
        } finally {
            Databases.closeSQLStuff(rs, statement);
            if (failure && connection != null) {
                databaseService.backWritable(contextId, connection);
            }
        }
        Iterator<PasswordChange> passwordsI = passwords.iterator();
        try {
            while (passwordsI.hasNext()) {
                PasswordChange password = passwordsI.next();
                if (password.getEncrLogin() == null) {  // No password stored to change
                    continue;
                }
                String decryptedLogin = decryptPassword(password.getEncrLogin(), oldPassword);
                if (decryptedLogin == null) {
                    failure = true;
                }
                password.setNewEncrLogin(encryptPassword(decryptedLogin, newPassword));
                password.setNewEncrPass(encryptPassword(newPassword, decryptedLogin));
                if (!updatePassword(connection, contextId, password)) {
                    failure = true;
                }
            }
            if (failure) {  // If not all success, notify
                throw AppPasswordExceptionCodes.UNABLE_UPDATE_PASSWORD.create();
            }
        } finally {
            databaseService.backWritable(contextId, connection);
        }
    }

    /**
     * Perform the update in the database
     *
     * @param con Connection to use
     * @param contextId Context Id of the user
     * @param changed PasswordChange to use. Contains the changed password
     * @return <code>true</code> if the password was updated; <code>false</code> if there was an SQL error
     */
    private boolean updatePassword(Connection con, int contextId, PasswordChange changed) {
        PreparedStatement statement = null;
        try {
            statement = con.prepareStatement(AppPasswordSQL.UPDATE_PASSWORD);
            int index = 1;
            statement.setString(index++, changed.getNewEncrPass());
            statement.setString(index++, changed.getNewEncrLogin());
            statement.setInt(index++, contextId);
            statement.setBytes(index++, UUIDs.toByteArray(changed.getGUID()));
            statement.execute();
        } catch (SQLException e) {
            LOG.error("Problem updating password", e);
            return false;
        } finally {
            Databases.closeSQLStuff(statement);
        }
        return true;
    }

}
