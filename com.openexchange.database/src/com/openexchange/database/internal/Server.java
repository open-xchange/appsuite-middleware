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

package com.openexchange.database.internal;

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.ConfigDatabaseService;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.exception.OXException;

/**
 * This class contains methods for handling the server name and identifier.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Server {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Server.class);

    private static final String PROPERTY_NAME = "SERVER_NAME";

    private static final String SELECT = "SELECT server_id FROM server WHERE name=?";

    private static final AtomicReference<String> SERVER_NAME_REF = new AtomicReference<String>(null);

    private static final AtomicReference<ConfigDatabaseService> CONFIG_DB_SERVICE_REF = new AtomicReference<ConfigDatabaseService>(null);

    static void setConfigDatabaseService(final ConfigDatabaseService configDatabaseService) {
        CONFIG_DB_SERVICE_REF.set(configDatabaseService);
    }

    /**
     * Prevent instantiation
     */
    private Server() {
        super();
    }

    private static final AtomicReference<Integer> SERVER_ID = new AtomicReference<>(null);

    /**
     * Gets the identifier of the registered server matching the configured <code>SERVER_NAME</code> property.
     *
     * @return The server identifier
     * @throws OXException If there is no such registered server matching configured <code>SERVER_NAME</code> property
     */
    public static int getServerId() throws OXException {
        // Load if not yet done
        Integer tmp = SERVER_ID.get();
        if (null == tmp) {
            synchronized (Server.class) {
                tmp = SERVER_ID.get();
                if (null == tmp) {
                    int iServerId = loadServerId(getServerName());
                    if (-1 == iServerId) {
                        throw DBPoolingExceptionCodes.NOT_RESOLVED_SERVER.create(getServerName());
                    }
                    tmp = Integer.valueOf(iServerId);
                    SERVER_ID.set(tmp);
                    LOG.trace("Got server id: {}", tmp);
                }
            }
        }
        return tmp.intValue();
    }

    /**
     * Initializes the server name using given configuration service.
     *
     * @param service The configuration service to use
     * @throws OXException If <code>SERVER_NAME</code> configuration property is missing
     */
    public static final void start(ConfigurationService service) throws OXException {
        String tmp = service.getProperty(PROPERTY_NAME);
        if (null == tmp || tmp.length() == 0) {
            throw DBPoolingExceptionCodes.NO_SERVER_NAME.create();
        }
        SERVER_NAME_REF.set(tmp);
    }

    /**
     * Gets the configured server name (see <code>SERVER_NAME</code> property in 'system.properties' file)
     *
     * @return The server name
     * @throws OXException If server name is absent
     */
    public static String getServerName() throws OXException {
        String tmp = SERVER_NAME_REF.get();
        if (null == tmp) {
            throw DBPoolingExceptionCodes.NOT_INITIALIZED.create(Server.class.getName());
        }
        return tmp;
    }

    /**
     * Resolves specified server name to its registered identifier.
     *
     * @param name The server name; e.g. <code>"oxserver"</code>
     * @return The server identifier or <code>-1</code> if no such server is registered with specified name
     * @throws OXException If resolving the server name fails
     */
    private static int loadServerId(String name) throws OXException {
        ConfigDatabaseService myService = CONFIG_DB_SERVICE_REF.get();
        if (null == myService) {
            throw DBPoolingExceptionCodes.NOT_INITIALIZED.create(Server.class.getName());
        }

        Connection con = myService.getReadOnly();
        try {
            return loadServerId(name, con);
        } finally {
            myService.backReadOnly(con);
        }
    }

    private static int loadServerId(String name, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement(SELECT);
            stmt.setString(1, name);
            result = stmt.executeQuery();
            return result.next() ? result.getInt(1) : -1;
        } catch (SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

}
