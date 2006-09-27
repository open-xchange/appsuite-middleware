package com.openexchange.webdav.action;

public class TraceTest extends ActionTestCase {
	public void testBasic() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setHeader("header1", "value1");
		req.setHeader("header2", "value2");
		req.setHeader("header3", "value3");
		req.setHeader("header4", "value4");
		
		
		req.setUrl(INDEX_HTML_URL);
		
		WebdavAction action = new WebdavTraceAction();
		
		action.perform(req,res);
		
		assertEquals("value1", res.getHeader("header1"));
		assertEquals("value2", res.getHeader("header2"));
		assertEquals("value3", res.getHeader("header3"));
		assertEquals("value4", res.getHeader("header4"));
		
	}
}
