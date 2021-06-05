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

package com.openexchange.oauth.provider.resourceserver.scope;


/**
 * Convenience superclass for scope providers.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class AbstractScopeProvider implements OAuthScopeProvider {

    private final String token;
    private final String description;

    /**
     * Initializes a new {@link AbstractScopeProvider}.
     *
     * @param token The scope token
     * @param description The tokens description as localizable string
     */
    public AbstractScopeProvider(String token, String description) {
        super();
        this.token = token;
        this.description = description;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getDescription() {
        return description;
    }

}
