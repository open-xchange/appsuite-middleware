
package com.openexchange.webdav.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

//Bug 6104
public class MaxUploadSizeActionTest extends ActionTestCase {

    private MockAction mockAction;
    private WebdavPath INDEX_HTML_URL;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockAction = new MockAction();
        INDEX_HTML_URL = testCollection.dup().append("index_new.html");
    }

    @Test
    public void testPassThru() throws OXException {
        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("content-length", "9");

        final WebdavMaxUploadSizeAction action = new TestMaxUploadSizeAction();
        action.setNext(mockAction);

        action.perform(req, res);

        assertTrue(mockAction.wasActivated());
    }

    @Test
    public void testDeny() throws OXException {
        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("content-length", "11");

        final WebdavMaxUploadSizeAction action = new TestMaxUploadSizeAction();
        action.setNext(mockAction);

        try {
            action.perform(req, res);
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, x.getStatus());
        }

        assertFalse(mockAction.wasActivated());
    }

    public static final class TestMaxUploadSizeAction extends WebdavMaxUploadSizeAction {

        @Override
        public boolean fits(final WebdavRequest req) {
            return Long.valueOf(req.getHeader("content-length")) < 10;
        }
    }
}
