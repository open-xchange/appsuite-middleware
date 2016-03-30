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

package com.openexchange.tools.caching;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.caching.Cache;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.internal.SchemaStoreImpl;

/**
 * The {@link SerializedCachingLoader} serializes parallel loading from the storage. The {@link Cache} is utilized to store already loaded
 * storage data. Additionally it shares conditions if somebody starts loading data to retard other threads in loadind the same data.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class SerializedCachingLoader {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaStoreImpl.class);

    public SerializedCachingLoader() {
        super();
    }

    /**
     * Tries to fetch the data for the given key from the cache. If this is not possible, a condition is put into the cache blocking other
     * threads from loading the same data. Then the data is loaded from the storage layer and afterwards put into the cache.
     * @param cache the cache.
     * @param regionName the region name within the cache.
     * @param groupName the name of the cache group or simply <code>null</code>.
     * @param lock the lock to use for throttling threads trying to access the cache or the storage layer.
     * @param loader the method to load the data from the storage layer if it is not available from the cache.
     * @return the data either fetched from the cache or loaded from the storage layer.
     * @throws OXException if the whole mechanism does not work as expected or some race conditions occurs.
     */
    public static <T extends Serializable> T fetch(Cache cache, String regionName, String groupName, Lock lock, StorageLoader<T> loader) throws OXException {
        if (null == cache) {
            return loader.load();
        }
        T retval = null;
        try {
            if (!lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                return loader.load();
            }
            // Lock obtained
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            LOG.error("", e);
            return loader.load();
        }
        // Lock acquired & replicated cache
        final Serializable key = loader.getKey();
        Condition cond = null;
        try {
            final Object tmp = get(cache, groupName, key);
            if (null == tmp) {
                // I am the thread to load the object. Put temporary condition into cache.
                cond = lock.newCondition();
                putSafe(cache, groupName, key, (Serializable) cond, false);
            } else if (tmp instanceof Condition) {
                // I have to wait for another thread to load the object.
                cond = (Condition) tmp;
                if (cond.await(1, TimeUnit.SECONDS)) {
                    // Other thread finished loading the object.
                    final Object tmp2 = get(cache, groupName, key);
                    if (null != tmp2 && !(tmp2 instanceof Condition)) {
                        retval = (T) tmp2;
                        cond = null;
                    }
                } else {
                    // We have to load it, too.
                    LOG.warn("Found 2 threads loading object \"" + String.valueOf(key) + "\" after 1 second into Cache \"" + regionName + "\"");
                }
            } else {
                // Only other option is that the cache contains the delegate object.
                retval = (T) tmp;
            }
        } catch (final InterruptedException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
        if (null != cond) {
            try {
                retval = loader.load();
            } catch (final OXException e) {
                remove(cache, groupName, key);
                throw e;
            }
            lock.lock();
            try {
                put(cache, groupName, key, retval, false);
                cond.signalAll();
            } finally {
                lock.unlock();
            }
        }
        return retval;
    }

    private static Object get(Cache cache, String groupName, Serializable key) {
        final Object retval;
        if (null == groupName) {
            retval = cache.get(key);
        } else {
            retval = cache.getFromGroup(key, groupName);
        }
        return retval;
    }

    private static void put(Cache cache, String groupName, Serializable key, Serializable value, boolean invalidate) throws OXException {
        if (null == groupName) {
            cache.put(key, value, invalidate);
        } else {
            cache.putInGroup(key, groupName, value, invalidate);
        }
    }

    private static void putSafe(Cache cache, String groupName, Serializable key, Serializable value, boolean invalidate) throws OXException {
        if (null == groupName) {
            cache.putSafe(key, value);
        } else {
            cache.putInGroup(key, groupName, value, invalidate);
        }
    }

    private static void remove(Cache cache, String groupName, Serializable key) throws OXException {
        if (null == groupName) {
            cache.remove(key);
        } else {
            cache.removeFromGroup(key, groupName);
        }
    }
}
