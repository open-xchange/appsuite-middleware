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

import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.contact.storage.ContactUserStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.tools.alias.UserAliasUtility;
import com.openexchange.session.Session;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link OwnContactFinder} checks if the user searches for its own contact via mail. E.g. in case the user has no access to the GAB.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class OwnContactFinder extends AbstractContactFinder {

    private final UserService userService;
    private final ContactUserStorage contactUserStorage;

    /**
     * Initializes a new {@link OwnContactFinder}.
     *
     * @param idBasedContactsAccessFactory The {@link IDBasedContactsAccessFactory}
     * @param userService The {@link UserService}
     * @param contactUserStorage The {@link ContactUserStorage}
     */
    public OwnContactFinder(IDBasedContactsAccessFactory idBasedContactsAccessFactory, UserService userService, ContactUserStorage contactUserStorage) {
        super(idBasedContactsAccessFactory);
        this.userService = userService;
        this.contactUserStorage = contactUserStorage;
    }

    @Override
    Contact getContact(Session session, PictureSearchData data, ContactField... fields) throws OXException {
        User user = userService.getUser(session.getUserId(), session.getContextId());
        if (user != null && isSearched(user, data)) {
            Contact contact;
            if (user.isGuest()) {
                contact = contactUserStorage.getGuestContact(session.getContextId(), session.getUserId(), fields);
            } else {
                IDBasedContactsAccess contactsAccess = idBasedContactsAccessFactory.createAccess(session);
                try {
                    contactsAccess.set(ContactsParameters.PARAMETER_FIELDS, fields);
                    contact = contactsAccess.getUserAccess().getUserContact(session.getUserId());
                } finally {
                    contactsAccess.finish();
                }
            }
            return contact;
        }
        return null;
    }

    /**
     * Checks if the own contact is a valid option for the search
     *
     * @param user The current user
     * @param data The search data
     * @return true if it is applicable, false otherwise
     */
    private boolean isSearched(User user, PictureSearchData data) {
        if (data.getUserId() != null && user.getId() == data.getUserId().intValue()) {
            return true;
        }
        for (String mail : data.getEmails()) {
            if (UserAliasUtility.isAlias(mail, user.getAliases())) {
                return true;
            }
        }

        return false;
    }

    @Override
    PictureSearchData modfiyResult(Contact contact) {
        // Do nothing
        return PictureSearchData.EMPTY_DATA;
    }

    @Override
    void handleException(PictureSearchData data, OXException exception) {
        LOGGER.debug("Unable to get own contact.", data.getUserId(), exception);
    }

}
