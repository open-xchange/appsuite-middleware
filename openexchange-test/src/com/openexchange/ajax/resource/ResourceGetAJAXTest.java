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
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.resource.actions.ResourceGetRequest;
import com.openexchange.ajax.resource.actions.ResourceGetResponse;
import com.openexchange.exception.OXException;
import com.openexchange.resource.Resource;

/**
 * {@link ResourceGetAJAXTest} - Tests the GET request on resource servlet
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ResourceGetAJAXTest extends AbstractResourceTest {

    /**
     * Initializes a new {@link ResourceGetAJAXTest}
     *
     * @param name
     *            The test name
     */
    public ResourceGetAJAXTest() {
        super();
    }

    /**
     * Tests the <code>action=get</code> request
     */
    @Test
    public void testUpdate() throws OXException, JSONException, IOException {
        int id = -1;
        try {
            /*
             * Create resource
             */
            Resource resource = new Resource();
            resource.setAvailable(true);
            resource.setMail("my.resource@domain.tld");
            resource.setSimpleName(ResourceGetAJAXTest.class.getName());
            resource.setDisplayName(ResourceGetAJAXTest.class.getName());
            resource.setDescription(ResourceGetAJAXTest.class.getName());
            id = createResource(resource);
            /*
             * Perform GET
             */
            final ResourceGetResponse response = Executor.execute(getSession(), new ResourceGetRequest(id, true));

            resource = response.getResource();
            assertTrue("GET failed", resource != null && resource.getIdentifier() == id);

            assertTrue(((JSONObject) response.getData()).has("last_modified_utc"));

        } finally {
            deleteResource(id);
        }
    }
}
