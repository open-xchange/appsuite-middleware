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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.oauth.provider.clt;

import java.rmi.RemoteException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import com.openexchange.auth.rmi.RemoteAuthenticator;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.rmi.OAuthClientRmi;

/**
 * {@link GetOAuthClientClt}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class GetOAuthClientClt extends AbstractOAuthProviderClt<Client> {

    /**
     * The main method
     *
     * @param args The arguments
     */
    public static void main(String[] args) {
        Client client = new GetOAuthClientClt().execute(args);

        StringBuilder builder = new StringBuilder(512);
        if (client.getId() != null) {
            builder.append("id").append(client.getId()).append(", ");
        }
        if (client.getSecret() != null) {
            builder.append("secret=").append(client.getSecret()).append(", ");
        }
        if (client.getName() != null) {
            builder.append("name=").append(client.getName()).append(", ");
        }
        if (client.getDescription() != null) {
            builder.append("description=").append(client.getDescription()).append(", ");
        }
        if (client.getWebsite() != null) {
            builder.append("website=").append(client.getWebsite()).append(", ");
        }
        if (client.getContactAddress() != null) {
            builder.append("contact-address=").append(client.getContactAddress()).append(", ");
        }
        if (client.getRedirectURIs() != null) {
            builder.append("redirect-URIs=").append(client.getRedirectURIs()).append(", ");
        }
        if (client.getDefaultScope() != null) {
            builder.append("default-scope=").append(client.getDefaultScope()).append(", ");
        }
        if (client.getRegistrationDate() != null) {
            builder.append("registration-date=").append(client.getRegistrationDate()).append(", ");
        }
        builder.append("enabled=").append(client.isEnabled());

        System.out.println(builder.toString());
    }

    // -----------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link GetOAuthClientClt}.
     */
    private GetOAuthClientClt() {
        super();
    }

    @Override
    protected void administrativeAuth(String login, String password, CommandLine cmd, RemoteAuthenticator authenticator) throws RemoteException {
        try {
            authenticator.doAuthentication(login, password);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    @Override
    protected void addOptions(Options options) {
        Option option = new Option("i", "client", true, "The identifier for the OAuth Client");
        option.setRequired(true);
        options.addOption(option);
    }

    @Override
    protected Client invoke(Options options, CommandLine cmd, String optRmiHostName) throws Exception {
        OAuthClientRmi oAuthClientRmiStub = getRmiStub(optRmiHostName, OAuthClientRmi.RMI_NAME);

        Client client = oAuthClientRmiStub.getClientById(cmd.getOptionValue('i'));
        if (null == client) {
            System.err.println("No such OAuth Client registered.");
            printHelp(options);
            System.exit(1);
        }

        return client;
    }

    @Override
    protected void checkOptions(CommandLine cmd) {
        checkOptions(cmd, null);
    }

    @Override
    protected void checkOptions(CommandLine cmd, Options options) {
        String value = cmd.getOptionValue('i');
        if (Strings.isEmpty(value)) {
            System.err.println("Invalid client identifier");
            if (null != options) {
                printHelp(options);
            }
            System.exit(1);
        }
    }

    @Override
    protected boolean requiresAdministrativePermission() {
        return true;
    }

    @Override
    protected String getFooter() {
        return "The command-line interface to retrieve an existing OAuth Client.";
    }

    @Override
    protected String getName() {
        return "getoauthclient";
    }

}
