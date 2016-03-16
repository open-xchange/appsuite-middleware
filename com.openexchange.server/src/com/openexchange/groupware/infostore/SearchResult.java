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

package com.openexchange.groupware.infostore;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class SearchResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<Serializable> entries = Collections.emptyList();

	private String sortedBy = "";
	private int order = -1;

	private int from;
	private int to;

	private int max;

	public List getEntries() {
		return entries;
	}

	public void setEntries(final List<Serializable> entries) {
		this.entries = entries;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(final int from) {
		this.from = from;
	}

	public int getMax() {
		return max;
	}

	public void setMax(final int max) {
		this.max = max;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(final int order) {
		this.order = order;
	}

	public String getSortedBy() {
		return sortedBy;
	}

	public void setSortedBy(final String sortedBy) {
		this.sortedBy = sortedBy;
	}

	public int getTo() {
		return to;
	}

	public void setTo(final int to) {
		this.to = to;
	}


}
