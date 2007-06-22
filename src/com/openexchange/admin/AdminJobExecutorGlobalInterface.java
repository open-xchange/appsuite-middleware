
package com.openexchange.admin;

import java.rmi.RemoteException;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import com.openexchange.admin.jobs.AdminJob;

/**
 * This interface is meant to be able to assign the old style AdminJobExecutor and
 * the new style AdminJobExecutor to one single point. See ClientAdminThread for this.
 * @author d7
 *
 */
public interface AdminJobExecutorGlobalInterface {
	
	public void addJob(Callable<Vector> jobcall, int context, int destination, int reason, AdminJob.Mode mode);
	
	public boolean jobsRunning();
	
	public void shutdown();
	
	public Vector<Object> getResult(final int jid) throws RemoteException, InterruptedException, ExecutionException;
	
	public void flush() throws RemoteException;
	
	public String getJobList() throws RemoteException;
}
