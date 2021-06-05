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

import static com.openexchange.java.Autoboxing.i;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.session.Session;

/**
 * {@link ContactUserFinder} - Finds picture based on user identifier
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class ContactUserFinder extends AbstractContactFinder {

    /**
     * Initializes a new {@link ContactUserFinder}.
     *
     * @param accessFactory A reference to the contacts access factory
     */
    public ContactUserFinder(IDBasedContactsAccessFactory accessFactory) {
        super(accessFactory);
    }

    @Override
    public Contact getContact(Session session, PictureSearchData data, ContactField... fields) throws OXException {
        if (data.hasUser()) {
            IDBasedContactsAccess contactsAccess = idBasedContactsAccessFactory.createAccess(session);
            try {
                contactsAccess.set(ContactsParameters.PARAMETER_FIELDS, fields);
                if (Integer.toString(FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID).equals(data.getFolderId())) {
                    return contactsAccess.getUserAccess().getGuestContact(i(data.getUserId()));
                }
                return contactsAccess.getUserAccess().getUserContact(i(data.getUserId()));
            } finally {
                contactsAccess.finish();
            }
        }
        return null;
    }

    @Override
    public PictureSearchData modfiyResult(Contact contact) {
        return new PictureSearchData(null, null, contact.getFolderId(true), contact.getId(true), null);
    }

    @Override
    public void handleException(PictureSearchData data, OXException e) {
        LOGGER.debug("Unable to get contact for user {},", data.getUserId(), e);
    }

}
