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

package com.openexchange.database.internal.change.custom;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.Databases;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * {@link RemoveParametersFromPoolConnectionUrlCustomTaskChange}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.1
 */
public class RemoveParametersFromPoolConnectionUrlCustomTaskChange implements CustomTaskChange {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveParametersFromPoolConnectionUrlCustomTaskChange.class);

    @Override
    public String getConfirmationMessage() {
        return "Successfully removed parameters from all Connection URLs in the db_pool table.";
    }

    @Override
    public void setUp() {
        // nothing
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        // nothing
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    @Override
    public void execute(Database database) throws CustomChangeException {
        DatabaseConnection databaseConnection = database.getConnection();
        if (!(databaseConnection instanceof JdbcConnection)) {
            throw new CustomChangeException("Cannot get underlying connection because database connection is not of type " + JdbcConnection.class.getName() + ", but of type: " + databaseConnection.getClass().getName());
        }
        Connection configDbCon = ((JdbcConnection) databaseConnection).getUnderlyingConnection();
        int rollback = 0;
        try {
            Databases.startTransaction(configDbCon);
            rollback = 1;

            execute(configDbCon);

            configDbCon.commit();
            rollback = 2;
        } catch (SQLException e) {
            LOG.error("Failed to removed parameters from all Connection URLs in the db_pool table", e);
            throw new CustomChangeException("SQL error", e);
        } catch (RuntimeException e) {
            LOG.error("Failed to removed parameters from all Connection URLs in the db_pool table", e);
            throw new CustomChangeException("Runtime error", e);
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(configDbCon);
                }
                Databases.autocommit(configDbCon);
            }
        }
    }

    private static final String SELECT = "SELECT db_pool_id, url FROM db_pool";

    private static final String UPDATE = "UPDATE db_pool SET url = ? WHERE db_pool_id = ?";

    private void execute(Connection con) throws SQLException {
        Map<Integer, String> id2Url = new HashMap<>();
        Map<Integer, String> id2NewUrl = new HashMap<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SELECT);
            rs = stmt.executeQuery();
            while (rs.next()) {
                id2Url.put(I(rs.getInt("db_pool_id")), rs.getString("url"));
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            stmt = null;
            rs = null;
        }

        for (Entry<Integer, String> entry : id2Url.entrySet()) {
            String url = entry.getValue();
            int paramStart = url.indexOf('?');
            if (paramStart != -1) {
                id2NewUrl.put(entry.getKey(), url.substring(0, paramStart));
            }
        }

        if (!id2NewUrl.isEmpty()) {
            try {
                stmt = con.prepareStatement(UPDATE);
                for (Entry<Integer, String> entry : id2NewUrl.entrySet()) {
                    stmt.setString(1, entry.getValue());
                    stmt.setInt(2, entry.getKey().intValue());
                    stmt.addBatch();

                    LOG.info("Changed url for db_pool_id {} from '{}' to '{}'", entry.getKey(), id2Url.get(entry.getKey()), id2NewUrl.get(entry.getKey()));
                }
                stmt.executeBatch();
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }
    }

}
