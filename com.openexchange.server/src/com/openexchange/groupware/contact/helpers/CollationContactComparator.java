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
        if (o1 == o2 || orderDir == 0) {
            return 0;
        }
        if (o1 == null && o2 != null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }

        Object f1 = o1.get(orderBy.getNumber());
        Object f2 = o2.get(orderBy.getNumber());
        if (f1 == f2) {
            return 0;
        }
        if (f1 == null && f2 != null) {
            return -1;
        }
        if (f2 == null) {
            return 1;
        }
        return orderDir * collator.compare(f1, f2);
	}

}
