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

package com.openexchange.jslob.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlobExceptionCodes;
import com.openexchange.jslob.JSlobService;
import com.openexchange.jslob.registry.JSlobServiceRegistry;

/**
 * {@link JSlobServiceRegistryImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JSlobServiceRegistryImpl implements JSlobServiceRegistry {

    private static final JSlobServiceRegistryImpl INSTANCE = new JSlobServiceRegistryImpl();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static JSlobServiceRegistryImpl getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<String, JSlobService> registry;

    /**
     * Initializes a new {@link JSlobServiceRegistryImpl}.
     */
    private JSlobServiceRegistryImpl() {
        super();
        registry = new ConcurrentHashMap<String, JSlobService>(2, 0.9f, 1);
    }

    @Override
    public JSlobService getJSlobService(final String serviceId) throws OXException {
        final JSlobService service = registry.get(serviceId);
        if (null == service) {
            throw JSlobExceptionCodes.NOT_FOUND.create(serviceId);
        }
        return service;
    }

    @Override
    public JSlobService optJSlobService(final String serviceId) throws OXException {
        return registry.get(serviceId);
    }

    @Override
    public Collection<JSlobService> getJSlobServices() throws OXException {
        final List<JSlobService> list = new ArrayList<JSlobService>(8);
        list.addAll(registry.values());
        return list;
    }

    @Override
    public boolean putJSlobService(final JSlobService jslobService) {
        final Set<String> keys = new HashSet<String>();
        if (null != registry.putIfAbsent(jslobService.getIdentifier(), jslobService)) {
            return false;
        }
        /*
         * Add aliases, too
         */
        keys.add(jslobService.getIdentifier());
        final List<String> aliases = jslobService.getAliases();
        if (null != aliases && !aliases.isEmpty()) {
            for (final String alias : aliases) {
                if (null != registry.putIfAbsent(alias, jslobService)) {
                    /*
                     * Clean-up all keys
                     */
                    for (final String key : keys) {
                        registry.remove(key);
                    }
                    return false;
                }
                keys.add(alias);
            }
        }
        return true;
    }

    @Override
    public void removeJSlobService(final JSlobService jslobService) throws OXException {
        registry.remove(jslobService.getIdentifier());
        /*
         * Remove aliases, too
         */
        final List<String> aliases = jslobService.getAliases();
        if (null != aliases && !aliases.isEmpty()) {
            for (final String alias : aliases) {
                registry.remove(alias);
            }
        }
    }

}
