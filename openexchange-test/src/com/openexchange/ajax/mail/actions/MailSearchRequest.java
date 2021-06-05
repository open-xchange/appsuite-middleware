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

import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.Header;

/**
 * {@link MailSearchRequest}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class MailSearchRequest implements AJAXRequest<MailSearchResponse> {

    private final List<Parameter> params;

    private JSONArray body;

    private final MailSearchParser searchParser;

    /**
     * This constructor allows to set the parameters of the request.
     * For it to work properly, you also have to set the body of the
     * PUT request afterwards.
     *
     * @see #setBody(JSONArray)
     */
    public MailSearchRequest(final String folder, final int[] columns, final int orderBy, final String orderDir, final boolean failOnError) {
        params = new LinkedList<Parameter>();

        searchParser = new MailSearchParser(failOnError, columns);

        param(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);

        param(AJAXServlet.PARAMETER_INFOLDER, folder);

        param(AJAXServlet.PARAMETER_COLUMNS, join(columns));

        if (orderBy != -1) {
            param(AJAXServlet.PARAMETER_SORT, String.valueOf(orderBy));
            param(AJAXServlet.PARAMETER_ORDER, orderDir);
        }
    }

    /**
     * Convenience constructor that allows the body to be passed on a
     * argument.
     *
     */
    public MailSearchRequest(final JSONArray body, final String folder, final int[] columns, final int orderBy, final String orderDir, final boolean failOnError) {
        this(folder, columns, orderBy, orderDir, failOnError);
        setBody(body);
    }

    @Override
    public Object getBody() {
        return body;
    }

    public void setBody(JSONArray body) {
        this.body = body;
        param(AJAXServlet.PARAMETER_DATA, body.toString());
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() {
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public MailSearchParser getParser() {
        return searchParser;
    }

    @Override
    public String getServletPath() {
        return "/ajax/mail";
    }

    // TODO refactor: move to super class or helper
    private void param(final String key, final String value) {
        if (value != null) {
            params.add(new Parameter(key, value));
        }
    }

    // TODO refactor: move to super class or helper
    private String join(final int[] values) {
        final StringBuilder b = new StringBuilder();
        for (final int v : values) {
            b.append(v).append(", ");
        }
        b.setLength(b.length() - 2);
        return b.toString();
    }
}
