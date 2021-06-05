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

import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Server;

/**
 * {@link ServerManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ServerManager extends AbstractManager {

    private static ServerManager INSTANCE;

    /**
     * Gets the instance of the {@link ServerManager}
     * 
     * @param host
     * @param masterCredentials
     * @return
     */
    public static ServerManager getInstance(String host, Credentials masterCredentials) {
        if (INSTANCE == null) {
            INSTANCE = new ServerManager(host, masterCredentials);
        }
        return INSTANCE;
    }

    /**
     * Initialises a new {@link ServerManager}.
     * 
     * @param rmiEndPointURL
     * @param masterCredentials
     */
    private ServerManager(String rmiEndPointURL, Credentials masterCredentials) {
        super(rmiEndPointURL, masterCredentials);
    }

    /**
     * Registers the specified server
     * 
     * @param server The {@link Server} to register
     * @return The registered server
     * @throws Exception if an error occurs during registration
     */
    public Server register(Server server) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        Server srv = utilInterface.registerServer(server, getMasterCredentials());
        managedObjects.put(server.getId(), srv);
        return srv;
    }

    /**
     * Unregisters the specified {@link Server}
     * 
     * @param server The {@link Server} to unregister
     * @throws Exception if an error occurs
     */
    public void unregister(Server server) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        utilInterface.unregisterServer(server, getMasterCredentials());
    }

    /**
     * Searches for servers with the specified search pattern
     * 
     * @param searchPattern The search pattern
     * @return An array with all found {@link Server}s
     * @throws Exception if an error is occurred
     */
    public Server[] search(String searchPattern) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        return utilInterface.listServer(searchPattern, getMasterCredentials());
    }

    @Override
    void clean(Object object) throws Exception {
        unregister((Server) object);
    }
}
