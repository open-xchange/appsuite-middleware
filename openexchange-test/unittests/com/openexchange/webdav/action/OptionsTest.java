package com.openexchange.webdav.action;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.openexchange.webdav.protocol.WebdavPath;

public class OptionsTest extends ActionTestCase {
	public void testOptions() throws Exception {
		final WebdavPath INDEX_HTML_URL = testCollection.dup().append("index.html");

		final MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		final MockWebdavResponse res = new MockWebdavResponse();

		req.setUrl(INDEX_HTML_URL);

		final WebdavAction action = new WebdavOptionsAction();

		action.perform(req,res);

		assertEquals("0", res.getHeader("content-length"));

		final Set<String> expected = new HashSet<String>(Arrays.asList("GET","PUT","DELETE","HEAD","OPTIONS","TRACE","PROPPATCH", "PROPFIND", "MOVE", "COPY", "LOCK", "UNLOCK", "REPORT", "ACL", "MKCALENDAR"));

		final String[] got = res.getHeader("Allow").split("\\s*,\\s*");

		for(final String allow : got) {
			assertTrue("Didn't expect: "+allow,expected.remove(allow));
		}
		assertTrue(expected.toString(), expected.isEmpty());

		final String[] davs = res.getHeader("DAV").split("\\s*,\\s*");
		assertEquals(9,davs.length);
		assertEquals("1", davs[0]);
		assertEquals("2", davs[1]);
		assertEquals("3", davs[2]);
		assertEquals("access-control", davs[3]);
		assertEquals("calendar-access", davs[4]);
		assertEquals("addressbook", davs[5]);
		assertEquals("extended-mkcol", davs[6]);
		assertEquals("calendar-auto-schedule", davs[7]);
		assertEquals("calendar-schedule", davs[8]);

		assertEquals("bytes", res.getHeader("Accept-Ranges"));
	}
}
