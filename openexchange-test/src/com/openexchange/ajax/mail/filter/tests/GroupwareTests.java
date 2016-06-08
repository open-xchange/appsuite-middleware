package com.openexchange.ajax.mail.filter.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.cookies.CookieListener;
import com.meterware.httpunit.cookies.CookieProperties;
import com.openexchange.ajax.LoginServlet;

public class GroupwareTests extends AJAXTest {

//    private static final String AUTHPASSWORD = "oxtest41";
    private static final String AUTHPASSWORD = "secret";

//    private static final String AUTHNAME = "user2@oxtest41.de";
    private static final String AUTHNAME = "olox20@premium";

    private final static String HOSTNAME = "localhost";

    private static final String LOGIN_URL = "/ajax/login";

    @Override
    public WebconversationAndSessionID login() throws MalformedURLException, IOException, SAXException, JSONException {
        HttpUnitOptions.setDefaultCharacterSet("UTF-8");
        HttpUnitOptions.setScriptingEnabled(false);
        CookieProperties.setPathMatchingStrict(false);
        CookieProperties.addCookieListener(new CookieListener() {

            @Override
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
        assertTrue("Session ID is missing: " + body, json.has(LoginServlet.PARAMETER_SESSION));
        //assertTrue("Random is missing: " + body, json.has(LoginFields.RANDOM_PARAM));
        System.out.println(json);
        return new WebconversationAndSessionID(conversation, (String)json.get(LoginServlet.PARAMETER_SESSION));
    }

    @Override
    protected String getHostname() {
        return HOSTNAME;
    }

    @Override
    protected String getUsername() {
        return null;
    }
}
