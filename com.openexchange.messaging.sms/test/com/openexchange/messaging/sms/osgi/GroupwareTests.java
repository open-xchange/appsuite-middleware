package com.openexchange.messaging.sms.osgi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.cookies.CookieListener;
import com.meterware.httpunit.cookies.CookieProperties;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Login;
import com.openexchange.ajax.fields.LoginFields;

/**
 * TODO needs to be refactored to an interface test.
 */
public class GroupwareTests {

    private static final String AUTHPASSWORD = "secret";

    private static final String AUTHNAME = "sieve@777";

    private final static String HOSTNAME = "localhost";

    private static final String LOGIN_URL = "/ajax/login";

    private static final String LOGOUT_URL = LOGIN_URL;

    private static final String PROTOCOL = "http://";

    public class WebconversationAndSessionID {
        
        private final WebConversation webConversation;
        
        private final String sessionid;

        /**
         * @param webConversation
         * @param sessionid
         */
        public WebconversationAndSessionID(WebConversation webConversation, String sessionid) {
            this.sessionid = sessionid;
            this.webConversation = webConversation;
        }

        public final WebConversation getWebConversation() {
            return webConversation;
        }

        public final String getSessionid() {
            return sessionid;
        }
        
    }


    public WebconversationAndSessionID login() throws MalformedURLException, IOException, SAXException, JSONException {
        HttpUnitOptions.setDefaultCharacterSet("UTF-8");
        HttpUnitOptions.setScriptingEnabled(false);
        CookieProperties.setPathMatchingStrict(false);
        CookieProperties.addCookieListener(new CookieListener() {
            
            public void cookieRejected(String cookieName, int reason, String attribute) {
                System.out.println("Cookie: " + cookieName + " was rejected due to " + reason + " ; attribute " + attribute);
            }
        });
        final WebConversation conversation = new WebConversation();
        final String login = AUTHNAME;
        final String password = AUTHPASSWORD;
        System.out.println("Logging in.");
        final WebRequest req = new PostMethodWebRequest(PROTOCOL + getHostname() + LOGIN_URL);
        req.setParameter("action", "login");
        req.setParameter("name", login);
        req.setParameter("password", password);
        final WebResponse resp = conversation.getResponse(req);
        System.out.println(resp);
        for (final String cookie : resp.getNewCookieNames()) {
            System.out.println("Found new cookie: " + cookie);
        }
        for (final String cookie : conversation.getCookieNames()) {
            System.out.println("Found Cookie: " + cookie);
        }
        System.out.println("Session:" + conversation.getCookieValue("JSESSIONID"));
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK, resp.getResponseCode());
        final String body = resp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(json.optString("error"), json.has("error"));
        assertTrue("Session ID is missing: " + body, json.has(Login.PARAMETER_SESSION));
        assertTrue("Random is missing: " + body, json.has(LoginFields.RANDOM_PARAM));
        System.out.println(json);
        return new WebconversationAndSessionID(conversation, (String)json.get(Login.PARAMETER_SESSION));
    }
    
    @Test
    public void GetConfigTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            String body;
            final WebRequest reqmailfilter = new GetMethodWebRequest(PROTOCOL + getHostname() + ServletRegisterer.SERVLET_PATH);
            reqmailfilter.setParameter("action", "getconfig");
            setSessionParameter(login, reqmailfilter);
            final WebResponse mailfilterresp = login.getWebConversation().getResponse(reqmailfilter);
            body = mailfilterresp.getText();
            final JSONObject json;
            try {
                json = new JSONObject(body);
            } catch (final JSONException e) {
                System.out.println("Can't parse this body to JSON: \"" + body + '\"');
                throw e;
            }
            assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
            System.out.println(json);
        } finally {
            logout(login);
        }
    }
    
    @Test
    public void GetStatusTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            String body;
            final WebRequest reqmailfilter = new GetMethodWebRequest(PROTOCOL + getHostname() + ServletRegisterer.SERVLET_PATH);
            reqmailfilter.setParameter("action", "getstatus");
            setSessionParameter(login, reqmailfilter);
            final WebResponse mailfilterresp = login.getWebConversation().getResponse(reqmailfilter);
            body = mailfilterresp.getText();
            final JSONObject json;
            try {
                json = new JSONObject(body);
            } catch (final JSONException e) {
                System.out.println("Can't parse this body to JSON: \"" + body + '\"');
                throw e;
            }
            assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
            System.out.println(json);
        } finally {
            logout(login);
        }
    }
    
    @Test
    public void SendTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            String body;
            final JSONObject object = new JSONObject();
            object.put("from", "1293847");
            object.put("to", "1293847");
            object.put("body", "test");
            final byte[] bytes = object.toString().getBytes("UTF-8");
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            final WebRequest reqmailfilter = new PutMethodWebRequest(PROTOCOL + getHostname() + ServletRegisterer.SERVLET_PATH + "?action=send&session=" + login.getSessionid(), bais, "text/javascript; charset=UTF-8");
            final WebResponse mailfilterresp = login.getWebConversation().getResponse(reqmailfilter);
            body = mailfilterresp.getText();
            final JSONObject json;
            try {
                json = new JSONObject(body);
            } catch (final JSONException e) {
                System.out.println("Can't parse this body to JSON: \"" + body + '\"');
                throw e;
            }
            assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
            System.out.println(json);
        } finally {
            logout(login);
        }
    }
    
    private void config(final WebconversationAndSessionID conversation, final String hostname) throws MalformedURLException, IOException, SAXException, JSONException {
        String body;
        final WebRequest reqmailfilter = new GetMethodWebRequest(PROTOCOL + hostname + ServletRegisterer.SERVLET_PATH);
        reqmailfilter.setParameter("action", "getconfig");
        setSessionParameter(conversation, reqmailfilter);
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (final JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
        System.out.println(json);
    }


    private void logout(final WebconversationAndSessionID conversation) throws MalformedURLException, IOException, SAXException, JSONException {
        final WebRequest req = new GetMethodWebRequest(PROTOCOL + getHostname() + LOGOUT_URL);
        req.setParameter("action", "logout");
        req.setParameter("session", conversation.getSessionid());
        final WebResponse resp = conversation.getWebConversation().getResponse(req);
        Assert.assertEquals(200, resp.getResponseCode());
    }

    private void setSessionParameter(final WebconversationAndSessionID conversation, final WebRequest reqmailfilter) {
        reqmailfilter.setParameter(AJAXServlet.PARAMETER_SESSION, conversation.getSessionid());
    }

    protected String getHostname() {
        return HOSTNAME;
    }

}
