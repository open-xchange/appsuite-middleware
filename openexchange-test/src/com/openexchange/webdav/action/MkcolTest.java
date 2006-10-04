package com.openexchange.webdav.action;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.webdav.protocol.WebdavResource;

public class MkcolTest extends ActionTestCase {
	public void testCreateCollection() throws Exception {
		final String NEW_COLLECTION = testCollection+"/newCollection";
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(NEW_COLLECTION);
		
		WebdavAction action = new WebdavMkcolAction();
		action.perform(req,res);
		
		assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());
		
		WebdavResource resource = factory.resolveResource(NEW_COLLECTION);
		assertTrue(resource.exists() && resource.isCollection());
		
	}
}
