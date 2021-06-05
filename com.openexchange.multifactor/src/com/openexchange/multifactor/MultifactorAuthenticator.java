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

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.exceptions.MultifactorExceptionCodes;
import com.openexchange.multifactor.listener.MultifactorListenerChain;

/**
 * {@link MultifactorAuthenticator} - A helper/convenience class around {@link MultifactorProvider}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */

public class MultifactorAuthenticator {

    private final MultifactorProvider multifactorProvider;
    private final MultifactorLockoutService lockoutService;
    private final MultifactorListenerChain listenerChain;
    private final MultifactorProviderRegistry providerRegistry;

    /**
     * Initializes a new {@link MultifactorAuthenticator}.
     *
     * @param multifactorProvider The {@link MultifactorProvider} instance to use for performing authentication
     * @param lockoutService The {@link MultifactorLockoutService} instance to handle bad attempts
     * @param listenerChain The {@link MultifactorListenerChain} to use
     * @param providerRegistry The {@link MultifactorProviderRegistry}
     * @throws OXException
     */
    public MultifactorAuthenticator(MultifactorProvider multifactorProvider, MultifactorLockoutService lockoutService, MultifactorListenerChain listenerChain, MultifactorProviderRegistry providerRegistry) {
        this.multifactorProvider = Objects.requireNonNull(multifactorProvider, "multifactorProvider must not be null");
        this.lockoutService = Objects.requireNonNull(lockoutService, "lockoutService must not be null");
        this.listenerChain = Objects.requireNonNull(listenerChain, "Multifactor Listener Chain must not be null");
        this.providerRegistry = Objects.requireNonNull(providerRegistry, "providerRegistry must not be null");
    }

    /**
     * Gets the device to use
     *
     * @param devices The list of devices
     * @param deviceId The ID of the device to get
     * @return The device with the specified ID
     */
    private Optional<? extends MultifactorDevice> getDevice(Collection<? extends MultifactorDevice> devices, String deviceId){
        return devices.stream().filter(d -> d.getId().equals(deviceId)).findFirst();
    }

    /**
     * Internal method to return the amount of enabled multifactor devices for a given user
     *
     * @param request The user's request
     * @return the amount of enabled multifactor devices
     * @throws OXException
     */
    private int getEnabledDeviceCount(MultifactorRequest request) throws OXException {
        int count = 0;
        Collection<MultifactorProvider> providers = providerRegistry.getProviders(request);
        for (MultifactorProvider provider : providers) {
            count += provider.getEnabledDevices(request).size();
        }
        return count;
    }

    /**
     * Called when authentication was successful. Can be overrided.
     *
     * @throws OXException
     */
    @SuppressWarnings("unused")
    protected void authenticationPerformed() throws OXException {
        // Nothing to do here
    }

    /**
     * Convenience method which performs the authentication against the provider if the provider
     * is enabled for the given session and the authentication is required for the given session.
     *
     * @param multifactorRequest The request to authenticate
     * @param deviceId The id of the device
     * @param answer The answer to a previously send {@link Challenge}
     * @throws OXException If the authentication fails
     */
    public void requireAuthentication(MultifactorRequest multifactorRequest, String deviceId, ChallengeAnswer answer) throws OXException {
        if (multifactorProvider.isEnabled(multifactorRequest)) {
            lockoutService.checkLockedOut(multifactorRequest);
            final Collection<? extends MultifactorDevice> devices = multifactorProvider.getEnabledDevices(multifactorRequest);
            if (devices != null && !devices.isEmpty()) {

                //Getting the specified device, or the default device if present
                final Optional<? extends MultifactorDevice> authDevice = getDevice(devices, deviceId);
                if (authDevice.isPresent()) {
                    // Perform the actual authentication
                    doAuthentication(multifactorRequest, deviceId, answer);
                } else {
                    throw MultifactorExceptionCodes.UNKNOWN_DEVICE_ID.create();
                }
            }
        }
    }

    /**
     * Starts a new registration for the {@link MultifactorProvider}.
     *
     * @param multifactorRequest The request of the user who wants to register for a multi-factor authentication provider.
     * @param device A new device containing all relevant information to start a new registration
     * @return The {@link RegistrationChallenge}
     * @throws OXException
     */
    public RegistrationChallenge startRegistration(MultifactorRequest multifactorRequest, MultifactorDevice device) throws OXException {
        if (multifactorProvider.isEnabled(multifactorRequest)) {
            return multifactorProvider.startRegistration(multifactorRequest, device);
        }
        throw MultifactorExceptionCodes.PROVIDER_NOT_AVAILABLE.create(multifactorProvider.getName());
    }

