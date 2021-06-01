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

package com.openexchange.contact.storage.rdb.internal.account;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.contact.common.ContactsAccount;
import com.openexchange.contact.common.DefaultContactsAccount;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.contact.storage.ContactsAccountStorage;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;

/**
 * {@link CachingContactsAccountStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class CachingContactsAccountStorage implements ContactsAccountStorage {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CachingContactsAccountStorage.class);
    private static final String REGION_NAME = "ContactsAccount";

    private final RdbContactsAccountStorage delegate;
    private final int contextId;
    private final CacheService cacheService;
    private final Cache cache;

    /**
     * Initialises a new {@link CachingContactsAccountStorage}.
     *
     * @param delegate The underlying persistent account storage
     * @param contextId The context identifier
     * @param cacheService A reference to the cache service
     */
    public CachingContactsAccountStorage(RdbContactsAccountStorage delegate, int contextId, CacheService cacheService) throws OXException {
        super();
        this.delegate = delegate;
        this.contextId = contextId;
        this.cacheService = cacheService;
        this.cache = cacheService.getCache(REGION_NAME);
    }

    @Override
    public int nextId() throws OXException {
        return delegate.nextId();
    }

    @Override
    public void insertAccount(ContactsAccount account) throws OXException {
        delegate.insertAccount(account);
        invalidateAccount(account.getUserId(), -1);
    }

    @Override
    public void updateAccount(ContactsAccount account, long clientTimestamp) throws OXException {
        delegate.updateAccount(account, clientTimestamp);
        invalidateAccount(account.getUserId(), account.getAccountId());
    }

    @Override
    public void deleteAccount(int userId, int accountId, long clientTimestamp) throws OXException {
        delegate.deleteAccount(userId, accountId, clientTimestamp);
        invalidateAccount(userId, accountId);
    }

    @Override
    public ContactsAccount loadAccount(int userId, int accountId) throws OXException {
        if (bypassCache()) {
            return delegate.loadAccount(userId, accountId);
        }
        CacheKey key = getAccountKey(userId, accountId);
        ContactsAccount account = optClonedAccount(cache.get(key));
        if (null != account) {
            return account;
        }
        account = delegate.loadAccount(userId, accountId);
        if (null != account) {
            cache.put(key, clone(account), false);
        }
        return account;
    }

    @Override
    public ContactsAccount[] loadAccounts(int userId, int[] accountIds) throws OXException {
        if (bypassCache()) {
            return delegate.loadAccounts(userId, accountIds);
        }
        List<Integer> accountsToLoad = new ArrayList<Integer>(accountIds.length);
        Map<Integer, ContactsAccount> accounts = new HashMap<>(accountIds.length);
        for (int i = 0; i < accountIds.length; i++) {
            CacheKey key = getAccountKey(userId, accountIds[i]);
            ContactsAccount account = optClonedAccount(cache.get(key));
            if (null == account) {
                accountsToLoad.add(I(accountIds[i]));
            } else {
                accounts.put(I(accountIds[i]), account);
            }
        }
        if (0 < accountsToLoad.size()) {
            for (ContactsAccount account : delegate.loadAccounts(userId, I2i(accountsToLoad))) {
                if (null == account) {
                    continue;
                }
                cache.put(getAccountKey(userId, account.getAccountId()), clone(account), false);
                accounts.put(I(account.getAccountId()), account);
            }
        }
        ContactsAccount[] retval = new ContactsAccount[accountIds.length];
        for (int i = 0; i < accountIds.length; i++) {
            retval[i] = accounts.get(I(accountIds[i]));
        }
        return retval;
    }

    @Override
    public List<ContactsAccount> loadAccounts(int userId) throws OXException {
        if (bypassCache()) {
            return delegate.loadAccounts(userId);
        }
        /*
         * try and get accounts via cached account id list for user
         */
        CacheKey accountIdsKey = getAccountIdsKey(userId);
        int[] accountIds = optClonedAccountIds(cache.get(accountIdsKey));
        if (null != accountIds) {
            List<ContactsAccount> accounts = new ArrayList<ContactsAccount>(accountIds.length);
            for (ContactsAccount account : loadAccounts(userId, accountIds)) {
                if (null == account) {
                    /*
                     * stale reference in cached user's account list, invalidate & try again
                     */
                    LOG.warn("Detected stale reference in account list for user {} in context {}, invalidating cache.", I(userId), I(contextId));
                    cache.remove(accountIdsKey);
                    return loadAccounts(userId);
                }
                accounts.add(account);
            }
            return accounts;
        }
        /*
         * get account list from storage & put into cache
         */
        List<ContactsAccount> accounts = delegate.loadAccounts(userId);
        accountIds = new int[accounts.size()];
        for (int i = 0; i < accounts.size(); i++) {
            ContactsAccount account = accounts.get(i);
            accountIds[i] = account.getAccountId();
            cache.put(getAccountKey(userId, account.getAccountId()), clone(account), false);
        }
        cache.put(accountIdsKey, accountIds, false);
        return accounts;
    }

    @Override
    public List<ContactsAccount> loadAccounts(int userId, String... providerIds) throws OXException {
        return delegate.loadAccounts(userId, providerIds);
    }

    @Override
    public void invalidateAccount(int userId, int accountId) throws OXException {
        if (-1 == accountId) {
            cache.remove(getAccountIdsKey(userId));
        } else {
            cache.remove(Arrays.asList(new Serializable[] { getAccountIdsKey(userId), getAccountKey(userId, accountId) }));
        }
    }

    /////////////////////////////////// HELPERS //////////////////////////////

    /**
     * Get the account's key
     *
     * @param userId The user identifier
     * @return the cache key
     */
    private CacheKey getAccountIdsKey(int userId) {
        return cacheService.newCacheKey(contextId, userId);
    }

    /**
     * Gets the account cache key
     *
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return The {@link CacheKey}
     */
    private CacheKey getAccountKey(int userId, int accountId) {
        String[] keys = new String[] { String.valueOf(userId), String.valueOf(accountId) };
        return cacheService.newCacheKey(contextId, keys);
    }

    /**
     * Optionally clone the specified cached account
     *
     * @param cachedAccount The cached account to clone
     * @return The cloned account or <code>null</code> if the cached account is <code>null</code>
     * @throws OXException if an error is occurred
     */
    private ContactsAccount optClonedAccount(Object cachedAccount) throws OXException {
        return null != cachedAccount && ContactsAccount.class.isInstance(cachedAccount) ? clone((ContactsAccount) cachedAccount) : null;
    }

    /**
     * Optionally clones the specified account identifiers
     *
     * @param cachedAccountIds The cached account identifiers
     * @return The cloned account ids {@link List} or <code>null</code> if the cached accounts is <code>null</code>
     */
    private int[] optClonedAccountIds(Object cachedAccountIds) {
        return null != cachedAccountIds && int[].class.isInstance(cachedAccountIds) ? clone((int[]) cachedAccountIds) : null;
    }

    /**
     * Clones the specified account
     *
     * @param account The account to clone
     * @return The cloned account
     * @throws OXException if an error is occurred
     */
    private ContactsAccount clone(ContactsAccount account) throws OXException {
        try {
            JSONObject internalConfig = null == account.getInternalConfiguration() ? null : new JSONObject(account.getInternalConfiguration().toString());
            JSONObject userConfig = null == account.getUserConfiguration() ? null : new JSONObject(account.getUserConfiguration().toString());
            return new DefaultContactsAccount(account.getProviderId(), account.getAccountId(), account.getUserId(), internalConfig, userConfig, account.getLastModified());
        } catch (JSONException e) {
            throw ContactsProviderExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Clones the specified array of account ids
     *
     * @param accountIds The account ids
     * @return The cloned List
     */
    private static int[] clone(int[] accountIds) {
        return accountIds.clone();
    }

    /**
     * Checks whether the delegates policy is set to transaction
     *
     * @return <code>true</code> if the cache should be bypassed, i.e. if the delegate is not in transaction mode, <code>false</code> otherwise
     */
    private boolean bypassCache() {
        return DBTransactionPolicy.NO_TRANSACTIONS.equals(delegate.getTransactionPolicy());
    }
}
