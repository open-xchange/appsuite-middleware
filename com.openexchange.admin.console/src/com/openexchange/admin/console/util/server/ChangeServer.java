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

package com.openexchange.admin.console.util.server;

import java.rmi.Naming;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.rmi.OXUtilInterface;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Server;
import com.openexchange.java.Strings;

/**
 * {@link ChangeServer} - Command line tool for changing/switching server for specific database schemata
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ChangeServer extends ServerAbstraction {

    private static final char SCHEMA_NAME_SHORT = 'm';
    private static final String SCHEMA_NAME_LONG = "schema-name";
    private static final String SCHEMA_NAME_ARG_NAME = "schema_name";
    private static final String SCHEMA_NAME_DESCRIPTION = "The name of the schema for which to change the server id";

    private static final char SERVER_ID_SHORT = 's';
    private static final String SERVER_ID_LONG = "server-id";
    private static final String SERVER_ID_ARG_NAME = "server_id";
    private static final String SERVER_ID_DESCRIPTION = "The identifier of the new server";

    private CLIOption schemaNameOption;
    @SuppressWarnings("hiding")
    private CLIOption serverIdOption;

    /**
     * Entry point
     * 
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        new ChangeServer().execute(args);
    }

    /**
     * Initializes a new {@link ChangeServer}.
     */
    private ChangeServer() {
        super();
    }

    /**
     * Executes the command
     * 
     * @param args The command line arguments
     */
    private void execute(String args[]) {
        AdminParser parser = new AdminParser("changeserver");
        setOptions(parser);

        try {
            parser.ownparse(args);

            final Credentials auth = credentialsparsing(parser);

            // Parse the schema name
            String schemaName = (String) parser.getOptionValue(schemaNameOption);
            if (Strings.isEmpty(schemaName)) {
                printError("Please specify the schema name\n", parser);
                sysexit(1);
                return;
            }

            // Parse the serverId
            Integer serverId;
            String sid = (String) parser.getOptionValue(serverIdOption);
            if (Strings.isEmpty(sid)) {
                printError("Please specify the server identifier\n", parser);
                sysexit(1);
                return;
            }

            try {
                serverId = Integer.valueOf(sid);
            } catch (NumberFormatException e) {
                printError("Invalid server id: " + sid, parser);
                sysexit(1);
                return;
            }
            final OXUtilInterface oxutil = (OXUtilInterface) Naming.lookup(RMI_HOSTNAME + OXUtilInterface.RMI_NAME);

            final Server srv = new Server();
            srv.setId(serverId);

            oxutil.changeServer(srv, schemaName, auth);
            System.out.println("Successfully changed to server '" + serverId + "' for schema '" + schemaName + "'.");
            sysexit(0);
        } catch (Exception e) {
            printErrors(null, null, e, parser);
        }
    }

    /**
     * Set additional options
     * 
     * @param parser the {@link AdminParser}
     */
    private void setOptions(final AdminParser parser) {
        schemaNameOption = setShortLongOpt(parser, SCHEMA_NAME_SHORT, SCHEMA_NAME_LONG, SCHEMA_NAME_ARG_NAME, SCHEMA_NAME_DESCRIPTION, true);
        serverIdOption = setShortLongOpt(parser, SERVER_ID_SHORT, SERVER_ID_LONG, SERVER_ID_ARG_NAME, SERVER_ID_DESCRIPTION, true);
        setDefaultCommandLineOptionsWithoutContextID(parser);
    }
}
