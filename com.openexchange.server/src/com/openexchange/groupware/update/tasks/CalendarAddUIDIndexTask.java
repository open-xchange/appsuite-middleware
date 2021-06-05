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
import static com.openexchange.tools.update.Tools.createIndex;
import static com.openexchange.tools.update.Tools.existsIndex;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link CalendarAddUIDIndexTask} - Adds (cid,uid) index to calendar tables if missing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CalendarAddUIDIndexTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link CalendarAddUIDIndexTask}.
     */
    public CalendarAddUIDIndexTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { EnlargeCalendarUid.class.getName() };
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (false == Databases.tablesExist(con, "prg_dates", "del_dates")) {
                return;
            }
            con.setAutoCommit(false);
            rollback = 1;

            final String[] tables = new String[] {"prg_dates", "del_dates"};
            createCalendarIndex(con, tables);

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    rollback(con);
                }
                autocommit(con);
            }
        }
    }

    private void createCalendarIndex(final Connection con, final String[] tables) {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CalendarAddUIDIndexTask.class);
        final String name = "uidIndex";
        for (final String table : tables) {
            try {
                final String indexName = existsIndex(con, table, new String[] { "cid", "uid" });
                if (null == indexName) {
                    log.info("Creating new index named \"{}\" with columns (cid,uid) on table {}.", name, table);
                    createIndex(con, table, name, new String[] { "cid", "`uid`(255)" }, false);
                } else {
                    log.info("New index named \"{}\" with columns (cid,uid) already exists on table {}.", indexName, table);
                }
            } catch (SQLException e) {
                log.error("Problem adding index \"{}\" on table {}.", name, table, e);
            }
        }
    }

}
