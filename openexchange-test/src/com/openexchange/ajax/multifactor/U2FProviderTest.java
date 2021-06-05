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

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import com.openexchange.ajax.multifactor.u2fclient.U2FClient;
import com.openexchange.ajax.multifactor.u2fclient.U2FClient.AuthenticationData;
import com.openexchange.ajax.multifactor.u2fclient.U2FClient.RegisterData;
import com.openexchange.ajax.multifactor.u2fclient.U2FClientCrypto;
import com.openexchange.ajax.multifactor.u2fclient.U2FClientException;
import com.openexchange.ajax.multifactor.u2fclient.U2FDeviceAccess.U2FKeyPair;
import com.openexchange.multifactor.MultifactorProperties;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.models.MultifactorDevice;
import com.openexchange.testing.httpclient.models.MultifactorStartAuthenticationResponseData;
import com.openexchange.testing.httpclient.models.MultifactorStartAuthenticationResponseDataChallenge;
import com.openexchange.testing.httpclient.models.MultifactorStartAuthenticationResponseDataChallengeSignRequests;
import com.openexchange.testing.httpclient.models.MultifactorStartRegistrationResponseData;
import com.openexchange.testing.httpclient.models.MultifactorStartRegistrationResponseDataChallenge;
import com.openexchange.testing.httpclient.models.MultifactorStartRegistrationResponseDataChallengeRegisterRequests;

/**
 * {@link U2FProviderTest}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.1
 */
public class U2FProviderTest extends AbstractMultifactorProviderTest  {

    private static final String U2F_PROVIDER_NAME = "U2F";
    U2FClient u2fClient;

    @Override
    public void setUp() throws Exception {
        u2fClient = U2FClient.createTestClient();
        super.setUp();
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        HashMap<String, String> result = new HashMap<>();
        result.put(MultifactorProperties.PREFIX + "u2f.enabled", Boolean.TRUE.toString());
        result.put(MultifactorProperties.PREFIX + "u2f.appId", "https://localhost");
        return result;
    }

    @Override
    protected String getProviderName() {
        return U2F_PROVIDER_NAME;
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
        return startRegistration(U2F_PROVIDER_NAME);
    }

    @Override
    protected void validateStartRegistrationResponse(MultifactorStartRegistrationResponseData startRegistrationData) throws Exception {
        MultifactorStartRegistrationResponseDataChallenge challenge = startRegistrationData.getChallenge();
        assertThat(challenge.getRequestId(), not(is(emptyString())));
        assertThat(challenge.getRegisterRequests(), is(not(nullValue())));
        assertThat(challenge.getRegisterRequests(), is(not(empty())));

        MultifactorStartRegistrationResponseDataChallengeRegisterRequests challengeData = challenge.getRegisterRequests().get(0);
        assertThat(challengeData.getVersion(), is(equalTo(U2FClient.U2F_VERSION)));
        assertThat(challengeData.getChallenge(), is(not(nullValue())));
        assertThat(challengeData.getRequestId(), is(not(nullValue())));
    }

    @Override
    protected MultifactorDevice doFinishRegistration(MultifactorStartRegistrationResponseData startRegistrationData) throws Exception {

        RegisterData registrationData = u2fClient.createRegisterData(startRegistrationData);

        return finishRegistration(U2F_PROVIDER_NAME,
            startRegistrationData.getDeviceId(),
            null,
            registrationData.getClientData(),
            registrationData.getRegistrationData());
    }

    @Override
    protected void validateRegisteredDevice(MultifactorDevice device) throws Exception {
        // nothing special to check here
    }

    @Override
    protected void validateStartAuthenticationResponse(MultifactorStartAuthenticationResponseData startAuthenticationData) {

        MultifactorStartAuthenticationResponseDataChallenge challenge = startAuthenticationData.getChallenge();
        assertThat(challenge.getRequestId(), is(not(nullValue())));

        List<MultifactorStartAuthenticationResponseDataChallengeSignRequests> signRequests = challenge.getSignRequests();
        assertThat(signRequests, is(not(nullValue())));
        assertThat(signRequests, is(not(empty())));

        MultifactorStartAuthenticationResponseDataChallengeSignRequests signRequest = signRequests.get(0);
        assertThat(signRequest.getVersion(), is(equalTo(U2FClient.U2F_VERSION)));
        assertThat(signRequest.getChallenge(), is(not(nullValue())));
        assertThat(signRequest.getKeyHandle(), is(not(nullValue())));
    }

    @Override
    protected CommonResponse doAuthentication(MultifactorStartRegistrationResponseData startRegistrationData, MultifactorStartAuthenticationResponseData startAuthenticationData) throws Exception {
        final AuthenticationData createAuthenticationData = u2fClient.createAuthenticationData(startAuthenticationData);
        return finishAuthentication(U2F_PROVIDER_NAME,
            startRegistrationData.getDeviceId(),
            null,
            createAuthenticationData.getClientData(),
            createAuthenticationData.getSignatureData(),
            createAuthenticationData.getKeyHandle());
    }

    @Override
    protected CommonResponse doWrongAuthentication(MultifactorStartRegistrationResponseData startRegistrationData, MultifactorStartAuthenticationResponseData startAuthenticationData) throws Exception {

        final U2FClientCrypto originalCrypto = u2fClient.getCrypto();

        //Setting up a crypto mock which will sign the challenge with a wrong private key in order to fail the authentication
        u2fClient.setCrypto(new U2FClientCrypto() {

            @Override
            public byte[] sha256(byte[] data) throws U2FClientException {
                return originalCrypto.sha256(data);
            }

            @Override
            public byte[] sign(byte[] data, PrivateKey key) throws U2FClientException {
                //Create a wrong key and sign the data with that key
                U2FKeyPair wrongKey = u2fClient.getDeviceAccess().getKeyPair(null,null);
                return originalCrypto.sign(data, wrongKey.getKeyPair().getPrivate());
            }
        });

        try {
            //create wrong data
            AuthenticationData createAuthenticationData = u2fClient.createAuthenticationData(startAuthenticationData);
            //Send wrong data
            return finishAuthentication(U2F_PROVIDER_NAME,
                startRegistrationData.getDeviceId(),
                null,
                createAuthenticationData.getClientData(),
                createAuthenticationData.getSignatureData(),
                createAuthenticationData.getKeyHandle());
        }
        finally {
            //Remove the crypto mock and reset the orgiginal crypto impl.
            u2fClient.setCrypto(originalCrypto);
        }
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
