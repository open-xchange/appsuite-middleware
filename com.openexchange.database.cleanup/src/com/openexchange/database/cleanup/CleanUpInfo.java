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

package com.openexchange.database.cleanup;


/**
 * {@link CleanUpInfo} - Information about a scheduled clean-up job.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public interface CleanUpInfo {

    /**
     * Attempts to cancel execution of associated clean-up job. This attempt will return <code>false</code> if the job has already
     * completed or already been canceled.
     * <p>
     * If successful, and associated clean-up job has not started when <tt>cancel()</tt> is called, associated clean-up job should never run.
     * <p>
     * If the clean-up job has already started, then the <tt>mayInterruptIfRunning</tt> parameter determines whether the thread executing
     * job's execution should be interrupted in an attempt to stop the execution.
     *
     * @param mayInterruptIfRunning <code>true</code> if the thread executing this job should be interrupted; otherwise, in-progress executions
     *                              are allowed to complete
     * @return <code>false</code> if the clean-up job could not be canceled, typically because it has already completed normally;
     *          <code>true</code> otherwise
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * Gets identifier of the clean-up job.
     *
     * @return The clean-up job identifier
     */
    CleanUpJobId getJobId();
}
