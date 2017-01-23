
package com.openexchange.webdav.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public class PutTest extends ActionTestCase {

    private WebdavPath INDEX_HTML_URL = null;
    private WebdavPath INDEX23_HTML_URL = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        INDEX_HTML_URL = testCollection.dup().append("index.html");
        INDEX23_HTML_URL = testCollection.dup().append("index23.html");
    }

    @Test
    public void testBasic() throws Exception {

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);

        final String content = "<html><head /><body>The New, Better Index</body></html>";
        req.setBodyAsString(content);
        req.setHeader("content-length", ((Integer) content.getBytes(com.openexchange.java.Charsets.UTF_8).length).toString());
        req.setHeader("content-type", "text/html");

        final WebdavAction action = new WebdavPutAction();

        action.perform(req, res);

        assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());

        final WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
        assertEquals(new Long(content.getBytes(com.openexchange.java.Charsets.UTF_8).length), resource.getLength());
        assertEquals("text/html", resource.getContentType());
        assertEquals(content, getContent(INDEX_HTML_URL));
    }

    @Test
    public void testCreate() throws Exception {
        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX23_HTML_URL);

        final String content = "<html><head /><body>The New, Better Index</body></html>";
        req.setBodyAsString(content);
        req.setHeader("content-length", ((Integer) content.getBytes(com.openexchange.java.Charsets.UTF_8).length).toString());
        req.setHeader("content-type", "text/html");

        final WebdavAction action = new WebdavPutAction();

        action.perform(req, res);

        assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());

        final WebdavResource resource = factory.resolveResource(INDEX23_HTML_URL);
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertEquals(resource.getLength(), new Long(content.getBytes(com.openexchange.java.Charsets.UTF_8).length));
        assertEquals("text/html", resource.getContentType());
        assertEquals(content, getContent(INDEX23_HTML_URL));
    }

    // Bug 6104
    @Test
    public void testTooLarge() throws Exception {
        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX23_HTML_URL);

        final String content = "<html><head /><body>The New, Better Index</body></html>";
        req.setBodyAsString(content);
        req.setHeader("content-length", ((Integer) content.getBytes(com.openexchange.java.Charsets.UTF_8).length).toString());
        req.setHeader("content-type", "text/html");

        final WebdavAction action = new WebdavPutAction() {

            @Override
            public long getMaxSize() {
                return 1;
            }
        };

        try {
            action.perform(req, res);
            assertFalse("Could upload", true);
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, x.getStatus());
        }
    }

    @Test
    public void testInvalidParent() throws Exception {

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(new WebdavPath("notExists/lalala"));

        final String content = "<html><head /><body>The New, Better Index</body></html>";
        req.setBodyAsString(content);
        req.setHeader("content-length", ((Integer) content.getBytes(com.openexchange.java.Charsets.UTF_8).length).toString());
        req.setHeader("content-type", "text/html");

        final WebdavAction action = new WebdavPutAction();

        try {
            action.perform(req, res);
            fail("Expected 409 CONFLICT");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_CONFLICT, x.getStatus());
        }

    }
}
