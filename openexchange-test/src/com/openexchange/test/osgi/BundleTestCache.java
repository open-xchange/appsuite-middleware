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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.LoginTest;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.tools.URLParameter;

/**
 * {@link BundleTestCache} - Test absence of cache bundle
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class BundleTestCache extends AbstractBundleTest {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(BundleTestCache.class);

    private static final String BUNDLE_ID = "com.openexchange.caching";

    /**
     * Initializes a new {@link BundleTestCache}
     */
    public BundleTestCache(final String name) {
        super(name);
    }

    public void testCacheAbsence() {
        try {
            final LoginTest loginTest = new LoginTest("LoginTest");
            final JSONObject jsonObject = login(
                getWebConversation(),
                loginTest.getHostName(),
                loginTest.getLogin(),
                loginTest.getPassword());

            /*
             * Login should succeed
             */
            assertTrue("Error contained in returned JSON object", !jsonObject.has("error") || jsonObject.isNull("error"));

            /*
             * Check for session ID
             */
            assertTrue("Missing session ID", jsonObject.has("session") && !jsonObject.isNull("session"));
            final String sessionId = jsonObject.getString("session");

            /*
             * Temporary stop sessionD bundle to check if session cache fails (due to missing cache service)
             */
            // stopBundle.stop("com.openexchange.sessiond");
            /*
             * Hmm... Tricky to check this behavior here... By now a manual check of server log has to be done to ensure an appropriate
             * error message appears.
             */
            // startBundle.start("com.openexchange.sessiond");
            /*
             * Access to user storage should further work
             */
            final JSONObject userObject = searchUser(getWebConversation(), "*", loginTest.getHostName(), sessionId);
            /*
             * Should succeed
             */
            assertTrue("Error contained in returned JSON object", !userObject.has("error") || userObject.isNull("error"));

            /*
             * Access to infostore should further work
             */
            final JSONObject infostoreObject = allInfostoreItems(
                getWebConversation(),
                loginTest.getHostName(),
                sessionId,
                getStandardInfostoreFolder(getWebConversation(), loginTest.getHostName(), sessionId),
                new int[] { Metadata.ID, Metadata.TITLE, Metadata.DESCRIPTION, Metadata.URL, Metadata.FOLDER_ID });
            /*
             * Should succeed
             */
            assertTrue("Error contained in returned JSON object", !infostoreObject.has("error") || infostoreObject.isNull("error"));

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private static final String USER_URL = "/ajax/contacts";

    private final static int[] CONTACT_FIELDS = { DataObject.OBJECT_ID, Contact.INTERNAL_USERID, Contact.EMAIL1, };

    private static JSONObject searchUser(final WebConversation webCon, final String searchpattern, final String host, final String session) throws Exception {

        final URLParameter parameter = new URLParameter();
        parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session);
        parameter.setParameter(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_SEARCH);

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(CONTACT_FIELDS[0]);
        for (int a = 1; a < CONTACT_FIELDS.length; a++) {
            stringBuilder.append(',').append(CONTACT_FIELDS[a]);
        }

        parameter.setParameter(AJAXServlet.PARAMETER_COLUMNS, stringBuilder.toString());

        final JSONObject jsonObj = new JSONObject();
        jsonObj.put("pattern", searchpattern);

        final ByteArrayInputStream bais = new ByteArrayInputStream(jsonObj.toString().getBytes());
        final WebRequest req = new PutMethodWebRequest(PROTOCOL + host + USER_URL + parameter.getURLParameters(), bais, "text/javascript");
        final WebResponse resp = webCon.getResponse(req);

        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK, resp.getResponseCode());
        final String body = resp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (final JSONException e) {
            LOG.error("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        return json;
    }

    private static StringBuffer getUrl(final String sessionId, final String action, final String hostname) {
        final StringBuffer url = new StringBuffer(PROTOCOL);
        url.append(hostname);
        url.append("/ajax/infostore?session=");
        url.append(sessionId);
        url.append("&action=");
        url.append(action);
        return url;
    }

    private static JSONObject allInfostoreItems(final WebConversation webConv, final String hostname, final String sessionId, final int folderId, final int[] columns) throws MalformedURLException, JSONException, IOException, SAXException {
        final StringBuffer url = getUrl(sessionId, "all", hostname);
        url.append("&folder=");
        url.append(folderId);
        url.append("&columns=");
        for (final int col : columns) {
            url.append(col);
            url.append(',');
        }
        url.deleteCharAt(url.length() - 1);

        final GetMethodWebRequest m = new GetMethodWebRequest(url.toString());
        final WebResponse resp = webConv.getResponse(m);
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK, resp.getResponseCode());
        final String body = resp.getText();
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
