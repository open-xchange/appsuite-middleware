/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.messaging.generic.internal;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.generic.services.MessagingGenericServiceRegistry;
import com.openexchange.oauth.OAuthAccountDeleteListener;
import com.openexchange.session.Session;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;

/**
 * {@link CachingMessagingAccountStorage} - The messaging account manager backed by {@link CacheService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class CachingMessagingAccountStorage implements MessagingAccountStorage, OAuthAccountDeleteListener {

    private static final CachingMessagingAccountStorage INSTANCE = new CachingMessagingAccountStorage();

    private static final String REGION_NAME = "MessagingAccount";

    /**
     * Gets the cache region name.
     *
     * @return The cache region name
     */
    public static String getRegionName() {
        return REGION_NAME;
    }

    /**
     * Gets the cache-backed instance.
     *
     * @return The cache-backed instance
     */
    public static CachingMessagingAccountStorage getInstance() {
        return INSTANCE;
    }

    /**
     * Generates a new cache key.
     *
     * @return The new cache key
     */
    static CacheKey newCacheKey(CacheService cacheService, String serviceId, int id, int userId, int contextId) {
        return cacheService.newCacheKey(contextId, serviceId, Integer.toString(id), Integer.toString(userId));
    }

    private static void invalidateMessagingAccount(final String serviceId, final int id, final int userId, final int contextId) throws OXException {
        final CacheService cacheService = MessagingGenericServiceRegistry.getService(CacheService.class);
        if (null != cacheService) {
            final Cache cache = cacheService.getCache(REGION_NAME);
            cache.remove(newCacheKey(cacheService, serviceId, id, userId, contextId));
            cache.remove(accountIDsCacheKey(cacheService, serviceId, userId, contextId));
        }
    }

    /**
     * Generates a new cache key.
     *
     * @return The new cache key
     */
    static CacheKey accountIDsCacheKey(CacheService cacheService, String serviceId, int userId, int contextId) {
        return cacheService.newCacheKey(contextId, serviceId, Integer.toString(userId));
    }

    private static void invalidateAccounts(String serviceId, int userId, int contextId) throws OXException {
        final CacheService cacheService = MessagingGenericServiceRegistry.getService(CacheService.class);
        if (null != cacheService) {
            final Cache cache = cacheService.getCache(REGION_NAME);
            cache.remove(accountIDsCacheKey(cacheService, serviceId, userId, contextId));
        }
    }

    /*-
     * ------------------------------ Member section ------------------------------
     */

    /**
     * The database-backed delegate.
     */
    private final RdbMessagingAccountStorage delegatee;

    /**
     * Initializes a new {@link CachingMessagingAccountStorage}.
     */
    private CachingMessagingAccountStorage() {
        super();
        delegatee = RdbMessagingAccountStorage.getInstance();
    }

    @Override
    public void onBeforeOAuthAccountDeletion(int id, Map<String, Object> eventProps, int userId, int contextId, Connection con) throws OXException {
        // Nothing to do
    }

    @Override
    public void onAfterOAuthAccountDeletion(int id, Map<String, Object> eventProps, int userId, int contextId, Connection con) throws OXException {
        List<MessagingAccount> accounts = delegatee.getAccounts(userId, contextId, null);
        if (accounts.isEmpty()) {
            return;
        }

        Map<String, List<MessagingAccount>> toDelete = new LinkedHashMap<>(accounts.size());
        for (MessagingAccount messagingAccount : accounts) {
            Object object = messagingAccount.getConfiguration().get("account");
            if (null != object && Integer.toString(id).equals(object.toString())) {
                String serviceId = messagingAccount.getMessagingService().getId();
                List<MessagingAccount> l = toDelete.get(serviceId);
                if (null == l) {
                    l = new LinkedList<>();
                    toDelete.put(serviceId, l);
                }
                l.add(messagingAccount);
            }
        }

        if (false == toDelete.isEmpty()) {
            CacheService cacheService = MessagingGenericServiceRegistry.getService(CacheService.class);
            Cache cache = null;
            if (cacheService != null) {
                cache = cacheService.getCache(REGION_NAME);
            }

            for (Map.Entry<String, List<MessagingAccount>> deleteEntry : toDelete.entrySet()) {
                String serviceId = deleteEntry.getKey();
                if (null != cache) {
                    cache.remove(accountIDsCacheKey(cacheService, serviceId, userId, contextId));
                }

                for (MessagingAccount deleteMe : deleteEntry.getValue()) {
                    if (null != cache) {
                        cache.remove(newCacheKey(cacheService, serviceId, deleteMe.getId(), userId, contextId));
                    }
                    delegatee.deleteAccounts(serviceId, new MessagingAccount[] { deleteMe }, new int[] { 0 }, userId, contextId, null, con);
                }
            }
        }
    }

    /**
     * Invalidates specified account.
     *
     * @param serviceId The service identifier
     * @param id The account identifier
     * @param user The user identifier
     * @param contextId The context identifier
     * @throws OXException If invalidation fails
     */
    public void invalidate(final String serviceId, final int id, final int user, final int contextId) throws OXException {
        invalidateMessagingAccount(serviceId, id, user, contextId);
    }

    @Override
    public int addAccount(final String serviceId, final MessagingAccount account, final Session session, final Modifier modifier) throws OXException {
        int identifier = delegatee.addAccount(serviceId, account, session, modifier);
        invalidateAccounts(serviceId, session.getUserId(), session.getContextId());
        return identifier;
    }

    @Override
    public void deleteAccount(final String serviceId, final MessagingAccount account, final Session session, final Modifier modifier) throws OXException {
        delegatee.deleteAccount(serviceId, account, session, modifier);
        invalidateMessagingAccount(serviceId, account.getId(), session.getUserId(), session.getContextId());
    }

    @Override
    public MessagingAccount getAccount(final String serviceId, final int id, final Session session, final Modifier modifier) throws OXException {
        CacheService cacheService = MessagingGenericServiceRegistry.getService(CacheService.class);
        if (cacheService == null) {
            return delegatee.getAccount(serviceId, id, session, modifier);
        }
        Cache cache = cacheService.getCache(REGION_NAME);
        CacheKey cacheKey = newCacheKey(cacheService, serviceId, id, session.getUserId(), session.getContextId());
        Object object = cache.get(cacheKey);
        if (object instanceof MessagingAccount) {
            return (MessagingAccount) object;
        }
        MessagingAccount messagingAccount = delegatee.getAccount(serviceId, id, session, modifier);
        cache.put(cacheKey, messagingAccount, false);
        return messagingAccount;
    }

    @Override
    public List<MessagingAccount> getAccounts(final String serviceId, final Session session, final Modifier modifier) throws OXException {
        TIntArrayList ids;
        {
            CacheService cacheService = MessagingGenericServiceRegistry.getService(CacheService.class);
            if (cacheService == null) {
                ids = delegatee.getAccountIDs(serviceId, session);
            } else {
                Cache cache = cacheService.getCache(REGION_NAME);
                CacheKey accountsKey = accountIDsCacheKey(cacheService, serviceId, session.getUserId(), session.getContextId());
                Object object = cache.get(accountsKey);
                if (object instanceof TIntArrayList) {
                    ids = (TIntArrayList) object;
                } else {
                    ids = delegatee.getAccountIDs(serviceId, session);
                    cache.put(accountsKey, ids, false);
                }
            }
        }

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        final List<MessagingAccount> accounts = new ArrayList<MessagingAccount>(ids.size());
        class AdderProcedure implements TIntProcedure {

            OXException me;

            @Override
            public boolean execute(final int id) {
                try {
                    accounts.add(getAccount(serviceId, id, session, modifier));
                    return true;
                } catch (OXException e) {
                    me = e;
                    return false;
                }
            }

        }
        final AdderProcedure ap = new AdderProcedure();
        if (!ids.forEach(ap) && null != ap.me) {
            throw ap.me;
        }
        return accounts;
    }

    @Override
    public void updateAccount(final String serviceId, final MessagingAccount account, final Session session, final Modifier modifier) throws OXException {
        delegatee.updateAccount(serviceId, account, session, modifier);
        invalidateMessagingAccount(serviceId, account.getId(), session.getUserId(), session.getContextId());
    }

    public String checkSecretCanDecryptStrings(final MessagingService parentService, final Session session, final String secret) throws OXException {
        return delegatee.checkSecretCanDecryptStrings(parentService, session, secret);
    }

    public void migrateToNewSecret(final MessagingService parentService, final String oldSecret, final String newSecret, final Session session) throws OXException {
        delegatee.migrateToNewSecret(parentService, oldSecret, newSecret, session);
    }

    public boolean hasAccount(final MessagingService service, final Session session) throws OXException {
        return delegatee.hasAccount(service, session);
    }

    public void cleanUp(MessagingService service, String secret, Session session) throws OXException {
        delegatee.cleanUp(service, secret, session);
    }

    public void removeUnrecoverableItems(MessagingService service, String secret, Session session) throws OXException {
        delegatee.removeUnrecoverableItems(service, secret, session);
    }

}
