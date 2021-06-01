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

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.resource.actions.ResourceAllRequest;
import com.openexchange.ajax.resource.actions.ResourceAllResponse;
import com.openexchange.exception.OXException;
import com.openexchange.resource.Resource;

/**
 * {@link ResourceAllAJAXTest} - Tests the ALL request on resource servlet
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ResourceAllAJAXTest extends AbstractResourceTest {

    /**
     * Initializes a new {@link ResourceAllAJAXTest}
     *
     * @param name
     *            The test name
     */
    public ResourceAllAJAXTest() {
        super();
    }

    /**
     * Tests the <code>action=all</code>
     */
    @Test
    public void testAll() throws OXException, JSONException, IOException {
        int id = -1;
        try {
            /*
             * Create a resource
             */
            final Resource resource = new Resource();
            resource.setAvailable(true);
            resource.setMail("my.resource@dom" + System.currentTimeMillis() + "ain.tdl");
            resource.setSimpleName(ResourceAllAJAXTest.class.getName());
            resource.setDisplayName(ResourceAllAJAXTest.class.getName());
            resource.setDescription(ResourceAllAJAXTest.class.getName());
            id = createResource(resource);

            /*
             * Perform all request
             */
            final ResourceAllResponse allResponse = Executor.execute(getSession(), new ResourceAllRequest(true));
            final int[] ids = allResponse.getIDs();
            assertTrue("All request failed", ids != null && ids.length >= 1);

        } finally {
            try {
                deleteResource(id);
            } catch (Exception e) {
                System.err.println("Temporary created resource with ID " + id + " could not be deleted: " + e.getMessage());
            }
        }
    }
}
