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

package com.openexchange.ajax.contact.action;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.CommonSearchResponse;

/**
 * @author Tobias Friedrich <tobias.friedrich@open-xchange.com>
 */
public class AutocompleteRequest extends AbstractContactRequest<CommonSearchResponse> {

    private final List<AJAXRequest.Parameter> params = new ArrayList<AJAXRequest.Parameter>();
    private final SearchParser searchParser;

    public AutocompleteRequest(String query, boolean requireEmail, String inFolder, int[] columns, boolean failOnError) {
        this(query, requireEmail, inFolder, columns, -1, null, null, failOnError);
    }

    public AutocompleteRequest(String query, boolean requireEmail, int[] columns, int orderBy, String orderDir, String collation, boolean failOnError) {
        this(query, requireEmail, null, columns, orderBy, orderDir, collation, failOnError);
    }

    public AutocompleteRequest(String query, boolean requireEmail, String inFolder, int[] columns, int orderBy, String orderDir, String collation, boolean failOnError) {
        super();
        this.searchParser = new SearchParser(failOnError, columns);
        params.add(new AJAXRequest.Parameter(AJAXServlet.PARAMETER_ACTION, "autocomplete"));
        params.add(new AJAXRequest.Parameter("query", query));
        params.add(new AJAXRequest.Parameter("email", String.valueOf(requireEmail)));
        if (null != columns) {
            params.add(new AJAXRequest.Parameter(AJAXServlet.PARAMETER_COLUMNS, getColumns(columns)));
        }
        if (-1 != orderBy) {
            params.add(new AJAXRequest.Parameter(AJAXServlet.PARAMETER_SORT, String.valueOf(orderBy)));
        }
        if (null != orderDir) {
            params.add(new AJAXRequest.Parameter(AJAXServlet.PARAMETER_ORDER, orderDir));
        }
        if (null != collation) {
            params.add(new AJAXRequest.Parameter(AJAXServlet.PARAMETER_COLLATION, collation));
        }
        if (null != inFolder) {
            params.add(new AJAXRequest.Parameter(AJAXServlet.PARAMETER_INFOLDER, inFolder));
        }
    }

    @Override
    public AJAXRequest.Method getMethod() {
        return AJAXRequest.Method.GET;
    }

    @Override
    public AJAXRequest.Parameter[] getParameters() {
        return params.toArray(new AJAXRequest.Parameter[params.size()]);
    }

    @Override
    public AbstractAJAXParser<SearchResponse> getParser() {
        return searchParser;
    }

    @Override
    public Object getBody() {
        return null;
    }

    private static String getColumns(int[] values) {
        StringBuilder b = new StringBuilder();
        for (int v : values) {
            b.append(v).append(",");
        }
        b.setLength(b.length() - 1);
        return b.toString();
    }

}
