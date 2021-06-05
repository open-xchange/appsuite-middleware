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

package com.openexchange.timer.internal;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import com.openexchange.threadpool.DelayedQueueProvider;
import com.openexchange.threadpool.internal.CustomThreadPoolExecutor;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link CustomThreadPoolExecutorTimerService} - The {@link TimerService} implementation backed by a {@link CustomThreadPoolExecutor}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CustomThreadPoolExecutorTimerService implements TimerService, DelayedQueueProvider {

    private final CustomThreadPoolExecutor executorService;

    /**
     * Initializes a new {@link CustomThreadPoolExecutorTimerService}.
     * @param executorService The {@link CustomThreadPoolExecutor} 
     */
    public CustomThreadPoolExecutorTimerService(final CustomThreadPoolExecutor executorService) {
        super();
        this.executorService = executorService;
        /*
         * Add a task to frequently purge canceled tasks
         */
        final class PurgeRunnable implements Runnable {

            private final CustomThreadPoolExecutor exec;

            public PurgeRunnable(final CustomThreadPoolExecutor exec) {
                super();
                this.exec = exec;
            }

            @Override
            public void run() {
                exec.purge();
            }

        }
        executorService.scheduleWithFixedDelay(new PurgeRunnable(executorService), 30L, 30L, TimeUnit.SECONDS);
    }

    @Override
    public BlockingQueue<Runnable> getDelayedWorkQueue() {
        return executorService.getDelayedWorkQueue();
    }

    @Override
    public ScheduledTimerTask schedule(final Runnable command, final long delay, final TimeUnit unit) {
        return new WrappingScheduledTimerTask(executorService.schedule(command, delay, unit));
    }

    @Override
    public ScheduledTimerTask schedule(final Runnable command, final long delay) {
        return schedule(command, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledTimerTask scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period, final TimeUnit unit) {
        return new WrappingScheduledTimerTask(executorService.scheduleAtFixedRate(command, initialDelay, period, unit));
    }

    @Override
    public ScheduledTimerTask scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period) {
        return scheduleAtFixedRate(command, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    @Override
    public ScheduledTimerTask scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay, final TimeUnit unit) {
        return new WrappingScheduledTimerTask(executorService.scheduleWithFixedDelay(command, initialDelay, delay, unit));
    }

    @Override
    public ScheduledTimerTask scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay) {
        return scheduleWithFixedDelay(command, initialDelay, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void purge() {
        executorService.purge();
    }

    @Override
    public Executor getExecutor() {
        return executorService;
    }

}
