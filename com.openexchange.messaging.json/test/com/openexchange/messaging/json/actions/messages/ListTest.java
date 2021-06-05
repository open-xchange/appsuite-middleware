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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.SimMessageAccess.Call;
import com.openexchange.messaging.json.MessagingMessageParser;
import com.openexchange.messaging.json.MessagingMessageWriter;

/**
 * {@link ListTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ListTest extends AbstractMessagingActionTest {

    // Success Cases

    @Test
    public void testList() throws JSONException, OXException {
        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("columns", "id, subject");

        final String folderId = "com.openexchange.test1://12/theFolderID";

        req.setData(new JSONArray("[{folder : '" + folderId + "', id : 'id1'}, {folder : '" + folderId + "', id : 'id2'}, {folder : '" + folderId + "', id : 'id3'}]"));

        final AJAXRequestResult result = perform(req);

        assertNotNull(result);

        final Object resultObject = result.getResultObject();
        assertNotNull(resultObject);

        assertTrue(JSONArray.class.isInstance(resultObject));

        final Call call = getMessagingAccessCall("com.openexchange.test1", 12);

        assertEquals("getMessages", call.getName());

        final Object[] args = call.getArgs();
        assertEquals("theFolderID", args[0]);
        assertIDs(args[1], "id1", "id2", "id3");
        assertEqualFields(args[2], MessagingField.ID, MessagingField.SUBJECT);

    }

    // Error Cases

    @Test
    public void testInvalidBody() throws JSONException {
        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");
        req.putParameter("columns", "id, subject");

        req.setData(new JSONObject("{}"));

        assertFails(req);
    }

    @Test
    public void testMissingBody() {
        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");
        req.putParameter("columns", "id, subject");

        assertFails(req);
    }

    @Test
    public void testMissingServiceID() throws JSONException {
        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");
        req.putParameter("columns", "id, subject");

        req.setData(new JSONArray("['id1', 'id2', 'id3']"));

        assertFails(req);

    }

    @Test
    public void testMissingFolderID() throws JSONException {
        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("account", "12");
        req.putParameter("columns", "id, subject");

        req.setData(new JSONArray("['id1', 'id2', 'id3']"));

        assertFails(req);
    }

    @Test
    public void testMissingAccountID() throws JSONException {
        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("columns", "id, subject");

        req.setData(new JSONArray("['id1', 'id2', 'id3']"));

        assertFails(req);

    }

    @Test
    public void testMissingColumns() throws JSONException {
        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");

        req.setData(new JSONArray("['id1', 'id2', 'id3']"));

        assertFails(req);

    }

    @Test
    public void testUnknownColumn() throws JSONException {
        final AJAXRequestData req = new AJAXRequestData();
        req.putParameter("messagingService", "com.openexchange.test1");
        req.putParameter("folder", "theFolderID");
        req.putParameter("account", "12");
        req.putParameter("columns", "id, subject,gnitz");

        req.setData(new JSONArray("['id1', 'id2', 'id3']"));

        assertFails(req);

    }

    @Override
    protected AbstractMessagingAction getAction() {
        return new ListAction(registry, new MessagingMessageWriter(), new MessagingMessageParser());
    }

}
