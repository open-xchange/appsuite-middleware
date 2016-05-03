/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.admin.storage.utils;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import com.openexchange.admin.rmi.exceptions.StorageException;

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
            closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
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
