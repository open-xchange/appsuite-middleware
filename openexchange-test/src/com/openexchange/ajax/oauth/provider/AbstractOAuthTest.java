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

package com.openexchange.ajax.oauth.provider;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.rmi.client.ClientDto;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.oauth.provider.rmi.client.ClientDataDto;
import com.openexchange.oauth.provider.rmi.client.IconDto;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;
import com.openexchange.tasks.json.TaskActionFactory;

/**
 * {@link AbstractOAuthTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class AbstractOAuthTest {

    protected ClientDto clientApp;

    protected OAuthClient client;

    protected AJAXClient ajaxClient;

    protected Scope scope;

    protected AbstractOAuthTest(Scope scope) throws OXException {
        super();
        AJAXConfig.init();
        this.scope = scope;
    }

    @Before
    public void before() throws Exception {
        // register client application
        clientApp = registerTestClient();
        if (scope == null) {
            scope = Scope.parseScope(clientApp.getDefaultScope());
        }
        client = new OAuthClient(User.User1, clientApp.getId(), clientApp.getSecret(), clientApp.getRedirectURIs().get(0), scope);
        ajaxClient = new AJAXClient(User.User1);
    }

    @After
    public void after() throws Exception {
        ajaxClient.logout();
        client.logout();
        unregisterTestClient(clientApp);
    }

    public static ClientDto registerTestClient() throws Exception {
        ClientDataDto clientData = prepareClient("Test App " + System.currentTimeMillis());
        RemoteClientManagement clientManagement = (RemoteClientManagement) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + RemoteClientManagement.RMI_NAME);
        return clientManagement.registerClient(RemoteClientManagement.DEFAULT_GID, clientData, getMasterAdminCredentials());
    }

    public static ClientDataDto prepareClient(String name) {
        IconDto icon = new IconDto();
        icon.setData(IconBytes.DATA);
        icon.setMimeType("image/jpg");

        List<String> redirectURIs = new ArrayList<>(2);
        redirectURIs.add("http://localhost");
        redirectURIs.add("http://localhost:8080");

        ClientDataDto clientData = new ClientDataDto();
        clientData.setName(name);
        clientData.setDescription(name);
        clientData.setIcon(icon);
        clientData.setContactAddress("webmaster@example.com");
        clientData.setWebsite("http://www.example.com");
        clientData.setDefaultScope(Scope.newInstance(ContactActionFactory.OAUTH_READ_SCOPE, ContactActionFactory.OAUTH_WRITE_SCOPE, AppointmentActionFactory.OAUTH_READ_SCOPE, AppointmentActionFactory.OAUTH_WRITE_SCOPE, TaskActionFactory.OAUTH_READ_SCOPE, TaskActionFactory.OAUTH_WRITE_SCOPE).toString());
        clientData.setRedirectURIs(redirectURIs);
        return clientData;
    }

    public static Credentials getMasterAdminCredentials() {
        String username = AJAXConfig.getProperty(AJAXConfig.Property.OX_ADMIN_MASTER);
        String password = AJAXConfig.getProperty(AJAXConfig.Property.OX_ADMIN_MASTER_PWD);
        return new Credentials(username, password);
    }

    public static void unregisterTestClient(ClientDto oAuthClientApp) throws Exception {
        RemoteClientManagement clientManagement = (RemoteClientManagement) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + RemoteClientManagement.RMI_NAME);
        clientManagement.unregisterClient(oAuthClientApp.getId(), getMasterAdminCredentials());
    }

}
