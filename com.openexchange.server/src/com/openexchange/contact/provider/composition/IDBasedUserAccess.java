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

package com.openexchange.contact.provider.composition;

import java.util.List;
import com.openexchange.contact.ContactService;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactsSearchObject;
import com.openexchange.search.SearchTerm;

/**
 * {@link IDBasedUserAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v8.0.0
 */
public interface IDBasedUserAccess {

    /**
     * Gets contact information associated with internal users.
     * <p/>
     * If the current user has no adequate permissions for the global address book folder where contact data for internal users is stored,
     * no exception is thrown, but the queried contact fields are limited to the fields defined by
     * {@link ContactService#LIMITED_USER_FIELDS} or {@link ContactService#LIMITED_USER_FIELDS_NO_MAIL} respectively.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param userIds The identifiers of the users to get the contact data for
     * @return The user contacts
     */
    List<Contact> getUserContacts(int[] userIds) throws OXException;

    /**
     * Gets contact information associated with a specific internal user.
     * <p/>
     * If the current user has no adequate permissions for the global address book folder where contact data for internal users is stored,
     * no exception is thrown, but the queried contact fields are limited to the fields defined by
     * {@link ContactService#LIMITED_USER_FIELDS} or {@link ContactService#LIMITED_USER_FIELDS_NO_MAIL} respectively.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param userId The identifier of the user to get the contact data for
     * @return The user contact
     */
    default Contact getUserContact(int userId) throws OXException {
        List<Contact> contacts = getUserContacts(new int[] { userId });
        if (null == contacts || contacts.isEmpty()) {
            throw OXException.notFound(String.valueOf(userId));
        }
        return contacts.get(0);
    }

    /**
     * Gets contact information associated with all internal users.
     * <p/>
     * If the current user has no adequate permissions for the global address book folder where contact data for internal users is stored,
     * no exception is thrown, but the queried contact fields are limited to the fields defined by
     * {@link ContactService#LIMITED_USER_FIELDS} or {@link ContactService#LIMITED_USER_FIELDS_NO_MAIL} respectively.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * <li>{@link ContactsParameters#PARAMETER_LEFT_HAND_LIMIT}</li>
     * <li>{@link ContactsParameters#PARAMETER_RIGHT_HAND_LIMIT}</li>
     * </ul>
     *
     * @return The user contacts
     */
    List<Contact> getUserContacts() throws OXException;

    /**
     * Searches for user contacts.
     * <p/>
     * If the current user has no adequate permissions for the global address book folder where contact data for internal users is stored,
     * no exception is thrown, but the queried contact fields are limited to the fields defined by
     * {@link ContactService#LIMITED_USER_FIELDS} or {@link ContactService#LIMITED_USER_FIELDS_NO_MAIL} respectively.
     * <p/>
     * <b>Note:</b> The folder-related parameters in the passed {@link ContactsSearchObject} have no effect.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * <li>{@link ContactsParameters#PARAMETER_LEFT_HAND_LIMIT}</li>
     * <li>{@link ContactsParameters#PARAMETER_RIGHT_HAND_LIMIT}</li>
     * </ul>
     *
     * @param contactSearch The contact search object
     * @return The found user contacts
     */
    List<Contact> searchUserContacts(ContactsSearchObject contactSearch) throws OXException;

    /**
     * Searches for user contacts.
     * <p/>
     * If the current user has no adequate permissions for the global address book folder where contact data for internal users is stored,
     * no exception is thrown, but the queried contact fields are limited to the fields defined by
     * {@link ContactService#LIMITED_USER_FIELDS} or {@link ContactService#LIMITED_USER_FIELDS_NO_MAIL} respectively.
     * <p/>
     * <b>Note:</b> The folder-related parameters in the passed {@link ContactsSearchObject} have no effect.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * <li>{@link ContactsParameters#PARAMETER_LEFT_HAND_LIMIT}</li>
     * <li>{@link ContactsParameters#PARAMETER_RIGHT_HAND_LIMIT}</li>
     * </ul>
     *
     * @param searchTerm The search term
     * @return The found user contacts
     */
    List<Contact> searchUserContacts(SearchTerm<?> searchTerm) throws OXException;

    /**
     * Gets contact information associated with a specific guest user.
     * <p/>
     * If the current user has no adequate permissions for the virtual folder where contact data for guest users is stored,
     * no exception is thrown, but the queried contact fields are limited to the fields defined by
     * {@link ContactService#LIMITED_USER_FIELDS} or {@link ContactService#LIMITED_USER_FIELDS_NO_MAIL} respectively.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param userId The identifier of the guest user to get the contact data for
     * @return The guest user contact
     */
    Contact getGuestContact(int userId) throws OXException;

}
