
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
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
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
        this.cache = ClientAdminThreadExtended.cache;
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
