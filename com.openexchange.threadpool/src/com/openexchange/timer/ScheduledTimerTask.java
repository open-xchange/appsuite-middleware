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

/**
 * {@link ScheduledTimerTask} - Represents a cancelable timer task scheduled to a {@link TimerService timer service}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ScheduledTimerTask {

    /**
     * Attempts to cancel execution of this task. This attempt will return <code>false</code> if the task has already completed or already
     * been canceled.
     * <p>
     * If successful, and this task has not started when <tt>cancel()</tt> is called, this task should never run.
     * <p>
     * If the task has already started, then the <tt>mayInterruptIfRunning</tt> parameter determines whether the thread executing this task
     * should be interrupted in an attempt to stop the task.
     *
     * @param mayInterruptIfRunning <code>true</code> if the thread executing this task should be interrupted; otherwise, in-progress tasks
     *            are allowed to complete
     * @return <code>false</code> if the task could not be canceled, typically because it has already completed normally; <code>true</code>
     *         otherwise
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * A convenience method that invokes {@link #cancel(boolean)} with argument set to <code>false</code>. Thus in-progress tasks are
     * allowed to complete prior to attempting to cancel execution of this task. This attempt will return <code>false</code> if the task has
     * already completed or already been canceled.
     * <p>
     * If successful, and this task has not started when <tt>cancel()</tt> is called, this task should never run.
     *
     * @return <code>false</code> if the task could not be canceled, typically because it has already completed normally; <code>true</code>
     *         otherwise
     */
    boolean cancel();
}
