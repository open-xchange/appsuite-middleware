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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import java.security.PrivateKey;
import java.util.List;
import com.openexchange.ajax.multifactor.u2fclient.U2FClient;
import com.openexchange.ajax.multifactor.u2fclient.U2FClient.AuthenticationData;
import com.openexchange.ajax.multifactor.u2fclient.U2FClient.RegisterData;
import com.openexchange.ajax.multifactor.u2fclient.U2FClientCrypto;
import com.openexchange.ajax.multifactor.u2fclient.U2FClientException;
import com.openexchange.ajax.multifactor.u2fclient.U2FDeviceAccess.U2FKeyPair;
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
        assertThat(challenge.getRequestId(), not(isEmptyString()));
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
}
