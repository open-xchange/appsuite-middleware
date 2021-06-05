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

import java.io.IOException;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.resource.actions.ResourceDeleteRequest;
import com.openexchange.ajax.resource.actions.ResourceGetRequest;
import com.openexchange.exception.OXException;
import com.openexchange.resource.Resource;

/**
 * {@link ResourceDeleteAJAXTest} - Tests the DELETE request on resource servlet
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ResourceDeleteAJAXTest extends AbstractResourceTest {

    /**
     * Initializes a new {@link ResourceDeleteAJAXTest}
     *
     * @param name
     *            The test name
     */
    public ResourceDeleteAJAXTest() {
        super();
    }

    /**
     * Tests the <code>action=delete</code> request
     */
    @Test
    public void testDelete() throws OXException, JSONException, IOException {
        int id = -1;
        try {
            /*
             * Create resource
             */
            final Resource resource = new Resource();
            resource.setAvailable(true);
            resource.setMail("my.resource@domain.tdl");
            resource.setSimpleName(ResourceDeleteAJAXTest.class.getName());
            resource.setDisplayName(ResourceDeleteAJAXTest.class.getName());
            resource.setDescription(ResourceDeleteAJAXTest.class.getName());
            id = createResource(resource);
            /*
             * Obtain timestamp
             */
            final long clientLastModified = Executor.execute(getSession(), new ResourceGetRequest(id, true)).getTimestamp().getTime();
            /*
             * Perform update request
             */
            resource.setIdentifier(id);
            Executor.execute(getSession(), new ResourceDeleteRequest(resource, clientLastModified, true));

            id = -1;
        } finally {
            deleteResource(id);
        }
    }
}
