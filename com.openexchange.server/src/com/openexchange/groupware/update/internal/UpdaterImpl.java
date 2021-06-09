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

import java.sql.Connection;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.groupware.update.SeparatedTasks;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.groupware.update.Updater;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.TimerService;

/**
 * Implementation for the updater interface.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class UpdaterImpl extends Updater {

    private static final UpdaterImpl INSTANCE = new UpdaterImpl();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static UpdaterImpl getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------------------ //

    /**
     * Default constructor.
     */
    private UpdaterImpl() {
        super();
    }

    @Override
    public UpdateStatus getStatus(final int contextId) throws OXException {
        return getStatus(getSchema(contextId));
    }

    @Override
    public UpdateStatus getStatus(final String schema, final int writePoolId) throws OXException {
        return getStatus(getSchema(writePoolId, schema));
    }

    private UpdateStatus getStatus(final SchemaUpdateState schema) {
        final SeparatedTasks tasks = UpdateTaskCollection.getInstance().getFilteredAndSeparatedTasks(schema);
        return new UpdateStatusImpl(tasks, schema);
    }

    @Override
    public void unblock(String schemaName, int poolId, int contextId) throws OXException {
        SchemaUpdateState schema = SchemaStore.getInstance().getSchema(poolId, schemaName);
        SchemaStore.getInstance().unlockSchema(schema, false);
    }

    @Override
    public void startUpdate(final int contextId) throws OXException {
        final TimerService timerService = ServerServiceRegistry.getInstance().getService(TimerService.class, true);
        timerService.schedule(new UpdateProcess(contextId), 0);
    }

    private SchemaUpdateState getSchema(final int contextId) throws OXException {
        return SchemaStore.getInstance().getSchema(contextId);
    }

    private SchemaUpdateState getSchema(final int poolId, final String schemaName) throws OXException {
        return SchemaStore.getInstance().getSchema(poolId, schemaName);
    }

    @Override
    public UpdateTaskV2[] getAvailableUpdateTasks() {
        List<UpdateTaskV2> retval = UpdateTaskCollection.getInstance().getListWithoutExcludes();
        return retval.toArray(new UpdateTaskV2[retval.size()]);
    }

    @Override
    public Collection<String> getLocallyScheduledTasks() {
        return LocalUpdateTaskMonitor.getInstance().getScheduledStates();
    }

    @Override
    public void invalidateCacheFor(int contextId) throws OXException {
        SchemaUpdateState schema = getSchema(contextId);
        removeContexts(schema);
        SchemaStore.getInstance().invalidateCache(schema);
    }

    @Override
    public void invalidateCacheFor(String schemaName, int poolId) throws OXException {
        SchemaUpdateState schema = getSchema(poolId, schemaName);
        removeContexts(schema);
        SchemaStore.getInstance().invalidateCache(schema);
    }

    private final void removeContexts(SchemaUpdateState schema) throws OXException {
        int[] contextIds = determineContextIds(schema);
        ContextStorage.getInstance().invalidateContexts(contextIds);
    }

    private final int[] determineContextIds(SchemaUpdateState schema) throws OXException {
        DatabaseService databaseService = Database.getDatabaseService();
        Connection con = databaseService.getReadOnly();
        try {
            return databaseService.getContextsInSchema(con, schema.getPoolId(), schema.getSchema());
        } finally {
            databaseService.backReadOnly(con);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------ //

    private static class UpdateStatusImpl implements UpdateStatus {

        private final SeparatedTasks tasks;
        private final SchemaUpdateState schema;

        UpdateStatusImpl(SeparatedTasks tasks, SchemaUpdateState schema) {
            super();
            this.tasks = tasks;
            this.schema = schema;
        }

        @Override
        public int getPoolId() {
            return schema.getPoolId();
        }

        @Override
        public String getSchemaName() {
            return schema.getSchema();
        }

        @Override
        public boolean needsBlockingUpdates() {
            return tasks.hasBlocking();
        }

        @Override
        public boolean needsBackgroundUpdates() {
            return tasks.hasBackground();
        }

        @Override
        public boolean blockingUpdatesRunning() {
            return schema.isLocked();
        }

        @Override
        public boolean backgroundUpdatesRunning() {
            return schema.backgroundUpdatesRunning();
        }

        @Override
        public Date blockingUpdatesRunningSince() {
            return schema.blockingUpdatesRunningSince();
        }

        @Override
        public Date backgroundUpdatesRunningSince() {
            return schema.backgroundUpdatesRunningSince();
        }

        @Override
        public boolean isExecutedSuccessfully(String taskName) {
            return schema.isExecutedSuccessfully(taskName);
        }

        @Override
        public boolean blockingUpdatesTimedOut() {
            Date blockingUpdatesRunningSince = schema.blockingUpdatesRunningSince();
            if (blockingUpdatesRunningSince == null) {
                return false;
            }
            long idleMillis = SchemaStore.getInstance().getIdleMillis(false);
            return idleMillis > 0 && (System.currentTimeMillis() - blockingUpdatesRunningSince.getTime() > idleMillis);
        }
    }

}
