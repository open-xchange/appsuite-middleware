
package com.openexchange.webdav.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavLock.Scope;
import com.openexchange.webdav.protocol.WebdavLock.Type;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public class IfTest extends ActionTestCase {

    private MockAction mockAction;
    private WebdavPath INDEX_HTML_URL;

    @Test
    public void testETag() throws Exception {
        final String etag = factory.resolveResource(INDEX_HTML_URL).getETag();

        MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If", "([" + etag + "])");

        final WebdavIfAction action = new WebdavIfAction();
        action.setDefaultDepth(0);
        action.setNext(mockAction);

        action.perform(req, res);

        assertTrue(mockAction.wasActivated());

        req = new MockWebdavRequest(factory, "http://localhost/");
        res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If", "([i_don_t_match])");
        mockAction.setActivated(false);

        try {
            action.perform(req, res);
            fail("Expected Precondition Failed");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }

    }

    @Test
    public void testLockedResource() throws Exception {
        final WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
        final WebdavLock lock = new WebdavLock();
        lock.setDepth(0);
        lock.setOwner("me");
        lock.setScope(Scope.EXCLUSIVE_LITERAL);
        lock.setType(Type.WRITE_LITERAL);
        lock.setTimeout(WebdavLock.NEVER);
        resource.lock(lock);

        MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If", "(<" + lock.getToken() + ">)");

        final WebdavIfAction action = new WebdavIfAction();
        action.setDefaultDepth(0);
        action.setNext(mockAction);

        action.perform(req, res);

        assertTrue(mockAction.wasActivated());

        req = new MockWebdavRequest(factory, "http://localhost/");
        res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If", "(<" + lock.getToken() + ">)");
        mockAction.setActivated(false);

        resource.unlock(lock.getToken());

        try {
            action.perform(req, res);
            fail("Expected Precondition Failed");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }
    }

    @Test
    public void testCaptureLocks() throws Exception {
        final WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
        final WebdavLock lock = new WebdavLock();
        lock.setDepth(0);
        lock.setOwner("me");
        lock.setScope(Scope.EXCLUSIVE_LITERAL);
        lock.setType(Type.WRITE_LITERAL);
        lock.setTimeout(WebdavLock.NEVER);
        resource.lock(lock);

        MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If", "(<" + lock.getToken() + ">)");

        final WebdavIfAction action = new WebdavIfAction();
        action.setDefaultDepth(0);
        action.setNext(mockAction);

        action.perform(req, res);

        resource.unlock(lock.getToken());

        assertTrue(mockAction.wasActivated());

        Map<String, Object> userInfo = req.getUserInfo();

        List<String> mentionedLocks = (List<String>) userInfo.get("mentionedLocks");
        assertNotNull(mentionedLocks);
        assertEquals(1, mentionedLocks.size());
        assertEquals(lock.getToken(), mentionedLocks.get(0));
    }

    @Test
    public void testOr() throws Exception {
        final String etag = factory.resolveResource(INDEX_HTML_URL).getETag();

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If", "(<no-lock>) ([" + etag + "])");

        final WebdavIfAction action = new WebdavIfAction();
        action.setDefaultDepth(0);
        action.setNext(mockAction);

        action.perform(req, res);

        assertTrue(mockAction.wasActivated());
    }

    @Test
    public void testTrue() throws Exception {
        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If", "(<opaquelocktoken:blabla>) (Not <no-lock>)");

        final WebdavIfAction action = new WebdavIfAction();
        action.setDefaultDepth(0);
        action.setNext(mockAction);

        action.perform(req, res);

        assertTrue(mockAction.wasActivated());
    }

    @Test
    public void testFalse() throws Exception {
        final String etag = factory.resolveResource(INDEX_HTML_URL).getETag();

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If", "([" + etag + "] [no-etag])");
        mockAction.setActivated(false);

        final WebdavIfAction action = new WebdavIfAction();
        action.setDefaultDepth(0);
        action.setNext(mockAction);

        try {
            action.perform(req, res);
            fail("Expected Precondition Failed");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }
    }

    @Test
    public void testLockedCollection() throws Exception {
        final WebdavCollection collection = factory.resolveCollection(testCollection);
        final WebdavLock lock = new WebdavLock();
        lock.setDepth(0);
        lock.setOwner("me");
        lock.setScope(Scope.EXCLUSIVE_LITERAL);
        lock.setType(Type.WRITE_LITERAL);
        lock.setTimeout(WebdavLock.NEVER);
        collection.lock(lock);

        MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(testCollection);
        req.setHeader("If", "(<" + lock.getToken() + ">)");

        final WebdavIfAction action = new WebdavIfAction();
        action.setDefaultDepth(0);
        action.setNext(mockAction);

        action.perform(req, res);

        assertTrue(mockAction.wasActivated());

        action.setDefaultDepth(1);

        req = new MockWebdavRequest(factory, "http://localhost/");
        res = new MockWebdavResponse();
        mockAction.setActivated(false);

        req.setUrl(testCollection);
        req.setHeader("If", "(<" + lock.getToken() + ">)");

        try {
            action.perform(req, res);
            fail("Expected Precondition Failed");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }

        collection.unlock(lock.getToken());
    }

    @Test
    public void testTagged() throws Exception {
        final String etag = factory.resolveResource(INDEX_HTML_URL).getETag();

        MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(testCollection);
        req.setHeader("If", "<http://localhost/" + INDEX_HTML_URL + "> ([" + etag + "])");

        final WebdavIfAction action = new WebdavIfAction();
        action.setDefaultDepth(1);
        action.setNext(mockAction);

        action.perform(req, res);

        assertTrue(mockAction.wasActivated());

        req = new MockWebdavRequest(factory, "http://localhost/");
        res = new MockWebdavResponse();
        mockAction.setActivated(false);

        req.setUrl(testCollection);
        req.setHeader("If", "<http://localhost/" + INDEX_HTML_URL + "> ([i_don_t_match])");

        try {
            action.perform(req, res);
            fail("Expected Precondition Failed");
        } catch (final WebdavProtocolException x) {
            assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }

    }

    @Test
    public void testMissingLockToken() throws Exception {
        final WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
        final WebdavLock lock = new WebdavLock();
        lock.setDepth(0);
        lock.setOwner("me");
        lock.setScope(Scope.EXCLUSIVE_LITERAL);
        lock.setType(Type.WRITE_LITERAL);
        lock.setTimeout(WebdavLock.NEVER);
        resource.lock(lock);

        MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);

        final WebdavIfAction action = new WebdavIfAction();
        action.setDefaultDepth(0);
        action.checkSourceLocks(true);
        action.setNext(mockAction);

        try {
            action.perform(req, res);
            fail("Expected Precondition Failed");
        } catch (final WebdavProtocolException x) {
            assertEquals(Protocol.SC_LOCKED, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }

        req = new MockWebdavRequest(factory, "http://localhost/");
        res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("If", "(<12345>) (Not <no-lock>)");

        try {
            action.perform(req, res);
            fail("Expected Precondition Failed");
        } catch (final WebdavProtocolException x) {
            assertEquals(Protocol.SC_LOCKED, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }

        final WebdavResource r = factory.resolveResource(INDEX_HTML_URL);
        r.unlock(lock.getToken());
        r.save();

    }

    @Test
    public void testMissingLockTokenDestination() throws Exception {
        final WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
        final WebdavLock lock = new WebdavLock();
        lock.setDepth(0);
        lock.setOwner("me");
        lock.setScope(Scope.EXCLUSIVE_LITERAL);
        lock.setType(Type.WRITE_LITERAL);
        lock.setTimeout(WebdavLock.NEVER);
        resource.lock(lock);

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(testCollection);
        req.setHeader("destination", INDEX_HTML_URL.toString());

        final WebdavIfAction action = new WebdavIfAction();
        action.setDefaultDepth(0);
        action.checkDestinationLocks(true);
        action.setNext(mockAction);

        try {
            action.perform(req, res);
            fail("Expected Precondition Failed");
        } catch (final WebdavProtocolException x) {
            assertEquals(Protocol.SC_LOCKED, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }

        final WebdavResource r = factory.resolveResource(INDEX_HTML_URL);
        r.unlock(lock.getToken());
        r.save();

    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mockAction = new MockAction();
        INDEX_HTML_URL = testCollection.dup().append("index.html");
    }
}
