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

package com.openexchange.file.storage.clt;

import java.rmi.RemoteException;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.groupware.infostore.rmi.FileChecksumsRMIService;

/**
 * {@link FileChecksumsCLT}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class FileChecksumsCLT extends AbstractRmiCLI<Void> {

    private static final String SYNTAX = "calculatefilechecksums [-d <databaseId> | -c <contextId>] [-C] " + BASIC_MASTER_ADMIN_USAGE;
    private static final String FOOTER = "Command-line tool to calculate missing file checksums";

    public static void main(String[] args) {
        new FileChecksumsCLT().execute(args);
    }

    /**
     * Initializes a new {@link FileChecksumsCLT}.
     */
    public FileChecksumsCLT() {
        super();
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("c", "context", "contextId", "The identifier of the context to determine/calculate missing checksums in", false));
        options.addOption(createArgumentOption("d", "database", "databaseId", "The database pool identifier to determine/calculate missing checksums in", false));
        options.addOption(createSwitch("C", "calculate", "Calculate and store missing checksums (if not specified, files with missing checksums are printed out only)", false));
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        checkOptions(cmd, options);
    }

    @Override
    protected void checkOptions(CommandLine cmd, Options options) {
        if ((false == cmd.hasOption('c') && false == cmd.hasOption('d')) || (cmd.hasOption('c') && cmd.hasOption('d'))) {
            System.out.println("You must either provide a context or database identifier.");
            if (null != options) {
                printHelp(options);
            }
            System.exit(-1);
            return;
        }
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    @Override
    protected String getFooter() {
        return FOOTER;
    }

    @Override
    protected String getName() {
        return SYNTAX;
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        if (cmd.hasOption('c')) {
            authenticator.doAuthentication(login, password, parseInt('c', 0, cmd, options));
        } else {
            authenticator.doAuthentication(login, password);
        }
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        FileChecksumsRMIService rmiService = getRmiStub(optRmiHostName, FileChecksumsRMIService.RMI_NAME);
        int contextId = parseInt('c', 0, cmd, options);
        int databaseId = parseInt('d', 0, cmd, options);
        boolean calculate = cmd.hasOption('C');
        List<String> result;
        if (calculate) {
            if (0 < contextId) {
                result = rmiService.calculateMissingChecksumsInContext(contextId);
            } else if (0 < databaseId) {
                result = rmiService.calculateMissingChecksumsInDatabase(databaseId);
            } else {
                checkOptions(cmd, options);
                return null;
            }
        } else {
            if (0 < contextId) {
                result = rmiService.listFilesWithoutChecksumInContext(contextId);
            } else if (0 < databaseId) {
                result = rmiService.listFilesWithoutChecksumInDatabase(databaseId);
            } else {
                checkOptions(cmd, options);
                return null;
            }
        }
        if (result.isEmpty()) {
            System.out.println("No files with missing checksums found.");
        } else {
            if (calculate) {
                System.out.println("Missing file checksums calculated for " + result.size() + " files:" + System.lineSeparator());
            } else {
                System.out.println("Missing file checksums detected for " + result.size() + " files:" + System.lineSeparator());
            }
            for (String item : result) {
                System.out.println("  " + item);
            }
        }
        return null;
    }
}
