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

package com.openexchange.oauth.provider.authorizationserver.grant;

import java.util.Date;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;

/**
 * An {@link Grant} encapsulates the context information of an access token.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 * @see Scope
 */
public interface Grant {

    /**
     * Gets the ID of the users context.
     *
     * @return The context ID
     */
    int getContextId();

    /**
     * Gets the ID of the user.
     *
     * @return The user ID
     */
    int getUserId();

    /**
     * Gets the access token.
     *
     * @return The access token
     */
    String getAccessToken();

    /**
     * Gets the refresh token if the server supports it
     *
     * @return The refresh token or <code>null</code>
     */
    String getRefreshToken();

    /**
     * Gets the expiration date.
     *
     * @return The expiration date
     */
    Date getExpirationDate();

    /**
     * Gets the scopes.
     *
     * @return The scopes
     */
    Scope getScope();

    /**
     * Gets the identifier of the client this grant belongs to.
     *
     * @return The client identifier
     */
    String getClientId();

}
