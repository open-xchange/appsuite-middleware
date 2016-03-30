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

package com.openexchange.ajax.resource;

import java.util.Date;
import java.util.List;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.resource.actions.ResourceDeleteRequest;
import com.openexchange.ajax.resource.actions.ResourceNewRequest;
import com.openexchange.ajax.resource.actions.ResourceNewResponse;
import com.openexchange.ajax.resource.actions.ResourceUpdatesRequest;
import com.openexchange.ajax.resource.actions.ResourceUpdatesResponse;
import com.openexchange.resource.Resource;


/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ResourceUpdatesAJAXTest extends AbstractResourceTest{

    private Resource resource;

    /**
     * Initializes a new {@link ResourceUpdatesAJAXTest}.
     * @param name
     */
    public ResourceUpdatesAJAXTest(String name) {
        super(name);
    }



    @Override
    protected void setUp() throws Exception {
        super.setUp();

        resource = new Resource();
        resource.setAvailable(true);
        resource.setMail("my.resource@domain.tdl");
        resource.setSimpleName("SimpleName");
        resource.setDisplayName("DisplayName");
        resource.setDescription("Description");
        final ResourceNewResponse newResponse = Executor.execute(getSession(),
                new ResourceNewRequest(resource, true));
        resource.setIdentifier(newResponse.getID());
        resource.setLastModified(newResponse.getTimestamp());
    }



    @Override
    protected void tearDown() throws Exception {
        if(resource != null) {
            Executor.execute(getSession(), new ResourceDeleteRequest(resource));
        }
        super.tearDown();
    }

    private boolean containsResource(List<Resource> resources, int resourceId) {
        boolean containsResource = false;
        for (Resource resource : resources) {
            if (resource.getIdentifier() == resourceId) {
                containsResource = true;
                break;
            }
        }
        return containsResource;
    }

    public void testUpdatesSinceBeginning() throws Exception{
        final ResourceUpdatesResponse response = Executor.execute(getSession(),
            new ResourceUpdatesRequest(new Date(0), true));
        assertTrue("Should find more than 0 new elements", response.getNew().size() > 0);
        assertTrue("Should find more than 0 updated elements", response.getModified().size() > 0);
        List<Resource> modified = response.getModified();
        assertTrue(containsResource(modified, resource.getIdentifier()));
    }

    public void testUpdates() throws Exception{
        Date since = new Date(resource.getLastModified().getTime() - 1);

        final ResourceUpdatesResponse response = Executor.execute(getSession(),
            new ResourceUpdatesRequest(since, true));

        List<Resource> modified = response.getModified();
        assertEquals("Should find one updated element", 1, modified.size());
        assertEquals("Should have matching ID", resource.getIdentifier(), modified.get(0).getIdentifier());
    }

    public void testUpdatesShouldContainDeletes() throws Exception{
        Date since = new Date(resource.getLastModified().getTime() - 1);

        ResourceUpdatesResponse response = Executor.execute(getSession(),
            new ResourceUpdatesRequest(since, true));
        int deletedBefore = response.getDeleted().size();

        Executor.execute(getSession(), new ResourceDeleteRequest(resource));

        response = Executor.execute(getSession(),
            new ResourceUpdatesRequest(since, true));
        int deletedAfter =  response.getDeleted().size();

        int identifier = resource.getIdentifier();
        resource = null; //so it does not get deleted in the tearDown()
        assertEquals("Should have one more element in deleted list after deletion", deletedAfter - 1, deletedBefore);
        assertTrue(containsResource(response.getDeleted(), identifier));

    }

}
