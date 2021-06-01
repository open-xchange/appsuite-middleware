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

package com.openexchange.ajax.multifactor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.openexchange.multifactor.MultifactorProperties;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.models.MultifactorDevice;
import com.openexchange.testing.httpclient.models.MultifactorFinishAuthenticationData;
import com.openexchange.testing.httpclient.models.MultifactorStartAuthenticationResponseData;
import com.openexchange.testing.httpclient.models.MultifactorStartRegistrationResponseData;

/**
 * {@link SMSProviderTests}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.1
 */
public class SMSProviderTests extends AbstractMultifactorProviderTest {

    private static final String SMS_PROVIDER_NAME = "SMS";
    private static final String DEMO_TOKEN = "0815";
    private static final String DEMO_PHONE_NUMBER = "+490123456789";

    @Override
    protected Map<String, String> getNeededConfigurations() {
        HashMap<String, String> configuration = new HashMap<String, String>();
        configuration.put("com.openexchange.multifactor.demo", Boolean.TRUE.toString());
        configuration.put(MultifactorProperties.PREFIX + "sms.enabled", Boolean.TRUE.toString());
        return configuration;
    }

    @Override
    protected String getReloadables() {
        return "DemoAwareTokenCreationStrategy,MultifactorSMSProvider";
    }

    @Override
    protected String getProviderName() {

        return SMS_PROVIDER_NAME;
    }

    @Override
    protected boolean isBackupProvider() {
        return true;
    }

    @Override
    protected boolean isBackupOnlyProvider() {
        return false;
    }

    @Override
    protected MultifactorStartRegistrationResponseData doStartRegistration() throws Exception {
        final Boolean backupDevice = Boolean.FALSE;
        final String noDeviceName = null;
        return startRegistration(SMS_PROVIDER_NAME, noDeviceName, DEMO_PHONE_NUMBER, backupDevice);
    }

    @Override
    protected void validateStartRegistrationResponse(MultifactorStartRegistrationResponseData startRegistrationData) throws Exception {
        /* NO-OP for SMS */
    }

    @Override
    protected MultifactorDevice doFinishRegistration(MultifactorStartRegistrationResponseData startRegistrationData) throws Exception {
        // sending the demo token back to the server in order to complete the registration
        return finishRegistration(SMS_PROVIDER_NAME, startRegistrationData.getDeviceId(), DEMO_TOKEN, null, null);
    }

    @Override
    protected void validateRegisteredDevice(MultifactorDevice device) {
        /* NO-OP for SMS */
    }

    @Override
    protected void validateStartAuthenticationResponse(MultifactorStartAuthenticationResponseData data) {
        //The server must have responded with a tail of the phone number used
        assertThat(data.getChallenge().getPhoneNumberTail(), is(not(nullValue())));
        assertThat(Integer.valueOf(data.getChallenge().getPhoneNumberTail().length()), is(Integer.valueOf(4)));
        //..But  this must not be the full phone number
        assertThat(data.getChallenge().getPhoneNumberTail(), is(not(DEMO_PHONE_NUMBER)));
    }

    @Override
    protected CommonResponse doAuthentication(MultifactorStartRegistrationResponseData startRegistrationData, MultifactorStartAuthenticationResponseData startAuthenticationData) throws ApiException {
        MultifactorFinishAuthenticationData data = new MultifactorFinishAuthenticationData();
        data.setSecretCode(DEMO_TOKEN);
        return MultifactorApi().multifactorDeviceActionfinishAuthentication(SMS_PROVIDER_NAME, startRegistrationData.getDeviceId(), data);
    }

    @Override
    protected CommonResponse doWrongAuthentication(MultifactorStartRegistrationResponseData startRegistrationData, MultifactorStartAuthenticationResponseData startAuthenticationData) throws Exception {
        MultifactorFinishAuthenticationData data = new MultifactorFinishAuthenticationData();
        data.setSecretCode("THIS IS A WRONG CODE");
        return MultifactorApi().multifactorDeviceActionfinishAuthentication(SMS_PROVIDER_NAME, startRegistrationData.getDeviceId(), data);
    }

    @Override
    @Test
    public void testReauthenticationRequiredAfterAutologin() throws Exception {
        super.testReauthenticationRequiredAfterAutologin();
    }

    @Override
    @Test
    public void testRegisterNewDeviceAfterDeviceDeletedAndAutologin() throws Exception {
        super.testRegisterNewDeviceAfterDeviceDeletedAndAutologin();
    }
}
