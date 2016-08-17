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

package com.openexchange.lock;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import com.openexchange.exception.OXException;


/**
 * {@link LockService} - Provides exclusive locks for arbitrary identifiers.
 * <p>
 * The locks a re self-managed and therefore are cleansed after certain amount of time (default idle time is 150 seconds).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface LockService {

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

    /**
     * The empty lock, doing nothing on invocations.
     */
    public static final Lock EMPTY_LOCK = new Lock() {

        @Override
        public void unlock() {
            // ignore
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
            // ignore
        }

        @Override
        public void lock() {
            // ignore
        }
    };

    /**
     * Gets the (volatile) lock for given identifier.
     *
     * @param identifier The identifier
     * @return The associated lock
     * @throws OXException If lock cannot be returned
     */
    Lock getLockFor(String identifier) throws OXException;

    /**
     * Gets the (self-cleaning) lock for given identifier.
     * <p>
     * When invoking {@link Lock#unlock()} the lock instance will be removed from this lock service.
     *
     * @param identifier The identifier
     * @return The associated lock
     * @throws OXException If lock cannot be returned
     */
    Lock getSelfCleaningLockFor(String identifier) throws OXException;

    /**
     * Removes the lock for given identifier.
     *
     * @param identifier The identifier
     */
    void removeLockFor(String identifier);

}
