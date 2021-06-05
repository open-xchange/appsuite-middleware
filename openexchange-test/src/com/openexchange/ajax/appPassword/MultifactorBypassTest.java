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

package com.openexchange.ajax.appPassword;

import static java.lang.Boolean.FALSE;
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
    @SuppressWarnings("hiding")
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

    private void setupSMS() throws ApiException {
        // Test SMS Device
        final MultifactorDevice deviceData = new MultifactorDevice();
        deviceData.setProviderName(SMS_PROVIDER_NAME);
        deviceData.setName(SMS_TEST_NAME);
        deviceData.backup(FALSE);
        MultifactorDeviceParameters paramters = new MultifactorDeviceParameters();
        paramters.setPhoneNumber(SMS_PHONE_NUMBER);
        deviceData.setParameters(paramters);

        // Start registration
        MultifactorStartRegistrationResponse resp = multifactorApi.multifactorDeviceActionStartRegistration(deviceData);
        checkResponse(resp.getError(), resp.getErrorDesc(), resp.getData());
        MultifactorStartRegistrationResponseData respData = resp.getData();
        MultifactorFinishRegistrationData data = new MultifactorFinishRegistrationData();
        data.setSecretCode(SMS_CODE);
        // Finalize
        MultifactorFinishRegistrationResponse finalresp =
            multifactorApi.multifactorDeviceActionfinishRegistration(SMS_PROVIDER_NAME, respData.getDeviceId(), data);
        checkResponse(finalresp.getError(), finalresp.getErrorDesc(), finalresp.getData());
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

        LoginResponse login = loginApi.doLogin(testUser.getLogin(), testUser.getPassword(), null, null, null, null, null, null, null, null, FALSE);
        assertThat("Requires multifactor", login.getRequiresMultifactor().booleanValue());
        loginApi.doLogout(login.getSession());

        // Now, check that our new app specific password does not
        login = loginApi.doLogin(loginData.getLogin(), loginData.getPassword(), null, null, null, null, "mobile-something", null, null, null, FALSE);
        checkResponse(login.getError(), login.getErrorDesc(), login);
        assertThat("Doesn't require multifactor", login.getRequiresMultifactor() == null);
        loginApi.doLogout(login.getSession());

    }

}
