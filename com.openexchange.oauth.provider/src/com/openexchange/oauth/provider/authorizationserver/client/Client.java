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

package com.openexchange.oauth.provider.authorizationserver.client;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.openexchange.oauth.provider.resourceserver.scope.Scope;


/**
 * {@link Client} - Represents an OAuth client.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public interface Client extends Serializable {

    /**
     * Gets the clients public identifier
     *
     * @return The public identifier
     */
    String getId();

    /**
     * Gets the clients name
     *
     * @return The name
     */
    String getName();

    /**
     * Gets the clients description
     *
     * @return The description
     */
    String getDescription();

    /**
     * Gets the icon
     *
     * @return The icon
     */
    Icon getIcon();

    /**
     * Gets the date of this clients registration.
     *
     * @return The date
     */
    Date getRegistrationDate();

    /**
     * Gets the contact address
     *
     * @return The address
     */
    String getContactAddress();

    /**
     * Gets the website
     *
     * @return The website
     */
    String getWebsite();

    /**
     * Gets the default scope that should be applied when an authorization request does not
     * specify a certain scope.
     *
     * @return The default scope
     */
    Scope getDefaultScope();

    /**
     * Gets the clients secret identifier
     *
     * @return The secret identifier
     */
    String getSecret();

    /**
     * Gets the redirect URIs
     *
     * @return The URIs
     */
    List<String> getRedirectURIs();

    /**
     * Checks if given redirect URI is contained in registered redirect URIs for this client
     *
     * @param uri The URI to check
     * @return <code>true</code> if contained; otherwise <code>false</code>
     */
    boolean hasRedirectURI(String uri);

    /**
     * Returns whether this client is enabled or not.
     *
     * @return <code>true</code> if the client is enabled
     */
    boolean isEnabled();

}
