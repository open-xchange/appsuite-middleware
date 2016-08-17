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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.onboarding.actions.ExecuteRequest;
import com.openexchange.ajax.onboarding.actions.OnboardingTestResponse;

/**
 * {@link PlistSMSUserLimitTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class PlistSMSUserLimitTest extends AbstractPlistSMSTest {

    /**
     * Initializes a new {@link PlistSMSUserLimitTest}.
     * 
     * @param name
     */
    public PlistSMSUserLimitTest() {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        //Wait until sms tokens are refreshed
        Thread.sleep(61000);
    }

    @Test
    public void testExceedUserLimitTest() throws Exception {
        // Expecting user limit 2 and refresh interval 1min

        String jsonString = "{\"sms\":\"+49276183850\"}";
        JSONObject body = new JSONObject(jsonString);

        for (int x = 0; x < 3; x++) {
            ExecuteRequest req = new ExecuteRequest("apple.iphone/mailsync", "sms", body, false);
            OnboardingTestResponse response = client.execute(req);
            assertNotNull("Response is empty!", response);
            assertNotNull("Unexpected response from the server! Response does not contain an exception.", response.getException());

            if (x < 2) {
                // Expecting an sipgate authorization exception
                assertEquals("Unexpected response from the server! Response does contain a wrong exception: " + response.getException().getMessage(), 3, response.getException().getCode());
                assertEquals("Unexpected response from the server! Response does contain a wrong exception: " + response.getException().getMessage(), "SMS", response.getException().getPrefix());
            } else {
                // SMS should run into user limit
                assertEquals("Unexpected response from the server! Response does contain a wrong exception: " + response.getException().getMessage(), 1, response.getException().getCode());
                assertEquals("Unexpected response from the server! Response does contain a wrong exception: " + response.getException().getMessage(), "SMSLIMIT", response.getException().getPrefix());
            }
        }
    }

    @Test
    public void testRefreshTest() throws Exception {
        // Expecting user limit 2 and refresh interval 1min

        String jsonString = "{\"sms\":\"+49276183850\"}";
        JSONObject body = new JSONObject(jsonString);

        for (int x = 0; x < 10; x++) {
            ExecuteRequest req = new ExecuteRequest("apple.iphone/mailsync", "sms", body, false);
            OnboardingTestResponse response = client.execute(req);
            assertNotNull("Response is empty!", response);
            assertNotNull("Unexpected response from the server! Response does not contain an exception.", response.getException());

            if (response.getException().getCode() == 1 && response.getException().getPrefix().equals("SMSLIMIT")) {
                break;
            }

            if (x == 9) {
                fail("User sms limit is never reached!");
            }
        }

        //Wait until sms tokens are refreshed
        Thread.sleep(61000);

        //Execute another sms request which shouldn't run into the user sms limit
        ExecuteRequest req = new ExecuteRequest("apple.iphone/mailsync", "sms", body, false);
        OnboardingTestResponse response = client.execute(req);
        assertNotNull("Response is empty!", response);
        assertNotNull("Unexpected response from the server! Response does not contain an exception.", response.getException());
        // Expecting an sipgate authorization exception
        assertEquals("Unexpected response from the server! Response does contain a wrong exception: " + response.getException().getMessage(), 3, response.getException().getCode());
        assertEquals("Unexpected response from the server! Response does contain a wrong exception: " + response.getException().getMessage(), "SMS", response.getException().getPrefix());
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("com.openexchange.sms.userlimit", String.valueOf(2));
        map.put("com.openexchange.sms.userlimit.refreshInterval", String.valueOf(1));
        return map;
    }

}
