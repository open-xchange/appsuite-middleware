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

import java.util.List;
import java.util.function.BiConsumer;
import com.google.api.services.people.v1.model.Person;
import com.google.api.services.people.v1.model.PhoneNumber;
import com.openexchange.groupware.container.Contact;
import com.openexchange.i18n.I18nService;
import com.openexchange.subscribe.google.parser.ContactNoteStrings;

/**
 * {@link PhoneNumbersConsumer} - Parses the contact's phone numbers. Note that google
 * can store an unlimited mount of phone numbers for a contact due to their different
 * data model (probably EAV). Our contacts API however can only store a handful, therefore
 * phone numbers, which can't be mapped are saved inside the contact's note field.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:philipp.schumacher@open-xchange.com">Philipp Schumacher</a>
 * @since v7.10.1
 */
public class PhoneNumbersConsumer extends AbstractNoteConsumer implements BiConsumer<Person, Contact> {

    /**
     * Initialises a new {@link PhoneNumbersConsumer}.
     */
    public PhoneNumbersConsumer(I18nService i18nService) {
        super(i18nService);
    }

    @Override
    public void accept(Person person, Contact contact) {
        List<PhoneNumber> phoneNumbers = person.getPhoneNumbers();
        if (phoneNumbers == null || phoneNumbers.isEmpty()) {
            return;
        }
        boolean firstPhoneNumberInNotes = true;
        for (PhoneNumber phoneNumber : person.getPhoneNumbers()) {
            switch (phoneNumber.getType()) {
                case "home":
                    firstPhoneNumberInNotes = setTelephoneHome(contact, phoneNumber, firstPhoneNumberInNotes);
                    break;
                case "work":
                    firstPhoneNumberInNotes = setTelephoneBusiness(contact, phoneNumber, firstPhoneNumberInNotes);
                    break;
                case "mobile":
                    firstPhoneNumberInNotes = setCellularTelephone(contact, phoneNumber, firstPhoneNumberInNotes);
                    break;
                case "homeFax":
                    contact.setFaxHome(phoneNumber.getValue());
                    break;
                case "workFax":
                    contact.setFaxBusiness(phoneNumber.getValue());
                    break;
                case "otherFax":
                    contact.setFaxOther(phoneNumber.getValue());
                    break;
                case "pager":
                    contact.setTelephonePager(phoneNumber.getValue());
                    break;
                case "workMobile":
                case "workPager":
                case "main":
                case "googleVoice":
                case "other":
                default:
                    firstPhoneNumberInNotes = addPhoneNumberToNote(contact, phoneNumber, firstPhoneNumberInNotes);
                    break;
            }
        }
    }

    /**
     * Sets the home phone number.
     * 
     * @param contact The {@link Contact}
     * @param phoneNumber The {@link PhoneNumber}
     */
    private boolean setTelephoneHome(Contact contact, PhoneNumber phoneNumber, boolean firstPhoneNumberInNotes) {
        if (!contact.containsTelephoneHome1()) {
            contact.setTelephoneHome1(phoneNumber.getValue());
            return firstPhoneNumberInNotes;
        } else if (!contact.containsTelephoneHome2()) {
            contact.setTelephoneHome2(phoneNumber.getValue());
            return firstPhoneNumberInNotes;
        } else {
            return addPhoneNumberToNote(contact, phoneNumber, firstPhoneNumberInNotes);
        }
    }

    /**
     * Sets the business phone number.
     * 
     * @param contact The {@link Contact}
     * @param phoneNumbner The phone number as {@link String}
     */
    private boolean setTelephoneBusiness(Contact contact, PhoneNumber phoneNumber, boolean firstPhoneNumberInNotes) {
        if (!contact.containsTelephoneBusiness1()) {
            contact.setTelephoneBusiness1(phoneNumber.getValue());
            return firstPhoneNumberInNotes;
        } else if (!contact.containsTelephoneBusiness2()) {
            contact.setTelephoneBusiness2(phoneNumber.getValue());
            return firstPhoneNumberInNotes;
        } else {
            return addPhoneNumberToNote(contact, phoneNumber, firstPhoneNumberInNotes);
        }
    }

    /**
     * Sets the mobile phone number.
     * 
     * @param contact The {@link Contact}
     * @param phoneNumbner The phone number as {@link String}
     */
    private boolean setCellularTelephone(Contact contact, PhoneNumber phoneNumber, boolean firstPhoneNumberInNotes) {
        if (!contact.containsCellularTelephone1()) {
            contact.setCellularTelephone1(phoneNumber.getValue());
            return firstPhoneNumberInNotes;
        } else if (!contact.containsCellularTelephone2()) {
            contact.setCellularTelephone2(phoneNumber.getValue());
            return firstPhoneNumberInNotes;
        } else {
            return addPhoneNumberToNote(contact, phoneNumber, firstPhoneNumberInNotes);
        }
    }

    /**
     * Adds the phone number to the contact's note
     * 
     * @param contact The {@link Contact}
     * @param phoneNumbner The phone number
     */
    private boolean addPhoneNumberToNote(Contact contact, PhoneNumber phoneNumber, boolean firstPhoneNumberInNotes) {
        if (phoneNumber == null) {
            return firstPhoneNumberInNotes;
        }
        return addValueToNote(contact, ContactNoteStrings.OTHER_PHONE_NUMBERS, phoneNumber.getFormattedType() + ": " + phoneNumber.getValue(), firstPhoneNumberInNotes);
    }
}
