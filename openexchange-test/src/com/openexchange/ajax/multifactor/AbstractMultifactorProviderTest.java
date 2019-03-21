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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import java.util.List;
import org.junit.Test;
import com.openexchange.testing.httpclient.invoker.ApiClient;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.models.ConfigResponse;
import com.openexchange.testing.httpclient.models.CurrentUserResponse;
import com.openexchange.testing.httpclient.models.LoginResponse;
import com.openexchange.testing.httpclient.models.MultifactorDeleteResponse;
import com.openexchange.testing.httpclient.models.MultifactorDevice;
import com.openexchange.testing.httpclient.models.MultifactorDeviceResponse;
import com.openexchange.testing.httpclient.models.MultifactorProvider;
import com.openexchange.testing.httpclient.models.MultifactorStartAuthenticationResponseData;
import com.openexchange.testing.httpclient.models.MultifactorStartRegistrationResponseData;
import com.openexchange.testing.httpclient.modules.ConfigApi;
import com.openexchange.testing.httpclient.modules.LoginApi;
import com.openexchange.testing.httpclient.modules.MultifactorApi;
import com.openexchange.testing.httpclient.modules.UserMeApi;

/**
 * {@link AbstractMultifactorProviderTest} is an abstract "Template Method Pattern" class which provides common tests for Multifactor Providers
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.1
 */
public abstract class AbstractMultifactorProviderTest extends AbstractMultifactorTest {

    protected int contextId;
    protected int userId;

    //----------------------------------------------------------------------------------------------
    //Template methods

    /**
     * Gets the provider name
     *
     * @return The provider's name
     */
    protected abstract String getProviderName();

    /**
     * Returns if the provider is meant to be a backup provider
     * @return True, if the provider is meant to be a backup provider, false otherwise
     */
    protected abstract boolean isBackupProvider();

    /**
     * Returns if the provider is meant to be a backup provider
     * @return True, if the provider is meant to be a backup provider, false otherwise
     */
    protected abstract boolean isBackupOnlyProvider();

    /**
     * Start registration of a new mulitfactor device
     *
     * @return The result of starting the registration
     * @throws Exception
     */
    protected abstract MultifactorStartRegistrationResponseData doStartRegistration() throws Exception;

    /**
     *  Validates the start registration result
     *
     * @param startRegistrationData the result to validate
     * @throws Exception
     */
    protected abstract void validateStartRegistrationResponse(MultifactorStartRegistrationResponseData startRegistrationData) throws Exception;

    /**
     * Finishes the registration process of a new device
     *
     * @param startRegistrationData The start registration result
     * @return The result of the registration
     * @throws Exception
     */
    protected abstract MultifactorDevice doFinishRegistration(MultifactorStartRegistrationResponseData startRegistrationData) throws Exception;

    /**
     * Validates the registered device
     *
     * @param finishRegistrationData The data to validate
     * @throws Exception
     */
    protected abstract void validateRegisteredDevice(MultifactorDevice device) throws Exception;


    /**
     * Validates the start authentication result
     *
     * @param startAuthenticationData The result to validate
     * @throws Exception
     */
    protected abstract void validateStartAuthenticationResponse(MultifactorStartAuthenticationResponseData startAuthenticationData) throws Exception;

    /**
     * Performs the actual multifactor authentication
     *
     * @param startRegistrationData The registration data
     * @param startAuthenticationData The start authentication data
     * @return The response
     * @throws Exception
     */
    protected abstract CommonResponse doAuthentication(MultifactorStartRegistrationResponseData startRegistrationData, MultifactorStartAuthenticationResponseData startAuthenticationData) throws Exception;

    /**
     * Performs an multifactor authentication with a wrong factor
     *
     * @param startRegistrationData The registration data
     * @param startAuthenticationData The start authentication data
     * @return The response
     * @throws Exception
     */
    protected abstract CommonResponse doWrongAuthentication(MultifactorStartRegistrationResponseData startRegistrationData, MultifactorStartAuthenticationResponseData startAuthenticationData) throws Exception;

    //----------------------------------------------------------------------------------------------

    private  MultifactorStartAuthenticationResponseData startAuthenticationInternal(String deviceId) throws Exception{
        return super.startAuthentication(getProviderName(), deviceId);
    }

    private MultifactorStartRegistrationResponseData registerNewDevice() throws Exception {

        //Start the registration process of a new multifactor device
        MultifactorStartRegistrationResponseData startRegistrationResult = doStartRegistration();
        assertThat(startRegistrationResult.getChallenge(), is(not(nullValue())));
        assertThat(startRegistrationResult.getDeviceId(), not(isEmptyOrNullString()));

        //Validate the response of the registration
        validateStartRegistrationResponse(startRegistrationResult);


        //Finish the registration of the new multifactor device
        MultifactorDevice device = doFinishRegistration(startRegistrationResult);
        //As a result the new device should have been returned
        assertThat(device, is(not(nullValue())));
        //..with an ID assigned
        assertThat(device.getId(), not(isEmptyOrNullString()));
        //..it must match the device ID returned from start registration
        assertThat(device.getId(), is(equalTo((startRegistrationResult.getDeviceId()))));
        //.. and the provider must match
        assertThat(device.getProviderName(), is(equalTo(getProviderName())));

        //Further provider specific validations
        validateRegisteredDevice(device);

        //Ensure the device is now present
        requireDevice(device.getId());
        return startRegistrationResult;
    }

