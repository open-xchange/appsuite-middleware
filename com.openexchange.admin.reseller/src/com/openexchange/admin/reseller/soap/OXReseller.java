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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.admin.reseller.soap;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.reseller.soap.dataobjects.ResellerContext;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.DuplicateExtensionException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.soap.OXSOAPRMIMapper;


/**
 * SOAP Service implementing RMI Interface OXResellerInterface
 * 
 * @author choeger
 *
 */
public class OXReseller extends OXSOAPRMIMapper {

    /**
     * @throws RemoteException
     */
    public OXReseller() throws RemoteException {
        super(OXResellerInterface.class);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#change(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void change(ResellerAdmin adm, Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException {
        reconnect();
        try {
            ((OXResellerInterface)rmistub).change(adm, creds);
            return;
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#create(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public ResellerAdmin create(ResellerAdmin adm, Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException, OXResellerException {
        reconnect();
        try {
            return ((OXResellerInterface)rmistub).create(adm, creds);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#delete(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void delete(ResellerAdmin adm, Credentials creds) throws RemoteException, StorageException, OXResellerException, InvalidCredentialsException {
        reconnect();
        try {
            ((OXResellerInterface)rmistub).delete(adm, creds);
            return;
        } catch (ConnectException e) {
            reconnect(true);
        } catch (InvalidDataException e) {
            throw new RemoteException(e.toString());
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getAvailableRestrictions(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Restriction[] getAvailableRestrictions(Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException, OXResellerException {
        reconnect();
        try {
            return ((OXResellerInterface)rmistub).getAvailableRestrictions(creds);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getData(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public ResellerAdmin getData(ResellerAdmin adm, Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        reconnect();
        try {
            return ((OXResellerInterface)rmistub).getData(adm, creds);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getMultipleData(com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin[], com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public ResellerAdmin[] getMultipleData(ResellerAdmin[] admins, Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException {
        reconnect();
        try {
            return ((OXResellerInterface)rmistub).getMultipleData(admins, creds);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#getRestrictionsFromContext(com.openexchange.admin.rmi.dataobjects.Context, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public Restriction[] getRestrictionsFromContext(ResellerContext ctx, Credentials creds) throws RemoteException, InvalidDataException, OXResellerException, StorageException, InvalidCredentialsException, DuplicateExtensionException {
        reconnect();
        try {
            return ((OXResellerInterface)rmistub).getRestrictionsFromContext(ResellerContextUtil.resellerContext2Context(ctx), creds);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#initDatabaseRestrictions(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void initDatabaseRestrictions(Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException {
        reconnect();
        try {
            ((OXResellerInterface)rmistub).initDatabaseRestrictions(creds);
            return;
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#list(java.lang.String, com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public ResellerAdmin[] list(String search_pattern, Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException {
        reconnect();
        try {
            return ((OXResellerInterface)rmistub).list(search_pattern, creds);
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#removeDatabaseRestrictions(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void removeDatabaseRestrictions(Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException, OXResellerException {
        reconnect();
        try {
            ((OXResellerInterface)rmistub).removeDatabaseRestrictions(creds);
            return;
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.reseller.rmi.OXResellerInterface#updateDatabaseModuleAccessRestrictions(com.openexchange.admin.rmi.dataobjects.Credentials)
     */
    public void updateDatabaseModuleAccessRestrictions(Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException {
        reconnect();
        try {
            ((OXResellerInterface)rmistub).updateDatabaseModuleAccessRestrictions(creds);
            return;
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

    public void updateDatabaseRestrictions(Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException {
        reconnect();
        try {
            ((OXResellerInterface)rmistub).updateDatabaseRestrictions(creds);
            return;
        } catch (ConnectException e) {
            reconnect(true);
        }
        throw new RemoteException(RMI_CONNECT_ERROR);
    }

}
