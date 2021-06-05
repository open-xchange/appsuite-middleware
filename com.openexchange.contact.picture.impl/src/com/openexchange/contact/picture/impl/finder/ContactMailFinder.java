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

package com.openexchange.contact.picture.impl.finder;

import static com.openexchange.java.Autoboxing.I;
import java.util.Iterator;
import java.util.List;
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
                contactsAccess.set(ContactsParameters.PARAMETER_RIGHT_HAND_LIMIT, I(1));
                result = contactsAccess.searchContacts(cso);
            } finally {
                contactsAccess.finish();
            }

            return null != result && 0 < result.size() ? result.get(0) : null;
        }
        return null;
    }

}
