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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.multifactor.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorAuthenticator;
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
        return new MultifactorRequest(contextId, userId, host, Locale.getDefault());
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
            final MultifactorAuthenticator authenticator = authenticatorFactory.createAuthenticator(provider.get());
            authenticator.deleteRegistration(request, deviceId);
        } else {
            throw MultifactorExceptionCodes.UNKNOWN_PROVIDER.create(providerName);
        }
    }

    @Override
    public void removeAllDevices(int contextId, int userId) throws OXException {
        final MultifactorRequest request = getMultifactorRequest(contextId, userId);
        final Collection<MultifactorProvider> providers = registry.getProviders(request);
        for (final MultifactorProvider provider : providers) {
            final MultifactorAuthenticator authenticator = authenticatorFactory.createAuthenticator(provider);
            authenticator.deleteRegistrations(request);
        }
    }
}
