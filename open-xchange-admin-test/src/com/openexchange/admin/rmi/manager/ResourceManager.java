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

import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Resource;

/**
 * {@link ResourceManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ResourceManager extends AbstractManager {

    private static ResourceManager INSTANCE;

    /**
     * Gets the instance of the {@link ResourceManager}
     * 
     * @param host
     * @param masterCredentials
     * @return
     */
    public static ResourceManager getInstance(String host, Credentials masterCredentials) {
        if (INSTANCE == null) {
            INSTANCE = new ResourceManager(host, masterCredentials);
        }
        return INSTANCE;
    }

    /**
     * Initialises a new {@link ResourceManager}.
     * 
     * @param rmiEndPointURL
     * @param masterCredentials
     */
    private ResourceManager(String rmiEndPointURL, Credentials masterCredentials) {
        super(rmiEndPointURL, masterCredentials);
    }

    /**
     * Creates the specified {@link Resource} in the specified {@link Context}
     * 
     * @param resource The {@link Resource} to create
     * @param context The {@link Context}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @return The created {@link Resource}
     * @throws Exception if an error is occurred
     */
    public Resource create(Resource resource, Context context, Credentials contextAdminCredentials) throws Exception {
        OXResourceInterface resourceInterface = getResourceInterface();
        return resourceInterface.create(context, resource, contextAdminCredentials);
    }

    /**
     * Retrieves all data of the specified {@link Resource} in the specified {@link Context}
     * 
     * @param resource The {@link Resource}
     * @param context The {@link Context}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @return The {@link Resource} with all its data loaded
     * @throws Exception if an error is occurred
     */
    public Resource getData(Resource resource, Context context, Credentials contextAdminCredentials) throws Exception {
        OXResourceInterface resourceInterface = getResourceInterface();
        return resourceInterface.getData(context, resource, contextAdminCredentials);
    }

    /**
     * Retrieves an array with all found {@link Resource} in the specified {@link Context}
     * that match the specified search pattern.
     * 
     * @param context The {@link Context}
     * @param searchPattern The search pattern
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @return An array with all found {@link Resource}s
     * @throws Exception if an error is occurred
     */
    public Resource[] search(Context context, String searchPattern, Credentials contextAdminCredentials) throws Exception {
        OXResourceInterface resourceInterface = getResourceInterface();
        return resourceInterface.list(context, searchPattern, contextAdminCredentials);
    }

    /**
     * Retrieves an array with all {@link Resource}s in the specified {@link Context}
     * 
     * @param context The {@link Context}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @return An array with all {@link Resource}s
     * @throws Exception if an error is occurred
     */
    public Resource[] listAll(Context context, Credentials contextAdminCredentials) throws Exception {
        OXResourceInterface resourceInterface = getResourceInterface();
        return resourceInterface.listAll(context, contextAdminCredentials);
    }

    /**
     * Changes the specified {@link Resource} in the specified {@link Context}
     * 
     * @param resource The {@link Resource} to change
     * @param context The {@link Context}
     * @param contextAdminCredentials The context admin {@link Credentials}
     * @throws Exception if an error is occurred
     */
    public void change(Resource resource, Context context, Credentials contextAdminCredentials) throws Exception {
        OXResourceInterface resourceInterface = getResourceInterface();
        resourceInterface.change(context, resource, contextAdminCredentials);
    }

    /**
     * Deletes the specified {@link Resource} from the specified {@link Context}
     * 
     * @param resource The {@link Resource} to delete
     * @param context The {@link Context}
     * @param contextAdminCredentials The context's admin {@link Credentials}
     * @throws Exception if an error is occurred
     */
    public void delete(Resource resource, Context context, Credentials contextAdminCredentials) throws Exception {
        OXResourceInterface resourceInterface = getResourceInterface();
        resourceInterface.delete(context, resource, contextAdminCredentials);
    }

    @Override
    void clean(Object object) {
        // Nothing to do, the resource will be implicitly deleted when the context is deleted.
    }

    /**
     * Retrieves the remote {@link OXResourceInterface}
     * 
     * @return the remote {@link OXResourceInterface}
     * @throws Exception if the remote interface cannot be retrieved
     */
    private OXResourceInterface getResourceInterface() throws Exception {
        return getRemoteInterface(OXResourceInterface.RMI_NAME, OXResourceInterface.class);
    }
}
