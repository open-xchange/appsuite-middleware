
package com.openexchange.webdav.action;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import com.openexchange.exception.OXException;
import com.openexchange.test.XMLCompare;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavResource;

public class LockTest extends ActionTestCase {

    private static final Namespace TEST_NS = Namespace.getNamespace("http://www.open-xchange.com/namespace/webdav-test");

    private WebdavPath INDEX_HTML_URL;

    private WebdavPath GUI_URL;

    private WebdavPath LOCK_HTML_URL;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        INDEX_HTML_URL = testCollection.dup().append("index.html");
        GUI_URL = testCollection.dup().append("development").append("gui");
        LOCK_HTML_URL = testCollection.dup().append("lock.html");
    }

    public void testLock() throws Exception {
        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><lockinfo xmlns=\"DAV:\"><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>me</owner></lockinfo>";

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);
        req.setHeader("Timeout", "infinite");

        final WebdavAction action = new WebdavLockAction();
        action.perform(req, res);
        assertEquals(HttpServletResponse.SC_OK, res.getStatus());

        // LockToken Header
        assertEquals(1, factory.resolveResource(INDEX_HTML_URL).getLocks().size());
        final WebdavLock lock = factory.resolveResource(INDEX_HTML_URL).getLocks().get(0);
        assertNotNull(lock.getToken());
        final String lockToken = lock.getToken();

        assertEquals(lockToken, res.getHeader("Lock-Token"));

        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><prop xmlns=\"DAV:\"><lockdiscovery><activelock><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>me</owner><depth>0</depth><locktoken><href>" + lockToken + "</href></locktoken><timeout></timeout></activelock></lockdiscovery></prop>";

        final XMLCompare compare = new XMLCompare();
        compare.setCheckTextNames("owner", "locktoken");

        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));

        final WebdavResource r = factory.resolveResource(INDEX_HTML_URL);
        r.unlock(lockToken);
        r.save();

    }

    public void testLockOwnerInXML() throws Exception {

        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><lockinfo xmlns=\"DAV:\"><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner><shortName xmlns=\"" + TEST_NS.getURI() + "\">me</shortName></owner></lockinfo>";

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);
        req.setHeader("Timeout", "infinite");

        final WebdavAction action = new WebdavLockAction();
        action.perform(req, res);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());

        // LockToken Header
        assertEquals(1, factory.resolveResource(INDEX_HTML_URL).getLocks().size());
        final WebdavLock lock = factory.resolveResource(INDEX_HTML_URL).getLocks().get(0);
        assertNotNull(lock.getToken());
        final String lockToken = lock.getToken();

        assertEquals(lockToken, res.getHeader("Lock-Token"));

        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><prop xmlns=\"DAV:\"><lockdiscovery><activelock><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner><shortName xmlns=\"" + TEST_NS.getURI() + "\">me</shortName></owner><depth>0</depth><locktoken><href>" + lockToken + "</href></locktoken><timeout>Infinite</timeout></activelock></lockdiscovery></prop>";

        final XMLCompare compare = new XMLCompare();
        compare.setCheckTextNames("owner", "locktoken", "timeout", "shortName");

        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));

        final WebdavResource r = factory.resolveResource(INDEX_HTML_URL);
        r.unlock(lockToken);
        r.save();

    }

    public void testDepth() throws Exception {
        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><lockinfo xmlns=\"DAV:\"><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>me</owner></lockinfo>";

        MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(GUI_URL);
        req.setHeader("Timeout", "infinite");
        req.setHeader("Depth", "infinity");

        final WebdavAction action = new WebdavLockAction();
        action.perform(req, res);
        assertEquals(HttpServletResponse.SC_OK, res.getStatus());

        final WebdavResource resource = factory.resolveResource(GUI_URL);

        assertEquals(1, resource.getLocks().size());
        WebdavLock lock = resource.getLocks().get(0);
        assertEquals(WebdavCollection.INFINITY, lock.getDepth());

        resource.unlock(lock.getToken());

        req = new MockWebdavRequest(factory, "http://localhost/");
        res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(GUI_URL);
        req.setHeader("Timeout", "infinite");
        req.setHeader("Depth", "1");

        action.perform(req, res);

        assertEquals(1, resource.getLocks().size());
        lock = resource.getLocks().get(0);
        assertEquals(1, lock.getDepth());
    }

    public void testTimeoutInSeconds() throws Exception {

        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><lockinfo xmlns=\"DAV:\"><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>me</owner></lockinfo>";

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);
        req.setHeader("Timeout", "Second-3600");

        final WebdavAction action = new WebdavLockAction();
        action.perform(req, res);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());

        // LockToken Header
        assertEquals(1, factory.resolveResource(INDEX_HTML_URL).getLocks().size());
        final WebdavLock lock = factory.resolveResource(INDEX_HTML_URL).getLocks().get(0);

        // assertTrue(3600000 - lock.getTimeout() < 200);
        // Bug 12575
        assertTrue(lock.getTimeout() <= 3600000);

        final WebdavResource r = factory.resolveResource(INDEX_HTML_URL);
        final String lockToken = r.getLocks().iterator().next().getToken();
        r.unlock(lockToken);
        r.save();

    }

    public void testLockNull() throws Exception {

        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><lockinfo xmlns=\"DAV:\"><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>me</owner></lockinfo>";

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(LOCK_HTML_URL);
        req.setHeader("Timeout", "infinite");

        final WebdavAction action = new WebdavLockAction();
        action.perform(req, res);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());

        final WebdavResource resource = factory.resolveResource(LOCK_HTML_URL);
        assertTrue(resource.isLockNull());

        final WebdavLock lock = resource.getLocks().get(0);
        final String lockToken = lock.getToken();

        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><prop xmlns=\"DAV:\"><lockdiscovery><activelock><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>me</owner><depth>0</depth><locktoken><href>" + lockToken + "</href></locktoken><timeout></timeout></activelock></lockdiscovery></prop>";

        final XMLCompare compare = new XMLCompare();
        compare.setCheckTextNames("owner", "locktoken");

        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));

    }

    // Bug 13482
    public void testLockWithoutXMLBody() throws OXException, UnsupportedEncodingException, JDOMException, IOException {

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(INDEX_HTML_URL);
        req.setHeader("Timeout", "infinite");

        final WebdavAction action = new WebdavLockAction();
        action.perform(req, res);
        assertEquals(HttpServletResponse.SC_OK, res.getStatus());

        // LockToken Header
        assertEquals(1, factory.resolveResource(INDEX_HTML_URL).getLocks().size());
        final WebdavLock lock = factory.resolveResource(INDEX_HTML_URL).getLocks().get(0);
        assertNotNull(lock.getToken());
        final String lockToken = lock.getToken();

        assertEquals(lockToken, res.getHeader("Lock-Token"));

        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><prop xmlns=\"DAV:\"><lockdiscovery><activelock><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>me</owner><depth>0</depth><locktoken><href>" + lockToken + "</href></locktoken><timeout></timeout></activelock></lockdiscovery></prop>";

        final XMLCompare compare = new XMLCompare();
        compare.setCheckTextNames("locktoken");

        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));

        final WebdavResource r = factory.resolveResource(INDEX_HTML_URL);
        r.unlock(lockToken);
        r.save();

    }

    public void testRelock() throws OXException, UnsupportedEncodingException, JDOMException, IOException {
        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><lockinfo xmlns=\"DAV:\"><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>Administrator</owner></lockinfo>";

        MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);
        req.setHeader("Timeout", "infinite");

        WebdavAction action = new WebdavLockAction();
        action.perform(req, res);

        String lockToken = res.getHeader("Lock-Token");

        req = new MockWebdavRequest(factory, "http://localhost/");
        req.setUrl(INDEX_HTML_URL);
        req.setHeader("Timeout", "infinite");
        req.setHeader("If", "(<"+lockToken+">)");
        req.getUserInfo().put("mentionedLocks", Arrays.asList(lockToken));
        res = new MockWebdavResponse();

        action = new WebdavLockAction();
        action.perform(req, res);

        assertEquals(HttpServletResponse.SC_OK, res.getStatus());

        // LockToken Header
        assertEquals(1, factory.resolveResource(INDEX_HTML_URL).getLocks().size());

        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><prop xmlns=\"DAV:\"><lockdiscovery><activelock><lockscope><exclusive /></lockscope><locktype><write /></locktype><owner>Administrator</owner><depth>0</depth><locktoken><href>" + lockToken + "</href></locktoken><timeout>Infinite</timeout></activelock></lockdiscovery></prop>";

        final XMLCompare compare = new XMLCompare();
        compare.setCheckTextNames("owner", "locktoken", "timeout", "shortName");

        assertTrue("got: "+res.getResponseBodyAsString(), compare.compare(expect, res.getResponseBodyAsString()));

        final WebdavResource r = factory.resolveResource(INDEX_HTML_URL);
        r.unlock(lockToken);
        r.save();
    }
}
