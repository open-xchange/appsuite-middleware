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

package com.openexchange.groupware.contact.helpers;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Collators;

/**
 * Compares two contacts based on the value of one of their fields.
 * Uses a collation for a locale to do that. Recommended for languages
 * that do not follow a standard western sorting (read: nearly all).
 *
 * @author tobiasp
 */
public class CollationContactComparator implements Comparator<Contact>{

	private int orderDir;
	private final ContactField orderBy;
	private final Collator collator;

	public CollationContactComparator(ContactField orderBy, Order order, Locale locale) {
	    super();
		this.orderBy = orderBy;
		switch (order) {
		case ASCENDING:
		    orderDir = 1;
		    break;
		case DESCENDING:
		    orderDir = -1;
		    break;
		case NO_ORDER:
	    default:
            orderDir = 0;
		}
		this.collator = Collators.getDefaultInstance(locale);
	}

	@Override
    public int compare(Contact o1, Contact o2) {
        if(o1 == o2 || orderDir == 0) {
            return 0;
        }
        if(o1 == null && o2 != null) {
            return -1;
        }
        if(o2 == null) {
            return 1;
        }

        Object f1 = o1.get(orderBy.getNumber());
        Object f2 = o2.get(orderBy.getNumber());
        if(f1 == f2) {
            return 0;
        }
        if(f1 == null && f2 != null) {
            return -1;
        }
        if(f2 == null) {
            return 1;
        }
        return orderDir * collator.compare(f1, f2);
	}

}
