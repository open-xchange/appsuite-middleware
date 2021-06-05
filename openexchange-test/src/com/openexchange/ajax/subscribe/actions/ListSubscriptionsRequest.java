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

package com.openexchange.ajax.subscribe.actions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.java.JSON;
import com.openexchange.java.Strings;

/**
 * {@link ListSubscriptionsRequest}
 *
 * @author <a href="mailto:firstname.lastname@open-xchange.com">Firstname Lastname</a>
 */
public class ListSubscriptionsRequest extends AbstractSubscriptionRequest<ListSubscriptionsResponse> {

    private List<Integer> IDs;

    private List<String> columns; // a list of column names to load (id, entityId, entityModule, url, target)

    private Map<String, List<String>> dynamicColumns;

    public void setIDs(List<Integer> iDs) {
        IDs = iDs;
    }

    public List<Integer> getIDs() {
        return IDs;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setDynamicColumns(Map<String, List<String>> dynamicColumns) {
        this.dynamicColumns = dynamicColumns;
    }

    public Map<String, List<String>> getDynamicColumns() {
        return dynamicColumns;
    }

    public ListSubscriptionsRequest(List<Integer> ids, List<String> columns) {
        super();
        setIDs(ids);
        setColumns(columns);
    }

    public ListSubscriptionsRequest(List<Integer> ids, List<String> columns, Map<String, List<String>> dynamicColumns) {
        this(ids, columns);
        setDynamicColumns(dynamicColumns);
    }

    @Override
    public Object getBody() throws JSONException {
        return JSON.collection2jsonArray(getIDs());
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        List<Parameter> params = new LinkedList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST));

        if (getColumns() != null) {
            params.add(new Parameter("columns", Strings.join(getColumns(), ",")));
        }

        if (getDynamicColumns() != null) {
            for (String plugin : getDynamicColumns().keySet()) {
                params.add(new Parameter(plugin, Strings.join(getDynamicColumns().get(plugin), ",")));
            }
        }
        return params.toArray(new Parameter[] {});
    }

    @Override
    public AbstractAJAXParser<ListSubscriptionsResponse> getParser() {
        return new AbstractAJAXParser<ListSubscriptionsResponse>(getFailOnError()) {

            @Override
            protected ListSubscriptionsResponse createResponse(final Response response) throws JSONException {
                return new ListSubscriptionsResponse(response);
            }
        };
    }

}
