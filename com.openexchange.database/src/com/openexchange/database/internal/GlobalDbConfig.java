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

package com.openexchange.database.internal;

import com.openexchange.exception.OXException;

/**
 * {@link GlobalDbConfig}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class GlobalDbConfig {

    /** The name used for the special "default" context group */
    static final String DEFAULT_GROUP = "default";

    private final String schema;
    private final int readPoolId;
    private final int writePoolId;

    /**
     * Initializes a new {@link GlobalDbConfig}.
     *
     * @param schema The schema name
     * @param readPoolId The read pool identifier
     * @param writePoolId The write pool identifier
     */
    GlobalDbConfig(String schema, int readPoolId, int writePoolId) {
        super();
        this.schema = schema;
        this.readPoolId = readPoolId;
        this.writePoolId = writePoolId;
    }

    /**
     * Gets an appropriate read-/write-pool assignment for this global database config.
     *
     * @return The assignment
     */
    public AssignmentImpl getAssignment() throws OXException {
        return getAssignment(Server.getServerId());
    }

    /**
     * Gets an appropriate read-/write-pool assignment for this global database config.
     *
     * @param serverID The server identifier to use
     * @return The assignment
     */
    public AssignmentImpl getAssignment(int serverID) {
        return new AssignmentImpl(0, serverID, readPoolId, writePoolId, schema);
    }

    /**
     * Gets the schema name.
     *
     * @return The schema name
     */
    public String getSchema() {
        return schema;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + readPoolId;
        result = prime * result + ((schema == null) ? 0 : schema.hashCode());
        result = prime * result + writePoolId;
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
        if (!(obj instanceof GlobalDbConfig)) {
            return false;
        }
        GlobalDbConfig other = (GlobalDbConfig) obj;
        if (readPoolId != other.readPoolId) {
            return false;
        }
        if (schema == null) {
            if (other.schema != null) {
                return false;
            }
        } else if (!schema.equals(other.schema)) {
            return false;
        }
        if (writePoolId != other.writePoolId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GlobalDbConfig [schema=" + schema + ", readPoolId=" + readPoolId + ", writePoolId=" + writePoolId + "]";
    }

}
