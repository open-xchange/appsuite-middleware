
package com.openexchange.webdav.action;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.webdav.protocol.WebdavPath;

public class TraceTest extends ActionTestCase {

    @Test
    public void testBasic() throws Exception {
        final WebdavPath INDEX_HTML_URL = testCollection.dup().append("index.html");

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setHeader("header1", "value1");
        req.setHeader("header2", "value2");
        req.setHeader("header3", "value3");
        req.setHeader("header4", "value4");

        req.setUrl(INDEX_HTML_URL);

        final WebdavAction action = new WebdavTraceAction();

        action.perform(req, res);

        assertEquals("value1", res.getHeader("header1"));
        assertEquals("value2", res.getHeader("header2"));
        assertEquals("value3", res.getHeader("header3"));
        assertEquals("value4", res.getHeader("header4"));

    }
}
