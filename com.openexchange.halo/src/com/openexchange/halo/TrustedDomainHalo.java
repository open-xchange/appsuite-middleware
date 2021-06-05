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

package com.openexchange.halo;

import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link TrustedDomainHalo}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public interface TrustedDomainHalo {

    /**
     * Retrieves the picture of a trusted domain
     * 
     * @param trustedDomain The domain
     * @param session The {@link Session}
     * @return the {@link ContactPicture}
     * @throws OXException On error
     */
    public ContactPicture getPicture(String trustedDomain, Session session) throws OXException;

    /**
     * Retrieves the etag of a trusted domain picture
     * 
     * @param trustedDomain The domain
     * @param session The {@link Session}
     * @return the etag of the {@link ContactPicture}
     * @throws OXException ON error
     */
    public String getPictureETag(String trustedDomain, Session session) throws OXException;

}
