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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
package com.openexchange.admin.soap;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import com.openexchange.admin.rmi.OXTaskMgmtInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.exceptions.TaskManagerException;
import com.openexchange.admin.soap.dataobjects.Context;


/**
 * SOAP Service implementing RMI Interface OXTaskMgmtInterface
 *
 * @author choeger
 *
 */
public class OXTaskMgmt extends OXSOAPRMIMapper {

    public OXTaskMgmt() throws RemoteException {
        super(OXTaskMgmtInterface.class);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXTaskMgmtInterface#deleteJob(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials, int)
     */
    public void deleteJob(Context ctx, Credentials auth, int i) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, TaskManagerException {
        reconnect();
        try {
            ((OXTaskMgmtInterface)rmistub).deleteJob(SOAPUtils.soapContext2Context(ctx), auth, i);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXTaskMgmtInterface)rmistub).deleteJob(SOAPUtils.soapContext2Context(ctx), auth, i);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXTaskMgmtInterface#flush(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void flush(Context ctx, Credentials auth) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, TaskManagerException {
        reconnect();
        try {
            ((OXTaskMgmtInterface)rmistub).flush(SOAPUtils.soapContext2Context(ctx), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXTaskMgmtInterface)rmistub).flush(SOAPUtils.soapContext2Context(ctx), auth);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXTaskMgmtInterface#getJobList(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public String getJobList(Context ctx, Credentials cred) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException {
        reconnect();
        try {
            return ((OXTaskMgmtInterface)rmistub).getJobList(SOAPUtils.soapContext2Context(ctx), cred);
        } catch (ConnectException e) {
            reconnect(true);
            return ((OXTaskMgmtInterface)rmistub).getJobList(SOAPUtils.soapContext2Context(ctx), cred);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXTaskMgmtInterface#getTaskResults(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials, int)
     */
    public Object getTaskResults(Context ctx, Credentials cred, int id) throws RemoteException, InvalidCredentialsException, StorageException, InterruptedException, ExecutionException, InvalidDataException {
        reconnect();
        try {
            return ((OXTaskMgmtInterface)rmistub).getTaskResults(SOAPUtils.soapContext2Context(ctx), cred, id);
        } catch (ConnectException e) {
            reconnect(true);
            return ((OXTaskMgmtInterface)rmistub).getTaskResults(SOAPUtils.soapContext2Context(ctx), cred, id);
        }
    }

}
