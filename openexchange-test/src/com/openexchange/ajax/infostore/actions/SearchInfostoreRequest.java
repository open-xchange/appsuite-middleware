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

package com.openexchange.ajax.infostore.actions;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.groupware.search.Order;

/**
 * Stores the parameter for searching for infoitems.
 *
 * @author <a href="mailto:markus.wagner@open-xchange.org">Markus Wagner</a>
 */
public class SearchInfostoreRequest extends AbstractInfostoreRequest<SearchInfostoreResponse> {

    final long folderId;

    final String title;

    final int[] columns;

    final int sort;

    final Order order;

    final boolean failOnError;

    private int limit;

    private int start;

    private int end;

    public SearchInfostoreRequest(final String title, final int[] columns) {
        this(-1, title, columns, true);
    }

    public SearchInfostoreRequest(final long folderId, final String title, final int[] columns) {
        this(folderId, title, columns, true);
    }

    public SearchInfostoreRequest(final long folderId, final String title, final int[] columns, final boolean failOnError) {
        this(folderId, title, columns, 0, null, -1, -1, -1, failOnError);
    }

    public SearchInfostoreRequest(final long folderId, final String title, final int[] columns, final int sort, final Order order, final int limit) {
        this(folderId, title, columns, sort, order, limit, -1, -1, true);
    }
    public SearchInfostoreRequest(final long folderId, final String title, final int[] columns, final int sort, final Order order, final int limit, final int start, final int end) {
        this(folderId, title, columns, sort, order, limit, start, end, true);
    }

    
    public SearchInfostoreRequest(final long folderId, final String title, final int[] columns, final int sort, final Order order, final int limit,final int start, final int end, final boolean failOnError) {
        super();
        this.folderId = folderId;
        this.title = title;
        this.columns = columns;
        this.sort = sort;
        this.order = order;
        this.limit = limit;
        this.start = start;
        this.end = end;
        this.failOnError = failOnError;
    }

    @Override
    public Object getBody() throws JSONException {
        final JSONObject json = new JSONObject();
        if (-1 != folderId) {
            json.put(AJAXServlet.PARAMETER_FOLDERID, folderId);
        }

        json.put(AJAXServlet.PARAMETER_SEARCHPATTERN, title);

        return json;
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
        params.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        if (null != order) {
            params.add(new Parameter(AJAXServlet.PARAMETER_SORT, sort));
            params.add(new Parameter(AJAXServlet.PARAMETER_ORDER, OrderFields.write(order)));
        }
        if (this.limit != -1) {
            params.add(new Parameter(AJAXServlet.PARAMETER_LIMIT, this.limit));
        }
        if (this.start != -1) {
            params.add(new Parameter(AJAXServlet.PARAMETER_START, this.start));
        }
        if (this.end != -1) {
            params.add(new Parameter(AJAXServlet.PARAMETER_END, this.end));
        }
        
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public AbstractAJAXParser<? extends SearchInfostoreResponse> getParser() {
        return new AbstractAJAXParser<SearchInfostoreResponse>(failOnError) {

            @Override
            protected SearchInfostoreResponse createResponse(final Response response) {
                return new SearchInfostoreResponse(response);
            }
        };
    }
}
