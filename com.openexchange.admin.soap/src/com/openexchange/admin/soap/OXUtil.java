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

import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

public class OXUtil implements OXUtilInterface {
    
    private static final Log log = LogFactory.getLog(OXUtil.class);    
    
    private final OXUtilInterface oxUtl;
    
    private final String RMI_HOSTNAME = "rmi://localhost:1099/";
    
    public OXUtil() throws MalformedURLException, RemoteException, NotBoundException {
        super();
        oxUtl = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);
    }

	public void changeDatabase(Database db, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		
		oxUtl.changeDatabase(db, auth);
	}

	public void changeFilestore(Filestore fstore, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		
		oxUtl.changeFilestore(fstore, auth);
	}

	public MaintenanceReason createMaintenanceReason(MaintenanceReason reason,
			Credentials auth) throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		
		return oxUtl.createMaintenanceReason(reason, auth);
	}

	public void deleteMaintenanceReason(MaintenanceReason[] reasons,
			Credentials auth) throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		 oxUtl.deleteMaintenanceReason(reasons, auth);
		
	}

	public Database[] listAllDatabase(Credentials auth) throws RemoteException,
			StorageException, InvalidCredentialsException, InvalidDataException {
		
		return oxUtl.listAllDatabase(auth);
	}

	public Filestore[] listAllFilestore(Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		
		return oxUtl.listAllFilestore(auth);
	}

	public MaintenanceReason[] listAllMaintenanceReason(Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		
		return oxUtl.listAllMaintenanceReason(auth);
	}

	public Server[] listAllServer(Credentials auth) throws RemoteException,
			StorageException, InvalidCredentialsException, InvalidDataException {
		
		return oxUtl.listAllServer(auth);
	}

	public Database[] listDatabase(String search_pattern, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		
		return oxUtl.listDatabase(search_pattern, auth);
	}

	public Filestore[] listFilestore(String search_pattern, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		
		return oxUtl.listFilestore(search_pattern, auth);
	}

	public MaintenanceReason[] listMaintenanceReason(String search_pattern,
			Credentials auth) throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		
		return oxUtl.listMaintenanceReason(search_pattern, auth);
	}

	public Server[] listServer(String search_pattern, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		
		return oxUtl.listServer(search_pattern, auth);
	}

	public Database registerDatabase(Database db, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		
		return oxUtl.registerDatabase(db, auth);
	}

	public Filestore registerFilestore(Filestore fstore, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		
		return oxUtl.registerFilestore(fstore, auth);
	}

	public Server registerServer(Server srv, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		
		return oxUtl.registerServer(srv, auth);
	}

	public void unregisterDatabase(Database dbhandle, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		
		oxUtl.unregisterDatabase(dbhandle, auth);
	}

	public void unregisterFilestore(Filestore store, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		
		oxUtl.unregisterFilestore(store, auth);
	}

	public void unregisterServer(Server serv, Credentials auth)
			throws RemoteException, StorageException,
			InvalidCredentialsException, InvalidDataException {
		oxUtl.unregisterServer(serv, auth);
		
	}

    
}
