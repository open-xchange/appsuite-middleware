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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.caching.dynamic;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import org.apache.commons.logging.Log;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.LockAware;
import com.openexchange.caching.PutIfAbsent;
import com.openexchange.caching.osgi.CacheActivator;
import com.openexchange.exception.OXException;
import com.openexchange.log.LogFactory;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class Refresher<T extends Serializable> {

    /**
     * Logger.
     */
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Refresher.class));

    /**
     * Factory for reloading cached objects.
     */
    private final OXObjectFactory<T> factory;

    /**
     * The cache region name.
     */
    private final String regionName;

    /**
     * Whether to issue a cache remove operation before replacing a cache element.
     */
    private final boolean removeBeforePut;

    /**
     * Default constructor.
     *
     * @throws IllegalArgumentException If provided region name is <code>null</code>
     */
    protected Refresher(final OXObjectFactory<T> factory, final String regionName, final boolean removeBeforePut) {
        super();
        this.factory = factory;
        if (null == regionName) {
            throw new IllegalArgumentException("Cache region name is null");
        }
        this.regionName = regionName;
        this.removeBeforePut = removeBeforePut;
    }

    private Cache getCache() throws OXException {
        return getCache(regionName);
    }

    protected void cache(final T obj) throws OXException {
        cache(obj, getCache(), factory);
    }

    /**
     * Checks if the object was removed from the cache and must be reloaded from the database.
     *
     * @throws OXException If refresh fails
     */
    protected T refresh() throws OXException {
        return refresh(regionName, factory, removeBeforePut);
    }

    public static <T extends Serializable> T cache(final T obj, final Cache cache, final OXObjectFactory<T> factory) throws OXException {
        T retval = null;
        final Lock lock = getLock(cache, factory);
        final Serializable key = factory.getKey();
        lock.lock();
        try {
            final Object tmp = cache.get(key);
            if (null == tmp) {
                cache.put(key, obj);
            } else if (tmp instanceof Condition) {
                cache.put(key, obj);
                ((Condition) tmp).signalAll();
            } else {
                // If object is already in cache, return it instead of putting new object into cache.
                @SuppressWarnings("unchecked")
                final
                T tmp2 = (T) tmp;
                retval = tmp2;
            }
        } finally {
            lock.unlock();
        }
        return retval;
    }

    private static <T extends Serializable> Lock getLock(final Cache cache, final OXObjectFactory<T> factory) {
        if (cache instanceof LockAware) {
            return ((LockAware) cache).getLock();
        }
        return factory.getCacheLock();
    }

    public static <T extends Serializable> T refresh(final String regionName, final OXObjectFactory<T> factory, final boolean removeBeforePut) throws OXException {
        return refresh(regionName, getCache(regionName), factory, removeBeforePut);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T refresh(final String regionName, final Cache cache, final OXObjectFactory<T> factory, final boolean removeBeforePut) throws OXException {
        if (null == cache) {
            return factory.load();
        }
        final Lock lock = getLock(cache, factory);
        try {
            if (!lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                return factory.load();
            }
            // Lock obtained
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            LOG.error(e.getMessage(), e);
            return factory.load();
        }
        /*
         * Lock acquired
         */
        final Serializable key = factory.getKey();
        T retval = null;
        /*
         * Check for distributed cache nature
         */
        if (cache.isDistributed()) {
            retval = (T) cache.get(key);
            if (null == retval) {
                try {
                    if (cache instanceof PutIfAbsent) {
                        final T newVal = factory.load();
                        retval = (T) ((PutIfAbsent) cache).putIfAbsent(key, newVal);
                        if (null == retval) {
                            retval = newVal;
                        }
                    } else {
                        try {
                            final T newVal = factory.load();
                            cache.putSafe(key, newVal);
                            retval = newVal;
                        } catch (final OXException e) {
                            if (!CacheExceptionCode.FAILED_SAFE_PUT.equals(e)) {
                                throw e;
                            }
                            // Obviously another thread put in the meantime
                            retval = (T) cache.get(key);
                        }
                    }
                } catch (final RuntimeException e) {
                    throw CacheExceptionCode.CACHE_ERROR.create(e, e.getMessage());
                }
            }
            return retval;
        }
        /*
         * Lock acquired & replicated cache
         */
        Condition cond = null;
        try {
            final Object tmp = cache.get(key);
            if (null == tmp) {
                // I am the thread to load the object. Put temporary condition
                // into cache.
                cond = lock.newCondition();
                {
                    cache.putSafe(key, (Serializable) cond);
                }
            } else if (tmp instanceof Condition) {
                // I have to wait for another thread to load the object.
                cond = (Condition) tmp;
                if (cond.await(1, TimeUnit.SECONDS)) {
                    // Other thread finished loading the object.
                    final Object tmp2 = cache.get(key);
                    if (null != tmp2 && !(tmp2 instanceof Condition)) {
                        @SuppressWarnings("unchecked")
                        final
                        T tmp3 = (T) tmp2;
                        retval = tmp3;
                        cond = null;
                    }
                } else {
                    // We have to load it, too.
                    LOG.warn("Found 2 threads loading object \"" + String.valueOf(key) + "\" after 1 second into Cache \"" + regionName + "\"");
                }
            } else {
                // Only other option is that the cache contains the delegate object.
                @SuppressWarnings("unchecked")
                final
                T tmp2 = (T) tmp;
                retval = tmp2;
            }
        } catch (final InterruptedException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
        if (null != cond) {
            try {
                retval = factory.load();
            } catch (final OXException e) {
                cache.remove(key);
                throw e;
            }
            lock.lock();
            try {
                if (removeBeforePut) {
                    // Do we replace an existing value?
                    final Object prev = cache.get(key);
                    if (null != prev && !(prev instanceof Condition)) {
                        // Issue remove for lateral distribution
                        cache.remove(key);
                    }
                }
                cache.put(key, retval);
                cond.signalAll();
            } finally {
                lock.unlock();
            }
        }
        return retval;
    }

    public static Cache getCache(final String regionName) throws OXException {
        if (null == regionName) {
            throw CacheExceptionCode.INVALID_CACHE_REGION_NAME.create("null");
        }
        final CacheService service = CacheActivator.getCacheService();
        return null == service ? null : service.getCache(regionName);
    }

}
