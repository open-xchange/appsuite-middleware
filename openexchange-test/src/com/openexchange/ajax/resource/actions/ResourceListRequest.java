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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link ResourceListRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ResourceListRequest extends AbstractResourceRequest<ResourceListResponse> {

    private final boolean failOnError;

    private final int[] ids;

    /**
     * Initializes a new {@link ResourceListRequest}
     *
     * @param failOnError
     *            <code>true</code> to fail on error; otherwise
     *            <code>false</code>
     */
    public ResourceListRequest(final int[] ids, final boolean failOnError) {
        super();
        this.ids = ids;
        this.failOnError = failOnError;
    }

    public ResourceListRequest(final int[] ids) {
        this(ids, true);
    }

    @Override
    public Object getBody() throws JSONException {
        final JSONArray data = new JSONArray();
        for (final int id : ids) {
            final JSONObject jo = new JSONObject();
            jo.put(AJAXServlet.PARAMETER_ID, id);
            data.put(jo);
        }
        return data;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        final List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST));
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public ResourceListParser getParser() {
        return new ResourceListParser(failOnError);
    }

    private static final class ResourceListParser extends AbstractAJAXParser<ResourceListResponse> {

        /**
         * Default constructor.
         */
        ResourceListParser(final boolean failOnError) {
            super(failOnError);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected ResourceListResponse createResponse(final Response response) throws JSONException {
            return new ResourceListResponse(response);
        }
    }

}
