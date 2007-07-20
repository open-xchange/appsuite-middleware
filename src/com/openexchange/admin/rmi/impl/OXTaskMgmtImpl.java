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

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.rmi.BasicAuthenticator;
import com.openexchange.admin.rmi.OXTaskMgmtInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.exceptions.TaskManagerException;
import com.openexchange.admin.taskmanagement.ExtendedFutureTask;
import com.openexchange.admin.taskmanagement.TaskManager;

public class OXTaskMgmtImpl extends OXCommonImpl implements OXTaskMgmtInterface {
    
    private static final Log log = LogFactory.getLog(OXTaskMgmtImpl.class);
    
    public void deleteJob(final Context ctx, final Credentials cred, final int id) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, TaskManagerException {
        try {
            doNullCheck(ctx);
            contextcheck(ctx);
            new BasicAuthenticator().doAuthentication(cred, ctx);
            if (id < 0) {
                throw new InvalidDataException("Job ID must be > 0.");
            }
            TaskManager.getInstance().deleteJob(id);
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final TaskManagerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void flush(final Context ctx, final Credentials cred) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, TaskManagerException {
        try {
            doNullCheck(ctx);
            contextcheck(ctx);
            new BasicAuthenticator().doAuthentication(cred, ctx);
            TaskManager.getInstance().flush();
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final TaskManagerException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    public String getJobList(final Context ctx, final Credentials cred) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException {
        try {
            doNullCheck(ctx);
            contextcheck(ctx);
            new BasicAuthenticator().doAuthentication(cred, ctx);
            return TaskManager.getInstance().getJobList();
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
    
    public Object getTaskResults(final Context ctx, final Credentials cred, final int id) throws RemoteException, InvalidCredentialsException, StorageException, InterruptedException, ExecutionException, InvalidDataException {
        try {
            doNullCheck(ctx);
            contextcheck(ctx);
            new BasicAuthenticator().doAuthentication(cred, ctx);
            return getTaskResults(id);
        } catch (final InvalidCredentialsException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final StorageException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (final InterruptedException e) {
            log.error(e.getMessage(), e);
            throw e;
        } // We needn't catch the ExecutionException because this is logged before
    }

    public Object getTaskResults(final int id) throws InterruptedException, ExecutionException, InvalidDataException {
        if (id < 0) {
            throw new InvalidDataException("Task must be a value >= 0");
        }
        final ExtendedFutureTask<?> task = TaskManager.getInstance().getTask(id);
        if (null != task) {
            if (task.isDone()) {
                return task.get();
            } else {
                return null;
            }
        } else {
            throw new InvalidDataException("No such Task ID");
        }
    }

    public boolean isTaskDone(final int id) throws InvalidDataException {
        if (id < 0) {
            throw new InvalidDataException("Task must be a value >= 0");
        }
        final ExtendedFutureTask<?> task = TaskManager.getInstance().getTask(id);
        if (null != task) {
            return task.isDone();
        } else {
            throw new InvalidDataException("No such Task ID");
        }
    }
}
