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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
     * @return
     * @throws RemoteException
     * @throws InvalidDataException
     * @throws StorageException
     * @throws InvalidCredentialsException
     */
    public String getJobList(final Context ctx, final Credentials cred) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException;

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
