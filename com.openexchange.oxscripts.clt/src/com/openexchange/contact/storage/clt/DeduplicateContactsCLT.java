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

package com.openexchange.contact.storage.clt;

import java.rmi.RemoteException;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.contact.storage.rdb.rmi.ContactStorageRMIService;

/**
 * {@link DeduplicateContactsCLT}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DeduplicateContactsCLT extends AbstractRmiCLI<Void> {

    private static final String FOOTER = "Handle with care and review the found duplicates before executing the de-duplication. Detected duplicates are deleted permanently, with no recovery option.";
    private static final String SYNTAX = "deduplicatecontacts -c <contextId> -f <folderId> [-m <maxNumber>] [-e] " + BASIC_MASTER_ADMIN_USAGE;

    /**
     * Entry point
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new DeduplicateContactsCLT().execute(args);
    }

    // ------------------------------------------------------------------------------------ //

    private int contextId;
    private int folderId;
    private long limit;
    private boolean dryRun;

    /**
     * Initializes a new {@link DeduplicateContactsCLT}.
     */
    private DeduplicateContactsCLT() {
        super();
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        contextId = parseInt('c', -1, cmd, options);
        checkParameterValue(contextId, "context id");

        folderId = parseInt('f', -1, cmd, options);
        checkParameterValue(folderId, "folder id");

        limit = parseInt('m', 1000000, cmd, options);
        checkParameterValue(limit, "limit");

        dryRun = false == cmd.hasOption('e');
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("c", "context", "contextId", "A valid context identifier", true));
        options.addOption(createArgumentOption("f", "folder", "folderId", "A valid contact folder identifier", true));
        options.addOption(createArgumentOption("m", "max", "maxNumber", "The maximum number of contacts to process, or 0 for no limit, defaults to 1000000", false));
        options.addOption(createSwitch("e", "execute", "Actually performs the de-duplication of contacts - by default, identifiers of duplicated contacts are printed out only.", false));
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
        int contextID;
        {
            String optionValue = cmd.getOptionValue('c');
            try {
                contextID = Integer.parseInt(optionValue.trim());
            } catch (NumberFormatException e) {
                System.err.println("Context identifier parameter is not a number: " + optionValue);
                System.exit(1);
                return;
            }
        }
        authenticator.doAuthentication(login, password, contextID);
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        ContactStorageRMIService rmiService = getRmiStub(optRmiHostName, ContactStorageRMIService.RMI_NAME);
        int[] objectIDs = rmiService.deduplicateContacts(contextId, folderId, limit, dryRun);
        if (null == objectIDs) {
            System.out.println("Unexpected result: " + objectIDs);
            System.exit(1);
            return null;
        }
        if (0 == objectIDs.length) {
            System.out.println("No duplicate contacts in folder " + folderId + " of context " + contextId + " found.");
        } else {
            System.out.println("Found " + (dryRun ? "" : "and deleted ") + objectIDs.length + " duplicate contact" + (1 == objectIDs.length ? "" : "s") + " in folder " + folderId + " of context " + contextId + ": " + System.getProperty("line.separator") + Arrays.toString(objectIDs));
        }
        return null;
    }

    /**
     * Checks the specified parameter's value
     * 
     * @param parameterValue The parameter value
     * @param parameterName The parameter name
     */
    private void checkParameterValue(long parameterValue, String parameterName) {
        if (parameterValue > 0) {
            return;
        }
        System.err.println("Invalid '" + parameterName + "' '" + parameterValue + "' was specified.");
        printHelp(options);
        System.exit(1);
    }
}
