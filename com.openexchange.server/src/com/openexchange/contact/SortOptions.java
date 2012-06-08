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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
 * {@link SortOptions} - Specifies sort options for the results of storage operations. 
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class SortOptions {
	
	public static final SortOptions EMPTY = new SortOptions();
	
	private SortOrder order[];	
	private String collation;
	private int rangeStart;
	private int limit;
	
	public static final SortOrder Order(final ContactField by, final Order order) {
		return new SortOrder(by, order);
	}
	
	public SortOptions(final String collation, final SortOrder... order) {
		super();
		this.collation = collation;
		this.order = order;
	}

	public SortOptions(final int rangeStart, final int limit) {
		this();
		this.limit = limit;
		this.rangeStart = rangeStart;		
	}

	public SortOptions(final SortOrder... order) {
		this(null, order);
	}

	public SortOptions(final String collation) {
		this(collation, (SortOrder[])null);
	}

	public SortOptions() {
		this((SortOrder[])null);
	}

	public SortOptions(final String collation, final ContactField orderBy, final Order order, final int rangeStart, final int limit) {
		this(collation, orderBy, order);
		this.limit = limit;
		this.rangeStart = rangeStart;		
	}

	public SortOptions(final String collation, final ContactField orderBy, final Order order) {
		this(collation, Order(orderBy, order));
	}
	
	public SortOptions(final ContactField orderBy, final Order order) {
		this(Order(orderBy, order));
	}
	
	public SortOptions(final String collation, final ContactField orderBy1, final Order order1, final ContactField orderBy2, final Order order2) {
		this(collation, Order(orderBy1, order1), Order(orderBy2, order2));
	}
	
	public SortOptions(final ContactField orderBy1, final Order order1, final ContactField orderBy2, final Order order2) {
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
		return order;
	}

	/**
	 * @param order the orderBy to set
	 */
	public void setOrderBy(SortOrder[] order) {
		this.order = order;
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

}
