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

package com.openexchange.lock.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.exception.OXException;
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
