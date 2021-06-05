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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.storage.JSlobStorage;
import com.openexchange.jslob.storage.registry.JSlobStorageRegistry;

/**
 * {@link JSlobStorageRegistryImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSlobStorageRegistryImpl implements JSlobStorageRegistry {

    private static final JSlobStorageRegistryImpl INSTANCE = new JSlobStorageRegistryImpl();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static JSlobStorageRegistryImpl getInstance() {
        return INSTANCE;
    }

    private final ConcurrentMap<String, JSlobStorage> registry;

    /**
     * Initializes a new {@link JSlobStorageRegistryImpl}.
     */
    private JSlobStorageRegistryImpl() {
        super();
        registry = new ConcurrentHashMap<String, JSlobStorage>(2, 0.9f, 1);
    }

    @Override
    public JSlobStorage getJSlobStorage(final String storageId) throws OXException {
        return registry.get(storageId);
    }

    @Override
    public Collection<JSlobStorage> getJSlobStorages() throws OXException {
        final List<JSlobStorage> list = new ArrayList<JSlobStorage>(8);
        list.addAll(registry.values());
        return list;
    }

    @Override
    public boolean putJSlobStorage(final JSlobStorage jslobStorage) {
        return (null == registry.putIfAbsent(jslobStorage.getIdentifier(), jslobStorage));
    }

    @Override
    public void removeJSlobStorage(final String storageId) throws OXException {
        registry.remove(storageId);
    }

}
