package com.openexchange.webdav.action;


public class GetTest extends ActionTestCase {
	
	public void testBasic() throws Exception {
		
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		
		WebdavAction action = new WebdavGetAction();
		
		action.perform(req,res);
		
		String content = getContent(INDEX_HTML_URL);
		
		assertEquals(getContent(INDEX_HTML_URL), res.getResponseBodyAsString());
		assertEquals(content.getBytes("UTF-8").length, (int) new Integer(res.getHeader("content-length")));
		assertEquals("text/html", res.getHeader("content-type"));
	}
	
}
