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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.rest.client.httpclient.internal.cookiestore;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import jersey.repackaged.com.google.common.cache.Cache;
import jersey.repackaged.com.google.common.cache.CacheBuilder;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;

/**
 * {@link MultiUserCookieStore} is a {@link CookieStore} which caches a {@link CookieStore} per user account. It uses the {@link AccountAwareCookieStore} as a cache key.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class MultiUserCookieStore {

    private static final MultiUserCookieStore INSTANCE = new MultiUserCookieStore();
    private final Cache<AccountAwareCookieStore, CookieStore> cache;

    /**
     * Gets the {@link MultiUserCookieStore} instance
     */
    public static MultiUserCookieStore getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link MultiUserCookieStore}.
     */
    private MultiUserCookieStore() {
        cache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    }

    /**
     * Gets the cookie store from cache
     *
     * @param key The {@link AccountAwareCookieStore} to get the store for
     * @return The {@link CookieStore}
     */
    private CookieStore getCookieStore(AccountAwareCookieStore key) {
        try {
            return cache.get(key, () -> new BasicCookieStore());
        } catch (ExecutionException e) {
            throw new IllegalStateException("Error getting or intitializing cookie store for: " + key.toString(), e);
        }
    }

    /**
     * Adds a cookie to the given store
     *
     * @param key The store key
     * @param cookie The cookie to add
     */
    public void addCookie(AccountAwareCookieStore key, Cookie cookie) {
        getCookieStore(key).addCookie(cookie);
    }

    /**
     * Get all cookies from the given store
     *
     * @param key The store key
     * @return The cookies
     */
    public List<Cookie> getCookies(AccountAwareCookieStore key) {
        return getCookieStore(key).getCookies();
    }

    /**
     * Removes all of Cookies from the given store that have expired by the specified {@link Date}.
     *
     * @param key The store key
     * @param date The date to expire
     * @return <code>true</code> if any cookie has been removed
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
