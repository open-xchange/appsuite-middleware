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

package com.openexchange.oauth.internal;

import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OXScope;

/**
 * {@link TestOAuthScope}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum TestOAuthScope implements OAuthScope {

    calendar("some scopes the OAuth provider specifies for CALENDAR", OXScope.calendar_ro),
    drive("some scopes the OAuth provider specifies for DRIVE", OXScope.drive),
    ;

    private final String mapping;
    private final OXScope module;

    /**
     * Initialises a new {@link TestOAuthScope}.
     */
    private TestOAuthScope(String mapping, OXScope module) {
        this.mapping = mapping;
        this.module = module;

    }

    @Override
    public String getProviderScopes() {
        return mapping;
    }

    @Override
    public OXScope getOXScope() {
        return module;
    }

}
