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

import static com.openexchange.java.Autoboxing.B;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;

/**
 * {@link DatabaseManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class DatabaseManager extends AbstractManager {

    private static DatabaseManager INSTANCE;

    /**
     * Gets the instance of the {@link DatabaseManager}
     *
     * @param host
     * @param masterCredentials
     * @return
     */
    public static DatabaseManager getInstance(String host, Credentials masterCredentials) {
        if (INSTANCE == null) {
            INSTANCE = new DatabaseManager(host, masterCredentials);
        }
        return INSTANCE;
    }

    /**
     * Initialises a new {@link DatabaseManager}.
     *
     * @param rmiEndPointURL
     * @param masterCredentials
     */
    private DatabaseManager(String rmiEndPointURL, Credentials masterCredentials) {
        super(rmiEndPointURL, masterCredentials);
    }

    /**
     * Registers the specified Database
     *
     * @param database The {@link Database} to register
     * @return The registered Database
     * @throws Exception if an error occurs during registration
     */
    public Database register(Database database, boolean createSchemata, Integer numberOfSchemata) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        Database db = utilInterface.registerDatabase(database, B(createSchemata), numberOfSchemata, getMasterCredentials());
        managedObjects.put(db.getId(), db);
        return db;
    }

    /**
     * Unregisters the specified {@link Database}
     *
     * @param database The {@link Database} to unregister
     * @throws Exception if an error occurs
     */
    public void unregister(Database database) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        utilInterface.unregisterDatabase(database, getMasterCredentials());
    }

    /**
     * Changes the specified {@link Database}
     *
     * @param database The database to change
     * @throws Exception if an error occurs
     */
    public void change(Database database) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        utilInterface.changeDatabase(database, getMasterCredentials());
    }

    /**
     * Searches for databases with the specified search pattern
     *
     * @param searchPattern The search pattern
     * @return An array with all found {@link Database}s
     * @throws Exception if an error is occurred
     */
    public Database[] search(String searchPattern) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        return utilInterface.listDatabase(searchPattern, getMasterCredentials());
    }

    /**
     * Lists all databases
     *
     * @param searchPattern The search pattern
     * @return An array with all found {@link Database}s
     * @throws Exception if an error is occurred
     */
    public Database[] listAll() throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        return utilInterface.listAllDatabase(getMasterCredentials());
    }

    @Override
    void clean(Object object) throws Exception {
        unregister((Database) object);
    }
}
