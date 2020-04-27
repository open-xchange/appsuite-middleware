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

package com.openexchange.ajax.appPassword;

import static org.hamcrest.MatcherAssert.assertThat;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.openexchange.multifactor.MultifactorProperties;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.AppPasswordRegistrationResponseData;
import com.openexchange.testing.httpclient.models.LoginResponse;
import com.openexchange.testing.httpclient.models.MultifactorDevice;
import com.openexchange.testing.httpclient.models.MultifactorDeviceParameters;
import com.openexchange.testing.httpclient.models.MultifactorFinishAuthenticationData;
import com.openexchange.testing.httpclient.models.MultifactorFinishRegistrationData;
import com.openexchange.testing.httpclient.models.MultifactorFinishRegistrationResponse;
import com.openexchange.testing.httpclient.models.MultifactorStartRegistrationResponse;
import com.openexchange.testing.httpclient.models.MultifactorStartRegistrationResponseData;
import com.openexchange.testing.httpclient.modules.LoginApi;
import com.openexchange.testing.httpclient.modules.MultifactorApi;

/**
 * {@link MultifactorBypassTest}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.3
 */
public class MultifactorBypassTest extends AbstractAppPasswordTest {

    private static final String SMS_PROVIDER_NAME = "SMS";
    private static final String SMS_PHONE_NUMBER = "+15555555555";
    private static final String SMS_TEST_NAME = "TEST";
    private static final String SMS_CODE = "0815";

    private MultifactorApi multifactorApi;
    private String deviceId;
    private LoginApi loginApi;

    @Override
    protected Map<String, String> getNeededConfigurations() {
        HashMap<String, String> configuration = new HashMap<String, String>();
        configuration.put("com.openexchange.multifactor.demo", Boolean.TRUE.toString());
        configuration.put(MultifactorProperties.PREFIX + "sms.enabled", Boolean.TRUE.toString());
        return configuration;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        multifactorApi = new MultifactorApi(getApiClient());
        loginApi = new LoginApi(getApiClient());
    }

    @Override
    public void tearDown() throws Exception {
        try {
            deleteDevice();
            removeAll();
        } finally {
            super.tearDown();
        }
    }

    private void setupSMS() throws ApiException {
        // Test SMS Device
        final MultifactorDevice deviceData = new MultifactorDevice();
        deviceData.setProviderName(SMS_PROVIDER_NAME);
        deviceData.setName(SMS_TEST_NAME);
        deviceData.backup(false);
        MultifactorDeviceParameters paramters = new MultifactorDeviceParameters();
        paramters.setPhoneNumber(SMS_PHONE_NUMBER);
        deviceData.setParameters(paramters);

        // Start registration
        MultifactorStartRegistrationResponse resp = multifactorApi.multifactorDeviceActionStartRegistration(multifactorApi.getApiClient().getSession(), deviceData);
        checkResponse(resp.getError(), resp.getErrorDesc(), resp.getData());
        MultifactorStartRegistrationResponseData respData = resp.getData();
        MultifactorFinishRegistrationData data = new MultifactorFinishRegistrationData();
        data.setSecretCode(SMS_CODE);
        // Finalize
        MultifactorFinishRegistrationResponse finalresp =
            multifactorApi.multifactorDeviceActionfinishRegistration(
                multifactorApi.getApiClient().getSession(), SMS_PROVIDER_NAME, respData.getDeviceId(), data);
        checkResponse(finalresp.getError(), finalresp.getErrorDesc(), finalresp.getData());
        deviceId = respData.getDeviceId();
    }

    private void deleteDevice() throws ApiException {
        if (deviceId != null) {
            try {
                getApiClient().logout();
            } catch (ApiException ex) {
                // May need to log out if hadn't completed test
            }
            // We need to log in the main testuser for cleanup
            getApiClient().login(testUser.getLogin(), testUser.getPassword());
            //multifactorApi = new MultifactorApi(getApiClient());  // update client with latest login
            // Authenticated against sms so we can cleanup
            MultifactorFinishAuthenticationData data = new MultifactorFinishAuthenticationData();
            data.setSecretCode(SMS_CODE);
            multifactorApi.multifactorDeviceActionStartAuthentication(getSessionId(), SMS_PROVIDER_NAME, deviceId);
            multifactorApi.multifactorDeviceActionfinishAuthentication(getSessionId(), SMS_PROVIDER_NAME, deviceId, data);
            // Delete the device
            multifactorApi.multifactorDeviceActionDelete(getSessionId(), SMS_PROVIDER_NAME, deviceId);
        }
    }

    @Test
    public void checkLoginWithMultifactor() throws ApiException {
        // Add multifactor auth to the account
        setupSMS();

        // Add a password
        AppPasswordRegistrationResponseData loginData = addPassword("mail");

        // Logout, and try logging in with new app Spec password
        getApiClient().logout();

        // Check that the normal login requires multifactor

        LoginResponse login = loginApi.doLogin(testUser.getLogin(), testUser.getPassword(), null, null, null, null, null, null, null, false);
        assertThat("Requires multifactor", login.getRequiresMultifactor());
        loginApi.doLogout(login.getSession());

        // Now, check that our new app specific password does not
        login = loginApi.doLogin(loginData.getLogin(), loginData.getPassword(), null, null, null, "mobile-something", null, null, null, false);
        checkResponse(login.getError(), login.getErrorDesc(), login);
        assertThat("Doesn't require multifactor", login.getRequiresMultifactor() == null);
        loginApi.doLogout(login.getSession());

    }

}
