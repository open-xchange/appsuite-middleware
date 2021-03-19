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

package com.openexchange.contact.provider.basic;

import java.util.Collections;
import java.util.List;
import com.openexchange.contact.common.ContactsParameters;
import com.openexchange.contact.provider.ContactsAccess;
import com.openexchange.contact.provider.ContactsProviderExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

/**
 * {@link BasicContactsAccess} - A read-only contacts access
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public interface BasicContactsAccess extends ContactsAccess {

    /** The virtual folder identifier used for basic contact providers */
    static final String FOLDER_ID = "0";

    /**
     * Gets the contacts settings.
     *
     * @return The settings
     */
    ContactsSettings getSettings();

    /**
     * Gets a specific contact
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param contactId The identifier of the contact to get
     * @return The contact
     */
    default Contact getContact(String contactId) throws OXException {
        List<Contact> contacts = getContacts(Collections.singletonList(contactId));
        if (null == contacts || contacts.isEmpty()) {
            throw ContactsProviderExceptionCodes.CONTACT_NOT_FOUND_IN_FOLDER.create(FOLDER_ID, contactId);
        }
        return contacts.get(0);
    }

    /**
     * Gets a list of contacts with the specified identifiers.
     *
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * </ul>
     *
     * @param contactIds A list of the identifiers of the contacts to get
     * @return The contacts
     */
    List<Contact> getContacts(List<String> contactIds) throws OXException;

    /**
     * Gets all contacts in this account.
     * <p/>
     * The following contacts parameters are evaluated:
     * <ul>
     * <li>{@link ContactsParameters#PARAMETER_FIELDS}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER}</li>
     * <li>{@link ContactsParameters#PARAMETER_ORDER_BY}</li>
     *
     * @return The contacts
     */
    List<Contact> getContacts() throws OXException;

}
