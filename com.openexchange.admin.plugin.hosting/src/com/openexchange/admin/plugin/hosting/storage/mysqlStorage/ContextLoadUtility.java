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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.admin.plugin.hosting.storage.mysqlStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.mysqlStorage.OXContextMySQLStorageCommon;
import com.openexchange.admin.storage.sqlStorage.OXAdminPoolInterface;
import com.openexchange.admin.storage.utils.PoolAndSchema;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.database.Databases;
import gnu.trove.list.TIntList;
import gnu.trove.map.TIntObjectMap;

/**
 * {@link ContextLoadUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ContextLoadUtility {

    /**
     * Provides the identifiers of the contexts to load.
     */
    public static interface ContextIdentifiersProvider {

        /**
         * Provides the identifiers of the contexts to load.
         *
         * @param con The connection to use
         * @return The context identifiers
         * @throws StorageException If returning context identifiers fails
         */
        TIntList getContextIdentifiers(Connection con) throws StorageException;
    }

    // -------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link ContextLoadUtility}.
     */
    private ContextLoadUtility() {
        super();
    }

    /**
     * Loads basic context data from specified result set
     *
     * @param rs The result set to load from
     * @param firstSelected <code>true</code> if first row is already selected; otherwise <code>false</code>
     * @param averageContextFileStoreSize The average context file storage size
     * @param length The number of queried contexts or <code>-1</code>
     * @return The loaded contexts
     * @throws StorageException If loading contexts fails
     */
    public static List<Context> loadBasicContexts(ResultSet rs, boolean firstSelected, Long averageContextFileStoreSize, int length) throws StorageException {
        try {
            if (false == firstSelected && false == rs.next()) {
                return Collections.emptyList();
            }

            List<Context> contexts = length > 0 ? new ArrayList<>(length) : new LinkedList<>();
            do {
                doLoadBasicContexts(rs, averageContextFileStoreSize, contexts);
            } while (rs.next());
            return contexts;
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    private static void doLoadBasicContexts(ResultSet rs, Long averageContextFileStoreSize, List<Context> contexts) throws StorageException {
        try {
            Context cs = new Context();

            int context_id = rs.getInt(1);
            cs.setId(Integer.valueOf(context_id));
            cs.setUsedQuota(Long.valueOf(0));

            String name = rs.getString(2); // name
            // name of the context, currently same with contextid
            if (name != null) {
                cs.setName(name);
            }

            cs.setEnabled(Boolean.valueOf(rs.getBoolean(3))); // enabled
            int reason_id = rs.getInt(4); // reason
            // CONTEXT STATE INFOS #
            if (-1 != reason_id) {
                cs.setMaintenanceReason(new MaintenanceReason(Integer.valueOf(reason_id)));
            }
            cs.setFilestoreId(Integer.valueOf(rs.getInt(5))); // filestore_id
            cs.setFilestore_name(rs.getString(6)); // filestore_name
            long quota_max = rs.getLong(7); // quota_max
            if (quota_max != -1) {
                quota_max = quota_max >> 20;
                // set quota max also in context setup object
                cs.setMaxQuota(Long.valueOf(quota_max));
            }
            cs.setAverage_size(averageContextFileStoreSize);

            contexts.add(cs);
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    /**
     * Fills given contexts with login mappings and database associations (read/write host).
     *
     * @param contexts The contexts to fill
     * @param id2context Helper map
     * @param con The connection to ConfigDB to use
     * @return The filled contexts grouped by schema
     * @throws StorageException If filling contexts fails
     */
    public static Map<PoolAndSchema, List<Context>> fillLoginMappingsAndDatabases(List<Context> contexts, TIntObjectMap<Context> id2context, Connection con) throws StorageException {
        if (contexts.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<PoolAndSchema, List<Context>> schema2contexts = new HashMap<>(contexts.size());
        if (contexts.size() <= Databases.IN_LIMIT) {
            fillLoginMappingsAndDatabasesChunk(contexts, false, id2context, con, schema2contexts);
        } else {
            for (List<Context> partition : Lists.partition(contexts, Databases.IN_LIMIT)) {
                fillLoginMappingsAndDatabasesChunk(partition, true, id2context, con, schema2contexts);
            }
        }
        return schema2contexts;
    }

    private static void fillLoginMappingsAndDatabasesChunk(List<Context> partition, boolean checkIfEmpty, TIntObjectMap<Context> id2context, Connection con, Map<PoolAndSchema, List<Context>> schema2contexts) throws StorageException {
        if (checkIfEmpty && partition.isEmpty()) {
            // Nothing to do
            return;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Load login mappings
            stmt = con.prepareStatement(Databases.getIN("SELECT login_info, cid FROM login2context WHERE cid IN (", partition.size()));
            id2context.clear();
            int pos = 1;
            for (Context context : partition) {
                int contextId = context.getId().intValue();
                stmt.setInt(pos++, contextId);
                id2context.put(contextId, context);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                id2context.get(rs.getInt(2)).addLoginMapping(rs.getString(1));
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            // Group by database schema association
            stmt = con.prepareStatement(Databases.getIN("SELECT write_db_pool_id, read_db_pool_id, db_schema, cid FROM context_server2db_pool WHERE cid IN (", partition.size()));
            pos = 1;
            for (Context context : partition) {
                stmt.setInt(pos++, context.getId().intValue());
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                String db_schema = rs.getString(3); // db_schema
                if (null != db_schema) {
                    int write_pool = rs.getInt(1); // write_pool_id
                    int read_pool = rs.getInt(2); // read_pool_id
                    Context cs = id2context.get(rs.getInt(4));
                    cs.setReadDatabase(new Database(read_pool, db_schema));
                    cs.setWriteDatabase(new Database(write_pool, db_schema));
                    PoolAndSchema pas = new PoolAndSchema(read_pool <= 0 ? write_pool : read_pool, db_schema);
                    List<Context> allInSchema = schema2contexts.get(pas);
                    if (null == allInSchema) {
                        allInSchema = new LinkedList<>();
                        schema2contexts.put(pas, allInSchema);
                    }
                    allInSchema.add(cs);
                }
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
        } catch (SQLException e) {
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Fills given contexts with file storage usage and context attributes.
     *
     * @param schema2contexts The contexts grouped by schema
     * @param withContextAttributes Whether to load context attributes
     * @param id2context Helper map
     * @param cache The admin cache for obtaining a connection
     * @throws StorageException If filling the contexts fails
     */
    public static void fillUsageAndAttributes(Map<PoolAndSchema, List<Context>> schema2contexts, boolean withContextAttributes, TIntObjectMap<Context> id2context, AdminCacheExtended cache) throws StorageException {
        if (schema2contexts.isEmpty()) {
            return;
        }

        // Query used quota per schema
        try {
            OXAdminPoolInterface pool = cache.getPool();
            for (Map.Entry<PoolAndSchema, List<Context>> schema2contextsEntry : schema2contexts.entrySet()) {
                PoolAndSchema poolAndSchema = schema2contextsEntry.getKey();
                Connection oxdb_read = pool.getConnection(poolAndSchema.getPoolId(), poolAndSchema.getSchema());
                try {
                    List<Context> allInSchema = schema2contextsEntry.getValue();
                    fillContextsSchema(allInSchema, withContextAttributes, id2context, oxdb_read);
                } finally {
                    pool.pushConnection(poolAndSchema.getPoolId(), oxdb_read);
                }
            }
        } catch (PoolException e) {
            throw new StorageException(e);
        }
    }

    private static void fillContextsSchema(List<Context> allInSchema, boolean withContextAttributes, TIntObjectMap<Context> id2context, Connection oxdb_read) throws StorageException {
        if (allInSchema.isEmpty()) {
            return;
        }

        if (allInSchema.size() <= Databases.IN_LIMIT) {
            fillContextsSchemaChunk(allInSchema, false, withContextAttributes, id2context, oxdb_read);
        } else {
            for (List<Context> partitionInSchema : Lists.partition(allInSchema, Databases.IN_LIMIT)) {
                fillContextsSchemaChunk(partitionInSchema, true, withContextAttributes, id2context, oxdb_read);
            }
        }
    }

    private static void fillContextsSchemaChunk(List<Context> partitionInSchema, boolean checkIfEmpty, boolean withContextAttributes, TIntObjectMap<Context> id2context, Connection oxdb_read) throws StorageException {
        if (checkIfEmpty && partitionInSchema.isEmpty()) {
            return;
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = oxdb_read.prepareStatement(Databases.getIN("SELECT used, cid FROM filestore_usage WHERE user=0 AND cid IN (", partitionInSchema.size()));
            id2context.clear();
            int pos = 1;
            for (Context context : partitionInSchema) {
                int contextId = context.getId().intValue();
                stmt.setInt(pos++, contextId);
                id2context.put(contextId, context);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                long quota_used = rs.getLong(1);
                if (quota_used > 0) {
                    quota_used = quota_used >> 20;
                }
                id2context.get(rs.getInt(2)).setUsedQuota(Long.valueOf(quota_used));
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            if (withContextAttributes) {
                stmt = oxdb_read.prepareStatement(Databases.getIN("SELECT cid, name, value FROM contextAttribute WHERE cid IN (", partitionInSchema.size()));
                pos = 1;
                for (Context context : partitionInSchema) {
                    stmt.setInt(pos++, context.getId().intValue());
                }
                rs = stmt.executeQuery();
                while (rs.next()) {
                    String name = rs.getString(2);
                    if (OXContextMySQLStorageCommon.isDynamicAttribute(name)) {
                        String[] namespaced = OXContextMySQLStorageCommon.parseDynamicAttribute(name);
                        String value = rs.getString(3);
                        id2context.get(rs.getInt(1)).setUserAttribute(namespaced[0], namespaced[1], value);
                    }
                }
                Databases.closeSQLStuff(rs, stmt);
                rs = null;
                stmt = null;
            }
        } catch (SQLException e) {
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
