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

package com.openexchange.file.storage.boxcom.access;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.file.storage.boxcom.Services;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link BoxAccessRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BoxAccessRegistry {

    private static final BoxAccessRegistry INSTANCE = new BoxAccessRegistry();

    /**
     * Gets the registry instance.
     *
     * @return The registry instance
     */
    public static BoxAccessRegistry getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<SimpleKey, ConcurrentMap<String, BoxAccess>> map;

    /**
     * Initializes a new {@link BoxAccessRegistry}.
     */
    private BoxAccessRegistry() {
        super();
        map = new ConcurrentHashMap<SimpleKey, ConcurrentMap<String, BoxAccess>>();
    }

    /**
     * Adds specified Box access.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @param boxAccess The Box access to add
     * @return The previous associated session, or <code>null</code> if there was no session.
     */
    public BoxAccess addAccess(final int contextId, final int userId, final String accountId, final BoxAccess boxAccess) {
        final SimpleKey key = SimpleKey.valueOf(contextId, userId);
        ConcurrentMap<String, BoxAccess> inner = map.get(key);
        if (null == inner) {
            final ConcurrentMap<String, BoxAccess> tmp = new ConcurrentHashMap<String, BoxAccess>();
            inner = map.putIfAbsent(key, tmp);
            if (null == inner) {
                inner = tmp;
            }
        }
        return inner.putIfAbsent(accountId, boxAccess);
    }

    /**
     * Check presence of the Box access associated with given user-context-pair.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return <code>true</code> if such a Box access is present; otherwise <code>false</code>
     */
    public boolean containsAccess(final int contextId, final int userId, final String accountId) {
        final ConcurrentMap<String, BoxAccess> inner = map.get(SimpleKey.valueOf(contextId, userId));
        return null != inner && inner.containsKey(accountId);
    }

    /**
     * Gets the Box access associated with given user-context-pair.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return The Box access or <code>null</code>
     */
    public BoxAccess getAccess(final int contextId, final int userId, final String accountId) {
        final ConcurrentMap<String, BoxAccess> inner = map.get(SimpleKey.valueOf(contextId, userId));
        return null == inner ? null : inner.get(accountId);
    }

    /**
     * Removes the OAuth access associated with given user-context-pair, if no more user-associated accesses are present.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if a Box access for given user-context-pair was found and removed; otherwise <code>false</code>
     */
    public boolean removeAccessIfLast(final int contextId, final int userId) {
        final SessiondService sessiondService = Services.getService(SessiondService.class);
        if (null == sessiondService || null == sessiondService.getAnyActiveSessionForUser(userId, contextId)) {
            /*
             * No sessions left for user
             */
            final ConcurrentMap<String, BoxAccess> inner = map.remove(SimpleKey.valueOf(contextId, userId));
            if (null == inner || inner.isEmpty()) {
                return false;
            }
            for (final BoxAccess boxAccess : inner.values()) {
                boxAccess.dispose();
            }
            return !inner.isEmpty();
        }
        return false;
    }

    /**
     * Purges specified user's Box access.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return <code>true</code> if a Box access for given user-context-pair was found and purged; otherwise <code>false</code>
     */
    public boolean purgeUserAccess(final int contextId, final int userId, final String accountId) {
        final SimpleKey key = SimpleKey.valueOf(contextId, userId);
        final ConcurrentMap<String, BoxAccess> inner = map.get(key);
        if (null == inner) {
            return false;
        }
        final BoxAccess oAuthInfo = inner.remove(accountId);
        if (null == oAuthInfo) {
            return false;
        }
        if (inner.isEmpty()) {
            map.remove(key);
        }
        return true;
    }

    /**
     * Gets a {@link Iterator iterator} over the Box access instances in this registry.
     *
     * @return A {@link Iterator iterator} over the Box access instances in this registry.
     */
    Iterator<ConcurrentMap<String, BoxAccess>> iterator() {
        return map.values().iterator();
    }

    private static final class SimpleKey {

        static SimpleKey valueOf(final int cid, final int user) {
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
