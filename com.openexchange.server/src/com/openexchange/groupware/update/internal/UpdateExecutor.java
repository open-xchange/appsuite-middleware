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
import com.google.common.collect.ImmutableSet;
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
        } catch (OXException e) {
            if (e.getCode() != SchemaExceptionCodes.ALREADY_LOCKED.getNumber()) {
                // Try to unlock schema
                try {
                    unlockSchema(blocking, state);
                } catch (OXException e1) {
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
                    checkDependencies(task, state);
                    final String taskName = task.getClass().getName();
                    boolean success = false;
                    startNanos = System.nanoTime();
                    try {
                        LOG.info("Starting update task {} on schema {}.", taskName, state.getSchema());
                        ProgressState logger = new ProgressStatusImpl(taskName, state.getSchema());
                        PerformParameters params = new PerformParametersImpl(state, connectionProvider, optContextId, logger);
                        task.perform(params);
                        success = true;
                    } catch (OXException e) {
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
                    addExecutedTask(taskName, success, poolId, state.getSchema(), connectionProvider.getConnection());
                    state.addExecutedTask(taskName, success);
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
        } catch (Throwable t) {
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

            // Unlock schema and invalidate context cache
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

    /**
     * (Re-)checks that all of the update tasks a specific update task is dependent upon were executed successfully in a schema, throwing
     * an appropriate exception if they're not met.
     *
     * @param task The update task to check the dependencies for
     * @param state The update state of the schema
     * @throws OXException {@link UpdateExceptionCodes#UNMET_DEPENDENCY}
     */
    private static void checkDependencies(UpdateTaskV2 task, SchemaUpdateState state) throws OXException {
        String[] dependencies = task.getDependencies();
        if (null == dependencies || 0 == dependencies.length) {
            return;
        }
        DependenciesResolvedChecker checker = new DependenciesResolvedChecker();
        String[] executedTasks = state.getExecutedList(true);
        for (String dependency : dependencies) {
            if (checker.dependencyFulfilled(dependency, ImmutableSet.copyOf(executedTasks), new UpdateTaskV2[0])) {
                continue;
            }
            Exception cause = null;
            if (false == state.isExecuted(dependency)) {
                cause = new Exception("Task \"" + dependency + "\" was not yet executed on schema \"" + state.getSchema() + "\".");
            } else if (false == state.isExecutedSuccessfully(dependency)) {
                cause = new Exception("Task \"" + dependency + "\" was not yet executed successfully on schema \"" + state.getSchema() + "\".");
            }
            throw UpdateExceptionCodes.UNMET_DEPENDENCY.create(cause, task.getClass().getName(), dependency);
        }
    }

}
