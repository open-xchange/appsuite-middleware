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

package com.openexchange.contact.provider.test.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import com.openexchange.groupware.container.Contact;

/**
 * {@link TestContactStorage} represents a simple in memory contact storage for testing purpose only
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v8.0.0
 */
public class TestContactsStorage {

    private final Map<String, Contact> store = new ConcurrentHashMap<String, Contact>();

    /**
     * Initializes a new {@link TestContactsStorage}.
     *
     * @param contacs The contacts to initially add to the storage
     */
    public TestContactsStorage(Contact... contacts) {
        insert(contacts);
    }

    /**
     * Initializes a new {@link TestContactsStorage}.
     *
     * @param contacs The contacts to initially add to the storage
     */
    public TestContactsStorage(List<Contact> contacts) {
        insert(contacts.toArray(new Contact[contacts.size()]));
    }

    /**
     * Gets a contact with the given ID
     *
     * @param id The ID of the contact
     * @return The contact with the given ID, or null if no such contact was found
     */
    public Contact get(String id) {
        return store.get(id);
    }

    /**
     * Gets all contacts fulfilling the given predicate
     *
     * @param p The predicate
     * @return A list of contacts for which the given predicate evaluates to <code>true</code>
     */
    public List<Contact> get(Predicate<? super Contact> p) {
        return store.values().stream().filter(p).collect(Collectors.toList());
    }

    /**
     * Returns all known contacts
     *
     * @return A list of all contacts
     */
    public List<Contact> getAll() {
        return new ArrayList<Contact>(store.values());
    }

    /**
     * Adds a bunch of contacts
     *
     * @param contacts The contacts to add
     */
    public void insert(Contact... contacts) {
        for (Contact contact : contacts) {
            store.put(contact.getId(), contact);
        }
    }

    /**
     * Deletes a contact with the given ID
     *
     * @param id The ID of the contact to delete
     * @return <code>True</code> if a contact with the given ID was deleted, <code>false/<code> if such a contact was not found
     */
    public boolean delete(String id) {
        return store.remove(id) != null;
    }
}
