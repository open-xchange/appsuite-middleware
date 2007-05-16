package com.openexchange.webdav.action;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.webdav.protocol.WebdavException;

//Bug 6104
public class MaxUploadSizeActionTest extends ActionTestCase {
	private MockAction mockAction;

	public void setUp() throws Exception {
		super.setUp();
		mockAction = new MockAction();
	}
	
	public void testPassThru() throws WebdavException{
		final String INDEX_HTML_URL = testCollection+"/index_new.html";
		
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("content-length", "9");
		
		WebdavMaxUploadSizeAction action = new TestMaxUploadSizeAction();
		action.setNext(mockAction);
		
		action.perform(req,res);
		
		assertTrue(mockAction.wasActivated());
	}
	
	public void testDeny(){
		final String INDEX_HTML_URL = testCollection+"/index_new.html";
		
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("content-length", "11");
		
		WebdavMaxUploadSizeAction action = new TestMaxUploadSizeAction();
		action.setNext(mockAction);
		
		try {
			action.perform(req,res);
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, x.getStatus());
		}
			
		
		assertFalse(mockAction.wasActivated());
	}
	
	
	public static final class TestMaxUploadSizeAction extends WebdavMaxUploadSizeAction {
		@Override
		public boolean fits(WebdavRequest req) {
			return Long.valueOf(req.getHeader("content-length")) < 10;
		}
	}
}
