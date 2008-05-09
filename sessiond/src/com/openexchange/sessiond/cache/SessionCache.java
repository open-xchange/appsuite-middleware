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

package com.openexchange.sessiond.cache;

import static com.openexchange.sessiond.services.SessiondServiceRegistry.getServiceRegistry;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.caching.ElementEventHandler;
import com.openexchange.caching.objects.CachedSession;
import com.openexchange.sessiond.cache.eventhandler.SessionCacheEventHandler;

/**
 * {@link SessionCache} - A cache for instances of {@link CachedSession}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class SessionCache {

	static final String LATERAL_REGION_NAME = "SessionLTCP";

	static final String REGION_NAME = "SessionCache";

	private static final ReadWriteLock READ_WRITE_LOCK = new ReentrantReadWriteLock();

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private static SessionCache singleton;

	/*
	 * Field members
	 */
	private final Cache cache;

	/**
	 * Initializes a new {@link SessionCache}
	 * 
	 * @throws CacheException
	 *             If initialization fails
	 */
	private SessionCache() throws CacheException {
		super();
		cache = getServiceRegistry().getService(CacheService.class).getCache(REGION_NAME);
//		/*
//		 * Add element event handler to default element attributes
//		 */
//		final ElementEventHandler eventHandler = new SessionCacheEventHandler();
//		final ElementAttributes attributes = cache.getDefaultElementAttributes();
//		attributes.addElementEventHandler(eventHandler);
//		cache.setDefaultElementAttributes(attributes);
	}

	/**
	 * Get the singleton instance
	 * 
	 * @return The singleton instance
	 * @throws CacheException
	 *             If instance initialization fails
	 */
	public static SessionCache getInstance() throws CacheException {
		if (!initialized.get()) {
			synchronized (initialized) {
				if (null == singleton) {
					singleton = new SessionCache();
					initialized.set(true);
				}
			}
		}
		return singleton;
	}

	/**
	 * Releases the singleton instance
	 */
	public static void releaseInstance() {
		if (initialized.get()) {
			synchronized (initialized) {
				if (null != singleton) {
					singleton = null;
					initialized.set(false);
				}
			}
		}
	}

	/**
	 * Removes and returns a cached session from cache
	 * 
	 * @param sessionId
	 *            The session ID
	 * @param contextId
	 *            The context ID
	 * @return A cached session or <code>null</code>
	 * @throws CacheException
	 *             If removing from cache fails
	 */
	public CachedSession removeCachedSession(final String sessionId) throws CacheException {
		READ_WRITE_LOCK.readLock().lock();
		try {
			final CacheKey key = createKey(sessionId);
			if (cache.get(key) == null) {
				/*
				 * Cached session is not available. Return immediately.
				 */
				return null;
			}
			/*
			 * Upgrade lock: unlock first to acquire write lock
			 */
			READ_WRITE_LOCK.readLock().unlock();
			READ_WRITE_LOCK.writeLock().lock();
			try {
				final CachedSession cachedSession = (CachedSession) cache.get(key);
				/*
				 * Still available?
				 */
				if (cachedSession == null) {
					return null;
				}
				cache.remove(key);
				return cachedSession;
			} finally {
				/*
				 * Downgrade lock: reacquire read without giving up write lock
				 * and...
				 */
				READ_WRITE_LOCK.readLock().lock();
				/*
				 * ... unlock write.
				 */
				READ_WRITE_LOCK.writeLock().unlock();
			}
		} finally {
			READ_WRITE_LOCK.readLock().unlock();
		}
	}

	/**
	 * Puts given cache-able session into cache if none user-bound session is
	 * already contained in cache
	 * 
	 * @param cachedSession
	 *            The cache-able session to put into cache
	 * @return <code>true</code> if cache-able session could be successfully
	 *         cached; otherwise <code>false</code>
	 * @throws CacheException
	 *             If put into cache fails
	 */
	public boolean putCachedSession(final CachedSession cachedSession) throws CacheException {
		READ_WRITE_LOCK.readLock().lock();
		try {
			final CacheKey key = createKey(cachedSession.getSessionId());
			if (cache.get(key) != null) {
				/*
				 * Key is already in use and therefore an IMAP connection is
				 * already in cache for current user
				 */
				return false;
			}
			/*
			 * Upgrade lock: unlock first to acquire write lock
			 */
			READ_WRITE_LOCK.readLock().unlock();
			READ_WRITE_LOCK.writeLock().lock();
			try {
				/*
				 * Still not present?
				 */
				if (cache.get(key) != null) {
					return false;
				}
				cache.put(key, cachedSession);
				return true;
			} finally {
				/*
				 * Downgrade lock: reacquire read without giving up write lock
				 * and...
				 */
				READ_WRITE_LOCK.readLock().lock();
				/*
				 * ... unlock write.
				 */
				READ_WRITE_LOCK.writeLock().unlock();
			}
		} finally {
			READ_WRITE_LOCK.readLock().unlock();
		}
	}

	/**
	 * Checks if cache already holds a user-bound cached session
	 * 
	 * @param sessionId
	 *            The session ID
	 * @return <code>true</code> if a user-bound cached session is already
	 *         present in cache; otherwise <code>false</code>
	 */
	public boolean containsCachedSession(final String sessionId) {
		READ_WRITE_LOCK.readLock().lock();
		try {
			return (cache.get(createKey(sessionId)) != null);
		} finally {
			READ_WRITE_LOCK.readLock().unlock();
		}
	}

	private static final int DUMMY = 1;

	private static CacheKey createKey(final String sessionId) {
		return getServiceRegistry().getService(CacheService.class).newCacheKey(DUMMY, sessionId);
	}

}
