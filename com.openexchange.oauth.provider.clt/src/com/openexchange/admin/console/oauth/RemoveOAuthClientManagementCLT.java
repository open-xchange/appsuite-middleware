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

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;

/**
 * {@link RemoveOAuthClientManagementCLT}
 *
 * A CLT to remove an oauth client
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.0
 */
public class RemoveOAuthClientManagementCLT extends AbstractOAuthCLT {

    /**
     * Entry point
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        new RemoveOAuthClientManagementCLT().execute(args);
    }

    /**
     * Executes
     *
     * @param args the command-line arguments
     */
    private void execute(String[] args) {
        final AdminParser parser = new AdminParser("removeoauthclient");
        setOptions(parser);
        RemoteClientManagement remote = getRemoteClientManagement(parser);
        try {
            parser.ownparse(args);
            Credentials auth = getCredentials(this.adminUserOption, this.adminPassOption, parser);
            String id = getClientId(clientID, parser);
            boolean retval = remote.unregisterClient(id, auth);
            System.out.println("The removal of the OAuth client with id " + id + " " + (retval ? "was successful!" : "has failed!"));
            sysexit(retval ? 0 : 1);
        } catch (Exception e) {
            handleException(e, parser);
        }
    }

    /**
     * Sets further options to the specified admin parser
     *
     * @param parser The admin parser
     */
    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);
        setClientIdOption(parser);
    }
}
