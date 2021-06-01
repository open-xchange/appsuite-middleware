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
import static com.openexchange.database.Databases.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link CalendarAddConfirmPerOccurrenceTask} - Extends those calendar tables that carry confirmation information by "occurrence" column.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CalendarAddConfirmPerOccurrenceTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link CalendarAddConfirmPerOccurrenceTask}.
     */
    public CalendarAddConfirmPerOccurrenceTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            if (false == Databases.tablesExist(connection, "prg_dates_members", "del_dates_members", "dateExternal", "delDateExternal")) {
                return;
            }
            connection.setAutoCommit(false);
            rollback = 1;

            final Column occurrenceColumn = new Column("occurrence", "INT(10) unsigned NOT NULL DEFAULT '0'");

            Tools.checkAndAddColumns(connection, "prg_dates_members", occurrenceColumn);
            Tools.checkAndAddColumns(connection, "del_dates_members", occurrenceColumn);

            Tools.checkAndAddColumns(connection, "dateExternal", occurrenceColumn);
            Tools.checkAndAddColumns(connection, "delDateExternal", occurrenceColumn);

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    rollback(connection);
                }
                autocommit(connection);
            }
        }
    }

}
