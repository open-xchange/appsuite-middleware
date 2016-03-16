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

import java.util.Arrays;
import java.util.List;
import junit.framework.TestCase;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.SimUser;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingField;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.SimMessageAccess;
import com.openexchange.messaging.SimMessageAccess.Call;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.tools.session.SimServerSession;


/**
 * {@link AbstractMessagingActionTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractMessagingActionTest extends TestCase {

    protected MessagingServiceRegistry registry = TestRegistryBuilder.buildTestRegistry();

    protected AJAXRequestResult perform(final AJAXRequestData req) throws OXException {
        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);
        return getAction().perform(req, session);
    }

    protected void assertFails(final AJAXRequestData requestData) throws OXException {
        final AbstractMessagingAction action = getAction();
        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);
        try {
            final AJAXRequestResult result = action.perform(requestData, session);
            fail("Should have thrown exception");
        } catch (final OXException x) {
            // SUCCESS
        }


    }

    protected abstract AbstractMessagingAction getAction();

    protected void assertEqualFields(final Object object, final MessagingField...messagingFields) {
        assertNotNull(object);
        assertEquals(Arrays.asList(messagingFields), Arrays.asList((MessagingField[]) object));
    }

    protected void assertIDs(final Object object, final String...ids) {
        assertNotNull(object);
        assertEquals(Arrays.asList((String[]) object), Arrays.asList(ids));
    }

    protected Call getMessagingAccessCall(final String serviceId, final int accountId) throws OXException {
        final MessagingService service = registry.getMessagingService(serviceId, -1, -1);
        final MessagingAccountAccess accountAccess = service.getAccountAccess(accountId, null);
        final SimMessageAccess messageAccess = (SimMessageAccess) accountAccess.getMessageAccess();
        final List<Call> calls = messageAccess.getCalls();
        if (calls.isEmpty()) {
            return null;
        }
        return calls.get(calls.size() - 1);

    }
}
