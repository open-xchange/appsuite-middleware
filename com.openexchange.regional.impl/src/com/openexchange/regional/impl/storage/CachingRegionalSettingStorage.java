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
