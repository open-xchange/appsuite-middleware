package com.openexchange.webdav.action;

import com.openexchange.webdav.protocol.WebdavPath;

public class HeadTest extends ActionTestCase {
	public void testBasic() throws Exception {
		final WebdavPath INDEX_HTML_URL = testCollection.dup().append("index.html");
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		
		WebdavAction action = new WebdavHeadAction();
		
		action.perform(req,res);
		
		String content = getContent(INDEX_HTML_URL);
		
		assertEquals("", res.getResponseBodyAsString());
		assertEquals(content.getBytes("UTF-8").length, (int) new Integer(res.getHeader("content-length")));
		assertEquals("text/html", res.getHeader("content-type"));
		assertEquals("bytes", res.getHeader("Accept-Ranges"));
	}
}
