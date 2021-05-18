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

import java.util.List;
import java.util.function.BiConsumer;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.openexchange.groupware.container.Contact;

/**
 * {@link NameConsumer} - Parses the title, suffix, given name, middle name, family name and full name of the specified contact
 * along with their yomi representations if available.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:philipp.schumacher@open-xchange.com">Philipp Schumacher</a>
 * @since v7.10.1
 */
public class NameConsumer implements BiConsumer<Person, Contact> {

    /**
     * Initialises a new {@link NameConsumer}.
     */
    public NameConsumer() {
        super();
    }

    @Override
    public void accept(Person person, Contact contact) {
        List<Name> names = person.getNames();
        if (names == null || names.isEmpty()) {
            return;
        }
        Name name = names.get(0);
        contact.setTitle(name.getHonorificPrefix());
        contact.setSuffix(name.getHonorificSuffix());
        contact.setDisplayName(name.getDisplayName());
        contact.setGivenName(name.getGivenName());
        contact.setMiddleName(name.getMiddleName());
        contact.setSurName(name.getFamilyName());
        contact.setYomiFirstName(name.getPhoneticGivenName());
        contact.setYomiLastName(name.getPhoneticFamilyName());
    }
}
