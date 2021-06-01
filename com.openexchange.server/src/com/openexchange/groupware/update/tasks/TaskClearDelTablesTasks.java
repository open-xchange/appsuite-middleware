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
 * {@link TaskClearDelTablesTasks}
 *
 * Removes obsolete data from the 'del_task' table.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class TaskClearDelTablesTasks extends UpdateTaskAdapter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TaskClearDelTablesTasks.class);

    /**
     * Initializes a new {@link TaskClearDelTablesTasks}.
     */
    public TaskClearDelTablesTasks() {
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
            con.setAutoCommit(false);
            rollback = 1;

            LOG.info("Clearing obsolete fields in 'del_task'...");
            int cleared = clearDeletedTasks(con);
            LOG.info("Cleared {} rows in 'del_task'.", I(cleared));

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    private static int clearDeletedTasks(Connection connection) throws SQLException {
//        String[] columsToRemain = {
//            "cid", "id", "private", "creating_date", "last_modified", "created_from", "changed_from",
//            "number_of_attachments", "uid", "filename", "recurrence_type"
//        };
        String[] columsToClear = {
            "start", "end", "completed", "title", "description", "state", "priority", "progress", "categories", "project",
            "target_duration", "actual_duration", "target_costs", "actual_costs", "currency", "trip_meter", "billing", "companies",
            "color_label", "recurrence_interval", "recurrence_days", "recurrence_dayinmonth", "recurrence_month", "recurrence_until",
            "recurrence_count"
        };
        StringBuilder StringBuilder = new StringBuilder("UPDATE del_task SET ").append(columsToClear[0]).append("=NULL");
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
