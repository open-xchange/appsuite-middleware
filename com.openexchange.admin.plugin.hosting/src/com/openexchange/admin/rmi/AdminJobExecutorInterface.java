
package com.openexchange.admin.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

public interface AdminJobExecutorInterface extends Remote {

	/**
     * RMI name to be used in RMI URL
     */
    public static final String RMI_NAME = "AdminJobExecutor_V2";

    /**
     *	Return a formated list of all jobs currently in the job queue.
     *	<p>
     *  Example:
     * <pre>
     *  ID    Type of Job          Context              Destination              Status
     *  2     Moving Database      111                  Database 9                 Done
     *  1     Moving Filestore     111                  Filestore 4                Done
     * </pre>
     * @return {@link System.format}ed String containing list of jobs.
     * @throws RemoteException
     */
	public String getJobList() throws RemoteException;
	
    /**
     *	get result Vector of job
     *
     * @param jid id of job
     * @return Vector containing result of job
     *         <p>
     *         1st Object is of type <code>String</code> and contains <code>"OK"</code>
     *         when method succeeds and <code>"ERROR"</code> in case of a failure.
     *         <p>
     * @throws RemoteException
     */
	public Vector<Object> getResult(final int jid) throws RemoteException, InterruptedException, ExecutionException;
	
    /**
     *	Flush all jobs in job queue, that are in state DONE
     * 
     * @throws RemoteException
     */
	public void flush() throws RemoteException;
}
