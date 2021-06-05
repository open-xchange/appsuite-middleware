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

package com.openexchange.drive.checksum.rdb;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.database.Databases.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link DirectoryChecksumsAddUserAndETagColumnTask}
 *
 * Deletes all existing directory checksums if the <code>user</code> column not yet exists, then
 * <ul>
 * <li>modifies the column <code>sequence</code> to <code>sequence BIGINT(20) DEFAULT NULL</code></li>
 * <li>adds the column <code>user INT4 UNSIGNED DEFAULT NULL</code></li>
 * <li>adds the column <code>etag VARCHAR(255) DEFAULT NULL</code></li>
 * </ul>
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectoryChecksumsAddUserAndETagColumnTask extends UpdateTaskAdapter {

    @Override
    public String[] getDependencies() {
        return new String[] { DriveCreateTableTask.class.getName() };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;

            if (false == Tools.columnExists(connection, "directoryChecksums", "user")) {
                deleteDirectoryChecksums(connection);
            }
            if (false == Tools.isNullable(connection, "directoryChecksums", "sequence")) {
                Tools.modifyColumns(connection, "directoryChecksums", new Column("sequence", "BIGINT(20) DEFAULT NULL"));
            }
            Tools.checkAndAddColumns(connection, "directoryChecksums",
                new Column("user", "INT4 UNSIGNED DEFAULT NULL"), new Column("etag", "VARCHAR(255) DEFAULT NULL"));

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    rollback(connection);
                }
                autocommit(connection);
            }
        }
    }

    /**
     * Deletes all entries from the <code>directoryChecksums</code> table.
     *
     * @param connection The connection
     * @throws SQLException
     */
    private static void deleteDirectoryChecksums(Connection connection) throws SQLException {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute("DELETE FROM directoryChecksums;");
        } finally {
            closeSQLStuff(stmt);
        }
    }

}
