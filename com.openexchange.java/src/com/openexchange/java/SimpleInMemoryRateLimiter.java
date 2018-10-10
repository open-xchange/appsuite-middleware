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

package com.openexchange.java;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import java.util.PriorityQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link SimpleInMemoryRateLimiter} - A simple in-memory rate limiter based on the token bucket algorithm.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class SimpleInMemoryRateLimiter {

    private final Bucket tokens;

    /**
     * Initializes a new {@link SimpleInMemoryRateLimiter}.
     *
     * @param permits The number of permits
     * @param timeFrameInMillis The time frame
     * @param unit The time unit
     * @throws IllegalArgumentException If either permits or timeFrameInMillis is negative or <code>0</code> (zero)
     */
    public SimpleInMemoryRateLimiter(int permits, long timeFrame, TimeUnit unit) {
        super();
        if (permits <= 0) {
            throw new IllegalArgumentException("Permits must not be negative or 0 (zero)");
        }
        if (timeFrame <= 0) {
            throw new IllegalArgumentException("Time frame must not be negative or 0 (zero)");
        }
        tokens = new Bucket(permits, TimeUnit.NANOSECONDS.convert(timeFrame, unit));
    }

    /**
     * Acquires a permit, blocking until it can be granted.
     *
     * @throws InterruptedException If interrupted while waiting
     */
    public void acquire() throws InterruptedException {
        tokens.takeExpired();
    }

    /**
     * Acquires a permit if it can be granted immediately without delay.
     *
     * @return <code>true</code> if permit is granted; otherwise <code>false</code>
     */
    public boolean tryAcquire() {
        Token polled = tokens.pollExpired();
        return (null != polled);
    }

    /**
     * Acquires a permit; waiting up to given time if necessary until it can be granted.
     *
     * @param timeout The time to wait for a permit being granted
     * @param unit The time unit
     * @return <code>true</code> if permit is granted in time; otherwise <code>false</code>
     * @throws InterruptedException If interrupted while waiting
     */
    public boolean tryAcquire(long timeout, TimeUnit unit) throws InterruptedException {
        Token polled = tokens.pollExpired(timeout, unit);
        return (null != polled);
    }

    /**
     * Checks if a permit can be granted immediately without delay.
     *
     * @return <code>true</code> if a permit can be granted; otherwise <code>false</code>
     */
    public boolean canAcquire() {
        Token peeked = tokens.peekExpired();
        return (null != peeked);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    private static abstract class Token implements Delayed {

        protected final long stamp;

        protected Token() {
            super();
            this.stamp = System.nanoTime();
        }

        @Override
        public final int compareTo(final Delayed o) {
            long thisStamp = stamp;
            long otherStamp = ((Token) o).stamp;
            return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
        }
    }

    private static class ImmediateToken extends Token {

        ImmediateToken() {
            super();
        }

        @Override
        public long getDelay(final TimeUnit unit) {
            return 0L;
        }
    }

    private static class DelayedToken extends Token {

        private final long delayNanos;

        DelayedToken(long delayNanos) {
            super();
            this.delayNanos = delayNanos;
        }

        @Override
        public long getDelay(final TimeUnit unit) {
            return unit.convert(delayNanos - (System.nanoTime() - stamp), TimeUnit.NANOSECONDS);
        }
    }

    private static class Bucket {

        private final long timeFrameInNanos;
        private final PriorityQueue<Token> q;
        private final ReentrantLock lock;
        private final Condition available;
        private Thread leader = null;

        /**
         * Creates a new {@code Bucket} that is initially empty.
         *
         * @param permits The number of permits
         * @param timeFrameInNanos The time frame in nanoseconds
         */
        Bucket(int permits, long timeFrameInNanos) {
            super();
            this.timeFrameInNanos = timeFrameInNanos;

            // Initialize members
            q = new PriorityQueue<Token>(permits);
            lock = new ReentrantLock();
            available = lock.newCondition();

            // Offer immediately available tokens
            for (int i = permits; i-- > 0;) {
                Token token = new ImmediateToken();
                q.offer(token);
                if (q.peek() == token) {
                    leader = null;
                }
            }
        }

        private void replenish() {
            Token token = new DelayedToken(timeFrameInNanos);
            q.offer(token);
            if (q.peek() == token) {
                leader = null;
                available.signal();
            }
        }

        /**
         * Retrieves, removes and replenishes the first token, or returns <code>null</code> if this bucket has no tokens with an expired
         * delay.
         *
         * @return The first token, or <code>null</code> if this bucket has no tokens with an expired delay
         */
        public Token pollExpired() {
            lock.lock();
            try {
                Token first = q.peek();
                if (first == null || first.getDelay(NANOSECONDS) > 0) {
                    return null;
                }

                Token polled = q.poll();
                replenish();
                return polled;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Retrieves, removes and replenishes the first token of this bucket, waiting if necessary until a token with an expired delay is
         * available.
         *
         * @return The first token
         * @throws InterruptedException If interrupted while waiting
         */
        public Token takeExpired() throws InterruptedException {
            lock.lockInterruptibly();
            try {
                for (;;) {
                    Token first = q.peek();
                    if (first == null) {
                        available.await();
                    } else {
                        long delay = first.getDelay(NANOSECONDS);
                        if (delay <= 0) {
                            Token polled = q.poll();
                            replenish();
                            return polled;
                        }
                        first = null; // don't retain ref while waiting
                        if (leader != null) {
                            available.await();
                        } else {
                            Thread thisThread = Thread.currentThread();
                            leader = thisThread;
                            try {
                                available.awaitNanos(delay);
                            } finally {
                                if (leader == thisThread) {
                                    leader = null;
                                }
                            }
                        }
                    }
                }
            } finally {
                if (leader == null && q.peek() != null) {
                    available.signal();
                }
                lock.unlock();
            }
        }

        /**
         * Retrieves, removes and replenishes the first token of this bucket, waiting if necessary until a token with an expired delay is
         * available, or the specified wait time expires.
         *
         * @param timeout The maximum time to wait
         * @param unit The time unit of the time to wait
         * @return The first token, or <code>null</code> if the specified waiting time elapses before a token with an expired delay becomes
         *         available
         * @throws InterruptedException If interrupted while waiting
         */
        public Token pollExpired(long timeout, TimeUnit unit) throws InterruptedException {
            lock.lockInterruptibly();
            try {
                long nanos = unit.toNanos(timeout);
                for (;;) {
                    Token first = q.peek();
                    if (first == null) {
                        if (nanos <= 0) {
                            return null;
                        }

                        nanos = available.awaitNanos(nanos);
                    } else {
                        long delay = first.getDelay(NANOSECONDS);
                        if (delay <= 0) {
                            Token polled = q.poll();
                            replenish();
                            return polled;
                        }
                        if (nanos <= 0) {
                            return null;
                        }
                        first = null; // don't retain ref while waiting
                        if (nanos < delay || leader != null) {
                            nanos = available.awaitNanos(nanos);
                        } else {
                            Thread thisThread = Thread.currentThread();
                            leader = thisThread;
                            try {
                                long timeLeft = available.awaitNanos(delay);
                                nanos -= delay - timeLeft;
                            } finally {
                                if (leader == thisThread) {
                                    leader = null;
                                }
                            }
                        }
                    }
                }
            } finally {
                if (leader == null && q.peek() != null) {
                    available.signal();
                }
                lock.unlock();
            }
        }

        /**
         * Retrieves, but does not remove/replenish, the first token of this bucket, or returns <code>null</code> if this bucket is empty or
         * has no tokens with expired delay.
         *
         * @return The first token of this bucket, or <code>null</code>
         */
        public Token peekExpired() {
            lock.lock();
            try {
                Token first = q.peek();
                return (first == null || first.getDelay(NANOSECONDS) > 0) ? null : first;
            } finally {
                lock.unlock();
            }
        }
    }

}
