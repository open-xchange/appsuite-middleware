package com.openexchange.admin.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * This interface defines the methods of the task management which are accessibly through RMI
 * 
 * @author d7
 *
 */
public interface OXTaskMgmtInterface extends Remote {

    /**
     * RMI name to be used in RMI URL
     */
    public static final String RMI_NAME = "OXTaskManagement";

    /**
     * Gets the result from the task with the specified id
     * 
     * @param ctx
     * @param cred
     * @param id
     * @return an object which has to be casted to the return value specified in the method which
     * adds the job
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws InvalidDataException
     */
    public Object getTaskResults(final Context ctx, final Credentials cred, final int id)
        throws RemoteException, InvalidCredentialsException, StorageException, InterruptedException, ExecutionException, InvalidDataException;
    
    /**
     * @return
     * @throws RemoteException
     * @throws InvalidDataException 
     * @throws StorageException 
     * @throws InvalidCredentialsException 
     */
    public String getJobList(final Context ctx, final Credentials cred) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException;
}
