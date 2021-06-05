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

import java.util.LinkedHashSet;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link ContactIDFinder} - Finds picture based on contact identifier
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ContactIDFinder extends AbstractContactFinder {

    /**
     * Initializes a new {@link ContactUserFinder}.
     *
     * @param idBasedContactsAccessFactory The {@link IDBasedContactsAccessFactory}
     */
    public ContactIDFinder(IDBasedContactsAccessFactory idBasedContactsAccessFactory) {
        super(idBasedContactsAccessFactory);
    }

    private static final ContactField[] FIELDS = new ContactField[] { ContactField.EMAIL1, ContactField.EMAIL2, ContactField.EMAIL3 };

    @Override
    public Contact getContact(Session session, PictureSearchData data, ContactField... fields) throws OXException {
        if (data.hasContact() && data.hasFolder()) {
            IDBasedContactsAccess contactsAccess = idBasedContactsAccessFactory.createAccess(session);
            try {
                contactsAccess.set(ContactsParameters.PARAMETER_FIELDS, Arrays.add(fields, FIELDS));
                return contactsAccess.getContact(createContactID(data));
            } finally {
                contactsAccess.finish();
            }
        }
        return null;
    }

    @Override
    public PictureSearchData modfiyResult(Contact contact) {
        LinkedHashSet<String> set = new LinkedHashSet<>(4, .9f);
        if (contact.containsEmail1() && Strings.isNotEmpty(contact.getEmail1())) {
            set.add(contact.getEmail1());
        }
        if (contact.containsEmail2() && Strings.isNotEmpty(contact.getEmail2())) {
            set.add(contact.getEmail2());
        }
        if (contact.containsEmail3() && Strings.isNotEmpty(contact.getEmail3())) {
            set.add(contact.getEmail3());
        }
        return new PictureSearchData(null, null, null, null, set);
    }

    @Override
    public void handleException(PictureSearchData data, OXException e) {
        LOGGER.debug("Unable to get contact for ID {} in folder {},", data.getContactId(), data.getFolderId(), e);
    }

}
