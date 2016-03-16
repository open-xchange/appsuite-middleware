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

package com.openexchange.caching;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link VolatileCacheLock} - A volatile lock for caching.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VolatileCacheLock {

    private static final class LockKey {

        final String regionName;
        final String groupName;
        final Object key;
        final int hash;

        LockKey(final Object key, final String groupName, final String regionName) {
            super();
            this.key = key;
            this.groupName = groupName;
            this.regionName = regionName;
            // Hash code
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((regionName == null) ? 0 : regionName.hashCode());
            result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LockKey)) {
                return false;
            }
            final LockKey other = (LockKey) obj;
            if (key == null) {
                if (other.key != null) {
                    return false;
                }
            } else if (!key.equals(other.key)) {
                return false;
            }
            if (groupName == null) {
                if (other.groupName != null) {
                    return false;
                }
            } else if (!groupName.equals(other.groupName)) {
                return false;
            }
            if (regionName == null) {
                if (other.regionName != null) {
                    return false;
                }
            } else if (!regionName.equals(other.regionName)) {
                return false;
            }
            return true;
        }
    } // End of class LockKey

    /**
     * The result returned from <tt>acquire()</tt> method.
     */
    public static final class Result {

        final Serializable key;
        final String groupName;
        final String regionName;
        final Lock lock;
        final boolean cleanUp;

        Result(final Serializable key, final String groupName, final String regionName, final Lock lock, final boolean cleanUp) {
            super();
            this.key = key;
            this.groupName = groupName;
            this.regionName = regionName;
            this.lock = lock;
            this.cleanUp = cleanUp;
        }

    } // End of class Result

    private static final VolatileCacheLock INSTANCE = new VolatileCacheLock();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static VolatileCacheLock getInstance() {
        return INSTANCE;
    }

    // --------------------------- Member stuff --------------------------------- //

    private final ConcurrentHashMap<LockKey, Lock> locks;

    /**
     * Initializes a new {@link VolatileCacheLock}.
     */
    private VolatileCacheLock() {
        super();
        locks = new ConcurrentHashMap<LockKey, Lock>(1024, 0.9f, 1);
    }

    /**
     * Acquire volatile lock for specified key.
     *
     * <pre>
     *         ...
     *         final VolatileCacheLock instance = VolatileCacheLock.getInstance();
     *         final Result res = instance.acquire(key, regionName);
     *         try {
     *             // Do some exclusive caching logic here
     *         } finally {
     *             instance.release(res);
     *         }
     * </pre>
     *
     * @param key The lock key
     * @param regionName The cache's region name
     * @return <code>true</code> whether clean-up needs to be performed; otherwise <code>false</code>
     * @see CacheService#newCacheKey()
     */
    public Result acquire(final Serializable key, final String regionName) {
        return acquire(key, null, regionName);
    }

    /**
     * Acquire volatile lock for specified key.
     *
     * <pre>
     *         ...
     *         final VolatileCacheLock instance = VolatileCacheLock.getInstance();
     *         final Result res = instance.acquire(key, groupName, regionName);
     *         try {
     *             // Do some exclusive caching logic here
     *         } finally {
     *             instance.release(res);
     *         }
     * </pre>
     *
     * @param key The lock key
     * @param groupName The optional group name
     * @param regionName The cache's region name
     * @return <code>true</code> whether clean-up needs to be performed; otherwise <code>false</code>
     * @see CacheService#newCacheKey()
     */
    public Result acquire(final Serializable key, final String groupName, final String regionName) {
        final LockKey lockKey = new LockKey(key, groupName, regionName);
        Lock lock = locks.get(lockKey);
        boolean retval = false;
        if (null == lock) {
            final Lock newLock = new ReentrantLock();
            lock = locks.putIfAbsent(lockKey, newLock);
            if (null == lock) {
                lock = newLock;
                retval = true;
            }
        }
        lock.lock();
        return new Result(key, groupName, regionName, lock, retval);
    }

    /**
     * Releases volatile lock for specified key and result.
     *
     * @param key The lock key
     * @param regionName The cache's region name
     * @param result The result
     */
    public void release(final Result result) {
        final Lock lock = result.lock;
        if (null != lock) {
            if (result.cleanUp) {
                locks.remove(new LockKey(result.key, result.groupName, result.regionName));
            }
            lock.unlock();
        }
    }
}
