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

package com.openexchange.jslob.storage.db.groupware;

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
 * {@link DBJSlobIncreaseBlobSizeTask} - Changes the column "data" from table "jsonStorage" from type BLOB (64KB) to MEDIUMBLOB (16MB).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DBJSlobIncreaseBlobSizeTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link DBJSlobIncreaseBlobSizeTask}.
     *
     * @param services The service look-up
     */
    public DBJSlobIncreaseBlobSizeTask() {
        super();
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection writeCon = params.getConnection();
        int rollback = 0;
        try {
            String typeName = Tools.getColumnTypeName(writeCon, "jsonStorage", "data");
            if ("MEDIUMBLOB".equalsIgnoreCase(typeName)) {
                // Nothing to do
                return;
            }

            // Change to MEDIUMBLOB
            writeCon.setAutoCommit(false); // BEGIN
            rollback = 1;

            Column column = new Column("data", "MEDIUMBLOB");
            Tools.modifyColumns(writeCon, "jsonStorage", column);

            writeCon.commit(); // COMMIT
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(writeCon);
                }
                Databases.autocommit(writeCon);
            }
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { DBJSlobCreateTableTask.class.getName() };
    }
}
