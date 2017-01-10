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

package com.openexchange.ajax.infostore.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.search.Order;

/**
 * {@link AllInfostoreRequest}
 *
 * @author <a href="mailto:markus.wagner@open-xchange.com">Markus Wagner</a>
 */
public class AllInfostoreRequest extends AbstractInfostoreRequest<AllInfostoreResponse> {

    public static final int GUI_SORT = Metadata.TITLE;

    public static final Order GUI_ORDER = Order.ASCENDING;

    private int folderId;

    private int[] columns;

    private int sort;

    private Order order;

    public AllInfostoreRequest(final int folderId, final int[] columns, final int sort, final Order order) {
        this(folderId, columns, sort, order, true);
    }

    public AllInfostoreRequest(final int folderId, final int[] columns, final int sort, final Order order, boolean failOnError) {
        setFailOnError(failOnError);
        this.folderId = folderId;
        this.columns = columns;
        this.sort = sort;
        this.order = order;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AllInfostoreParser getParser() {
        return new AllInfostoreParser(getFailOnError());
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Method getMethod() {
        return Method.GET;
    }

    @Override
    public com.openexchange.ajax.framework.AJAXRequest.Parameter[] getParameters() throws IOException, JSONException {
        final List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL));
        params.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        if (columns != null) {
            params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        }
        if (null != order) {
            params.add(new Parameter(AJAXServlet.PARAMETER_SORT, sort));
            params.add(new Parameter(AJAXServlet.PARAMETER_ORDER, OrderFields.write(order)));
        }
        return params.toArray(new Parameter[params.size()]);
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

}
