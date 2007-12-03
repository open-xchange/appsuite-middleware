package com.openexchange.webdav.action;

import java.io.ByteArrayInputStream;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavResource;

public abstract class StructureTest extends ActionTestCase {
	// noroot ?
	
	public void testResource() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		final String COPIED_INDEX_HTML_URL = testCollection+"/copied_index.html";
		
		String content = getContent(INDEX_HTML_URL);
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("Destination", COPIED_INDEX_HTML_URL);
		
		WebdavAction action = getAction(factory);
		action.perform(req, res);
		
		assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());
		
		WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
		assertTrue(resource.exists());
		assertEquals(content, getContent(INDEX_HTML_URL));
		
		resource = factory.resolveResource(COPIED_INDEX_HTML_URL);
		assertTrue(resource.exists());
		assertEquals(content, getContent(COPIED_INDEX_HTML_URL));
	}
	
	public void testOverwrite() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		final String SITEMAP_HTML_URL = testCollection+"/sitemap.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("Destination", SITEMAP_HTML_URL);
		req.setHeader("Overwrite", "F");
		
		WebdavAction action = getAction(factory);
		try {
			action.perform(req, res);
			fail("Expected 412 Precondition Failed");
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
		}	
	}
	
	public void testSuccessfulOverwrite() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		final String SITEMAP_HTML_URL = testCollection+"/sitemap.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("Destination", SITEMAP_HTML_URL);
		req.setHeader("Overwrite", "T");
		
		WebdavAction action = getAction(factory);
		action.perform(req, res);
		
		assertEquals(HttpServletResponse.SC_NO_CONTENT, res.getStatus());
		
	}
	
	public void testOverwriteCollection() throws Exception {
		final String DEVELOPMENT_URL = testCollection+"/development";
		final String PM_URL = testCollection+"/pm";
		
		factory.resolveCollection(DEVELOPMENT_URL).resolveResource("test.html").create();
		factory.resolveCollection(PM_URL).resolveResource("test.html").create();
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(DEVELOPMENT_URL);
		req.setHeader("Destination", PM_URL);
		req.setHeader("Overwrite", "F");
		
		WebdavAction action = getAction(factory);
		try {
			action.perform(req, res);
			fail("Expected 412 Precondition Failed");
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
		}
		
	}
	
	public void testMergeCollection() throws Exception {
		final String DEVELOPMENT_URL = testCollection+"/development";
		final String PM_URL = testCollection+"/pm";
		
		WebdavResource r = factory.resolveCollection(DEVELOPMENT_URL).resolveResource("test.html");
		r.putBodyAndGuessLength(new ByteArrayInputStream(new byte[2]));
		r.create(); // FIXME
		
		r = factory.resolveCollection(PM_URL).resolveResource("test2.html");
		r.putBodyAndGuessLength(new ByteArrayInputStream(new byte[2]));
		r.create(); // FIXME
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(DEVELOPMENT_URL);
		req.setHeader("Destination", PM_URL);
		
		WebdavAction action = getAction(factory);
		
		action.perform(req, res);
		
		//assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());
		assertTrue(factory.resolveResource(PM_URL+"/test.html").exists());
	}
	
	public void testSame() throws Exception {
		final String DEVELOPMENT_URL = testCollection+"/development";
		
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(DEVELOPMENT_URL);
		req.setHeader("Destination", DEVELOPMENT_URL);
		
		WebdavAction action = getAction(factory);
		try {
			action.perform(req, res);
			fail("Expected 403 FORBIDDEN");
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_FORBIDDEN, x.getStatus());
		}
	}
	
	public void testConflict() throws Exception {
		final String DEVELOPMENT_URL = testCollection+"/development";
			
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(DEVELOPMENT_URL);
		req.setHeader("Destination", "/doesntExist/nonono");
		
		WebdavAction action = getAction(factory);
		try {
			action.perform(req, res);
			fail("Expected 409 CONFLICT, 412 PRECONDITION FAILED or 207 MULTISTATUS");
		} catch (WebdavException x) {
			assertTrue(""+x.getStatus(), HttpServletResponse.SC_CONFLICT == x.getStatus() 
					|| Protocol.SC_MULTISTATUS == x.getStatus()
					|| HttpServletResponse.SC_PRECONDITION_FAILED == x.getStatus()
			);
		}
	}
	
	
	
	public abstract WebdavAction getAction(WebdavFactory factory);
}
