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

package com.openexchange.mailaccount.internal;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.cache.dynamic.impl.OXObjectFactory;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheException;
import com.openexchange.caching.CacheService;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link CachingMailAccountStorage} - The caching implementation of mail account storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
final class CachingMailAccountStorage implements MailAccountStorageService {

    private static final String REGION_NAME = "MailAccount";

    /**
     * Proxy attribute for the object implementing the persistent methods.
     */
    private final MailAccountStorageService delegate;

    /**
     * Lock for the cache.
     */
    private final Lock cacheLock;

    /**
     * Initializes a new {@link CachingMailAccountStorage}.
     * 
     * @param delegate
     */
    CachingMailAccountStorage(final MailAccountStorageService delegate) {
        super();
        this.delegate = delegate;
        cacheLock = new ReentrantLock(true);
    }

    private void invalidateUser(final int id, final int user, final int cid) throws MailAccountException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            try {
                final Cache cache = cacheService.getCache(REGION_NAME);
                cache.remove(cache.newCacheKey(cid, id ^ user));
            } catch (final CacheException e) {
                throw new MailAccountException(e);
            }
        }
    }

    public void deleteMailAccount(final int id, final int user, final int cid, final boolean deletePrimary) throws MailAccountException {
        delegate.deleteMailAccount(id, user, cid, deletePrimary);
        invalidateUser(id, user, cid);
    }

    public void deleteMailAccount(final int id, final int user, final int cid) throws MailAccountException {
        delegate.deleteMailAccount(id, user, cid);
        invalidateUser(id, user, cid);
    }

    public MailAccount getDefaultMailAccount(final int user, final int cid) throws MailAccountException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getDefaultMailAccount(user, cid);
        }
        final OXObjectFactory<MailAccount> factory = new OXObjectFactory<MailAccount>() {

            public Serializable getKey() {
                return cacheService.newCacheKey(cid, MailAccount.DEFAULT_ID ^ user);
            }

            public MailAccount load() throws MailAccountException {
                return delegate.getDefaultMailAccount(user, cid);
            }

            public Lock getCacheLock() {
                return cacheLock;
            }
        };
        try {
            return new MailAccountReloader(factory, REGION_NAME);
        } catch (final AbstractOXException e) {
            if (e instanceof MailAccountException) {
                throw (MailAccountException) e;
            }
            throw new MailAccountException(e);
        }
    }

    public MailAccount getMailAccount(final int id, final int user, final int cid) throws MailAccountException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getMailAccount(id, user, cid);
        }
        final OXObjectFactory<MailAccount> factory = new OXObjectFactory<MailAccount>() {

            public Serializable getKey() {
                return cacheService.newCacheKey(cid, Integer.valueOf(id), Integer.valueOf(user));
            }

            public MailAccount load() throws MailAccountException {
                return delegate.getMailAccount(id, user, cid);
            }

            public Lock getCacheLock() {
                return cacheLock;
            }
        };
        try {
            return new MailAccountReloader(factory, REGION_NAME);
        } catch (final AbstractOXException e) {
            if (e instanceof MailAccountException) {
                throw (MailAccountException) e;
            }
            throw new MailAccountException(e);
        }
    }

    public int getByPrimaryAddress(final String primaryAddress, final int user, final int cid) throws MailAccountException {
        return delegate.getByPrimaryAddress(primaryAddress, user, cid);
    }

    public MailAccount[] getUserMailAccounts(final int user, final int cid) throws MailAccountException {
        return delegate.getUserMailAccounts(user, cid);
    }

    public MailAccount[] resolveLogin(final String login, final int cid) throws MailAccountException {
        return delegate.resolveLogin(login, cid);
    }

    public MailAccount[] resolveLogin(final String login, final InetSocketAddress server, final int cid) throws MailAccountException {
        return delegate.resolveLogin(login, server, cid);
    }

    public void updateMailAccount(final MailAccountDescription mailAccount, final Set<Attribute> attributes, final int user, final int cid, final String sessionPassword) throws MailAccountException {
        delegate.updateMailAccount(mailAccount, attributes, user, cid, sessionPassword);
        invalidateUser(mailAccount.getId(), user, cid);
    }

    public void updateMailAccount(final MailAccountDescription mailAccount, final Set<Attribute> attributes, final int user, final int cid, final String sessionPassword, final Connection con, final boolean changePrimary) throws MailAccountException {
        delegate.updateMailAccount(mailAccount, attributes, user, cid, sessionPassword, con, changePrimary);
        invalidateUser(mailAccount.getId(), user, cid);
    }

    public void updateMailAccount(final MailAccountDescription mailAccount, final int user, final int cid, final String sessionPassword) throws MailAccountException {
        delegate.updateMailAccount(mailAccount, user, cid, sessionPassword);
        invalidateUser(mailAccount.getId(), user, cid);
    }

    public int insertMailAccount(final MailAccountDescription mailAccount, final int user, final Context ctx, final String sessionPassword) throws MailAccountException {
        return delegate.insertMailAccount(mailAccount, user, ctx, sessionPassword);
    }

    public int insertMailAccount(final MailAccountDescription mailAccount, final int user, final Context ctx, final String sessionPassword, final Connection con) throws MailAccountException {
        return delegate.insertMailAccount(mailAccount, user, ctx, sessionPassword, con);
    }

    public MailAccount[] resolvePrimaryAddr(final String primaryAddress, final InetSocketAddress server, final int cid) throws MailAccountException {
        return delegate.resolvePrimaryAddr(primaryAddress, server, cid);
    }

}
