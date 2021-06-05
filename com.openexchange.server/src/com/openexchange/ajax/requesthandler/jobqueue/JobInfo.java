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

package com.openexchange.ajax.requesthandler.jobqueue;

import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;

/**
 * {@link JobInfo}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface JobInfo {

    /**
     * Gets the job's identifier
     *
     * @return The identifier
     */
    UUID getId();

    /**
     * Gets the job
     *
     * @return The job
     */
    Job getJob();

    // ------------------------------------------------------------------------------------------

    /**
     * Attempts to cancel execution of the associated job (and implicitly drops it from job queue).
     * <p>
     * This attempt will fail if the task has already completed, has already been cancelled,
     * or could not be cancelled for some other reason.
     * <p>
     * After this method returns, subsequent calls to {@link #isDone} will
     * always return {@code true}. Subsequent calls to {@link #isCancelled}
     * will always return {@code true} if this method returned {@code true}.
     *
     * @param mayInterruptIfRunning {@code true} if the thread executing the associated job should be interrupted; otherwise, in-progress tasks are allowed to complete
     * @return {@code false} if the task could not be cancelled, typically because it has already completed normally; {@code true} otherwise
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * Checks if this task was cancelled before it completed normally.
     *
     * @return {@code true} if this task was cancelled before it completed
     */
    boolean isCancelled();

    /**
     * Checks if this task completed.
     * <p>
     * Completion may be due to normal termination, an exception, or
     * cancellation -- in all of these cases, this method will return
     * {@code true}.
     *
     * @return {@code true} if this task completed
     */
    boolean isDone();

    /**
     * Waits if necessary for the computation to complete, and then retrieves its result.
     *
     * @param removeAfterRetrieval <code>true</code> to remove this job info from queue after its result has been retrieved; otherwise <code>false</code> to leave in queue
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws OXException If the job execution threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    AJAXRequestResult get(boolean removeAfterRetrieval) throws InterruptedException, OXException;

    /**
     * Waits if necessary for at most the given time for the computation to complete, and then retrieves its result, if available.
     *
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @param removeAfterRetrieval <code>true</code> to remove this job info from queue after its result has been retrieved; otherwise <code>false</code> to leave in queue
     * @return the computed result
     * @throws CancellationException if the computation was cancelled
     * @throws OXException If the job execution threw an exception
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws TimeoutException if the wait timed out
     */
    AJAXRequestResult get(long timeout, TimeUnit unit, boolean removeAfterRetrieval) throws InterruptedException, OXException, TimeoutException;

}
