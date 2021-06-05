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

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ResourceSearchRequest extends AbstractResourceRequest<ResourceSearchResponse> {

    private boolean failOnError;

    private String searchPattern;

    public ResourceSearchRequest(String pattern, boolean failOnError) {
        super();
        setSearchPattern(pattern);
        setFailOnError(failOnError);
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @Override
    public Object getBody() throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("pattern", getSearchPattern());
        return jo;
    }

    public String getSearchPattern() {
        return this.searchPattern;
    }

    public void setSearchPattern(String pattern) {
        this.searchPattern = pattern;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        return new Params(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH).toArray();
    }

    @Override
    public AbstractAJAXParser<? extends ResourceSearchResponse> getParser() {
        return new AbstractAJAXParser<ResourceSearchResponse>(failOnError) {

            @Override
            protected ResourceSearchResponse createResponse(final Response response) {
                return new ResourceSearchResponse(response);
            }
        };
    }

}
