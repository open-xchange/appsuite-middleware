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

package com.openexchange.authentication.application.storage.rdb.history;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.openexchange.authentication.application.exceptions.AppPasswordExceptionCodes;
import com.openexchange.authentication.application.storage.AuthenticatedApplicationPassword;
import com.openexchange.authentication.application.storage.history.AppPasswordLogin;
import com.openexchange.authentication.application.storage.history.AppPasswordLoginHistoryStorage;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link AppPasswordLoginHistoryImpl}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.4
 */
public class AppPasswordLoginHistoryImpl implements AppPasswordLoginHistoryStorage {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AppPasswordLoginHistoryImpl}.
     * 
     * @param services The service lookup
     */
    public AppPasswordLoginHistoryImpl(ServiceLookup services) {
        this.services = services;
    }

    /**
     * Get registered DatabaseService
     *
     * @return The database service
     * @throws OXException if the service is absent
     */
    private DatabaseService getDatabaseService() throws OXException {
        return services.getServiceSafe(DatabaseService.class);
    }

    @Override
    public void trackLogin(AuthenticatedApplicationPassword authenticatedPassword, AppPasswordLogin login) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getWritable(authenticatedPassword.getContextId());
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(LoginHistorySQL.UPDATE_LOGIN);
            int index = 1;
            statement.setBytes(index++, UUIDs.toByteArray(UUID.fromString(authenticatedPassword.getApplicationPassword().getGUID())));
            statement.setInt(index++, authenticatedPassword.getContextId());
            statement.setInt(index++, authenticatedPassword.getUserId());
            statement.setLong(index++, login.getTimestamp());
            statement.setString(index++, login.getClient());
            statement.setString(index++, login.getUserAgent());
            statement.setString(index++, login.getIpAddress());
            statement.execute();
        } catch (SQLException e) {
            throw AppPasswordExceptionCodes.DATABASE_ERROR.create(e);
        } finally {
            Databases.closeSQLStuff(statement);
            if (connection != null) {
                databaseService.backWritable(authenticatedPassword.getContextId(), connection);
            }
        }
    }

    @Override
    public Map<String, AppPasswordLogin> getHistory(Session session) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getReadOnly(session.getContextId());
        PreparedStatement statement = null;
        ResultSet rs = null;
        Map<String, AppPasswordLogin> results = new HashMap<String, AppPasswordLogin>();
        try {
            statement = connection.prepareStatement(LoginHistorySQL.GET_LOGIN_HISTORY);
            int index = 1;
            statement.setInt(index++, session.getUserId());
            statement.setInt(index++, session.getContextId());
            rs = statement.executeQuery();
            while (rs.next()) {
                String uuid = UUIDs.toUUID(rs.getBytes("uuid")).toString();
                final String ip = rs.getString("ip");
                AppPasswordLogin login =
                    AppPasswordLogin.builder().setUserAgent(rs.getString("userAgent")).setClient(rs.getString("client")).setIpAddress(ip).setTimestamp(rs.getLong("timestamp")).build();
                results.put(uuid, login);
            }
            return results;
        } catch (SQLException e) {
            throw AppPasswordExceptionCodes.DATABASE_ERROR.create(e);
        } finally {
            Databases.closeSQLStuff(rs, statement);
            if (connection != null) {
                databaseService.backReadOnly(session.getContextId(), connection);
            }
        }
    }

    @Override
    public void deleteHistory(Session session, String passwordId) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection connection = databaseService.getWritable(session.getContextId());
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(LoginHistorySQL.DELETE_FOR_UUID);
            int index = 1;
            statement.setBytes(index++, UUIDs.toByteArray(UUID.fromString(passwordId)));
            statement.execute();
        } catch (SQLException e) {
            throw AppPasswordExceptionCodes.DATABASE_ERROR.create(e);
        } finally {
            Databases.closeSQLStuff(statement);
            if (connection != null) {
                databaseService.backWritable(session.getContextId(), connection);
            }
        }
    }

    public void deleteForContext(int contextId, Connection connection) throws OXException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(LoginHistorySQL.DELETE_FOR_CONTEXT);
            int index = 1;
            statement.setInt(index++, contextId);
            statement.execute();
        } catch (SQLException e) {
            throw AppPasswordExceptionCodes.DATABASE_ERROR.create(e);
        } finally {
            Databases.closeSQLStuff(statement);
        }

    }

    public void deleteForUser(int userId, int contextId, Connection connection) throws OXException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(LoginHistorySQL.DELETE_FOR_USER);
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

}
