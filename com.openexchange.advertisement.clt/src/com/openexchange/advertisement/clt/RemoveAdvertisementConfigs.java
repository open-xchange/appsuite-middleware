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

package com.openexchange.advertisement.clt;

import java.rmi.RemoteException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.advertisement.RemoteAdvertisementService;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;

/**
 * {@link RemoveAdvertisementConfigs}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class RemoveAdvertisementConfigs extends AbstractRmiCLI<Void> {

    String reseller = null;
    boolean clean = false;
    boolean includePreview = false;

    /**
     * Invokes this command-line tool
     *
     * @param args The arguments
     */
    public static void main(String[] args) {
        new RemoveAdvertisementConfigs().execute(args);
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("r", "reseller", "resellerId", "Defines the reseller for which the configurations should be deleted. Use 'default' for the default reseller or in case no reseller are defined. If missing all configurations are deleted instead.", false));
        options.addOption(createSwitch("c", "clean", "If set the clt only removes configurations of resellers which doesn't exist any more.", false));
        options.addOption(createSwitch("i", "includePreviews", "If set the clt also removes preview configurations. This is only applicable in case the argument 'clean' is used.", false));
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        RemoteAdvertisementService rmiService = getRmiStub(optRmiHostName, RemoteAdvertisementService.RMI_NAME);
        rmiService.removeConfigurations(reseller, clean, includePreview);
        return null;
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        reseller = cmd.getOptionValue('r');
        clean = cmd.hasOption('c');
        includePreview = cmd.hasOption('i');

    }

    @Override
    protected String getFooter() {
        return "The command-line tool for deleting advertisement configurations.";
    }

    @Override
    protected String getName() {
        return "removeadvertisementconfigs -r <resellerId> -c -i " + BASIC_MASTER_ADMIN_USAGE;
    }
}
