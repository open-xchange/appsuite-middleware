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
 *    trademarks of the OX Software GmbH. group of companies.
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
        return getUserContacts(new int[] { userId }).get(0);
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

}
