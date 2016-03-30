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

package com.openexchange.messaging.json.actions.services;

import junit.framework.TestCase;
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
public class GetActionTest extends TestCase {

    // Success Case
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

    public void testUnknownId() throws OXException {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();

        final GetAction action = new GetAction(registry);

        final AJAXRequestData requestData = new AJAXRequestData();
        requestData.putParameter("id", "com.openexchange.test");

        try {
            final AJAXRequestResult result = action.perform(requestData, new SimServerSession(new SimContext(1), new SimUser(), null));
            fail("Should fail");
            assertNull(result);
        } catch (final OXException e) {
            //SUCCESS
        }

    }

    public void testMissingParameter() throws OXException {
        final SimMessagingServiceRegistry registry = new SimMessagingServiceRegistry();
        final GetAction action = new GetAction(registry);
        final AJAXRequestData requestData = new AJAXRequestData();
        try {
            final AJAXRequestResult result = action.perform(requestData, new SimServerSession(new SimContext(1), new SimUser(), null));
            fail("Should fail");
            assertNull(result);
        } catch (final OXException e) {
            //SUCCESS
            assertTrue(e.getMessage().contains("parameter"));
        }
    }

    public void testOXException() throws OXException {
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
        } catch (final OXException e) {
            //SUCCESS
        }
    }
}
