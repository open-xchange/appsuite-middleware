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

package liquibase.precondition.ext;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.lang.Validate;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomPreconditionErrorException;
import liquibase.exception.CustomPreconditionFailedException;
import liquibase.precondition.CustomPrecondition;

/**
 * Checks if the denoted column is allowed to hold <code>NULL</code> values.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.8.1
 */
public class IsNullPrecondition implements CustomPrecondition {

    private String tableName;
    private String columnName;

    /**
     * Initializes a new {@link IsNullPrecondition}.
     */
    public IsNullPrecondition() {
        super();
    }

    /**
     * Sets the tableName (by reflection from liquibase)
     *
     * @param tableName The tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Sets the columnName (by reflection from liquibase)
     *
     * @param columnName The columnName to set
     */
    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void check(final Database database) throws CustomPreconditionFailedException, CustomPreconditionErrorException {
        ResultSet rsColumns = null;
        try {
            Validate.notNull(database, "Database provided by Liquibase might not be null!");

            DatabaseConnection databaseConnection = database.getConnection();
            Validate.notNull(databaseConnection, "DatabaseConnection might not be null!");

            JdbcConnection connection = null;
            if (databaseConnection instanceof JdbcConnection) {
                connection = (JdbcConnection) databaseConnection;
            } else {
                throw new CustomPreconditionErrorException("Cannot get underlying connection because database connection is not of type " + JdbcConnection.class.getSimpleName() + ". Type is: " + databaseConnection.getClass().getName());
            }

            boolean columnFound = false;
            DatabaseMetaData meta = connection.getUnderlyingConnection().getMetaData();
            rsColumns = meta.getColumns(null, null, tableName, null);
            if (!rsColumns.next()) {
                throw new CustomPreconditionErrorException("No columns for table " + tableName + " found! Aborting database migration execution for the given changeset.");
            }

            do {
                final String lColumnName = rsColumns.getString("COLUMN_NAME");
                if (columnName.equals(lColumnName)) {
                    columnFound = true;

                    /*-
                     * NULLABLE int => is NULL allowed.
                     *  - columnNoNulls - might not allow NULL values
                     *  - columnNullable - definitely allows NULL values
                     *  - columnNullableUnknown - nullability unknown
                     */

                    int nullable = rsColumns.getInt("NULLABLE");
                    if (DatabaseMetaData.typeNullable != nullable) {
                        throw new CustomPreconditionFailedException("A NULL value is not allowed for this data type.");
                    }
                }
            } while (rsColumns.next());

            if (!columnFound) {
                throw new CustomPreconditionErrorException("Desired column to update not found! Tried update for column " + columnName + " on table " + tableName);
            }
        } catch (SQLException sqlException) {
            throw new CustomPreconditionErrorException("Error while evaluating type of column " + columnName + " in table " + tableName + ".", sqlException);
        } catch (RuntimeException e) {
            throw new CustomPreconditionErrorException("Unexpected error", e);
        } finally {
            if (null != rsColumns) {
                try { rsColumns.close(); } catch (SQLException e) { /* Ignore */ }
            }
        }
    }
}
