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
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.cookies.CookieJar;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.LoginTest;
import com.openexchange.ajax.Mail;

/**
 * {@link BundleTestIMAP} - Test absence of IMAP bundle
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BundleTestIMAP extends AbstractBundleTest {

    private static final String BUNDLE_ID = "com.openexchange.imap";

    private static final String MAIL_URL = "/ajax/mail";

    /**
     * Initializes a new {@link BundleTestIMAP}
     */
    public BundleTestIMAP(final String name) {
        super(name);
    }

    public void testIMAPAbsence() {
        try {
            final LoginTest loginTest = new LoginTest("LoginTest");
            final JSONObject jsonObject = login(
                getWebConversation(),
                loginTest.getHostName(),
                loginTest.getLogin(),
                loginTest.getPassword());

            /*
             * No error should occur while login
             */
            assertTrue("Error contained in returned JSON object", !jsonObject.has("error") || jsonObject.isNull("error"));

            /*
             * Check for session ID
             */
            assertTrue("Missing session ID", jsonObject.has("session") && !jsonObject.isNull("session"));

            /*
             * Try to access mail
             */
            final String sessionId = jsonObject.getString("session");
            final JSONObject mailObject = getAllMails(getWebConversation(), loginTest.getHostName(), sessionId, "INBOX", null, true);

            /*
             * Check for error
             */
            assertTrue("No error contained in returned JSON object", mailObject.has("error") && !mailObject.isNull("error"));

            /*
             * Check for code "MSG-0044": No provider found.
             */
            assertTrue("Missing error code", mailObject.has("code") && !mailObject.isNull("code"));
            assertTrue("Unexpected error code: " + mailObject.getString("code"), "MSG-0044".equals(mailObject.get("code")));

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private static JSONObject getAllMails(final WebConversation conversation, final String hostname, final String sessionId, final String folder, final int[] cols, final boolean setCookie) throws IOException, SAXException, JSONException {
        final GetMethodWebRequest getReq = new GetMethodWebRequest(PROTOCOL + hostname + MAIL_URL);
        if (setCookie) {
            /*
             * Set cookie cause a request has already been fired before with the same session id.
             */
            final CookieJar cookieJar = new CookieJar();
            cookieJar.putCookie(LoginServlet.SESSION_PREFIX + sessionId, sessionId);
        }
        getReq.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        getReq.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_ALL);
        getReq.setParameter(Mail.PARAMETER_MAILFOLDER, folder);
        final String colsStr;
        if (cols != null && cols.length > 0) {
            final StringBuilder sb = new StringBuilder();
            sb.append(cols[0]);
            for (int i = 1; i < cols.length; i++) {
                // sb.append("%2C").append(cols[i]);
                sb.append(',').append(cols[i]);
            }
            colsStr = sb.toString();
        } else {
            colsStr = "600,601,602,612,603,607,610,608,611,614,102";
        }
        getReq.setParameter(AJAXServlet.PARAMETER_COLUMNS, colsStr);
        getReq.setParameter(AJAXServlet.PARAMETER_SORT, "610");
        getReq.setParameter(AJAXServlet.PARAMETER_ORDER, "asc");
        final WebResponse resp = conversation.getResponse(getReq);
        final JSONObject jResponse = new JSONObject(resp.getText());
        return jResponse;
    }

    @Override
    protected String getBundleName() {
        return BUNDLE_ID;
    }

}
