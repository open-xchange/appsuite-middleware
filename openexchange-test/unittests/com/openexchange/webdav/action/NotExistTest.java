package com.openexchange.webdav.action;

import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavLock;

import javax.servlet.http.HttpServletResponse;

public class NotExistTest extends ActionTestCase {
	private MockAction mockAction;
	
	public void testNotExists() throws Exception {
		final WebdavPath NOT_EXIST_URL = new WebdavPath("notExists.txt");
		
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
		final WebdavPath INDEX_HTML_URL = testCollection.dup().append("index.html");
		
		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();
		
		req.setUrl(INDEX_HTML_URL);
		
		AbstractAction action = new WebdavExistsAction();
		action.setNext(mockAction);
		
		action.perform(req,res);
		assertTrue(mockAction.wasActivated());
	}

    public void testLockNullExist() throws Exception{
        final WebdavPath LOCK_NULL = createLockNull();

		MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
		MockWebdavResponse res = new MockWebdavResponse();

		req.setUrl(LOCK_NULL);

		WebdavExistsAction action = new WebdavExistsAction();
        action.setTolerateLockNull(true);

        action.setNext(mockAction);

		action.perform(req,res);
		assertTrue(mockAction.wasActivated());
    }

    public void testLockNullDontExist() throws Exception {
        final WebdavPath LOCK_NULL = createLockNull();
        MockWebdavRequest req = new MockWebdavRequest(factory, "http://localhost/");
        MockWebdavResponse res = new MockWebdavResponse();

        req.setUrl(LOCK_NULL);

        WebdavExistsAction action = new WebdavExistsAction();
       
        action.setNext(mockAction);
        try {
            action.perform(req,res);
            fail("Expected 404 Not Found");
        } catch (WebdavException x) {
            assertEquals(HttpServletResponse.SC_NOT_FOUND, x.getStatus());
            assertFalse(mockAction.wasActivated());
        }

    }

    public void setUp() throws Exception {
		super.setUp();
		this.mockAction = new MockAction();
	}

    private WebdavPath createLockNull() throws WebdavException {
        WebdavPath LOCK_NULL = testCollection.dup().append("lock.txt");
        WebdavLock lock = new WebdavLock();
		lock.setDepth(0);
		lock.setOwner("me");
		lock.setScope(WebdavLock.Scope.EXCLUSIVE_LITERAL);
		lock.setTimeout(WebdavLock.NEVER);
		lock.setType(WebdavLock.Type.WRITE_LITERAL);
        factory.resolveResource(LOCK_NULL).lock(lock);
        return LOCK_NULL;
    }
}