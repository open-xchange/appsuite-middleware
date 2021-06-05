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

package com.openexchange.regional.impl.storage;

import java.sql.Connection;
import java.util.concurrent.locks.Lock;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.lock.LockService;
import com.openexchange.regional.RegionalSettings;
import com.openexchange.regional.impl.service.RegionalSettingsImpl;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CachingRegionalSettingStorage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class CachingRegionalSettingStorage implements RegionalSettingStorage {

    private static final RegionalSettings EMPTY_SETTINGS = RegionalSettingsImpl.newBuilder().build();
    private static final String REGION_NAME = "RegionalSettings";

    private final SQLRegionalSettingStorage storage;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link CachingRegionalSettingStorage}.
     *
     * @param cacheService
     * @param storage
     */
    public CachingRegionalSettingStorage(ServiceLookup services, SQLRegionalSettingStorage storage) {
        super();
        this.services = services;
        this.storage = storage;
    }

    @Override
    public RegionalSettings get(int contextId, int userId) throws OXException {
        Cache cache = getCacheService().getCache(REGION_NAME);
        CacheKey key = cache.newCacheKey(contextId, userId);
        Object object = cache.get(key);
        if (object instanceof RegionalSettings) {
            RegionalSettings cachedSettings = (RegionalSettings) object;
            return EMPTY_SETTINGS.equals(cachedSettings) ? null : cachedSettings;
        }

        LockService lockService = optLockService();
        Lock lock = null == lockService ? LockService.EMPTY_LOCK : lockService.getSelfCleaningLockFor(new StringBuilder(32).append("loadRegionSettings-").append(contextId).append('-').append(userId).toString());
        lock.lock();
        try {
            object = cache.get(key);
            if (object instanceof RegionalSettings) {
                RegionalSettings cachedSettings = (RegionalSettings) object;
                return EMPTY_SETTINGS.equals(cachedSettings) ? null : cachedSettings;
            }

            RegionalSettings loadedSettings = storage.get(contextId, userId);
            cache.put(key, null == loadedSettings ? EMPTY_SETTINGS : loadedSettings, false);
            return loadedSettings;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void upsert(int contextId, int userId, RegionalSettings settings) throws OXException {
        storage.upsert(contextId, userId, settings);
        Cache cache = getCacheService().getCache(REGION_NAME);
        cache.put(cache.newCacheKey(contextId, userId), settings, true);
    }

    @Override
    public void delete(int contextId, int userId) throws OXException {
        delete(contextId, userId, null);
    }

    @Override
    public void delete(int contextId, int userId, Connection writeCon) throws OXException {
        storage.delete(contextId, userId, writeCon);
        Cache cache = getCacheService().getCache(REGION_NAME);
        cache.remove(cache.newCacheKey(contextId, userId));
    }

    @Override
    public void delete(int contextId) throws OXException {
        delete(contextId, null);
    }

    @Override
    public void delete(int contextId, Connection writeCon) throws OXException {
        storage.delete(contextId, writeCon);
    }

    ////////////////////////////////////////// HELPERS //////////////////////////////////////////////

    /**
     * Returns the {@link CacheService}
     * 
     * @return the {@link CacheService}
     * @throws OXException if the service is absent.
     */
    private CacheService getCacheService() throws OXException {
        return services.getServiceSafe(CacheService.class);
    }

    /**
     * Optionally gets the {@link LockService} or <code>null</code>
     * if the service is absent.
     * 
     * @return the {@link LockService} or <code>null</code> if the service is absent.
     */
    private LockService optLockService() {
        return services.getOptionalService(LockService.class);
    }
}
