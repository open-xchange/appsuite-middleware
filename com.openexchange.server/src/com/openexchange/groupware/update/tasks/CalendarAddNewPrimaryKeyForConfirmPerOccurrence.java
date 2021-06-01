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
import java.util.Arrays;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Tools;

/**
 * {@link CalendarAddNewPrimaryKeyForConfirmPerOccurrence} - Adapts the keys for those calendar tables that carry confirmation information to new "occurrence" column.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CalendarAddNewPrimaryKeyForConfirmPerOccurrence extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link CalendarAddNewPrimaryKeyForConfirmPerOccurrence}.
     */
    public CalendarAddNewPrimaryKeyForConfirmPerOccurrence() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { PrgDatesMembersPrimaryKeyUpdateTask.class.getName(), DelDatesMembersPrimaryKeyUpdateTask.class.getName(), CalendarAddConfirmPerOccurrenceTask.class.getName() };
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (false == Databases.tablesExist(con, "prg_dates", "del_dates", "dateExternal", "delDateExternal", "prg_dates_members", "del_dates_members")) {
                return;
            }
            con.setAutoCommit(false);
            rollback = 1;

            {
                // Drop & re-create primary key
                final String[] tables = new String[] { "prg_dates_members", "del_dates_members" };
                final String[] columns = new String[] {"cid","object_id","member_uid","pfid","occurrence"};

                final int[] lengths = new int[5];
                Arrays.fill(lengths, 0);
                checkPrimaryKey(columns, lengths, tables, con);

                // Drop & re-create unique key
                {
                    final String[] oldCols = new String[] {"cid","member_uid","object_id"};
                    final String[] newCols = new String[] {"cid","member_uid","object_id", "occurrence"};
                    checkUniqueKey(oldCols, newCols, tables, con);
                }
            }

            {
                // Drop foreign key: dateExternal(cid, objectId) -> prg_dates(cid, intfield01)
                String foreignKey = Tools.existsForeignKey(con, "prg_dates", new String[] {"cid", "intfield01"}, "dateExternal", new String[] {"cid", "objectId"});
                if (null != foreignKey && !foreignKey.equals("")) {
                    Tools.dropForeignKey(con, "dateExternal", foreignKey);
                }

                // Drop foreign key: delDateExternal(cid, objectId) -> del_dates(cid, intfield01)
                foreignKey = Tools.existsForeignKey(con, "del_dates", new String[] {"cid", "intfield01"}, "delDateExternal", new String[] {"cid", "objectId"});
                if (null != foreignKey && !foreignKey.equals("")) {
                    Tools.dropForeignKey(con, "delDateExternal", foreignKey);
                }

                // Drop & re-create primary key
                final String[] tables = new String[] { "dateExternal", "delDateExternal" };
                final String[] columns = new String[] {"cid","objectId","mailAddress","occurrence"};
                final int[] lengths = new int[4];
                Arrays.fill(lengths, 0);
                lengths[2] = 255;
                checkPrimaryKey(columns, lengths, tables, con);
            }

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

    private void checkPrimaryKey(final String[] columns, final int[] lengths, final String[] tables, final Connection connnection) throws SQLException {
        for (final String table : tables) {
            if (!Tools.existsPrimaryKey(connnection, table, columns)) {
                try {
                    Tools.dropPrimaryKey(connnection, table);
                } catch (Exception x) {
                    // Ignore failed deletion
                }
                Tools.createPrimaryKey(connnection, table, columns, lengths);
            }
        }
    }

    private void checkUniqueKey(final String[] oldColumns, final String[] newColumns, final String[] tables, final Connection connnection) throws SQLException {
        for (final String table : tables) {
            final String oldIndex = Tools.existsIndex(connnection, table, oldColumns);
            if (null != oldIndex) {
                Tools.dropIndex(connnection, table, oldIndex);
            }

            final String newIndex = Tools.existsIndex(connnection, table, newColumns);
            if (null == newIndex) {
                Tools.createIndex(connnection, table, "member", newColumns, true);
            }
        }
    }

}
