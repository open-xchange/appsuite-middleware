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
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.groupware.search.Order;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractAllRequest<T extends AbstractColumnsResponse> implements AJAXRequest<T> {

    protected final String servletPath;

    protected final String folderId;

    protected final int[] columns;

    protected final String alias;

    protected final int sort;

    protected final Order order;

    protected final boolean failOnError;

    protected int leftHandLimit = -1;

    protected int rightHandLimit = -1;

    public AbstractAllRequest(final String servletPath, final int folderId, final int[] columns, final int sort, final Order order, final boolean failOnError) {
        super();
        this.servletPath = servletPath;
        this.folderId = String.valueOf(folderId);
        this.columns = columns;
        this.alias = null;
        this.sort = sort;
        this.order = order;
        this.failOnError = failOnError;
    }

    public AbstractAllRequest(final String servletPath, final int folderId, final String alias, final int sort, final Order order, final boolean failOnError) {
        super();
        this.servletPath = servletPath;
        this.folderId = String.valueOf(folderId);
        this.columns = null;
        this.alias = alias;
        this.sort = sort;
        this.order = order;
        this.failOnError = failOnError;
    }

    public AbstractAllRequest(final String servletPath, final String folderPath, final int[] columns, final int sort, final Order order, final boolean failOnError) {
        super();
        this.servletPath = servletPath;
        this.folderId = folderPath;
        this.columns = columns;
        this.alias = null;
        this.sort = sort;
        this.order = order;
        this.failOnError = failOnError;
    }

    public AbstractAllRequest(final String servletPath, final String folderPath, final String alias, final int sort, final Order order, final boolean failOnError) {
        super();
        this.servletPath = servletPath;
        this.folderId = folderPath;
        this.columns = null;
        this.alias = alias;
        this.sort = sort;
        this.order = order;
        this.failOnError = failOnError;
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
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
    public Parameter[] getParameters() {
        final List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL));
        params.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        if (columns != null) {
            params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        }
        if (alias != null) {
            params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, alias));
        }
        if (null != order) {
            params.add(new Parameter(AJAXServlet.PARAMETER_SORT, sort));
            params.add(new Parameter(AJAXServlet.PARAMETER_ORDER, OrderFields.write(order)));
        }
        if (validateLimit()) {
            params.add(new Parameter(AJAXServlet.LEFT_HAND_LIMIT, leftHandLimit));
            params.add(new Parameter(AJAXServlet.RIGHT_HAND_LIMIT, rightHandLimit));
        }
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public abstract AbstractColumnsParser<T> getParser();

    public int[] getColumns() {
        return columns;
    }

    public String getAlias() {
        return alias;
    }

    protected boolean isFailOnError() {
        return failOnError;
    }

    /**
     * Sets the leftHandLimit
     *
     * @param leftHandLimit the leftHandLimit to set
     */
    public void setLeftHandLimit(final int leftHandLimit) {
        this.leftHandLimit = leftHandLimit;
    }

    /**
     * Sets the rightHandLimit
     *
     * @param rightHandLimit the rightHandLimit to set
     */
    public void setRightHandLimit(final int rightHandLimit) {
        this.rightHandLimit = rightHandLimit;
    }

    protected final boolean validateLimit() {
        if ((rightHandLimit != -1 || leftHandLimit != -1)) {
            if (rightHandLimit < leftHandLimit) {
                throw new IllegalArgumentException("right-hand index is less than left-hand index");
            }
            return true;
        }
        return false;
    }

}
