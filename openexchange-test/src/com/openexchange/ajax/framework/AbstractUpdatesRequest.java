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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.groupware.search.Order;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractUpdatesRequest<T extends AbstractColumnsResponse> implements AJAXRequest<T> {

    private final String servletPath;
    private final int folderId;
    private final int[] columns;
    private final int sort;
    private final Order order;
    private final Date lastModified;
    private final Ignore ignore;
    private final boolean failOnError;

    public AbstractUpdatesRequest(String servletPath, int folderId, int[] columns, int sort, Order order, Date lastModified, boolean failOnError) {
        this(servletPath, folderId, columns, sort, order, lastModified, Ignore.DELETED, failOnError);
    }

    public AbstractUpdatesRequest(String servletPath, int folderId, int[] columns, int sort, Order order, Date lastModified, Ignore ignore, boolean failOnError) {
        super();
        this.servletPath = servletPath;
        this.folderId = folderId;
        this.columns = columns;
        this.sort = sort;
        this.order = order;
        this.lastModified = lastModified;
        this.ignore = ignore;
        this.failOnError = failOnError;
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    @Override
    public Parameter[] getParameters() {
        final List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATES));
        if (-1 != folderId) {
            params.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        }
        params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, getColumns()));
        if (null != order) {
            params.add(new Parameter(AJAXServlet.PARAMETER_SORT, sort));
            params.add(new Parameter(AJAXServlet.PARAMETER_ORDER, OrderFields.write(order)));
        }
        params.add(new Parameter(AJAXServlet.PARAMETER_TIMESTAMP, lastModified));
        params.add(new Parameter(AJAXServlet.PARAMETER_IGNORE, ignore.getValue()));
        return params.toArray(new Parameter[params.size()]);
    }

    public int[] getColumns() {
        return columns;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public enum Ignore {
        DELETED("deleted"),
        CHANGED("changed"),
        NONE("none");

        private final String value;

        private Ignore(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
