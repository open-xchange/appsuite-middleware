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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.calendar.cache;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarVolatileCache} - The volatile calendar cache.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CalendarVolatileCache {

    public static final String REGION = "CalendarVolatileCache";

    private static final AtomicReference<CalendarVolatileCache> INSTANCE_REF = new AtomicReference<CalendarVolatileCache>();

    /**
     * Initializes cache instance.
     * 
     * @param context The bundle context
     */
    public static void initInstance(final BundleContext context) {
        final AtomicReference<CalendarVolatileCache> ref = INSTANCE_REF;
        CalendarVolatileCache tmp;
        do {
            tmp = ref.get();
            if (null != tmp) {
                return;
            }
        } while (!ref.compareAndSet(null, new CalendarVolatileCache()));
        tmp = ref.get();
        tmp.startUp(context);
    }

    /**
     * Drops the instance.
     */
    public static void dropInstance() {
        final AtomicReference<CalendarVolatileCache> ref = INSTANCE_REF;
        CalendarVolatileCache tmp;
        do {
            tmp = ref.get();
            if (null == tmp) {
                return;
            }
        } while (!ref.compareAndSet(tmp, null));
        tmp.shutDown();
    }

    /**
     * Gets the instance.
     * 
     * @return The instance
     */
    public static CalendarVolatileCache getInstance() {
        return INSTANCE_REF.get();
    }

    /*-
     * --------------------------------------------------------------------------------------------
     */

    protected volatile Cache cache;

    private volatile ServiceTracker<CacheService, CacheService> tracker;

    /**
     * Initializes a new {@link CalendarVolatileCache}.
     */
    private CalendarVolatileCache() {
        super();
    }

    private void startUp(final BundleContext context) {
        final CalendarVolatileCache instance = this;
        tracker =
            new ServiceTracker<CacheService, CacheService>(
                context,
                CacheService.class,
                new ServiceTrackerCustomizer<CacheService, CacheService>() {

                    @Override
                    public CacheService addingService(final ServiceReference<CacheService> reference) {
                        final CacheService cacheService = context.getService(reference);
                        try {
                            instance.cache = cacheService.getCache(REGION);
                            return cacheService;
                        } catch (final OXException e) {
                            context.ungetService(reference);
                            return null;
                        }
                    }

                    @Override
                    public void modifiedService(final ServiceReference<CacheService> reference, final CacheService service) {
                        // Nope
                    }

                    @Override
                    public void removedService(final ServiceReference<CacheService> reference, final CacheService service) {
                        instance.shutDownCache();
                        context.ungetService(reference);
                    }
                });
        tracker.open();
    }

    protected void shutDownCache() {
        final Cache cache = this.cache;
        if (null != cache) {
            try {
                cache.clear();
            } catch (final Exception e) {
                // Ignore
            } finally {
                cache.dispose();
            }
            this.cache = null;
        }
    }

    private void shutDown() {
        shutDownCache();
        final ServiceTracker<CacheService, CacheService> tracker = this.tracker;
        if (null != tracker) {
            tracker.close();
            this.tracker = null;
        }
    }

    /**
     * Clears this cache.
     * 
     * @throws OXException
     */
    public void clear() throws OXException {
        final Cache cache = this.cache;
        if (null != cache) {
            cache.clear();
        }
    }

    /**
     * Gets the value for specified key.
     * 
     * @param key The key
     * @return The value or <code>null</code> if absent
     */
    public Object get(final Serializable key) {
        final Cache cache = this.cache;
        return null == cache ? null : cache.get(key);
    }

    /**
     * Gets the value for specified key in given group.
     * 
     * @param key The key
     * @param group The group identifier
     * @return The value from group or <code>null</code> if absent
     */
    public Object getFromGroup(final Serializable key, final String group) {
        final Cache cache = this.cache;
        return null == cache ? null : cache.getFromGroup(key, group);
    }

    /**
     * Invalidates denoted group.
     * 
     * @param group The group identifier
     */
    public void invalidateGroup(final String group) {
        final Cache cache = this.cache;
        if (null != cache) {
            cache.invalidateGroup(group);
        }
    }

    /**
     * Puts specified key-value-pair.
     * 
     * @param key The key
     * @param obj The value
     * @throws OXException If put into cache fails
     */
    public void put(final Serializable key, final Serializable obj) throws OXException {
        final Cache cache = this.cache;
        if (null != cache) {
            cache.put(key, obj);
        }
    }

    /**
     * Puts specified key-value-pair into give group.
     * 
     * @param key The key
     * @param group The group identifier
     * @param value The value
     * @throws OXException If put into cache fails
     */
    public void putInGroup(final Serializable key, final String group, final Serializable value) throws OXException {
        final Cache cache = this.cache;
        if (null != cache) {
            cache.putInGroup(key, group, value);
        }
    }

    /**
     * Removes the value associated with given key.
     * 
     * @param key The key
     * @throws OXException If removal fails
     */
    public void remove(final Serializable key) throws OXException {
        final Cache cache = this.cache;
        if (null != cache) {
            cache.remove(key);
        }
    }

    /**
     * Removes the value associated with given key from group.
     * 
     * @param key The key
     * @param group The group identifier
     */
    public void removeFromGroup(final Serializable key, final String group) {
        final Cache cache = this.cache;
        if (null != cache) {
            cache.removeFromGroup(key, group);
        }
    }

    /**
     * New cache key for given <code>int</code> pair.
     * 
     * @param int1 The first <code>int</code> value
     * @param int2 The second <code>int</code> value
     * @return The cache key or <code>null</code>
     */
    public CacheKey newCacheKey(final int int1, final int int2) {
        final Cache cache = this.cache;
        return null == cache ? null : cache.newCacheKey(int1, int2);
    }

    /**
     * New cache key.
     * 
     * @param intVal An <code>int</code> value
     * @param objs Arbitrary further keys
     * @return The cache key or <code>null</code>
     */
    public CacheKey newCacheKey(final int intVal, final Serializable... objs) {
        final Cache cache = this.cache;
        return null == cache ? null : cache.newCacheKey(intVal, objs);
    }

}
