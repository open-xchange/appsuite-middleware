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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.file.storage.webdav.session;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

/**
 * {@link WebDAVHttpClientRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class WebDAVHttpClientRegistry {

    private static final WebDAVHttpClientRegistry INSTANCE = new WebDAVHttpClientRegistry();

    /**
     * Gets the registry instance.
     *
     * @return The registry instance
     */
    public static WebDAVHttpClientRegistry getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<SimpleKey, ConcurrentMap<String, HttpClient>> map;

    /**
     * Initializes a new {@link WebDAVHttpClientRegistry}.
     */
    private WebDAVHttpClientRegistry() {
        super();
        map = new ConcurrentHashMap<SimpleKey, ConcurrentMap<String, HttpClient>>();
    }

    /**
     * Adds specified HttpClient.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @param client The HttpClient to add
     * @return The previous associated HttpClient, or <code>null</code> if there was no HttpClient.
     */
    public HttpClient addClient(final int contextId, final int userId, final String accountId, final HttpClient client) {
        final SimpleKey key = SimpleKey.valueOf(contextId, userId);
        ConcurrentMap<String, HttpClient> inner = map.get(key);
        if (null == inner) {
            final ConcurrentMap<String, HttpClient> tmp = new ConcurrentHashMap<String, HttpClient>();
            inner = map.putIfAbsent(key, tmp);
            if (null == inner) {
                inner = tmp;
            }
        }
        return inner.putIfAbsent(accountId, client);
    }

    /**
     * Check presence of the HttpClient associated with given user-context-pair.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return <code>true</code> if such a session is present; otherwise <code>false</code>
     */
    public boolean containsClient(final int contextId, final int userId, final String accountId) {
        final ConcurrentMap<String, HttpClient> inner = map.get(SimpleKey.valueOf(contextId, userId));
        return null != inner && inner.containsKey(accountId);
    }

    /**
     * Gets the HttpClient associated with given user-context-pair.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return The HttpClient or <code>null</code>
     */
    public HttpClient getClient(final int contextId, final int userId, final String accountId) {
        final ConcurrentMap<String, HttpClient> inner = map.get(SimpleKey.valueOf(contextId, userId));
        return null == inner ? null : inner.get(accountId);
    }

    /**
     * Removes specified session identifier associated with given user-context-pair and the HttpClient as well, if no more
     * user-associated session identifiers are present.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if a HttpClient for given user-context-pair was found and removed; otherwise <code>false</code>
     */
    public boolean removeClientIfLast(final int contextId, final int userId) {
        final ConcurrentMap<String, HttpClient> inner = map.remove(SimpleKey.valueOf(contextId, userId));
        if (null == inner || inner.isEmpty()) {
            return false;
        }
        for (final HttpClient client : inner.values()) {
            ((MultiThreadedHttpConnectionManager) client.getHttpConnectionManager()).shutdown();
        }
        return true;
    }

    /**
     * Purges specified user's HttpClient.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return <code>true</code> if a HttpClient for given user-context-pair was found and purged; otherwise <code>false</code>
     */
    public boolean purgeUserClient(final int contextId, final int userId, final String accountId) {
        final SimpleKey key = SimpleKey.valueOf(contextId, userId);
        final ConcurrentMap<String, HttpClient> inner = map.get(key);
        if (null == inner) {
            return false;
        }
        final HttpClient client = inner.remove(accountId);
        if (null == client) {
            return false;
        }
        ((MultiThreadedHttpConnectionManager) client.getHttpConnectionManager()).shutdown();
        if (inner.isEmpty()) {
            map.remove(key);
        }
        return true;
    }

    /**
     * Gets a {@link Iterator iterator} over the sessions in this registry.
     *
     * @return A {@link Iterator iterator} over the sessions in this registry.
     */
    Iterator<ConcurrentMap<String, HttpClient>> iterator() {
        return map.values().iterator();
    }

    private static final class SimpleKey {

        public static SimpleKey valueOf(final int cid, final int user) {
            return new SimpleKey(cid, user);
        }

        final int cid;

        final int user;

        private final int hash;

        private SimpleKey(final int cid, final int user) {
            super();
            this.cid = cid;
            this.user = user;
            // hash code
            final int prime = 31;
            int result = 1;
            result = prime * result + cid;
            result = prime * result + user;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof SimpleKey)) {
                return false;
            }
            final SimpleKey other = (SimpleKey) obj;
            if (cid != other.cid) {
                return false;
            }
            if (user != other.user) {
                return false;
            }
            return true;
        }
    } // End of SimpleKey

}
