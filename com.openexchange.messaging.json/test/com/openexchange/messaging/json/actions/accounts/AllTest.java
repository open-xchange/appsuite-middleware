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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.SimUser;
import com.openexchange.messaging.SimAccountManager;
import com.openexchange.messaging.SimMessagingAccount;
import com.openexchange.messaging.SimMessagingService;
import com.openexchange.messaging.registry.SimMessagingServiceRegistry;
import com.openexchange.tools.session.SimServerSession;

/**
 * {@link AllTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AllTest {

    private SimMessagingServiceRegistry registry;

    @Before
    public void setUp() {
        registry = new SimMessagingServiceRegistry();

        final SimMessagingService service1 = new SimMessagingService();
        service1.setId("com.openexchange.test1");

        final SimAccountManager accManager1 = new SimAccountManager();

        final SimMessagingAccount account11 = new SimMessagingAccount();
        account11.setDisplayName("acc1.1");
        account11.setId(11);
        account11.setMessagingService(service1);

        final SimMessagingAccount account12 = new SimMessagingAccount();
        account12.setDisplayName("acc1.2");
        account12.setId(12);
        account12.setMessagingService(service1);

        accManager1.setAllAccounts(account11, account12);
        service1.setAccountManager(accManager1);

        final SimMessagingService service2 = new SimMessagingService();
        service2.setId("com.openexchange.test2");

        final SimAccountManager accManager2 = new SimAccountManager();

        final SimMessagingAccount account21 = new SimMessagingAccount();
        account21.setDisplayName("acc2.1");
        account21.setId(11);
        account21.setMessagingService(service2);

        final SimMessagingAccount account22 = new SimMessagingAccount();
        account22.setDisplayName("acc2.2");
        account22.setId(22);
        account22.setMessagingService(service2);

        accManager2.setAllAccounts(account21, account22);
        service2.setAccountManager(accManager2);

        registry.add(service1);
        registry.add(service2);
    }

    // Success Cases
    @Test
    public void testListAllForACertainMessagingService() throws OXException {

        final AllAction action = new AllAction(registry);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("messagingService", "com.openexchange.test1");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        final AJAXRequestResult result = action.perform(requestData, session);

        assertNotNull(result);
        final Object resultObject = result.getResultObject();

        assertNotNull(resultObject);
        assertTrue(JSONArray.class.isInstance(resultObject));

        final JSONArray arr = (JSONArray) resultObject;
        assertEquals(2, arr.length());

    }

    @Test
    public void testListAll() throws OXException {
        final AllAction action = new AllAction(registry);

        final AJAXRequestData requestData = new AJAXRequestData();

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        final AJAXRequestResult result = action.perform(requestData, session);

        assertNotNull(result);
        final Object resultObject = result.getResultObject();

        assertNotNull(resultObject);
        assertTrue(JSONArray.class.isInstance(resultObject));

        final JSONArray arr = (JSONArray) resultObject;
        assertEquals(4, arr.length());

    }

    // Error Cases
    @Test
    public void testExceptionInMessagingRegistry() {
        registry.setException(new OXException(-1));
        final AllAction action = new AllAction(registry);

        final AJAXRequestData requestData = new AJAXRequestData();

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        try {
            action.perform(requestData, session);
            fail("Should not swallow exceptions");
        } catch (OXException x) {
            //SUCCESS
        }

    }

    @Test
    public void testExceptionInMessagingAccountManager() throws OXException {
        ((SimAccountManager) registry.getAllServices(-1, -1).get(0).getAccountManager()).setException(new OXException(-1));
        final AllAction action = new AllAction(registry);

        final AJAXRequestData requestData = new AJAXRequestData();

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        try {
            action.perform(requestData, session);
            fail("Should not swallow exceptions");
        } catch (OXException x) {
            //SUCCESS
        }
    }

}
