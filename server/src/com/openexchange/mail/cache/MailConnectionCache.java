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
import java.util.concurrent.atomic.AtomicBoolean;
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
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.mail.MailAccess;
import com.openexchange.mail.cache.eventhandler.MailConnectionEventHandler;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link MailConnectionCache} - a very volatile cache for already established
 * mail connections.
 * <p>
 * Only one connection can be cached per user and is dedicated to fasten
 * sequential mail requests<br>
 * TODO: Maybe own cache implementation (+ timer thread) to reduce lock overhead
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailConnectionCache {

	static final String REGION_NAME = "MailConnectionCache";

	private static final Lock LOCK_MOD = new ReentrantLock();

	private static final AtomicBoolean initialized = new AtomicBoolean();

	private static final Map<CacheKey, ReadWriteLock> contextLocks = new HashMap<CacheKey, ReadWriteLock>();

	private static MailConnectionCache singleton;

	/*
	 * Field members
	 */
	private final Cache cache;

	/**
	 * Prevent instantiation
	 * 
	 * @throws CacheException
	 *             If initialization fails
	 */
	private MailConnectionCache() throws CacheException {
		super();
		cache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache(REGION_NAME);
		/*
		 * Add element event handler to default element attributes
		 */
		final ElementEventHandler eventHandler = new MailConnectionEventHandler();
		final ElementAttributes attributes = cache.getDefaultElementAttributes();
		attributes.addElementEventHandler(eventHandler);
		cache.setDefaultElementAttributes(attributes);
	}

	/**
	 * Fetches the appropriate lock
	 * 
	 * @param key
	 *            The lock's key
	 * @return The appropriate lock
	 */
	private static ReadWriteLock getLock(final CacheKey key) {
		ReadWriteLock l = contextLocks.get(key);
		if (l == null) {
			LOCK_MOD.lock();
			try {
				if ((l = contextLocks.get(key)) == null) {
					l = new ReentrantReadWriteLock();
					contextLocks.put(key, l);
				}
			} finally {
				LOCK_MOD.unlock();
			}
		}
		return l;
	}

	/**
	 * Get the instance
	 * 
	 * @return The instance
	 * @throws CacheException
	 *             If instance initialization fails
	 */
	public static MailConnectionCache getInstance() throws CacheException {
		if (!initialized.get()) {
			synchronized (initialized) {
				if (null == singleton) {
					singleton = new MailConnectionCache();
					initialized.set(true);
				}
			}
		}
		return singleton;
	}

	/**
	 * Removes and returns a mail connection from cache
	 * 
	 * @param session
	 *            The session
	 * @return An active instance of {@link MailAccess} or <code>null</code>
	 * @throws CacheException
	 *             If removing from cache fails
	 */
	public MailAccess<?, ?, ?> removeMailConnection(final Session session) throws CacheException {
		final CacheKey key;
		try {
			key = getUserKey(session.getUserId(), ContextStorage.getStorageContext(session.getContextId()));
		} catch (final ContextException e1) {
			throw new CacheException(e1);
		}
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
				final MailAccess<?, ?, ?> mailConnection = (MailAccess<?, ?, ?>) cache.get(key);
				/*
				 * Still available?
				 */
				if (mailConnection == null) {
					return null;
				}
				cache.remove(key);
				return mailConnection;
			} finally {
				/*
				 * Downgrade lock: reacquire read without giving up write lock
				 * and...
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
	 * Puts given mail connection into cache if none user-bound connection is
	 * already contained in cache
	 * 
	 * @param session
	 *            The session
	 * @param mailConnection
	 *            The mail connection to put into cache
	 * @return <code>true</code> if mail connection could be successfully
	 *         cached; otherwise <code>false</code>
	 * @throws CacheException
	 *             If put into cache fails
	 */
	public boolean putMailConnection(final Session session, final MailAccess<?, ?, ?> mailConnection)
			throws CacheException {
		final CacheKey key;
		try {
			key = getUserKey(session.getUserId(), ContextStorage.getStorageContext(session.getContextId()));
		} catch (final ContextException e1) {
			throw new CacheException(e1);
		}
		final Lock readLock = getLock(key).readLock();
		readLock.lock();
		try {
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
				cache.put(key, mailConnection);
				return true;
			} finally {
				/*
				 * Downgrade lock: reacquire read without giving up write lock
				 * and...
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
	 * Checks if cache already holds a user-bound mail connection
	 * 
	 * @param session
	 *            The session
	 * @return <code>true</code> if a user-bound mail connection is already
	 *         present in cache; otherwise <code>false</code>
	 * @throws CacheException
	 *             If context loading fails
	 */
	public boolean containsMailConnection(final Session session) throws CacheException {
		final CacheKey key;
		try {
			key = getUserKey(session.getUserId(), ContextStorage.getStorageContext(session.getContextId()));
		} catch (ContextException e) {
			throw new CacheException(e);
		}
		final Lock readLock = getLock(key).readLock();
		readLock.lock();
		try {
			return (cache.get(key) != null);
		} finally {
			readLock.unlock();
		}
	}

	private static CacheKey getUserKey(final int user, final Context ctx) {
		return ServerServiceRegistry.getInstance().getService(CacheService.class).newCacheKey(ctx.getContextId(), user);
	}
}
