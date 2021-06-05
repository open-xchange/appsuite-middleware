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

package com.openexchange.oauth.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthAPIRegistry;

/**
 * {@link OAuthAPIRegistryImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class OAuthAPIRegistryImpl implements OAuthAPIRegistry {

    private static final OAuthAPIRegistry INSTANCE = new OAuthAPIRegistryImpl();

    /**
     * Gets the registry instance
     *
     * @return The instance
     */
    public static OAuthAPIRegistry getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------

    private final ConcurrentMap<String, API> registry;

    /**
     * Initializes a new {@link OAuthAPIRegistryImpl}.
     */
    private OAuthAPIRegistryImpl() {
        super();
        KnownApi[] apis = KnownApi.values();
        registry = new ConcurrentHashMap<>(apis.length, 0.9F, 1);
        for (KnownApi api : apis) {
            registry.put(Strings.asciiLowerCase(api.getServiceId()), api);
        }
    }

    @Override
    public boolean registerAPI(String serviceId, API api) {
        if (Strings.isEmpty(serviceId) || null == api) {
            return false;
        }
        return null == registry.putIfAbsent(Strings.asciiLowerCase(serviceId), api);
    }

    @Override
    public API resolveFromServiceId(String serviceId) {
        return null == serviceId ? null : registry.get(Strings.asciiLowerCase(serviceId));
    }

    @Override
    public Collection<API> getAllAPIs() {
        return Collections.unmodifiableCollection(registry.values());
    }

}
