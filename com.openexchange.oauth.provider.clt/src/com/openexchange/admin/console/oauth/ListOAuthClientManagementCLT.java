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

package com.openexchange.admin.console.oauth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.oauth.provider.rmi.client.ClientDto;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;

/**
 * {@link ListOAuthClientManagementCLT}
 *
 * A CLT to list oauth clients
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.0
 */
public class ListOAuthClientManagementCLT extends AbstractOAuthCLT {

    private static final String GROUP_CTX_ID_LONG = "context-group-id";
    private static final char GROUP_CTX_ID_SHORT = 'c';

    //create and update options
    private CLIOption ctxGroupID = null;

    /**
     * Entry point
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        new ListOAuthClientManagementCLT().execute(args);
    }

    /**
     * Executes
     *
     * @param args the command-line arguments
     */
    private void execute(String[] args) {
        AdminParser parser = new AdminParser("listoauthclient");
        setOptions(parser);
        RemoteClientManagement remote = getRemoteClientManagement(parser);
        try {
            parser.ownparse(args);
            Credentials auth = getCredentials(this.adminUserOption, this.adminPassOption, parser);

            String contextGroup = getContextGroup(this.ctxGroupID, parser);

            List<ClientDto> retval = remote.getClients(contextGroup, auth);
            nullCheck(retval, "Error. Remote returns null!");

            if (null != parser.getOptionValue(this.csvOutputOption)) {
                doCSVOutput(retval);
                return;
            }

            System.out.println("Following clients are registered: ");
            for (ClientDto client : retval) {
                printClient(client);
            }
        } catch (Exception e) {
            handleException(e, parser);
        }
    }

    final static String[] OAUTH_CLIENT_COLUMNS = new String[] { "Client Id", "Name", "Enabled", "Description", "Website", "Contact address", "Default scope", "Redirect url's", "Client secret" };

    /**
     * @param retval
     * @throws InvalidDataException
     */
    private void doCSVOutput(List<ClientDto> clients) throws InvalidDataException {
        ArrayList<ArrayList<String>> data = new ArrayList<>();

        for (ClientDto client : clients) {
            ArrayList<String> list = new ArrayList<>(OAUTH_CLIENT_COLUMNS.length);
            list.add(client.getId());
            list.add(client.getName());
            list.add(Boolean.toString(client.isEnabled()));
            list.add(client.getDescription());
            list.add(client.getWebsite());
            list.add(client.getContactAddress());
            list.add(client.getDefaultScope());
            StringBuilder urls = new StringBuilder();
            for (String str : client.getRedirectURIs()) {
                urls.append(str).append(", ");
            }
            list.add(urls.toString());
            list.add(client.getSecret());
            data.add(list);
        }

        this.doCSVOutput(Arrays.asList(OAUTH_CLIENT_COLUMNS), data);

    }

    /**
     * Sets further options to the specified admin parser
     *
     * @param parser The admin parser
     */
    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setCSVOutputOption(parser);
        this.ctxGroupID = setShortLongOpt(parser, GROUP_CTX_ID_SHORT, GROUP_CTX_ID_LONG, "cgid", "The id of the context group", true);
    }

    /**
     * Prints the information of a ClientDto to system.out
     *
     * @param client
     */
    private void printClient(ClientDto client) {
        System.out.println("Client_ID = " + client.getId());
        System.out.println("Name = " + client.getName());
        System.out.println("Enabled = " + client.isEnabled());
        System.out.println("Description = " + client.getDescription());
        System.out.println("Website = " + client.getWebsite());
        System.out.println("Contact address = " + client.getContactAddress());
        System.out.println("Default scope = " + client.getDefaultScope());
        System.out.println("Redirect URL's = " + client.getRedirectURIs());
        System.out.println("Client's current secret = " + client.getSecret());
        System.out.println("-------------------------------------------------------------------------------------");
    }

}
