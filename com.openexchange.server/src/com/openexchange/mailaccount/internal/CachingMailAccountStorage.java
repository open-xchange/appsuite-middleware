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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import static com.openexchange.mail.utils.ProviderUtility.toSocketAddrString;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.database.DatabaseService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.cache.memory.FolderMap;
import com.openexchange.folderstorage.cache.memory.FolderMapManagement;
import com.openexchange.folderstorage.outlook.OutlookFolderStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPools;

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
     * Initializes a new {@link CachingMailAccountStorage}.
     *
     * @param delegate The database-backed delegate storage
     */
    CachingMailAccountStorage(final RdbMailAccountStorage delegate) {
        super();
        this.delegate = delegate;
    }

    RdbMailAccountStorage getDelegate() {
        return delegate;
    }

    static CacheKey newCacheKey(CacheService cacheService, int id, int userId, int contextId) {
        return cacheService.newCacheKey(contextId, String.valueOf(id), String.valueOf(userId));
    }

    static CacheKey accountsCacheKey(CacheService cacheService, int userId, int contextId) {
        return cacheService.newCacheKey(contextId, String.valueOf(userId));
    }

    @Override
    public void invalidateMailAccount(int id, int userId, int contextId) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            Cache cache = cacheService.getCache(REGION_NAME);
            cache.remove(newCacheKey(cacheService, id, userId, contextId));
            cache.remove(accountsCacheKey(cacheService, userId, contextId));
            cache.invalidateGroup(Integer.toString(contextId));
        }
        final FolderMap folderMap = FolderMapManagement.getInstance().optFor(userId, contextId);
        if (null != folderMap) {
            folderMap.remove(MailFolder.DEFAULT_FOLDER_ID + id, FolderStorage.REAL_TREE_ID);
            folderMap.remove(MailFolder.DEFAULT_FOLDER_ID + id, OutlookFolderStorage.OUTLOOK_TREE_ID);
        }
    }

    @Override
    public void invalidateMailAccounts(int userId, int contextId) throws OXException {
        final CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            final DatabaseService db = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
            final Connection con = db.getWritable(contextId);
            final int[] ids;
            try {
                ids = delegate.getUserMailAccountIDs(userId, contextId, con);
            } finally {
                db.backWritableAfterReading(contextId, con);
            }

            Cache cache = cacheService.getCache(REGION_NAME);
            cache.remove(accountsCacheKey(cacheService, userId, contextId));
            for (final int id : ids) {
                cache.remove(newCacheKey(cacheService, id, userId, contextId));
                cache.invalidateGroup(Integer.toString(contextId));
            }
        }

        FolderMapManagement.getInstance().dropFor(userId, contextId);
    }

    @Override
    public void clearFullNamesForMailAccount(int id, int userId, int contextId) throws OXException {
        delegate.clearFullNamesForMailAccount(id, userId, contextId);
        invalidateMailAccount(id, userId, contextId);
    }

    @Override
    public void clearFullNamesForMailAccount(int id, int[] indexes, int userId, int contextId) throws OXException {
        delegate.clearFullNamesForMailAccount(id, indexes, userId, contextId);
        invalidateMailAccount(id, userId, contextId);
    }

    @Override
    public void deleteMailAccount(int id, Map<String, Object> properties, int userId, int contextId, boolean deletePrimary, Connection con) throws OXException {
        dropSessionParameter(userId, contextId);

        delegate.deleteMailAccount(id, properties, userId, contextId, deletePrimary, con);
        invalidateMailAccount(id, userId, contextId);
    }

    @Override
    public void deleteMailAccount(int id, Map<String, Object> properties, int userId, int contextId, boolean deletePrimary) throws OXException {
        dropSessionParameter(userId, contextId);

        delegate.deleteMailAccount(id, properties, userId, contextId, deletePrimary);
        invalidateMailAccount(id, userId, contextId);
    }

    @Override
    public void deleteMailAccount(int id, Map<String, Object> properties, int userId, int contextId) throws OXException {
        dropSessionParameter(userId, contextId);

        delegate.deleteMailAccount(id, properties, userId, contextId);
        invalidateMailAccount(id, userId, contextId);
    }

    private void dropSessionParameter(final int userId, final int contextId) {
        Task<Void> task = new AbstractTask<Void>() {

            @Override
            public Void call() {
                SessiondService service = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                if (null != service) {
                    for (Session session : service.getSessions(userId, contextId)) {
                        session.setParameter("com.openexchange.mailaccount.unifiedMailEnabled", null);
                    }
                }
                return null;
            }
        };
        ThreadPools.getThreadPool().submit(task);
    }

    @Override
    public MailAccount getDefaultMailAccount(int userId, int contextId) throws OXException {
        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getDefaultMailAccount(userId, contextId);
        }
        Cache cache = cacheService.getCache(REGION_NAME);
        Object object = cache.get(newCacheKey(cacheService, MailAccount.DEFAULT_ID, userId, contextId));
        if (object instanceof MailAccount) {
            return (MailAccount) object;
        }
        MailAccount defaultMailAccount = delegate.getDefaultMailAccount(userId, contextId);
        cache.put(newCacheKey(cacheService, MailAccount.DEFAULT_ID, userId, contextId), defaultMailAccount, false);
        return defaultMailAccount;
    }

    @Override
    public MailAccount getMailAccount(int id, int userId, int contextId) throws OXException {
        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getMailAccount(id, userId, contextId);
        }

        Cache cache = cacheService.getCache(REGION_NAME);
        CacheKey key = newCacheKey(cacheService, id, userId, contextId);
        Object object = cache.get(key);
        if (object instanceof MailAccount) {
            return (MailAccount) object;
        }
        RdbMailAccountStorage d = delegate;
        try {
            MailAccount mailAccount = d.getMailAccount(id, userId, contextId);
            cache.put(key, mailAccount, false);
            return mailAccount;
        } catch (OXException e) {
            if (!MailAccountExceptionCodes.NOT_FOUND.equals(e)) {
                throw e;
            }
            Connection wcon = Database.get(contextId, true);
            try {
                MailAccount mailAccount = d.getMailAccount(id, userId, contextId, wcon);
                cache.put(key, mailAccount, false);
                return mailAccount;
            } finally {
                Database.backAfterReading(contextId, wcon);
            }
        }
    }

    @Override
    public int getByPrimaryAddress(String primaryAddress, int userId, int contextId) throws OXException {
        return delegate.getByPrimaryAddress(primaryAddress, userId, contextId);
    }

    @Override
    public int[] getByHostNames(Collection<String> hostNames, int userId, int contextId) throws OXException {
        return delegate.getByHostNames(hostNames, userId, contextId);
    }

    @Override
    public MailAccount[] getUserMailAccounts(int userId, int contextId, Connection con) throws OXException {
        int[] ids = delegate.getUserMailAccountIDs(userId, contextId, con);
        MailAccount[] accounts = new MailAccount[ids.length];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = getMailAccount0(ids[i], userId, contextId, con);
        }
        return accounts;
    }

    @Override
    public MailAccount getMailAccount(int id, int userId, int contextId, Connection con) throws OXException {
        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getMailAccount(id, userId, contextId, con);
        }

        CacheKey key = newCacheKey(cacheService, id, userId, contextId);
        Cache cache = cacheService.getCache(REGION_NAME);
        Object object = cache.get(key);
        if (object instanceof MailAccount) {
            return (MailAccount) object;
        }

        MailAccount mailAccount = delegate.getMailAccount(id, userId, contextId, con);
        cache.put(key, mailAccount, false);
        return mailAccount;
    }

    private MailAccount getMailAccount0(int id, int userId, int contextId, Connection con) throws OXException {
        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService == null) {
            return delegate.getMailAccount(id, userId, contextId, con);
        }

        Cache cache = cacheService.getCache(REGION_NAME);
        CacheKey key = newCacheKey(cacheService, id, userId, contextId);
        Object object = cache.get(key);
        if (object == null) {
            /*
             * Not contained in cache. Load with specified connection
             */
            MailAccount mailAccount = delegate.getMailAccount(id, userId, contextId, con);
            cache.put(key, mailAccount, false);
        }
        /*
         * Return mail account
         */
        if (object instanceof MailAccount) {
            return (MailAccount) object;
        }
        MailAccount mailAccount = delegate.getMailAccount(id, userId, contextId, con);
        cache.put(key, mailAccount, false);
        return mailAccount;
    }

    @Override
    public MailAccount[] getUserMailAccounts(int userId, int contextId) throws OXException {
        int[] ids;
        {
            CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
            if (cacheService == null) {
                ids = delegate.getUserMailAccountIDs(userId, contextId);
            } else {
                Cache cache = cacheService.getCache(REGION_NAME);
                CacheKey key = accountsCacheKey(cacheService, userId, contextId);
                Object object = cache.get(key);
                if (object instanceof int[]) {
                    ids = (int[]) object;
                } else {
                    ids = delegate.getUserMailAccountIDs(userId, contextId);
                    cache.put(key, ids, false);
                }
            }
        }

        MailAccount[] accounts = new MailAccount[ids.length];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = getMailAccount(ids[i], userId, contextId);
        }
        return accounts;
    }

    @Override
    public MailAccount[] resolveLogin(String login, int contextId) throws OXException {
        int[][] idsAndUsers = resolveFromCache(login, contextId, new FromDelegate() {
            @Override
            public int[][] getFromDelegate(String pattern, int contextId) throws OXException {
                return getDelegate().resolveLogin2IDs(pattern, contextId);
            }
        }, CachedResolveType.LOGIN);
        MailAccount[] accounts = new MailAccount[idsAndUsers.length];
        for (int i = 0; i < accounts.length; i++) {
            int[] idAndUser = idsAndUsers[i];
            accounts[i] = getMailAccount(idAndUser[0], idAndUser[1], contextId);
        }
        return accounts;
    }

    @Override
    public MailAccount[] resolveLogin(String login, String serverUrl, int contextId) throws OXException {
        int[][] idsAndUsers = resolveFromCache(login, contextId, new FromDelegate() {
            @Override
            public int[][] getFromDelegate(String pattern, int contextId) throws OXException {
                return getDelegate().resolveLogin2IDs(pattern, contextId);
            }
        }, CachedResolveType.LOGIN);
        List<MailAccount> l = new ArrayList<MailAccount>(idsAndUsers.length);
        for (int[] idAndUser : idsAndUsers) {
            MailAccount candidate = getMailAccount(idAndUser[0], idAndUser[1], contextId);
            if (serverUrl.equals(toSocketAddrString(candidate.generateMailServerURL(), 143))) {
                l.add(candidate);
            }
        }
        return l.toArray(new MailAccount[l.size()]);
    }

    @Override
    public void updateMailAccount(MailAccountDescription mailAccount, Set<Attribute> attributes, int userId, int contextId, Session session) throws OXException {
        if (null != session) {
            session.setParameter("com.openexchange.mailaccount.unifiedMailEnabled", null);
        }
        dropSessionParameter(userId, contextId);

        Connection con = Database.get(contextId, true);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;
            updateMailAccount(mailAccount, attributes, userId, contextId, session, con, false);
            con.commit();
            rollback = false;
        } catch (SQLException e) {
            throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            Database.back(contextId, true, con);
        }
    }

    @Override
    public void updateMailAccount(MailAccountDescription mailAccount, Set<Attribute> attributes, int userId, int contextId, Session session, Connection con, boolean changePrimary) throws OXException {
        if (null != session) {
            session.setParameter("com.openexchange.mailaccount.unifiedMailEnabled", null);
        }
        dropSessionParameter(userId, contextId);

        delegate.updateMailAccount(mailAccount, attributes, userId, contextId, session, con, changePrimary);
        invalidateMailAccount(mailAccount.getId(), userId, contextId);

        if (null != con) {
            CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
            if (cacheService != null) {
                Cache cache = cacheService.getCache(REGION_NAME);
                CacheKey key = newCacheKey(cacheService, mailAccount.getId(), userId, contextId);
                MailAccount macc = delegate.getMailAccount(mailAccount.getId(), userId, contextId, con);
                cache.put(key, macc, false);
            }
        }
    }

    @Override
    public void updateMailAccount(MailAccountDescription mailAccount, int userId, int contextId, Session session) throws OXException {
        if (null != session) {
            session.setParameter("com.openexchange.mailaccount.unifiedMailEnabled", null);
        }
        dropSessionParameter(userId, contextId);

        MailAccount changedAccount = delegate.updateAndReturnMailAccount(mailAccount, userId, contextId, session);
        invalidateMailAccount(mailAccount.getId(), userId, contextId);

        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (cacheService != null) {
            Cache cache = cacheService.getCache(REGION_NAME);
            CacheKey key = newCacheKey(cacheService, mailAccount.getId(), userId, contextId);
            cache.put(key, changedAccount, false);
        }
    }

    @Override
    public int insertMailAccount(MailAccountDescription mailAccount, int userId, Context ctx, Session session) throws OXException {
        int id = delegate.insertMailAccount(mailAccount, userId, ctx, session);
        invalidateMailAccount(id, userId, ctx.getContextId());
        return id;
    }

    @Override
    public int insertMailAccount(MailAccountDescription mailAccount, int userId, Context ctx, Session session, Connection con) throws OXException {
        int id = delegate.insertMailAccount(mailAccount, userId, ctx, session, con);
        invalidateMailAccount(id, userId, ctx.getContextId());
        return id;
    }

    @Override
    public MailAccount[] resolvePrimaryAddr(String primaryAddress, int contextId) throws OXException {
        int[][] idsAndUsers = resolveFromCache(primaryAddress, contextId, new FromDelegate() {
            @Override
            public int[][] getFromDelegate(String pattern, int contextId) throws OXException {
                return getDelegate().resolvePrimaryAddr2IDs(pattern, contextId);
            }
        }, CachedResolveType.PRIMARY_ADDRESS);
        List<MailAccount> l = new ArrayList<MailAccount>(idsAndUsers.length);
        for (int[] idAndUser : idsAndUsers) {
            l.add(getMailAccount(idAndUser[0], idAndUser[1], contextId));
        }
        return l.toArray(new MailAccount[l.size()]);
    }

    private static interface FromDelegate {
        int[][] getFromDelegate(String pattern, int contextId) throws OXException;
    }

    private static int[][] resolveFromCache(String pattern, int contextId, FromDelegate delegate, CachedResolveType type) throws OXException {
        CacheService cacheService = ServerServiceRegistry.getInstance().getService(CacheService.class);
        if (null == cacheService) {
            return delegate.getFromDelegate(pattern, contextId);
        }
        Cache cache;
        try {
            cache = cacheService.getCache(REGION_NAME);
        } catch (OXException e) {
            cache = null;
        }
        if (null == cache) {
            return delegate.getFromDelegate(pattern, contextId);
        }
        int[][] idsAndUsers;
        CacheKey key = cacheService.newCacheKey(type.ordinal(), pattern);
        int[][] tmp;
        try {
            tmp = (int[][]) cache.getFromGroup(key, Integer.toString(contextId));
        } catch (ClassCastException e) {
            tmp = null;
        }
        if (null == tmp) {
            idsAndUsers = delegate.getFromDelegate(pattern, contextId);
            cache.putInGroup(key, Integer.toString(contextId), idsAndUsers, false);
        } else {
            idsAndUsers = tmp;
        }
        return idsAndUsers;
    }

    @Override
    public MailAccount getTransportAccountForID(int id, int userId, int contextId) throws OXException {
        MailAccount account = getMailAccount(id, userId, contextId);
        if (null == account.getTransportServer()) {
            return getDefaultMailAccount(userId, contextId);
        }
        return account;
    }

    @Override
    public void migratePasswords(String oldSecret, String newSecret, Session session) throws OXException {
        delegate.migratePasswords(oldSecret, newSecret, session);
        invalidateMailAccounts(session.getUserId(), session.getContextId());
    }

    @Override
    public boolean hasAccounts(Session session) throws OXException {
        return delegate.hasAccounts(session);
    }

    @Override
    public void cleanUp(String secret, Session session) throws OXException {
        delegate.cleanUp(secret, session);
    }

    @Override
    public void removeUnrecoverableItems(String secret, Session session) throws OXException {
        delegate.removeUnrecoverableItems(secret, session);
    }

}
