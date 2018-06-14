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
        for (Entry<Integer, Context> entry : registeredContexts.entrySet()) {
            try {
                deleteContext(entry.getValue());
            } catch (Exception e) {
                LOG.error("Context '{}' could not be deleted!", entry.getValue().getId());
            }
        }
    }

    /**
     * Creates a new {@link Context}
     * 
     * @param contextAdminCredentials
     * @return
     * @throws Exception
     */
    public Context createContext(Credentials contextAdminCredentials) throws Exception {
        return createContext(getNextFreeContextId(), contextAdminCredentials);
    }

    /**
     * Creates a new {@link Context}. Goes through the process and registers a server, a filestore and/or
     * a database if any of those are absent
     * 
     * @param contextId The context identifier
     * @param contextAdminCredentials The context admin credentials
     * @return The created context
     * @throws Exception if the context cannot be created or any other error is occurred
     */
    public Context createContext(int contextId, Credentials contextAdminCredentials) throws Exception {
        OXUtilInterface oxu = (OXUtilInterface) Naming.lookup(host + OXUtilInterface.RMI_NAME);

        // Register server if none exists
        if (oxu.listServer("local", masterCredentials).length != 1) {
            Server srv = new Server();
            srv.setName("local");
            oxu.registerServer(srv, masterCredentials);
        }

        // Register a filestore if none exists
        if (oxu.listFilestore("*", masterCredentials).length == 0) {
            Filestore filestore = new Filestore();
            filestore.setMaxContexts(10000);
            filestore.setSize(8796093022208L);
            URI uri = new URI("file:/tmp/disc_" + System.currentTimeMillis());
            filestore.setUrl(uri.toString());
            new java.io.File(uri.getPath()).mkdir();
            oxu.registerFilestore(filestore, masterCredentials);
        }

        // Register a database if none exists
        if (oxu.listDatabase("test-ox-db", masterCredentials).length == 0) {
            Database database = UtilTest.getTestDatabaseObject("localhost", "test-ox-db");
            oxu.registerDatabase(database, Boolean.FALSE, Integer.valueOf(0), masterCredentials);
        }

        OXContextInterface contextInterface = getContextInterface();
        Context context = ContextFactory.createContext(contextId);
        contextInterface.create(context, UserTest.getTestUserObject(contextAdminCredentials.getLogin(), contextAdminCredentials.getPassword(), context), masterCredentials);
        return context;
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
     * Returns the {@link OXContextInterface}
     * 
     * @return The {@link OXContextInterface}
     * @throws Exception if an error is occurred during RMI look-up
     */
    private OXContextInterface getContextInterface() throws Exception {
        return (OXContextInterface) Naming.lookup(host + OXContextInterface.RMI_NAME);
    }
}
