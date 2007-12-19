package com.openexchange.webdav.action;

import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavResource;

import javax.servlet.http.HttpServletResponse;

public class MkcolTest extends ActionTestCase {
	public void testCreateCollection() throws Exception {
		final WebdavPath NEW_COLLECTION = testCollection.dup().append("newCollection");
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(NEW_COLLECTION);
		
		WebdavAction action = new WebdavMkcolAction();
		action.perform(req,res);
		
		assertEquals(HttpServletResponse.SC_CREATED, res.getStatus());
		
		WebdavResource resource = factory.resolveResource(NEW_COLLECTION);
		assertTrue(resource.exists() && resource.isCollection());
		
	}
	
	public void testInvalidParent() throws Exception {
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(new WebdavPath("doesntExist/lalala"));
		
		WebdavAction action = new WebdavMkcolAction();
		
		try {
			action.perform(req,res);
			fail("Expected 409 CONFLICT or 412 PRECONDITION FAILED");
		} catch (WebdavException e) {
			assertTrue(""+e.getStatus(), HttpServletResponse.SC_CONFLICT == e.getStatus() || HttpServletResponse.SC_PRECONDITION_FAILED == e.getStatus());
		}
		
		
	}
}
