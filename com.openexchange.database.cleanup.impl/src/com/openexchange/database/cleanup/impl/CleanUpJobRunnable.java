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

import static com.eaio.util.text.HumanTime.exactly;
import static com.openexchange.database.cleanup.impl.DatabaseCleanUpServiceImpl.REFRESH_LAST_TOUCHED_STAMP_INTERVAL_MILLIS;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.context.ContextService;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.database.cleanup.CleanUpJob;
import com.openexchange.database.cleanup.DatabaseCleanUpExceptionCode;
import com.openexchange.database.cleanup.impl.storage.DatabaseCleanUpExecutionManagement;
import com.openexchange.exception.Category;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link CleanUpJobRunnable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class CleanUpJobRunnable implements Runnable {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CleanUpJobRunnable.class);

    /** The decimal format to use when printing milliseconds */
    private static final NumberFormat MILLIS_FORMAT = newNumberFormat();

    /** The accompanying lock for shared decimal format */
    private static final Lock MILLIS_FORMAT_LOCK = new ReentrantLock();

    /**
     * Creates a new {@code DecimalFormat} instance.
     *
     * @return The format instance
     */
    private static NumberFormat newNumberFormat() {
        NumberFormat f = NumberFormat.getInstance(Locale.US);
        if (f instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat) f;
            df.applyPattern("#,##0");
        }
        return f;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final CleanUpJob job;
    private final DatabaseCleanUpExecutionManagement executionManagement;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link CleanUpJobRunnable}.
     *
     * @param job The wrapped job
     * @param executionManagement The storage service to use
     * @param services The service look-up
     */
    public CleanUpJobRunnable(CleanUpJob job, DatabaseCleanUpExecutionManagement executionManagement, ServiceLookup services) {
        super();
        this.job = job;
        this.executionManagement = executionManagement;
        this.services = services;
    }

    @Override
    public void run() {
        Thread currentThread = Thread.currentThread();
        String prevName = currentThread.getName();
        currentThread.setName(job.getId().getIdentifier());
        try {
            // Map to manage state
            Map<String, Object> state = new HashMap<>(4);

            // Prepare...
            if (job.getExecution().prepareCleanUp(state) == false) {
                LOG.info("Could not prepare clean-up of job '{}'.", job.getId());
                return;
            }

            // Clean-up and finish
            cleanUpAndFinish(state, currentThread);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.info("Interrupting clean-up of job '{}'.", job.getId(), e);
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.warn("Failed to perform clean-up for: '{}'", job.getId(), t);
        } finally {
            currentThread.setName(prevName);
        }
    }

    /**
     * Checks the update status of given schema.
     *
     * @param poolAndSchema The pool and schema information
     * @param updater The update instance to use
     * @return <code>true</code> if schema update status is up-to-date, otherwise <code>false</code> if either currently updating or updates are pending
     * @throws OXException If update status cannot be checked
     */
    private boolean checkSchemaStatus(PoolAndSchema poolAndSchema, Updater updater) throws OXException {
        UpdateStatus status = updater.getStatus(poolAndSchema.getSchema(), poolAndSchema.getPoolId());
        if (status.blockingUpdatesRunning()) {
            // Context-associated schema is currently updated. Abort clean-up for that schema
            LOG.info("Update running: Skipping clean-up of job '{}' for schema {} since that schema is currently updated", job.getId(), poolAndSchema.getSchema());
            return false;
        }
        if ((status.needsBlockingUpdates() || status.needsBackgroundUpdates()) && !status.blockingUpdatesRunning() && !status.backgroundUpdatesRunning()) {
            // Context-associated schema needs an update. Abort clean-up for that schema
            LOG.info("Update needed: Skipping clean-up of job '{}' for schema {} since that schema needs an update", job.getId(), poolAndSchema.getSchema());
            return false;
        }
        return true;
    }

    private void cleanUpAndFinish(Map<String, Object> state, Thread currentThread) throws OXException, InterruptedException {
        try {
            // Some time stamps...
            long start = System.currentTimeMillis();
            long logTimeDistance = TimeUnit.SECONDS.toMillis(10);
            long lastLogTime = start;

            // Grab needed services
            ContextService contextService = services.getServiceSafe(ContextService.class);
            TimerService timerService = services.getServiceSafe(TimerService.class);
            Updater updater = Updater.getInstance();

            // Iterate over representative context identifier per schema
            List<Integer> contextsIdInDifferentSchemas = ContextStorage.getInstance().getDistinctContextsPerSchema();
            int size = contextsIdInDifferentSchemas.size();
            Iterator<Integer> iter = contextsIdInDifferentSchemas.iterator();
            for (int i = 0, k = size; k-- > 0; i++) {
                // Check if thread has been interrupted meanwhile
                if (currentThread.isInterrupted()) {
                    LOG.info("Interrupting clean-up of job '{}'.", job.getId());
                    return;
                }

                // Process schema
                Integer representativeContextId = iter.next();
                PoolAndSchema poolAndSchema = getSchema(representativeContextId, contextService);
                if (checkSchemaStatus(poolAndSchema, updater)) {
                    // No update running or pending. Continue clean-up run for that schema...
                    int retryCount = 3;
                    for (int retry = retryCount; retry-- > 0;) {
                        // Check again if thread has been interrupted meanwhile
                        if (currentThread.isInterrupted()) {
                            LOG.info("Interrupting clean-up of job '{}'.", job.getId());
                            return;
                        }

                        // Progress logging
                        long now = System.currentTimeMillis();
                        if (now > lastLogTime + logTimeDistance) {
                            LOG.info("Clean-up job '{}' {}% finished ({}/{}).", job.getId(), I(i * 100 / size), I(i), I(size));
                            lastLogTime = now;
                        }

                        // Perform clean-up
                        try {
                            cleanUpForSchema(representativeContextId.intValue(), poolAndSchema.getSchema(), poolAndSchema.getPoolId(), state, timerService);
                            retry = 0;
                        } catch (OXException e) {
                            if (retry > 0 && Category.CATEGORY_TRY_AGAIN.equals(e.getCategory())) {
                                long delay = ((retryCount - retry) * 1000) + ((long) (Math.random() * 1000));
                                LOG.debug("Failed clean-up of job '{}' for schema {}: {}; trying again in {}ms...", job.getId(), poolAndSchema.getSchema(), e.getMessage(), L(delay));
                                Thread.sleep(delay);
                            } else {
                                LOG.warn("Failed clean-up of job '{}' for schema {}", job.getId(), poolAndSchema.getSchema(), e);
                                retry = 0;
                            }
                        } catch (Exception e) {
                            LOG.warn("Failed clean-up of job '{}' for schema {}", job.getId(), poolAndSchema.getSchema(), e);
                            retry = 0;
                        }
                    }
                }
            }
            long duration = System.currentTimeMillis() - start;
            LOG.info("Clean-up for job '{}' took {}ms ({})", job.getId(), formatDuration(duration), exactly(duration, true));
        } finally {
            finishSafe(state);
        }
    }

    private void cleanUpForSchema(int representativeContextId, String schema, int poolId, Map<String, Object> state, TimerService timerService) throws OXException {
        if (isNotApplicableFor(representativeContextId, schema, poolId, state)) {
            // Not applicable for current schema
            return;
        }

        if (job.isRunsExclusive()) {
            // Job's execution needs to be coordinated among cluster nodes
            ScheduledTimerTask timerTask = null;

            // Check for permission to execute it
            if (executionManagement.checkExecutionPermission(job, representativeContextId)) {
                // Permission acquired...
                try {
                    // Start timer task for periodic refresh of job's last-touched time stamp
                    long refreshIntervalMillis = REFRESH_LAST_TOUCHED_STAMP_INTERVAL_MILLIS;
                    timerTask = timerService.scheduleWithFixedDelay(newRefreshTask(schema, representativeContextId), refreshIntervalMillis, refreshIntervalMillis);

                    // Execute job
                    executeJobFor(representativeContextId, schema, poolId, state);
                } finally {
                    // Stop timer task
                    stopTimerTaskSafe(timerTask, schema, timerService);
                    executionManagement.markExecutionDone(job, representativeContextId);
                }
            } else {
                // No permission
                LOG.debug("No permission to execute clean-up job '{}' against schema {}; e.g. another process currently performs that job or job's delay has not yet elapsed.", job.getId(), schema);
            }
        } else {
            // May run at any time on any node, thus just execute it
            executeJobFor(representativeContextId, schema, poolId, state);
        }

        LOG.debug("Successfully executed clean-up job '{}' against schema {}", job.getId(), schema);
    }

    private Runnable newRefreshTask(String schema, int representativeContextId) {
        return new RefreshJobTimeStampTask(job, schema, representativeContextId, executionManagement);
    }

    private void stopTimerTaskSafe(ScheduledTimerTask timerTask, String schema, TimerService timerService) {
        if (timerTask != null) {
            try {
                timerTask.cancel();
                timerService.purge();
            } catch (Exception e) {
                LOG.warn("Failed to stop stamp-refreshing timer task of clean-up of job '{}' for schema {}", job.getId(), schema, e);
            }
        }
    }

    private boolean isNotApplicableFor(int representativeContextId, String schema, int poolId, Map<String, Object> state) throws OXException {
        return isApplicableFor(representativeContextId, schema, poolId, state) == false;
    }

    private boolean isApplicableFor(int representativeContextId, String schema, int poolId, Map<String, Object> state) throws OXException {
        ReadOnlyCleanUpExecutionConnectionProvider connectionProvider = new ReadOnlyCleanUpExecutionConnectionProvider(representativeContextId, services);
        try {
            return job.getExecution().isApplicableFor(schema, representativeContextId, poolId, state, connectionProvider);
        } finally {
            connectionProvider.close();
        }
    }

    private void executeJobFor(int representativeContextId, String schema, int poolId, Map<String, Object> state) throws OXException {
        // Create connection provider
        ReadWriteCleanUpExecutionConnectionProvider connectionProvider = new ReadWriteCleanUpExecutionConnectionProvider(representativeContextId, job.isPreferNoConnectionTimeout(), services);
        try {
            // Execute job
            job.getExecution().executeFor(schema, representativeContextId, poolId, state, connectionProvider);

            // Commit optional connection
            connectionProvider.commitAfterSuccess();
        } catch (SQLException e) {
            throw DatabaseCleanUpExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            // Roll-back optional connection
            connectionProvider.close();
        }
    }

    private void finishSafe(Map<String, Object> state) {
        try {
            job.getExecution().finishCleanUp(state);
        } catch (Exception e) {
            LOG.warn("Failed to finish clean-up of job '{}'", job.getId(), e);
        }
    }

    private static String formatDuration(long duration) {
        if (MILLIS_FORMAT_LOCK.tryLock()) {
            try {
                return MILLIS_FORMAT.format(duration);
            } finally {
                MILLIS_FORMAT_LOCK.unlock();
            }
        }

        // Use thread-specific DecimalFormat instance
        NumberFormat format = newNumberFormat();
        return format.format(duration);
    }

    private static PoolAndSchema getSchema(Integer representativeContextId, ContextService contextService) throws OXException {
        Map<PoolAndSchema, List<Integer>> associations = contextService.getSchemaAssociationsFor(Collections.singletonList(representativeContextId));
        if (associations.isEmpty()) {
            throw OXException.general("No such database pool and schema found for context " + representativeContextId);
        }
        return associations.keySet().iterator().next();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class RefreshJobTimeStampTask implements Runnable {

        private final DatabaseCleanUpExecutionManagement executionManagement;
        private final CleanUpJob job;
        private final int representativeContextId;
        private final String schema;

        RefreshJobTimeStampTask(CleanUpJob job, String schema, int representativeContextId, DatabaseCleanUpExecutionManagement executionManagement) {
            super();
            this.job = job;
            this.representativeContextId = representativeContextId;
            this.schema = schema;
            this.executionManagement = executionManagement;
        }

        @Override
        public void run() {
            try {
                if (executionManagement.refreshTimeStamp(job, representativeContextId)) {
                    LOG.debug("Successfully refreshed last-touched time stamp of clean-up job '{}' for schema {}", job.getId(), schema);
                }
            } catch (Exception e) {
                LOG.warn("Failed to refresh the last-touched time stamp for clean-up of job '{}' for schema {}", job.getId(), schema, e);
            }
        }
    }

}
