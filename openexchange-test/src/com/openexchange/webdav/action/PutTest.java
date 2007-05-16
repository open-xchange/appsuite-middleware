package com.openexchange.webdav.action;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavResource;

public class PutTest extends ActionTestCase {
	
	public void testBasic() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		
		String content = "<html><head /><body>The New, Better Index</body></html>";
		req.setBodyAsString(content);
		req.setHeader("content-length",((Integer)content.getBytes("UTF-8").length).toString());
		req.setHeader("content-type", "text/html");
		
		WebdavAction action = new WebdavPutAction();
		
		action.perform(req,res);
		
		assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());
		
		WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
		assertEquals(new Long(content.getBytes("UTF-8").length),resource.getLength());
		assertEquals("text/html", resource.getContentType());
		assertEquals(content, getContent(INDEX_HTML_URL));
	}
	
	public void testCreate() throws Exception {
		final String INDEX23_HTML_URL = testCollection+"/index23.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX23_HTML_URL);
		
		String content = "<html><head /><body>The New, Better Index</body></html>";
		req.setBodyAsString(content);
		req.setHeader("content-length",((Integer)content.getBytes("UTF-8").length).toString());
		req.setHeader("content-type", "text/html");
		
		WebdavAction action = new WebdavPutAction();
		
		action.perform(req,res);
		
		assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());
		
		WebdavResource resource = factory.resolveResource(INDEX23_HTML_URL);
		assertNotNull(resource);
		assertTrue(resource.exists());
		assertEquals(resource.getLength(), new Long(content.getBytes("UTF-8").length));
		assertEquals("text/html", resource.getContentType());
		assertEquals(content, getContent(INDEX23_HTML_URL));
	}
	
	// Bug 6104
	public void testTooLarge() throws Exception {
		final String INDEX23_HTML_URL = testCollection+"/index23.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX23_HTML_URL);
		
		String content = "<html><head /><body>The New, Better Index</body></html>";
		req.setBodyAsString(content);
		req.setHeader("content-length",((Integer)content.getBytes("UTF-8").length).toString());
		req.setHeader("content-type", "text/html");
		
		WebdavAction action = new WebdavPutAction() {
			@Override
			public long getMaxSize(){
				return 1;
			}
		};
		
		try {
			action.perform(req,res);
			assertFalse("Could upload", true);
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, x.getStatus());
		}
	}
	
	public void testInvalidParent() throws Exception {
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl("/notExists/lalala");
		
		String content = "<html><head /><body>The New, Better Index</body></html>";
		req.setBodyAsString(content);
		req.setHeader("content-length",((Integer)content.getBytes("UTF-8").length).toString());
		req.setHeader("content-type", "text/html");
		
		WebdavAction action = new WebdavPutAction();
		
		try {
			action.perform(req,res);
			fail("Expected 409 CONFLICT");
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_CONFLICT, x.getStatus());
		}
		
		
	}
}
