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

package com.openexchange.ajax.snippet.actions;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link ListSnippetRequest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ListSnippetRequest extends AbstractSnippetRequest<ListSnippetResponse> {

    private JSONArray array;

    /**
     * Initializes a new {@link ListSnippetRequest}.
     * 
     * @param failOnError
     */
    protected ListSnippetRequest(boolean failOnError) {
        super(failOnError);
    }

    /**
     * Initializes a new {@link ListSnippetRequest}.
     * 
     * @param body
     * @param failOnError
     */
    public ListSnippetRequest(final JSONArray array, final boolean failOnError) {
        super(failOnError);
        this.array = array;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> params = new LinkedList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST));
        return params.toArray(new Parameter[] {});
    }

    @Override
    public AbstractAJAXParser<? extends ListSnippetResponse> getParser() {
        return new ListSnippetParser(failOnError);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return array;
    }

}
