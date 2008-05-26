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

package com.openexchange.groupware.ldap;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.cache.dynamic.impl.CacheProxy;
import com.openexchange.cache.dynamic.impl.OXObjectFactory;
import com.openexchange.cache.registry.CacheAvailabilityListener;
import com.openexchange.cache.registry.CacheAvailabilityRegistry;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException.Code;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * This class implements the user storage using a cache to store once read
 * objects.
 */
public class CachingUserStorage extends UserStorage {

	/**
	 * Logger.
	 */
	private static final Log LOG = LogFactory.getLog(CachingUserStorage.class);

	/**
	 * Proxy attribute for the object implementing the persistent methods.
	 */
	private UserStorage delegate;

	/**
	 * The cache availability listener
	 */
	private final CacheAvailabilityListener cacheAvailabilityListener;

	/**
	 * Cache.
	 */
	private Cache cache;

	/**
	 * Lock for the cache.
	 */
	private final Lock cacheLock;

	/**
	 * Default constructor.
	 */
	public CachingUserStorage() {
		super();
		cacheLock = new ReentrantLock(true);
		cacheAvailabilityListener = new CacheAvailabilityListener() {
			public void handleAbsence() throws AbstractOXException {
				releaseCache();
			}
			public void handleAvailability() throws AbstractOXException {
				initCache();
			}
		};
		initCache();
	}

	/**
	 * Initializes cache reference
	 */
	private void initCache() {
		if (cache != null) {
			return;
		}
		try {
			cache = ServerServiceRegistry.getInstance().getService(CacheService.class).getCache("User");
		} catch (CacheException e) {
			throw new RuntimeException("Cannot create user cache.", e);
		}
	}

	/**
	 * Releases cache reference
	 */
	private void releaseCache() {
		if (cache == null) {
			return;
		}
		try {
			cache.clear();
		} catch (final CacheException e) {
			throw new RuntimeException("Cannot clear user cache.", e);
		}
		cache = null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getUser(final int uid, final Context context) throws LdapException {
		if (null == cache) {
			return getUserStorage().getUser(uid, context);
		}
		final OXObjectFactory<User> factory = new OXObjectFactory<User>() {
			public Serializable getKey() {
				return cache.newCacheKey(context.getContextId(), uid);
			}

			public User load() throws LdapException {
				return getUserStorage().getUser(uid, context);
			}

			public Lock getCacheLock() {
				return cacheLock;
			}
		};
		cacheLock.lock();
		try {
			if (null == cache.get(factory.getKey())) {
				/*
				 * Check existence through a load
				 */
				final User user = getUserStorage().getUser(uid, context);
				if (null != user) {
					try {
						cache.putSafe(factory.getKey(), (Serializable) user);
					} catch (final CacheException e) {
						throw new LdapException(e);
					}
				}
			}
		} finally {
			cacheLock.unlock();
		}
		return CacheProxy.getCacheProxy(factory, cache, User.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateUser(final User user, final Context context) throws LdapException {
		getUserStorage().updateUser(user, context);
		if (null != cache) {
			try {
				cache.remove(cache.newCacheKey(context.getContextId(), user.getId()));
			} catch (final CacheException e) {
				throw new LdapException(EnumComponent.USER, Code.CACHE_PROBLEM, e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getUserId(final String uid, final Context context) throws LdapException {
		if (null == cache) {
			return getUserStorage().getUserId(uid, context);
		}
		final CacheKey key = cache.newCacheKey(context.getContextId(), uid);
		int identifier = -1;
		final Integer tmp = (Integer) cache.get(key);
		if (null == tmp) {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Cache MISS. Context: " + context.getContextId() + " User: " + uid);
			}
			identifier = getUserStorage().getUserId(uid, context);
			try {
				cache.put(key, Integer.valueOf(identifier));
			} catch (CacheException e) {
				throw new LdapException(EnumComponent.USER, Code.CACHE_PROBLEM, e);
			}
		} else {
			if (LOG.isTraceEnabled()) {
				LOG.trace("Cache HIT. Context: " + context.getContextId() + " User: " + uid);
			}
			identifier = tmp.intValue();
		}
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] listModifiedUser(final Date modifiedSince, final Context context) throws LdapException {
		// Caching doesn't make any sense here.
		return getUserStorage().listModifiedUser(modifiedSince, context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User searchUser(final String email, final Context context) throws LdapException {
		return getUserStorage().searchUser(email, context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] listAllUser(final Context context) throws UserException {
		try {
			return getUserStorage().listAllUser(context);
		} catch (LdapException e) {
			throw new UserException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int resolveIMAPLogin(final String imapLogin, final Context context) throws UserException {
		if (null == cache) {
			try {
				return getUserStorage().resolveIMAPLogin(imapLogin, context);
			} catch (LdapException e) {
				throw new UserException(e);
			}
		}
		final CacheKey key = cache.newCacheKey(context.getContextId(), imapLogin);
		final int identifier;
		final Integer tmp = (Integer) cache.get(key);
		if (null == tmp) {
			try {
				identifier = getUserStorage().resolveIMAPLogin(imapLogin, context);
			} catch (LdapException e) {
				throw new UserException(e);
			}
			try {
				cache.put(key, Integer.valueOf(identifier));
			} catch (CacheException e) {
				throw new UserException(UserException.Code.CACHE_PROBLEM, e);
			}
		} else {
			identifier = tmp.intValue();
		}
		return identifier;
	}

	/**
	 * Creates a the instance implementing the user storage interface with
	 * persistent storing.
	 * 
	 * @return an instance implementing the user storage interface.
	 * @throws LdapException
	 *             if the instance can't be created.
	 */
	private UserStorage getUserStorage() throws LdapException {
		if (null == delegate) {
			final String className = LdapUtility.findProperty(Names.USERSTORAGE_IMPL);
			final Class<? extends UserStorage> clazz = LdapUtility.getImplementation(className, UserStorage.class);
			delegate = LdapUtility.getInstance(clazz);
		}
		return delegate;
	}

	@Override
	protected void startInternal() throws UserException {
		final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
		if (null != reg && !reg.registerListener(cacheAvailabilityListener)) {
			LOG.error("Cache availability listener could not be registered", new Throwable());
		}
	}

	@Override
	protected void stopInternal() throws UserException {
		final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
		if (null != reg) {
			reg.unregisterListener(cacheAvailabilityListener);
		}
		try {
			ServerServiceRegistry.getInstance().getService(CacheService.class).freeCache("User");
		} catch (final CacheException e) {
			throw new UserException(e);
		}
	}
}
