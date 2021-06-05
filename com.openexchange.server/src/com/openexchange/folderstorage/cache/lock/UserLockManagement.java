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

package com.openexchange.folderstorage.cache.lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.openexchange.session.Session;

/**
 * {@link UserLockManagement} - The folder tree lock management.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserLockManagement {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserLockManagement.class);

    private static final UserLockManagement INSTANCE = new UserLockManagement();

    /**
     * Gets the {@link UserLockManagement management} instance.
     *
     * @return The management instance
     */
    public static UserLockManagement getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<Integer, ConcurrentMap<Integer, ReadWriteLock>> map;

    /**
     * Initializes a new {@link UserLockManagement}.
     */
    private UserLockManagement() {
        super();
        map = new ConcurrentHashMap<Integer, ConcurrentMap<Integer, ReadWriteLock>>(32, 0.9f, 1);
    }

    /**
     * Clears the lock management.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Drops locks for given session's user.
     *
     * @param session The session
     */
    public void dropFor(final Session session) {
        final ConcurrentMap<Integer, ReadWriteLock> userMap = map.remove(Integer.valueOf(session.getContextId()));
        if (null != userMap) {
            userMap.remove(Integer.valueOf(session.getUserId()));
            LOG.debug("Cleaned folder locks for user {} in context {}", session.getUserId(), session.getContextId());
        }
    }

    /**
     * Drops locks for given user.
     */
    public void dropFor(Integer userId, Integer contextId) {
        final ConcurrentMap<Integer, ReadWriteLock> userMap = map.remove(contextId);
        if (null != userMap) {
            userMap.remove(userId);
            LOG.debug("Cleaned folder locks for user {} in context {}", userId, contextId);
        }
    }

    /**
     * Drops locks for given context.
     *
     * @param contextId The context identifier
     */
    public void dropFor(final int contextId) {
        map.remove(Integer.valueOf(contextId));
        LOG.debug("Cleaned folder locks for context {}", contextId);
    }

    /**
     * Gets the lock for specified tree and session.
     *
     * @param session The session
     * @return The read-write lock
     */
    public ReadWriteLock getFor(final Session session) {
        return getFor(session.getUserId(), session.getContextId());
    }

    /**
     * Gets the lock for specified tree and session.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The read-write lock
     */
    public ReadWriteLock getFor(final int userId, final int contextId) {
        ConcurrentMap<Integer, ReadWriteLock> userMap = map.remove(Integer.valueOf(contextId));
        if (null == userMap) {
            final ConcurrentMap<Integer, ReadWriteLock> newUserMap = new ConcurrentHashMap<Integer, ReadWriteLock>(32, 0.9f, 1);
            userMap = map.putIfAbsent(Integer.valueOf(contextId), newUserMap);
            if (null == userMap) {
                userMap = newUserMap;
            }
        }
        ReadWriteLock readWriteLock = userMap.get(Integer.valueOf(userId));
        if (null == readWriteLock) {
            final ReadWriteLock nrwl = new ReentrantReadWriteLock();
            readWriteLock = userMap.putIfAbsent(Integer.valueOf(userId), nrwl);
            if (null == readWriteLock) {
                readWriteLock = nrwl;
            }
        }
        return readWriteLock;
    }

    /**
     * Optionally gets the lock for specified tree and session.
     *
     * @param session The session
     * @return The lock or <code>null</code> if absent
     */
    public ReadWriteLock optFor(final Session session) {
        final ConcurrentMap<Integer, ReadWriteLock> userMap = map.remove(Integer.valueOf(session.getContextId()));
        if (null == userMap) {
            return null;
        }
        return userMap.get(Integer.valueOf(session.getUserId()));
    }

}
