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

package com.openexchange.admin.console.context.group;

import java.rmi.RemoteException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import com.openexchange.admin.rmi.OXContextGroupInterface;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.cli.AbstractRmiCLI;
import com.openexchange.java.Strings;

/**
 * {@link DeleteContextGroup}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class DeleteContextGroup extends AbstractRmiCLI<Void> {

    /**
     * Entry point
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        new DeleteContextGroup().execute(args);
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        try {
            authenticator.doAuthentication(login, password);
        } catch (RemoteException e) {
            System.err.print(e.getMessage());
            System.exit(-1);
        }
    }

    @Override
    protected void addOptions(Options options) {
        options.addOption(createArgumentOption("g", "context-group-id", "contextGroupId", "The context group identifier", false));
    }

    @Override
    protected Void invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        OXContextGroupInterface oxContexGroup = getRmiStub(OXContextGroupInterface.RMI_NAME);
        oxContexGroup.deleteContextGroup(cmd.getOptionValue('g'));
        return null;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        checkOptions(cmd, null);
    }

    @Override
    protected void checkOptions(CommandLine cmd, Options options) {
        String value = cmd.getOptionValue('g');
        if (Strings.isEmpty(value)) {
            printHelp(options);
            System.exit(1);
        }
    }

    @Override
    protected Boolean requiresAdministrativePermission() {
        return Boolean.TRUE;
    }

    @Override
    protected String getFooter() {
        return "Command line tool for deleting context groups and all data associated to them";
    }

    @Override
    protected String getName() {
        return "deletecontextgroup -g <contextGroupId> " + BASIC_MASTER_ADMIN_USAGE;
    }

}
