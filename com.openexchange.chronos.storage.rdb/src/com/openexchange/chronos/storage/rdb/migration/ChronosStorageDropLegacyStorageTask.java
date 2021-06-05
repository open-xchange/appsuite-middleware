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
 * {@link ChronosStorageDropLegacyStorageTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class ChronosStorageDropLegacyStorageTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link ChronosStorageDropLegacyStorageTask}.
     */
    public ChronosStorageDropLegacyStorageTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { ChronosStoragePurgeLegacyDataTask.class.getName() };
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
