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

package com.openexchange.ajax.continuation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;

/**
 * {@link ExecutorContinuation} - A {@link Continuation} backed by an {@link Executor}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ExecutorContinuation<V> implements Continuation<Collection<V>> {

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

    // ------------------------------------------------------------------------------ //

    private final UUID uuid;
    private final Executor executor;
    private final BlockingQueue<Future<V>> completionQueue;
    private final List<Future<V>> completedFutures;
    private int count;
    private final String format;
    private final int hash;

    /**
     * Initializes a new {@link ExecutorContinuation}.
     *
     * @param executor The executor to use
     */
    public ExecutorContinuation(final Executor executor, final String format) {
        super();
        this.format = null == format ? "json" : format;
        this.executor = executor;
        uuid = UUID.randomUUID();
        completionQueue = new LinkedBlockingQueue<Future<V>>();
        completedFutures = new LinkedList<Future<V>>();
        count = 0;

        final int prime = 31;
        int result = 1;
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        hash = result;
    }

    @Override
    public String getFormat() {
        return format;
    }

    /**
     * Submits a value-returning task for execution.
     *
     * @param task The task to submit
     * @return A Future representing pending completion of the task
     * @throws RejectedExecutionException If the task cannot be scheduled for execution
     * @throws NullPointerException If the task is <code>null</code>
     */
    public synchronized void submit(final Callable<V> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        final QueueingFuture<V> f = new QueueingFuture<V>(task, completionQueue);
        executor.execute(f);
        count++;
    }

    /**
     * Submits a task for execution.
     *
     * @param task The task to submit
     * @param result The result to return upon successful completion
     * @return A Future representing pending completion of the task, and whose <tt>get()</tt> method will return the given result value upon
     *         completion
     * @throws RejectedExecutionException If the task cannot be scheduled for execution
     * @throws NullPointerException If the task is <code>null</code>
     */
    public synchronized void submit(final Runnable task, final V result) {
        if (task == null) {
            throw new NullPointerException();
        }
        final QueueingFuture<V> f = new QueueingFuture<V>(task, result, completionQueue);
        executor.execute(f);
        count++;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public ContinuationResponse<Collection<V>> getNextResponse(final long time, final TimeUnit unit) throws OXException, InterruptedException {
        return getNextResponse(time, unit, null);
    }

    @Override
    public synchronized ContinuationResponse<Collection<V>> getNextResponse(final long time, final TimeUnit unit, final Collection<V> defaultResponse) throws OXException, InterruptedException {
        int completedCount = completedFutures.size();
        if (completedCount == count) {
            return new ContinuationResponse<Collection<V>>(defaultResponse, true);
        }

        // At least one completion to await
        Future<V> f = completionQueue.poll(time, unit);
        if (null == f) {
            // Time elapsed
            return new ContinuationResponse<Collection<V>>(defaultResponse, false);
        }
        completedFutures.add(f);
        completedCount++;

        // Collect more if available
        for (int i = count - completedCount; i-- > 0;) {
            f = completionQueue.poll();
            if (null == f) {
                i = 0;
            } else {
                completedFutures.add(f);
            }
        }

        final List<V> retval = new ArrayList<V>(completedFutures.size());
        for (final Future<V> completedFuture : completedFutures) {
            try {
                retval.add(completedFuture.get());
            } catch (final ExecutionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof OXException) {
                    throw (OXException) cause;
                }
                throw OXException.general("Continuation failed", cause);
            }
        }
        return new ContinuationResponse<Collection<V>>(prepare(retval), false);
    }

    /**
     * Prepares given results.
     *
     * @param col The results
     * @return The prepared results
     */
    protected Collection<V> prepare(final List<V> col) {
        return col;
    }

    // -------------------------------------------------------------------------------------------- //

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Continuation)) {
            return false;
        }
        final Continuation<?> other = (Continuation<?>) obj;
        if (uuid == null) {
            if (other.getUuid() != null) {
                return false;
            }
        } else if (!uuid.equals(other.getUuid())) {
            return false;
        }
        return true;
    }

}
