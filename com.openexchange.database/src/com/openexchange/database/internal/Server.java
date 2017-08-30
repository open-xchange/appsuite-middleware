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

    /**
     * Prevent instantiation
     */
    private Server() {
        super();
    }

    static void setConfigDatabaseService(final ConfigDatabaseService configDatabaseService) {
        CONFIG_DB_SERVICE_REF.set(configDatabaseService);
    }

    private static volatile Integer serverId;

    /**
     * Gets the identifier of the registered server matching the configured <code>SERVER_NAME</code> property.
     *
     * @return The server identifier
     * @throws OXException If there is no such registered server matching configured <code>SERVER_NAME</code> property
     */
    public static int getServerId() throws OXException {
        // Load if not yet done
        Integer tmp = serverId;
        if (null == tmp) {
            synchronized (Server.class) {
                tmp = serverId;
                if (null == tmp) {
                    int iServerId = Server.loadServerId(getServerName());
                    if (-1 == iServerId) {
                        throw DBPoolingExceptionCodes.NOT_RESOLVED_SERVER.create(getServerName());
                    }
                    tmp = Integer.valueOf(iServerId);
                    serverId = tmp;
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
    private static int loadServerId(final String name) throws OXException {
        ConfigDatabaseService myService = CONFIG_DB_SERVICE_REF.get();
        if (null == myService) {
            throw DBPoolingExceptionCodes.NOT_INITIALIZED.create(Server.class.getName());
        }

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = myService.getReadOnly();
            stmt = con.prepareStatement(SELECT);
            stmt.setString(1, name);
            result = stmt.executeQuery();
            return result.next() ? result.getInt(1) : -1;
        } catch (final SQLException e) {
            throw DBPoolingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
            if (null != con) {
                myService.backReadOnly(con);
            }
        }
    }

}
