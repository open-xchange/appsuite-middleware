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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.caching.internal;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.jcs.JCS;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.DefaultCacheKeyService;
import com.openexchange.caching.events.CacheEventService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;

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
    private final ConcurrentMap<String, Cache> caches;

    /**
     * Initializes a new {@link JCSCacheService}
     */
    private JCSCacheService() {
        super();
        this.caches = new ConcurrentHashMap<String, Cache>();
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
        this.caches.remove(name);
        /*-
         * try {
        	final Cache c = getCache(name);
        	if (null != c) {
        		c.dispose();
        	}
        } catch (final CacheException e) {
        	LOG.error("", e);
        }
         */
    }

    @Override
    public Cache getCache(final String name) throws OXException {
        Cache cache = caches.get(name);
        if (null == cache) {
            try {
                /*
                 * The JCS cache manager already tracks initialized caches though the same region name always points to the same cache
                 */
                cache = new JCSCache(JCS.getInstance(name), name);
                /*
                 * Wrap with notifying cache if configured
                 */
                if (JCSCacheServiceInit.getInstance().isEventInvalidation()) {
                    CacheEventService eventService = JCSCacheServiceInit.getInstance().getCacheEventService();
                    if (null == eventService) {
                        throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(CacheEventService.class.getName());
                    }
                    cache = new NotifyingCache(name, cache, eventService);
                }
            } catch (final org.apache.jcs.access.exception.CacheException e) {
                throw CacheExceptionCode.CACHE_ERROR.create(e, e.getMessage());
            } catch (final NullPointerException npe) {
                /*
                 * Can't use JCS without a configuration file or to be more precise a configuration file which lacks a region of the specified
                 * name. It should fail more gracefully, but that's a minor concern in the eyes of JCS developer.
                 */
                throw CacheExceptionCode.MISSING_CACHE_REGION.create(npe, name);
            }
            Cache existingCache = caches.putIfAbsent(name, cache);
            if (null != existingCache) {
                cache = existingCache;
            }
        }
        return cache;
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

}
