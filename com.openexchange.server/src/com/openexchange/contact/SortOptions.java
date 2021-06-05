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

package com.openexchange.contact;

import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.search.Order;

/**
 * {@link SortOptions}
 *
 * Defines sort options for the results of storage operations. This includes
 * the specification ranged results, a collation and multiple sort orders.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class SortOptions {

    /**
     * Empty sort options
     */
	public static final SortOptions EMPTY = new SortOptions();

	private SortOrder sortOrders[];
	private String collation;
	private int rangeStart;
	private int limit;

	/**
     * Creates a new {@link SortOrder} instance.
     *
     * @param by the contact field for ordering
     * @param order the order
     * @return the sort order or null in case {@link ContactField} is null
     */
	public static final SortOrder Order(ContactField by, Order order) {
        if (by == null) {
            return null;
        }
		return new SortOrder(by, order);
	}

	/**
	 * Initializes a new {@link SortOptions}.
	 *
	 * @param collation the collation
	 * @param sortOrders the sort order definitions
	 */
	public SortOptions(String collation, SortOrder... sortOrders) {
		super();
		this.collation = collation;
		this.sortOrders = sortOrders;
	}

	/**
	 * Initializes a new {@link SortOptions}.
	 *
	 * @param rangeStart the start index for the results
	 * @param limit the maximum number of results to return
	 */
	public SortOptions(int rangeStart, int limit) {
		this();
		this.limit = limit;
		this.rangeStart = rangeStart;
	}

	/**
	 * Initializes a new {@link SortOptions}.
	 *
	 * @param order the sort order definitions
	 */
	public SortOptions(SortOrder... order) {
		this(null, order);
	}

	/**
	 * Initializes a new {@link SortOptions}.
	 *
     * @param collation the collation
	 */
	public SortOptions(String collation) {
		this(collation, (SortOrder[])null);
	}

	/**
	 * Initializes a new {@link SortOptions}.
	 */
	public SortOptions() {
		this((SortOrder[])null);
	}

	/**
	 * Initializes a new {@link SortOptions}.
	 *
     * @param collation the collation
	 * @param orderBy the field to order by
	 * @param order the order
     * @param rangeStart the start index for the results
     * @param limit the maximum number of results to return
	 */
	public SortOptions(String collation, ContactField orderBy, Order order, int rangeStart, int limit) {
		this(collation, orderBy, order);
		this.limit = limit;
		this.rangeStart = rangeStart;
	}

	/**
	 * Initializes a new {@link SortOptions}.
	 *
     * @param collation the collation
     * @param orderBy the field to order by
     * @param order the order
	 */
	public SortOptions(String collation, ContactField orderBy, Order order) {
		this(collation, Order(orderBy, order));
	}

	/**
	 * Initializes a new {@link SortOptions}.
	 *
     * @param orderBy the field to order by
     * @param order the order
	 */
	public SortOptions(ContactField orderBy, Order order) {
		this(Order(orderBy, order));
	}

	/**
	 * Initializes a new {@link SortOptions}.
	 *
     * @param collation the collation
     * @param orderBy1 the 1st field to order by
     * @param order1 the 1st order
     * @param orderBy2 the 2nd field to order by
     * @param order2 the 2nd order
	 */
	public SortOptions(String collation, ContactField orderBy1, Order order1, ContactField orderBy2, Order order2) {
		this(collation, Order(orderBy1, order1), Order(orderBy2, order2));
	}

	/**
	 * Initializes a new {@link SortOptions}.
	 *
     * @param orderBy1 the 1st field to order by
     * @param order1 the 1st order
     * @param orderBy2 the 2nd field to order by
     * @param order2 the 2nd order
	 */
	public SortOptions(ContactField orderBy1, Order order1, ContactField orderBy2, Order order2) {
		this((String)null, Order(orderBy1, order1), Order(orderBy2, order2));
	}

	/**
	 * @return the collation
	 */
	public String getCollation() {
		return collation;
	}

	/**
	 * @param collation the collation to set
	 */
	public void setCollation(String collation) {
		this.collation = collation;
	}

	/**
	 * @return the order
	 */
	public SortOrder[] getOrder() {
		return sortOrders;
	}

	/**
	 * @param order the orderBy to set
	 */
	public void setOrderBy(SortOrder[] order) {
		this.sortOrders = order;
	}

	/**
	 * @return the rangeStart
	 */
	public int getRangeStart() {
		return rangeStart;
	}

	/**
	 * @param rangeStart the rangeStart to set
	 */
	public void setRangeStart(int rangeStart) {
		this.rangeStart = rangeStart;
	}

	/**
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (false == SortOptions.EMPTY.equals(this)) {
            if (null != sortOrders && 0 < sortOrders.length) {
                stringBuilder.append("ORDER BY ").append(sortOrders[0].getBy()).append(' ').append((Order.NO_ORDER.equals(
                    sortOrders[0].getOrder()) ? "" : (Order.ASCENDING.equals(sortOrders[0].getOrder()) ? "ASC" : "DESC")));
                for (int i = 1; i < sortOrders.length; i++) {
                    stringBuilder.append(", ").append(sortOrders[i].getBy()).append(' ').append((Order.NO_ORDER.equals(
                        sortOrders[i].getOrder()) ? "" : (Order.ASCENDING.equals(sortOrders[i].getOrder()) ? "ASC" : "DESC")));
                }
            }
            if (0 < limit) {
                stringBuilder.append(" LIMIT ");
                if (0 < rangeStart) {
                    stringBuilder.append(rangeStart).append(", ");
                }
                stringBuilder.append(limit);
            }
        }
        return stringBuilder.toString();
    }

}
