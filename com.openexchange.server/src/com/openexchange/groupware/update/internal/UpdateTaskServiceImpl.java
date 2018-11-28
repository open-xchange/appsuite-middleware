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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.SchemaInfo;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.ExecutedTask;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.TaskInfo;
import com.openexchange.groupware.update.UpdateTaskService;
import com.openexchange.groupware.update.tools.UpdateTaskToolkit;
import com.openexchange.groupware.update.tools.UpdateTaskToolkitJob;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.CanceledTimerTaskException;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link UpdateTaskServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class UpdateTaskServiceImpl implements UpdateTaskService {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateTaskServiceImpl.class);

    private final ConcurrentMap<String, UpdateTaskToolkitJob<?>> jobs;
    private ScheduledTimerTask timerTask; // Guarded by synchronized

    /**
     * Initialises a new {@link UpdateTaskServiceImpl}.
     */
    public UpdateTaskServiceImpl() {
        super();
        jobs = new ConcurrentHashMap<String, UpdateTaskToolkitJob<?>>(10, 0.9F, 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskService#runUpdate(int)
     */
    @Override
    public List<Map<String, Object>> runUpdate(int contextId) throws RemoteException {
        try {
            UpdateProcess updateProcess = new UpdateProcess(contextId);
            updateProcess.run();
            return getFailures(updateProcess);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskService#runUpdate(java.lang.String)
     */
    @Override
    public List<Map<String, Object>> runUpdate(String schemaName) throws RemoteException {
        try {
            SchemaInfo schema = UpdateTaskToolkit.getInfoBySchemaName(schemaName);
            UpdateProcess updateProcess = new UpdateProcess(schema.getPoolId(), schema.getSchema(), true, false);
            updateProcess.run();
            return getFailures(updateProcess);
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskService#runAllUpdates(boolean)
     */
    @Override
    public String runAllUpdates(boolean throwExceptionOnFailure) throws RemoteException {
        try {
            UpdateTaskToolkitJob<Void> job = UpdateTaskToolkit.runUpdateOnAllSchemas(throwExceptionOnFailure);
            addJob(job);
            return job.getId();
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskService#forceUpdateTask(int, java.lang.String)
     */
    @Override
    public void forceUpdateTask(int contextId, String taskName) throws RemoteException {
        try {
            UpdateTaskToolkit.forceUpdateTask(taskName, contextId);
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskService#forceUpdateTask(java.lang.String, java.lang.String)
     */
    @Override
    public void forceUpdateTask(String schemaName, String taskName) throws RemoteException {
        try {
            UpdateTaskToolkit.forceUpdateTask(taskName, schemaName);
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskService#forceUpdateTaskOnAllSchemata(java.lang.String)
     */
    @Override
    public void forceUpdateTaskOnAllSchemata(String taskName) throws RemoteException {
        try {
            UpdateTaskToolkit.forceUpdateTaskOnAllSchemas(taskName);
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskService#getExecutedTasksList(java.lang.String)
     */
    @Override
    public List<Map<String, Object>> getExecutedTasksList(String schemaName) throws RemoteException {
        SchemaStore store = SchemaStore.getInstance();
        try {
            SchemaInfo schemaInfo = UpdateTaskToolkit.getInfoBySchemaName(schemaName);
            ExecutedTask[] tasks = store.getExecutedTasks(schemaInfo.getPoolId(), schemaName);
            if (null == tasks) {
                tasks = new ExecutedTask[0];
            } else {
                Arrays.sort(tasks);
            }
            List<Map<String, Object>> executedTasks = new ArrayList<>(tasks.length);
            for (ExecutedTask task : tasks) {
                executedTasks.add(new TaskMetadataBuilder().withTaskName(task.getTaskName()).withSuccess(Boolean.valueOf(task.isSuccessful())).withLastModified(task.getLastModified()).withUUID(task.getUUID().toString()).build());
            }
            return executedTasks;
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskService#getPendingTasksList(java.lang.String, boolean, boolean)
     */
    @Override
    public List<Map<String, Object>> getPendingTasksList(String schemaName, boolean pending, boolean excluded, boolean namespaceAware) throws RemoteException {
        SchemaStore store = SchemaStore.getInstance();
        try {
            SchemaInfo schemaInfo = UpdateTaskToolkit.getInfoBySchemaName(schemaName);
            ExecutedTask[] tasks = store.getExecutedTasks(schemaInfo.getPoolId(), schemaName);
            if (null == tasks) {
                tasks = new ExecutedTask[0];
            } else {
                Arrays.sort(tasks);
            }

            List<Map<String, Object>> pendingTasks = new LinkedList<>();

            // First get the successfully executed tasks
            Set<String> executedTasks = new HashSet<>();
            for (ExecutedTask task : tasks) {
                if (task.isSuccessful()) {
                    executedTasks.add(task.getTaskName());
                }
            }

            if (pending) {
                // Add all update tasks that are not in the executed set
                Set<String> registeredTasks = UpdateTaskToolkit.getRegisteredUpdateTasks();
                for (String r : registeredTasks) {
                    if (executedTasks.contains(r)) {
                        continue;
                    }
                    pendingTasks.add(new TaskMetadataBuilder().withTaskName(r).withTaskState("pending").build());
                }
            }

            // Consider excluded via properties
            if (excluded) {
                for (String s : UpdateTaskToolkit.getExcludedUpdateTasks()) {
                    pendingTasks.add(new TaskMetadataBuilder().withTaskName(s).withTaskState("excluded via file 'excludedupdatetask.properties'").build());
                }
            }

            // Consider excluded via namespace
            if (namespaceAware) {
                for (Entry<String, Set<String>> entry : UpdateTaskToolkit.getNamespaceAwareUpdateTasks().entrySet()) {
                    String state = "excluded via namespace '" + entry.getKey() + "'";
                    for (String s : entry.getValue()) {
                        pendingTasks.add(new TaskMetadataBuilder().withTaskName(s).withTaskState(state).build());
                    }
                }
            }

            return pendingTasks;
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getPlainLogMessage(), e);
        } catch (RuntimeException | Error e) {
            LOG.error("", e);
            throw e;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskService#getJobStatus(java.lang.String)
     */
    @Override
    public String getJobStatus(String jobId) throws RemoteException {
        UpdateTaskToolkitJob<?> job = jobs.get(jobId);
        if (null == job) {
            return null;
        }

        String stText = job.getStatusText();
        if (job.isDone()) {
            if (null != jobs.remove(jobId)) {
                synchronized (this) {
                    dropTimerTaskIfEmpty();
                }
            }
            return "NOK:" + stText;
        }

        return "OK:" + stText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.groupware.update.UpdateTaskService#getNamespaceAware()
     */
    @Override
    public Map<String, Set<String>> getNamespaceAware() throws RemoteException {
        return UpdateTaskToolkit.getNamespaceAwareUpdateTasks();
    }

    ///////////////////////////////////// HELPERS /////////////////////////////////

    /**
     * Schedules an update task job
     * 
     * @param job The job to schedule
     */
    private void addJob(UpdateTaskToolkitJob<Void> job) {
        synchronized (this) {
            jobs.put(job.getId(), job);

            if (null == timerTask) {
                TimerService timerService = ServerServiceRegistry.getInstance().getService(TimerService.class);
                if (null != timerService) {
                    Runnable task = () -> cleanUp();
                    timerTask = timerService.scheduleWithFixedDelay(task, 5L, 5L, TimeUnit.MINUTES);
                }
            }

            job.start();
        }
    }

    /**
     * Cleans-up the job collection.
     *
     * @throws CanceledTimerTaskException If timer task is supposed to be terminated
     */
    void cleanUp() {
        synchronized (this) {
            boolean somethingRemoved = false;
            for (Iterator<UpdateTaskToolkitJob<?>> it = jobs.values().iterator(); it.hasNext();) {
                UpdateTaskToolkitJob<?> job = it.next();
                if (null == job || job.isDone()) {
                    it.remove();
                    somethingRemoved = true;
                }
            }
            if (somethingRemoved && dropTimerTaskIfEmpty()) {
                throw new CanceledTimerTaskException();
            }
        }
    }

    /**
     * Drops the timer task if no jobs are running
     * 
     * @return <code>true</code> if the timer task was dropped and the {@link TimerService} purged;
     *         <code>false</code> otherwise
     */
    private boolean dropTimerTaskIfEmpty() {
        if (null == timerTask || !jobs.isEmpty()) {
            return false;
        }

        timerTask.cancel();
        TimerService timerService = ServerServiceRegistry.getInstance().getService(TimerService.class);
        if (null != timerService) {
            timerService.purge();
        }
        timerTask = null;
        return true;
    }

    /**
     * Returns any failures the update task encountered
     * 
     * @param updateProcess the {@link UpdateProcess}
     * @return the failures
     */
    private List<Map<String, Object>> getFailures(UpdateProcess updateProcess) {
        // Return possible failures
        Queue<TaskInfo> failures = updateProcess.getFailures();
        if (failures == null || failures.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> failuresList = new ArrayList<>(failures.size());
        for (TaskInfo taskInfo : failures) {
            failuresList.add(new TaskMetadataBuilder().withTaskName(taskInfo.getTaskName()).withClassName(taskInfo.getClass().getName()).withSchema(taskInfo.getSchema()).build());
        }
        return failuresList;
    }

    /////////////////////////////////// METADATA BUILDER ////////////////////////////////////////

    private enum TaskMetadata {
        taskName, state, successful, lastModified, uuid, schema, className;
    }

    /**
     * {@link TaskMetadataBuilder}
     */
    private static final class TaskMetadataBuilder {

        private String taskName;
        private String taskState;
        private String className;
        private String schema;
        private Boolean isSuccessful;
        private Date lastModified;
        private String uuid;

        /**
         * Initialises a new {@link UpdateTaskServiceImpl.TaskMetadataBuilder}.
         */
        TaskMetadataBuilder() {
            super();
        }

        /**
         * Sets the specified task name
         * 
         * @param taskName The task name to set
         * @return <code>this</code> instance for chained calls
         */
        final TaskMetadataBuilder withTaskName(String taskName) {
            this.taskName = taskName;
            return this;
        }

        /**
         * Sets the specified task state
         * 
         * @param taskState The task state to set
         * @return <code>this</code> instance for chained calls
         */
        final TaskMetadataBuilder withTaskState(String taskState) {
            this.taskState = taskState;
            return this;
        }

        /**
         * Sets the specified task class name
         * 
         * @param className The task class name to set
         * @return <code>this</code> instance for chained calls
         */
        final TaskMetadataBuilder withClassName(String className) {
            this.className = className;
            return this;
        }

        /**
         * Sets the specified schema name
         * 
         * @param schema The schema name to set
         * @return <code>this</code> instance for chained calls
         */
        final TaskMetadataBuilder withSchema(String schema) {
            this.schema = schema;
            return this;
        }

        /**
         * Sets whether the task was executed successfully
         * 
         * @param successful whether the task was executed successfully
         * @return <code>this</code> instance for chained calls
         */
        final TaskMetadataBuilder withSuccess(Boolean successful) {
            this.isSuccessful = successful;
            return this;
        }

        /**
         * Sets whether the last modified date of the task
         * 
         * @param lastModified the last modified date of the task
         * @return <code>this</code> instance for chained calls
         */
        final TaskMetadataBuilder withLastModified(Date lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        /**
         * Sets the specified task UUID
         * 
         * @param uuid The task uuid to set
         * @return <code>this</code> instance for chained calls
         */
        final TaskMetadataBuilder withUUID(String uuid) {
            this.uuid = uuid;
            return this;
        }

        /**
         * Builds an unmodifiable {@link Map} with the {@link TaskMetadata}
         * 
         * @return an unmodifiable {@link Map} with the {@link TaskMetadata}
         */
        Map<String, Object> build() {
            Map<String, Object> taskMap = new HashMap<>(8);
            if (Strings.isNotEmpty(taskName)) {
                taskMap.put(TaskMetadata.taskName.name(), taskName);
            }
            if (Strings.isNotEmpty(taskState)) {
                taskMap.put(TaskMetadata.state.name(), taskState);
            }
            if (Strings.isNotEmpty(className)) {
                taskMap.put(TaskMetadata.className.name(), className);
            }
            if (Strings.isNotEmpty(schema)) {
                taskMap.put(TaskMetadata.schema.name(), schema);
            }
            if (null != isSuccessful) {
                taskMap.put(TaskMetadata.successful.name(), isSuccessful);
            }
            if (null != lastModified) {
                taskMap.put(TaskMetadata.lastModified.name(), lastModified);
            }
            if (Strings.isNotEmpty(uuid)) {
                taskMap.put(TaskMetadata.uuid.name(), uuid);
            }
            return Collections.unmodifiableMap(taskMap);
        }
    }
}
