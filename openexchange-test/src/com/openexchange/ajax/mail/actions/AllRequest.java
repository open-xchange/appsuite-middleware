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

import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.framework.AbstractAllRequest;
import com.openexchange.groupware.search.Order;
import com.openexchange.mail.json.actions.AbstractMailAction;

/**
 * {@link AllRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="karsten.will@open-xchange.com">Karsten Will</a>
 */
public class AllRequest extends AbstractAllRequest<AllResponse> {

    private boolean threadSort;

    /**
     * Default constructor.
     */
    public AllRequest(final String servletPath, final int folderId, final int[] columns, final int sort, final Order order, final boolean failOnError) {
        super(servletPath, folderId, columns, sort, order, failOnError);
    }

    public AllRequest(final String servletPath, final int folderId, final int[] columns, final int sort, final Order order, final boolean failOnError, String categoryId) {
        super(servletPath, folderId, columns, sort, order, failOnError);
        this.categoryId = categoryId;
    }

    public AllRequest(final String servletPath, final int folderId, final String alias, final int sort, final Order order, final boolean failOnError) {
        super(servletPath, folderId, alias, sort, order, failOnError);
    }

    /**
     * Default constructor.
     */
    public AllRequest(final String folderPath, final int[] columns, final int sort, final Order order, final boolean failOnError) {
        super(AbstractMailRequest.MAIL_URL, folderPath, columns, sort, order, failOnError);
    }

    public AllRequest(final String folderPath, final int[] columns, final int sort, final Order order, final boolean failOnError, String categoryId) {
        super(AbstractMailRequest.MAIL_URL, folderPath, columns, sort, order, failOnError);
        this.categoryId = categoryId;
    }

    public AllRequest(final String folderPath, final String alias, final int sort, final Order order, final boolean failOnError) {
        super(AbstractMailRequest.MAIL_URL, folderPath, alias, sort, order, failOnError);
    }


    @Override
    public Parameter[] getParameters() {
        if (!threadSort) {
            return super.getParameters();
        }
        final List<Parameter> params = new ArrayList<Parameter>();
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL));
        params.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        if (columns != null) {
            params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, columns));
        }
        if (alias != null) {
            params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, alias));
        }
        params.add(new Parameter(AJAXServlet.PARAMETER_SORT, "thread"));
        params.add(new Parameter(AJAXServlet.PARAMETER_ORDER, OrderFields.write(order)));
        if (validateLimit()) {
            params.add(new Parameter(AJAXServlet.LEFT_HAND_LIMIT, leftHandLimit));
            params.add(new Parameter(AJAXServlet.RIGHT_HAND_LIMIT, rightHandLimit));
        }
        return params.toArray(new Parameter[params.size()]);
    }

    /**
     * Enables thread-sort.
     *
     * @param threadSort <code>true</code> to enable thread-sort; otherwise <code>false</code>
     * @return This ALL request with thread-sort enabled/disabled
     */
    public AllRequest setThreadSort(final boolean threadSort) {
        this.threadSort = threadSort;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AllParser getParser() {
        if (getColumns() != null) {
            return new AllParser(isFailOnError(), getColumns());
        }
        if (getAlias() != null) {
            if (getAlias().equals("all")) {
                return new AllParser(isFailOnError(), AbstractMailAction.FIELDS_ALL_ALIAS);
            }
            if (getAlias().equals("list")) {
                return new AllParser(isFailOnError(), AbstractMailAction.FIELDS_LIST_ALIAS);
            }
        }
        return null;
    }

}
