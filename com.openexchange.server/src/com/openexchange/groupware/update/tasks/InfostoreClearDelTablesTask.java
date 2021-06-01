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

import static com.openexchange.groupware.update.UpdateConcurrency.BACKGROUND;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link InfostoreClearDelTablesTask} - Removes obsolete data from the 'del_infostore_document' table.
 * We don't need to clean up 'del_infostore' as it does only contain the metadata we have to keep anyway.<br>
 * <br>
 * Columns that will be cleaned up:
 * <ul>
 * <li>title</li>
 * <li>url</li>
 * <li>description</li>
 * <li>categories</li>
 * <li>filename</li>
 * <li>file_store_location</li>
 * <li>file_size</li>
 * <li>file_mimetype</li>
 * <li>file_md5sum</li>
 * <li>file_version_comment</li>
 * </ul>
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class InfostoreClearDelTablesTask extends UpdateTaskAdapter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostoreClearDelTablesTask.class);

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        PreparedStatement stmt = null;
        try {
            con.setAutoCommit(false);
            rollback = 1;
            LOG.info("Clearing obsolete fields in 'del_infostore_document'...");

            String query = "UPDATE " +
                               "del_infostore_document " +
                           "SET " +
                               "title = NULL, " +
                               "url = NULL, " +
                               "description = NULL, " +
                               "categories = NULL, " +
                               "filename = NULL, " +
                               "file_store_location = NULL, " +
                               "file_size = NULL, " +
                               "file_mimetype = NULL, " +
                               "file_md5sum = NULL, " +
                               "file_version_comment = NULL" +
                           ";";

            stmt = con.prepareStatement(query);
            int cleared = stmt.executeUpdate();
            LOG.info("Cleared {} rows in 'del_infostore_document'.", I(cleared));
            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BACKGROUND);
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

}
