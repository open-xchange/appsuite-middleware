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

package com.openexchange.ajax.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
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
public class ResourceUpdatesAJAXTest extends AbstractResourceTest {

    private Resource resource;

    /**
     * Initializes a new {@link ResourceUpdatesAJAXTest}.
     *
     * @param name
     */
    public ResourceUpdatesAJAXTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        resource = new Resource();
        resource.setAvailable(true);
        resource.setMail("my.resource@domain.tdl");
        resource.setSimpleName("SimpleName");
        resource.setDisplayName("DisplayName");
        resource.setDescription("Description");
        final ResourceNewResponse newResponse = Executor.execute(getSession(), new ResourceNewRequest(resource, true));
        resource.setIdentifier(newResponse.getID());
        resource.setLastModified(newResponse.getTimestamp());
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

    @Test
    public void testUpdatesSinceBeginning() throws Exception {
        final ResourceUpdatesResponse response = Executor.execute(getSession(), new ResourceUpdatesRequest(new Date(0), true));
        assertTrue("Should find more than 0 new elements", response.getNew().size() > 0);
        assertTrue("Should find more than 0 updated elements", response.getModified().size() > 0);
        List<Resource> modified = response.getModified();
        assertTrue(containsResource(modified, resource.getIdentifier()));
    }

    @Test
    public void testUpdates() throws Exception {
        Date since = new Date(resource.getLastModified().getTime() - 1);

        final ResourceUpdatesResponse response = Executor.execute(getSession(), new ResourceUpdatesRequest(since, true));

        List<Resource> modified = response.getModified();
        assertEquals("Should find one updated element", 1, modified.size());
        assertEquals("Should have matching ID", resource.getIdentifier(), modified.get(0).getIdentifier());
    }

    @Test
    public void testUpdatesShouldContainDeletes() throws Exception {
        Date since = new Date(resource.getLastModified().getTime() - 1);

        ResourceUpdatesResponse response = Executor.execute(getSession(), new ResourceUpdatesRequest(since, true));
        int deletedBefore = response.getDeleted().size();

        Executor.execute(getSession(), new ResourceDeleteRequest(resource));

        response = Executor.execute(getSession(), new ResourceUpdatesRequest(since, true));
        int deletedAfter = response.getDeleted().size();

        int identifier = resource.getIdentifier();
        resource = null; //so it does not get deleted in the tearDown()
        assertEquals("Should have one more element in deleted list after deletion", deletedAfter - 1, deletedBefore);
        assertTrue(containsResource(response.getDeleted(), identifier));

    }

}
