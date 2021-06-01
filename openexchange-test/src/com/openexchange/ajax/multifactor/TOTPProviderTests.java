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

import static com.openexchange.java.Autoboxing.B;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorProperties;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.models.MultifactorDevice;
import com.openexchange.testing.httpclient.models.MultifactorStartAuthenticationResponseData;
import com.openexchange.testing.httpclient.models.MultifactorStartRegistrationResponseData;
import com.openexchange.testing.httpclient.models.MultifactorStartRegistrationResponseDataChallenge;

/**
 * {@link TOTPProviderTests}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.1
 */
public class TOTPProviderTests extends AbstractMultifactorProviderTest {

    private static final String WRONG_TEST_TOKEN = "WRONG TEST TOKEN";
    private static final String TOTP_URL_REGEX = "otpauth:\\/\\/totp\\/.*\\?secret=.*&issuer=.*";

    public static final String TOTP_PROVIDER_NAME = "TOTP";

    private String createTOTPToken(String secret) throws OXException {
        return Integer.toString((new TotpGenerator().create(secret)));
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        HashMap<String, String> result = new HashMap<>();
        result.put(MultifactorProperties.PREFIX + "totp.enabled", Boolean.TRUE.toString());
        return result;
    }

    @Override
    protected String getProviderName() {
        return TOTP_PROVIDER_NAME;
    }

    @Override
    protected boolean isBackupProvider() {
        return false;
    }
    @Override
    protected boolean isBackupOnlyProvider() {
        return false;
    }

    @Override
    protected MultifactorStartRegistrationResponseData doStartRegistration() throws Exception {
        return startRegistration(TOTP_PROVIDER_NAME);
    }

    @Override
    protected void validateStartRegistrationResponse(MultifactorStartRegistrationResponseData startRegistrationData) throws Exception {
        MultifactorStartRegistrationResponseDataChallenge challenge = startRegistrationData.getChallenge();
        //Check that a shared secret was returned
        assertThat(challenge.getSharedSecret(), is(not(nullValue())));

        //Check that a valid URL was returned
        assertThat(challenge.getUrl(), is(not(nullValue())));
        Pattern urlPattern = Pattern.compile(TOTP_URL_REGEX);
        Matcher matcher = urlPattern.matcher(challenge.getUrl());
        assertThat(String.format("Provided TOTP URL must be in the correct format (%s), but was: %s ", TOTP_URL_REGEX, challenge.getUrl()),
            B(matcher.matches()), is(Boolean.TRUE));

        //Chat that some QR-Barcode data was returned
        assertThat(challenge.getBase64Image(), not(is(emptyOrNullString())));
    }

    @Override
    protected MultifactorDevice doFinishRegistration(MultifactorStartRegistrationResponseData startRegistrationData) throws Exception {
        String token = createTOTPToken(startRegistrationData.getChallenge().getSharedSecret());
        return finishRegistration(TOTP_PROVIDER_NAME, startRegistrationData.getDeviceId(), token, null, null);
    }

    @Override
    protected void validateRegisteredDevice(MultifactorDevice device) {
        /* NO-OP */
    }

    @Override
    protected void validateStartAuthenticationResponse(MultifactorStartAuthenticationResponseData data) throws Exception {
        //NO-OP
    }

    @Override
    protected CommonResponse doAuthentication(MultifactorStartRegistrationResponseData startRegistrationData, MultifactorStartAuthenticationResponseData startAuthenticationData) throws ApiException, OXException {
        String token = createTOTPToken(startRegistrationData.getChallenge().getSharedSecret());
        return finishAuthentication(TOTP_PROVIDER_NAME, startRegistrationData.getDeviceId(), token, null, null, null);
    }

    @Override
    protected CommonResponse doWrongAuthentication(MultifactorStartRegistrationResponseData startRegistrationData, MultifactorStartAuthenticationResponseData startAuthenticationData) throws Exception {
        return finishAuthentication(TOTP_PROVIDER_NAME, startRegistrationData.getDeviceId(), WRONG_TEST_TOKEN, null, null, null);
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
