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

package com.openexchange.java;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * {@link LimitedExecutorService} - An {@link ExecutorService} that limits the number of threads used by delegate <code>executor</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class LimitedExecutorService implements ExecutorService {

    private static final class ScheduledLock {

        private int count;
        private final Lock lock;
        private final int max;
        private final Queue<Runnable> workQueue;

        ScheduledLock(final int max, final Queue<Runnable> workQueue) {
            super();
            this.workQueue = workQueue;
            this.max = max;
            lock = new ReentrantLock();
            count = 0;
        }

        boolean incrementFor(final Runnable command) {
            lock.lock();
            try {
                if (count < max) {
                    count++;
                    return true;
                }
                workQueue.offer(command);
                return false;
            } finally {
                lock.unlock();
            }
        }

        Runnable decrementIfNoneInQueue() {
            Runnable fromQueue = workQueue.poll();
            if (null != fromQueue) {
                return fromQueue;
            }
            // Acquire lock
            lock.lock();
            try {
                // Once again check work queue since lock is acquired
                fromQueue = workQueue.poll();
                if (null != fromQueue) {
                    return fromQueue;
                }
                if (count > 1) {
                    count--;
                }
            } finally {
                lock.unlock();
            }
            return null;
        }

    }

    private static final class ReschedulingRunnable implements Runnable {

        private final Runnable firstTask;
        private final ScheduledLock scheduledLock;

        ReschedulingRunnable(final Runnable firstTask, final ScheduledLock scheduledLock) {
            super();
            this.firstTask = firstTask;
            this.scheduledLock = scheduledLock;
        }

        @Override
        public void run() {
            Runnable next = firstTask;
            while (null != next) { // Use this thread to execute either the initial task or dequeued tasks
                try {
                    next.run();
                } finally {
                    next = scheduledLock.decrementIfNoneInQueue();
                }
            }
        }

    }

    // ----------------------------------------------------------------------------------------------

    private final ExecutorService executor;
    private final ScheduledLock scheduledLock;

    /**
     * Initializes a new {@link LimitedExecutorService}.
     */
    public LimitedExecutorService(final ExecutorService executor, final int concurrencyLimit) {
        super();
        if (concurrencyLimit <= 0) {
            throw new IllegalArgumentException("concurrencyLimit is less than or equal to zero.");
        }
        this.executor = executor;
        scheduledLock = new ScheduledLock(concurrencyLimit, new ConcurrentLinkedQueue<Runnable>());
    }

    @Override
    public void execute(final Runnable command) {
        if (command == null) {
            throw new NullPointerException("Runnable is null");
        }
        if (scheduledLock.incrementFor(command)) {
            executor.execute(new ReschedulingRunnable(command, scheduledLock));
        } // Otherwise added to work queue
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return executor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return executor.isTerminated();
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        return executor.submit(task);
    }

    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        return executor.submit(task, result);
    }

    @Override
    public Future<?> submit(final Runnable task) {
        return executor.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executor.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException {
        return executor.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executor.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executor.invokeAny(tasks, timeout, unit);
    }

}
