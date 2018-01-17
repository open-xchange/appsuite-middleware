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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import java.util.HashMap;
import java.util.Map;
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
public class SMSProviderTests  extends AbstractMultifactorProviderTest {

    private static final String SMS_PROVIDER_NAME = "SMS";
    private static final String DEMO_TOKEN = "0815";
    private static final String DEMO_PHONE_NUMBER = "+490123456789";

    @Override
    protected Map<String, String> getNeededConfigurations() {
        HashMap<String, String> configuration = new HashMap<String, String>();
        configuration.put("com.openexchange.multifactor.demo", "true");
        return configuration;
    }

    @Override
    protected String getReloadables() {
        return "DemoAwareTokenCreationStrategy,DemoAwareSMSServiceSPI";
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
        final boolean backupDevice = false;
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
        assertThat(data.getChallenge().getPhoneNumberTail().length(), is(4));
        //..But  this must not be the full phone number
        assertThat(data.getChallenge().getPhoneNumberTail(), is(not(DEMO_PHONE_NUMBER)));
    }

    @Override
    protected CommonResponse doAuthentication(MultifactorStartRegistrationResponseData startRegistrationData, MultifactorStartAuthenticationResponseData startAuthenticationData) throws ApiException {
        MultifactorFinishAuthenticationData data = new MultifactorFinishAuthenticationData();
        data.setSecretCode(DEMO_TOKEN);
        return MultifactorApi().multifactorDeviceActionfinishAuthentication(getSessionId(), SMS_PROVIDER_NAME, startRegistrationData.getDeviceId(), data);
    }

    @Override
    protected CommonResponse doWrongAuthentication(MultifactorStartRegistrationResponseData startRegistrationData, MultifactorStartAuthenticationResponseData startAuthenticationData) throws Exception {
        MultifactorFinishAuthenticationData data = new MultifactorFinishAuthenticationData();
        data.setSecretCode("THIS IS A WRONG CODE");
        return MultifactorApi().multifactorDeviceActionfinishAuthentication(getSessionId(), SMS_PROVIDER_NAME, startRegistrationData.getDeviceId(), data);
    }
}
