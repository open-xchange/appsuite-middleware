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

import java.rmi.Naming;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;

/**
 * {@link AbstractManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
abstract class AbstractManager {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractManager.class);

    private final String rmiEndPointURL;
    private final Credentials masterCredentials;

    final Map<Integer, Object> managedObjects;

    /**
     * Initialises a new {@link AbstractManager}.
     * 
     * @param rmiEndPointURL the RMI endpoint url
     * @param masterCredentials The master {@link Credentials}
     */
    public AbstractManager(String rmiEndPointURL, Credentials masterCredentials) {
        super();
        this.masterCredentials = masterCredentials;
        this.rmiEndPointURL = rmiEndPointURL;
        managedObjects = new HashMap<>();
    }

    /**
     * Cleans up all managed objects
     */
    public void cleanUp() {
        Map<Integer, Object> failed = new HashMap<>();
        for (Entry<Integer, Object> entry : managedObjects.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (!delete(entry.getValue(), entry.getValue().getClass())) {
                failed.put(entry.getKey(), entry.getValue());
            }
        }
        managedObjects.clear();

        if (failed.isEmpty()) {
            return;
        }
        LOG.warn("The following '{}' objects were not removed: '{}'. Manual intervention might be required.", failed.keySet().toString());
    }

    /**
     * Generic method for deleting an object from the managed map
     * 
     * @param object The object to delete
     * @param clazz The type of the object
     * @return <code>true</code> if clean was successful; <code>false</code> otherwise
     */
    <T> boolean delete(Object object, Class<T> clazz) {
        if (object == null) {
            return true;
        }
        if (!(object.getClass().isAssignableFrom(clazz))) {
            LOG.error("The specified object is not of type {}", object.toString(), clazz.getSimpleName());
            return false;
        }
        try {
            clean(object);
            return true;
        } catch (Exception e) {
            LOG.error("The {} '{}' could not be deleted", clazz.getSimpleName(), object.toString());
            return false;
        }
    }

    /**
     * Part of the clean-up procedure. Cleans the specified object.
     * 
     * @param object The object to clean
     * @return <code>true</code> if clean was successful; <code>false</code> otherwise
     */
    abstract void clean(Object object) throws Exception;

    /**
     * Gets the masterCredentials
     *
     * @return The masterCredentials
     */
    public Credentials getMasterCredentials() {
        return masterCredentials;
    }

    /**
     * Returns the {@link Remote} interface with the specified rmi name
     * 
     * @param rmiName The rmi name of the {@link Remote} interface
     * @return The {@link Remote} interface
     * @throws Exception if an error is occurred during RMI look-up
     */
    <T extends Remote> T getRemoteInterface(String rmiName, Class<T> clazz) throws Exception {
        return clazz.cast(Naming.lookup(rmiEndPointURL + rmiName));
    }

    /**
     * Returns the {@link OXUtilInterface}
     * 
     * @return The {@link OXUtilInterface}
     * @throws Exception if an error is occurred during RMI look-up
     */
    OXUtilInterface getUtilInterface() throws Exception {
        return getRemoteInterface(OXUtilInterface.RMI_NAME, OXUtilInterface.class);
    }
}
