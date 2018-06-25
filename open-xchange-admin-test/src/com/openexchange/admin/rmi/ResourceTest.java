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

package com.openexchange.admin.rmi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.exceptions.NoSuchResourceException;

/**
 * {@link ResourceTest}
 *
 * @author cutmasta
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ResourceTest extends AbstractRMITest {

    private Context context;

    /**
     * Initialises a new {@link ResourceTest}.
     */
    public ResourceTest() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.admin.rmi.AbstractTest#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        context = getContextManager().create(adminCredentials);
    }

    @Test
    public void testCreateResource() throws Exception {
        Resource res = getTestResourceObject("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, adminCredentials);

        Resource[] srv_response = getResourceManager().search(context, "*", adminCredentials);
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
        Resource res = getTestResourceObject("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, adminCredentials);

        Resource[] srv_response = getResourceManager().search(context, "*", adminCredentials);
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
        getResourceManager().change(createResourcedresource, context, adminCredentials);

        // get resource from server and verify changed data
        Resource srv_res = getResourceManager().getData(createResourcedresource, context, adminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());

    }

    @Test
    public void testChangeNull() throws Exception {
        // set description attribute to null 
        Resource res = getTestResourceObject("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, adminCredentials);

        Resource[] srv_response = getResourceManager().search(context, "*", adminCredentials);
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
        getResourceManager().change(createResourcedresource, context, adminCredentials);

        // get resource from server and verify changed data
        Resource srv_res = getResourceManager().getData(createResourcedresource, context, adminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription()); // must be able to null description        

        assertNotNull("email cannot be null", srv_res.getEmail());
        assertNotNull("name cannot be null", srv_res.getName());
        assertNotNull("display name cannot be null", srv_res.getDisplayname());
    }

    @Test
    public void testGet() throws Exception {
        Resource res = getTestResourceObject("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, adminCredentials);

        // get resource from server
        Resource srv_res = getResourceManager().getData(createResourcedresource, context, adminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());
    }

    @Test
    public void testGetIdentifiedByID() throws Exception {
        Resource res = getTestResourceObject("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, adminCredentials);

        // get resource from server
        Resource tmp = new Resource(createResourcedresource.getId());
        Resource srv_res = getResourceManager().getData(tmp, context, adminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());
    }

    @Test
    public void testGetIdentifiedByName() throws Exception {
        Resource res = getTestResourceObject("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, adminCredentials);

        // get resource from server
        Resource tmp = new Resource();
        tmp.setName(createResourcedresource.getName());
        Resource srv_res = getResourceManager().getData(tmp, context, adminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());
    }

    @Test
    public void testDelete() throws Exception {
        Resource res = getTestResourceObject("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, adminCredentials);

        // get resource from server
        Resource srv_res = getResourceManager().getData(createResourcedresource, context, adminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());

        // delete resource
        getResourceManager().delete(createResourcedresource, context, adminCredentials);

        // try to get resource again, this MUST fail
        try {
            srv_res = getResourceManager().getData(createResourcedresource, context, adminCredentials);
            fail("Expected that the resource was deleted!");
        } catch (NoSuchResourceException nsr) {
        }

    }

    @Test
    public void testcreateResourceDeletecreateResource() throws Exception {
        Resource res = getTestResourceObject("tescase-createResource-resource-" + System.currentTimeMillis());
        Resource createResourcedresource = getResourceManager().create(res, context, adminCredentials);

        // get resource from server
        Resource srv_res = getResourceManager().getData(createResourcedresource, context, adminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());

        // delete resource
        getResourceManager().delete(createResourcedresource, context, adminCredentials);

        // try to get resource again, this MUST fail
        try {
            srv_res = getResourceManager().getData(createResourcedresource, context, adminCredentials);
            fail("Expected that the resource was deleted!");
        } catch (NoSuchResourceException nsr) {
        }

        // createResource again
        getResourceManager().create(res, context, adminCredentials);

    }

    @Test
    public void testDeleteIdentifiedByName() throws Exception {
        Resource res = getTestResourceObject("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, adminCredentials);

        // get resource from server
        Resource srv_res = getResourceManager().getData(createResourcedresource, context, adminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());

        // delete resource
        Resource tmp = new Resource();
        tmp.setName(createResourcedresource.getName());
        getResourceManager().delete(tmp, context, adminCredentials);

        // try to get resource again, this MUST fail
        try {
            srv_res = getResourceManager().getData(createResourcedresource, context, adminCredentials);
            fail("Expected that the resource was deleted!");
        } catch (NoSuchResourceException nsr) {
        }

    }

    @Test
    public void testDeleteIdentifiedByID() throws Exception {
        Resource res = getTestResourceObject("tescase-createResource-resource-" + System.currentTimeMillis());
        final Resource createResourcedresource = getResourceManager().create(res, context, adminCredentials);

        // get resource from server
        Resource srv_res = getResourceManager().getData(createResourcedresource, context, adminCredentials);

        assertEquals(createResourcedresource.getDescription(), srv_res.getDescription());
        assertEquals(createResourcedresource.getDisplayname(), srv_res.getDisplayname());
        assertEquals(createResourcedresource.getEmail(), srv_res.getEmail());
        assertEquals(createResourcedresource.getName(), srv_res.getName());

        // delete resource
        Resource tmp = new Resource(createResourcedresource.getId());
        getResourceManager().delete(tmp, context, adminCredentials);

        // try to get resource again, this MUST fail
        try {
            srv_res = getResourceManager().getData(createResourcedresource, context, adminCredentials);
            fail("Expected that the resource was deleted!");
        } catch (NoSuchResourceException nsr) {
        }

    }

    @Test
    public void testlistResources() throws Exception {
        Resource res = getTestResourceObject("tescase-createResource-resource-" + System.currentTimeMillis());
        getResourceManager().create(res, context, adminCredentials);

        Resource[] srv_response = getResourceManager().search(context, "*", adminCredentials);
        if (srv_response == null) {
            fail("server response was null");
            return;
        }
        assertTrue("Expected listResources size > 0 ", srv_response.length > 0);
    }

    public static Resource getTestResourceObject(String name) {
        Resource res = new Resource();
        //        res.setAvailable(true);
        res.setDescription("description of resource " + name);
        res.setDisplayname("displayname of resource " + name);
        res.setEmail("resource-email-" + name + "@" + TEST_DOMAIN);
        res.setName(name);
        return res;
    }
}
