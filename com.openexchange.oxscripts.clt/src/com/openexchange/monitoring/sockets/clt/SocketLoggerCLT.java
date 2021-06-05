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

package com.openexchange.monitoring.sockets.clt;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.monitoring.sockets.SocketLoggerRMIService;

/**
 * {@link SocketLoggerCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class SocketLoggerCLT extends AbstractRmiCLI<Void> {

    private static final String USAGE = "socketLogging " + BASIC_MASTER_ADMIN_USAGE;
    private static final String FOOTER = "Socket Logger Management Tool";

    private Boolean register;
    private String loggerName;

    /**
     * Entry point
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new SocketLoggerCLT().execute(args);
    }

    /**
     * Initialises a new {@link SocketLoggerCLT}.
     */
    private SocketLoggerCLT() {
        super();
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createSwitch("r", "register", "Registers a logger for socket logging", false));
        options.addOption(createSwitch("u", "unregister", "Unregisters a logger from socket logging ", false));
        options.addOption(createArgumentOption("n", "name", "loggerName", "The logger name to register/unregister for/from socket logging ", false));
        options.addOption(createSwitch("lr", "list-registered", "Lists all registered socket loggers", false));
        options.addOption(createSwitch("lb", "list-blacklisted", "Lists all blacklisted socket loggers", false));
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        try {
            SocketLoggerRMIService service = getRmiStub(optRmiHostName, SocketLoggerRMIService.RMI_NAME);
            return register == null ? listLoggers(cmd, service) : manageLogger(service);
        } catch (NotBoundException e) {
            System.out.println("The socket traffic logging service is not enabled. Activate socket monitoring by setting the properties 'com.openexchange.monitoring.sockets.enabled' and 'com.openexchange.monitoring.sockets.traffic.logging.enabled' to 'true' and reloading the server configuration.");
            return null;
        }
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (cmd.hasOption('r') && cmd.hasOption('u')) {
            System.out.println("Incompatible flag combination is used. You can either register or unregister a logger.");
            System.exit(-1);
        }
        if (cmd.hasOption('r')) {
            register = Boolean.valueOf(true);
        }
        if (cmd.hasOption('u')) {
            register = Boolean.valueOf(false);
        }
        if (register == null && cmd.hasOption('n')) {
            System.out.println("The -n flag can only be used in conjunction with -r/-u");
            System.exit(-1);
        }
        if (cmd.hasOption('n')) {
            loggerName = cmd.getOptionValue('n');
        }
    }

    @Override
    protected String getFooter() {
        return FOOTER;
    }

    @Override
    protected String getName() {
        return USAGE;
    }

    /////////////////////////////////////////////////////// HELPERS ////////////////////////////////////////////////

    /**
     * Lists registered or blacklisted loggers.
     *
     * @param cmd The command line instance
     * @param service The RMI service
     * @return <code>null</code>
     * @throws RemoteException if an error is occurred
     */
    private Void listLoggers(CommandLine cmd, SocketLoggerRMIService service) throws RemoteException {
        if (cmd.hasOption("lr")) {
            for (String logger : service.getRegisteredLoggers()) {
                System.out.println(logger);
            }
        } else if (cmd.hasOption("lb")) {
            for (String logger : service.getBlacklistedLoggers()) {
                System.out.println(logger);
            }
        }
        return null;
    }

    /**
     * Registers or unregisters a logger
     *
     * @param service The service
     * @return <code>null</code>
     * @throws RemoteException if an error is occurred
     */
    private Void manageLogger(SocketLoggerRMIService service) throws RemoteException {
        if (register.booleanValue()) {
            service.registerLoggerFor(loggerName);
        } else {
            service.unregisterLoggerFor(loggerName);
        }
        return null;
    }
}
