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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import org.json.JSONException;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.groupware.search.Order;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class CommonAllRequest implements AJAXRequest {

    private final String servletPath;

    private final String folderId;

    private final int[] columns;

    private final int sort;

    private final Order order;

    private final boolean failOnError;

    /**
     * Default constructor.
     */
    public CommonAllRequest(final String servletPath, final int folderId,
        final int[] columns, final int sort, final Order order,
        final boolean failOnError) {
        super();
        this.servletPath = servletPath;
        this.folderId = String.valueOf(folderId);
        this.columns = columns;
        this.sort = sort;
        this.order = order;
        this.failOnError = failOnError;
    }

    /**
     * Default constructor.
     */
    public CommonAllRequest(final String servletPath, final String folderPath,
        final int[] columns, final int sort, final Order order,
        final boolean failOnError) {
        super();
        this.servletPath = servletPath;
        this.folderId = folderPath;
        this.columns = columns;
        this.sort = sort;
        this.order = order;
        this.failOnError = failOnError;
    }

    /**
     * {@inheritDoc}
     */
    public String getServletPath() {
        return servletPath;
    }

    /**
     * {@inheritDoc}
     */
    public Object getBody() throws JSONException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Method getMethod() {
        return Method.GET;
    }

    /**
     * {@inheritDoc}
     */
    public Parameter[] getParameters() {
        final List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet
            .ACTION_ALL));
        params.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        if (null != order) {
            params.add(new Parameter(AJAXServlet.PARAMETER_SORT, sort));
            params.add(new Parameter(AJAXServlet.PARAMETER_ORDER, OrderFields
                .write(order)));
        }
        return params.toArray(new Parameter[params.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public CommonAllParser getParser() {
        return new CommonAllParser(failOnError, columns);
    }

    /**
     * @return the columns
     */
    protected int[] getColumns() {
        return columns;
    }

    /**
     * @return the failOnError
     */
    protected boolean isFailOnError() {
        return failOnError;
    }
}
