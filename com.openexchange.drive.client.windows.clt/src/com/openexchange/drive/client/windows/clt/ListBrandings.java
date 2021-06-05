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
 * {@link ListBrandings}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class ListBrandings extends AbstractRmiCLI<Void> {

    private static final String AVAILABLE_STR = "The following brands are available:";
    private static final String AVAILABLE_AND_VALID_STR = "The following brands are available and valid:";
    private static final String INVALID_STR = "The following brands are invalid:";

    private final static String VALIDATE_LONG = "validate";
    private final static String VALIDATE_SHORT = "v";

    private final static String INVALID_ONLY_LONG = "invalid_only";
    private final static String INVALID_ONLY_SHORT = "o";

    /**
     * @param args
     */
    public static void main(String[] args) {
        new ListBrandings().execute(args);
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(VALIDATE_SHORT, VALIDATE_LONG, false, "If defined, the brandings will be validated and only valid brandings will be returned. This validation verifies if all required files are present.");
        options.addOption(INVALID_ONLY_SHORT, INVALID_ONLY_LONG, false, "Retrieves only invalid brandings.");
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        boolean validate = cmd.hasOption(VALIDATE_LONG);
        boolean invalid_only = cmd.hasOption(INVALID_ONLY_LONG);
        validate = invalid_only ? true : validate;
        BrandingConfigurationRemote remote = (BrandingConfigurationRemote) Naming.lookup(RMI_HOSTNAME + BrandingConfigurationRemote.RMI_NAME);
        List<String> brands = remote.getBrandings(validate, invalid_only);

        if (invalid_only) {
            System.out.println(INVALID_STR);
        } else {
            if (validate) {
                System.out.println(AVAILABLE_AND_VALID_STR);
            } else {
                System.out.println(AVAILABLE_STR);
            }
        }
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
        return "The command-line tool to list the available windows drive clients";
    }

    @Override
    protected String getName() {
        return "listdriveclients [-o | -v] " + BASIC_MASTER_ADMIN_USAGE;
    }
}
