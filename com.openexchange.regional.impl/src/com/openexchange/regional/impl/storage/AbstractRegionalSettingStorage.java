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

package com.openexchange.regional.impl.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptions;
import com.openexchange.regional.RegionalSettings;
import com.openexchange.regional.impl.service.RegionalSettingsImpl;
import com.openexchange.regional.impl.service.RegionalSettingsImpl.Builder;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractRegionalSettingStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.3
 */
abstract class AbstractRegionalSettingStorage {

    private final ServiceLookup services;

    /**
     * Initialises a new {@link AbstractRegionalSettingStorage}.
     */
    AbstractRegionalSettingStorage(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Performs an SQL query with the specified statement and the specified {@link PreparedStatementConsumer}.
     * 
     * @param contextId The context identifier
     * @param statement The SQL statement to execute
     * @param consumer The statement consumer to fill in the SQL statement values
     * @return The {@link RegionalSettingsImpl}
     * @throws OXException if an error is occurred
     */
    RegionalSettings query(int contextId, String statement, PreparedStatementConsumer consumer) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection con = databaseService.getReadOnly(contextId);
        ResultSet resultSet = null;
        try (PreparedStatement stmt = con.prepareStatement(statement)) {
            consumer.accept(stmt);
            resultSet = stmt.executeQuery();
            return resultSet.next() ? readSettings(resultSet) : null;
        } catch (SQLException e) {
            throw OXExceptions.database(e);
        } finally {
            Databases.closeSQLStuff(resultSet);
            databaseService.backReadOnly(contextId, con);
        }
    }

    /**
     * Performs an SQL update with the specified statement and the specified {@link PreparedStatementConsumer}.
     * 
     * @param contextId the context identifier
     * @param statement The update SQL statement
     * @param consumer The statement consumer to fill in the SQL statement values
     * @throws OXException if an error is occurred
     */
    void update(int contextId, String statement, PreparedStatementConsumer consumer) throws OXException {
        update(contextId, statement, consumer, null);
    }

    /**
     * Performs an SQL update with the specified statement and the specified {@link PreparedStatementConsumer}.
     * 
     * @param contextId the context identifier
     * @param statement The update SQL statement
     * @param consumer The statement consumer to fill in the SQL statement values
     * @throws OXException if an error is occurred
     */
    void update(int contextId, String statement, PreparedStatementConsumer consumer, Connection writeCon) throws OXException {
        boolean connectionInit = false;
        DatabaseService databaseService = getDatabaseService();
        if (writeCon == null) {
            writeCon = databaseService.getWritable(contextId);
            connectionInit = true;
        }
        try (PreparedStatement stmt = writeCon.prepareStatement(statement)) {
            consumer.accept(stmt);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw OXExceptions.database(e);
        } finally {
            if (connectionInit) {
                databaseService.backWritable(contextId, writeCon);
            }
        }
    }

    ///////////////////////////////////////////////////////// HELPERS ////////////////////////////////////////////////////////

    /**
     * Returns the {@link DatabaseService}
     * 
     * @return the {@link DatabaseService}
     * @throws OXException if the service is absent
     */
    private DatabaseService getDatabaseService() throws OXException {
        return services.getServiceSafe(DatabaseService.class);
    }

    /**
     * Creates a {@link RegionalSettingsImpl} object from the {@link ResultSet}
     *
     * @param resultSet The {@link ResultSet}
     * @return The {@link RegionalSettingsImpl}
     * @throws SQLException if an SQL error is occurred
     */
    private RegionalSettings readSettings(ResultSet resultSet) throws SQLException {
        String timeFormat = resultSet.getString("timeFormat");
        String timeFormatLong = resultSet.getString("timeFormatLong");
        String dateFormat = resultSet.getString("dateFormat");
        String dateFormatShort = resultSet.getString("dateFormatShort");
        String dateFormatMedium = resultSet.getString("dateFormatMedium");
        String dateFormatLong = resultSet.getString("dateFormatLong");
        String dateFormatFull = resultSet.getString("dateFormatFull");
        String numberFormat = resultSet.getString("numberFormat");
        int firstDayOfWeek = resultSet.getInt("firstDayOfWeek");
        int firstDayOfYear = resultSet.getInt("firstDayOfYear");

        Builder builder = RegionalSettingsImpl.newBuilder();

        if (timeFormat != null) {
            builder.withTimeFormat(timeFormat);
        }

        if (timeFormatLong != null) {
            builder.withTimeFormatLong(timeFormatLong);
        }

        if (dateFormat != null) {
            builder.withDateFormat(dateFormat);
        }

        if (dateFormatShort != null) {
            builder.withDateFormatShort(dateFormatShort);
        }

        if (dateFormatMedium != null) {
            builder.withDateFormatMedium(dateFormatMedium);
        }

        if (dateFormatLong != null) {
            builder.withDateFormatLong(dateFormatLong);
        }

        if (dateFormatFull != null) {
            builder.withDateFormatFull(dateFormatFull);
        }

        if (numberFormat != null) {
            builder.withNumberFormat(numberFormat);
        }

        if (firstDayOfWeek >= 1) {
            builder.withFirstDayOfWeek(firstDayOfWeek);
        }

        if (firstDayOfYear >= 1) {
            builder.withFirstDayOfYear(firstDayOfYear);
        }

        return builder.build();
    }
}
