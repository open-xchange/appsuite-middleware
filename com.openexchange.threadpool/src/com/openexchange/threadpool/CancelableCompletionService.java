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

import java.util.concurrent.CompletionService;

/**
 * {@link CancelableCompletionService} - Extends {@link CompletionService} interface by {@link #cancel(boolean)} method.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface CancelableCompletionService<V> extends CompletionService<V> {

    /**
     * Attempts to cancel execution of this completion service. The attempt will fail for tasks that have already completed, have already
     * been cancelled, or could not be cancelled for some other reason. If a task has already started, then the
     * <tt>mayInterruptIfRunning</tt> parameter determines whether the thread executing this task should be interrupted in an attempt to
     * stop the task.
     *
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing a task should be interrupted; otherwise, in-progress tasks are
     *            allowed to complete
     */
    public void cancel(boolean mayInterruptIfRunning);
}
