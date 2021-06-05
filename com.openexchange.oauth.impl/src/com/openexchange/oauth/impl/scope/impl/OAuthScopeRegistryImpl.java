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

package com.openexchange.oauth.impl.scope.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.API;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OAuthScopeExceptionCodes;
import com.openexchange.oauth.scope.OAuthScopeRegistry;
import com.openexchange.oauth.scope.OXScope;

/**
 * {@link OAuthScopeRegistryImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OAuthScopeRegistryImpl implements OAuthScopeRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthScopeRegistryImpl.class);

    private final ConcurrentMap<String, ConcurrentMap<OAuthScope, OAuthScope>> registry;

    /**
     * Initialises a new {@link OAuthScopeRegistryImpl}.
     */
    public OAuthScopeRegistryImpl() {
        super();
        registry = new ConcurrentHashMap<>();
    }

    @Override
    public void registerScope(API api, OAuthScope scope) {
        if (null == api || null == scope) {
            return;
        }

        ConcurrentMap<OAuthScope, OAuthScope> scopes = registry.get(api.getServiceId());
        if (null == scopes) {
            ConcurrentMap<OAuthScope, OAuthScope> newScopes = new ConcurrentHashMap<>(8, 0.9F, 1);
            scopes = registry.putIfAbsent(api.getServiceId(), newScopes);
            if (null == scopes) {
                scopes = newScopes;
            }
        }
        scopes.put(scope, scope);
    }

    @Override
    public void registerScopes(API api, OAuthScope... scopes) {
        for (OAuthScope scope : scopes) {
            registerScope(api, scope);
        }
    }

    @Override
    public void unregisterScope(API api, OXScope module) {
        try {
            OAuthScope scope = getScope(api, module);

            ConcurrentMap<OAuthScope, OAuthScope> scopes = registry.get(api.getServiceId());
            if (scopes == null || scopes.isEmpty()) {
                return;
            }

            scopes.remove(scope);
        } catch (OXException e) {
            LOG.warn("{}", e.getMessage(), e);
        }
    }

    @Override
    public void unregisterScopes(API api) {
        registry.remove(api.getServiceId());
    }

    @Override
    public void purge() {
        registry.clear();
    }

    @Override
    public Set<OAuthScope> getAvailableScopes(API api) throws OXException {
        ConcurrentMap<OAuthScope, OAuthScope> scopes = registry.get(api.getServiceId());
        if (scopes == null) {
            throw OAuthScopeExceptionCodes.NO_SCOPES.create(api.getDisplayName());
        }
        return Collections.unmodifiableSet(scopes.keySet());
    }

    @Override
    public Set<OAuthScope> getLegacyScopes(API api) throws OXException {
        ConcurrentMap<OAuthScope, OAuthScope> scopes = registry.get(api.getServiceId());
        Set<OAuthScope> legacyScopes = new HashSet<>();
        for (OAuthScope scope : scopes.keySet()) {
            if (scope.getOXScope().isLegacy()) {
                legacyScopes.add(scope);
            }
        }
        if (legacyScopes.isEmpty()) {
            throw OAuthScopeExceptionCodes.NO_LEGACY_SCOPES.create(api.getDisplayName());
        }
        return legacyScopes;
    }

    @Override
    public Set<OAuthScope> getAvailableScopes(API api, OXScope... modules) throws OXException {
        ImmutableSet.Builder<OAuthScope> availableScopes = ImmutableSet.builder();
        for (OXScope module : modules) {
            availableScopes.add(getScope(api, module));
        }
        return availableScopes.build();
    }

    @Override
    public OAuthScope getScope(API api, OXScope module) throws OXException {
        ConcurrentMap<OAuthScope, OAuthScope> scopes = registry.get(api.getServiceId());
        if (scopes == null) {
            throw OAuthScopeExceptionCodes.NO_SCOPES.create(api.getDisplayName());
        }

        Set<OAuthScope> availableScopes = scopes.keySet();
        for (OAuthScope scope : availableScopes) {
            if (scope.getOXScope().equals(module)) {
                return scope;
            }
        }
        throw OAuthScopeExceptionCodes.NO_SCOPE_FOR_MODULE.create(module, api.getDisplayName());
    }
}
