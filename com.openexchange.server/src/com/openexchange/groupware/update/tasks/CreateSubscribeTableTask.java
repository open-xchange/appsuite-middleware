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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Tools;


/**
 * {@link CreateSubscribeTableTask}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.5
 */
public class CreateSubscribeTableTask extends UpdateTaskAdapter {

    private final static String CREATE_SUBSCRIPTIONS_SQL =
        "CREATE TABLE subscriptions (" +
        "cid INT4 UNSIGNED NOT NULL," +
        "id INT4 UNSIGNED NOT NULL," +
        "user_id INT4 UNSIGNED NOT NULL," +
        "configuration_id INT4 UNSIGNED NOT NULL," +
        "source_id VARCHAR(255) NOT NULL," +
        "folder_id VARCHAR(255) NOT NULL," +
        "last_update INT8 UNSIGNED NOT NULL," +
        "enabled BOOLEAN DEFAULT true NOT NULL," +
        "created INT8 NOT NULL DEFAULT 0," +
        "lastModified INT8 NOT NULL DEFAULT 0," +
        "PRIMARY KEY (cid,id)," +
        "INDEX `folderIndex` (`cid`, `folder_id`(191))," +
        "FOREIGN KEY(cid,user_id) REFERENCES user(cid,id)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    private final static String CREATE_SEQUENCE_SUBSCRIPTIONS_SQL =
        "CREATE TABLE sequence_subscriptions (" +
        "cid INT4 UNSIGNED NOT NULL," +
        "id INT4 UNSIGNED NOT NULL," +
        "PRIMARY KEY (cid)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";

    @Override
    public void perform(PerformParameters params) throws OXException {
        createTable("subscriptions", CREATE_SUBSCRIPTIONS_SQL, params.getConnection());
        createTable("sequence_subscriptions", CREATE_SEQUENCE_SUBSCRIPTIONS_SQL, params.getConnection());
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    private void createTable(String tablename, String sqlCreate, Connection writeCon) throws OXException {
        PreparedStatement stmt = null;
        try {
            if (Tools.tableExists(writeCon, tablename)) {
                return;
            }
            stmt = writeCon.prepareStatement(sqlCreate);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
