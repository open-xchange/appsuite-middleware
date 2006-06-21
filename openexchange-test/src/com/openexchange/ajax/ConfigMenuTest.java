package com.openexchange.ajax;

import java.util.Random;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.tools.RandomString;

public class ConfigMenuTest extends AbstractAJAXTest {

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
        WebRequest req = new GetMethodWebRequest("http://" +
            getAJAXProperty("hostname") + "/ajax/config/" + path);
        req.setParameter("session", getSessionId());
        req.setHeaderField("Content-Type", "");
        WebResponse resp = getWebConversation().getResponse(req);
        assertEquals(200, resp.getResponseCode());
        return resp.getText();
    }

    private void storeSetting(final String path, final String value)
        throws Throwable {
        WebRequest req = new PostMethodWebRequest("http://" +
            getAJAXProperty("hostname") + "/ajax/config/" + path);
        req.setParameter("session", getSessionId());
        req.setParameter("value", value);
        WebResponse resp = getWebConversation().getResponse(req);
        assertEquals(200, resp.getResponseCode());
        assertEquals(0, resp.getContentLength());
    }
}
