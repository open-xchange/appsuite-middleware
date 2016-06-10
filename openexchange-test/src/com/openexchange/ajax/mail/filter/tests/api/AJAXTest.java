
package com.openexchange.ajax.mail.filter.tests.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.mail.filter.tests.bug.Bug11519Test;

public abstract class AJAXTest {

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

    private enum CurrentDate {
        date, time, weekday
    };

    public static final String PROTOCOL = "http://";

    private static final String MAILFILTER_URL = "/ajax/mailfilter";

    private static final String LOGOUT_URL = "/ajax/login";

    /**
     * This test is used to check the correct operation of the currentdate test, this was dealt in bug 11519
     * 
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws JSONException
     * @deprecated Moved to {@link Bug11519Test#testBug11519()}
     */
    @Test
    public void MailfilternewCurrentDateTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final JSONObject base = new JSONObject();
            base.put("rulename", "sizerule");
            base.put("active", Boolean.TRUE);
            base.append("flags", "vacation");
            final JSONObject test = new JSONObject();
            test.put("id", "allof");
            test.append("tests", currentdate(1183759200000L, "ge", CurrentDate.date));
            test.append("tests", currentdate(1183759200000L, "le", CurrentDate.date));
            test.append("tests", currentdate(1183759200000L, "is", CurrentDate.date));
            base.put("test", test);
            final JSONObject action = new JSONObject();
            //            action.put("id", "keep");
            action.put("id", "vacation");
            action.put("days", 7);
            action.append("addresses", "dennis.sieben@open-xchange.com");
            action.put("text", "I'm out of office");
            base.append("actioncmds", action);

