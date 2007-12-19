package com.openexchange.webdav.action;

import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavPath;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;


public class GetTest extends ActionTestCase {

    private WebdavPath INDEX_HTML_URL = null;

    public void setUp() throws Exception {
        super.setUp();
        INDEX_HTML_URL = testCollection.dup().append("index.html");

    }

    public void testBasic() throws Exception {
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
		assertEquals("bytes", res.getHeader("Accept-Ranges"));

	}
	
	public void testNotFound() throws Exception {
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(new WebdavPath("iDontExist"));
		
		WebdavAction action = new WebdavGetAction();
		
		try {
			action.perform(req,res);
			fail("Expected 404 not found");
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_NOT_FOUND, x.getStatus());
		}
		
	}
	
	public void testPartial() throws Exception {
		assertEquals((Long) 11l, factory.resolveResource(INDEX_HTML_URL).getLength());
		
		rangeTest(INDEX_HTML_URL, "2-5", getBytes(INDEX_HTML_URL, 2, 5));
	}
	

	public void testPartialWithOpenEnd() throws Exception {
		assertEquals((Long) 11l, factory.resolveResource(INDEX_HTML_URL).getLength());
		
		rangeTest(INDEX_HTML_URL, "5-", getBytes(INDEX_HTML_URL, 5, 10));
	}
	
	public void testPartialWithOpenBeginning() throws Exception {
		assertEquals((Long) 11l, factory.resolveResource(INDEX_HTML_URL).getLength());
		
		rangeTest(INDEX_HTML_URL, "-5", getBytes(INDEX_HTML_URL, 6, 10));
	}
	
	public void testPartialWithOpenBeginningTooMuch() throws Exception {
		assertEquals((Long) 11l, factory.resolveResource(INDEX_HTML_URL).getLength());
		
		rangeTest(INDEX_HTML_URL, "-23", getBytes(INDEX_HTML_URL, 0, 10));
	}
	
	public void testBogusRange() throws Exception {
		assertEquals((Long) 11l, factory.resolveResource(INDEX_HTML_URL).getLength());
		
		rangeTest(INDEX_HTML_URL, "5-2", new byte[0]);
		
		
	}
	
	public void testRangeOutsideLength() throws Exception {
		assertEquals((Long) 11l, factory.resolveResource(INDEX_HTML_URL).getLength());
		try {
			rangeTest(INDEX_HTML_URL, "23-25", getBytes(INDEX_HTML_URL, 0, 10));
			fail();
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE, x.getStatus());
		}
	}
	
	public void testMultipleRanges() throws Exception {
		assertEquals((Long) 11l, factory.resolveResource(INDEX_HTML_URL).getLength());
		byte[] all = getBytes(INDEX_HTML_URL, 0, 10);
		byte[] expect = new byte[]{all[0], all[10]};
		rangeTest(INDEX_HTML_URL, "0-0,-1", expect);
		
	}
	
	private void rangeTest(WebdavPath url, String byteHeader, byte[] expect) throws WebdavException {
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(url);
		req.setHeader("Bytes", byteHeader);
		
		
		WebdavAction action = new WebdavGetAction();
		
		action.perform(req,res);
		
		assertEquals(HttpServletResponse.SC_PARTIAL_CONTENT, res.getStatus());
		
		byte[] bytes = res.getResponseBytes();
		assertEquals(expect.length+"", res.getHeader("Content-Length"));
		assertEquals(expect.length, bytes.length);
		for(int i = 0; i < expect.length; i++) {
			assertEquals(expect[i], bytes[i]);
		}
	}
	
	private byte[] getBytes(WebdavPath url, int start, int stop) throws WebdavException, IOException {
		InputStream is = null;
		try {
			is = factory.resolveResource(url).getBody();
			is.skip(start);
			byte[] bytes = new byte[stop-start+1];
			is.read(bytes);
			return bytes;
		} finally {
			if(is != null)
				is.close();
		}
	}
}
