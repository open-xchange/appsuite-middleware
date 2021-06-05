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

package com.openexchange.ajax.jslob.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.jslob.DefaultJSlob;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobId;

/**
 * {@link ListRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ListRequest extends AbstractJSlobRequest<ListResponse> {

    private final String[] identifiers;
    private final boolean failOnError;

    /**
     * Initializes a new {@link ListRequest}.
     */
    public ListRequest(final String... identifiers) {
        this(true, identifiers);
    }

    /**
     * Initializes a new {@link ListRequest}.
     */
    public ListRequest(final boolean failOnError, final String... identifiers) {
        super();
        this.failOnError = failOnError;
        this.identifiers = identifiers;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return com.openexchange.ajax.framework.AJAXRequest.Method.PUT;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        final List<Parameter> list = new LinkedList<Parameter>();
        list.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST));
        return list.toArray(new Parameter[list.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends ListResponse> getParser() {
        return new ListParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        final int length = identifiers.length;
        final JSONArray jArray = new JSONArray(length);
        for (int i = 0; i < length; i++) {
            jArray.put(identifiers[i]);
        }
        return jArray;
    }

    private static class ListParser extends AbstractAJAXParser<ListResponse> {

        /**
         * Initializes a new {@link ListParser}.
         */
        protected ListParser(boolean failOnError) {
            super(failOnError);
        }

        @Override
        protected ListResponse createResponse(Response response) throws JSONException {
            final JSONArray jArray = (JSONArray) response.getData();
            final int length = jArray.length();
            final List<JSlob> jSlobs = new ArrayList<JSlob>(length);
            for (int i = 0; i < length; i++) {
                final JSONObject jObject = jArray.getJSONObject(i);
                final DefaultJSlob jSlob = new DefaultJSlob(jObject.getJSONObject("tree"));
                jSlob.setMetaObject(jObject.optJSONObject("meta"));
                jSlob.setId(new JSlobId(null, jObject.getString("id"), 0, 0));
                jSlobs.add(jSlob);
            }
            return new ListResponse(response, jSlobs);
        }
    }

}
