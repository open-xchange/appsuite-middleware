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

package com.openexchange.database.cleanup.impl.storage;

import com.openexchange.database.cleanup.CleanUpJob;
import com.openexchange.exception.OXException;

/**
 * {@link DatabaseCleanUpExecutionManagement} - Management for database clean-up executions.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public interface DatabaseCleanUpExecutionManagement {

    /**
     * Checks whether job execution is permitted.
     *
     * @param job The job whose permission is supposed to be checked
     * @param representativeContextId The representative context identifier to denote the schema the execution is supposed to run against
     * @return <code>true</code> if permission is granted; otherwise <code>false</code>
     * @throws OXException If check for execution permission fails
     */
    boolean checkExecutionPermission(CleanUpJob job, int representativeContextId) throws OXException;

    /**
     * Marks current execution of given job as done.
     *
     * @param job The job
     * @param representativeContextId The representative context identifier to denote the schema the execution is supposed to run against
     * @throws OXException If operation fails
     */
    void markExecutionDone(CleanUpJob job, int representativeContextId) throws OXException;

    /**
     * Refreshes the last-touched time stamp for given job.
     *
     * @param job The job whose last-touched time stamp shall be refreshed
     * @param representativeContextId The representative context identifier to denote the schema the execution is supposed to run against
     * @return <code>true</code> if last-touched time stamp has been successfully refreshed; otherwise <code>false</code>
     * @throws OXException If refreshing last-touched time stamp fails
     */
    boolean refreshTimeStamp(CleanUpJob job, int representativeContextId) throws OXException;

}
