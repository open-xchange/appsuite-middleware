package com.openexchange.ajax;

import java.util.Properties;
import java.util.Random;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.groupware.Init;
import com.openexchange.tools.RandomString;

import junit.framework.TestCase;

public class ConfigMenuTest extends TestCase {

    private WebConversation wc = null;

    private String sessionId = null;

    private Properties ajaxProps = null;

    protected void setUp() throws Exception {
        super.setUp();
        ajaxProps = Init.getAJAXProperties();
        wc = new WebConversation();
        sessionId = LoginTest.getLogin(wc, ajaxProps.getProperty("hostname"),
            ajaxProps.getProperty("login"), ajaxProps.getProperty("password"));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testStoreExampleSetting() throws Throwable {
        Random rand = new Random(System.currentTimeMillis());
        String[] path = new String[10];
        String[] value = new String[10];
        for (int i = 0; i < 10; i++) {
            path[i] = "";
            int pathLength = rand.nextInt(10) + 1;
            for (int j = 0; j < pathLength; j++) {
                path[i] += RandomString.generateLetter(rand.nextInt(10) + 1) + "/";
            }
            path[i] = path[i].substring(0, path[i].length() - 1);
            value[i] = RandomString.generateLetter(rand.nextInt(10) + 1);
        }
        for (int i = 0; i < 10; i++) {
            storeSetting(path[i], value[i]);
        }
        for (int i = 0; i < 10; i++) {
            assertEquals(value[i], readSetting(path[i]));
        }
    }
    
    private String readSetting(final String path) throws Throwable {
        WebRequest req = new GetMethodWebRequest("http://" + ajaxProps
            .getProperty("hostname") + "/ajax/config/" + path);
        req.setParameter("session", sessionId);
        req.setHeaderField("Content-Type", "");
        WebResponse resp = wc.getResponse(req);
        assertEquals(200, resp.getResponseCode());
        return resp.getText();
    }

    private void storeSetting(final String path, final String value)
        throws Throwable {
        WebRequest req = new PostMethodWebRequest("http://" + ajaxProps
            .getProperty("hostname") + "/ajax/config/" + path);
        req.setParameter("session", sessionId);
        req.setParameter("value", value);
        WebResponse resp = wc.getResponse(req);
        assertEquals(200, resp.getResponseCode());
        assertEquals(0, resp.getContentLength());
    }
}
