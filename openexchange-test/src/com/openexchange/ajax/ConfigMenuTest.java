package com.openexchange.ajax;

import java.util.Properties;
import java.util.Random;

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
        for (int i = 0; i < 10; i++) {
            String path = "";
            int pathLength = rand.nextInt(10) + 1;
            for (int j = 0; j < pathLength; j++) {
                path += RandomString.generateLetter(rand.nextInt(10) + 1) + "/";
            }
            path = path.substring(0, path.length() - 1);
            testStoreSetting(path, RandomString.generateLetter(rand.nextInt(10) + 1));
        }
    }
    
    private void testStoreSetting(final String path, final String value)
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
