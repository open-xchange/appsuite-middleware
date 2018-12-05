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

package com.openexchange.admin.rmi.manager;

import java.net.URI;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.UtilTest;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.factory.ContextFactory;
import com.openexchange.admin.rmi.factory.UserFactory;

/**
 * {@link ContextManager}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class ContextManager extends AbstractManager {

    private static ContextManager INSTANCE;

    /**
     * Gets the instance of the {@link ContextManager}
     * 
     * @param host
     * @param masterCredentials
     * @return
     */
    public static ContextManager getInstance(String host, Credentials masterCredentials) {
        if (INSTANCE == null) {
            INSTANCE = new ContextManager(host, masterCredentials);
        }
        return INSTANCE;
    }

    /** Default max quota for a {@link Context}, 5GB */
    private static final long DEFAULT_MAX_QUOTA = 5000;

    private final ServerManager serverManager;
    private final DatabaseManager databaseManager;
    private final FilestoreManager filestoreManager;

    /**
     * Initialises a new {@link ContextManager}.
     */
    private ContextManager(String host, Credentials masterCredentials) {
        super(host, masterCredentials);
        serverManager = ServerManager.getInstance(host, masterCredentials);
        databaseManager = DatabaseManager.getInstance(host, masterCredentials);
        filestoreManager = FilestoreManager.getInstance(host, masterCredentials);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.manager.AbstractManager#clean(java.lang.Object)
     */
    @Override
    void clean(Object object) throws Exception {
        delete((Context) object);
    }

    /**
     * Creates a new {@link Context} with default max quota
     * 
     * @param contextAdminCredentials The context admin credentials
     * @return The newly created {@link Context}
     * @throws Exception
     */
    public Context create(Credentials contextAdminCredentials) throws Exception {
        return create(contextAdminCredentials, DEFAULT_MAX_QUOTA);
    }

    /**
     * Creates a new {@link Context}
     * 
     * @param contextAdminCredentials The context admin credentials
     * @return The newly created {@link Context}
     * @throws Exception if the context cannot be created or any other error is occurred
     */
    public Context create(Credentials contextAdminCredentials, long maxQuota) throws Exception {
        return create(getNextFreeContextId(), maxQuota, contextAdminCredentials);
    }

    /**
     * Creates a new {@link Context}.
     * 
     * @param contextId The context identifier
     * @param contextAdminCredentials The context admin credentials
     * @return The created context
     * @throws Exception if the context cannot be created or any other error is occurred
     */
    public Context create(int contextId, long maxQuota, Credentials contextAdminCredentials) throws Exception {
        return create(ContextFactory.createContext(contextId, maxQuota), contextAdminCredentials);
    }

    /**
     * Creates a new {@link Context}.
     * 
     * @param context The {@link Context} to create
     * @param contextAdminCredentials The context admin credentials
     * @return The created context
     * @throws Exception if the context cannot be created or any other error is occurred
     */
    public Context create(Context context, Credentials contextAdminCredentials) throws Exception {
        return create(context, UserFactory.createUser(contextAdminCredentials.getLogin(), contextAdminCredentials.getPassword(), "example.org", context));
    }

    /**
     * Creates a new {@link Context}
     * 
     * @param context The {@link Context} to create
     * @param contextAdmin The {@link Context} admin
     * @return The newly created context
     * @throws Exception if an error is occurred
     */
    public Context create(Context context, User contextAdmin) throws Exception {
        return create(context, contextAdmin, getMasterCredentials());
    }

    /**
     * Creates a new {@link Context} with the specified context admin. It uses
     * the specified authCredentials to authenticate against the server
     * 
     * @param context The {@link Context} to create
     * @param contextAdmin The {@link Context} admin
     * @param authCredentials the authentication credentials to authenicate against the server
     * @return The newly created context
     * @throws Exception if an error is occurred
     */
    public Context create(Context context, User contextAdmin, Credentials authCredentials) throws Exception {
        prerequisites();
        if (context.getId() == null || context.getId().intValue() <= 0) {
            context.setId(new Integer(getNextFreeContextId()));
        }
        OXContextInterface contextInterface = getContextInterface();
        Context ctx = contextInterface.create(context, contextAdmin, authCredentials);
        managedObjects.put(ctx.getId(), ctx);
        return ctx;
    }

    /**
     * Creates a new {@link Context}
     * 
     * @param context The {@link Context} to create
     * @param contextAdmin The {@link Context} admin
     * @param combinationName The combination access name
     * @return The newly created context
     * @throws Exception if an error is occurred
     */
    public Context create(Context context, User contextAdmin, String combinationName) throws Exception {
        prerequisites();
        if (context.getId() == null || context.getId().intValue() <= 0) {
            context.setId(new Integer(getNextFreeContextId()));
        }
        OXContextInterface contextInterface = getContextInterface();
        Context ctx = contextInterface.create(context, contextAdmin, combinationName, getMasterCredentials());
        managedObjects.put(ctx.getId(), ctx);
        return ctx;
    }

    /**
     * Moves all data of a context contained in a database to another database
     * 
     * @param context The {@link Context}
     * @param destinationDatabase The destination {@link Database}
     * @return The job identifier
     * @throws Exception if an error is occurred
     */
    public int moveContextDatabase(Context context, Database destinationDatabase) throws Exception {
        OXContextInterface contextInterface = getContextInterface();
        return contextInterface.moveContextDatabase(context, destinationDatabase, getMasterCredentials());
    }

    /**
     * Changes/Updates the specified {@link Context}
     * 
     * @param context The {@link Context} to change
     * @throws Exception if an error is occurred
     */
    public void change(Context context) throws Exception {
        change(context, getMasterCredentials());
    }

    /**
     * Changes/Updates the specified {@link Context}
     * 
     * @param context The {@link Context} to change
     * @param credentials The credentials to authenicate against the server
     * @throws Exception if an error is occurred
     */
    public void change(Context context, Credentials authCredentials) throws Exception {
        OXContextInterface contextInterface = getContextInterface();
        contextInterface.change(context, authCredentials);
    }

    /**
     * Loads the data for the specified {@link Context}
     * 
     * @param context The context for which the data should be loaded
     * @return The {@link Context} with the loaded data
     * @throws Exception if an error is occurred
     */
    public Context getData(Context context) throws Exception {
        return getData(context, getMasterCredentials());
    }

    /**
     * Loads the data for the specified {@link Context}
     * 
     * @param context The context for which the data should be loaded
     * @param authCredentials The credentials to authenticate against the server
     * @return The {@link Context} with the loaded data
     * @throws Exception if an error is occurred
     */
    public Context getData(Context context, Credentials authCredentials) throws Exception {
        OXContextInterface contextInterface = getContextInterface();
        return contextInterface.getData(context, authCredentials);
    }

    /**
     * Searches the database for {@link Context}s with the specified pattern
     * 
     * @param pattern The pattern to search for
     * @return An array with the results
     * @throws Exception if an error is occurred
     */
    public Context[] search(String pattern) throws Exception {
        OXContextInterface xres = getContextInterface();
        return xres.list(pattern, getMasterCredentials());
    }

    /**
     * Searches the database for {@link Context}s by database
     * 
     * @param database The {@link Database}
     * @return An array with the results
     * @throws Exception if an error is occurred
     */
    public Context[] search(Database database) throws Exception {
        OXContextInterface xres = getContextInterface();
        return xres.listByDatabase(database, getMasterCredentials());
    }

    /**
     * Searches the database for {@link Context}s by {@link Filestore}
     * 
     * @param database The {@link Filestore}
     * @return An array with the results
     * @throws Exception if an error is occurred
     */
    public Context[] search(Filestore filestore) throws Exception {
        OXContextInterface xres = getContextInterface();
        return xres.listByFilestore(filestore, getMasterCredentials());
    }

    /**
     * Lists all contexts
     * 
     * @param authCredentials The credentials to authenticate against the server
     * @return An array with all contexts
     * @throws Exception if an error is occurred
     */
    public Context[] listAll(Credentials authCredentials) throws Exception {
        OXContextInterface xres = getContextInterface();
        return xres.listAll(authCredentials);
    }

    /**
     * Delete the specified {@link Context}
     * 
     * @param ctx The {@link Context} to delete
     * @throws Exception if the context cannot be deleted or any other error occurs
     */
    public void delete(Context ctx) throws Exception {
        delete(ctx, getMasterCredentials());
    }

    /**
     * Delete the specified {@link Context}
     * 
     * @param ctx The {@link Context} to delete
     * @param credentials the credentials to use to authenticate against the server
     * @throws Exception if the context cannot be deleted or any other error occurs
     */
    public void delete(Context ctx, Credentials credentials) throws Exception {
        OXContextInterface contextInterface = getContextInterface();
        contextInterface.delete(ctx, credentials);
    }

    /**
     * Enables the specified {@link Context}
     * 
     * @param context The {@link Context} to enable
     * @throws Exception if an error is occurred
     */
    public void enable(Context context) throws Exception {
        OXContextInterface contextInterface = getContextInterface();
        contextInterface.enable(context, getMasterCredentials());
    }

    /**
     * Disables the specified {@link Context}
     * 
     * @param context The {@link Context} to disable
     * @throws Exception if an error is occurred
     */
    public void disable(Context context) throws Exception {
        OXContextInterface contextInterface = getContextInterface();
        contextInterface.disable(context, getMasterCredentials());
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
        return contextInterface.exists(context, getMasterCredentials());
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
        return contextInterface.getAdminId(context, getMasterCredentials());
    }

    /**
     * Searches and returns the next available free context identifier
     * 
     * @return The context identifier
     * @throws Exception if an error occurs
     */
    public int getNextFreeContextId() throws Exception {
        int pos = 5;
        int ret = -1;
        while (ret == -1) {
            ret = search(String.valueOf(pos)).length == 0 ? pos : -1;
            pos = pos + 3;
        }
        return ret;
    }

    /**
     * If context was changed, call this method to flush data which is no longer needed due to access permission changes!
     * 
     * @param context The {@link Context} to flush its data
     * @param contextAdminCredentials the context admin credentials
     * @throws Exception if an error is occurred
     */
    public void downgrade(Context context) throws Exception {
        OXContextInterface contextInteface = getContextInterface();
        contextInteface.downgrade(context, getMasterCredentials());
    }

    /**
     * Changes the context's module access combination
     * 
     * @param context The context
     * @param moduleAccessName The module access combination name
     * @throws Exception if an error is occurred
     */
    public void changeModuleAccess(Context context, String moduleAccessName) throws Exception {
        OXContextInterface contextInterface = getContextInterface();
        contextInterface.changeModuleAccess(context, moduleAccessName, getMasterCredentials());
    }

    ////////////////////////////// HELPERS ///////////////////////////////

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
        if (serverManager.search("local").length == 1) {
            return;
        }

        Server server = new Server();
        server.setName("local");
        serverManager.register(server);
    }

    /**
     * Registers a database if none exists
     * 
     * @throws Exception if an error is occurred during registration
     */
    private void registerDatabase() throws Exception {
        if (databaseManager.search("test-ox-db").length != 0) {
            return;
        }
        Database database = UtilTest.getTestDatabaseObject("localhost", "test-ox-db");
        databaseManager.register(database, Boolean.FALSE, Integer.valueOf(0));
    }

    /**
     * Registers a filestore if none exists
     * 
     * @throws Exception if an error is occurred during registration
     */
    private void registerFilestore() throws Exception {
        if (filestoreManager.search("*").length != 0) {
            return;
        }
        Filestore filestore = new Filestore();
        filestore.setMaxContexts(10000);
        filestore.setSize(8796093022208L);

        URI uri = new URI("file:/tmp/disc_" + System.currentTimeMillis());
        filestore.setUrl(uri.toString());

        new java.io.File(uri.getPath()).mkdir();
        filestoreManager.register(filestore);
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
}
