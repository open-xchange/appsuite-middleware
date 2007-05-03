package com.openexchange.admin.taskmanagement;

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;

public class TaskManager {

    private final AdminCache cache;

    private final PropertyHandler prop;

    private static final Log log = LogFactory.getLog(TaskManager.class);

    private final Hashtable<Integer, ExtendedFutureTask<?>> jobs = new Hashtable<Integer, ExtendedFutureTask<?>>();

    private final ExecutorService executor;

    private int lastID = 0;
    
    private int runningjobs = 0;

    private static class JobManagerSingletonHolder {
        private static final TaskManager instance = new TaskManager();
    }

    // TODO: Find out how to invoke super with generic types
    private class Extended extends ExtendedFutureTask {
        @SuppressWarnings("unchecked")
        public Extended(final Callable callable, final String typeofjob, final String furtherinformation) { super(callable, typeofjob, furtherinformation); }
        @Override
        protected void done() { TaskManager.this.runningjobs--; }
    }

    /**
     * This is a singleton so constructor is private use getInstance instead
     */
    private TaskManager() {
        this.cache = ClientAdminThread.cache;
        this.prop = this.cache.getProperties();
        final int threadCount = Integer.parseInt(this.prop.getProp("CONCURRENT_JOBS", "2"));
        log.info("AdminJobExecutor: running " + threadCount + " jobs parallel");
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    public static TaskManager getInstance() {
        return JobManagerSingletonHolder.instance;
    }

    public int addJob(final Callable jobcall, final String typeofjob, final String furtherinformation) {
        final Extended job = new Extended(jobcall, typeofjob, furtherinformation);
        this.jobs.put(++this.lastID, job);
        log.debug("Adding job number " + lastID);
        runningjobs++;
        this.executor.execute(job);
        return lastID;
    }
    
//    public void addJob(final ExtendedFutureTask job) {
//        jobs.put(lastID++, job);
//        executor.execute(job);
//    }
    
    public boolean jobsRunning() {
        return (runningjobs > 0);
//        boolean haveJobs = false;
//
//        final Enumeration<Integer> jids = this.jobs.keys();
//
//        while (jids.hasMoreElements()) {
//            final Integer id = jids.nextElement();
//            final ExtendedFutureTask job = this.jobs.get(id);
//            if (job.isRunning()) {
//                haveJobs = true;
//            }
//        }
//        return haveJobs;
    }
    
    public void shutdown() {
        this.executor.shutdown();
    }

    public ExtendedFutureTask<?> getTask(final int jid) throws RemoteException {
        synchronized (this.jobs) {
            return this.jobs.get(jid);
        }
    }

    /**
     * flushing all jobs in DONE or FAILED state
     */
    public void flush() throws RemoteException {
        synchronized (this.jobs) {
            final Enumeration<Integer> jids = this.jobs.keys();

            while (jids.hasMoreElements()) {
                final Integer id = jids.nextElement();
                final ExtendedFutureTask job = this.jobs.get(id);
                if (job.isDone() || job.isFailed()) {
                    this.jobs.remove(id);
                }
            }
        }
    }

    public String getJobList() throws RemoteException {
        final StringBuffer buf = new StringBuffer();
        final Enumeration<Integer> jids = this.jobs.keys();
    
        final String TFORMAT = "%-5s %-20s %-10s %-40s \n";
        final String VFORMAT = "%-5s %-20s %-10s %-40s \n";
        if (jids.hasMoreElements()) {
            buf.append(String.format(TFORMAT, "ID", "Type of Job", "Status", "Further Information"));
        } else {
            buf.append("Currently no jobs queued");
        }
        while (jids.hasMoreElements()) {
            final Integer id = jids.nextElement();
            final ExtendedFutureTask job = this.jobs.get(id);
            buf.append(String.format(VFORMAT, id, job.getTypeofjob(), formatStatus(job), job.getFurtherinformation()));
        }
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
