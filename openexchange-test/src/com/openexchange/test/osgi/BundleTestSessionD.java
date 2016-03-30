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

package com.openexchange.test.osgi;

import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.LoginTest;

/**
 * {@link BundleTestSessionD} - Test absence of SessionD bundle
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BundleTestSessionD extends AbstractBundleTest {

    private static final String BUNDLE_ID = "com.openexchange.sessiond";

    /**
     * Initializes a new {@link BundleTestSessionD}
     *
     * @param name
     */
    public BundleTestSessionD(final String name) {
        super(name);
    }

    @Override
    protected String getBundleName() {
        return BUNDLE_ID;
    }

    public void testSessionDAbsenceThroughLogin() {
        try {
            final LoginTest loginTest = new LoginTest("LoginTest");
            final JSONObject jsonObject = login(
                getWebConversation(),
                loginTest.getHostName(),
                loginTest.getLogin(),
                loginTest.getPassword());

            /*
             * Check for error
             */
            assertTrue("No error contained in returned JSON object", jsonObject.has("error") && !jsonObject.isNull("error"));

            /*
             * Check for code "LGI-0005": Missing service
             */
            assertTrue("Missing error code", jsonObject.has("code") && !jsonObject.isNull("code"));
            assertTrue("Unexpected error code: " + jsonObject.getString("code"), "LGI-0005".equals(jsonObject.get("code")));

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testSessionDAbsence() {
        try {
            /*
             * Restart sessiond
             */
            startBundle.start(BUNDLE_ID);

            final LoginTest loginTest = new LoginTest("LoginTest");
            final JSONObject loginObject = login(
                getWebConversation(),
                loginTest.getHostName(),
                loginTest.getLogin(),
                loginTest.getPassword());

            /*
             * Check for error
             */
            assertTrue("Error contained in returned JSON object", !loginObject.has("error") || loginObject.isNull("error"));

            /*
             * Check for session ID
             */
            assertTrue("Error contained in returned JSON object", loginObject.has("session") && !loginObject.isNull("session"));

            /*
             * Now stop again...
             */
            stopBundle.stop(BUNDLE_ID);

            /*
             * ... and try a normal request which should fail
             */
            final String sessionId = loginObject.getString("session");
            final JSONObject jsonObject = getRootFolders(getWebConversation(), loginTest.getHostName(), sessionId);

            /*
             * Check for error
             */
            assertTrue("No error contained in returned JSON object", jsonObject.has("error") && !jsonObject.isNull("error"));

            /*
             * Check for code "SRV-0001": Missing service
             */
            assertTrue("Missing error code", jsonObject.has("code") && !jsonObject.isNull("code"));
            assertTrue("Unexpected error code: " + jsonObject.getString("code"), "SRV-0001".equals(jsonObject.get("code")));

            /*
             * Check for proper error parameters
             */
            assertTrue("Missing error parameters", jsonObject.has("error_params") && !jsonObject.isNull("error_params"));
            final JSONArray jArray = jsonObject.getJSONArray("error_params");
            assertTrue(
                "Unexpected error parameters: " + jArray,
                jArray.length() == 1 && "com.openexchange.sessiond.SessiondService".equals(jArray.getString(0)));

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}
