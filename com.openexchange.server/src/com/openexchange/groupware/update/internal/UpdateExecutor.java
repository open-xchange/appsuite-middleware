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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.groupware.update.SeparatedTasks;
import com.openexchange.groupware.update.TaskInfo;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.UpdateTaskV2;

/**
 * {@link UpdateExecutor}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class UpdateExecutor {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdateExecutor.class);

    private static final ConcurrentMap<String, SchemaUpdateState> LOCKED_SCHEMAS = new ConcurrentHashMap<String, SchemaUpdateState>();

    private static final SchemaStore store = SchemaStore.getInstance();

    private SchemaUpdateState state;
    private final int contextId;
    private final List<UpdateTask> tasks;
    private SeparatedTasks separatedTasks;

    public UpdateExecutor(final SchemaUpdateState state, final int contextId, final List<UpdateTask> tasks) {
        super();
        this.state = state;
        this.contextId = contextId;
        this.tasks = tasks;
    }

    /**
     * Executes this update w/o tracing failures.
     *
     * @throws OXException If update fails
     */
    public void execute() throws OXException {
        execute(null);
    }

    /**
     * Executes this update while storing failures in specified <code>failures</code> queue if not <code>null</code>.
     *
     * @param failures The optional failure queue for tracing
     * @throws OXException If update fails
     */
    public void execute(final Queue<TaskInfo> failures) throws OXException {
        if (null != tasks) {
            separatedTasks = UpdateTaskCollection.getInstance().separateTasks(tasks);
            if (separatedTasks.getBlocking().size() > 0) {
                runUpdates(true, failures);
            }
            if (separatedTasks.getBackground().size() > 0) {
                runUpdates(false, failures);
            }
        } else {
            final SeparatedTasks forCheck = UpdateTaskCollection.getInstance().getFilteredAndSeparatedTasks(state);
            if (forCheck.getBlocking().size() > 0) {
                runUpdates(true, failures);
            }
            if (forCheck.getBackground().size() > 0) {
                runUpdates(false, failures);
            }
        }
    }

    private void runUpdates(final boolean blocking, final Queue<TaskInfo> failures) throws OXException {
        LOG.info("Starting {} updates on schema {}", (blocking ? "blocking" : "background"), state.getSchema());
        try {
            lockSchema(blocking);
        } catch (final OXException e) {
            if (e.getCode() != SchemaExceptionCodes.ALREADY_LOCKED.getNumber()) {
                // Try to unlock schema
                try {
                    unlockSchema(blocking);
                } catch (final OXException e1) {
                    LOG.error("", e1);
                }
            }
            throw e;
        }
        // Lock successfully obtained, thus remember to unlock
        try {
            // Contexts on that scheme can continue
            if (blocking) {
                removeContexts();
            }
            final List<UpdateTask> scheduled = new ArrayList<UpdateTask>();
            if (null == separatedTasks) {
                state = store.getSchema(contextId);
                // Get filtered & sorted list of update tasks
                scheduled.addAll(UpdateTaskCollection.getInstance().getFilteredAndSortedUpdateTasks(state, blocking));
            } else {
                scheduled.addAll(blocking ? separatedTasks.getBlocking() : separatedTasks.getBackground());
            }
            final int poolId = Database.resolvePool(contextId, true);
            // Perform updates
            for (final UpdateTask task : scheduled) {
                final String taskName = task.getClass().getSimpleName();
                boolean success = false;
                try {
                    LOG.info("Starting update task {} on schema {}.", taskName, state.getSchema());
                    if (task instanceof UpdateTaskV2) {
                        final ProgressState logger = new ProgressStatusImpl(taskName, state.getSchema());
                        final PerformParameters params = new PerformParametersImpl(state, contextId, logger);
                        ((UpdateTaskV2) task).perform(params);
                    } else {
                        task.perform(state, contextId);
                    }
                    success = true;
                } catch (final OXException e) {
                    LOG.error("", e);
                }
                if (success) {
                    LOG.info("Update task {} on schema {} done.", taskName, state.getSchema());
                } else {
                    if (null != failures) {
                        failures.offer(new TaskInfo(taskName, state.getSchema()));
                    }
                    LOG.error("Update task {} on schema {} failed.", taskName, state.getSchema());
                }
                addExecutedTask(task.getClass().getName(), success, poolId, state.getSchema());
            }
            LOG.info("Finished {} updates on schema {}", (blocking ? "blocking" : "background"), state.getSchema());
        } catch (final OXException e) {
            throw e;
        } catch (final Throwable t) {
            throw UpdateExceptionCodes.UPDATE_FAILED.create(t, state.getSchema(), t.getMessage());
        } finally {
            unlockSchema(blocking);
            // Remove contexts from cache if they are cached during update process.
            if (blocking) {
                removeContexts();
            }
        }
    }

    private final void lockSchema(final boolean blocking) throws OXException {
        store.lockSchema(state, contextId, !blocking);
        LocalUpdateTaskMonitor.getInstance().addState(state.getSchema());
    }

    private final void unlockSchema(final boolean blocking) throws OXException {
        try {
            store.unlockSchema(state, contextId, !blocking);
        } finally {
            LocalUpdateTaskMonitor.getInstance().removeState(state.getSchema());
        }
    }

    private final void addExecutedTask(final String taskName, final boolean success, final int poolId, final String schema) throws OXException {
        store.addExecutedTask(contextId, taskName, success, poolId, schema);
    }

    private final void removeContexts() throws OXException {
        final int[] contextIds = Database.getContextsInSameSchema(contextId);
        ContextStorage.getInstance().invalidateContexts(contextIds);
    }
}
