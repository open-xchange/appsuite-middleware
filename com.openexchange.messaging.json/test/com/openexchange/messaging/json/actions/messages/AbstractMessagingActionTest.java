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
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.List;
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
public abstract class AbstractMessagingActionTest {
    protected MessagingServiceRegistry registry = TestRegistryBuilder.buildTestRegistry();

    protected AJAXRequestResult perform(final AJAXRequestData req) throws OXException {
        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);
        return getAction().perform(req, session);
    }

    protected void assertFails(final AJAXRequestData requestData) {
        final AbstractMessagingAction action = getAction();
        final SimServerSession session = new SimServerSession(new SimContext(1), new SimUser(), null);
        try {
            action.perform(requestData, session);
            fail("Should have thrown exception");
        } catch (OXException x) {
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
