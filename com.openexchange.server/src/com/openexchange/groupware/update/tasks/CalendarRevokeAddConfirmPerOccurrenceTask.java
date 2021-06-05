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
 * {@link CalendarRevokeAddConfirmPerOccurrenceTask} - Extends those calendar tables that carry confirmation information by "occurrence" column.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CalendarRevokeAddConfirmPerOccurrenceTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link CalendarRevokeAddConfirmPerOccurrenceTask}.
     */
    public CalendarRevokeAddConfirmPerOccurrenceTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { CalendarAddConfirmPerOccurrenceTask.class.getName(), CalendarRevokeAddNewPrimaryKeyForConfirmPerOccurrence.class.getName() };
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (false == Databases.tablesExist(con, "prg_dates_members", "del_dates_members", "dateExternal", "delDateExternal")) {
                return;
            }
            con.setAutoCommit(false);
            rollback = 1;

            final Column occurrenceColumn = new Column("occurrence", "INT(10) unsigned NOT NULL DEFAULT '0'");

            Tools.checkAndDropColumns(con, "prg_dates_members", occurrenceColumn);
            Tools.checkAndDropColumns(con, "del_dates_members", occurrenceColumn);

            Tools.checkAndDropColumns(con, "dateExternal", occurrenceColumn);
            Tools.checkAndDropColumns(con, "delDateExternal", occurrenceColumn);

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    rollback(con);
                }
                autocommit(con);
            }
        }
    }

}
