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

package com.openexchange.admin.console.util.database;

import java.rmi.Naming;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.AdminParser.NeededQuadState;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.util.UtilAbstraction;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.admin.schemamove.mbean.SchemaMoveRemote;
import com.openexchange.groupware.update.tools.Constants;

/**
 * {@link UpgradeSchemata}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class UpgradeSchemata extends UtilAbstraction {

    private static final String OPT_NAME_LONG = "name";
    private static final char OPT_NAME_SHORT = 'n';
    private CLIOption serverNameOption;

    private Server server;

    /**
     * Entry point
     * 
     * @param args The command line arguments
     */
    public static void main(String args[]) {
        new UpgradeSchemata(args);
    }

    /**
     * Initialises a new {@link UpgradeSchemata}.
     * 
     * @param args The command line arguments
     */
    public UpgradeSchemata(String[] args) {
        super();

        final AdminParser parser = new AdminParser("upgradeschemata");
        setOptions(parser);
        try {
            parser.ownparse(args);
            execute(parser);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.console.ObjectNamingAbstraction#getObjectName()
     */
    @Override
    protected String getObjectName() {
        return "upgradeschemata";
    }

    //////////////////////// HELPERS //////////////////////////

    /**
     * Executes the command
     * 
     * @param parser The {@link AdminParser}
     * @throws Exception
     */
    private void execute(AdminParser parser) throws Exception {
        checkAndSetArguments(parser);
        Credentials auth = credentialsparsing(parser);

        // Register server
        OXUtilInterface oxUtil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);
        server = oxUtil.registerServer(server, auth);
        System.out.println("The server with name '" + server.getName() + "' was successfully registered with id '" + server.getId() + "'.");

        // List all database schemata
        final Database[] databases = oxUtil.listDatabaseSchema("*", false, auth);

        SchemaMoveRemote smr = (SchemaMoveRemote) Naming.lookup(RMI_HOSTNAME + SchemaMoveRemote.RMI_NAME);

        //TODO: add prameters for JMX
        final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
        final JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
        final MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();

        for (Database database : databases) {
            // Disable schema
            String schemaName = database.getScheme();
            System.out.println("Disabling schema '" + schemaName + "'");
            smr.disableSchema(auth, schemaName);
            smr.invalidateContexts(auth, schemaName, true);

            // Change server
            System.out.println("Changing server for schema '" + schemaName + "' to '" + server.getName() + "'");
            oxUtil.changeServer(server, schemaName, auth);

            // Perform upgrade
            System.out.println("Running updates...");
            Object failures = mbsc.invoke(Constants.OBJECT_NAME, "runUpdate", new Object[] { schemaName }, null);
            if (null != failures) {
                String message = failures.toString();
                message = message.replaceAll("\\\\R", System.getProperty("line.separator"));
                System.out.println(message);
            }

            // Enable schema
            System.out.println("Enabling schema '" + schemaName + "'");
            smr.enableSchema(auth, schemaName);
            smr.invalidateContexts(auth, schemaName, false);
        }
    }

    /**
     * Checks the arguments
     * 
     * @param parser The {@link AdminParser}
     */
    private void checkAndSetArguments(AdminParser parser) {
        // Parse the server name
        String serverName = (String) parser.getOptionValue(serverNameOption);
        server = new Server();
        server.setName(serverName);
    }

    /**
     * Set additional options
     * 
     * @param parser the {@link AdminParser}
     */
    private void setOptions(AdminParser parser) {
        serverNameOption = setShortLongOpt(parser, OPT_NAME_SHORT, OPT_NAME_LONG, "The name of the server", true, NeededQuadState.needed);
        setDefaultCommandLineOptionsWithoutContextID(parser);
    }
}
