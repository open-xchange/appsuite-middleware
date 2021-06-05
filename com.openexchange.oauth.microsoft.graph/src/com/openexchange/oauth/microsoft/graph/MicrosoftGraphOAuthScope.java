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

package com.openexchange.oauth.microsoft.graph;

import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OXScope;

/**
 * {@link MicrosoftGraphOAuthScope}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public enum MicrosoftGraphOAuthScope implements OAuthScope {

    /**
     * Defines the drive scopes
     * 
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/permissions_reference#files-permissions">File Permissions</a>
     */
    drive("offline_access User.Read Files.Read Files.Read.All Files.ReadWrite Files.ReadWrite.All Files.Read.Selected Files.ReadWrite.Selected", OXScope.drive),
    /**
     * Defines the contacts' scopes
     * 
     * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/permissions_reference#contacts-permissions">Contacts Permissions</a>
     */
    contacts_ro("offline_access User.Read Contacts.Read Contacts.Read.Shared", OXScope.contacts_ro);

    private final String scopes;
    private final OXScope oxScope;

    /**
     * Initialises a new {@link MicrosoftGraphOAuthScope}.
     */
    private MicrosoftGraphOAuthScope(String scopes, OXScope oxScope) {
        this.scopes = scopes;
        this.oxScope = oxScope;
    }

    @Override
    public String getProviderScopes() {
        return scopes;
    }

    @Override
    public OXScope getOXScope() {
        return oxScope;
    }

}
