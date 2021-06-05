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

package com.openexchange.groupware.contact;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link ContactPictureURLService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public interface ContactPictureURLService {

    /**
     * Provides a URL to the picture of a contact.
     *
     * @param contactId The contact id.
     * @param folderId The folder of the contact id. Must not be null in case the contact id is set.
     * @param session The users session
     * @param timestamp An optional timestamp value to add to the url
     * @param preferRelativeUrl Whether a relative URL is preferred or not.
     * @return The URL to the picture.
     * @throws OXException If user or folder ID is missing or DispatcherPrefixService is absent
     */
    public String getContactPictureUrl(String contactId, String folderId, final Session session, Long timestamp, final boolean preferRelativeUrl) throws OXException;

    /**
     * Provides a URL to the picture of an internal user.
     *
     * @param userId The user id.
     * @param session The session
     * @param timestamp An optional timestamp value to add to the url
     * @param preferRelativeUrl Whether a relative URL is preferred or not.
     * @return The URL to the picture.
     * @throws OXException If user ID is missing or DispatcherPrefixService is absent
     */
    public String getUserPictureUrl(int userId, final Session session, Long timestamp, final boolean preferRelativeUrl) throws OXException;
}
