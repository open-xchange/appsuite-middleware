
package com.openexchange.webdav.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

public class IfMatchTest extends ActionTestCase {

    private MockAction mockAction;
    private WebdavPath INDEX_HTML_URL;

    @Test
    public void testIfMatchWorks() throws Exception {
        final String etag = factory.resolveResource(INDEX_HTML_URL).getETag();

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If-Match", etag);

        final AbstractAction action = new WebdavIfMatchAction();

        action.setNext(mockAction);

        action.perform(req, res);

        assertTrue(mockAction.wasActivated());

    }

    @Test
    public void testIfMatchFails() throws Exception {

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If-Match", "i_don_t_match");

        final AbstractAction action = new WebdavIfMatchAction();

        mockAction.setActivated(false);
        try {
            action.setNext(mockAction);
            action.perform(req, res);
            fail("Expected precondition exception");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }
    }

    @Test
    public void testIfMatchMany() throws Exception {
        final String etag = factory.resolveResource(INDEX_HTML_URL).getETag();

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If-Match", "bla-1, bla-2, " + etag + ", bla, bla2, bla3");

        final AbstractAction action = new WebdavIfMatchAction();

        action.setNext(mockAction);

        action.perform(req, res);

        assertTrue(mockAction.wasActivated());
    }

    @Test
    public void testWildcardsIfMatchWorks() throws Exception {
        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If-Match", "*");

        final AbstractAction action = new WebdavIfMatchAction();

        action.setNext(mockAction);

        action.perform(req, res);

        assertTrue(mockAction.wasActivated());
    }

    @Test
    public void testWildcardsIfMatchFails() throws Exception {

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(new WebdavPath("doesntExist"));
        req.setHeader("If-Match", "*");

        final AbstractAction action = new WebdavIfMatchAction();

        mockAction.setActivated(false);
        try {
            action.setNext(mockAction);
            action.perform(req, res);
            fail("Expected precondition exception");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }
    }

    @Test
    public void testIfNoneMatchWorks() throws Exception {
        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If-None-Match", "i_don_t_match");

        final AbstractAction action = new WebdavIfMatchAction();

        action.setNext(mockAction);

        action.perform(req, res);

        assertTrue(mockAction.wasActivated());
    }

    @Test
    public void testIfNoneMatchFails() throws Exception {
        final String etag = factory.resolveResource(INDEX_HTML_URL).getETag();

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If-None-Match", etag);

        final AbstractAction action = new WebdavIfMatchAction();

        mockAction.setActivated(false);
        try {
            action.setNext(mockAction);
            action.perform(req, res);
            fail("Expected precondition exception");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }
    }

    @Test
    public void testIfNoneMatchMany() throws Exception {
        final String etag = factory.resolveResource(INDEX_HTML_URL).getETag();

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If-None-Match", "bla-1, bla-2, " + etag + ", bla, bla2, bla3");

        final AbstractAction action = new WebdavIfMatchAction();

        mockAction.setActivated(false);
        try {
            action.setNext(mockAction);
            action.perform(req, res);
            fail("Expected precondition exception");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }
    }

    @Test
    public void testWildcardsIfNonMatchWorks() throws Exception {
        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(new WebdavPath("doesntExist"));
        req.setHeader("If-None-Match", "*");

        final AbstractAction action = new WebdavIfMatchAction();

        mockAction.setActivated(false);

        action.setNext(mockAction);
        action.perform(req, res);

        assertTrue(mockAction.wasActivated());
    }

    @Test
    public void testWildcardsIfNonMatchFails() throws Exception {
        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If-None-Match", "*");

        final AbstractAction action = new WebdavIfMatchAction();

        mockAction.setActivated(false);
        try {
            action.setNext(mockAction);
            action.perform(req, res);
            fail("Expected precondition exception");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }
    }

    @Test
    public void testNoneSet() throws Exception {
        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);

        final AbstractAction action = new WebdavIfMatchAction();

        action.setNext(mockAction);

        action.perform(req, res);

        assertTrue(mockAction.wasActivated());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockAction = new MockAction();
        INDEX_HTML_URL = testCollection.dup().append("index.html");
    }
}
