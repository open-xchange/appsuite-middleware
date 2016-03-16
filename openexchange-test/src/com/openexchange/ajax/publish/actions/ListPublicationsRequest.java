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

package com.openexchange.ajax.publish.actions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.java.Strings;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ListPublicationsRequest extends AbstractPublicationRequest<ListPublicationsResponse> {

    private List<String> columns; // a list of column names to load (id, entityId, entityModule, url, target)

    private Map<String, List<String>> dynamicColumns;

    private List<Integer> ids;

    public ListPublicationsRequest() {
        super();
    }

    public ListPublicationsRequest(List<Integer> ids, List<String> columns) {
        this();
        setIds(ids);
        setColumns(columns);
    }

    public ListPublicationsRequest(List<Integer> ids, List<String> columns, Map<String, List<String>> dynamicColumns) {
        this(ids, columns);
        setDynamicColumns(dynamicColumns);
    }

    @Override
    public Object getBody() throws JSONException {
        return new JSONArray(ids);
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
    public AbstractAJAXParser<? extends ListPublicationsResponse> getParser() {
        return new AbstractAJAXParser<ListPublicationsResponse>(isFailOnError()) {

            @Override
            protected ListPublicationsResponse createResponse(final Response response) throws JSONException {
                return new ListPublicationsResponse(response);
            }
        };
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

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }

    public List<Integer> getIds() {
        return ids;
    }

}
