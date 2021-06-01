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
import com.google.gdata.data.extensions.FamilyName;
import com.google.gdata.data.extensions.GivenName;
import com.google.gdata.data.extensions.Name;
import com.openexchange.groupware.container.Contact;

/**
 * {@link NameConsumer} - Parses the given name, family name and full name of the specified contact
 * along with their yomi representations if available.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class NameConsumer implements BiConsumer<ContactEntry, Contact> {

    /**
     * Initialises a new {@link NameConsumer}.
     */
    public NameConsumer() {
        super();
    }

    @Override
    public void accept(ContactEntry t, Contact u) {
        if (!t.hasName()) {
            return;
        }
        Name name = t.getName();
        if (name.hasGivenName()) {
            GivenName given = name.getGivenName();
            u.setGivenName(t.getName().getGivenName().getValue());
            if (given.hasYomi()) {
                u.setYomiFirstName(given.getYomi());
            }
        }
        if (name.hasFamilyName()) {
            FamilyName familyName = name.getFamilyName();
            u.setSurName(familyName.getValue());
            if (familyName.hasYomi()) {
                u.setYomiLastName(familyName.getYomi());
            }
        }
        if (name.hasFullName()) {
            u.setDisplayName(name.getFullName().getValue());
        }
    }
}
