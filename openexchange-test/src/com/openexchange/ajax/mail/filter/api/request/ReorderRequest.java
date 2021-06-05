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

package com.openexchange.ajax.mail.filter.api.request;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.mail.filter.api.parser.ReorderParser;
import com.openexchange.ajax.mail.filter.api.response.ReorderResponse;

/**
 * {@link ReorderRequest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ReorderRequest extends AbstractMailFilterRequest<ReorderResponse> {

    private int[] ids;
    private String username;

    /**
     * Initialises a new {@link ReorderRequest}.
     */
    public ReorderRequest(int[] ids) {
        super();
        this.ids = ids;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> parameters = new LinkedList<Parameter>();
        parameters.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "reorder"));
        if (username != null) {
            parameters.add(new Parameter("username", username));
        }
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends ReorderResponse> getParser() {
        return new ReorderParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        JSONArray array = new JSONArray(ids.length);
        for (int i : ids) {
            array.put(i);
        }
        return array;
    }
}
