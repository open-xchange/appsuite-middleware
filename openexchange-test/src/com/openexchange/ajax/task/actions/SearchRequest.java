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

package com.openexchange.ajax.task.actions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;

/**
 * Stores the parameter for searching for tasks.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class SearchRequest extends AbstractTaskRequest<SearchResponse> {

    final TaskSearchObject search;

    final int[] columns;

    final int sort;

    final Order order;

    // TODO add unimplemented limit

    final boolean failOnError;

    public SearchRequest(final TaskSearchObject search, final int[] columns) {
        this(search, columns, true);
    }

    public SearchRequest(final TaskSearchObject search, final int[] columns, final boolean failOnError) {
        this(search, columns, 0, null, failOnError);
    }

    public SearchRequest(final TaskSearchObject search, final int[] columns, final int sort, final Order order) {
        this(search, columns, sort, order, true);
    }

    public SearchRequest(final TaskSearchObject search, final int[] columns, final int sort, final Order order, final boolean failOnError) {
        super();
        this.search = search;
        this.columns = AbstractTaskRequest.addGUIColumns(columns);
        this.sort = sort;
        this.order = order;
        this.failOnError = failOnError;
    }

    @Override
    public JSONObject getBody() throws JSONException {
        try {
            return TaskSearchJSONWriter.write(search);
        } catch (OXException e) {
            throw new JSONException(e);
        }
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        final List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH));
        params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        if (null != order) {
            params.add(new Parameter(AJAXServlet.PARAMETER_SORT, sort));
            params.add(new Parameter(AJAXServlet.PARAMETER_ORDER, OrderFields.write(order)));
        }
        final Date[] range = search.getRange();
        if (null != range && range.length == 2) {
            params.add(new Parameter(AJAXServlet.PARAMETER_START, range[0]));
            params.add(new Parameter(AJAXServlet.PARAMETER_END, range[1]));
        }
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public SearchParser getParser() {
        return new SearchParser(failOnError, columns);
    }
}
