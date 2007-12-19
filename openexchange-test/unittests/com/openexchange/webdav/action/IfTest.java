package com.openexchange.webdav.action;

import javax.servlet.http.HttpServletResponse;

import com.openexchange.webdav.protocol.*;
import com.openexchange.webdav.protocol.WebdavLock.Scope;
import com.openexchange.webdav.protocol.WebdavLock.Type;

public class IfTest extends ActionTestCase {
	private MockAction mockAction;
    private WebdavPath INDEX_HTML_URL;

    public void testETag() throws Exception {
		String etag = factory.resolveResource(INDEX_HTML_URL).getETag();
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If", "(["+etag+"])");
		
		WebdavIfAction action = new WebdavIfAction();
		action.setDefaultDepth(0);
		action.setNext(mockAction);
		
		action.perform(req,res);
		
		assertTrue(mockAction.wasActivated());
		
		req = new MockWebdavRequest(factory,"http://localhost/");
		res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If", "([i_don_t_match])");
		mockAction.setActivated(false);
		
		try {
			action.perform(req,res);
			fail("Expected Precondition Failed");
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
			assertFalse(mockAction.wasActivated());
		}
		
	}
	
	public void testLockedResource() throws Exception {
		WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
		WebdavLock lock = new WebdavLock();
		lock.setDepth(0);
		lock.setOwner("me");
		lock.setScope(Scope.EXCLUSIVE_LITERAL);
		lock.setType(Type.WRITE_LITERAL);
		lock.setTimeout(WebdavLock.NEVER);
		resource.lock(lock);
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If", "(<"+lock.getToken()+">)");
		
		WebdavIfAction action = new WebdavIfAction();
		action.setDefaultDepth(0);
		action.setNext(mockAction);
		
		action.perform(req,res);
		
		assertTrue(mockAction.wasActivated());
		
		req = new MockWebdavRequest(factory,"http://localhost/");
		res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If", "(<"+lock.getToken()+">)");
		mockAction.setActivated(false);
		
		resource.unlock(lock.getToken());
		
		try {
			action.perform(req,res);
			fail("Expected Precondition Failed");
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
			assertFalse(mockAction.wasActivated());
		}
	}
	
	public void testOr() throws Exception {
		String etag = factory.resolveResource(INDEX_HTML_URL).getETag();
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If", "(<no-lock>) (["+etag+"])");
		
		WebdavIfAction action = new WebdavIfAction();
		action.setDefaultDepth(0);
		action.setNext(mockAction);
		
		action.perform(req,res);
		
		assertTrue(mockAction.wasActivated());
	}
	
	public void testTrue() throws Exception {
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If", "(<opaquelocktoken:blabla>) (Not <no-lock>)");
		
		WebdavIfAction action = new WebdavIfAction();
		action.setDefaultDepth(0);
		action.setNext(mockAction);
		
		action.perform(req,res);
		
		assertTrue(mockAction.wasActivated());
	}
	
	public void testFalse() throws Exception {
		String etag = factory.resolveResource(INDEX_HTML_URL).getETag();
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If", "(["+etag+"] [no-etag])");
		mockAction.setActivated(false);
		
		WebdavIfAction action = new WebdavIfAction();
		action.setDefaultDepth(0);
		action.setNext(mockAction);
		
		try {
			action.perform(req,res);
			fail("Expected Precondition Failed");
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
			assertFalse(mockAction.wasActivated());
		}
	}
	
	public void testLockedCollection() throws Exception {
		WebdavCollection collection = factory.resolveCollection(testCollection);
		WebdavLock lock = new WebdavLock();
		lock.setDepth(0);
		lock.setOwner("me");
		lock.setScope(Scope.EXCLUSIVE_LITERAL);
		lock.setType(Type.WRITE_LITERAL);
		lock.setTimeout(WebdavLock.NEVER);
		collection.lock(lock);
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(testCollection);
		req.setHeader("If", "(<"+lock.getToken()+">)");
		
		WebdavIfAction action = new WebdavIfAction();
		action.setDefaultDepth(0);
		action.setNext(mockAction);
		
		action.perform(req,res);
		
		assertTrue(mockAction.wasActivated());
		
		
		action.setDefaultDepth(1);
		
		req = new MockWebdavRequest(factory,"http://localhost/");
		res = new MockWebdavResponse();
		mockAction.setActivated(false);
		
		req.setUrl(testCollection);
		req.setHeader("If", "(<"+lock.getToken()+">)");
		
		try {
			action.perform(req,res);
			fail("Expected Precondition Failed");
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
			assertFalse(mockAction.wasActivated());
		}
		
		collection.unlock(lock.getToken());
	}
	
