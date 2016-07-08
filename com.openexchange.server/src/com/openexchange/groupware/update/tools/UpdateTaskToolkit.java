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

package com.openexchange.groupware.update.tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.SchemaUpdateState;
import com.openexchange.groupware.update.TaskInfo;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.groupware.update.internal.DynamicList;
import com.openexchange.groupware.update.internal.UpdateExecutor;
import com.openexchange.groupware.update.internal.UpdateProcess;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link UpdateTaskToolkit} - Toolkit for update tasks.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UpdateTaskToolkit {

    static final Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdateTaskToolkit.class);

    static final Object LOCK = new Object();

    /**
     * Initializes a new {@link UpdateTaskToolkit}.
     */
    private UpdateTaskToolkit() {
        super();
    }

    /**
     * Force (re-)run of update task denoted by given class name
     *
     * @param className The update task's class name
     * @param schemaName A valid schema name
     * @throws OXException If update task cannot be performed
     */
    public static void forceUpdateTask(final String className, final String schemaName) throws OXException {
        forceUpdateTask(className, getContextIdBySchema(schemaName));
    }

    /**
     * Force (re-)run of update task denoted by given class name
     *
     * @param className The update task's class name
     * @param contextId The context identifier
     * @throws OXException If update task cannot be performed
     */
    public static void forceUpdateTask(final String className, final int contextId) throws OXException {
        synchronized (LOCK) {
            forceUpdateTask0(getUpdateTask(className), contextId);
        }
    }

    /**
     * Force (re-)run of update task denoted by given class name. This method should only be called when holding <code>LOCK</code>.
     *
     * @param task The update task
     * @param contextId The context identifier
     * @throws OXException If update task cannot be performed
     */
    private static void forceUpdateTask0(final UpdateTaskV2 task, final int contextId) throws OXException {
        final List<UpdateTaskV2> taskList = new ArrayList<UpdateTaskV2>(1);
        taskList.add(task);
        new UpdateExecutor(getSchema(contextId), contextId, taskList).execute();
    }

    /**
     * Force (re-)run of update task denoted by given class name on all schemas.
     *
     * @param className The update task's class name
     * @throws OXException If update task cannot be performed
     */
    public static void forceUpdateTaskOnAllSchemas(final String className) throws OXException {
        synchronized (LOCK) {
            // Get update task by class name
            final UpdateTaskV2 updateTask = getUpdateTask(className);
            // Get all available schemas
            final Map<String, Set<Integer>> map = getSchemasAndContexts();
            // ... and iterate them
            final Iterator<Set<Integer>> iter = map.values().iterator();
            while (iter.hasNext()) {
                final Set<Integer> set = iter.next();
                if (!set.isEmpty()) {
                    forceUpdateTask0(updateTask, set.iterator().next().intValue());
                }
            }
        }
    }

    /**
     * Runs the update process on all available schemas
     *
     * @param throwExceptionOnFailure Whether a possible exception is supposed to abort process
     * @return A status text reference
     * @throws OXException If update process fails
     */
    public static UpdateTaskToolkitJob<Void> runUpdateOnAllSchemas(final boolean throwExceptionOnFailure) throws OXException {
        // Get all available schemas
        final Map<String, Set<Integer>> map = getSchemasAndContexts();
        final int total = map.size();

        // Status text
        final AtomicReference<String> statusText = new AtomicReference<String>("Attempting to update " + total + " schemas in total...");

        // Task...
        Callable<Void> task = new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                synchronized (LOCK) {
                    // Iterate schemas
                    int count = 0;
                    StringBuilder sb = new StringBuilder(32);
                    Map<String, Queue<TaskInfo>> totalFailures = new HashMap<String, Queue<TaskInfo>>(32);
                    for (Iterator<Set<Integer>> iter = map.values().iterator(); iter.hasNext();) {
                        Set<Integer> set = iter.next();
                        if (!set.isEmpty()) {
                            int contextId = set.iterator().next().intValue();
                            UpdateProcess updateProcess = new UpdateProcess(contextId, true, throwExceptionOnFailure);
                            if (throwExceptionOnFailure) {
                                try {
                                    updateProcess.runUpdate();
                                } catch (OXException e) {
                                    LOG.error("", e);
                                    statusText.set(e.getPlainLogMessage());
                                    throw e;
                                } catch (Exception e) {
                                    LOG.error("", e);
                                    statusText.set(e.getMessage());
                                    throw e;
                                }
                            } else {
                                updateProcess.run();

                                // Check possible failures
                                Queue<TaskInfo> failures = updateProcess.getFailures();
                                if (null != failures && !failures.isEmpty()) {
                                    for (TaskInfo taskInfo : failures) {
                                        Queue<TaskInfo> schemaFailures = totalFailures.get(taskInfo.getSchema());
                                        if (null == schemaFailures) {
                                            schemaFailures = new LinkedList<TaskInfo>();
                                            totalFailures.put(taskInfo.getSchema(), schemaFailures);
                                        }
                                        schemaFailures.offer(taskInfo);
                                    }
                                }
                            }
                        }
                        count++;
                        if (count < total) {
                            sb.setLength(0);
                            sb.append("Processed ").append(count).append(" of ").append(total).append(" schemas.");
                            statusText.set(sb.toString());
                        }
                    }

                    // Completed...
                    sb.setLength(0);
                    sb.append("Processed ").append(total).append(" of ").append(total).append(" schemas.");

                    // Append failure information (if any)
                    if (!totalFailures.isEmpty()) {
                        sb.append("\\R\\R");
                        boolean first = true;
                        for (Map.Entry<String, Queue<TaskInfo>> failureEntry : totalFailures.entrySet()) {
                            if (first) {
                                first = false;
                            } else {
                                sb.append("\\R\\R");
                            }
                            sb.append("The following update task(s) failed on schema \"").append(failureEntry.getKey()).append("\": \\R");
                            boolean firstTaskInfo = true;
                            for (TaskInfo taskInfo : failureEntry.getValue()) {
                                if (firstTaskInfo) {
                                    firstTaskInfo = false;
                                } else {
                                    sb.append("\\R");
                                }
                                sb.append(' ').append(taskInfo.getTaskName()).append(" (schema=").append(taskInfo.getSchema()).append(')');
                            }
                        }
                    }

                    // Set new status text
                    statusText.set(sb.toString());
                }
                return null;
            }
        };

        // Submit & return
        UpdateTaskToolkitJob<Void> job = new UpdateTaskToolkitJob<>(task, statusText);
        ThreadPools.getThreadPool().submit(ThreadPools.task(job, "RunAllUpdate"));
        return job;
    }

    private static final String SQL_SELECT_SCHEMAS = "SELECT db_schema,cid FROM context_server2db_pool";

    /**
     * Gets schemas and their contexts as a map.
     *
     * @return A map containing schemas and their contexts.
     * @throws OXException If an error occurs
     */
    private static Map<String, Set<Integer>> getSchemasAndContexts() throws OXException {
        final Connection con = Database.get(false);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_SCHEMAS);
            rs = stmt.executeQuery();
            final Map<String, Set<Integer>> schemasAndContexts = new HashMap<String, Set<Integer>>();
            while (rs.next()) {
                final String schemaName = rs.getString(1);
                final int contextId = rs.getInt(2);
                Set<Integer> contextIds = schemasAndContexts.get(schemaName);
                if (null == contextIds) {
                    contextIds = new HashSet<Integer>();
                    schemasAndContexts.put(schemaName, contextIds);
                }
                contextIds.add(Integer.valueOf(contextId));
            }
            return schemasAndContexts;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
            Database.back(false, con);
        }

    }

    public static int getContextIdBySchema(final String schemaName) throws OXException {
        final Map<String, Set<Integer>> map = getSchemasAndContexts();
        final Set<Integer> set = map.get(schemaName);
        if (null == set) {
            throw UpdateExceptionCodes.UNKNOWN_SCHEMA.create(schemaName);
        }
        return set.iterator().next().intValue();
    }

    /**
     * Load update task by class name.
     * @param className name of the update task class.
     * @return the update task class.
     * @throws OXException if the update task class can not be determined.
     */
    private static UpdateTaskV2 getUpdateTask(final String className) throws OXException {
        final List<UpdateTaskV2> taskList = DynamicList.getInstance().getTaskList();
        for (final UpdateTaskV2 task : taskList) {
            if (task.getClass().getName().equals(className)) {
                return task;
            }
        }
        throw UpdateExceptionCodes.UNKNOWN_TASK.create(className);
    }

    /*
     * ++++++++++++++++++++++++++++ ++ + HELPER METHODS + ++ ++++++++++++++++++++++++++++
     */

    private static SchemaUpdateState getSchema(final int contextId) throws OXException {
        return SchemaStore.getInstance().getSchema(contextId);
    }

}
