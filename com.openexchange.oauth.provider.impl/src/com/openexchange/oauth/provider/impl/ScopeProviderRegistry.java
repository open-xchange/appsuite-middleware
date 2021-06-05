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

package com.openexchange.oauth.provider.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.oauth.provider.resourceserver.scope.OAuthScopeProvider;


/**
 * {@link ScopeProviderRegistry}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ScopeProviderRegistry {

    private static final ScopeProviderRegistry INSTANCE = new ScopeProviderRegistry();

    private final ConcurrentMap<String, OAuthScopeProvider> providers = new ConcurrentHashMap<>();

    private ScopeProviderRegistry() {
        super();
    }

    public static ScopeProviderRegistry getInstance() {
        return INSTANCE;
    }

    public void addScopeProvider(OAuthScopeProvider provider) {
        providers.put(provider.getToken(), provider);
    }

    public void removeScopeProvider(OAuthScopeProvider provider) {
        providers.remove(provider.getToken(), provider);
    }

    public OAuthScopeProvider getProvider(String token) {
        return providers.get(token);
    }

    public boolean hasScopeProvider(String token) {
        return providers.containsKey(token);
    }

}
