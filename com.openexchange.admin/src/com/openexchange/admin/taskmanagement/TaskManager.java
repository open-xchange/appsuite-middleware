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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.admin.taskmanagement;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.daemons.AdminDaemon;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.TaskManagerException;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.admin.tools.PropertyHandler.PropertyFiles;

public class TaskManager {

    private final AdminCache cache;

    private final PropertyHandler prop;

    private static final Log log = LogFactory.getLog(TaskManager.class);

    private final Hashtable<Integer, ExtendedFutureTask<?>> jobs = new Hashtable<Integer, ExtendedFutureTask<?>>();

    private final ExecutorService executor;

    private int lastID = 0;
    
    private int runningjobs = 0;
    
    private ArrayList<Integer> finishedJobs = new ArrayList<Integer>();

    private static class JobManagerSingletonHolder {
        private static final TaskManager instance = new TaskManager();
    }

    // TODO: Find out how to invoke super with generic types
    private class Extended<V> extends ExtendedFutureTask<V> {
        @SuppressWarnings("unchecked")
        public Extended(final Callable callable, final String typeofjob, final String furtherinformation, final int id) { super(callable, typeofjob, furtherinformation, id); }
        @Override
        protected void done() {
            TaskManager.this.runningjobs--;
            log.debug("Removing job number " + this.id);
            finishedJobs.add(this.id);
        }
    }

    /**
     * This is a singleton so constructor is private use getInstance instead
     * @throws InvalidDataException 
     */
    private TaskManager() {
        this.cache = ClientAdminThread.cache;
        this.prop = this.cache.getProperties();
        int threadCount = 2;
        try {
            threadCount = this.prop.getInt(PropertyFiles.ADMIN, "CONCURRENT_JOBS", 2);
        } catch (final InvalidDataException e) {
            log.error("Error while reading concurrent Jobs. Shutting down system...", e);
            AdminDaemon.shutdown();
        }
        if (log.isInfoEnabled()) {
            log.info("AdminJobExecutor: running " + threadCount + " jobs parallel");
        }
        this.executor = Executors.newFixedThreadPool(threadCount);
    }

    public static TaskManager getInstance() {
        return JobManagerSingletonHolder.instance;
    }

    @SuppressWarnings("unchecked")
    public int addJob(final Callable<?> jobcall, final String typeofjob, final String furtherinformation) {
        final Extended<?> job = new Extended(jobcall, typeofjob, furtherinformation, ++this.lastID);
        this.jobs.put(this.lastID, job);
        if (log.isDebugEnabled()) {
        log.debug("Adding job number " + this.lastID);
        }
        runningjobs++;
        this.executor.execute(job);
        return lastID;
    }
    
    public boolean jobsRunning() {
        return (runningjobs > 0);
    }
    
    public void shutdown() {
        this.executor.shutdown();
    }

    public ExtendedFutureTask<?> getTask(final int jid) {
        synchronized (this.jobs) {
            return this.jobs.get(jid);
        }
    }

    public String getJobList() {
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
            final ExtendedFutureTask<?> job = this.jobs.get(id);
            buf.append(String.format(VFORMAT, id, job.getTypeofjob(), formatStatus(job), job.getFurtherinformation()));
        }
        return buf.toString();
    }
    
    public void deleteJob(int id) throws TaskManagerException {
        final ExtendedFutureTask<?> job = this.jobs.get(id);
        if (!job.isRunning()) {
            this.jobs.remove(id);
        } else {
            throw new TaskManagerException("The job with the id " + id + " is currently running and cannot be deleted");
        }
    }
    
    public void flush() throws TaskManagerException {
        while(!finishedJobs.isEmpty()) {
            final Integer jobid = finishedJobs.get(0);
            deleteJob(jobid);
            finishedJobs.remove(0);
        }
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
