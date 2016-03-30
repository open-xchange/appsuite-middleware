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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.messaging.json.actions.messages;

import org.json.JSONArray;
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

        final JSONArray resultJSON = (JSONArray) result.getResultObject();
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

        final JSONArray resultJSON = (JSONArray) result.getResultObject();
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

        final JSONArray resultJSON = (JSONArray) result.getResultObject();
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

/*    public void testAllWithIndex() throws OXException {
        AllAction action = new AllAction(registry, new MessagingMessageWriter(), new MessagingMessageParser());

        AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("account", "12");
        requestData.putParameter("columns", "id,subject");

        SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);
        AJAXRequestResult result = action.perform(requestData, session);

        assertNotNull(result);

        JSONArray resultJSON = (JSONArray) result.getResultObject();
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

    public void testUnknownSortingColumn() throws OXException {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("sort", "gnitz");
        requestData.putParameter("account", "12");
        requestData.putParameter("columns", "id,subject");

        assertFails(requestData);

    }

    public void testUnknownColumn() throws OXException {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("account", "12");
        requestData.putParameter("columns", "id,subject,gnitz");

        assertFails(requestData);
    }

    public void testMissingColumns() throws OXException {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("account", "12");

        assertFails(requestData);

    }

/*    public void testInvalidIndex() {

    } */ // Later

    public void testAllWithInvalidDirection() throws OXException {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("sort", "subject");
        requestData.putParameter("order", "condesc");
        requestData.putParameter("account", "12");
        requestData.putParameter("columns", "id,subject");

        assertFails(requestData);
    }

    public void testMissingServiceID() throws OXException {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("account", "12");
        requestData.putParameter("columns", "id,subject");

        assertFails(requestData);

    }

    public void testMissingAccountID() throws OXException {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("folder", "theFolderID");
        requestData.putParameter("columns", "id,subject");

        assertFails(requestData);
   }

    public void testMissingFolderID() throws OXException {
        final AJAXRequestData requestData = new AJAXRequestData();

        requestData.putParameter("messagingService", "com.openexchange.test1");
        requestData.putParameter("account", "12");
        requestData.putParameter("columns", "id,subject");

        assertFails(requestData);

    }

    public void testNumberFormatExceptionInAccountID() throws OXException {
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
