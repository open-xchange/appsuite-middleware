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

package com.openexchange.subscribe.google.parser;

import java.util.List;
import java.util.function.BiConsumer;
import com.google.common.collect.ImmutableList;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.openexchange.groupware.container.Contact;
import com.openexchange.subscribe.google.parser.consumers.BirthdayConsumer;
import com.openexchange.subscribe.google.parser.consumers.EmailAddressesConsumer;
import com.openexchange.subscribe.google.parser.consumers.ImAddressesConsumer;
import com.openexchange.subscribe.google.parser.consumers.NameConsumer;
import com.openexchange.subscribe.google.parser.consumers.NicknameConsumer;
import com.openexchange.subscribe.google.parser.consumers.OccupationConsumer;
import com.openexchange.subscribe.google.parser.consumers.PhoneNumbersConsumer;
import com.openexchange.subscribe.google.parser.consumers.PhotoConsumer;
import com.openexchange.subscribe.google.parser.consumers.StructuredPostalAddressConsumer;
import com.openexchange.subscribe.google.parser.consumers.UnstructuredPostalAddressConsumer;

/**
 * {@link ContactParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public final class ContactParser {

    private final List<BiConsumer<ContactEntry, Contact>> elementParsers;

    /**
     * Initialises a new {@link ContactParser}.
     *
     * @param googleContactsService The Google's {@link ContactsService}
     */
    public ContactParser(ContactsService googleContactsService) {
        ImmutableList.Builder<BiConsumer<ContactEntry, Contact>> listBuilder = ImmutableList.builder();
        listBuilder.add(new NameConsumer());
        listBuilder.add(new NicknameConsumer());
        listBuilder.add(new EmailAddressesConsumer());
        listBuilder.add(new BirthdayConsumer());
        listBuilder.add(new OccupationConsumer());
        listBuilder.add(new ImAddressesConsumer());
        listBuilder.add(new PhoneNumbersConsumer());
        listBuilder.add(new UnstructuredPostalAddressConsumer());
        listBuilder.add(new StructuredPostalAddressConsumer());
        listBuilder.add(new PhotoConsumer(googleContactsService));
        elementParsers = listBuilder.build();
    }

    /**
     * Parses the specified {@link ContactFeed} and returns it as a {@link List} of {@link Contact}s
     *
     * @param feed The {@link ContactFeed} to parse
     * @return a {@link List} of {@link Contact}s
     */
    public List<Contact> parseFeed(ContactFeed feed) {
        List<ContactEntry> entries = feed.getEntries();
        List<Contact> contacts = new java.util.ArrayList<Contact>(entries.size());
        for (ContactEntry contact : entries) {
            contacts.add(parseContactEntry(contact));
        }
        return contacts;
    }

    /**
     * Parses the specified {@link ContactEntry} to a {@link Contact}
     *
     * @param entry The {@link ContactEntry} to parse
     * @return a new {@link Contact}
     */
    private Contact parseContactEntry(ContactEntry entry) {
        Contact c = new Contact();
        for (BiConsumer<ContactEntry, Contact> consumer : elementParsers) {
            consumer.accept(entry, c);
        }
        return c;
    }
}
