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

package com.openexchange.tokenlogin;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link TokenLoginService} - The token-login service.
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public interface TokenLoginService {

    /**
     * Acquires a unique token for specified session.
     * @param session The associated session
     *
     * @return The token as a string
     * @throws OXException If token cannot be generated for any reason
     */
    String acquireToken(Session session) throws OXException;

    /**
     * Redeems given token and generates an appropriate session.
     *
     * @param token The token previously generated
     * @param appSecret The secret identifier associated with requesting Web service/application
     * @param optClientIdentifier The optional client identifier
     * @param optAuthId The optional authentication identifier
     * @param optHash The optional hash value that applies to newly generated session
     * @param optClientIp The optional client IP address that applies to newly generated session
     * @param optUserAgent The optional user agent that applies to newly generated session
     * @return The generated session
     * @throws OXException If token cannot be turned into a valid session
     */
    Session redeemToken(String token, String appSecret, String optClientIdentifier, String optAuthId, String optHash, String optClientIp, String optUserAgent) throws OXException;

    /**
     * Gets the token-login secret (and its parameters) for specified secret identifier.
     *
     * @param secret The secret identifier
     * @return The associated token-login secret or <code>null</code> if there is none associated with given secret identifier
     */
    TokenLoginSecret getTokenLoginSecret(String secret);

}
