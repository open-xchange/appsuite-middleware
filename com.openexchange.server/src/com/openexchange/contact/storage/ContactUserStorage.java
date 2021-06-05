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

package com.openexchange.contact.storage;

import java.sql.Connection;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.session.Session;


/**
 * {@link ContactUserStorage}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8
 */
public interface ContactUserStorage extends ContactStorage {

    /**
     * Creates a contact for a guest user
     *
     * @param contextId The context id
     * @param contact The contact
     * @param con Database connection
     * @return Id of created contact
     * @throws OXException On error
     */
    int createGuestContact(int contextId, Contact contact, Connection con) throws OXException;

    /**
     * Deletes a contact for a guest user
     *
     * @param contextId The context id
     * @param userId The internal user id
     * @param lastRead Time when the contact was last read from storage
     * @param con Database connection
     * @throws OXException On error
     */
    void deleteGuestContact(int contextId, int userId, Date lastRead, Connection con) throws OXException;

    /**
     * Updates a contact for a guest user without any checks
     *
     * @param contextId The context id
     * @param contactId The contact id
     * @param contact The updated contact
     * @param con Database connection
     * @throws OXException On error
     */
    void updateGuestContact(int contextId, int contactId, Contact contact, Connection con) throws OXException;

    /**
     * Updates a contact for a guest user
     *
     * @param session
     * @param contactId The contact id
     * @param contact The updated contact
     * @param lastRead Time when the contact was last read from storage
     * @throws OXException On error
     */
    void updateGuestContact(Session session, int contactId, Contact contact, Date lastRead) throws OXException;

    /**
     * Gets the guest's contact
     * @param contextId The context id
     * @param guestId The guest id
     * @param contactFields Fields to fill in the contact
     * @return The contact
     * @throws OXException On error
     */
    Contact getGuestContact(int contextId, int guestId, ContactField[] contactFields) throws OXException;

}
