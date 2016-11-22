
package com.openexchange.webdav.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public class MkcolTest extends ActionTestCase {

    @Test
    public void testCreateCollection() throws Exception {
        final WebdavPath NEW_COLLECTION = testCollection.dup().append("newCollection");

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(NEW_COLLECTION);

        final WebdavAction action = new WebdavMkcolAction();
        action.perform(req, res);

        assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());

        final WebdavResource resource = factory.resolveResource(NEW_COLLECTION);
        assertTrue(resource.exists() && resource.isCollection());

    }

    @Test
    public void testInvalidParent() throws Exception {
        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(new WebdavPath("doesntExist/lalala"));

        final WebdavAction action = new WebdavMkcolAction();

        try {
            action.perform(req, res);
            fail("Expected 409 CONFLICT or 412 PRECONDITION FAILED");
        } catch (final WebdavProtocolException e) {
            assertTrue("" + e.getStatus(), HttpServletResponse.SC_CONFLICT == e.getStatus() || HttpServletResponse.SC_PRECONDITION_FAILED == e.getStatus());
        }

    }
}
