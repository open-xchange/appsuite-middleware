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

package com.openexchange.rest.client.httpclient.util;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;
import com.openexchange.rest.client.httpclient.internal.cookiestore.AccountAwareCookieStore;
import com.openexchange.rest.client.httpclient.internal.cookiestore.MultiUserCookieStore;
import com.openexchange.session.Session;

/**
 * {@link HttpContextUtils} is a utility class which helps setup and invalidate a cookie store for a given HTTP context.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class HttpContextUtils {

    /**
     * Creates a new, simple, non-cached cookie store
     *
     * @return A cookie store
     */
    public static CookieStore createCookieStore() {
        return new BasicCookieStore();
    }

    /**
     * Adds a {@link CookieStore} to the given HTTP context.
     *
     * @param context The HTTP context that represents the execution state of an HTTP process
     * @param session The session providing user information
     * @param accountId The account identifier
     * @return The newly created or cached cookie store for the combination of IDs
     */
    public static CookieStore addCookieStore(HttpContext context, Session session, String accountId) {
        return addCookieStore(context, session.getContextId(), session.getUserId(), accountId);
    }

    /**
     * Adds a {@link CookieStore} to the given HTTP context.
     *
     * @param context The HTTP context that represents the execution state of an HTTP process
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return The newly created or cached cookie store for the combination of IDs
     */
    public static CookieStore addCookieStore(HttpContext context, int contextId, int userId, String accountId) {
        AccountAwareCookieStore cookieStore = new AccountAwareCookieStore(accountId, contextId, userId);
        context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
        return cookieStore;
    }

    /**
     * Adds the given {@link CookieStore} to the given HTTP context.
     *
     * @param context The HTTP context that represents the execution state of an HTTP process
     * @param cookieStore The cookie store to add
     */
    public static void addCookieStore(HttpContext context, CookieStore cookieStore) {
        context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
    }

    /**
     * Get the {@link CookieStore} from the given HTTP context.
     *
     * @param context The HTTP context that represents the execution state of an HTTP process
     * @return The cookie store or <code>null</code> if not set
     */
    public static CookieStore getCookieStore(HttpContext context) {
        return (CookieStore) context.getAttribute(HttpClientContext.COOKIE_STORE);
    }

    /**
     * Removes the {@link CookieStore} for the given session and account
     *
     * @param session The session providing user information
     * @param accountId The account identifier
     */
    public static void removeCookieStore(Session session, String accountId) {
        removeCookieStore(session.getContextId(), session.getUserId(), accountId);
    }

    /**
     * Removes the {@link CookieStore} for the given session and account
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     */
    public static void removeCookieStore(int contextId, int userId, String accountId) {
        MultiUserCookieStore.getInstance().clear(new AccountAwareCookieStore(accountId, contextId, userId));
    }

    /**
     * Adds a {@link CredentialsProvider} for the given user, password combination to the given HTTP context.
     *
     * @param context The HTTP context that represents the execution state of an HTTP process
     * @param username The user name to use
     * @param password The password to use
     * @param targetHost The target host to send the request to
     */
    public static void addCredentialProvider(HttpContext context, String username, String password, HttpHost targetHost) {
        addCredentialProvider(context, username, password, new AuthScope(targetHost.getHostName(), targetHost.getPort()));
    }

    /**
     * Adds a {@link CredentialsProvider} for the given user, password combination to the given HTTP context.
     *
     * @param context The HTTP context that represents the execution state of an HTTP process
     * @param username The user name to use
     * @param password The password to use
     * @param authScope The {@link AuthScope} of the credentials
     */
    public static void addCredentialProvider(HttpContext context, String username, String password, AuthScope authScope) {
        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(authScope, new UsernamePasswordCredentials(username, password));
        context.setAttribute(HttpClientContext.CREDS_PROVIDER, credentialsProvider);
    }

    /**
     * Adds an {@link AuthCache} for the given HTTP context.
     *
     * @param targetHost The target host to send the request to
     * @param context The HTTP context that represents the execution state of an HTTP process
     */
    public static void addAuthCache(HttpContext context, HttpHost targetHost) {
        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());
        context.setAttribute(HttpClientContext.AUTH_CACHE, authCache);
    }

}
