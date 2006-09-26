package com.openexchange.webdav.action;

import com.openexchange.webdav.protocol.WebdavResource;

public class MkcolTest extends ActionTestCase {
	public void testCreateCollection() throws Exception {
		final String NEW_COLLECTION = testCollection+"/newCollection";
		
		MockWebdavRequest req = new MockWebdavRequest(factory);
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(NEW_COLLECTION);
		
		WebdavAction action = new WebdavMkcolAction();
		action.perform(req,res);
		
		
		WebdavResource resource = factory.resolveResource(NEW_COLLECTION);
		assertTrue(resource.exists() && resource.isCollection());
		
	}
}
