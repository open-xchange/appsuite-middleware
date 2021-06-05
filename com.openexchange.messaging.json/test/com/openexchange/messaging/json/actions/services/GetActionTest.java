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

package com.openexchange.messaging.json.actions.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.SimUser;
import com.openexchange.messaging.SimMessagingService;
import com.openexchange.messaging.registry.SimMessagingServiceRegistry;
import com.openexchange.tools.session.SimServerSession;

/**
 * {@link GetActionTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class GetActionTest {

    // Success Case
    @Test
    public void testGet() throws OXException {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();

        final SimMessagingService messagingService = new SimMessagingService();
        messagingService.setId("com.openexchange.test");

        registry.add(messagingService);

        final GetAction action = new GetAction(registry);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("id", "com.openexchange.test");

        final AJAXRequestResult result = action.perform(requestData, new SimServerSession(new SimContext(1), new SimUser(), null));
        assertNotNull(result);

        final Object resultObject = result.getResultObject();
        assertNotNull(resultObject);

    }

    // Error cases

    @Test
    public void testUnknownId() {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();

        final GetAction action = new GetAction(registry);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("id", "com.openexchange.test");

        try {
            final AJAXRequestResult result = action.perform(requestData, new SimServerSession(new SimContext(1), new SimUser(), null));
            fail("Should fail");
            assertNull(result);
        } catch (OXException e) {
            //SUCCESS
        }

    }

    @Test
    public void testMissingParameter() {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();
        final GetAction action = new GetAction(registry);
        final AJAXRequestData requestData = new AJAXRequestData();
        try {
            final AJAXRequestResult result = action.perform(requestData, new SimServerSession(new SimContext(1), new SimUser(), null));
            fail("Should fail");
            assertNull(result);
        } catch (OXException e) {
            //SUCCESS
            assertTrue(e.getMessage().contains("parameter"));
        }
    }

    @Test
    public void testOXException() {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();

        final SimMessagingService messagingService = new SimMessagingService();
        messagingService.setId("com.openexchange.test");

        registry.add(messagingService);
        registry.setException(new OXException());

        final GetAction action = new GetAction(registry);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("id", "com.openexchange.test");

        try {
            final AJAXRequestResult result = action.perform(requestData, new SimServerSession(new SimContext(1), new SimUser(), null));
            fail("Should fail");
            assertNull(result);
        } catch (OXException e) {
            //SUCCESS
        }
    }
}
