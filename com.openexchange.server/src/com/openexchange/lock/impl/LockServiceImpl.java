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

package com.openexchange.lock.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.exception.OXException;
import com.openexchange.lock.AccessControl;
import com.openexchange.lock.LockService;


/**
 * {@link LockServiceImpl} - The default implementation based on {@link com.openexchange.concurrent.TimeoutConcurrentMap}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class LockServiceImpl implements LockService {

    private final com.openexchange.concurrent.TimeoutConcurrentMap<String, Lock> locks;

    /**
     * Initializes a new {@link LockServiceImpl}.
     *
     * @throws OXException If initialization fails
     */
    public LockServiceImpl() throws OXException {
        super();
        locks = new com.openexchange.concurrent.TimeoutConcurrentMap<String, Lock>(30);
    }

    /**
     * Disposes this lock service
     */
    public void dispose() {
        locks.dispose();
    }

    @Override
    public AccessControl getAccessControlFor(String identifier, int permits, int userId, int contextId) throws OXException {
       return AccessControlImpl.getAccessControl(identifier, permits, userId, contextId);
    }

    @Override
    public Lock getLockFor(String identifier) throws OXException {
        return getLockFor0(identifier, false);
    }

    @Override
    public Lock getSelfCleaningLockFor(String identifier) throws OXException {
        return getLockFor0(identifier, true);
    }

    private Lock getLockFor0(String identifier, boolean selfCleaning) {
        Lock lock = locks.get(identifier);
        if (null == lock) {
            ReentrantLock newLock = new ReentrantLock();
            lock = locks.putIfAbsent(identifier, newLock, 150, null);
            if (null == lock) {
                lock = selfCleaning ? new SelfCleaningLock(newLock, identifier, this) : newLock;
            }
        }
        return lock;
    }

    @Override
    public void removeLockFor(String identifier) {
        locks.remove(identifier);
    }

    // ---------------------------------------------------------------------------------------------------------------

    private static class SelfCleaningLock implements Lock {

        private final Lock lock;
        private final LockServiceImpl lockServiceImpl;
        private final String identifier;

        SelfCleaningLock(Lock lock, String identifier, LockServiceImpl lockServiceImpl) {
            super();
            this.lock = lock;
            this.identifier = identifier;
            this.lockServiceImpl = lockServiceImpl;
        }

        @Override
        public void lock() {
            lock.lock();
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            lock.lockInterruptibly();
        }

        @Override
        public boolean tryLock() {
            return lock.tryLock();
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return lock.tryLock(time, unit);
        }

        @Override
        public void unlock() {
            lockServiceImpl.removeLockFor(identifier);
            lock.unlock();
        }

        @Override
        public Condition newCondition() {
            return lock.newCondition();
        }
    }

}
