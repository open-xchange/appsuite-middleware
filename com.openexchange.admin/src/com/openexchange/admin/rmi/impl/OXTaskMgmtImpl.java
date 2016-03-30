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
package com.openexchange.admin.rmi.impl;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;


import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.rmi.OXTaskMgmtInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.exceptions.TaskManagerException;
import com.openexchange.admin.taskmanagement.ExtendedFutureTask;
import com.openexchange.admin.taskmanagement.TaskManager;
import com.openexchange.admin.tools.AdminCache;

public class OXTaskMgmtImpl extends OXCommonImpl implements OXTaskMgmtInterface {

    private final AdminCache cache;

    public OXTaskMgmtImpl() throws StorageException {
        super();
        this.cache = ClientAdminThread.cache;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OXTaskMgmtImpl.class);

    private void doAuth(final Credentials creds, final Context ctx) throws InvalidCredentialsException, StorageException, InvalidDataException {
        if( cache.isMasterAdmin(creds) ) {
            new BasicAuthenticator().doAuthentication(creds);
        } else {
            contextcheck(ctx);
            new BasicAuthenticator().doAuthentication(creds, ctx);
        }
    }

    @Override
    public void deleteJob(final Context ctx, final Credentials cred, final int id) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, TaskManagerException {
        try {
            doAuth(cred, ctx);
            if (id < 0) {
                throw new InvalidDataException("Job ID must be > 0.");
            }
            if( cache.isMasterAdmin(cred) ) {
                TaskManager.getInstance().deleteJob(id);
            } else {
                contextcheck(ctx);
                TaskManager.getInstance().deleteJob(id, ctx.getId());
            }
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error("", e);
            throw e;
        } catch (final TaskManagerException e) {
            log.error("", e);
            throw e;
        }
    }

    @Override
    public void flush(final Context ctx, final Credentials cred) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, TaskManagerException {
        try {
            doAuth(cred, ctx);
            if( cache.isMasterAdmin(cred) ) {
                TaskManager.getInstance().flush();
            } else {
                contextcheck(ctx);
                TaskManager.getInstance().flush(ctx.getId());
            }
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error("", e);
            throw e;
        } catch (final TaskManagerException e) {
            log.error("", e);
            throw e;
        }
    }

    @Override
    public String getJobList(final Context ctx, final Credentials cred) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException {
        try {
            doAuth(cred, ctx);
            if( cache.isMasterAdmin(cred) ) {
                return TaskManager.getInstance().getJobList();
            } else {
                contextcheck(ctx);
                return TaskManager.getInstance().getJobList(ctx.getId());
            }
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error("", e);
            throw e;
        }
    }

    @Override
    public Object getTaskResults(final Context ctx, final Credentials cred, final int id) throws RemoteException, InvalidCredentialsException, StorageException, InterruptedException, ExecutionException, InvalidDataException {
        try {
            doAuth(cred, ctx);
            if( cache.isMasterAdmin(cred) ) {
                return getTaskResults(id, null);
            } else {
                contextcheck(ctx);
                return getTaskResults(id, ctx.getId());
            }
        } catch (final InvalidCredentialsException e) {
            log.error("", e);
            throw e;
        } catch (final StorageException e) {
            log.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            log.error("", e);
            throw e;
        } catch (final InterruptedException e) {
            log.error("", e);
            throw e;
        } catch (TaskManagerException e) {
            log.error("", e);
            throw new InvalidDataException(e.getMessage());
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
