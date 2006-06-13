package com.openexchange.ajax;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

import junit.framework.TestCase;

public class LoginTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public static String getLogin(final WebConversation wc,
        final String hostname, final String login, final String password)
        throws MalformedURLException, IOException, SAXException, JSONException {
        WebRequest req = new GetMethodWebRequest("http://" + hostname
            + "/ajax/login");
        req.setParameter("name", login);
        req.setParameter("password", password);
        req.setHeaderField("Content-Type", "");
        WebResponse resp = wc.getResponse(req);
        JSONObject jslogin = new JSONObject(resp.getText());
        String sessionId = jslogin.getString("session");
        return sessionId;
    }
    
}
