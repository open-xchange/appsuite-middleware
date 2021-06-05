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

package com.openexchange.database;

/**
 * {@link SchemaInfo} - An immutable simple wrapper for a pool identifier and schema name tuple.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class SchemaInfo {

    /**
     * Creates a new {@code SchemaInfo} instance for specified pool identifier and schema name.
     *
     * @param poolId The identifier of the database pool, in which the schema resides
     * @param schema The schema name
     * @return The resulting {@code SchemaInfo} instance
     */
    public static SchemaInfo valueOf(int poolId, String schema) {
        return new SchemaInfo(poolId, schema);
    }

    // ------------------------------------------------------------------------------------------------------------

    private final int poolId;
    private final String schema;
    private int hash = 0;

    /**
     * Initializes a new {@link SchemaInfo}.
     */
    private SchemaInfo(int poolId, String schema) {
        super();
        this.poolId = poolId;
        this.schema = schema;
    }

    /**
     * Gets the identifier of the database pool, in which the schema resides
     *
     * @return The pool identifier
     */
    public int getPoolId() {
        return poolId;
    }

    /**
     * Gets the schema name
     *
     * @return The schema name
     */
    public String getSchema() {
        return schema;
    }

    @Override
    public int hashCode() {
        // Not thread-safe, but does not matter. In worst case a thread calculates the hash code itself
        int result = hash;
        if (result == 0) {
            result = 31 * 1 + poolId;
            result = 31 * result + ((schema == null) ? 0 : schema.hashCode());
            this.hash = result;
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SchemaInfo)) {
            return false;
        }
        SchemaInfo other = (SchemaInfo) obj;
        if (poolId != other.poolId) {
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
        StringBuilder builder = new StringBuilder(32);
        builder.append("[poolId=").append(poolId).append(", ");
        builder.append("schema=").append(schema);
        builder.append("]");
        return builder.toString();
    }

}
