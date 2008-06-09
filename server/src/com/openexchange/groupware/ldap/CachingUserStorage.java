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

	private static final String REGION_NAME = "User";

	/**
	 * Proxy attribute for the object implementing the persistent methods.
	 */
	private UserStorage delegate;

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
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getUser(final int uid, final Context context) throws LdapException {
		final OXObjectFactory<User> factory = new OXObjectFactory<User>() {
			public Serializable getKey() {
				return ServerServiceRegistry.getInstance().getService(CacheService.class).newCacheKey(
						context.getContextId(), uid);
			}

			public User load() throws LdapException {
				return getUserStorage().getUser(uid, context);
			}

			public Lock getCacheLock() {
				return cacheLock;
			}
		};
		try {
			return CacheProxy.getCacheProxy(factory, REGION_NAME, User.class);
		} catch (final IllegalArgumentException e) {
			/*
			 * Should not occur
			 */
			LOG.error(e.getMessage(), e);
			return getUserStorage().getUser(uid, context);
		} catch (final LdapException e) {
			throw e;
		} catch (final AbstractOXException e) {
			throw new LdapException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateUser(final User user, final Context context) throws LdapException {
		getUserStorage().updateUser(user, context);
		try {
            invalidateUser(context, user.getId());
        } catch (final UserException e) {
            throw new LdapException(e);
        }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getUserId(final String uid, final Context context) throws LdapException {
		final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
		if (null == cacheService) {
			return getUserStorage().getUserId(uid, context);
		}
		try {
			final Cache cache = cacheService.getCache(REGION_NAME);
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
		} catch (final CacheException e) {
			throw new LdapException(EnumComponent.USER, Code.CACHE_PROBLEM, e);
		}
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
        // Caching doesn't make any sense here.
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
		final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
		if (null == cacheService) {
			try {
				return getUserStorage().resolveIMAPLogin(imapLogin, context);
			} catch (LdapException e) {
				throw new UserException(e);
			}
		}
		try {
			final Cache cache = cacheService.getCache(REGION_NAME);
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
		} catch (final CacheException e) {
			throw new UserException(UserException.Code.CACHE_PROBLEM, e);
		}
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public void invalidateUser(Context ctx, int userId) throws UserException {
        final CacheService cacheService = ServerServiceRegistry.getInstance()
            .getService(CacheService.class);
        if (null != cacheService) {
            try {
                final Cache cache = cacheService.getCache(REGION_NAME);
                cache.remove(cache.newCacheKey(ctx.getContextId(), userId));
            } catch (final CacheException e) {
                throw new UserException(UserException.Code.CACHE_PROBLEM, e);
            }
        }
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
	}

	@Override
	protected void stopInternal() throws UserException {
		try {
			ServerServiceRegistry.getInstance().getService(CacheService.class).freeCache(REGION_NAME);
		} catch (final CacheException e) {
			throw new UserException(e);
		}
	}
}
