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

import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.session.Session;

/**
 * {@link ContactTombstoneStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public interface ContactTombstoneStorage {

    /**
     * Gets a value indicating whether the storage supports a folder or not.
     *
     * @param session the session
     * @param folderId the ID of the folder to check the support for
     * @return <code>true</code>, if the folder is supported, <code>false</code>, otherwise
     */
    boolean supports(Session session, String folderId) throws OXException;

    /**
     * Inserts a contact record into the <i>tombstone</i> table to represent a deletion of a specific contact in a certain folder. This
     * will effectively make the contact appear in the results of the {@link #deleted}-methods for this folder.
     * <p/>
     * Unless already set in the passed contact, a new object identifier will be generated implicitly.
     * <p/>
     * This method is aimed to correct synchronization issues with external clients.
     *
     * @param session The session
     * @param folderId The identifier of the parent folder to insert the tombstone for
     * @param contact The contact to insert as tombstone
     */
    void insertTombstone(Session session, String folderId, Contact contact) throws OXException;

}
