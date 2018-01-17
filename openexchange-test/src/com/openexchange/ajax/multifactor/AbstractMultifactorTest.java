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

package com.openexchange.ajax.multifactor;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import java.util.List;
import java.util.Optional;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.models.MultifactorDeleteResponse;
import com.openexchange.testing.httpclient.models.MultifactorDevice;
import com.openexchange.testing.httpclient.models.MultifactorDeviceParameters;
import com.openexchange.testing.httpclient.models.MultifactorDeviceResponse;
import com.openexchange.testing.httpclient.models.MultifactorDevicesResponse;
import com.openexchange.testing.httpclient.models.MultifactorFinishAuthenticationData;
import com.openexchange.testing.httpclient.models.MultifactorFinishRegistrationData;
import com.openexchange.testing.httpclient.models.MultifactorFinishRegistrationResponse;
import com.openexchange.testing.httpclient.models.MultifactorProvider;
import com.openexchange.testing.httpclient.models.MultifactorProvidersResponse;
import com.openexchange.testing.httpclient.models.MultifactorStartAuthenticationResponse;
import com.openexchange.testing.httpclient.models.MultifactorStartAuthenticationResponseData;
import com.openexchange.testing.httpclient.models.MultifactorStartRegistrationResponse;
import com.openexchange.testing.httpclient.models.MultifactorStartRegistrationResponseData;
import com.openexchange.testing.httpclient.modules.MultifactorApi;

/**
 * {@link AbstractMultifactorTest}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.1
 */
public class AbstractMultifactorTest extends AbstractConfigAwareAPIClientSession {

