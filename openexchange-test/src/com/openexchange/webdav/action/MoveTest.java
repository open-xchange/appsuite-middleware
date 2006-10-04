package com.openexchange.webdav.action;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.webdav.protocol.WebdavResource;

public class MoveTest extends ActionTestCase {

	//TODO noroot
	//TODO overwrite
	
	public void testResource() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		final String MOVED_INDEX_HTML_URL = testCollection+"/moved_index.html";
		
		String content = getContent(INDEX_HTML_URL);
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("Destination", MOVED_INDEX_HTML_URL);
		
		WebdavAction action = new WebdavMoveAction();
		action.perform(req, res);
		
		assertEquals(HttpServletResponse.SC_NO_CONTENT, res.getStatus());
		
		WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
		assertFalse(resource.exists());
		
		resource = factory.resolveResource(MOVED_INDEX_HTML_URL);
		assertTrue(resource.exists());
		
		assertEquals(content, getContent(MOVED_INDEX_HTML_URL));
	}
	
	public void testCollection() throws Exception {
		
	}
}
