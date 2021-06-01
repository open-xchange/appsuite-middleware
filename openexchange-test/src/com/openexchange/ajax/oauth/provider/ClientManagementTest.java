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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.ajax.framework.AbstractTestEnvironment;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.chronos.json.oauth.ChronosOAuthScope;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException.Reason;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.oauth.provider.rmi.client.ClientDataDto;
import com.openexchange.oauth.provider.rmi.client.ClientDto;
import com.openexchange.oauth.provider.rmi.client.IconDto;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagementException;
import com.openexchange.tasks.json.TaskActionFactory;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.configuration.AJAXConfig.Property;

/**
 * {@link ClientManagementTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ClientManagementTest extends AbstractTestEnvironment {

    private RemoteClientManagement clientManagement;

    private Credentials credentials;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() throws Exception {
        clientManagement = (RemoteClientManagement) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMIHOST) + ":1099/" + RemoteClientManagement.RMI_NAME);
        credentials = AbstractOAuthTest.getMasterAdminCredentials();
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testUpdatePermutations() throws Exception {
        ClientDataDto clientData = prepareClient(ClientManagementTest.class.getSimpleName() + "_" + System.currentTimeMillis());
        ClientDto client = clientManagement.registerClient(ClientManagement.DEFAULT_GID, clientData, credentials);
        compare(clientData, client);

        // name
        clientData = new ClientDataDto();
        clientData.setName(Strings.reverse(client.getName()));
        assertTrue(clientData.containsName());
        client = clientManagement.updateClient(client.getId(), clientData, credentials);
        assertEquals(clientData.getName(), client.getName());

        // description
        clientData = new ClientDataDto();
        clientData.setDescription(Strings.reverse(client.getDescription()));
        assertTrue(clientData.containsDescription());
        client = clientManagement.updateClient(client.getId(), clientData, credentials);
        assertEquals(clientData.getDescription(), client.getDescription());

        // website
        clientData = new ClientDataDto();
        clientData.setWebsite(Strings.reverse(client.getWebsite()));
        assertTrue(clientData.containsWebsite());
        client = clientManagement.updateClient(client.getId(), clientData, credentials);
        assertEquals(clientData.getWebsite(), client.getWebsite());

        // contact address
        clientData = new ClientDataDto();
        clientData.setContactAddress(Strings.reverse(client.getContactAddress()));
        assertTrue(clientData.containsContactAddress());
        client = clientManagement.updateClient(client.getId(), clientData, credentials);
        assertEquals(clientData.getContactAddress(), client.getContactAddress());

        // default scope
        clientData = new ClientDataDto();
        clientData.setDefaultScope(TaskActionFactory.OAUTH_READ_SCOPE + " " + TaskActionFactory.OAUTH_WRITE_SCOPE);
        assertTrue(clientData.containsDefaultScope());
        client = clientManagement.updateClient(client.getId(), clientData, credentials);
        assertEquals(clientData.getDefaultScope(), client.getDefaultScope());

        // redirect URIs
        clientData = new ClientDataDto();
        clientData.setRedirectURIs(Collections.singletonList("http://[::1]/some/where"));
        assertTrue(clientData.containsRedirectURIs());
        client = clientManagement.updateClient(client.getId(), clientData, credentials);
        assertEquals(clientData.getRedirectURIs(), client.getRedirectURIs());
    }

    @Test
    public void testInvalidRedirectURIOnRegister() throws Exception {
        String invalidURI = "http://oauth.example.com/api/callback"; // HTTPS must be enforced for non-localhost URIs
        thrown.expect(new CMEMatcher(invalidURI));
        ClientDataDto clientData = prepareClient(ClientManagementTest.class.getSimpleName() + "_" + System.currentTimeMillis());
        clientData.setRedirectURIs(Collections.singletonList(invalidURI));
        clientManagement.registerClient(ClientManagement.DEFAULT_GID, clientData, credentials);
    }

    @Test
    public void testInvalidRedirectURIOnUpdate() throws Exception {
        ClientDto client = clientManagement.registerClient(ClientManagement.DEFAULT_GID, prepareClient(ClientManagementTest.class.getSimpleName() + "_" + System.currentTimeMillis()), credentials);
        String invalidURI = "http://oauth.example.com/api/callback"; // HTTPS must be enforced for non-localhost URIs
        thrown.expect(new CMEMatcher(invalidURI));
        ClientDataDto clientData = new ClientDataDto();
        clientData.setRedirectURIs(Collections.singletonList(invalidURI));
        clientManagement.updateClient(client.getId(), clientData, credentials);
    }

    @Test
    public void testInvalidIconMimeTypeOnRegister() throws Exception {
        String invalidMimeType = "image/gif"; // Only png and jpg are allowed
        thrown.expect(new CMEMatcher(invalidMimeType));
        ClientDataDto clientData = prepareClient(ClientManagementTest.class.getSimpleName() + "_" + System.currentTimeMillis());
        IconDto icon = new IconDto();
        icon.setData(IconBytes.DATA);
        icon.setMimeType(invalidMimeType);
        clientData.setIcon(icon);
        clientManagement.registerClient(ClientManagement.DEFAULT_GID, clientData, credentials);
    }

    @Test
    public void testInvalidIconMimeTypeOnUpdate() throws Exception {
        ClientDto client = clientManagement.registerClient(ClientManagement.DEFAULT_GID, prepareClient(ClientManagementTest.class.getSimpleName() + "_" + System.currentTimeMillis()), credentials);
        String invalidMimeType = "image/gif"; // Only png and jpg are allowed
        thrown.expect(new CMEMatcher(invalidMimeType));
        ClientDataDto clientData = new ClientDataDto();
        IconDto icon = new IconDto();
        icon.setData(IconBytes.DATA);
        icon.setMimeType(invalidMimeType);
        clientData.setIcon(icon);
        clientManagement.updateClient(client.getId(), clientData, credentials);
    }

    @Test
    public void testIconTooLargeOnRegister() throws Exception {
        int maxSize = 0x40000; // Max 256kb
        thrown.expect(new CMEMatcher(Integer.toString(maxSize)));
        ClientDataDto clientData = prepareClient(ClientManagementTest.class.getSimpleName() + "_" + System.currentTimeMillis());
        IconDto icon = new IconDto();
        byte[] data = new byte[maxSize + 1];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 255);
        }
        icon.setData(data);
        icon.setMimeType("image/png");
        clientData.setIcon(icon);
        clientManagement.registerClient(ClientManagement.DEFAULT_GID, clientData, credentials);
    }

    @Test
    public void testIconTooLargeOnUpdate() throws Exception {
        ClientDto client = clientManagement.registerClient(ClientManagement.DEFAULT_GID, prepareClient(ClientManagementTest.class.getSimpleName() + "_" + System.currentTimeMillis()), credentials);
        int maxSize = 0x40000; // Max 256kb
        thrown.expect(new CMEMatcher(Integer.toString(maxSize)));
        ClientDataDto clientData = new ClientDataDto();
        IconDto icon = new IconDto();
        byte[] data = new byte[maxSize + 1];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 255);
        }
        icon.setData(data);
        icon.setMimeType("image/png");
        clientData.setIcon(icon);
        clientManagement.updateClient(client.getId(), clientData, credentials);
    }

    @Test
    public void testInvalidScopeOnRegister() throws Exception {
        String invalidScope = "doSomething";
        thrown.expect(new CMEMatcher(invalidScope));
        ClientDataDto clientData = prepareClient(ClientManagementTest.class.getSimpleName() + "_" + System.currentTimeMillis());
        clientData.setDefaultScope(invalidScope);
        clientManagement.registerClient(ClientManagement.DEFAULT_GID, clientData, credentials);
    }

    @Test
    public void testInvalidScopeOnUpdate() throws Exception {
        ClientDto client = clientManagement.registerClient(ClientManagement.DEFAULT_GID, prepareClient(ClientManagementTest.class.getSimpleName() + "_" + System.currentTimeMillis()), credentials);
        String invalidScope = "doSomething";
        thrown.expect(new CMEMatcher(invalidScope));
        ClientDataDto clientData = new ClientDataDto();
        clientData.setDefaultScope(invalidScope);
        clientManagement.updateClient(client.getId(), clientData, credentials);
    }

    private static void compare(ClientDataDto clientData, ClientDto client) {
        assertNotNull(client.getId());
        assertEquals(clientData.getName(), client.getName());
        assertEquals(clientData.getContactAddress(), client.getContactAddress());
        assertEquals(clientData.getDescription(), client.getDescription());
        assertEquals(clientData.getWebsite(), client.getWebsite());
        assertEquals(clientData.getDefaultScope(), client.getDefaultScope().toString());
        assertEquals(new HashSet<>(clientData.getRedirectURIs()), new HashSet<>(client.getRedirectURIs()));
        assertArrayEquals(clientData.getIcon().getData(), client.getIcon().getData());
        assertTrue(client.getRegistrationDate() > 0);
        assertNotNull(client.getSecret());
        assertTrue(client.isEnabled());
    }

    private static final class CMEMatcher extends TypeSafeMatcher<RemoteClientManagementException> {

        private final String invalidValue;

        CMEMatcher(String invalidValue) {
            super();
            this.invalidValue = invalidValue;
        }

        @Override
        protected boolean matchesSafely(RemoteClientManagementException e) {
            String message = e.getMessage();
            return message.contains("Invalid client data") && message.contains(invalidValue);
        }

        @Override
        public void describeTo(Description d) {
            d.appendText(new com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException(Reason.INVALID_CLIENT_DATA, invalidValue).getMessage());
        }

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

}
