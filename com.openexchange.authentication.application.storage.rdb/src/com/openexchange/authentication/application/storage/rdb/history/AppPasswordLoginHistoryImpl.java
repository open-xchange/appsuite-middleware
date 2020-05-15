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
 *    trademarks of the OX Software GmbH. group of companies.
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
