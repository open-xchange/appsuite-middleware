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

import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.soap.dataobjects.Database;
import com.openexchange.admin.soap.dataobjects.Filestore;
import com.openexchange.admin.soap.dataobjects.Server;

/**
 * SOAP Service implementing RMI Interface OXUtilInterface
 *
 * @author choeger
 *
 */
public class OXUtil extends OXSOAPRMIMapper {

    public OXUtil() throws RemoteException {
        super(OXUtilInterface.class);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#changeDatabase(com.openexchange.admin.rmi.dataobjects.Database, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void changeDatabase(Database db, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            ((OXUtilInterface)rmistub).changeDatabase(SOAPUtils.soapDatabase2Database(db), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUtilInterface)rmistub).changeDatabase(SOAPUtils.soapDatabase2Database(db), auth);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#changeFilestore(com.openexchange.admin.rmi.dataobjects.Filestore, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void changeFilestore(Filestore fstore, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            ((OXUtilInterface)rmistub).changeFilestore(SOAPUtils.soapFilestore2Filestore(fstore), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUtilInterface)rmistub).changeFilestore(SOAPUtils.soapFilestore2Filestore(fstore), auth);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#createMaintenanceReason(com.openexchange.admin.rmi.dataobjects.MaintenanceReason, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public MaintenanceReason createMaintenanceReason(MaintenanceReason reason, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return ((OXUtilInterface)rmistub).createMaintenanceReason(reason, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return ((OXUtilInterface)rmistub).createMaintenanceReason(reason, auth);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#deleteMaintenanceReason(com.openexchange.admin.rmi.dataobjects.MaintenanceReason[], com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void deleteMaintenanceReason(MaintenanceReason[] reasons, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            ((OXUtilInterface)rmistub).deleteMaintenanceReason(reasons, auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUtilInterface)rmistub).deleteMaintenanceReason(reasons, auth);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listAllDatabase(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Database[] listAllDatabase(Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return SOAPUtils.databases2SoapDatabases(((OXUtilInterface)rmistub).listAllDatabase(auth));
        } catch (ConnectException e) {
            reconnect(true);
            return SOAPUtils.databases2SoapDatabases(((OXUtilInterface)rmistub).listAllDatabase(auth));
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listAllFilestore(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Filestore[] listAllFilestore(Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return SOAPUtils.filestores2SoapFilestores(((OXUtilInterface)rmistub).listAllFilestore(auth));
        } catch (ConnectException e) {
            reconnect(true);
            return SOAPUtils.filestores2SoapFilestores(((OXUtilInterface)rmistub).listAllFilestore(auth));
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listAllMaintenanceReason(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public MaintenanceReason[] listAllMaintenanceReason(Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return ((OXUtilInterface)rmistub).listAllMaintenanceReason(auth);
        } catch (ConnectException e) {
            reconnect(true);
            return ((OXUtilInterface)rmistub).listAllMaintenanceReason(auth);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listAllServer(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Server[] listAllServer(Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return SOAPUtils.servers2SoapServers(((OXUtilInterface)rmistub).listAllServer(auth));
        } catch (ConnectException e) {
            reconnect(true);
            return SOAPUtils.servers2SoapServers(((OXUtilInterface)rmistub).listAllServer(auth));
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listDatabase(java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Database[] listDatabase(String search_pattern, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return SOAPUtils.databases2SoapDatabases(((OXUtilInterface)rmistub).listDatabase(search_pattern, auth));
        } catch (ConnectException e) {
            reconnect(true);
            return SOAPUtils.databases2SoapDatabases(((OXUtilInterface)rmistub).listDatabase(search_pattern, auth));
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listFilestore(java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Filestore[] listFilestore(String search_pattern, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return SOAPUtils.filestores2SoapFilestores(((OXUtilInterface)rmistub).listFilestore(search_pattern, auth));
        } catch (ConnectException e) {
            reconnect(true);
            return SOAPUtils.filestores2SoapFilestores(((OXUtilInterface)rmistub).listFilestore(search_pattern, auth));
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listMaintenanceReason(java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public MaintenanceReason[] listMaintenanceReason(String search_pattern, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return ((OXUtilInterface)rmistub).listMaintenanceReason(search_pattern, auth);
        } catch (ConnectException e) {
            reconnect(true);
            return ((OXUtilInterface)rmistub).listMaintenanceReason(search_pattern, auth);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#listServer(java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Server[] listServer(String search_pattern, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return SOAPUtils.servers2SoapServers(((OXUtilInterface)rmistub).listServer(search_pattern, auth));
        } catch (ConnectException e) {
            reconnect(true);
            return SOAPUtils.servers2SoapServers(((OXUtilInterface)rmistub).listServer(search_pattern, auth));
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#registerDatabase(com.openexchange.admin.rmi.dataobjects.Database, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Database registerDatabase(Database db, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return new Database(((OXUtilInterface)rmistub).registerDatabase(SOAPUtils.soapDatabase2Database(db), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return new Database(((OXUtilInterface)rmistub).registerDatabase(SOAPUtils.soapDatabase2Database(db), auth));
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#registerFilestore(com.openexchange.admin.rmi.dataobjects.Filestore, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Filestore registerFilestore(Filestore fstore, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return new Filestore(((OXUtilInterface)rmistub).registerFilestore(SOAPUtils.soapFilestore2Filestore(fstore), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return new Filestore(((OXUtilInterface)rmistub).registerFilestore(SOAPUtils.soapFilestore2Filestore(fstore), auth));
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#registerServer(com.openexchange.admin.rmi.dataobjects.Server, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Server registerServer(Server srv, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            return new Server(((OXUtilInterface)rmistub).registerServer(SOAPUtils.soapServer2Server(srv), auth));
        } catch (ConnectException e) {
            reconnect(true);
            return new Server(((OXUtilInterface)rmistub).registerServer(SOAPUtils.soapServer2Server(srv), auth));
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#unregisterDatabase(com.openexchange.admin.rmi.dataobjects.Database, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void unregisterDatabase(Database dbhandle, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            ((OXUtilInterface)rmistub).unregisterDatabase(SOAPUtils.soapDatabase2Database(dbhandle), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUtilInterface)rmistub).unregisterDatabase(SOAPUtils.soapDatabase2Database(dbhandle), auth);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#unregisterFilestore(com.openexchange.admin.rmi.dataobjects.Filestore, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void unregisterFilestore(Filestore store, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            ((OXUtilInterface)rmistub).unregisterFilestore(SOAPUtils.soapFilestore2Filestore(store), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUtilInterface)rmistub).unregisterFilestore(SOAPUtils.soapFilestore2Filestore(store), auth);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.OXUtilInterface#unregisterServer(com.openexchange.admin.rmi.dataobjects.Server, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void unregisterServer(Server serv, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException {
        reconnect();
        try {
            ((OXUtilInterface)rmistub).unregisterServer(SOAPUtils.soapServer2Server(serv), auth);
        } catch (ConnectException e) {
            reconnect(true);
            ((OXUtilInterface)rmistub).unregisterServer(SOAPUtils.soapServer2Server(serv), auth);
        }
    }

}
