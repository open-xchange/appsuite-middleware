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

package com.openexchange.contact.internal.mapping;

import java.util.Comparator;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

/**
 * {@link ContactMapping} - Mapping operations for contact properties.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class ContactMapping<T> extends com.openexchange.groupware.tools.mappings.DefaultMapping<T, Contact> implements Comparator<Contact> {

	/**
	 * Validates the property in a contact, throwing exceptions if validation
	 * fails.
	 *
	 * @param contact the contact to validate the property for
	 * @throws OXException
	 */
    @SuppressWarnings("unused")
    public void validate(final Contact contact) throws OXException {
        // empty
	}

	@Override
	public int compare(final Contact object1, final Contact object2) {
		return this.compare(object1, object2, (Comparator<Object>) null);
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int compare(final Contact object1, final Contact object2, final Comparator<Object> collator) {
		final T value1 = this.get(object1);
		final T value2 = this.get(object2);
		if (value1 == value2) {
			return 0;
		} else if (null == value1 && null != value2) {
			return -1;
		} else if (null == value2) {
			return 1;
		} else if (null != collator && String.class.isInstance(value1)) {
			return collator.compare(value1, value2);
		} else if (Comparable.class.isInstance(value1)) {
			return ((Comparable)value1).compareTo(value2);
		} else {
	        throw new UnsupportedOperationException("Don't know how to compare two values of class " + value1.getClass().getName());
		}
	}

}
