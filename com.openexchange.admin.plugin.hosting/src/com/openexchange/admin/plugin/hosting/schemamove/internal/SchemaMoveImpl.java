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

package com.openexchange.admin.plugin.hosting.schemamove.internal;

import static com.openexchange.log.LogProperties.Name.DATABASE_POOL_ID;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.plugin.hosting.exceptions.TargetDatabaseException;
import com.openexchange.admin.plugin.hosting.schemamove.SchemaMoveService;
import com.openexchange.admin.plugin.hosting.services.AdminServiceRegistry;
import com.openexchange.admin.plugin.hosting.storage.interfaces.OXContextStorageInterface;
import com.openexchange.admin.plugin.hosting.storage.mysqlStorage.OXContextMySQLStorage;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.MissingServiceException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.mysqlStorage.OXUtilMySQLStorage;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link SchemaMoveImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SchemaMoveImpl implements SchemaMoveService {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaMoveImpl.class);

    private static final int DEFAULT_REASON = 1431655765;

    /**
     * Initializes a new {@link SchemaMoveImpl}.
     */
    public SchemaMoveImpl() {
        super();
    }

    @Override
    public void disableSchema(String schemaName) throws StorageException, NoSuchObjectException, TargetDatabaseException, MissingServiceException {
        /*
         * Precondition: a distinct write pool must be set for all contexts of the given schema
         */
        OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.isDistinctWritePoolIDForSchema(schemaName)) {
            throw new TargetDatabaseException(
                "Cannot proceed with schema move: Multiple write pool IDs are in use for schema " + schemaName);
        }

        /*
         * Disable all enabled contexts with configured maintenance reason
         */
        Integer reasonId = Integer.valueOf(ClientAdminThreadExtended.cache.getProperties().getProp("SCHEMA_MOVE_MAINTENANCE_REASON", Integer.toString(DEFAULT_REASON)));
        OXContextStorageInterface contextStorage = OXContextStorageInterface.getInstance();
        contextStorage.disable(schemaName, new MaintenanceReason(reasonId));
    }

    @Override
    public Map<String, String> getDbAccessInfoForSchema(String schemaName) throws StorageException, NoSuchObjectException {
        if (null == schemaName) {
            return null;
        }
        int writePoolId = OXToolStorageInterface.getInstance().getDatabaseIDByDatabaseSchema(schemaName);
        return fetchDbAccessInfo(writePoolId);
    }

    @Override
    public Map<String, String> getDbAccessInfoForCluster(int clusterId) throws StorageException, NoSuchObjectException {
        if (clusterId <= 0) {
            return null;
        }
        int writePoolId = OXUtilMySQLStorage.getInstance().getWritePoolIdForCluster(clusterId);
        return fetchDbAccessInfo(writePoolId);
    }

    /**
     * Fetch db access information
     *
     * @param writePoolId
     * @return
     * @throws StorageException
     */
    private Map<String, String> fetchDbAccessInfo(int writePoolId) throws StorageException {
        Database database = OXToolStorageInterface.getInstance().loadDatabaseById(writePoolId);

        final Map<String, String> props = new HashMap<String, String>(6);
        class SafePut {

            void put(String name, String value) {
                if (null != value) {
                    props.put(name, value);
                }
            }
        }
        SafePut safePut = new SafePut();

        safePut.put("url", database.getUrl());
        safePut.put("driver", database.getDriver());
        safePut.put("login", database.getLogin());
        safePut.put("name", database.getName());
        safePut.put("password", database.getPassword());

        return props;
    }

    @Override
    public void invalidateContexts(String schemaName, boolean invalidateSession) throws StorageException, MissingServiceException {
        /*
         * Obtain needed services
         */
        ContextService contextService = getContextService();
        DatabaseService dbService = getDatabaseService();
        CacheService cacheService = getCacheService();
        List<Integer> contextIds;
        try {
            /*
             * Determine all contexts associated with given schema
             */
            contextIds = OXContextStorageInterface.getInstance().getContextIdsBySchema(schemaName);
            if (contextIds == null || contextIds.isEmpty()) {
                return;
            }
            /*
             * Obtain pool identifier
             */
            int poolId;
            {
                String sPoolId = LogProperties.get(DATABASE_POOL_ID);
                if (Strings.isEmpty(sPoolId)) {
                    poolId = dbService.getSchemaInfo(contextIds.get(0).intValue()).getPoolId();
                } else {
                    try {
                        poolId = Integer.parseInt(sPoolId);
                    } catch (NumberFormatException e) {
                        poolId = dbService.getSchemaInfo(contextIds.get(0).intValue()).getPoolId();
                    }
                }
            }
            /*
             * Invalidate contexts
             */
            int[] contextIdArray = Autoboxing.I2i(contextIds);
            // Invalidate database assignments
            dbService.invalidate(contextIdArray);
            // Invalidate context stuff
            invalidateSchema(poolId, schemaName, cacheService);
            contextService.invalidateContexts(contextIdArray);
            LOG.info("Invalidated {} cached context objects for schema '{}'", Integer.valueOf(contextIdArray.length), schemaName);
        } catch (OXException e) {
            throw StorageException.wrapForRMI(e);
        } finally {
            LogProperties.remove(DATABASE_POOL_ID);
        }

        if (invalidateSession) {
            /*
             * Kill sessions for the disabled contexts globally
             */
            SessiondService sessiondService = getSessiondService();
            try {
                sessiondService.removeContextSessionsGlobal(new HashSet<Integer>(contextIds));
            } catch (OXException e) {
                throw StorageException.wrapForRMI(e);
            }
        }
    }

    private static final String CACHE_REGION = "OXDBPoolCache";

    private void invalidateSchema(int poolId, String schemaName, CacheService cacheService) throws OXException {
        Cache cache = cacheService.getCache(CACHE_REGION);
        if (null != cache) {
            CacheKey key = cache.newCacheKey(poolId, schemaName);
            try {
                cache.remove(key);
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
    }

    @Override
    public void enableSchema(String schemaName) throws StorageException, NoSuchObjectException, MissingServiceException {
        /*
         * Disable all enabled contexts with configured maintenance reason
         */
        Integer reasonId = Integer.valueOf(ClientAdminThreadExtended.cache.getProperties().getProp("SCHEMA_MOVE_MAINTENANCE_REASON", Integer.toString(DEFAULT_REASON)));
        OXContextStorageInterface contextStorage = OXContextStorageInterface.getInstance();
        contextStorage.enable(schemaName, new MaintenanceReason(reasonId));
    }

    private ContextService getContextService() throws MissingServiceException {
        try {
            return AdminServiceRegistry.getInstance().getService(ContextService.class, true);
        } catch (OXException e) {
            throw new MissingServiceException(e.getMessage());
        }
    }

    private SessiondService getSessiondService() throws MissingServiceException {
        try {
            return AdminServiceRegistry.getInstance().getService(SessiondService.class, true);
        } catch (OXException e) {
            throw new MissingServiceException(e.getMessage());
        }
    }

    private DatabaseService getDatabaseService() throws MissingServiceException {
        try {
            return AdminServiceRegistry.getInstance().getService(DatabaseService.class, true);
        } catch (OXException e) {
            throw new MissingServiceException(e.getMessage());
        }
    }

    private CacheService getCacheService() throws MissingServiceException {
        try {
            return AdminServiceRegistry.getInstance().getService(CacheService.class, true);
        } catch (OXException e) {
            throw new MissingServiceException(e.getMessage());
        }
    }

    @Override
    public void restorePoolReferences(String sourceSchema, String targetSchema, int targetClusterId) throws StorageException {
        OXContextStorageInterface contextStorage = OXContextStorageInterface.getInstance();
        contextStorage.updateContextReferences(sourceSchema, targetSchema, targetClusterId);
    }

    @Override
    public String createSchema(int targetClusterId) throws StorageException {
        OXContextStorageInterface contextStorage = OXContextMySQLStorage.getInstance();
        return contextStorage.createSchema(targetClusterId);
    }

}
