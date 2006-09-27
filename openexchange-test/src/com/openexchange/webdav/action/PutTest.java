package com.openexchange.webdav.action;

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
		
		WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
		assertEquals(resource.getLength(), new Long(content.getBytes("UTF-8").length));
		assertEquals("text/html", resource.getContentType());
		assertEquals(content, getContent(INDEX_HTML_URL));
	}
}
