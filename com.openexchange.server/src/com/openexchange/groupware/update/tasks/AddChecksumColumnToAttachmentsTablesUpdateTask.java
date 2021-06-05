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
 * {@link AddChecksumColumnToAttachmentsTablesUpdateTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.3
 */
public class AddChecksumColumnToAttachmentsTablesUpdateTask extends UpdateTaskAdapter {
    
    private final static String[] TABLES = { "prg_attachment", "del_attachment" };
    private final static Column column = new Column("checksum", "VARCHAR(32) DEFAULT NULL");

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        try {
            con.setAutoCommit(false);
            for (String table : TABLES) {
                Tools.checkAndAddColumns(con, table, column);
            }
            con.commit();
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.autocommit(con);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { AttachmentConvertUtf8ToUtf8mb4Task.class.getName() };
    }

}
