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

package com.openexchange.groupware.ldap;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.log.LogFactory;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * This class implements the user storage using a cache to store once read
 * objects.
 */
public class CachingUserStorage extends UserStorage {

    /**
     * Logger.
     */
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(CachingUserStorage.class));

    private static final String REGION_NAME = "User";

    /**
     * Proxy attribute for the object implementing the persistent methods.
     */
    private final UserStorage delegate;

    /**
     * Default constructor.
     */
    public CachingUserStorage(final UserStorage delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public User getUser(final int uid, final Context context) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getUser(uid, context);
        }
        final Cache cache = cacheService.getCache(REGION_NAME);
        final Object object = cache.get(cacheService.newCacheKey(context.getContextId(), uid));
        if (object instanceof User) {
            return (User) object;
        }
        final User user = delegate.getUser(uid, context);
        cache.put(cacheService.newCacheKey(context.getContextId(), uid), user, false);
        return user;
    }

    @Override
    public int createUser(final Connection con, final Context context, final User user) throws OXException {
        return delegate.createUser(con, context, user);
    }

    @Override
    public int createUser(final Context context, final User user) throws OXException {
        return delegate.createUser(context, user);
    }

    @Override
    public User getUser(final Context ctx, final int userId, final Connection con) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getUser(ctx, userId, con);
        }
        final User user = delegate.getUser(ctx, userId, con);
        cacheService.getCache(REGION_NAME).put(cacheService.newCacheKey(ctx.getContextId(), user.getId()), user, false);
        return user;
    }

    @Override
    public User[] getUser(final Context ctx) throws OXException {
        return getUser(ctx, listAllUser(ctx));
    }

    @Override
    public User[] getUser(final Context ctx, final int[] userIds) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getUser(ctx, userIds);
        }
        final Cache cache = cacheService.getCache(REGION_NAME);
        final Map<Integer, User> map = new HashMap<Integer, User>(userIds.length, 1);
        final List<Integer> toLoad = new ArrayList<Integer>(userIds.length);
        final int contextId = ctx.getContextId();
        for (final int userId : userIds) {
            final Object object = cache.get(cacheService.newCacheKey(contextId, userId));
            if (object instanceof User) {
                map.put(I(userId), (User) object);
            } else {
                toLoad.add(I(userId));
            }
        }
        final User[] loaded = delegate.getUser(ctx, I2i(toLoad));
        for (final User user : loaded) {
            cache.put(cacheService.newCacheKey(contextId, user.getId()), user, false);
            map.put(I(user.getId()), user);
        }
        final List<User> retval = new ArrayList<User>(userIds.length);
        for (final int userId : userIds) {
            retval.add(map.get(I(userId)));
        }
        return retval.toArray(new User[retval.size()]);
    }

    @Override
    public void updateUserInternal(final User user, final Context context) throws OXException {
        delegate.updateUser(user, context);
        invalidateUser(context, user.getId());
    }

    @Override
    public String getUserAttribute(final String name, final int userId, final Context context) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getUserAttribute(name, userId, context);
        }
        final Set<String> set = getUser(userId, context).getAttributes().get(new StringBuilder("attr_").append(name).toString());
        return null == set ? null : (set.isEmpty() ? null : set.iterator().next());
    }

    @Override
    public void setUserAttribute(final String name, final String value, final int userId, final Context context) throws OXException {
        delegate.setUserAttribute(name, value, userId, context);
        invalidateUser(context, userId);
    }

    @Override
    public void setAttribute(final String name, final String value, final int userId, final Context context) throws OXException {
        delegate.setAttribute(name, value, userId, context);
        invalidateUser(context, userId);
    }

    @Override
    public int getUserId(final String uid, final Context context) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            return delegate.getUserId(uid, context);
        }
//        try {
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
                    cache.put(key, Integer.valueOf(identifier), false);
                } catch (final OXException e) {
                    throw LdapExceptionCode.CACHE_PROBLEM.create(e, new Object[0]).setPrefix("USR");
                }
            } else {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Cache HIT. Context: " + context.getContextId() + " User: " + uid);
                }
                identifier = tmp.intValue();
            }
            return identifier;
//        }
//        catch (final OXException e) {
//            throw LdapExceptionCode.CACHE_PROBLEM.create(e).setPrefix("USR");
//        }
    }

    @Override
    public int[] listModifiedUser(final Date modifiedSince, final Context context) throws OXException {
        // Caching doesn't make any sense here.
        return delegate.listModifiedUser(modifiedSince, context);
    }

    @Override
    public User searchUser(final String email, final Context context) throws OXException {
        // Caching doesn't make any sense here.
        return delegate.searchUser(email, context);
    }

    @Override
    public User searchUser(final String email, final Context context, boolean considerAliases) throws OXException {
        // Caching doesn't make any sense here.
        return delegate.searchUser(email, context, considerAliases);
    }

    @Override
    public User[] searchUserByMailLogin(final String login, final Context context) throws OXException {
        // Caching doesn't make any sense here.
        return delegate.searchUserByMailLogin(login, context);
    }

    @Override
    public User[] searchUserByName(final String name, final Context context, final int searchType) throws OXException {
        // Caching doesn't make any sense here.
        return delegate.searchUserByName(name, context, searchType);
    }

    @Override
    public int[] listAllUser(final Context ctx) throws OXException {
        return delegate.listAllUser(ctx);
    }

    @Override
    public int[] resolveIMAPLogin(final String imapLogin, final Context context) throws OXException {
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
                    if (null != cache.get(key)) {
                        cache.remove(key);
                    }
                    cache.put(key, identifiers, false);
                } catch (final OXException e) {
                    throw UserExceptionCode.CACHE_PROBLEM.create(e);
                }
            } else {
                identifiers = tmp;
            }
            return identifiers;
        } catch (final OXException e) {
            throw UserExceptionCode.CACHE_PROBLEM.create(e);
        }
    }

    @Override
    public void invalidateUser(final Context ctx, final int userId) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            try {
                final Cache cache = cacheService.getCache(REGION_NAME);
                cache.remove(cache.newCacheKey(ctx.getContextId(), userId));
            } catch (final OXException e) {
                throw UserExceptionCode.CACHE_PROBLEM.create(e);
            }
        }
        try {
            UserConfigurationStorage.getInstance().invalidateCache(userId, ctx);
        } catch (final Exception e) {
            // Ignore
        }
    }

    @Override
    protected void startInternal() {
        // Nothing to start
    }

    @Override
    protected void stopInternal() throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService != null) {
            cacheService.freeCache(REGION_NAME);
        }
    }

    UserStorage getDelegate() {
        return delegate;
    }

}
