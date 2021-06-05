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

package com.openexchange.ajax.resource.actions;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;
import com.openexchange.resource.Resource;
import com.openexchange.resource.json.ResourceWriter;

/**
 * {@link ResourceUpdateRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ResourceUpdateRequest extends AbstractResourceRequest<ResourceUpdateResponse> {

    private final boolean failOnError;

    private final JSONObject resourceJSON;

    private final long clientLastModified;

    private final Resource resource;

    /**
     * Initializes a new {@link ResourceUpdateRequest}
     *
     * @param resource
     *            The resource containing values to update
     * @param clientLastModified
     *            The client last-modified timestamp to verify request on server
     *            (possible concurrent modification)
     * @param failOnError
     *            <code>true</code> to fail on error; otherwise
     *            <code>false</code>
     * @throws JSONException
     *             If a JSON error occurs
     */
    public ResourceUpdateRequest(final Resource resource, final long clientLastModified, final boolean failOnError) throws JSONException {
        super();
        this.failOnError = failOnError;
        this.clientLastModified = clientLastModified;
        this.resource = resource;
        resourceJSON = ResourceWriter.writeResource(resource);
    }

    @Override
    public Object getBody() throws JSONException {
        return resourceJSON;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        return new Params(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE, AJAXServlet.PARAMETER_TIMESTAMP, String.valueOf(clientLastModified), AJAXServlet.PARAMETER_ID, String.valueOf(resource.getIdentifier())).toArray();
    }

    @Override
    public ResourceUpdateParser getParser() {
        return new ResourceUpdateParser(failOnError);
    }

    private static final class ResourceUpdateParser extends AbstractAJAXParser<ResourceUpdateResponse> {

        /**
         * Default constructor.
         */
        ResourceUpdateParser(final boolean failOnError) {
            super(failOnError);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected ResourceUpdateResponse createResponse(final Response response) throws JSONException {
            return new ResourceUpdateResponse(response);
        }
    }

}
