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

package com.openexchange.ajax.subscribe.source.action;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.java.Strings;

/**
 * {@link ListSourcesRequest}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ListSourcesRequest extends AbstractSubscriptionSourceRequest<ListSourcesResponse> {

    private final String module;

    private List<String> columns;

    public ListSourcesRequest(final String module) {
        this.module = module;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getColumns() {
        return columns;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> params = new LinkedList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "listSources"));
        if (module != null) {
            params.add(new Parameter("module", module));
        }
        if (getColumns() != null) {
            params.add(new Parameter("columns", Strings.join(getColumns(), ",")));
        }
        return params.toArray(new Parameter[] {});
    }

    @Override
    public AbstractAJAXParser<? extends ListSourcesResponse> getParser() {
        return new AbstractAJAXParser<ListSourcesResponse>(getFailOnError()) {

            @Override
            protected ListSourcesResponse createResponse(final Response response) throws JSONException {
                return new ListSourcesResponse(response);
            }
        };
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        // TODO Auto-generated method stub
        return null;
    }

}
