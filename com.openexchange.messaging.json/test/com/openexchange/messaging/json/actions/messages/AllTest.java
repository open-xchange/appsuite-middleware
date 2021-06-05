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
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.SimUser;
import com.openexchange.messaging.IndexRange;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.OrderDirection;
import com.openexchange.messaging.SimMessageAccess.Call;
import com.openexchange.messaging.json.MessagingMessageParser;
import com.openexchange.messaging.json.MessagingMessageWriter;
import com.openexchange.tools.session.SimServerSession;

/**
 * {@link AllTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AllTest extends AbstractMessagingActionTest {

    // Success Cases

    @Test
    public void testAll() throws OXException {
        final AllAction action = new AllAction(registry, new MessagingMessageWriter(), new MessagingMessageParser());

        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("account", "12");
        requestData.putParameter("columns", "id,subject");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);
        final AJAXRequestResult result = action.perform(requestData, session);

        assertNotNull(result);

        final JSONArray resultJSON = JSONArray.class.cast(result.getResultObject());
        assertEquals(1, resultJSON.length());

        final Call call = getMessagingAccessCall("com.openexchange.test1", 12);
        assertNotNull(call);

        assertEquals(call.getName(), "getAllMessages");

        final Object[] args = call.getArgs();
        assertEquals("theFolderID", args[0]);
        assertEquals(IndexRange.NULL, args[1]);
        assertEquals(null, args[2]);
        assertEquals(null, args[3]);
        assertEqualFields(args[4], MessagingField.ID, MessagingField.SUBJECT);

    }

    @Test
    public void testAllWithSorting() throws OXException {
        final AllAction action = new AllAction(registry, new MessagingMessageWriter(), new MessagingMessageParser());

        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("account", "12");
        requestData.putParameter("sort", "subject");
        requestData.putParameter("columns", "id,subject");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);
        final AJAXRequestResult result = action.perform(requestData, session);

        assertNotNull(result);

        final JSONArray resultJSON = JSONArray.class.cast(result.getResultObject());
        assertEquals(1, resultJSON.length());

        final Call call = getMessagingAccessCall("com.openexchange.test1", 12);
        assertNotNull(call);

        assertEquals(call.getName(), "getAllMessages");

        final Object[] args = call.getArgs();
        assertEquals("theFolderID", args[0]);
        assertEquals(IndexRange.NULL, args[1]);
        assertEquals(MessagingField.SUBJECT, args[2]);
        assertEquals(OrderDirection.ASC, args[3]);
        assertEqualFields(args[4], MessagingField.ID, MessagingField.SUBJECT);
    }

    @Test
    public void testAllWithSortingAndDirection() throws OXException {
        final AllAction action = new AllAction(registry, new MessagingMessageWriter(), new MessagingMessageParser());

        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("account", "12");
        requestData.putParameter("sort", "subject");
        requestData.putParameter("order", "desc");
        requestData.putParameter("columns", "id,subject");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);
        final AJAXRequestResult result = action.perform(requestData, session);

        assertNotNull(result);

        final JSONArray resultJSON = JSONArray.class.cast(result.getResultObject());
        assertEquals(1, resultJSON.length());

        final Call call = getMessagingAccessCall("com.openexchange.test1", 12);
        assertNotNull(call);

        assertEquals(call.getName(), "getAllMessages");

        final Object[] args = call.getArgs();
        assertEquals("theFolderID", args[0]);
        assertEquals(IndexRange.NULL, args[1]);
        assertEquals(MessagingField.SUBJECT, args[2]);
        assertEquals(OrderDirection.DESC, args[3]);
        assertEqualFields(args[4], MessagingField.ID, MessagingField.SUBJECT);

    }

/*         @Test
     public void testAllWithIndex() {
        AllAction action = new AllAction(registry, new MessagingMessageWriter(), new MessagingMessageParser());

        AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("account", "12");
        requestData.putParameter("columns", "id,subject");

        SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);
        AJAXRequestResult result = action.perform(requestData, session);

        assertNotNull(result);

        JSONArray resultJSON = JSONArray.class.cast(result.getResultObject());
        assertEquals(1, resultJSON.length());

        Call call = getMessagingAccessCall("com.openexchange.test1", 12);
        assertNotNull(call);

        assertEquals(call.getName(), "getAllMessages");

        Object[] args = call.getArgs();
        assertEquals("theFolderID", args[0]);
        assertEquals(IndexRange.NULL, args[1]);
        assertEquals(null, args[2]);
        assertEquals(null, args[3]);
        assertEqualFields(new MessagingField[] { MessagingField.ID, MessagingField.SUBJECT }, args[4]);
    } */ // Later

    // Error Cases

    @Test
    public void testUnknownSortingColumn() {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("sort", "gnitz");
        requestData.putParameter("account", "12");
        requestData.putParameter("columns", "id,subject");

        assertFails(requestData);

    }

    @Test
    public void testUnknownColumn() {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("account", "12");
        requestData.putParameter("columns", "id,subject,gnitz");

        assertFails(requestData);
    }

    @Test
    public void testMissingColumns() {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("account", "12");

        assertFails(requestData);

    }

    /*
     * @Test
     * public void testInvalidIndex() {
     * 
     * }
     */ // Later

    @Test
    public void testAllWithInvalidDirection() {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("sort", "subject");
        requestData.putParameter("order", "condesc");
        requestData.putParameter("account", "12");
        requestData.putParameter("columns", "id,subject");

        assertFails(requestData);
    }

    @Test
    public void testMissingServiceID() {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("account", "12");
        requestData.putParameter("columns", "id,subject");

        assertFails(requestData);

    }

    @Test
    public void testMissingAccountID() {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("columns", "id,subject");

        assertFails(requestData);
    }

    @Test
    public void testMissingFolderID() {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("account", "12");
        requestData.putParameter("columns", "id,subject");

        assertFails(requestData);

    }

    @Test
    public void testNumberFormatExceptionInAccountID() {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("account", "12abc");
        requestData.putParameter("columns", "id,subject");

        assertFails(requestData);

    }

    @Override
    protected AbstractMessagingAction getAction() {
        return new AllAction(registry, new MessagingMessageWriter(), new MessagingMessageParser());
    }

}
