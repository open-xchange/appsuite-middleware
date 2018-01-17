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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
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
            matcher.matches(), is(true));

        //Chat that some QR-Barcode data was returned
        assertThat(challenge.getBase64Image(), not(isEmptyOrNullString()));
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
}
