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

package com.openexchange.contact.picture.impl.finder;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.contact.picture.impl.ContactPictureUtil;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactsSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link ContactMailFinder} - Finds picture based on the provided mail addresses
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ContactMailFinder extends AbstractContactFinder {

    /**
     * Initializes a new {@link ContactMailFinder}.
     *
     * @param idBasedContactsAccessFactory The {@link IDBasedContactsAccessFactory}
     */
    public ContactMailFinder(IDBasedContactsAccessFactory idBasedContactsAccessFactory) {
        super(idBasedContactsAccessFactory);
    }
    // ---------------------------------------------------------------------------------------------

    @Override
    public Contact getContact(Session session, PictureSearchData data, ContactField... fields) throws OXException {
        if (data.hasEmail()) {
            return findContactByMail(session, data, fields);
        }
        return null;
    }

    @Override
    public PictureSearchData modfiyResult(Contact contact) {
        // Do nothing
        return PictureSearchData.EMPTY_DATA;
    }

    @Override
    void handleException(PictureSearchData data, OXException e) {
        LOGGER.debug("Unable to get contact for mail addresses {}.", data.getEmails(), e);
    }
    // ---------------------------------------------------------------------------------------------

    /**
     * Searches for a contact via its mail address.
     *
     * @param session The {@link Session}
     * @param data The {@link PictureSearchData}
     * @param fields The {@link ContactField}s that should be retrieved
     * @return The {@link Contact} or <code>null</code>
     * @throws OXException If the contact could not be found
     */
    private Contact findContactByMail(Session session, PictureSearchData data, ContactField... fields) throws OXException {
        // Make sure fields for sorting are available
        if (false == Arrays.contains(fields, ContactField.OBJECT_ID)) {
            Arrays.add(fields, ContactField.OBJECT_ID);
        }
        if (false == Arrays.contains(fields, ContactField.FOLDER_ID)) {
            Arrays.add(fields, ContactField.FOLDER_ID);
        }

        Set<String> emails = data.getEmails();
        for (Iterator<String> iterator = emails.iterator(); iterator.hasNext();) {
            String email = iterator.next();
            if (Strings.isEmpty(email) || false == ContactPictureUtil.isValidMailAddress(email)) {
                // skip empty email addresses
                continue;
            }

            ContactsSearchObject cso = new ContactsSearchObject();
            cso.setAllEmail(email);
            cso.setOrSearch(true);
            cso.setHasImage(true);
            cso.setExactMatch(true);

            // Search from system folder (e.g. GAB, if accessible) to private folders, by oldest contacts first
            List<Contact> result;
            IDBasedContactsAccess contactsAccess = idBasedContactsAccessFactory.createAccess(session);
            try {
                contactsAccess.set(ContactsParameters.PARAMETER_FIELDS, fields);
                contactsAccess.set(ContactsParameters.PARAMETER_ORDER_BY, ContactField.FOLDER_ID);
                contactsAccess.set(ContactsParameters.PARAMETER_ORDER, Order.ASCENDING);
                result = contactsAccess.searchContacts(cso);
            } finally {
                contactsAccess.finish();
            }

            if (result != null) {
                //Find the first result with an image
                Optional<Contact> contact = result.stream().filter( c -> c.getImage1() != null).findFirst();
                return contact.orElseGet(() -> null);
            }
        }
        return null;
    }

}
