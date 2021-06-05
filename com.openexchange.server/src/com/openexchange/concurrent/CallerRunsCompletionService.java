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

package com.openexchange.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import com.openexchange.threadpool.Task;

/**
 * {@link CallerRunsCompletionService} - A {@link CompletionService} that uses submitting thread to perform the task.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CallerRunsCompletionService<V> implements CompletionService<V> {

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

    private final BlockingQueue<Future<V>> completionQueue;

    /**
     * Initializes a new {@link CallerRunsCompletionService} with an unbound {@link BlockingQueue}.
     */
    public CallerRunsCompletionService() {
        super();
        this.completionQueue = new LinkedBlockingQueue<Future<V>>();
    }

    /**
     * Submits specified task to this completion service.
     *
     * @param task The task
     * @return The associated future
     */
    public Future<V> submit(final Task<V> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        final QueueingFuture<V> f = new QueueingFuture<V>(task, completionQueue);
        task.beforeExecute(Thread.currentThread());
        f.run();
        try {
            f.get();
            task.afterExecute(null);
        } catch (InterruptedException e) {
            // Cannot occur
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            task.afterExecute(e.getCause());
        }
        return f;
    }

    @Override
    public Future<V> submit(final Callable<V> task) {
        if (task == null) {
            throw new NullPointerException();
        }
        final QueueingFuture<V> f = new QueueingFuture<V>(task, completionQueue);
        f.run();
        return f;
    }

    @Override
    public Future<V> submit(final Runnable task, final V result) {
        if (task == null) {
            throw new NullPointerException();
        }
        final QueueingFuture<V> f = new QueueingFuture<V>(task, result, completionQueue);
        f.run();
        return f;
    }

    @Override
    public Future<V> take() throws InterruptedException {
        return completionQueue.take();
    }

    @Override
    public Future<V> poll() {
        return completionQueue.poll();
    }

    @Override
    public Future<V> poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        return completionQueue.poll(timeout, unit);
    }

}
