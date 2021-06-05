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

import java.time.Duration;

/**
 * {@link CleanUpJob} - A clean-up job which is supposed to periodically executed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public interface CleanUpJob {

    /**
     * Gets the unique job identifier.
     *
     * @return The job identifier
     */
    CleanUpJobId getId();

    /**
     * Gets the execution.
     *
     * @return The execution
     */
    CleanUpExecution getExecution();

    /**
     * Gets the duration to delay first execution.
     *
     * @return The duration to delay first execution
     */
    Duration getInitialDelay();

    /**
     * Gets the delay between the termination of one execution and the commencement of the next.
     *
     * @return The delay
     */
    Duration getDelay();

    /**
     * Checks whether this job should run exclusively or if concurrent executions are allowed.
     *
     * @return <code>true</code> for exclusive executions; otherwise <code>false</code> for concurrent executions
     */
    boolean isRunsExclusive();

    /**
     * Gets a value indicating whether the jobs prefers using a database connection without timeout to omit warnings during long running
     * tasks.
     *
     * @return <code>true</code> if the job prefers a database connection w/o timeout, <code>false</code>, otherwise
     */
    boolean isPreferNoConnectionTimeout();

}
