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

package com.openexchange.external.accounts.clt;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.external.account.ExternalAccountModule;
import com.openexchange.java.Enums;

/**
 * {@link AbstractExternalAccountCLT}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.4
 */
abstract class AbstractExternalAccountCLT extends AbstractRmiCLI<Void> {

    static final String USAGE = "[-c <contextId>] [-u <userId>] [-m <module>]";

    private final String footer;
    private final String syntax;

    /**
     * Initializes a new {@link AbstractExternalAccountCLT}.
     */
    public AbstractExternalAccountCLT(String syntax, String footer) {
        super();
        this.footer = footer;
        this.syntax = syntax;
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("c", "context", "contextId", "Required. The context identifier", true));
        options.addOption(createArgumentOption("u", "user", "userId", (isUserMandatory() ? "Required. " : "") + "The user identifier", isUserMandatory()));
        options.addOption(createArgumentOption("m", "module", "module", (isModuleMandatory() ? "Required. " : "") + "The module identifier. Valid modules are: " + getAvailableModules(), isModuleMandatory()));
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws Exception {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected String getFooter() {
        return footer;
    }

    @Override
    protected String getName() {
        return syntax;
    }

    /**
     * Gets the {@link ExternalAccountModule} from the command line arguments
     *
     * @param cmd the command line containing the arguments
     * @return The context identifier
     */
    ExternalAccountModule getModule(CommandLine cmd) {
        String module = cmd.getOptionValue('m');
        try {
            return ExternalAccountModule.valueOf(module.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid value '" + module + "' for option 'm'. Available modules are: " + getAvailableModules());
            System.exit(1);
            return null;
        }
    }

    /**
     * Returns a comma-separated list with all available modules
     *
     * @return the string
     */
    String getAvailableModules() {
        return Enums.toCommaSeparatedList(ExternalAccountModule.values());
    }

    /**
     * Returns whether the user option is mandatory
     *
     * @return whether the user option is mandatory
     */
    abstract boolean isUserMandatory();

    /**
     * Returns whether the module option is mandatory
     *
     * @return whether the module option is mandatory
     */
    abstract boolean isModuleMandatory();
}
