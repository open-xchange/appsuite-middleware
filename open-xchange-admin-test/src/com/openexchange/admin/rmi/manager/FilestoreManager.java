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
import com.openexchange.admin.rmi.dataobjects.Filestore;

/**
 * {@link FilestoreManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class FilestoreManager extends AbstractManager {

    private static FilestoreManager INSTANCE;

    /**
     * Gets the instance of the {@link FilestoreManager}
     * 
     * @param host
     * @param masterCredentials
     * @return
     */
    public static FilestoreManager getInstance(String host, Credentials masterCredentials) {
        if (INSTANCE == null) {
            INSTANCE = new FilestoreManager(host, masterCredentials);
        }
        return INSTANCE;
    }

    /**
     * Initialises a new {@link FilestoreManager}.
     * 
     * @param rmiEndPointURL
     * @param masterCredentials
     */
    private FilestoreManager(String rmiEndPointURL, Credentials masterCredentials) {
        super(rmiEndPointURL, masterCredentials);
    }

    /**
     * Registers the specified filestore
     * 
     * @param filestore The {@link Filestore} to register
     * @return The registered filestore
     * @throws Exception if an error occurs during registration
     */
    public Filestore register(Filestore filestore) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        Filestore fs = utilInterface.registerFilestore(filestore, getMasterCredentials());
        managedObjects.put(filestore.getId(), fs);
        return fs;
    }

    /**
     * Unregisters the specified {@link Filestore}
     * 
     * @param filestore The {@link Filestore} to unregister
     * @throws Exception if an error occurs
     */
    public void unregister(Filestore filestore) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        utilInterface.unregisterFilestore(filestore, getMasterCredentials());
    }

    /**
     * Searches for filestores with the specified search pattern
     * 
     * @param searchPattern The search pattern
     * @return An array with all found {@link Filestore}s
     * @throws Exception if an error is occurred
     */
    public Filestore[] search(String searchPattern) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        return utilInterface.listFilestore(searchPattern, getMasterCredentials());
    }

    /**
     * Searches for filestores with the specified search pattern
     * 
     * @param searchPattern The search pattern
     * @return An array with all found {@link Filestore}s
     * @throws Exception if an error is occurred
     */
    public Filestore[] search(String searchPattern, boolean omitUsage) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        return utilInterface.listFilestore(searchPattern, getMasterCredentials(), omitUsage);
    }

    @Override
    void clean(Object object) throws Exception {
        unregister((Filestore) object);
    }
}
