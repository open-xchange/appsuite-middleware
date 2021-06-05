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

package com.openexchange.database.migration;

import liquibase.resource.ResourceAccessor;

/**
 * {@link DBMigration}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DBMigration {

    private final DBMigrationConnectionProvider connectionProvider;
    private final String fileLocation;
    private final ResourceAccessor accessor;
    private final String schema;

    /**
     * Initializes a new {@link DBMigration}.
     *
     * @param connectionProvider The connection provider used to acquire and release database connections
     * @param fileLocation Location of the changelog file (e.g. "/liquibase/configdbChangeLog.xml")
     * @param accessor The {@link ResourceAccessor} to read in the changelog file identified by <code>fileLocation</code>
     * @param schema The database schema name the migration operates on
     */
    public DBMigration(DBMigrationConnectionProvider connectionProvider, String fileLocation, ResourceAccessor accessor, String schema) {
        super();
        this.connectionProvider = connectionProvider;
        this.fileLocation = fileLocation;
        this.accessor = accessor;
        this.schema = schema;
    }

    /**
     * Gets the connection provider.
     *
     * @return The connectionProvider
     */
    public DBMigrationConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    /**
     * Gets the file location.
     *
     * @return The file location
     */
    public String getFileLocation() {
        return fileLocation;
    }

    /**
     * Gets the database schema name.
     *
     * @return The schema name
     */
    public String getSchemaName() {
        return schema;
    }

    /**
     * Gets the resource accessor.
     *
     * @return The accessor
     */
    public ResourceAccessor getAccessor() {
        return accessor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fileLocation == null) ? 0 : fileLocation.hashCode());
        result = prime * result + ((schema == null) ? 0 : schema.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DBMigration)) {
            return false;
        }
        DBMigration other = (DBMigration) obj;
        if (fileLocation == null) {
            if (other.fileLocation != null) {
                return false;
            }
        } else if (!fileLocation.equals(other.fileLocation)) {
            return false;
        }
        if (schema == null) {
            if (other.schema != null) {
                return false;
            }
        } else if (!schema.equals(other.schema)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "DBMigration [fileLocation=" + fileLocation + ", schema=" + schema + "]";
    }

}
