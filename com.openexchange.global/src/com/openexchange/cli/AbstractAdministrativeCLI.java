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

package com.openexchange.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.annotation.Nullable;
import com.openexchange.java.Strings;

/**
 * {@link AbstractAdministrativeCLI} - The basic class for administrative command-line tools that probably require appropriate permissions.
 *
 * @param <R> - The return type
 * @param <C> - The execution context type
 * @param <A> - The authenticator type
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractAdministrativeCLI<R, C, A> extends AbstractCLI<R, C> {

    /**
     * Initializes a new {@link AbstractAdministrativeCLI}.
     */
    protected AbstractAdministrativeCLI() {
        super();
    }

    /**
     * Signals if this command-line tool requires administrative permission.
     *
     * @return <code>null</code> if it's configuration based and shall be affected by the <code>MASTER_AUTHENTICATION_DISABLED</code> property,
     *         <code>true</code> for mandatory administrative permission which shall override the previous mentioned property; otherwise <code>false</code>
     */
    @Nullable
    protected Boolean requiresAdministrativePermission() {
        return null;
    }

    /**
     * Checks if the administrative options are mandatory, configuration based, or optional and adds them as command line options as such,
     * i.e. as mandatory, optional or not at all.
     *
     * @param args The optional command-line arguments
     * @return <code>true</code> if the administrative options are required or configuration based; <code>false</code> otherwise
     * @throws Exception If check for administrative options fails
     */
    protected boolean optAdministrativeOptions(String... args) throws Exception {
        Boolean requiresAdministrativePermission = requiresAdministrativePermission();
        if (requiresAdministrativePermission != null && false == requiresAdministrativePermission.booleanValue()) {
            return false;
        }

        // If not null, requiresAdministrativePermission is set to Boolean.TRUE
        try {
            boolean mandatory = (requiresAdministrativePermission != null && isAuthEnabled(getAuthenticator()));
            addAdministrativeOptions(options, mandatory);
            return true;
        } catch (Exception e) {
            boolean helpRequested = helpRequested(args);
            if (helpRequested) {
                // Assume administrative options are enabled for "--help" output
                System.out.println("Note: usage output below might not be accurate due to encountered " + e.getClass().getName());
                System.out.println();
                addAdministrativeOptions(options, true);
                return true;
            }
            throw e;
        }
    }

    /**
     * Performs administrative authentication
     *
     * @param cmd The {@link CommandLine} that contains all command line arguments
     * @throws Exception if the operation fails
     */
    protected void optAuthenticate(CommandLine cmd) throws Exception {
        A authenticator = getAuthenticator();
        if (false == isAuthEnabled(authenticator)) {
            return;
        }
        // Options for administrative authentication
        String adminLogin = cmd.getOptionValue('A');
        if (Strings.isEmpty(adminLogin)) {
            System.out.println("You must provide administrative credentials to proceed.");
            printHelp(options);
            System.exit(getAuthFailedExitCode());
        }

        String adminPassword = cmd.getOptionValue('P');
        if (Strings.isEmpty(adminPassword)) {
            System.out.println("You must provide administrative credentials to proceed.");
            printHelp(options);
            System.exit(getAuthFailedExitCode());
        }

        administrativeAuth(adminLogin, adminPassword, cmd, authenticator);
    }

    /**
     * Adds administrative options
     *
     * @param options The {@link Options} instance to add administrative options to.
     * @param mandatory Whether the administrative options shall be mandatory
     */
    protected void addAdministrativeOptions(Options options, boolean mandatory) {
        options.addOption(createArgumentOption("A", "adminuser", "adminUser", "Admin username", mandatory));
        options.addOption(createArgumentOption("P", "adminpass", "adminPassword", "Admin password", mandatory));
    }

    /**
     * Returns the exit code when authentication fails
     *
     * @return the exit code when authentication fails
     */
    protected abstract int getAuthFailedExitCode();

    /**
     * Returns the authenticator stub
     *
     * @return The Authenticator stub
     * @throws Exception if the operation fails
     */
    protected abstract A getAuthenticator() throws Exception;

    /**
     * Performs appropriate administrative authentication.
     * <p>
     * This method needs only to be implemented in case {@link #requiresAdministrativePermission()} is supposed to return <code>true</code>.
     *
     * @param login The administrator login
     * @param password The administrator password
     * @param cmd The command line providing options
     * @param authenticator The authenticator stub
     * @throws Exception If operation fails
     */
    protected abstract void administrativeAuth(String login, String password, CommandLine cmd, A authenticator) throws Exception;

    /**
     * Checks if authentication is enabled.
     * <p>
     * By default property <code>"MASTER_AUTHENTICATION_DISABLED"</code> gets examined.
     *
     * @param authenticator The authenticator stub
     * @throws Exception If operation fails
     */
    protected abstract boolean isAuthEnabled(A authenticator) throws Exception;
}
