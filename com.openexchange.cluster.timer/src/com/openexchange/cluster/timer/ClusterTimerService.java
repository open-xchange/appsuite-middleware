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

package com.openexchange.cluster.timer;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link ClusterTimerService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @deprecated Use DatabaseCleanUpService instead
 */
@Deprecated
public interface ClusterTimerService {

    /**
     * Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given
     * period; that is executions will commence after <tt>initialDelay</tt> then <tt>initialDelay+period</tt>, then
     * <tt>initialDelay + 2 * period</tt>, and so on. If any execution of the task encounters an exception, subsequent executions are
     * suppressed. Otherwise, the task will only terminate via cancellation or termination of the executor.
     * <p/>
     * In contrast to the method offered by the regular {@link TimerService}, the scheduled tasks are executed only once in the cluster,
     * each time the trigger interval is elapsed. Note that the interval until the next execution may not be accurate after the node who
     * executed the last task leaves the cluster, i.e. it may vary up until the next node's timer elapses.
     *
     * @param id The cluster-wide unique identifier of the periodic action
     * @param task The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period The period between successive executions.
     * @param unit The time unit of the initialDelay and period parameters
     * @return A cancelable scheduled timer task representing pending completion of the task.
     * @throws RejectedExecutionException If task cannot be scheduled for execution.
     * @throws NullPointerException If command is <code>null</code>
     * @throws IllegalArgumentException If period is less than or equal to zero.
     */
    ScheduledTimerTask scheduleAtFixedRate(String id, Runnable task, long initialDelay, long period, TimeUnit unit);

    /**
     * Convenience method that invokes {@link #scheduleAtFixedRate(Runnable, long, long, TimeUnit)} with time unit set to
     * {@link TimeUnit#MILLISECONDS}.
     * <p>
     * Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given
     * period; that is executions will commence after <tt>initialDelay</tt> then <tt>initialDelay+period</tt>, then
     * <tt>initialDelay + 2 * period</tt>, and so on. If any execution of the task encounters an exception, subsequent executions are
     * suppressed. Otherwise, the task will only terminate via cancellation or termination of the executor.
     * <p/>
     * In contrast to the method offered by the regular {@link TimerService}, the scheduled tasks are executed only once in the cluster,
     * each time the trigger interval is elapsed. Note that the interval until the next execution may not be accurate after the node who
     * executed the last task leaves the cluster, i.e. it may vary up until the next node's timer elapses.
     *
     * @param id The cluster-wide unique identifier of the periodic action
     * @param task The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param period The period between successive executions.
     * @return A cancelable scheduled timer task representing pending completion of the task.
     * @throws RejectedExecutionException If task cannot be scheduled for execution.
     * @throws NullPointerException If command is <code>null</code>
     * @throws IllegalArgumentException If period is less than or equal to zero.
     */
    ScheduledTimerTask scheduleAtFixedRate(String id, Runnable task, long initialDelay, long period);

    /**
     * Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given
     * delay between the termination of one execution and the commencement of the next. If any execution of the task encounters an
     * exception, subsequent executions are suppressed. Otherwise, the task will only terminate via cancellation or termination of the
     * executor.
     * <p/>
     * In contrast to the method offered by the regular {@link TimerService}, the scheduled tasks are executed only once in the cluster,
     * each time the trigger interval is elapsed. Note that the interval until the next execution may not be accurate after the node who
     * executed the last task leaves the cluster, i.e. it may vary up until the next node's timer elapses.
     * <p/>
     * Note that the task's typical runtime should be shorter than the configured delay, otherwise it's currently not guaranteed that no
     * other node in the cluster will start another execution after the delay is elapsed.
     *
     * @param id The cluster-wide unique identifier of the periodic action
     * @param task The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param delay The delay between the termination of one execution and the commencement of the next.
     * @param unit The time unit of the initialDelay and delay parameters
     * @return A cancelable scheduled timer task representing pending completion of the task.
     * @throws RejectedExecutionException If task cannot be scheduled for execution.
     * @throws NullPointerException If command is <code>null</code>
     * @throws IllegalArgumentException If delay is less than or equal to zero.
     */
    ScheduledTimerTask scheduleWithFixedDelay(String id, Runnable task, long initialDelay, long delay, TimeUnit unit);

    /**
     * Convenience method that invokes {@link #scheduleWithFixedDelay(Runnable, long, long, TimeUnit)} with time unit set to
     * {@link TimeUnit#MILLISECONDS}.
     * <p>
     * Creates and executes a periodic action that becomes enabled first after the given initial delay, and subsequently with the given
     * delay between the termination of one execution and the commencement of the next. If any execution of the task encounters an
     * exception, subsequent executions are suppressed. Otherwise, the task will only terminate via cancellation or termination of the
     * executor.
     * <p/>
     * In contrast to the method offered by the regular {@link TimerService}, the scheduled tasks are executed only once in the cluster,
     * each time the trigger interval is elapsed. Note that the interval until the next execution may not be accurate after the node who
     * executed the last task leaves the cluster, i.e. it may vary up until the next node's timer elapses.
     * <p/>
     * Note that the task's typical runtime should be shorter than the configured delay, otherwise it's currently not guaranteed that no
     * other node in the cluster will start another execution after the delay is elapsed.
     *
     * @param id The cluster-wide unique identifier of the periodic action
     * @param task The task to execute.
     * @param initialDelay The time to delay first execution.
     * @param delay The delay between the termination of one execution and the commencement of the next.
     * @return A cancelable scheduled timer task representing pending completion of the task.
     * @throws RejectedExecutionException If task cannot be scheduled for execution.
     * @throws NullPointerException If command is <code>null</code>
     * @throws IllegalArgumentException If delay is less than or equal to zero.
     */
    ScheduledTimerTask scheduleWithFixedDelay(String id, Runnable task, long initialDelay, long delay);

}
