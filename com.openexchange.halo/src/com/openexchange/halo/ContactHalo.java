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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.halo;

import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
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
    Picture getPicture(Contact contact, ServerSession session) throws OXException;

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
