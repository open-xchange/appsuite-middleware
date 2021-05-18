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

package com.openexchange.subscribe.google.parser.consumers;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import com.google.api.services.people.v1.model.ImClient;
import com.google.api.services.people.v1.model.Person;
import com.openexchange.groupware.container.Contact;
import com.openexchange.i18n.I18nService;
import com.openexchange.java.Strings;
import com.openexchange.subscribe.google.parser.ContactNoteStrings;

/**
 * {@link ImAddressesConsumer} - Parses the instant messaging addresses. Note that google
 * can store an unlimited mount of instant messaging addresses for a contact due to their
 * different data model (probably EAV). Our contacts API however can only store two,
 * therefore we only fetch the first two we encounter.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:philipp.schumacher@open-xchange.com">Philipp Schumacher</a>
 * @since v7.10.1
 */
public class ImAddressesConsumer extends AbstractNoteConsumer implements BiConsumer<Person, Contact> {

    private static final Comparator<ImClient> IMCLIENT_COMPARATOR = new Comparator<ImClient>() {

        @Override
        public int compare(ImClient imClient1, ImClient imClient2) {
            String imClientString1 = Strings.concat("-", new String[] { imClient1.getType(), imClient1.getUsername() });
            String imClientString2 = Strings.concat("-", new String[] { imClient2.getType(), imClient2.getUsername() });
            return imClientString1.compareTo(imClientString2);
        }
    };

    /**
     * Initialises a new {@link ImAddressesConsumer}.
     */
    public ImAddressesConsumer(I18nService i18nService) {
        super(i18nService);
    }

    @Override
    public void accept(Person person, Contact contact) {
        List<ImClient> imClients = person.getImClients();
        if (imClients == null || imClients.isEmpty()) {
            return;
        }
        boolean firstImClientInNotes = true;
        Collections.sort(imClients, IMCLIENT_COMPARATOR);
        for (ImClient imClient : imClients) {
            firstImClientInNotes = setImAddress(imClient, contact, firstImClientInNotes);
        }
    }

    /**
     * Adds the instant messenger address to the contact.
     *
     * @param imClient The {@link ImClient}
     * @param contact The {@link Contact}
     */
    private boolean setImAddress(ImClient imClient, Contact contact, boolean firstImClientInNotes) {
        if (contact.getInstantMessenger1() == null) {
            contact.setInstantMessenger1(imClient.getFormattedProtocol() + ": " + imClient.getUsername());
            return firstImClientInNotes;
        } else if (contact.getInstantMessenger2() == null) {
            contact.setInstantMessenger2(imClient.getFormattedProtocol() + ": " + imClient.getUsername());
            return firstImClientInNotes;
        } else {
            return addImAddressToNote(imClient, contact, firstImClientInNotes);
        }
    }

    /**
     * Adds the instant messenger address to the contact's note
     *
     * @param imClient The {@link ImClient}
     * @param contact The {@link Contact}
     */
    private boolean addImAddressToNote(ImClient imClient, Contact contact, boolean firstImClientInNotes) {
        String imUsername = imClient.getUsername();
        String imProtocol = imClient.getFormattedProtocol();
        if (Strings.isEmpty(imUsername)) {
            return firstImClientInNotes;
        }
        String imString = imProtocol == null ? imUsername : imClient.getFormattedProtocol() + ": " + imClient.getUsername();
        return addValueToNote(contact, ContactNoteStrings.OTHER_IM_ADDRESSES, imString, firstImClientInNotes);
    }
}
