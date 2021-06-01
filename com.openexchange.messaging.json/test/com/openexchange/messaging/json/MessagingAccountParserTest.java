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

package com.openexchange.messaging.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.SimMessagingService;
import com.openexchange.messaging.registry.SimMessagingServiceRegistry;

/**
 * {@link MessagingAccountParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Firstname Lastname</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingAccountParserTest {

    @Test
    public void testParse() throws JSONException, OXException {

        final SimMessagingService messagingService = new SimMessagingService();

        final DynamicFormDescription formDescription = new DynamicFormDescription().add(FormElement.input("inputField", "My nice input field"));

        messagingService.setId("com.openexchange.twitter");
        messagingService.setFormDescription(formDescription);

        final SimMessagingServiceRegistry serviceRegistry = new SimMessagingServiceRegistry();
        serviceRegistry.add(messagingService);

        final JSONObject accountJSON = new JSONObject();
        accountJSON.put("id", 12);
        accountJSON.put("displayName", "My nice twitter feed");
        accountJSON.put("messagingService", "com.openexchange.twitter");

        final JSONObject configJSON = new JSONObject();
        configJSON.put("inputField", "My nice input value");
        accountJSON.put("configuration", configJSON);

        final MessagingAccount account = new MessagingAccountParser(serviceRegistry).parse(accountJSON, -1, -1);

        assertNotNull("Account was null!", account);
        assertEquals(12, account.getId());
        assertEquals("My nice twitter feed", account.getDisplayName());
        assertSame(messagingService, account.getMessagingService());
        assertEquals("My nice input value", account.getConfiguration().get("inputField"));
    }

    @Test
    public void testMandatoryFieldsOnly() throws OXException, JSONException {
        final SimMessagingService messagingService = new SimMessagingService();

        final DynamicFormDescription formDescription = new DynamicFormDescription().add(FormElement.input("inputField", "My nice input field"));

        messagingService.setId("com.openexchange.twitter");
        messagingService.setFormDescription(formDescription);

        final SimMessagingServiceRegistry serviceRegistry = new SimMessagingServiceRegistry();
        serviceRegistry.add(messagingService);

        final JSONObject accountJSON = new JSONObject();
        accountJSON.put("messagingService", "com.openexchange.twitter");

        final MessagingAccount account = new MessagingAccountParser(serviceRegistry).parse(accountJSON, -1, -1);

        assertNotNull("Account was null!", account);
        assertTrue("Expected unset ID, but was: " + account.getId(), 0 >= account.getId());
        assertTrue("Expected unset displayName, but was: '" + account.getDisplayName() + "'", null == account.getDisplayName());
        assertTrue("Expected unset configuration, but was: " + account.getConfiguration(), null == account.getConfiguration());
        assertSame(messagingService, account.getMessagingService());
    }

    @Test
    public void testUnknownMessagingService() throws JSONException {
        final OXException exception = new OXException();
        final SimMessagingServiceRegistry serviceRegistry = new SimMessagingServiceRegistry();
        serviceRegistry.setException(exception);

        try {
            final JSONObject accountJSON = new JSONObject();
            accountJSON.put("messagingService", "com.openexchange.twitter");
            new MessagingAccountParser(serviceRegistry).parse(accountJSON, -1, -1);
            fail("Should have failed with exception from message service lookup");
        } catch (OXException x) {
            assertSame(exception, x);
        }
    }
}
