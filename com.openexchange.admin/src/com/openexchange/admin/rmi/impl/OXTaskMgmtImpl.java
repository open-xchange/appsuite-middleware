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

package com.openexchange.admin.rmi.impl;

import static com.openexchange.java.Autoboxing.I;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.OXTaskMgmtInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.AbstractAdminRmiException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.RemoteExceptionUtils;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.exceptions.TaskManagerException;
import com.openexchange.admin.taskmanagement.ExtendedFutureTask;
import com.openexchange.admin.taskmanagement.TaskManager;
import com.openexchange.admin.tools.AdminCache;

public class OXTaskMgmtImpl extends OXCommonImpl implements OXTaskMgmtInterface {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXTaskMgmtImpl.class);

    private final AdminCache cache;

    public OXTaskMgmtImpl() {
        super();
        this.cache = ClientAdminThread.cache;
    }

    private void logAndEnhanceException(Throwable t, final Credentials credentials, final Context ctx, final int id) {
        logAndEnhanceException(t, credentials, null != ctx ? ctx.getIdAsString() : null, Integer.toString(id));
    }

    private void logAndEnhanceException(Throwable t, final Credentials credentials, final String contextId, final String id) {
        if (t instanceof AbstractAdminRmiException) {
            logAndReturnException(log, ((AbstractAdminRmiException) t), credentials, contextId, id);
        } else if (t instanceof RemoteException) {
            RemoteException remoteException = (RemoteException) t;
            String exceptionId = AbstractAdminRmiException.generateExceptionId();
            RemoteExceptionUtils.enhanceRemoteException(remoteException, exceptionId);
            logAndReturnException(log, remoteException, exceptionId, credentials, contextId, id);
        } else if (t instanceof Exception) {
            RemoteException remoteException = RemoteExceptionUtils.convertException((Exception) t);
            String exceptionId = AbstractAdminRmiException.generateExceptionId();
            RemoteExceptionUtils.enhanceRemoteException(remoteException, exceptionId);
            logAndReturnException(log, remoteException, exceptionId, credentials, contextId, id);
        }
    }

    private void doAuth(final Credentials creds, final Context ctx) throws InvalidCredentialsException, StorageException {
        BasicAuthenticator basicAuth = BasicAuthenticator.createNonPluginAwareAuthenticator();
        if (cache.isMasterAdmin(creds)) {
            basicAuth.doAuthentication(creds);
        } else {
            contextcheck(ctx);
            basicAuth.doAuthentication(creds, ctx);
        }
    }

    @Override
    public void deleteJob(final Context ctx, final Credentials cred, final int id) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, TaskManagerException {
        try {
            doAuth(cred, ctx);
            if (id < 0) {
                throw new InvalidDataException("Job ID must be > 0.");
            }
            if (cache.isMasterAdmin(cred)) {
                TaskManager.getInstance().deleteJob(id);
            } else {
                contextcheck(ctx);
                TaskManager.getInstance().deleteJob(id, ctx.getId());
            }
        } catch (Throwable e) {
            logAndEnhanceException(e, cred, ctx, id);
            throw e;
        }
    }

    @Override
    public void flush(final Context ctx, final Credentials cred) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, TaskManagerException {
        try {
            doAuth(cred, ctx);
            if (cache.isMasterAdmin(cred)) {
                TaskManager.getInstance().flush();
            } else {
                contextcheck(ctx);
                TaskManager.getInstance().flush(ctx.getId());
            }
        } catch (Throwable e) {
            logAndEnhanceException(e, cred, ctx.getIdAsString(), null);
            throw e;
        }
    }

    @Override
    public String getJobList(final Context ctx, final Credentials cred) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException {
        try {
            doAuth(cred, ctx);
            if (cache.isMasterAdmin(cred)) {
                return TaskManager.getInstance().getJobList();
            }

            contextcheck(ctx);
            return TaskManager.getInstance().getJobList(ctx.getId());
        } catch (Throwable e) {
            logAndEnhanceException(e, cred, ctx.getIdAsString(), null);
            throw e;
        }
    }

    @Override
    public String getJob(final Context ctx, final Credentials cred, final int jobId) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException {
        try {
            doAuth(cred, ctx);
            if (cache.isMasterAdmin(cred)) {
                return TaskManager.getInstance().getJob(null, I(jobId));
            }
            contextcheck(ctx);
            return TaskManager.getInstance().getJob(ctx.getId(), I(jobId));
        } catch (Throwable e) {
            logAndEnhanceException(e, cred, ctx.getIdAsString(), null);
            throw e;
        }
    }

    @Override
    public Object getTaskResults(final Context ctx, final Credentials cred, final int id) throws RemoteException, InvalidCredentialsException, StorageException, InterruptedException, ExecutionException, InvalidDataException {
        try {
            doAuth(cred, ctx);
            if (cache.isMasterAdmin(cred)) {
                return getTaskResults(id, null);
            }

            contextcheck(ctx);
            return getTaskResults(id, ctx.getId());
        } catch (TaskManagerException e) {
            RemoteException remoteException = RemoteExceptionUtils.convertException(e);
            logAndReturnException(log, remoteException, e.getExceptionId(), cred, ctx.getIdAsString(), String.valueOf(id));
            throw remoteException;
        } catch (Throwable e) {
            logAndEnhanceException(e, cred, ctx, id);
            throw e;
        }
    }

    private Object getTaskResults(final int id, final Integer cid) throws InterruptedException, ExecutionException, InvalidDataException, TaskManagerException {
        if (id < 0) {
            throw new InvalidDataException("Task identifier must be a value >= 0");
        }

        ExtendedFutureTask<?> task = TaskManager.getInstance().getTask(id, cid);
        if (null == task) {
            throw new InvalidDataException("No such task for identifier " + id);
        }

        return task.isDone() ? task.get() : null;
    }
}
