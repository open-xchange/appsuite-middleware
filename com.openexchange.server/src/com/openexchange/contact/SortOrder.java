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
 * {@link SortOrder}
 *
 * Defines a single sort order for contacts.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class SortOrder {

	private final ContactField by;
	private final Order order;

	/**
	 * Initializes a new {@link SortOrder}.
	 *
	 * @param by the contact field
	 * @param order the order
	 */
	public SortOrder(ContactField by, Order order) {
		this.by = by;
		this.order = order;
	}

	/**
	 * @return the order
	 */
	public Order getOrder() {
		return order;
	}

	/**
	 * @return the by
	 */
	public ContactField getBy() {
		return by;
	}

    @Override
    public String toString() {
        return "ORDER BY " + by + ' ' + (Order.NO_ORDER.equals(order) ? "" : (Order.ASCENDING.equals(order) ? "ASC" : "DESC"));
    }

}
