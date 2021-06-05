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

import static com.openexchange.java.Autoboxing.I;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.openexchange.multifactor.MultifactorProperties;
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
    protected Map<String, String> getNeededConfigurations() {
        HashMap<String, String> result = new HashMap<>();
        result.put(MultifactorProperties.PREFIX + "totp.enabled", Boolean.TRUE.toString());
        return result;
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
        final Boolean isBackup = Boolean.TRUE;
        return startRegistration(BACKUP_STRING_PROVIDER, null, null, isBackup);
    }

    @Override
    protected void validateStartRegistrationResponse(MultifactorStartRegistrationResponseData startRegistrationData) throws Exception {
        assertThat(startRegistrationData.getChallenge().getSharedSecret(), not(is(emptyOrNullString())));
    }

    @Override
    protected MultifactorDevice doFinishRegistration(MultifactorStartRegistrationResponseData startRegistrationData) throws Exception {
        //This is rather a NO-OP for BACKUP_STRING, but we perform it anyway in order to get the new device returned
        return finishRegistration(BACKUP_STRING_PROVIDER, startRegistrationData.getDeviceId(), null, null, null);
    }

    @Override
    protected void validateRegisteredDevice(MultifactorDevice device) throws Exception {
        //The registered device must be a backup device
        assertThat(device.getBackup(), is(Boolean.TRUE));
    }

    @Override
    protected void validateStartAuthenticationResponse(MultifactorStartAuthenticationResponseData data) {
        assertThat(data.getChallenge().getBackupStringLength(), is(greaterThan(I(0))));
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
