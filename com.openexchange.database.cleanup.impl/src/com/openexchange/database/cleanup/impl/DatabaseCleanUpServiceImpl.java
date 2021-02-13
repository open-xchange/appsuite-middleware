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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.database.cleanup.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import com.openexchange.database.cleanup.CleanUpInfo;
import com.openexchange.database.cleanup.CleanUpJob;
import com.openexchange.database.cleanup.CleanUpJobId;
import com.openexchange.database.cleanup.DatabaseCleanUpExceptionCode;
import com.openexchange.database.cleanup.DatabaseCleanUpService;
import com.openexchange.database.cleanup.impl.storage.DatabaseCleanUpExecutionManagement;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;


/**
 * {@link DatabaseCleanUpServiceImpl} - The database clean-up service implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class DatabaseCleanUpServiceImpl implements DatabaseCleanUpService {

    /** The interval in milliseconds for refreshing a job's last-touched time stamp */
    public static final long REFRESH_LAST_TOUCHED_STAMP_INTERVAL_MILLIS = 20000L;

    /** The expiration in milliseconds. If a task is idle for that long, is is assumed to be expired/stalled. */
    public static final long EXPIRATION_MILLIS = 60000L;

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ConcurrentMap<CleanUpJobId, Future<ScheduledTimerTask>> submittedJobs;
    private final DatabaseCleanUpExecutionManagement executionManagement;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link DatabaseCleanUpServiceImpl}.
     *
     * @param executionManagement The execution management to use
     * @param services The service look-up
     */
    public DatabaseCleanUpServiceImpl(DatabaseCleanUpExecutionManagement executionManagement, ServiceLookup services) {
        super();
        this.services = services;
        this.executionManagement = executionManagement;
        submittedJobs = new ConcurrentHashMap<>(32, 0.9F, 1);
    }

    @Override
    public List<String> getCleanUpJobs() throws OXException {
        List<String> identifiers = new ArrayList<>(submittedJobs.size());
        for (CleanUpJobId jobId : submittedJobs.keySet()) {
            identifiers.add(jobId.getIdentifier());
        }
        return identifiers;
    }

    @Override
    public CleanUpInfo scheduleCleanUpJob(CleanUpJob job) throws OXException {
        // Require timer service
        TimerService timerService = services.getServiceSafe(TimerService.class);

        // Try to exclusively put into map
        FutureTask<ScheduledTimerTask> ft = new FutureTask<>(new CallableImpl(timerService, job, executionManagement, services));
        Future<ScheduledTimerTask> existent = submittedJobs.putIfAbsent(job.getId(), ft);
        if (existent != null) {
            // Such a task already exists
            throw DatabaseCleanUpExceptionCode.DUPLICATE_CLEAN_UP_JOB.create(job.getId().getIdentifier());
        }

        // Successfully put into map. Submit clean-up job.
        ft.run();

        // Get & return clean-up info
        return new CleanUpInfoImpl(job.getId(), getFrom(ft), submittedJobs);
    }

    /**
     * Stops this clean-up service.
     *
     * @throws OXException If stopping fails
     */
    public void stop() throws OXException {
        for (Future<ScheduledTimerTask> timerTaskFuture : submittedJobs.values()) {
            getFrom(timerTaskFuture).cancel(true);
        }
        submittedJobs.clear();
    }

    private static <V> V getFrom(Future<V> future) throws OXException {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw DatabaseCleanUpExceptionCode.UNEXPECTED_ERROR.create(e, "Interrupted");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            if (cause == null) {
                cause = e;
            }
            throw DatabaseCleanUpExceptionCode.UNEXPECTED_ERROR.create(cause, cause.getMessage());
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class CallableImpl implements Callable<ScheduledTimerTask> {

        private final TimerService timerService;
        private final CleanUpJob job;
        private final DatabaseCleanUpExecutionManagement executionManagement;
        private final ServiceLookup services;

        CallableImpl(TimerService timerService, CleanUpJob job, DatabaseCleanUpExecutionManagement executionManagement, ServiceLookup services) {
            super();
            this.timerService = timerService;
            this.job = job;
            this.executionManagement = executionManagement;
            this.services = services;
        }

        @Override
        public ScheduledTimerTask call() {
            CleanUpJobRunnable runnable = new CleanUpJobRunnable(job, executionManagement, services);
            return timerService.scheduleWithFixedDelay(runnable, job.getInitialDelay().toMillis(), job.getDelay().toMillis());
        }
    }

}
