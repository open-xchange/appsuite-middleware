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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;


/**
 * {@link AddFailedAuthColumnsToMailAccountTablesTask} - Adds several columns to "user_mail_account" and "user_transport_account" tables for tracking/managing failed authentication attempts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class AddFailedAuthColumnsToMailAccountTablesTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link AddFailedAuthColumnsToMailAccountTablesTask}.
     */
    public AddFailedAuthColumnsToMailAccountTablesTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            Databases.startTransaction(con);
            rollback = 1;

            Column columnDisabled = new Column("disabled", "TINYINT UNSIGNED NOT NULL DEFAULT 0");
            Column columnFailedAuthCount = new Column("failed_auth_count", "INT4 UNSIGNED NOT NULL DEFAULT 0");
            Column columnFailedAuthDate = new Column("failed_auth_date", "BIGINT(64) NOT NULL DEFAULT 0");
            for (String table : new String[] { "user_mail_account", "user_transport_account" }) {
                List<Column> cols = new ArrayList<>(2);
                if (false == Tools.columnExists(con, table, "disabled")) {
                    cols.add(columnDisabled);
                }
                if (false == Tools.columnExists(con, table, "failed_auth_count")) {
                    cols.add(columnFailedAuthCount);
                }
                if (false == Tools.columnExists(con, table, "failed_auth_date")) {
                    cols.add(columnFailedAuthDate);
                }
                int size = cols.size();
                if (size > 0) {
                    Tools.addColumns(con, table, cols.toArray(new Column[size]));
                }
            }

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

    @Override
    public String[] getDependencies() {
        return new String[] { com.openexchange.groupware.update.tasks.AddStartTLSColumnForMailAccountTablesTask.class.getName() };
    }

}
