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

import javax.mail.internet.AddressException;
import com.openexchange.config.ConfigurationService;
import com.openexchange.contact.internal.ContactServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Contact;
import com.openexchange.mail.mime.QuotedInternetAddress;


/**
 * {@link EMailMapping} - Default mapping for email properties in Contacts.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class EMailMapping extends StringMapping {

    private Boolean validateEmail = null;

    private boolean isValidateEmail() throws OXException {
        if (null == validateEmail) {
            validateEmail = Boolean.valueOf(
                ContactServiceLookup.getService(ConfigurationService.class).getBoolProperty("validate_contact_email", true));
        }
        return validateEmail.booleanValue();
    }

	@Override
	public void validate(Contact contact) throws OXException {
		super.validate(contact);
		if (isValidateEmail() && this.isSet(contact)) {
			final String value = this.get(contact);
			if (null != value && !value.trim().isEmpty()) {
				try {
					new QuotedInternetAddress(value).validate();
				} catch (AddressException e) {
					throw ContactExceptionCodes.INVALID_EMAIL.create(e, value);
				}
			}
		}
	}

}
