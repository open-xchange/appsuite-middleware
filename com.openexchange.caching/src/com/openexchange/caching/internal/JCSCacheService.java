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

package com.openexchange.caching.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.jcs.JCS;
import org.apache.jcs.engine.memory.MemoryCache;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.DefaultCacheKeyService;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;

/**
 * {@link JCSCacheService} - Cache service implementation through JCS cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JCSCacheService extends DefaultCacheKeyService implements CacheService {

    private static final JCSCacheService SINGLETON = new JCSCacheService();

    /**
     * Gets the singleton instance of JCS cache service
     *
     * @return The singleton instance of JCS cache service
     */
    public static JCSCacheService getInstance() {
        return SINGLETON;
    }

    /**
     * Holds references to already initialized caches
     */
    private final ConcurrentMap<String, MeteredCache> caches;

    /**
     * Initializes a new {@link JCSCacheService}
     */
    private JCSCacheService() {
        super();
        this.caches = new ConcurrentHashMap<>();
    }

    @Override
    public boolean isReplicated() {
        return true;
    }

    @Override
    public boolean isDistributed() {
        return false;
    }

    @Override
    public void freeCache(final String name) {
        if (JCSCacheServiceInit.getInstance().isDefaultCacheRegion(name)) {
            // No freeing of a default cache, this is done on bundle stop
            return;
        }
        JCSCacheServiceInit.getInstance().freeCache(name);
        MeteredCache meteredCache = this.caches.remove(name);
        if (meteredCache != null) {
            meteredCache.unregisterMeters();
        }
        /*-
         * try {
        	final Cache c = getCache(name);
        	if (null != c) {
        		c.dispose();
        	}
        } catch (CacheException e) {
        	LOG.error("", e);
        }
         */
    }

    @Override
    public Cache getCache(final String name) throws OXException {
        MeteredCache cache = caches.get(name);
        if (null == cache) {
            try {
                /*
                 * The JCS cache manager already tracks initialized caches though the same region name always points to the same cache
                 */
                JCSCache newJCSCache = new JCSCache(JCS.getInstance(name), name);
                cache = new MeteredCache(name, newJCSCache);
                /*
                 * Wrap with notifying cache if configured
                 */
                if (JCSCacheServiceInit.getInstance().isEventInvalidation()) {
                    CacheEventService eventService = JCSCacheServiceInit.getInstance().getCacheEventService();
                    if (null == eventService) {
                        throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(CacheEventService.class.getName());
                    }
                    cache.setNotifyingCache(new NotifyingCache(name, newJCSCache, eventService));
                }
            } catch (org.apache.jcs.access.exception.CacheException e) {
                throw CacheExceptionCode.CACHE_ERROR.create(e, e.getMessage());
            } catch (NullPointerException npe) {
                /*
                 * Can't use JCS without a configuration file or to be more precise a configuration file which lacks a region of the specified
                 * name. It should fail more gracefully, but that's a minor concern in the eyes of JCS developer.
                 */
                throw CacheExceptionCode.MISSING_CACHE_REGION.create(npe, name);
            }
            MeteredCache existingCache = caches.putIfAbsent(name, cache);
            if (null == existingCache) {
                cache.registerMeters();
            } else {
                cache = existingCache;
            }
        }
        return cache.getEffectiveCache();
    }

    @Override
    public void loadConfiguration(final String cacheConfigFile) throws OXException {
        JCSCacheServiceInit.getInstance().loadConfiguration(cacheConfigFile);
    }

    @Override
    public void loadConfiguration(final InputStream inputStream) throws OXException {
        JCSCacheServiceInit.getInstance().loadConfiguration(inputStream, false);
    }

    @Override
    public void loadConfiguration(final InputStream inputStream, final boolean overwrite) throws OXException {
        JCSCacheServiceInit.getInstance().loadConfiguration(inputStream, overwrite);
    }

    @Override
    public void loadConfiguration(final Properties properties) throws OXException {
        JCSCacheServiceInit.getInstance().loadConfiguration(properties);
    }

    @Override
    public void loadDefaultConfiguration() throws OXException {
        JCSCacheServiceInit.getInstance().loadDefaultConfiguration();
    }

    /**
     * Wrapper that holds the actual cache instance and takes care to manage
     * the caches Micrometer monitoring metrics.
     */
    private static final class MeteredCache {

        private final String region;
        private final JCSCache jcsCache;
        private final List<Meter> meters;
        private NotifyingCache notifyingCache;

        MeteredCache(String region, JCSCache cache) {
            super();
            this.region = region;
            this.jcsCache = cache;
            this.meters = new ArrayList<>(7);
        }

        /**
         * Gets the effective cache instance, either {@link JCSCache} or - if set - the {@link NotifyingCache}.
         */
        Cache getEffectiveCache() {
            if (notifyingCache == null) {
                return jcsCache;
            }
            return notifyingCache;
        }

        /**
         * Sets the optional {@link NotifyingCache} instance
         *
         * @param notifyingCache
         */
        void setNotifyingCache(NotifyingCache notifyingCache) {
            this.notifyingCache = notifyingCache;
        }

        /**
         * Registers all cache-specific monitoring metrics
         */
        void registerMeters() {
            MemoryCache memCache = jcsCache.getMemCache();
            if (memCache == null) {
                return;
            }

            meters.add(Gauge.builder("appsuite.jcs.cache.elements.max", memCache, (c) -> ((double) c.getCacheAttributes().getMaxObjects()))
                .description("Max. number of cached elements")
                .tag("region", region)
                .register(Metrics.globalRegistry));

            meters.add(Gauge.builder("appsuite.jcs.cache.elements.total", memCache, (c) -> ((double) c.getSize()))
                .description("Current number of cached elements")
                .tag("region", region)
                .register(Metrics.globalRegistry));

            meters.add(FunctionCounter.builder("appsuite.jcs.cache.puts.total", memCache, (c) -> ((double) c.getCompositeCache().getUpdateCount()))
                .description("Number of cache put operations")
                .tag("region", region)
                .register(Metrics.globalRegistry));

            meters.add(FunctionCounter.builder("appsuite.jcs.cache.removals.total", memCache, (c) -> ((double) c.getCompositeCache().getRemoveCount()))
                .description("Number of remove from cache operations")
                .tag("region", region)
                .register(Metrics.globalRegistry));

            meters.add(FunctionCounter.builder("appsuite.jcs.cache.hits.total", memCache, (c) -> ((double) c.getCompositeCache().getHitCountRam()))
                .description("Number of cache hits")
                .tag("region", region)
                .register(Metrics.globalRegistry));

            meters.add(FunctionCounter.builder("appsuite.jcs.cache.misses.notfound.total", memCache, (c) -> ((double) c.getCompositeCache().getMissCountNotFound()))
                .description("Number of cache misses (element not in cache)")
                .tag("region", region)
                .register(Metrics.globalRegistry));

            meters.add(FunctionCounter.builder("appsuite.jcs.cache.misses.expired.total", memCache, (c) -> ((double) c.getCompositeCache().getMissCountExpired()))
                .description("Number of cache misses (element in cache but expired)")
                .tag("region", region)
                .register(Metrics.globalRegistry));

            meters.add(FunctionCounter.builder("appsuite.jcs.cache.misses.total", memCache, (c) ->
                    ((double) (c.getCompositeCache().getMissCountNotFound() + c.getCompositeCache().getMissCountExpired())))
                .description("Number of cache misses (not in cache + in cache but expired)")
                .tag("region", region)
                .register(Metrics.globalRegistry));
        }

        /**
         * Unregisters all monitoring metrics for the contained cache
         */
        void unregisterMeters() {
            Iterator<Meter> it = meters.iterator();
            while (it.hasNext()) {
                Meter meter = it.next();
                Metrics.globalRegistry.remove(meter);
                it.remove();
            }
        }

    }

}
