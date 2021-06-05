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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.openexchange.exception.OXException;

/**
 * {@link JobQueueService} - The dispatcher job queue.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public interface JobQueueService {

    /**
     * Gets the max. age permitted for a request in milliseconds
     *
     * @return The max. age permitted for a request in milliseconds
     * @throws OXException If value cannot be returned
     */
    long getMaxRequestAgeMillis() throws OXException;

    /**
     * Checks if this job queue already contains a job of specified kind.
     *
     * @param key The job key describing/identifying a job
     * @return The job identifier if contained; otherwise <code>null</code>
     * @throws OXException If check fails
     */
    UUID contains(JobKey key) throws OXException;

    /**
     * Enqueues specified job into this queue for execution
     *
     * @param job The job to perform
     * @return The job information
     * @throws OXException If job cannot be enqueued
     */
    JobInfo enqueue(Job job) throws OXException;

    /**
     * Enqueues specified job into this queue for execution; waiting for at most the given time for the computation to complete.
     * <p>
     * If computation completes in time; job will be not be enqueued and {@link JobInfo#get()} will immediately return its result.<br>
     * Otherwise an <code>EnqueuedException</code> is thrown providing the enqueued job information for the in-progress job.
     *
     * @param job The job to perform
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return The job information for the already executed job
     * @throws EnqueuedException Thrown in case wait time is exceeded and job was enqueued
     * @throws InterruptedException If thread gets interrupted while waiting
     * @throws OXException If job cannot be enqueued
     */
    JobInfo enqueueAndWait(Job job, long timeout, TimeUnit unit) throws EnqueuedException, InterruptedException, OXException;

    /**
     * Gets the job information for specified job identifier and user.
     *
     * @param id The job identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The job information or <code>null</code>
     * @throws OXException If job info cannot be returned
     */
    JobInfo get(UUID id, int userId, int contextId) throws OXException;

    /**
     * Requires the job information for specified job identifier and user.
     *
     * @param id The job identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The job information (never <code>null</code>)
     * @throws OXException If job info cannot be returned
     */
    JobInfo require(UUID id, int userId, int contextId) throws OXException;

    /**
     * Gets the job information representations for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The job information representations
     * @throws OXException If job information representations cannot be returned
     */
    List<JobInfo> getAllFor(int userId, int contextId) throws OXException;

    /**
     * Gets the job information for specified job identifier and user.
     * <p>
     * Removes if from job queue if already {@link JobInfo#isDone() done}.
     *
     * @param id The job identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The job information or <code>null</code>
     * @throws OXException If job info cannot be returned
     */
    JobInfo getAndRemoveIfDone(UUID id, int userId, int contextId) throws OXException;

    /**
     * Clears this job queue and terminates all remaining jobs.
     */
    void clear();

}
