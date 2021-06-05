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
import com.openexchange.tools.update.Tools;


/**
 * {@link DelDatesPrimaryKeyUpdateTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class DelDatesPrimaryKeyUpdateTask extends UpdateTaskAdapter {

    private static final String DEL_DATES = "del_dates";
    private static final String DEL_DATE_EXTERNAL = "delDateExternal";

    /**
     * Initializes a new {@link DelDatesPrimaryKeyUpdateTask}.
     */
    public DelDatesPrimaryKeyUpdateTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (false == Databases.tablesExist(con, "del_dates", "delDateExternal")) {
                return;
            }
            con.setAutoCommit(false);
            rollback = 1;

            String foreignKey = Tools.existsForeignKey(
                con,
                DEL_DATES,
                new String[] { "cid", "intfield01" },
                DEL_DATE_EXTERNAL,
                new String[] { "cid", "objectId" });
            if (null != foreignKey && !foreignKey.equals("")) {
                Tools.dropForeignKey(con, DEL_DATE_EXTERNAL, foreignKey);
            }
            if (Tools.hasPrimaryKey(con, DEL_DATES)) {
                Tools.dropPrimaryKey(con, DEL_DATES);
                Tools.createPrimaryKey(con, DEL_DATES, new String[] { "cid", "intfield01", "fid" });
            }

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
        return new String[] { DelDateExternalDropForeignKeyUpdateTask.class.getName() };
    }

}
