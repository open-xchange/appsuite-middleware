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

package com.openexchange.groupware.update;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.java.Strings;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * {@link AbstractLiquibaseUtf8mb4Adapter}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public abstract class AbstractLiquibaseUtf8mb4Adapter extends AbstractConvertUtf8ToUtf8mb4Task implements CustomTaskChange {

    @Override
    public final void execute(Database database) throws CustomChangeException {
        DatabaseConnection databaseConnection = database.getConnection();
        if (!(databaseConnection instanceof JdbcConnection)) {
            throw new CustomChangeException("Cannot get underlying connection because database connection is not of type " + JdbcConnection.class.getName() + ", but of type: " + databaseConnection.getClass().getName());
        }

        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AbstractLiquibaseUtf8mb4Adapter.class);

        Connection con = ((JdbcConnection) databaseConnection).getUnderlyingConnection();
        String schemaName = null;
        try {
            schemaName = getSchemaName(con, database);
            if (Strings.isEmpty(schemaName)) {
                throw new CustomChangeException("Unable to determine schema name");
            }

            before(con, schemaName);
            innerPerform(con, schemaName);
            after(con, schemaName);
        } catch (SQLException e) {
            logger.error("Failed to convert tables in schema {} to utf8mb4", schemaName, e);
            throw new CustomChangeException("SQL error", e);
        } catch (RuntimeException e) {
            logger.error("Failed to convert tables in schema {} to utf8mb4", schemaName, e);
            throw new CustomChangeException("Runtime error", e);
        }
    }

    private String getSchemaName(Connection connection, Database database) throws SQLException {
        String catalogName = connection.getCatalog();
        if (catalogName != null && Strings.isNotEmpty(catalogName)) {
            return catalogName;
        }

        String defaultCatalogName = database.getDefaultCatalogName();
        return (defaultCatalogName != null && Strings.isNotEmpty(defaultCatalogName) ? defaultCatalogName : getDefaultSchemaName());
    }

    protected abstract String getDefaultSchemaName();

    protected abstract void before(Connection configDbCon, String schemaName) throws SQLException;

    protected abstract void after(Connection configDbCon, String schemaName) throws SQLException;

    @Override
    public String[] getDependencies() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected final void before(PerformParameters params, Connection connection) throws SQLException {
        // cannot be used
    }

    @Override
    protected final void after(PerformParameters params, Connection connection) throws SQLException {
        // cannot be used
    }

    @Override
    public final void setUp() throws SetupException {
        // Nothing
    }

    @Override
    public final void setFileOpener(ResourceAccessor resourceAccessor) {
        // Ignore
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}
