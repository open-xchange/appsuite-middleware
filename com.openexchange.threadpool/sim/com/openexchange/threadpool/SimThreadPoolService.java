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

package com.openexchange.threadpool;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.openexchange.threadpool.internal.FixedExecutorService;

/**
 * {@link SimThreadPoolService}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class SimThreadPoolService implements ThreadPoolService {

    private ExecutorService executor;

    private final int corePoolSize;

    public SimThreadPoolService() {
        super();
        executor = Executors.newSingleThreadExecutor();
        corePoolSize = getCorePoolSize();
    }

    private static int getCorePoolSize() {
        return Runtime.getRuntime().availableProcessors() + 1;
    }

    @Override
    public int getActiveCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getCompletedTaskCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExecutorService getExecutor() {
        return executor;
    }

    @Override
    public ExecutorService getFixedExecutor(final int size) {
        return new FixedExecutorService(size, executor);
    }

    @Override
    public ExecutorService getFixedExecutor() {
        return new FixedExecutorService(corePoolSize, executor);
    }

    @Override
    public int getLargestPoolSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPoolSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getTaskCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> CompletionFuture<T> invoke(final Collection<? extends Task<T>> tasks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> CompletionFuture<T> invoke(final Task<T>[] tasks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> CompletionFuture<T> invoke(final Collection<? extends Task<T>> tasks, final RefusedExecutionBehavior<T> refusedExecutionBehavior) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Task<T>> tasks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Task<T>> tasks, final long timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShutdown() {
        return null == executor;
    }

    @Override
    public boolean isTerminated() {
        return null == executor;
    }

    @Override
    public <T> Future<T> submit(final Task<T> task) {
        return executor.submit(task);
    }

    @Override
    public <T> Future<T> submit(final Task<T> task, final RefusedExecutionBehavior<T> refusedExecutionBehavior) {
        throw new UnsupportedOperationException();
    }

    public void shutdown() {
        executor.shutdown();
        executor = null;
    }
}
