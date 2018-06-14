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

import static org.junit.Assert.assertNotNull;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import com.google.common.io.BaseEncoding;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.sms.SMSExceptionCode;
import com.openexchange.testing.httpclient.models.CommonResponse;

/**
 * {@link PlistSMSTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class PlistSMSTest extends AbstractPlistSMSTest {

    private static final String SLASH = "/";


    @Test
    public void testExecute() throws Exception {
        String jsonString = "{\"sms\":\"+49276183850\"}";
        for (String id : SCENARIOS) {
            CommonResponse response = onboardingApi.executeClientOnboarding(getSessionId(), id, "sms", jsonString);

            // Expecting an sipgate authorization exception
            assertNotNull("Unexpected response from the server! Response does not contain an exception.", response.getError());

            if (response.getCode().equals("ONBRD-0010")) {
                // scenario disabled
                continue;
            }
            checkException(response.getCode(), SMSExceptionCode.NOT_SENT);
        }
    }

    @Test
    public void testExecute_missingNumber() throws Exception {
        String jsonString = "{\"sms\":\"\"}";

        String id = SCENARIOS[0];
        CommonResponse response = onboardingApi.executeClientOnboarding(getSessionId(), id, "sms", jsonString);

        // Expecting an invalid number exception
        checkException(response.getCode(), OnboardingExceptionCodes.INVALID_PHONE_NUMBER);
    }

    @Test
    public void testExecute_invalidNumber() throws Exception {
        String jsonString = "{\"sms\":\"1234\"}";

        String id = SCENARIOS[0];
        CommonResponse response = onboardingApi.executeClientOnboarding(getSessionId(), id, "sms", jsonString);

        // Expecting an invalid number exception
        checkException(response.getCode(), OnboardingExceptionCodes.INVALID_PHONE_NUMBER);

        jsonString = "{\"sms\":\"abcde\"}";
        response = onboardingApi.executeClientOnboarding(getSessionId(), id, "sms", jsonString);
        // Expecting an invalid number exception
        checkException(response.getCode(), OnboardingExceptionCodes.INVALID_PHONE_NUMBER);
    }

    @Test
    public void testDownload() throws Exception {
        PListDownloadTestHelper helper = new PListDownloadTestHelper(PlistSMSTest.class.getName());
        AJAXClient client = getClient();
        String url = getURL(client.getValues().getUserId(), client.getValues().getContextId(), "mailsync", "apple.iphone");
        helper.testMailDownload(url, client.getProtocol()+"://"+client.getHostname());

        url = getURL(client.getValues().getUserId(), client.getValues().getContextId(), "eassync", "apple.iphone");
        helper.testEASDownload(url, client.getProtocol()+"://"+client.getHostname());

        url = getURL(client.getValues().getUserId(), client.getValues().getContextId(), "davsync", "apple.iphone");
        helper.testDavDownload(url, client.getProtocol()+"://"+client.getHostname());
    }



    public String getURL(int userId, int contextId, String scenario, String device) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        BaseEncoding encoder = BaseEncoding.base64().omitPadding();
        StringBuilder url = new StringBuilder();

        String userString = new String(encoder.encode(String.valueOf(userId).getBytes()));
        String contextString = new String(encoder.encode(String.valueOf(contextId).getBytes()));
        String scenarioString = new String(encoder.encode(scenario.getBytes()));
        String deviceString = new String(encoder.encode(device.getBytes()));
        String challenge = toHash(userId, contextId, scenario, device);
        url.append("/ajax/plist");
        url.append(SLASH).append(userString).append(SLASH).append(contextString).append(SLASH).append(deviceString).append(SLASH).append(scenarioString).append(SLASH).append(challenge);
        return url.toString();
    }

    private static String toHash(int userId, int contextId, String scenario, String device) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        String secret = UID;
        String challenge = userId + contextId + device + scenario + secret;
        MessageDigest md;

        md = MessageDigest.getInstance("SHA-1");

        byte[] sha1hash = new byte[40];
        md.update(challenge.getBytes("UTF-8"), 0, challenge.length());
        sha1hash = md.digest();
        return convertToHex(sha1hash);
    }

    private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    @Override
    protected Map<String, String> getNeededConfigurations() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("com.openexchange.sms.userlimit.enabled", String.valueOf(false));
        return map;
    }

    @Override
    protected String getScope() {
        return "user";
    }
}
