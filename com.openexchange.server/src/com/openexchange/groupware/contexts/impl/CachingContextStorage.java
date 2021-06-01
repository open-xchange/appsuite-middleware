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

package com.openexchange.groupware.contexts.impl;

import static com.openexchange.java.Autoboxing.I;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.UpdateBehavior;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.groupware.update.internal.SchemaExceptionCodes;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * This class implements a caching for the context storage. It provides a proxy implementation for the Context interface to the outside
 * world to be able to keep the referenced context data up-to-date.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class CachingContextStorage extends ContextStorage {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CachingContextStorage.class);

    private static final String REGION_NAME = "Context";

    public static volatile CachingContextStorage parent;

    private final RdbContextStorage persistantImpl;

    private boolean started;

    public CachingContextStorage(final RdbContextStorage persistantImpl) {
        super();
        this.persistantImpl = persistantImpl;
    }

    @Override
    public int getContextId(final String loginInfo) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            return persistantImpl.getContextId(loginInfo);
        }
        final Cache cache = cacheService.getCache(REGION_NAME);
        Integer contextId = (Integer) cache.get(loginInfo);
        if (null == contextId) {
            LOG.trace("Cache MISS. Login info: {}", loginInfo);
            ContextExtended context = persistantImpl.getContext(loginInfo);
            if (null == context) {
                contextId = I(NOT_FOUND);
            } else {
                context = triggerUpdate(context, UpdateBehavior.NONE);
                contextId = I(context.getContextId());
                try {
                    cache.put(loginInfo, contextId, false);
                    cache.put(contextId, context, false);
                } catch (OXException e) {
                    LOG.error("", e);
                }
            }
        } else {
            LOG.trace("Cache HIT. Login info: {}", loginInfo);
        }
        return contextId.intValue();
    }

    @Override
    public ContextExtended loadContext(int contextId, UpdateBehavior updateBehavior) throws OXException {
        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return load(contextId, updateBehavior);
        }

        Cache cache = cacheService.getCache(REGION_NAME);
        Integer key = I(contextId);
        Object object = cache.get(key);
        if (object instanceof ContextExtended) {
            return (ContextExtended) object;
        }

        // Load it
        ContextExtended contextExtended = load(contextId, updateBehavior);
        cache.put(key, contextExtended, false);
        return contextExtended;
    }

    @Override
    public boolean exists(int contextId) throws OXException {
        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return persistantImpl.exists(contextId);
        }

        Cache cache = cacheService.getCache(REGION_NAME);
        Integer key = I(contextId);
        Object object = cache.get(key);
        if (object instanceof ContextExtended) {
            return true;
        }
        return persistantImpl.exists(contextId);
    }

    @Override
    public List<Integer> getAllContextIds() throws OXException {
        return persistantImpl.getAllContextIds();
    }

    @Override
    public List<Integer> getDistinctContextsPerSchema() throws OXException {
        return persistantImpl.getDistinctContextsPerSchema();
    }

    @Override
    public Map<PoolAndSchema, List<Integer>> getSchemaAssociations() throws OXException {
        return persistantImpl.getSchemaAssociations();
    }

    @Override
    public Map<PoolAndSchema, List<Integer>> getSchemaAssociationsFor(List<Integer> contextIds) throws OXException {
        return persistantImpl.getSchemaAssociationsFor(contextIds);
    }

    @Override
    public List<Integer> getAllContextIdsForFilestore(int filestoreId) throws OXException {
        return persistantImpl.getAllContextIdsForFilestore(filestoreId);
    }

    @Override
    protected void startUp() throws OXException {
        if (started) {
            LOG.error("Duplicate initialization of CachingContextStorage.");
            return;
        }
        persistantImpl.startUp();
        started = true;
    }

    @Override
    public void setAttribute(String name, String value, int contextId) throws OXException {
        persistantImpl.setAttribute(name, value, contextId);
        invalidateContexts(new int[] { contextId });
    }

    @Override
    protected void shutDown() throws OXException {
        if (!started) {
            LOG.error("Duplicate shutdown of CachingContextStorage.");
            return;
        }
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService != null) {
            try {
                cacheService.freeCache(REGION_NAME);
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
        persistantImpl.shutDown();
        started = false;
    }

    @Override
    public void invalidateContext(final int contextId) throws OXException {
        invalidateContexts(new int[] { contextId });
    }

    @Override
    public void invalidateContexts(int[] contextIDs) throws OXException {
        if (contextIDs == null || contextIDs.length == 0) {
            return;
        }

        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            // Cache not initialized.
            return;
        }

        // Gather cache keys to invalidate
        Cache cache = cacheService.getCache(REGION_NAME);
        List<Serializable> keys = new LinkedList<Serializable>();
        for (int contextID : contextIDs) {
            Integer key = Integer.valueOf(contextID);
            keys.add(key);
            Object cached = cache.get(key);
            if (Context.class.isInstance(cached)) {
                String[] loginInfos = ((Context) cached).getLoginInfo();
                if (null != loginInfos && 0 < loginInfos.length) {
                    for (String loginInfo : loginInfos) {
                        keys.add(loginInfo);
                    }
                }
            }
        }
        /*
         * invalidate cache
         */
        cache.remove(keys);
    }

    @Override
    public void invalidateLoginInfo(final String loginContextInfo) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            // Cache not initialized.
            return;
        }
        final Cache cache = cacheService.getCache(REGION_NAME);
        cache.remove(loginContextInfo);
    }

    ContextStorage getPersistantImpl() {
        return persistantImpl;
    }

    private ContextExtended load(int contextId, UpdateBehavior updateBehavior) throws OXException {
        final ContextExtended retval = persistantImpl.loadContext(contextId);
        return triggerUpdate(retval, updateBehavior);
    }

    private ContextExtended triggerUpdate(ContextExtended context, UpdateBehavior updateBehavior) {
        // TODO We should introduce a logic layer above this context storage
        // layer. That layer should then trigger the update tasks.
        // Nearly all accesses to the ContextStorage need then to be replaced
        // with an access to the ContextService.
        try {
            Updater updater = Updater.getInstance();
            UpdateStatus status = updater.getStatus(context.getContextId());
            context.setUpdating(status.blockingUpdatesRunning());
            if ((status.needsBlockingUpdates() || status.needsBackgroundUpdates()) && !status.blockingUpdatesRunning() && !status.backgroundUpdatesRunning()) {
                if (denyImplicitUpdateOnContextLoad(updateBehavior)) {
                    context.setUpdateNeeded(true);
                } else {
                    if (status.needsBlockingUpdates()) {
                        context.setUpdating(true);
                    }
                    updater.startUpdate(context);
                }
            }
        } catch (OXException e) {
            if (SchemaExceptionCodes.DATABASE_DOWN.equals(e)) {
                LOG.warn("Switching to read only mode for context {} because master database is down.", I(context.getContextId()), e);
                context.setReadOnly(true);
            }
        }
        return context;
    }

    private static boolean denyImplicitUpdateOnContextLoad(UpdateBehavior updateBehavior) {
        UpdateBehavior behavior = updateBehavior;
        if (behavior == null) {
            behavior = UpdateBehavior.NONE;
        }

        switch (behavior) {
            case DENY_UPDATE:
                return true;
            case TRIGGER_UPDATE:
                return false;
            case NONE:
                //$FALL-THROUGH$
            default:
                return getDenyImplicitUpdateOnContextLoadProperty();
        }
    }

    private static boolean getDenyImplicitUpdateOnContextLoadProperty() {
        boolean defaultValue = false;

        ConfigurationService configService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null == configService) {
            return defaultValue;
        }

        return configService.getBoolProperty("com.openexchange.groupware.update.denyImplicitUpdateOnContextLoad", defaultValue);
    }

}
