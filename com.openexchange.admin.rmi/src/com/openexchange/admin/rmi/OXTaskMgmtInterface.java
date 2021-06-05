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
package com.openexchange.admin.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.exceptions.TaskManagerException;

/**
 * This interface defines the methods of the task management which are accessibly through RMI.
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public interface OXTaskMgmtInterface extends Remote {

    /**
     * RMI name to be used in the naming lookup.
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
     * Gets a pretty printed list of jobs
     *
     * @param ctx The optional context
     * @param cred The credentials
     * @return A pretty printed list of jobs
     * @throws RemoteException
     * @throws InvalidDataException
     * @throws StorageException
     * @throws InvalidCredentialsException
     */
    public String getJobList(final Context ctx, final Credentials cred) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException;

    /**
     * Gets the pretty printed job
     *
     * @param ctx The optional context
     * @param cred The credentials
     * @param jobId The job id
     * @return A pretty printed job
     * @throws RemoteException
     * @throws InvalidDataException
     * @throws StorageException
     * @throws InvalidCredentialsException
     */
    public String getJob(final Context ctx, final Credentials cred, int jobId) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException;

    /**
     * This method is used to delete finished jobs (jobs != running) from the list
     *
     * @param ctx
     * @param auth
     * @param i
     * @throws RemoteException
     * @throws InvalidDataException
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws TaskManagerException
     */
    public void deleteJob(final Context ctx, final Credentials auth, final int i) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, TaskManagerException;

    /**
     * Flushes all jobs from the queue which are finished ( != running)
     *
     * @param ctx
     * @param auth
     * @throws RemoteException
     * @throws InvalidDataException
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws TaskManagerException
     */
    public void flush(final Context ctx, final Credentials auth) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, TaskManagerException;
}
