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

package com.openexchange.continuation;

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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import com.openexchange.continuation.utils.TimeAwareBlockingQueue;
import com.openexchange.exception.OXException;

/**
 * {@link ExecutorContinuation} - A {@link Continuation} backed by an {@link Executor}.
 * <p>
 * Acts in favor of a <code>CompletionService</code>. Supports submitting <code>Runnable</code>s/<code>Callable</code>s.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public class ExecutorContinuation<V> implements Continuation<Collection<V>> {

    private static final long serialVersionUID = -5214485512866728446L;

    private static final class QueueingFuture<R> extends FutureTask<R> {

        private final BlockingQueue<Future<R>> queue;

        QueueingFuture(final Callable<R> c, final BlockingQueue<Future<R>> queue) {
            super(c);
            this.queue = queue;
        }

        QueueingFuture(final Runnable t, final R r, final BlockingQueue<Future<R>> queue) {
            super(t, r);
            this.queue = queue;
        }

        @Override
        protected void done() {
            queue.add(this);
        }
    }

    /**
     * May be passed to an instance of {@link ExecutorContinuation} to customize generating a continuation result; e.g. apply
     * sorting/filtering or shrink to certain ranges.
     */
    public static interface ContinuationResponseGenerator<V> {

        /**
         * Yields a <code>ContinuationResponse</code> for given results; e.g. apply sorting/filtering or shrink to certain ranges.
         *
         * @param col The results
         * @param completed Whether continuation is completed or not
         * @return The prepared results
         */
        ContinuationResponse<Collection<V>> responseFor(List<V> col, boolean completed) throws OXException;
    }

    /**
     * Creates a new <code>ExecutorContinuation</code> instance using given executor.
     *
     * @param executor The executor to use
     * @return The new <code>ExecutorContinuation</code> instance
     */
    public static <V> ExecutorContinuation<V> newContinuation(final Executor executor) {
        return newContinuation(executor, null);
    }

    /**
     * Creates a new <code>ExecutorContinuation</code> instance using given executor that delegates to optional <code>responseGenerator</code>.
     *
     * @param executor The executor to use
     * @param responseGenerator The optional response generator used for e.g. apply sorting/filtering or shrink to certain ranges
     * @return The new <code>ExecutorContinuation</code> instance
     */
    public static <V> ExecutorContinuation<V> newContinuation(final Executor executor, final ContinuationResponseGenerator<V> responseGenerator) {
        if (null == executor) {
            throw new IllegalArgumentException("executor is null");
        }
        return new ExecutorContinuation<V>(executor, responseGenerator);
    }

    // ------------------------------------------------------------------------------ //

    /** The UUID */
    protected final UUID uuid;

    /** The backing executor */
    protected final Executor executor;

    /** An extended <code>LinkedBlockingQueue</code> that offers special <code>pollUntilElapsed()</code> method */
    protected final TimeAwareBlockingQueue<Future<Collection<V>>> completionQueue;

    /** A list containing already completed tasks */
    protected final List<Future<Collection<V>>> completedFutures;

    /** The number of submitted tasks */
    protected int count;

    /** The optional response generator */
    protected final ContinuationResponseGenerator<V> responseGenerator;

    /** Signals whether this continuation has been canceled */
    protected boolean canceled;

    private final int hash;

    /**
     * Initializes a new {@link ExecutorContinuation}.
     *
     * @param executor The executor to use
     * @param responseGenerator The response generator
     */
    private ExecutorContinuation(final Executor executor, final ContinuationResponseGenerator<V> responseGenerator) {
        super();
        this.executor = executor;
        uuid = UUID.randomUUID();
        completionQueue = new TimeAwareBlockingQueue<Future<Collection<V>>>();
        completedFutures = new LinkedList<Future<Collection<V>>>();
        count = 0;
        this.responseGenerator = responseGenerator;
        hash = uuid.hashCode();
    }

    /**
     * Submits a value-returning task for execution.
     *
     * @param task The task to submit
     * @return A Future representing pending completion of the task
     * @throws RejectedExecutionException If the task cannot be scheduled for execution
     * @throws NullPointerException If the task is <code>null</code>
     */
    public synchronized void submit(final Callable<Collection<V>> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        final QueueingFuture<Collection<V>> f = new QueueingFuture<Collection<V>>(task, completionQueue);
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
    public synchronized void submit(final Runnable task, final Collection<V> result) {
        if (task == null) {
            throw new NullPointerException();
        }
        final QueueingFuture<Collection<V>> f = new QueueingFuture<Collection<V>>(task, result, completionQueue);
        executor.execute(f);
        count++;
    }

    @Override
    public synchronized void cancel(final boolean mayInterruptIfRunning) {
        if (canceled || (completedFutures.size() == count)) {
            // Already canceled or completed
            return;
        }

        // Stop execution of running/pending ones
        for (final Future<Collection<V>> future : completionQueue) {
            future.cancel(mayInterruptIfRunning);
        }
        // Clear completed ones
        completedFutures.clear();

        canceled = true;
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
        if (canceled) {
            throw ContinuationExceptionCodes.CONTINUATION_CANCELED.create(uuid);
        }

        int completedCount = completedFutures.size();
        if (completedCount == count) {
            return new ContinuationResponse<Collection<V>>(defaultResponse, null, null, true);
        }

        // Await elements
        final List<Future<Collection<V>>> polled = completionQueue.pollUntilElapsed(time, unit, count - completedCount);
        if (polled.isEmpty()) {
            // Time elapsed, but no element available
            return new ContinuationResponse<Collection<V>>(defaultResponse, null, null, false);
        }
        // Update completed information
        completedFutures.addAll(polled);
        completedCount += polled.size();

        final List<V> retval = new ArrayList<V>(completedFutures.size() << 2);
        for (final Future<Collection<V>> completedFuture : completedFutures) {
            try {
                final Collection<V> collection = completedFuture.get();
                for (final V value : collection) {
                    retval.add(value);
                }
            } catch (final ExecutionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof OXException) {
                    throw (OXException) cause;
                }
                throw ContinuationExceptionCodes.UNEXPECTED_ERROR.create(cause, cause.getMessage());
            }
        }
        return responseFor(retval, completedCount == count);
    }

    /**
     * Awaits but does not retrieve the first response.
     *
     * @throws OXException If this continuation has been canceled
     * @throws InterruptedException If thread was interrupted
     */
    public void awaitFirstResponse() throws OXException, InterruptedException {
        synchronized (this) {
            if (canceled) {
                throw ContinuationExceptionCodes.CONTINUATION_CANCELED.create(uuid);
            }

            final int completedCount = completedFutures.size();
            if (completedCount == count) {
                return;
            }
        }
        // Await...
        completionQueue.await();
    }

    /**
     * Yields a <code>ContinuationResponse</code> for given results; e.g. apply sorting/filtering or shrink to certain ranges.
     *
     * @param col The results
     * @param completed Whether continuation is completed or not
     * @return The prepared results
     * @throws OXException If generating response fails
     */
    protected ContinuationResponse<Collection<V>> responseFor(final List<V> col, final boolean completed) throws OXException {
        final ContinuationResponseGenerator<V> responseGenerator = this.responseGenerator;
        if (null == responseGenerator) {
            return new ContinuationResponse<Collection<V>>(col, null, "json", completed);
        }
        return responseGenerator.responseFor(col, completed);
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
