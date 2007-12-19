package com.openexchange.webdav.action;

import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavResource;

import javax.servlet.http.HttpServletResponse;

public class PutTest extends ActionTestCase {

    private WebdavPath INDEX_HTML_URL = null;
    private WebdavPath INDEX23_HTML_URL = null;

    public void setUp() throws Exception {
        super.setUp();
        INDEX_HTML_URL = testCollection.dup().append("index.html");
        INDEX23_HTML_URL =  testCollection.dup().append("index23.html");
    }


    public void testBasic() throws Exception {
		
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
		
		req.setUrl(new WebdavPath("notExists/lalala"));
		
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
