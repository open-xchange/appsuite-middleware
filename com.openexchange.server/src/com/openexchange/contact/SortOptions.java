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
	 * @return the sort order
	 */
	public static final SortOrder Order(ContactField by, Order order) {
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
