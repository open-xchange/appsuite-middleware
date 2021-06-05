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

package com.openexchange.multiple.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.multiple.MultipleHandlerFactoryService;

/**
 * {@link MultipleHandlerRegistryImpl} - Implementation backed by a {@link ConcurrentMap concurrent map}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MultipleHandlerRegistryImpl implements com.openexchange.multiple.internal.MultipleHandlerRegistry {

    /*
     * Member section
     */

    private final ConcurrentMap<String, MultipleHandlerFactoryService> registry;

    /**
     * Initializes a new {@link MultipleHandlerRegistryImpl}.
     */
    MultipleHandlerRegistryImpl() {
        super();
        registry = new ConcurrentHashMap<String, MultipleHandlerFactoryService>();
    }

    @Override
    public boolean addFactoryService(final MultipleHandlerFactoryService factoryService) {
        return (null == registry.putIfAbsent(factoryService.getSupportedModule(), factoryService));
    }

    @Override
    public MultipleHandlerFactoryService getFactoryService(final String module) {
        MultipleHandlerFactoryService candidate = registry.get(module);
        if (candidate == null) {
            // Maybe prefixed
            for (Map.Entry<String, MultipleHandlerFactoryService> entry : registry.entrySet()) {
                if (module.startsWith(entry.getKey())) {
                    return entry.getValue();
                }
            }
        }
        return candidate;
    }

    @Override
    public void removeFactoryService(final String module) {
        registry.remove(module);
    }

}
