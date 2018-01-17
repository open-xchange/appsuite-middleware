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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CommonResponse;
import com.openexchange.testing.httpclient.models.MultifactorDevice;
import com.openexchange.testing.httpclient.models.MultifactorStartAuthenticationResponseData;
import com.openexchange.testing.httpclient.models.MultifactorStartRegistrationResponseData;

/**
 * {@link BackupStringProviderTests}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.1
 */
public class BackupStringProviderTests extends AbstractMultifactorProviderTest {

    private static final String WRONG_CODE = "THIS IS A WRONG BACKUP CODE";
    private static final String BACKUP_STRING_PROVIDER = "BACKUP_STRING";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Override
    public void setUp() throws Exception {
        super.setUp();

        //A user cannot register a "backup" device if no other devices are registered
        //so we start by adding a primary device first
        MultifactorStartRegistrationResponseData registration = startRegistration(TOTPProviderTests.TOTP_PROVIDER_NAME);
        String token = Integer.toString(new TotpGenerator().create(registration.getChallenge().getSharedSecret()));
        finishRegistration(TOTPProviderTests.TOTP_PROVIDER_NAME,
            registration.getDeviceId(),
            token,
            null,
            null);
    }


    @Override
    protected String getProviderName() {
        return BACKUP_STRING_PROVIDER;
    }

    @Override
    protected boolean isBackupProvider() {
        return true;
    }

    @Override
    protected boolean isBackupOnlyProvider() {
        return true;
    }

    @Override
    protected MultifactorStartRegistrationResponseData doStartRegistration() throws Exception {
        //Register a new BACK_STRING device
        final boolean isBackup = true;
        return startRegistration(BACKUP_STRING_PROVIDER, null, null, isBackup);
    }

    @Override
    protected void validateStartRegistrationResponse(MultifactorStartRegistrationResponseData startRegistrationData) throws Exception {
        assertThat(startRegistrationData.getChallenge().getSharedSecret(), not(isEmptyOrNullString()));
    }

    @Override
    protected MultifactorDevice doFinishRegistration(MultifactorStartRegistrationResponseData startRegistrationData) throws Exception {
        //This is rather a NO-OP for BACKUP_STRING, but we perform it anyway in order to get the new device returned
        return finishRegistration(BACKUP_STRING_PROVIDER, startRegistrationData.getDeviceId(), null, null, null);
    }

    @Override
    protected void validateRegisteredDevice(MultifactorDevice device) throws Exception {
        //The registered device must be a backup device
        assertThat(device.getBackup(), is(true));
    }

    @Override
    protected void validateStartAuthenticationResponse(MultifactorStartAuthenticationResponseData data) {
        assertThat(data.getChallenge().getBackupStringLength(), is(greaterThan(0)));
    }

    @Override
    protected CommonResponse doAuthentication(MultifactorStartRegistrationResponseData startRegistrationData, MultifactorStartAuthenticationResponseData startAuthenticationData) throws ApiException {
        //We are just replying with the shared secret
        return finishAuthentication(BACKUP_STRING_PROVIDER,
                startRegistrationData.getDeviceId(),
                startRegistrationData.getChallenge().getSharedSecret(),
                null,
                null,
                null);
    }

    @Override
    protected CommonResponse doWrongAuthentication(MultifactorStartRegistrationResponseData startRegistrationData, MultifactorStartAuthenticationResponseData startAuthenticationData) throws Exception {
        //We are replying with a wrong shared secret
        return finishAuthentication(BACKUP_STRING_PROVIDER,
                startRegistrationData.getDeviceId(),
                WRONG_CODE,
                null,
                null,
                null);
    }

    @Test
    public void testRegisterBackupDeviceNotPossibleIfNoPrimaryDevicePresent() throws Exception {
        //Ensure that the user does not have any devices
        clearAllMultifactorDevices();

        //This should fail, because no primary device is present for the user
        thrown.expect(AssertionError.class);
        thrown.expectMessage(containsString("Primary authentication devices must be set up before backup devices"));
        doStartRegistration();
    }
}
