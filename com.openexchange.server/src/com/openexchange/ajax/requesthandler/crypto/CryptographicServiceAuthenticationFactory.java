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

package com.openexchange.ajax.requesthandler.crypto;

import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link CryptographicServiceAuthenticationFactory} parses authentication for cryptographic services from HTTP requests.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.8.3
 */
public interface CryptographicServiceAuthenticationFactory {

    /**
     * Parses authentication information from the given request.
     *
     * @param request The request to parse the authentication information from.
     * @return The authentication information obtained from the request, or null if the request does not contain all necessary information.
     * @throws OXException
     */
    String createAuthenticationFrom(HttpServletRequest request) throws OXException;

    /**
     * Parses authentication information for a given {@link AJAXRequestData} object.
     *
     * @param requestData The {@link AJAXRequestData} to parse the authentication information from
     * @return The authentication information obtained from the {@link AJAXRequestData} object, or null if the {@link AJAXRequestData} object does not contain all necessary information.
     * @throws OXException
     */
    String createAuthenticationFrom(AJAXRequestData requestData) throws OXException;

    /**
     * Parses the authentication information from a given {@link Session} and the given data
     *
     * @param session The session to obtain required authentication information from
     * @param data additional data to construct the actual authentication information from, or null in order to fall back to {@link #getAuthTokenFromSession(Session)}
     * @return The authentication information obtained from the given {@link Session} and data, or null if the given parameters do not contain all necessary and not authentication is attached to the given session.
     * @throws OXException
     */
    String createAuthenticationFrom(Session session, String data) throws OXException;

    /**
     * Gets the cryptographic session identifier from the given {@link Session}
     *
     * @param session  The session to get the cryptographic session identifier for
     * @return The value operating as cryptographic session identifier
     * @throws OXException
     */
    String getSessionValueFrom(Session session) throws OXException;

    /**
     * Gets the authentication token for specified session
     *
     * @param session The session
     * @return The authentication token
     * @throws OXException If authentication token cannot be returned
     */
    String getAuthTokenFromSession(Session session) throws OXException;

    /**
     * Gets the token value from full authentication token's string
     *
     * @param string The authentication token's string
     * @return The token value
     */
    String getTokenValueFromString(String string);

}
