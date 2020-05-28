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

package com.openexchange.lock.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.lock.AccessControl;

/**
 * {@link AccessControlImplTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class AccessControlImplTest {

    private static final String AC_ID = "test";
    private static final int USER_ID = 1;
    private static final int CONTEXT_ID = 1;

    @Before
    public void before() {
        // test failed in CI; seems like a previous test case leaves the main thread in interrupted state
        if (Thread.interrupted()) {
            System.err.println("Clearing interrupted status of main thread before running...");
        }
    }

    /**
     * One thread gets lock within timeout
     */
    @Test
    public void tryWithTimeoutSingle_Success() throws OXException, InterruptedException {
        AccessControlImpl accessControl = AccessControlImpl.getAccessControl(AC_ID, 1, USER_ID, CONTEXT_ID);
        boolean acquired = false;
        try {
            if (accessControl.tryAcquireGrant(100L, TimeUnit.MILLISECONDS)) {
                acquired = true;
            }

            Assert.assertTrue("Lock was not granted within 100ms!", acquired);
        } finally {
            accessControl.release(acquired);
        }
    }

    /**
     * Two threads may obtain the same lock. The main thread gets it within timeout.
     */
    @Test
    public void tryWithTimeoutMulti_Success() throws OXException, InterruptedException {
        AccessControlImpl accessControl = AccessControlImpl.getAccessControl(AC_ID, 2, USER_ID, CONTEXT_ID);
        CountDownLatch latch = new CountDownLatch(1);

        // start other
        Thread other = new Thread(new LockHolder(accessControl, TimeUnit.MILLISECONDS.toNanos(100), latch));
        other.start();

        boolean acquired = false;
        try {
            latch.await();
            if (accessControl.tryAcquireGrant(50L, TimeUnit.MILLISECONDS)) {
                acquired = true;
            }

            Assert.assertTrue("Lock was not granted within 50ms!", acquired);
        } finally {
            accessControl.release(acquired);
            other.join(100L);
        }
    }

    /**
     * Three threads get the same lock, which only allows two concurrent holders. Thread "other2"
     * releases the lock before main thread times out.
     */
    @Test
    public void tryWithTimeoutMulti2_Success() throws OXException, InterruptedException {
        AccessControlImpl accessControl = AccessControlImpl.getAccessControl(AC_ID, 2, USER_ID, CONTEXT_ID);
        CountDownLatch latch = new CountDownLatch(2);

        // start other
        Thread other1 = new Thread(new LockHolder(accessControl, TimeUnit.MILLISECONDS.toNanos(100), latch));
        other1.start();

        Thread other2 = new Thread(new LockHolder(accessControl, TimeUnit.MILLISECONDS.toNanos(49), latch));
        other2.start();

        boolean acquired = false;
        try {
            latch.await();
            if (accessControl.tryAcquireGrant(50L, TimeUnit.MILLISECONDS)) {
                acquired = true;
            }

            Assert.assertTrue("Lock was not granted", acquired);
        } finally {
            accessControl.release(acquired);
            other1.join(100L);
            other2.join(100L);
        }
    }

    /**
     * Main thread tries to obtain lock which is currently held by another. Timeout gets exceeded.
     */
    @Test
    public void tryWithTimeoutSingle_Fail() throws OXException, InterruptedException {
        AccessControlImpl accessControl = AccessControlImpl.getAccessControl(AC_ID, 1, USER_ID, CONTEXT_ID);
        CountDownLatch latch = new CountDownLatch(1);

        // start other
        Thread other = new Thread(new LockHolder(accessControl, TimeUnit.MILLISECONDS.toNanos(100), latch));
        other.start();

        boolean acquired = false;
        try {
            latch.await();
            if (accessControl.tryAcquireGrant(50L, TimeUnit.MILLISECONDS)) {
                acquired = true;
            }

            Assert.assertFalse("Lock was granted", acquired);
        } finally {
            accessControl.release(acquired);
            other.join(100L);
        }
    }

    /**
     * Two threads hold lock which allows two concurrent lockers. Main thread tries to obtain lock with
     * timeout and fails.
     */
    @Test
    public void tryWithTimeoutMulti_Fail() throws OXException, InterruptedException {
        AccessControlImpl accessControl = AccessControlImpl.getAccessControl(AC_ID, 2, USER_ID, CONTEXT_ID);
        CountDownLatch latch = new CountDownLatch(2);

        // start other
        Thread other1 = new Thread(new LockHolder(accessControl, TimeUnit.MILLISECONDS.toNanos(120), latch));
        other1.start();

        Thread other2 = new Thread(new LockHolder(accessControl, TimeUnit.MILLISECONDS.toNanos(100), latch));
        other2.start();

        boolean acquired = false;
        try {
            latch.await();
            if (accessControl.tryAcquireGrant(50L, TimeUnit.MILLISECONDS)) {
                acquired = true;
            }

            Assert.assertFalse("Lock was granted but it shouldn't.", acquired);
        } finally {
            accessControl.release(acquired);
            other1.join(100L);
            other2.join(100L);
        }
    }

    private static final class LockHolder implements Runnable {

        private final AccessControl accessControl;
        private final long holdNanos;
        private final CountDownLatch latch;

        LockHolder(AccessControl accessControl, long holdNanos, CountDownLatch latch) {
            this.accessControl = accessControl;
            this.holdNanos = holdNanos;
            this.latch = latch;
        }

        @Override
        public void run() {
            try {
                accessControl.acquireGrant();
                latch.countDown();
                LockSupport.parkNanos(holdNanos);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                accessControl.release();
            }
        }

    }

}
