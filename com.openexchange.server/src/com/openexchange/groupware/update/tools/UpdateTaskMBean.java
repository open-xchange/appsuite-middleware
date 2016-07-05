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

import static com.openexchange.groupware.update.tools.Utility.parsePositiveInt;
import static com.openexchange.java.Autoboxing.B;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.ExecutedTask;
import com.openexchange.groupware.update.SchemaStore;
import com.openexchange.groupware.update.TaskInfo;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.internal.UpdateProcess;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.CanceledTimerTaskException;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * MBean for update task toolkit.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class UpdateTaskMBean implements DynamicMBean {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdateTaskMBean.class);

    private final MBeanInfo mbeanInfo;

    private final String[] taskTypeNames = { "taskName", "successful", "lastModified" };
    private final ConcurrentMap<String, JobInfo<?>> jobs;
    private CompositeType taskType;
    private TabularType taskListType;
    private ScheduledTimerTask timerTask; // Guarded by synchronized

    /**
     * Initializes a new {@link UpdateTaskMBean}.
     */
    public UpdateTaskMBean() {
        super();
        mbeanInfo = buildMBeanInfo();
        jobs = new ConcurrentHashMap<String, JobInfo<?>>(10, 0.9F, 1);
    }

    private void addJobInfo(String jobId, JobInfo<Void> jobInfo) {
        synchronized (this) {
            jobs.put(jobId, jobInfo);

            if (null == timerTask) {
                TimerService timerService = ServerServiceRegistry.getInstance().getService(TimerService.class);
                if (null != timerService) {
                    Runnable task = new Runnable() {

                        @Override
                        public void run() {
                            cleanUp();
                        }
                    };
                    timerTask = timerService.scheduleWithFixedDelay(task, 5L, 5L, TimeUnit.MINUTES);
                }
            }

            jobInfo.start();
        }
    }

    private String getJobStatusText(final Object[] params) {
        String key = params[0].toString();
        JobInfo<?> jobInfo = jobs.get(key);
        if (null == jobInfo) {
            return null;
        }

        String stText = jobInfo.getStatusText();
        if (jobInfo.isDone()) {
            if (null != jobs.remove(key)) {
                synchronized (this) {
                    dropTimerTaskIfEmpty();
                }
            }
            return "NOK:" + stText;
        }

        return "OK:" + stText;
    }

    /**
     * Cleans-up the job collection.
     *
     * @throws CanceledTimerTaskException If timer task is supposed to be terminated
     */
    void cleanUp() {
        synchronized (this) {
            boolean somethingRemoved = false;
            for (Iterator<JobInfo<?>> it = jobs.values().iterator(); it.hasNext();) {
                JobInfo<?> jobInfo = it.next();
                if (null == jobInfo || jobInfo.isDone()) {
                    it.remove();
                    somethingRemoved = true;
                }
            }
            if (somethingRemoved && dropTimerTaskIfEmpty()) {
                throw new CanceledTimerTaskException();
            }
        }
    }

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

    private MBeanInfo buildMBeanInfo() {
        final List<MBeanOperationInfo> operations = new ArrayList<MBeanOperationInfo>(6);
        // Trigger update process
        final MBeanParameterInfo[] tparams = { new MBeanParameterInfo(
            "id",
            "java.lang.String",
            "A valid context identifier contained in target schema or a schema name") };
        operations.add(new MBeanOperationInfo("runUpdate", "Runs the schema's update.", tparams, "void", MBeanOperationInfo.ACTION));
        // Force re-run operation
        final MBeanParameterInfo[] forceParams = {
            new MBeanParameterInfo("className", "java.lang.String", "The update task's class name"),
            new MBeanParameterInfo("id", "java.lang.String", "A valid context identifier contained in target schema or a schema name") };
        operations.add(new MBeanOperationInfo(
            "force",
            "Forces re-run of given update task.",
            forceParams,
            "void",
            MBeanOperationInfo.ACTION));
        // Force re-run operation on all schemas
        final MBeanParameterInfo[] forceAllParams = { new MBeanParameterInfo(
            "className",
            "java.lang.String",
            "The update task's class name") };
        operations.add(new MBeanOperationInfo(
            "forceOnAllSchemas",
            "Forces re-run of given update task on all schemas.",
            forceAllParams,
            "void",
            MBeanOperationInfo.ACTION));
        // Run update on all database schemas
        operations.add(new MBeanOperationInfo("runAllUpdate", "Runs the update on all schemas.", null, "void", MBeanOperationInfo.ACTION));
        try {
            // List executed update tasks
            final String[] taskTypeDescriptions = {
                "Class name of the update task", "Wether it is executed successfully or not.", "Last task execution time stamp." };
            final OpenType[] taskTypes = { SimpleType.STRING, SimpleType.BOOLEAN, SimpleType.DATE };
            taskType = new CompositeType(
                "Update task",
                "Executed update task",
                taskTypeNames,
                taskTypeDescriptions,
                taskTypes);
            taskListType = new TabularType("UpdateTask list", "List of update tasks.", taskType, new String[] { "taskName" });
            final MBeanParameterInfo[] listExecutedTasks = { new MBeanParameterInfo(
                "schema",
                "java.lang.String",
                "Name of a schema that update tasks should be listed.") };
            operations.add(new MBeanOperationInfo(
                "listExecutedTasks",
                "Lists executed update tasks of a schema.",
                listExecutedTasks,
                "javax.management.openmbean.TabularData",
                MBeanOperationInfo.INFO));
        } catch (final OpenDataException e) {
            LOG.error("", e);
        }
        // Get status text
        final MBeanParameterInfo[] sparams = { new MBeanParameterInfo(
            "id",
            "java.lang.String",
            "A valid job identifier") };
        operations.add(new MBeanOperationInfo("getStatus", "Gets the status text for a given job identifier.", tparams, "void", MBeanOperationInfo.ACTION));
        // MBean info
        return new MBeanInfo(UpdateTaskMBean.class.getName(), "Update task toolkit", null, null, operations.toArray(new MBeanOperationInfo[operations.size()]), null);
    }

    @Override
    public Object getAttribute(final String attribute) throws AttributeNotFoundException {
        throw new AttributeNotFoundException("No attribute can be obtained in this MBean");
    }

    @Override
    public AttributeList getAttributes(final String[] attributes) {
        return new AttributeList();
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return mbeanInfo;
    }

    @Override
    public Object invoke(final String actionName, final Object[] params, final String[] signature) throws MBeanException, ReflectionException {
        if (actionName.equals("runUpdate")) {
            try {
                // Initialize UpdateProcess instance
                UpdateProcess updateProcess;
                {
                    Object param = params[0];
                    if (param instanceof Integer) {
                        updateProcess = new UpdateProcess(((Integer) param).intValue());
                    } else {
                        final String sParam = param.toString();
                        final int parsed = parsePositiveInt(sParam);

                        updateProcess = parsed >= 0 ? new UpdateProcess(parsed, true, false) : new UpdateProcess(UpdateTaskToolkit.getContextIdBySchema(param.toString()), true, false);
                    }
                }

                // Trigger run
                updateProcess.run();

                // Return possible failures
                Queue<TaskInfo> failures = updateProcess.getFailures();
                if (null != failures && !failures.isEmpty()) {
                    final StringBuilder sb = new StringBuilder("The following update task(s) failed: \\R");
                    boolean first = true;
                    for (final TaskInfo taskInfo : failures) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append("\\R");
                        }
                        sb.append(' ').append(taskInfo.getTaskName()).append(" (schema=").append(taskInfo.getSchema()).append(')');
                    }
                    return sb.toString();
                }
            } catch (final OXException e) {
                LOG.error("", e);
                final Exception wrapMe = new Exception(e.getPlainLogMessage());
                throw new MBeanException(wrapMe);
            } catch (final RuntimeException e) {
                LOG.error("", e);
                throw e;
            } catch (final Error e) {
                LOG.error("", e);
                throw e;
            }
            // Void
            return null;
        } else if (actionName.equals("runAllUpdate")) {
            try {
                boolean throwExceptionOnFailure;
                {
                    Object param = params[0];
                    if (param instanceof Boolean) {
                        throwExceptionOnFailure = ((Boolean) param).booleanValue();
                    } else {
                        throwExceptionOnFailure = Boolean.parseBoolean(param.toString());
                    }
                }

                String jobId = UUIDs.getUnformattedString(UUID.randomUUID());
                JobInfo<Void> jobInfo = UpdateTaskToolkit.runUpdateOnAllSchemas(jobId, throwExceptionOnFailure);
                addJobInfo(jobId, jobInfo);
                return jobId;
            } catch (final OXException e) {
                LOG.error("", e);
                final Exception wrapMe = new Exception(e.getPlainLogMessage());
                throw new MBeanException(wrapMe);
            } catch (final RuntimeException e) {
                LOG.error("", e);
                throw e;
            } catch (final Error e) {
                LOG.error("", e);
                throw e;
            }
        } else if (actionName.equals("force")) {
            try {
                final Object secParam = params[1];
                if (secParam instanceof Integer) {
                    UpdateTaskToolkit.forceUpdateTask(((String) params[0]), ((Integer) secParam).intValue());
                } else {
                    final String sParam = secParam.toString();
                    final int parsed = parsePositiveInt(sParam);
                    if (parsed >= 0) {
                        UpdateTaskToolkit.forceUpdateTask(((String) params[0]), parsed);
                    } else {
                        UpdateTaskToolkit.forceUpdateTask(((String) params[0]), sParam);
                    }
                }
            } catch (final OXException e) {
                LOG.error("", e);
                final Exception wrapMe = new Exception(e.getPlainLogMessage());
                throw new MBeanException(wrapMe);
            } catch (final RuntimeException e) {
                LOG.error("", e);
                throw e;
            } catch (final Error e) {
                LOG.error("", e);
                throw e;
            }
            // Void
            return null;
        } else if (actionName.equals("forceOnAllSchemas")) {
            try {
                UpdateTaskToolkit.forceUpdateTaskOnAllSchemas(((String) params[0]));
            } catch (final OXException e) {
                LOG.error("", e);
                final Exception wrapMe = new Exception(e.getPlainLogMessage());
                throw new MBeanException(wrapMe);
            } catch (final RuntimeException e) {
                LOG.error("", e);
                throw e;
            } catch (final Error e) {
                LOG.error("", e);
                throw e;
            }
            // Void
            return null;
        } else if (actionName.equals("listExecutedTasks")) {
            try {
                return getExecutedTasksList(params[0].toString());
            } catch (final OXException e) {
                LOG.error("", e);
                String message = e.getPlainLogMessage();
                throw new MBeanException(new Exception(message), message);
            } catch (final RuntimeException e) {
                LOG.error("", e);
                throw e;
            } catch (final Error e) {
                LOG.error("", e);
                throw e;
            }
        } else if (actionName.equals("getStatus")) {
            try {
                return getJobStatusText(params);
            } catch (final RuntimeException e) {
                LOG.error("", e);
                throw e;
            } catch (final Error e) {
                LOG.error("", e);
                throw e;
            }
        }
        final ReflectionException e = new ReflectionException(new NoSuchMethodException(actionName));
        LOG.error("", e);
        throw e;
    }

    @Override
    public void setAttribute(final Attribute attribute) throws AttributeNotFoundException {
        throw new AttributeNotFoundException("No attribute can be set in this MBean");
    }

    @Override
    public AttributeList setAttributes(final AttributeList attributes) {
        return new AttributeList();
    }

    private TabularDataSupport getExecutedTasksList(final String schemaName) throws OXException {
        final SchemaStore store = SchemaStore.getInstance();
        final TabularDataSupport retval;
        try {
            final int contextId = UpdateTaskToolkit.getContextIdBySchema(schemaName);
            final int poolId = Database.resolvePool(contextId, true);
            ExecutedTask[] tasks = store.getExecutedTasks(poolId, schemaName);
            if (null == tasks) {
                tasks = new ExecutedTask[0];
            }
            Arrays.sort(tasks);
            retval = new TabularDataSupport(taskListType, tasks.length, 1);
            for (final ExecutedTask task : tasks) {
                final CompositeDataSupport data = new CompositeDataSupport(taskType, taskTypeNames, new Object[] { task.getTaskName(), B(task.isSuccessful()), task.getLastModified() });
                retval.put(data);
            }
        } catch (final OpenDataException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        }
        return retval;
    }

}
