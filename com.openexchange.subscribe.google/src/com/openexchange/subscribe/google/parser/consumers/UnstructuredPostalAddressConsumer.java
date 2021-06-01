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
import com.google.gdata.data.extensions.PostalAddress;
import com.openexchange.groupware.container.Contact;

/**
 * {@link UnstructuredPostalAddressConsumer} - Parses the contact's postal addresses. Note that google
 * can store an unlimited mount of postal addresses for a contact due to their different
 * data model (probably EAV). Our contacts API however can only store a home, business and other
 * address, therefore we only fetch the first three we encounter. Furthermore, we set as home
 * address the {@link PostalAddress} that is marked as primary.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class UnstructuredPostalAddressConsumer implements BiConsumer<ContactEntry, Contact> {

    /**
     * Initialises a new {@link UnstructuredPostalAddressConsumer}.
     */
    public UnstructuredPostalAddressConsumer() {
        super();
    }

    @Override
    public void accept(ContactEntry t, Contact u) {
        if (!t.hasPostalAddresses()) {
            return;
        }
        int count = 0;
        for (PostalAddress pa : t.getPostalAddresses()) {
            if (pa.getPrimary()) {
                u.setAddressHome(pa.getValue());
            }
            switch (count++) {
                case 0:
                    u.setAddressHome(pa.getValue());
                    break;
                case 1:
                    u.setAddressBusiness(pa.getValue());
                    break;
                case 2:
                    u.setAddressOther(pa.getValue());
                    break;
                default:
                    return;
            }
        }
    }
}
