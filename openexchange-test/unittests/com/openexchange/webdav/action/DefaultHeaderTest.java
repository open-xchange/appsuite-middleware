package com.openexchange.webdav.action;

import java.util.Date;

import com.openexchange.webdav.protocol.util.Utils;

public class DefaultHeaderTest extends ActionTestCase{
	public void testDefaultHeader() throws Exception {
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		MockAction mockAction = new MockAction();
		
		AbstractAction action = new WebdavDefaultHeaderAction();
		action.setNext(mockAction);
		
		Date now = new Date();
		action.perform(req, res);
		assertEquals(Utils.convert(now), res.getHeader("Date"));
		assertEquals("Openexchange WebDAV", res.getHeader("Server"));
	}
}
