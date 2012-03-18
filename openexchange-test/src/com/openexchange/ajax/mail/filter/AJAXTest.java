package com.openexchange.ajax.mail.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import junit.framework.Assert;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.AJAXServlet;

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

    public static final String PROTOCOL = "http://";

    private static final String MAILFILTER_URL = "/ajax/mailfilter";

    private static final String LOGOUT_URL = "/ajax/login";

    @Test
    public void MailfilterconfigTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            mailfilterconfig(login, getHostname(), getUsername());
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilterlistTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            mailfilterlist(login, getHostname(), getUsername());
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilterdeleteTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":true,\"flags\":[],\"actioncmds\":[{\"into\":\"INBOX/Spam\",\"id\":\"move\"},{\"id\":\"stop\"}],\"id\":0,\"test\":{\"tests\":[{\"headers\":[\"from\"],\"values\":[\"zitate.at\"],\"comparison\":\"user\",\"id\":\"address\"},{\"headers\":[\"subject\"],\"values\":[\"Zitat des Tages\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":true,\"position\":0,\"flags\":[],\"actioncmds\":[{\"into\":\"default.INBOX/Spam\",\"id\":\"move\"},{\"id\":\"stop\"}],\"id\":0,\"test\":{\"tests\":[{\"headers\":[\"from\"],\"values\":[\"zitate.at\"],\"comparison\":\"user\",\"id\":\"address\"},{\"headers\":[\"subject\"],\"values\":[\"Zitat des Tages\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilterVacationTestWithOutSubject() throws MalformedURLException, IOException, SAXException, JSONException {
        WebconversationAndSessionID login;
        login = login();
        try {
            final JSONObject base = new JSONObject();
            base.put("rulename", "Abwesenheitsbenachrichtigung");
            base.put("active", Boolean.TRUE);
            base.append("flags", "vacation");
            final JSONObject test = new JSONObject();
            test.put("id", "true");
            base.put("test", test);
            final JSONObject action = new JSONObject();
            action.put("id", "vacation");
            action.put("days", 7);
            action.append("addresses", "dennis.sieben@open-xchange.com");
            action.put("text", "I'm out of office");
            base.append("actioncmds", action);

            final String newid = mailfilternew(login, getHostname(), getUsername(), base.toString(), null);
            System.out.println("Rule created with newid: " + newid);
            //mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
        } finally {
            // Log out in any case
            logout(login);
        }
    }

    @Test
    public void MailfilternewVacationPlainAtTheEndTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":true,\"text\":\"if true \\r\\n{\\r\\n    vacation :days 13 :addresses [ \\\"root@localhost\\\" , \\\"billg@microsoft.com\\\" ] :mime :subject \\\"Betreff\\\" \\\"Text\\r\\nText\\\" ;\\r\\n}\\r\\n\",\"errormsg\":\"\",\"flags\":[\"vacation\"],\"id\":3,\"rulename\":\"Vacation Notice\"}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewVacationPlainInBetweenTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":true,\"position\":0,\"text\":\"if true \\r\\n{\\r\\n    vacation :days 13 :addresses [ \\\"root@localhost\\\" , \\\"billg@microsoft.com\\\" ] :mime :subject \\\"Betreff\\\" \\\"Text\\r\\nText\\\" ;\\r\\n}\\r\\n\",\"errormsg\":\"\",\"flags\":[\"vacation\"],\"id\":3,\"rulename\":\"Vacation Notice\"}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewVacationTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"rulename\":\"Vacation Notice\",\"active\":true,\"flags\":[\"vacation\"],\"test\":{\"id\":\"true\"},\"actioncmds\":[{\"id\":\"vacation\",\"days\":13,\"addresses\":[\"root@localhost\",\"billg@microsoft.com\"],\"subject\":\"Betreff\",\"text\":\"Text\\u000aText\"}]}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
        } finally {
            logout(login);
        }
    }

    /**
     * This test case is used for testing the bug that the subject was written as text
     *
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws JSONException
     */
    @Test
    public void MailfilternewVacation2Test() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"rulename\":\"Vacation Notice\",\"active\":true,\"flags\":[\"vacation\"],\"test\":{\"id\":\"true\"},\"actioncmds\":[{\"id\":\"vacation\",\"days\":13,\"addresses\":[\"root@localhost\",\"billg@microsoft.com\"],\"subject\":\"Betreff\",\"text\":\"Text\\u000aText\"}],\"id\":5}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewVacation3Test() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test ="{\"rulename\":\"New x\",\"test\":{\"id\":\"header\",\"comparison\":\"contains\",\"values\":[\"\"],\"headers\":[\"X-Been-There\",\"X-Mailinglist\"]},\"actioncmds\":[{\"id\":\"redirect\",\"to\":\"xyz@bla.de\"}],\"flags\":[],\"active\":true}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewVacationDeactiveAtTheEndTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"rulename\":\"Vacation Notice\",\"active\":false,\"flags\":[\"vacation\"],\"test\":{\"id\":\"true\"},\"actioncmds\":[{\"id\":\"vacation\",\"days\":1,\"addresses\":[\"dsfa\"],\"subject\":\"123\",\"text\":\"123\"}]}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewVacationDeactiveInBetweenTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"rulename\":\"Vacation Notice\",\"position\":0,\"active\":false,\"flags\":[\"vacation\"],\"test\":{\"id\":\"true\"},\"actioncmds\":[{\"id\":\"vacation\",\"days\":1,\"addresses\":[\"dsfa\"],\"subject\":\"123\",\"text\":\"123\"}]}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
        } finally {
            logout(login);
        }
    }

    /**
     * This test is used to check the correct operation of the size test, this was dealt in bug 11519
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws JSONException
     */
    @Test
    public void MailfilternewSizeTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"rulename\":\"sizerule\",\"test\":{\"id\":\"size\",\"comparison\":\"over\",\"size\":88},\"actioncmds\":[{\"id\":\"keep\"}],\"flags\":[],\"active\":true}";
            final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
            System.out.println("Rule created with newid: " + newid);
        } finally {
            logout(login);
        }
    }

    /**
     * This test is used to check the correct operation of the currentdate test, this was dealt in bug 11519
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws JSONException
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
            test.append("tests", currentdate(1183759200000L, "ge"));
            test.append("tests", currentdate(1183759200000L, "le"));
            test.append("tests", currentdate(1183759200000L, "is"));
            base.put("test", test);
            final JSONObject action = new JSONObject();
//            action.put("id", "keep");
            action.put("id", "vacation");
            action.put("days", 7);
            action.append("addresses", "dennis.sieben@open-xchange.com");
            action.put("text", "I'm out of office");
            base.append("actioncmds", action);

            final String newid = mailfilternew(login, getHostname(), getUsername(), base.toString(), null);
            System.out.println("Rule created with newid: " + newid);
        } finally {
            logout(login);
        }
    }

    private JSONObject currentdate(long date, String comparison) throws JSONException {
        final JSONObject currentdate2 = new JSONObject();
        currentdate2.put("id", "currentdate");
        currentdate2.put("comparison", comparison);
        currentdate2.append("datevalue", date);
        currentdate2.put("datepart", "date");
        return currentdate2;
    }

    @Test
    public void MailfilternewTestMissingHeaders() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":true,\"position\":0,\"flags\":[],\"actioncmds\":[{\"into\":\"INBOX/Spam\",\"id\":\"move\"},{\"id\":\"stop\"}],\"id\":0,\"test\":{\"tests\":[{\"values\":[\"zitate.at\"],\"comparison\":\"user\",\"id\":\"address\"},{\"headers\":[\"subject\"],\"values\":[\"Zitat des Tages\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
            mailfilternew(login, getHostname(), getUsername(), test, "headers");
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilternewTestWithoutPosition() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            final String test = "{\"active\":true,\"flags\":[],\"actioncmds\":[{\"into\":\"INBOX/Spam\",\"id\":\"move\"},{\"id\":\"stop\"}],\"id\":0,\"test\":{\"tests\":[{\"headers\":[\"from\"],\"values\":[\"zitate.at\"],\"comparison\":\"user\",\"id\":\"address\"},{\"headers\":[\"subject\"],\"values\":[\"Zitat des Tages\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
            mailfilternew(login, getHostname(), getUsername(), test, null);
        } finally {
            logout(login);
        }
    }

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

    @Test
    public void MailfiltergetScriptTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            mailfiltergetScript(login, getHostname(), getUsername());
        } finally {
            logout(login);
        }
    }

    @Test
    public void MailfilterdeleteScriptTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebconversationAndSessionID login = login();
        try {
            mailfilterdeleteScript(login, getHostname(), getUsername());
        } finally {
            logout(login);
        }
    }

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

    private void mailfilterlist(final WebconversationAndSessionID conversation, final String hostname, final String username) throws MalformedURLException, IOException, SAXException, JSONException {
        String body;
        final WebRequest reqmailfilter = new GetMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL);
        reqmailfilter.setParameter("action", "list");
//        reqmailfilter.setParameter(AJAXServlet.PARAMETER_SESSION, conversation.);
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
        assertFalse(String.format(json.optString("error"), json.optJSONArray("error_params")), json.has("error"));
        System.out.println("Rules:");
        System.out.println("------");
        final JSONArray testJsonArray = json.getJSONArray("data");
        for (int i = 0; i < testJsonArray.length(); i++) {
            System.out.println(testJsonArray.getJSONObject(i));
            System.out.println("Test: " + testJsonArray.getJSONObject(i).getJSONObject("test"));
            System.out.println("--------------");
        }
    }

    private void mailfilterconfig(final WebconversationAndSessionID conversation, final String hostname, final String username) throws MalformedURLException, IOException, SAXException, JSONException {
        String body;
        final WebRequest reqmailfilter = new GetMethodWebRequest(PROTOCOL + hostname + MAILFILTER_URL);
        reqmailfilter.setParameter("action", "config");
//        reqmailfilter.setParameter(AJAXServlet.PARAMETER_SESSION, conversation.);
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
        System.out.println("Tests:");
        System.out.println("------");
        final JSONArray testJsonArray = json.getJSONObject("data").getJSONArray("tests");
        for (int i = 0; i < testJsonArray.length(); i++) {
            System.out.println(testJsonArray.getJSONObject(i));
        }
        System.out.println("Actioncommands:" + json.getJSONObject("data").getJSONArray("actioncommands"));
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
            assertTrue("No error params", json.has("error_params"));
            assertTrue("The given error string: " + errorfound + " was not found in the error params", json.optString("error_params").contains(errorfound));
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
}
