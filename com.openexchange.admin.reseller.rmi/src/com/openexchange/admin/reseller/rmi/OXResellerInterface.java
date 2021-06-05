/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.admin.reseller.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.reseller.rmi.exceptions.OXResellerException;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;

/**
 * {@link OXResellerInterface}
 * 
 * @author choeger
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface OXResellerInterface extends Remote {

    /**
     * RMI name to be used in the naming lookup.
     */
    public static final String RMI_NAME = "OXReseller";

    /**
     * Creates a ResellerAdmin subadmin account in the database.
     * Example:
     * <pre>
     * final ResellerAdmin ra = new ResellerAdmin("reselleradm");
     * ra.setDisplayname("Reseller Admin");
     * ra.setPassword("secret");
     * HashSet<Restriction> rss = new HashSet<Restriction>();
     * rss.add(new Restriction(Restriction.MAX_CONTEXT_PER_SUBADMIN, "100"));
     * rss.add(new Restriction(Restriction.MAX_OVERALL_USER_PER_SUBADMIN,"1000"));
     * </pre>
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
    ResellerAdmin create(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException, OXResellerException;

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
    void delete(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException;

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
    void change(final ResellerAdmin adm, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException;

    /**
     * Allows a reseller admin to change his own capabilities, properties and taxonomies
     *
     * @param admin The {@link ResellerAdmin} object
     * @param credentials {@link Credentials} of the reseller admin
     * @throws RemoteException
     * @throws InvalidDataException
     * @throws StorageException
     * @throws OXResellerException
     * @throws InvalidCredentialsException
     */
    void changeSelf(ResellerAdmin admin, Credentials credentials) throws RemoteException, InvalidDataException, StorageException, OXResellerException, InvalidCredentialsException;

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
    ResellerAdmin[] list(final String search_pattern, final Credentials creds) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException;

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
    ResellerAdmin[] getMultipleData(final ResellerAdmin[] admins, Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException;

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
    ResellerAdmin getData(final ResellerAdmin adm, Credentials creds) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException;

    /**
     * Returns data for the specified reseller admin
     *
     * @param admin The reseller admin to return data for
     * @param credentials The reseller admin's credentials
     * @return The reseller admin with the data
     * @throws RemoteException
     * @throws InvalidDataException
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws OXResellerException
     */
    ResellerAdmin getSelfData(ResellerAdmin admin, Credentials credentials) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, OXResellerException;

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
    Restriction[] getAvailableRestrictions(final Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException, OXResellerException;

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
    Restriction[] getRestrictionsFromContext(final Context ctx, final Credentials creds) throws RemoteException, InvalidDataException, OXResellerException, StorageException, InvalidCredentialsException;

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
    void initDatabaseRestrictions(final Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException;

    /**
     * Remove all restrictions from database
     *
     * @param creds {@link Credentials} of the master admin
     * @throws RemoteException
     * @throws InvalidCredentialsException
     * @throws StorageException
     * @throws OXResellerException
     */
    void removeDatabaseRestrictions(final Credentials creds) throws RemoteException, InvalidCredentialsException, StorageException, OXResellerException;

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
    void updateDatabaseModuleAccessRestrictions(final Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException;

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
    void updateDatabaseRestrictions(final Credentials creds) throws RemoteException, StorageException, InvalidCredentialsException, OXResellerException;

    /**
     * Retrieves all capabilities for the reseller with the specified identifier
     * 
     * @param admin the reseller admin
     * @param credentials {@link Credentials} of the reseller admin
     * @return The capabilities
     * @throws RemoteException
     * @throws StorageException
     * @throws InvalidCredentialsException
     * @throws OXResellerException
     * @throws InvalidDataException
     */
    Set<String> getCapabilities(ResellerAdmin admin, Credentials credentials) throws RemoteException, StorageException, InvalidDataException, InvalidCredentialsException, OXResellerException;
}
