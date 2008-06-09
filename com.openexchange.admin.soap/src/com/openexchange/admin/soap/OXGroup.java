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

import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchGroupException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class OXGroup implements OXGroupInterface {
    
    private static final Log log = LogFactory.getLog(OXGroup.class);    
    
    private final OXGroupInterface oxGrp;
    
    private final String RMI_HOSTNAME = "rmi://localhost:1099/";
    
    public OXGroup() throws MalformedURLException, RemoteException, NotBoundException {
        super();
        oxGrp = (OXGroupInterface) Naming.lookup(RMI_HOSTNAME + OXGroupInterface.RMI_NAME);
    }

	public void addMember(Context ctx, Group grp, User[] members,
			Credentials auth) throws RemoteException,
			InvalidCredentialsException, NoSuchContextException,
			StorageException, InvalidDataException, DatabaseUpdateException,
			NoSuchUserException, NoSuchGroupException {
		
		oxGrp.addMember(ctx, grp, members, auth);
		
	}

	public void change(Context ctx, Group grp, Credentials auth)
			throws RemoteException, InvalidCredentialsException,
			NoSuchContextException, NoSuchUserException, StorageException,
			InvalidDataException, DatabaseUpdateException, NoSuchGroupException {
		
		oxGrp.change(ctx, grp, auth);
	}

	public Group create(Context ctx, Group grp, Credentials auth)
			throws RemoteException, InvalidCredentialsException,
			NoSuchContextException, NoSuchUserException, StorageException,
			InvalidDataException, DatabaseUpdateException {
		
		return oxGrp.create(ctx, grp, auth);
	}

	public void delete(Context ctx, Group grp, Credentials auth)
			throws RemoteException, InvalidCredentialsException,
			NoSuchContextException, StorageException, InvalidDataException,
			DatabaseUpdateException, NoSuchGroupException {
		
		oxGrp.delete(ctx, grp, auth);
	}

	public void delete(Context ctx, Group[] grps, Credentials auth)
			throws RemoteException, InvalidCredentialsException,
			NoSuchContextException, StorageException, InvalidDataException,
			DatabaseUpdateException, NoSuchGroupException {
		
		oxGrp.delete(ctx, grps, auth);
	}

	public Group getData(Context ctx, Group grp, Credentials auth)
			throws RemoteException, InvalidCredentialsException,
			NoSuchContextException, StorageException, InvalidDataException,
			DatabaseUpdateException, NoSuchGroupException {
		
		return oxGrp.getData(ctx, grp, auth);
	}

	public Group[] getData(Context ctx, Group[] grps, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, NoSuchGroupException, DatabaseUpdateException {
		
		return oxGrp.getData(ctx, grps, auth);
	}

	public Group getDefaultGroup(Context ctx, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, NoSuchContextException,
			InvalidDataException, DatabaseUpdateException {
		
		return oxGrp.getDefaultGroup(ctx, auth);
	}

	public User[] getMembers(Context ctx, Group grp, Credentials auth)
			throws RemoteException, InvalidCredentialsException,
			NoSuchContextException, StorageException, InvalidDataException,
			DatabaseUpdateException, NoSuchGroupException {
		
		return oxGrp.getMembers(ctx, grp, auth);
	}

	public Group[] list(Context ctx, String pattern, Credentials auth)
			throws RemoteException, InvalidCredentialsException,
			NoSuchContextException, StorageException, InvalidDataException,
			DatabaseUpdateException {
		
		return oxGrp.list(ctx, pattern, auth);
	}

	public Group[] listAll(Context ctx, Credentials auth)
			throws RemoteException, InvalidCredentialsException,
			NoSuchContextException, StorageException, InvalidDataException,
			DatabaseUpdateException {
		
		return oxGrp.listAll(ctx, auth);
	}

	public Group[] listGroupsForUser(Context ctx, User usr, Credentials auth)
			throws RemoteException, InvalidCredentialsException,
			NoSuchContextException, StorageException, InvalidDataException,
			DatabaseUpdateException, NoSuchUserException {
		
		return oxGrp.listGroupsForUser(ctx, usr, auth);
	}

	public void removeMember(Context ctx, Group grp, User[] members,
			Credentials auth) throws RemoteException,
			InvalidCredentialsException, NoSuchContextException,
			StorageException, InvalidDataException, DatabaseUpdateException,
			NoSuchGroupException, NoSuchUserException {
		oxGrp.removeMember(ctx, grp, members, auth);
		
	}

    
}
