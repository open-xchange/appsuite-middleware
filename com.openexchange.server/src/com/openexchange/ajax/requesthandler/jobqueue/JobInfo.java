/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
