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

package com.openexchange.client.onboarding.clt;

import java.rmi.RemoteException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.cli.OutputHelper;
import com.openexchange.client.onboarding.rmi.RemoteOnboardingService;

/**
 * {@link ListOnboardingProvidersTool}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ListOnboardingProvidersTool extends AbstractRmiCLI<Void> {

    /**
     * Invokes this command-line tool
     *
     * @param args The arguments
     */
    public static void main(String[] args) {
        new ListOnboardingProvidersTool().execute(args);
    }

    // --------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link ListOnboardingProvidersTool}.
     */
    private ListOnboardingProvidersTool() {
        super();
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        // No more options needed
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        RemoteOnboardingService rmiService = getRmiStub(optRmiHostName, RemoteOnboardingService.RMI_NAME);
        List<List<String>> data = rmiService.getAllProviders();
        OutputHelper.doOutput(new String[] { "l", "l", "l", "l" }, new String[] { "Provider", "Description", "Supported types", "Supported devices" }, data);
        return null;
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    @Override
    protected void checkOptions(CommandLine cmd, Options options) {
        // Nothing to check
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        checkOptions(cmd, null);
    }

    @Override
    protected String getFooter() {
        return "The command-line tool for listing available on-boarding providers";
    }

    @Override
    protected String getName() {
        return "listonboardingproviders " + BASIC_MASTER_ADMIN_USAGE;
    }

}
