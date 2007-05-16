package com.openexchange.admin.rmi.impl;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

import com.openexchange.admin.rmi.BasicAuthenticator;
import com.openexchange.admin.rmi.OXTaskMgmtInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.taskmanagement.ExtendedFutureTask;
import com.openexchange.admin.taskmanagement.TaskManager;

public class OXTaskMgmtImpl extends BasicAuthenticator implements OXTaskMgmtInterface {
    
    public Object getTaskResults(final Context ctx, final Credentials cred, final int id) throws RemoteException, InvalidCredentialsException, StorageException, InterruptedException, ExecutionException, InvalidDataException {
        doNullCheck(ctx,cred);
        contextcheck(ctx);
        doAuthentication(cred, ctx);
        final ExtendedFutureTask<?> task = TaskManager.getInstance().getTask(id);
        if (null != task && task.isDone()) {
            return task.get();
        } else {
            return null;
        }
    }

    public String getJobList(final Context ctx, final Credentials cred) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException {
        doNullCheck(ctx,cred);
        contextcheck(ctx);
        doAuthentication(cred, ctx);
        return TaskManager.getInstance().getJobList();
    }

}
