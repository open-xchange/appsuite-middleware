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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.CLIIllegalOptionValueException;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.CLIParseException;
import com.openexchange.admin.console.CLIUnknownOptionException;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;
import com.openexchange.oauth.provider.rmi.client.ClientDto;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagementException;

/**
 * {@link ListOAuthClientManagementCLT}
 *
 * A CLT to list oauth clients
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class ListOAuthClientManagementCLT extends AbstractOAuthCLT {

    private static final String GROUP_CTX_ID_LONG = "context-group-id";
    private static final char GROUP_CTX_ID_SHORT = 'c';

    //create and update options
    private CLIOption ctxGroupID = null;

    public static void main(String[] args) {
        new ListOAuthClientManagementCLT().execute(args);
    }

    private void execute(String[] args) {
        final AdminParser parser = new AdminParser("listoauthclient");
        setOptions(parser);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            parser.ownparse(args);
            final Credentials auth = new Credentials(checkEmpty(this.adminUserOption, (String) parser.getOptionValue(this.adminUserOption)), checkEmpty(this.adminPassOption, (String) parser.getOptionValue(this.adminPassOption)));
            final RemoteClientManagement remote = (RemoteClientManagement) Naming.lookup(RMI_HOSTNAME + RemoteClientManagement.RMI_NAME);

            if (null == remote) {
                System.err.println("Unable to connect to rmi.");
                sysexit(1);
            }

                String contextGroup = (String) parser.getOptionValue(this.ctxGroupID);
                if (null == contextGroup || contextGroup.isEmpty()) {
                    contextGroup = RemoteClientManagement.DEFAULT_GID;
                }

                List<ClientDto> retval = remote.getClients(contextGroup, auth);
                if (null == retval) {
                    System.err.println("Error. Remote returns null!");
                    sysexit(1);
                }

            if (null != parser.getOptionValue(this.csvOutputOption)) {

                doCSVOutput(retval);

            } else {
                System.out.println("Following clients are registered: ");
                for (ClientDto client : retval) {
                    printClient(client);
                }
                }
        } catch (CLIParseException e) {
            printError("Parsing command-line failed : " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (CLIIllegalOptionValueException e) {
            printError("Illegal option value : " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_ILLEGAL_OPTION_VALUE);
        } catch (CLIUnknownOptionException e) {
            printError("Unrecognized options on the command line: " + e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_UNKNOWN_OPTION);
        } catch (MissingOptionException e) {
            printError(e.getMessage(), parser);
            parser.printUsage();
            sysexit(SYSEXIT_MISSING_OPTION);
        } catch (MalformedURLException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (RemoteException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_REMOTE_ERROR);
        } catch (NotBoundException e) {
            printServerException(e, parser);
            sysexit(1);
        } catch (RemoteClientManagementException e) {
            printError(e.getMessage(), parser);
            sysexit(BasicCommandlineOptions.SYSEXIT_COMMUNICATION_ERROR);
        } catch (InvalidCredentialsException e) {
            printServerException(e, parser);
            sysexit(SYSEXIT_INVALID_CREDENTIALS);
        } catch (InvalidDataException e) {
            printError(e.getMessage(), parser);
            sysexit(1);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }

            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                }
            }

        }

    }

    final static String[] OAUTH_CLIENT_COLUMNS = new String[] { "Client Id", "Name", "Enabled", "Description", "Website", "Contact address", "Default scope", "Redirect url's", "Client secret" };

    /**
     * @param retval
     * @throws InvalidDataException
     */
    private void doCSVOutput(List<ClientDto> clients) throws InvalidDataException {

        ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();

        for (ClientDto client : clients) {
            ArrayList<String> list = new ArrayList<String>(OAUTH_CLIENT_COLUMNS.length);
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
     * Checks if an argument string is null or empty
     *
     * @param opt
     * @param str
     * @return the string if not empty or null
     * @throws CLIIllegalOptionValueException
     */
    private String checkEmpty(CLIOption opt, String str) throws CLIIllegalOptionValueException {

        if (null == str || str.isEmpty()) {
            throw new CLIIllegalOptionValueException(opt, str);
        }

        return str;
    }

    private void setOptions(final AdminParser parser) {
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
        System.out.println("Client's current secret = "+client.getSecret());
        System.out.println("-------------------------------------------------------------------------------------");
    }

}

