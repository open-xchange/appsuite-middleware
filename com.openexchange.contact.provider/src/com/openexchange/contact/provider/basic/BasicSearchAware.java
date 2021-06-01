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

package com.openexchange.contact.provider.basic;

import java.util.List;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.provider.ContactsAccess;
import com.openexchange.contact.provider.extensions.SearchAware;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactsSearchObject;
import com.openexchange.search.SearchTerm;

/**
 * {@link BasicSearchAware} - Provides access to search functionality
 * for a {@link ContactsAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public interface BasicSearchAware extends SearchAware {

    /**
     * Searches for contacts.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * </ul>
     *
     * @param term the search term
     * @return the contacts found with the search
     * @throws OXException
     */
    <O> List<Contact> searchContacts(SearchTerm<O> term) throws OXException;

    /**
     * Searches for contacts.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * </ul>
     *
     * @param contactSearch The contact search object
     * @return The found contacts
     */
    List<Contact> searchContacts(ContactsSearchObject contactSearch) throws OXException;

    /**
     * Performs an "auto-complete" lookup for contacts. Depending <code>com.openexchange.contacts.allFoldersForAutoComplete</code>, either
     * all folders visible to the user, or a reduced set of specific folders is used for the search.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     * </ul>
     *
     * @param query The search query as supplied by the client
     * @return The contacts found with the search
     * @throws OXException
     */
    List<Contact> autocompleteContacts(String query) throws OXException;

}
