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
