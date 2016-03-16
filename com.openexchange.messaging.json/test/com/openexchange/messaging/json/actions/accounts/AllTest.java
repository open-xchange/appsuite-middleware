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
import org.json.JSONArray;
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
public class AllTest extends TestCase {
    private SimMessagingServiceRegistry registry;

    @Override
    protected void setUp() throws Exception {
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
    public void testExceptionInMessagingRegistry() throws OXException {
        registry.setException(new OXException(-1));
        final AllAction action = new AllAction(registry);

        final AJAXRequestData requestData = new AJAXRequestData();

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        try {
            action.perform(requestData, session);
            fail("Should not swallow exceptions");
        } catch (final OXException x) {
            //SUCCESS
        }


    }

    public void testExceptionInMessagingAccountManager() throws OXException {
        ((SimAccountManager) registry.getAllServices(-1, -1).get(0).getAccountManager()).setException(new OXException(-1));
        final AllAction action = new AllAction(registry);

        final AJAXRequestData requestData = new AJAXRequestData();

        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);

        try {
            action.perform(requestData, session);
            fail("Should not swallow exceptions");
        } catch (final OXException x) {
            //SUCCESS
        }
    }

}
