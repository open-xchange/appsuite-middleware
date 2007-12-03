package com.openexchange.webdav.action;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavLock.Scope;
import com.openexchange.webdav.protocol.WebdavLock.Type;

public class UnlockTest extends ActionTestCase {
	public void testUnlock() throws Exception {
		final String INDEX_HTML = testCollection+"/index.html";
		
		WebdavResource resource = factory.resolveResource(INDEX_HTML);
		
		WebdavLock lock = new WebdavLock();
		lock.setTimeout(WebdavLock.NEVER);
		lock.setDepth(0);
		lock.setOwner("me");
		lock.setScope(Scope.EXCLUSIVE_LITERAL);
		lock.setType(Type.WRITE_LITERAL);
		
		resource.lock(lock);
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML);
		req.setHeader("Lock-Token","<"+lock.getToken()+">");
		
		WebdavAction action = new WebdavUnlockAction();
		
		action.perform(req,res);
		
		assertEquals(HttpServletResponse.SC_OK, res.getStatus());
		assertTrue(resource.getLocks().isEmpty());
	}
}
