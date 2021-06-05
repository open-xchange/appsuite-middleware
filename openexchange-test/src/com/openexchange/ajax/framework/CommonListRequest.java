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

package com.openexchange.ajax.framework;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.DataFields;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class CommonListRequest implements AJAXRequest<CommonListResponse> {

    private final String servletPath;

    protected final ListIDs identifier;

    private final int[] columns;

    private final String alias;

    private final boolean failOnError;

    public CommonListRequest(final String servletPath, final int[][] folderAndObjectIds, final int[] columns) {
        this(servletPath, folderAndObjectIds, columns, true);
    }

    public CommonListRequest(final String servletPath, final int[][] folderAndObjectIds, final int[] columns, final boolean failOnError) {
        this(servletPath, createListIDs(folderAndObjectIds), columns, failOnError);
    }

    private static ListIDs createListIDs(final int[][] folderAndObjectIds) {
        final ListIDs retval = new ListIDs();
        for (int i = 0; i < folderAndObjectIds.length; i++) {
            retval.add(new ListIDInt(folderAndObjectIds[i][0], folderAndObjectIds[i][1]));
        }
        return retval;
    }

    public CommonListRequest(final String servletPath, final String[][] folderAndObjectIds, final int[] columns) {
        this(servletPath, folderAndObjectIds, columns, true);
    }

    public CommonListRequest(final String servletPath, final String[][] folderAndObjectIds, final int[] columns, final boolean failOnError) {
        this(servletPath, createListIDs(folderAndObjectIds), columns, failOnError);
    }

    private static ListIDs createListIDs(final String[][] folderAndObjectIds) {
        final ListIDs retval = new ListIDs();
        for (int i = 0; i < folderAndObjectIds.length; i++) {
            retval.add(new ListIDString(folderAndObjectIds[i][0], folderAndObjectIds[i][1]));
        }
        return retval;
    }

    public CommonListRequest(final String servletPath, final ListIDs identifier, final int[] columns, final boolean failOnError) {
        super();
        this.servletPath = servletPath;
        this.identifier = identifier;
        this.columns = columns;
        this.alias = null;
        this.failOnError = failOnError;
    }

    public CommonListRequest(final String servletPath, final int[][] folderAndObjectIds, final String alias) {
        this(servletPath, folderAndObjectIds, alias, true);
    }

    public CommonListRequest(final String servletPath, final int[][] folderAndObjectIds, final String alias, final boolean failOnError) {
        this(servletPath, createListIDs(folderAndObjectIds), alias, failOnError);
    }

    public CommonListRequest(final String servletPath, final String[][] folderAndObjectIds, final String alias) {
        this(servletPath, folderAndObjectIds, alias, true);
    }

    public CommonListRequest(final String servletPath, final String[][] folderAndObjectIds, final String alias, final boolean failOnError) {
        this(servletPath, createListIDs(folderAndObjectIds), alias, failOnError);
    }

    public CommonListRequest(final String servletPath, final ListIDs identifier, final String alias, final boolean failOnError) {
        super();
        this.servletPath = servletPath;
        this.identifier = identifier;
        this.columns = null;
        this.alias = alias;
        this.failOnError = failOnError;
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    @Override
    public Object getBody() throws JSONException {
        final JSONArray array = new JSONArray();
        for (int i = 0; i < identifier.size(); i++) {
            final ListID ids = identifier.get(i);
            final JSONObject json = new JSONObject();
            json.put(AJAXServlet.PARAMETER_INFOLDER, ids.getFolder());
            json.put(DataFields.ID, ids.getObject());
            array.put(json);
        }
        return array;
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        if (columns != null) {
            return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST), new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns)
            };
        }
        if (alias != null) {
            return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_LIST), new Parameter(AJAXServlet.PARAMETER_COLUMNS, alias)
            };
        }
        return null;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    @Override
    public CommonListParser getParser() {
        return new CommonListParser(failOnError, columns);
    }
}
