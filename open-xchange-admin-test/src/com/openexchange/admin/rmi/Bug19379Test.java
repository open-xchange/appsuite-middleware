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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.HashSet;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.factory.ContextFactory;

/**
 * {@link Bug19379Test}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Bug19379Test extends AbstractRMITest {

    @Test
    public void testAddSingleMapping() throws Exception {
        final int contextId = ContextFactory.getRandomContextId();
        try {
            Context context = createContext(contextId, createMappings("m1", "m2"));

            // check mappings
            context = getContextManager().getData(new Context(I(contextId)));
            assertTrue(context.getLoginMappings().equals(createMappings(getContextName(contextId), "m1", "m2")));

            // add some mappings...
            context = new Context(I(contextId));
            // ALWAYS use contextID + mappings!
            context.setLoginMappings(createMappings("m1", "m2", "m4"));
            getContextManager().change(context);

            // check mappings
            context = getContextManager().getData(new Context(I(contextId)));
            assertTrue(context.getLoginMappings().equals(createMappings(getContextName(contextId), "m1", "m2", "m4")));
        } finally {
            deleteContext(contextId);
        }
    }

    @Test
    public void testChangeContextName() throws Exception {
        final int contextId = ContextFactory.getRandomContextId();
        try {
            Context context = createContext(contextId, createMappings("m2"));

            // check context name and mappings...
            context = getContextManager().getData(new Context(I(contextId)));
            assertEquals(context.getName(), contextId + "_test" + contextId + ".it");
            assertTrue(context.getLoginMappings().equals(createMappings(getContextName(contextId), "m2")));

            // change contextName and mappings
            context = new Context(I(contextId));
            context.setName(contextId + "_test333.it");
            context.setLoginMappings(createMappings("m1"));
            getContextManager().change(context);

            // check context name and mappings
            context = getContextManager().getData(new Context(I(contextId)));
            assertTrue(context.getLoginMappings().equals(createMappings(contextId+"_test333.it", "m1")));
        } finally {
            deleteContext(contextId);
        }
    }

    @Test
    public void testDuplicateContextMappings() throws Exception {
        final int contextId1 = ContextFactory.getRandomContextId();
        final int contextId2 = ContextFactory.getRandomContextId();
        try {
            Context context1 = createContext(contextId1, createMappings("m1", "m2"));
            Context context2 = createContext(contextId2, createMappings("m3"));

            // check context names and mappings...
            context1 = getContextManager().getData(new Context(I(contextId1)));
            assertEquals(context1.getName(), contextId1 +"_test" + contextId1 +".it");
            assertTrue(context1.getLoginMappings().equals(createMappings(getContextName(contextId1), "m1", "m2")));

            context2 = getContextManager().getData(new Context(I(contextId2)));
            assertEquals(context2.getName(), contextId2 + "_test"+ contextId2 + ".it");
            assertTrue(context2.getLoginMappings().equals(createMappings(getContextName(contextId2), "m3")));

            // now add an ILLEGAL mapping...
            context1 = new Context(I(contextId1));
            // use same contextId2 contextName!
            context1.setLoginMappings(createMappings(Integer.toString(contextId1), "m1", "m2", "m3"));
            try {
                getContextManager().change(context1);
                fail("A StorageException must be thrown!");
            } catch (StorageException e) {
                // Found the duplicate login mapping
                e.printStackTrace();
            }
            // check previous mappings
            context1 = getContextManager().getData(new Context(I(contextId1)));
            assertTrue(context1.getLoginMappings().equals(createMappings(getContextName(contextId1), "m1", "m2")));
        } finally {
            deleteContext(contextId1);
            deleteContext(contextId2);
        }
    }

    // ====PRIVATE METHODS=========================================================================
    private String getContextName(final int contextID) {
        return contextID + "_test" + contextID + ".it";
    }

    private HashSet<String> createMappings(final String... mappings) {
        HashSet<String> result = new HashSet<String>();
        for (final String mapping : mappings) {
            result.add(mapping);
        }
        return result;
    }

    private Context createContext(final int contextID, final HashSet<String> lmappings) throws Exception {
        final Context context = new Context(I(contextID));
        context.setName(getContextName(contextID));
        context.setMaxQuota(L(512));
        if (null != lmappings) {
            context.setLoginMappings(lmappings);
        }

        final User admin = new User();
        admin.setName("admin@email" + contextID + ".it");
        admin.setPassword("password");
        admin.setGiven_name("Admin");
        admin.setSur_name("" + contextID);
        admin.setPrimaryEmail(admin.getName());
        admin.setEmail1(admin.getName());
        admin.setDisplay_name(admin.getGiven_name() + " " + admin.getSur_name());

        return getContextManager().create(context, admin);
    }

    private void deleteContext(final int contextID) throws Exception {
        Context context = new Context(I(contextID));
        try {
            getContextManager().delete(context);
        } catch (NoSuchContextException e) {
            // Ignore
        }
    }
}
