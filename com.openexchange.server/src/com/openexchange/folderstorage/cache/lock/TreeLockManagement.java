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

package com.openexchange.folderstorage.cache.lock;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import com.openexchange.session.Session;

/**
 * {@link TreeLockManagement} - The folder tree lock management.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TreeLockManagement {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TreeLockManagement.class);

    private static final TreeLockManagement INSTANCE = new TreeLockManagement();

    /**
     * Gets the {@link TreeLockManagement management} instance.
     *
     * @return The management instance
     */
    public static TreeLockManagement getInstance() {
        return INSTANCE;
    }

    // ----------------------------------------------------------------------------------------------------------- //

    private final ConcurrentMap<Integer, ConcurrentMap<Integer, ConcurrentMap<String, ReadWriteLock>>> map;

    /**
     * Initializes a new {@link TreeLockManagement}.
     */
    private TreeLockManagement() {
        super();
        map = new ConcurrentHashMap<Integer, ConcurrentMap<Integer,ConcurrentMap<String,ReadWriteLock>>>(32);
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
        final ConcurrentMap<Integer, ConcurrentMap<String, ReadWriteLock>> userMap = map.get(Integer.valueOf(session.getContextId()));
        if (null != userMap) {
            userMap.remove(Integer.valueOf(session.getUserId()));
            LOG.debug("Cleaned folder locks for user {} in context {}", session.getUserId(), session.getContextId());
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
     * @param treeId The tree identifier
     * @param session The session
     * @return The read-write lock
     */
    public ReadWriteLock getFor(final String treeId, final Session session) {
        return getFor(treeId, session.getUserId(), session.getContextId());
    }

    /**
     * Gets the lock for specified tree and session.
     *
     * @param treeId The tree identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The read-write lock
     */
    public ReadWriteLock getFor(final String treeId, final int userId, final int contextId) {
        return EMPTY_READ_WRITE_LOCK;
        /*-
         *
        ConcurrentMap<Integer, ConcurrentMap<String, ReadWriteLock>> userMap = map.get(Integer.valueOf(contextId));
        if (null == userMap) {
            final ConcurrentMap<Integer, ConcurrentMap<String, ReadWriteLock>> newUserMap = new ConcurrentHashMap<Integer, ConcurrentMap<String,ReadWriteLock>>(32);
            userMap = map.putIfAbsent(Integer.valueOf(contextId), newUserMap);
            if (null == userMap) {
                userMap = newUserMap;
            }
        }
        ConcurrentMap<String, ReadWriteLock> lockMap = userMap.get(Integer.valueOf(userId));
        if (null == lockMap) {
            final ConcurrentMap<String, ReadWriteLock> nlm = new ConcurrentHashMap<String, ReadWriteLock>(4);
            lockMap = userMap.putIfAbsent(Integer.valueOf(userId), nlm);
            if (null == lockMap) {
                lockMap = nlm;
            }
        }
        ReadWriteLock readWriteLock = lockMap.get(treeId);
        if (null == readWriteLock) {
            final ReadWriteLock nrwl = new ReentrantReadWriteLock();
            readWriteLock = lockMap.putIfAbsent(treeId, nrwl);
            if (null == readWriteLock) {
                readWriteLock = nrwl;
            }
        }
        return readWriteLock;
         *
         */
    }

    /**
     * Optionally gets the lock for specified tree and session.
     *
     * @param treeId The tree identifier
     * @param session The session
     * @return The lock or <code>null</code> if absent
     */
    public ReadWriteLock optFor(final String treeId, final Session session) {
        return EMPTY_READ_WRITE_LOCK;
        /*-
         *
        ConcurrentMap<Integer, ConcurrentMap<String, ReadWriteLock>> userMap = map.get(Integer.valueOf(session.getContextId()));
        if (null == userMap) {
            return null;
        }
        ConcurrentMap<String, ReadWriteLock> lockMap = userMap.get(Integer.valueOf(session.getUserId()));
        if (null == lockMap) {
            return null;
        }
        return lockMap.get(treeId);
         *
         */
    }

    // ----------------------------------------------------------------------------------------------- //

    static final Lock EMPTY_LOCK = new Lock() {

        @Override
        public void unlock() {
            // Nothing to do
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public boolean tryLock() {
            return true;
        }

        @Override
        public Condition newCondition() {
            return EMPTY_CONDITION;
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            // Nothing to do
        }

        @Override
        public void lock() {
            // Nothing to do
        }
    };

    static final Condition EMPTY_CONDITION = new Condition() {

        @Override
        public void signalAll() {
            // Nothing to do
        }

        @Override
        public void signal() {
            // Nothing to do
        }

        @Override
        public boolean awaitUntil(Date deadline) throws InterruptedException {
            return true;
        }

        @Override
        public void awaitUninterruptibly() {
            // Nothing to do
        }

        @Override
        public long awaitNanos(long nanosTimeout) throws InterruptedException {
            return 0;
        }

        @Override
        public boolean await(long time, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public void await() throws InterruptedException {
            // Nothing to do
        }
    };

    private static final ReadWriteLock EMPTY_READ_WRITE_LOCK = new ReadWriteLock() {

        @Override
        public Lock writeLock() {
            return EMPTY_LOCK;
        }

        @Override
        public Lock readLock() {
            return EMPTY_LOCK;
        }
    };

}
