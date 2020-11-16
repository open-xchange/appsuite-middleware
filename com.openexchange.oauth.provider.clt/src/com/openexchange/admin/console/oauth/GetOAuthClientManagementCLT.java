/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.admin.console.oauth;

import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.oauth.provider.rmi.client.ClientDto;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;

/**
 * {@link GetOAuthClientManagementCLT}
 *
 * A CLT to get an oauth client
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.0
 */
public class GetOAuthClientManagementCLT extends AbstractOAuthCLT {

    /**
     * Entry point
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        new GetOAuthClientManagementCLT().execute(args);
    }

    /**
     * Executes
     *
     * @param args the command-line arguments
     */
    private void execute(String[] args) {
        final AdminParser parser = new AdminParser("getoauthclient");
        setOptions(parser);
        RemoteClientManagement remote = getRemoteClientManagement(parser);
        try {
            parser.ownparse(args);
            Credentials auth = getCredentials(this.adminUserOption, this.adminPassOption, parser);
            String id = getClientId(clientID, parser);
            ClientDto client = remote.getClientById(id, auth);
            nullCheck(client, "Client not found!");
            printClient(client);
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
