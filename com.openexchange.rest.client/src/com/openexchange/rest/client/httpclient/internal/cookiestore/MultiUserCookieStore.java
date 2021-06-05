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

package com.openexchange.rest.client.httpclient.internal.cookiestore;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * {@link MultiUserCookieStore} caches a {@link CookieStore} per user account. It uses the {@link AccountAwareCookieStore} as a cache key.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class MultiUserCookieStore {

    private static final MultiUserCookieStore INSTANCE = new MultiUserCookieStore();

    /**
     * Gets the {@link MultiUserCookieStore} instance
     *
     * @return The instance
     */
    public static MultiUserCookieStore getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final LoadingCache<AccountAwareCookieStore, CookieStore> cache;

    /**
     * Initializes a new {@link MultiUserCookieStore}.
     */
    private MultiUserCookieStore() {
        cache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build(new CacheLoader<AccountAwareCookieStore, CookieStore>() {

            @Override
            public CookieStore load(AccountAwareCookieStore key) throws Exception {
                return new BasicCookieStore();
            }
        });
    }

    /**
     * Gets the cookie store from cache.
     *
     * @param key The {key to get the store for
     * @return The cookie store
     * @throws IllegalStateException If a cookie store cannot be returned for given key
     */
    private CookieStore getCookieStore(AccountAwareCookieStore key) {
        try {
            return cache.get(key);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Error getting or intitializing cookie store for: " + key.toString(), e);
        }
    }

    /**
     * Adds a cookie to the given store.
     *
     * @param key The store key
     * @param cookie The cookie to add
     */
    public void addCookie(AccountAwareCookieStore key, Cookie cookie) {
        getCookieStore(key).addCookie(cookie);
    }

    /**
     * Get all cookies from the given store.
     *
     * @param key The store key
     * @return The cookies
     */
    public List<Cookie> getCookies(AccountAwareCookieStore key) {
        return getCookieStore(key).getCookies();
    }

    /**
     * Removes all of Cookies from the given store that have expired by the specified date.
     *
     * @param key The store key
     * @param date The expiration date
     * @return <code>true</code> if any cookie has been removed; otherwise <code>false</code>
     */
    public boolean clearExpired(AccountAwareCookieStore key, Date date) {
        return getCookieStore(key).clearExpired(date);
    }

    /**
     * Removes all cookies from the given cookie store
     *
     * @param key The store key
     */
    public void clear(AccountAwareCookieStore key) {
        getCookieStore(key).clear();
    }

}
