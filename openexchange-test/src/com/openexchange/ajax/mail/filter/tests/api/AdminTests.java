package com.openexchange.ajax.mail.filter.tests.api;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class AdminTests extends AJAXTest {

    private static final String AUTHPASSWORD = "secret";

    private static final String AUTHNAME = "oxadmin";

    private static final String LOGIN_URL_ADMIN = "/oxadmin/login";

    private static final String USER_ADMIN = "/oxadmin/users";

    private static final String USERNAME = "test3.test3";

    @Override
    protected String getHostname() {
        return "192.168.73.131";
    }

    @Override
    protected String getUsername() {
        return USERNAME;
    }

    @Override
    protected WebconversationAndSessionID login() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebConversation conversation = new WebConversation();
        final String login = AUTHNAME;
        final String password = AUTHPASSWORD;
        System.out.println("Logging in.");
        final WebRequest req = new PostMethodWebRequest(PROTOCOL + getHostname() + LOGIN_URL_ADMIN);
        req.setParameter("action", "login");
        req.setParameter("loginUsername", login);
        req.setParameter("loginPassword", password);
        final WebResponse resp = conversation.getResponse(req);
        System.out.println("Session:" + conversation.getCookieValue("JSESSIONID"));
        assertEquals("Response code is not okay.", HttpServletResponse.SC_OK, resp.getResponseCode());
        String body = resp.getText();
        JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        System.out.println(json);

        final WebRequest requsers = new GetMethodWebRequest(PROTOCOL + getHostname() + USER_ADMIN);
        requsers.setParameter("action", "all");
        final WebResponse userresp = conversation.getResponse(requsers);
        body = userresp.getText();
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        System.out.println(json);

        return new WebconversationAndSessionID(conversation, null);
    }

}
