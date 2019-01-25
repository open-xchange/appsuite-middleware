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

package com.openexchange.push.console;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.cli.OutputHelper;
import com.openexchange.push.rmi.PushRMIService;

/**
 * {@link PushUsersCLT} - The command-line tool for unregistering and listing (registered) push users
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class PushUsersCLT extends AbstractRmiCLI<Void> {

    private static final String SYNTAX = "pushusers [-l | [ -r -c <contextId> -u <userId> -i <client>] ] " + BASIC_MASTER_ADMIN_USAGE;
    private static final String FOOTER = "Command-line tool for unregistering and listing push users and client registrations";

    private int contextId;
    private int userId;
    private String clientId;

    private enum Mode {
        LIST,
        LIST_REGISTERED_CLIENTS,
        UNREGISTER;
    }

    private Mode mode = Mode.LIST;

    /**
     * Entry point
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new PushUsersCLT().execute(args);
    }

    /**
     * Initialises a new {@link PushUsersCLT}.
     */
    private PushUsersCLT() {
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
        options.addOption(createSwitch("l", "list-client-registrations", "Flag to list client registrations", false));
        options.addOption(createSwitch("r", "unregister", "Flag to unregister a push user", false));
        options.addOption(createArgumentOption("c", "context", "contextId", "A valid context identifier", false));
        options.addOption(createArgumentOption("u", "user", "userId", "A valid user identifier", false));
        options.addOption(createArgumentOption("i", "client", "clientId", "The client identifier", false));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        switch (mode) {
            case LIST:
                listPushUsers(optRmiHostName);
                break;
            case LIST_REGISTERED_CLIENTS:
                listRegisteredPushUsers(optRmiHostName);
                break;
            case UNREGISTER:
                unregisterPushListener(optRmiHostName);
                break;
            default:
                printHelp(options, 120);
                System.exit(1);
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
        if (cmd.hasOption('l') && cmd.hasOption('r')) {
            System.err.println("The options -l and -r are mutually exclusive.");
            printHelp(options, 120);
            System.exit(1);
        }
        if (cmd.hasOption('l')) {
            mode = Mode.LIST_REGISTERED_CLIENTS;
            return;
        }
        if (cmd.hasOption('r')) {
            mode = Mode.UNREGISTER;
            contextId = parseInt('c', -1, cmd, options);
            userId = parseInt('u', -1, cmd, options);
            clientId = cmd.getOptionValue('i');
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

    /**
     * Lists all push users
     */
    private void listPushUsers(String optRmiHostName) throws MalformedURLException, RemoteException, NotBoundException {
        PushRMIService rmiService = getRmiStub(optRmiHostName, PushRMIService.RMI_NAME);
        List<List<String>> data = rmiService.listPushUsers();
        if (null == data || data.isEmpty()) {
            System.out.println("No running push users on this node.");
        } else {
            OutputHelper.doOutput(new String[] { "r", "l", "l" }, new String[] { "Context", "User", "Permanent" }, data);
        }
    }

    /**
     * Lists the registered push users
     */
    private void listRegisteredPushUsers(String optRmiHostName) throws RemoteException, MalformedURLException, NotBoundException {
        PushRMIService rmiService = getRmiStub(optRmiHostName, PushRMIService.RMI_NAME);
        List<List<String>> data = rmiService.listClientRegistrations();
        if (null == data || data.isEmpty()) {
            System.out.println("No registered push users.");
        } else {
            OutputHelper.doOutput(new String[] { "r", "l", "l" }, new String[] { "Context", "User", "Client" }, data);
        }
    }

    /**
     * Unregisters the push listener for the user and client
     */
    private void unregisterPushListener(String optRmiHostName) throws MalformedURLException, RemoteException, NotBoundException {
        PushRMIService rmiService = getRmiStub(optRmiHostName, PushRMIService.RMI_NAME);
        boolean deleted = rmiService.unregisterPermanentListenerFor(userId, contextId, clientId);
        if (deleted) {
            System.out.println("Push registration successfully deleted for user " + userId + " in context " + contextId + " for client '" + clientId + "'");
        } else {
            System.out.println("No such push registration for user " + userId + " in context " + contextId + " for client '" + clientId + "'");
        }
    }

}
