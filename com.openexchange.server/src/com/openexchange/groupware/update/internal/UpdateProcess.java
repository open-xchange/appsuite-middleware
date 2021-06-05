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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.groupware.update.TaskInfo;
import com.openexchange.tools.exceptions.ExceptionUtils;

/**
 * The {@link #run()} method of this class is started in a separate thread for
 * the update process.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UpdateProcess implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdateProcess.class);

    private final int optContextId;
    private final SchemaStore schemaStore = SchemaStore.getInstance();
    private final Queue<TaskInfo> failures;
    private final boolean throwExceptionOnFailure;
    private final int poolId;
    private final String schema;

    /**
     * Initializes a new {@link UpdateProcess} w/o tracing failures.
     *
     * @param contextId The context identifier
     */
    public UpdateProcess(int contextId) {
        this(contextId, false, false);
    }

    /**
     * Initializes a new {@link UpdateProcess}.
     *
     * @param contextId The context identifier
     * @param traceFailures <code>true</code> to trace failures available via {@link #getFailures()}; otherwise <code>false</code>
     * @param throwExceptionOnFailure Whether to throw an exception if a task failed
     */
    public UpdateProcess(int contextId, boolean traceFailures, boolean throwExceptionOnFailure) {
        super();
        this.optContextId = contextId;
        this.failures = traceFailures ? new ConcurrentLinkedQueue<TaskInfo>() : null;
        this.throwExceptionOnFailure = throwExceptionOnFailure;
        poolId = 0;
        schema = null;
    }

    /**
     * Initializes a new {@link UpdateProcess} w/o tracing failures.
     *
     * @param poolId The identifier of the database pool in which the schema resides
     * @param schema The database schema that is supposed to be updated
     */
    public UpdateProcess(int poolId, String schema) {
        this(poolId, schema, false, false);
    }

    /**
     * Initializes a new {@link UpdateProcess}.
     *
     * @param poolId The identifier of the database pool in which the schema resides
     * @param schema The database schema that is supposed to be updated
     * @param traceFailures <code>true</code> to trace failures available via {@link #getFailures()}; otherwise <code>false</code>
     * @param throwExceptionOnFailure Whether to throw an exception if a task failed
     */
    public UpdateProcess(int poolId, String schema, boolean traceFailures, boolean throwExceptionOnFailure) {
        super();
        optContextId = 0;
        this.schema = schema;
        this.poolId = poolId;
        this.failures = traceFailures ? new ConcurrentLinkedQueue<TaskInfo>() : null;
        this.throwExceptionOnFailure = throwExceptionOnFailure;
    }

    /**
     * Gets the optional failures.
     *
     * @return The failures or <code>null</code>
     * @see #UpdateProcess(int, boolean)
     */
    public Queue<TaskInfo> getFailures() {
        return failures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        try {
            runUpdate();
        } catch (OXException e) {
            LOG.error("", e);
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            LOG.error("", t);
        }
    }

    /**
     * Triggers an update run.
     *
     * @return <code>true</code> if context-associated schema was successfully updated; otherwise <code>false</code> if already up-to-date
     * @throws OXException If update attempt fails
     */
    public boolean runUpdate() throws OXException {
        int contextId = optContextId;
        if (contextId > 0) {
            // Load schema
            SchemaUpdateState state = schemaStore.getSchema(contextId);
            if (!UpdateTaskCollection.getInstance().needsUpdate(state)) {
                // Already been updated before by previous thread
                return false;
            }
            new UpdateExecutor(state, contextId, null).execute(failures, throwExceptionOnFailure);
            return true;
        }

        // Load schema
        SchemaUpdateState state = schemaStore.getSchema(poolId, schema);
        if (!UpdateTaskCollection.getInstance().needsUpdate(state)) {
            // Already been updated before by previous thread
            return false;
        }
        new UpdateExecutor(state, null).execute(failures, throwExceptionOnFailure);
        return true;
    }
}
