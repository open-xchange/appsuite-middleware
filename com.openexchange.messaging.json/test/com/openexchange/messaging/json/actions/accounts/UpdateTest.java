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

package com.openexchange.messaging.json.actions.accounts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.SimUser;
import com.openexchange.messaging.SimAccountManager;
import com.openexchange.messaging.SimMessagingService;
import com.openexchange.messaging.registry.SimMessagingServiceRegistry;
import com.openexchange.tools.session.SimServerSession;

/**
 * {@link UpdateTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class UpdateTest {    // Success Case

    @Test
    public void testUpdate() throws JSONException, OXException {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();

        final SimAccountManager accManager = new SimAccountManager();
        final SimMessagingService service = new SimMessagingService();
        service.setAccountManager(accManager);

        final DynamicFormDescription formDescription = new DynamicFormDescription().add(FormElement.input("inputField", "My nice input field"));

        service.setId("com.openexchange.twitter");
        service.setFormDescription(formDescription);
        registry.add(service);

        final UpdateAction action = new UpdateAction(registry);

        final JSONObject accountJSON = new JSONObject();
        accountJSON.put("id", 12);
        accountJSON.put("displayName", "My nicer twitter feed");
        accountJSON.put("messagingService", "com.openexchange.twitter");

        final AJAXRequestData request = new AJAXRequestData();
        request.setData(accountJSON);

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        action.perform(request, session);

        assertNotNull(accManager.getUpdatedAccount());
        assertEquals(12, accManager.getUpdatedAccount().getId());

        assertSame(session, accManager.getSession());

    }

    // Error Cases

    @Test
    public void testMissingID() throws JSONException {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();

        final SimAccountManager accManager = new SimAccountManager();
        final SimMessagingService service = new SimMessagingService();
        service.setAccountManager(accManager);

        final DynamicFormDescription formDescription = new DynamicFormDescription().add(FormElement.input("inputField", "My nice input field"));

        service.setId("com.openexchange.twitter");
        service.setFormDescription(formDescription);
        registry.add(service);

        final UpdateAction action = new UpdateAction(registry);

        final JSONObject accountJSON = new JSONObject();
        accountJSON.put("displayName", "My nicer twitter feed");
        accountJSON.put("messagingService", "com.openexchange.twitter");

        final AJAXRequestData request = new AJAXRequestData();
        request.setData(accountJSON);

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        try {
            action.perform(request, session);
            fail("Should have failed w/o ID");
        } catch (OXException x) {
            //SUCCESS
        }

    }

    @Test
    public void testMessagingExceptionFromRegistry() throws JSONException {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();
        registry.setException(new OXException(-1));

        final SimAccountManager accManager = new SimAccountManager();
        final SimMessagingService service = new SimMessagingService();
        service.setAccountManager(accManager);

        final DynamicFormDescription formDescription = new DynamicFormDescription().add(FormElement.input("inputField", "My nice input field"));

        service.setId("com.openexchange.twitter");
        service.setFormDescription(formDescription);
        registry.add(service);

        final UpdateAction action = new UpdateAction(registry);

        final JSONObject accountJSON = new JSONObject();
        accountJSON.put("displayName", "My nice twitter feed");

        final AJAXRequestData request = new AJAXRequestData();
        request.setData(accountJSON);

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        try {
            action.perform(request, session);
            fail("Should not swallow exceptions");
        } catch (OXException x) {
            // SUCCESS
        }

    }

    @Test
    public void testMessagingExceptionFromAccManager() throws JSONException {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();

        final SimAccountManager accManager = new SimAccountManager();
        accManager.setException(new OXException(-1));

        final SimMessagingService service = new SimMessagingService();
        service.setAccountManager(accManager);

        final DynamicFormDescription formDescription = new DynamicFormDescription().add(FormElement.input("inputField", "My nice input field"));

        service.setId("com.openexchange.twitter");
        service.setFormDescription(formDescription);
        registry.add(service);

        final UpdateAction action = new UpdateAction(registry);

        final JSONObject accountJSON = new JSONObject();
        accountJSON.put("displayName", "My nice twitter feed");

        final AJAXRequestData request = new AJAXRequestData();
        request.setData(accountJSON);

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        try {
            action.perform(request, session);
            fail("Should not swallow exceptions");
        } catch (OXException x) {
            // SUCCESS
        }

    }
}
