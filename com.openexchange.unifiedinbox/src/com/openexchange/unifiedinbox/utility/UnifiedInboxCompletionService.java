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

package com.openexchange.unifiedinbox.utility;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * {@link UnifiedInboxCompletionService} - A {@link TrackingCompletionService} that uses a supplied {@link Executor} to execute tasks. This
 * class arranges that submitted tasks are, upon completion, placed on a queue accessible using <tt>take</tt>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnifiedInboxCompletionService<V> implements TrackingCompletionService<V> {

    /**
     * FutureTask extension to enqueue upon completion
     */
    private static final class QueueingFuture<V> extends FutureTask<V> {

        private final BlockingQueue<Future<V>> queue;

        QueueingFuture(final Callable<V> c, final BlockingQueue<Future<V>> queue) {
            super(c);
            this.queue = queue;
        }

        QueueingFuture(final Runnable t, final V r, final BlockingQueue<Future<V>> queue) {
            super(t, r);
            this.queue = queue;
        }

        @Override
        protected void done() {
            queue.add(this);
        }
    }

    private final Executor executor;

    private final BlockingQueue<Future<V>> completionQueue;

    private long start;

    private long duration;

    private int count;

    /**
     * Initializes a new {@link UnifiedInboxCompletionService}.
     *
     * @param executor The executor to use
     * @throws NullPointerException If executor is <tt>null</tt>
     */
    public UnifiedInboxCompletionService(final Executor executor) {
        super();
        if (executor == null) {
            throw new NullPointerException();
        }
        this.executor = executor;
        this.completionQueue = new LinkedBlockingQueue<Future<V>>();
    }

    /**
     * Initializes a new {@link UnifiedInboxCompletionService}.
     *
     * @param executor The executor to use
     * @param completionQueue The queue to use as the completion queue normally one dedicated for use by this service
     * @throws NullPointerException If executor or completionQueue are <tt>null</tt>
     */
    public UnifiedInboxCompletionService(final Executor executor, final BlockingQueue<Future<V>> completionQueue) {
        super();
        if (executor == null || completionQueue == null) {
            throw new NullPointerException();
        }
        this.executor = executor;
        this.completionQueue = completionQueue;
    }

    @Override
    public Future<V> submit(final Callable<V> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        final QueueingFuture<V> f = new QueueingFuture<V>(task, completionQueue);
        executor.execute(f);
        if (++count == 1) {
            // First element submitted
            start = System.currentTimeMillis();
        }
        return f;
    }

    @Override
    public Future<V> submit(final Runnable task, final V result) {
        if (task == null) {
            throw new NullPointerException();
        }
        final QueueingFuture<V> f = new QueueingFuture<V>(task, result, completionQueue);
        executor.execute(f);
        if (++count == 1) {
            // First element submitted
            start = System.currentTimeMillis();
        }
        return f;
    }

    @Override
    public Future<V> take() throws InterruptedException {
        final Future<V> f = completionQueue.take();
        if (0 == --count) {
            duration = (System.currentTimeMillis() - start);
        }
        return f;
    }

    @Override
    public Future<V> poll() {
        final Future<V> f = completionQueue.poll();
        if (null != f && 0 == --count) {
            duration = (System.currentTimeMillis() - start);
        }
        return f;
    }

    @Override
    public Future<V> poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        final Future<V> f = completionQueue.poll(timeout, unit);
        if (null != f && 0 == --count) {
            duration = (System.currentTimeMillis() - start);
        }
        return f;
    }

    @Override
    public long getDuration() {
        return duration;
    }
}