            System.out.println(base.toString());
            final String newid = mailfilternew(login, getHostname(), getUsername(), base.toString(), null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    /**
     * @deprecated Moved to {@link ReorderTest#testReorder()}
     */
    @Test
    public void MailfilterreorderTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "[5,7,8]";
            mailfilterreorder(login, getHostname(), getUsername(), test);
        } finally {
            logout(login);
        }
    }

    /**
     * @deprecated Move to {@link UpdateTest}
     */
    @Test
    public void MailfilterupdateTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":true,\"position\":0,\"flags\":[],\"id\":7,\"rulename\":\"testrule\"}";
            mailfilterupdate(login, getHostname(), getUsername(), test);
        } finally {
            logout(login);
        }
    }

    /**
     * @deprecated Move to {@link UpdateTest}
     */
    @Test
    public void MailfilterupdateTest2() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":false,\"position\":0,\"flags\":[],\"id\":7,\"test\":{\"tests\":[{\"headers\":[\"from\"],\"values\":[\"zitate.at\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
            mailfilterupdate(login, getHostname(), getUsername(), test);
        } finally {
            logout(login);
        }
    }

    /**
     * @deprecated Moved to {@link AuxiliaryAPITest#testGetScript()}
     */
    @Test
    public void MailfiltergetScriptTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            mailfiltergetScript(login, getHostname(), getUsername());
        } finally {
            logout(login);
        }
    }

    /**
     * @deprecated Moved to {@link AuxiliaryAPITest#testDeleteScript()}
     */
    @Test
    public void MailfilterdeleteScriptTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            mailfilterdeleteScript(login, getHostname(), getUsername());
        } finally {
            logout(login);
        }
    }

    @Test
    public void testWeekDayField() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final JSONObject rule = new JSONObject();
            rule.put("rulename", "weekday rule");
            rule.put("active", Boolean.TRUE);
            rule.append("flags", "vacation");
            final JSONObject test = new JSONObject();
            test.put("id", "allof");
            test.append("tests", currentdate(3, "is", CurrentDate.weekday));
            rule.put("test", test);
            final JSONObject action = new JSONObject();
            action.put("id", "vacation");
            action.put("days", 7);
            action.append("addresses", "foo@invalid.tld");
            action.put("text", "I'm out of office");
            rule.append("actioncmds", action);

            final String newid = mailfilternew(login, getHostname(), getUsername(), rule.toString(), null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    @Test
    public void testDateField() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final JSONObject rule = new JSONObject();
            rule.put("rulename", "time rule");
            rule.put("active", Boolean.TRUE);
            rule.append("flags", "vacation");
            final JSONObject test = new JSONObject();
            test.put("id", "allof");
            test.append("tests", currentdate(3627279000000L, "is", CurrentDate.time));
            rule.put("test", test);
            final JSONObject action = new JSONObject();
            action.put("id", "vacation");
            action.put("days", 7);
            action.append("addresses", "foo@invalid.tld");
            action.put("text", "I'm out of office");
            rule.append("actioncmds", action);

            final String newid = mailfilternew(login, getHostname(), getUsername(), rule.toString(), null);
            System.out.println("Rule created with newid: " + newid);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    ///////////////////////////////////////////////// HELPERS /////////////////////////////////////////////////

    protected abstract String getHostname();

    protected abstract String getUsername();

    protected abstract WebconversationAndSessionID login() throws MalformedURLException, IOException, SAXException, JSONException;

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

    /**
     * @param conversation
     * @param hostname
     * @param username
     * @param number The id of the rule which should be deleted
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws JSONException
     */
    private void mailfilterdelete(final WebconversationAndSessionID conversation, final String hostname, final String username, int number) throws MalformedURLException, IOException, SAXException, JSONException {
        final JSONObject object = new JSONObject();
        object.put("id", number);
        final byte[] bytes = object.toString().getBytes(com.openexchange.java.Charsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest reqmailfilter;
        if (null != username) {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=delete&session=" + conversation.getSessionid() + "&username=" + username, bais, "text/javascript; charset=UTF-8");
        } else {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=delete&session=" + conversation.getSessionid(), bais, "text/javascript; charset=UTF-8");
        }
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        Assert.assertEquals(200, mailfilterresp.getResponseCode());
    }

    private String mailfilternew(final WebconversationAndSessionID conversation, final String hostname, final String username, String jsonString, String errorfound) throws MalformedURLException, IOException, SAXException, JSONException {
        final byte[] bytes = jsonString.getBytes(com.openexchange.java.Charsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest reqmailfilter;
        if (null != username) {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=new&username=" + username + "&session=" + conversation.getSessionid(), bais, "text/javascript; charset=UTF-8");
        } else {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=new&session=" + conversation.getSessionid(), bais, "text/javascript; charset=UTF-8");
        }
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        final String body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        if (null != errorfound) {
            assertTrue("No error desc", json.has("error_desc"));
            assertTrue("The given error string: " + errorfound + " was not found in the error desc", json.optString("error_desc").contains(errorfound));
            return null;
        } else {
            assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
            return json.getString("data");
        }
    }

    private void mailfilterreorder(final WebconversationAndSessionID conversation, final String hostname, final String username, String jsonArray) throws MalformedURLException, IOException, SAXException, JSONException {
        final byte[] bytes = jsonArray.getBytes(com.openexchange.java.Charsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest reqmailfilter;
        if (null != username) {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=new&username=" + username, bais, "text/javascript; charset=UTF-8");
        } else {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=reorder", bais, "text/javascript; charset=UTF-8");
        }
        setSessionParameter(conversation, reqmailfilter);
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        final String body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
        System.out.println(json);
    }

    private void mailfilterupdate(final WebconversationAndSessionID conversation, final String hostname, final String username, String test) throws MalformedURLException, IOException, SAXException, JSONException {
        final byte[] bytes = test.getBytes(com.openexchange.java.Charsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest reqmailfilter;
        if (null != username) {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=update&username=" + username, bais, "text/javascript; charset=UTF-8");
        } else {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL + "?action=update", bais, "text/javascript; charset=UTF-8");
        }
        setSessionParameter(conversation, reqmailfilter);
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        final String body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
        System.out.println(json);
    }

    private void mailfiltergetScript(final WebconversationAndSessionID conversation, final String hostname, final String username) throws MalformedURLException, IOException, SAXException, JSONException {
        String body;
        final WebRequest reqmailfilter = new GetMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL);
        reqmailfilter.setParameter("action", "getscript");
        if (null != username) {
            reqmailfilter.setParameter("username", username);
        }
        //        reqmailfilter.setParameter("flag", "AdminFlag");
        setSessionParameter(conversation, reqmailfilter);
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
        System.out.println(json);
    }

    private void mailfilterdeleteScript(final WebconversationAndSessionID conversation, final String hostname, final String username) throws MalformedURLException, IOException, SAXException, JSONException {
        String body;
        final WebRequest reqmailfilter = new GetMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL);
        reqmailfilter.setParameter("action", "deletescript");
        if (null != username) {
            reqmailfilter.setParameter("username", username);
        }
        //        reqmailfilter.setParameter("flag", "AdminFlag");
        setSessionParameter(conversation, reqmailfilter);
        final WebResponse mailfilterresp = conversation.getWebConversation().getResponse(reqmailfilter);
        body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(String.format(json.optString("error"), json.opt("error_params")), json.has("error"));
        System.out.println(json);
    }

    private JSONObject currentdate(long date, String comparison, CurrentDate cd) throws JSONException {
        final JSONObject currentdate2 = new JSONObject();
        currentdate2.put("id", "currentdate");
        currentdate2.put("comparison", comparison);
        currentdate2.append("datevalue", date);
        currentdate2.put("datepart", cd.toString());
        return currentdate2;
    }
}
