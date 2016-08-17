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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.BasicCommandlineOptions;
import com.openexchange.admin.console.CLIIllegalOptionValueException;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.console.CLIParseException;
import com.openexchange.admin.console.CLIUnknownOptionException;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.MissingOptionException;
import com.openexchange.oauth.provider.rmi.client.ClientDataDto;
import com.openexchange.oauth.provider.rmi.client.ClientDto;
import com.openexchange.oauth.provider.rmi.client.IconDto;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagementException;

/**
 * {@link UpdateOAuthClientManagementCLT}
 *
 * A CLT to update an oauth client
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public class UpdateOAuthClientManagementCLT extends AbstractOAuthCLT {


    private static final String GROUP_CTX_ID_LONG = "context-group-id";
    private static final char GROUP_CTX_ID_SHORT = 'x';
    private static final String NAME_LONG = "name";
    private static final char NAME_Short = 'n';
    private static final String DESCRIPTION_LONG = "description";
    private static final char DESCRIPTION_SHORT = 'd';
    private static final String WEBSITE_LONG = "website";
    private static final char WEBSITE_SHORT = 'w';
    private static final String CONTACT_ADRESS_LONG = "contact-address";
    private static final char CONTACT_ADRESS_SHORT = 'o';
    private static final String ICON_PATH_LONG = "icon-path";
    private static final char ICON_PATH_SHORT = 'i';
    private static final String DEFAULT_SCOPE_LONG = "default-scope";
    private static final char DEFAULT_SCOPE_SHORT = 's';
    private static final String REDIRECT_URL_LONG = "urls";
    private static final String CLIENT_ID_LONG = "id";

    private CLIOption clientID = null;

    //create and update options
    private CLIOption ctxGroupID = null;
    private CLIOption name = null;
    private CLIOption description = null;
    private CLIOption website = null;
    private CLIOption contact_address = null;
    private CLIOption icon_path = null;
    private CLIOption default_scope = null;
    private CLIOption redirect_urls = null;

    public static void main(String[] args) {
        new UpdateOAuthClientManagementCLT().execute(args);
    }

    private void execute(String[] args) {
        final AdminParser parser = new AdminParser("updateoauthclient");
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

                String id = checkEmpty(this.clientID, (String) parser.getOptionValue(this.clientID));

                String contextGroup = (String) parser.getOptionValue(this.ctxGroupID);
                if (null == contextGroup || contextGroup.isEmpty()) {
                    contextGroup = RemoteClientManagement.DEFAULT_GID;
                }

                String name = (String) parser.getOptionValue(this.name);
                String desc = (String) parser.getOptionValue(this.description);
                String website = (String) parser.getOptionValue(this.website);
                String contact = (String) parser.getOptionValue(this.contact_address);
                String icon_path = (String) parser.getOptionValue(this.icon_path);
                String scope = (String) parser.getOptionValue(this.default_scope);
                String urlsString = (String) parser.getOptionValue(this.redirect_urls);

                ClientDataDto clientData = new ClientDataDto();
                if (null != name) {
                    clientData.setName(name);
                }
                if (null != desc) {
                    clientData.setDescription(desc);
                }
                if (null != website) {
                    clientData.setWebsite(website);
                }
                if (null != contact) {
                    clientData.setContactAddress(contact);
                }

                if (icon_path != null && !icon_path.isEmpty()) {
                    IconDto icon = new IconDto();
                    Path path = Paths.get(icon_path);
                    fis = new FileInputStream(new File(icon_path));
                    bis = new BufferedInputStream(fis);
                    byte[] byteArray = Files.readAllBytes(path);
                    MimeTypes mimeTypes = TikaConfig.getDefaultConfig().getMimeRepository();
                    MediaType mime = mimeTypes.detect(bis, new Metadata());
                    icon.setMimeType(mime.toString());
                    icon.setData(byteArray);
                    clientData.setIcon(icon);
                }

                if (null != scope) {
                    clientData.setDefaultScope(scope);
                }

                if (urlsString != null && !urlsString.isEmpty()) {
                    List<String> urls = Arrays.asList(urlsString.trim().split("\\s*,\\s*"));
                    clientData.setRedirectURIs(urls);
                }
                ClientDto retval = remote.updateClient(id, clientData, auth);

                if (null == retval) {
                System.out.println("The update of oauth client with id " + id + " has failed!");
                    sysexit(1);
                } else {
                System.out.println("The update of oauth client with id " + id + " was successful!");
                    System.out.println("The updated oauth client: ");
                    printClient(retval);
                    sysexit(0);
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
        } catch (IOException e) {
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

        this.clientID = setLongOpt(parser, CLIENT_ID_LONG, "id", "The id of the oauth client", true, true, false);

        this.ctxGroupID = setShortLongOpt(parser, GROUP_CTX_ID_SHORT, GROUP_CTX_ID_LONG, "cgid", "The id of the context group", false);
        this.name = setShortLongOpt(parser, NAME_Short, NAME_LONG, "name", "Define the name of the oauth client", false);
        this.description = setShortLongOpt(parser, DESCRIPTION_SHORT, DESCRIPTION_LONG, "description", "The description of the oauth client", false);
        this.website = setShortLongOpt(parser, WEBSITE_SHORT, WEBSITE_LONG, "website", "The client website", false);
        this.contact_address = setShortLongOpt(parser, CONTACT_ADRESS_SHORT, CONTACT_ADRESS_LONG, "contact address", "The contact adress of the oauth client", false);
        this.icon_path = setShortLongOpt(parser, ICON_PATH_SHORT, ICON_PATH_LONG, "icon path", "Path to a image file which acts as a icon for the oauth client", false);
        this.default_scope = setShortLongOpt(parser, DEFAULT_SCOPE_SHORT, DEFAULT_SCOPE_LONG, "default scope", "The default scope of the oauth client", false);
        this.redirect_urls = setLongOpt(parser, REDIRECT_URL_LONG, "The redirect urls of the oauth client as a comma separated list", true, false);
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

