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
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link AllowNullValuesForStandardFolderNamesUpdateTask}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class AllowNullValuesForStandardFolderNamesUpdateTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link AllowNullValuesForStandardFolderNamesUpdateTask}.
     */
    public AllowNullValuesForStandardFolderNamesUpdateTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            Column[] columns = new Column[14];
            columns[0] = new Column("trash", "varchar(64)");
            columns[1] = new Column("sent", "varchar(64)");
            columns[2] = new Column("drafts", "varchar(64)");
            columns[3] = new Column("spam", "varchar(64)");
            columns[4] = new Column("confirmed_spam", "varchar(64)");
            columns[5] = new Column("confirmed_ham", "varchar(64)");
            columns[6] = new Column("archive", "varchar(64)");
            columns[7] = new Column("trash_fullname", "varchar(256)");
            columns[8] = new Column("sent_fullname", "varchar(256)");
            columns[9] = new Column("drafts_fullname", "varchar(256)");
            columns[10] = new Column("spam_fullname", "varchar(256)");
            columns[11] = new Column("confirmed_spam_fullname", "varchar(256)");
            columns[12] = new Column("confirmed_ham_fullname", "varchar(256)");
            columns[13] = new Column("archive_fullname", "varchar(256)");
            Tools.modifyColumns(con, "user_mail_account", false, columns);

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
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
