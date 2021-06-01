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
 * {@link FolderAddIndex2LastModified} - Creates indexes on tables "prg_contacts" and "del_contacts" to improve auto-complete
 * search.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderAddIndex2LastModified extends UpdateTaskAdapter {

    public FolderAddIndex2LastModified() {
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
            createMyIndex(con, new String[] { "oxfolder_tree", "del_oxfolder_tree" }, "lastModifiedIndex");
            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw createSQLError(e);
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    private void createMyIndex(final Connection con, final String[] tables, final String name) {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FolderAddIndex2LastModified.class);
        final String[] columns = { "cid", "changing_date", "module" };
        final StringBuilder sb = new StringBuilder(64);
        for (final String table : tables) {
            try {
                final String indexName = existsIndex(con, table, columns);
                if (null == indexName) {
                    if (log.isInfoEnabled()) {
                        sb.setLength(0);
                        sb.append("Creating new index named \"");
                        sb.append(name);
                        sb.append("\" with columns (cid, changing_date, module) on table ");
                        sb.append(table);
                        sb.append('.');
                        log.info(sb.toString());
                    }
                    createIndex(con, table, name, columns, false);
                } else {
                    if (log.isInfoEnabled()) {
                        sb.setLength(0);
                        sb.append("New index named \"");
                        sb.append(indexName);
                        sb.append("\" with columns (cid, changing_date, module)");
                        sb.append(" already exists on table ");
                        sb.append(table);
                        sb.append('.');
                        log.info(sb.toString());
                    }
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
