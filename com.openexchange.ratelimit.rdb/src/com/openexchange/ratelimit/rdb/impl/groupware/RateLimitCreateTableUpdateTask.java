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

package com.openexchange.ratelimit.rdb.impl.groupware;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.database.Databases.tableExists;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.LoggerFactory;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link RateLimitCreateTableUpdateTask} - The appropriate update task to create needed "ratelimit" table.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class RateLimitCreateTableUpdateTask extends UpdateTaskAdapter {

    private final RateLimitCreateTableService service;

    /**
     * Initializes a new {@link RateLimitCreateTableUpdateTask}.
     */
    public RateLimitCreateTableUpdateTask(RateLimitCreateTableService createTableService) {
        super();
        this.service = createTableService;
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            String tableName = service.tablesToCreate()[0];
            if (tableExists(connection, tableName)) {
                LoggerFactory.getLogger(RateLimitCreateTableUpdateTask.class).debug("Table {} already exists, skipping.", tableName);
                return;
            }

            connection.setAutoCommit(false);
            rollback = 1;

            {
                PreparedStatement stmt = null;
                try {
                    stmt = connection.prepareStatement(service.getCreateStatements()[0]);
                    stmt.executeUpdate();
                } finally {
                    closeSQLStuff(stmt);
                }
            }

            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
        }
    }

}
