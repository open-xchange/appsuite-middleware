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

package com.openexchange.context;

/**
 * {@link PoolAndSchema} - A pair of a database (write) pool identifier and a schema name.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class PoolAndSchema {

    private final int poolId;
    private final String schema;
    private final int hash;

    /**
     * Initializes a new {@link PoolAndSchema}.
     *
     * @param poolId The pool identifier
     * @param schema The name of the associated database schema
     */
    public PoolAndSchema(int poolId, String schema) {
        super();
        this.poolId = poolId;
        this.schema = schema;

        int result = 31 * 1 + poolId;
        result = 31 * result + ((schema == null) ? 0 : schema.hashCode());
        this.hash = result;
    }

    /**
     * Gets the name of the associated database schema
     *
     * @return The name of the associated database schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Gets the pool identifier
     *
     * @return The pool identifier
     */
    public int getPoolId() {
        return poolId;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PoolAndSchema)) {
            return false;
        }
        PoolAndSchema other = (PoolAndSchema) obj;
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

}
