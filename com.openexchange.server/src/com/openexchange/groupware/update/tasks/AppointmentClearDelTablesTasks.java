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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.groupware.update.UpdateConcurrency.BACKGROUND;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link AppointmentClearDelTablesTasks}
 *
 * Removes obsolete data from the 'del_dates' table.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class AppointmentClearDelTablesTasks extends UpdateTaskAdapter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AppointmentClearDelTablesTasks.class);

    /**
     * Initializes a new {@link AppointmentClearDelTablesTasks}.
     */
    public AppointmentClearDelTablesTasks() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (false == Databases.tableExists(con, "del_dates")) {
                return;
            }
            con.setAutoCommit(false);
            rollback = 1;

            LOG.info("Clearing obsolete fields in 'del_dates'...");
            int cleared = clearDeletedAppointments(con);
            LOG.info("Cleared {} rows in 'del_dates'.", I(cleared));

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(con);
                }
                autocommit(con);
            }
        }
    }

    private static int clearDeletedAppointments(Connection connection) throws SQLException {
//        String[] columsToRemain = {
//            "creating_date", "created_from", "changing_date", "changed_from", "fid", "pflag", "cid",
//            "intfield01", "intfield02", "uid", "filename"
//        };
        String[] columsToClear = {
            "timestampfield01", "timestampfield02", "timezone", "intfield03", "intfield04", "intfield05", "intfield06", "intfield07",
            "intfield08", "field01", "field02", "field04", "field06", "field07", "field08", "field09", "organizer", "sequence",
            "organizerId", "principal", "principalId"
        };
        StringBuilder StringBuilder = new StringBuilder("UPDATE del_dates SET ").append(columsToClear[0]).append("=NULL");
        for (int i = 1; i < columsToClear.length; i++) {
            StringBuilder.append(',').append(columsToClear[i]).append("=NULL");
        }
        StringBuilder.append(';');
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(StringBuilder.toString());
            return statement.executeUpdate();
        } finally {
            Databases.closeSQLStuff(statement);
        }
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BACKGROUND);
    }

}
