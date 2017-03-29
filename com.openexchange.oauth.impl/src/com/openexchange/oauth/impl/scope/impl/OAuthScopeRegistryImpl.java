/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import com.openexchange.exception.OXException;
import com.openexchange.oauth.API;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.oauth.scope.OAuthScopeExceptionCodes;
import com.openexchange.oauth.scope.OAuthScopeRegistry;

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
            throw OAuthScopeExceptionCodes.NO_SCOPES.create(api.getName());
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
            throw OAuthScopeExceptionCodes.NO_LEGACY_SCOPES.create(api.getName());
        }
        return legacyScopes;
    }

    @Override
    public Set<OAuthScope> getAvailableScopes(API api, OXScope... modules) throws OXException {
        Set<OAuthScope> availableScopes = new HashSet<>(modules.length);
        for (OXScope module : modules) {
            availableScopes.add(getScope(api, module));
        }
        return Collections.unmodifiableSet(availableScopes);
    }

    @Override
    public OAuthScope getScope(API api, OXScope module) throws OXException {
        ConcurrentMap<OAuthScope, OAuthScope> scopes = registry.get(api.getServiceId());
        if (scopes == null) {
            throw OAuthScopeExceptionCodes.NO_SCOPES.create(api.getName());
        }

        Set<OAuthScope> availableScopes = scopes.keySet();
        for (OAuthScope scope : availableScopes) {
            if (scope.getOXScope().equals(module)) {
                return scope;
            }
        }
        throw OAuthScopeExceptionCodes.NO_SCOPE_FOR_MODULE.create(module, api.getName());
    }
}
