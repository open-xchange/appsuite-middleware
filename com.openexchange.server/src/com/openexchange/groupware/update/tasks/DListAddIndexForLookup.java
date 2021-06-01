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
import java.util.Arrays;
import org.slf4j.LoggerFactory;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link DListAddIndexForLookup} - Creates indexes on tables "prg_dlist" and "del_dlist" to improve look-up.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DListAddIndexForLookup extends UpdateTaskAdapter {

    public DListAddIndexForLookup() {
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
            con.setAutoCommit(false);
            rollback = 1;

            final String[] tables = { "prg_dlist", "del_dlist" };
            createDListIndex(con, tables, "userIndex", "intfield02", "intfield03");

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.UPDATE_FAILED.create(e, params.getSchema().getSchema(), e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(con);
                }
                autocommit(con);
            }
        }
    }

    private void createDListIndex(final Connection con, final String[] tables, final String name, final String... columns) throws SQLException {
        final org.slf4j.Logger log = LoggerFactory.getLogger(DListAddIndexForLookup.class);
        final String[] cols = new String[columns.length + 1];
        cols[0] = "cid";
        System.arraycopy(columns, 0, cols, 1, columns.length);
        final StringBuilder sb = new StringBuilder(64);
        for (final String table : tables) {
            final String indexName = existsIndex(con, table, cols);
            if (null == indexName) {
                if (log.isInfoEnabled()) {
                    sb.setLength(0);
                    sb.append("Creating new index named \"");
                    sb.append(name);
                    sb.append("\" with columns ");
                    sb.append(Arrays.toString(cols));
                    sb.append(" on table ");
                    sb.append(table);
                    sb.append('.');
                    log.info(sb.toString());
                }
                createIndex(con, table, name, cols, false);
            } else {
                if (log.isInfoEnabled()) {
                    sb.setLength(0);
                    sb.append("New index named \"");
                    sb.append(indexName);
                    sb.append("\" with columns ");
                    sb.append(Arrays.toString(cols));
                    sb.append(" already exists on table ");
                    sb.append(table);
                    sb.append('.');
                    log.info(sb.toString());
                }
            }
        }
    }

}
