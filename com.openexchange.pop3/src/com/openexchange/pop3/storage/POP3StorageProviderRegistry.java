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

package com.openexchange.pop3.storage;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link POP3StorageProviderRegistry} - The registry for {@link POP3StorageProvider POP3 storage providers}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3StorageProviderRegistry {

    private static final POP3StorageProviderRegistry instance = new POP3StorageProviderRegistry();

    /**
     * Gets the POP3 storage provider registry.
     *
     * @return The POP3 storage provider registry
     */
    public static POP3StorageProviderRegistry getInstance() {
        return instance;
    }

    /*-
     * Member section
     */

    private final ConcurrentMap<String, POP3StorageProvider> registryMap;

    /**
     * Initializes a new {@link POP3StorageProviderRegistry}.
     */
    private POP3StorageProviderRegistry() {
        super();
        registryMap = new ConcurrentHashMap<String, POP3StorageProvider>();
    }

    /**
     * Gets the provider from this registry which is bound to specified provider name.
     *
     * @param providerName The provider name
     * @return The provider bound to specified provider name or <code>null</code> if none found
     */
    public POP3StorageProvider getPOP3StorageProvider(final String providerName) {
        return registryMap.get(providerName);
    }

    /**
     * Adds given provider to this registry bound to name obtained by {@link POP3StorageProvider#getPOP3StorageName()}.
     *
     * @param provider The provider
     * @return <code>true</code> if provider could be successfully added; otherwise <code>false</code>
     */
    public boolean addPOP3StorageProvider(final POP3StorageProvider provider) {
        return addPOP3StorageProvider(provider.getPOP3StorageName(), provider);
    }

    /**
     * Adds given provider to this registry bound to specified provider name.
     *
     * @param providerName The provider name
     * @param provider The provider
     * @return <code>true</code> if provider could be successfully added; otherwise <code>false</code>
     */
    public boolean addPOP3StorageProvider(final String providerName, final POP3StorageProvider provider) {
        return (null == registryMap.putIfAbsent(providerName, provider));
    }

    /**
     * Removes the provider from this registry which is bound to specified provider name.
     *
     * @param providerName The provider name
     * @return The removed provider or <code>null</code> if none removed
     */
    public POP3StorageProvider removePOP3StorageProvider(final String providerName) {
        return registryMap.remove(providerName);
    }

    /**
     * Clears this registry.
     */
    public void clear() {
        registryMap.clear();
    }
}
