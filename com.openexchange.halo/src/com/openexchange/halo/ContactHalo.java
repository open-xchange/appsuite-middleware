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

import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.osgi.annotation.SingletonService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ContactHalo} - The singleton contact halo service.
 */
@SingletonService
public interface ContactHalo {

    /**
     * Investigates specified contact using given provider.
     *
     * @param provider The provider identifier
     * @param contact The contact to investigate
     * @param req The associated AJAX request
     * @param session The associated session
     * @return The investigation result
     * @throws OXException If operation fails
     */
    AJAXRequestResult investigate(String provider, Contact contact, AJAXRequestData req, ServerSession session) throws OXException;

    /**
     * Gets the contact's picture.
     *
     * @param contact The associated contact
     * @param session The session
     * @return The picture or <code>null</code>
     * @throws OXException If returning the picture fails
     */
    ContactPicture getPicture(Contact contact, ServerSession session) throws OXException;

    /**
     * Gets the ETag of the contact's picture.
     *
     * @param contact The associated contact
     * @param session The session
     * @return The picture's ETag or <code>null</code>
     * @throws OXException If returning the ETag fails
     */
    String getPictureETag(Contact contact, ServerSession session) throws OXException;

    /**
     * Gets the identifiers of all currently known providers
     *
     * @param session The associated session
     * @return A listing of the identifiers of all currently known providers
     * @throws OXException If listing cannot be returned
     */
    List<String> getProviders(ServerSession session) throws OXException;

}
