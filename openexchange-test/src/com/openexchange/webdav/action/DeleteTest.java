package com.openexchange.webdav.action;

public class DeleteTest extends ActionTestCase {
	public void testResource() throws Exception {
		final String INDEX_HTML_URL = testCollection+"/index.html";
		final String DEVELOPMENT_URL = testCollection+"/development";
		final String DEVELOPMENT_GUI_INDEX3_HTML_URL = testCollection+"/development/gui/index3.html";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		
		WebdavAction action = new WebdavDeleteAction();
		
		action.perform(req,res);
		
		
		assertFalse(factory.resolveResource(INDEX_HTML_URL).exists());
		
		req = new MockWebdavRequest(factory, "http://localhost/");
		req.setUrl(DEVELOPMENT_URL);
		
		action.perform(req,res);
		
		assertFalse(factory.resolveResource(DEVELOPMENT_URL).exists());
		assertFalse(factory.resolveResource(DEVELOPMENT_GUI_INDEX3_HTML_URL).exists());
		
	}
}
