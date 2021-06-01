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

package com.openexchange.ms.internal.portable;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import com.hazelcast.nio.serialization.PortableReader;
import com.hazelcast.nio.serialization.PortableWriter;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.hazelcast.serialization.AbstractCustomPortable;
import com.openexchange.java.Strings;
import com.openexchange.ms.internal.Services;

/**
 * {@link PortableContextInvalidationCallable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class PortableContextInvalidationCallable extends AbstractCustomPortable implements Callable<Boolean> {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PortableContextInvalidationCallable.class);

    public static final int CLASS_ID = 26;

    public static final String PARAMETER_POOL_IDS = "poolIds";
    public static final String PARAMETER_SCHEMAS = "schemas";

    private static final String CACHE_REGION_SCHEMA_STORE = "OXDBPoolCache";
    private static final String CACHE_REGION_CONTEXT = "Context";

    private int[] poolIds;
    private String[] schemas;

    /**
     * Initializes a new {@link PortableContextInvalidationCallable}.
     */
    public PortableContextInvalidationCallable() {
        super();
    }

    /**
     * Initializes a new {@link PortableContextInvalidationCallable}.
     *
     * @param contextIds The schemas, which shall be invalidated
     */
    public PortableContextInvalidationCallable(List<PoolAndSchema> list) {
        super();
        int size = list.size();
        poolIds = new int[size];
        schemas = new String[size];
        int i = 0;
        for (PoolAndSchema poolAndSchema : list) {
            poolIds[i] = poolAndSchema.getPoolId();
            schemas[i] = poolAndSchema.getSchema();
            i++;
        }
    }

    @Override
    public Boolean call() throws Exception {
        CacheService cacheService = Services.optService(CacheService.class);
        if (null == cacheService) {
            LOGGER.warn("Failed invalidating of the following schemas due to absence of cache service:{}{}", Strings.getLineSeparator(), info());
            return Boolean.FALSE;
        }

        // Invalidate the schemas
        invalidateSchemas(cacheService);

        // Invalidate the contexts associated with specified schemas
        invalidateContexts(cacheService);

        return Boolean.TRUE;
    }

    private void invalidateSchemas(CacheService cacheService) throws OXException {
        Cache schemaCache = cacheService.getCache(CACHE_REGION_SCHEMA_STORE);
        for (int i = 0; i < poolIds.length; i++) {
            int poolId = poolIds[i];
            String schema = schemas[i];
            schemaCache.localRemove(schemaCache.newCacheKey(poolId, schema));
            LOGGER.info("Successfully invalidated schema {} from pool {}", schema, Integer.valueOf(poolId));
        }
    }

    private void invalidateContexts(CacheService cacheService) {
        try {
            Map<PoolAndSchema, int[]> contextIdsForSchemas = getContextIdsForSchemas();
            if (contextIdsForSchemas.isEmpty()) {
                return;
            }

            Cache contextCache = cacheService.getCache(CACHE_REGION_CONTEXT);
            for (Map.Entry<PoolAndSchema, int[]> contextIds : contextIdsForSchemas.entrySet()) {
                List<Serializable> keys = new LinkedList<Serializable>();
                for (int contextID : contextIds.getValue()) {
                    Integer key = Integer.valueOf(contextID);
                    keys.add(key);
                    Object cached = contextCache.get(key);
                    if (null != cached && Context.class.isInstance(cached)) {
                        String[] loginInfos = ((Context) cached).getLoginInfo();
                        if (null != loginInfos && 0 < loginInfos.length) {
                            for (String loginInfo : loginInfos) {
                                keys.add(loginInfo);
                            }
                        }
                    }
                }
                for (Serializable key : keys) {
                    contextCache.localRemove(key);
                }
                LOGGER.info("Successfully invalidated contexts for schema {}", contextIds.getKey().getSchema());
            }

        } catch (Exception e) {
            LOGGER.error("Failed to invalidate contexts for schemas:{}{}", Strings.getLineSeparator(), info(), e);
        }
    }

    private Map<PoolAndSchema, int[]> getContextIdsForSchemas() throws OXException {
        DatabaseService databaseService = Services.optService(DatabaseService.class);
        if (null == databaseService) {
            return Collections.emptyMap();
        }

        Connection connection = databaseService.getReadOnly();
        try {
            Map<PoolAndSchema, int[]> cids = new LinkedHashMap<>(poolIds.length);
            for (int i = 0; i < poolIds.length; i++) {
                int poolId = poolIds[i];
                String schema = schemas[i];
                cids.put(new PoolAndSchema(poolId, schema), databaseService.getContextsInSchema(connection, poolId, schema));
            }
            return cids;
        } finally {
            databaseService.backReadOnly(connection);
        }
    }

    private String info() {
        StringBuilder sb = new StringBuilder(poolIds.length << 2);
        for (int i = 0; i < poolIds.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append('[').append(schemas[i]).append(", ").append(poolIds[i]).append(']');
        }
        return sb.toString();
    }

    @Override
    public int getClassId() {
        return CLASS_ID;
    }

    @Override
    public void writePortable(PortableWriter writer) throws IOException {
        writer.writeIntArray(PARAMETER_POOL_IDS, poolIds);
        writer.writeUTFArray(PARAMETER_SCHEMAS, schemas);
    }

    @Override
    public void readPortable(PortableReader reader) throws IOException {
        poolIds = reader.readIntArray(PARAMETER_POOL_IDS);
        schemas = reader.readUTFArray(PARAMETER_SCHEMAS);
    }

}
