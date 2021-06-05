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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.factory.ContextFactory;

/**
 * {@link ContextTest}
 * 
 * @author cutmasta
 * @author d7
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ContextTest extends AbstractRMITest {

    /**
     * Tests getting the admin id for a newly created context
     */
    @Test
    public void testGetAdminId() throws Exception {
        // The context admin's id is always '2'
        Context context = ContextFactory.createContext(10L);
        getContextManager().create(context, contextAdminCredentials);
        assertEquals(2, getContextManager().getAdminId(context));
    }

    /**
     * Tests loading all data for a context
     */
    @Test
    public void testGetByName() throws Exception {
        Context testContext = getContextManager().create(contextAdminCredentials);
        Integer idAsInt = testContext.getId();
        testContext.setId(null);
        final Context srv_loaded = getContextManager().getData(testContext);

        // Reset for compare
        testContext.setId(idAsInt);
        // FIXME: Add equals for context here. Same at nearly all other occurrences
        assertTrue("Expected same context ids", idAsInt.intValue() == srv_loaded.getId().intValue());
    }

    /**
     * Tests listing contexts by database
     */
    @Test
    public void testListContextByDatabase() throws Exception {
        Database[] dbs = getDatabaseManager().search("*");
        if (dbs.length > 0) {
            Context[] ids = getContextManager().search(dbs[0]);
            assertTrue("No contexts found in database " + dbs[0].getUrl(), ids.length > 0);
        } else {
            fail("No databases found.");
        }
    }

    /**
     * Test list contexts by filestore
     */
    @Test
    public void testListContextByFilestore() throws Exception {
        getContextManager().create(contextAdminCredentials);
        Filestore[] fiss = getFilestoreManager().search("*");
        if (fiss.length <= 0) {
            fail("No filestores found.");
        }
        boolean foundContextViaFilestore = false;
        for (int a = 0; a < fiss.length; a++) {
            Context[] ids = getContextManager().search(fiss[a]);
            if (ids.length > 0) {
                foundContextViaFilestore = true;
                break;
            }
        }
        assertTrue("No contexts found using filestores", foundContextViaFilestore);
    }

    /**
     * Test happy path, create context
     */
    @Test
    public void testCreateContext() throws Exception {
        getContextManager().create(superAdminCredentials);
    }

    /**
     * Test context creation with absent of quota information
     */
    @Test(expected = InvalidDataException.class)
    public void testCreateContextNoQuota() throws Exception {
        Context c = new Context();
        c.setName("Name-" + UUID.randomUUID().toString());
        getContextManager().create(c, contextAdminCredentials);
    }

    /**
     * Test context creation, immediate delete, and re-creation with the same identifier
     */
    @Test
    public void testCreateDeleteCreateContext() throws Exception {
        Context context = getContextManager().create(contextAdminCredentials);
        int ctxId = context.getId().intValue();
        getContextManager().delete(context);

        // Wait until the context is fully deleted; Timeout 5 seconds
        boolean contextExists;
        long timeoutTime = System.currentTimeMillis() + 5000;
        do {
            contextExists = getContextManager().exists(context);
        } while (contextExists == false && timeoutTime > System.currentTimeMillis());

        getContextManager().create(ctxId, 5000, contextAdminCredentials);
        
    }

    /**
     * Tests context deletion
     */
    @Test
    public void testDeleteContext() throws Exception {
        Context context = getContextManager().create(superAdminCredentials);
        getContextManager().delete(context);
        assertFalse(getContextManager().exists(context));
    }

    /**
     * Tests whether a context exists
     */
    @Test
    public void testExistsContext() throws Exception {
        Context testContext = getContextManager().create(contextAdminCredentials);
        Context nonExistent = new Context();
        nonExistent.setName("notexists.com");

        assertFalse("Context must not exist", getContextManager().exists(nonExistent));
        assertTrue("Context must exist", getContextManager().exists(testContext));
    }

    /**
     * Tests enabling a context
     */
    @Test
    public void testEnableContext() throws Exception {
        Context testContext = getContextManager().create(contextAdminCredentials);

        MaintenanceReason[] mrs = getMaintenanceReasonManager().search("*");
        MaintenanceReason mr = new MaintenanceReason();
        if (mrs.length == 0) {
            // add reason , and then use this reason to disable the context
            mr.setText("Context disabled " + System.currentTimeMillis());
            int mr_id = getMaintenanceReasonManager().create(mr).getId().intValue();
            mr.setId(mr_id);
        } else {
            mr.setId(mrs[0].getId());
        }

        getContextManager().disable(testContext);
        Context[] ctxs = getContextManager().search(testContext.getIdAsString());
        boolean ctx_disabled = false;
        for (final Context elem : ctxs) {
            if (elem.getId().intValue() == testContext.getId().intValue() && !elem.isEnabled()) {
                ctx_disabled = true;
            }
        }
        assertTrue("Context could be not disabled", ctx_disabled);

        // now enable context again
        getContextManager().enable(testContext);
        ctxs = getContextManager().search(testContext.getIdAsString());
        boolean ctx_ensabled = false;
        for (final Context elem : ctxs) {
            if ((elem.getId().intValue() == testContext.getId().intValue()) && elem.isEnabled()) {
                ctx_ensabled = true;
            }
        }
        assertTrue("Context could be not enabled", ctx_ensabled);
    }

    /**
     * Tests disabling a context
     */
    @Test
    public void testDisableContext() throws Exception {
        Context testContext = getContextManager().create(contextAdminCredentials);

        MaintenanceReason[] mrs = getMaintenanceReasonManager().search("*");
        MaintenanceReason mr = new MaintenanceReason();
        if (mrs.length == 0) {
            // add reason , and then use this reason to disable the context
            mr.setText("Context disabled " + System.currentTimeMillis());
            int mr_id = getMaintenanceReasonManager().create(mr).getId().intValue();
            mr.setId(mr_id);
        } else {
            mr.setId(mrs[0].getId());
        }
        getContextManager().disable(testContext);
        Context[] ctxs = getContextManager().search(testContext.getIdAsString());
        boolean ctx_disabled = false;

        for (final Context elem : ctxs) {
            if (!elem.isEnabled()) {
                ctx_disabled = true;
            }
        }
        assertTrue("context could be not disabled", ctx_disabled);
    }

    /**
     * Test getting and changing an existing context
     */
    @Test
    public void testGetAndChangeContext() throws Exception {
        Context context = new Context();
        context.setName("testGetAndChangeContext-" + UUID.randomUUID().toString());
        context.setMaxQuota(5000L);
        context.setUserAttribute("com.openexchange.test", "flavor", "lemon");
        context.setUserAttribute("com.openexchange.test", "texture", "squishy");

        getContextManager().create(context, contextAdminCredentials);
        Context srv_loaded = getContextManager().getData(context);

        assertEquals("lemon", srv_loaded.getUserAttribute("com.openexchange.test", "flavor"));
        assertEquals("squishy", srv_loaded.getUserAttribute("com.openexchange.test", "texture"));

        assertTrue("Expected same context ids", context.getId().intValue() == srv_loaded.getId().intValue());

        String add_mapping = srv_loaded.getId().intValue() + "_" + System.currentTimeMillis();
        srv_loaded.addLoginMapping(add_mapping);

        String changed_context_name = srv_loaded.getName() + "_" + System.currentTimeMillis();
        srv_loaded.setName(changed_context_name);

        // And for good measure some dynamic attributes

        srv_loaded.setUserAttribute("com.openexchange.test", "flavor", "pistaccio");
        srv_loaded.setUserAttribute("com.openexchange.test", "color", "green");
        srv_loaded.setUserAttribute("com.openexchange.test", "texture", null);

        // change context and load again
        getContextManager().change(srv_loaded);

        Context edited_ctx = getContextManager().getData(context);

        assertEquals("pistaccio", srv_loaded.getUserAttribute("com.openexchange.test", "flavor"));
        assertEquals("green", srv_loaded.getUserAttribute("com.openexchange.test", "color"));
        assertEquals(null, srv_loaded.getUserAttribute("com.openexchange.test", "texture"));

        // ids must be correct again and the mapping should now exist
        assertTrue("Expected same context ids", edited_ctx.getId().intValue() == srv_loaded.getId().intValue());

        // new mapping must exists
        assertTrue("Expected changed login mapping in loaded context", edited_ctx.getLoginMappings().contains(add_mapping));

        // changed conmtext name must exists
        assertTrue("Expected changed context name to be same as loaded ctx", edited_ctx.getName().equals(changed_context_name));
    }

    /**
     * Tests adding the same login mapping twice
     */
    @Test
    public void testDuplicateLoginMappingsThrowReadableError() throws Exception {
        Context ctx1 = ContextFactory.createContext(5000);
        ctx1.setLoginMappings(new HashSet<String>(Arrays.asList("foo")));

        getContextManager().create(ctx1, contextAdminCredentials);
        try {
            Context ctx2 = ContextFactory.createContext(5000);
            ctx2.setLoginMappings(new HashSet<String>(Arrays.asList("foo")));
            getContextManager().create(ctx2, contextAdminCredentials);
            fail("Could add Context");
        } catch (Exception x) {
            assertTrue(x.getMessage().startsWith("Cannot map 'foo' to the newly created context. This mapping is already in use."));
        }
    }

    /**
     * List contexts, search by id
     */
    @Test
    public void testListContext() throws Exception {
        Context testContext = getContextManager().create(contextAdminCredentials);
        int ctxid = testContext.getId().intValue();
        // now search exactly for the added context
        Context[] ctxs = getContextManager().search(Integer.toString(ctxid));
        boolean foundctx = false;
        for (final Context elem : ctxs) {
            if (elem.getId().intValue() == ctxid) {
                foundctx = true;
            }
        }
        assertTrue("context not found", foundctx);
    }
}
