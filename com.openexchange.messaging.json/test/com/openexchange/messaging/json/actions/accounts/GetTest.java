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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.SimUser;
import com.openexchange.messaging.SimAccountManager;
import com.openexchange.messaging.SimMessagingAccount;
import com.openexchange.messaging.SimMessagingService;
import com.openexchange.messaging.registry.SimMessagingServiceRegistry;
import com.openexchange.tools.session.SimServerSession;

/**
 * {@link GetTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class GetTest {

    // Success Case
    @Test
    public void testGet() throws OXException {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();

        final SimAccountManager accManager = new SimAccountManager();
        final SimMessagingService service = new SimMessagingService();

        final SimMessagingAccount account = new SimMessagingAccount();
        account.setDisplayName("My Twitter Account");
        account.setMessagingService(service);

        accManager.setAccountToGet(account);

        service.setAccountManager(accManager);

        final DynamicFormDescription formDescription = new DynamicFormDescription().add(FormElement.input("inputField", "My nice input field"));

        service.setId("com.openexchange.twitter");
        service.setFormDescription(formDescription);
        registry.add(service);

        final GetAction action = new GetAction(registry);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("id", "12");
        requestData.putParameter("messagingService", "com.openexchange.twitter");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        final AJAXRequestResult result = action.perform(requestData, session);

        assertNotNull(result);

        final Object resultObject = result.getResultObject();
        assertNotNull(resultObject);
        assertTrue(JSONObject.class.isInstance(resultObject));

        assertEquals(12, accManager.getId());
        assertSame(session, accManager.getSession());
    }

    // Error Cases

    @Test
    public void testMissingParameterID() {

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("messagingService", "com.openexchange.twitter");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        final GetAction action = new GetAction(null);

        try {
            action.perform(requestData, session);
            fail("Should have died horribly");
        } catch (OXException x) {
            //SUCCESS
        }
    }

    @Test
    public void testMissingParameterMessagingService() {
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("id", "12");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        final GetAction action = new GetAction(null);

        try {
            action.perform(requestData, session);
            fail("Should have died horribly");
        } catch (OXException x) {
            //SUCCESS
        }
    }

    @Test
    public void testNumberFormatExceptionInID() {
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("id", "I'm not a number");
        requestData.putParameter("messagingService", "com.openexchange.twitter");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        final GetAction action = new GetAction(null);

        try {
            action.perform(requestData, session);
            fail("Should have died horribly");
        } catch (OXException x) {
            //SUCCESS
        }

    }

    @Test
    public void testOXExceptionInRegistry() {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();
        registry.setException(new OXException());

        final SimAccountManager accManager = new SimAccountManager();
        final SimMessagingService service = new SimMessagingService();
        service.setAccountManager(accManager);

        service.setId("com.openexchange.twitter");
        registry.add(service);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("id", "12");
        requestData.putParameter("messagingService", "com.openexchange.twitter");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        final GetAction action = new GetAction(registry);
        try {
            action.perform(requestData, session);
            fail("Should have died horribly");
        } catch (OXException x) {
            //SUCCESS
        }

    }

    @Test
    public void testOXExceptionInAccountManager() {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();

        final SimAccountManager accManager = new SimAccountManager();
        accManager.setException(new OXException());

        final SimMessagingService service = new SimMessagingService();
        service.setAccountManager(accManager);

        service.setId("com.openexchange.twitter");
        registry.add(service);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("id", "12");
        requestData.putParameter("messagingService", "com.openexchange.twitter");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        final GetAction action = new GetAction(registry);
        try {
            action.perform(requestData, session);
            fail("Should have died horribly");
        } catch (OXException x) {
            //SUCCESS
        }

    }

}
