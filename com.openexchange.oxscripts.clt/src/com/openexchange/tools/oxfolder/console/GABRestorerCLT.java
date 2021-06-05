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

package com.openexchange.tools.oxfolder.console;

import java.rmi.RemoteException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.tools.oxfolder.GABMode;
import com.openexchange.tools.oxfolder.GABRestorerRMIService;

/**
 * {@link GABRestorerCLT} - Restores default permissions for global address book (GAB).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class GABRestorerCLT extends AbstractRmiCLI<Void> {

    private static final String SYNTAX = "restoregabdefaults -c <contextId> -g <gabMode> " + BASIC_MASTER_ADMIN_USAGE;
    private static final String FOOTER = "Restores the default permissions for the global address book (GAB).";

    /**
     * Entry point
     * 
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        new GABRestorerCLT().execute(args);
    }

    private int contextId;
    private String gabMode;

    /**
     * Initializes a new {@link GABRestorerCLT}.
     */
    private GABRestorerCLT() {
        super();
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("c", "context", "contextId", "A valid context identifier contained in target schema", true));
        options.addOption(createArgumentOption("g", "gabMode", "gabMode", "The optional modus the global address book shall operate on. Currently 'global' and 'individual' are known values.", false));
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        GABRestorerRMIService rmiService = getRmiStub(optRmiHostName, GABRestorerRMIService.RMI_NAME);
        rmiService.restoreDefaultPermissions(contextId, GABMode.of(gabMode));
        return null;
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (cmd.hasOption('g')) {
            gabMode = cmd.getOptionValue('g');
        }
        if (cmd.hasOption('c')) {
            final String optionValue = cmd.getOptionValue('c');
            try {
                contextId = Integer.parseInt(optionValue.trim());
            } catch (NumberFormatException e) {
                System.err.println("Context parameter is not a number: " + optionValue);
                printHelp();
                System.exit(1);
            }
            return;
        }
        System.err.println("Missing context identifier.");
        printHelp();
        System.exit(1);
    }

    @Override
    protected String getFooter() {
        return FOOTER;
    }

    @Override
    protected String getName() {
        return SYNTAX;
    }
}
