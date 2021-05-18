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
