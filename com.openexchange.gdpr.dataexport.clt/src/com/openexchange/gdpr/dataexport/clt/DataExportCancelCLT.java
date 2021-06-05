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

package com.openexchange.gdpr.dataexport.clt;

import java.rmi.RemoteException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.gdpr.dataexport.rmi.DataExportRMIService;
import com.openexchange.java.Strings;

/**
 * {@link DataExportCancelCLT}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DataExportCancelCLT extends AbstractRmiCLI<Void> {

    /**
     * Entry point
     *
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        new DataExportCancelCLT().execute(args);
    }

    private int contextId;
    private int userId;

    /**
     * Initializes a new {@link DataExportCancelCLT}.
     */
    public DataExportCancelCLT() {
        super();
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        // Try context administrator authentication first
        if (contextId > 0) {
            try {
                authenticator.doAuthentication(login, password, contextId);
                return;
            } catch (RemoteException e) {
                if (e.getMessage() == null || Strings.asciiLowerCase(e.getMessage()).indexOf("authentication failed") < 0) {
                    throw e;
                }
                // Context administrator authentication failed. Try with master authentication
            }
        }

        // Master authentication
        authenticator.doAuthentication(login, password);
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("c", "context", "contextId", "The context identifier. If only context identifier is given, all data export tasks associated with denoted context are requested for being canceled", true));
        options.addOption(createArgumentOption("i", "userid", "userId", "The user identifier. If this option is set, only the data export task associated with that user is canceled", false));
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        if (cmd.hasOption("c")) {
            contextId = parseInt('c', -1, cmd, options);
        }
        if (cmd.hasOption("i")) {
            userId = parseInt('i', -1, cmd, options);
        }
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        if (contextId <= 0) {
            throw new MissingOptionException("Please specify a valid context identifier");
        }

        DataExportRMIService rmiService = getRmiStub(optRmiHostName, DataExportRMIService.RMI_NAME);
        if (userId > 0) {
            rmiService.cancelDataExportTask(userId, contextId);
            System.out.println("Requested to cancel data export for user " + userId + " in context " + contextId);
        } else {
            rmiService.cancelDataExportTasks(contextId);
            System.out.println("Requested to cancel data export for context " + contextId);
        }
        return null;
    }

    @Override
    protected String getFooter() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("Command line tool to cancel data exports for a single user or whole context.\n");
        sb.append("\n");
        sb.append("Examples\n");
        sb.append("====================================\n");
        sb.append("Request to cancel all data exports for a certain context:\n");
        sb.append("\n");
        sb.append("  $ canceldataexports -c 1234 -A oxadminmaster -P secret\n");
        sb.append("\n");
        sb.append("\n");
        sb.append("Request to cancel the data export for a certain user\n");
        sb.append("\n");
        sb.append("  $ canceldataexports -c 1234 -i 3 -A oxadminmaster -P secret\n");
        return sb.toString();
    }

    @Override
    protected String getName() {
        return "canceldataexports -c <contextId> [-i <userId>] " + BASIC_CONTEXT_ADMIN_USAGE;
    }

}
