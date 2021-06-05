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

package com.openexchange.multifactor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link MultifactorAuthenticationProviderRegistryImpl}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorProviderRegistryImpl implements MultifactorProviderRegistry{

    private final ConcurrentHashMap<String /* key: provider-name */ , MultifactorProvider> providers = new ConcurrentHashMap<>();

    @Override
    public void registerProvider(MultifactorProvider provider) {
        providers.put(provider.getName(), provider);
    }

    @Override
    public void unRegisterProvider(MultifactorProvider provider) {
        providers.remove(provider.getName());
    }

    @Override
    public Optional<MultifactorProvider> getProvider(String name) {
        return Optional.ofNullable(providers.get(name));
    }

    @Override
    public Collection<MultifactorProvider> getProviders() {
        return Collections.unmodifiableCollection(providers.values());
    }

    @Override
    public Collection<MultifactorProvider> getProviders(MultifactorRequest multifactorRequest) {
        ArrayList<MultifactorProvider> ret = new ArrayList<MultifactorProvider>();
        Iterator<MultifactorProvider> iterator = getProviders().iterator();
        while (iterator.hasNext()) {
            final MultifactorProvider provider = iterator.next();
            if (provider.isEnabled(multifactorRequest)) {
                ret.add(provider);
            }
        }
        return Collections.unmodifiableCollection(ret);
    }

    @Override
    public Collection<MultifactorProvider> getProviders(MultifactorRequest multifactorRequest, String[] nameFilters) {
        return (nameFilters == null || nameFilters.length == 0) ?
            getProviders(multifactorRequest) :
            getProviders(multifactorRequest).stream().filter(p -> Arrays.contains(nameFilters,p.getName())).collect(Collectors.toList());
    }
}
