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

package com.openexchange.ajax.mail.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.groupware.search.Order;

/**
 * {@link SearchRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SearchRequest extends AbstractMailRequest<SearchResponse> {

    private final boolean failOnError;

    private final String folder;

    private final JSONObject searchObject;

    private final String[] patterns;

    private final int[] searchColumns;

    private final int sort;

    private final Order order;

    private final int[] columns;

    /**
     * Initializes a new {@link SearchRequest}.
     *
     * @param searchObject The search object: <tt>{"filter":{...}}</tt>
     * @param folder The mail folder fullname
     * @param columns The columns to output
     * @param sort The sort column
     * @param order The sort order
     * @param failOnError <code>true</code> to fail on error; otherwise <code>false</code>
     */
    public SearchRequest(final JSONObject searchObject, final String folder, final int[] columns, final int sort, final Order order, final boolean failOnError) {
        super();
        this.searchObject = searchObject;
        this.failOnError = failOnError;
        this.columns = columns;
        this.searchColumns = null;
        this.patterns = null;
        this.folder = folder;
        this.sort = sort;
        this.order = order;
    }

    /**
     * Initializes a new {@link SearchRequest}.
     * 
     * @param searchColumns The columns to search in
     * @param patterns The patterns to search for each column
     * @param folder The mail folder fullname
     * @param columns The columns to output
     * @param sort The sort column
     * @param order The sort order
     * @param failOnError <code>true</code> to fail on error; otherwise <code>false</code>
     */
    public SearchRequest(final int[] searchColumns, final String[] patterns, final String folder, final int[] columns, final int sort, final Order order, final boolean failOnError) {
        super();
        this.searchColumns = searchColumns;
        this.patterns = patterns;
        this.columns = columns;
        this.failOnError = failOnError;
        this.searchObject = null;
        this.folder = folder;
        this.sort = sort;
        this.order = order;
    }

    @Override
    public Parameter[] getParameters() {
        final List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH));
        params.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folder));
        params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        params.add(new Parameter(AJAXServlet.PARAMETER_SORT, sort));
        params.add(new Parameter(AJAXServlet.PARAMETER_ORDER, OrderFields.write(order)));
        return params.toArray(new Parameter[params.size()]);
    }

    /**
     * @return the failOnError
     */
    public boolean isFailOnError() {
        return failOnError;
    }

    @Override
    public SearchParser getParser() {
        return new SearchParser(failOnError, columns);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        if (null != searchObject) {
            return searchObject;
        }
        // Array
        final JSONArray ja = new JSONArray();
        for (int i = 0; i < searchColumns.length; i++) {
            final JSONObject jo = new JSONObject();
            jo.put("pattern", patterns[i]);
            jo.put("field", searchColumns[i]);
        }
        return ja;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.PUT;
    }

}
