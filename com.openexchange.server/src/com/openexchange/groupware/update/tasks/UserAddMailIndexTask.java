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
import static com.openexchange.tools.update.Tools.dropIndex;
import static com.openexchange.tools.update.Tools.existsIndex;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link UserAddMailIndexTask} -Adds/corrects user mail index:
 * <p>
 * <i>INDEX (mail)</i> -&gt; <i>INDEX (cid, mail(255))</i>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserAddMailIndexTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link UserAddMailIndexTask}.
     */
    public UserAddMailIndexTask() {
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
            con.setAutoCommit(false);
            rollback = 1;
            final String[] tables = new String[] { "user" };
            createMailIndex(con, tables);
            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw createSQLError(e);
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

    private void createMailIndex(final Connection con, final String[] tables) throws OXException {
        final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserAddMailIndexTask.class);
        final String name = "mailIndex";
        for (final String table : tables) {
            try {
                // Check for old index
                String indexName = existsIndex(con, table, new String[] { "mail" });
                if (null != indexName) {
                    dropIndex(con, table, indexName);
                }

                // Check for new index
                indexName = existsIndex(con, table, new String[] { "cid", "mail" });
                if (null == indexName) {
                    log.info("Creating new index named \"{}\" with columns (cid, mail) on table {}.", name, table);
                    createIndex(con, table, name, new String[] { "cid", "`mail`(255)" }, false);
                } else {
                    log.info("New index named \"{}\" with columns (cid, mail) already exists on table {}.", indexName, table);
                }
            } catch (SQLException e) {
                log.error("Problem adding index \"{}\" on table {}.", name, table, e);
                throw createSQLError(e);
            }
        }
    }

    private OXException createSQLError(final SQLException e) {
        return UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
    }

}
