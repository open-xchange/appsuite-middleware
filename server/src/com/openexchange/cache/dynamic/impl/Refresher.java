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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.cache.dynamic.impl;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.cache.OXCachingException;
import com.openexchange.cache.OXCachingException.Code;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class Refresher<T extends Serializable> {

    /**
     * Logger.
     */
    private static final Log LOG = LogFactory.getLog(Refresher.class);

    /**
     * Factory for reloading cached objects.
     */
    private final OXObjectFactory<T> factory;

    /**
     * Cache key of the cached object.
     */
    private final Serializable key;

    /**
     * The cache region name.
     */
    private final String regionName;

    /**
     * Default constructor.
     */
    protected Refresher(final OXObjectFactory<T> factory, final String regionName) {
        super();
        this.factory = factory;
        this.key = factory.getKey();
        this.regionName = regionName;
    }

    private Cache getCache() throws CacheException {
        final CacheService service = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == service) {
            return null;
        }
        return service.getCache(regionName);
    }

    /**
     * Checks if the object was removed from the cache and must be reloaded from the database.
     * @throws AbstractOXException if loading or putting into cache fails.
     */
    protected T refresh() throws AbstractOXException {
        final Cache cache = getCache();
        if (null == cache) {
            return factory.load();
        }
        T retval = null;
        final Lock lock = factory.getCacheLock();
        Condition cond = null;
        lock.lock();
        try {
            final Object tmp = cache.get(key);
            if (null == tmp) {
                // I am the thread to load the object. Put temporary condition
                // into cache.
                cond = lock.newCondition();
                try {
                    cache.putSafe(key, (Serializable) cond);
                } catch (final CacheException e) {
                    throw new OXCachingException(Code.FAILED_PUT, e);
                }
            } else if (tmp instanceof Condition) {
                // I have to wait for another thread to load the object.
                cond = (Condition) tmp;
                if (cond.await(1, TimeUnit.SECONDS)) {
                    // Other thread finished loading the object.
                    final Object tmp2 = cache.get(key);
                    if (null != tmp2 && !(tmp2 instanceof Condition)) {
                        retval = (T) tmp2;
                        cond = null;
                    }
                } else {
                    // We have to load it, too.
                    LOG.warn("Found 2 threads loading cached objects after 1 second. Cache: " + regionName);
                }
            } else {
                // Only other option is that the cache contains the delegate
                // object.
                retval = (T) tmp;
            }
        } catch (final InterruptedException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
        if (null != cond) {
            retval = factory.load();
            lock.lock();
            try {
                cache.put(key, retval);
                cond.signalAll();
            } catch (final CacheException e) {
                throw new OXCachingException(Code.FAILED_PUT, e);
            } finally {
                lock.unlock();
            }
        }
        return retval;
    }
}
