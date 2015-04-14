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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth2;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.rmi.Naming;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.google.common.io.ByteStreams;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.DefaultIcon;
import com.openexchange.oauth.provider.DefaultScopes;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.client.ClientData;
import com.openexchange.oauth.provider.client.ClientManagement;
import com.openexchange.oauth.provider.internal.tools.ClientId;
import com.openexchange.oauth.provider.rmi.OAuthClientRmi;
import com.openexchange.tasks.json.TaskActionFactory;


/**
 * {@link ClientManagementTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ClientManagementTest {

    private OAuthClientRmi clientManagement;

    @BeforeClass
    public static void initTestFramework() throws Exception {
        AJAXConfig.init();
    }

    @Before
    public void before() throws Exception {
        clientManagement = (OAuthClientRmi) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + OAuthClientRmi.RMI_NAME);
    }

    @Test
    public void testClientLifecycle() throws Exception {
        /*
         * Create client and check data transmission
         */
        ClientData clientData = prepareClient(ClientManagementTest.class.getSimpleName() + "_" + System.currentTimeMillis());
        Client client = clientManagement.registerClient(ClientManagement.DEFAULT_GID, clientData);
        String groupId = ClientId.parse(client.getId()).getGroupId();
        try {
            compare(clientData, client);

            /*
             * Assure client is listed and can be got
             */
            List<Client> clients = clientManagement.getClients(groupId);
            boolean found = false;
            for (Client c : clients) {
                if (client.getId().equals(c.getId())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);

            Client reloaded = clientManagement.getClientById(client.getId());
            compare(client, reloaded);

            /*
             * Check disabling and enabling
             */
            assertTrue(clientManagement.disableClient(client.getId()));
            reloaded = clientManagement.getClientById(client.getId());
            assertFalse(reloaded.isEnabled());
            assertFalse(clientManagement.disableClient(client.getId())); // cannot disable a disabled client

            assertTrue(clientManagement.enableClient(client.getId()));
            reloaded = clientManagement.getClientById(client.getId());
            assertTrue(reloaded.isEnabled());
            assertFalse(clientManagement.enableClient(client.getId())); // cannot enable an enabled client

            /*
             * Revoke secret
             */
            String oldSecret = client.getSecret();
            client = clientManagement.revokeClientSecret(client.getId());
            assertNotNull(client.getSecret());
            assertNotEquals(oldSecret, client.getSecret());

            /*
             * Update
             */
            ClientData updatedClientData = new ClientData();
            updatedClientData.setName(Strings.reverse(client.getName()));
            updatedClientData.setContactAddress(Strings.reverse(client.getContactAddress()));
            updatedClientData.setDescription(Strings.reverse(client.getDescription()));
            updatedClientData.setWebsite(Strings.reverse(client.getWebsite()));
            DefaultIcon updatedIcon = new DefaultIcon();
            updatedIcon.setMimeType("image/png");
            byte[] updatedIconBytes = ByteStreams.toByteArray(client.getIcon().getInputStream());
            Arrays.sort(updatedIconBytes);
            updatedIcon.setData(updatedIconBytes);
            updatedClientData.setIcon(updatedIcon);
            updatedClientData.setRedirectURIs(Collections.singleton("http://example.com/oauth/client/endpoint"));
            updatedClientData.setDefaultScope(new DefaultScopes(ContactActionFactory.OAUTH_READ_SCOPE, ContactActionFactory.OAUTH_WRITE_SCOPE));
            client = clientManagement.updateClient(client.getId(), updatedClientData);
            compare(updatedClientData, client);
        } finally {
            assertTrue(clientManagement.unregisterClient(client.getId()));

            /*
             * Assure client is not listed anymore
             */
            List<Client> clients = clientManagement.getClients(groupId);
            boolean found = false;
            for (Client c : clients) {
                if (client.getId().equals(c.getId())) {
                    found = true;
                    break;
                }
            }
            assertFalse(found);

            /*
             * Assure client cannot be got anymore
             */
            Client reloaded = clientManagement.getClientById(client.getId());
            assertNull(reloaded);
        }
    }

    private static void compare(Client client1, Client client2) throws Exception {
        assertEquals(client1.getId(), client2.getId());
        assertEquals(client1.getName(), client2.getName());
        assertEquals(client1.getContactAddress(), client2.getContactAddress());
        assertEquals(client1.getDescription(), client2.getDescription());
        assertEquals(client1.getWebsite(), client2.getWebsite());
        assertEquals(client1.getDefaultScope(), client2.getDefaultScope());
        assertEquals(new HashSet<>(client1.getRedirectURIs()), new HashSet<>(client2.getRedirectURIs()));
        assertArrayEquals(ByteStreams.toByteArray(client1.getIcon().getInputStream()), ByteStreams.toByteArray(client2.getIcon().getInputStream()));
        assertEquals(client1.getRegistrationDate().getTime(), client2.getRegistrationDate().getTime());
        assertEquals(client1.getSecret(), client2.getSecret());
        assertEquals(client1.isEnabled(), client2.isEnabled());
    }

    private static void compare(ClientData clientData, Client client) throws Exception {
        assertNotNull(client.getId());
        assertEquals(clientData.getName(), client.getName());
        assertEquals(clientData.getContactAddress(), client.getContactAddress());
        assertEquals(clientData.getDescription(), client.getDescription());
        assertEquals(clientData.getWebsite(), client.getWebsite());
        assertEquals(clientData.getDefaultScope(), client.getDefaultScope());
        assertEquals(new HashSet<>(clientData.getRedirectURIs()), new HashSet<>(client.getRedirectURIs()));
        assertArrayEquals(ByteStreams.toByteArray(clientData.getIcon().getInputStream()), ByteStreams.toByteArray(client.getIcon().getInputStream()));
        assertTrue(client.getRegistrationDate().getTime() > 0);
        assertNotNull(client.getSecret());
        assertTrue(client.isEnabled());
    }

    public static ClientData prepareClient(String name) {
        DefaultIcon icon = new DefaultIcon();
        icon.setData(IconBytes.DATA);
        icon.setMimeType("image/jpg");

        Set<String> redirectURIs = new HashSet<>();
        redirectURIs.add("http://localhost");
        redirectURIs.add("http://localhost:8080");

        ClientData clientData = new ClientData();
        clientData.setName(name);
        clientData.setDescription(name);
        clientData.setIcon(icon);
        clientData.setContactAddress("webmaster@example.com");
        clientData.setWebsite("http://www.example.com");
        clientData.setDefaultScope(new DefaultScopes(ContactActionFactory.OAUTH_READ_SCOPE, ContactActionFactory.OAUTH_WRITE_SCOPE, AppointmentActionFactory.OAUTH_READ_SCOPE, AppointmentActionFactory.OAUTH_WRITE_SCOPE, TaskActionFactory.OAUTH_READ_SCOPE, TaskActionFactory.OAUTH_WRITE_SCOPE));
        clientData.setRedirectURIs(redirectURIs);
        return clientData;
    }

}
