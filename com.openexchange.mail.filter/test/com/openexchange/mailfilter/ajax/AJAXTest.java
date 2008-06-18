package com.openexchange.mailfilter.ajax;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public abstract class AJAXTest {

    public static final String PROTOCOL = "http://";
    
    private static final String MAILFILTER_ADMIN = "/ajax/mailfilter";
    
    @Test
    public void MailfilterconfigTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebConversation login = login();
        mailfilterconfig(login, getHostname(), getUsername());
    }

    @Test
    public void MailfilterlistTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebConversation login = login();
        mailfilterlist(login, getHostname(), getUsername());
    }
    
    @Test
    public void MailfilternewTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebConversation login = login();
        final String test = "{\"active\":true,\"position\":0,\"flags\":[],\"actioncmds\":[{\"into\":\"INBOX/Spam\",\"id\":\"move\"},{\"id\":\"stop\"}],\"id\":0,\"test\":{\"tests\":[{\"headers\":[\"from\"],\"values\":[\"zitate.at\"],\"comparison\":\"user\",\"id\":\"address\"},{\"headers\":[\"subject\"],\"values\":[\"Zitat des Tages\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
        final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
        System.out.println("Rule created with newid: " + newid);
    }
    
    @Test
    public void MailfilterdeleteTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebConversation login = login();
        final String test = "{\"active\":true,\"flags\":[],\"actioncmds\":[{\"into\":\"INBOX/Spam\",\"id\":\"move\"},{\"id\":\"stop\"}],\"id\":0,\"test\":{\"tests\":[{\"headers\":[\"from\"],\"values\":[\"zitate.at\"],\"comparison\":\"user\",\"id\":\"address\"},{\"headers\":[\"subject\"],\"values\":[\"Zitat des Tages\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
        final String newid = mailfilternew(login, getHostname(), getUsername(), test, null);
        mailfilterdelete(login, getHostname(), getUsername(), Integer.parseInt(newid));
    }
    
    @Test
    public void MailfilternewTestMissingHeaders() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebConversation login = login();
        final String test = "{\"active\":true,\"position\":0,\"flags\":[],\"actioncmds\":[{\"into\":\"INBOX/Spam\",\"id\":\"move\"},{\"id\":\"stop\"}],\"id\":0,\"test\":{\"tests\":[{\"values\":[\"zitate.at\"],\"comparison\":\"user\",\"id\":\"address\"},{\"headers\":[\"subject\"],\"values\":[\"Zitat des Tages\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
        mailfilternew(login, getHostname(), getUsername(), test, "headers");
    }
    
    @Test
    public void MailfilternewTestWithoutPosition() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebConversation login = login();
        final String test = "{\"active\":true,\"flags\":[],\"actioncmds\":[{\"into\":\"INBOX/Spam\",\"id\":\"move\"},{\"id\":\"stop\"}],\"id\":0,\"test\":{\"tests\":[{\"headers\":[\"from\"],\"values\":[\"zitate.at\"],\"comparison\":\"user\",\"id\":\"address\"},{\"headers\":[\"subject\"],\"values\":[\"Zitat des Tages\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
        mailfilternew(login, getHostname(), getUsername(), test, null);
    }
    
    @Test
    public void MailfilterreorderTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebConversation login = login();
        final String test = "[5,7,8]";
        mailfilterreorder(login, getHostname(), getUsername(), test);
    }
    
    @Test
    public void MailfilterupdateTest() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebConversation login = login();
        final String test = "{\"active\":true,\"position\":0,\"flags\":[],\"id\":7,\"rulename\":\"testrule\"}";
        mailfilterupdate(login, getHostname(), getUsername(), test);
    }
    
    @Test
    public void MailfilterupdateTest2() throws MalformedURLException, IOException, SAXException, JSONException {
        final WebConversation login = login();
        final String test = "{\"active\":false,\"position\":0,\"flags\":[],\"id\":7,\"test\":{\"tests\":[{\"headers\":[\"from\"],\"values\":[\"zitate.at\"],\"comparison\":\"contains\",\"id\":\"header\"}],\"id\":\"allof\"},\"rulename\":\"\"}";
        mailfilterupdate(login, getHostname(), getUsername(), test);
    }
    
    protected abstract String getHostname();
    
    protected abstract String getUsername();
    
    protected abstract WebConversation login() throws MalformedURLException, IOException, SAXException, JSONException;

    private void mailfilterconfig(final WebConversation conversation, final String hostname, final String username) throws MalformedURLException, IOException, SAXException, JSONException {
        String body;
        final WebRequest reqmailfilter = new GetMethodWebRequest(PROTOCOL + hostname + MAILFILTER_ADMIN);
        reqmailfilter.setParameter("action", "config");
        if (null != username) {
            reqmailfilter.setParameter("username", username);
        }
        final WebResponse mailfilterresp = conversation.getResponse(reqmailfilter);
        body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(json.optString("error"), json.has("error"));
        System.out.println(json);
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
    private void mailfilterdelete(final WebConversation conversation, final String hostname, final String username, int number) throws MalformedURLException, IOException, SAXException, JSONException {
        String body;
        final JSONObject object = new JSONObject();
        object.put("id", number);
        final byte[] bytes = object.toString().getBytes("UTF-8");
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest reqmailfilter;
        if (null != username) {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_ADMIN + "?action=delete&username=" + username, bais, "text/javascript; charset=UTF-8");
        } else {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_ADMIN + "?action=delete", bais, "text/javascript; charset=UTF-8");
        }
        final WebResponse mailfilterresp = conversation.getResponse(reqmailfilter);
        body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(json.optString("error"), json.has("error"));
        System.out.println(json);
    }

    private void mailfilterlist(final WebConversation conversation, final String hostname, final String username) throws MalformedURLException, IOException, SAXException, JSONException {
        String body;
        final WebRequest reqmailfilter = new GetMethodWebRequest(PROTOCOL + hostname + MAILFILTER_ADMIN);
        reqmailfilter.setParameter("action", "list");
        if (null != username) {
            reqmailfilter.setParameter("username", username);
        }
//        reqmailfilter.setParameter("flag", "AdminFlag");
        final WebResponse mailfilterresp = conversation.getResponse(reqmailfilter);
        body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(json.optString("error"), json.has("error"));
        System.out.println(json);
    }

    private String mailfilternew(final WebConversation conversation, final String hostname, final String username, String jsonString, String errorfound) throws MalformedURLException, IOException, SAXException, JSONException {
        final byte[] bytes = jsonString.getBytes("UTF-8");
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest reqmailfilter;
        if (null != username) {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_ADMIN + "?action=new&username=" + username, bais, "text/javascript; charset=UTF-8");
        } else {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_ADMIN + "?action=new", bais, "text/javascript; charset=UTF-8");
        }
        final WebResponse mailfilterresp = conversation.getResponse(reqmailfilter);
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
            assertFalse(json.optString("error"), json.has("error"));
            return json.getString("data");
        }
    }

    private void mailfilterreorder(final WebConversation conversation, final String hostname, final String username, String jsonArray) throws MalformedURLException, IOException, SAXException, JSONException {
        final byte[] bytes = jsonArray.getBytes("UTF-8");
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest reqmailfilter;
        if (null != username) {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_ADMIN + "?action=new&username=" + username, bais, "text/javascript; charset=UTF-8");
        } else {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_ADMIN + "?action=reorder", bais, "text/javascript; charset=UTF-8");
        }
        final WebResponse mailfilterresp = conversation.getResponse(reqmailfilter);
        final String body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(json.optString("error"), json.has("error"));
        System.out.println(json);
    }
    
    private void mailfilterupdate(final WebConversation conversation, final String hostname, final String username, String test) throws MalformedURLException, IOException, SAXException, JSONException {
        final byte[] bytes = test.getBytes("UTF-8");
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        final WebRequest reqmailfilter;
        if (null != username) {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_ADMIN + "?action=update&username=" + username, bais, "text/javascript; charset=UTF-8");
        } else {
            reqmailfilter = new PutMethodWebRequest(PROTOCOL + hostname + MAILFILTER_ADMIN + "?action=update", bais, "text/javascript; charset=UTF-8");
        }
        final WebResponse mailfilterresp = conversation.getResponse(reqmailfilter);
        final String body = mailfilterresp.getText();
        final JSONObject json;
        try {
            json = new JSONObject(body);
        } catch (JSONException e) {
            System.out.println("Can't parse this body to JSON: \"" + body + '\"');
            throw e;
        }
        assertFalse(json.optString("error"), json.has("error"));
        System.out.println(json);
    }
    
}
