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

package com.openexchange.ajax.oauth.provider;

import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.Before;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.ajax.framework.AbstractSmtpAJAXSession;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.chronos.json.oauth.ChronosOAuthScope;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.oauth.provider.rmi.client.ClientDataDto;
import com.openexchange.oauth.provider.rmi.client.ClientDto;
import com.openexchange.oauth.provider.rmi.client.IconDto;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;
import com.openexchange.tasks.json.TaskActionFactory;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.configuration.AJAXConfig.Property;
import com.openexchange.test.common.test.pool.TestContextPool;
import com.openexchange.test.common.test.pool.TestUser;

/**
 * {@link AbstractOAuthTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class AbstractOAuthTest extends AbstractSmtpAJAXSession {

    protected ClientDto clientApp;

    protected OAuthClient oAuthClient;

    protected Scope scope;

    protected AbstractOAuthTest(Scope scope) {
        super();
        this.scope = scope;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // register client application
        clientApp = registerTestClient();
        if (scope == null) {
            scope = Scope.parseScope(clientApp.getDefaultScope());
        }
        oAuthClient = new OAuthClient(testUser, clientApp.getId(), clientApp.getSecret(), clientApp.getRedirectURIs().get(0), scope);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        try {
            OAuthClient oAuthClient = this.oAuthClient;
            if (null != oAuthClient) {
                oAuthClient.logout();
            }
            unregisterTestClient(clientApp);
        } finally {
            super.tearDown();
        }
    }

    public static ClientDto registerTestClient() throws Exception {
        ClientDataDto clientData = prepareClient("Test App " + UUID.randomUUID().toString());
        RemoteClientManagement clientManagement = (RemoteClientManagement) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMIHOST) + ":1099/" + RemoteClientManagement.RMI_NAME);
        return clientManagement.registerClient(RemoteClientManagement.DEFAULT_GID, clientData, getMasterAdminCredentials());
    }

    @SuppressWarnings("deprecation")
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
        clientData.setDefaultScope(Scope.newInstance(ContactActionFactory.OAUTH_READ_SCOPE, ContactActionFactory.OAUTH_WRITE_SCOPE, AppointmentActionFactory.OAUTH_READ_SCOPE, AppointmentActionFactory.OAUTH_WRITE_SCOPE, ChronosOAuthScope.OAUTH_WRITE_SCOPE, ChronosOAuthScope.OAUTH_READ_SCOPE, TaskActionFactory.OAUTH_READ_SCOPE, TaskActionFactory.OAUTH_WRITE_SCOPE).toString());
        clientData.setRedirectURIs(redirectURIs);
        return clientData;
    }

    public static Credentials getMasterAdminCredentials() {
        TestUser oxAdminMaster = TestContextPool.getOxAdminMaster();
        return new Credentials(oxAdminMaster.getUser(), oxAdminMaster.getPassword());
    }

    public static void unregisterTestClient(ClientDto oAuthClientApp) throws Exception {
        RemoteClientManagement clientManagement = (RemoteClientManagement) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMIHOST) + ":1099/" + RemoteClientManagement.RMI_NAME);
        clientManagement.unregisterClient(oAuthClientApp.getId(), getMasterAdminCredentials());
    }

}
