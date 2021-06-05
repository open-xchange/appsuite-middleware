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

import javax.mail.internet.AddressException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.QuotedInternetAddress;

/**
 * {@link ContactSwitcherForEmailAddresses}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ContactSwitcherForEmailAddresses extends AbstractContactSwitcherWithDelegate {

    /**
     * Initializes a new {@link ContactSwitcherForEmailAddresses}.
     */
    public ContactSwitcherForEmailAddresses() {
        super();
    }

    /**
     * Initializes a new {@link ContactSwitcherForEmailAddresses}.
     *
     * @param delegate The delegate switcher
     */
    public ContactSwitcherForEmailAddresses(ContactSwitcher delegate) {
        this();
        setDelegate(delegate);
    }

    @Override
    public Object email1(Object... objects) throws OXException {
        try {
            return delegate.email1(validate(objects));
        } catch (OXException e) {
            Contact contact = (Contact) objects[0];
            contact.addWarning(e);
            return contact;
        }
    }

    @Override
    public Object email2(Object... objects) throws OXException {
        try {
            return delegate.email2(validate(objects));
        } catch (OXException e) {
            Contact contact = (Contact) objects[0];
            contact.addWarning(e);
            return contact;
        }
    }

    @Override
    public Object email3(Object... objects) throws OXException {
        try {
            return delegate.email3(validate(objects));
        } catch (OXException e) {
            Contact contact = (Contact) objects[0];
            contact.addWarning(e);
            return contact;
        }
    }

    /**
     * Validates the e-mail address in the supplied switcher parameters, throwing an appropriate exception in case the value is not empty,
     * but no valid e-mail address.
     *
     * @param switcherParameters The switcher parameters
     * @param The validated switcher parameters
     */
    private static Object[] validate(Object[] switcherParameters) throws OXException {
        if (null != switcherParameters && 1 < switcherParameters.length &&
            null != switcherParameters[1] && String.class.isInstance(switcherParameters[1])) {
            String emailAddress = (String) switcherParameters[1];
            if (Strings.isNotEmpty(emailAddress)) {
                try {
                    new QuotedInternetAddress(emailAddress).validate();
                } catch (AddressException e) {
                    throw ContactExceptionCodes.INVALID_EMAIL.create(e, emailAddress);
                }
            }
        }
        return switcherParameters;
    }

}
