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

package com.openexchange.chronos.storage.rdb.migration;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.groupware.update.UpdateConcurrency.BLOCKING;
import static com.openexchange.groupware.update.WorkingLevel.SCHEMA;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link ChronosStoragePurgeLegacyDataTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ChronosStoragePurgeLegacyDataTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link ChronosStoragePurgeLegacyDataTask}.
     */
    public ChronosStoragePurgeLegacyDataTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { ChronosStorageMigrationTask.class.getName() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BLOCKING, SCHEMA);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        String[] tableNames = new String[] {
            "prg_dates", "del_dates", "prg_dates_members", "del_dates_members",
            "prg_date_rights", "del_date_rights", "dateExternal", "delDateExternal"
        };
        Connection connection = params.getConnection();
        boolean committed = false;
        try {
            connection.setAutoCommit(false);
            /*
             * drop legacy calendar tables
             */
            for (String tableName : tableNames) {
                dropTableIfExists(connection, tableName);
            }
            /*
             * delete any appointment reminder triggers
             */
            purgeAppointmentReminders(connection);
            connection.commit();
            committed = true;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (false == committed) {
                rollback(connection);
            }
            autocommit(connection);
        }
    }

    private static int purgeAppointmentReminders(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM reminder WHERE module=?;")) {
            stmt.setInt(1, com.openexchange.groupware.Types.APPOINTMENT);
            return stmt.executeUpdate();
        }
    }

    private static int dropTableIfExists(Connection connection, String tableName) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate("DROP TABLE IF EXISTS " + tableName + ';');
        }
    }

}
