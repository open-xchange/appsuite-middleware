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

package com.openexchange.sessiond.impl;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link TimeoutTaskWrapper} - Simple wrapper to delegate task execution with respect to a timeout.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @param <V> The task's return type
 */
final class TimeoutTaskWrapper<V> extends AbstractTask<V> {

    /**
     * Submits given task to thread pool for execution using a wrapping {@code TimeoutTaskWrapper} instance
     *
     * @param task The task to submit
     */
    static <V> void submit(Task<V> task) {
        try {
            ThreadPools.getThreadPool().submit(new TimeoutTaskWrapper<V>(task));
        } catch (RejectedExecutionException e) {
            try {
                ThreadPools.execute(task);
            } catch (Exception x) {
                // Ignore
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------- //

    private final Task<V> task;
    private final V defaultValue;

    /**
     * Initializes a new {@link TimeoutTaskWrapper}.
     *
     * @param task The task to execute
     */
    TimeoutTaskWrapper(Task<V> task) {
        this(task, null);
    }

    /**
     * Initializes a new {@link TimeoutTaskWrapper}.
     *
     * @param task The task to execute
     * @param defaultValue The default value to return in case timeout will be exceeded
     */
    TimeoutTaskWrapper(Task<V> task, V defaultValue) {
        super();
        this.task = task;
        this.defaultValue = defaultValue;
    }

    @Override
    public V call() throws Exception {
        Future<V> f = ThreadPools.getThreadPool().submit(task);
        try {
            return f.get(SessionHandler.timeout(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            f.cancel(true);
            return defaultValue;
        } catch (ExecutionException e) {
            throw ThreadPools.launderThrowable(e, Exception.class);
        } catch (CancellationException e) {
            return defaultValue;
        }
    }

}
