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

package com.openexchange.push.impl.credstorage.rdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.credstorage.CredentialStorage;
import com.openexchange.push.credstorage.Credentials;
import com.openexchange.push.credstorage.DefaultCredentials;
import com.openexchange.push.impl.credstorage.Obfuscator;
import com.openexchange.push.impl.credstorage.osgi.CredStorageServices;

/**
 * {@link RdbCredentialStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class RdbCredentialStorage implements CredentialStorage {

    private final Obfuscator obfuscator;

    /**
     * Initializes a new {@link RdbCredentialStorage}.
     */
    public RdbCredentialStorage(Obfuscator obfuscator) {
        super();
        this.obfuscator = obfuscator;
    }

    @Override
    public Credentials getCredentials(int userId, int contextId) throws OXException {
        DatabaseService service = CredStorageServices.requireService(DatabaseService.class);
        Connection connection = service.getReadOnly(contextId);
        try {
            return obfuscator.unobfuscateCredentials(getCredentials(userId, contextId, connection));
        } finally {
            service.backReadOnly(contextId, connection);
        }
    }

    private Credentials getCredentials(int userId, int contextId, Connection connection) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT password, login FROM credentials WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return null;
            }
            DefaultCredentials.Builder credentials = DefaultCredentials.builder();
            credentials.withContextId(contextId);
            credentials.withUserId(userId);
            credentials.withPassword(rs.getString(1));
            credentials.withLogin(rs.getString(2));
            return credentials.build();
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public void storeCredentials(Credentials credentials) throws OXException {
        if ((null == credentials) || (false == isValid(credentials))) {
            throw OXException.general("Invalid credentials given: " + (null == credentials ? "null" : credentials.toString()));
        }

        int contextId = credentials.getContextId();
        DatabaseService service = CredStorageServices.requireService(DatabaseService.class);
        Connection connection = service.getWritable(contextId);
        boolean modified = false;
        try {
            modified = storeCredentials(obfuscator.obfuscateCredentials(credentials), true, connection);
        } finally {
            if (modified) {
                service.backWritable(contextId, connection);
            } else {
                service.backWritableAfterReading(contextId, connection);
            }
        }
    }

    private boolean isValid(Credentials credentials) {
        return (null != credentials.getLogin()) && (null != credentials.getPassword());
    }

    private boolean storeCredentials(Credentials obfuscatedCredentials, boolean retry, Connection connection) throws OXException {
        int contextId = obfuscatedCredentials.getContextId();
        int userId = obfuscatedCredentials.getUserId();

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT login, password FROM credentials WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();

            boolean exists = false;
            String currentObfuscatedLogin = null;
            String currentObfuscatedPassword = null;
            if (rs.next()) {
                exists = true;
                currentObfuscatedLogin = rs.getString(1);
                currentObfuscatedPassword = rs.getString(2);
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;

            if (exists) {
                // Check current credentials
                if (obfuscatedCredentials.getLogin().equals(currentObfuscatedLogin) && obfuscatedCredentials.getPassword().equals(currentObfuscatedPassword)) {
                    // Nothing to do
                    return false;
                }

                // Perform UPDATE statement
                stmt = connection.prepareStatement("UPDATE credentials SET password=?, login=? WHERE cid=? AND user=? AND password=? AND login=?");
                stmt.setString(1, obfuscatedCredentials.getPassword());
                stmt.setString(2, obfuscatedCredentials.getLogin());
                stmt.setInt(3, contextId);
                stmt.setInt(4, userId);
                stmt.setString(5, currentObfuscatedPassword);
                stmt.setString(6, currentObfuscatedLogin);
                int updatedRows = stmt.executeUpdate();
                return updatedRows > 0 ? true : storeCredentials(obfuscatedCredentials, false, connection);
            }

            // Perform INSERT statement
            stmt = connection.prepareStatement("INSERT INTO credentials (cid, user, password, login) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE password=?, login=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, obfuscatedCredentials.getPassword());
            stmt.setString(4, obfuscatedCredentials.getLogin());
            stmt.setString(5, obfuscatedCredentials.getPassword());
            stmt.setString(6, obfuscatedCredentials.getLogin());
            try {
                int insertedRows = stmt.executeUpdate();
                return insertedRows > 0 ? true : storeCredentials(obfuscatedCredentials, false, connection);
            } catch (SQLException e) {
                // Duplicate write attempt
                if (!retry) {
                    throw e;
                }
                return storeCredentials(obfuscatedCredentials, false, connection);
            }
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public Credentials deleteCredentials(int userId, int contextId) throws OXException {
        DatabaseService service = CredStorageServices.requireService(DatabaseService.class);
        Connection connection = service.getWritable(contextId);
        Credentials deletedCredentials = null;
        try {
            deletedCredentials = deleteCredentials(userId, contextId, connection);
            return obfuscator.unobfuscateCredentials(deletedCredentials);
        } finally {
            if (null == deletedCredentials) {
                service.backWritableAfterReading(contextId, connection);
            } else {
                service.backWritable(contextId, connection);
            }
        }
    }

    private Credentials deleteCredentials(int userId, int contextId, Connection connection) throws OXException {
        PreparedStatement stmt = null;
        try {
            Credentials credentials = getCredentials(userId, contextId, connection);
            if (null == credentials) {
                return null;
            }

            stmt = connection.prepareStatement("DELETE FROM credentials WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0 ? credentials : null;
        } catch (SQLException e) {
            throw PushExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
