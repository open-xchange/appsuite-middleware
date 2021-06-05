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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;
import com.openexchange.admin.rmi.factory.ResourceFactory;

/**
 * {@link ResourceTest}
 *
 * @author cutmasta
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ResourceTest extends AbstractRMITest {

    private Context context;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        context = getContextManager().create(contextAdminCredentials);
    }

    @Test
    public void testCreateResource() throws Exception {
        Resource res = ResourceFactory.createResource("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, contextAdminCredentials);

        Resource[] srv_response = getResourceManager().search(context, "*", contextAdminCredentials);
        if (srv_response == null) {
            fail("server response is null");
            return;
        }
        boolean found_resource = false;
        for (int a = 0; a < srv_response.length; a++) {
            Resource tmp = srv_response[a];
            if (tmp.getId().equals(createResourcedresource.getId())) {
                assertEquals(createResourcedresource.getDescription(), tmp.getDescription());
                assertEquals(createResourcedresource.getDisplayname(), tmp.getDisplayname());
                assertEquals(createResourcedresource.getEmail(), tmp.getEmail());
                assertEquals(createResourcedresource.getName(), tmp.getName());
                found_resource = true;
            }
        }
        assertTrue("Expected to find resource with correct data", found_resource);
    }

    @Test
    public void testChange() throws Exception {
        Resource res = ResourceFactory.createResource("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, contextAdminCredentials);

        Resource[] srv_response = getResourceManager().search(context, "*", contextAdminCredentials);
        if (srv_response == null) {
            fail("server response is null");
            return;
        }
        boolean found_resource = false;
        for (int a = 0; a < srv_response.length; a++) {
            Resource tmp = srv_response[a];
            if (tmp.getId().equals(createResourcedresource.getId())) {
                assertEquals(createResourcedresource.getDescription(), tmp.getDescription());
                assertEquals(createResourcedresource.getDisplayname(), tmp.getDisplayname());
                assertEquals(createResourcedresource.getEmail(), tmp.getEmail());
                assertEquals(createResourcedresource.getName(), tmp.getName());
                found_resource = true;
            }
        }

        assertTrue("Expected to find resource with correct data", found_resource);

        // set change data
        if (null != createResourcedresource.getAvailable()) {
            createResourcedresource.setAvailable(!createResourcedresource.getAvailable());
        } else {
            createResourcedresource.setAvailable(true);
        }
        createResourcedresource.setDescription(createResourcedresource.getDescription() + change_suffix);
        createResourcedresource.setDisplayname(createResourcedresource.getDisplayname() + change_suffix);
        createResourcedresource.setEmail(getChangedEmailAddress(createResourcedresource.getEmail(), change_suffix));
        createResourcedresource.setName(createResourcedresource.getName() + change_suffix);

        // change on server
        getResourceManager().change(createResourcedresource, context, contextAdminCredentials);

        // get resource from server and verify changed data
        Resource srv_res = getResourceManager().getData(createResourcedresource, context, contextAdminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());

    }

    @Test
    public void testChangeNull() throws Exception {
        // set description attribute to null 
        Resource res = ResourceFactory.createResource("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, contextAdminCredentials);

        Resource[] srv_response = getResourceManager().search(context, "*", contextAdminCredentials);
        if (srv_response == null) {
            fail("server response is null");
            return;
        }
        boolean found_resource = false;
        for (int a = 0; a < srv_response.length; a++) {
            Resource tmp = srv_response[a];
            if (tmp.getId().equals(createResourcedresource.getId())) {
                assertEquals(createResourcedresource.getDescription(), tmp.getDescription());
                assertEquals(createResourcedresource.getDisplayname(), tmp.getDisplayname());
                assertEquals(createResourcedresource.getEmail(), tmp.getEmail());
                assertEquals(createResourcedresource.getName(), tmp.getName());
                found_resource = true;
            }
        }

        assertTrue("Expected to find resource with correct data", found_resource);

        // set change data
        if (null != createResourcedresource.getAvailable()) {
            createResourcedresource.setAvailable(!createResourcedresource.getAvailable());
        } else {
            createResourcedresource.setAvailable(true);
        }

        // Set to null
        createResourcedresource.setDescription(null);

        // change on server
        getResourceManager().change(createResourcedresource, context, contextAdminCredentials);

        // get resource from server and verify changed data
        Resource srv_res = getResourceManager().getData(createResourcedresource, context, contextAdminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription()); // must be able to null description        

        assertNotNull("email cannot be null", srv_res.getEmail());
        assertNotNull("name cannot be null", srv_res.getName());
        assertNotNull("display name cannot be null", srv_res.getDisplayname());
    }

    @Test
    public void testGet() throws Exception {
        Resource res = ResourceFactory.createResource("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, contextAdminCredentials);

        // get resource from server
        Resource srv_res = getResourceManager().getData(createResourcedresource, context, contextAdminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());
    }

    @Test
    public void testGetIdentifiedByID() throws Exception {
        Resource res = ResourceFactory.createResource("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, contextAdminCredentials);

        // get resource from server
        Resource tmp = new Resource(createResourcedresource.getId());
        Resource srv_res = getResourceManager().getData(tmp, context, contextAdminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());
    }

    @Test
    public void testGetIdentifiedByName() throws Exception {
        Resource res = ResourceFactory.createResource("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, contextAdminCredentials);

        // get resource from server
        Resource tmp = new Resource();
        tmp.setName(createResourcedresource.getName());
        Resource srv_res = getResourceManager().getData(tmp, context, contextAdminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());
    }

    @Test
    public void testDelete() throws Exception {
        Resource res = ResourceFactory.createResource("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, contextAdminCredentials);

        // get resource from server
        Resource srv_res = getResourceManager().getData(createResourcedresource, context, contextAdminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());

        // delete resource
        getResourceManager().delete(createResourcedresource, context, contextAdminCredentials);

        // try to get resource again, this MUST fail
        try {
            srv_res = getResourceManager().getData(createResourcedresource, context, contextAdminCredentials);
            fail("Expected that the resource was deleted!");
        } catch (NoSuchResourceException nsr) {
        }

    }

    @Test
    public void testcreateResourceDeletecreateResource() throws Exception {
        Resource res = ResourceFactory.createResource("tescase-createResource-resource-" + System.currentTimeMillis());
        Resource createResourcedresource = getResourceManager().create(res, context, contextAdminCredentials);

        // get resource from server
        Resource srv_res = getResourceManager().getData(createResourcedresource, context, contextAdminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());

        // delete resource
        getResourceManager().delete(createResourcedresource, context, contextAdminCredentials);

        // try to get resource again, this MUST fail
        try {
            srv_res = getResourceManager().getData(createResourcedresource, context, contextAdminCredentials);
            fail("Expected that the resource was deleted!");
        } catch (NoSuchResourceException nsr) {
        }

        // createResource again
        getResourceManager().create(res, context, contextAdminCredentials);

    }

    @Test
    public void testDeleteIdentifiedByName() throws Exception {
        Resource res = ResourceFactory.createResource("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, contextAdminCredentials);

        // get resource from server
        Resource srv_res = getResourceManager().getData(createResourcedresource, context, contextAdminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());

        // delete resource
        Resource tmp = new Resource();
        tmp.setName(createResourcedresource.getName());
        getResourceManager().delete(tmp, context, contextAdminCredentials);

        // try to get resource again, this MUST fail
        try {
            srv_res = getResourceManager().getData(createResourcedresource, context, contextAdminCredentials);
            fail("Expected that the resource was deleted!");
        } catch (NoSuchResourceException nsr) {
        }

    }

    @Test
    public void testDeleteIdentifiedByID() throws Exception {
        Resource res = ResourceFactory.createResource("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, contextAdminCredentials);

        // get resource from server
        Resource srv_res = getResourceManager().getData(createResourcedresource, context, contextAdminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());

        // delete resource
        Resource tmp = new Resource(createResourcedresource.getId());
        getResourceManager().delete(tmp, context, contextAdminCredentials);

        // try to get resource again, this MUST fail
        try {
            srv_res = getResourceManager().getData(createResourcedresource, context, contextAdminCredentials);
            fail("Expected that the resource was deleted!");
        } catch (NoSuchResourceException nsr) {
        }

    }

    @Test
    public void testlistResources() throws Exception {
        Resource res = ResourceFactory.createResource("tescase-createResource-resource-" + System.currentTimeMillis());
        getResourceManager().create(res, context, contextAdminCredentials);

        Resource[] srv_response = getResourceManager().search(context, "*", contextAdminCredentials);
        if (srv_response == null) {
            fail("server response was null");
            return;
        }
        assertTrue("Expected listResources size > 0 ", srv_response.length > 0);
    }
}
