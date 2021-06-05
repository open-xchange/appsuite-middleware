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

package com.openexchange.ajax.appointment.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.java.Strings;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class SearchRequest extends AbstractAppointmentRequest<SearchResponse> {

    private final JSONObject body = new JSONObject();

    private final SearchParser searchParser;

    private final List<Parameter> params = new ArrayList<Parameter>();

    public SearchRequest(String pattern, int folderId, int[] columns) {
        this(pattern, folderId, columns, true);
    }

    public SearchRequest(final String pattern, final int inFolder, final int[] columns, final boolean failOnError) {
        this(pattern, inFolder, null, null, columns, -1, null, false, failOnError);
    }

    public SearchRequest(final String pattern, final int inFolder, final Date startDate, final Date endDate, final int[] columns, final int orderBy, final String orderDir, final boolean recurrenceMaster, final boolean failOnError) {
        super();
        searchParser = new SearchParser(failOnError, columns);

        param(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);
        param(AJAXServlet.PARAMETER_COLUMNS, Strings.join(columns, ", "));
        if (orderBy != -1) {
            param(AJAXServlet.PARAMETER_SORT, String.valueOf(orderBy));
            param(AJAXServlet.PARAMETER_ORDER, orderDir);
        }
        param(AJAXServlet.PARAMETER_START, startDate);
        param(AJAXServlet.PARAMETER_END, endDate);
        if (recurrenceMaster) {
            param(AJAXServlet.PARAMETER_RECURRENCE_MASTER, String.valueOf(recurrenceMaster));
        }

        try {
            if (inFolder != -1) {
                body.put(AJAXServlet.PARAMETER_INFOLDER, inFolder);
            }
            if (pattern != null) {
                body.put(SearchFields.PATTERN, pattern);
            }
        } catch (JSONException e) {
            throw new IllegalStateException(e); // Shouldn't happen
        }

    }

    private void param(final String key, final String value) {
        if (value != null) {
            params.add(new Parameter(key, value));
        }
    }

    private void param(final String key, final Date value) {
        if (value != null) {
            params.add(new Parameter(key, value));
        }
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public SearchParser getParser() {
        return searchParser;
    }

    @Override
    public Object getBody() {
        return body;
    }
}
