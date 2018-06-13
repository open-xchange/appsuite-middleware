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
                    if (Context.class.isInstance(cached)) {
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
