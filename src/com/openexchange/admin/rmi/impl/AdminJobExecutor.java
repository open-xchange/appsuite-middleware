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
package com.openexchange.admin.rmi.impl;

import com.openexchange.admin.rmi.AdminJobExecutorInterface;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.AdminJobExecutorGlobalInterface;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.jobs.AdminJob;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.PropertyHandler;

/**
 * @author choeger
 *
 */
public class AdminJobExecutor implements AdminJobExecutorInterface, AdminJobExecutorGlobalInterface {

    private AdminCache      cache   = null;
    private PropertyHandler prop    = null;
    private static Log log = LogFactory.getLog(AdminJobExecutor.class);

	private Hashtable<Integer, AdminJob> jobs =
		new Hashtable<Integer, AdminJob>();
	private ExecutorService executor = null;
	private int lastID = 0;

	/**
	 * 
	 */
	public AdminJobExecutor() {
        this.cache = ClientAdminThread.cache;
        this.prop    = cache.getProperties();
        int threadCount = Integer.parseInt(prop.getProp("CONCURRENT_JOBS","2"));
        log.info("AdminJobExecutor: running " + threadCount + " jobs parallel");
        this.executor = Executors.newFixedThreadPool(threadCount);
	}

	public void addJob(Callable<Vector> jobcall, int context, int destination, int reason, AdminJob.Mode mode) {
		AdminJob job = new AdminJob(jobcall);
		job.setMode(mode);
		job.setContext(context);
		job.setReason(reason);
		job.setDestination(destination);
		jobs.put(++lastID, job);
		executor.execute(job);
	}
	
	public boolean jobsRunning() {
		boolean haveJobs = false;

		Enumeration<Integer> jids = jobs.keys();
		
		while(jids.hasMoreElements() ) {
			Integer id = jids.nextElement();
			AdminJob job = jobs.get(id);
			if( job.isRunning() ) {
				haveJobs = true;
			}
		}
		return haveJobs;
	}
	
	public void shutdown() {
		executor.shutdown();
	}

	public Vector<Object> getResult(final int jid) throws RemoteException, InterruptedException, ExecutionException {
		Vector<Object> ret = new Vector<Object>();
		synchronized (jobs) {
			if( ! jobs.containsKey(jid) ) {
				ret.add("ERROR");
				ret.add("no such id: "+jid);
			} else {
				AdminJob ajob = jobs.get(jid);
				ret = ajob.get();
			}
		}
		return ret;
	}
	
	/**
	 * flushing all jobs in DONE or FAILED state
	 */
	public void flush() throws RemoteException {
		synchronized (jobs) {
			Enumeration<Integer> jids = jobs.keys();

			while(jids.hasMoreElements() ) {
				Integer id = jids.nextElement();
				AdminJob job = jobs.get(id);
				if( job.isDone() || job.isFailed() ) {
					jobs.remove(id);
				}
			}
		}
	}

	private String FormatJobText(final AdminJob.Mode mode) {
		if (mode == AdminJob.Mode.MOVE_DATABASE) {
			return "Moving Database";
		} else {
			return "Moving Filestore";
		}
	}
	
	private String FormatDestination(final AdminJob.Mode mode, final int dest) {
		if (mode == AdminJob.Mode.MOVE_DATABASE) {
			return "Database " + dest;
		} else {
			return "Filestore " + dest;
		}
	}
	
	private String FormatStatus(final AdminJob job) {
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

	public String getJobList() throws RemoteException {
		StringBuffer buf = new StringBuffer();
		Enumeration<Integer> jids = jobs.keys();
        
		String TFORMAT = "%-5s %-20s %-20s %-20s %10s \n";
		String VFORMAT = "%-5s %-20s %-20s %-20s %10s \n";
		if (jids.hasMoreElements()) {
			buf.append(String.format(TFORMAT,
	                "ID", "Type of Job", "Context", "Destination", "Status"));
		} else {
			buf.append("Currently no jobs queued");
		}
		while(jids.hasMoreElements() ) {
			Integer id = jids.nextElement();
			AdminJob job = jobs.get(id);
            buf.append(String.format(VFORMAT, id, FormatJobText(job.getMode()), job.getContext(),
            		FormatDestination(job.getMode(), job.getDestination()), FormatStatus(job)));
		}
		return buf.toString();
	}
}
