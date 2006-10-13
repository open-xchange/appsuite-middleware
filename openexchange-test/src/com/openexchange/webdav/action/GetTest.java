package com.openexchange.webdav.action;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.webdav.protocol.WebdavException;


public class GetTest extends ActionTestCase {
	
	public void testBasic() throws Exception {
		
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		
		WebdavAction action = new WebdavGetAction();
		
		action.perform(req,res);
		
		String content = getContent(INDEX_HTML_URL);
		
		assertEquals(getContent(INDEX_HTML_URL), res.getResponseBodyAsString());
		assertEquals(content.getBytes("UTF-8").length, (int) new Integer(res.getHeader("content-length")));
		assertEquals("text/html", res.getHeader("content-type"));
		assertEquals(factory.resolveResource(INDEX_HTML_URL).getETag(), res.getHeader("ETag"));
		assertEquals(HttpServletResponse.SC_OK, res.getStatus());
	}
	
	public void testNotFound() throws Exception {
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl("iDontExist");
		
		WebdavAction action = new WebdavGetAction();
		
		try {
			action.perform(req,res);
			fail("Expected 404 not found");
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_NOT_FOUND, x.getStatus());
		}
		
	}
	
	public void testPartial() throws Exception {
		//TODO
	}
	
	public void testPartialWithOpenEnd() throws Exception {
		//TODO
	}
	
}
