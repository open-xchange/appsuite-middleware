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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.rmi.client.ClientDto;
import com.openexchange.oauth.provider.rmi.client.ClientDataDto;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagementException;
import com.openexchange.oauth.provider.rmi.client.IconDto;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagementException.Reason;
import com.openexchange.oauth.provider.impl.tools.ClientId;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;
import com.openexchange.tasks.json.TaskActionFactory;


/**
 * {@link ClientManagementTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ClientManagementTest {

    private RemoteClientManagement clientManagement;

    private Credentials credentials;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeClass
    public static void initTestFramework() throws Exception {
        AJAXConfig.init();
    }

    @Before
    public void before() throws Exception {
        clientManagement = (RemoteClientManagement) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + RemoteClientManagement.RMI_NAME);
        credentials = AbstractOAuthTest.getMasterAdminCredentials();
    }

    @Test
    public void testClientLifecycle() throws Exception {
        /*
         * Create client and check data transmission
         */
        ClientDataDto clientData = prepareClient(ClientManagementTest.class.getSimpleName() + "_" + System.currentTimeMillis());
        ClientDto client = clientManagement.registerClient(ClientManagement.DEFAULT_GID, clientData, credentials);
        String groupId = ClientId.parse(client.getId()).getGroupId();
        try {
            compare(clientData, client);

            /*
             * Assure client is listed and can be got
             */
            List<ClientDto> clients = clientManagement.getClients(groupId, credentials);
            boolean found = false;
            for (ClientDto c : clients) {
                if (client.getId().equals(c.getId())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);

            ClientDto reloaded = clientManagement.getClientById(client.getId(), credentials);
            compare(client, reloaded);

            /*
             * Check disabling and enabling
             */
            assertTrue(clientManagement.disableClient(client.getId(), credentials));
            reloaded = clientManagement.getClientById(client.getId(), credentials);
            assertFalse(reloaded.isEnabled());
            assertFalse(clientManagement.disableClient(client.getId(), credentials)); // cannot disable a disabled client

            assertTrue(clientManagement.enableClient(client.getId(), credentials));
            reloaded = clientManagement.getClientById(client.getId(), credentials);
            assertTrue(reloaded.isEnabled());
            assertFalse(clientManagement.enableClient(client.getId(), credentials)); // cannot enable an enabled client

            /*
             * Revoke secret
             */
            String oldSecret = client.getSecret();
            client = clientManagement.revokeClientSecret(client.getId(), credentials);
            assertNotNull(client.getSecret());
            assertNotEquals(oldSecret, client.getSecret());

            /*
             * Update
             */
            ClientDataDto updatedClientData = new ClientDataDto();
            updatedClientData.setName(Strings.reverse(client.getName()));
            updatedClientData.setContactAddress(Strings.reverse(client.getContactAddress()));
            updatedClientData.setDescription(Strings.reverse(client.getDescription()));
            updatedClientData.setWebsite(Strings.reverse(client.getWebsite()));
            IconDto updatedIcon = new IconDto();
            updatedIcon.setMimeType("image/png");
            byte[] originalIconBytes = client.getIcon().getData();
            byte[] updatedIconBytes = new byte[originalIconBytes.length];
            System.arraycopy(originalIconBytes, 0, updatedIconBytes, 0, originalIconBytes.length);
            Arrays.sort(updatedIconBytes);
            updatedIcon.setData(updatedIconBytes);
            updatedClientData.setIcon(updatedIcon);
            updatedClientData.setRedirectURIs(Collections.singletonList("https://example.com/oauth/client/endpoint"));
            updatedClientData.setDefaultScope(Scope.newInstance(ContactActionFactory.OAUTH_READ_SCOPE, ContactActionFactory.OAUTH_WRITE_SCOPE).toString());
            client = clientManagement.updateClient(client.getId(), updatedClientData, credentials);
            compare(updatedClientData, client);
        } finally {
            assertTrue(clientManagement.unregisterClient(client.getId(), credentials));

            /*
             * Assure client is not listed anymore
             */
            List<ClientDto> clients = clientManagement.getClients(groupId, credentials);
            boolean found = false;
            for (ClientDto c : clients) {
                if (client.getId().equals(c.getId())) {
                    found = true;
                    break;
                }
            }
            assertFalse(found);

            /*
             * Assure client cannot be got anymore
             */
            ClientDto reloaded = clientManagement.getClientById(client.getId(), credentials);
            assertNull(reloaded);
        }
    }

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

    private static void compare(ClientDto client1, ClientDto client2) throws Exception {
        assertEquals(client1.getId(), client2.getId());
        assertEquals(client1.getName(), client2.getName());
        assertEquals(client1.getContactAddress(), client2.getContactAddress());
        assertEquals(client1.getDescription(), client2.getDescription());
        assertEquals(client1.getWebsite(), client2.getWebsite());
        assertEquals(client1.getDefaultScope(), client2.getDefaultScope());
        assertEquals(new HashSet<>(client1.getRedirectURIs()), new HashSet<>(client2.getRedirectURIs()));
        assertArrayEquals(client1.getIcon().getData(), client2.getIcon().getData());
        assertEquals(client1.getRegistrationDate(), client2.getRegistrationDate());
        assertEquals(client1.getSecret(), client2.getSecret());
        assertEquals(client1.isEnabled(), client2.isEnabled());
    }

    private static void compare(ClientDataDto clientData, ClientDto client) throws Exception {
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

        private CMEMatcher(String invalidValue) {
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

}