    private MultifactorApi multifactorApi;
    private static String EMPTY_PROVIDER_FILTER = "";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        multifactorApi = new MultifactorApi(getApiClient());
        super.setUpConfiguration();
    }

    protected MultifactorApi MultifactorApi() {
        return multifactorApi;
    }

    protected <T> T checkResponse(MultifactorProvidersResponse response, T data) {
        return super.checkResponse(response.getError(), response.getErrorDesc(), data);
    }

    protected <T> T checkResponse(MultifactorDevicesResponse response, T data) {
        return super.checkResponse(response.getError(), response.getErrorDesc(), data);
    }

    protected <T> T checkResponse(MultifactorDeviceResponse response, T data) {
       return super.checkResponse(response.getError(), response.getErrorDesc(), data);
    }

    protected <T> T checkResponse(MultifactorStartRegistrationResponse response, T data) {
        return super.checkResponse(response.getError(), response.getErrorDesc(), data);
    }

    protected <T> T checkResponse(MultifactorFinishRegistrationResponse response, T data) {
       return super.checkResponse(response.getError(), response.getErrorDesc(), data) ;
    }

    protected <T> T checkResponse(MultifactorStartAuthenticationResponse response, T data) {
        return super.checkResponse(response.getError(), response.getErrorDesc(), data);
    }

    protected <T> T checkResponse(MultifactorDeleteResponse response, T data) {
        return super.checkResponse(response.getError(), response.getErrorDesc(), data);
    }

    /**
     * Gets a list of providers available for the current session.
     *
     * @return A list of available providers
     * @throws ApiException
     */
    protected List<MultifactorProvider> getProviders(String filter) throws ApiException {
        MultifactorProvidersResponse resp = this.multifactorApi.multifactorProviderActionAll(getSessionId(), filter);
        return checkResponse(resp, resp.getData());
    }

    /**
     * Gets a list of providers available for the current session.
     *
     * @return A list of available providers
     * @throws ApiException
     */
    protected List<MultifactorProvider> getProviders() throws ApiException {
        return getProviders(EMPTY_PROVIDER_FILTER);
    }

    /**
     * Gets a provider by name
     *
     * @param name The name of the provider
     * @return The provider with the given name, or an empty optional
     * @throws ApiException
     */
    protected Optional<MultifactorProvider> getProvider(String name) throws ApiException {
        return getProviders().stream().filter(p -> p.getName().contentEquals(name)).findFirst();
    }

    /**
     * Gets a provider by name and asserts that the provider exists
     *
     * @param name The name of the provider
     * @return The provider with the given name
     * @throws ApiException
     */
    protected MultifactorProvider requireProvider(String name) throws ApiException {
        Optional<MultifactorProvider> provider = getProvider(name);
        assertThat(provider.isPresent(), is(true));
        return provider.get();
    }

    /**
     * Gets a list of available device for the ensures that the list contains at least one device
     *
     * @return A list of devices for the given provider
     * @throws ApiException
     */
    protected List<MultifactorDevice> requireDevices() throws ApiException{
        List<MultifactorDevice> devices = getDevices();
        assertThat(devices, is(not(empty())));
        return devices;
    }

    /**
     * Gets a list of availbale device for the given provider
     *
     * @return A list of devices for the given provider
     * @throws ApiException
     */
    protected List<MultifactorDevice> getDevices() throws ApiException{
        MultifactorDevicesResponse resp = multifactorApi.multifactorDeviceActionAll(getSessionId());
        return checkResponse(resp, resp.getData());
    }

    /**
     * Gets a device by ID
     *
     * @param deviceId The ID of the device to get
     * @return The device or an empty Optional
     * @throws ApiException
     */
    protected Optional<MultifactorDevice> getDevice(String deviceId) throws ApiException {
        List<MultifactorDevice> devices = getDevices();
        return devices.stream().filter(d -> deviceId.equals(d.getId())).findFirst();
    }

    /**
     * Gets the device with the given ID and ensures that the device is present
     *
     * @param deviceId The ID of the device to get
     * @return The device
     * @throws ApiException
     */
    protected MultifactorDevice requireDevice(String deviceId) throws ApiException {
        Optional<MultifactorDevice> device = getDevice(deviceId);
        return device.orElseThrow(() -> new AssertionError("The device with the given ID \"" + deviceId + "\" must be present."));
    }

    /**
     * Starts registering a new multifactor device
     *
     * @param providerName The name of the provider.
     * @return The response data
     * @throws ApiException
     */
    protected MultifactorStartRegistrationResponseData startRegistration(String providerName) throws ApiException {
        final String phoneNumber = null;
        final String deviceName = null;
        final boolean backupDevice = false;
        return startRegistration(providerName, deviceName, phoneNumber,backupDevice);
    }

    /**
     * Starts registering a new multifactor device for the given provider
     *
     * @param providerName The name of the provider
     * @param deviceName The name of the new device, or null to choose a default name
     * @param phoneNumber [SMS] The phone number for the SMS provider or null for other providers
     * @param backup [SMS] Whether the device should be used as backup device or not
     * @return The response data
     * @throws ApiException
     */
    protected MultifactorStartRegistrationResponseData startRegistration(String providerName,
        String deviceName,
        String phoneNumber,
        boolean backup) throws ApiException {

        final MultifactorDevice deviceData = new MultifactorDevice();
        deviceData.setProviderName(providerName);
        deviceData.setName(deviceName);
        deviceData.backup(backup);

        if(phoneNumber != null) {
            MultifactorDeviceParameters paramters = new MultifactorDeviceParameters();
            paramters.setPhoneNumber(phoneNumber);
            deviceData.setParameters(paramters);
        }

        MultifactorStartRegistrationResponse resp = MultifactorApi().multifactorDeviceActionStartRegistration(getSessionId(), deviceData);
        return checkResponse(resp, resp.getData());
    }

    /**
     * Finishes the registration of new mulitfactor device for the given provider
     *
     * @param providerName The name of the provider
     * @param deviceId The ID of the device to finish registration for
     * @param secretToken [TOTP, SMS] The secret authentication token
     * @param clientData [U2F] clientData
     * @param registrationData [U2F] registrationData
     * @return The response data
     * @throws ApiException
     */
    protected MultifactorDevice  finishRegistration(String providerName, String deviceId, String secretToken, String clientData, String registrationData) throws ApiException {
        MultifactorFinishRegistrationData  data = new MultifactorFinishRegistrationData();
        data.setSecretCode(secretToken);
        data.setClientData(clientData);;
        data.setRegistrationData(registrationData);

        MultifactorFinishRegistrationResponse resp = MultifactorApi().multifactorDeviceActionfinishRegistration(getSessionId(), providerName, deviceId, data);
        return checkResponse(resp, resp.getData());
    }

    /**
     * Unregisters a device and asserts that it was removed
     *
     * @param providerName The name of the provider to unregister the device for
     * @param deviceId The ID of the device to delete
     * @return A list of device ID which were deleted
     * @throws ApiException
     */
    protected List<String> unregisterDevice(String providerName, String deviceId) throws ApiException {
        MultifactorDeleteResponse resp = MultifactorApi().multifactorDeviceActionDelete(getSessionId(), providerName, deviceId);
        List<String> deletedDeviceIds = checkResponse(resp.getError(), resp.getErrorDesc(), resp.getData());

        //ensure it's gone
        assertThat(deletedDeviceIds.isEmpty(), is(false));
        Optional<MultifactorDevice> device = getDevice(deviceId);
        assertThat(device, is(Optional.empty()));

        return deletedDeviceIds;
    }

    /**
     * Start the authentication for a given device
     *
     * @param providerName The name of the provider
     * @param deviceId The ID of the device to start authentication for
     * @return The response data containing the challenge
     * @throws ApiException
     */
    protected MultifactorStartAuthenticationResponseData startAuthentication(String providerName, String deviceId) throws ApiException {
        MultifactorStartAuthenticationResponse resp = MultifactorApi().multifactorDeviceActionStartAuthentication(getSessionId(), providerName, deviceId);
        return checkResponse(resp, resp.getData());
    }

    /**
     * Performs the authentication for a given device
     *
     * @param providerName The name of the provider
     * @param deviceId The ID of the device to use for authentication
     * @param secretCode [SMS | TOTP | BACKUP_STRING] The secret code for authentication
     * @param clientData [U2F] The client-data for authentication
     * @param signatureData [U2F] The signature data for authentication
     * @param keyHandle [U2F] The key handle data for authentication
     * @return The response
     * @throws ApiException
     */
    protected CommonResponse finishAuthentication(String providerName, String deviceId, String secretCode, String clientData, String signatureData, String keyHandle) throws ApiException {
        MultifactorFinishAuthenticationData data = new MultifactorFinishAuthenticationData();
        data.setSecretCode(secretCode);
        data.setClientData(clientData);
        data.setKeyHandle(keyHandle);
        data.setSignatureData(signatureData);
        return MultifactorApi().multifactorDeviceActionfinishAuthentication(getSessionId(), providerName, deviceId, data);
    }
}