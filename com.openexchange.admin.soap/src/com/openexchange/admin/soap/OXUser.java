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

import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class OXUser implements OXUserInterface {
    
    private static final Log log = LogFactory.getLog(OXUser.class);    
    
    private final OXUserInterface oxUsr;
    
    private final String RMI_HOSTNAME = "rmi://localhost:1099/";
    
    public OXUser() throws MalformedURLException, RemoteException, NotBoundException {
        super();
        oxUsr = (OXUserInterface) Naming.lookup(RMI_HOSTNAME + OXUserInterface.RMI_NAME);
    }

	public void change(Context ctx, User usrdata, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, DatabaseUpdateException, NoSuchUserException {
		
		oxUsr.change(ctx, usrdata, auth);
		
	}

	public void changeModuleAccess(Context ctx, User user,
			UserModuleAccess moduleAccess, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, DatabaseUpdateException, NoSuchUserException {
		
		oxUsr.changeModuleAccess(ctx, user, moduleAccess, auth);
	}

	public void changeModuleAccess(Context ctx, User user,
			String access_combination_name, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, DatabaseUpdateException, NoSuchUserException {
		
		oxUsr.changeModuleAccess(ctx, user, access_combination_name, auth);
		
	}

	public User create(Context ctx, User usrdata, UserModuleAccess access,
			Credentials auth) throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, DatabaseUpdateException {
		
		return oxUsr.create(ctx, usrdata, access, auth);
	}

	public User create(Context ctx, User usrdata,
			String access_combination_name, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, DatabaseUpdateException {
		
		return oxUsr.create(ctx, usrdata, access_combination_name, auth);
	}

	public User create(Context ctx, User usrdata, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, DatabaseUpdateException {
		
		return oxUsr.create(ctx, usrdata, auth);
	}

	public void delete(Context ctx, User[] users, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, DatabaseUpdateException, NoSuchUserException {
		
		oxUsr.delete(ctx, users, auth);
	}

	public void delete(Context ctx, User user, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, DatabaseUpdateException, NoSuchUserException {
		
		oxUsr.delete(ctx, user, auth);
	}

	public String getAccessCombinationName(Context ctx, User user,
			Credentials auth) throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, DatabaseUpdateException, NoSuchUserException {
		
		return oxUsr.getAccessCombinationName(ctx, user, auth);
	}

	public User[] getData(Context ctx, User[] users, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, NoSuchUserException, DatabaseUpdateException {
		
		return oxUsr.getData(ctx, users, auth);
	}

	public User getData(Context ctx, User user, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, NoSuchUserException, DatabaseUpdateException {
		
		return oxUsr.getData(ctx, user, auth);
	}

	public UserModuleAccess getModuleAccess(Context ctx, User user,
			Credentials auth) throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, DatabaseUpdateException, NoSuchUserException {
		
		return oxUsr.getModuleAccess(ctx, user, auth);
	}

	public User[] list(Context ctx, String search_pattern, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, DatabaseUpdateException {
		
		return oxUsr.list(ctx, search_pattern, auth);
	}

	public User[] listAll(Context ctx, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, DatabaseUpdateException {
		
		return oxUsr.listAll(ctx, auth);
	}

	

    
}
