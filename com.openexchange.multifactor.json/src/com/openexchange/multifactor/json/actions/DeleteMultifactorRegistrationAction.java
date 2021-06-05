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

package com.openexchange.multifactor.json.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorAuthenticator;
import com.openexchange.multifactor.MultifactorAuthenticatorFactory;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.multifactor.MultifactorProvider;
import com.openexchange.multifactor.MultifactorProviderRegistry;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DelteMultifactorRegistration} - Deletes an existing {@link MultifactorDevice}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.2
 */
public class DeleteMultifactorRegistrationAction extends AbstractMultifactorAction {

    /**
     * Initializes a new {@link DelteMultifactorRegistrationAction}.
     *
     * @param serviceLookup The {@link ServiceLookup}
     * @throws OXException
     */
    public DeleteMultifactorRegistrationAction(ServiceLookup serviceLookup) throws OXException {
        super(serviceLookup);
    }

    /**
     * Simple factory method to create a {@link MultifactorAuthenticator}
     *
     * @param provider The provider to create the authenticator for
     * @return The new authenticator
     * @throws OXException
     */
    private MultifactorAuthenticator createAuthenticator(MultifactorProvider provider) throws OXException {
        return serviceLookup.getServiceSafe(MultifactorAuthenticatorFactory.class).createAuthenticator(provider);
    }

    /**
     * Gets all devices for the given providers
     *
     * @param multifactorRequest The request
     * @param providers A collection of providers to get the devices for
     * @return A collection of devices for all given providers
     * @throws OXException
     */
    private Collection<MultifactorDevice> getAllDevices(MultifactorRequest multifactorRequest, Collection<MultifactorProvider> providers) throws OXException{
        ArrayList<MultifactorDevice> devices = new ArrayList<>();
        for (MultifactorProvider provider : providers) {
            devices.addAll(provider.getDevices(multifactorRequest));
        }
        return devices;
    }

    /**
     * Deletes all backup devices
     *
     * @param multifactorRequest The {@link MultifactorRequest}
     * @param providers A collection of providers to delete backup devices from
     * @param devices A list of devices to consider
     * @throws OXException
     */
    private List<String> deleteBackups(MultifactorRequest multifactorRequest, Collection<MultifactorProvider> providers, Collection<MultifactorDevice> devices) throws OXException {
        ArrayList<MultifactorDevice> deletedBackupDevices = new ArrayList<MultifactorDevice>();
        for (final MultifactorProvider provider : providers) {
            final MultifactorAuthenticator authenticator = createAuthenticator(provider);
            final Collection<MultifactorDevice> devicesForProvider = devices.stream().filter(d -> d.getProviderName().equals(provider.getName())).collect(Collectors.toList());
            for (final MultifactorDevice device : devicesForProvider) {
                if (device.isBackup()) {
                    authenticator.deleteRegistration(multifactorRequest, device.getId());
                    deletedBackupDevices.add(device);
                }
            }
        }
        return deletedBackupDevices.stream().map(MultifactorDevice::getId).collect(Collectors.toList());
    }

    /**
     * Checks if there are non-backup devices in the list of given devices
     *
     * @param devices The list of devices to check
     * @return <code>true</code> if there is at least one non-backup device in the list of devices, <code>false</code> otherwise
     * @throws OXException
     */
    private boolean containsOnlyBackupDevices(Collection<MultifactorDevice> devices) {
        return false == devices.stream().anyMatch((device) -> !device.isBackup());
    }

    @Override
    protected AJAXRequestResult doPerform(AJAXMultifactorRequest request) throws OXException {
        final MultifactorProvider provider = requireProvider(request.getProviderName());

        final MultifactorAuthenticator authenticator = createAuthenticator(provider);
        final MultifactorRequest requestData = request.getMultifactorRequest();
        String deviceId = request.getDeviceID();
        authenticator.deleteRegistration(requestData, deviceId);
        // Cleanup backup devices
        final MultifactorProviderRegistry service = serviceLookup.getServiceSafe(MultifactorProviderRegistry.class);
        final Collection<MultifactorProvider> providersFor = service.getProviders(requestData);
        List<String> allDeletedDevices = null;
        Collection<MultifactorDevice> devices = getAllDevices(requestData, providersFor);
        if (containsOnlyBackupDevices(devices)) {
            allDeletedDevices = deleteBackups(requestData, providersFor, devices);
            allDeletedDevices.add(deviceId);
        } else {
            allDeletedDevices = Collections.singletonList(deviceId);
        }
        final JSONArray ret = new JSONArray();
        allDeletedDevices.forEach(d -> ret.put(d) );
        return new AJAXRequestResult(ret);
    }
}
