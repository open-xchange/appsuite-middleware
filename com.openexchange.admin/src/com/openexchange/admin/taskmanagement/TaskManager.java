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
package com.openexchange.admin.taskmanagement;

import static com.openexchange.java.Autoboxing.I;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.exceptions.TaskManagerException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link TaskManager} - The task manager for job scheduling.
 */
public class TaskManager {

    /** The logger */
    static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TaskManager.class);

    /** The number of milliseconds a finished job may remain idle in task manager prior to being removed */
    private static final long MAX_TASK_IDLE_MILLIS = TimeUnit.HOURS.toMillis(1L);

    /** The task manager instance */
    private static final TaskManager INSTANCE = new TaskManager();

    /**
     * Gets the task manager instance.
     *
     * @return The task manager instance
     */
    public static TaskManager getInstance() {
        return INSTANCE;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private static class IncrementingCallable<V> implements Callable<V> {

        private final Callable<V> delegate;
        private final AtomicInteger numberOfRunningJobs;
        private final int id;
        private final String typeofjob;

        /**
         * Initializes a new {@link IncrementingCallable}.
         */
        IncrementingCallable(Callable<V> delegate, String typeofjob, int id, AtomicInteger runningjobs) {
            super();
            this.id = id;
            this.typeofjob = typeofjob;
            this.numberOfRunningJobs = runningjobs;
            this.delegate = delegate;
        }

        @Override
        public V call() throws Exception {
            numberOfRunningJobs.incrementAndGet();
            try {
                V result = delegate.call();
                LOGGER.info("Job '{}' with number {} successfully terminated.", typeofjob, I(id));
                return result;
            } catch (Exception e) {
                LOGGER.error("Job '{}' with number {} failed.", typeofjob, I(id), e);
                throw e;
            } catch (Throwable t) {
                LOGGER.error("Job '{}' with number {} failed.", typeofjob, I(id), t);
                throw new Exception(t);
            } finally {
                numberOfRunningJobs.decrementAndGet();
            }
        }
    }

    private static class Extended<V> extends ExtendedFutureTask<V> {

        private final AtomicReference<Long> completionStamp;
        private final TaskManager taskManager;

        /**
         * Initializes a new {@link Extended}.
         */
        Extended(Callable<V> callable, String typeofjob, String furtherinformation, int id, int cid, TaskManager taskManager) {
            super(new IncrementingCallable<V>(callable, typeofjob, id, taskManager.numberOfRunningJobs), typeofjob, furtherinformation, id, cid);
            this.taskManager = taskManager;
            completionStamp = new AtomicReference<Long>(null);
        }

        @Override
        protected void done() {
            Integer id = Integer.valueOf(this.id);
            LOGGER.debug("Removing job number {}", id);
            completionStamp.set(Long.valueOf(System.currentTimeMillis()));
            taskManager.finishedJobs.offer(id);
        }

        /**
         * Gets the completion stamp.
         *
         * @return The completion stamp or <code>null</code>
         */
        public Long completionStmap() {
            return completionStamp.get();
        }
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    /** The number of currently running jobs */
    final AtomicInteger numberOfRunningJobs;

    /** The queue for identifiers of finished jobs; gets periodically cleaned once per hour */
    final Queue<Integer> finishedJobs = new ConcurrentLinkedQueue<Integer>();

    private final AdminCache cache;
    private final PropertyHandler prop;
    private final ConcurrentMap<Integer, Extended<?>> jobs;
    private final ExecutorService executor;
    private final AtomicInteger lastID;

    /** The timer task for job clean-up */
    private volatile ScheduledTimerTask timerTask;

    /**
     * Prevent instantiation. Use {@link #getInstance()} instead.
     */
    private TaskManager() {
        super();
        lastID = new AtomicInteger(0);
        numberOfRunningJobs = new AtomicInteger(0);
        jobs = new ConcurrentHashMap<Integer, Extended<?>>(16, 0.9F, 1);
        this.cache = ClientAdminThread.cache;
        this.prop = this.cache.getProperties();
        final int threadCount = Integer.parseInt(this.prop.getProp("CONCURRENT_JOBS", "2"));
        LOGGER.info("AdminJobExecutor: running {} jobs parallel", Integer.valueOf(threadCount));
        this.executor = Executors.newFixedThreadPool(threadCount, new TaskManagerThreadFactory());
    }

    /**
     * Adds a job to this task manager.
     *
     * @param <V> the result type of method <tt>call</tt>
     * @param jobcall The <code>Callable</code> to schedule with a new job
     * @param typeofjob The job type
     * @param furtherinformation Arbitrary information
     * @param cid The associated context identifier
     * @return The job identifier
     * @throws RejectedExecutionException If the task cannot be accepted for execution
     */
    public <V> int addJob(Callable<V> jobcall, String typeofjob, String furtherinformation, int cid) {
        // Get next job identifier
        int jobId = lastID.incrementAndGet();

        // Instantiate job
        Extended<V> job = new Extended<V>(jobcall, typeofjob, furtherinformation, jobId, cid, this);
        this.jobs.put(Integer.valueOf(jobId), job);

        // Schedule job
        LOGGER.info("Adding job number {}", Integer.valueOf(jobId));
        this.executor.execute(job);

        // Return job identifier
        return jobId;
    }

    /**
     * Starts the periodic cleaner using specified timer service
     *
     * @param timerService The timer service to use
     */
    public void startCleaner(TimerService timerService) {
        ScheduledTimerTask timerTask = this.timerTask;
        if (null == timerTask) {
            synchronized (this) {
                timerTask = this.timerTask;
                if (null == timerTask) {
                    Runnable cleaner = new Runnable() {

                        @Override
                        public void run() {
                            cleanUp();
                        }
                    };
                    timerTask = timerService.scheduleWithFixedDelay(cleaner, 20L, 20L, TimeUnit.MINUTES);
                    this.timerTask = timerTask;
                }
            }
        }
    }

    /**
     * Checks if there are currently running jobs.
     *
     * @return <code>true</code> if there are currently running jobs; otherwise <code>false</code>
     */
    public boolean jobsRunning() {
        return (numberOfRunningJobs.get() > 0);
    }

    /**
     * Shuts-down this task manager.
     * <p>
     * Initiates an orderly shutdown in which previously submitted jobs are executed, but no new job will be accepted.
     */
    public void shutdown() {
        ScheduledTimerTask timerTask = this.timerTask;
        if (null != timerTask) {
            this.timerTask = null;
            timerTask.cancel(true);
        }
        this.executor.shutdown();
    }

    /**
     * Gets the task associated with specified identifier
     *
     * @param jid The task identifier
     * @return The task or <code>null</code>
     */
    public ExtendedFutureTask<?> getTask(int jid) {
        try {
            return getTask(jid, null);
        } catch (TaskManagerException e) {
            // Cannot occur
            throw new IllegalStateException("TaskManagerException although no context identifier specified", e);
        }
    }

    /**
     * Gets the task associated with specified identifier
     *
     * @param jid The task identifier
     * @param cid The context identifier or <code>null</code>
     * @return The task or <code>null</code>
     * @throws TaskManagerException If task does not belong to given context identifier (if specified)
     */
    public ExtendedFutureTask<?> getTask(int jid, Integer cid) throws TaskManagerException {
        ExtendedFutureTask<?> job = jobs.get(Integer.valueOf(jid));
        if (null == job) {
            return null;
        }

        if (null != cid && job.cid != cid.intValue()) {
            throw new TaskManagerException("The job with the id " + jid + " does not belong to context id " + cid);
        }

        return job;
    }

    /**
     * Deletes specified job.
     *
     * @param id The job identifier
     */
    public void deleteJob(int id) {
        try {
            deleteJob(id, null);
        } catch (TaskManagerException e) {
            // Cannot occur
            throw new IllegalStateException("TaskManagerException although no context identifier specified", e);
        }
    }

    /**
     * Deletes the specified job associated with given context (if specified).
     *
     * @param id The job identifier
     * @param cid The context identifier or <code>null</code>
     * @throws TaskManagerException If the task to delete does not belong to given context identifier
     */
    public void deleteJob(int id, Integer cid) throws TaskManagerException {
        Integer jobId = Integer.valueOf(id);
        ExtendedFutureTask<?> job = jobs.get(jobId);
        if (null == job) {
            // No such job
            return;
        }

        if (null != cid && job.cid != cid.intValue()) {
            throw new TaskManagerException("The job with the id " + id + " does not belong to context id " + cid);
        }

        if (job.isRunning()) {
            throw new TaskManagerException("The job with the id " + id + " is currently running and cannot be deleted");
        }
        this.jobs.remove(jobId, job);
    }

    /**
     * Flushes this task manager.
     */
    public void flush() {
        try {
            flush(null);
        } catch (TaskManagerException e) {
            // Cannot occur
            throw new IllegalStateException("TaskManagerException although no context identifier specified", e);
        }
    }

    /**
     * Flushes this task manager affecting tasks belonging to specified context (if specified).
     *
     * @param cid The context identifier or <code>null</code>
     * @throws TaskManagerException If a finished task does not belong to given context
     */
    public void flush(final Integer cid) throws TaskManagerException {
        for (Integer jobid; (jobid = finishedJobs.poll()) != null;) {
            deleteJob(jobid.intValue(), cid);
        }
    }

    /**
     * Flushes this task manager affecting tasks older than 1 hour.
     */
    void cleanUp() {
        Thread currentThread = Thread.currentThread();
        for (Iterator<Integer> iter = finishedJobs.iterator(); !currentThread.isInterrupted() && iter.hasNext();) {
            Integer jobId = iter.next();
            Extended<?> job = jobs.get(jobId);
            if (null == job) {
                // No such job
                iter.remove();
            } else {
                Long completionStmap = job.completionStmap();
                if (null != completionStmap && (System.currentTimeMillis() - completionStmap.longValue()) > MAX_TASK_IDLE_MILLIS) {
                    iter.remove();
                    if (jobs.remove(jobId, job)) {
                        LOGGER.info("Cleaned-up timed out job {}.", jobId);
                    }
                }
            }
        }
    }

    /**
     * Gets the pretty-printed job list
     *
     * @return The pretty-printed job list
     */
    public String getJobList() {
        return getJobList(null);
    }

    private final static String PRETTY_PRINT_FORMAT = "%-5s %-40s %-10s %-40s %n";

    /**
     * Gets the pretty-printed list for jobs belonging to specified context (if not <code>null</code>).
     *
     * @param cid The context identifier or <code>null</code>
     * @return The pretty-printed job list
     */
    public String getJobList(Integer cid) {
        Iterator<Entry<Integer, Extended<?>>> jids = jobs.entrySet().iterator();
        if (!jids.hasNext()) {
            return "Currently no jobs queued";
        }

        StringBuffer buf = new StringBuffer(256);
        buf.append(String.format(PRETTY_PRINT_FORMAT, "ID", "Type of Job", "Status", "Further Information"));

        while (jids.hasNext()) {
            final Entry<Integer, Extended<?>> jidEntry = jids.next();
            final ExtendedFutureTask<?> job = jidEntry.getValue();
            if (null == cid || job.cid == cid.intValue() ) {
                buf.append(String.format(PRETTY_PRINT_FORMAT, jidEntry.getKey(), job.getTypeofjob(), formatStatus(job), job.getFurtherinformation()));
            }
        }
        return buf.toString();
    }

    /**
     * Gets a pretty-printed info about the job with the given id
     *
     * @param An optional context id
     * @param jobId The job id
     * @return The pretty-printed job info
     */
    public String getJob(Integer cid, Integer jobId) {

        ExtendedFutureTask<?> job = jobs.get(jobId);
        if (job == null) {
            return "Job not found";
        }

        if (cid != null && job.cid != cid.intValue()) {
            return "Job not found";
        }

        StringBuffer buf = new StringBuffer(256);
        buf.append(String.format(PRETTY_PRINT_FORMAT, "ID", "Type of Job", "Status", "Further Information"));
        buf.append(String.format(PRETTY_PRINT_FORMAT, jobId, job.getTypeofjob(), formatStatus(job), job.getFurtherinformation()));
        return buf.toString();
    }

    private String formatStatus(final ExtendedFutureTask<?> job) {
        if (job.isRunning()) {
            return "Running";
        } else if (job.isFailed()) {
            return "Failed";
        } else if (job.isDone()) {
            return "Done";
        } else if (job.isCancelled()) {
            return "Cancelled";
        } else {
            return "Waiting";
        }
    }

}
