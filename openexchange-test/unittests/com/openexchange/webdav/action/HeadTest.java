
package com.openexchange.webdav.action;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.openexchange.webdav.protocol.WebdavPath;

public class HeadTest extends ActionTestCase {

    @Test
    public void testBasic() throws Exception {
        final WebdavPath INDEX_HTML_URL = testCollection.dup().append("index.html");

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);

        final WebdavAction action = new WebdavHeadAction();

        action.perform(req, res);

        final String content = getContent(INDEX_HTML_URL);

        assertEquals("", res.getResponseBodyAsString());
        assertEquals(content.getBytes(com.openexchange.java.Charsets.UTF_8).length, (int) new Integer(res.getHeader("content-length")));
        assertEquals("text/html", res.getHeader("content-type"));
        assertEquals("bytes", res.getHeader("Accept-Ranges"));
    }
}
