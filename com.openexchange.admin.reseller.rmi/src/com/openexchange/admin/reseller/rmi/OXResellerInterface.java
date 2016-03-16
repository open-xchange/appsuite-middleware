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
 */
public interface OXResellerInterface extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "OXReseller";

    /**
     * Creates a ResellerAdmin subadmin account in the database.
     *  Example:
     *  final ResellerAdmin ra = new ResellerAdmin("reselleradm");
     *  ra.setDisplayname("Reseller Admin");
     *  ra.setPassword("secret");
     *  HashSet<Restriction> rss = new HashSet<Restriction>();
     *  rss.add(new Restriction(Restriction.MAX_CONTEXT_PER_SUBADMIN, "100"));
     *  rss.add(new Restriction(Restriction.MAX_OVERALL_USER_PER_SUBADMIN,"1000"));
     *
     * @param adm {@link ResellerAdmin} object
     * @param creds {@link Credentials} of the master admin
     * @return {@link ResellerAdmin} object
     * @throws RemoteException
     * @throws InvalidDataException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws OXResellerException
     */
    public ResellerAdmin create(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException, OXResellerException;

    /**
     * Delete ResellerAdmin from database If any objects still belong to this subadmin, the deletion will fail
     *
     * @param adm {@link ResellerAdmin} object
     * @param creds {@link Credentials} of the master admin
     * @throws RemoteException
     * @throws InvalidDataException
     * @throws StorageException
     * @throws OXResellerException
     * @throws InvalidCredentialsException
     */
    public void delete(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException;

    /**
     * Change ResellerAdmin parameters
     *
     * @param adm {@link ResellerAdmin} object
     * @param creds {@link Credentials} of the master admin
     * @throws RemoteException
     * @throws InvalidDataException
     * @throws StorageException
     * @throws OXResellerException
     * @throws InvalidCredentialsException
     */
    public void change(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException;

    /**
     * Search for specific or all subadmins in the database
     *
     * @param search_pattern search pattern like e.g. "*foo*"
     * @param creds {@link Credentials} of the master admin
     * @return {@link ResellerAdmin} array containing search result
     * @throws RemoteException
     * @throws InvalidDataException
     * @throws StorageException
     * @throws InvalidCredentialsException
     */
    public ResellerAdmin[] list(final String search_pattern, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException;

    /**
     * Get complete data from all subadmin objects contained in array. It is required to
     * either specify subadmin objects name or id.
     *
     * @param Array containing {@link ResellerAdmin} objects
     * @param creds {@link Credentials} of the master admin
     * @return {@link ResellerAdmin} array containing complete data
     * @throws RemoteException
     * @throws InvalidDataException
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws OXResellerException
     */
    public ResellerAdmin[] getMultipleData(final ResellerAdmin[] admins, Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException;

    /**
     * Get complete data from subadmin object. It is required to either specify subadmin objects name or id.
     *
     * @param {@link ResellerAdmin} object
     * @param creds {@link Credentials} of the master admin
     * @return {@link ResellerAdmin} containing complete data
     * @throws RemoteException
     * @throws OXResellerException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws InvalidDataException
     */
    public ResellerAdmin getData(final ResellerAdmin adm, Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException;

    /**
     * Retrieve a list of all currently available {@link Restriction} objects
     *
     * @param creds {@link Credentials} of the master admin
     * @return {@link HashSet} of available restrictions
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws OXResellerException
     */
    public Restriction[] getAvailableRestrictions(final Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException, OXResellerException;

    /**
     * Retrieve a list of all restrictions applied to given {@link Context}
     *
     * @param {@link Context} object
     * @param creds {@link Credentials} of the master admin
     * @return {@link HashSet} of restrictions applied to context
     * @throws RemoteException
     * @throws InvalidDataException
     * @throws OXResellerException
     * @throws StorageException
     * @throws InvalidCredentialsException
     */
    public Restriction[] getRestrictionsFromContext(final Context ctx, final Credentials creds) throws RemoteException, InvalidDataException, OXResellerException, StorageException, InvalidCredentialsException;

    /**
     * Initialize the database with all currently possible restrictions.
     * There's a set of static restrictions (see {@link Restriction}) as
     * well as every such restriction per defined module access combination from
     * /opt/open-xchange/etc/admindaemon/ModuleAccessDefinitions.properties
     *
     * @param creds {@link Credentials} of the master admin
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws OXResellerException
     */
    public void initDatabaseRestrictions(final Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException;

    /**
     * Remove all restrictions from database
     *
     * @param creds {@link Credentials} of the master admin
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws OXResellerException
     */
    public void removeDatabaseRestrictions(final Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException, OXResellerException;

    /**
     * Update all restrictions based on module access combinations in case of changes to
     * /opt/open-xchange/etc/admindaemon/ModuleAccessDefinitions.properties
     *
     * @param creds {@link Credentials} of the master admin
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws OXResellerException
     */
    public void updateDatabaseModuleAccessRestrictions(final Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException;

    /**
     * Update list of restrictions. This is going to add new restrictions that might ship with
     * a newer version of the reseller plugin.
     *
     * @param creds {@link Credentials} of the master admin
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws OXResellerException
     */
    public void updateDatabaseRestrictions(final Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException;
}
