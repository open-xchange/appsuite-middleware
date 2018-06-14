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

import java.net.URI;
import java.rmi.Naming;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.ContextFactory;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.UserTest;
import com.openexchange.admin.rmi.UtilTest;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.Server;

/**
 * {@link ContextManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ContextManager {

    private static final Logger LOG = LoggerFactory.getLogger(ContextManager.class);

    /** Default max quota for a {@link Context}, 5GB */
    private static final long DEFAULT_MAX_QUOTA = 5000;

    private String host;
    private final Map<Integer, Context> registeredContexts;
    private final Credentials masterCredentials;

    /**
     * Initialises a new {@link ContextManager}.
     */
    public ContextManager(String host, Credentials masterCredentials) {
        super();
        this.masterCredentials = masterCredentials;
        registeredContexts = new HashMap<>();
        this.host = host;
    }

    /**
     * Deletes all managed {@link Context}s
     */
    public void cleanUp() {
        Map<Integer, Context> failed = new HashMap<>();
        for (Entry<Integer, Context> entry : registeredContexts.entrySet()) {
            try {
                deleteContext(entry.getValue());
            } catch (Exception e) {
                LOG.error("Context '{}' could not be deleted!", entry.getValue().getId());
                failed.put(entry.getKey(), entry.getValue());
            }
        }
        registeredContexts.clear();

        if (failed.isEmpty()) {
            return;
        }
        LOG.warn("The following contexts were not deleted: '{}'. Manual intervention might be required.", failed.toString());
    }

    /**
     * Creates a new {@link Context} with default max quota
     * 
     * @param contextAdminCredentials The context admin credentials
     * @return The newly created {@link Context}
     * @throws Exception
     */
    public Context createContext(Credentials contextAdminCredentials) throws Exception {
        return createContext(contextAdminCredentials, DEFAULT_MAX_QUOTA);
    }

    /**
     * Creates a new {@link Context}
     * 
     * @param contextAdminCredentials The context admin credentials
     * @return The newly created {@link Context}
     * @throws Exception if the context cannot be created or any other error is occurred
     */
    public Context createContext(Credentials contextAdminCredentials, long maxQuota) throws Exception {
        return createContext(getNextFreeContextId(), maxQuota, contextAdminCredentials);
    }

    /**
     * Creates a new {@link Context}.
     * 
     * @param contextId The context identifier
     * @param contextAdminCredentials The context admin credentials
     * @return The created context
     * @throws Exception if the context cannot be created or any other error is occurred
     */
    public Context createContext(int contextId, long maxQuota, Credentials contextAdminCredentials) throws Exception {
        return createContext(ContextFactory.createContext(contextId, maxQuota), contextAdminCredentials);
    }

    /**
     * Creates a new {@link Context}.
     * 
     * @param context The {@link Context} to create
     * @param contextAdminCredentials The context admin credentials
     * @return The created context
     * @throws Exception if the context cannot be created or any other error is occurred
     */
    public Context createContext(Context context, Credentials contextAdminCredentials) throws Exception {
        prerequisites();
        OXContextInterface contextInterface = getContextInterface();
        contextInterface.create(context, UserTest.getTestUserObject(contextAdminCredentials.getLogin(), contextAdminCredentials.getPassword(), context), masterCredentials);
        registeredContexts.put(context.getId(), context);
        return context;
    }

    /**
     * Loads the data for the specified {@link Context}
     * 
     * @param context The context for which the data should be loaded
     * @return The {@link Context} with the loaded data
     * @throws Exception if an error is occurred
     */
    public Context getData(Context context) throws Exception {
        OXContextInterface contextInterface = getContextInterface();
        return contextInterface.getData(context, masterCredentials);
    }

    /**
     * Searches the database for {@link Context}s with the specified pattern
     * 
     * @param pattern The pattern to search for
     * @return An array with the results
     * @throws Exception if an error is occurred
     */
    public Context[] searchContext(String pattern) throws Exception {
        OXContextInterface xres = getContextInterface();
        return xres.list(pattern, masterCredentials);
    }

    /**
     * Searches the database for {@link Context}s by database
     * 
     * @param database The {@link Database}
     * @return An array with the results
     * @throws Exception if an error is occurred
     */
    public Context[] searchContext(Database database) throws Exception {
        OXContextInterface xres = getContextInterface();
        return xres.listByDatabase(database, masterCredentials);
    }

    /**
     * Searches the database for {@link Context}s by {@link Filestore}
     * 
     * @param database The {@link Filestore}
     * @return An array with the results
     * @throws Exception if an error is occurred
     */
    public Context[] searchContext(Filestore filestore) throws Exception {
        OXContextInterface xres = getContextInterface();
        return xres.listByFilestore(filestore, masterCredentials);
    }

    /**
     * Delete the specified {@link Context}
     * 
     * @param ctx The {@link Context} to delete
     * @param masterCredentials The master {@link Credentials}
     * @throws Exception if the context cannot be deleted or any other error occurs
     */
    public void deleteContext(Context ctx) throws Exception {
        OXContextInterface contextInterface = (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
        contextInterface.delete(ctx, masterCredentials);
        registeredContexts.remove(ctx.getId());
    }

    /**
     * Enables the specified {@link Context}
     * 
     * @param context The {@link Context} to enable
     * @throws Exception if an error is occurred
     */
    public void enableContext(Context context) throws Exception {
        OXContextInterface contextInterface = getContextInterface();
        contextInterface.enable(context, masterCredentials);
    }

    /**
     * Disables the specified {@link Context}
     * 
     * @param context The {@link Context} to disable
     * @throws Exception if an error is occurred
     */
    public void disableContext(Context context) throws Exception {
        OXContextInterface contextInterface = getContextInterface();
        contextInterface.disable(context, masterCredentials);
    }

    /**
     * Checks whether the specified {@link Context} exists
     * 
     * @param context The {@link Context} to check for existence
     * @return <code>true</code> if the context exists; <code>false</code> otherwise
     * @throws Exception if an error is occurred
     */
    public boolean exists(Context context) throws Exception {
        OXContextInterface contextInterface = getContextInterface();
        return contextInterface.exists(context, masterCredentials);
    }

    /**
     * Retrieves the context admin identifier for the specified {@link Context}
     * 
     * @param context The {@link Context} for which to retrieve the admin identifier
     * @return The context admin identifier
     * @throws Exception if an error is occurred
     */
    public int getAdminId(Context context) throws Exception {
        OXContextInterface contextInterface = getContextInterface();
        return contextInterface.getAdminId(context, masterCredentials);
    }

    ////////////////////////////// HELPERS ///////////////////////////////

    /**
     * Searches and returns the next available free context identifier
     * 
     * @return The context identifier
     * @throws Exception if an error occurs
     */
    private int getNextFreeContextId() throws Exception {
        int pos = 5;
        int ret = -1;
        while (ret == -1) {
            ret = searchContext(String.valueOf(pos)).length == 0 ? pos : -1;
            pos = pos + 3;
        }
        return ret;
    }

    /**
     * Checks for the prerequisites when creating a context
     */
    private void prerequisites() throws Exception {
        // TODO: perform checks during initialisation instead on performing them on every create operation
        registerServer();
        registerFilestore();
        registerDatabase();
    }

    /**
     * Registers a server if none exists
     * 
     * @throws Exception if an error is occurred during registration
     */
    private void registerServer() throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        if (utilInterface.listServer("local", masterCredentials).length == 1) {
            return;
        }
        Server srv = new Server();
        srv.setName("local");
        utilInterface.registerServer(srv, masterCredentials);
    }

    /**
     * Registers a database if none exists
     * 
     * @throws Exception if an error is occurred during registration
     */
    private void registerDatabase() throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        if (utilInterface.listDatabase("test-ox-db", masterCredentials).length != 0) {
            return;
        }
        Database database = UtilTest.getTestDatabaseObject("localhost", "test-ox-db");
        utilInterface.registerDatabase(database, Boolean.FALSE, Integer.valueOf(0), masterCredentials);
    }

    /**
     * Registers a filestore if none exists
     * 
     * @throws Exception if an error is occurred during registration
     */
    private void registerFilestore() throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        if (utilInterface.listFilestore("*", masterCredentials).length != 0) {
            return;
        }
        Filestore filestore = new Filestore();
        filestore.setMaxContexts(10000);
        filestore.setSize(8796093022208L);

        URI uri = new URI("file:/tmp/disc_" + System.currentTimeMillis());
        filestore.setUrl(uri.toString());

        new java.io.File(uri.getPath()).mkdir();
        utilInterface.registerFilestore(filestore, masterCredentials);
    }

    //////////////////////////// RMI LOOK-UPS //////////////////////////////

    /**
     * Returns the {@link OXContextInterface}
     * 
     * @return The {@link OXContextInterface}
     * @throws Exception if an error is occurred during RMI look-up
     */
    private OXContextInterface getContextInterface() throws Exception {
        return (OXContextInterface) getRemoteInterface(OXContextInterface.RMI_NAME, OXContextInterface.class);
    }

    /**
     * Returns the {@link OXUtilInterface}
     * 
     * @return The {@link OXUtilInterface}
     * @throws Exception if an error is occurred during RMI look-up
     */
    private OXUtilInterface getUtilInterface() throws Exception {
        return (OXUtilInterface) getRemoteInterface(OXUtilInterface.RMI_NAME, OXUtilInterface.class);
    }

    /**
     * Returns the {@link Remote} interface with the specified rmi name
     * 
     * @param rmiName The rmi name of the {@link Remote} interface
     * @return The {@link Remote} interface
     * @throws Exception if an error is occurred during RMI look-up
     */
    private <T extends Remote> T getRemoteInterface(String rmiName, Class<T> clazz) throws Exception {
        return clazz.cast(Naming.lookup(host + rmiName));
    }
}
