package com.openexchange.webdav.action;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.webdav.protocol.WebdavException;

public class IfMatchTest extends ActionTestCase {
	private MockAction mockAction;

	public void testIfMatchWorks() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		String etag = factory.resolveResource(INDEX_HTML_URL).getETag();
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If-Match", etag);
		
		AbstractAction action = new WebdavIfMatchAction();
		
		action.setNext(mockAction);
		
		action.perform(req, res);
		
		assertTrue(mockAction.wasActivated());
		
		
	}
	
	public void testIfMatchFails() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If-Match", "i_don_t_match");
		
		AbstractAction action = new WebdavIfMatchAction();
		
		mockAction.setActivated(false);
		try {
			action.setNext(mockAction);
			action.perform(req, res);
			fail("Expected precondition exception");		
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
			assertFalse(mockAction.wasActivated());
		}
	}
	
	public void testIfMatchMany() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		String etag = factory.resolveResource(INDEX_HTML_URL).getETag();
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If-Match", "bla-1, bla-2, "+etag+", bla, bla2, bla3");
		
		AbstractAction action = new WebdavIfMatchAction();
		
		action.setNext(mockAction);
		
		action.perform(req, res);
		
		assertTrue(mockAction.wasActivated());
	}
	
	public void testWildcardsIfMatchWorks() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If-Match", "*");
		
		AbstractAction action = new WebdavIfMatchAction();
		
		action.setNext(mockAction);
		
		action.perform(req, res);
		
		assertTrue(mockAction.wasActivated());
	}
	
	public void testWildcardsIfMatchFails() throws Exception {
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl("/doesntExist");
		req.setHeader("If-Match", "*");
		
		AbstractAction action = new WebdavIfMatchAction();
		
		mockAction.setActivated(false);
		try {
			action.setNext(mockAction);
			action.perform(req, res);
			fail("Expected precondition exception");		
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
			assertFalse(mockAction.wasActivated());
		}
	}

	
	public void testIfNoneMatchWorks() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If-None-Match", "i_don_t_match");
		
		AbstractAction action = new WebdavIfMatchAction();
		
		action.setNext(mockAction);
		
		action.perform(req, res);
		
		assertTrue(mockAction.wasActivated());
	}
	
	public void testIfNoneMatchFails() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		String etag = factory.resolveResource(INDEX_HTML_URL).getETag();
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If-None-Match", etag);
		
		AbstractAction action = new WebdavIfMatchAction();
		
		mockAction.setActivated(false);
		try {
			action.setNext(mockAction);
			action.perform(req, res);
			fail("Expected precondition exception");		
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
			assertFalse(mockAction.wasActivated());
		}
	}
	
	public void testIfNoneMatchMany() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		String etag = factory.resolveResource(INDEX_HTML_URL).getETag();
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If-None-Match", "bla-1, bla-2, "+etag+", bla, bla2, bla3");
		
		AbstractAction action = new WebdavIfMatchAction();
		
		mockAction.setActivated(false);
		try {
			action.setNext(mockAction);
			action.perform(req, res);
			fail("Expected precondition exception");		
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
			assertFalse(mockAction.wasActivated());
		}
	}
	
	public void testWildcardsIfNonMatchWorks() throws Exception {
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl("/doesntExist");
		req.setHeader("If-None-Match", "*");
		
		AbstractAction action = new WebdavIfMatchAction();
		
		mockAction.setActivated(false);
		
		action.setNext(mockAction);
		action.perform(req, res);
		
		assertTrue(mockAction.wasActivated());
	}
	
	public void testWildcardsIfNonMatchFails() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If-None-Match", "*");
		
		AbstractAction action = new WebdavIfMatchAction();
		
		mockAction.setActivated(false);
		try {
			action.setNext(mockAction);
			action.perform(req, res);
			fail("Expected precondition exception");		
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
			assertFalse(mockAction.wasActivated());
		}
	}
	
	public void testNoneSet() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		
		AbstractAction action = new WebdavIfMatchAction();
		
		action.setNext(mockAction);
		
		action.perform(req, res);
		
		assertTrue(mockAction.wasActivated());
	}
	
	public void setUp() throws Exception {
		super.setUp();
		mockAction = new MockAction();
	}
}
