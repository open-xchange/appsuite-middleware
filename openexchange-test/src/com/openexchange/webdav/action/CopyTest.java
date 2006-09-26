package com.openexchange.webdav.action;

import com.openexchange.webdav.protocol.WebdavResource;

public class CopyTest extends ActionTestCase {
	//	TODO noroot
	//TODO overwrite
	
	public void testResource() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		final String COPIED_INDEX_HTML_URL = testCollection+"/copied_index.html";
		
		String content = getContent(INDEX_HTML_URL);
		
		MockWebdavRequest req = new MockWebdavRequest(factory);
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("Destination", COPIED_INDEX_HTML_URL);
		
		WebdavAction action = new WebdavCopyAction();
		action.perform(req, res);
		
		WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
		assertTrue(resource.exists());
		assertEquals(content, getContent(INDEX_HTML_URL));
		
		resource = factory.resolveResource(COPIED_INDEX_HTML_URL);
		assertTrue(resource.exists());
		assertEquals(content, getContent(COPIED_INDEX_HTML_URL));
	}
	
	public void testCollection() throws Exception {

	}
}
