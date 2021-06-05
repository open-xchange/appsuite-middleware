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

package com.openexchange.filestore.impl.groupware;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link AddUserColumnToFilestoreUsageTable} - Extends "filestore_usage" table by the column <code>`user`</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AddUserColumnToFilestoreUsageTable extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link AddUserColumnToFilestoreUsageTable}.
     */
    public AddUserColumnToFilestoreUsageTable() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Logger log = org.slf4j.LoggerFactory.getLogger(AddUserColumnToFilestoreUsageTable.class);
        log.info("Performing update task {}", AddUserColumnToFilestoreUsageTable.class.getSimpleName());

        Connection con = params.getConnection();
        int rollback = 0;
        try {
            Databases.startTransaction(con);
            rollback = 1;

            // Add the new column
            Column[] columns;
            {
                List<Column> l = new LinkedList<Column>();
                l.add(new Column("user", "INT4 unsigned NOT NULL DEFAULT 0"));
                columns = l.toArray(new Column[l.size()]);
            }
            Tools.checkAndAddColumns(con, "filestore_usage", columns);

            // Change PRIMARY_KEY
            Tools.createPrimaryKeyIfAbsent(con, "filestore_usage", new String[] { "cid", "user" });

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }

        log.info("{} successfully performed.", AddUserColumnToFilestoreUsageTable.class.getSimpleName());
    }

    @Override
    public String[] getDependencies() {
        return new String[] { AddFilestoreColumnsToUserTable.class.getName() };
    }
}
