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

package com.openexchange.contact.picture.finder;

import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.Ranked;
import com.openexchange.session.Session;

/**
 * {@link ContactPictureFinder} - Class that tries to lookup a contact picture in a specific service
 * 
 * Ranking of registered {@link ContactPictureFinder}:
 * <li> 1000 : UserPictureFinder</li>
 * <li> 500 : ContactFinders (Children will register with 20 + continuous, decrementing number)</li>
 * <li> 100 : GAB</li>
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public interface ContactPictureFinder extends Ranked {

    /**
     * Get the contact picture for the provided {@link PictureSearchData}
     *
     * @param session The {@link Session}
     * @param data The {@link PictureSearchData}
     * @return A {@link PictureResult} an <b>never</b> null
     * @throws OXException If harmful picture was found
     */
    PictureResult getPicture(Session session, PictureSearchData data) throws OXException;

    /**
     * Get the ETag for an contact picture
     *
     * @param session The {@link Session}
     * @param data The {@link PictureSearchData}
     * @return A {@link PictureResult} an <b>never</b> null
     * @throws OXException On error
     */
    PictureResult getETag(Session session, PictureSearchData data) throws OXException;
    
    
    /**
     * Get the timestamp of the last modification date for a picture
     * 
     * @param session The {@link Session}
     * @param data The {@link PictureSearchData} to get the pictures last modified for
     * @return A {@link PictureResult} an <b>never</b> null
     */
    PictureResult getLastModified(Session session, PictureSearchData data);

}
