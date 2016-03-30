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

package com.openexchange.messaging.json.actions.accounts;

import junit.framework.TestCase;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.SimUser;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.SimAccountManager;
import com.openexchange.messaging.SimMessagingService;
import com.openexchange.messaging.registry.SimMessagingServiceRegistry;
import com.openexchange.tools.session.SimServerSession;


/**
 * {@link DeleteTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DeleteTest extends TestCase {
    // Success Case

    public void testDelete() throws OXException {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();

        final SimAccountManager accManager = new SimAccountManager();
        final SimMessagingService service = new SimMessagingService();
        service.setAccountManager(accManager);

        service.setId("com.openexchange.twitter");
        registry.add(service);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("id", "12");
        requestData.putParameter("messagingService", "com.openexchange.twitter");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        final DeleteAction action = new DeleteAction(registry);
        action.perform(requestData, session);

        final MessagingAccount account = accManager.getDeletedAccount();
        assertNotNull(account);
        assertEquals(12, account.getId());
    }

    // Error Cases

    public void testMissingParameterID() throws OXException {

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("messagingService", "com.openexchange.twitter");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        final DeleteAction action = new DeleteAction(null);

        try {
            action.perform(requestData, session);
            fail("Should have died horribly");
        } catch (final OXException x) {
            //SUCCESS
        }
    }

    public void testMissingParameterMessagingService() throws OXException {
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("id", "12");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        final DeleteAction action = new DeleteAction(null);

        try {
            action.perform(requestData, session);
            fail("Should have died horribly");
        } catch (final OXException x) {
            //SUCCESS
        }
    }

    public void testNumberFormatExceptionInID() throws OXException {
        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("id", "I'm not a number");
        requestData.putParameter("messagingService", "com.openexchange.twitter");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        final DeleteAction action = new DeleteAction(null);

        try {
            action.perform(requestData, session);
            fail("Should have died horribly");
        } catch (final OXException x) {
            //SUCCESS
        }

    }

    public void testMessagingExceptionInRegistry() throws OXException {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();
        registry.setException(new OXException(-1));

        final SimAccountManager accManager = new SimAccountManager();
        final SimMessagingService service = new SimMessagingService();
        service.setAccountManager(accManager);

        service.setId("com.openexchange.twitter");
        registry.add(service);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("id", "12");
        requestData.putParameter("messagingService", "com.openexchange.twitter");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        final DeleteAction action = new DeleteAction(registry);
        try {
            action.perform(requestData, session);
            fail("Should have died horribly");
        } catch (final OXException x) {
            //SUCCESS
        }

    }

    public void testMessagingExceptionInAccountManager() throws OXException {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();

        final SimAccountManager accManager = new SimAccountManager();
        accManager.setException(new OXException(-1));
        final SimMessagingService service = new SimMessagingService();
        service.setAccountManager(accManager);

        service.setId("com.openexchange.twitter");
        registry.add(service);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("id", "12");
        requestData.putParameter("messagingService", "com.openexchange.twitter");

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        final DeleteAction action = new DeleteAction(registry);
        try {
            action.perform(requestData, session);
            fail("Should have died horribly");
        } catch (final OXException x) {
            //SUCCESS
        }

    }
}
