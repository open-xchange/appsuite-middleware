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

package com.openexchange.sessiond.impl.clt;

import java.rmi.RemoteException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.sessiond.rmi.SessiondRMIService;

/**
 * {@link CloseSessionsCLT} - Command-Line access clear all sessions belonging to a given context.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class CloseSessionsCLT extends AbstractRmiCLI<Void> {

    private static final String SYNTAX = "closesessions -c <contextId> [-u <userId>] [-g] -A <masterAdmin | contextAdmin> -P <masterAdminPassword | contextAdminPassword> [-p <RMI-Port>] [-s <RMI-Server>] | [-h]";

    private int contextId;
    private int userId;
    private boolean global;

    /**
     * Entry point
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new CloseSessionsCLT().execute(args);
    }

    // ------------------------------------------------------------------------------------ //

    /**
     * Initializes a new {@link CloseSessionsCLT}.
     */
    private CloseSessionsCLT() {
        super();
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (false == cmd.hasOption('c')) {
            System.err.println("Missing context identifier.");
            printHelp(options);
            System.exit(1);
        }

        contextId = parseInt('c', -1, cmd, options);
        if (cmd.hasOption('u')) {
            userId = parseInt('u', -1, cmd, options);
        }
        global = cmd.hasOption('g');
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("c", "context", "contextId", "A valid context identifier", true));
        options.addOption(createArgumentOption("u", "user", "userId", "A valid user identifier", false));
        options.addOption(createSwitch("g", "global", "Switch instructing the tool to perform a global session clean-up ", false));
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    @Override
    protected String getFooter() {
        return "Clear all sessions belonging to a given context and/or user.";
    }

    @Override
    protected String getName() {
        return SYNTAX;
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
     * @see com.openexchange.cli.AbstractRmiCLI#invoke(org.apache.commons.cli.Options, org.apache.commons.cli.CommandLine, java.lang.String)
     */
    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        SessiondRMIService rmiService = getRmiStub(optRmiHostName, SessiondRMIService.RMI_NAME);
        if (userId > 0) {
            if (global) {
                rmiService.clearUserSessionsGlobally(userId, contextId);
                System.out.println("Globally cleared sessions for user " + userId + " in context " + contextId);
            } else {
                rmiService.clearUserSessions(userId, contextId);
                System.out.println("Locally cleared sessions for user " + userId + " in context " + contextId);
            }
            return null;
        }
        if (global) {
            rmiService.clearContextSessionsGlobally(contextId);
            System.out.println("Globally cleared sessions for context " + contextId);
        } else {
            rmiService.clearContextSessions(contextId);
            System.out.println("Locally cleared sessions for context " + contextId);
        }
        return null;
    }
}
