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

package com.openexchange.timer;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * A {@link TimerService} that can schedule commands to run after a given delay, or to execute periodically.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@SingletonService
public interface TimerService {

    /**
     * Creates and executes a one-shot action that becomes enabled after the given delay.
     *
     * @param task The task to execute.
     * @param delay The time from now to delay execution.
     * @param unit The time unit of the delay parameter.
     * @return A cancelable scheduled timer task representing pending completion of the task.
     * @throws RejectedExecutionException If task cannot be scheduled for execution.
     * @throws NullPointerException If command is <code>null</code>
     */
    public ScheduledTimerTask schedule(Runnable task, long delay, TimeUnit unit);

    /**
     * Convenience method that invokes {@link #schedule(Runnable, long, TimeUnit)} with time unit set to {@link TimeUnit#MILLISECONDS}.
     * <p>
     * Creates and executes a one-shot action that becomes enabled after the given delay.
     *
     * @param task The task to execute.
     * @param delay The time from now to delay execution.
     * @return A cancelable scheduled timer task representing pending completion of the task.
     * @throws RejectedExecutionException If task cannot be scheduled for execution.
     * @throws NullPointerException If command is <code>null</code>
     */
    public ScheduledTimerTask schedule(Runnable task, long delay);

    /**
     * Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given
     * period; that is executions will commence after <tt>initialDelay</tt> then <tt>initialDelay+period</tt>, then
     * <tt>initialDelay + 2 * period</tt>, and so on. If any execution of the task encounters an exception, subsequent executions are
     * suppressed. Otherwise, the task will only terminate via cancellation or termination of the executor.
     *
     * @param task The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period The period between successive executions.
     * @param unit The time unit of the initialDelay and period parameters
     * @return A cancelable scheduled timer task representing pending completion of the task.
     * @throws RejectedExecutionException If task cannot be scheduled for execution.
     * @throws NullPointerException If command is <code>null</code>
     * @throws IllegalArgumentException If period is less than or equal to zero.
     */
    public ScheduledTimerTask scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit);

    /**
     * Convenience method that invokes {@link #scheduleAtFixedRate(Runnable, long, long, TimeUnit)} with time unit set to
     * {@link TimeUnit#MILLISECONDS}.
     * <p>
     * Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given
     * period; that is executions will commence after <tt>initialDelay</tt> then <tt>initialDelay+period</tt>, then
     * <tt>initialDelay + 2 * period</tt>, and so on. If any execution of the task encounters an exception, subsequent executions are
     * suppressed. Otherwise, the task will only terminate via cancellation or termination of the executor.
     *
     * @param task The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period The period between successive executions.
     * @return A cancelable scheduled timer task representing pending completion of the task.
     * @throws RejectedExecutionException If task cannot be scheduled for execution.
     * @throws NullPointerException If command is <code>null</code>
     * @throws IllegalArgumentException If period is less than or equal to zero.
     */
    public ScheduledTimerTask scheduleAtFixedRate(Runnable task, long initialDelay, long period);

    /**
     * Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given
     * delay between the termination of one execution and the commencement of the next. If any execution of the task encounters an
     * exception, subsequent executions are suppressed. Otherwise, the task will only terminate via cancellation or termination of the
     * executor.
     *
     * @param task The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param delay The delay between the termination of one execution and the commencement of the next.
     * @param unit The time unit of the initialDelay and delay parameters
     * @return A cancelable scheduled timer task representing pending completion of the task.
     * @throws RejectedExecutionException If task cannot be scheduled for execution.
     * @throws NullPointerException If command is <code>null</code>
     * @throws IllegalArgumentException If delay is less than or equal to zero.
     */
    public ScheduledTimerTask scheduleWithFixedDelay(Runnable task, long initialDelay, long delay, TimeUnit unit);

    /**
     * Convenience method that invokes {@link #scheduleWithFixedDelay(Runnable, long, long, TimeUnit)} with time unit set to
     * {@link TimeUnit#MILLISECONDS}.
     * <p>
     * Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given
     * delay between the termination of one execution and the commencement of the next. If any execution of the task encounters an
     * exception, subsequent executions are suppressed. Otherwise, the task will only terminate via cancellation or termination of the
     * executor.
     *
     * @param task The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param delay The delay between the termination of one execution and the commencement of the next.
     * @return A cancelable scheduled timer task representing pending completion of the task.
     * @throws RejectedExecutionException If task cannot be scheduled for execution.
     * @throws NullPointerException If command is <code>null</code>
     * @throws IllegalArgumentException If delay is less than or equal to zero.
     */
    public ScheduledTimerTask scheduleWithFixedDelay(Runnable task, long initialDelay, long delay);

    /**
     * Tries to remove from the work queue all tasks that have been canceled.
     * <p>
     * This method can be useful as a storage reclamation operation, that has no other impact on functionality. Canceled tasks are never
     * executed, but may accumulate in work queues until worker threads can actively remove them. Invoking this method instead tries to
     * remove them now. However, this method may fail to remove tasks in the presence of interference by other threads.
     */
    public void purge();

    /**
     * Returns an {@link Executor view on this timer service.
     *
     * @return An unmodifiable {@link Executor} backing this timer service
     */
    public Executor getExecutor();
}
