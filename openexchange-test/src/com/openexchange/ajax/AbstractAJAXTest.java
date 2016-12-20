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

package com.openexchange.ajax;

import static org.junit.Assert.assertFalse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;
import com.google.code.tempusfugit.concurrency.ConcurrentTestRunner;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AbstractUploadParser;
import com.openexchange.ajax.framework.ProvisioningSetup;
import com.openexchange.ajax.infostore.actions.InfostoreTestManager;
import com.openexchange.ajax.mail.MailTestManager;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.exception.OXException;
import com.openexchange.test.AjaxInit;
import com.openexchange.test.AttachmentTestManager;
import com.openexchange.test.CalendarTestManager;
import com.openexchange.test.ContactTestManager;
import com.openexchange.test.FolderTestManager;
import com.openexchange.test.ResourceTestManager;
import com.openexchange.test.TaskTestManager;
import com.openexchange.test.pool.TestContext;
import com.openexchange.test.pool.TestContextPool;
import com.openexchange.test.pool.TestUser;

/**
 * This class implements inheritable methods for AJAX tests.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @deprecated use {@link AbstractAJAXSession}.
 */
@Deprecated
@RunWith(ConcurrentTestRunner.class)
public abstract class AbstractAJAXTest {

    public static final String PROTOCOL = "http://";

    private static final String HOSTNAME = "hostname";

    protected static final String jsonTagData = "data";

    protected static final String jsonTagTimestamp = "timestamp";

    protected static final String jsonTagError = "error";

    private static final Map<String, String> testArgsMap = new HashMap<String, String>();

    private String hostName = null;

    private WebConversation webConversation = null;

    private WebConversation webConversation2 = null;

    private Properties ajaxProps = null;

    protected static final int APPEND_MODIFIED = 1000000;

    protected TestContext testContext;

    private AJAXClient client;

    private AJAXClient client2;

    protected TestUser testUser;

    protected TestUser testUser2;

    protected FolderTestManager ftm;

    protected CalendarTestManager catm;

    protected ContactTestManager cotm;
    
    protected TaskTestManager ttm;

    protected InfostoreTestManager itm;

    protected ResourceTestManager rtm;

    protected MailTestManager mtm;

    protected AttachmentTestManager atm;

    @Before
    public void setUp() throws Exception {
        try {
            AJAXConfig.init();
            ProvisioningSetup.init();

            testContext = TestContextPool.acquireContext(this.getClass().getCanonicalName());
            testUser = testContext.acquireUser();
            testUser2 = testContext.acquireUser();
            client = new AJAXClient(testUser);
            client2 = new AJAXClient(testUser2);
            
            ftm = new FolderTestManager(client);
            catm = new CalendarTestManager(client);
            cotm = new ContactTestManager(client);
            ttm = new TaskTestManager(client);
            itm = new InfostoreTestManager(client);
            rtm = new ResourceTestManager(client);
            mtm = new MailTestManager(client);
            atm = new AttachmentTestManager(client);
        } catch (final OXException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @After
    public void tearDown() throws Exception {
        try {
            ftm.cleanUp();
            catm.cleanUp();
            cotm.cleanUp();
            ttm.cleanUp();
            itm.cleanUp();
            rtm.cleanUp();
            mtm.cleanUp();
            atm.cleanUp();
        } finally {
            TestContextPool.backContext(testContext);
        }
    }

    protected String getAJAXProperty(final String key) {
        return getAJAXProperties().getProperty(key);
    }

    protected Properties getAJAXProperties() {
        if (null == ajaxProps) {
            ajaxProps = AjaxInit.getAJAXProperties();
        }
        return ajaxProps;
    }

    /**
     * @return Returns the hostname.
     */
    public String getHostName() {
        if (null == hostName) {
            hostName = getAJAXProperty(HOSTNAME);
        }
        return hostName;
    }

    /**
     * @return Returns the webConversation.
     */
    protected WebConversation getWebConversation() {
        if (null == webConversation) {
            webConversation = newWebConversation();
        }
        return webConversation;
    }

    /**
     * @return Returns the webConversation.
     */
    protected WebConversation getSecondWebConversation() {
        if (null == webConversation2) {
            webConversation2 = newWebConversation();
        }
        return webConversation2;
    }

    /**
     * Setup the web conversation here so tests are able to create additional if
     * several users are needed for tests.
     * 
     * @return a new web conversation.
     */
    protected WebConversation newWebConversation() {
        HttpUnitOptions.setDefaultCharacterSet("UTF-8");
        HttpUnitOptions.setScriptingEnabled(false);

        WebConversation conv = new WebConversation();
        conv.setUserAgent(AJAXSession.USER_AGENT);
        return conv;
    }

    protected String getSessionId() {
        return getClient().getSession().getId();
    }

    protected String getSecondSessionId() {
        return client2.getSession().getId();
    }

    public String getLogin() {
        return testUser.getLogin();
    }

    public String getPassword() {
        return testUser.getPassword();
    }

    public String getSeconduser() {
        return testUser2.getLogin();
    }

    // Query methods

    protected String putS(final WebConversation webConv, final String url, final String body) throws MalformedURLException, IOException, SAXException {
        final PutMethodWebRequest m = new PutMethodWebRequest(url, new ByteArrayInputStream(body.getBytes(com.openexchange.java.Charsets.UTF_8)), "text/javascript; charset=UTF-8");
        final WebResponse resp = webConv.getResponse(m);
        String text = resp.getText();
        return text;
    }

    protected JSONObject put(final WebConversation webConv, final String url, final String body) throws MalformedURLException, JSONException, IOException, SAXException {
        final JSONObject o = new JSONObject(putS(webConv, url, body));
        return o;
    }

    protected String gS(final WebConversation webConv, final String url) throws MalformedURLException, IOException, SAXException {
        final GetMethodWebRequest m = new GetMethodWebRequest(url);
        final WebResponse resp = webConv.getResponse(m);
        return resp.getText();
    }

    protected Response gT(final WebConversation webConv, final String url) throws MalformedURLException, JSONException, IOException, SAXException {
        final String res = gS(webConv, url);
        if ("".equals(res.trim())) {
            return null;
        }
        return Response.parse(res);
    }

    protected Response putT(final WebConversation webConv, final String url, final String data) throws MalformedURLException, JSONException, IOException, SAXException {
        final String res = putS(webConv, url, data);
        if ("".equals(res.trim())) {
            return null;
        }
        return Response.parse(res);
    }

    public static void assertNoError(final Response res) {
        assertFalse(res.getErrorMessage(), res.hasError());
    }

    public static JSONObject extractFromCallback(final String html) throws JSONException {
        return new JSONObject(AbstractUploadParser.extractFromCallback(html));
    }

    // A poor mans hash literal
    protected Map<String, String> m(final String... pairs) {
        if (pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Must contain matching pairs");
        }

        final Map<String, String> m = new HashMap<String, String>();

        for (int i = 0; i < pairs.length; i++) {
            m.put(pairs[i], pairs[++i]);
        }

        return m;

    }

    public static String appendPrefix(final String host) {
        if (host.startsWith("http://") || host.startsWith("https://")) {
            return host;
        }
        return "http://" + host;
    }

    public static final boolean containsTestArg(final String arg) {
        return testArgsMap.containsKey(arg);
    }

    public static final String getTestArg(final String arg) {
        return testArgsMap.get(arg);
    }

    public static final void putTestArg(final String arg, final String value) {
        testArgsMap.put(arg, value);
    }

    public AJAXClient getClient() {
        return client;
    }

    public AJAXClient getClient2() {
        return client2;
    }
}
