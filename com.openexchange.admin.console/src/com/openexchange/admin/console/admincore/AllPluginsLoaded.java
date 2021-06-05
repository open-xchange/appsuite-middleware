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

package com.openexchange.admin.console.admincore;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.admin.rmi.OXAdminCoreInterface;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;

public class AllPluginsLoaded extends AbstractRmiCLI<Void> {

    /**
     * The main method invoked on CLT execution.
     *
     * @param args The command-line arguments
     */
    public static void main(String[] args) {
        new AllPluginsLoaded().execute(args);
    }

    // ---------------------------------------------------------------------

    /**
     * Initializes a new {@link AllPluginsLoaded}.
     */
    public AllPluginsLoaded() {
        super();
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        checkOptions(cmd, null);
    }

    @Override
    protected void checkOptions(CommandLine cmd, Options options) {
        // Nothing to check
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.FALSE;
    }

    @Override
    protected String getFooter() {
        return "Checks whether all bundles are loaded and active.";
    }

    @Override
    protected String getName() {
        return "allpluginsloaded";
    }

    @Override
    protected void addOptions(Options options) {
        // Nothing
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        OXAdminCoreInterface pluginsLoaded = getRmiStub(optRmiHostName, OXAdminCoreInterface.RMI_NAME);
        if (pluginsLoaded.allPluginsLoaded()) {
            System.exit(0);
        } else {
            System.exit(1);
        }
        return null;
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws Exception {
        authenticator.doAuthentication(login, password);
    }
}
