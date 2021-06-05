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
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.SimMessageAccess.Call;
import com.openexchange.messaging.json.MessagingMessageParser;
import com.openexchange.messaging.json.MessagingMessageWriter;

/**
 * {@link GetTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class GetTest extends AbstractMessagingActionTest {

    // Success Cases

    @Test
    public void testGet() throws OXException {
        final AJAXRequestData req = new AJAXRequestData();

        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");
        req.putParameter("id", "theID");

        final AJAXRequestResult result = perform(req);

        assertNotNull(result);
        final Object resultObject = result.getResultObject();
        assertNotNull(resultObject);
        assertTrue(JSONObject.class.isInstance(resultObject));

        final Call call = getMessagingAccessCall("com.openexchange.test1", 12);

        assertEquals("getMessage", call.getName());

        final Object[] args = call.getArgs();
        assertEquals("theFolderID", args[0]);
        assertEquals("theID", args[1]);
        assertEquals(Boolean.FALSE, args[2]);
    }

    @Test
    public void testPeek() throws OXException {
        final AJAXRequestData req = new AJAXRequestData();

        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");
        req.putParameter("peek", "true");
        req.putParameter("id", "theID");

        perform(req);

        final Call call = getMessagingAccessCall("com.openexchange.test1", 12);

        assertEquals("getMessage", call.getName());

        final Object[] args = call.getArgs();
        assertEquals("theFolderID", args[0]);
        assertEquals("theID", args[1]);
        assertEquals(Boolean.TRUE, args[2]);
    }

    // Error Cases

    @Test
    public void testMissingMessagingService() {
        final AJAXRequestData req = new AJAXRequestData();

        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");
        req.putParameter("id", "theID");

        assertFails(req);
    }

    @Test
    public void testMissingAccountID() {
        final AJAXRequestData req = new AJAXRequestData();

        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("id", "theID");

        assertFails(req);
    }

    @Test
    public void testMissingFolder() {
        final AJAXRequestData req = new AJAXRequestData();

        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("account", "12");
        req.putParameter("id", "theID");

        assertFails(req);
    }

    @Test
    public void testMissingID() {
        final AJAXRequestData req = new AJAXRequestData();

        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");

        assertFails(req);
    }

    @Test
    public void testInvalidPeekParameter() {
        final AJAXRequestData req = new AJAXRequestData();

        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");
        req.putParameter("id", "theID");
        req.putParameter("peek", "aboo");

        assertFails(req);
    }

    @Override
    protected GetAction getAction() {
        return new GetAction(registry, new MessagingMessageWriter(), new MessagingMessageParser());
    }

}
