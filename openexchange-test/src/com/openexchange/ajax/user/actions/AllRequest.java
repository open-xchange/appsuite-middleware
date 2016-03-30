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

package com.openexchange.ajax.user.actions;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;

/**
 * {@link AllRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AllRequest extends AbstractUserRequest<AllResponse> {

    /**
     * Default columns to request: ID, FOLDER_ID, DISPLAY_NAME, GROUPS, and CONTACT_ID.
     */
    public static final int[] COLS = { 1, 20, 500, 613, 614 };

    private final int[] cols;

    private Integer sortColumn;

    private Boolean orderDirection;

    private Integer leftHandLimit;

    private Integer rightHandLimit;

    public AllRequest(final int[] cols) {
        super();
        this.cols = null == cols ? COLS : cols;
        orderDirection = Boolean.TRUE;
    }

    /**
     * Gets the sortColumn
     *
     * @return The sortColumn
     */
    public Integer getSortColumn() {
        return sortColumn;
    }

    /**
     * Sets the sortColumn
     *
     * @param sortColumn The sortColumn to set
     */
    public void setSortColumn(final Integer sortColumn) {
        this.sortColumn = sortColumn;
    }

    /**
     * Gets the orderDirection
     *
     * @return The orderDirection
     */
    public Boolean getOrderDirection() {
        return orderDirection;
    }

    /**
     * Sets the orderDirection
     *
     * @param orderDirection The orderDirection to set
     */
    public void setOrderDirection(final Boolean orderDirection) {
        this.orderDirection = orderDirection;
    }

    /**
     * Gets the leftHandLimit
     *
     * @return The leftHandLimit
     */
    public Integer getLeftHandLimit() {
        return leftHandLimit;
    }

    /**
     * Sets the leftHandLimit
     *
     * @param leftHandLimit The leftHandLimit to set
     */
    public void setLeftHandLimit(final Integer leftHandLimit) {
        this.leftHandLimit = leftHandLimit;
    }

    /**
     * Gets the rightHandLimit
     *
     * @return The rightHandLimit
     */
    public Integer getRightHandLimit() {
        return rightHandLimit;
    }

    /**
     * Sets the rightHandLimit
     *
     * @param rightHandLimit The rightHandLimit to set
     */
    public void setRightHandLimit(final Integer rightHandLimit) {
        this.rightHandLimit = rightHandLimit;
    }

    /**
     * Gets the cols
     *
     * @return The cols
     */
    public int[] getCols() {
        return cols;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getBody() throws JSONException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getMethod() {
        return Method.GET;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Parameter[] getParameters() {
        final List<Parameter> params = new ArrayList<Parameter>(8);
        params.add(new Parameter(AJAXServlet.PARAMETER_ACTION, "all"));
        params.add(new Parameter(AJAXServlet.PARAMETER_COLUMNS, cols));
        if (null != sortColumn) {
            params.add(new Parameter(AJAXServlet.PARAMETER_SORT, sortColumn.intValue()));
            params.add(new Parameter(AJAXServlet.PARAMETER_ORDER, orderDirection.booleanValue() ? "asc" : "desc"));
        }
        if (null != leftHandLimit) {
            params.add(new Parameter(AJAXServlet.LEFT_HAND_LIMIT, leftHandLimit.intValue()));
        }
        if (null != rightHandLimit) {
            params.add(new Parameter(AJAXServlet.RIGHT_HAND_LIMIT, rightHandLimit.intValue()));
        }
        return params.toArray(new Parameter[params.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AllParser getParser() {
        return new AllParser(true);
    }
}
