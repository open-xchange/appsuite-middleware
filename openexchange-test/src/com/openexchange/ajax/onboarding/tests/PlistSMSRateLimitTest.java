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

package com.openexchange.ajax.onboarding.tests;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.sms.SMSExceptionCode;
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

        CommonResponse response = onboardingApi.executeClientOnboarding(getSessionId(), "apple.iphone/mailsync", "sms", jsonString);
        // Expecting an sipgate authorization exception
        checkException(response.getCode(), SMSExceptionCode.NOT_SENT);

        response = onboardingApi.executeClientOnboarding(getSessionId(), "apple.iphone/mailsync", "sms", jsonString);
        // Expecting an SENT_QUOTA_EXCEEDED exeption
        checkException(response.getCode(), OnboardingExceptionCodes.SENT_QUOTA_EXCEEDED);

        // Wait until user should be able to send sms again
        Thread.sleep(11000);

        response = onboardingApi.executeClientOnboarding(getSessionId(), "apple.iphone/mailsync", "sms", jsonString);
        // Expecting an sipgate authorization exception
        checkException(response.getCode(), SMSExceptionCode.NOT_SENT);
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
