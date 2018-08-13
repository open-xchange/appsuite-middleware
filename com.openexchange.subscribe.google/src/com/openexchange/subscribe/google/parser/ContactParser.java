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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.subscribe.google.parser;

import java.util.LinkedList;
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
        List<Contact> contacts = new LinkedList<Contact>();
        for (ContactEntry contact : feed.getEntries()) {
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
