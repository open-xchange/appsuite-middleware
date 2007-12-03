package com.openexchange.webdav.action;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.webdav.protocol.WebdavException;

public class NotExistTest extends ActionTestCase {
	private MockAction mockAction;
	
	public void testNotExists() throws Exception {
		final String NOT_EXIST_URL = "notExists.txt";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(NOT_EXIST_URL);
		
		AbstractAction action = new WebdavExistsAction();
		action.setNext(mockAction);
		
		try {
			action.perform(req,res);
			fail("Expected 404 Not Found");
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_NOT_FOUND, x.getStatus());
			assertFalse(mockAction.wasActivated());
		}
	}
	
	public void testExists() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		
		AbstractAction action = new WebdavExistsAction();
		action.setNext(mockAction);
		
		action.perform(req,res);
		assertTrue(mockAction.wasActivated());
	}
	
	public void setUp() throws Exception {
		super.setUp();
		this.mockAction = new MockAction();
	}
}
