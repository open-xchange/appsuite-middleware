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

import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import com.openexchange.cache.CacheKey;
import com.openexchange.cache.Configuration;
import com.openexchange.cache.dynamic.impl.CacheProxy;
import com.openexchange.cache.dynamic.impl.OXObjectFactory;
import com.openexchange.configuration.ConfigurationException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.LdapException.Code;

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
     * Reference to the context.
     */
    private final transient Context context;

    /**
     * Cache.
     */
    private static final JCS CACHE;

    /**
     * Lock for the cache.
     */
    private static final Lock CACHE_LOCK;

    /**
     * Default constructor.
     * @param context Context.
     */
    public CachingUserStorage(final Context context) {
        super();
        this.context = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUser(final int uid) throws LdapException {
        final OXObjectFactory<User> factory = new OXObjectFactory<User>() {
            public Object getKey() {
                return new CacheKey(context, uid);
            }
            public User load() throws LdapException {
                return getUserStorage().getUser(uid);
            }
            public Lock getCacheLock() {
                return CACHE_LOCK;
            }
        };
        if (null == CACHE.get(factory.getKey())) {
            getUserStorage().getUser(uid);
        }
        return CacheProxy.getCacheProxy(factory, CACHE, User.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(final User user) throws LdapException {
        getUserStorage().updateUser(user);
        try {
            CACHE.remove(new CacheKey(context, user.getId()));
        } catch (CacheException e) {
            throw new LdapException(Component.USER, Code.CACHE_PROBLEM, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getUserId(final String uid) throws LdapException {
        final CacheKey key = new CacheKey(context, uid);
        int identifier = -1;
        final Integer tmp = (Integer) CACHE.get(key);
        if (null == tmp) {
            if (LOG.isTraceEnabled()) {
				LOG.trace("Cache MISS. Context: " + context.getContextId() + " User: " + uid);
			}
            identifier = getUserStorage().getUserId(uid);
            try {
                CACHE.put(key, Integer.valueOf(identifier));
            } catch (CacheException e) {
                throw new LdapException(Component.USER, Code.CACHE_PROBLEM, e);
            }
        } else {
            if (LOG.isTraceEnabled()) {
				LOG.trace("Cache HIT. Context: " + context.getContextId() + " User: " + uid);
			}
            identifier = tmp;
        }
        return identifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] listModifiedUser(final Date modifiedSince)
        throws LdapException {
        // Caching doesn't make any sense here.
        return getUserStorage().listModifiedUser(modifiedSince);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User searchUser(final String email) throws LdapException {
        return getUserStorage().searchUser(email);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] listAllUser() throws UserException {
        try {
            return getUserStorage().listAllUser();
        } catch (LdapException e) {
            throw new UserException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int resolveIMAPLogin(final String imapLogin) throws UserException {
        final CacheKey key = new CacheKey(context, imapLogin);
        final int identifier;
        final Integer tmp = (Integer) CACHE.get(key);
        if (null == tmp) {
            try {
                identifier = getUserStorage().resolveIMAPLogin(imapLogin);
            } catch (LdapException e) {
                throw new UserException(e);
            }
            try {
                CACHE.put(key, identifier);
            } catch (CacheException e) {
                throw new UserException(UserException.Code.CACHE_PROBLEM, e);
            }
        } else {
            identifier = tmp;
        }
        return identifier;
    }

    /**
     * Creates a the instance implementing the user storage interface with
     * persitent storing.
     * @return an instance implementing the user storage interface.
     * @throws LdapException if the instance can't be created.
     */
    private UserStorage getUserStorage() throws LdapException {
        if (null == delegate) {
            final String className = LdapUtility.findProperty(Names.
                USERSTORAGE_IMPL);
            final Class< ? extends UserStorage> clazz = LdapUtility
                .getImplementation(className, UserStorage.class);
            delegate = LdapUtility.getInstance(clazz, context);
        }
        return delegate;
    }

    static {
        try {
            Configuration.load();
            CACHE = JCS.getInstance("User");
        } catch (CacheException e) {
            throw new RuntimeException("Cannot create user cache.", e);
        } catch (ConfigurationException e) {
        	 throw new RuntimeException("Cannot load cache configuration.", e);
		}
        CACHE_LOCK = new ReentrantLock(true);
    }
}
