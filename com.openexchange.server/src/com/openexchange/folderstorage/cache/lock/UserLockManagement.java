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
