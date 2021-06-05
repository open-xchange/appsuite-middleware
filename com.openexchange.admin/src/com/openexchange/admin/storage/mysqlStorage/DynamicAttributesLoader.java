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

package com.openexchange.admin.storage.mysqlStorage;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.tools.pipesnfilters.Filter;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersException;

/**
 * {@link DynamicAttributesLoader}
 *
 */
public class DynamicAttributesLoader implements Filter<Context, Context> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DynamicAttributesLoader.class);

    private final AdminCache cache;

    public DynamicAttributesLoader(AdminCache cache) {
        super();
        this.cache = cache;
    }

    @Override
    public Context[] filter(Collection<Context> contexts) throws PipesAndFiltersException {
        Map<Integer, Map<String, Map<Integer, Context>>> readIdMap = new HashMap<Integer, Map<String, Map<Integer, Context>>>();
        for (Context context : contexts) {
            Integer readId = context.getReadDatabase().getId();
            Map<String, Map<Integer, Context>> schemaMap = readIdMap.get(readId);
            if (null == schemaMap) {
                schemaMap = new HashMap<String, Map<Integer, Context>>();
                readIdMap.put(readId, schemaMap);
            }
            String schema = context.getReadDatabase().getScheme();
            Map<Integer, Context> cidMap = schemaMap.get(schema);
            if (null == cidMap) {
                cidMap = new HashMap<Integer, Context>();
                schemaMap.put(schema, cidMap);
            }
            cidMap.put(context.getId(), context);
        }
        List<Context> retval = new ArrayList<Context>();
        for (Entry<Integer, Map<String, Map<Integer, Context>>> readIdEntry : readIdMap.entrySet()) {
            for (Entry<String, Map<Integer, Context>> schemaEntry : readIdEntry.getValue().entrySet()) {
                try {
                    retval.addAll(loadAttributes(schemaEntry.getValue()));
                } catch (StorageException e) {
                    throw new PipesAndFiltersException(e);
                }
            }
        }
        return retval.toArray(new Context[retval.size()]);
    }

    private static final String SQL = "SELECT cid, name, value FROM contextAttribute";

    private Collection<Context> loadAttributes(Map<Integer, Context> contexts) throws StorageException {
        int cid = contexts.values().iterator().next().getId().intValue();
        Connection con;
        try {
            con = cache.getConnectionForContext(cid);
        } catch (PoolException e) {
            throw new StorageException(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL);
            rs = stmt.executeQuery();
            while (rs.next()) {
                Context context = contexts.get(I(rs.getInt(1)));
                if (null != context) {
                    final String name = rs.getString(2);
                    final String value = rs.getString(3);
                    if (OXContextMySQLStorageCommon.isDynamicAttribute(name)) {
                        final String[] namespaced = OXContextMySQLStorageCommon.parseDynamicAttribute(name);
                        context.setUserAttribute(namespaced[0], namespaced[1], value);
                    }
                }
            }
        } catch (SQLException e) {
            throw new StorageException(e.getMessage(), e);
        } finally {
            closeSQLStuff(rs, stmt);
            if (null != con) {
                try {
                    cache.pushConnectionForContextAfterReading(cid, con);
                } catch (PoolException e) {
                    LOG.error("", e);
                }
            }
        }
        return contexts.values();
    }
}
