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

package com.openexchange.contact.storage;

import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.search.Order;

/**
 * {@link SortOptions} - Specifies sort options for the results of storage operations. 
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class SortOptions {
	
	public static final SortOptions EMPTY = new SortOptions();
	
	private Order order; 
	
	private String collation;
	
	private ContactField[] orderBy;
	
	
	public SortOptions(final ContactField[] orderBy, final Order order, final String collation) {
		super();
		this.orderBy = orderBy;
		this.order = order;
		this.collation = collation;
	}
	
	public SortOptions(final ContactField orderBy, final Order order, final String collation) {
		this(new ContactField[] { orderBy }, order, collation);
	}
	
	public SortOptions(final ContactField[] orderBy, final Order order) {
		this(orderBy, order, null);
	}
	
	public SortOptions(final ContactField orderBy, final Order order) {
		this(orderBy, order, null);
	}
	
	public SortOptions(final ContactField[] orderBy) {
		this(orderBy, Order.NO_ORDER);
	}	

	public SortOptions(final ContactField orderBy) {
		this(orderBy, Order.NO_ORDER);
	}	
	
	public SortOptions() {
		this((ContactField)null);
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
	 * @return the orderBy
	 */
	public ContactField[] getOrderBy() {
		return orderBy;
	}

	/**
	 * @param orderBy the orderBy to set
	 */
	public void setOrderBy(ContactField[] orderBy) {
		this.orderBy = orderBy;
	}

	/**
	 * @return the order
	 */
	public Order getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(Order order) {
		this.order = order;
	}
	
}
