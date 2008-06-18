package com.openexchange.webdav.action;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.webdav.protocol.WebdavFactory;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavResource;

public class MoveTest extends StructureTest {

	//TODO noroot
	
	@Override
	public void testResource() throws Exception {
		final WebdavPath INDEX_HTML_URL = testCollection.dup().append("index.html");
		final WebdavPath MOVED_INDEX_HTML_URL = testCollection.dup().append("moved_index.html");
		
		final String content = getContent(INDEX_HTML_URL);
		
		final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		final MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("Destination", MOVED_INDEX_HTML_URL.toString());
		
		final WebdavAction action = new WebdavMoveAction(factory);
		action.perform(req, res);
		
		assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());
		
		WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
		assertFalse(resource.exists());
		
		resource = factory.resolveResource(MOVED_INDEX_HTML_URL);
		assertTrue(resource.exists());
		
		assertEquals(content, getContent(MOVED_INDEX_HTML_URL));
	}
	
	@Override
	public WebdavAction getAction(final WebdavFactory factory) {
		return new WebdavMoveAction(factory);
	}
}
