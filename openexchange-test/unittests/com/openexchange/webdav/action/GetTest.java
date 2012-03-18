package com.openexchange.webdav.action;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.exception.OXException;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavPath;


public class GetTest extends ActionTestCase {

    private WebdavPath INDEX_HTML_URL = null;

    @Override
	public void setUp() throws Exception {
        super.setUp();
        INDEX_HTML_URL = testCollection.dup().append("index.html");

    }

    public void testBasic() throws Exception {
		final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		final MockWebdavResponse res = new MockWebdavResponse();

		req.setUrl(INDEX_HTML_URL);

		final WebdavAction action = new WebdavGetAction();

		action.perform(req,res);

		final String content = getContent(INDEX_HTML_URL);

		assertEquals(getContent(INDEX_HTML_URL), res.getResponseBodyAsString());
		assertEquals(content.getBytes(com.openexchange.java.Charsets.UTF_8).length, (int) new Integer(res.getHeader("content-length")));
		assertEquals("text/html", res.getHeader("content-type"));
		assertEquals(factory.resolveResource(INDEX_HTML_URL).getETag(), res.getHeader("ETag"));
		assertEquals(HttpServletResponse.SC_OK, res.getStatus());
		assertEquals("bytes", res.getHeader("Accept-Ranges"));

	}

	public void testNotFound() throws Exception {
		final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		final MockWebdavResponse res = new MockWebdavResponse();

		req.setUrl(new WebdavPath("iDontExist"));

		final WebdavAction action = new WebdavGetAction();

		try {
			action.perform(req,res);
			fail("Expected 404 not found");
		} catch (final WebdavProtocolException x) {
			assertEquals(HttpServletResponse.SC_NOT_FOUND, x.getStatus());
		}

	}

	public void testPartial() throws Exception {
		assertEquals((Long) 11L, factory.resolveResource(INDEX_HTML_URL).getLength());

		rangeTest(INDEX_HTML_URL, "2-5", getBytes(INDEX_HTML_URL, 2, 5));
	}


	public void testPartialWithOpenEnd() throws Exception {
		assertEquals((Long) 11L, factory.resolveResource(INDEX_HTML_URL).getLength());

		rangeTest(INDEX_HTML_URL, "5-", getBytes(INDEX_HTML_URL, 5, 10));
	}

	public void testPartialWithOpenBeginning() throws Exception {
		assertEquals((Long) 11L, factory.resolveResource(INDEX_HTML_URL).getLength());

		rangeTest(INDEX_HTML_URL, "-5", getBytes(INDEX_HTML_URL, 6, 10));
	}

	public void testPartialWithOpenBeginningTooMuch() throws Exception {
		assertEquals((Long) 11L, factory.resolveResource(INDEX_HTML_URL).getLength());

		rangeTest(INDEX_HTML_URL, "-23", getBytes(INDEX_HTML_URL, 0, 10));
	}

	public void testBogusRange() throws Exception {
		assertEquals((Long) 11L, factory.resolveResource(INDEX_HTML_URL).getLength());

		rangeTest(INDEX_HTML_URL, "5-2", new byte[0]);


	}

	public void testRangeOutsideLength() throws Exception {
		assertEquals((Long) 11L, factory.resolveResource(INDEX_HTML_URL).getLength());
		try {
			rangeTest(INDEX_HTML_URL, "23-25", getBytes(INDEX_HTML_URL, 0, 10));
			fail();
		} catch (final WebdavProtocolException x) {
			assertEquals(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE, x.getStatus());
		}
	}

	public void testMultipleRanges() throws Exception {
		assertEquals((Long) 11L, factory.resolveResource(INDEX_HTML_URL).getLength());
		final byte[] all = getBytes(INDEX_HTML_URL, 0, 10);
		final byte[] expect = new byte[]{all[0], all[10]};
		rangeTest(INDEX_HTML_URL, "0-0,-1", expect);

	}

	private void rangeTest(final WebdavPath url, final String byteHeader, final byte[] expect) throws OXException {
		final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		final MockWebdavResponse res = new MockWebdavResponse();

		req.setUrl(url);
		req.setHeader("Bytes", byteHeader);


		final WebdavAction action = new WebdavGetAction();

		action.perform(req,res);

		assertEquals(HttpServletResponse.SC_PARTIAL_CONTENT, res.getStatus());

		final byte[] bytes = res.getResponseBytes();
		assertEquals(expect.length+"", res.getHeader("Content-Length"));
		assertEquals(expect.length, bytes.length);
		for(int i = 0; i < expect.length; i++) {
			assertEquals(expect[i], bytes[i]);
		}
	}

	private byte[] getBytes(final WebdavPath url, final int start, final int stop) throws OXException, IOException {
		InputStream is = null;
		try {
			is = factory.resolveResource(url).getBody();
			is.skip(start);
			final byte[] bytes = new byte[stop-start+1];
			is.read(bytes);
			return bytes;
		} finally {
			if(is != null) {
				is.close();
			}
		}
	}
}
