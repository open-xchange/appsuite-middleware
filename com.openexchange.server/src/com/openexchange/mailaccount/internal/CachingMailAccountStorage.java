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

package com.openexchange.mailaccount.internal;

import static com.openexchange.mail.utils.ProviderUtility.toSocketAddr;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.dynamic.OXObjectFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.cache.memory.FolderMap;
import com.openexchange.folderstorage.cache.memory.FolderMapManagement;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

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
    private final RdbMailAccountStorage delegate;

    /**
     * Lock for the cache.
     */
    private final Lock cacheLock;

    /**
     * Initializes a new {@link CachingMailAccountStorage}.
     *
     * @param delegate The database-backed delegate storage
     */
    CachingMailAccountStorage(final RdbMailAccountStorage delegate) {
        super();
        this.delegate = delegate;
        cacheLock = new ReentrantLock(true);
    }

    RdbMailAccountStorage getDelegate() {
        return delegate;
    }

    static CacheKey newCacheKey(final CacheService cacheService, final int id, final int user, final int cid) {
        return cacheService.newCacheKey(cid, Integer.valueOf(id), Integer.valueOf(user));
    }

    @Override
    public void invalidateMailAccount(final int id, final int user, final int cid) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            final Cache cache = cacheService.getCache(REGION_NAME);
            cache.remove(newCacheKey(cacheService, id, user, cid));
            cache.invalidateGroup(Integer.toString(cid));
        }
        FolderMapManagement.getInstance().dropFor(user, cid);
    }

    @Override
    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int user, final int cid, final boolean deletePrimary, final Connection con) throws OXException {
        delegate.deleteMailAccount(id, properties, user, cid, deletePrimary, con);
        invalidateMailAccount(id, user, cid);
    }

    @Override
    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int user, final int cid, final boolean deletePrimary) throws OXException {
        delegate.deleteMailAccount(id, properties, user, cid, deletePrimary);
        invalidateMailAccount(id, user, cid);
    }

    @Override
    public void deleteMailAccount(final int id, final Map<String, Object> properties, final int user, final int cid) throws OXException {
        delegate.deleteMailAccount(id, properties, user, cid);
        invalidateMailAccount(id, user, cid);
    }

    @Override
    public MailAccount getDefaultMailAccount(final int user, final int cid) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getDefaultMailAccount(user, cid);
        }
        final RdbMailAccountStorage d = delegate;
        final Lock l = cacheLock;
        final OXObjectFactory<MailAccount> factory = new OXObjectFactory<MailAccount>() {

            @Override
            public Serializable getKey() {
                return newCacheKey(cacheService, MailAccount.DEFAULT_ID, user, cid);
            }

            @Override
            public MailAccount load() throws OXException, OXException {
                return d.getDefaultMailAccount(user, cid);
            }

            @Override
            public Lock getCacheLock() {
                return l;
            }
        };
        return new MailAccountReloader(factory, REGION_NAME);
    }

    @Override
    public MailAccount getMailAccount(final int id, final int user, final int cid) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getMailAccount(id, user, cid);
        }
        final RdbMailAccountStorage d = delegate;
        final Lock l = cacheLock;
        final OXObjectFactory<MailAccount> factory = new OXObjectFactory<MailAccount>() {

            @Override
            public Serializable getKey() {
                return newCacheKey(cacheService, id, user, cid);
            }

            @Override
            public MailAccount load() throws OXException, OXException {
                return d.getMailAccount(id, user, cid);
            }

            @Override
            public Lock getCacheLock() {
                return l;
            }
        };
        return new MailAccountReloader(factory, REGION_NAME);
    }

    @Override
    public int getByPrimaryAddress(final String primaryAddress, final int user, final int cid) throws OXException {
        return delegate.getByPrimaryAddress(primaryAddress, user, cid);
    }

    @Override
    public int[] getByHostNames(final Collection<String> hostNames, final int user, final int cid) throws OXException {
        return delegate.getByHostNames(hostNames, user, cid);
    }

    @Override
    public MailAccount[] getUserMailAccounts(final int user, final int cid, final Connection con) throws OXException {
        final int[] ids = delegate.getUserMailAccountIDs(user, cid, con);
        final MailAccount[] accounts = new MailAccount[ids.length];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = getMailAccount0(ids[i], user, cid, con);
        }
        return accounts;
    }

    private MailAccount getMailAccount0(final int id, final int user, final int cid, final Connection con) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getMailAccount(id, user, cid);
        }
        final CacheKey key = newCacheKey(cacheService, id, user, cid);
        final Cache cache = cacheService.getCache(REGION_NAME);
        cacheLock.lock();
        try {
            if (cache.get(key) == null) {
                /*
                 * Not contained in cache. Load with specified connection
                 */
                final MailAccount mailAccount = delegate.getMailAccount(id, user, cid, con);
                cache.put(key, mailAccount);
            }
        } finally {
            cacheLock.unlock();
        }
        /*
         * Return reloading mail account
         */
        final RdbMailAccountStorage d = delegate;
        final Lock l = cacheLock;
        final OXObjectFactory<MailAccount> factory = new OXObjectFactory<MailAccount>() {

            @Override
            public Serializable getKey() {
                return key;
            }

            @Override
            public MailAccount load() throws OXException, OXException {
                return d.getMailAccount(id, user, cid);
            }

            @Override
            public Lock getCacheLock() {
                return l;
            }
        };
        return new MailAccountReloader(factory, REGION_NAME);
    }

    @Override
    public MailAccount[] getUserMailAccounts(final int user, final int cid) throws OXException {
        final int[] ids = delegate.getUserMailAccountIDs(user, cid);
        final MailAccount[] accounts = new MailAccount[ids.length];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = getMailAccount(ids[i], user, cid);
        }
        return accounts;
    }

    @Override
    public MailAccount[] resolveLogin(final String login, final int cid) throws OXException {
        final int[][] idsAndUsers = resolveFromCache(login, cid, new FromDelegate() {
            @Override
            public int[][] getFromDelegate(final String pattern, final int contextId) throws OXException {
                return getDelegate().resolveLogin2IDs(pattern, contextId);
            }
        }, CachedResolveType.LOGIN);
        final MailAccount[] accounts = new MailAccount[idsAndUsers.length];
        for (int i = 0; i < accounts.length; i++) {
            final int[] idAndUser = idsAndUsers[i];
            accounts[i] = getMailAccount(idAndUser[0], idAndUser[1], cid);
        }
        return accounts;
    }

    @Override
    public MailAccount[] resolveLogin(final String login, final InetSocketAddress server, final int cid) throws OXException {
        final int[][] idsAndUsers = resolveFromCache(login, cid, new FromDelegate() {
            @Override
            public int[][] getFromDelegate(final String pattern, final int contextId) throws OXException {
                return getDelegate().resolveLogin2IDs(pattern, contextId);
            }
        }, CachedResolveType.LOGIN);
        final List<MailAccount> l = new ArrayList<MailAccount>(idsAndUsers.length);
        for (final int[] idAndUser : idsAndUsers) {
            final MailAccount candidate = getMailAccount(idAndUser[0], idAndUser[1], cid);
            if (server.equals(toSocketAddr(candidate.generateMailServerURL(), 143))) {
                l.add(candidate);
            }
        }
        return l.toArray(new MailAccount[l.size()]);
    }

    @Override
    public void updateMailAccount(final MailAccountDescription mailAccount, final Set<Attribute> attributes, final int user, final int cid, final Session session) throws OXException {
        delegate.updateMailAccount(mailAccount, attributes, user, cid, session);
        invalidateMailAccount(mailAccount.getId(), user, cid);
    }

    @Override
    public void updateMailAccount(final MailAccountDescription mailAccount, final Set<Attribute> attributes, final int user, final int cid, final Session session, final Connection con, final boolean changePrimary) throws OXException {
        delegate.updateMailAccount(mailAccount, attributes, user, cid, session, con, changePrimary);
        invalidateMailAccount(mailAccount.getId(), user, cid);
    }

    @Override
    public void updateMailAccount(final MailAccountDescription mailAccount, final int user, final int cid, final Session session) throws OXException {
        delegate.updateMailAccount(mailAccount, user, cid, session);
        invalidateMailAccount(mailAccount.getId(), user, cid);
    }

    @Override
    public int insertMailAccount(final MailAccountDescription mailAccount, final int user, final Context ctx, final Session session) throws OXException {
        final int id = delegate.insertMailAccount(mailAccount, user, ctx, session);
        invalidateMailAccount(id, user, ctx.getContextId());
        return id;
    }

    @Override
    public int insertMailAccount(final MailAccountDescription mailAccount, final int user, final Context ctx, final Session session, final Connection con) throws OXException {
        final int id = delegate.insertMailAccount(mailAccount, user, ctx, session, con);
        invalidateMailAccount(id, user, ctx.getContextId());
        return id;
    }

    @Override
    public MailAccount[] resolvePrimaryAddr(final String primaryAddress, final int cid) throws OXException {
        final int[][] idsAndUsers = resolveFromCache(primaryAddress, cid, new FromDelegate() {
            @Override
            public int[][] getFromDelegate(final String pattern, final int contextId) throws OXException {
                return getDelegate().resolvePrimaryAddr2IDs(pattern, contextId);
            }
        }, CachedResolveType.PRIMARY_ADDRESS);
        final List<MailAccount> l = new ArrayList<MailAccount>(idsAndUsers.length);
        for (final int[] idAndUser : idsAndUsers) {
            l.add(getMailAccount(idAndUser[0], idAndUser[1], cid));
        }
        return l.toArray(new MailAccount[l.size()]);
    }

    private static interface FromDelegate {
        int[][] getFromDelegate(String pattern, int cid) throws OXException;
    }

    private static int[][] resolveFromCache(final String pattern, final int cid, final FromDelegate delegate, final CachedResolveType type) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            return delegate.getFromDelegate(pattern, cid);
        }
        Cache cache;
        try {
            cache = cacheService.getCache(REGION_NAME);
        } catch (final OXException e) {
            cache = null;
        }
        if (null == cache) {
            return delegate.getFromDelegate(pattern, cid);
        }
        final int[][] idsAndUsers;
        final CacheKey key = cacheService.newCacheKey(type.ordinal(), pattern);
        int[][] tmp;
        try {
            tmp = (int[][]) cache.getFromGroup(key, Integer.toString(cid));
        } catch (final ClassCastException e) {
            tmp = null;
        }
        if (null == tmp) {
            idsAndUsers = delegate.getFromDelegate(pattern, cid);
            cache.putInGroup(key, Integer.toString(cid), idsAndUsers);
        } else {
            idsAndUsers = tmp;
        }
        return idsAndUsers;
    }

    @Override
    public MailAccount getTransportAccountForID(final int id, final int user, final int cid) throws OXException {
        final MailAccount account = getMailAccount(id, user, cid);
        if (null == account.getTransportServer()) {
            return getDefaultMailAccount(user, cid);
        }
        return account;
    }

    @Override
    public void migratePasswords(final int user, final int cid, final String oldSecret, final String newSecret) throws OXException {
        delegate.migratePasswords(user, cid, oldSecret, newSecret);
        final int[] ids = delegate.getUserMailAccountIDs(user, cid);
        for (final int id : ids) {
            invalidateMailAccount(id, user, cid);
        }
    }

    @Override
    public boolean hasAccounts(final Session session) throws OXException {
        return delegate.hasAccounts(session);
    }

}
