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
package com.openexchange.admin.soap;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchDatabaseException;
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchReasonException;
import com.openexchange.admin.rmi.exceptions.OXContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class OXContext implements OXContextInterface {
    
    private static final Log log = LogFactory.getLog(OXContext.class);    
    
    private final OXContextInterface oxCtx;
    
    private final String RMI_HOSTNAME = "rmi://localhost:1099/";
    
    public OXContext() throws MalformedURLException, RemoteException, NotBoundException {
        super();
        oxCtx = (OXContextInterface) Naming.lookup(RMI_HOSTNAME + OXContextInterface.RMI_NAME);
    }

	public void change(Context ctx, Credentials auth) throws RemoteException,
			InvalidCredentialsException, NoSuchContextException,
			StorageException, InvalidDataException {
		
		oxCtx.change(ctx, auth);
	}

	public void changeModuleAccess(Context ctx, UserModuleAccess access,
			Credentials auth) throws RemoteException,
			InvalidCredentialsException, NoSuchContextException,
			StorageException, InvalidDataException {
		oxCtx.changeModuleAccess(ctx, access, auth);
	}

	public void changeModuleAccess(Context ctx, String access_combination_name,
			Credentials auth) throws RemoteException,
			InvalidCredentialsException, NoSuchContextException,
			StorageException, InvalidDataException {
		oxCtx.changeModuleAccess(ctx, access_combination_name, auth);
	}

	public Context create(Context ctx, User admin_user, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException,
			ContextExistsException {
		return oxCtx.create(ctx, admin_user, auth);
	}

	public Context create(Context ctx, User admin_user,
			String access_combination_name, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException,
			ContextExistsException {
		return oxCtx.create(ctx, admin_user, access_combination_name, auth);
	}

	public Context create(Context ctx, User admin_user,
			UserModuleAccess access, Credentials auth) throws RemoteException,
			StorageException, InvalidCredentialsException,
			InvalidDataException, ContextExistsException {
		return oxCtx.create(ctx, admin_user, access, auth);
	}

	public void delete(Context ctx, Credentials auth) throws RemoteException,
			InvalidCredentialsException, NoSuchContextException,
			StorageException, DatabaseUpdateException, InvalidDataException {
		oxCtx.delete(ctx, auth);
	}

	public void disable(Context ctx, Credentials auth) throws RemoteException,
			InvalidCredentialsException, NoSuchContextException,
			StorageException, InvalidDataException, NoSuchReasonException,
			OXContextException {
		oxCtx.disable(ctx, auth);
	}

	public void disableAll(Credentials auth) throws RemoteException,
			StorageException, InvalidCredentialsException,
			InvalidDataException, NoSuchReasonException {
		oxCtx.disableAll(auth);
	}

	public void downgrade(Context ctx, Credentials auth)
			throws RemoteException, InvalidCredentialsException,
			NoSuchContextException, StorageException, DatabaseUpdateException,
			InvalidDataException {
		oxCtx.downgrade(ctx, auth);
	}

	public void enable(Context ctx, Credentials auth) throws RemoteException,
			InvalidCredentialsException, NoSuchContextException,
			StorageException, InvalidDataException {
		oxCtx.enable(ctx, auth);
	}

	public void enableAll(Credentials auth) throws RemoteException,
			StorageException, InvalidCredentialsException {
		oxCtx.enableAll(auth);
	}

	public String getAccessCombinationName(Context ctx, Credentials auth)
			throws RemoteException, InvalidCredentialsException,
			NoSuchContextException, StorageException, InvalidDataException {
		return oxCtx.getAccessCombinationName(ctx, auth);
	}

	public Context getData(Context ctx, Credentials auth)
			throws RemoteException, InvalidCredentialsException,
			NoSuchContextException, StorageException, InvalidDataException {
		return oxCtx.getData(ctx, auth);
	}

	public UserModuleAccess getModuleAccess(Context ctx, Credentials auth)
			throws RemoteException, InvalidCredentialsException,
			NoSuchContextException, StorageException, InvalidDataException {
		return oxCtx.getModuleAccess(ctx, auth);
	}

	public Context[] list(String search_pattern, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		return oxCtx.list(search_pattern, auth);
	}

	public Context[] listAll(String search_pattern, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		return oxCtx.listAll(search_pattern, auth);
	}

	public Context[] listAll(Credentials auth) throws RemoteException,
			StorageException, InvalidCredentialsException, InvalidDataException {
		return oxCtx.listAll(auth);
	}

	public Context[] listByDatabase(Database db, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException,
			NoSuchDatabaseException {
		return oxCtx.listByDatabase(db, auth);
	}

	public Context[] listByFilestore(Filestore fs, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException,
			NoSuchFilestoreException {
		return oxCtx.listByFilestore(fs, auth);
	}

	public int moveContextDatabase(Context ctx, Database dst_database_id,
			Credentials auth) throws RemoteException,
			InvalidCredentialsException, NoSuchContextException,
			StorageException, InvalidDataException, DatabaseUpdateException,
			OXContextException {
		return oxCtx.moveContextDatabase(ctx, dst_database_id, auth);
	}

	public int moveContextFilestore(Context ctx, Filestore dst_filestore_id,
			Credentials auth) throws RemoteException,
			InvalidCredentialsException, NoSuchContextException,
			StorageException, InvalidDataException, NoSuchFilestoreException,
			NoSuchReasonException, OXContextException {
		return oxCtx.moveContextFilestore(ctx, dst_filestore_id, auth);
	}


    
}
