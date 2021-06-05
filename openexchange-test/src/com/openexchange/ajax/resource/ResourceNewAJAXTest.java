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
import com.openexchange.ajax.resource.actions.ResourceNewRequest;
import com.openexchange.ajax.resource.actions.ResourceNewResponse;
import com.openexchange.exception.OXException;
import com.openexchange.resource.Resource;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ResourceNewAJAXTest} - Tests the NEW request on resource servlet
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ResourceNewAJAXTest extends AbstractResourceTest {

    /**
     * Initializes a new {@link ResourceNewAJAXTest}
     *
     * @param name
     *            The test name
     */
    public ResourceNewAJAXTest() {
        super();
    }

    /**
     * Tests the <code>action=new</code> request
     */
    @Test
    public void testNew() throws OXException, JSONException, IOException {
        int id = -1;
        try {
            /*
             * Create resource
             */
            final Resource resource = new Resource();
            resource.setAvailable(true);
            resource.setMail("my.resource@domain.tdl");
            resource.setSimpleName("MySimpleResourceIdentifier");
            resource.setDisplayName("Resource 1337");
            resource.setDescription("MySimpleResourceIdentifier - Resource 1337");
            /*
             * Perform new request
             */
            final ResourceNewResponse newResponse = Executor.execute(getSession(), new ResourceNewRequest(resource, true));
            id = newResponse.getID();
            assertTrue("New request failed", id > 0);

        } finally {
            deleteResource(id);
        }
    }

    @Test
    public void testNew_noBody_returnException() throws OXException, JSONException, IOException {
        final ResourceNewResponse newResponse = Executor.execute(getSession(), new ResourceNewRequest(null, false));

        assertTrue(newResponse.hasError());
        assertTrue(AjaxExceptionCodes.MISSING_REQUEST_BODY.equals(newResponse.getException()));
    }

}
