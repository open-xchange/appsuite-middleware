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

package com.openexchange.ajax.framework;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Strings;

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

    protected String categoryId;

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
        if (!Strings.isEmpty(categoryId)) {
            params.add(new Parameter("categoryid", categoryId));
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
