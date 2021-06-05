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

package com.openexchange.ajax.framework;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class MultipleRequest<T extends AbstractAJAXResponse> implements AJAXRequest<MultipleResponse<T>> {

    private final AJAXRequest<T>[] requests;

    public MultipleRequest(final AJAXRequest<T>[] requests) {
        this.requests = requests.clone();
    }

    public static <T extends AbstractAJAXResponse> MultipleRequest<T> create(final AJAXRequest<T>... requests) {
        return new MultipleRequest<T>(requests);
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    @Override
    public Parameter[] getParameters() {
        return new Parameter[0];
    }

    @Override
    public String getServletPath() {
        return "/ajax/multiple";
    }

    @Override
    public Object getBody() throws JSONException, IOException {
        final JSONArray array = new JSONArray();
        for (final AJAXRequest<?> request : requests) {
            final JSONObject object = new JSONObject();
            final String path = request.getServletPath();
            String module = path.substring(path.lastIndexOf('/') + 1);
            if ("folders".equals(module)) {
                module = "folder";
            }
            object.put("module", module);
            for (final Parameter parameter : request.getParameters()) {
                object.put(parameter.getName(), parameter.getValue());
            }
            object.put(AJAXServlet.PARAMETER_DATA, request.getBody());
            array.put(object);
        }
        return array.toString();
    }

    @Override
    public MultipleParser<T> getParser() {
        return new MultipleParser<T>(requests);
    }
}
