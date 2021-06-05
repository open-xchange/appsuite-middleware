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
import com.openexchange.search.Operand;

/**
 * {@link ContactFieldOperand} - 'Column' type search term operand for contact fields.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactFieldOperand implements Operand<ContactField>{

	private final ContactField value;

	public ContactFieldOperand(final ContactField value) {
		super();
		this.value = value;
	}

	@Override
	public com.openexchange.search.Operand.Type getType() {
		return Type.COLUMN;
	}

	@Override
	public ContactField getValue() {
		return this.value;
	}

    @Override
    public String toString() {
        return new StringBuilder(Type.COLUMN.getType()).append(':').append(value).toString();
    }

}
