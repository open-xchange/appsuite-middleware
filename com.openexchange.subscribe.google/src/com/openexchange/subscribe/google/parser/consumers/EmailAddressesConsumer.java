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

package com.openexchange.subscribe.google.parser.consumers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.Person;
import com.openexchange.groupware.container.Contact;
import com.openexchange.i18n.I18nService;
import com.openexchange.subscribe.google.parser.ContactNoteStrings;

/**
 * {@link EmailAddressesConsumer} - Parses the contact's e-mail addresses. Note that google
 * can store an unlimited mount of e-mail addresses for a contact due to their different
 * data model (probably EAV). Our contacts API however can only store three, therefore
 * more then three e-mail addresses are stored inside the contact's note field.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:philipp.schumacher@open-xchange.com">Philipp Schumacher</a>
 * @since v7.10.1
 */
public class EmailAddressesConsumer extends AbstractNoteConsumer implements BiConsumer<Person, Contact> {

    private static final Comparator<EmailAddress> MAIL_COMPARATOR = new Comparator<EmailAddress>() {

        @Override
        public int compare(EmailAddress emailAddress1, EmailAddress emailAddress2) {
            return emailAddress1.getType().compareTo(emailAddress2.getType());
        }
    };

    /**
     * Initialises a new {@link EmailAddressesConsumer}.
     */
    public EmailAddressesConsumer(I18nService i18nService) {
        super(i18nService);
    }

    @Override
    public void accept(Person t, Contact u) {
        List<EmailAddress> emailAddresses = t.getEmailAddresses();
        if (emailAddresses == null || emailAddresses.isEmpty()) {
            return;
        }
        boolean firstAddressInNotes = true;
        Collections.sort(emailAddresses, MAIL_COMPARATOR);
        for (EmailAddress email : emailAddresses) {
            firstAddressInNotes = setEmailAddress(email, u, firstAddressInNotes);
        }
    }

    /**
     * Sets the e-mail address {@link EmailAddress} to the contact
     *
     * @param email The e-mail address {@link EmailAddress}
     * @param u The {@link Contact}
     */
    private boolean setEmailAddress(EmailAddress email, Contact u, boolean firstAddressInNotes) {
        if (u.getEmail1() == null) {
            u.setEmail1(email.getValue());
            return firstAddressInNotes;
        } else if (u.getEmail2() == null) {
            u.setEmail2(email.getValue());
            return firstAddressInNotes;
        } else if (u.getEmail3() == null) {
            u.setEmail3(email.getValue());
            return firstAddressInNotes;
        } else {
            return addEmailAddressToNote(email, u, firstAddressInNotes);
        }
    }

    /**
     * Adds the e-mail address to the contact's note if it is formatted
     *
     * @param emailAddress The {@link EmailAddress}
     * @param contact The {@link Contact}
     */
    private boolean addEmailAddressToNote(EmailAddress emailAddress, Contact contact, boolean firstAddressInNotes) {
        if (emailAddress == null) {
            return firstAddressInNotes;
        }
        return addValueToNote(contact, ContactNoteStrings.OTHER_EMAIL_ADDRESSES, emailAddress.getValue(), firstAddressInNotes);
    }
}
