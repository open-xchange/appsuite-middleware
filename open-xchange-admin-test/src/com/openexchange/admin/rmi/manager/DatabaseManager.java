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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.admin.rmi.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseManager.class);

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
    public Database registerDatabase(Database database, boolean createSchemata, Integer numberOfSchemata) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        Database db = utilInterface.registerDatabase(database, createSchemata, numberOfSchemata, getMasterCredentials());
        managedObjects.put(db.getId(), db);
        return db;
    }

    /**
     * Unregisters the specified {@link Database}
     * 
     * @param database The {@link Database} to unregister
     * @throws Exception if an error occurs
     */
    public void unregisterDatabase(Database database) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        utilInterface.unregisterDatabase(database, getMasterCredentials());
    }

    /**
     * Changes the specified {@link Database}
     * 
     * @param database The database to change
     * @throws Exception if an error occurs
     */
    public void changeDatabase(Database database) throws Exception {
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
    public Database[] listDatabases(String searchPattern) throws Exception {
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
    public Database[] listAllDatabases() throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        return utilInterface.listAllDatabase(getMasterCredentials());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.manager.AbstractManager#clean(java.lang.Object)
     */
    @Override
    boolean clean(Object object) {
        if (!(object instanceof Database)) {
            LOG.error("The specified object is not of type Database", object.toString());
            return false;
        }
        Database database = (Database) object;
        try {
            unregisterDatabase(database);
            return true;
        } catch (Exception e) {
            LOG.error("The database '{}' could not be unregistered!", database.getId(), e);
            return false;
        }
    }
}