    protected void clearAllMultifactorDevices() throws Exception {
        getAdminApi().multifactorDeleteDevices(contextId, userId);
    }

    protected void clearAllMultifactorDevicesByUser() throws Exception {
       List<MultifactorDevice> devices = getDevices();
       for(MultifactorDevice device : devices) {
           MultifactorApi().multifactorDeviceActionDelete(getSessionId(), device.getProviderName(), device.getId());
       }
    }

    //----------------------------------------------------------------------------------------------

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ConfigApi configApi = new ConfigApi(apiClient);
        ConfigResponse configResponse = configApi.getConfigNode("/context_id", apiClient.getSession());
        this.contextId = (int)
            super.checkResponse(configResponse.getError(),configResponse.getErrorDesc(), configResponse.getData());
        this.userId = MultifactorApi().getApiClient().getUserId();
    }

    @Override
    public void tearDown() throws Exception {
        try {
            //finally remove all multifactor devices registered during the test run
            clearAllMultifactorDevices();
        }
        finally {
            super.tearDown();
        }
    }

    //----------------------------------------------------------------------------------------------

    @Test
    public void testBackupProviderFlags() throws Exception {
       MultifactorProvider requireProvider = requireProvider(getProviderName());
       assertThat(requireProvider.getBackupProvider(), is(isBackupProvider()));
       assertThat(requireProvider.getBackupOnlyProvider(), is(isBackupOnlyProvider()));
    }

    @Test
    public void testRegisterUnregisterDevice() throws Exception {
        MultifactorStartRegistrationResponseData registrationResponseData = registerNewDevice();
        unregisterDevice(getProviderName(), registrationResponseData.getDeviceId());
    }

    @Test
    public void testsAuthentication() throws Exception {
        //Performing a complete registration of a new device
        MultifactorStartRegistrationResponseData registrationResponseData = registerNewDevice();

        //Begin authentication against the new device
        MultifactorStartAuthenticationResponseData startAuthenticationResultData = startAuthenticationInternal(registrationResponseData.getDeviceId());
        assertThat(startAuthenticationResultData, is(not(nullValue())));
        assertThat(startAuthenticationResultData.getChallenge(), is(not(nullValue())));
        validateStartAuthenticationResponse(startAuthenticationResultData);

        //Perform authentication
        CommonResponse response = doAuthentication(registrationResponseData, startAuthenticationResultData);
        assertThat(response.getErrorDesc(), is(nullValue()));
    }

    @Test
    public void testWrongAuthentication() throws Exception {
        MultifactorStartRegistrationResponseData registrationResponseData = registerNewDevice();

        MultifactorStartAuthenticationResponseData startAuthenticationResultData = startAuthenticationInternal(registrationResponseData.getDeviceId());
        assertThat(startAuthenticationResultData, is(not(nullValue())));

        validateStartAuthenticationResponse(startAuthenticationResultData);
        CommonResponse response = doWrongAuthentication(registrationResponseData, startAuthenticationResultData);

        //Authentication must have failed and an appropriated error code should have been returned
        assertThat(response.getError(), is(not(nullValue())));
        assertThat(response.getCode(), is("MFA-0023"));
    }

    @Test
    public void testUnregisterDeviceMustNotBeAllowedIfNotAuthenticated() throws Exception {

        //Register a new device
        MultifactorStartRegistrationResponseData registrationResponseData = registerNewDevice();

        //login with a new session but do not provide 2nd factor
        ApiClient client2 = generateApiClient(testUser);
        rememberClient(client2);
        MultifactorApi multifactorApi = new MultifactorApi(client2);

        //Try to delete the 2nd factor. THIS MUST FAIL.
        MultifactorDeleteResponse deleteResponse = multifactorApi.multifactorDeviceActionDelete(client2.getSession(), getProviderName(), registrationResponseData.getDeviceId());
        assertThat(deleteResponse.getError(), not(isEmptyOrNullString()));
        assertThat(deleteResponse.getCode(), is("MFA-0001"));

        //As a result the device must still be present
        requireDevice(registrationResponseData.getDeviceId());
    }

    @Test
    public void testAuthenticationRequiredForAction() throws Exception {
        //Register a new device
        registerNewDevice();

        //login with a new session but do not provide 2nd factor
        ApiClient client2 = generateApiClient(testUser);
        rememberClient(client2);

        //Perform some API call which is 2fa protected
        //This MUST FAIL, because the 2nd factor was not provided
        CurrentUserResponse currentUser = new UserMeApi(client2).getCurrentUser(client2.getSession());
        assertThat(currentUser.getData(), is(nullValue()));
        assertThat(currentUser.getError(), not(isEmptyOrNullString()));
        assertThat(currentUser.getCode(), is("MFA-0001"));
    }

    @Test
    public void testReauthenticationRequiredAfterAutologin() throws Exception {

        //Register a new device and logout
        MultifactorStartRegistrationResponseData deviceData = registerNewDevice();
        getApiClient().logout();

        //Login again with autologin enabled
        LoginApi loginApi = new LoginApi(getApiClient());
        getApiClient().login(testUser.getUser(), testUser.getPassword());
        loginApi.refreshAutoLoginCookie(getSessionId());

        //..And provide the 2nd factor - Authentication must not fail!
        MultifactorStartAuthenticationResponseData startAuthData = startAuthenticationInternal(deviceData.getDeviceId());
        CommonResponse authResponse = doAuthentication(deviceData, startAuthData);
        assertThat(authResponse.getErrorDesc(), is(nullValue()));

        //Autologin again
        LoginResponse autologin = loginApi.autologin(true, null, null, null);
        assertThat(autologin.getErrorDesc(), is(nullValue()));

        //After autologin is must be allowed to perform almost all API actions without re-authenticating
        CurrentUserResponse currentUser = new UserMeApi(getApiClient()).getCurrentUser(getSessionId());
        assertThat(currentUser.getErrorDesc(), is(nullValue()));

        //However, it must not be allowed to perform certain API actions which require re-authenticating.
        //For example deleting multifactor devices
        MultifactorDeleteResponse deleteResponse = MultifactorApi().multifactorDeviceActionDelete(getSessionId(), getProviderName(), deviceData.getDeviceId());
        assertThat(deleteResponse.getData(), is(empty()));
        assertThat(deleteResponse.getError(), not(isEmptyOrNullString()));
        assertThat(deleteResponse.getCode(), is("MFA-0015"));
    }

    @Test
    public void testRegisterNewDeviceAfterDeviceDeletedAndAutologin() throws Exception {
        //After the last device was deleted, it should be possible to register new devices again after autologin was performed

        //Register a new device and logout
        MultifactorStartRegistrationResponseData deviceData = registerNewDevice();
        getApiClient().logout();

        //Login again with autologin enabled
        LoginApi loginApi = new LoginApi(getApiClient());
        getApiClient().login(testUser.getUser(), testUser.getPassword());
        loginApi.refreshAutoLoginCookie(getSessionId());

        //..And provide the 2nd factor - Authentication must not fail!
        MultifactorStartAuthenticationResponseData startAuthData = startAuthenticationInternal(deviceData.getDeviceId());
        CommonResponse authResponse = doAuthentication(deviceData, startAuthData);
        assertThat(authResponse.getErrorDesc(), is(nullValue()));

        //Delete all devices
        clearAllMultifactorDevicesByUser();
        //Ensure all devices are gone
        assertThat(getDevices(),is(empty()));

        //Autologin again
        //This should clear the requirement for "recent authentication" because no device is left
        LoginResponse autologin = loginApi.autologin(true, null, null, null);
        assertThat(autologin.getErrorDesc(), is(nullValue()));

        //Thus registering new device should be successful
        if(isBackupOnlyProvider()) {
            //A user cannot register a "backup-only" device if no other devices are registered
            //so we start by adding a primary device first
            MultifactorStartRegistrationResponseData registration = startRegistration(TOTPProviderTests.TOTP_PROVIDER_NAME);
            String token = Integer.toString(new TotpGenerator().create(registration.getChallenge().getSharedSecret()));
            finishRegistration(TOTPProviderTests.TOTP_PROVIDER_NAME, registration.getDeviceId(), token, null, null);
        }
        registerNewDevice();
    }

    @Test
    public void testRenameDevice() throws Exception {
        //Register a new device
        MultifactorStartRegistrationResponseData registerData = registerNewDevice();

        //Rename the device
        MultifactorDevice newDevice = new MultifactorDevice();
        final String newDeviceName = "ThisIsANewDeviceName";
        newDevice.setName(newDeviceName);
        newDevice. setId(registerData.getDeviceId());
        MultifactorDeviceResponse renamedResponse = MultifactorApi().multifactorDeviceActionRename(getSessionId(), getProviderName(), newDevice);
        assertThat(renamedResponse, is(not(nullValue())));
        MultifactorDevice renamedDevice = checkResponse(renamedResponse, renamedResponse.getData());

        //Validate that the returned device has a new name
        assertThat(renamedDevice, is(not(nullValue())));
        assertThat(renamedDevice.getId(), is(registerData.getDeviceId()));
        assertThat(renamedDevice.getName(), is(newDeviceName));

        //Re-fetch the device and check if the new name is returned
        MultifactorDevice device = getDevice(newDevice.getId()).get();
        assertThat(device.getName(), is(newDeviceName));
    }
}
