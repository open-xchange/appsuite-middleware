package com.openexchange.webdav.action;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.java.Strings;
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

		List<String> davHeaders = Strings.splitAndTrim(res.getHeader("DAV"), ",");
		List<String> expectedDavHeaders = Strings.splitAndTrim(WebdavOptionsAction.DAV_OPTIONS, ",");
		assertTrue(expectedDavHeaders.equals(davHeaders));

		assertEquals("bytes", res.getHeader("Accept-Ranges"));
	}

}