    /**
     * Finishes a new registration for the {@link MultifactorProvider}.
     *
     * @param multifactorRequest The request of the user who wants to register for a multi-factor authentication provider.
     * @param deviceId The id of the device
     * @param answer The answer to the previous send {@link Challenge}
     * @return The registered {@link MultifactorDevice}
     * @throws OXException
     */
    public MultifactorDevice finishRegistration(MultifactorRequest multifactorRequest, String deviceId, ChallengeAnswer answer) throws OXException {
        if (multifactorProvider.isEnabled(multifactorRequest)) {
            MultifactorDevice result = multifactorProvider.finishRegistration(multifactorRequest, deviceId, answer);
            listenerChain.onAfterAdd(multifactorRequest.getUserId(), multifactorRequest.getContextId(), getEnabledDeviceCount(multifactorRequest));
            return result;
        }
        throw MultifactorExceptionCodes.PROVIDER_NOT_AVAILABLE.create(multifactorProvider.getName());
    }

    /**
     * Removes the registration for the {@link MultifactorProvider}
     *
     * @param multifactorRequest The request of the user who wants to remove the registration for the multi-factor provider.
     * @param deviceId The id of the device to unregister
     * @throws OXException
     */
    public void deleteRegistration(MultifactorRequest multifactorRequest, String deviceId) throws OXException {
        multifactorProvider.deleteRegistration(multifactorRequest, deviceId);
        listenerChain.onAfterDelete(multifactorRequest.getUserId(), multifactorRequest.getContextId(), getEnabledDeviceCount(multifactorRequest));
    }

    /**
     * Removes all registrations for the {@link MultifactorProvider}
     *
     * @param multifactorRequest The request of the user who wants to remove the registration for the multi-factor provider.
     * @throws OXException
     */
    public void deleteRegistrations(MultifactorRequest multifactorRequest) throws OXException {
        if (multifactorProvider.deleteRegistrations(multifactorRequest.getContextId(), multifactorRequest.getUserId())) {
            listenerChain.onAfterDelete(multifactorRequest.getUserId(), multifactorRequest.getContextId(), getEnabledDeviceCount(multifactorRequest));
        }
    }

    /**
     * Starts the authentication process against a given {@link MultifactorDevice}
     *
     * @param multifactorRequest The request
     * @param deviceId The id of the device to authenticate against
     * @return The {@link Challenge}
     * @throws OXException
     */
    public Challenge beginAuthorization(MultifactorRequest multifactorRequest, String deviceId) throws OXException {
        lockoutService.checkLockedOut(multifactorRequest);
        if (!multifactorProvider.isEnabled(multifactorRequest)) {
            throw MultifactorExceptionCodes.PROVIDER_NOT_AVAILABLE.create(multifactorProvider.getName());
        }

        return multifactorProvider.beginAuthentication(multifactorRequest, deviceId);
    }

    /**
     * Performs an authentication process against a given {@link MultifactorDevice}
     *
     * @param multifactorRequest The request
     * @param deviceId The id of the device to authenticate against
     * @param answer The answer to a previously send authentication {@link Challenge}
     * @throws OXException
     */
    public void doAuthentication(MultifactorRequest multifactorRequest, String deviceId, ChallengeAnswer answer) throws OXException {
        lockoutService.checkLockedOut(multifactorRequest);
        if (multifactorProvider.isEnabled(multifactorRequest)) {
            try {
                multifactorProvider.doAuthentication(multifactorRequest, deviceId, answer);
                listenerChain.onAfterAuthentication(multifactorRequest.getUserId(), multifactorRequest.getContextId(), true);
                authenticationPerformed();
                lockoutService.registerSuccessfullLogin(multifactorRequest.getUserId(), multifactorRequest.getContextId());
                return;
            } catch (OXException e) {
                lockoutService.registerFailedAttempt(multifactorRequest.getUserId(), multifactorRequest.getContextId());
                listenerChain.onAfterAuthentication(multifactorRequest.getUserId(), multifactorRequest.getContextId(), false);
                if (MultifactorExceptionCodes.AUTHENTICATION_FAILED.equals(e)) {
                    int maxCount = lockoutService.getMaxBadAttempts(multifactorRequest.getUserId(), multifactorRequest.getContextId());
                    if (maxCount > 0) {
                        throw MultifactorExceptionCodes.AUTHENTICATION_FAILED_WITH_LOCKOUT.create(I(maxCount));
                    }
                    throw e;
                }
                throw MultifactorExceptionCodes.AUTHENTICATION_FAILED_EXT.create(e.getMessage(), e);
            }
        }
        throw MultifactorExceptionCodes.PROVIDER_NOT_AVAILABLE.create(multifactorProvider.getName());
    }

}
