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
import java.util.stream.Collectors;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Person;
import com.google.common.collect.ImmutableList;
import com.openexchange.groupware.container.Contact;
import com.openexchange.i18n.I18nService;
import com.openexchange.subscribe.google.parser.consumers.AddressConsumer;
import com.openexchange.subscribe.google.parser.consumers.BirthdayConsumer;
import com.openexchange.subscribe.google.parser.consumers.EmailAddressesConsumer;
import com.openexchange.subscribe.google.parser.consumers.ImAddressesConsumer;
import com.openexchange.subscribe.google.parser.consumers.NameConsumer;
import com.openexchange.subscribe.google.parser.consumers.NicknameConsumer;
import com.openexchange.subscribe.google.parser.consumers.OccupationConsumer;
import com.openexchange.subscribe.google.parser.consumers.PhoneNumbersConsumer;
import com.openexchange.subscribe.google.parser.consumers.PhotoConsumer;

/**
 * {@link ContactParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:philipp.schumacher@open-xchange.com">Philipp Schumacher</a>
 * @since v7.10.1
 */
public final class ContactParser {

    private final List<BiConsumer<Person, Contact>> elementParsers;

    /**
     * Initialises a new {@link ContactParser}.
     *
     * @param googleContactsService The Google's {@link ContactsService}
     */
    public ContactParser(PeopleService googlePeopleService, I18nService i18nService, int maxImageSize) {
        ImmutableList.Builder<BiConsumer<Person, Contact>> listBuilder = ImmutableList.builder();
        listBuilder.add(new NameConsumer());
        listBuilder.add(new NicknameConsumer());
        listBuilder.add(new EmailAddressesConsumer(i18nService));
        listBuilder.add(new BirthdayConsumer());
        listBuilder.add(new OccupationConsumer());
        listBuilder.add(new ImAddressesConsumer(i18nService));
        listBuilder.add(new PhoneNumbersConsumer(i18nService));
        listBuilder.add(new PhotoConsumer(googlePeopleService, maxImageSize));
        listBuilder.add(new AddressConsumer(i18nService));
        elementParsers = listBuilder.build();
    }

    /**
     * Parses the specified {@link ListConnectionsResponse} and returns it as a {@link List} of {@link Contact}s
     *
     * @param response The {@link ListConnectionsResponse} to parse
     * @return a {@link List} of {@link Contact}s
     */
    public List<Contact> parseListConnectionsResponse(ListConnectionsResponse response) {
        List<Person> connections = response.getConnections();
        return connections.stream().map(person -> parsePerson(person)).collect(Collectors.toList());
    }

    /**
     * Parses the specified {@link Person} to a {@link Contact}
     *
     * @param person The {@link Person} to parse
     * @return a new {@link Contact}
     */
    private Contact parsePerson(Person person) {
        Contact contact = new Contact();
        elementParsers.forEach(parser -> parser.accept(person, contact));
        return contact;
    }
}
