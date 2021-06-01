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

package com.openexchange.drive.client.windows.clt;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.drive.client.windows.service.rmi.BrandingConfigurationRemote;

/**
 * {@link ReloadBrandings}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class ReloadBrandings extends AbstractRmiCLI<Void> {

    private static final String BRANDINGS_AVAILABLE_MSG = "The following brandingss are now available:";
    private static final String BRANDINGS_RELOADED_MSG = "Brandings successful reloaded!";

    private static final String PATH_LONG = "path";

    public static void main(String[] args) {
        new ReloadBrandings().execute(args);
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(null, PATH_LONG, true, "Defines the path to look for brandings. If set the configured path will be ignored.");
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        BrandingConfigurationRemote remote = (BrandingConfigurationRemote) Naming.lookup(RMI_HOSTNAME + BrandingConfigurationRemote.RMI_NAME);
        String path = cmd.getOptionValue(PATH_LONG);
        List<String> brands;
        if (path == null || path.length() == 0) {
            brands = remote.reload();
        } else {
            brands = remote.reload(path);
        }
        System.out.println(BRANDINGS_RELOADED_MSG);
        System.out.println(BRANDINGS_AVAILABLE_MSG);
        for (String brand : brands) {
            System.out.println("\t -" + brand);
        }
        return null;
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        // Nothing to check
    }

    @Override
    protected String getFooter() {
        return "The command-line tool to reload the available windows drive clients";
    }

    @Override
    protected String getName() {
        return "reloaddriveclients";
    }
}
