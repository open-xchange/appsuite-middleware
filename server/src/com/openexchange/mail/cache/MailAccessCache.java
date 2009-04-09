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

package com.openexchange.mail.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.caching.ElementEventHandler;
import com.openexchange.mail.MailException;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.cache.eventhandler.MailAccessEventHandler;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link MailAccessCache} - a very volatile cache for already connected instances of {@link MailAccess}.
 * <p>
 * Only one mail access can be cached per user and is dedicated to fasten sequential mail requests<br>
 * TODO: Maybe own cache implementation (+ timer thread) to reduce lock overhead
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccessCache {

    static final String REGION_NAME = "MailConnectionCache";

    private static final Lock LOCK_MOD = new ReentrantLock();

    private static final Map<CacheKey, ReadWriteLock> contextLocks = new HashMap<CacheKey, ReadWriteLock>();

    private static volatile MailAccessCache singleton;

    /*
     * Field members
     */
    private Cache cache;

    private long defaultIdleSeconds;

    /**
     * Prevent instantiation
     * 
     * @throws CacheException If initialization fails
     */
    private MailAccessCache() throws CacheException {
        super();
        initCache();
    }

    /**
     * Fetches the appropriate lock
     * 
     * @param key The lock's key
     * @return The appropriate lock
     */
    private static ReadWriteLock getLock(final CacheKey key) {
        if (!contextLocks.containsKey(key)) {
            LOCK_MOD.lock();
            try {
                if (!contextLocks.containsKey(key)) {
                    contextLocks.put(key, new ReentrantReadWriteLock());
                }
            } finally {
                LOCK_MOD.unlock();
            }
        }
        return contextLocks.get(key);
    }

    /**
     * Gets the singleton instance
     * <p>
     * Singleton instance is created following the thread-safe lazy-initialization pattern:
     * 
     * <pre>
     * 
     * 
     * 
     * 
     * 
     * 
     * // Works with acquire/release semantics for volatile
     * class Foo {
     * 
     *     private volatile Helper helper = null;
     * 
     *     public Helper getHelper() {
     *         if (helper == null) {
     *             synchronized (this) {
     *                 if (helper == null)
     *                     helper = new Helper();
     *             }
     *         }
     *         return helper;
     *     }
     * }
     * 
     * </pre>
     * 
     * @return The singleton instance
     * @throws CacheException If instance initialization fails
     */
    public static MailAccessCache getInstance() throws CacheException {
        if (null == singleton) {
            synchronized (MailAccessCache.class) {
                if (null == singleton) {
                    singleton = new MailAccessCache();
                }
            }
        }
        return singleton;
    }

    /**
     * Releases the singleton instance
     */
    public static void releaseInstance() {
        if (null != singleton) {
            synchronized (MailAccessCache.class) {
                if (null != singleton) {
                    singleton = null;
                }
            }
        }
    }

    /**
     * Initializes cache reference
     * 
     * @throws CacheException If initializing the cache reference fails
     */
    public void initCache() throws CacheException {
        /*
         * Check for proper started mail cache configuration
         */
        if (!MailCacheConfiguration.getInstance().isStarted()) {
            throw new CacheException(new MailException(MailException.Code.INITIALIZATION_PROBLEM));
        }
        if (cache != null) {
            return;
        }
        cache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache(REGION_NAME);
        /*
         * Add element event handler to default element attributes
         */
        final ElementEventHandler eventHandler = new MailAccessEventHandler();
        final ElementAttributes attributes = cache.getDefaultElementAttributes();
        attributes.addElementEventHandler(eventHandler);
        cache.setDefaultElementAttributes(attributes);
        defaultIdleSeconds = attributes.getIdleTime();
    }

    /**
     * Releases cache reference
     * 
     * @throws CacheException If clearing cache fails
     */
    public void releaseCache() throws CacheException {
        if (cache == null) {
            return;
        }
        cache.clear();
        cache = null;
        defaultIdleSeconds = 0;
    }

    /**
     * Removes and returns a mail access from cache
     * 
     * @param session The session
     * @param accountId The account ID
     * @return An active instance of {@link MailAccess} or <code>null</code>
     * @throws CacheException If removing from cache fails
     */
    public MailAccess<?, ?> removeMailAccess(final Session session, final int accountId) throws CacheException {
        if (null == cache) {
            return null;
        }
        final CacheKey key = getUserKey(session.getUserId(), accountId, session.getContextId());
        final Lock readLock = getLock(key).readLock();
        readLock.lock();
        try {
            if (cache.get(key) == null) {
                /*
                 * Connection is not available. Return immediately.
                 */
                return null;
            }
            /*
             * Upgrade lock: unlock first to acquire write lock
             */
            readLock.unlock();
            final Lock writeLock = getLock(key).writeLock();
            writeLock.lock();
            try {
                final MailAccess<?, ?> mailAccess = (MailAccess<?, ?>) cache.get(key);
                /*
                 * Still available?
                 */
                if (mailAccess == null) {
                    return null;
                }
                cache.remove(key);
                return mailAccess;
            } finally {
                /*
                 * Downgrade lock: reacquire read without giving up write lock and...
                 */
                readLock.lock();
                /*
                 * ... unlock write.
                 */
                writeLock.unlock();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Puts given mail access into cache if none user-bound connection is already contained in cache
     * 
     * @param session The session
     * @param accountId The account ID
     * @param mailAccess The mail access to put into cache
     * @return <code>true</code> if mail access could be successfully cached; otherwise <code>false</code>
     * @throws CacheException If put into cache fails
     */
    public boolean putMailAccess(final Session session, final int accountId, final MailAccess<?, ?> mailAccess) throws CacheException {
        if (null == cache) {
            return false;
        }
        final CacheKey key = getUserKey(session.getUserId(), accountId, session.getContextId());
        final Lock readLock = getLock(key).readLock();
        readLock.lock();
        try {
            if (cache.get(key) != null) {
                /*
                 * Key is already in use and therefore an mail connection is already in cache for current user
                 */
                return false;
            }
            /*
             * Upgrade lock: unlock first to acquire write lock
             */
            readLock.unlock();
            final Lock writeLock = getLock(key).writeLock();
            writeLock.lock();
            try {
                /*
                 * Still not present?
                 */
                if (cache.get(key) != null) {
                    return false;
                }
                final int idleTime = mailAccess.getCacheIdleSeconds();
                if (idleTime <= 0 || defaultIdleSeconds == idleTime) {
                    cache.put(key, mailAccess);
                } else {
                    final ElementAttributes attributes = cache.getDefaultElementAttributes();
                    attributes.setIdleTime(idleTime);
                    cache.put(key, mailAccess, attributes);
                }
                return true;
            } finally {
                /*
                 * Downgrade lock: reacquire read without giving up write lock and...
                 */
                readLock.lock();
                /*
                 * ... unlock write.
                 */
                writeLock.unlock();
            }
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Checks if cache already holds a user-bound mail access for specified account.
     * 
     * @param session The session
     * @param accountId The account ID
     * @return <code>true</code> if a user-bound mail access is already present in cache; otherwise <code>false</code>
     * @throws CacheException If context loading fails
     */
    public boolean containsMailAccess(final Session session, final int accountId) throws CacheException {
        if (null == cache) {
            return false;
        }
        final CacheKey key = getUserKey(session.getUserId(), accountId, session.getContextId());
        final Lock readLock = getLock(key).readLock();
        readLock.lock();
        try {
            return (cache.get(key) != null);
        } finally {
            readLock.unlock();
        }
    }

    private CacheKey getUserKey(final int user, final int accountId, final int cid) {
        return cache.newCacheKey(cid, user ^ accountId);
    }
}
