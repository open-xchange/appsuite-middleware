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

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link DatabaseCleanUpService} - The service to schedule database clean-up jobs.
 * <p>
 * Example:
 * <pre>
 *     DatabaseCleanUpServiceImpl service = ...;
 *
 *     CleanUpExecution execution = new CleanUpExecution() {
 *
 *          public boolean isApplicableFor(String schema, int representativeContextId, int databasePoolId) throws OXException {
 *              return true;
 *          }
 *
 *          public void executeFor(String schema, int representativeContextId, int databasePoolId, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
 *              System.out.println("Cleaning it all...");
 *          }
 *      };
 *      DefaultCleanUpJob job = DefaultCleanUpJob.builder().
 *          withId(CleanUpJobId.newInstanceFor("com.openexchange.database.cleanup.Test")).
 *          withDelay(Duration.ofSeconds(15)).
 *          withInitialDelay(Duration.ofSeconds(5)).
 *          withRunsExclusive(true).
 *          withExecution(execution).
 *          build();
 *      service.scheduleCleanUpJob(job);
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
@SingletonService
public interface DatabaseCleanUpService {

    /**
     * Schedules given clean-up job.
     *
     * @param job The clean-up job
     * @return The job info
     * @throws OXException If clean-up job cannot be scheduled
     */
    CleanUpInfo scheduleCleanUpJob(CleanUpJob job) throws OXException;

    /**
     * Gets the currently submitted clean-up jobs.
     *
     * @return The identifiers of currently submitted clean-up jobs
     * @throws OXException If clean-up jobs cannot be returned
     */
    List<String> getCleanUpJobs() throws OXException;

}
