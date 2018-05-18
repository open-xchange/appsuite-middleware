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

package com.openexchange.tools.oxfolder.property.impl;

import java.sql.Connection;
import java.util.Collections;
import java.util.LinkedHashMap;
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
    public void deleteFolderProperties(int contextId, int folderId, int userId, Set<String> propertyKeys) throws OXException {
        deleteFolderProperties(contextId, folderId, userId, propertyKeys, null);
    }

    @Override
    public void deleteFolderProperties(int contextId, int folderId, int userId, Set<String> propertyKeys, Connection connection) throws OXException {
        delegate.deleteFolderProperties(contextId, folderId, userId, propertyKeys, connection);

        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            Cache cache = cacheService.getCache(REGION_NAME);
            CacheKey key = newCacheKey(cacheService, folderId, userId, contextId);
            Object object = cache.get(key);
            if (object instanceof Map) {
                if (null == propertyKeys || propertyKeys.isEmpty()) {
                    cache.put(key, EMPTY_IMMUTABLE_MAP, true);
                } else {
                    Map<String, String> newProperties = new LinkedHashMap<>((Map<String, String>) object);
                    for (String propertyKey : propertyKeys) {
                        newProperties.remove(propertyKey);
                    }
                    cache.put(key, newProperties.isEmpty() ? EMPTY_IMMUTABLE_MAP : ImmutableMap.copyOf(newProperties), true);
                }
            }
        }
    }

    @Override
    public void deleteFolderProperty(int contextId, int folderId, int userId, String key) throws OXException {
        deleteFolderProperty(contextId, folderId, userId, key, null);
    }

    @Override
    public void deleteFolderProperty(int contextId, int folderId, int userId, String propertyKey, Connection connection) throws OXException {
        delegate.deleteFolderProperty(contextId, folderId, userId, propertyKey, connection);

        if (null != propertyKey) {
            CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
            if (null != cacheService) {
                Cache cache = cacheService.getCache(REGION_NAME);
                CacheKey key = newCacheKey(cacheService, folderId, userId, contextId);
                Object object = cache.get(key);
                if (object instanceof Map) {
                    Map<String, String> newProperties = new LinkedHashMap<>((Map<String, String>) object);
                    newProperties.remove(propertyKey);
                    cache.put(key, newProperties.isEmpty() ? EMPTY_IMMUTABLE_MAP : ImmutableMap.copyOf(newProperties), true);
                }
            }
        }
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

}
