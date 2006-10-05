package com.openexchange.webdav.action;

import org.jdom.Namespace;

import com.openexchange.test.XMLCompare;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavProperty;
import com.openexchange.webdav.protocol.WebdavResource;

public class ProppatchTest extends ActionTestCase {
	
	private static final Namespace TEST_NS = Namespace.getNamespace("http://www.open-xchange.com/namespace/webdav-test");
	
	
	public void testBasic() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propertyupdate xmlns:D=\"DAV:\" xmlns:OX=\""+TEST_NS.getURI()+"\"><D:set><D:prop><D:displayname>The index page</D:displayname></D:prop><D:prop><OX:test>Hallo</OX:test></D:prop></D:set><D:set><D:prop><OX:test2>N'Abend</OX:test2></D:prop><D:prop><OX:test3>Moggähn!</OX:test3></D:prop></D:set></D:propertyupdate>";
		String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\" xmlns:OX=\""+TEST_NS.getURI()+"\"><D:response><D:href>http://localhost/"+INDEX_HTML_URL+"</D:href><D:propstat><D:prop><D:displayname /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><OX:test /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><OX:test2 /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><OX:test3 /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(INDEX_HTML_URL);
		
		WebdavAction action = new WebdavProppatchAction(new Protocol());
		action.perform(req, res);
		
		XMLCompare compare = new XMLCompare();
		
		
		assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
		
		WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
		
		assertEquals("The index page",resource.getDisplayName());
		
		WebdavProperty prop = resource.getProperty(TEST_NS.getURI(),"test");
		assertNotNull(prop);
		assertEquals("Hallo", prop.getValue());
		
		prop = resource.getProperty(TEST_NS.getURI(),"test2");
		assertNotNull(prop);
		assertEquals("N'Abend", prop.getValue());
		
		prop = resource.getProperty(TEST_NS.getURI(),"test3");
		assertNotNull(prop);
		assertEquals("Moggähn!", prop.getValue());
		
		// Remove
		
		body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propertyupdate xmlns:D=\"DAV:\" xmlns:OX=\""+TEST_NS.getURI()+"\"><D:set><D:prop><OX:test4>Was? Wo!</OX:test4></D:prop></D:set><D:remove><D:prop><OX:test /></D:prop><D:prop><OX:test2 /></D:prop></D:remove><D:remove><D:prop><OX:test3 /></D:prop></D:remove></D:propertyupdate>";
		expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\" xmlns:OX=\""+TEST_NS.getURI()+"\"><D:response><D:href>http://localhost/"+INDEX_HTML_URL+"</D:href><D:propstat><D:prop><OX:test4 /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><OX:test /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><OX:test2 /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><OX:test3 /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";
		
		req = new MockWebdavRequest(factory, "http://localhost/");
		res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(INDEX_HTML_URL);
		
		action.perform(req, res);
		
		assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());
		
		assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
		
		resource = factory.resolveResource(INDEX_HTML_URL);
		
		prop = resource.getProperty(TEST_NS.getURI(),"test");
		assertNull(prop);
		
		prop = resource.getProperty(TEST_NS.getURI(),"test2");
		assertNull(prop);
		
		prop = resource.getProperty(TEST_NS.getURI(),"test3");
		assertNull(prop);
		
		prop = resource.getProperty(TEST_NS.getURI(),"test4");
		assertNotNull(prop);
		assertEquals("Was? Wo!", prop.getValue());
		
	}
	
	public void testXML() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propertyupdate xmlns:D=\"DAV:\" xmlns:OX=\""+TEST_NS.getURI()+"\"><D:set><D:prop><OX:test><OX:gnatzel>GNA!</OX:gnatzel><bla xmlns=\"http://www.open-xchange.com/namespace/webdav-test2\" /></OX:test></D:prop></D:set></D:propertyupdate>";
		String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\" xmlns:OX=\""+TEST_NS.getURI()+"\"><D:response><D:href>http://localhost/"+INDEX_HTML_URL+"</D:href><D:propstat><D:prop><OX:test /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat></D:response></D:multistatus>";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(INDEX_HTML_URL);
		
		WebdavAction action = new WebdavProppatchAction(new Protocol());
		action.perform(req, res);
		assertEquals(Protocol.SC_MULTISTATUS, res.getStatus());
		
		XMLCompare compare = new XMLCompare();
		
		
		assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
		
		WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
		
		WebdavProperty prop = resource.getProperty(TEST_NS.getURI(),"test");
		assertNotNull(prop);
		assertTrue(prop.isXML());
		assertEquals("<OX:gnatzel xmlns:OX=\"http://www.open-xchange.com/namespace/webdav-test\">GNA!</OX:gnatzel><bla xmlns=\"http://www.open-xchange.com/namespace/webdav-test2\" />", prop.getValue());
	}
	
	public void testForbidden() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		
		String body = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propertyupdate xmlns:D=\"DAV:\"><D:set><D:prop><D:displayname>The index page</D:displayname></D:prop><D:prop><D:getlastmodified>Hallo</D:getlastmodified></D:prop></D:set></D:propertyupdate>";
		String expect = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:multistatus xmlns:D=\"DAV:\"><D:response><D:href>http://localhost/"+INDEX_HTML_URL+"</D:href><D:propstat><D:prop><D:displayname /></D:prop><D:status>HTTP/1.1 200 OK</D:status></D:propstat><D:propstat><D:prop><D:getlastmodified /></D:prop><D:status>HTTP/1.1 403 FORBIDDEN</D:status></D:propstat></D:response></D:multistatus>";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setBodyAsString(body);
		req.setUrl(INDEX_HTML_URL);
		
		WebdavAction action = new WebdavProppatchAction(new Protocol());
		action.perform(req, res);
		
		XMLCompare compare = new XMLCompare();
		
		assertTrue(compare.compare(expect, res.getResponseBodyAsString()));
	}
}
