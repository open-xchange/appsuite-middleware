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

package com.openexchange.multifactor.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorAuthenticatorFactory;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.MultifactorManagementService;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.MultifactorProviderRegistry;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;

/**
 * {@link MultifactorManagementServiceImpl}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorManagementServiceImpl implements MultifactorManagementService {

    private final MultifactorProviderRegistry registry;
    private final MultifactorAuthenticatorFactory authenticatorFactory;

    /**
     * Initializes a new {@link MultifactorManagementServiceImpl}.
     *
     * @param registry The {@link MultifactorProviderRegistry}
     * @param authenticatorFactory The {@link MultifactorAuthenticatorFactory}
     */
    public MultifactorManagementServiceImpl(MultifactorProviderRegistry registry, MultifactorAuthenticatorFactory authenticatorFactory) {
        this.registry = Objects.requireNonNull(registry, "registry must not be null");
        this.authenticatorFactory = Objects.requireNonNull(authenticatorFactory, "authenticatorFactory must not be null");
    }

    /**
     * Internal method to create a {@link MultifactorRequest} object for a given user
     *
     * @param contextId The context-id of the user
     * @param userId The ID of the user
     * @return The request object for the given user with a default locale
     */
    private MultifactorRequest getMultifactorRequest(int contextId, int userId) {
        final String host = null;
        return new MultifactorRequest(contextId, userId, null, host, Locale.getDefault());
    }

    @Override
    public Collection<MultifactorDevice> getMultifactorDevices(int contextId, int userId) throws OXException {
        final Collection<MultifactorDevice> devices = new ArrayList<MultifactorDevice>();
        final MultifactorRequest request = getMultifactorRequest(contextId, userId);
        final Collection<MultifactorProvider> providers = registry.getProviders(request);
        for (final MultifactorProvider p : providers) {
            devices.addAll(p.getDevices(request));
        }
        return devices;
    }

    @Override
    public void removeDevice(int contextId, int userId, String providerName, String deviceId) throws OXException {
        final MultifactorRequest request = getMultifactorRequest(contextId, userId);
        Optional<MultifactorProvider> provider = registry.getProvider(providerName);
        if (provider.isPresent()) {
            authenticatorFactory.createAuthenticator(provider.get()).deleteRegistration(request, deviceId);
        } else {
            throw MultifactorExceptionCodes.UNKNOWN_PROVIDER.create(providerName);
        }
    }

    @Override
    public void removeAllDevices(int contextId, int userId) throws OXException {
        final MultifactorRequest request = getMultifactorRequest(contextId, userId);
        final Collection<MultifactorProvider> providers = registry.getProviders(request);
        for (final MultifactorProvider provider : providers) {
            authenticatorFactory.createAuthenticator(provider).deleteRegistrations(request);
        }
    }
}
