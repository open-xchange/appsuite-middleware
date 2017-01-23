
package com.openexchange.webdav.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.jdom2.Namespace;
import org.junit.Test;
import com.openexchange.test.XMLCompare;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavResource;

public class ProppatchTest extends ActionTestCase {

    private static final Namespace TEST_NS = Namespace.getNamespace("http://www.open-xchange.com/namespace/webdav-test");

    private WebdavPath INDEX_HTML_URL = null;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        INDEX_HTML_URL = testCollection.dup().append("index.html");
    }

    @Test
    public void testBasic() throws Exception {

        String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propertyupdate xmlns:D=\"DAV:\" xmlns:OX=\"" + TEST_NS.getURI() + "\"><D:set><D:prop><D:displayname>The index page</D:displayname></D:prop><D:prop><OX:test>Hallo</OX:test></D:prop></D:set><D:set><D:prop><OX:test2>N'Abend</OX:test2></D:prop><D:prop><OX:test3>Mogg\u00e4hn!</OX:test3></D:prop></D:set></D:propertyupdate>";
        String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\" xmlns:OX=\"" + TEST_NS.getURI() + "\"><D:response><D:href>http://localhost/" + INDEX_HTML_URL + "</D:href><D:propstat><D:prop><D:displayname /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><OX:test /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><OX:test2 /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><OX:test3 /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";

        MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);

        final WebdavAction action = new WebdavProppatchAction(new Protocol());
        action.perform(req, res);

        final XMLCompare compare = new XMLCompare();

        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));

        WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);

        assertEquals("The index page", resource.getDisplayName());

        WebdavProperty prop = resource.getProperty(TEST_NS.getURI(), "test");
        assertNotNull(prop);
        assertEquals("Hallo", prop.getValue());

        prop = resource.getProperty(TEST_NS.getURI(), "test2");
        assertNotNull(prop);
        assertEquals("N'Abend", prop.getValue());

        prop = resource.getProperty(TEST_NS.getURI(), "test3");
        assertNotNull(prop);
        assertEquals("Mogg\u00e4hn!", prop.getValue());

        // Remove

        body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propertyupdate xmlns:D=\"DAV:\" xmlns:OX=\"" + TEST_NS.getURI() + "\"><D:set><D:prop><OX:test4>Was? Wo!</OX:test4></D:prop></D:set><D:remove><D:prop><OX:test /></D:prop><D:prop><OX:test2 /></D:prop></D:remove><D:remove><D:prop><OX:test3 /></D:prop></D:remove></D:propertyupdate>";
        expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\" xmlns:OX=\"" + TEST_NS.getURI() + "\"><D:response><D:href>http://localhost/" + INDEX_HTML_URL + "</D:href><D:propstat><D:prop><OX:test4 /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><OX:test /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><OX:test2 /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><OX:test3 /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";

        req = new MockWebdavRequest(factory, "http://localhost/");
        res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);

        action.perform(req, res);

        assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());

        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));

        resource = factory.resolveResource(INDEX_HTML_URL);

        prop = resource.getProperty(TEST_NS.getURI(), "test");
        assertNull(prop);

        prop = resource.getProperty(TEST_NS.getURI(), "test2");
        assertNull(prop);

        prop = resource.getProperty(TEST_NS.getURI(), "test3");
        assertNull(prop);

        prop = resource.getProperty(TEST_NS.getURI(), "test4");
        assertNotNull(prop);
        assertEquals("Was? Wo!", prop.getValue());

    }

    @Test
    public void testXML() throws Exception {

        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propertyupdate xmlns:D=\"DAV:\" xmlns:OX=\"" + TEST_NS.getURI() + "\"><D:set><D:prop><OX:test><OX:gnatzel>GNA!</OX:gnatzel><bla xmlns=\"http://www.open-xchange.com/namespace/webdav-test2\" /></OX:test></D:prop></D:set></D:propertyupdate>";
        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\" xmlns:OX=\"" + TEST_NS.getURI() + "\"><D:response><D:href>http://localhost/" + INDEX_HTML_URL + "</D:href><D:propstat><D:prop><OX:test /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);

        final WebdavAction action = new WebdavProppatchAction(new Protocol());
        action.perform(req, res);
        assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());

        final XMLCompare compare = new XMLCompare();

        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));

        final WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);

        final WebdavProperty prop = resource.getProperty(TEST_NS.getURI(), "test");
        assertNotNull(prop);
        assertTrue(prop.isXML());
        assertEquals("<OX:gnatzel xmlns:OX=\"http://www.open-xchange.com/namespace/webdav-test\">GNA!</OX:gnatzel><bla xmlns=\"http://www.open-xchange.com/namespace/webdav-test2\" />", prop.getValue());
    }

    @Test
    public void testForbidden() throws Exception {

        final String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propertyupdate xmlns:D=\"DAV:\"><D:set><D:prop><D:displayname>The index page</D:displayname></D:prop><D:prop><D:getlastmodified>Hallo</D:getlastmodified></D:prop></D:set></D:propertyupdate>";
        final String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\"><D:response><D:href>http://localhost/" + INDEX_HTML_URL + "</D:href><D:propstat><D:prop><D:displayname /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><D:getlastmodified /></D:prop><D:status>HTTP/1.1 403 FORBIDDEN</D:status></D:propstat></D:response></D:multistatus>";

        final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        final MockWebdavResponse res = new MockWebdavResponse();

        req.setBodyAsString(body);
        req.setUrl(INDEX_HTML_URL);

        final WebdavAction action = new WebdavProppatchAction(new Protocol());
        action.perform(req, res);

        final XMLCompare compare = new XMLCompare();

        assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
    }
}
