
package com.openexchange.oauth.impl;

import org.scribe.model.Verb;

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

/**
 * {@link OAuthIdentityAware}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.10.0
 */
public interface OAuthIdentityAware {

    /**
     * Returns the identity URL used to identity the current user
     * 
     * @param accessToken The accessToken in case it needs to be part of the URL
     * @return The identity URL used to identify the current user
     */
    String getIdentityURL(String accessToken);

    /**
     * Returns the HTTP method used for the identity request
     * 
     * @return HTTP method used for the identity request
     */
    Verb getIdentityHTTPMethod();

    /**
     * Returns the field name that contains the identity of the user in the response object
     * 
     * @return the field name that contains the identity of the user in the response object
     */
    String getIdentityFieldName();

    /**
     * Returns the content type of the identity request
     * 
     * @return the content type of the identity request
     */
    String getContentType();
}
