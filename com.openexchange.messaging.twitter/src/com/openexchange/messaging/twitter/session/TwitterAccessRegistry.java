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

package com.openexchange.messaging.twitter.session;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.twitter.TwitterAccess;

/**
 * {@link TwitterAccessRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class TwitterAccessRegistry {

    private static final TwitterAccessRegistry INSTANCE = new TwitterAccessRegistry();

    /**
     * Gets the registry instance.
     *
     * @return The registry instance
     */
    public static TwitterAccessRegistry getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<SimpleKey, ConcurrentMap<Integer, TwitterAccess>> map;

    /**
     * Initializes a new {@link TwitterAccessRegistry}.
     */
    private TwitterAccessRegistry() {
        super();
        map = new ConcurrentHashMap<SimpleKey, ConcurrentMap<Integer, TwitterAccess>>();
    }

    /**
     * Adds specified twitter access.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @param twitterAccess The twitter access to add
     * @return The previous associated twitter access, or <code>null</code> if there was no twitter access.
     */
    public TwitterAccess addAccess(final int contextId, final int userId, final int accountId, final TwitterAccess twitterAccess) {
        final SimpleKey key = SimpleKey.valueOf(contextId, userId);
        ConcurrentMap<Integer, TwitterAccess> inner = map.get(key);
        if (null == inner) {
            final ConcurrentMap<Integer, TwitterAccess> tmp = new ConcurrentHashMap<Integer, TwitterAccess>();
            inner = map.putIfAbsent(key, tmp);
            if (null == inner) {
                inner = tmp;
            }
        }
        return inner.putIfAbsent(Integer.valueOf(accountId), twitterAccess);
    }

    /**
     * Check presence of the twitter access associated with given user-context-pair.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return <code>true</code> if such a twitter access is present; otherwise <code>false</code>
     */
    public boolean containsAccess(final int contextId, final int userId, final int accountId) {
        final ConcurrentMap<Integer, TwitterAccess> inner = map.get(SimpleKey.valueOf(contextId, userId));
        return null != inner && inner.containsKey(Integer.valueOf(accountId));
    }

    /**
     * Gets the twitter access associated with given user-context-pair.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return The twitter access or <code>null</code>
     */
    public TwitterAccess getAccess(final int contextId, final int userId, final int accountId) {
        final ConcurrentMap<Integer, TwitterAccess> inner = map.get(SimpleKey.valueOf(contextId, userId));
        return null == inner ? null : inner.get(Integer.valueOf(accountId));
    }

    /**
     * Removes specified access identifier associated with given user-context-pair and the twitter access as well, if no more
     * user-associated session identifiers are present.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if a twitter access for given user-context-pair was found and removed; otherwise <code>false</code>
     */
    public boolean removeAccessIfLast(final int contextId, final int userId) {
        return (null != map.remove(SimpleKey.valueOf(contextId, userId)));
    }

    /**
     * Purges specified user's twitter access.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return <code>true</code> if a twitter access for given user-context-pair was found and purged; otherwise <code>false</code>
     */
    public boolean purgeUserAccess(final int contextId, final int userId, final int accountId) {
        final SimpleKey key = SimpleKey.valueOf(contextId, userId);
        final ConcurrentMap<Integer, TwitterAccess> inner = map.get(key);
        if (null == inner) {
            return false;
        }
        return (null != inner.remove(Integer.valueOf(accountId)));
    }

    /**
     * Purges specified user's twitter accesses.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if a twitter access for given user-context-pair was found and purged; otherwise <code>false</code>
     */
    public void purgeUserAccess(int contextId, int userId) {
        final SimpleKey key = SimpleKey.valueOf(contextId, userId);
        final ConcurrentMap<Integer, TwitterAccess> inner = map.get(key);
        if (null == inner) {
            return;
        }

        inner.clear();
    }

    /**
     * Gets a {@link Iterator iterator} over the twitter accesses in this registry.
     *
     * @return A {@link Iterator iterator} over the twitter accesses in this registry.
     */
    Iterator<ConcurrentMap<Integer, TwitterAccess>> iterator() {
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
