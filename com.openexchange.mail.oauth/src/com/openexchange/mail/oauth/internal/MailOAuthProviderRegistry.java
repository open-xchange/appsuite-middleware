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

package com.openexchange.mail.oauth.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.mail.oauth.MailOAuthProvider;

/**
 * {@link MailOAuthProviderRegistry}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class MailOAuthProviderRegistry {

    private final ConcurrentMap<String, MailOAuthProvider> providers;

    /**
     * Initializes a new {@link MailOAuthProviderRegistry}.
     */
    public MailOAuthProviderRegistry() {
        super();
        providers = new ConcurrentHashMap<>(8, 9.0F, 1);
    }

    /**
     * Gets the providers currently held in this registry.
     *
     * @return The available providers
     */
    public Collection<MailOAuthProvider> getProviders() {
        return Collections.unmodifiableCollection(providers.values());
    }

    /**
     * Gets the provider for given identifier.
     *
     * @param provderId The provider identifier
     * @return The associated provider instance or <code>null</code>
     */
    public MailOAuthProvider getProviderFor(String provderId) {
        return providers.get(provderId);
    }

    /**
     * Adds given provider to this registry.
     *
     * @param provider The provider to add
     * @return <code>true</code> if provider was successfully added; otherwise <code>false</code>
     */
    public boolean addProvider(MailOAuthProvider provider) {
        return null == provider ? false : null == providers.putIfAbsent(provider.getProviderId(), provider);
    }

    /**
     * Removes given provider from this registry.
     *
     * @param provider The provider to remove
     * @return <code>true</code> if provider was successfully removed; otherwise <code>false</code>
     */
    public boolean removeProvider(MailOAuthProvider provider) {
        return null == provider ? false : null != providers.remove(provider.getProviderId());
    }

}
