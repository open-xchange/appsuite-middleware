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
	private final UserStorage delegate;

	/**
	 * Lock for the cache.
	 */
	private final Lock cacheLock;

	/**
	 * Default constructor.
	 */
	public CachingUserStorage(final UserStorage delegate) {
		super();
		this.delegate = delegate;
		cacheLock = new ReentrantLock(true);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public User getUser(final int uid, final Context context) throws LdapException {
       final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getUser(uid, context);
        }
		final OXObjectFactory<User> factory = new OXObjectFactory<User>() {
			public Serializable getKey() {
				return cacheService.newCacheKey(context.getContextId(), uid);
			}
			public User load() throws LdapException {
				return delegate.getUser(uid, context);
			}
			public Lock getCacheLock() {
				return cacheLock;
			}
		};
        try {
            return new UserReloader(factory, REGION_NAME);
        } catch (final AbstractOXException e) {
            if (e instanceof LdapException) {
                throw (LdapException) e;
            }
            throw new LdapException(e);
        }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateUser(final User user, final Context context) throws LdapException {
	    delegate.updateUser(user, context);
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
			return delegate.getUserId(uid, context);
		}
		try {
			final Cache cache = cacheService.getCache(REGION_NAME);
			final CacheKey key = cache.newCacheKey(context.getContextId(), uid);
			int identifier = -1;
			Integer tmp;
			try {
				tmp = (Integer) cache.get(key);
			} catch (final ClassCastException e) {
				tmp = null;
			}
			if (null == tmp) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("Cache MISS. Context: " + context.getContextId() + " User: " + uid);
				}
				identifier = delegate.getUserId(uid, context);
				try {
					cache.put(key, Integer.valueOf(identifier));
				} catch (final CacheException e) {
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
		return delegate.listModifiedUser(modifiedSince, context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public User searchUser(final String email, final Context context) throws LdapException {
        // Caching doesn't make any sense here.
		return delegate.searchUser(email, context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] listAllUser(final Context context) throws UserException {
	    return delegate.listAllUser(context);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[] resolveIMAPLogin(final String imapLogin, final Context context) throws UserException {
		final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
		if (null == cacheService) {
		    return delegate.resolveIMAPLogin(imapLogin, context);
		}
		try {
			final Cache cache = cacheService.getCache(REGION_NAME);
			final CacheKey key = cache.newCacheKey(context.getContextId(), new StringBuilder(imapLogin.length() + 1)
					.append('~').append(imapLogin).toString());
			final int[] identifiers;
			int[] tmp;
			try {
				tmp = (int[]) cache.get(key);
			} catch (final ClassCastException e) {
				tmp = null;
			}
			if (null == tmp) {
			    identifiers = delegate.resolveIMAPLogin(imapLogin, context);
				try {
					cache.put(key, identifiers);
				} catch (final CacheException e) {
					throw new UserException(UserException.Code.CACHE_PROBLEM, e);
				}
			} else {
				identifiers = tmp;
			}
			return identifiers;
		} catch (final CacheException e) {
			throw new UserException(UserException.Code.CACHE_PROBLEM, e);
		}
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public void invalidateUser(final Context ctx, final int userId) throws UserException {
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

	@Override
	protected void startInternal() throws UserException {
	}

	@Override
	protected void stopInternal() throws UserException {
        final CacheService cacheService = ServerServiceRegistry.getInstance()
            .getService(CacheService.class);
        if (cacheService != null) {
    		try {
    			cacheService.freeCache(REGION_NAME);
    		} catch (final CacheException e) {
    			throw new UserException(e);
    		}
        }
	}
}
