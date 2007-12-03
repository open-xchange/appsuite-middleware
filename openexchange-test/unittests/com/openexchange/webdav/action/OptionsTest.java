package com.openexchange.webdav.action;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OptionsTest extends ActionTestCase {
	public void testOptions() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		
		WebdavAction action = new WebdavOptionsAction();
		
		action.perform(req,res);
		
		assertEquals("0", res.getHeader("content-length"));
		
		Set<String> expected = new HashSet<String>(Arrays.asList("GET","PUT","DELETE","HEAD","OPTIONS","TRACE","PROPPATCH", "PROPFIND","MOVE","COPY","LOCK","UNLOCK"));
		
		String[] got = res.getHeader("Allow").split("\\s*,\\s*");
		
		for(String allow : got) {
			assertTrue("Didn't expect: "+allow,expected.remove(allow));
		}
		assertTrue(expected.toString(), expected.isEmpty());
		
		String[] davs = res.getHeader("DAV").split("\\s*,\\s*");
		assertEquals(2,davs.length);
		assertEquals("1", davs[0]);
		assertEquals("2", davs[1]);
		
		assertEquals("bytes", res.getHeader("Accept-Ranges"));
	}
}
