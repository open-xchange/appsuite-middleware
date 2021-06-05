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

package com.openexchange.ajax.mail.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.OrderFields;
import com.openexchange.ajax.framework.AbstractAllRequest;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Strings;
import com.openexchange.mail.json.actions.AbstractMailAction;

/**
 * {@link AllRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="karsten.will@open-xchange.com">Karsten Will</a>
 */
public class AllRequest extends AbstractAllRequest<AllResponse> {

    private String categoryId;

    private boolean threadSort;

    private List<com.openexchange.ajax.framework.AJAXRequest.Parameter> additionalParams;

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
        this(folderPath, columns, sort, order, failOnError, Collections.<Parameter> emptyList());
    }

    public AllRequest(final String folderPath, final int[] columns, final int sort, final Order order, final boolean failOnError, List<Parameter> additionalParams) {
        super(AbstractMailRequest.MAIL_URL, folderPath, columns, sort, order, failOnError);
        this.additionalParams = additionalParams;
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
        if (Strings.isNotEmpty(categoryId)) {
            params.add(new Parameter("categoryid", categoryId));
        }
        if (additionalParams != null) {
            for (Parameter additional : additionalParams) {
                params.add(additional);
            }
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
