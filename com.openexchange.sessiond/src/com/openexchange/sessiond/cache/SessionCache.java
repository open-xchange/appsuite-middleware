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

package com.openexchange.sessiond.cache;

import static com.openexchange.sessiond.services.SessiondServiceRegistry.getServiceRegistry;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.InvalidatedMarker;
import com.openexchange.caching.SupportsLocalOperations;
import com.openexchange.caching.objects.CachedSession;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link SessionCache} - A cache for instances of {@link CachedSession}.
 * <p>
 * <b>Note</b>: The appropriate instance of {@link CacheService} is obtained on every request thus there's no need to to release/re-init any
 * references.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionCache {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(SessionCache.class));

    static final String LATERAL_REGION_NAME = "SessionLTCP";

    static final String REGION_NAME = "SessionCache";

    private static volatile SessionCache singleton;

    private final ReadWriteLock readWriteLock;

    private SessionCache() {
        super();
        readWriteLock = new ReentrantReadWriteLock();
        /**
         * Uncomment this to enable default element event handler
         *
         * <pre>
         * final Cache cache;
         * try {
         *     cache = getCache();
         * } catch (final OXException e) {
         *     throw new CacheException(e);
         * }
         * final ElementAttributes attributes = cache.getDefaultElementAttributes();
         * final ElementEventHandler eventHandler = new SessionCacheEventHandler();
         * attributes.addElementEventHandler(eventHandler);
         * cache.setDefaultElementAttributes(attributes);
         * </pre>
         */
    }

    /**
     * Gets associated cache.
     * 
     * @return The cache
     * @throws OXException If cache is absent
     */
    public Cache getCache() throws OXException {
        final CacheService cacheService = getServiceRegistry().getService(CacheService.class);
        if (null == cacheService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create( CacheService.class.getName());
        }
        return cacheService.getCache(REGION_NAME);
    }

    public static SessionCache getInstance() {
        if (null == singleton) {
            synchronized (SessionCache.class) {
                if (null == singleton) {
                    singleton = new SessionCache();
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
            synchronized (SessionCache.class) {
                if (null != singleton) {
                    singleton = null;
                }
            }
        }
    }

    /**
     * Checks if cache contains invalidate marker for given context (and performs local-remove if present).
     *
     * @param contextId The context identifier
     * @return <code>true</code> if present in cache; otherwise <code>false</code>
     * @throws OXException If a caching error occurs
     */
    @SuppressWarnings("unchecked")
    public boolean containsInvalidateMarker(final int contextId) throws OXException {
        final Cache cache = getCache();
        final boolean supportsLocal = (cache instanceof SupportsLocalOperations);
        if (supportsLocal) {
            // Proceed
            final Lock readLock = readWriteLock.readLock();
            readLock.lock();
            try {
                final Integer key = Integer.valueOf(contextId);
                Object element = cache.get(key);
                if ((null != element) && (element instanceof InvalidatedMarker) && (contextId == ((InvalidatedMarker<Integer>) element).getIdentifier().intValue())) {
                    final Lock writeLock = readWriteLock.writeLock();
                    readLock.unlock();
                    writeLock.lock();
                    try {
                        element = cache.get(key);
                        if (null != element) {
                            cache.localRemove(key);
                            if (LOG.isInfoEnabled()) {
                                LOG.info("Detected & (locally) removed invalidate marker for context: " + contextId);
                            }
                        }
                    } finally {
                        readLock.lock();
                        writeLock.unlock();
                    }
                    return true;
                }
                return false;
            } finally {
                readLock.unlock();
            }
        }
        return false;
    }

    /**
     * Removes and returns a cached session from cache
     *
     * @param sessionId The session identifier (which is sent as <i>"session=..."</i> in every request)
     * @param contextId The context ID
     * @return A cached session or <code>null</code>
     * @throws OXException If caching service is not available
     */
    public CachedSession removeCachedSession(final String sessionId) throws OXException {
        final Cache cache = getCache();
        final Lock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            final CacheKey key = createKey(sessionId, cache);
            if (cache.get(key) == null) {
                /*
                 * Cached session is not available. Return immediately.
                 */
                return null;
            }
            /*
             * Upgrade lock: unlock first to acquire write lock
             */
            readLock.unlock();
            final Lock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                final CachedSession cachedSession = (CachedSession) cache.get(key);
                /*
                 * Still available?
                 */
                if (cachedSession == null) {
                    return null;
                }
                cache.remove(key);
                cache.remove(createUserKey(cachedSession, cache));
                return cachedSession;
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
     * Puts given cache-able session into cache if none user-bound session is already contained in cache.
     * <p>
     * The secret cookie identifier obtained by {@link CachedSession#getSecret()} is used as key.
     *
     * @param cachedSession The cache-able session to put into cache
     * @return <code>true</code> if cache-able session could be successfully cached; otherwise <code>false</code>
     * @throws OXException If caching service is not available
     */
    public boolean putCachedSession(final CachedSession cachedSession) throws OXException {
        final Cache cache = getCache();
        final Lock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            final CacheKey key = createKey(cachedSession.getSessionId(), cache);
            if (cache.get(key) != null) {
                /*
                 * Key is already in use
                 */
                return false;
            }
            /*
             * Upgrade lock: unlock first to acquire write lock
             */
            readLock.unlock();
            final Lock writeLock = readWriteLock.writeLock();
            writeLock.lock();
            try {
                /*
                 * Still not present?
                 */
                if (cache.get(key) != null) {
                    return false;
                }
                cachedSession.setMarkedAsRemoved(false);
                cache.put(key, cachedSession);
                cache.put(createUserKey(cachedSession, cache), cachedSession.getSessionId());
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
     * Puts given cache-able session into cache to distribute a remove for associated session among auxiliary caches.
     *
     * @param cachedSession The cached session which shall be removed in auxiliary caches
     * @throws OXException If caching service is not available
     */
    public void putCachedSessionForRemoteRemoval(final CachedSession cachedSession) throws OXException {
        final Cache cache = getCache();
        final Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            /*
             * Mark for remote removal
             */
            cachedSession.setMarkedAsRemoved(true);
            /*
             * Put to cache
             */
            cache.put(createKey(cachedSession.getSessionId(), cache), cachedSession);
            cache.put(createUserKey(cachedSession, cache), cachedSession.getSessionId());
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Checks if cache already holds a user-bound cached session
     *
     * @param sessionId The secret cookie identifier (which is sent as <i>"session=..."</i> in every request)
     * @return <code>true</code> if a user-bound cached session is already present in cache; otherwise <code>false</code>
     * @throws OXException If caching service is not available
     */
    public boolean containsCachedSession(final String sessionId) throws OXException {
        final Cache cache = getCache();
        final Lock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            return (cache.get(createKey(sessionId, cache)) != null);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Gets the first encountered cached session for given user in specified context
     *
     * @param userId The user ID
     * @param contextId The context ID
     * @return The first encountered cached session for given user in specified context or <code>null</code> if none found
     * @throws OXException If caching service is not available
     */
    public CachedSession getCachedSessionByUser(final int userId, final int contextId) throws OXException {
        final Cache cache = getCache();
        final Lock readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            final String sessionId = (String) cache.get(createUserKey(userId, contextId, cache));
            if (sessionId == null) {
                return null;
            }
            return (CachedSession) cache.get(createKey(sessionId, cache));
        } finally {
            readLock.unlock();
        }
    }

    /**
     * This method removes the dummy integer from the session cache. This triggers a remote remove and a broken connection is detected.
     *
     * @throws OXException
     */
    public void testConnection() throws OXException {
        final CacheService cacheService = getServiceRegistry().getService(CacheService.class);
        if (null == cacheService) {
            return;
        }
        final Cache cache = cacheService.getCache(REGION_NAME);
        cache.remove(Integer.valueOf(DUMMY));
    }

    private static final int DUMMY = 1;

    private static CacheKey createKey(final String sessionId, final Cache cache) {
        return cache.newCacheKey(DUMMY, sessionId);
    }

    private static CacheKey createUserKey(final CachedSession cs, final Cache cache) {
        return cache.newCacheKey(cs.getContextId(), cs.getUserId());
    }

    private static CacheKey createUserKey(final int userId, final int contextId, final Cache cache) {
        return cache.newCacheKey(contextId, userId);
    }
}
