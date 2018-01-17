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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.multifactor.rmi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.BasicAuthenticator;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.MultifactorProviderRegistry;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link MultifactorManagementRemoteServiceImpl}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MultifactorManagementRemoteServiceImpl implements MultifactorManagementRemoteService {

    private MultifactorProviderRegistry multifactorProviderRegistry;
    private final BasicAuthenticator basicAuth;

    /**
     * Initializes a new {@link MultifactorManagementRemoteServiceImpl}.
     *
     * @throws StorageException
     */
    public MultifactorManagementRemoteServiceImpl() throws StorageException {
        basicAuth = BasicAuthenticator.createPluginAwareAuthenticator();
    }

    /**
     * Sets the {@link MultifactorProviderRegistry} to operate on
     *
     * @param registry The {@link MultifactorProviderRegistry} to operate on
     * @return this
     */
    public MultifactorManagementRemoteServiceImpl setRegistry(MultifactorProviderRegistry registry) {
        this.multifactorProviderRegistry = registry;
        return this;
    }

    private MultifactorProviderRegistry requireRegistry() throws OXException {
        if (this.multifactorProviderRegistry == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(MultifactorProviderRegistry.class.getName());
        }
        return this.multifactorProviderRegistry;
    }

    private void requireAuthentication(Credentials credentials) throws InvalidCredentialsException {
        basicAuth.doAuthentication(credentials);
    }

    private MultifactorRequest getMultifactorRequest(int contextId, int userId) {
        final String host = null;
        return new MultifactorRequest(contextId, userId, host, Locale.getDefault());
    }

    private Collection<MultifactorDeviceResult> toDevices(MultifactorProvider provider, Collection<? extends MultifactorDevice> multifactorDevices) {
        return multifactorDevices.stream().map(md -> toDeviceResult(provider, md)).collect(Collectors.toList());
    }

    private MultifactorDeviceResult toDeviceResult(MultifactorProvider provider, MultifactorDevice multifactorDevice) {
        return new MultifactorDeviceResultImpl()
            .setId(multifactorDevice.getId())
            .setName(multifactorDevice.getName())
            .setProviderName(provider.getName())
            .setEnabled(multifactorDevice.isEnabled())
            .setBackupDevice(multifactorDevice.isBackup());
    }

    @Override
    public MultifactorDeviceResult[] getMultifactorDevices(int contextId, int userId, Credentials credentials) throws Exception {

        requireAuthentication(credentials);

        final MultifactorRequest request = getMultifactorRequest(contextId, userId);
        final Collection<MultifactorProvider> providers = requireRegistry().getProviders(request );
        final Collection<MultifactorDeviceResult> devices = new ArrayList<MultifactorDeviceResult>();
        for (final MultifactorProvider p : providers) {
            devices.addAll(toDevices(p, p.getDevices(request)));
        }
        return devices.toArray(new MultifactorDeviceResult[devices.size()]);
    }

    @Override
    public void removeDevice(int contextId, int userId, String providerName, String deviceId, Credentials credentials) throws Exception {

        requireAuthentication(credentials);

        final MultifactorRequest request = getMultifactorRequest(contextId, userId);
        Optional<MultifactorProvider> provider = requireRegistry().getProvider(providerName);
        if (provider.isPresent()) {
            provider.get().deleteRegistration(request, deviceId);
        } else {
            throw MultifactorExceptionCodes.UNKNOWN_PROVIDER.create(providerName);
        }
    }

    @Override
    public void removeAllDevices(int contextId, int userId, Credentials credentials) throws Exception {

        requireAuthentication(credentials);

        final MultifactorRequest request = getMultifactorRequest(contextId, userId);
        final Collection<MultifactorProvider> providers = requireRegistry().getProviders(request);
        for (final MultifactorProvider provider : providers) {
            final Collection<? extends MultifactorDevice> devices = provider.getDevices(request);
            for (final MultifactorDevice device : devices) {
                provider.deleteRegistration(request, device.getId());
            }
        }
    }
}