	public void testTagged() throws Exception {
		String etag = factory.resolveResource(INDEX_HTML_URL).getETag();
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(testCollection);
		req.setHeader("If", "<http://localhost/"+INDEX_HTML_URL+"> (["+etag+"])");
		
		WebdavIfAction action = new WebdavIfAction();
		action.setDefaultDepth(1);
		action.setNext(mockAction);
		
		action.perform(req,res);
		
		assertTrue(mockAction.wasActivated());
		
		
		req = new MockWebdavRequest(factory,"http://localhost/");
		res = new MockWebdavResponse();
		mockAction.setActivated(false);
		
		req.setUrl(testCollection);
		req.setHeader("If", "<http://localhost/"+INDEX_HTML_URL+"> ([i_don_t_match])");
		
		try {
			action.perform(req,res);
			fail("Expected Precondition Failed");
		} catch (WebdavException x) {
			assertEquals(HttpServletResponse.SC_PRECONDITION_FAILED, x.getStatus());
			assertFalse(mockAction.wasActivated());
		}
		
	}
	
	public void testMissingLockToken() throws Exception {
		WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
		WebdavLock lock = new WebdavLock();
		lock.setDepth(0);
		lock.setOwner("me");
		lock.setScope(Scope.EXCLUSIVE_LITERAL);
		lock.setType(Type.WRITE_LITERAL);
		lock.setTimeout(WebdavLock.NEVER);
		resource.lock(lock);
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		
		WebdavIfAction action = new WebdavIfAction();
		action.setDefaultDepth(0);
		action.checkSourceLocks(true);
		action.setNext(mockAction);
		
		try {
			action.perform(req,res);
			fail("Expected Precondition Failed");
		} catch (WebdavException x) {
			assertEquals(Protocol.SC_LOCKED, x.getStatus());
			assertFalse(mockAction.wasActivated());
		}
		
		req = new MockWebdavRequest(factory,"http://localhost/");
		res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		req.setHeader("If", "(<12345>) (Not <no-lock>)");
		
		try {
			action.perform(req,res);
			fail("Expected Precondition Failed");
		} catch (WebdavException x) {
			assertEquals(Protocol.SC_LOCKED, x.getStatus());
			assertFalse(mockAction.wasActivated());
		}
		
		WebdavResource r = factory.resolveResource(INDEX_HTML_URL);
		r.unlock(lock.getToken());
		r.save();
		
	}
	
	public void testMissingLockTokenDestination() throws Exception {
		WebdavResource resource = factory.resolveResource(INDEX_HTML_URL);
		WebdavLock lock = new WebdavLock();
		lock.setDepth(0);
		lock.setOwner("me");
		lock.setScope(Scope.EXCLUSIVE_LITERAL);
		lock.setType(Type.WRITE_LITERAL);
		lock.setTimeout(WebdavLock.NEVER);
		resource.lock(lock);
		
		MockWebdavRequest req = new MockWebdavRequest(factory,"http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(testCollection);
		req.setHeader("destination", INDEX_HTML_URL.toString());
		
		WebdavIfAction action = new WebdavIfAction();
		action.setDefaultDepth(0);
		action.checkDestinationLocks(true);
		action.setNext(mockAction);
		
		try {
			action.perform(req,res);
			fail("Expected Precondition Failed");
		} catch (WebdavException x) {
			assertEquals(Protocol.SC_LOCKED, x.getStatus());
			assertFalse(mockAction.wasActivated());
		}
		
		WebdavResource r = factory.resolveResource(INDEX_HTML_URL);
		r.unlock(lock.getToken());
		r.save();

	}
	
	public void setUp() throws Exception {
		super.setUp();
		mockAction = new MockAction();
        INDEX_HTML_URL = testCollection.dup().append("index.html");
    }
}
