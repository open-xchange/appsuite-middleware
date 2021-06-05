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

package com.openexchange.push.impl.jobqueue;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushUser;

/**
 * {@link PermanentListenerJob} - A job scheduled for starting a permanent listener.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public interface PermanentListenerJob extends Comparable<PermanentListenerJob> {

    /**
     * Gets the associated push user.
     *
     * @return The push user
     */
    PushUser getPushUser();

    /**
     * Checks if this job completed.
     * <p>
     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * <code>true</code>.
     *
     * @return <code>true</code> if this job completed; otherwise <code>false</code>
     */
    boolean isDone();

    /**
     * Waits if necessary for the established push listener, and then returns it.
     *
     * @return The established push listener
     * @throws CancellationException If the establishing a push listener was cancelled
     * @throws ExecutionException If the establishing a push listener threw an exception
     * @throws InterruptedException If the current thread was interrupted while waiting
     */
    PushListener get() throws InterruptedException, ExecutionException;

    /**
     * Waits if necessary for at most the given time for the established push listener, and then returns it, if available.
     *
     * @param timeout The maximum time to wait
     * @param unit The time unit of the timeout argument
     * @return The established push listener
     * @throws CancellationException If the establishing a push listener was cancelled
     * @throws ExecutionException If the establishing a push listener threw an exception
     * @throws InterruptedException If the current thread was interrupted while waiting
     * @throws TimeoutException If the wait timed out
     */
    PushListener get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

}
