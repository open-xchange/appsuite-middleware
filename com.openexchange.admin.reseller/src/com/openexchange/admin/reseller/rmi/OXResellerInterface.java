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

package com.openexchange.admin.reseller.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;

import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * @author choeger
 *
 */
public interface OXResellerInterface extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "OXReseller";

    /**
     * @param adm
     * @param creds
     * @return
     * @throws RemoteException
     * @throws InvalidDataException 
     * @throws StorageException 
     * @throws InvalidCredentialsException 
     * @throws OXResellerException 
     */
    public ResellerAdmin create(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException, OXResellerException;

    /**
     * @param adm
     * @param creds
     * @throws RemoteException
     * @throws InvalidDataException 
     * @throws StorageException 
     * @throws OXResellerException 
     * @throws InvalidCredentialsException 
     */
    public void delete(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException;

    /**
     * @param adm
     * @param creds
     * @throws RemoteException
     * @throws InvalidDataException 
     * @throws StorageException 
     * @throws OXResellerException 
     * @throws InvalidCredentialsException 
     */
    public void change(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException;

    /**
     * @param search_pattern
     * @param creds
     * @return
     * @throws RemoteException
     * @throws InvalidDataException 
     * @throws StorageException 
     * @throws InvalidCredentialsException 
     */
    public ResellerAdmin[] list(final String search_pattern, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException;
    
    /**
     * @param admins
     * @param creds
     * @return
     * @throws RemoteException
     * @throws InvalidDataException 
     * @throws InvalidCredentialsException 
     * @throws StorageException 
     * @throws OXResellerException 
     */
    public ResellerAdmin[] getMultipleData(final ResellerAdmin[] admins, Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException;

    /**
     * @param adm
     * @param creds
     * @return
     * @throws RemoteException
     * @throws OXResellerException 
     * @throws StorageException 
     * @throws InvalidCredentialsException 
     * @throws InvalidDataException 
     */
    public ResellerAdmin getData(final ResellerAdmin adm, Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException;
    
    /**
     * @param creds
     * @return
     * @throws RemoteException
     * @throws InvalidCredentialsException 
     * @throws StorageException 
     * @throws OXResellerException 
     */
    public HashSet<Restriction> getAvailableRestrictions(final Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException, OXResellerException;

    /**
     * @param ctx
     * @param creds
     * @return
     * @throws RemoteException
     * @throws InvalidDataException 
     * @throws OXResellerException 
     * @throws StorageException 
     * @throws InvalidCredentialsException 
     */
    public HashSet<Restriction> getRestrictionsFromContext(final Context ctx, final Credentials creds) throws RemoteException, InvalidDataException, OXResellerException, StorageException, InvalidCredentialsException;
    
    /**
     * @param creds
     * @throws StorageException
     * @throws InvalidCredentialsException 
     * @throws OXResellerException 
     */
    public void initDatabaseRestrictions(final Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException;

    /**
     * @param creds
     * @throws RemoteException
     * @throws InvalidCredentialsException 
     * @throws StorageException 
     * @throws OXResellerException 
     */
    public void removeDatabaseRestrictions(final Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException, OXResellerException;

    /**
     * @param creds
     * @throws RemoteException
     * @throws StorageException 
     * @throws InvalidCredentialsException 
     * @throws OXResellerException 
     */
    public void updateDatabaseModuleAccessRestrictions(final Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException;
}
