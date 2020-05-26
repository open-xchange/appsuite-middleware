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

package com.openexchange.chronos.storage.rdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProviders;
import com.openexchange.chronos.storage.AdministrativeCalendarAccountStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AdministrativeRdbCalendarAccountStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
public class AdministrativeRdbCalendarAccountStorage implements AdministrativeCalendarAccountStorage {

    private static final String DELETE_ACCOUNT = "DELETE FROM calendar_account WHERE cid=? AND id=? AND user=? AND id > 0 AND provider != '" + CalendarProviders.ID_BIRTHDAYS + "'";
    private static final String SELECT_CONTEXT = "SELECT cid,id,user,provider,modified,internalConfig,userConfig FROM calendar_account WHERE cid=?;";
    private static final String SELECT_CONTEXT_PROVIDER = "SELECT cid,id,user,provider,modified,internalConfig,userConfig FROM calendar_account WHERE cid=? AND provider=?;";
    private static final String SELECT_ACCOUNT = "SELECT cid,id,user,provider,modified,internalConfig,userConfig FROM calendar_account WHERE cid=? AND user=? AND id=? AND id > 0 AND provider != '" + CalendarProviders.ID_BIRTHDAYS + "'";
    private static final String SELECT_USER = "SELECT cid,id,user,provider,modified,internalConfig,userConfig FROM calendar_account WHERE cid=? AND user=?;";
    private static final String SELECT_PROVIDER = "SELECT cid,id,user,provider,modified,internalConfig,userConfig FROM calendar_account WHERE cid=? AND user=? AND provider=?;";

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AdministrativeRdbCalendarAccountStorage}.
     * 
     * @param services The {@link ServiceLookup} instance
     */
    public AdministrativeRdbCalendarAccountStorage(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public List<CalendarAccount> getAccounts(int contextId) throws OXException {
        Connection connection = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        DatabaseService databaseService = getDatabaseService();
        List<CalendarAccount> accounts = new LinkedList<>();
        try {
            connection = databaseService.getReadOnly(contextId);
            stmt = connection.prepareStatement(SELECT_CONTEXT);
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                accounts.add(RdbUtil.readAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(connection);
        }
    }

    @Override
    public List<CalendarAccount> getAccounts(int contextId, String providerId) throws OXException {
        Connection connection = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        DatabaseService databaseService = getDatabaseService();
        List<CalendarAccount> accounts = new LinkedList<>();
        try {
            connection = databaseService.getReadOnly(contextId);
            stmt = connection.prepareStatement(SELECT_CONTEXT_PROVIDER);
            stmt.setInt(1, contextId);
            stmt.setString(2, providerId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                accounts.add(RdbUtil.readAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(connection);
        }
    }

    @Override
    public List<CalendarAccount> getAccounts(int contextId, int userId) throws OXException {
        Connection connection = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        DatabaseService databaseService = getDatabaseService();
        List<CalendarAccount> accounts = new LinkedList<>();
        try {
            connection = databaseService.getReadOnly(contextId);
            stmt = connection.prepareStatement(SELECT_USER);
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                accounts.add(RdbUtil.readAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(connection);
        }
    }

    @Override
    public List<CalendarAccount> getAccounts(int contextId, int userId, String providerId) throws OXException {
        Connection connection = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        DatabaseService databaseService = getDatabaseService();
        List<CalendarAccount> accounts = new LinkedList<>();
        try {
            connection = databaseService.getReadOnly(contextId);
            stmt = connection.prepareStatement(SELECT_PROVIDER);
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setString(3, providerId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                accounts.add(RdbUtil.readAccount(rs));
            }
            return accounts;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(connection);
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
            connection = databaseService.getWritable(contextId);
            boolean deleted = deleteAccount(contextId, userId, accountId, connection);
            connection.commit();
            rollback = 2;
            return deleted;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
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
        PreparedStatement stmt = null;
        try {
            CalendarAccount account = getAccount(contextId, userId, accountId, connection);
            if (account == null) {
                return false;
            }

            stmt = connection.prepareStatement(DELETE_ACCOUNT);
            stmt.setInt(1, contextId);
            stmt.setInt(2, accountId);
            stmt.setInt(3, userId);
            boolean deleted = stmt.executeUpdate() > 0;

            invalidate(contextId, userId, account);
            return deleted;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    ////////////////////////////// HELPERS ///////////////////////////////

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
    private CalendarAccount getAccount(int contextId, int userId, int accountId, Connection connection) throws OXException {
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
            return RdbUtil.readAccount(rs);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(statement, rs);
        }
    }

    /**
     * Invalidates the specified account
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param account The account to invalidate
     * @throws OXException if invalidation fails
     */
    private void invalidate(int contextId, int userId, CalendarAccount account) throws OXException {
        ContextService contextService = getContextService();
        Context ctx = contextService.getContext(contextId);
        CalendarStorage calendarStorage = getCalendarStorageFactory().create(ctx, -1, null);
        calendarStorage.getAccountStorage().invalidateAccount(userId, account.getAccountId());
    }

    ///////////////////////////// SERVICES /////////////////////////////////

    /**
     * Returns the {@link CalendarStorageFactory}
     *
     * @return the {@link CalendarStorageFactory}
     * @throws OXException if the service is absent
     */
    private CalendarStorageFactory getCalendarStorageFactory() throws OXException {
        return services.getServiceSafe(CalendarStorageFactory.class);
    }

    /**
     * Returns the {@link ContextService}
     *
     * @return the {@link ContextService}
     * @throws OXException if the service is absent
     */
    private ContextService getContextService() throws OXException {
        return services.getServiceSafe(ContextService.class);
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
