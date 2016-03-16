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
