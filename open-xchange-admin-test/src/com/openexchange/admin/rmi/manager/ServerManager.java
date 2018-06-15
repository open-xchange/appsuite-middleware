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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
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
    
    private static final Logger LOG = LoggerFactory.getLogger(ServerManager.class);
    
    private final Map<Integer, Server> managedServers;

    /**
     * Initialises a new {@link ServerManager}.
     * 
     * @param rmiEndPointURL
     * @param masterCredentials
     */
    private ServerManager(String rmiEndPointURL, Credentials masterCredentials) {
        super(rmiEndPointURL, masterCredentials);
        managedServers = new HashMap<>();
    }
    
    /**
     * Unregisters all managed {@link Server}s
     */
    public void cleanUp() {
        Map<Integer, Server> failed = new HashMap<>();
        for (Entry<Integer, Server> entry : managedServers.entrySet()) {
            try {
                unregisterServer(entry.getValue());
            } catch (Exception e) {
                LOG.error("Server '{}' could not be unregistered!", entry.getValue().getId());
                failed.put(entry.getKey(), entry.getValue());
            }
        }
        managedServers.clear();

        if (failed.isEmpty()) {
            return;
        }
        LOG.warn("The following server were not unregistered: '{}'. Manual intervention might be required.", failed.toString());
    }

    /**
     * Registers the specified server
     * 
     * @param server The {@link Server} to register
     * @return The registered server
     * @throws Exception if an error occurs during registration
     */
    public Server registerServer(Server server) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        return utilInterface.registerServer(server, getMasterCredentials());
    }

    /**
     * Unregisters the specified {@link Server}
     * 
     * @param server The {@link Server} to unregister
     * @throws Exception if an error occurs
     */
    public void unregisterServer(Server server) throws Exception {
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
    public Server[] listServers(String searchPattern) throws Exception {
        OXUtilInterface utilInterface = getUtilInterface();
        return utilInterface.listServer(searchPattern, getMasterCredentials());
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.manager.AbstractManager#clean(java.lang.Object)
     */
    @Override
    boolean clean(Object object) {
        // TODO Auto-generated method stub
        return false;
    }
}
