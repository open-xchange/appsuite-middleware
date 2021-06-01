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

package com.openexchange.mail.compose.clt;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.mail.compose.rmi.RemoteCompositionSpaceService;

/**
 * {@link DeleteOrphanedReferencesTool}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class DeleteOrphanedReferencesTool extends AbstractRmiCLI<Void> {

    /**
     * Invokes this command-line tool
     *
     * @param args The arguments
     */
    public static void main(String[] args) {
        new DeleteOrphanedReferencesTool().execute(args);
    }

    // --------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link DeleteOrphanedReferencesTool}.
     */
    private DeleteOrphanedReferencesTool() {
        super();
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        Option filestoresOption = createArgumentOption("f", "filestores", "filestores", "Accepts one or more file storage identifiers", true);
        filestoresOption.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(filestoresOption);
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        RemoteCompositionSpaceService rmiService = getRmiStub(optRmiHostName, RemoteCompositionSpaceService.RMI_NAME);

        List<Integer> fileStorageIds;
        {
            String[] values = cmd.getOptionValues("f");
            if (values == null || values.length <= 0) {
                System.err.println("Missing file storage identifiers");
                printHelp();
                System.exit(-1);
                return null; // Keep IDE happy
            }
            fileStorageIds = new ArrayList<Integer>(values.length);
            for (String id : values) {
                try {
                    fileStorageIds.add(Integer.valueOf(id));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid file storage identifier: " + id);
                    printHelp();
                    System.exit(-1);
                }
            }
        }

        rmiService.deleteOrphanedReferences(fileStorageIds);
        System.out.println("Orphaned references successfully deleted");
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
        return "The command-line tool for deleting orphaned references from mail compose for specified file storage identifiers";
    }

    @Override
    protected String getName() {
        return "deleteorphanedattachments " + BASIC_MASTER_ADMIN_USAGE;
    }

}
