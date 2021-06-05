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

package com.openexchange.tools.oxfolder.property.impl;

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.property.FolderUserPropertyStorage;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;


/**
 * {@link CachingFolderUserPropertyStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class CachingFolderUserPropertyStorage implements FolderUserPropertyStorage {

    private static final ImmutableMap<Object, Object> EMPTY_IMMUTABLE_MAP = ImmutableMap.of();

    private static final String REGION_NAME = "FolderUserProperty";

    /**
     * Gets the name of the cache region.
     *
     * @return The region name
     */
    public static String getRegionName() {
        return REGION_NAME;
    }

    static CacheKey newCacheKey(CacheService cacheService, int folderId, int userId, int contextId) {
        return cacheService.newCacheKey(contextId, Integer.toString(userId), Integer.toString(folderId));
    }

    /**
     * Proxy attribute for the object implementing the persistent methods.
     */
    private final RdbFolderUserPropertyStorage delegate;

    /**
     * Initializes a new {@link CachingFolderUserPropertyStorage}.
     *
     * @param delegate The database-backed delegate storage
     */
    public CachingFolderUserPropertyStorage(RdbFolderUserPropertyStorage delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public void deleteFolderProperties(int contextId, int folderId, int[] userIds, Set<String> propertyKeys) throws OXException {
        delegate.deleteFolderProperties(contextId, folderId, userIds, propertyKeys);
        invalidateCache(contextId, folderId, userIds);
    }

    @Override
    public void deleteFolderProperties(int contextId, int folderId, int[] userIds, Set<String> propertyKeys, Connection connection) throws OXException {
        delegate.deleteFolderProperties(contextId, folderId, userIds, propertyKeys, connection);
        invalidateCache(contextId, folderId, userIds);
    }

    @Override
    public boolean exists(int contextId, int folderId, int userId) throws OXException {
        return exists(contextId, folderId, userId, null);
    }

    @Override
    public boolean exists(int contextId, int folderId, int userId, Connection connection) throws OXException {
        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            return delegate.exists(contextId, folderId, userId, connection);
        }

        Cache cache = cacheService.getCache(REGION_NAME);
        CacheKey key = newCacheKey(cacheService, folderId, userId, contextId);
        Object object = cache.get(key);
        if (object instanceof Map) {
            return false == ((Map<String, String>) object).isEmpty();
        }
        return delegate.exists(contextId, folderId, userId, connection);
    }

    @Override
    public Map<String, String> getFolderProperties(int contextId, int folderId, int userId) throws OXException {
        return getFolderProperties(contextId, folderId, userId, null);
    }

    @Override
    public Map<String, String> getFolderProperties(int contextId, int folderId, int userId, Connection connection) throws OXException {
        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            return delegate.getFolderProperties(contextId, folderId, userId, connection);
        }

        Cache cache = cacheService.getCache(REGION_NAME);
        CacheKey key = newCacheKey(cacheService, folderId, userId, contextId);
        Object object = cache.get(key);
        if (object instanceof Map) {
            return ((Map<String, String>) object);
        }

        Map<String, String> properties = delegate.getFolderProperties(contextId, folderId, userId, connection);
        cache.put(key, properties.isEmpty() ? EMPTY_IMMUTABLE_MAP : ImmutableMap.copyOf(properties), false);
        return properties;
    }

    @Override
    public Map<Integer, Map<String, String>> getFolderProperties(int contextId, int[] folderIds, int userId) throws OXException {
        return getFolderProperties(contextId, folderIds, userId, null);
    }

    @Override
    public Map<Integer, Map<String, String>> getFolderProperties(int contextId, int[] folderIds, int userId, Connection connection) throws OXException {
        if (null == folderIds) {
            return Collections.emptyMap();
        }

        int length = folderIds.length;
        if (length == 0) {
            return Collections.emptyMap();
        }

        if (1 == length) {
            int folderId = folderIds[0];
            return Collections.singletonMap(Integer.valueOf(folderId), getFolderProperties(contextId, folderId, userId, connection));
        }

        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            return delegate.getFolderProperties(contextId, folderIds, userId, connection);
        }

        Cache cache = cacheService.getCache(REGION_NAME);
        Map<Integer, Map<String, String>> retval = new LinkedHashMap<>(length);
        TIntList toLoad = new TIntArrayList(length);
        for (int folderId : folderIds) {
            CacheKey key = newCacheKey(cacheService, folderId, userId, contextId);
            Object object = cache.get(key);
            if (object instanceof Map) {
                retval.put(Integer.valueOf(folderId), ((Map<String, String>) object));
            } else {
                toLoad.add(folderId);
            }
        }

        if (!toLoad.isEmpty()) {
            TIntIterator iterator = toLoad.iterator();
            for (int i = toLoad.size(); i-- > 0;) {
                int folderId = iterator.next();
                Map<String, String> properties = delegate.getFolderProperties(contextId, folderId, userId, connection);
                CacheKey key = newCacheKey(cacheService, folderId, userId, contextId);
                cache.put(key, properties.isEmpty() ? EMPTY_IMMUTABLE_MAP : ImmutableMap.copyOf(properties), false);
                retval.put(Integer.valueOf(folderId), properties);
            }
        }

        return retval;
    }

    @Override
    public String getFolderProperty(int contextId, int folderId, int userId, String key) throws OXException {
        return getFolderProperty(contextId, folderId, userId, key, null);
    }

    @Override
    public String getFolderProperty(int contextId, int folderId, int userId, String propertyKey, Connection connection) throws OXException {
        if (null == propertyKey) {
            return null;
        }

        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            return delegate.getFolderProperty(contextId, folderId, userId, propertyKey, connection);
        }

        Cache cache = cacheService.getCache(REGION_NAME);
        CacheKey key = newCacheKey(cacheService, folderId, userId, contextId);
        Object object = cache.get(key);
        if (object instanceof Map) {
            return ((Map<String, String>) object).get(propertyKey);
        }

        Map<String, String> properties = delegate.getFolderProperties(contextId, folderId, userId, connection);
        cache.put(key, properties.isEmpty() ? EMPTY_IMMUTABLE_MAP : ImmutableMap.copyOf(properties), false);
        return properties.get(propertyKey);
    }

    @Override
    public void insertFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties) throws OXException {
        insertFolderProperties(contextId, folderId, userId, properties, null);
    }

    @Override
    public void insertFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties, Connection connection) throws OXException {
        if (null == properties || properties.isEmpty()) {
            // Nothing to insert
            return;
        }

        delegate.insertFolderProperties(contextId, folderId, userId, properties, connection);

        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            Cache cache = cacheService.getCache(REGION_NAME);
            CacheKey key = newCacheKey(cacheService, folderId, userId, contextId);
            Object object = cache.get(key);
            if (object instanceof Map) {
                Map<String, String> newProperties = new LinkedHashMap<>((Map<String, String>) object);
                boolean inserted = false;
                for (Map.Entry<String, String> property : properties.entrySet()) {
                    String newValue = property.getValue();
                    String previousValue = newProperties.put(property.getKey(), newValue);
                    inserted |= (false == newValue.equals(previousValue));
                }
                if (inserted) {
                    cache.put(key, newProperties.isEmpty() ? EMPTY_IMMUTABLE_MAP : ImmutableMap.copyOf(newProperties), true);
                }
            }
        }
    }

    @Override
    public void insertFolderProperty(int contextId, int folderId, int userId, String key, String value) throws OXException {
        insertFolderProperty(contextId, folderId, userId, key, value, null);
    }

    @Override
    public void insertFolderProperty(int contextId, int folderId, int userId, String propertyKey, String newValue, Connection connection) throws OXException {
        if (null == propertyKey || null == newValue) {
            return;
        }

        delegate.insertFolderProperty(contextId, folderId, userId, propertyKey, newValue, connection);

        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            Cache cache = cacheService.getCache(REGION_NAME);
            CacheKey key = newCacheKey(cacheService, folderId, userId, contextId);
            Object object = cache.get(key);
            if (object instanceof Map) {
                Map<String, String> newProperties = new LinkedHashMap<>((Map<String, String>) object);
                String previousValue = newProperties.put(propertyKey, newValue);
                boolean inserted = (false == newValue.equals(previousValue));
                if (inserted) {
                    cache.put(key, newProperties.isEmpty() ? EMPTY_IMMUTABLE_MAP : ImmutableMap.copyOf(newProperties), true);
                }
            }
        }
    }

    @Override
    public void setFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties) throws OXException {
        setFolderProperties(contextId, folderId, userId, properties, null);
    }

    @Override
    public void setFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties, Connection connection) throws OXException {
        if (null == properties || properties.isEmpty()) {
            // Nothing to set
            return;
        }

        delegate.setFolderProperties(contextId, folderId, userId, properties, connection);

        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            Cache cache = cacheService.getCache(REGION_NAME);
            CacheKey key = newCacheKey(cacheService, folderId, userId, contextId);
            Object object = cache.get(key);
            if (object instanceof Map) {
                Map<String, String> newProperties = new LinkedHashMap<>((Map<String, String>) object);
                boolean inserted = false;
                for (Map.Entry<String, String> property : properties.entrySet()) {
                    String newValue = property.getValue();
                    String previousValue = newProperties.put(property.getKey(), newValue);
                    inserted |= (false == newValue.equals(previousValue));
                }
                if (inserted) {
                    cache.put(key, newProperties.isEmpty() ? EMPTY_IMMUTABLE_MAP : ImmutableMap.copyOf(newProperties), true);
                }
            }
        }
    }

    @Override
    public void updateFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties) throws OXException {
        updateFolderProperties(contextId, folderId, userId, properties, null);
    }

    @Override
    public void updateFolderProperties(int contextId, int folderId, int userId, Map<String, String> properties, Connection connection) throws OXException {
        if (null == properties || properties.isEmpty()) {
            // Nothing to update
            return;
        }

        delegate.updateFolderProperties(contextId, folderId, userId, properties, connection);

        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            Cache cache = cacheService.getCache(REGION_NAME);
            CacheKey key = newCacheKey(cacheService, folderId, userId, contextId);
            Object object = cache.get(key);
            if (object instanceof Map) {
                Map<String, String> newProperties = new LinkedHashMap<>((Map<String, String>) object);
                boolean inserted = false;
                for (Map.Entry<String, String> property : properties.entrySet()) {
                    if (false == newProperties.containsKey(property.getKey())) {
                        String newValue = property.getValue();
                        String previousValue = newProperties.put(property.getKey(), newValue);
                        inserted |= (false == newValue.equals(previousValue));
                    }
                }
                if (inserted) {
                    cache.put(key, newProperties.isEmpty() ? EMPTY_IMMUTABLE_MAP : ImmutableMap.copyOf(newProperties), true);
                }
            }
        }
    }

    @Override
    public void updateFolderProperty(int contextId, int folderId, int userId, String key, String value) throws OXException {
        updateFolderProperty(contextId, folderId, userId, key, value, null);
    }

    @Override
    public void updateFolderProperty(int contextId, int folderId, int userId, String propertyKey, String newValue, Connection connection) throws OXException {
        if (null == propertyKey) {
            return;
        }

        if (null == newValue) {
            deleteFolderProperty(contextId, folderId, userId, propertyKey, connection);
            return;
        }

        delegate.updateFolderProperty(contextId, folderId, userId, propertyKey, newValue, connection);

        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            Cache cache = cacheService.getCache(REGION_NAME);
            CacheKey key = newCacheKey(cacheService, folderId, userId, contextId);
            Object object = cache.get(key);
            if (object instanceof Map) {
                Map<String, String> newProperties = new LinkedHashMap<>((Map<String, String>) object);
                if (newProperties.containsKey(propertyKey)) {
                    String previousValue = newProperties.put(propertyKey, newValue);
                    boolean inserted = (false == newValue.equals(previousValue));
                    if (inserted) {
                        cache.put(key, newProperties.isEmpty() ? EMPTY_IMMUTABLE_MAP : ImmutableMap.copyOf(newProperties), true);
                    }
                }
            }
        }
    }

    private static void invalidateCache(int contextId, int folderId, int[] userIds) {
        if (null == userIds || 0 == userIds.length) {
            return;
        }
        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            List<Serializable> keys = new ArrayList<Serializable>(userIds.length);
            for (int userId : userIds) {
                keys.add(newCacheKey(cacheService, folderId, userId, contextId));
            }
            try {
                cacheService.getCache(REGION_NAME).remove(keys);
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(CachingFolderUserPropertyStorage.class).warn("Unexpected error invalidating cache", e);
            }
        }
    }

}
