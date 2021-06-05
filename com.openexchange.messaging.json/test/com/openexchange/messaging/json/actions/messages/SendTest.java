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

package com.openexchange.messaging.json.actions.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Collection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAddressHeader;
import com.openexchange.messaging.MessagingMessage;
import com.openexchange.messaging.SimMessagingTransport;
import com.openexchange.messaging.StringContent;
import com.openexchange.messaging.json.MessagingMessageParser;
import com.openexchange.messaging.json.MessagingMessageWriter;
import com.openexchange.tools.session.SimServerSession;

/**
 * {@link SendTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SendTest extends AbstractMessagingActionTest {

    // Success Case

    @Test
    public void testSendWithRecipients() throws JSONException, OXException {
        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("account", "12");
        req.putParameter("recipients", "clark.kent@dailyplanet.com");

        req.setData(new JSONObject("{'headers' : {'content-type' : 'text/plain'}, body : 'Hello World'}"));

        perform(req);

        final SimMessagingTransport transport = (SimMessagingTransport) registry.getMessagingService("com.openexchange.test1", -1, -1).getAccountTransport(12, new SimServerSession(null, null, null));

        final MessagingMessage message = transport.getMessage();
        assertNotNull(message);
        assertEquals("Hello World", ((StringContent) message.getContent()).getData());

        final Collection<MessagingAddressHeader> recipients = transport.getRecipients();
        assertNotNull(recipients);
        assertEquals(1, recipients.size());
        assertEquals("clark.kent@dailyplanet.com", recipients.iterator().next().getAddress());

    }

    @Test
    public void testSendWithoutRecipients() throws OXException, JSONException {
        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("account", "12");

        req.setData(new JSONObject("{'headers' : {'content-type' : 'text/plain'}, body : 'Hello World'}"));

        perform(req);

        final SimMessagingTransport transport = (SimMessagingTransport) registry.getMessagingService("com.openexchange.test1", -1, -1).getAccountTransport(12, new SimServerSession(null, null, null));

        final MessagingMessage message = transport.getMessage();
        assertNotNull(message);
        assertEquals("Hello World", ((StringContent) message.getContent()).getData());

        final Collection<MessagingAddressHeader> recipients = transport.getRecipients();
        assertTrue(recipients == null);
    }

    // Error Cases

    @Test
    public void testInvalidBody() {
        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("account", "12");

        req.setData(new JSONArray());

        assertFails(req);
    }

    @Test
    public void testMissingBody() {
        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("account", "12");

        assertFails(req);
    }

    @Test
    public void testMissingMessagingServiceID() throws JSONException {
        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("account", "12");

        req.setData(new JSONObject("{'headers' : {'content-type' : 'text/plain'}, content : 'Hello World'}"));

        assertFails(req);
    }

    @Test
    public void testMissingAccountID() throws JSONException {
        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("messagingService", "com.openexchange.test1");

        req.setData(new JSONObject("{'headers' : {'content-type' : 'text/plain'}, content : 'Hello World'}"));

        assertFails(req);
    }

    @Override
    protected AbstractMessagingAction getAction() {
        return new SendAction(registry, new MessagingMessageWriter(), new MessagingMessageParser());
    }

}
