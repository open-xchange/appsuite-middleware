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

package com.openexchange.threadpool.internal;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link FixedExecutorService} - An {@link ExecutorService} backed by another {@link ExecutorService} using a fixed number of active
 * threads.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FixedExecutorService extends AbstractExecutorService {

    private final class PollingFuture extends FutureTask<Object> {

        public PollingFuture(final Runnable command) {
            super(command, null);
        }

        @Override
        protected void done() {
            /*
             * Poll next available command from queue
             */
            pollFromQueue();
        }

    }

    private final ExecutorService executorService;

    private final Lock lock;

    private final BlockingQueue<Runnable> queue;

    private final int size;

    private final AtomicInteger count;

    /**
     * Initializes a new {@link FixedExecutorService}.
     */
    public FixedExecutorService(final int size, final ExecutorService executorService) {
        super();
        lock = new ReentrantLock();
        this.size = size;
        queue = new LinkedBlockingQueue<Runnable>();
        count = new AtomicInteger();
        this.executorService = executorService;
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(final Runnable command) {
        lock.lock();
        try {
            if (queue.isEmpty() && count.get() < size) {
                /*
                 * Pass to execute() and leave
                 */
                count.incrementAndGet();
                executorService.execute(new PollingFuture(command));
            } else {
                /*
                 * Too many in-progress commands or queue not empty. Enqueue and leave
                 */
                queue.offer(command);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Polls next available command from queue. If queue is not empty, command is immediately passed to {@link #execute(Runnable)} method;
     * otherwise counter is decremented to signal a free resource for further processing of passed {@link Runnable commands}.
     *
     * @return <code>true</code> if queue was not empty and thus a command is scheduled for execution; otherwise <code>false</code>
     */
    protected boolean pollFromQueue() {
        lock.lock();
        try {
            final Runnable command = queue.poll();
            if (null != command) {
                /*
                 * Continue executing next queued command
                 */
                executorService.execute(new PollingFuture(command));
                return true;
            }
            /*
             * No queued command available. Signal free thread for processing.
             */
            count.decrementAndGet();
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public void shutdown() {
        // No shut-down
        throw new UnsupportedOperationException("Shutdown not allowed.");
    }

    @Override
    public List<Runnable> shutdownNow() {
        // No shut-down
        throw new UnsupportedOperationException("Shutdown not allowed.");
    }

}
