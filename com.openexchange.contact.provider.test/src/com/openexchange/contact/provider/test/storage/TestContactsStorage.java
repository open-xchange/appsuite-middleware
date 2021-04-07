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
 *    trademarks of the OX Software GmbH. group of companies.
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
