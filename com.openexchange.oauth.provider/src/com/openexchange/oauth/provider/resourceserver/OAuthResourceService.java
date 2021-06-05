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

package com.openexchange.oauth.provider.resourceserver;

import javax.servlet.http.HttpServletRequest;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * Service interface to to be used by the resource server components (i.e. API implementations that
 * provide OAuth 2.0 as authentication and authorization mechanism) to e.g. validate access tokens.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.1
 */
@SingletonService
public interface OAuthResourceService {

    /**
     * Validates the given access token. If the token is valid, an according session is looked up or created
     * and an {@link OAuthAccess} instance is returned.
     *
     * @param accessToken The access token
     * @param httpRequest The servlet request
     * @return The access
     * @throws OXException If the token is invalid {@link OAuthInvalidTokenException} is thrown
     */
    OAuthAccess checkAccessToken(String accessToken, HttpServletRequest httpRequest) throws OXException;

    /**
     * Checks if the OAuth provider is enabled for the given user.
     *
     * @param contextId The context ID
     * @param userId The user ID
     * @return <code>true</code> if the provider is enabled
     * @throws OXException
     */
    boolean isProviderEnabled(int contextId, int userId) throws OXException;

}
