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

package com.openexchange.admin.rmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.factory.ContextFactory;
import com.openexchange.admin.rmi.factory.UserFactory;

/**
 * {@link Bug35430Test}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class Bug35430Test extends AbstractRMITest {

    private final int contextId = ContextFactory.getRandomContextId();

    /**
     * Initialises a new {@link Bug35430Test}.
     */
    public Bug35430Test() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        createContext("bug" + contextId + "context.com", contextId);
    }

    @Test
    public void test() throws Throwable {
        Context[] contexts = getContextManager().search("bug35430context.com");
        assertEquals(1, contexts.length);
        assertEquals(new Integer(contextId), contexts[0].getId());
        assertEquals("bug35430context.com", contexts[0].getName());

        contexts = getContextManager().search("00" + contextId + ".pi");
        assertEquals(0, contexts.length);
    }

    private Context createContext(String name, int cid) throws Exception {
        Context newContext = ContextFactory.createContext(cid, name);
        User newAdmin = UserFactory.createUser("oxadmin", "secret", "New Admin", "New", "Admin", "newadmin@ox.invalid");
        boolean created = false;
        try {
            newContext = getContextManager().create(newContext, newAdmin);
            created = true;
            try {
                getContextManager().create(newContext, newAdmin);
                fail("Should throw ContextExistsException");
            } catch (ContextExistsException e) {
                assertTrue("Caught exception", true);
            }
        } catch (Exception e) {
            if (!created) {
                Context[] ctxs = getContextManager().search(name);
                if (ctxs.length > 0) {
                    newContext = ctxs[0];
                }
            }
        }
        return newContext;
    }
}
