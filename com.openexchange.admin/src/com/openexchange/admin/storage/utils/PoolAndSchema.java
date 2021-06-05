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

package com.openexchange.admin.storage.utils;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.database.Databases;
import com.openexchange.java.Strings;

/**
 * {@link PoolAndSchema} - A simple helper class to hold the pool identifier and name of the associated database schema.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class PoolAndSchema {

    /**
     * Determines available pools/schemas.
     *
     * @param serverId The server identifier
     * @param configDbCon The connection to configDb
     * @return The available pools/schemas
     * @throws StorageException If pools/schemas cannot be determined
     */
    public static Set<PoolAndSchema> determinePoolsAndSchemas(int serverId, Connection configDbCon) throws StorageException {
        // Determine available pools/schemas
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = configDbCon.prepareStatement("SELECT DISTINCT write_db_pool_id, db_schema FROM context_server2db_pool WHERE server_id=?");
            stmt.setInt(1, serverId);
            rs = stmt.executeQuery();

            Set<PoolAndSchema> pools = new LinkedHashSet<PoolAndSchema>();
            while (rs.next()) {
                pools.add(new PoolAndSchema(rs.getInt(1), rs.getString(2)));
            }
            return pools;
        } catch (SQLException e) {
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
        }
    }

    /**
     * Lists all available schemas.
     * <p>
     * More or less the same as {@link #determinePoolsAndSchemas(int, Connection)}, but returning fully filled instances of {@link Database}.
     *
     * @param configDbCon The connection to configDb
     * @return The available schemas
     * @throws StorageException If schemas cannot be returned
     */
    public static List<Database> listAllSchemas(Connection configDbCon) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = configDbCon.prepareStatement("SELECT c.write_db_pool_id,s.schemaname,db.url,db.driver,db.login,db.password,db.name,c.read_db_pool_id,c.max_units FROM db_cluster AS c LEFT JOIN contexts_per_dbschema AS s ON c.write_db_pool_id=s.db_pool_id JOIN db_pool AS db ON db.db_pool_id = c.write_db_pool_id");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<Database> databases = new LinkedList<Database>();
            int pos;
            do {
                pos = 1;
                int poolId = rs.getInt(pos++);
                String schema = rs.getString(pos++);
                if (Strings.isNotEmpty(schema)) {
                    Database db = new Database();
                    db.setId(I(poolId));
                    db.setUrl(rs.getString(pos++));
                    db.setDriver(rs.getString(pos++));
                    db.setLogin(rs.getString(pos++));
                    db.setPassword(rs.getString(pos++));
                    db.setName(rs.getString(pos++));
                    final int slaveId = rs.getInt(pos++);
                    if (slaveId > 0) {
                        db.setRead_id(I(slaveId));
                    }
                    db.setMaxUnits(I(rs.getInt(pos++)));
                    db.setScheme(schema);
                    databases.add(db);
                }
            } while (rs.next());
            return databases;
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Lists all available schemas for specified database.
     *
     * @param databaseId The database identifier
     * @param configDbCon The connection to configDb
     * @return The available schemas
     * @throws StorageException If schemas cannot be returned
     */
    public static List<Database> listDatabaseSchemas(int databaseId, Connection configDbCon) throws StorageException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = configDbCon.prepareStatement("SELECT s.schemaname,db.url,db.driver,db.login,db.password,db.name,c.read_db_pool_id,c.max_units FROM db_cluster AS c LEFT JOIN contexts_per_dbschema AS s ON c.write_db_pool_id=s.db_pool_id JOIN db_pool AS db ON db.db_pool_id = c.write_db_pool_id WHERE c.write_db_pool_id=?");
            stmt.setInt(1, databaseId);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptyList();
            }

            List<Database> databases = new LinkedList<Database>();
            int pos;
            do {
                pos = 1;
                String schema = rs.getString(pos++);
                if (Strings.isNotEmpty(schema)) {
                    Database db = new Database();
                    db.setId(I(databaseId));
                    db.setUrl(rs.getString(pos++));
                    db.setDriver(rs.getString(pos++));
                    db.setLogin(rs.getString(pos++));
                    db.setPassword(rs.getString(pos++));
                    db.setName(rs.getString(pos++));
                    final int slaveId = rs.getInt(pos++);
                    if (slaveId > 0) {
                        db.setRead_id(I(slaveId));
                    }
                    db.setMaxUnits(I(rs.getInt(pos++)));
                    db.setScheme(schema);
                    databases.add(db);
                }
            } while (rs.next());
            return databases;
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

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
