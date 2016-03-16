/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

    public SearchRequest(final TaskSearchObject search, final int[] columns,
        final boolean failOnError) {
        this(search, columns, 0, null, failOnError);
    }

    public SearchRequest(final TaskSearchObject search, final int[] columns,
        final int sort, final Order order) {
        this(search, columns, sort, order, true);
    }

    public SearchRequest(final TaskSearchObject search, final int[] columns,
        final int sort, final Order order, final boolean failOnError) {
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
        } catch (final OXException e) {
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
            params.add(new Parameter(AJAXServlet.PARAMETER_ORDER, OrderFields
                .write(order)));
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
