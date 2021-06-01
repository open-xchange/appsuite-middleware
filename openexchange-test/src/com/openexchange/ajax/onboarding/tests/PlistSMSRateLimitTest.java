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

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.sms.sipgate.SipgateSMSExceptionCode;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.CommonResponse;

/**
 * {@link PlistSMSRateLimitTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class PlistSMSRateLimitTest extends AbstractPlistSMSTest {

    @Test
    public void testRateLimit() throws InterruptedException, ApiException {
        String jsonString = "{\"sms\":\"+49276183850\"}";

        CommonResponse response = onboardingApi.executeClientOnboarding("apple.iphone/mailsync", "sms", jsonString);
        // Expecting an sipgate authorization exception
        checkException(response.getCode(), SipgateSMSExceptionCode.NOT_CONFIGURED);

        response = onboardingApi.executeClientOnboarding("apple.iphone/mailsync", "sms", jsonString);
        // Expecting an SENT_QUOTA_EXCEEDED exeption
        checkException(response.getCode(), OnboardingExceptionCodes.SENT_QUOTA_EXCEEDED);

        // Wait until user should be able to send sms again
        Thread.sleep(11000);

        response = onboardingApi.executeClientOnboarding("apple.iphone/mailsync", "sms", jsonString);
        // Expecting an sipgate authorization exception
        checkException(response.getCode(), SipgateSMSExceptionCode.NOT_CONFIGURED);
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("com.openexchange.sms.userlimit.enabled", String.valueOf(false));
        map.put("com.openexchange.client.onboarding.sms.ratelimit", String.valueOf(10000));
        return map;
    }

    @Override
    protected String getScope() {
        return "user";
    }

}
