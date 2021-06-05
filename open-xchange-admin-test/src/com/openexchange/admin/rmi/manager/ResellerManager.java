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

package com.openexchange.admin.rmi.manager;

import com.openexchange.admin.reseller.rmi.OXResellerInterface;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * {@link ResellerManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ResellerManager extends AbstractManager {

    private static ResellerManager INSTANCE;

    /**
     * Gets the instance of the {@link ResellerManager}
     * 
     * @param host
     * @param masterCredentials
     * @return
     */
    public static ResellerManager getInstance(String host, Credentials masterCredentials) {
        if (INSTANCE == null) {
            INSTANCE = new ResellerManager(host, masterCredentials);
        }
        return INSTANCE;
    }

    /**
     * Initialises a new {@link ResellerManager}.
     * 
     * @param rmiEndPointURL
     * @param masterCredentials
     */
    private ResellerManager(String rmiEndPointURL, Credentials masterCredentials) {
        super(rmiEndPointURL, masterCredentials);
    }

    /**
     * Creates the specified {@link ResellerAdmin}
     * 
     * @param resellerAdmin The {@link ResellerAdmin} to create
     * @return The created {@link ResellerAdmin}
     * @throws Exception if an error is occurred
     */
    public ResellerAdmin create(ResellerAdmin resellerAdmin) throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        ResellerAdmin admin = resellerInterface.create(resellerAdmin, getMasterCredentials());
        managedObjects.put(admin.getId(), admin);
        return admin;
    }

    /**
     * Creates the specified {@link ResellerAdmin} under the specified parent {@link ResellerAdmin}
     * 
     * @param parent The parent {@link ResellerAdmin}
     * @param resellerAdmin The {@link ResellerAdmin} to create
     * @return The created {@link ResellerAdmin}
     * @throws Exception if an error is occurred
     */
    public ResellerAdmin create(ResellerAdmin parent, ResellerAdmin resellerAdmin) throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        ResellerAdmin admin = resellerInterface.create(resellerAdmin, new Credentials(parent.getName(), parent.getPassword()));
        managedObjects.put(admin.getId(), admin);
        return admin;
    }

    /**
     * Changes/Updates the specified {@link ResellerAdmin}
     * 
     * @param resellerAdmin The {@link ResellerAdmin} to change
     * @throws Exception if an error is occurred
     */
    public void change(ResellerAdmin resellerAdmin) throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        resellerInterface.change(resellerAdmin, getMasterCredentials());
    }

    /**
     * Fetches all data for the specified {@link ResellerAdmin}
     * 
     * @param resellerAdmin The {@link ResellerAdmin} to fetch the data
     * @return The data of the {@link ResellerAdmin}
     * @throws Exception if an error is occurred
     */
    public ResellerAdmin getData(ResellerAdmin resellerAdmin) throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        return resellerInterface.getData(resellerAdmin, getMasterCredentials());
    }

    /**
     * Retrieve a list of all restrictions applied to given {@link Context}
     * 
     * @param context The {@link Context} for which to retrieve the restrictions
     * @return An array with all restrictions applied to the specified {@link Context}
     * @throws Exception if an error is occurred
     */
    public Restriction[] getContextRestrictions(Context context) throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        return resellerInterface.getRestrictionsFromContext(context, getMasterCredentials());
    }

    /**
     * Deletes the specified {@link ResellerAdmin}
     * 
     * @param resellerAdmin The {@link ResellerAdmin} to delete
     * @throws Exception if an error is occurred
     */
    public void delete(ResellerAdmin resellerAdmin) throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        resellerInterface.delete(resellerAdmin, getMasterCredentials());
    }

    /**
     * Returns an array with all found {@link ResellerAdmin}s that match the
     * specified search pattern
     * 
     * @param searchPattern The search pattern
     * @return an array with all found {@link ResellerAdmin}s that match the
     *         specified search pattern
     * @throws Exception if an error is occurred
     */
    public ResellerAdmin[] search(String searchPattern) throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        return resellerInterface.list(searchPattern, getMasterCredentials());
    }

    /**
     * Update all restrictions based on module access combinations in case of changes to
     * <code>/opt/open-xchange/etc/admindaemon/ModuleAccessDefinitions.properties</code>
     * 
     * @throws Exception if an error is occurred
     */
    public void updateDatabaseModuleAccessRestrictions() throws Exception {
        OXResellerInterface resellerInterface = getResellerInterface();
        resellerInterface.updateDatabaseModuleAccessRestrictions(getMasterCredentials());
    }

    @Override
    void clean(Object object) throws Exception {
        delete((ResellerAdmin) object);
    }

    //////////////////////////// RMI LOOK-UPS //////////////////////////////

    /**
     * Returns the {@link OXResellerInterface}
     * 
     * @return The {@link OXResellerInterface}
     * @throws Exception if an error is occurred during RMI look-up
     */
    private OXResellerInterface getResellerInterface() throws Exception {
        return getRemoteInterface(OXResellerInterface.RMI_NAME, OXResellerInterface.class);
    }
}
