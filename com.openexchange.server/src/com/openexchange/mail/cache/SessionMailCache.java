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

package com.openexchange.mail.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.LoggerFactory;
import com.openexchange.caching.CacheKey;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link SessionMailCache} - Several cacheable data bound to a user session.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SessionMailCache {

    /**
     * (Optionally) Gets the session-bound mail cache.
     *
     * @param session The session whose mail cache shall be returned
     * @param accountId The account identifier
     * @return The session-bound mail cache or <code>null</code>
     */
    public static SessionMailCache optInstance(Session session, int accountId) {
        MailSessionCache mailSessionCache = MailSessionCache.optInstance(session);
        if (null == mailSessionCache) {
            return null;
        }

        String key = MailSessionParameterNames.getParamMailCache();
        SessionMailCache mailCache = null;
        try {
            mailCache = (SessionMailCache) mailSessionCache.getParameter(accountId, key);
        } catch (ClassCastException e) {
            /*
             * Class version does not match; just renew session cache.
             */
            mailCache = null;
            mailSessionCache.removeParameter(accountId, key);
        }
        return mailCache;
    }

    /**
     * Gets the session-bound mail cache.
     *
     * @param session The session whose mail cache shall be returned
     * @param accountId The account identifier
     * @return The session-bound mail cache.
     */
    public static SessionMailCache getInstance(Session session, int accountId) {
        MailSessionCache mailSessionCache = MailSessionCache.getInstance(session);
        String key = MailSessionParameterNames.getParamMailCache();
        SessionMailCache mailCache = null;
        try {
            mailCache = (SessionMailCache) mailSessionCache.getParameter(accountId, key);
        } catch (ClassCastException e) {
            /*
             * Class version does not match; just renew session cache.
             */
            mailCache = null;
            mailSessionCache.removeParameter(accountId, key);
        }
        if (null == mailCache) {
            final SessionMailCache newInst = new SessionMailCache();
            mailCache = (SessionMailCache) mailSessionCache.putParameterIfAbsent(accountId, key, newInst);
            if (null == mailCache) {
                mailCache = newInst;
            }
        }
        return mailCache;
    }

    /**
     * Clears all user-related cached data.
     */
    public static void clearAll(Session session) {
        try {
            final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class);
            if (null != storageService) {
                final MailAccount[] accounts = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
                for (MailAccount mailAccount : accounts) {
                    SessionMailCache.getInstance(session, mailAccount.getId()).clear();
                }
            }
        } catch (Exception e) {
            // SWALLOW
           LoggerFactory.getLogger(SessionMailCache.class).warn("", e);
        }
    }

    /*-
     * ##################################### MEMBER STUFF #####################################
     */

    private final ConcurrentMap<CacheKey, Object> cache;

    /**
     * Default constructor
     */
    private SessionMailCache() {
        super();
        cache = new ConcurrentHashMap<CacheKey, Object>();
    }

    /**
     * Puts specified <code>entry</code> into cache if {@link SessionMailCacheEntry#getValue()} is not <code>null</code>. Otherwise the a
     * possibly previously associated value with entry's key is removed from cache.
     * <p>
     * {@link SessionMailCacheEntry#getKey()} is used as key and {@link SessionMailCacheEntry#getValue()} as value.
     *
     * @param entry The mail cache entry
     */
    public <V extends Object> void put(SessionMailCacheEntry<V> entry) {
        if (null == entry.getValue()) {
            cache.remove(entry.getKey());
        } else {
            cache.put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Gets the entry acquired through {@link SessionMailCacheEntry#getKey()}. If present it's applied to <code>entry</code> via
     * {@link SessionMailCacheEntry#setValue(Object)}.
     *
     * @param <V> The cache entry's type
     * @param entry The mail cache entry
     */
    public <V extends Object> void get(SessionMailCacheEntry<V> entry) {
        entry.setValue(entry.getEntryClass().cast(cache.get(entry.getKey())));
    }

    /**
     * Removes the entry acquired through {@link SessionMailCacheEntry#getKey()} . If present it's applied to <code>entry</code> via
     * {@link SessionMailCacheEntry#setValue(Object)}.
     *
     * @param <V> The cache entry's type
     * @param entry The mail cache entry
     */
    public <V extends Object> void remove(SessionMailCacheEntry<V> entry) {
        entry.setValue(entry.getEntryClass().cast(cache.remove(entry.getKey())));
    }

    /**
     * Clears all entries contained in cache.
     */
    public void clear() {
        cache.clear();
    }
}
