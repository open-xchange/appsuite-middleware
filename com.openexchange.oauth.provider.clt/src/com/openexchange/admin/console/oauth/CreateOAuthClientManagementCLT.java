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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;
import com.openexchange.admin.console.AdminParser;
import com.openexchange.admin.console.CLIOption;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.oauth.provider.rmi.client.ClientDataDto;
import com.openexchange.oauth.provider.rmi.client.ClientDto;
import com.openexchange.oauth.provider.rmi.client.IconDto;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;

/**
 * {@link CreateOAuthClientManagementCLT}
 *
 * A CLT to register an oauth client
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.8.0
 */
public class CreateOAuthClientManagementCLT extends AbstractOAuthCLT {

    private static final String GROUP_CTX_ID_LONG = "context-group-id";
    private static final char GROUP_CTX_ID_SHORT = 'c';
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

    //create and update options
    private CLIOption ctxGroupID = null;
    private CLIOption name = null;
    private CLIOption description = null;
    private CLIOption website = null;
    private CLIOption contact_address = null;
    private CLIOption icon_path = null;
    private CLIOption default_scope = null;
    private CLIOption redirect_urls = null;

    /**
     * Entry point
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        new CreateOAuthClientManagementCLT().execute(args);
    }

    /**
     * Executes
     *
     * @param args the command-line arguments
     */
    private void execute(String[] args) {
        AdminParser parser = new AdminParser("createoauthclient");
        setOptions(parser);

        RemoteClientManagement remote = getRemoteClientManagement(parser);

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            parser.ownparse(args);

            Credentials auth = getCredentials(this.adminUserOption, this.adminPassOption, parser);

            String contextGroup = getContextGroup(this.ctxGroupID, parser);
            String name = checkEmpty(this.name, String.class.cast(parser.getOptionValue(this.name)));
            String desc = checkEmpty(this.description, String.class.cast(parser.getOptionValue(this.description)));
            String website = checkEmpty(this.website, String.class.cast(parser.getOptionValue(this.website)));
            String contact = checkEmpty(this.contact_address, String.class.cast(parser.getOptionValue(this.contact_address)));
            String icon_path = checkEmpty(this.icon_path, String.class.cast(parser.getOptionValue(this.icon_path)));
            String scope = checkEmpty(this.default_scope, String.class.cast(parser.getOptionValue(this.default_scope)));
            String urlsString = checkEmpty(this.redirect_urls, String.class.cast(parser.getOptionValue(this.redirect_urls)));

            ClientDataDto clientData = new ClientDataDto();
            clientData.setName(name);
            clientData.setDescription(desc);
            clientData.setWebsite(website);
            clientData.setContactAddress(contact);

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

            clientData.setDefaultScope(scope);

            List<String> urls = Arrays.asList(urlsString.trim().split("\\s*,\\s*"));
            clientData.setRedirectURIs(urls);
            ClientDto retval = remote.registerClient(contextGroup, clientData, auth);
            nullCheck(retval, "The registration of oauth client has failed");

            System.out.println("The registration of oauth client was successful");
            System.out.println("The registered oauth client: ");
            printClient(retval);
            sysexit(0);
        } catch (Exception e) {
            handleException(e, parser);
        } finally {
            closeQuietly(fis);
            closeQuietly(bis);
        }
    }

    /**
     * Sets further options to the specified admin parser
     *
     * @param parser The admin parser
     */
    private void setOptions(AdminParser parser) {
        setDefaultCommandLineOptionsWithoutContextID(parser);

        this.ctxGroupID = setShortLongOpt(parser, GROUP_CTX_ID_SHORT, GROUP_CTX_ID_LONG, "cgid", "The id of the context group", true);
        this.name = setShortLongOpt(parser, NAME_Short, NAME_LONG, "name", "Define the name of the oauth client", true);
        this.description = setShortLongOpt(parser, DESCRIPTION_SHORT, DESCRIPTION_LONG, "description", "The description of the oauth client", true);
        this.website = setShortLongOpt(parser, WEBSITE_SHORT, WEBSITE_LONG, "website", "The client website", true);
        this.contact_address = setShortLongOpt(parser, CONTACT_ADRESS_SHORT, CONTACT_ADRESS_LONG, "contact address", "The contact adress of the oauth client", true);
        this.icon_path = setShortLongOpt(parser, ICON_PATH_SHORT, ICON_PATH_LONG, "icon path", "Path to a image file which acts as a icon for the oauth client", true);
        this.default_scope = setShortLongOpt(parser, DEFAULT_SCOPE_SHORT, DEFAULT_SCOPE_LONG, "default scope", "The default scope of the oauth client", true);
        this.redirect_urls = setLongOpt(parser, REDIRECT_URL_LONG, "The redirect urls of the oauth client as a comma separated list", true, true);
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
