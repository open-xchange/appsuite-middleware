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
 * {@link CalendarAddIndex2DatesMembers} - Creates indexes on tables "prg_contacts" and "del_contacts" to improve auto-complete
 * search.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class CalendarAddIndex2DatesMembers extends UpdateTaskAdapter {

    public CalendarAddIndex2DatesMembers() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (false == Databases.tablesExist(con, "prg_dates_members", "del_dates_members")) {
                return;
            }
            con.setAutoCommit(false);
            rollback = 1;

            createMyIndex(con, new String[] { "prg_dates_members", "del_dates_members" }, "pfid", "givenname");

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw createSQLError(e);
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

    private void createMyIndex(final Connection con, final String[] tables, final String fieldName, final String name) {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CalendarAddIndex2DatesMembers.class);
        final String[] columns = { "cid", fieldName };
        for (final String table : tables) {
            try {
                final String indexName = existsIndex(con, table, columns);
                if (null == indexName) {
                    log.info("Creating new index named \"{}\" with columns (cid,{}) on table {}.", name, fieldName, table);
                    createIndex(con, table, name, columns, false);
                } else {
                    log.info("New index named \"{}\" with columns (cid,{}) already exists in table {}.", indexName, fieldName, table);
                }
            } catch (SQLException e) {
                log.error("Problem adding index {} on table {}.", name, table, e);
            }
        }
    }

    private static OXException createSQLError(final SQLException e) {
        return UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
    }
}
