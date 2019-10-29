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
     * Checks if the administrative options are mandatory, configuration based, or optional and adds them
     * as command line options as such, i.e. as mandatory, optional or not at all.
     *
     * @return <code>true</code> if the administrative are required or configuration based; <code>false</code> otherwise
     */
    protected boolean optAdministrativeOptions() {
        Boolean requiresAdministrativePermission = requiresAdministrativePermission();
        if (requiresAdministrativePermission != null && false == requiresAdministrativePermission.booleanValue()) {
            return false;
        }

        // If not null, requiresAdministrativePermission is set to Boolean.TRUE at this location
        boolean mandatory = (requiresAdministrativePermission != null);
        addAdministrativeOptions(options, mandatory);
        return true;
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
