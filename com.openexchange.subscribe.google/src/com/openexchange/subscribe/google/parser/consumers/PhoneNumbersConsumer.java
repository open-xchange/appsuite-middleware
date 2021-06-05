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

import java.util.function.BiConsumer;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.PhoneNumber;
import com.openexchange.groupware.container.Contact;

/**
 * {@link PhoneNumbersConsumer} - Parses the contact's phone numbers. Note that google
 * can store an unlimited mount of phone numbers for a contact due to their different
 * data model (probably EAV). Our contacts API however can only store a handful, therefore
 * we only fetch the first seven we encounter.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class PhoneNumbersConsumer implements BiConsumer<ContactEntry, Contact> {

    /**
     * Initialises a new {@link PhoneNumbersConsumer}.
     */
    public PhoneNumbersConsumer() {
        super();
    }

    @Override
    public void accept(ContactEntry t, Contact u) {
        if (!t.hasPhoneNumbers()) {
            return;
        }
        int count = 0;
        for (PhoneNumber pn : t.getPhoneNumbers()) {
            if (pn.getPrimary()) {
                u.setTelephonePrimary(pn.getPhoneNumber());
            }
            // Unfortunately we do not have enough information
            // about the type of the telephone number, nor we
            // can make an educated guess. So we simply fetching
            // as much as possible.
            switch (count++) {
                case 0:
                    u.setTelephoneOther(pn.getPhoneNumber());
                    break;
                case 1:
                    u.setTelephoneHome1(pn.getPhoneNumber());
                    break;
                case 2:
                    u.setTelephoneHome2(pn.getPhoneNumber());
                    break;
                case 3:
                    u.setTelephoneBusiness1(pn.getPhoneNumber());
                    break;
                case 4:
                    u.setTelephoneBusiness2(pn.getPhoneNumber());
                    break;
                case 5:
                    u.setTelephoneAssistant(pn.getPhoneNumber());
                    break;
                case 6:
                    u.setTelephoneCompany(pn.getPhoneNumber());
                    break;
                case 7:
                    u.setTelephoneCallback(pn.getPhoneNumber());
                    break;
                // Maybe add more?
                default:
                    return;
            }
        }
    }
}
