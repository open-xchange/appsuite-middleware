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
