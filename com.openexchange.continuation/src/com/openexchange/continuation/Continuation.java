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

package com.openexchange.continuation;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;

/**
 * {@link Continuation} - Represents a continuing/background AJAX request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public interface Continuation<V> extends Serializable {

    /**
     * Gets the UUID.
     *
     * @return The UUID
     */
    UUID getUuid();

    /**
     * Attempts to cancel execution of this continuation.
     * <p>
     * This attempt will fail if the continuation has already completed, has already been cancelled, or could not be cancelled for some
     * other reason. If successful, and this continuation has not started when <tt>cancel</tt> is called, this continuation should never
     * run. If the continuation has already started, then the <tt>mayInterruptIfRunning</tt> parameter determines whether the thread
     * executing this task should be interrupted in an attempt to stop the task.
     *
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this continuation should be interrupted; otherwise, in-progress
     *            continuations are allowed to complete
     */
    void cancel(boolean mayInterruptIfRunning);

    /**
     * Gets the next available value.
     *
     * @param time The maximum time to wait
     * @param unit The time unit of the {@code time} argument
     * @return The next available value or <code>null</code>
     * @throws OXException If awaiting next available response fails
     * @throws InterruptedException If the current thread is interrupted
     */
    ContinuationResponse<V> getNextResponse(long time, TimeUnit unit) throws OXException, InterruptedException;

    /**
     * Gets the next available value.
     *
     * @param time The maximum time to wait
     * @param unit The time unit of the {@code time} argument
     * @param defaultValue The default response to return if no next value was available in given time span
     * @return The next available value or given <code>defaultValue</code>
     * @throws OXException If awaiting next available response fails
     * @throws InterruptedException If the current thread is interrupted
     */
    ContinuationResponse<V> getNextResponse(long time, TimeUnit unit, V defaultResponse) throws OXException, InterruptedException;

}
