
package com.openexchange.webdav.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;

public class NotExistTest extends ActionTestCase {

    private MockAction mockAction;

    @Test
    public void testNotExists() throws Exception {
        final WebdavPath NOT_EXIST_URL = new WebdavPath("notExists.txt");

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(NOT_EXIST_URL);

        final AbstractAction action = new WebdavExistsAction();
        action.setNext(mockAction);

        try {
            action.perform(req, res);
            fail("Expected 404 Not Found");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_NOT_FOUND, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }
    }

    @Test
    public void testExists() throws Exception {
        final WebdavPath INDEX_HTML_URL = testCollection.dup().append("index.html");

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);

        final AbstractAction action = new WebdavExistsAction();
        action.setNext(mockAction);

        action.perform(req, res);
        assertTrue(mockAction.wasActivated());
    }

    @Test
    public void testLockNullExist() throws Exception {
        final WebdavPath LOCK_NULL = createLockNull();

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(LOCK_NULL);

        final WebdavExistsAction action = new WebdavExistsAction();
        action.setTolerateLockNull(true);

        action.setNext(mockAction);

        action.perform(req, res);
        assertTrue(mockAction.wasActivated());
    }

    @Test
    public void testLockNullDontExist() throws Exception {
        final WebdavPath LOCK_NULL = createLockNull();
        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(LOCK_NULL);

        final WebdavExistsAction action = new WebdavExistsAction();

        action.setNext(mockAction);
        try {
            action.perform(req, res);
            fail("Expected 404 Not Found");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_NOT_FOUND, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }
    }

    // Bug 9845
    @Test
    public void testNotFoundShouldIncludePayload() throws OXException {
        final WebdavPath NOT_EXIST_URL = new WebdavPath("notExists.txt");

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(NOT_EXIST_URL);

        final AbstractAction action = new WebdavExistsAction();
        action.setNext(mockAction);

        try {
            action.perform(req, res);
            fail("Expected 404 Not Found");
        } catch (final WebdavProtocolException x) {
            assertNotNull(res.getResponseBytes());
            assertFalse(0 == res.getResponseBytes().length);
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.mockAction = new MockAction();
    }

    private WebdavPath createLockNull() throws OXException {
        final WebdavPath LOCK_NULL = testCollection.dup().append("lock.txt");
        final WebdavLock lock = new WebdavLock();
        lock.setDepth(0);
        lock.setOwner("me");
        lock.setScope(WebdavLock.Scope.EXCLUSIVE_LITERAL);
        lock.setTimeout(WebdavLock.NEVER);
        lock.setType(WebdavLock.Type.WRITE_LITERAL);
        factory.resolveResource(LOCK_NULL).lock(lock);
        return LOCK_NULL;
    }
}
