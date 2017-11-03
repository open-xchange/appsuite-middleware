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
 *     Copyright (C) 2017-2020 OX Software GmbH
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
    private CLIOption serverIdOption;

    /**
     * Entry point
     * 
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        new ChangeServer(args);
    }

    /**
     * Initialises a new {@link ChangeServer}.
     * 
     * @param args The command line arguments
     */
    public ChangeServer(String args[]) {
        super();

        final AdminParser parser = new AdminParser("changeserver");
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
        } catch (final Exception e) {
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
