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
import static com.openexchange.database.Databases.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Tools;

/**
 * {@link DirectoryChecksumsReIndexTask}
 *
 * Removes the obsolete <code>(folder, cid)</code> and <code>(checksum, cid)</code> indices and creates the following new ones:
 * <code>(cid, user, folder)</code> and <code>(cid, checksum)</code>
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectoryChecksumsReIndexTask extends UpdateTaskAdapter {

    @Override
    public String[] getDependencies() {
        return new String[] { DirectoryChecksumsAddUserAndETagColumnTask.class.getName() };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;
            /*
             * remove obsolete indices as needed
             */
            String oldIndexName = Tools.existsIndex(connection, "directoryChecksums", new String[] { "checksum", "cid" });
            if (null != oldIndexName) {
                Tools.dropIndex(connection, "directoryChecksums", oldIndexName);
            }
            oldIndexName = Tools.existsIndex(connection, "directoryChecksums", new String[] { "folder", "cid" });
            if (null != oldIndexName) {
                Tools.dropIndex(connection, "directoryChecksums", oldIndexName);
            }
            /*
             * create new indices
             */
            Tools.createIndex(connection, "directoryChecksums", new String[] { "cid", "checksum" });
            Tools.createIndex(connection, "directoryChecksums", new String[] { "cid", "user", "folder" });
            /*
             * commit
             */
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

}
