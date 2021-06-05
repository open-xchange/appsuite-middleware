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

package com.openexchange.contact.picture;

import java.util.Date;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.session.Session;

/**
 * {@link ContactPictureService} - Service to get the contact picture
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
@SingletonService
public interface ContactPictureService {

    /**
     * Get the contact picture for the provided {@link PictureSearchData}
     * 
     * @param session The session
     * @param data The {@link PictureSearchData} to get the picture for
     * @return The {@link ContactPicture} and never <code>null</code>. If no picture is found {@link ContactPicture#NOT_FOUND} is used
     */
    ContactPicture getPicture(Session session, PictureSearchData data);

    /**
     * Get the ETag for a contact picture.
     * 
     * @param session The session
     * @param data The {@link PictureSearchData} to get the pictures ETag for
     * @return The ETag and never <code>null</code>. If no picture is found {@link ContactPicture#NOT_FOUND} is used to get the ETag
     */
    String getETag(Session session, PictureSearchData data);

    /**
     * Get the timestamp of the last modification date for a picture
     * 
     * @param session The {@link Session}
     * @param data The {@link PictureSearchData} to get the pictures last modified for
     * @return A timestamp as {@link Date}. If not found {@link ContactPicture#UNMODIFIED} is returned
     */
    Date getLastModified(Session session, PictureSearchData data);

}
