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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.AbstractUploadParser;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.exception.OXException;
import com.openexchange.test.AjaxInit;

/**
 * This class implements inheritable methods for AJAX tests.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @deprecated use {@link AbstractAJAXSession}.
 */
@Deprecated
public abstract class AbstractAJAXTest extends TestCase {

    public static final String PROTOCOL = "http://";

    private static final String HOSTNAME = "hostname";

    protected static final String jsonTagData = "data";

    protected static final String jsonTagTimestamp = "timestamp";

    protected static final String jsonTagError = "error";

    private static final Map<String, String> testArgsMap = new HashMap<String, String>();

    private String hostName = null;

    private WebConversation webConversation = null;

    private WebConversation webConversation2 = null;

    private String sessionId = null;

    private String sessionId2 = null;

    private String login = null;

    private String seconduser = null;

    private String password = null;

    private Properties ajaxProps = null;

    protected static final int APPEND_MODIFIED = 1000000;

    public AbstractAJAXTest(final String name) {
        super(name);

        try {
            AJAXConfig.init();
        } catch (final OXException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        logout();
        super.tearDown();
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
     * @return a new web conversation.
     */
    protected WebConversation newWebConversation() {
        HttpUnitOptions.setDefaultCharacterSet("UTF-8");
        HttpUnitOptions.setScriptingEnabled(false);

        WebConversation conv = new WebConversation();
        conv.setUserAgent(AJAXSession.USER_AGENT);
        return conv;
    }

    /**
     * @return Returns the sessionId.
     * @throws JSONException if parsing of serialized json fails.
     * @throws SAXException if a SAX error occurs.
     * @throws IOException if the communication with the server fails.
     * @throws OXException
     */
    protected String getSessionId() throws IOException, JSONException, OXException {
        if (null == sessionId) {
            sessionId = LoginTest.getSessionId(getWebConversation(),
                    getHostName(), getLogin(), getPassword());
            assertNotNull("Can't get session id.", sessionId);
        }
        return sessionId;
    }

    protected String getSecondSessionId() throws IOException, JSONException, OXException {
        if(null == sessionId2) {
            sessionId2 = LoginTest.getSessionId(getSecondWebConversation(),
                    getHostName(), getSeconduser(), getPassword());
            assertNotNull("Can't get session id for second user.",sessionId2);
        }
        return sessionId2;
    }

    /**
     * Terminates the session on the server.
     * @throws Exception if an error occurs.
     */
    protected void logout() throws Exception {
        if (null != sessionId) {
            LoginTest.logout(getWebConversation(), getHostName(),
                    getSessionId());
            sessionId = null;
            webConversation = null;
        }
        webConversation = null;
        if (null != sessionId2) {
            LoginTest.logout(getSecondWebConversation(), getHostName(),
                    getSecondSessionId());
            sessionId2 = null;
        }
        webConversation2 = null;
    }

    public String getLogin() {
        if (null == login) {
            login = getAJAXProperty("login");
        }
        if(! login.contains("@")){
            login += "@" + getAJAXProperty("contextName");
        }
        return login;
    }

    public String getPassword() {
        if (null == password) {
            password = getAJAXProperty("password");
        }
        return password;
    }

    public String getSeconduser() {
        if (null == seconduser) {
            seconduser = getAJAXProperty("seconduser");
        }
        if(! seconduser.contains("@")){
        	seconduser += "@" + getAJAXProperty("contextName");
        }
        return seconduser;
    }

    // Query methods

    protected String putS(final WebConversation webConv, final String url, final String body) throws MalformedURLException, IOException, SAXException {
        final PutMethodWebRequest m = new PutMethodWebRequest(url, new ByteArrayInputStream(body.getBytes(com.openexchange.java.Charsets.UTF_8)), "text/javascript; charset=UTF-8");
        final WebResponse resp = webConv.getResponse(m);
        String text = resp.getText();
        return text;
    }

    protected JSONObject put(final WebConversation webConv, final String url, final String body) throws MalformedURLException, JSONException, IOException, SAXException {
        final JSONObject o = new JSONObject(putS(webConv,url, body));
        return o;
    }

    protected void putN(final WebConversation webConv, final String url, final String body) throws MalformedURLException, IOException, SAXException  {
        putS(webConv,url, body);
    }

    protected JSONArray putA(final WebConversation webConv, final String url, final String body) throws MalformedURLException, JSONException, IOException, SAXException  {
        final JSONArray a = new JSONArray(putS(webConv,url, body));
        return a;
    }

    protected String gS(final WebConversation webConv, final String url) throws MalformedURLException, IOException, SAXException {
        final GetMethodWebRequest m = new GetMethodWebRequest(url);
        final WebResponse resp = webConv.getResponse(m);
        return resp.getText();
    }

    protected JSONObject g(final WebConversation webConv, final String url) throws MalformedURLException, JSONException, IOException, SAXException {
        final JSONObject o = new JSONObject(gS(webConv, url));
        return o;
    }

    protected JSONArray gA(final WebConversation webConv, final String url) throws MalformedURLException, JSONException, IOException, SAXException {
        final JSONArray a = new JSONArray(gS(webConv, url));
        return a;
    }

    protected String pS(final WebConversation webConv, final String url, final Map<String,String> data) throws MalformedURLException, IOException, SAXException {
        final PostMethodWebRequest m = new PostMethodWebRequest(url);

        for(final String key : data.keySet()) {
            m.setParameter(key,data.get(key));
        }
        final WebResponse resp = webConv.getResponse(m);
        return resp.getText();
    }

    protected JSONObject p(final WebConversation webConv, final String url, final Map<String,String> data) throws MalformedURLException, JSONException, IOException, SAXException {
        final JSONObject o = new JSONObject(pS(webConv,url, data));
        return o;
    }

    protected JSONArray pA(final WebConversation webConv, final String url, final Map<String,String> data) throws MalformedURLException, JSONException, IOException, SAXException {
        return new JSONArray(pS(webConv,url, data));
    }

    protected Response gT(final WebConversation webConv, final String url) throws MalformedURLException, JSONException, IOException, SAXException {
        final String res = gS(webConv, url);
        if("".equals(res.trim())) {
            return null;
        }
        return Response.parse(res);

    }

    protected Response pT(final WebConversation webConv, final String url, final Map<String,String> data) throws MalformedURLException, JSONException, IOException, SAXException {
        final String res = pS(webConv,url, data);
        if("".equals(res.trim())) {
            return null;
        }
        return Response.parse(res);

    }

    protected Response putT(final WebConversation webConv, final String url, final String data) throws MalformedURLException, JSONException, IOException, SAXException  {
        final String res = putS(webConv,url, data);
        if("".equals(res.trim())) {
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
    protected Map<String,String> m(final String ...pairs){
        if(pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Must contain matching pairs");
        }

        final Map<String,String> m = new HashMap<String,String>();

        for(int i = 0; i < pairs.length; i++) {
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
}
