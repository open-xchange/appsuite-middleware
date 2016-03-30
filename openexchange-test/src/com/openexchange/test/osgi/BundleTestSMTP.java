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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.cookies.CookieJar;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AbstractAJAXTest;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.LoginTest;
import com.openexchange.tools.URLParameter;

/**
 * {@link BundleTestSMTP} - Test absence of SMTP bundle
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BundleTestSMTP extends AbstractBundleTest {

    private static final String BUNDLE_ID = "com.openexchange.smtp";

    private static final String MAIL_URL = "/ajax/mail";

    /**
     * Initializes a new {@link BundleTestSMTP}
     */
    public BundleTestSMTP(final String name) {
        super(name);
    }

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss", Locale.GERMAN);

    public void testSMTPAbsence() {
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
             * Try to send mail
             */
            final String sessionId = jsonObject.getString("session");
            final JSONObject mailObj = createSimpleMailObject(loginTest);
            final JSONObject mailObject = sendMail(getWebConversation(), loginTest.getHostName(), sessionId, mailObj, true);

            /*
             * Check for error
             */
            assertTrue("No error contained in returned JSON object", mailObject.has("error") && !mailObject.isNull("error"));

            /*
             * Check for code "MSG-0053": No transport provider found.
             */
            assertTrue("Missing error code", mailObject.has("code") && !mailObject.isNull("code"));
            assertTrue("Unexpected error code: " + mailObject.getString("code"), "MSG-0053".equals(mailObject.get("code")));

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private static JSONObject createSimpleMailObject(final AbstractAJAXTest ajaxTest) throws JSONException {
        final JSONObject mailObj = new JSONObject();
        mailObj.put("from", ajaxTest.getSeconduser());
        mailObj.put("to", ajaxTest.getLogin());
        mailObj.put("subject", "JUnit Test Mail: " + SDF.format(new Date()));
        final JSONArray attachments = new JSONArray();
        /*
         * Mail text
         */
        final JSONObject attach = new JSONObject();
        attach.put("content", "This is mail text!<br>Next line<br/><br/>best regards,<br>Max Mustermann");
        attach.put("content_type", "text/plain");
        attachments.put(attach);

        mailObj.put("attachments", attachments);
        return mailObj;
    }

    private static JSONObject sendMail(final WebConversation conversation, final String hostname, final String sessionId, final JSONObject mailObj, final boolean setCookie) throws Exception {
        return sendMail(conversation, hostname, sessionId, mailObj.toString(), setCookie);
    }

    private static JSONObject sendMail(final WebConversation conversation, final String hostname, final String sessionId, final String mailObjStr, final boolean setCookie) throws Exception {
        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, sessionId);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_NEW);

        WebRequest req = null;
        WebResponse resp = null;

        if (setCookie) {
            /*
             * Add cookie
             */
            final CookieJar cookieJar = new CookieJar();
            cookieJar.putCookie(LoginServlet.SESSION_PREFIX + sessionId, sessionId);
        }

        final PostMethodWebRequest postReq = new PostMethodWebRequest(PROTOCOL + hostname + MAIL_URL + parameter.getURLParameters(), true);
        postReq.setParameter("json_0", mailObjStr);

        JSONObject jResponse = null;
        req = postReq;
        resp = conversation.getResource(req);
        jResponse = extractFromCallback(resp.getText());
        return jResponse;
    }

    private static Pattern CALLBACK_ARG_PATTERN = Pattern.compile("callback\\s*\\((\\{.*?)\\);");

    private static JSONObject extractFromCallback(final String html) throws JSONException {
        final Matcher matcher = CALLBACK_ARG_PATTERN.matcher(html);
        if (matcher.find()) {
            final String jsonString = matcher.group(1);
            return new JSONObject(jsonString);
        }
        return null;
    }

    @Override
    protected String getBundleName() {
        return BUNDLE_ID;
    }

}
