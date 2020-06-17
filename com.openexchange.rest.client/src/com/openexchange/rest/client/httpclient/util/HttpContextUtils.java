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
     * Adds a {@link CookieStore} to the given HTTP context.
     *
     * @param context The HTTP context that represents the execution state of an HTTP process
     * @param session The session providing user information
     * @param accountId The account identifier
     */
    public static void addCookieStore(HttpContext context, Session session, String accountId) {
        addCookieStore(context, session.getContextId(), session.getUserId(), accountId);
    }

    /**
     * Adds a {@link CookieStore} to the given HTTP context.
     *
     * @param context The HTTP context that represents the execution state of an HTTP process
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     */
    public static void addCookieStore(HttpContext context, int contextId, int userId, String accountId) {
        context.setAttribute(HttpClientContext.COOKIE_STORE, new AccountAwareCookieStore(accountId, contextId, userId));
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
