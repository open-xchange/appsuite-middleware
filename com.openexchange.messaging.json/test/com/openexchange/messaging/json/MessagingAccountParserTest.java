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

package com.openexchange.messaging.json;

import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.SimMessagingService;
import com.openexchange.messaging.registry.SimMessagingServiceRegistry;


/**
 * {@link MessagingAccountParserTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Firstname Lastname</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingAccountParserTest extends TestCase {
    public void testParse() throws JSONException, OXException {

        final SimMessagingService messagingService = new SimMessagingService();

        final DynamicFormDescription formDescription = new DynamicFormDescription().add(FormElement.input("inputField", "My nice input field"));

        messagingService.setId("com.openexchange.twitter");
        messagingService.setFormDescription(formDescription);

        final SimMessagingServiceRegistry serviceRegistry = new SimMessagingServiceRegistry();
        serviceRegistry.add(messagingService);

        final JSONObject accountJSON = new JSONObject();
        accountJSON.put("id", 12);
        accountJSON.put("displayName", "My nice twitter feed");
        accountJSON.put("messagingService", "com.openexchange.twitter");

        final JSONObject configJSON = new JSONObject();
        configJSON.put("inputField", "My nice input value");
        accountJSON.put("configuration", configJSON);

        final MessagingAccount account = new MessagingAccountParser(serviceRegistry).parse(accountJSON, -1, -1);

        assertNotNull("Account was null!", account);
        assertEquals(12, account.getId());
        assertEquals("My nice twitter feed", account.getDisplayName());
        assertSame(messagingService, account.getMessagingService());
        assertEquals("My nice input value", account.getConfiguration().get("inputField"));
    }

    public void testMandatoryFieldsOnly() throws OXException, JSONException {
        final SimMessagingService messagingService = new SimMessagingService();

        final DynamicFormDescription formDescription = new DynamicFormDescription().add(FormElement.input("inputField", "My nice input field"));

        messagingService.setId("com.openexchange.twitter");
        messagingService.setFormDescription(formDescription);

        final SimMessagingServiceRegistry serviceRegistry = new SimMessagingServiceRegistry();
        serviceRegistry.add(messagingService);

        final JSONObject accountJSON = new JSONObject();
        accountJSON.put("messagingService", "com.openexchange.twitter");


        final MessagingAccount account = new MessagingAccountParser(serviceRegistry).parse(accountJSON, -1, -1);

        assertNotNull("Account was null!", account);
        assertTrue("Expected unset ID, but was: "+account.getId(), 0 >= account.getId());
        assertTrue("Expected unset displayName, but was: '"+account.getDisplayName()+"'", null == account.getDisplayName());
        assertTrue("Expected unset configuration, but was: "+account.getConfiguration(), null == account.getConfiguration());
        assertSame(messagingService, account.getMessagingService());
    }

    public void testUnknownMessagingService() throws JSONException {
        final OXException exception = new OXException();
        final SimMessagingServiceRegistry serviceRegistry = new SimMessagingServiceRegistry();
        serviceRegistry.setException(exception);

        try {
            final JSONObject accountJSON = new JSONObject();
            accountJSON.put("messagingService", "com.openexchange.twitter");
            final MessagingAccount account = new MessagingAccountParser(serviceRegistry).parse(accountJSON, -1, -1);
            fail("Should have failed with exception from message service lookup");
        } catch (final OXException x) {
            assertSame(exception, x);
        }
    }
}
