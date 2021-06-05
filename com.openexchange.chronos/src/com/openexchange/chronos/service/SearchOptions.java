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

package com.openexchange.chronos.service;

import java.util.Arrays;
import java.util.Date;
import com.openexchange.chronos.EventField;

/**
 * {@link SearchOptions}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class SearchOptions {

    /** Empty sort options */
    public static final SearchOptions EMPTY = new SearchOptions();

    private SortOrder[] sortOrders;
    private int limit;
    private int offset;
    private Date from;
    private Date until;

    /**
     * Initializes a new {@link SearchOptions}.
     */
    public SearchOptions() {
        super();
    }

    /**
     * Initializes a new {@link SearchOptions} base on the supplied calendar parameters.
     *
     * The following calendar parameters are extracted:
     * <ul>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_START}</li>
     * <li>{@link CalendarParameters#PARAMETER_RANGE_END}</li>
     * <li>{@link CalendarParameters#PARAMETER_ORDER}</li>
     * <li>{@link CalendarParameters#PARAMETER_ORDER_BY}</li>
     * <li>{@link CalendarParameters#PARAMETER_LEFT_HAND_LIMIT}</li>
     * <li>{@link CalendarParameters#PARAMETER_RIGHT_HAND_LIMIT}</li>
     * </ul>
     *
     * @param parameters The calendar parameters to extract the sort options from
     */
    public SearchOptions(CalendarParameters parameters) {
        this();
        EventField by = parameters.get(CalendarParameters.PARAMETER_ORDER_BY, EventField.class);
        if (null != by) {
            addOrder(SortOrder.getSortOrder(by, parameters.get(CalendarParameters.PARAMETER_ORDER, SortOrder.Order.class, SortOrder.Order.ASC)));
        }
        Integer leftHandLimit = parameters.get(CalendarParameters.PARAMETER_LEFT_HAND_LIMIT, Integer.class);
        Integer rightHandLimit = parameters.get(CalendarParameters.PARAMETER_RIGHT_HAND_LIMIT, Integer.class);
        setLimits(null != leftHandLimit ? leftHandLimit.intValue() : 0, null != rightHandLimit ? rightHandLimit.intValue() : -1);
        Date from = parameters.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
        Date until = parameters.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        setRange(from, until);
    }

    /**
     * Adds a sort order.
     *
     * @param order The sort order to add
     * @return A self reference
     */
    public SearchOptions addOrder(SortOrder order) {
        if (null == sortOrders) {
            sortOrders = new SortOrder[] { order };
        } else {
            sortOrders = com.openexchange.tools.arrays.Arrays.add(sortOrders, order);
        }
        return this;
    }

    /**
     * Sets the offset and limit based on the supplied <i>left-hand-</i> and <i>right-hand-limits</i>.
     *
     * @param leftHandLimit The "left-hand" limit of the range to return
     * @param rightHandLimit The "right-hand" limit of the range to return
     * @return A self reference
     */
    public SearchOptions setLimits(int leftHandLimit, int rightHandLimit) {
        if (0 < leftHandLimit) {
            offset = leftHandLimit;
            if (0 > rightHandLimit || rightHandLimit < leftHandLimit) {
                throw new IllegalArgumentException("rightHandLimit");
            }
            limit = rightHandLimit - leftHandLimit;
        } else {
            offset = 0;
            limit = 0 < rightHandLimit ? rightHandLimit : -1;
        }
        return this;
    }

    /**
     * Sets the from/to range based on the supplied <i>from</i> and <i>until</i> values..
     *
     * @param from The lower inclusive limit of the queried range, or <code>null</code> if not set
     * @param until The upper exclusive limit of the queried range, or <code>null</code> if not set
     * @return A self reference
     */
    public SearchOptions setRange(Date from, Date until) {
        this.from = from;
        this.until = until;
        return this;
    }

    public SortOrder[] getSortOrders() {
        return sortOrders;
    }

    /**
     * Gets the maximum number of results to return.
     *
     * @return The limit, or <code>-1</code> for no limitations
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Gets the offset of the results to return.
     *
     * @return The offset, or <code>0</code> for no offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Gets the lower inclusive limit of the queried range.
     *
     * @return The lower inclusive limit of the queried range, or <code>null</code> if not set
     */
    public Date getFrom() {
        return from;
    }

    /**
     * Gets the upper exclusive limit of the queried range.
     *
     * @return The upper exclusive limit of the queried range, or <code>null</code> if not set
     */
    public Date getUntil() {
        return until;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + limit;
        result = prime * result + offset;
        result = prime * result + Arrays.hashCode(sortOrders);
        result = prime * result + ((until == null) ? 0 : until.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SearchOptions other = (SearchOptions) obj;
        if (from == null) {
            if (other.from != null) {
                return false;
            }
        } else if (!from.equals(other.from)) {
            return false;
        }
        if (limit != other.limit) {
            return false;
        }
        if (offset != other.offset) {
            return false;
        }
        if (!Arrays.equals(sortOrders, other.sortOrders)) {
            return false;
        }
        if (until == null) {
            if (other.until != null) {
                return false;
            }
        } else if (!until.equals(other.until)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (null != sortOrders && 0 < sortOrders.length) {
            stringBuilder.append("ORDER BY ").append(sortOrders[0]);
            for (int i = 1; i < sortOrders.length; i++) {
                stringBuilder.append(", ").append(sortOrders[i]);
            }
        }
        if (0 < limit) {
            stringBuilder.append(" LIMIT ");
            if (0 < offset) {
                stringBuilder.append(offset).append(", ");
            }
            stringBuilder.append(limit);
        }
        return stringBuilder.toString();
    }

}
