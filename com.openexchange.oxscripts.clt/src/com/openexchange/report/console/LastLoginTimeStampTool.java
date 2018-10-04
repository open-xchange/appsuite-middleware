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

package com.openexchange.report.console;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import com.openexchange.ajax.Client;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.cli.AsciiTable;
import com.openexchange.report.internal.LoginCounterRMIService;

/**
 * {@link LastLoginTimeStampTool}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LastLoginTimeStampTool extends AbstractRmiCLI<Void> {

    private static final String HEADER = "Prints the time stamp of the last login for a user using a certain client.";
    private static final String SYNTAX = "lastlogintimestamp -c <contextId> [-u <userId> | -i <userId>] -t <clientId> [-d <datePattern>] -A <masterAdmin> -P <masterAdminPassword> [-p <RMI-Port>] [-s <RMI-Server] | --listclients | [-h]";
    private static final String FOOTER = "Examples:\n./lastlogintimestamp -c 1 -u 6 -t open-xchange-appsuite\n./lastlogintimestamp -c 1 -u 6 -t open-xchange-appsuite -d \"yyyy.MM.dd G 'at' HH:mm:ss z\"";

    public static void main(final String[] args) {
        new LastLoginTimeStampTool().execute(args);
    }

    private int userId = -1;
    private int contextId = -1;
    private String client = null;
    private String pattern = null;

    /**
     * Initializes a new {@link LastLoginTimeStampTool}.
     */
    private LastLoginTimeStampTool() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#administrativeAuth(java.lang.String, java.lang.String, org.apache.commons.cli.CommandLine, com.openexchange.auth.rmi.RemoteAuthenticator)
     */
    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#addOptions(org.apache.commons.cli.Options)
     */
    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("c", "context", "contextId", "A valid (numeric) context identifier", false));
        options.addOption(createArgumentOption("u", "user", "userId", "A valid (numeric) user identifier", false));
        options.addOption(createArgumentOption("d", "datepattern", "datePattern", "The optional date pattern used for formatting retrieved time stamp; e.g \"EEE, d MMM yyyy HH:mm:ss Z\" would yield \"Wed, 4 Jul 2001 12:08:56 -0700\"", false));
        options.addOption(createSwitch(null, "list-clients", "Outputs a table of known client identifiers", false));

        OptionGroup optionGroup = new OptionGroup();
        optionGroup.setRequired(false);
        optionGroup.addOption(createArgumentOption("i", null, "userId", "A valid (numeric) user identifier. As alternative for the \"-u, --user\" option.", false));
        optionGroup.addOption(createArgumentOption("t", "client", "clientId", "A client identifier; e.g \"open-xchange-appsuite\" for App Suite UI. Execute \"./lastlogintimestamp --listclients\" to get a listing of known identifiers.", false));
        options.addOptionGroup(optionGroup);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        boolean error = true;
        try {
            LoginCounterRMIService rmiService = getRmiStub(optRmiHostName, LoginCounterRMIService.RMI_NAME);
            List<Object[]> lastLoginTimeStamp = rmiService.getLastLoginTimeStamp(userId, contextId, client);
            if (null == lastLoginTimeStamp || lastLoginTimeStamp.isEmpty()) {
                System.out.println("No matching entry found.");
            } else if (1 == lastLoginTimeStamp.size()) {
                SimpleDateFormat sdf = new SimpleDateFormat(null == pattern ? "EEE, d MMM yyyy HH:mm:ss z" : pattern, Locale.US);
                final Object[] objs = lastLoginTimeStamp.get(0);
                System.out.println(sdf.format(objs[0]));
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat(null == pattern ? "EEE, d MMM yyyy HH:mm:ss z" : pattern, Locale.US);
                for (final Object[] objs : lastLoginTimeStamp) {
                    System.out.println(sdf.format(objs[0]) + " -- " + objs[1]);
                }
            }
            error = false;
        } catch (final MalformedURLException e) {
            System.err.println("URL to connect to server is invalid: " + e.getMessage());
        } catch (final IOException e) {
            System.err.println("Unable to communicate with the server: " + e.getMessage());
        } catch (final RuntimeException e) {
            System.err.println("Problem in runtime: " + e.getMessage());
            printHelp();
        } finally {
            if (error) {
                System.exit(1);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractAdministrativeCLI#requiresAdministrativePermission()
     */
    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#checkOptions(org.apache.commons.cli.CommandLine)
     */
    @Override
    protected void checkOptions(CommandLine cmd) {
        if (cmd.hasOption("listclients")) {
            printClients();
            System.exit(0);
        }
        if (!cmd.hasOption('t')) {
            System.err.println("Missing client identifier.");
            printHelp();
            System.exit(1);
        }
        client = cmd.getOptionValue('t');

        if (cmd.hasOption('d')) {
            pattern = cmd.getOptionValue('d');
        }

        if (!cmd.hasOption('c')) {
            System.err.println("Missing context identifier.");
            printHelp();
            System.exit(1);
        }
        String optionValue = cmd.getOptionValue('c');
        try {
            contextId = Integer.parseInt(optionValue.trim());
        } catch (final NumberFormatException e) {
            System.err.println("Context identifier parameter is not a number: " + optionValue);
            printHelp();
            System.exit(1);
        }

        if (cmd.hasOption('u')) {
            optionValue = cmd.getOptionValue('u');
        } else if (cmd.hasOption('i')) {
            optionValue = cmd.getOptionValue('i');
        } else {
            System.err.println("Missing user identifier.");
            printHelp();
            System.exit(1);
        }
        try {
            userId = Integer.parseInt(optionValue.trim());
        } catch (final NumberFormatException e) {
            System.err.println("User identifier parameter is not a number: " + optionValue);
            printHelp();
            System.exit(1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getFooter()
     */
    @Override
    protected String getFooter() {
        return FOOTER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getName()
     */
    @Override
    protected String getName() {
        return SYNTAX;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractCLI#getHeader()
     */
    @Override
    protected String getHeader() {
        return HEADER;
    }

    //////////////////////////////// HELPERS ///////////////////////////////////

    /**
     * Prints the clients
     */
    private void printClients() {
        AsciiTable table = new AsciiTable();
        table.setMaxColumnWidth(45);

        table.addColumn(new AsciiTable.Column("Client ID"));
        table.addColumn(new AsciiTable.Column("Description"));
        for (Client client : Client.values()) {
            AsciiTable.Row row = new AsciiTable.Row();
            row.addValue(client.getClientId());
            row.addValue(client.getDescription());
            table.addData(row);
        }

        table.calculateColumnWidth();
        table.render();
    }

}
