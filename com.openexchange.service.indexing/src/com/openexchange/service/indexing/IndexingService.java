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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.service.indexing;

import java.util.Date;

import com.openexchange.exception.OXException;


/**
 * {@link IndexingService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public interface IndexingService {

    /**
     * Default priority for jobs.
     */
    public static final int DEFAULT_PRIORITY = 5;

    /**
     * Use this to schedule one-shot jobs.
     */
    public static final long NO_INTERVAL = -1L;

    /**
     * Use this if the job should start immediately.
     */
    public static final Date NOW = null;


    /**
     * Schedules a job with a progressive interval. If the the job is already known, it will be updated.
     * That means the timeout and the interval will be reset. The progression starts again.
     * After every run the next fire time is based on the last interval and the progression rate.
     *
     * @param info The information needed to run this job.
     * @param startDate The start date of the job. May be <code>null</code> to run immediately.
     * @param timeout The timeout in ms. Must be > 0. A job will be removed automatically after 
     * this amount of time if the job was not updated in the meantime.
     * @param progressionRate The progression rate in percent. Must be > 0. The new interval is always increased by this value.
     * @param initialInterval The initial repeat interval in milliseconds. Must be > 0. The first run starts at startDate.
     * The second one at startDate + initialInterval. Every further runs start time is calculated based on the progression
     * rate and the initial interval.
     * @param priority The priority. If two jobs shall be started at the same time, the one with the higher priority wins.
     * See {@link IndexingService#DEFAULT_PRIORITY}.
     * @param onlyResetProgression If <code>true</code> only the progression and job timeout will be reset and no triggers will be activated.
     * If the job was unknown before, triggers will be added nevertheless to ensure proper indexing.
     * @throws OXException 
     */
    void scheduleJobWithProgressiveInterval(JobInfo info, Date startDate, long timeout, long initialInterval, int progressionRate, int priority, boolean onlyResetProgression) throws OXException;
    
    /**
     * Schedules an indexing job.
     *
     * @param async If <code>true</code> the call returns immediately and the scheduling will be performed asynchronously.
     * @param info The information needed to run this job.
     * @param startDate The start date of the job. May be <code>null</code> to run immediately.
     * @param repeatInterval The repeat interval in milliseconds. May be negative if the job shall only run once.
     * @param priority The priority. If two jobs shall be started at the same time, the one with the higher priority wins. See {@link IndexingService#DEFAULT_PRIORITY}.
     * @throws OXException
     */
    void scheduleJob(boolean async, JobInfo info, Date startDate, long repeatInterval, int priority) throws OXException;

    /**
     * Deletes an indexing job from the scheduler.
     *
     * @param async If <code>true</code> the call returns immediately and the unscheduling will be performed asynchronously.
     * @param info The information needed to delete this job.
     * @throws OXException
     */
    void unscheduleJob(boolean async, JobInfo info) throws OXException;

    /**
     * Deletes all jobs for a given user from the scheduler.
     *
     * @param async If <code>true</code> the call returns immediately and the unscheduling will be performed asynchronously.
     * @param contextId The context id.
     * @param userId The user id.
     * @throws OXException
     */
    void unscheduleAllForUser(boolean async, int contextId, int userId) throws OXException;

    /**
     * Deletes all jobs for a given context.
     *
     * @param async If <code>true</code> the call returns immediately and the unscheduling will be performed asynchronously.
     * @param contextId The context id.
     * @throws OXException
     */
    void unscheduleAllForContext(boolean async, int contextId) throws OXException;

}
