package com.openexchange.webdav.action;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OptionsTest extends ActionTestCase {
	public void testOptions() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory);
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		
		WebdavAction action = new WebdavOptionsAction();
		
		action.perform(req,res);
		
		assertEquals("0", res.getHeader("content-length"));
		
		Set<String> expected = new HashSet<String>(Arrays.asList("GET","PUT","DELETE","HEAD","OPTIONS","TRACE","PROPPATCH", "PROPFIND","MOVE","COPY","LOCK","UNLOCK"));
		
		String[] got = res.getHeader("Allow").split("\\s*,\\s");
		
		for(String allow : got) {
			assertTrue("Didn't expect: "+allow,expected.remove(allow));
		}
		assertTrue(expected.toString(), expected.isEmpty());
	}
}
