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

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.LoginTest;

/**
 * {@link BundleTestConfigJump} - Test absence of generic config-jump bundle
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BundleTestConfigJump extends AbstractBundleTest {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(BundleTestConfigJump.class);

    private static final String BUNDLE_ID = "com.openexchange.configjump.generic";

    private static final String CONTROL_URL = "/ajax/control";

    /**
     * Initializes a new {@link BundleTestConfigJump}
     */
    public BundleTestConfigJump(final String name) {
        super(name);
    }

    public void testConfighJumpAbsence() {
        try {
            final LoginTest loginTest = new LoginTest("LoginTest");
            final JSONObject jsonObject = login(
                getWebConversation(),
                loginTest.getHostName(),
                loginTest.getLogin(),
                loginTest.getPassword());

            /*
             * Login should work
             */
            assertTrue("Error contained in returned JSON object", !jsonObject.has("error") || jsonObject.isNull("error"));

            /*
             * Check session ID
             */
            assertTrue("Missing session ID", jsonObject.has("session") && !jsonObject.isNull("session"));
            final String sessionId = jsonObject.getString("session");

            /*
             * Check config-jump
             */
            final JSONObject configJumpObject = readURL(getWebConversation(), loginTest.getHostName(), sessionId);

            /*
             * Check for error
             */
            assertTrue("No error contained in returned JSON object", configJumpObject.has("error") && !configJumpObject.isNull("error"));

            /*
             * Check for code "LGI-0008": Missing service
             */
            assertTrue("Missing error code", configJumpObject.has("code") && !configJumpObject.isNull("code"));
            assertTrue("Unexpected error code: " + configJumpObject.get("code"), "LGI-0008".equals(configJumpObject.get("code")));

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private static JSONObject readURL(final WebConversation conversation, final String hostName, final String sessionId) throws IOException, SAXException, JSONException {
        LOG.trace("Reading control center URL.");
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + hostName + CONTROL_URL);
        req.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        final WebResponse resp = conversation.getResponse(req);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK, resp.getResponseCode());
        final String body = resp.getText();
        LOG.trace("Response body: \"" + body + "\"");
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (final JSONException e) {
            LOG.error("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        return json;
    }

    @Override
    protected String getBundleName() {
        return BUNDLE_ID;
    }

}
