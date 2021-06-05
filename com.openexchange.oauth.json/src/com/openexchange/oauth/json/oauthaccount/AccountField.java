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

package com.openexchange.oauth.json.oauthaccount;

import com.openexchange.oauth.OAuthConstants;

/**
 * {@link AccountField} - Enumeration for OAuth account fields.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum AccountField {

    /**
     * The identifier
     */
    ID("id"),
    /**
     * The display name
     */
    DISPLAY_NAME(OAuthConstants.ARGUMENT_DISPLAY_NAME),
    /**
     * The service identifier
     */
    SERVICE_ID("serviceId"),
    /**
     * The token
     */
    TOKEN("token"),
    /**
     * The secret
     */
    SECRET(OAuthConstants.ARGUMENT_SECRET),
    /**
     * The authorization URL
     */
    AUTH_URL("authUrl"),
    /**
     * The interaction type
     */
    INTERACTION_TYPE("type"),
    /**
     * The enabled OAuth scopes of the account
     */
    ENABLED_SCOPES("enabledScopes"),
    /**
     * The available scopes of the provider
     */
    AVAILABLE_SCOPES("availableScopes"),
    /**
     * The associated accounts of the different modules
     */
    ASSOCIATIONS("associations")
    ;

    private final String name;

    private AccountField(final String name) {
        this.name = name;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

}
