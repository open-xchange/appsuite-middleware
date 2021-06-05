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

package com.openexchange.xing.access;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link XingOAuthAccessProvider} - Provides a XING OAuth access for a given XING OAuth account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface XingOAuthAccessProvider {

    /**
     * Gets the XING OAuth access for given XING OAuth account.
     *
     * @param oauthAccountId The identifier of the XING OAuth account providing credentials and settings
     * @param session The user session
     * @return The XING OAuth access; either newly created or fetched from underlying registry
     * @throws OXException If a XING session could not be created
     */
    XingOAuthAccess accessFor(int oauthAccountId, Session session) throws OXException;

    /**
     * Gets the XING OAuth access for given XING OAuth account.
     *
     * @param token The identifier of the XING OAuth token
     * @param secret The identifier of the XING OAuth secret
     * @return The XING OAuth access; either newly created or fetched from underlying registry
     * @throws OXException If a XING session could not be created
     */
    XingOAuthAccess accessFor(String token, String secret, Session session) throws OXException;

    /**
     * Gets the identifier of the default XING OAuth account.
     *
     * @param session The associated session
     * @return The identifier of the default XING OAuth account
     * @throws OXException If retrieval fails
     */
    int getXingOAuthAccount(Session session) throws OXException;

}
