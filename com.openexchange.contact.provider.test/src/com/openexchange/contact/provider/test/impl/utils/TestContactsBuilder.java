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

package com.openexchange.contact.provider.test.impl.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.groupware.container.Contact;

/**
 * {@link TestContactsBuilder} helper class to build test contacts
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v8.0.0
 */
public class TestContactsBuilder {

    private final List<TestContactBuilder> contacts = new ArrayList<TestContactBuilder>();

    /**
     * Gets the test contacts
     *
     * @return A list of contacts
     */
    public List<Contact> getContacts() {
        return contacts.stream().map(c -> c.getContact()).collect(Collectors.toList());
    }

    /**
     * Adds a {@link TestContactBuilder} which will be mapped to a {@link Contact}
     *
     * @param contact The contact
     * @return this
     */
    public TestContactsBuilder add(TestContactBuilder contact) {
        contacts.add(contact);
        return this;
    }
}
