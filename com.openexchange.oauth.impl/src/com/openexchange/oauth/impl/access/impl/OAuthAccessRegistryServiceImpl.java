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

package com.openexchange.oauth.impl.access.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.access.OAuthAccessRegistry;
import com.openexchange.oauth.access.OAuthAccessRegistryService;

/**
 * {@link OAuthAccessRegistryServiceImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OAuthAccessRegistryServiceImpl implements OAuthAccessRegistryService {

    private final AtomicReference<ConcurrentMap<String, OAuthAccessRegistryImpl>> mapRef;

    /**
     * Initializes a new {@link OAuthAccessRegistryServiceImpl}.
     */
    public OAuthAccessRegistryServiceImpl() {
        super();
        mapRef = new AtomicReference<ConcurrentMap<String, OAuthAccessRegistryImpl>>(new ConcurrentHashMap<String, OAuthAccessRegistryImpl>());
    }

    @Override
    public OAuthAccessRegistry get(String serviceId) throws OXException {
        ConcurrentMap<String, OAuthAccessRegistryImpl> map = mapRef.get();
        if (null == map) {
            throw new OXException(new IllegalStateException("Shut-down initiated"));
        }

        OAuthAccessRegistry registry = map.get(serviceId);
        if (registry == null) {
            OAuthAccessRegistryImpl newRegistry = new OAuthAccessRegistryImpl(serviceId);
            registry = map.putIfAbsent(serviceId, newRegistry);
            if (null == registry) {
                registry = newRegistry;
            }
        }
        return registry;
    }

    /**
     * Notifies this registry that specified user went inactive.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void userInactive(int userId, int contextId) {
        ConcurrentMap<String, OAuthAccessRegistryImpl> map = mapRef.get();
        if (null == map) {
            return;
        }

        for (OAuthAccessRegistryImpl registry : map.values()) {
            registry.removeIfLast(contextId, userId);
        }
    }

    /**
     * Clears this registry
     */
    public void clear() {
        ConcurrentMap<String, OAuthAccessRegistryImpl> map = mapRef.getAndSet(null);
        if (null != map) {
            for (OAuthAccessRegistryImpl registry : map.values()) {
                registry.disposeAll();
            }
            map.clear();
        }
    }
}
