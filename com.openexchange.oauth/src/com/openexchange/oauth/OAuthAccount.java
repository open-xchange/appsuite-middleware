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

package com.openexchange.oauth;

import java.util.Set;
import com.openexchange.oauth.scope.OAuthScope;

/**
 * {@link OAuthAccount} - Represents an OAuth account.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public interface OAuthAccount extends OAuthToken {

    /**
     * Gets this account's identifier.
     *
     * @return The identifier
     */
    int getId();

    /**
     * Gets the display name.
     *
     * @return The display name
     */
    String getDisplayName();

    /**
     * Gets the associated OAuth meta data.
     *
     * @return The OAuth meta data
     */
    OAuthServiceMetaData getMetaData();

    /**
     * Gets the API style
     *
     * @return The API
     */
    API getAPI();

    /**
     * Returns an unmodifiable {@link Set} with all enabled {@link OAuthScope}s for this {@link OAuthAccount}
     *
     * @return an unmodifiable {@link Set} with all enabled {@link OAuthScope}s for this {@link OAuthAccount}
     */
    Set<OAuthScope> getEnabledScopes();

    /**
     * Returns the user's identity for this {@link OAuthAccount}
     * 
     * @return the user's identity for this {@link OAuthAccount} or <code>null</code>
     *         if no identity is set yet
     */
    String getUserIdentity();
}
