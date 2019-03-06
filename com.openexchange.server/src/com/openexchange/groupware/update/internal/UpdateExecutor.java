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

package com.openexchange.groupware.update.internal;

import static com.eaio.util.text.HumanTime.exactly;
import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.event.CommonEvent;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.groupware.update.SeparatedTasks;
import com.openexchange.groupware.update.TaskInfo;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.groupware.update.UpdaterEventConstants;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.exceptions.ExceptionUtils;

/**
 * {@link UpdateExecutor}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class UpdateExecutor {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdateExecutor.class);

    private static final SchemaStore store = SchemaStore.getInstance();

    private volatile SchemaUpdateState state;
    private final int optContextId;
    private final int poolId;
    private final String schema;
    private final List<UpdateTaskV2> tasks;

    /**
     * Initializes a new {@link UpdateExecutor}.
     *
     * @param state The schema information
     * @param optContextId The optional context identifier; otherwise <code>0</code> (zero)
     * @param tasks The optional tasks to perform; otherwise <code>null</code> (all tasks will be executed)
     */
    public UpdateExecutor(SchemaUpdateState state, int optContextId, List<UpdateTaskV2> tasks) {
        super();
        this.state = state;
        this.optContextId = optContextId;
        this.tasks = tasks;
        this.poolId = state.getPoolId();
        this.schema = state.getSchema();
    }

    /**
     * Initializes a new {@link UpdateExecutor}.
     *
     * @param state The schema information
     * @param tasks The optional tasks to perform; otherwise <code>null</code> (all tasks will be executed)
     */
    public UpdateExecutor(SchemaUpdateState state, List<UpdateTaskV2> tasks) {
        this(state, 0, tasks);
    }

    /**
     * Executes this update w/o tracing failures.
     *
     * @throws OXException If update fails
     */
    public void execute() throws OXException {
        execute(null, false);
    }

    /**
     * Executes this update while storing failures in specified <code>failures</code> queue if not <code>null</code>.
     *
     * @param failures The optional failure queue for tracing
     * @param throwExceptionOnFailure Whether a possible exception is supposed to abort process
     * @throws OXException If update fails
     */
    public void execute(final Queue<TaskInfo> failures, boolean throwExceptionOnFailure) throws OXException {
        SeparatedTasks separatedTasks = null;
        if (null == tasks) {
            final SeparatedTasks forCheck = UpdateTaskCollection.getInstance().getFilteredAndSeparatedTasks(state);
            if (forCheck.hasBlocking()) {
                runUpdates(true, failures, throwExceptionOnFailure, separatedTasks);
            }
            if (forCheck.hasBackground()) {
                runUpdates(false, failures, throwExceptionOnFailure, separatedTasks);
            }
        } else {
            separatedTasks = UpdateTaskCollection.getInstance().separateTasks(tasks);
            if (separatedTasks.hasBlocking()) {
                runUpdates(true, failures, throwExceptionOnFailure, separatedTasks);
            }
            if (separatedTasks.hasBackground()) {
                runUpdates(false, failures, throwExceptionOnFailure, separatedTasks);
            }
        }
    }

    private void runUpdates(final boolean blocking, final Queue<TaskInfo> failures, boolean throwExceptionOnFailure, SeparatedTasks separatedTasks) throws OXException {
        SchemaUpdateState state = this.state;
        LOG.info("Starting {} updates on schema {}", (blocking ? "blocking" : "background"), state.getSchema());
        try {
            lockSchema(blocking, state);
        } catch (final OXException e) {
            if (e.getCode() != SchemaExceptionCodes.ALREADY_LOCKED.getNumber()) {
                // Try to unlock schema
                try {
                    unlockSchema(blocking, state);
                } catch (final OXException e1) {
                    LOG.error("", e1);
                }
            }
            throw e;
        }
        // Lock successfully obtained, thus remember to unlock
        TimerService timerService = ServerServiceRegistry.getInstance().getService(TimerService.class);
        ScheduledTimerTask timerTask = null;
        try {
            // Contexts on that scheme can continue
            if (blocking) {
                removeContexts();
            }
            final List<UpdateTaskV2> scheduled = new ArrayList<UpdateTaskV2>();
            if (null == separatedTasks) {
                state = store.getSchema(poolId, schema);
                this.state = state;
                // Get filtered & sorted list of update tasks
                scheduled.addAll(UpdateTaskCollection.getInstance().getFilteredAndSortedUpdateTasks(state, blocking));
            } else {
                scheduled.addAll(blocking ? separatedTasks.getBlocking() : separatedTasks.getBackground());
            }

            // Start timer task for periodic refresh
            if (null != timerService) {
                final SchemaUpdateState _state = state;
                Runnable task = new Runnable() {

                    @Override
                    public void run() {
                        try {
                            boolean refreshed = tryRefreshSchemaLock(blocking, _state);
                            if (refreshed) {
                                LOG.info("Refreshed lock for schema {}", _state.getSchema());
                            }
                        } catch (Exception e) {
                            LOG.warn("Failed to refresh lock for schema {}", _state.getSchema(), e);
                        }
                    }
                };
                timerTask = timerService.scheduleWithFixedDelay(task, 5L, 5L, TimeUnit.MINUTES);
            }

            // Perform updates
            int poolId = this.poolId;
            AbstractConnectionProvider connectionProvider = optContextId > 0 ? new ContextConnectionProvider(optContextId) : new PoolAndSchemaConnectionProvider(poolId, schema);
            try {
                long startNanos;
                long durMillis;
                for (final UpdateTaskV2 task : scheduled) {
                    final String taskName = task.getClass().getSimpleName();
                    boolean success = false;
                    startNanos = System.nanoTime();
                    try {
                        LOG.info("Starting update task {} on schema {}.", taskName, state.getSchema());
                        ProgressState logger = new ProgressStatusImpl(taskName, state.getSchema());
                        PerformParameters params = new PerformParametersImpl(state, connectionProvider, optContextId, logger);
                        task.perform(params);
                        success = true;
                    } catch (final OXException e) {
                        LOG.error("", e);
                    }
                    durMillis = TimeUnit.MILLISECONDS.convert(System.nanoTime() - startNanos, TimeUnit.NANOSECONDS);
                    if (success) {
                        LOG.info("Update task {} on schema {} done ({}).", taskName, state.getSchema(), exactly(durMillis, true));
                    } else {
                        if (throwExceptionOnFailure) {
                            throw SchemaExceptionCodes.TASK_FAILED.create(taskName, state.getSchema());
                        }
                        if (null != failures) {
                            failures.offer(new TaskInfo(taskName, state.getSchema()));
                        }
                        LOG.error("Update task {} on schema {} failed ({}).", taskName, state.getSchema(), exactly(durMillis, true));
                    }
                    addExecutedTask(task.getClass().getName(), success, poolId, state.getSchema(), connectionProvider.getConnection());
                }
            } finally {
                connectionProvider.close();
            }

            // Post event for finished update(s)
            {
                EventAdmin service = ServerServiceRegistry.getInstance().getService(EventAdmin.class);
                if (null != service) {
                    Map<String, Object> eventProperties = new HashMap<String, Object>(6);
                    eventProperties.put(UpdaterEventConstants.PROPERTY_SCHEMA, state.getSchema());
                    eventProperties.put(UpdaterEventConstants.PROPERTY_POOL_ID, Integer.valueOf(poolId));
                    eventProperties.put(CommonEvent.PUBLISH_MARKER, Boolean.TRUE);
                    service.postEvent(new Event(UpdaterEventConstants.TOPIC, eventProperties));
                }
            }

            LOG.info("Finished {} updates on schema {}", (blocking ? "blocking" : "background"), state.getSchema());
        } catch (final Throwable t) {
            ExceptionUtils.handleThrowable(t);
            throw UpdateExceptionCodes.UPDATE_FAILED.create(t, state.getSchema(), t.getMessage());
        } finally {
            // Stop timer task
            if (null != timerTask) {
                timerTask.cancel();
                if (null != timerService) {
                    timerService.purge();
                }
            }

            // Is context cache supposed to be invalidated?
            boolean doRemoveContexts = blocking;

            // Either unlock schema or unlock schema and invalidate context cache
            if (doRemoveContexts) {
                try {
                    unlockSchema(blocking, state);
                } catch (OXException oxe) {
                    if (!SchemaExceptionCodes.SQL_PROBLEM.equals(oxe)) {
                        throw oxe;
                    }

                    Throwable cause = oxe.getCause();
                    if (!(cause instanceof SQLException) || !Databases.isReadTimeout((SQLException) cause)) {
                        throw oxe;
                    }

                    // Unlocking the schema might be successfully executed then...
                }

                // Remove contexts from cache
                removeContexts();
            } else {
                unlockSchema(blocking, state);
            }
        }
    }

    private final void lockSchema(boolean blocking, SchemaUpdateState state) throws OXException {
        store.lockSchema(state, !blocking);
        LocalUpdateTaskMonitor.getInstance().addState(state.getSchema());
    }

    private final void unlockSchema(boolean blocking, SchemaUpdateState state) throws OXException {
        try {
            store.unlockSchema(state, !blocking);
        } finally {
            LocalUpdateTaskMonitor.getInstance().removeState(state.getSchema());
        }
    }

    final boolean tryRefreshSchemaLock(boolean blocking, SchemaUpdateState state) throws OXException {
        return store.tryRefreshSchemaLock(state, !blocking);
    }

    private final void addExecutedTask(String taskName, boolean success, int poolId, String schema, Connection con) throws OXException {
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            store.addExecutedTask(con, taskName, success, poolId, schema);

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw SchemaExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    rollback(con);
                }
                autocommit(con);
            }
        }
    }

    private final void removeContexts() throws OXException {
        int[] contextIds = determineContextIds();
        ContextStorage.getInstance().invalidateContexts(contextIds);
    }

    private final int[] determineContextIds() throws OXException {
        DatabaseService databaseService = Database.getDatabaseService();
        Connection con = databaseService.getReadOnly();
        try {
            return databaseService.getContextsInSchema(con, poolId, schema);
        } finally {
            databaseService.backReadOnly(con);
        }
    }
}
