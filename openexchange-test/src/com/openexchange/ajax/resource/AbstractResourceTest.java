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
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.resource.actions.ResourceDeleteRequest;
import com.openexchange.ajax.resource.actions.ResourceGetRequest;
import com.openexchange.ajax.resource.actions.ResourceGetResponse;
import com.openexchange.ajax.resource.actions.ResourceNewRequest;
import com.openexchange.exception.OXException;
import com.openexchange.resource.Resource;

/**
 * {@link AbstractResourceTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public abstract class AbstractResourceTest extends AbstractAJAXSession {

    /**
     * Default constructor.
     *
     * @param name
     *            name of the test.
     */
    protected AbstractResourceTest() {
        super();
    }

    /**
     * Gets the client time zone
     *
     * @return The client time zone
     * @throws OXException
     *             If an AJAX error occurs
     * @throws IOException
     *             If an I/O error occurs
     * @throws JSONException
     *             If a JSON error occurs
     */
    protected TimeZone getTimeZone() throws OXException, IOException, JSONException {
        return getClient().getValues().getTimeZone();
    }

    /**
     * Gets the resource identified through specified <code>resourceId</code>
     *
     * @param resourceId
     *            The resource ID
     * @return The resource identified through specified <code>resourceId</code>
     *         or <code>null</code> on an invalid <code>resourceId</code>
     * @throws OXException
     *             If an AJAX error occurs
     * @throws JSONException
     *             If a JSON error occurs
     * @throws IOException
     *             If an I/O error occurs
     */
    protected Resource getResource(final int resourceId) throws OXException, JSONException, IOException {
        if (resourceId <= 0) {
            return null;
        }
        return Executor.execute(getSession(), new ResourceGetRequest(resourceId, true)).getResource();
    }

    /**
     * Deletes the resource identified through specified <code>resourceId</code>
     * .
     *
     * @param resourceId
     *            The resource ID
     * @throws OXException
     *             If an AJAX error occurs
     * @throws JSONException
     *             If a JSON error occurs
     * @throws IOException
     *             If an I/O error occurs
     */
    protected void deleteResource(final int resourceId) throws OXException, JSONException, IOException {
        if (resourceId <= 0) {
            return;
        }
        /*
         * Perform GET to hold proper timestamp
         */
        final ResourceGetResponse getResponse = Executor.execute(getSession(), new ResourceGetRequest(resourceId, true));
        final Date timestamp = getResponse.getTimestamp();
        /*
         * Perform delete request
         */
        Executor.execute(getSession(), new ResourceDeleteRequest(getResponse.getResource(), timestamp.getTime(), true));
    }

    /**
     * Creates specified resource
     *
     * @param toCreate
     *            The resource to create
     * @return The ID of the newly created resource
     * @throws OXException
     *             If an AJAX error occurs
     * @throws JSONException
     *             If a JSON error occurs
     * @throws IOException
     *             If an I/O error occurs
     */
    protected int createResource(final Resource toCreate) throws OXException, JSONException, IOException {
        /*
         * Perform new request
         */
        final int id = (Executor.execute(getSession(), new ResourceNewRequest(toCreate, true))).getID();
        return id;
    }
}
