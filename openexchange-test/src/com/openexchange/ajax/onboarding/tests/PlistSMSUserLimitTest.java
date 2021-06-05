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

package com.openexchange.ajax.onboarding.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import com.openexchange.sms.sipgate.SipgateSMSExceptionCode;
import com.openexchange.sms.tools.SMSBucketExceptionCodes;
import com.openexchange.testing.httpclient.models.CommonResponse;

/**
 * {@link PlistSMSUserLimitTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
@RunWith(BlockJUnit4ClassRunner.class)
public class PlistSMSUserLimitTest extends AbstractPlistSMSTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        // Wait until sms tokens are refreshed
        Thread.sleep(61000);
    }

    @Test
    public void testExceedUserLimitTest() throws Exception {
        // Expecting user limit 2 and refresh interval 1min

        String jsonString = "{\"sms\":\"+49276183850\"}";

        for (int x = 0; x < 3; x++) {
            CommonResponse response = onboardingApi.executeClientOnboarding("apple.iphone/mailsync", "sms", jsonString);
            assertNotNull("Unexpected response from the server! Response does not contain an exception.", response.getError());

            if (x < 2) {
                // Expecting an sipgate authorization exception
                checkException(response.getCode(), SipgateSMSExceptionCode.NOT_CONFIGURED);
            } else {
                // SMS should run into user limit
                checkException(response.getCode(), SMSBucketExceptionCodes.SMS_LIMIT_REACHED);
            }
        }
    }

    @Test
    public void testRefreshTest() throws Exception {
        // Expecting user limit 2 and refresh interval 2min

        String jsonString = "{\"sms\":\"+49276183850\"}";

        for (int x = 0; x < 10; x++) {
            CommonResponse response = onboardingApi.executeClientOnboarding("apple.iphone/mailsync", "sms", jsonString);

            assertNotNull("Unexpected response from the server! Response does not contain an exception.", response.getError());

            if (response.getCode().endsWith("0001") && response.getCode().startsWith("SMSLIMIT")) {
                break;
            }

            if (x == 9) {
                fail("User sms limit is never reached!");
            }
        }

        // Wait until sms tokens are refreshed
        Thread.sleep(122000);

        // Execute another sms request which shouldn't run into the user sms limit
        CommonResponse response = onboardingApi.executeClientOnboarding("apple.iphone/mailsync", "sms", jsonString);
        assertNotNull("Unexpected response from the server! Response does not contain an exception.", response.getError());
        // Expecting an sipgate authorization exception
        checkException(response.getCode(), SipgateSMSExceptionCode.NOT_CONFIGURED);
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("com.openexchange.sms.userlimit.enabled", Boolean.TRUE.toString());
        map.put("com.openexchange.sms.userlimit", String.valueOf(2));
        map.put("com.openexchange.sms.userlimit.refreshInterval", String.valueOf(2));
        map.put("com.openexchange.client.onboarding.sms.ratelimit", String.valueOf(0));
        return map;
    }

    @Override
    protected String getScope() {
        return "user";
    }

}
