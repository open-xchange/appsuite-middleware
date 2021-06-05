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
