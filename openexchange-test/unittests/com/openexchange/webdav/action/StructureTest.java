package com.openexchange.webdav.action;

import com.openexchange.webdav.protocol.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;

public abstract class StructureTest extends ActionTestCase {
	// noroot ?
	
	private WebdavPath INDEX_HTML_URL = null;
	private WebdavPath COPIED_INDEX_HTML_URL = null;
    private WebdavPath SITEMAP_HTML_URL = null;
    private WebdavPath DEVELOPMENT_URL = null;
    private WebdavPath PM_URL = null;

    public void setUp() throws Exception {
        super.setUp();
        INDEX_HTML_URL = testCollection.dup().append("index.html");
        COPIED_INDEX_HTML_URL = testCollection.dup().append("copied_index.html");
        SITEMAP_HTML_URL = testCollection.dup().append("sitemap.html");
        DEVELOPMENT_URL = testCollection.dup().append("development");
        PM_URL = testCollection.dup().append("pm");
    }

    public void testResource() throws Exception {

		String content = getContent(INDEX_HTML_URL);
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("Destination", COPIED_INDEX_HTML_URL.toString());
		
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

		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("Destination", SITEMAP_HTML_URL.toString());
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

		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("Destination", SITEMAP_HTML_URL.toString());
		req.setHeader("Overwrite", "T");
		
		WebdavAction action = getAction(factory);
		action.perform(req, res);
		
		assertEquals(HttpServletResponse.SC_NO_CONTENT, res.getStatus());
		
	}
	
	public void testOverwriteCollection() throws Exception {

		factory.resolveCollection(DEVELOPMENT_URL).resolveResource(new WebdavPath("test.html")).create();
		factory.resolveCollection(PM_URL).resolveResource(new WebdavPath("test.html")).create();
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(DEVELOPMENT_URL);
		req.setHeader("Destination", PM_URL.toString());
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
		WebdavResource r = factory.resolveCollection(DEVELOPMENT_URL).resolveResource(new WebdavPath("test.html"));
		r.putBodyAndGuessLength(new ByteArrayInputStream(new byte[2]));
		r.create(); // FIXME
		
		r = factory.resolveCollection(PM_URL).resolveResource(new WebdavPath("test2.html"));
		r.putBodyAndGuessLength(new ByteArrayInputStream(new byte[2]));
		r.create(); // FIXME
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(DEVELOPMENT_URL);
		req.setHeader("Destination", PM_URL.toString());
		
		WebdavAction action = getAction(factory);
		
		action.perform(req, res);
		
		//assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());
		assertTrue(factory.resolveResource(PM_URL+"/test.html").exists());
	}
	
	public void testSame() throws Exception {

		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(DEVELOPMENT_URL);
		req.setHeader("Destination", DEVELOPMENT_URL.toString());
		
		WebdavAction action = getAction(factory);
		try {
			action.perform(req, res);
			fail("Expected 403 FORBIDDEN");
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_FORBIDDEN, x.getStatus());
		}
	}
	
	public void testConflict() throws Exception {

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
